import {useCallback, useEffect, useMemo, useRef, useState} from 'react'
import {zodResolver} from '@hookform/resolvers/zod'
import {closestCenter, DndContext, DragOverlay, PointerSensor, useSensor, useSensors} from '@dnd-kit/core'
import type {DragEndEvent, DragMoveEvent, DragOverEvent, DragStartEvent} from '@dnd-kit/core'
import {SortableContext, verticalListSortingStrategy} from '@dnd-kit/sortable'
import {Check, Loader2, Pencil, Plus, RefreshCw, Trash2, X,} from 'lucide-react'
import {useForm} from 'react-hook-form'
import {toast} from 'sonner'
import {z} from 'zod'
import {ConfirmDialog} from '@/components/confirm-dialog'
import {ConfigDrawer} from '@/components/config-drawer'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {Badge} from '@/components/ui/badge'
import {Button} from '@/components/ui/button'
import {Card, CardContent} from '@/components/ui/card'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage,} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {ScrollArea} from '@/components/ui/scroll-area'
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from '@/components/ui/select'
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table'
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs'
import {ThemeSwitch} from '@/components/theme-switch'
import {
    createMenu,
    createMenuOp,
    deleteMenu,
    deleteMenuOp,
    getMenu,
    getMenuTree,
    listMenuOps,
    listOpDefinitions,
    listSystems,
    reorderMenus,
    type Menu,
    type MenuCreateRequest,
    type MenuOpCreateRequest,
    type MenuOpManage,
    type MenuOpUpdateRequest,
    type MenuTreeNode,
    type MenuUpdateRequest,
    type OpDefinition,
    type SystemDefinition,
    updateMenu,
    updateMenuOp,
} from '@/features/system/api'
import {toOptionalNumber, toOptionalString} from '@/features/system/common'
import {PageToolbar} from '@/features/system/components/page-toolbar'
import {handleServerError} from '@/lib/handle-server-error'
import {useAuthStore} from '@/stores/auth-store'
import {SortableTreeItem, DragOverlayItem} from './sortable-tree-item'
import {
    flattenTree,
    removeChildrenOf,
    getProjection,
    buildReorderPayload,
    updateFlatItemsAfterDrop,
    INDENTATION_WIDTH,
    type FlattenedItem,
    type Projected,
} from './tree-utils'

const MENU_OP_PAGE_SIZE = 10

const menuFormSchema = z.object({
    systemId: z.string().min(1, '请选择所属系统'),
    menuCode: z.string().trim().max(64, '菜单编码最长 64 位').optional(),
    menuName: z.string().trim().min(1, '菜单名称不能为空').max(128, '菜单名称最长 128 位'),
    menuType: z.enum(['0', '1']),
    path: z.string().trim().max(255, '路由路径最长 255 位').optional(),
    icon: z.string().trim().max(64, '图标最长 64 位').optional(),
    visible: z.enum(['0', '1']),
    isFrame: z.enum(['0', '1']),
    status: z.enum(['1', '2']),
    sort: z.string().trim().max(6, '排序值过大').optional(),
})

type MenuFormValues = z.infer<typeof menuFormSchema>

interface InlineMenuOp {
    id?: number
    opCode: string
    opName: string
    status: number
    sort: number
    isNew?: boolean
}

const DEFAULT_MENU_FORM: MenuFormValues = {
    systemId: '',
    menuCode: '',
    menuName: '',
    menuType: '1',
    path: '',
    icon: '',
    visible: '1',
    isFrame: '0',
    status: '1',
    sort: '0',
}

type MenuTreeNodeUI = MenuTreeNode & { children?: MenuTreeNodeUI[] }

function toStatusLabel(value?: number): string {
    if (value === 1) return '启用'
    if (value === 2) return '停用'
    return '-'
}

function flattenMenuIds(nodes: MenuTreeNodeUI[]): number[] {
    const ids: number[] = []
    const traverse = (items: MenuTreeNodeUI[]) => {
        items.forEach((item) => {
            if (item.id) ids.push(item.id)
            if (item.children?.length) traverse(item.children)
        })
    }
    traverse(nodes)
    return ids
}

function filterMenuTree(nodes: MenuTreeNodeUI[], keyword: string): MenuTreeNodeUI[] {
    const normalized = keyword.trim().toLowerCase()
    if (!normalized) return nodes

    const filterNode = (node: MenuTreeNodeUI): MenuTreeNodeUI | null => {
        const children = (node.children ?? [])
            .map((child) => filterNode(child))
            .filter((child): child is MenuTreeNodeUI => child !== null)

        const text = `${node.menuName ?? ''} ${node.menuCode ?? ''}`.toLowerCase()
        if (text.includes(normalized) || children.length > 0) {
            return {...node, children}
        }
        return null
    }

    return nodes
        .map((node) => filterNode(node))
        .filter((node): node is MenuTreeNodeUI => node !== null)
}

