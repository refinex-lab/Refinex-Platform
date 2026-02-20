import { useEffect, useState } from 'react'
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
import { useAuthStore } from '@/stores/auth-store'

const SYSTEM_OPTION_PAGE_SIZE = 200
const DATA_RESOURCE_PAGE_SIZE = 200
const DATA_RESOURCE_INTERFACE_PAGE_SIZE = 200

const roleFormSchema = z.object({
  roleName: z.string().trim().min(1, '角色名称不能为空').max(128, '角色名称最长 128 位'),
  roleType: z.enum(['0', '1']),
  status: z.enum(['1', '2']),
  sort: z.string().trim().max(6, '排序值过大').optional(),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

type RoleFormValues = z.infer<typeof roleFormSchema>

const DEFAULT_ROLE_FORM: RoleFormValues = {
  roleName: '',
  roleType: '1',
  status: '1',
  sort: '0',
  remark: '',
}

function toRoleTypeLabel(value?: number): string {
  if (value === 0) return '系统内置'
  if (value === 1) return '租户内置'
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
  return '-'
}

function toUserDisplay(user: RoleBindingUser): string {
  if (user.displayName && user.username) return `${user.displayName}（${user.username}）`
  return user.displayName || user.username || user.userCode || '-'
}

export function RoleManagementPage() {
  const loginUser = useAuthStore((state) => state.auth.user)
  const activeEstabId = loginUser?.estabId ?? loginUser?.primaryEstabId ?? 0
  const isPlatformAdmin = Boolean(loginUser?.roleCodes?.includes('PLATFORM_SUPER_ADMIN'))
  const roleQueryEstabId = isPlatformAdmin ? undefined : activeEstabId

  const [systems, setSystems] = useState<SystemDefinition[]>([])
  const [systemLoading, setSystemLoading] = useState(false)
  const [permissionSystemId, setPermissionSystemId] = useState<number | undefined>(undefined)

  const [roles, setRoles] = useState<Role[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<RoleListQuery>({
    estabId: roleQueryEstabId,
    currentPage: 1,
    pageSize: 10,
  })

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
  const [userDialogOpen, setUserDialogOpen] = useState(false)
  const [tempSelectedUserIds, setTempSelectedUserIds] = useState<Set<number>>(new Set())
  const [userCandidatesTotal, setUserCandidatesTotal] = useState(0)
  const [userCandidatesPage, setUserCandidatesPage] = useState(1)
  const [userCandidatesPageSize, setUserCandidatesPageSize] = useState(10)

  const [menuTree, setMenuTree] = useState<MenuTreeNode[]>([])
  const [menuTreeLoading, setMenuTreeLoading] = useState(false)
  const [selectedMenuIds, setSelectedMenuIds] = useState<Set<number>>(new Set())
  const [selectedMenuOpIds, setSelectedMenuOpIds] = useState<Set<number>>(new Set())

  const [resourceDialogOpen, setResourceDialogOpen] = useState(false)
  const [tempSelectedDrsInterfaceIds, setTempSelectedDrsInterfaceIds] = useState<Set<number>>(new Set())

  const [dataResources, setDataResources] = useState<DataResource[]>([])
  const [dataResourcesLoading, setDataResourcesLoading] = useState(false)
  const [selectedDrsId, setSelectedDrsId] = useState<number | undefined>(undefined)
  const [interfaceKeyword, setInterfaceKeyword] = useState('')
  const [drsInterfaces, setDrsInterfaces] = useState<DataResourceInterface[]>([])
  const [drsInterfacesLoading, setDrsInterfacesLoading] = useState(false)
  const [selectedDrsInterfaceIds, setSelectedDrsInterfaceIds] = useState<Set<number>>(new Set())
  const [allDrsInterfaces, setAllDrsInterfaces] = useState<DataResourceInterface[]>([])

  const form = useForm<RoleFormValues>({
    resolver: zodResolver(roleFormSchema),
    defaultValues: DEFAULT_ROLE_FORM,
    mode: 'onChange',
  })

  async function loadSystems() {
    setSystemLoading(true)
    try {
      const pageData = await listSystems({
        currentPage: 1,
        pageSize: SYSTEM_OPTION_PAGE_SIZE,
      })
      const rows = pageData.data ?? []
      setSystems(rows)
      setPermissionSystemId((prev) => prev ?? rows[0]?.id)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSystemLoading(false)
    }
  }

  async function loadRoles(activeQuery: RoleListQuery = query) {
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

  async function loadMenuTree(systemId: number, roleId?: number, estabId?: number) {
    setMenuTreeLoading(true)
    try {
      const tree = await getMenuTree(
        roleId ? { systemId, roleId, estabId } : { systemId, estabId }
      )
      setMenuTree(tree)
    } catch (error) {
      handleServerError(error)
    } finally {
      setMenuTreeLoading(false)
    }
  }

  async function loadAllDrsInterfaces(ownerEstabId?: number) {
    try {
      const allResources = await listDataResources({
        status: 1,
        ownerEstabId,
        currentPage: 1,
        pageSize: DATA_RESOURCE_PAGE_SIZE,
      })
      const resources = allResources.data ?? []

      const interfacePromises = resources.map((resource) =>
        listDataResourceInterfaces({
          drsId: resource.id!,
          status: 1,
          currentPage: 1,
          pageSize: DATA_RESOURCE_INTERFACE_PAGE_SIZE,
        })
      )

      const interfaceResults = await Promise.all(interfacePromises)
      const allInterfaces = interfaceResults.flatMap((result) => result.data ?? [])
      setAllDrsInterfaces(allInterfaces)
    } catch (error) {
      handleServerError(error)
    }
  }

  async function loadDataResources(ownerEstabId?: number) {
    setDataResourcesLoading(true)
    try {
      const pageData = await listDataResources({
        status: 1,
        ownerEstabId,
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
    setQuery((prev) => ({
      ...prev,
      estabId: roleQueryEstabId,
      currentPage: 1,
    }))
  }, [roleQueryEstabId])

  useEffect(() => {
    void loadRoles(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  useEffect(() => {
    if (!dialogOpen) return
    const systemId = permissionSystemId
    if (!systemId) return
    const roleEstabId = editingRole?.estabId ?? roleQueryEstabId ?? 0
    void loadMenuTree(systemId, editingRole?.id, roleEstabId)
    void loadDataResources(activeEstabId)
    void loadAllDrsInterfaces(activeEstabId)
    if (!editingRole) {
      setSelectedMenuIds(new Set())
      setSelectedMenuOpIds(new Set())
      setSelectedDrsInterfaceIds(new Set())
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dialogOpen, permissionSystemId, editingRole?.id, editingRole?.estabId, roleQueryEstabId])

  useEffect(() => {
    if ((!dialogOpen && !resourceDialogOpen) || !selectedDrsId) {
      setDrsInterfaces([])
      return
    }
    void loadDrsInterfaces(selectedDrsId, interfaceKeyword)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dialogOpen, resourceDialogOpen, selectedDrsId, interfaceKeyword])

  useEffect(() => {
    if (!userDialogOpen) return

    const timer = window.setTimeout(async () => {
      setUserSearching(true)
      try {
        const pageData = await listSystemUsers({
          keyword: userKeyword.trim() || undefined,
          status: 1,
          currentPage: userCandidatesPage,
          pageSize: userCandidatesPageSize,
        })
        const rows = pageData.data ?? []
        // 过滤掉已经授权的用户
        const candidates: RoleBindingUser[] = rows
          .filter((item) => item.userId != null && !selectedUserIds.has(item.userId))
          .map((item) => ({
            userId: item.userId,
            userCode: item.userCode,
            username: item.username,
            displayName: item.displayName,
          }))
        setUserCandidates(candidates)
        setUserCandidatesTotal(pageData.total ?? 0)
      } catch (error) {
        handleServerError(error)
      } finally {
        setUserSearching(false)
      }
    }, 250)

    return () => window.clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userDialogOpen, userKeyword, userCandidatesPage, userCandidatesPageSize, selectedUserIds])

  function applyFilter() {
    setQuery((prev) => ({
      estabId: prev.estabId,
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
      estabId: prev.estabId,
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

    form.reset(DEFAULT_ROLE_FORM)
    setPermissionSystemId((prev) => prev ?? systems[0]?.id)
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
      roleName: role.roleName ?? '',
      roleType: String(role.roleType ?? 1) as '0' | '1',
      status: String(role.status ?? 1) as '1' | '2',
      sort: String(role.sort ?? 0),
      remark: role.remark ?? '',
    })
    setPermissionSystemId((prev) => prev ?? systems[0]?.id)
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
    setSaving(true)
    try {
      const basePayload: RoleUpdateRequest = {
        roleName: values.roleName.trim(),
        roleType: Number(values.roleType),
        status: Number(values.status),
        isBuiltin: editingRole?.isBuiltin ?? 0,
        sort: toOptionalNumber(values.sort),
        remark: toOptionalString(values.remark),
      }

      if (editingRole?.id) {
        await updateRole(editingRole.id, basePayload)
        toast.success('角色信息已更新')
      } else {
        const createPayload: RoleCreateRequest = {
          estabId: roleQueryEstabId ?? 0,
          roleName: values.roleName.trim(),
          roleType: Number(values.roleType),
          status: Number(values.status),
          isBuiltin: 0,
          sort: toOptionalNumber(values.sort),
          remark: toOptionalString(values.remark),
        }
        const created = await createRole(createPayload)
        setEditingRole(created)
        toast.success('角色创建成功，请继续配置权限')
      }

      await loadRoles(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function applyUserPermissions() {
    if (!editingRole?.id) {
      toast.error('请先保存角色基本信息')
      return
    }

    setSaving(true)
    try {
      await assignRoleUsers(editingRole.id, {
        userIds: Array.from(selectedUserIds),
      })
      toast.success('用户授权已应用')
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function applyMenuPermissions() {
    if (!editingRole?.id) {
      toast.error('请先保存角色基本信息')
      return
    }

    setSaving(true)
    try {
      await assignRolePermissions(editingRole.id, {
        menuIds: Array.from(selectedMenuIds),
        menuOpIds: Array.from(selectedMenuOpIds),
        drsInterfaceIds: [],
      })
      toast.success('菜单权限已应用')
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function applyResourcePermissions() {
    if (!editingRole?.id) {
      toast.error('请先保存角色基本信息')
      return
    }

    setSaving(true)
    try {
      await assignRolePermissions(editingRole.id, {
        menuIds: [],
        menuOpIds: [],
        drsInterfaceIds: Array.from(selectedDrsInterfaceIds),
      })
      toast.success('数据资源权限已应用')
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  function openUserDialog() {
    setTempSelectedUserIds(new Set(selectedUserIds))
    setUserKeyword('')
    setUserCandidates([])
    setUserCandidatesPage(1)
    setUserCandidatesPageSize(10)
    setUserDialogOpen(true)
  }

  function closeUserDialog() {
    setUserDialogOpen(false)
    setUserKeyword('')
    setUserCandidates([])
    setUserCandidatesPage(1)
  }

  function toggleTempUser(userId: number, checked: boolean) {
    setTempSelectedUserIds((prev) => {
      const next = new Set(prev)
      if (checked) next.add(userId)
      else next.delete(userId)
      return next
    })
  }

  async function confirmAddUsers() {
    const newUserIds = Array.from(tempSelectedUserIds).filter((id) => !selectedUserIds.has(id))
    if (newUserIds.length === 0) {
      closeUserDialog()
      return
    }

    setUserSearching(true)
    try {
      // 从候选列表中获取新选中的用户
      const newUsers: RoleBindingUser[] = userCandidates.filter(
        (user) => user.userId != null && newUserIds.includes(user.userId)
      )

      // 如果候选列表中没有全部新选中的用户，需要额外查询
      if (newUsers.length < newUserIds.length) {
        const pageData = await listSystemUsers({
          status: 1,
          userIds: newUserIds,
          currentPage: 1,
          pageSize: 200,
        })
        const allUsers = pageData.data ?? []
        const additionalUsers: RoleBindingUser[] = allUsers
          .filter((item) => item.userId != null && newUserIds.includes(item.userId))
          .map((item) => ({
            userId: item.userId,
            userCode: item.userCode,
            username: item.username,
            displayName: item.displayName,
          }))

        // 合并去重
        const userMap = new Map<number, RoleBindingUser>()
        ;[...newUsers, ...additionalUsers].forEach((user) => {
          if (user.userId) userMap.set(user.userId, user)
        })
        const finalNewUsers = Array.from(userMap.values())

        setSelectedUserIds(tempSelectedUserIds)
        setBoundUsers((prev) => [...prev, ...finalNewUsers])
      } else {
        setSelectedUserIds(tempSelectedUserIds)
        setBoundUsers((prev) => [...prev, ...newUsers])
      }

      closeUserDialog()
    } catch (error) {
      handleServerError(error)
    } finally {
      setUserSearching(false)
    }
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

  function openResourceDialog() {
    setTempSelectedDrsInterfaceIds(new Set(selectedDrsInterfaceIds))
    setInterfaceKeyword('')
    setResourceDialogOpen(true)
  }

  function closeResourceDialog() {
    setResourceDialogOpen(false)
    setInterfaceKeyword('')
  }

  function toggleTempDrsInterface(interfaceId: number, checked: boolean) {
    setTempSelectedDrsInterfaceIds((prev) => {
      const next = new Set(prev)
      if (checked) next.add(interfaceId)
      else next.delete(interfaceId)
      return next
    })
  }

  function confirmAddResources() {
    setSelectedDrsInterfaceIds(tempSelectedDrsInterfaceIds)
    closeResourceDialog()
  }

  function removeDrsInterface(interfaceId?: number) {
    if (!interfaceId) return
    setSelectedDrsInterfaceIds((prev) => {
      const next = new Set(prev)
      next.delete(interfaceId)
      return next
    })
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
          <CardContent className='pt-0 grid gap-3 lg:grid-cols-[180px_1fr_auto]'>
            <Select value={statusInput} onValueChange={(value) => setStatusInput(value as 'all' | '1' | '2')}>
              <SelectTrigger className='w-full'>
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
                    <TableHead>状态</TableHead>
                    <TableHead>排序</TableHead>
                    <TableHead>备注</TableHead>
                    <TableHead className='w-[120px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={7}>
                        <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载角色列表...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : roles.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className='py-6 text-center text-muted-foreground'>
                        暂无角色数据
                      </TableCell>
                    </TableRow>
                  ) : (
                    roles.map((role) => (
                      <TableRow key={role.id}>
                        <TableCell>{role.roleCode || '-'}</TableCell>
                        <TableCell>{role.roleName || '-'}</TableCell>
                        <TableCell>{toRoleTypeLabel(role.roleType)}</TableCell>
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
              <div className='grid gap-4 md:grid-cols-2'>
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
                  name='roleType'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>角色类型</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue placeholder='请选择角色类型' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='0'>系统内置</SelectItem>
                          <SelectItem value='1'>租户内置</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='status'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>状态</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className='w-full'>
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

              {editingRole?.id && (
                <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as 'users' | 'resources' | 'menus')}>
                  <TabsList className='grid w-full grid-cols-3'>
                    <TabsTrigger value='users'>人员列表</TabsTrigger>
                    <TabsTrigger value='resources'>数据资源</TabsTrigger>
                    <TabsTrigger value='menus'>菜单权限</TabsTrigger>
                  </TabsList>

                <TabsContent value='users' className='space-y-3'>
                  <Card className='gap-3 py-3'>
                    <CardHeader className='pb-0 flex flex-row items-center justify-between'>
                      <CardTitle className='text-base'>已授权用户（{selectedUserIds.size}）</CardTitle>
                      <div className='flex items-center gap-2'>
                        <Button type='button' variant='outline' size='sm' className='gap-1' onClick={applyUserPermissions} disabled={saving}>
                          {saving ? <Loader2 className='h-3.5 w-3.5 animate-spin' /> : <Check className='h-3.5 w-3.5' />}
                          应用
                        </Button>
                        <Button type='button' variant='link' size='sm' className='gap-1 h-auto p-0' onClick={openUserDialog}>
                          <Plus className='h-3.5 w-3.5' />
                          新增
                        </Button>
                      </div>
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
                </TabsContent>

                <TabsContent value='resources' className='space-y-3'>
                  <Card className='gap-3 py-3'>
                    <CardHeader className='pb-0 flex flex-row items-center justify-between'>
                      <CardTitle className='text-base'>已授权数据资源接口（{selectedDrsInterfaceIds.size}）</CardTitle>
                      <div className='flex items-center gap-2'>
                        <Button type='button' variant='outline' size='sm' className='gap-1' onClick={applyResourcePermissions} disabled={saving}>
                          {saving ? <Loader2 className='h-3.5 w-3.5 animate-spin' /> : <Check className='h-3.5 w-3.5' />}
                          应用
                        </Button>
                        <Button type='button' variant='link' size='sm' className='gap-1 h-auto p-0' onClick={openResourceDialog}>
                          <Plus className='h-3.5 w-3.5' />
                          新增
                        </Button>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className='overflow-hidden rounded-md border border-border/90'>
                        <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                          <TableHeader>
                            <TableRow className='bg-muted/30 hover:bg-muted/30'>
                              <TableHead>接口编码</TableHead>
                              <TableHead>接口名称</TableHead>
                              <TableHead>SQL 过滤表达式</TableHead>
                              <TableHead>状态</TableHead>
                              <TableHead className='w-[88px] text-center'>操作</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {allDrsInterfaces.filter((item) => item.id && selectedDrsInterfaceIds.has(item.id)).length === 0 ? (
                              <TableRow>
                                <TableCell colSpan={5} className='py-4 text-center text-muted-foreground'>
                                  当前未授权任何数据资源接口
                                </TableCell>
                              </TableRow>
                            ) : (
                              allDrsInterfaces
                                .filter((item) => item.id && selectedDrsInterfaceIds.has(item.id))
                                .map((item) => (
                                  <TableRow key={item.id}>
                                    <TableCell>{item.interfaceCode || '-'}</TableCell>
                                    <TableCell>{item.interfaceName || '-'}</TableCell>
                                    <TableCell className='max-w-[420px] truncate'>{item.interfaceSql || '-'}</TableCell>
                                    <TableCell>
                                      <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                                        {toStatusLabel(item.status)}
                                      </Badge>
                                    </TableCell>
                                    <TableCell className='text-center'>
                                      <Button
                                        type='button'
                                        variant='ghost'
                                        size='sm'
                                        className='mx-auto gap-1 text-destructive hover:text-destructive'
                                        onClick={() => removeDrsInterface(item.id)}
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
                </TabsContent>

                <TabsContent value='menus' className='space-y-3'>
                  <Card className='gap-3 py-3'>
                    <CardHeader className='pb-0 flex flex-row items-center justify-between'>
                      <div className='flex items-center gap-3'>
                        <CardTitle className='text-base'>菜单与操作授权</CardTitle>
                        <Select
                          value={permissionSystemId ? String(permissionSystemId) : ''}
                          onValueChange={(value) => setPermissionSystemId(Number(value))}
                        >
                          <SelectTrigger className='w-[240px]' disabled={systemLoading}>
                            <SelectValue placeholder='请选择系统' />
                          </SelectTrigger>
                          <SelectContent>
                            {systems.map((system) => (
                              <SelectItem key={system.id} value={String(system.id)}>
                                {system.systemName}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <Button type='button' variant='outline' size='sm' className='gap-1' onClick={applyMenuPermissions} disabled={saving}>
                        {saving ? <Loader2 className='h-3.5 w-3.5 animate-spin' /> : <Check className='h-3.5 w-3.5' />}
                        应用
                      </Button>
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
              )}

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

      <Dialog open={userDialogOpen} onOpenChange={(open) => (open ? setUserDialogOpen(true) : closeUserDialog())}>
        <DialogContent className='sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>添加授权用户</DialogTitle>
            <DialogDescription>从企业用户列表中勾选需要授权的用户</DialogDescription>
          </DialogHeader>

          <div className='space-y-3'>
            <Input
              value={userKeyword}
              placeholder='输入用户名/显示名快速检索'
              onChange={(event) => setUserKeyword(event.target.value)}
            />
            <div className='overflow-hidden rounded-md border border-border/90'>
              <ScrollArea className='h-[420px]'>
                <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                  <TableHeader>
                    <TableRow className='bg-muted/30 hover:bg-muted/30'>
                      <TableHead className='w-[64px] text-center'>选择</TableHead>
                      <TableHead>用户</TableHead>
                      <TableHead>用户编码</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {userSearching ? (
                      <TableRow>
                        <TableCell colSpan={3}>
                          <div className='flex items-center justify-center gap-2 py-4 text-muted-foreground'>
                            <Loader2 className='h-4 w-4 animate-spin' />
                            正在加载用户...
                          </div>
                        </TableCell>
                      </TableRow>
                    ) : userCandidates.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={3} className='py-4 text-center text-muted-foreground'>
                          暂无用户数据
                        </TableCell>
                      </TableRow>
                    ) : (
                      userCandidates.map((user) => {
                        const userId = user.userId
                        const checked = userId ? tempSelectedUserIds.has(userId) : false
                        return (
                          <TableRow key={userId ?? user.userCode ?? user.username}>
                            <TableCell className='text-center'>
                              <Checkbox
                                checked={checked}
                                onCheckedChange={(checked) => userId && toggleTempUser(userId, checked === true)}
                              />
                            </TableCell>
                            <TableCell>{toUserDisplay(user)}</TableCell>
                            <TableCell>{user.userCode || '-'}</TableCell>
                          </TableRow>
                        )
                      })
                    )}
                  </TableBody>
                </Table>
              </ScrollArea>
            </div>
            <PageToolbar
              page={userCandidatesPage}
              size={userCandidatesPageSize}
              total={userCandidatesTotal}
              loading={userSearching}
              onPageChange={(page) => setUserCandidatesPage(page)}
              onPageSizeChange={(size) => {
                setUserCandidatesPageSize(size)
                setUserCandidatesPage(1)
              }}
            />
          </div>

          <DialogFooter>
            <Button type='button' variant='outline' onClick={closeUserDialog}>
              取消
            </Button>
            <Button type='button' onClick={confirmAddUsers} disabled={userSearching}>
              {userSearching ? <Loader2 className='mr-2 h-4 w-4 animate-spin' /> : null}
              确定
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={resourceDialogOpen} onOpenChange={(open) => (open ? setResourceDialogOpen(true) : closeResourceDialog())}>
        <DialogContent className='sm:max-w-5xl'>
          <DialogHeader>
            <DialogTitle>添加数据资源接口授权</DialogTitle>
            <DialogDescription>从数据资源接口列表中勾选需要授权的接口</DialogDescription>
          </DialogHeader>

          <div className='space-y-3'>
            <div className='grid gap-3 lg:grid-cols-[260px_1fr]'>
              <Select
                value={selectedDrsId ? String(selectedDrsId) : ''}
                onValueChange={(value) => setSelectedDrsId(Number(value))}
              >
                <SelectTrigger disabled={dataResourcesLoading} className='w-full'>
                  <SelectValue placeholder='选择数据资源' />
                </SelectTrigger>
                <SelectContent>
                  {dataResources.map((resource) => (
                    <SelectItem key={resource.id} value={String(resource.id)}>
                      {resource.drsName}（{resource.drsCode}）
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
              <ScrollArea className='h-[420px]'>
                <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                  <TableHeader>
                    <TableRow className='bg-muted/30 hover:bg-muted/30'>
                      <TableHead className='w-[64px] text-center'>授权</TableHead>
                      <TableHead>接口编码</TableHead>
                      <TableHead>接口名称</TableHead>
                      <TableHead>SQL 过滤表达式</TableHead>
                      <TableHead>状态</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {drsInterfacesLoading ? (
                      <TableRow>
                        <TableCell colSpan={5}>
                          <div className='flex items-center justify-center gap-2 py-4 text-muted-foreground'>
                            <Loader2 className='h-4 w-4 animate-spin' />
                            正在加载接口列表...
                          </div>
                        </TableCell>
                      </TableRow>
                    ) : drsInterfaces.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={5} className='py-4 text-center text-muted-foreground'>
                          当前数据资源暂无接口定义
                        </TableCell>
                      </TableRow>
                    ) : (
                      drsInterfaces.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className='text-center'>
                            <Checkbox
                              checked={item.id ? tempSelectedDrsInterfaceIds.has(item.id) : false}
                              onCheckedChange={(checked) => item.id && toggleTempDrsInterface(item.id, checked === true)}
                            />
                          </TableCell>
                          <TableCell>{item.interfaceCode || '-'}</TableCell>
                          <TableCell>{item.interfaceName || '-'}</TableCell>
                          <TableCell className='max-w-[420px] truncate'>{item.interfaceSql || '-'}</TableCell>
                          <TableCell>
                            <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                              {toStatusLabel(item.status)}
                            </Badge>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </ScrollArea>
            </div>
          </div>

          <DialogFooter>
            <Button type='button' variant='outline' onClick={closeResourceDialog}>
              取消
            </Button>
            <Button type='button' onClick={confirmAddResources}>
              确定
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
