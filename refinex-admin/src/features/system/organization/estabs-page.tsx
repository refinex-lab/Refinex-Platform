import { useEffect, useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  Loader2,
  MapPin,
  Pencil,
  Plus,
  RefreshCw,
  Search as SearchIcon,
  Shield,
  Trash2,
  Users,
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
  createEstab,
  createEstabAddress,
  createEstabUser,
  deleteEstab,
  deleteEstabAddress,
  deleteEstabUser,
  getEstabAuthPolicy,
  listEstabAddresses,
  listEstabUsers,
  listEstabs,
  type Estab,
  type EstabAddress,
  type EstabAddressCreateRequest,
  type EstabAddressListQuery,
  type EstabAddressUpdateRequest,
  type EstabAuthPolicy,
  type EstabAuthPolicyUpdateRequest,
  type EstabCreateRequest,
  type EstabListQuery,
  type EstabUpdateRequest,
  type EstabUser,
  type EstabUserCreateRequest,
  type EstabUserListQuery,
  type EstabUserUpdateRequest,
  updateEstab,
  updateEstabAddress,
  updateEstabAuthPolicy,
  updateEstabUser,
} from '@/features/system/api'
import {
  formatDateTime,
  normalizeDateTimeLocal,
  toDateTimeLocalValue,
  toOptionalNumber,
  toOptionalString,
} from '@/features/system/common'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const estabFormSchema = z.object({
  estabCode: z.string().trim().min(1, '企业编码不能为空').max(64, '企业编码最长 64 位'),
  estabName: z.string().trim().min(1, '企业名称不能为空').max(128, '企业名称最长 128 位'),
  estabShortName: z.string().trim().max(64, '简称最长 64 位').optional(),
  estabType: z.enum(['1', '2', '3']),
  status: z.enum(['1', '2']),
  ownerUserId: z.string().trim().max(20, '负责人用户ID格式非法').optional(),
  contactName: z.string().trim().max(64, '联系人最长 64 位').optional(),
  contactPhone: z.string().trim().max(32, '联系电话最长 32 位').optional(),
  contactEmail: z.string().trim().max(128, '联系邮箱最长 128 位').optional(),
  websiteUrl: z.string().trim().max(255, '官网地址最长 255 位').optional(),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

const addressFormSchema = z.object({
  addrType: z.enum(['1', '2']),
  countryCode: z.string().trim().max(16, '国家编码最长 16 位').optional(),
  provinceName: z.string().trim().max(64, '省份名称最长 64 位').optional(),
  cityName: z.string().trim().max(64, '城市名称最长 64 位').optional(),
  districtName: z.string().trim().max(64, '区县名称最长 64 位').optional(),
  addressLine1: z.string().trim().max(255, '地址最长 255 位').optional(),
  addressLine2: z.string().trim().max(255, '地址最长 255 位').optional(),
  postalCode: z.string().trim().max(32, '邮编最长 32 位').optional(),
  isDefault: z.enum(['0', '1']),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

const estabUserFormSchema = z.object({
  userId: z.string().trim().min(1, '用户ID不能为空').max(20, '用户ID格式非法'),
  memberType: z.enum(['1', '2', '3']),
  isAdmin: z.enum(['0', '1']),
  status: z.enum(['1', '2']),
  positionTitle: z.string().trim().max(128, '岗位最长 128 位').optional(),
  joinTime: z.string().trim().optional(),
  leaveTime: z.string().trim().optional(),
})

const policyFormSchema = z.object({
  passwordLoginEnabled: z.enum(['0', '1']),
  smsLoginEnabled: z.enum(['0', '1']),
  emailLoginEnabled: z.enum(['0', '1']),
  wechatLoginEnabled: z.enum(['0', '1']),
  mfaRequired: z.enum(['0', '1']),
  passwordMinLen: z.string().trim().max(3, '最大三位数字').optional(),
  loginFailThreshold: z.string().trim().max(3, '最大三位数字').optional(),
  lockMinutes: z.string().trim().max(4, '最大四位数字').optional(),
  sessionTimeoutMinutes: z.string().trim().max(5, '最大五位数字').optional(),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

type EstabFormValues = z.infer<typeof estabFormSchema>
type AddressFormValues = z.infer<typeof addressFormSchema>
type EstabUserFormValues = z.infer<typeof estabUserFormSchema>
type PolicyFormValues = z.infer<typeof policyFormSchema>

const DEFAULT_ESTAB_FORM: EstabFormValues = {
  estabCode: '',
  estabName: '',
  estabShortName: '',
  estabType: '1',
  status: '1',
  ownerUserId: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  websiteUrl: '',
  remark: '',
}

const DEFAULT_ADDRESS_FORM: AddressFormValues = {
  addrType: '1',
  countryCode: 'CN',
  provinceName: '',
  cityName: '',
  districtName: '',
  addressLine1: '',
  addressLine2: '',
  postalCode: '',
  isDefault: '0',
  remark: '',
}

const DEFAULT_ESTAB_USER_FORM: EstabUserFormValues = {
  userId: '',
  memberType: '1',
  isAdmin: '0',
  status: '1',
  positionTitle: '',
  joinTime: '',
  leaveTime: '',
}

const DEFAULT_POLICY_FORM: PolicyFormValues = {
  passwordLoginEnabled: '1',
  smsLoginEnabled: '1',
  emailLoginEnabled: '1',
  wechatLoginEnabled: '0',
  mfaRequired: '0',
  passwordMinLen: '8',
  loginFailThreshold: '5',
  lockMinutes: '30',
  sessionTimeoutMinutes: '120',
  remark: '',
}

function toEstabTypeLabel(type?: number): string {
  if (type === 1) return '租户企业'
  if (type === 2) return '平台组织'
  if (type === 3) return '合作伙伴'
  return '-'
}

function toStatusLabel(status?: number): string {
  if (status === 1) return '启用'
  if (status === 2) return '停用'
  return '-'
}

function toAddressTypeLabel(type?: number): string {
  if (type === 1) return '办公地址'
  if (type === 2) return '账单地址'
  return '-'
}

function toMemberTypeLabel(type?: number): string {
  if (type === 1) return '正式成员'
  if (type === 2) return '外部协作'
  if (type === 3) return '顾问/兼职'
  return '-'
}

function parsePositiveInteger(value?: string): number | undefined {
  const parsed = toOptionalNumber(value)
  if (parsed == null || parsed <= 0) return undefined
  return parsed
}

export function EstabsPage() {
  const [estabs, setEstabs] = useState<Estab[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [keywordInput, setKeywordInput] = useState('')
  const [statusInput, setStatusInput] = useState<'all' | '1' | '2'>('all')
  const [estabTypeInput, setEstabTypeInput] = useState<'all' | '1' | '2' | '3'>('all')
  const [query, setQuery] = useState<EstabListQuery>({ currentPage: 1, pageSize: 10 })

  const [selectedEstab, setSelectedEstab] = useState<Estab | null>(null)
  const selectedEstabId = selectedEstab?.id

  const [activeTab, setActiveTab] = useState<'addresses' | 'members' | 'policy'>('addresses')

  const [addresses, setAddresses] = useState<EstabAddress[]>([])
  const [addressTotal, setAddressTotal] = useState(0)
  const [addressLoading, setAddressLoading] = useState(false)
  const [addressQuery, setAddressQuery] = useState<EstabAddressListQuery>({
    currentPage: 1,
    pageSize: 10,
  })

  const [members, setMembers] = useState<EstabUser[]>([])
  const [memberTotal, setMemberTotal] = useState(0)
  const [memberLoading, setMemberLoading] = useState(false)
  const [memberQuery, setMemberQuery] = useState<EstabUserListQuery>({
    currentPage: 1,
    pageSize: 10,
  })

  const [policyLoading, setPolicyLoading] = useState(false)
  const [policySaving, setPolicySaving] = useState(false)

  const [estabDialogOpen, setEstabDialogOpen] = useState(false)
  const [savingEstab, setSavingEstab] = useState(false)
  const [editingEstab, setEditingEstab] = useState<Estab | null>(null)
  const [deletingEstab, setDeletingEstab] = useState<Estab | null>(null)
  const [deletingEstabLoading, setDeletingEstabLoading] = useState(false)

  const [addressDialogOpen, setAddressDialogOpen] = useState(false)
  const [savingAddress, setSavingAddress] = useState(false)
  const [editingAddress, setEditingAddress] = useState<EstabAddress | null>(null)
  const [deletingAddress, setDeletingAddress] = useState<EstabAddress | null>(null)
  const [deletingAddressLoading, setDeletingAddressLoading] = useState(false)

  const [memberDialogOpen, setMemberDialogOpen] = useState(false)
  const [savingMember, setSavingMember] = useState(false)
  const [editingMember, setEditingMember] = useState<EstabUser | null>(null)
  const [deletingMember, setDeletingMember] = useState<EstabUser | null>(null)
  const [deletingMemberLoading, setDeletingMemberLoading] = useState(false)

  const estabForm = useForm<EstabFormValues>({
    resolver: zodResolver(estabFormSchema),
    defaultValues: DEFAULT_ESTAB_FORM,
    mode: 'onChange',
  })
  const addressForm = useForm<AddressFormValues>({
    resolver: zodResolver(addressFormSchema),
    defaultValues: DEFAULT_ADDRESS_FORM,
    mode: 'onChange',
  })
  const memberForm = useForm<EstabUserFormValues>({
    resolver: zodResolver(estabUserFormSchema),
    defaultValues: DEFAULT_ESTAB_USER_FORM,
    mode: 'onChange',
  })
  const policyForm = useForm<PolicyFormValues>({
    resolver: zodResolver(policyFormSchema),
    defaultValues: DEFAULT_POLICY_FORM,
    mode: 'onChange',
  })

  const selectedEstabSummary = useMemo(() => {
    if (!selectedEstab) {
      return '请从上方企业列表中选择要管理的企业。'
    }
    return `当前企业：${selectedEstab.estabName || '-'}（${selectedEstab.estabCode || '-'}）`
  }, [selectedEstab])

  async function loadEstabs(activeQuery: EstabListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listEstabs(activeQuery)
      const rows = pageData.data ?? []
      setEstabs(rows)
      setTotal(pageData.total ?? 0)

      setSelectedEstab((prev) => {
        if (rows.length === 0) return null
        if (!prev?.id) return rows[0]
        const matched = rows.find((item) => item.id === prev.id)
        return matched ?? rows[0]
      })
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  async function loadAddresses(estabId: number, activeQuery: EstabAddressListQuery = addressQuery) {
    setAddressLoading(true)
    try {
      const pageData = await listEstabAddresses(estabId, activeQuery)
      setAddresses(pageData.data ?? [])
      setAddressTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setAddressLoading(false)
    }
  }

  async function loadMembers(estabId: number, activeQuery: EstabUserListQuery = memberQuery) {
    setMemberLoading(true)
    try {
      const pageData = await listEstabUsers(estabId, activeQuery)
      setMembers(pageData.data ?? [])
      setMemberTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setMemberLoading(false)
    }
  }

  async function loadPolicy(estabId: number) {
    setPolicyLoading(true)
    try {
      const policy = await getEstabAuthPolicy(estabId)
      applyPolicyToForm(policy)
    } catch (error) {
      handleServerError(error)
    } finally {
      setPolicyLoading(false)
    }
  }

  function applyPolicyToForm(policy?: EstabAuthPolicy) {
    policyForm.reset({
      passwordLoginEnabled: String(policy?.passwordLoginEnabled ?? 1) as '0' | '1',
      smsLoginEnabled: String(policy?.smsLoginEnabled ?? 1) as '0' | '1',
      emailLoginEnabled: String(policy?.emailLoginEnabled ?? 1) as '0' | '1',
      wechatLoginEnabled: String(policy?.wechatLoginEnabled ?? 0) as '0' | '1',
      mfaRequired: String(policy?.mfaRequired ?? 0) as '0' | '1',
      passwordMinLen: String(policy?.passwordMinLen ?? 8),
      loginFailThreshold: String(policy?.loginFailThreshold ?? 5),
      lockMinutes: String(policy?.lockMinutes ?? 30),
      sessionTimeoutMinutes: String(policy?.sessionTimeoutMinutes ?? 120),
      remark: policy?.remark ?? '',
    })
  }

  useEffect(() => {
    void loadEstabs(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  useEffect(() => {
    if (!selectedEstabId) {
      setAddresses([])
      setAddressTotal(0)
      setMembers([])
      setMemberTotal(0)
      policyForm.reset(DEFAULT_POLICY_FORM)
      return
    }
    setAddressQuery((prev) => ({ ...prev, currentPage: 1 }))
    setMemberQuery((prev) => ({ ...prev, currentPage: 1 }))
    void loadPolicy(selectedEstabId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedEstabId])

  useEffect(() => {
    if (!selectedEstabId) return
    void loadAddresses(selectedEstabId, addressQuery)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedEstabId, addressQuery])

  useEffect(() => {
    if (!selectedEstabId) return
    void loadMembers(selectedEstabId, memberQuery)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedEstabId, memberQuery])

  function applyFilters() {
    setQuery({
      keyword: toOptionalString(keywordInput),
      status: statusInput === 'all' ? undefined : Number(statusInput),
      estabType: estabTypeInput === 'all' ? undefined : Number(estabTypeInput),
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function resetFilters() {
    setKeywordInput('')
    setStatusInput('all')
    setEstabTypeInput('all')
    setQuery({ currentPage: 1, pageSize: query.pageSize ?? 10 })
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  function openCreateEstabDialog() {
    setEditingEstab(null)
    setSelectedEstab(null)
    estabForm.reset(DEFAULT_ESTAB_FORM)
    setEstabDialogOpen(true)
  }

  function openEditEstabDialog(estab: Estab) {
    setSelectedEstab(estab)
    setEditingEstab(estab)
    estabForm.reset({
      estabCode: estab.estabCode ?? '',
      estabName: estab.estabName ?? '',
      estabShortName: estab.estabShortName ?? '',
      estabType: String(estab.estabType ?? 1) as '1' | '2' | '3',
      status: String(estab.status ?? 1) as '1' | '2',
      ownerUserId: estab.ownerUserId == null ? '' : String(estab.ownerUserId),
      contactName: estab.contactName ?? '',
      contactPhone: estab.contactPhone ?? '',
      contactEmail: estab.contactEmail ?? '',
      websiteUrl: estab.websiteUrl ?? '',
      remark: estab.remark ?? '',
    })
    setEstabDialogOpen(true)
  }

  function closeEstabDialog() {
    setEstabDialogOpen(false)
    setEditingEstab(null)
    estabForm.reset(DEFAULT_ESTAB_FORM)
  }

  async function submitEstab(values: EstabFormValues) {
    setSavingEstab(true)
    try {
      const basePayload: EstabUpdateRequest = {
        estabName: values.estabName.trim(),
        estabShortName: toOptionalString(values.estabShortName),
        estabType: Number(values.estabType),
        status: Number(values.status),
        ownerUserId: parsePositiveInteger(values.ownerUserId),
        contactName: toOptionalString(values.contactName),
        contactPhone: toOptionalString(values.contactPhone),
        contactEmail: toOptionalString(values.contactEmail),
        websiteUrl: toOptionalString(values.websiteUrl),
        remark: toOptionalString(values.remark),
      }

      if (editingEstab?.id) {
        await updateEstab(editingEstab.id, basePayload)
        toast.success('企业信息已更新。')
      } else {
        const payload: EstabCreateRequest = {
          ...basePayload,
          estabCode: values.estabCode.trim(),
          estabName: values.estabName.trim(),
        }
        await createEstab(payload)
        toast.success('企业已创建。')
      }

      closeEstabDialog()
      await loadEstabs(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingEstab(false)
    }
  }

  async function confirmDeleteEstab() {
    if (!deletingEstab?.id) return
    setDeletingEstabLoading(true)
    try {
      await deleteEstab(deletingEstab.id)
      toast.success('企业已删除。')
      setDeletingEstab(null)
      await loadEstabs(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingEstabLoading(false)
    }
  }

  function openCreateAddressDialog() {
    if (!selectedEstabId) {
      toast.error('请先选择企业。')
      return
    }
    setEditingAddress(null)
    addressForm.reset(DEFAULT_ADDRESS_FORM)
    setAddressDialogOpen(true)
  }

  function openEditAddressDialog(address: EstabAddress) {
    setEditingAddress(address)
    addressForm.reset({
      addrType: String(address.addrType ?? 1) as '1' | '2',
      countryCode: address.countryCode ?? 'CN',
      provinceName: address.provinceName ?? '',
      cityName: address.cityName ?? '',
      districtName: address.districtName ?? '',
      addressLine1: address.addressLine1 ?? '',
      addressLine2: address.addressLine2 ?? '',
      postalCode: address.postalCode ?? '',
      isDefault: String(address.isDefault ?? 0) as '0' | '1',
      remark: address.remark ?? '',
    })
    setAddressDialogOpen(true)
  }

  function closeAddressDialog() {
    setAddressDialogOpen(false)
    setEditingAddress(null)
    addressForm.reset(DEFAULT_ADDRESS_FORM)
  }

  async function submitAddress(values: AddressFormValues) {
    if (!selectedEstabId) {
      toast.error('请先选择企业。')
      return
    }
    setSavingAddress(true)
    try {
      const payload: EstabAddressUpdateRequest = {
        addrType: Number(values.addrType),
        countryCode: toOptionalString(values.countryCode),
        provinceName: toOptionalString(values.provinceName),
        cityName: toOptionalString(values.cityName),
        districtName: toOptionalString(values.districtName),
        addressLine1: toOptionalString(values.addressLine1),
        addressLine2: toOptionalString(values.addressLine2),
        postalCode: toOptionalString(values.postalCode),
        isDefault: Number(values.isDefault),
        remark: toOptionalString(values.remark),
      }

      if (editingAddress?.id) {
        await updateEstabAddress(editingAddress.id, payload)
        toast.success('地址已更新。')
      } else {
        await createEstabAddress(selectedEstabId, payload as EstabAddressCreateRequest)
        toast.success('地址已创建。')
      }

      closeAddressDialog()
      await loadAddresses(selectedEstabId, addressQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingAddress(false)
    }
  }

  async function confirmDeleteAddress() {
    if (!selectedEstabId || !deletingAddress?.id) return
    setDeletingAddressLoading(true)
    try {
      await deleteEstabAddress(deletingAddress.id)
      toast.success('地址已删除。')
      setDeletingAddress(null)
      await loadAddresses(selectedEstabId, addressQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingAddressLoading(false)
    }
  }

  function openCreateMemberDialog() {
    if (!selectedEstabId) {
      toast.error('请先选择企业。')
      return
    }
    setEditingMember(null)
    memberForm.reset(DEFAULT_ESTAB_USER_FORM)
    setMemberDialogOpen(true)
  }

  function openEditMemberDialog(member: EstabUser) {
    setEditingMember(member)
    memberForm.reset({
      userId: member.userId == null ? '' : String(member.userId),
      memberType: String(member.memberType ?? 1) as '1' | '2' | '3',
      isAdmin: String(member.isAdmin ?? 0) as '0' | '1',
      status: String(member.status ?? 1) as '1' | '2',
      positionTitle: member.positionTitle ?? '',
      joinTime: toDateTimeLocalValue(member.joinTime),
      leaveTime: toDateTimeLocalValue(member.leaveTime),
    })
    setMemberDialogOpen(true)
  }

  function closeMemberDialog() {
    setMemberDialogOpen(false)
    setEditingMember(null)
    memberForm.reset(DEFAULT_ESTAB_USER_FORM)
  }

  async function submitMember(values: EstabUserFormValues) {
    if (!selectedEstabId) {
      toast.error('请先选择企业。')
      return
    }
    const userId = parsePositiveInteger(values.userId)
    if (!userId) {
      toast.error('请输入有效的用户ID。')
      return
    }

    setSavingMember(true)
    try {
      const payload: EstabUserUpdateRequest = {
        memberType: Number(values.memberType),
        isAdmin: Number(values.isAdmin),
        status: Number(values.status),
        positionTitle: toOptionalString(values.positionTitle),
        joinTime: normalizeDateTimeLocal(values.joinTime),
        leaveTime: normalizeDateTimeLocal(values.leaveTime),
      }

      if (editingMember?.id) {
        await updateEstabUser(editingMember.id, payload)
        toast.success('成员关系已更新。')
      } else {
        await createEstabUser(selectedEstabId, {
          ...(payload as EstabUserCreateRequest),
          userId,
        })
        toast.success('成员关系已创建。')
      }

      closeMemberDialog()
      await loadMembers(selectedEstabId, memberQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingMember(false)
    }
  }

  async function confirmDeleteMember() {
    if (!selectedEstabId || !deletingMember?.id) return
    setDeletingMemberLoading(true)
    try {
      await deleteEstabUser(deletingMember.id)
      toast.success('成员关系已删除。')
      setDeletingMember(null)
      await loadMembers(selectedEstabId, memberQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingMemberLoading(false)
    }
  }

  async function submitPolicy(values: PolicyFormValues) {
    if (!selectedEstabId) {
      toast.error('请先选择企业。')
      return
    }

    setPolicySaving(true)
    try {
      const payload: EstabAuthPolicyUpdateRequest = {
        passwordLoginEnabled: Number(values.passwordLoginEnabled),
        smsLoginEnabled: Number(values.smsLoginEnabled),
        emailLoginEnabled: Number(values.emailLoginEnabled),
        wechatLoginEnabled: Number(values.wechatLoginEnabled),
        mfaRequired: Number(values.mfaRequired),
        passwordMinLen: toOptionalNumber(values.passwordMinLen),
        loginFailThreshold: toOptionalNumber(values.loginFailThreshold),
        lockMinutes: toOptionalNumber(values.lockMinutes),
        sessionTimeoutMinutes: toOptionalNumber(values.sessionTimeoutMinutes),
        remark: toOptionalString(values.remark),
      }

      await updateEstabAuthPolicy(selectedEstabId, payload)
      toast.success('认证策略已更新。')
      await loadPolicy(selectedEstabId)
    } catch (error) {
      handleServerError(error)
    } finally {
      setPolicySaving(false)
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
        <Card>
          <CardContent className='grid gap-3 lg:grid-cols-[1fr_140px_140px_auto]'>
            <Input
              value={keywordInput}
              placeholder='企业编码 / 企业名称 / 联系人'
              onChange={(event) => setKeywordInput(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault()
                  applyFilters()
                }
              }}
            />
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
            <Select
              value={estabTypeInput}
              onValueChange={(value) => setEstabTypeInput(value as 'all' | '1' | '2' | '3')}
            >
              <SelectTrigger>
                <SelectValue placeholder='企业类型' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部类型</SelectItem>
                <SelectItem value='1'>租户企业</SelectItem>
                <SelectItem value='2'>平台组织</SelectItem>
                <SelectItem value='3'>合作伙伴</SelectItem>
              </SelectContent>
            </Select>
            <div className='flex items-center gap-2'>
              <Button type='button' variant='outline' onClick={applyFilters} className='gap-2'>
                <SearchIcon className='h-4 w-4' />
                查询
              </Button>
              <Button type='button' variant='outline' onClick={resetFilters} className='gap-2'>
                <RefreshCw className='h-4 w-4' />
                重置
              </Button>
              <Button type='button' onClick={openCreateEstabDialog} className='gap-2'>
                <Plus className='h-4 w-4' />
                新建企业
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className='mt-4 overflow-hidden'>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>企业编码</TableHead>
                  <TableHead>企业名称</TableHead>
                  <TableHead>企业类型</TableHead>
                  <TableHead>联系人</TableHead>
                  <TableHead>联系电话</TableHead>
                  <TableHead className='w-[88px] text-center'>状态</TableHead>
                  <TableHead className='w-[132px] text-center'>操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                        <Loader2 className='h-4 w-4 animate-spin' />
                        正在加载企业列表...
                      </div>
                    </TableCell>
                  </TableRow>
                ) : estabs.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className='py-8 text-center text-muted-foreground'>
                      暂无企业数据
                    </TableCell>
                  </TableRow>
                ) : (
                  estabs.map((item) => (
                    <TableRow
                      key={item.id}
                      data-state={selectedEstabId === item.id ? 'selected' : undefined}
                      className='cursor-pointer'
                      onClick={() => setSelectedEstab(item)}
                    >
                      <TableCell>{item.estabCode || '-'}</TableCell>
                      <TableCell>{item.estabName || '-'}</TableCell>
                      <TableCell>{toEstabTypeLabel(item.estabType)}</TableCell>
                      <TableCell>{item.contactName || '-'}</TableCell>
                      <TableCell>{item.contactPhone || '-'}</TableCell>
                      <TableCell className='text-center'>
                        <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                          {toStatusLabel(item.status)}
                        </Badge>
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
                              openEditEstabDialog(item)
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
                              setDeletingEstab(item)
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

      <Dialog open={estabDialogOpen} onOpenChange={setEstabDialogOpen}>
        <DialogContent className='max-h-[92vh] overflow-y-auto sm:max-w-6xl'>
          <DialogHeader>
            <DialogTitle>{editingEstab ? '编辑企业' : '新建企业'}</DialogTitle>
            <DialogDescription>维护企业主体信息，保存后可继续配置地址、成员与策略。</DialogDescription>
          </DialogHeader>
          <Form {...estabForm}>
            <form className='grid gap-4 md:grid-cols-2' onSubmit={estabForm.handleSubmit(submitEstab)}>
              <FormField
                control={estabForm.control}
                name='estabCode'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>企业编码</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={Boolean(editingEstab)} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='estabName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>企业名称</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='estabShortName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>企业简称</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='estabType'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>企业类型</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>租户企业</SelectItem>
                        <SelectItem value='2'>平台组织</SelectItem>
                        <SelectItem value='3'>合作伙伴</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='status'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>状态</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
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
                control={estabForm.control}
                name='ownerUserId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>负责人用户ID</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder='例如：10001' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='contactName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>联系人</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='contactPhone'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>联系电话</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='contactEmail'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>联系邮箱</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='websiteUrl'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>官网地址</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={estabForm.control}
                name='remark'
                render={({ field }) => (
                  <FormItem className='md:col-span-2'>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Textarea rows={3} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter className='md:col-span-2'>
                <Button type='button' variant='outline' onClick={closeEstabDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={savingEstab}>
                  {savingEstab ? (
                    <>
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      保存中...
                    </>
                  ) : (
                    '保存企业'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </Form>
          {editingEstab?.id ? (
            <div className='mt-2 border-t pt-4'>
              <p className='mb-3 text-sm text-muted-foreground'>{selectedEstabSummary}</p>
              <Tabs
                value={activeTab}
                onValueChange={(value) => setActiveTab(value as 'addresses' | 'members' | 'policy')}
              >
                <TabsList className='grid h-auto w-full grid-cols-3 gap-1.5 lg:w-[420px]'>
                  <TabsTrigger value='addresses' className='gap-1.5'>
                    <MapPin className='h-3.5 w-3.5' />
                    地址管理
                  </TabsTrigger>
                  <TabsTrigger value='members' className='gap-1.5'>
                    <Users className='h-3.5 w-3.5' />
                    企业成员
                  </TabsTrigger>
                  <TabsTrigger value='policy' className='gap-1.5'>
                    <Shield className='h-3.5 w-3.5' />
                    认证策略
                  </TabsTrigger>
                </TabsList>

                <TabsContent value='addresses' className='mt-4 space-y-3'>
                  <div className='flex items-center justify-between'>
                    <div className='text-sm text-muted-foreground'>按地址类型维护企业办公地址与账单地址。</div>
                    <Button type='button' onClick={openCreateAddressDialog} className='gap-2'>
                      <Plus className='h-4 w-4' />
                      新增地址
                    </Button>
                  </div>
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>类型</TableHead>
                        <TableHead>地区</TableHead>
                        <TableHead>详细地址</TableHead>
                        <TableHead className='w-[88px] text-center'>默认</TableHead>
                        <TableHead className='w-[132px] text-center'>操作</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {addressLoading ? (
                        <TableRow>
                          <TableCell colSpan={5}>
                            <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                              <Loader2 className='h-4 w-4 animate-spin' />
                              正在加载地址...
                            </div>
                          </TableCell>
                        </TableRow>
                      ) : addresses.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={5} className='py-6 text-center text-muted-foreground'>
                            暂无地址信息
                          </TableCell>
                        </TableRow>
                      ) : (
                        addresses.map((item) => (
                          <TableRow key={item.id}>
                            <TableCell>{toAddressTypeLabel(item.addrType)}</TableCell>
                            <TableCell>
                              {[item.provinceName, item.cityName, item.districtName].filter(Boolean).join(' / ') || '-'}
                            </TableCell>
                            <TableCell>{item.addressLine1 || item.addressLine2 || '-'}</TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.isDefault === 1 ? 'default' : 'secondary'}>
                                {item.isDefault === 1 ? '是' : '否'}
                              </Badge>
                            </TableCell>
                            <TableCell>
                              <div className='flex items-center justify-center gap-1'>
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='h-8 w-8'
                                  onClick={() => openEditAddressDialog(item)}
                                >
                                  <Pencil className='h-4 w-4' />
                                </Button>
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='h-8 w-8 text-destructive'
                                  onClick={() => setDeletingAddress(item)}
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
                    page={addressQuery.currentPage ?? 1}
                    size={addressQuery.pageSize ?? 10}
                    total={addressTotal}
                    loading={addressLoading}
                    onPageChange={(page) => setAddressQuery((prev) => ({ ...prev, currentPage: page }))}
                    onPageSizeChange={(size) =>
                      setAddressQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
                    }
                  />
                </TabsContent>

                <TabsContent value='members' className='mt-4 space-y-3'>
                  <div className='flex items-center justify-between'>
                    <div className='text-sm text-muted-foreground'>维护企业成员关系、管理员标记和在岗状态。</div>
                    <Button type='button' onClick={openCreateMemberDialog} className='gap-2'>
                      <Plus className='h-4 w-4' />
                      新增成员
                    </Button>
                  </div>
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>用户ID</TableHead>
                        <TableHead>成员类型</TableHead>
                        <TableHead className='w-[88px] text-center'>管理员</TableHead>
                        <TableHead className='w-[88px] text-center'>状态</TableHead>
                        <TableHead>岗位</TableHead>
                        <TableHead>加入时间</TableHead>
                        <TableHead className='w-[132px] text-center'>操作</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {memberLoading ? (
                        <TableRow>
                          <TableCell colSpan={7}>
                            <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                              <Loader2 className='h-4 w-4 animate-spin' />
                              正在加载成员...
                            </div>
                          </TableCell>
                        </TableRow>
                      ) : members.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={7} className='py-6 text-center text-muted-foreground'>
                            暂无成员数据
                          </TableCell>
                        </TableRow>
                      ) : (
                        members.map((item) => (
                          <TableRow key={item.id}>
                            <TableCell>{item.userId ?? '-'}</TableCell>
                            <TableCell>{toMemberTypeLabel(item.memberType)}</TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.isAdmin === 1 ? 'default' : 'secondary'}>
                                {item.isAdmin === 1 ? '是' : '否'}
                              </Badge>
                            </TableCell>
                            <TableCell className='text-center'>
                              <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                                {toStatusLabel(item.status)}
                              </Badge>
                            </TableCell>
                            <TableCell>{item.positionTitle || '-'}</TableCell>
                            <TableCell>{formatDateTime(item.joinTime)}</TableCell>
                            <TableCell>
                              <div className='flex items-center justify-center gap-1'>
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='h-8 w-8'
                                  onClick={() => openEditMemberDialog(item)}
                                >
                                  <Pencil className='h-4 w-4' />
                                </Button>
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='h-8 w-8 text-destructive'
                                  onClick={() => setDeletingMember(item)}
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
                    page={memberQuery.currentPage ?? 1}
                    size={memberQuery.pageSize ?? 10}
                    total={memberTotal}
                    loading={memberLoading}
                    onPageChange={(page) => setMemberQuery((prev) => ({ ...prev, currentPage: page }))}
                    onPageSizeChange={(size) =>
                      setMemberQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
                    }
                  />
                </TabsContent>

                <TabsContent value='policy' className='mt-4'>
                  <Form {...policyForm}>
                    <form className='grid gap-4 lg:grid-cols-2' onSubmit={policyForm.handleSubmit(submitPolicy)}>
                      <FormField
                        control={policyForm.control}
                        name='passwordLoginEnabled'
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>密码登录</FormLabel>
                            <Select onValueChange={field.onChange} value={field.value}>
                              <FormControl>
                                <SelectTrigger>
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
                      <FormField
                        control={policyForm.control}
                        name='smsLoginEnabled'
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>短信登录</FormLabel>
                            <Select onValueChange={field.onChange} value={field.value}>
                              <FormControl>
                                <SelectTrigger>
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
                      <FormField
                        control={policyForm.control}
                        name='emailLoginEnabled'
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>邮箱登录</FormLabel>
                            <Select onValueChange={field.onChange} value={field.value}>
                              <FormControl>
                                <SelectTrigger>
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
                      <FormField
                        control={policyForm.control}
                        name='wechatLoginEnabled'
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>微信登录</FormLabel>
                            <Select onValueChange={field.onChange} value={field.value}>
                              <FormControl>
                                <SelectTrigger>
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
                      <FormField
                        control={policyForm.control}
                        name='mfaRequired'
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>MFA 强制校验</FormLabel>
                            <Select onValueChange={field.onChange} value={field.value}>
                              <FormControl>
                                <SelectTrigger>
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
                      <div className='grid grid-cols-3 gap-3 lg:col-span-2'>
                        <FormField
                          control={policyForm.control}
                          name='passwordMinLen'
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>密码最小长度</FormLabel>
                              <FormControl>
                                <Input {...field} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                        <FormField
                          control={policyForm.control}
                          name='loginFailThreshold'
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>失败阈值</FormLabel>
                              <FormControl>
                                <Input {...field} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                        <FormField
                          control={policyForm.control}
                          name='lockMinutes'
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>锁定分钟</FormLabel>
                              <FormControl>
                                <Input {...field} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                      </div>
                      <FormField
                        control={policyForm.control}
                        name='sessionTimeoutMinutes'
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>会话超时（分钟）</FormLabel>
                            <FormControl>
                              <Input {...field} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={policyForm.control}
                        name='remark'
                        render={({ field }) => (
                          <FormItem className='lg:col-span-2'>
                            <FormLabel>策略备注</FormLabel>
                            <FormControl>
                              <Textarea rows={3} {...field} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <div className='lg:col-span-2'>
                        <Button type='submit' disabled={policyLoading || policySaving || !selectedEstabId}>
                          {policySaving ? (
                            <>
                              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                              保存中...
                            </>
                          ) : (
                            '保存认证策略'
                          )}
                        </Button>
                      </div>
                    </form>
                  </Form>
                </TabsContent>
              </Tabs>
            </div>
          ) : (
            <p className='mt-2 text-sm text-muted-foreground'>先保存企业基础信息后，再维护地址、成员与认证策略。</p>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={addressDialogOpen} onOpenChange={setAddressDialogOpen}>
        <DialogContent className='max-h-[92vh] overflow-y-auto sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>{editingAddress ? '编辑地址' : '新增地址'}</DialogTitle>
            <DialogDescription>维护企业地址信息，可区分办公地址和账单地址。</DialogDescription>
          </DialogHeader>
          <Form {...addressForm}>
            <form className='grid gap-4 md:grid-cols-2' onSubmit={addressForm.handleSubmit(submitAddress)}>
              <FormField
                control={addressForm.control}
                name='addrType'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>地址类型</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>办公地址</SelectItem>
                        <SelectItem value='2'>账单地址</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='countryCode'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>国家编码</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='provinceName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>省份</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='cityName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>城市</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='districtName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>区县</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='postalCode'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>邮编</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='addressLine1'
                render={({ field }) => (
                  <FormItem className='md:col-span-2'>
                    <FormLabel>地址行1</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='addressLine2'
                render={({ field }) => (
                  <FormItem className='md:col-span-2'>
                    <FormLabel>地址行2</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={addressForm.control}
                name='isDefault'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>是否默认</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
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
                control={addressForm.control}
                name='remark'
                render={({ field }) => (
                  <FormItem className='md:col-span-2'>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Textarea rows={3} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter className='md:col-span-2'>
                <Button type='button' variant='outline' onClick={closeAddressDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={savingAddress}>
                  {savingAddress ? (
                    <>
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      保存中...
                    </>
                  ) : (
                    '保存地址'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <Dialog open={memberDialogOpen} onOpenChange={setMemberDialogOpen}>
        <DialogContent className='max-h-[92vh] overflow-y-auto sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>{editingMember ? '编辑成员关系' : '新增成员关系'}</DialogTitle>
            <DialogDescription>通过用户ID建立成员关系，并维护管理员标记与在岗状态。</DialogDescription>
          </DialogHeader>
          <Form {...memberForm}>
            <form className='grid gap-4 md:grid-cols-2' onSubmit={memberForm.handleSubmit(submitMember)}>
              <FormField
                control={memberForm.control}
                name='userId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>用户ID</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={Boolean(editingMember)} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={memberForm.control}
                name='memberType'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>成员类型</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>正式成员</SelectItem>
                        <SelectItem value='2'>外部协作</SelectItem>
                        <SelectItem value='3'>顾问/兼职</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={memberForm.control}
                name='isAdmin'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>管理员</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
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
                control={memberForm.control}
                name='status'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>状态</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue />
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
                control={memberForm.control}
                name='positionTitle'
                render={({ field }) => (
                  <FormItem className='md:col-span-2'>
                    <FormLabel>岗位</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={memberForm.control}
                name='joinTime'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>加入时间</FormLabel>
                    <FormControl>
                      <Input {...field} type='datetime-local' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={memberForm.control}
                name='leaveTime'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>离开时间</FormLabel>
                    <FormControl>
                      <Input {...field} type='datetime-local' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter className='md:col-span-2'>
                <Button type='button' variant='outline' onClick={closeMemberDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={savingMember}>
                  {savingMember ? (
                    <>
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      保存中...
                    </>
                  ) : (
                    '保存成员关系'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deletingEstab)}
        onOpenChange={(open) => !open && setDeletingEstab(null)}
        title='删除企业'
        desc={`将删除企业「${deletingEstab?.estabName ?? '-'}」，该操作不可撤销。`}
        confirmText={deletingEstabLoading ? '删除中...' : '确认删除'}
        destructive
        isLoading={deletingEstabLoading}
        handleConfirm={() => void confirmDeleteEstab()}
      />

      <ConfirmDialog
        open={Boolean(deletingAddress)}
        onOpenChange={(open) => !open && setDeletingAddress(null)}
        title='删除地址'
        desc='将删除该地址记录，删除后不可恢复。'
        confirmText={deletingAddressLoading ? '删除中...' : '确认删除'}
        destructive
        isLoading={deletingAddressLoading}
        handleConfirm={() => void confirmDeleteAddress()}
      />

      <ConfirmDialog
        open={Boolean(deletingMember)}
        onOpenChange={(open) => !open && setDeletingMember(null)}
        title='删除成员关系'
        desc='将解除该用户与企业的成员关系，删除后不可恢复。'
        confirmText={deletingMemberLoading ? '删除中...' : '确认删除'}
        destructive
        isLoading={deletingMemberLoading}
        handleConfirm={() => void confirmDeleteMember()}
      />
    </>
  )
}
