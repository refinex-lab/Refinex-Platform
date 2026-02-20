import {useEffect, useMemo, useState} from 'react'
import {zodResolver} from '@hookform/resolvers/zod'
import {ChevronDown, ChevronRight, Loader2, Pencil, Plus, RefreshCw, Trash2,} from 'lucide-react'
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
    listSystems,
    type Menu,
    type MenuCreateRequest,
    type MenuOpCreateRequest,
    type MenuOpManage,
    type MenuOpUpdateRequest,
    type MenuTreeNode,
    type MenuUpdateRequest,
    type SystemDefinition,
    updateMenu,
    updateMenuOp,
} from '@/features/system/api'
import {toOptionalNumber, toOptionalString} from '@/features/system/common'
import {PageToolbar} from '@/features/system/components/page-toolbar'
import {handleServerError} from '@/lib/handle-server-error'

const SYSTEM_OPTION_PAGE_SIZE = 200
const MENU_OP_PAGE_SIZE = 10

const menuFormSchema = z.object({
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

const menuOpFormSchema = z.object({
    opCode: z.string().trim().max(64, '操作编码最长 64 位').optional(),
    opName: z.string().trim().min(1, '操作名称不能为空').max(64, '操作名称最长 64 位'),
    status: z.enum(['1', '2']),
    sort: z.string().trim().max(6, '排序值过大').optional(),
})

type MenuFormValues = z.infer<typeof menuFormSchema>
type MenuOpFormValues = z.infer<typeof menuOpFormSchema>

const DEFAULT_MENU_FORM: MenuFormValues = {
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

const DEFAULT_MENU_OP_FORM: MenuOpFormValues = {
    opCode: '',
    opName: '',
    status: '1',
    sort: '0',
}

type MenuTreeNodeUI = MenuTreeNode & { children?: MenuTreeNodeUI[] }

function toMenuTypeLabel(value?: number): string {
    if (value === 0) return '目录'
    if (value === 1) return '菜单'
    return '-'
}

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
    const [systems, setSystems] = useState<SystemDefinition[]>([])
    const [systemLoading, setSystemLoading] = useState(false)
    const [systemId, setSystemId] = useState<number | undefined>(undefined)

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

    const [menuOpDialogOpen, setMenuOpDialogOpen] = useState(false)
    const [editingMenuOp, setEditingMenuOp] = useState<MenuOpManage | null>(null)
    const [savingMenuOp, setSavingMenuOp] = useState(false)
    const [deletingMenuOp, setDeletingMenuOp] = useState<MenuOpManage | null>(null)
    const [deletingMenuOpLoading, setDeletingMenuOpLoading] = useState(false)

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

    const menuOpForm = useForm<MenuOpFormValues>({
        resolver: zodResolver(menuOpFormSchema),
        defaultValues: DEFAULT_MENU_OP_FORM,
        mode: 'onChange',
    })

    const filteredTree = useMemo(() => filterMenuTree(menuTree, menuKeyword), [menuTree, menuKeyword])

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

    async function loadSystems() {
        setSystemLoading(true)
        try {
            const pageData = await listSystems({currentPage: 1, pageSize: SYSTEM_OPTION_PAGE_SIZE})
            const rows = pageData.data ?? []
            setSystems(rows)
            setSystemId((prev) => prev ?? rows[0]?.id)
        } catch (error) {
            handleServerError(error)
        } finally {
            setSystemLoading(false)
        }
    }

    async function loadMenuTree(system: number) {
        setMenuTreeLoading(true)
        try {
            const tree = await getMenuTree({systemId: system})
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

    useEffect(() => {
        void loadSystems()
    }, [])

    useEffect(() => {
        if (!systemId) {
            setMenuTree([])
            setSelectedMenuId(undefined)
            return
        }
        void loadMenuTree(systemId)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [systemId])

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

    function openCreateMenuDialog(parentId: number) {
        if (!systemId) {
            toast.error('请先选择系统')
            return
        }
        setMenuDialogParentId(parentId)
        menuCreateForm.reset(DEFAULT_MENU_FORM)
        setMenuDialogOpen(true)
    }

    function closeCreateMenuDialog() {
        setMenuDialogOpen(false)
        menuCreateForm.reset(DEFAULT_MENU_FORM)
    }

    async function submitCreateMenu(values: MenuFormValues) {
        if (!systemId) {
            toast.error('请先选择系统')
            return
        }
        setSavingNewMenu(true)
        try {
            const payload: MenuCreateRequest = {
                systemId,
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
            await loadMenuTree(systemId)
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
            if (systemId) {
                await loadMenuTree(systemId)
            }
            await loadMenuDetail(selectedMenuId)
        } catch (error) {
            handleServerError(error)
        } finally {
            setSavingMenuDetail(false)
        }
    }

    async function confirmDeleteMenu() {
        if (!selectedMenuId || !systemId) return
        setDeletingMenuLoading(true)
        try {
            await deleteMenu(selectedMenuId)
            toast.success('菜单删除成功')
            setDeletingMenuOpen(false)
            await loadMenuTree(systemId)
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
        setEditingMenuOp(null)
        menuOpForm.reset(DEFAULT_MENU_OP_FORM)
        setMenuOpDialogOpen(true)
    }

    function openEditMenuOpDialog(item: MenuOpManage) {
        setEditingMenuOp(item)
        menuOpForm.reset({
            opCode: item.opCode ?? '',
            opName: item.opName ?? '',
            status: String(item.status ?? 1) as '1' | '2',
            sort: String(item.sort ?? 0),
        })
        setMenuOpDialogOpen(true)
    }

    function closeMenuOpDialog() {
        setMenuOpDialogOpen(false)
        setEditingMenuOp(null)
        menuOpForm.reset(DEFAULT_MENU_OP_FORM)
    }

    async function submitMenuOp(values: MenuOpFormValues) {
        if (!selectedMenuId) return
        setSavingMenuOp(true)
        try {
            if (editingMenuOp?.id) {
                const payload: MenuOpUpdateRequest = {
                    opCode: values.opCode?.trim() || editingMenuOp.opCode || '',
                    opName: values.opName.trim(),
                    status: Number(values.status),
                    sort: toOptionalNumber(values.sort),
                }
                await updateMenuOp(editingMenuOp.id, payload)
                toast.success('菜单操作更新成功')
            } else {
                const payload: MenuOpCreateRequest = {
                    opName: values.opName.trim(),
                    status: Number(values.status),
                    sort: toOptionalNumber(values.sort),
                }
                await createMenuOp(selectedMenuId, payload)
                toast.success('菜单操作创建成功')
            }
            closeMenuOpDialog()
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

    function renderTreeNode(node: MenuTreeNodeUI, level = 0) {
        const nodeId = node.id
        const hasChildren = Boolean(node.children?.length)
        const expanded = nodeId ? expandedNodeIds.has(nodeId) : false
        const selected = nodeId != null && selectedMenuId === nodeId

        return (
            <div key={node.id ?? `${node.menuCode}-${level}`}>
                <div
                    className={`group flex items-center gap-1 rounded-md border border-transparent px-2 py-1.5 ${
                        selected ? 'bg-muted text-foreground' : 'hover:bg-muted/40'
                    }`}
                    style={{marginLeft: level * 14}}
                >
                    {hasChildren ? (
                        <Button type='button' variant='ghost' size='icon' className='h-6 w-6'
                                onClick={() => toggleNode(nodeId)}>
                            {expanded ? <ChevronDown className='h-4 w-4'/> : <ChevronRight className='h-4 w-4'/>}
                        </Button>
                    ) : (
                        <span className='inline-block h-6 w-6'/>
                    )}

                    <button
                        type='button'
                        className='flex min-w-0 flex-1 items-center gap-2 text-left'
                        onClick={() => setSelectedMenuId(nodeId)}
                    >
                        <span className='truncate'>{node.menuName || '-'}</span>
                        <Badge variant='outline'>{toMenuTypeLabel(node.menuType)}</Badge>
                    </button>

                    <Button
                        type='button'
                        variant='ghost'
                        size='icon'
                        className='h-6 w-6 opacity-0 transition-opacity group-hover:opacity-100'
                        onClick={() => openCreateMenuDialog(nodeId ?? 0)}
                    >
                        <Plus className='h-3.5 w-3.5'/>
                    </Button>
                </div>

                {hasChildren && expanded ? node.children?.map((child) => renderTreeNode(child, level + 1)) : null}
            </div>
        )
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
                        {/*<CardHeader className='pb-0'>*/}
                        {/*    <CardTitle className='text-base'>菜单树</CardTitle>*/}
                        {/*</CardHeader>*/}
                        <CardContent className='space-y-3'>
                            <Select value={systemId ? String(systemId) : ''}
                                    onValueChange={(value) => setSystemId(Number(value))}>
                                <SelectTrigger disabled={systemLoading} className='w-full'>
                                    <SelectValue placeholder='选择所属系统'/>
                                </SelectTrigger>
                                <SelectContent>
                                    {systems.map((system) => (
                                        <SelectItem key={system.id} value={String(system.id)}>
                                            {system.systemName}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>

                            <div className='flex items-center gap-2'>
                                <Input
                                    value={menuKeyword}
                                    placeholder='按菜单名称或编码检索'
                                    onChange={(event) => setMenuKeyword(event.target.value)}
                                />
                                <Button type='button' variant='outline' size='icon'
                                        onClick={() => systemId && loadMenuTree(systemId)}>
                                    <RefreshCw className='h-4 w-4'/>
                                </Button>
                            </div>

                            <Button type='button' className='w-full gap-2' onClick={() => openCreateMenuDialog(0)}>
                                <Plus className='h-4 w-4'/>
                                新建根菜单
                            </Button>

                            <ScrollArea className='h-[calc(100vh-280px)] rounded-md border border-border/70 p-2'>
                                {menuTreeLoading ? (
                                    <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                                        <Loader2 className='h-4 w-4 animate-spin'/>
                                        正在加载菜单树...
                                    </div>
                                ) : filteredTree.length === 0 ? (
                                    <div className='py-8 text-center text-sm text-muted-foreground'>暂无菜单定义</div>
                                ) : (
                                    <div className='space-y-1'>{filteredTree.map((node) => renderTreeNode(node))}</div>
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
                                    <TabsList className='grid w-full grid-cols-2'>
                                        <TabsTrigger value='basic'>基本信息</TabsTrigger>
                                        <TabsTrigger value='ops'>菜单操作</TabsTrigger>
                                    </TabsList>

                                    <TabsContent value='basic'>
                                        <Form {...menuDetailForm}>
                                            <form onSubmit={menuDetailForm.handleSubmit(submitUpdateMenu)}
                                                  className='space-y-4'>
                                                <div className='grid gap-4 md:grid-cols-3'>
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
                                            <Button type='button' className='gap-2' onClick={openCreateMenuOpDialog}>
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
                                                        <TableHead>状态</TableHead>
                                                        <TableHead>排序</TableHead>
                                                        <TableHead className='w-[140px] text-center'>操作</TableHead>
                                                    </TableRow>
                                                </TableHeader>
                                                <TableBody>
                                                    {menuOpsLoading ? (
                                                        <TableRow>
                                                            <TableCell colSpan={5}>
                                                                <div
                                                                    className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                                                                    <Loader2 className='h-4 w-4 animate-spin'/>
                                                                    正在加载菜单操作...
                                                                </div>
                                                            </TableCell>
                                                        </TableRow>
                                                    ) : menuOps.length === 0 ? (
                                                        <TableRow>
                                                            <TableCell colSpan={5}
                                                                       className='py-6 text-center text-muted-foreground'>
                                                                当前菜单暂无操作定义
                                                            </TableCell>
                                                        </TableRow>
                                                    ) : (
                                                        menuOps.map((item) => (
                                                            <TableRow key={item.id}>
                                                                <TableCell>{item.opCode || '-'}</TableCell>
                                                                <TableCell>{item.opName || '-'}</TableCell>
                                                                <TableCell>
                                                                    <Badge
                                                                        variant={item.status === 1 ? 'default' : 'secondary'}>
                                                                        {toStatusLabel(item.status)}
                                                                    </Badge>
                                                                </TableCell>
                                                                <TableCell>{item.sort ?? '-'}</TableCell>
                                                                <TableCell className='text-center'>
                                                                    <div
                                                                        className='flex items-center justify-center gap-1'>
                                                                        <Button
                                                                            type='button'
                                                                            variant='ghost'
                                                                            size='sm'
                                                                            className='gap-1'
                                                                            onClick={() => openEditMenuOpDialog(item)}
                                                                        >
                                                                            <Pencil className='h-3.5 w-3.5'/>
                                                                            编辑
                                                                        </Button>
                                                                        <Button
                                                                            type='button'
                                                                            variant='ghost'
                                                                            size='sm'
                                                                            className='gap-1 text-destructive hover:text-destructive'
                                                                            onClick={() => setDeletingMenuOp(item)}
                                                                        >
                                                                            <Trash2 className='h-3.5 w-3.5'/>
                                                                            删除
                                                                        </Button>
                                                                    </div>
                                                                </TableCell>
                                                            </TableRow>
                                                        ))
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

            <Dialog open={menuOpDialogOpen}
                    onOpenChange={(open) => (open ? setMenuOpDialogOpen(true) : closeMenuOpDialog())}>
                <DialogContent className='sm:max-w-3xl'>
                    <DialogHeader>
                        <DialogTitle>{editingMenuOp?.id ? '编辑菜单操作' : '新建菜单操作'}</DialogTitle>
                        <DialogDescription>操作编码由后端自动生成，仅维护操作名称、状态与排序。</DialogDescription>
                    </DialogHeader>
                    <Form {...menuOpForm}>
                        <form onSubmit={menuOpForm.handleSubmit(submitMenuOp)} className='space-y-4'>
                            <div className='grid gap-4 md:grid-cols-1'>
                                <FormField
                                    control={menuOpForm.control}
                                    name='opName'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>操作名称</FormLabel>
                                            <FormControl>
                                                <Input {...field} placeholder='请输入操作名称'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className='grid gap-4 md:grid-cols-2'>
                                <FormField
                                    control={menuOpForm.control}
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

                                <FormField
                                    control={menuOpForm.control}
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

                            <DialogFooter>
                                <Button type='button' variant='outline' onClick={closeMenuOpDialog}>
                                    取消
                                </Button>
                                <Button type='submit' disabled={savingMenuOp}>
                                    {savingMenuOp ? <Loader2 className='mr-2 h-4 w-4 animate-spin'/> : null}
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
