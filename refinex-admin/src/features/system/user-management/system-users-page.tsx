import { useEffect, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowUpDown,
  Eye,
  EyeOff,
  Fingerprint,
  Building2,
  Loader2,
  Pencil,
  Plus,
  Search as SearchIcon,
  Trash2,
} from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
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
  batchDeleteSystemUsers,
  createSystemUser,
  createSystemUserIdentity,
  deleteSystemUser,
  deleteSystemUserIdentity,
  listEstabs,
  listSystemUserEstabs,
  listSystemUserIdentities,
  listSystemUsers,
  type Estab,
  type SystemUser,
  type SystemUserCreateRequest,
  type SystemUserEstab,
  type SystemUserIdentity,
  type SystemUserIdentityCreateRequest,
  type SystemUserIdentityUpdateRequest,
  type SystemUserListQuery,
  type SystemUserUpdateRequest,
  updateSystemUser,
  updateSystemUserIdentity,
} from '@/features/system/api'
import {
  formatDateTime,
  toDateTimeLocalValue,
  toOptionalString,
} from '@/features/system/common'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const userFormSchema = z.object({
  userCode: z.string().trim().max(64, '用户编码最长 64 位').optional(),
  username: z.string().trim().max(64, '用户名最长 64 位').optional(),
  displayName: z.string().trim().min(1, '显示名称不能为空').max(64, '显示名称最长 64 位'),
  nickname: z.string().trim().max(64, '昵称最长 64 位').optional(),
  gender: z.enum(['0', '1', '2', '3']),
  birthday: z.string().trim().optional(),
  userType: z.enum(['0', '1', '2']),
  status: z.enum(['1', '2', '3']),
  primaryEstabId: z.string().trim().min(1, '请选择所属企业').max(20, '企业标识格式非法'),
  primaryPhone: z.string().trim().max(32, '手机号最长 32 位').optional(),
  phoneVerified: z.enum(['0', '1']),
  primaryEmail: z.string().trim().max(128, '邮箱最长 128 位').optional(),
  emailVerified: z.enum(['0', '1']),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

const searchFormSchema = z.object({
  userCode: z.string().trim().max(64, '用户编码最长 64 位').optional(),
  username: z.string().trim().max(64, '用户名最长 64 位').optional(),
  displayName: z.string().trim().max(64, '显示名称最长 64 位').optional(),
  nickname: z.string().trim().max(64, '昵称最长 64 位').optional(),
  primaryPhone: z.string().trim().max(32, '手机号最长 32 位').optional(),
  primaryEmail: z.string().trim().max(128, '邮箱最长 128 位').optional(),
  userType: z.enum(['all', '0', '1', '2']),
  status: z.enum(['all', '1', '2', '3']),
})

const identityFormSchema = z.object({
  identityType: z.enum(['1', '2', '3', '4', '5', '6', '7', '8']),
  identifier: z.string().trim().min(1, '身份标识不能为空').max(128, '身份标识最长 128 位'),
  issuer: z.string().trim().max(128, '发行方最长 128 位').optional(),
  credential: z.string().trim().max(255, '凭证最长 255 位').optional(),
  credentialAlg: z.string().trim().max(64, '算法最长 64 位').optional(),
  isPrimary: z.enum(['0', '1']),
  verified: z.enum(['0', '1']),
  status: z.enum(['0', '1']),
})

type UserFormValues = z.infer<typeof userFormSchema>
type SearchFormValues = z.infer<typeof searchFormSchema>
type IdentityFormValues = z.infer<typeof identityFormSchema>
type UserDialogMode = 'create' | 'edit' | 'view'

const DEFAULT_USER_FORM: UserFormValues = {
  userCode: '',
  username: '',
  displayName: '',
  nickname: '',
  gender: '0',
  birthday: '',
  userType: '1',
  status: '1',
  primaryEstabId: '',
  primaryPhone: '',
  phoneVerified: '0',
  primaryEmail: '',
  emailVerified: '0',
  remark: '',
}

const DEFAULT_SEARCH_FORM: SearchFormValues = {
  userCode: '',
  username: '',
  displayName: '',
  nickname: '',
  primaryPhone: '',
  primaryEmail: '',
  userType: 'all',
  status: 'all',
}

const DEFAULT_IDENTITY_FORM: IdentityFormValues = {
  identityType: '1',
  identifier: '',
  issuer: '',
  credential: '',
  credentialAlg: '',
  isPrimary: '0',
  verified: '0',
  status: '1',
}

function toUserTypeLabel(type?: number): string {
  if (type === 0) return '平台用户'
  if (type === 1) return '租户用户'
  if (type === 2) return '合作方'
  return '-'
}

function toStatusLabel(status?: number): string {
  if (status === 1) return '启用'
  if (status === 2) return '停用'
  if (status === 3) return '锁定'
  return '-'
}

function toIdentityTypeLabel(type?: number): string {
  if (type === 1) return '用户名密码'
  if (type === 2) return '手机号短信'
  if (type === 3) return '邮箱密码'
  if (type === 4) return '邮箱验证码'
  if (type === 5) return '微信扫码'
  if (type === 6) return 'OAuth/OIDC'
  if (type === 7) return 'SAML'
  if (type === 8) return 'TOTP'
  return '-'
}

function parseOptionalLong(value?: string): number | undefined {
  if (!value) return undefined
  const parsed = Number(value)
  if (!Number.isInteger(parsed) || parsed <= 0) return undefined
  return parsed
}

function maskPhone(phone?: string): string {
  if (!phone) return '-'
  if (phone.length <= 7) return phone
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

function maskEmail(email?: string): string {
  if (!email) return '-'
  const index = email.indexOf('@')
  if (index <= 1) return email
  const prefix = email.slice(0, index)
  const domain = email.slice(index)
  if (prefix.length <= 2) {
    return `${prefix[0] ?? ''}***${domain}`
  }
  return `${prefix.slice(0, 2)}***${domain}`
}

export function SystemUsersPage() {
  const [users, setUsers] = useState<SystemUser[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [filterDialogOpen, setFilterDialogOpen] = useState(false)
  const [query, setQuery] = useState<SystemUserListQuery>({ currentPage: 1, pageSize: 10 })
  const [estabOptions, setEstabOptions] = useState<Estab[]>([])
  const [estabLoading, setEstabLoading] = useState(false)
  const [selectedUserIds, setSelectedUserIds] = useState<Set<number>>(new Set())
  const [visiblePhoneUserIds, setVisiblePhoneUserIds] = useState<Set<number>>(new Set())
  const [visibleEmailUserIds, setVisibleEmailUserIds] = useState<Set<number>>(new Set())

  const [selectedUser, setSelectedUser] = useState<SystemUser | null>(null)
  const selectedUserId = selectedUser?.userId

  const [identities, setIdentities] = useState<SystemUserIdentity[]>([])
  const [userEstabs, setUserEstabs] = useState<SystemUserEstab[]>([])
  const [userEstabsLoading, setUserEstabsLoading] = useState(false)
  const [identityTotal, setIdentityTotal] = useState(0)
  const [identityLoading, setIdentityLoading] = useState(false)
  const [identityQuery, setIdentityQuery] = useState({ currentPage: 1, pageSize: 10 })

  const [userDialogOpen, setUserDialogOpen] = useState(false)
  const [editingUser, setEditingUser] = useState<SystemUser | null>(null)
  const [userDialogMode, setUserDialogMode] = useState<UserDialogMode>('create')
  const [savingUser, setSavingUser] = useState(false)
  const [userDialogTab, setUserDialogTab] = useState<'identities' | 'estabs'>('identities')

  const [identityDialogOpen, setIdentityDialogOpen] = useState(false)
  const [editingIdentity, setEditingIdentity] = useState<SystemUserIdentity | null>(null)
  const [savingIdentity, setSavingIdentity] = useState(false)
  const [deletingIdentity, setDeletingIdentity] = useState<SystemUserIdentity | null>(null)
  const [deletingIdentityLoading, setDeletingIdentityLoading] = useState(false)
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)
  const [deletingUsersLoading, setDeletingUsersLoading] = useState(false)
  const [pendingDeleteUserIds, setPendingDeleteUserIds] = useState<number[]>([])

  const userForm = useForm<UserFormValues>({
    resolver: zodResolver(userFormSchema),
    defaultValues: DEFAULT_USER_FORM,
    mode: 'onChange',
  })
  const searchForm = useForm<SearchFormValues>({
    resolver: zodResolver(searchFormSchema),
    defaultValues: DEFAULT_SEARCH_FORM,
    mode: 'onChange',
  })
  const identityForm = useForm<IdentityFormValues>({
    resolver: zodResolver(identityFormSchema),
    defaultValues: DEFAULT_IDENTITY_FORM,
    mode: 'onChange',
  })

  const identityUserId = editingUser?.userId ?? (userDialogMode === 'view' ? selectedUserId : undefined)
  const isUserViewMode = userDialogMode === 'view'

  async function loadUsers(activeQuery: SystemUserListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listSystemUsers(activeQuery)
      const rows = pageData.data ?? []
      setUsers(rows)
      setTotal(pageData.total ?? 0)
      setSelectedUserIds(new Set())
      setVisiblePhoneUserIds(new Set())
      setVisibleEmailUserIds(new Set())
      setSelectedUser((prev) => {
        if (rows.length === 0) return null
        if (!prev?.userId) return rows[0]
        const matched = rows.find((item) => item.userId === prev.userId)
        return matched ?? rows[0]
      })
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  async function loadIdentities(userId: number, currentPage: number, pageSize: number) {
    setIdentityLoading(true)
    try {
      const pageData = await listSystemUserIdentities(userId, { currentPage, pageSize })
      setIdentities(pageData.data ?? [])
      setIdentityTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setIdentityLoading(false)
    }
  }

  async function loadEstabOptions() {
    setEstabLoading(true)
    try {
      const pageData = await listEstabs({ currentPage: 1, pageSize: 200, status: 1 })
      setEstabOptions(pageData.data ?? [])
    } catch (error) {
      handleServerError(error)
    } finally {
      setEstabLoading(false)
    }
  }

  async function loadUserEstabs(userId: number) {
    setUserEstabsLoading(true)
    try {
      const rows = await listSystemUserEstabs(userId)
      setUserEstabs(rows)
    } catch (error) {
      handleServerError(error)
    } finally {
      setUserEstabsLoading(false)
    }
  }

  useEffect(() => {
    void loadUsers(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  useEffect(() => {
    void loadEstabOptions()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (!identityUserId) {
      setIdentities([])
      setIdentityTotal(0)
      return
    }
    void loadIdentities(identityUserId, identityQuery.currentPage, identityQuery.pageSize)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [identityUserId, identityQuery])

  useEffect(() => {
    if (!userDialogOpen || !identityUserId) {
      setUserEstabs([])
      return
    }
    void loadUserEstabs(identityUserId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userDialogOpen, identityUserId])

  function openFilterDialog() {
    searchForm.reset({
      userCode: query.userCode ?? '',
      username: query.username ?? '',
      displayName: query.displayName ?? '',
      nickname: query.nickname ?? '',
      primaryPhone: query.primaryPhone ?? '',
      primaryEmail: query.primaryEmail ?? '',
      userType: query.userType == null ? 'all' : (String(query.userType) as '0' | '1' | '2'),
      status: query.status == null ? 'all' : (String(query.status) as '1' | '2' | '3'),
    })
    setFilterDialogOpen(true)
  }

  function applyFilters(values: SearchFormValues) {
    setQuery((prev) => ({
      ...prev,
      userCode: toOptionalString(values.userCode),
      username: toOptionalString(values.username),
      displayName: toOptionalString(values.displayName),
      nickname: toOptionalString(values.nickname),
      primaryPhone: toOptionalString(values.primaryPhone),
      primaryEmail: toOptionalString(values.primaryEmail),
      userType: values.userType === 'all' ? undefined : Number(values.userType),
      status: values.status === 'all' ? undefined : Number(values.status),
      currentPage: 1,
    }))
    setFilterDialogOpen(false)
  }

  function resetFilters() {
    searchForm.reset(DEFAULT_SEARCH_FORM)
    setQuery({ currentPage: 1, pageSize: query.pageSize ?? 10 })
    setFilterDialogOpen(false)
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  function openCreateUserDialog() {
    setEditingUser(null)
    setUserDialogMode('create')
    setUserDialogTab('identities')
    userForm.reset(DEFAULT_USER_FORM)
    setUserDialogOpen(true)
  }

  function openEditUserDialog(user: SystemUser) {
    setSelectedUser(user)
    setEditingUser(user)
    setUserDialogMode('edit')
    setUserDialogTab('identities')
    setIdentityQuery((prev) => ({ ...prev, currentPage: 1 }))
    userForm.reset({
      userCode: user.userCode ?? '',
      username: user.username ?? '',
      displayName: user.displayName ?? '',
      nickname: user.nickname ?? '',
      gender: String(user.gender ?? 0) as '0' | '1' | '2' | '3',
      birthday: user.birthday ? toDateTimeLocalValue(user.birthday).slice(0, 10) : '',
      userType: String(user.userType ?? 1) as '0' | '1' | '2',
      status: String(user.status ?? 1) as '1' | '2' | '3',
      primaryEstabId: user.primaryEstabId == null ? '' : String(user.primaryEstabId),
      primaryPhone: user.primaryPhone ?? '',
      phoneVerified: String(user.phoneVerified ?? 0) as '0' | '1',
      primaryEmail: user.primaryEmail ?? '',
      emailVerified: String(user.emailVerified ?? 0) as '0' | '1',
      remark: user.remark ?? '',
    })
    setUserDialogOpen(true)
  }

  function openViewUserDialog(user: SystemUser) {
    setSelectedUser(user)
    setEditingUser(user)
    setUserDialogMode('view')
    setUserDialogTab('identities')
    setIdentityQuery((prev) => ({ ...prev, currentPage: 1 }))
    userForm.reset({
      userCode: user.userCode ?? '',
      username: user.username ?? '',
      displayName: user.displayName ?? '',
      nickname: user.nickname ?? '',
      gender: String(user.gender ?? 0) as '0' | '1' | '2' | '3',
      birthday: user.birthday ? toDateTimeLocalValue(user.birthday).slice(0, 10) : '',
      userType: String(user.userType ?? 1) as '0' | '1' | '2',
      status: String(user.status ?? 1) as '1' | '2' | '3',
      primaryEstabId: user.primaryEstabId == null ? '' : String(user.primaryEstabId),
      primaryPhone: user.primaryPhone ?? '',
      phoneVerified: String(user.phoneVerified ?? 0) as '0' | '1',
      primaryEmail: user.primaryEmail ?? '',
      emailVerified: String(user.emailVerified ?? 0) as '0' | '1',
      remark: user.remark ?? '',
    })
    setUserDialogOpen(true)
  }

  function closeUserDialog() {
    setUserDialogOpen(false)
    setEditingUser(null)
    setUserDialogMode('create')
    setUserDialogTab('identities')
    setUserEstabs([])
    userForm.reset(DEFAULT_USER_FORM)
  }

  async function submitUser(values: UserFormValues) {
    setSavingUser(true)
    try {
      const payload: SystemUserUpdateRequest = {
        displayName: values.displayName.trim(),
        nickname: toOptionalString(values.nickname),
        gender: Number(values.gender),
        birthday: toOptionalString(values.birthday),
        userType: Number(values.userType),
        status: Number(values.status),
        primaryEstabId: parseOptionalLong(values.primaryEstabId),
        primaryPhone: toOptionalString(values.primaryPhone),
        phoneVerified: Number(values.phoneVerified),
        primaryEmail: toOptionalString(values.primaryEmail),
        emailVerified: Number(values.emailVerified),
        remark: toOptionalString(values.remark),
      }

      if (editingUser?.userId) {
        await updateSystemUser(editingUser.userId, payload)
        toast.success('用户已更新。')
      } else {
        await createSystemUser({
          ...(payload as SystemUserCreateRequest),
          username: toOptionalString(values.username),
          displayName: values.displayName.trim(),
        })
        toast.success('用户已创建。')
      }

      closeUserDialog()
      await loadUsers(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingUser(false)
    }
  }

  function openCreateIdentityDialog() {
    if (!identityUserId) {
      toast.error('请先选择用户。')
      return
    }
    setEditingIdentity(null)
    identityForm.reset(DEFAULT_IDENTITY_FORM)
    setIdentityDialogOpen(true)
  }

  function openEditIdentityDialog(identity: SystemUserIdentity) {
    setEditingIdentity(identity)
    identityForm.reset({
      identityType: String(identity.identityType ?? 1) as '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8',
      identifier: identity.identifier ?? '',
      issuer: identity.issuer ?? '',
      credential: '',
      credentialAlg: identity.credentialAlg ?? '',
      isPrimary: String(identity.isPrimary ?? 0) as '0' | '1',
      verified: String(identity.verified ?? 0) as '0' | '1',
      status: String(identity.status ?? 1) as '0' | '1',
    })
    setIdentityDialogOpen(true)
  }

  function closeIdentityDialog() {
    setIdentityDialogOpen(false)
    setEditingIdentity(null)
    identityForm.reset(DEFAULT_IDENTITY_FORM)
  }

  async function submitIdentity(values: IdentityFormValues) {
    if (!identityUserId) {
      toast.error('请先选择用户。')
      return
    }
    setSavingIdentity(true)
    try {
      const payload: SystemUserIdentityUpdateRequest = {
        identifier: values.identifier.trim(),
        issuer: toOptionalString(values.issuer),
        credential: toOptionalString(values.credential),
        credentialAlg: toOptionalString(values.credentialAlg),
        isPrimary: Number(values.isPrimary),
        verified: Number(values.verified),
        status: Number(values.status),
      }

      if (editingIdentity?.identityId) {
        await updateSystemUserIdentity(editingIdentity.identityId, payload)
        toast.success('身份已更新。')
      } else {
        await createSystemUserIdentity(identityUserId, {
          ...(payload as SystemUserIdentityCreateRequest),
          identityType: Number(values.identityType),
          identifier: values.identifier.trim(),
        })
        toast.success('身份已创建。')
      }

      closeIdentityDialog()
      await loadIdentities(identityUserId, identityQuery.currentPage, identityQuery.pageSize)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingIdentity(false)
    }
  }

  async function confirmDeleteIdentity() {
    if (!identityUserId || !deletingIdentity?.identityId) return
    setDeletingIdentityLoading(true)
    try {
      await deleteSystemUserIdentity(deletingIdentity.identityId)
      toast.success('身份已删除。')
      setDeletingIdentity(null)
      await loadIdentities(identityUserId, identityQuery.currentPage, identityQuery.pageSize)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingIdentityLoading(false)
    }
  }

  function togglePhoneVisible(userId?: number) {
    if (!userId) return
    setVisiblePhoneUserIds((prev) => {
      const next = new Set(prev)
      if (next.has(userId)) {
        next.delete(userId)
      } else {
        next.add(userId)
      }
      return next
    })
  }

  function toggleEmailVisible(userId?: number) {
    if (!userId) return
    setVisibleEmailUserIds((prev) => {
      const next = new Set(prev)
      if (next.has(userId)) {
        next.delete(userId)
      } else {
        next.add(userId)
      }
      return next
    })
  }

  function toggleRowSelect(userId?: number, checked?: boolean) {
    if (!userId) return
    setSelectedUserIds((prev) => {
      const next = new Set(prev)
      if (checked) {
        next.add(userId)
      } else {
        next.delete(userId)
      }
      return next
    })
  }

  function toggleSelectAll(checked?: boolean) {
    if (!checked) {
      setSelectedUserIds(new Set())
      return
    }
    const ids = users
      .map((item) => item.userId)
      .filter((id): id is number => id != null)
    setSelectedUserIds(new Set(ids))
  }

  function toggleSort(field: string) {
    setQuery((prev) => {
      if (prev.sortBy !== field) {
        return {
          ...prev,
          sortBy: field,
          sortDirection: 'asc',
          currentPage: 1,
        }
      }
      if (prev.sortDirection === 'asc') {
        return {
          ...prev,
          sortDirection: 'desc',
          currentPage: 1,
        }
      }
      return {
        ...prev,
        sortBy: undefined,
        sortDirection: undefined,
        currentPage: 1,
      }
    })
  }

  function renderSortLabel(label: string, field: string) {
    const direction = query.sortBy === field ? query.sortDirection : undefined
    const suffix = direction === 'asc' ? '↑' : direction === 'desc' ? '↓' : ''
    return (
      <Button
        type='button'
        variant='ghost'
        className='h-8 px-1 font-semibold'
        onClick={() => toggleSort(field)}
      >
        <span>{label}{suffix ? ` ${suffix}` : ''}</span>
        <ArrowUpDown className='ml-1 h-3.5 w-3.5 text-muted-foreground' />
      </Button>
    )
  }

  function confirmDeleteUsers(userIds: number[]) {
    if (userIds.length === 0) {
      toast.error('请先选择要删除的用户。')
      return
    }
    setPendingDeleteUserIds(userIds)
    setDeleteConfirmOpen(true)
  }

  async function deleteUsersConfirmed() {
    if (pendingDeleteUserIds.length === 0) {
      return
    }
    setDeletingUsersLoading(true)
    try {
      if (pendingDeleteUserIds.length === 1) {
        await deleteSystemUser(pendingDeleteUserIds[0])
      } else {
        await batchDeleteSystemUsers({ userIds: pendingDeleteUserIds })
      }
      toast.success('用户已删除。')
      setDeleteConfirmOpen(false)
      setPendingDeleteUserIds([])
      await loadUsers(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingUsersLoading(false)
    }
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
        <div className='mb-2 flex items-center gap-2 px-6'>
          <Button type='button' variant='outline' onClick={openFilterDialog} className='gap-2'>
            <SearchIcon className='h-4 w-4' />
            查找
          </Button>
          <Button type='button' onClick={openCreateUserDialog} className='gap-2'>
            <Plus className='h-4 w-4' />
            新建用户
          </Button>
          <Button
            type='button'
            variant='outline'
            className='gap-2'
            disabled={selectedUserIds.size === 0}
            onClick={() => confirmDeleteUsers(Array.from(selectedUserIds))}
          >
            <Trash2 className='h-4 w-4' />
            删除
          </Button>
          {selectedUserIds.size > 0 ? (
            <span className='text-sm text-muted-foreground'>
              已选择 {selectedUserIds.size} 项
            </span>
          ) : null}
        </div>

        <Card className='overflow-hidden py-3 gap-3'>
          <CardContent className='pt-0'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='w-[48px]'>
                    <Checkbox
                      checked={users.length > 0 && selectedUserIds.size === users.filter((u) => u.userId != null).length}
                      onCheckedChange={(checked) => toggleSelectAll(Boolean(checked))}
                      aria-label='全选用户'
                    />
                  </TableHead>
                  <TableHead>{renderSortLabel('用户编码', 'userCode')}</TableHead>
                  <TableHead>{renderSortLabel('用户名', 'username')}</TableHead>
                  <TableHead>{renderSortLabel('显示名', 'displayName')}</TableHead>
                  <TableHead>{renderSortLabel('昵称', 'nickname')}</TableHead>
                  <TableHead>{renderSortLabel('所属企业', 'primaryEstabId')}</TableHead>
                  <TableHead>{renderSortLabel('用户类型', 'userType')}</TableHead>
                  <TableHead className='w-[96px] text-center'>{renderSortLabel('状态', 'status')}</TableHead>
                  <TableHead>{renderSortLabel('主手机号', 'primaryPhone')}</TableHead>
                  <TableHead>{renderSortLabel('主邮箱', 'primaryEmail')}</TableHead>
                  <TableHead className='w-[132px] text-center'>操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={11}>
                      <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                        <Loader2 className='h-4 w-4 animate-spin' />
                        正在加载用户...
                      </div>
                    </TableCell>
                  </TableRow>
                ) : users.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={11} className='py-8 text-center text-muted-foreground'>
                      暂无用户数据
                    </TableCell>
                  </TableRow>
                ) : (
                  users.map((item) => (
                    <TableRow
                      key={item.userId}
                      data-state={selectedUserId === item.userId ? 'selected' : undefined}
                      className='cursor-pointer'
                      onClick={() => setSelectedUser(item)}
                    >
                      <TableCell
                        onClick={(event) => event.stopPropagation()}
                      >
                        <Checkbox
                          checked={item.userId != null && selectedUserIds.has(item.userId)}
                          onCheckedChange={(checked) => toggleRowSelect(item.userId, Boolean(checked))}
                          aria-label='选择用户'
                        />
                      </TableCell>
                      <TableCell>{item.userCode || '-'}</TableCell>
                      <TableCell>{item.username || '-'}</TableCell>
                      <TableCell>{item.displayName || '-'}</TableCell>
                      <TableCell>{item.nickname || '-'}</TableCell>
                      <TableCell>{item.primaryEstabName || '-'}</TableCell>
                      <TableCell>{toUserTypeLabel(item.userType)}</TableCell>
                      <TableCell className='text-center'>
                        <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                          {toStatusLabel(item.status)}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className='flex items-center gap-2'>
                          <span>{visiblePhoneUserIds.has(item.userId ?? -1) ? (item.primaryPhone || '-') : maskPhone(item.primaryPhone)}</span>
                          <Button
                            type='button'
                            variant='ghost'
                            size='icon'
                            className='h-7 w-7'
                            onClick={(event) => {
                              event.stopPropagation()
                              togglePhoneVisible(item.userId)
                            }}
                          >
                            {visiblePhoneUserIds.has(item.userId ?? -1) ? (
                              <EyeOff className='h-4 w-4' />
                            ) : (
                              <Eye className='h-4 w-4' />
                            )}
                          </Button>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className='flex items-center gap-2'>
                          <span>{visibleEmailUserIds.has(item.userId ?? -1) ? (item.primaryEmail || '-') : maskEmail(item.primaryEmail)}</span>
                          <Button
                            type='button'
                            variant='ghost'
                            size='icon'
                            className='h-7 w-7'
                            onClick={(event) => {
                              event.stopPropagation()
                              toggleEmailVisible(item.userId)
                            }}
                          >
                            {visibleEmailUserIds.has(item.userId ?? -1) ? (
                              <EyeOff className='h-4 w-4' />
                            ) : (
                              <Eye className='h-4 w-4' />
                            )}
                          </Button>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className='flex items-center justify-center gap-1'>
                          <Button
                            type='button'
                            variant='ghost'
                            size='icon'
                            className='h-8 w-8'
                            onClick={(event) => {
                              event.stopPropagation()
                              openViewUserDialog(item)
                            }}
                          >
                            <Eye className='h-4 w-4' />
                          </Button>
                          <Button
                            type='button'
                            variant='ghost'
                            size='icon'
                            className='h-8 w-8'
                            onClick={(event) => {
                              event.stopPropagation()
                              openEditUserDialog(item)
                            }}
                          >
                            <Pencil className='h-4 w-4' />
                          </Button>
                          <Button
                            type='button'
                            variant='ghost'
                            size='icon'
                            className='h-8 w-8 text-destructive'
                            onClick={(event) => {
                              event.stopPropagation()
                              if (!item.userId) return
                              confirmDeleteUsers([item.userId])
                            }}
                          >
                            <Trash2 className='h-4 w-4' />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
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

      <Dialog open={filterDialogOpen} onOpenChange={setFilterDialogOpen}>
        <DialogContent className='sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>查找用户</DialogTitle>
          </DialogHeader>
          <Form {...searchForm}>
            <form className='grid gap-4 md:grid-cols-2 xl:grid-cols-3' onSubmit={searchForm.handleSubmit(applyFilters)}>
              <FormField
                control={searchForm.control}
                name='userCode'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户编码</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='支持模糊匹配' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={searchForm.control}
                name='username'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户名</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='支持模糊匹配' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={searchForm.control}
                name='displayName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>显示名称</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='支持模糊匹配' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={searchForm.control}
                name='nickname'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>昵称</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='支持模糊匹配' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={searchForm.control}
                name='primaryPhone'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>手机号</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='支持模糊匹配' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={searchForm.control}
                name='primaryEmail'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>邮箱</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='支持模糊匹配' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={searchForm.control}
                name='userType'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户类型</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='all'>全部类型</SelectItem>
                        <SelectItem value='0'>平台用户</SelectItem>
                        <SelectItem value='1'>租户用户</SelectItem>
                        <SelectItem value='2'>合作方</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={searchForm.control}
                name='status'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>状态</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='all'>全部状态</SelectItem>
                        <SelectItem value='1'>启用</SelectItem>
                        <SelectItem value='2'>停用</SelectItem>
                        <SelectItem value='3'>锁定</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter className='xl:col-span-3'>
                <Button type='button' variant='outline' onClick={resetFilters}>
                  重置
                </Button>
                <Button type='submit' className='gap-2'>
                  <SearchIcon className='h-4 w-4' />
                  查找
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <Dialog
        open={userDialogOpen}
        onOpenChange={(open) => {
          if (!open) {
            closeUserDialog()
            return
          }
          setUserDialogOpen(true)
        }}
      >
        <DialogContent className='max-h-[92vh] overflow-y-auto sm:max-w-5xl'>
          <DialogHeader>
            <DialogTitle>
              {isUserViewMode ? '查看用户' : editingUser ? '编辑用户' : '新建用户'}
            </DialogTitle>
            <DialogDescription>维护用户主档信息，保存后可继续维护身份凭证与所属企业。</DialogDescription>
          </DialogHeader>
          <Form {...userForm}>
            <form className='grid gap-4 md:grid-cols-2 xl:grid-cols-3' onSubmit={userForm.handleSubmit(submitUser)}>
              {!editingUser && !isUserViewMode ? (
                <FormItem>
                  <FormLabel>用户编码</FormLabel>
                  <FormControl>
                    <Input value='系统自动生成' disabled />
                  </FormControl>
                </FormItem>
              ) : null}
              <FormField
                control={userForm.control}
                name='username'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户名</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={Boolean(editingUser) || isUserViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='displayName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>显示名称</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={isUserViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='nickname'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>昵称</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={isUserViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='gender'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>性别</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={isUserViewMode}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='0'>未知</SelectItem>
                        <SelectItem value='1'>男</SelectItem>
                        <SelectItem value='2'>女</SelectItem>
                        <SelectItem value='3'>保密</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='birthday'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>生日</FormLabel>
                    <FormControl>
                      <Input {...field} type='date' disabled={isUserViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='userType'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户类型</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={isUserViewMode}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='0'>平台用户</SelectItem>
                        <SelectItem value='1'>租户用户</SelectItem>
                        <SelectItem value='2'>合作方</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='status'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>状态</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={isUserViewMode}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>启用</SelectItem>
                        <SelectItem value='2'>停用</SelectItem>
                        <SelectItem value='3'>锁定</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='primaryEstabId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>所属企业</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value || undefined} disabled={isUserViewMode || estabLoading}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue placeholder={estabLoading ? '企业加载中...' : '请选择企业'} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {estabOptions.map((estab) => (
                          <SelectItem key={estab.id} value={String(estab.id)}>
                            {estab.estabName || estab.estabShortName || `企业 ${estab.id}`}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='primaryPhone'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>主手机号</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={isUserViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='phoneVerified'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>手机号已验证</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={isUserViewMode}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>是</SelectItem>
                        <SelectItem value='0'>否</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='primaryEmail'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>主邮箱</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={isUserViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='emailVerified'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>邮箱已验证</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={isUserViewMode}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>是</SelectItem>
                        <SelectItem value='0'>否</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={userForm.control}
                name='remark'
                render={({ field }) => (
                  <FormItem className='xl:col-span-3'>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Textarea rows={3} {...field} disabled={isUserViewMode} placeholder='请输入备注说明（可选）' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter className='xl:col-span-3'>
                <Button type='button' variant='outline' onClick={closeUserDialog}>
                  {isUserViewMode ? '关闭' : '取消'}
                </Button>
                {!isUserViewMode ? (
                  <Button type='submit' disabled={savingUser}>
                    {savingUser ? (
                      <>
                        <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                        保存中...
                      </>
                    ) : (
                      '保存用户'
                    )}
                  </Button>
                ) : null}
              </DialogFooter>
            </form>
          </Form>
          {editingUser?.userId ? (
            <div className='mt-2 border-t pt-4'>
              <Tabs
                value={userDialogTab}
                onValueChange={(value) => setUserDialogTab(value as 'identities' | 'estabs')}
              >
                <TabsList className='grid h-auto w-full grid-cols-2 gap-1.5 lg:w-[360px]'>
                  <TabsTrigger value='identities' className='gap-1.5'>
                    <Fingerprint className='h-3.5 w-3.5' />
                    用户身份管理
                  </TabsTrigger>
                  <TabsTrigger value='estabs' className='gap-1.5'>
                    <Building2 className='h-3.5 w-3.5' />
                    所属企业
                  </TabsTrigger>
                </TabsList>

                <TabsContent value='identities' className='mt-4 space-y-3'>
                  <div className='flex items-center justify-between'>
                    {/* <div className='text-sm text-muted-foreground'>{selectedUserSummary}</div> */}
                    {!isUserViewMode ? (
                      <Button type='button' onClick={openCreateIdentityDialog} className='gap-2'>
                        <Plus className='h-4 w-4' />
                        新增身份
                      </Button>
                    ) : null}
                  </div>

                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>身份类型</TableHead>
                        <TableHead>身份标识</TableHead>
                        <TableHead>发行方</TableHead>
                        <TableHead className='w-[88px] text-center'>主身份</TableHead>
                        <TableHead className='w-[88px] text-center'>已验证</TableHead>
                        <TableHead className='w-[88px] text-center'>状态</TableHead>
                        <TableHead>绑定时间</TableHead>
                        <TableHead className='w-[132px] text-center'>操作</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {identityLoading ? (
                        <TableRow>
                          <TableCell colSpan={8}>
                            <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                              <Loader2 className='h-4 w-4 animate-spin' />
                              正在加载身份...
                            </div>
                          </TableCell>
                        </TableRow>
                      ) : identities.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={8} className='py-8 text-center text-muted-foreground'>
                            暂无身份数据
                          </TableCell>
                        </TableRow>
                      ) : (
                        identities.map((item) => (
                          <TableRow key={item.identityId}>
                            <TableCell>{toIdentityTypeLabel(item.identityType)}</TableCell>
                            <TableCell>{item.identifier || '-'}</TableCell>
                            <TableCell>{item.issuer || '-'}</TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.isPrimary === 1 ? 'default' : 'secondary'}>
                                {item.isPrimary === 1 ? '是' : '否'}
                              </Badge>
                            </TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.verified === 1 ? 'default' : 'secondary'}>
                                {item.verified === 1 ? '是' : '否'}
                              </Badge>
                            </TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                                {item.status === 1 ? '启用' : '停用'}
                              </Badge>
                            </TableCell>
                            <TableCell>{formatDateTime(item.bindTime)}</TableCell>
                            <TableCell>
                              {isUserViewMode ? (
                                <div className='text-center text-muted-foreground'>-</div>
                              ) : (
                                <div className='flex items-center justify-center gap-1'>
                                  <Button
                                    type='button'
                                    variant='ghost'
                                    size='icon'
                                    className='h-8 w-8'
                                    onClick={() => openEditIdentityDialog(item)}
                                  >
                                    <Pencil className='h-4 w-4' />
                                  </Button>
                                  <Button
                                    type='button'
                                    variant='ghost'
                                    size='icon'
                                    className='h-8 w-8 text-destructive'
                                    onClick={() => setDeletingIdentity(item)}
                                  >
                                    <Trash2 className='h-4 w-4' />
                                  </Button>
                                </div>
                              )}
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>

                  <PageToolbar
                    page={identityQuery.currentPage}
                    size={identityQuery.pageSize}
                    total={identityTotal}
                    loading={identityLoading}
                    onPageChange={(page) => setIdentityQuery((prev) => ({ ...prev, currentPage: page }))}
                    onPageSizeChange={(size) => setIdentityQuery({ currentPage: 1, pageSize: size })}
                  />
                </TabsContent>

                <TabsContent value='estabs' className='mt-4 space-y-3'>
                  {/* <div className='text-sm text-muted-foreground'>
                    用户：{selectedUserDisplay}
                  </div> */}
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>企业名称</TableHead>
                        <TableHead>企业编码</TableHead>
                        <TableHead>企业简称</TableHead>
                        <TableHead className='w-[96px] text-center'>管理员</TableHead>
                        <TableHead className='w-[96px] text-center'>当前活跃</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {userEstabsLoading ? (
                        <TableRow>
                          <TableCell colSpan={5}>
                            <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                              <Loader2 className='h-4 w-4 animate-spin' />
                              正在加载所属企业...
                            </div>
                          </TableCell>
                        </TableRow>
                      ) : userEstabs.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={5} className='py-8 text-center text-muted-foreground'>
                            暂无所属企业
                          </TableCell>
                        </TableRow>
                      ) : (
                        userEstabs.map((item) => (
                          <TableRow key={item.estabId}>
                            <TableCell>{item.estabName || '-'}</TableCell>
                            <TableCell>{item.estabCode || '-'}</TableCell>
                            <TableCell>{item.estabShortName || '-'}</TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.admin ? 'default' : 'secondary'}>
                                {item.admin ? '是' : '否'}
                              </Badge>
                            </TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.current ? 'default' : 'secondary'}>
                                {item.current ? '是' : '否'}
                              </Badge>
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TabsContent>
              </Tabs>
            </div>
          ) : (
            <p className='mt-2 text-sm text-muted-foreground'>先保存用户基础信息后，再维护身份凭证。</p>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={identityDialogOpen} onOpenChange={setIdentityDialogOpen}>
        <DialogContent className='max-h-[92vh] overflow-y-auto sm:max-w-xl'>
          <DialogHeader>
            <DialogTitle>{editingIdentity ? '编辑身份' : '新增身份'}</DialogTitle>
            <DialogDescription>维护用户身份标识、凭证算法与验证状态。</DialogDescription>
          </DialogHeader>
          <Form {...identityForm}>
            <form className='grid gap-4' onSubmit={identityForm.handleSubmit(submitIdentity)}>
              <FormField
                control={identityForm.control}
                name='identityType'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>身份类型</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={Boolean(editingIdentity)}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>用户名密码</SelectItem>
                        <SelectItem value='2'>手机号短信</SelectItem>
                        <SelectItem value='3'>邮箱密码</SelectItem>
                        <SelectItem value='4'>邮箱验证码</SelectItem>
                        <SelectItem value='5'>微信扫码</SelectItem>
                        <SelectItem value='6'>OAuth/OIDC</SelectItem>
                        <SelectItem value='7'>SAML</SelectItem>
                        <SelectItem value='8'>TOTP</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={identityForm.control}
                name='identifier'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>身份标识</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={identityForm.control}
                name='issuer'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>发行方</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={identityForm.control}
                name='credential'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>凭证（可选）</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={identityForm.control}
                name='credentialAlg'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>凭证算法</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='例如：bcrypt' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className='grid grid-cols-3 gap-3'>
                <FormField
                  control={identityForm.control}
                  name='isPrimary'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>主身份</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='1'>是</SelectItem>
                          <SelectItem value='0'>否</SelectItem>
                        </SelectContent>
                      </Select>
                    </FormItem>
                  )}
                />
                <FormField
                  control={identityForm.control}
                  name='verified'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>已验证</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='1'>是</SelectItem>
                          <SelectItem value='0'>否</SelectItem>
                        </SelectContent>
                      </Select>
                    </FormItem>
                  )}
                />
                <FormField
                  control={identityForm.control}
                  name='status'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>状态</FormLabel>
                      <Select onValueChange={field.onChange} value={field.value}>
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='1'>启用</SelectItem>
                          <SelectItem value='0'>停用</SelectItem>
                        </SelectContent>
                      </Select>
                    </FormItem>
                  )}
                />
              </div>
              <DialogFooter>
                <Button type='button' variant='outline' onClick={closeIdentityDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={savingIdentity}>
                  {savingIdentity ? (
                    <>
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      保存中...
                    </>
                  ) : (
                    '保存身份'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={deleteConfirmOpen}
        onOpenChange={(open) => {
          setDeleteConfirmOpen(open)
          if (!open) {
            setPendingDeleteUserIds([])
          }
        }}
        title='删除用户'
        desc={
          pendingDeleteUserIds.length > 1
            ? `将删除已选择的 ${pendingDeleteUserIds.length} 个用户，同时移除其身份与企业绑定关系。`
            : '将删除该用户，同时移除其身份与企业绑定关系。'
        }
        confirmText={deletingUsersLoading ? '删除中...' : '确认删除'}
        destructive
        isLoading={deletingUsersLoading}
        handleConfirm={() => void deleteUsersConfirmed()}
      />

      <ConfirmDialog
        open={Boolean(deletingIdentity)}
        onOpenChange={(open) => !open && setDeletingIdentity(null)}
        title='删除身份'
        desc='将删除该身份凭证，删除后用户可能无法使用对应方式登录。'
        confirmText={deletingIdentityLoading ? '删除中...' : '确认删除'}
        destructive
        isLoading={deletingIdentityLoading}
        handleConfirm={() => void confirmDeleteIdentity()}
      />
    </>
  )
}