export function MenuManagementPage() {
    const [menuKeyword, setMenuKeyword] = useState('')
    const [menuTree, setMenuTree] = useState<MenuTreeNodeUI[]>([])
    const [menuTreeLoading, setMenuTreeLoading] = useState(false)
    const [expandedNodeIds, setExpandedNodeIds] = useState<Set<number>>(new Set())
    const [selectedMenuId, setSelectedMenuId] = useState<number | undefined>(undefined)
    const [selectedMenu, setSelectedMenu] = useState<Menu | null>(null)
    const [loadingMenuDetail, setLoadingMenuDetail] = useState(false)
    const [savingMenuDetail, setSavingMenuDetail] = useState(false)

    const [menuDialogOpen, setMenuDialogOpen] = useState(false)
    const [menuDialogParentId, setMenuDialogParentId] = useState<number>(0)
    const [savingNewMenu, setSavingNewMenu] = useState(false)
    const [deletingMenuOpen, setDeletingMenuOpen] = useState(false)
    const [deletingMenuLoading, setDeletingMenuLoading] = useState(false)

    const [menuOps, setMenuOps] = useState<MenuOpManage[]>([])
    const [menuOpsTotal, setMenuOpsTotal] = useState(0)
    const [menuOpsLoading, setMenuOpsLoading] = useState(false)
    const [menuOpsQuery, setMenuOpsQuery] = useState({currentPage: 1, pageSize: MENU_OP_PAGE_SIZE})

    const [editingRowId, setEditingRowId] = useState<number | 'new' | null>(null)
    const [editingRow, setEditingRow] = useState<InlineMenuOp | null>(null)
    const [savingMenuOp, setSavingMenuOp] = useState(false)
    const [deletingMenuOp, setDeletingMenuOp] = useState<MenuOpManage | null>(null)
    const [deletingMenuOpLoading, setDeletingMenuOpLoading] = useState(false)

    const [opDefinitions, setOpDefinitions] = useState<OpDefinition[]>([])
    const [systems, setSystems] = useState<SystemDefinition[]>([])

    // DnD state
    const [activeId, setActiveId] = useState<number | null>(null)
    const [overId, setOverId] = useState<number | null>(null)
    const [offsetLeft, setOffsetLeft] = useState(0)
    const previousTreeRef = useRef<MenuTreeNodeUI[]>([])

    const sensors = useSensors(
        useSensor(PointerSensor, {activationConstraint: {distance: 5}})
    )

    const menuDetailForm = useForm<MenuFormValues>({
        resolver: zodResolver(menuFormSchema),
        defaultValues: DEFAULT_MENU_FORM,
        mode: 'onChange',
    })

    const menuCreateForm = useForm<MenuFormValues>({
        resolver: zodResolver(menuFormSchema),
        defaultValues: DEFAULT_MENU_FORM,
        mode: 'onChange',
    })

    const filteredTree = useMemo(() => filterMenuTree(menuTree, menuKeyword), [menuTree, menuKeyword])

    const isDragDisabled = menuKeyword.trim().length > 0

    // 扁平化树用于 DnD
    const flattenedItems = useMemo(() => {
        const flat = flattenTree(filteredTree, expandedNodeIds)
        if (activeId) {
            return removeChildrenOf(flat, [activeId])
        }
        return flat
    }, [filteredTree, expandedNodeIds, activeId])

    const sortedIds = useMemo(() => flattenedItems.map((i) => i.id), [flattenedItems])

    // 计算投影位置
    const projected = useMemo(() => {
        if (activeId && overId) {
            return getProjection(flattenedItems, activeId, overId, offsetLeft, INDENTATION_WIDTH)
        }
        return null
    }, [flattenedItems, activeId, overId, offsetLeft])

    const activeItem = useMemo(() => {
        if (!activeId) return null
        return flattenedItems.find((i) => i.id === activeId) ?? null
    }, [flattenedItems, activeId])

    const parentMenuName = useMemo(() => {
        if (menuDialogParentId === 0) return '根菜单'
        const id = menuDialogParentId
        const queue = [...menuTree]
        while (queue.length > 0) {
            const current = queue.shift()
            if (!current) break
            if (current.id === id) {
                return current.menuName || '当前菜单'
            }
            if (current.children?.length) queue.push(...current.children)
        }
        return '当前菜单'
    }, [menuDialogParentId, menuTree])

    async function loadMenuTree() {
        setMenuTreeLoading(true)
        try {
            const estabId = useAuthStore.getState().auth.user?.estabId
            const tree = await getMenuTree({ estabId })
            const normalized = tree as MenuTreeNodeUI[]
            setMenuTree(normalized)
            const allIds = flattenMenuIds(normalized)
            setExpandedNodeIds(new Set(allIds))
            setSelectedMenuId((prev) => {
                if (!prev) return normalized[0]?.id
                return allIds.includes(prev) ? prev : normalized[0]?.id
            })
        } catch (error) {
            handleServerError(error)
        } finally {
            setMenuTreeLoading(false)
        }
    }

    async function loadMenuDetail(menuId: number) {
        setLoadingMenuDetail(true)
        try {
            const detail = await getMenu(menuId)
            setSelectedMenu(detail)
            menuDetailForm.reset({
                systemId: String(detail.systemId ?? ''),
                menuCode: detail.menuCode ?? '',
                menuName: detail.menuName ?? '',
                menuType: String(detail.menuType ?? 1) as '0' | '1',
                path: detail.path ?? '',
                icon: detail.icon ?? '',
                visible: String(detail.visible ?? 1) as '0' | '1',
                isFrame: String(detail.isFrame ?? 0) as '0' | '1',
                status: String(detail.status ?? 1) as '1' | '2',
                sort: String(detail.sort ?? 0),
            })
        } catch (error) {
            handleServerError(error)
        } finally {
            setLoadingMenuDetail(false)
        }
    }

    async function loadMenuOps(menuId: number, currentPage: number, pageSize: number) {
        setMenuOpsLoading(true)
        try {
            const pageData = await listMenuOps(menuId, {currentPage, pageSize})
            setMenuOps(pageData.data ?? [])
            setMenuOpsTotal(pageData.total ?? 0)
        } catch (error) {
            handleServerError(error)
        } finally {
            setMenuOpsLoading(false)
        }
    }

    async function loadOpDefinitions() {
        try {
            const ops = await listOpDefinitions()
            setOpDefinitions(ops)
        } catch (error) {
            handleServerError(error)
        }
    }

    async function loadSystems() {
        try {
            const pageData = await listSystems({currentPage: 1, pageSize: 999, status: 1})
            setSystems(pageData.data ?? [])
        } catch (error) {
            handleServerError(error)
        }
    }

    useEffect(() => {
        void loadMenuTree()
        void loadOpDefinitions()
        void loadSystems()
    }, [])

    useEffect(() => {
        if (!selectedMenuId) {
            setSelectedMenu(null)
            return
        }
        void loadMenuDetail(selectedMenuId)
        setMenuOpsQuery({currentPage: 1, pageSize: MENU_OP_PAGE_SIZE})
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedMenuId])

    useEffect(() => {
        if (!selectedMenuId) {
            setMenuOps([])
            setMenuOpsTotal(0)
            return
        }
        void loadMenuOps(selectedMenuId, menuOpsQuery.currentPage, menuOpsQuery.pageSize)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedMenuId, menuOpsQuery])

    function toggleNode(nodeId?: number) {
        if (!nodeId) return
        setExpandedNodeIds((prev) => {
            const next = new Set(prev)
            if (next.has(nodeId)) next.delete(nodeId)
            else next.add(nodeId)
            return next
        })
    }

    // DnD handlers
    const handleDragStart = useCallback(({active}: DragStartEvent) => {
        setActiveId(active.id as number)
        setOverId(active.id as number)
        setOffsetLeft(0)
        previousTreeRef.current = menuTree
    }, [menuTree])

    const handleDragMove = useCallback(({delta}: DragMoveEvent) => {
        setOffsetLeft(delta.x)
    }, [])

    const handleDragOver = useCallback(({over}: DragOverEvent) => {
        setOverId((over?.id as number) ?? null)
    }, [])

    const handleDragEnd = useCallback(async ({active, over}: DragEndEvent) => {
        resetDragState()
        if (!over || active.id === over.id || !projected) return

        const originalFlat = flattenTree(filteredTree, expandedNodeIds)
        const newFlat = updateFlatItemsAfterDrop(
            flattenedItems,
            active.id as number,
            over.id as number,
            projected,
        )

        const payload = buildReorderPayload(originalFlat, newFlat)
        if (payload.length === 0) return

        // 乐观更新：重新加载树
        try {
            await reorderMenus(payload)
            toast.success('菜单排序已更新')
            await loadMenuTree()
        } catch (error) {
            handleServerError(error)
            setMenuTree(previousTreeRef.current)
        }
    }, [projected, flattenedItems, filteredTree, expandedNodeIds])

    const handleDragCancel = useCallback(() => {
        resetDragState()
    }, [])

    function resetDragState() {
        setActiveId(null)
        setOverId(null)
        setOffsetLeft(0)
    }

    function openCreateMenuDialog(parentId: number) {
        setMenuDialogParentId(parentId)
        menuCreateForm.reset(DEFAULT_MENU_FORM)
        setMenuDialogOpen(true)
    }

    function closeCreateMenuDialog() {
        setMenuDialogOpen(false)
        menuCreateForm.reset(DEFAULT_MENU_FORM)
    }

    async function submitCreateMenu(values: MenuFormValues) {
        setSavingNewMenu(true)
        try {
            const payload: MenuCreateRequest = {
                systemId: Number(values.systemId),
                parentId: menuDialogParentId,
                menuName: values.menuName.trim(),
                menuType: Number(values.menuType),
                path: toOptionalString(values.path),
                icon: toOptionalString(values.icon),
                visible: Number(values.visible),
                isFrame: Number(values.isFrame),
                status: Number(values.status),
                sort: toOptionalNumber(values.sort),
            }
            const created = await createMenu(payload)
            toast.success('菜单创建成功')
            closeCreateMenuDialog()
            await loadMenuTree()
            if (created.id) {
                setSelectedMenuId(created.id)
            }
        } catch (error) {
            handleServerError(error)
        } finally {
            setSavingNewMenu(false)
        }
    }

    async function submitUpdateMenu(values: MenuFormValues) {
        if (!selectedMenuId) return
        setSavingMenuDetail(true)
        try {
            const payload: MenuUpdateRequest = {
                systemId: Number(values.systemId),
                parentId: selectedMenu?.parentId ?? 0,
                menuCode: values.menuCode?.trim() || selectedMenu?.menuCode || '',
                menuName: values.menuName.trim(),
                menuType: Number(values.menuType),
                path: toOptionalString(values.path),
                icon: toOptionalString(values.icon),
                visible: Number(values.visible),
                isFrame: Number(values.isFrame),
                status: Number(values.status),
                sort: toOptionalNumber(values.sort),
            }
            await updateMenu(selectedMenuId, payload)
            toast.success('菜单更新成功')
            await loadMenuTree()
            await loadMenuDetail(selectedMenuId)
        } catch (error) {
            handleServerError(error)
        } finally {
            setSavingMenuDetail(false)
        }
    }

    async function confirmDeleteMenu() {
        if (!selectedMenuId) return
        setDeletingMenuLoading(true)
        try {
            await deleteMenu(selectedMenuId)
            toast.success('菜单删除成功')
            setDeletingMenuOpen(false)
            await loadMenuTree()
        } catch (error) {
            handleServerError(error)
        } finally {
            setDeletingMenuLoading(false)
        }
    }

    function openCreateMenuOpDialog() {
        if (!selectedMenuId) {
            toast.error('请先选择菜单')
            return
        }
        setEditingRowId('new')
        setEditingRow({opCode: '', opName: '', status: 1, sort: 0, isNew: true})
    }

    function startEditMenuOp(item: MenuOpManage) {
        setEditingRowId(item.id ?? null)
        setEditingRow({
            id: item.id,
            opCode: item.opCode ?? '',
            opName: item.opName ?? '',
            status: item.status ?? 1,
            sort: item.sort ?? 0,
        })
    }

    function cancelEditMenuOp() {
        setEditingRowId(null)
        setEditingRow(null)
    }

    async function saveEditingRow() {
        if (!selectedMenuId || !editingRow) return
        if (!editingRow.opCode) {
            toast.error('请选择操作类型')
            return
        }
        setSavingMenuOp(true)
        try {
            if (editingRow.isNew) {
                const payload: MenuOpCreateRequest = {
                    opCode: editingRow.opCode,
                    opName: editingRow.opName,
                    status: editingRow.status,
                    sort: editingRow.sort,
                }
                await createMenuOp(selectedMenuId, payload)
                toast.success('菜单操作创建成功')
            } else if (editingRow.id) {
                const payload: MenuOpUpdateRequest = {
                    opCode: editingRow.opCode,
                    opName: editingRow.opName,
                    status: editingRow.status,
                    sort: editingRow.sort,
                }
                await updateMenuOp(editingRow.id, payload)
                toast.success('菜单操作更新成功')
            }
            cancelEditMenuOp()
            await loadMenuOps(selectedMenuId, menuOpsQuery.currentPage, menuOpsQuery.pageSize)
        } catch (error) {
            handleServerError(error)
        } finally {
            setSavingMenuOp(false)
        }
    }

    async function confirmDeleteMenuOp() {
        if (!deletingMenuOp?.id || !selectedMenuId) return
        setDeletingMenuOpLoading(true)
        try {
            await deleteMenuOp(deletingMenuOp.id)
            toast.success('菜单操作删除成功')
            setDeletingMenuOp(null)
            await loadMenuOps(selectedMenuId, menuOpsQuery.currentPage, menuOpsQuery.pageSize)
        } catch (error) {
            handleServerError(error)
        } finally {
            setDeletingMenuOpLoading(false)
        }
    }

    function handleMenuOpPageChange(page: number) {
        setMenuOpsQuery((prev) => ({...prev, currentPage: page}))
    }

    function handleMenuOpPageSizeChange(size: number) {
        setMenuOpsQuery((prev) => ({...prev, currentPage: 1, pageSize: size}))
    }

    return (
        <>
            <Header>
                <Search/>
                <div className='ms-auto flex items-center gap-4'>
                    <ThemeSwitch/>
                    <ConfigDrawer/>
                    <ProfileDropdown/>
                </div>
            </Header>

            <Main fixed fluid>
                <div className='grid h-full gap-2 xl:grid-cols-[340px_1fr]'>
                    <Card className='py-3 gap-3'>
                        <CardContent className='space-y-3'>
                            <div className='flex items-center gap-2'>
                                <Input
                                    value={menuKeyword}
                                    placeholder='按菜单名称或编码检索'
                                    onChange={(event) => setMenuKeyword(event.target.value)}
                                />
                                <Button type='button' variant='outline' size='icon'
                                        onClick={() => loadMenuTree()}>
                                    <RefreshCw className='h-4 w-4'/>
                                </Button>
                            </div>

                            <ScrollArea className='h-[calc(100vh-200px)] rounded-md border border-border/70 p-2'>
                                {menuTreeLoading ? (
                                    <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                                        <Loader2 className='h-4 w-4 animate-spin'/>
                                        正在加载菜单树...
                                    </div>
                                ) : filteredTree.length === 0 ? (
                                    <div className='py-8 text-center text-sm text-muted-foreground'>暂无菜单定义</div>
                     ) : (
                                    <DndContext
                                        sensors={sensors}
                                        collisionDetection={closestCenter}
                                        onDragStart={handleDragStart}
                                        onDragMove={handleDragMove}
                                        onDragOver={handleDragOver}
                                        onDragEnd={handleDragEnd}
                                        onDragCancel={handleDragCancel}
                                    >
                                        <SortableContext items={sortedIds} strategy={verticalListSortingStrategy}>
                                            <div className='space-y-1'>
                                                {flattenedItems.map((item) => (
                                                    <SortableTreeItem
                                                        key={item.id}
                                                        item={item}
                                                        depth={item.id === activeId && projected ? projected.depth : item.depth}
                                                        isActive={item.id === activeId}
                                                        isSelected={item.id === selectedMenuId}
                                                        isExpanded={expandedNodeIds.has(item.id)}
                                                        projected={projected}
                                                        activeId={activeId}
                                                        onSelect={setSelectedMenuId}
                                                        onToggle={toggleNode}
                                                        onAddChild={openCreateMenuDialog}
                                                        isDragDisabled={isDragDisabled}
                                                    />
                                                ))}
                                            </div>
                                        </SortableContext>
                                        <DragOverlay dropAnimation={{duration: 200, easing: 'ease'}}>
                                            {activeId && activeItem ? <DragOverlayItem item={activeItem}/> : null}
                                        </DragOverlay>
                                    </DndContext>
                                )}
                            </ScrollArea>
                        </CardContent>
                    </Card>

                    <Card className='py-3 gap-3'>
                        {/*<CardHeader className='pb-0'>*/}
                        {/*    <CardTitle className='text-base'>菜单详情与操作</CardTitle>*/}
                        {/*</CardHeader>*/}
                        <CardContent className='space-y-4'>
                            {!selectedMenuId ? (
                                <div
                                    className='py-16 text-center text-muted-foreground'>请先在左侧选择一个菜单节点</div>
                            ) : loadingMenuDetail ? (
                                <div className='flex items-center justify-center gap-2 py-16 text-muted-foreground'>
                                    <Loader2 className='h-4 w-4 animate-spin'/>
                                    正在加载菜单详情...
                                </div>
                            ) : (
                                <Tabs defaultValue='basic' className='space-y-3'>
                                    <TabsList className={`grid w-full ${selectedMenu?.menuType === 0 ? 'grid-cols-1' : 'grid-cols-2'}`}>
                                        <TabsTrigger value='basic'>基本信息</TabsTrigger>
                                        {selectedMenu?.menuType !== 0 && (
                                            <TabsTrigger value='ops'>菜单操作</TabsTrigger>
                                        )}
                                    </TabsList>

                                    <TabsContent value='basic'>
                                        <Form {...menuDetailForm}>
                                            <form onSubmit={menuDetailForm.handleSubmit(submitUpdateMenu)}
                                                  className='space-y-4'>
                                                <div className='grid gap-4 md:grid-cols-4'>
                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='systemId'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>所属系统</FormLabel>
                                                                <Select value={field.value}
                                                                        onValueChange={field.onChange}>
                                                                    <FormControl>
                                                                        <SelectTrigger className='w-full'>
                                                                            <SelectValue placeholder='请选择所属系统'/>
                                                                        </SelectTrigger>
                                                                    </FormControl>
                                                                    <SelectContent>
                                                                        {systems.map((sys) => (
                                                                            <SelectItem key={sys.id} value={String(sys.id)}>{sys.systemName}</SelectItem>
                                                                        ))}
                                                                    </SelectContent>
                                                                </Select>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />

                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='menuName'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>菜单名称</FormLabel>
                                                                <FormControl>
                                                                    <Input {...field} placeholder='请输入菜单名称'/>
                                                                </FormControl>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />

                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='menuType'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>菜单类型</FormLabel>
                                                                <Select value={field.value}
                                                                        onValueChange={field.onChange}>
                                                                    <FormControl>
                                                                        <SelectTrigger className='w-full'>
                                                                            <SelectValue placeholder='请选择类型'/>
                                                                        </SelectTrigger>
                                                                    </FormControl>
                                                                    <SelectContent>
                                                                        <SelectItem value='0'>目录</SelectItem>
                                                                        <SelectItem value='1'>菜单</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />

                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='status'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>状态</FormLabel>
                                                                <Select value={field.value}
                                                                        onValueChange={field.onChange}>
                                                                    <FormControl>
                                                                        <SelectTrigger className='w-full'>
                                                                            <SelectValue placeholder='请选择状态'/>
                                                                        </SelectTrigger>
                                                                    </FormControl>
                                                                    <SelectContent>
                                                                        <SelectItem value='1'>启用</SelectItem>
                                                                        <SelectItem value='2'>停用</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />
                                                </div>

                                                <div className='grid gap-4 md:grid-cols-1'>
                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='path'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>路由路径</FormLabel>
                                                                <FormControl>
                                                                    <Input {...field}
                                                                           placeholder='例如：/system-management/menus'/>
                                                                </FormControl>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />
                                                </div>

                                                <div className='grid gap-4 md:grid-cols-2'>
                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='icon'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>图标</FormLabel>
                                                                <FormControl>
                                                                    <Input {...field} placeholder='例如：Settings'/>
                                                                </FormControl>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />

                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='sort'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>排序</FormLabel>
                                                                <FormControl>
                                                                    <Input {...field} placeholder='默认 0'/>
                                                                </FormControl>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />
                                                </div>

                                                <div className='grid gap-4 md:grid-cols-3'>
                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='visible'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>可见性</FormLabel>
                                                                <Select value={field.value}
                                                                        onValueChange={field.onChange}>
                                                                    <FormControl>
                                                                        <SelectTrigger className='w-full'>
                                                                            <SelectValue placeholder='请选择可见性'/>
                                                                        </SelectTrigger>
                                                                    </FormControl>
                                                                    <SelectContent>
                                                                        <SelectItem value='1'>可见</SelectItem>
                                                                        <SelectItem value='0'>隐藏</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />

                                                    <FormField
                                                        control={menuDetailForm.control}
                                                        name='isFrame'
                                                        render={({field}) => (
                                                            <FormItem>
                                                                <FormLabel>外链</FormLabel>
                                                                <Select value={field.value}
                                                                        onValueChange={field.onChange}>
                                                                    <FormControl>
                                                                        <SelectTrigger className='w-full'>
                                                                            <SelectValue placeholder='请选择外链状态'/>
                                                                        </SelectTrigger>
                                                                    </FormControl>
                                                                    <SelectContent>
                                                                        <SelectItem value='0'>否</SelectItem>
                                                                        <SelectItem value='1'>是</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                                <FormMessage/>
                                                            </FormItem>
                                                        )}
                                                    />
                                                </div>

                                                <div className='flex items-center justify-end gap-2'>
                                                    <Button
                                                        type='button'
                                                        variant='outline'
                                                        className='gap-2 text-destructive hover:text-destructive'
                                                        onClick={() => setDeletingMenuOpen(true)}
                                                    >
                                                        <Trash2 className='h-4 w-4'/>
                                                        删除菜单
                                                    </Button>
                                                    <Button type='submit' disabled={savingMenuDetail}>
                                                        {savingMenuDetail ?
                                                            <Loader2 className='mr-2 h-4 w-4 animate-spin'/> : null}
                                                        保存菜单
                                                    </Button>
                                                </div>
                                            </form>
                                        </Form>
                                    </TabsContent>

                                    <TabsContent value='ops' className='space-y-3'>
                                        <div className='flex items-center justify-end'>
                                            <Button type='button' className='gap-2' onClick={openCreateMenuOpDialog}
                                                    disabled={editingRowId !== null}>
                                                <Plus className='h-4 w-4'/>
                                                新建操作
                                            </Button>
                                        </div>

                                        <div className='overflow-hidden rounded-md border border-border/90'>
                                            <Table
                                                className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                                                <TableHeader>
                                                    <TableRow className='bg-muted/30 hover:bg-muted/30'>
                                                        <TableHead>操作编码</TableHead>
                                                        <TableHead>操作名称</TableHead>
                                                        <TableHead className='w-[100px]'>状态</TableHead>
                                                        <TableHead className='w-[80px]'>排序</TableHead>
                                                        <TableHead className='w-[120px] text-center'>操作</TableHead>
                                                    </TableRow>
                                                </TableHeader>
                                                <TableBody>
                                                    {editingRowId === 'new' && editingRow && (
                                                        <TableRow>
                                                            <TableCell>
                                                                <Select value={editingRow.opCode} onValueChange={(value) => {
                                                                    const matched = opDefinitions.find((op) => op.opCode === value)
                                                                    setEditingRow({...editingRow, opCode: value, opName: matched?.opName ?? ''})
                                                                }}>
                                                                    <SelectTrigger className='w-full'>
                                                                        <SelectValue placeholder='选择操作'/>
                                                                    </SelectTrigger>
                                                                    <SelectContent>
                                                                        {opDefinitions.map((op) => (
                                                                            <SelectItem key={op.opCode} value={op.opCode ?? ''}>
                                                                                {op.opName}（{op.opCode}）
                                                                            </SelectItem>
                                                                        ))}
                                                                    </SelectContent>
                                                                </Select>
                                                            </TableCell>
                                                            <TableCell>
                                                                <span className='text-muted-foreground'>{editingRow.opName || '-'}</span>
                                                            </TableCell>
                                                            <TableCell>
                                                                <Select value={String(editingRow.status)} onValueChange={(v) => setEditingRow({...editingRow, status: Number(v)})}>
                                                                    <SelectTrigger className='w-full'>
                                                                        <SelectValue/>
                                                                    </SelectTrigger>
                                                                    <SelectContent>
                                                                        <SelectItem value='1'>启用</SelectItem>
                                                                        <SelectItem value='2'>停用</SelectItem>
                                                                    </SelectContent>
                                                                </Select>
                                                            </TableCell>
                                                            <TableCell>
                                                                <Input value={String(editingRow.sort)} onChange={(e) => setEditingRow({...editingRow, sort: Number(e.target.value) || 0})} className='w-full'/>
                                                            </TableCell>
                                                            <TableCell className='text-center'>
                                                                <div className='flex items-center justify-center gap-1'>
                                                                    <Button type='button' variant='ghost' size='icon' className='h-7 w-7' disabled={savingMenuOp} onClick={saveEditingRow}>
                                                                        {savingMenuOp ? <Loader2 className='h-3.5 w-3.5 animate-spin'/> : <Check className='h-3.5 w-3.5'/>}
                                                                    </Button>
                                                                    <Button type='button' variant='ghost' size='icon' className='h-7 w-7' onClick={cancelEditMenuOp}>
                                                                        <X className='h-3.5 w-3.5'/>
                                                                    </Button>
                                                                </div>
                                                            </TableCell>
                                                        </TableRow>
                                                    )}
                                                    {menuOpsLoading ? (
                                                        <TableRow>
                                                            <TableCell colSpan={5}>
                                                                <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                                                                    <Loader2 className='h-4 w-4 animate-spin'/>
                                                                    正在加载菜单操作...
                                                                </div>
                                                            </TableCell>
                                                        </TableRow>
                                                    ) : menuOps.length === 0 && editingRowId !== 'new' ? (
                                                        <TableRow>
                                                            <TableCell colSpan={5} className='py-6 text-center text-muted-foreground'>
                                                                当前菜单暂无操作定义
                                                            </TableCell>
                                                        </TableRow>
                                                    ) : (
                                                        menuOps.map((item) => {
                                                            const isEditing = editingRowId === item.id && editingRow
                                                            return (
                                                                <TableRow key={item.id}>
                                                                    <TableCell>
                                                                        {isEditing ? (
                                                                            <span className='text-muted-foreground'>{editingRow.opCode}</span>
                                                                        ) : (
                                                                            item.opCode || '-'
                                                                        )}
                                                                    </TableCell>
                                                                    <TableCell>
                                                                        {isEditing ? (
                                                                            <span className='text-muted-foreground'>{editingRow.opName || '-'}</span>
                                                                        ) : (
                                                                            item.opName || '-'
                                                                        )}
                                                                    </TableCell>
                                                                    <TableCell>
                                                                        {isEditing ? (
                                                                            <Select value={String(editingRow.status)} onValueChange={(v) => setEditingRow({...editingRow, status: Number(v)})}>
                                                                                <SelectTrigger className='w-full'>
                                                                                    <SelectValue/>
                                                                                </SelectTrigger>
                                                                                <SelectContent>
                                                                                    <SelectItem value='1'>启用</SelectItem>
                                                                                    <SelectItem value='2'>停用</SelectItem>
                                                                                </SelectContent>
                                                                            </Select>
                                                                        ) : (
                                                                            <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                                                                                {toStatusLabel(item.status)}
                                                                            </Badge>
                                                                        )}
                                                                    </TableCell>
                                                                    <TableCell>
                                                                        {isEditing ? (
                                                                            <Input value={String(editingRow.sort)} onChange={(e) => setEditingRow({...editingRow, sort: Number(e.target.value) || 0})} className='w-full'/>
                                                                        ) : (
                                                                            item.sort ?? '-'
                                                                        )}
                                                                    </TableCell>
                                                                    <TableCell className='text-center'>
                                                                        {isEditing ? (
                                                                            <div className='flex items-center justify-center gap-1'>
                                                                                <Button type='button' variant='ghost' size='icon' className='h-7 w-7' disabled={savingMenuOp} onClick={saveEditingRow}>
                                                                                    {savingMenuOp ? <Loader2 className='h-3.5 w-3.5 animate-spin'/> : <Check className='h-3.5 w-3.5'/>}
                                                                                </Button>
                                                                                <Button type='button' variant='ghost' size='icon' className='h-7 w-7' onClick={cancelEditMenuOp}>
                                                                                    <X className='h-3.5 w-3.5'/>
                                                                                </Button>
                                                                            </div>
                                                                        ) : (
                                                                            <div className='flex items-center justify-center gap-1'>
                                                                                <Button type='button' variant='ghost' size='sm' className='gap-1' disabled={editingRowId !== null} onClick={() => startEditMenuOp(item)}>
                                                                                    <Pencil className='h-3.5 w-3.5'/>
                                                                                    编辑
                                                                                </Button>
                                                                                <Button type='button' variant='ghost' size='sm' className='gap-1 text-destructive hover:text-destructive' disabled={editingRowId !== null} onClick={() => setDeletingMenuOp(item)}>
                                                                                    <Trash2 className='h-3.5 w-3.5'/>
                                                                                    删除
                                                                                </Button>
                                                                            </div>
                                                                        )}
                                                                    </TableCell>
                                                                </TableRow>
                                                            )
                                                        })
                                                    )}
                                                </TableBody>
                                            </Table>
                                        </div>

                                        <PageToolbar
                                            page={menuOpsQuery.currentPage}
                                            size={menuOpsQuery.pageSize}
                                            total={menuOpsTotal}
                                            loading={menuOpsLoading}
                                            onPageChange={handleMenuOpPageChange}
                                            onPageSizeChange={handleMenuOpPageSizeChange}
                                        />
                                    </TabsContent>
                                </Tabs>
                            )}
                        </CardContent>
                    </Card>
                </div>
            </Main>

            <Dialog open={menuDialogOpen}
                    onOpenChange={(open) => (open ? setMenuDialogOpen(true) : closeCreateMenuDialog())}>
                <DialogContent className='sm:max-w-3xl'>
                    <DialogHeader>
                        <DialogTitle>新建菜单</DialogTitle>
                        <DialogDescription>当前父节点：{parentMenuName}</DialogDescription>
                    </DialogHeader>
                    <Form {...menuCreateForm}>
                        <form onSubmit={menuCreateForm.handleSubmit(submitCreateMenu)} className='space-y-4'>
                            <div className='grid gap-4 md:grid-cols-2'>
                                <FormField
                                    control={menuCreateForm.control}
                                    name='systemId'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>所属系统</FormLabel>
                                            <Select value={field.value} onValueChange={field.onChange}>
                                                <FormControl>
                                                    <SelectTrigger className='w-full'>
                                                        <SelectValue placeholder='请选择所属系统'/>
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    {systems.map((sys) => (
                                                        <SelectItem key={sys.id} value={String(sys.id)}>{sys.systemName}</SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={menuCreateForm.control}
                                    name='menuName'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>菜单名称</FormLabel>
                                            <FormControl>
                                                <Input {...field} placeholder='请输入菜单名称'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={menuCreateForm.control}
                                    name='menuType'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>菜单类型</FormLabel>
                                            <Select value={field.value} onValueChange={field.onChange}>
                                                <FormControl>
                                                    <SelectTrigger className='w-full'>
                                                        <SelectValue placeholder='请选择菜单类型'/>
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value='0'>目录</SelectItem>
                                                    <SelectItem value='1'>菜单</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className='grid gap-4 md:grid-cols-1'>
                                <FormField
                                    control={menuCreateForm.control}
                                    name='path'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>路由路径</FormLabel>
                                            <FormControl>
                                                <Input {...field} placeholder='例如：/system-management/menus'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className='grid gap-4 md:grid-cols-2'>
                                <FormField
                                    control={menuCreateForm.control}
                                    name='icon'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>图标</FormLabel>
                                            <FormControl>
                                                <Input {...field} placeholder='例如：Settings'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={menuCreateForm.control}
                                    name='sort'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>排序</FormLabel>
                                            <FormControl>
                                                <Input {...field} placeholder='默认 0'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className='grid gap-4 md:grid-cols-3'>
                                <FormField
                                    control={menuCreateForm.control}
                                    name='visible'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>可见性</FormLabel>
                                            <Select value={field.value} onValueChange={field.onChange}>
                                                <FormControl>
                                                    <SelectTrigger className='w-full'>
                                                        <SelectValue placeholder='请选择可见性'/>
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value='1'>可见</SelectItem>
                                                    <SelectItem value='0'>隐藏</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={menuCreateForm.control}
                                    name='isFrame'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>外链</FormLabel>
                                            <Select value={field.value} onValueChange={field.onChange}>
                                                <FormControl>
                                                    <SelectTrigger className='w-full'>
                                                        <SelectValue placeholder='请选择是否外链'/>
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value='0'>否</SelectItem>
                                                    <SelectItem value='1'>是</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <FormField
                                control={menuCreateForm.control}
                                name='status'
                                render={({field}) => (
                                    <FormItem>
                                        <FormLabel>状态</FormLabel>
                                        <Select value={field.value} onValueChange={field.onChange}>
                                            <FormControl>
                                                <SelectTrigger className='w-full'>
                                                    <SelectValue placeholder='请选择状态'/>
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                <SelectItem value='1'>启用</SelectItem>
                                                <SelectItem value='2'>停用</SelectItem>
                                            </SelectContent>
                                        </Select>
                                        <FormMessage/>
                                    </FormItem>
                                )}
                            />

                            <DialogFooter>
                                <Button type='button' variant='outline' onClick={closeCreateMenuDialog}>
                                    取消
                                </Button>
                                <Button type='submit' disabled={savingNewMenu}>
                                    {savingNewMenu ? <Loader2 className='mr-2 h-4 w-4 animate-spin'/> : null}
                                    保存
                                </Button>
                            </DialogFooter>
                        </form>
                    </Form>
                </DialogContent>
            </Dialog>

            <ConfirmDialog
                open={deletingMenuOpen}
                onOpenChange={setDeletingMenuOpen}
                title='确认删除菜单'
                desc={`确定要删除菜单「${selectedMenu?.menuName || '-'}」吗？`}
                confirmText='删除'
                destructive
                isLoading={deletingMenuLoading}
                handleConfirm={confirmDeleteMenu}
            />

            <ConfirmDialog
                open={Boolean(deletingMenuOp)}
                onOpenChange={(open) => {
                    if (!open) setDeletingMenuOp(null)
                }}
                title='确认删除菜单操作'
                desc={`确定要删除操作「${deletingMenuOp?.opName || '-'}」吗？`}
                confirmText='删除'
                destructive
                isLoading={deletingMenuOpLoading}
                handleConfirm={confirmDeleteMenuOp}
            />
        </>
    )
}
