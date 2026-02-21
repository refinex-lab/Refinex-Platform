import type {MenuTreeNode, MenuReorderItem} from '@/features/system/api'

export const INDENTATION_WIDTH = 14

export interface FlattenedItem {
    id: number
    parentId: number
    depth: number
    sort: number
    menuName: string
    menuCode: string
    menuType: number
    isBuiltin: number
    hasChildren: boolean
}

export interface Projected {
    depth: number
    parentId: number
}

type TreeNode = MenuTreeNode & { children?: TreeNode[] }

/**
 * 将嵌套树扁平化为可排序列表
 * 只展开 expandedIds 中包含的节点
 */
export function flattenTree(
    nodes: TreeNode[],
    expandedIds: Set<number>,
    parentId = 0,
    depth = 0,
): FlattenedItem[] {
    const result: FlattenedItem[] = []
    for (const node of nodes) {
        const id = node.id ?? 0
        result.push({
            id,
            parentId,
            depth,
            sort: node.sort ?? 0,
            menuName: node.menuName ?? '',
            menuCode: node.menuCode ?? '',
            menuType: node.menuType ?? 0,
            isBuiltin: node.isBuiltin ?? 0,
            hasChildren: Boolean(node.children?.length),
        })
        if (node.children?.length && expandedIds.has(id)) {
            result.push(...flattenTree(node.children, expandedIds, id, depth + 1))
        }
    }
    return result
}

/**
 * 移除指定节点的所有后代（拖拽时后代跟随父节点移动）
 */
export function removeChildrenOf(items: FlattenedItem[], ids: number[]): FlattenedItem[] {
    const excludeParentIds = new Set(ids)
    const result: FlattenedItem[] = []
    for (const item of items) {
        if (excludeParentIds.has(item.parentId)) {
            excludeParentIds.add(item.id)
        } else {
            result.push(item)
        }
    }
    return result
}

/**
 * 根据拖拽偏移量计算目标层级和父节点
 */
export function getProjection(
    items: FlattenedItem[],
    activeId: number,
    overId: number,
    dragOffsetX: number,
    indentationWidth: number,
): Projected | null {
    const activeIndex = items.findIndex((i) => i.id === activeId)
    const overIndex = items.findIndex((i) => i.id === overId)
    if (activeIndex === -1 || overIndex === -1) return null

    const activeItem = items[activeIndex]
    // 基于水平偏移计算新的 depth
    const projectedDepth = activeItem.depth + Math.round(dragOffsetX / indentationWidth)

    // 计算允许的 depth 范围
    const newItems = arrayMoveItems(items, activeIndex, overIndex)
    const newIndex = newItems.findIndex((i) => i.id === activeId)

    // 最大 depth：前一个节点的 depth + 1（可以成为前一个节点的子节点）
    const prevItem = newItems[newIndex - 1]
    const maxDepth = prevItem ? prevItem.depth + 1 : 0

    // 最小 depth：下一个非后代节点的 depth（不能比后续兄弟更浅）
    let minDepth = 1 // 禁止拖到根级（depth 0）
    const nextItem = newItems[newIndex + 1]
    if (nextItem) {
        minDepth = Math.max(minDepth, nextItem.depth)
    }

    const depth = Math.min(Math.max(projectedDepth, minDepth), maxDepth)

    // 根据 depth 找到父节点
    const parentId = findParentId(newItems, newIndex, depth)

    return {depth, parentId}
}

function findParentId(items: FlattenedItem[], index: number, depth: number): number {
    if (depth === 0) return 0
    // 向上查找第一个 depth 比当前小 1 的节点
    for (let i = index - 1; i >= 0; i--) {
        if (items[i].depth === depth - 1) {
            return items[i].id
        }
    }
    return 0
}

function arrayMoveItems(items: FlattenedItem[], from: number, to: number): FlattenedItem[] {
    const result = [...items]
    const [removed] = result.splice(from, 1)
    result.splice(to, 0, removed)
    return result
}

/**
 * 从扁平化列表构建排序 payload（只包含变化的节点）
 * 对比原始树和新的扁平列表，计算每个节点的新 parentId 和 sort
 */
export function buildReorderPayload(
    originalFlat: FlattenedItem[],
    newFlat: FlattenedItem[],
): MenuReorderItem[] {
    // 构建原始数据的 map
    const originalMap = new Map<number, { parentId: number; sort: number }>()
    for (const item of originalFlat) {
        originalMap.set(item.id, {parentId: item.parentId, sort: item.sort})
    }

    // 按 parentId 分组，计算每个节点在新列表中的 sort
    const parentGroups = new Map<number, number[]>()
    for (const item of newFlat) {
        const group = parentGroups.get(item.parentId) ?? []
        group.push(item.id)
        parentGroups.set(item.parentId, group)
    }

    // 构建 parentId map
    const newParentMap = new Map<number, number>()
    for (const item of newFlat) {
        newParentMap.set(item.id, item.parentId)
    }

    const payload: MenuReorderItem[] = []
    for (const [parentId, childIds] of parentGroups) {
        for (let i = 0; i < childIds.length; i++) {
            const menuId = childIds[i]
            const newSort = i
            const newParent = parentId
            const original = originalMap.get(menuId)
            if (!original || original.parentId !== newParent || original.sort !== newSort) {
                payload.push({menuId, parentId: newParent, sort: newSort})
            }
        }
    }

    return payload
}

/**
 * 将扁平列表中某个节点的 depth 更新后，重新计算所有节点的 parentId
 */
export function updateFlatItemsAfterDrop(
    items: FlattenedItem[],
    activeId: number,
    overId: number,
    projected: Projected,
): FlattenedItem[] {
    const activeIndex = items.findIndex((i) => i.id === activeId)
    const overIndex = items.findIndex((i) => i.id === overId)
    if (activeIndex === -1 || overIndex === -1) return items

    const newItems = arrayMoveItems(items, activeIndex, overIndex)
    const newIndex = newItems.findIndex((i) => i.id === activeId)

    // 更新被拖拽节点的 depth 和 parentId
    newItems[newIndex] = {
        ...newItems[newIndex],
        depth: projected.depth,
        parentId: projected.parentId,
    }

    // 重新计算后续节点的 parentId（被拖拽节点的后代已被移除，不需要处理）
    return newItems
}
