import { useEffect, useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  Check,
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search as SearchIcon,
  Trash2,
} from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { ScrollArea } from '@/components/ui/scroll-area'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import { ThemeSwitch } from '@/components/theme-switch'
import {
  assignRolePermissions,
  assignRoleUsers,
  createRole,
  getMenuTree,
  getRoleBindings,
  listDataResourceInterfaces,
  listDataResources,
  listRoles,
  listSystemUsers,
  listSystems,
  type DataResource,
  type DataResourceInterface,
  type MenuTreeNode,
  type Role,
  type RoleBindingUser,
  type RoleCreateRequest,
  type RoleListQuery,
  type RoleUpdateRequest,
  type SystemDefinition,
  updateRole,
} from '@/features/system/api'
import { toOptionalNumber, toOptionalString } from '@/features/system/common'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const USER_CANDIDATE_LIMIT = 10
const SYSTEM_OPTION_PAGE_SIZE = 200
const DATA_RESOURCE_PAGE_SIZE = 200
const DATA_RESOURCE_INTERFACE_PAGE_SIZE = 200

const roleFormSchema = z.object({
  systemId: z.string().trim().min(1, '请选择所属系统'),
  estabId: z.string().trim().max(20, '企业ID格式非法').optional(),
  roleName: z.string().trim().min(1, '角色名称不能为空').max(128, '角色名称最长 128 位'),
  roleType: z.enum(['0', '1', '2']),
  dataScopeType: z.enum(['0', '1', '2', '3']),
  status: z.enum(['1', '2']),
  sort: z.string().trim().max(6, '排序值过大').optional(),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

type RoleFormValues = z.infer<typeof roleFormSchema>

const DEFAULT_ROLE_FORM: RoleFormValues = {
  systemId: '',
  estabId: '',
  roleName: '',
  roleType: '2',
  dataScopeType: '0',
  status: '1',
  sort: '0',
  remark: '',
}

function parsePositiveLong(value?: string): number | undefined {
  const parsed = toOptionalNumber(value)
  if (parsed == null || parsed <= 0) return undefined
  return parsed
}

function toRoleTypeLabel(value?: number): string {
  if (value === 0) return '系统内置'
  if (value === 1) return '租户内置'
  if (value === 2) return '自定义'
  return '-'
}

function toDataScopeLabel(value?: number): string {
  if (value === 0) return '全部'
  if (value === 1) return '本人'
  if (value === 2) return '团队/部门'
  if (value === 3) return '自定义'
  return '-'
}

function toStatusLabel(value?: number): string {
  if (value === 1) return '启用'
  if (value === 2) return '停用'
  return '-'
}

function toMenuTypeLabel(value?: number): string {
  if (value === 0) return '目录'
  if (value === 1) return '菜单'
  if (value === 2) return '按钮'
  return '-'
}

function toDataResourceTypeLabel(value?: number): string {
  if (value === 0) return '数据库表'
  if (value === 1) return '接口资源'
  if (value === 2) return '文件'
  if (value === 3) return '其他'
  return '-'
}

function toUserDisplay(user: RoleBindingUser): string {
  if (user.displayName && user.username) return `${user.displayName}（${user.username}）`
  return user.displayName || user.username || user.userCode || '-'
}

export function RoleManagementPage() {
  const [systems, setSystems] = useState<SystemDefinition[]>([])
  const [systemLoading, setSystemLoading] = useState(false)

  const [roles, setRoles] = useState<Role[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<RoleListQuery>({ currentPage: 1, pageSize: 10 })

  const [keywordInput, setKeywordInput] = useState('')
  const [statusInput, setStatusInput] = useState<'all' | '1' | '2'>('all')

  const [dialogOpen, setDialogOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [editingRole, setEditingRole] = useState<Role | null>(null)
  const [activeTab, setActiveTab] = useState<'users' | 'resources' | 'menus'>('users')

  const [boundUsers, setBoundUsers] = useState<RoleBindingUser[]>([])
  const [selectedUserIds, setSelectedUserIds] = useState<Set<number>>(new Set())
  const [userKeyword, setUserKeyword] = useState('')
  const [userCandidates, setUserCandidates] = useState<RoleBindingUser[]>([])
  const [userSearching, setUserSearching] = useState(false)

  const [menuTree, setMenuTree] = useState<MenuTreeNode[]>([])
  const [menuTreeLoading, setMenuTreeLoading] = useState(false)
  const [selectedMenuIds, setSelectedMenuIds] = useState<Set<number>>(new Set())
  const [selectedMenuOpIds, setSelectedMenuOpIds] = useState<Set<number>>(new Set())

  const [dataResources, setDataResources] = useState<DataResource[]>([])
  const [dataResourcesLoading, setDataResourcesLoading] = useState(false)
  const [selectedDrsId, setSelectedDrsId] = useState<number | undefined>(undefined)
  const [interfaceKeyword, setInterfaceKeyword] = useState('')
  const [drsInterfaces, setDrsInterfaces] = useState<DataResourceInterface[]>([])
  const [drsInterfacesLoading, setDrsInterfacesLoading] = useState(false)
  const [selectedDrsInterfaceIds, setSelectedDrsInterfaceIds] = useState<Set<number>>(new Set())

  const form = useForm<RoleFormValues>({
    resolver: zodResolver(roleFormSchema),
    defaultValues: DEFAULT_ROLE_FORM,
    mode: 'onChange',
  })

  const watchedSystemId = form.watch('systemId')

  const selectedSystemName = useMemo(() => {
    const systemId = parsePositiveLong(watchedSystemId)
    const matched = systems.find((item) => item.id === systemId)
    return matched?.systemName || '-'
  }, [systems, watchedSystemId])

  async function loadSystems() {
    setSystemLoading(true)
    try {
      const pageData = await listSystems({
        currentPage: 1,
        pageSize: SYSTEM_OPTION_PAGE_SIZE,
      })
      const rows = pageData.data ?? []
      setSystems(rows)
      setQuery((prev) => {
        if (prev.systemId) return prev
        return { ...prev, systemId: rows[0]?.id }
      })
    } catch (error) {
      handleServerError(error)
    } finally {
      setSystemLoading(false)
    }
  }

  async function loadRoles(activeQuery: RoleListQuery = query) {
    if (!activeQuery.systemId) {
      setRoles([])
      setTotal(0)
      return
    }
    setLoading(true)
    try {
      const pageData = await listRoles(activeQuery)
      setRoles(pageData.data ?? [])
      setTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  async function loadRoleBindings(roleId: number) {
    try {
      const bindings = await getRoleBindings(roleId)
      const userIds = (bindings.userIds ?? []).filter((item): item is number => Number.isFinite(item))
      const users = bindings.users ?? []

      setSelectedUserIds(new Set(userIds))
      setBoundUsers(users)
      setSelectedMenuIds(new Set(bindings.menuIds ?? []))
      setSelectedMenuOpIds(new Set(bindings.menuOpIds ?? []))
      setSelectedDrsInterfaceIds(new Set(bindings.drsInterfaceIds ?? []))
    } catch (error) {
      handleServerError(error)
    }
  }

  async function loadMenuTree(systemId: number, roleId?: number) {
    setMenuTreeLoading(true)
    try {
      const tree = await getMenuTree(
        roleId ? { systemId, roleId } : { systemId }
      )
      setMenuTree(tree)
    } catch (error) {
      handleServerError(error)
    } finally {
      setMenuTreeLoading(false)
    }
  }

  async function loadDataResources(systemId: number) {
    setDataResourcesLoading(true)
    try {
      const pageData = await listDataResources({
        systemId,
        status: 1,
        currentPage: 1,
        pageSize: DATA_RESOURCE_PAGE_SIZE,
      })
      const rows = pageData.data ?? []
      setDataResources(rows)
      setSelectedDrsId((prev) => {
        if (prev && rows.some((item) => item.id === prev)) return prev
        return rows[0]?.id
      })
    } catch (error) {
      handleServerError(error)
    } finally {
      setDataResourcesLoading(false)
    }
  }

  async function loadDrsInterfaces(drsId: number, keyword?: string) {
    setDrsInterfacesLoading(true)
    try {
      const pageData = await listDataResourceInterfaces({
        drsId,
        status: 1,
        keyword: toOptionalString(keyword),
        currentPage: 1,
        pageSize: DATA_RESOURCE_INTERFACE_PAGE_SIZE,
      })
      setDrsInterfaces(pageData.data ?? [])
    } catch (error) {
      handleServerError(error)
    } finally {
      setDrsInterfacesLoading(false)
    }
  }

  useEffect(() => {
    void loadSystems()
  }, [])

  useEffect(() => {
    void loadRoles(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  useEffect(() => {
    if (!dialogOpen) return
    const systemId = parsePositiveLong(watchedSystemId)
    if (!systemId) return
    void loadMenuTree(systemId, editingRole?.id)
    void loadDataResources(systemId)
    if (!editingRole) {
      setSelectedMenuIds(new Set())
      setSelectedMenuOpIds(new Set())
      setSelectedDrsInterfaceIds(new Set())
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dialogOpen, watchedSystemId, editingRole?.id])

  useEffect(() => {
    if (!dialogOpen || !selectedDrsId) {
      setDrsInterfaces([])
      return
    }
    void loadDrsInterfaces(selectedDrsId, interfaceKeyword)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dialogOpen, selectedDrsId, interfaceKeyword])

  useEffect(() => {
    if (!dialogOpen || activeTab !== 'users') return
    if (!userKeyword.trim()) {
      setUserCandidates([])
      return
    }

    const timer = window.setTimeout(async () => {
      const systemId = parsePositiveLong(form.getValues('systemId'))
      if (!systemId) return

      setUserSearching(true)
      try {
        const pageData = await listSystemUsers({
          keyword: userKeyword.trim(),
          status: 1,
          currentPage: 1,
          pageSize: USER_CANDIDATE_LIMIT,
        })
        const rows = pageData.data ?? []
        const candidates: RoleBindingUser[] = rows
          .filter((item) => item.userId != null)
          .map((item) => ({
            userId: item.userId,
            userCode: item.userCode,
            username: item.username,
            displayName: item.displayName,
          }))
        setUserCandidates(candidates)
      } catch (error) {
        handleServerError(error)
      } finally {
        setUserSearching(false)
      }
    }, 250)

    return () => window.clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dialogOpen, activeTab, userKeyword])

  function applyFilter() {
    setQuery((prev) => ({
      systemId: prev.systemId,
      keyword: toOptionalString(keywordInput),
      status: statusInput === 'all' ? undefined : Number(statusInput),
      currentPage: 1,
      pageSize: prev.pageSize ?? 10,
    }))
  }

  function resetFilter() {
    setKeywordInput('')
    setStatusInput('all')
    setQuery((prev) => ({
      systemId: prev.systemId,
      currentPage: 1,
      pageSize: prev.pageSize ?? 10,
    }))
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  function openCreateDialog() {
    setEditingRole(null)
    setActiveTab('users')
    setUserKeyword('')
    setUserCandidates([])
    setBoundUsers([])
    setSelectedUserIds(new Set())
    setSelectedMenuIds(new Set())
    setSelectedMenuOpIds(new Set())
    setSelectedDrsInterfaceIds(new Set())
    setInterfaceKeyword('')

    form.reset({
      ...DEFAULT_ROLE_FORM,
      systemId: query.systemId ? String(query.systemId) : systems[0]?.id ? String(systems[0].id) : '',
    })
    setDialogOpen(true)
  }

  function openEditDialog(role: Role) {
    setEditingRole(role)
    setActiveTab('users')
    setUserKeyword('')
    setUserCandidates([])
    setInterfaceKeyword('')
    setBoundUsers([])
    setSelectedUserIds(new Set())
    setSelectedMenuIds(new Set())
    setSelectedMenuOpIds(new Set())
    setSelectedDrsInterfaceIds(new Set())

    form.reset({
      systemId: String(role.systemId ?? query.systemId ?? ''),
      estabId: role.estabId == null || role.estabId === 0 ? '' : String(role.estabId),
      roleName: role.roleName ?? '',
      roleType: String(role.roleType ?? 2) as '0' | '1' | '2',
      dataScopeType: String(role.dataScopeType ?? 0) as '0' | '1' | '2' | '3',
      status: String(role.status ?? 1) as '1' | '2',
      sort: String(role.sort ?? 0),
      remark: role.remark ?? '',
    })
    setDialogOpen(true)
    if (role.id) {
      void loadRoleBindings(role.id)
    }
  }

  function closeDialog() {
    setDialogOpen(false)
    setEditingRole(null)
    setActiveTab('users')
    form.reset(DEFAULT_ROLE_FORM)
  }

  async function submitRole(values: RoleFormValues) {
    const systemId = parsePositiveLong(values.systemId)
    if (!systemId) {
      toast.error('请先选择所属系统')
      return
    }

    setSaving(true)
    try {
      const basePayload: RoleUpdateRequest = {
        roleName: values.roleName.trim(),
        roleType: Number(values.roleType),
        dataScopeType: Number(values.dataScopeType),
        status: Number(values.status),
        parentRoleId: 0,
        isBuiltin: editingRole?.isBuiltin ?? 0,
        sort: toOptionalNumber(values.sort),
        remark: toOptionalString(values.remark),
      }

      let roleId = editingRole?.id
      if (editingRole?.id) {
        await updateRole(editingRole.id, basePayload)
      } else {
        const createPayload: RoleCreateRequest = {
          systemId,
          estabId: parsePositiveLong(values.estabId),
          roleName: values.roleName.trim(),
          roleType: Number(values.roleType),
          dataScopeType: Number(values.dataScopeType),
          status: Number(values.status),
          parentRoleId: 0,
          isBuiltin: 0,
          sort: toOptionalNumber(values.sort),
          remark: toOptionalString(values.remark),
        }
        const created = await createRole(createPayload)
        roleId = created.id
      }

      if (roleId) {
        await assignRoleUsers(roleId, {
          userIds: Array.from(selectedUserIds),
        })
        await assignRolePermissions(roleId, {
          menuIds: Array.from(selectedMenuIds),
          menuOpIds: Array.from(selectedMenuOpIds),
          drsInterfaceIds: Array.from(selectedDrsInterfaceIds),
        })
      }

      toast.success(editingRole?.id ? '角色配置已更新' : '角色创建成功')
      closeDialog()
      await loadRoles(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  function addUser(user: RoleBindingUser) {
    const userId = user.userId
    if (!userId) return
    setSelectedUserIds((prev) => {
      const next = new Set(prev)
      next.add(userId)
      return next
    })
    setBoundUsers((prev) => {
      if (prev.some((item) => item.userId === userId)) return prev
      return [...prev, user]
    })
  }

  function removeUser(userId?: number) {
    if (!userId) return
    setSelectedUserIds((prev) => {
      const next = new Set(prev)
      next.delete(userId)
      return next
    })
    setBoundUsers((prev) => prev.filter((item) => item.userId !== userId))
  }

  function toggleMenu(menuId: number | undefined, checked: boolean) {
    if (!menuId) return
    setSelectedMenuIds((prev) => {
      const next = new Set(prev)
      if (checked) next.add(menuId)
      else next.delete(menuId)
      return next
    })
  }

  function toggleMenuOp(menuOpId: number | undefined, checked: boolean) {
    if (!menuOpId) return
    setSelectedMenuOpIds((prev) => {
      const next = new Set(prev)
      if (checked) next.add(menuOpId)
      else next.delete(menuOpId)
      return next
    })
  }

  function toggleDrsInterface(interfaceId: number | undefined, checked: boolean) {
    if (!interfaceId) return
    setSelectedDrsInterfaceIds((prev) => {
      const next = new Set(prev)
      if (checked) next.add(interfaceId)
      else next.delete(interfaceId)
      return next
    })
  }

  function renderMenuNode(node: MenuTreeNode, level = 0) {
    const menuId = node.id
    const isMenuChecked = menuId ? selectedMenuIds.has(menuId) : false

    return (
      <div key={node.id ?? `${node.menuCode}-${level}`} className='space-y-2'>
        <div className='rounded-sm border border-border/60 px-3 py-2' style={{ marginLeft: level * 16 }}>
          <div className='flex items-center justify-between gap-3'>
            <div className='flex items-center gap-2'>
              <Checkbox
                checked={isMenuChecked}
                onCheckedChange={(checked) => toggleMenu(menuId, checked === true)}
              />
              <span className='font-medium'>{node.menuName || '-'}</span>
              <Badge variant='outline'>{toMenuTypeLabel(node.menuType)}</Badge>
              {node.permissionKey ? (
                <span className='text-xs text-muted-foreground'>{node.permissionKey}</span>
              ) : null}
            </div>
          </div>

          {node.operations?.length ? (
            <div className='mt-2 flex flex-wrap gap-2'>
              {node.operations.map((operation) => (
                <label
                  key={operation.id}
                  className='flex items-center gap-2 rounded border border-border/60 px-2 py-1 text-xs'
                >
                  <Checkbox
                    checked={operation.id ? selectedMenuOpIds.has(operation.id) : false}
                    onCheckedChange={(checked) => toggleMenuOp(operation.id, checked === true)}
                  />
                  <span>{operation.opName || operation.opCode || '-'}</span>
                  {operation.permissionKey ? (
                    <span className='text-muted-foreground'>{operation.permissionKey}</span>
                  ) : null}
                </label>
              ))}
            </div>
          ) : null}
        </div>
        {node.children?.map((child) => renderMenuNode(child, level + 1))}
      </div>
    )
  }

  return (
    <>
      <Header>
        <Search />
        <div className='ms-auto flex items-center gap-4'>
          <ThemeSwitch />
          <ConfigDrawer />
          <ProfileDropdown />
        </div>
      </Header>

      <Main fixed fluid>
        <Card className='py-3 gap-3'>
          <CardContent className='pt-0 grid gap-3 lg:grid-cols-[220px_180px_1fr_auto]'>
            <Select
              value={query.systemId ? String(query.systemId) : ''}
              onValueChange={(value) =>
                setQuery((prev) => ({
                  ...prev,
                  systemId: Number(value),
                  currentPage: 1,
                }))
              }
            >
              <SelectTrigger disabled={systemLoading}>
                <SelectValue placeholder='选择所属系统' />
              </SelectTrigger>
              <SelectContent>
                {systems.map((system) => (
                  <SelectItem key={system.id} value={String(system.id)}>
                    {system.systemName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={statusInput} onValueChange={(value) => setStatusInput(value as 'all' | '1' | '2')}>
              <SelectTrigger>
                <SelectValue placeholder='状态' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部状态</SelectItem>
                <SelectItem value='1'>启用</SelectItem>
                <SelectItem value='2'>停用</SelectItem>
              </SelectContent>
            </Select>
            <Input
              value={keywordInput}
              placeholder='请输入角色编码或名称'
              onChange={(event) => setKeywordInput(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault()
                  applyFilter()
                }
              }}
            />
            <div className='flex items-center gap-2'>
              <Button type='button' variant='outline' className='gap-2' onClick={applyFilter}>
                <SearchIcon className='h-4 w-4' />
                查询
              </Button>
              <Button type='button' variant='outline' className='gap-2' onClick={resetFilter}>
                <RefreshCw className='h-4 w-4' />
                重置
              </Button>
              <Button type='button' className='gap-2' onClick={openCreateDialog}>
                <Plus className='h-4 w-4' />
                新建角色
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className='mt-2 grow overflow-hidden py-3 gap-3'>
          <CardContent className='pt-0'>
            <div className='overflow-hidden rounded-md border border-border/90'>
              <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                <TableHeader>
                  <TableRow className='bg-muted/30 hover:bg-muted/30'>
                    <TableHead>角色编码</TableHead>
                    <TableHead>角色名称</TableHead>
                    <TableHead>角色类型</TableHead>
                    <TableHead>数据范围</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead>排序</TableHead>
                    <TableHead>备注</TableHead>
                    <TableHead className='w-[120px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={8}>
                        <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载角色列表...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : roles.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} className='py-6 text-center text-muted-foreground'>
                        暂无角色数据
                      </TableCell>
                    </TableRow>
                  ) : (
                    roles.map((role) => (
                      <TableRow key={role.id}>
                        <TableCell>{role.roleCode || '-'}</TableCell>
                        <TableCell>{role.roleName || '-'}</TableCell>
                        <TableCell>{toRoleTypeLabel(role.roleType)}</TableCell>
                        <TableCell>{toDataScopeLabel(role.dataScopeType)}</TableCell>
                        <TableCell>
                          <Badge variant={role.status === 1 ? 'default' : 'secondary'}>
                            {toStatusLabel(role.status)}
                          </Badge>
                        </TableCell>
                        <TableCell>{role.sort ?? '-'}</TableCell>
                        <TableCell className='max-w-[220px] truncate'>{role.remark || '-'}</TableCell>
                        <TableCell className='text-center'>
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            className='mx-auto gap-1'
                            onClick={() => openEditDialog(role)}
                          >
                            <Pencil className='h-4 w-4' />
                            配置
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
            <PageToolbar
              page={query.currentPage ?? 1}
              size={query.pageSize ?? 10}
              total={total}
              loading={loading}
              onPageChange={handlePageChange}
              onPageSizeChange={handlePageSizeChange}
            />
          </CardContent>
        </Card>
      </Main>

      <Dialog open={dialogOpen} onOpenChange={(open) => (open ? setDialogOpen(true) : closeDialog())}>
        <DialogContent className='sm:max-w-7xl'>
          <DialogHeader>
            <DialogTitle>{editingRole?.id ? '编辑角色与权限' : '新建角色与权限'}</DialogTitle>
            <DialogDescription>
              统一维护角色基础信息，并在同一界面完成人员、数据资源和菜单权限配置。
            </DialogDescription>
          </DialogHeader>

          <Form {...form}>
            <form onSubmit={form.handleSubmit(submitRole)} className='space-y-4'>
              <div className='grid gap-4 md:grid-cols-3'>
                <FormField
                  control={form.control}
                  name='systemId'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>所属系统</FormLabel>
                      <Select
                        value={field.value}
                        onValueChange={field.onChange}
                        disabled={Boolean(editingRole?.id)}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder='请选择系统' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {systems.map((system) => (
                            <SelectItem key={system.id} value={String(system.id)}>
                              {system.systemName}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name='roleName'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>角色名称</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='请输入角色名称' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name='estabId'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>企业ID（可选）</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='留空表示平台级角色' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className='grid gap-4 md:grid-cols-4'>
                <FormField
                  control={form.control}
                  name='roleType'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>角色类型</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder='请选择角色类型' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='0'>系统内置</SelectItem>
                          <SelectItem value='1'>租户内置</SelectItem>
                          <SelectItem value='2'>自定义</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name='dataScopeType'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>数据范围</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder='请选择数据范围' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='0'>全部</SelectItem>
                          <SelectItem value='1'>本人</SelectItem>
                          <SelectItem value='2'>团队/部门</SelectItem>
                          <SelectItem value='3'>自定义</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name='status'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>状态</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder='请选择状态' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='1'>启用</SelectItem>
                          <SelectItem value='2'>停用</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name='sort'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>排序</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='默认 0' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name='remark'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Textarea {...field} rows={2} placeholder='选填' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as 'users' | 'resources' | 'menus')}>
                <TabsList className='grid w-full grid-cols-3'>
                  <TabsTrigger value='users'>人员列表</TabsTrigger>
                  <TabsTrigger value='resources'>数据资源</TabsTrigger>
                  <TabsTrigger value='menus'>菜单权限</TabsTrigger>
                </TabsList>

                <TabsContent value='users' className='space-y-3'>
                  <div className='grid gap-3 lg:grid-cols-2'>
                    <Card className='gap-3 py-3'>
                      <CardHeader className='pb-0'>
                        <CardTitle className='text-base'>候选用户</CardTitle>
                      </CardHeader>
                      <CardContent className='space-y-3'>
                        <Input
                          value={userKeyword}
                          placeholder='输入用户名/显示名快速检索'
                          onChange={(event) => setUserKeyword(event.target.value)}
                        />
                        <div className='overflow-hidden rounded-md border border-border/90'>
                          <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                            <TableHeader>
                              <TableRow className='bg-muted/30 hover:bg-muted/30'>
                                <TableHead>用户</TableHead>
                                <TableHead>用户编码</TableHead>
                                <TableHead className='w-[88px] text-center'>操作</TableHead>
                              </TableRow>
                            </TableHeader>
                            <TableBody>
                              {userSearching ? (
                                <TableRow>
                                  <TableCell colSpan={3}>
                                    <div className='flex items-center justify-center gap-2 py-4 text-muted-foreground'>
                                      <Loader2 className='h-4 w-4 animate-spin' />
                                      正在检索用户...
                                    </div>
                                  </TableCell>
                                </TableRow>
                              ) : userCandidates.length === 0 ? (
                                <TableRow>
                                  <TableCell colSpan={3} className='py-4 text-center text-muted-foreground'>
                                    输入关键字后可检索用户
                                  </TableCell>
                                </TableRow>
                              ) : (
                                userCandidates.map((user) => {
                                  const userId = user.userId
                                  const selected = userId ? selectedUserIds.has(userId) : false
                                  return (
                                    <TableRow key={userId ?? user.userCode ?? user.username}>
                                      <TableCell>{toUserDisplay(user)}</TableCell>
                                      <TableCell>{user.userCode || '-'}</TableCell>
                                      <TableCell className='text-center'>
                                        <Button
                                          type='button'
                                          size='sm'
                                          variant={selected ? 'secondary' : 'outline'}
                                          className='mx-auto gap-1'
                                          disabled={selected}
                                          onClick={() => addUser(user)}
                                        >
                                          {selected ? <Check className='h-3.5 w-3.5' /> : <Plus className='h-3.5 w-3.5' />}
                                          {selected ? '已选' : '添加'}
                                        </Button>
                                      </TableCell>
                                    </TableRow>
                                  )
                                })
                              )}
                            </TableBody>
                          </Table>
                        </div>
                      </CardContent>
                    </Card>

                    <Card className='gap-3 py-3'>
                      <CardHeader className='pb-0'>
                        <CardTitle className='text-base'>已授权用户（{selectedUserIds.size}）</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className='overflow-hidden rounded-md border border-border/90'>
                          <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                            <TableHeader>
                              <TableRow className='bg-muted/30 hover:bg-muted/30'>
                                <TableHead>用户</TableHead>
                                <TableHead>用户编码</TableHead>
                                <TableHead className='w-[88px] text-center'>操作</TableHead>
                              </TableRow>
                            </TableHeader>
                            <TableBody>
                              {boundUsers.length === 0 ? (
                                <TableRow>
                                  <TableCell colSpan={3} className='py-4 text-center text-muted-foreground'>
                                    当前未授权任何用户
                                  </TableCell>
                                </TableRow>
                              ) : (
                                boundUsers.map((user) => (
                                  <TableRow key={user.userId ?? user.userCode ?? user.username}>
                                    <TableCell>{toUserDisplay(user)}</TableCell>
                                    <TableCell>{user.userCode || '-'}</TableCell>
                                    <TableCell className='text-center'>
                                      <Button
                                        type='button'
                                        variant='ghost'
                                        size='sm'
                                        className='mx-auto gap-1 text-destructive hover:text-destructive'
                                        onClick={() => removeUser(user.userId)}
                                      >
                                        <Trash2 className='h-3.5 w-3.5' />
                                        移除
                                      </Button>
                                    </TableCell>
                                  </TableRow>
                                ))
                              )}
                            </TableBody>
                          </Table>
                        </div>
                      </CardContent>
                    </Card>
                  </div>
                </TabsContent>

                <TabsContent value='resources' className='space-y-3'>
                  <Card className='gap-3 py-3'>
                    <CardHeader className='pb-0'>
                      <CardTitle className='text-base'>数据资源接口授权</CardTitle>
                    </CardHeader>
                    <CardContent className='space-y-3'>
                      <div className='grid gap-3 lg:grid-cols-[260px_1fr]'>
                        <Select
                          value={selectedDrsId ? String(selectedDrsId) : ''}
                          onValueChange={(value) => setSelectedDrsId(Number(value))}
                        >
                          <SelectTrigger disabled={dataResourcesLoading}>
                            <SelectValue placeholder='选择数据资源' />
                          </SelectTrigger>
                          <SelectContent>
                            {dataResources.map((resource) => (
                              <SelectItem key={resource.id} value={String(resource.id)}>
                                {resource.drsName}（{toDataResourceTypeLabel(resource.drsType)}）
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <Input
                          value={interfaceKeyword}
                          placeholder='按接口编码/名称筛选'
                          onChange={(event) => setInterfaceKeyword(event.target.value)}
                        />
                      </div>
                      <div className='overflow-hidden rounded-md border border-border/90'>
                        <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                          <TableHeader>
                            <TableRow className='bg-muted/30 hover:bg-muted/30'>
                              <TableHead className='w-[64px] text-center'>授权</TableHead>
                              <TableHead>接口编码</TableHead>
                              <TableHead>接口名称</TableHead>
                              <TableHead>方法</TableHead>
                              <TableHead>路径</TableHead>
                              <TableHead>权限标识</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {drsInterfacesLoading ? (
                              <TableRow>
                                <TableCell colSpan={6}>
                                  <div className='flex items-center justify-center gap-2 py-4 text-muted-foreground'>
                                    <Loader2 className='h-4 w-4 animate-spin' />
                                    正在加载接口列表...
                                  </div>
                                </TableCell>
                              </TableRow>
                            ) : drsInterfaces.length === 0 ? (
                              <TableRow>
                                <TableCell colSpan={6} className='py-4 text-center text-muted-foreground'>
                                  当前数据资源暂无接口定义
                                </TableCell>
                              </TableRow>
                            ) : (
                              drsInterfaces.map((item) => (
                                <TableRow key={item.id}>
                                  <TableCell className='text-center'>
                                    <Checkbox
                                      checked={item.id ? selectedDrsInterfaceIds.has(item.id) : false}
                                      onCheckedChange={(checked) => toggleDrsInterface(item.id, checked === true)}
                                    />
                                  </TableCell>
                                  <TableCell>{item.interfaceCode || '-'}</TableCell>
                                  <TableCell>{item.interfaceName || '-'}</TableCell>
                                  <TableCell>{item.httpMethod || '-'}</TableCell>
                                  <TableCell>{item.pathPattern || '-'}</TableCell>
                                  <TableCell>{item.permissionKey || '-'}</TableCell>
                                </TableRow>
                              ))
                            )}
                          </TableBody>
                        </Table>
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>

                <TabsContent value='menus' className='space-y-3'>
                  <Card className='gap-3 py-3'>
                    <CardHeader className='pb-0'>
                      <CardTitle className='text-base'>
                        菜单与操作授权（系统：{selectedSystemName}）
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ScrollArea className='h-[420px] rounded-md border border-border/70 p-3'>
                        {menuTreeLoading ? (
                          <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                            <Loader2 className='h-4 w-4 animate-spin' />
                            正在加载菜单树...
                          </div>
                        ) : menuTree.length === 0 ? (
                          <div className='py-6 text-center text-muted-foreground'>当前系统暂无菜单定义</div>
                        ) : (
                          <div className='space-y-2'>{menuTree.map((node) => renderMenuNode(node))}</div>
                        )}
                      </ScrollArea>
                    </CardContent>
                  </Card>
                </TabsContent>
              </Tabs>

              <DialogFooter>
                <Button type='button' variant='outline' onClick={closeDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={saving}>
                  {saving ? <Loader2 className='mr-2 h-4 w-4 animate-spin' /> : null}
                  保存
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </>
  )
}
