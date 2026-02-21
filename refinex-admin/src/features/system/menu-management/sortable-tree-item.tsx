import React from 'react'
import {useSortable} from '@dnd-kit/sortable'
import {CSS} from '@dnd-kit/utilities'
import {ChevronDown, ChevronRight, GripVertical, Plus} from 'lucide-react'
import {Badge} from '@/components/ui/badge'
import {Button} from '@/components/ui/button'
import type {FlattenedItem, Projected} from './tree-utils'
import {INDENTATION_WIDTH} from './tree-utils'

function toMenuTypeLabel(value?: number): string {
    if (value === 0) return '目录'
    if (value === 1) return '菜单'
    return '-'
}

interface SortableTreeItemProps {
    item: FlattenedItem
    depth: number
    isActive: boolean
    isSelected: boolean
    isExpanded: boolean
    projected: Projected | null
    activeId: number | null
    onSelect: (id?: number) => void
    onToggle: (id?: number) => void
    onAddChild: (parentId: number) => void
    isDragDisabled: boolean
}

export const SortableTreeItem = React.memo(function SortableTreeItem({
    item,
    depth,
    isActive,
    isSelected,
    isExpanded,
    onSelect,
    onToggle,
    onAddChild,
    isDragDisabled,
}: SortableTreeItemProps) {
    const {
        attributes,
        listeners,
        setNodeRef,
        setActivatorNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({
        id: item.id,
        disabled: isDragDisabled || depth === 0,
    })

    const style: React.CSSProperties = {
        transform: CSS.Translate.toString(transform),
        transition,
        opacity: isDragging ? 0.4 : 1,
        paddingLeft: depth * INDENTATION_WIDTH,
    }

    return (
        <div ref={setNodeRef} style={style} {...attributes}>
            <div
                className={`group flex items-center gap-1 rounded-md border border-transparent px-2 py-1.5 ${
                    isSelected ? 'bg-muted text-foreground' : 'hover:bg-muted/40'
                }`}
            >
                {/* 拖拽手柄 - 仅非根节点 */}
                {depth > 0 && !isDragDisabled ? (
                    <button
                        ref={setActivatorNodeRef}
                        {...listeners}
                        className='flex h-6 w-4 shrink-0 cursor-grab items-center justify-center text-muted-foreground hover:text-foreground active:cursor-grabbing'
                        tabIndex={-1}
                    >
                        <GripVertical className='h-3.5 w-3.5'/>
                    </button>
                ) : (
                    <span className='inline-block h-6 w-4 shrink-0'/>
                )}

                {/* 展开/折叠 */}
                {item.hasChildren ? (
                    <Button type='button' variant='ghost' size='icon' className='h-6 w-6'
                            onClick={() => onToggle(item.id)}>
                        {isExpanded ? <ChevronDown className='h-4 w-4'/> : <ChevronRight className='h-4 w-4'/>}
                    </Button>
                ) : (
                    <span className='inline-block h-6 w-6'/>
                )}

                {/* 节点内容 */}
                <button
                    type='button'
                    className='flex min-w-0 flex-1 items-center gap-2 text-left'
                    onClick={() => onSelect(item.id)}
                >
                    <span className='truncate'>{item.menuName || '-'}</span>
                    <Badge variant='outline'>{toMenuTypeLabel(item.menuType)}</Badge>
                </button>

                {/* 添加子菜单 */}
                <Button
                    type='button'
                    variant='ghost'
                    size='icon'
                    className='h-6 w-6 opacity-0 transition-opacity group-hover:opacity-100'
                    onClick={() => onAddChild(item.id)}
                >
                    <Plus className='h-3.5 w-3.5'/>
                </Button>
            </div>
        </div>
    )
})

interface DragOverlayItemProps {
    item: FlattenedItem
}

export function DragOverlayItem({item}: DragOverlayItemProps) {
    return (
        <div className='flex items-center gap-2 rounded-md border bg-background px-3 py-1.5 shadow-lg'>
            <GripVertical className='h-3.5 w-3.5 text-muted-foreground'/>
            <span className='truncate'>{item.menuName || '-'}</span>
            <Badge variant='outline'>{toMenuTypeLabel(item.menuType)}</Badge>
        </div>
    )
}
