import { useEffect, useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
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
import { ConfirmDialog } from '@/components/confirm-dialog'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
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
import { Textarea } from '@/components/ui/textarea'
import { ThemeSwitch } from '@/components/theme-switch'
import {
  createDataResource,
  createDataResourceInterface,
  deleteDataResource,
  deleteDataResourceInterface,
  listDataResourceInterfaces,
  listDataResources,
  listSystems,
  type DataResource,
  type DataResourceCreateRequest,
  type DataResourceInterface,
  type DataResourceInterfaceCreateRequest,
  type DataResourceInterfaceUpdateRequest,
  type DataResourceListQuery,
  type DataResourceUpdateRequest,
  type SystemDefinition,
  updateDataResource,
  updateDataResourceInterface,
} from '@/features/system/api'
import { toOptionalNumber, toOptionalString } from '@/features/system/common'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const SYSTEM_OPTION_PAGE_SIZE = 200
const DATA_RESOURCE_PAGE_SIZE = 10
const DATA_RESOURCE_INTERFACE_PAGE_SIZE = 10

const resourceFormSchema = z.object({
  systemId: z.string().trim().min(1, '请选择所属系统'),
  drsName: z.string().trim().min(1, '资源名称不能为空').max(128, '资源名称最长 128 位'),
  drsType: z.enum(['0', '1', '2', '3']),
  resourceUri: z.string().trim().max(255, '资源标识最长 255 位').optional(),
  ownerEstabId: z.string().trim().max(20, '企业ID格式非法').optional(),
  dataOwnerType: z.enum(['0', '1', '2']),
  status: z.enum(['1', '2']),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

const interfaceFormSchema = z.object({
  interfaceCode: z.string().trim().max(64, '接口编码最长 64 位').optional(),
  interfaceName: z.string().trim().min(1, '接口名称不能为空').max(128, '接口名称最长 128 位'),
  httpMethod: z.string().trim().max(16, 'HTTP 方法最长 16 位').optional(),
  pathPattern: z.string().trim().max(255, '路径最长 255 位').optional(),
  permissionKey: z.string().trim().max(128, '权限标识最长 128 位').optional(),
  status: z.enum(['1', '2']),
  sort: z.string().trim().max(6, '排序值过大').optional(),
})

type ResourceFormValues = z.infer<typeof resourceFormSchema>
type InterfaceFormValues = z.infer<typeof interfaceFormSchema>

const DEFAULT_RESOURCE_FORM: ResourceFormValues = {
  systemId: '',
  drsName: '',
  drsType: '1',
  resourceUri: '',
  ownerEstabId: '',
  dataOwnerType: '1',
  status: '1',
  remark: '',
}

const DEFAULT_INTERFACE_FORM: InterfaceFormValues = {
  interfaceCode: '',
  interfaceName: '',
  httpMethod: '',
  pathPattern: '',
  permissionKey: '',
  status: '1',
  sort: '0',
}

function parsePositiveLong(value?: string): number | undefined {
  const parsed = toOptionalNumber(value)
  if (parsed == null || parsed <= 0) return undefined
  return parsed
}

function toDrsTypeLabel(value?: number): string {
  if (value === 0) return '数据库表'
  if (value === 1) return '接口资源'
  if (value === 2) return '文件'
  if (value === 3) return '其他'
  return '-'
}

function toOwnerTypeLabel(value?: number): string {
  if (value === 0) return '平台'
  if (value === 1) return '租户'
  if (value === 2) return '用户'
  return '-'
}

function toStatusLabel(value?: number): string {
  if (value === 1) return '启用'
  if (value === 2) return '停用'
  return '-'
}

export function DataResourceManagementPage() {
  const [systems, setSystems] = useState<SystemDefinition[]>([])
  const [systemLoading, setSystemLoading] = useState(false)

  const [resources, setResources] = useState<DataResource[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<DataResourceListQuery>({ currentPage: 1, pageSize: DATA_RESOURCE_PAGE_SIZE })

  const [keywordInput, setKeywordInput] = useState('')
  const [statusInput, setStatusInput] = useState<'all' | '1' | '2'>('all')
  const [typeInput, setTypeInput] = useState<'all' | '0' | '1' | '2' | '3'>('all')

  const [selectedResource, setSelectedResource] = useState<DataResource | null>(null)

  const [resourceDialogOpen, setResourceDialogOpen] = useState(false)
  const [editingResource, setEditingResource] = useState<DataResource | null>(null)
  const [savingResource, setSavingResource] = useState(false)
  const [deletingResource, setDeletingResource] = useState<DataResource | null>(null)
  const [deletingResourceLoading, setDeletingResourceLoading] = useState(false)

  const [interfaces, setInterfaces] = useState<DataResourceInterface[]>([])
  const [interfaceTotal, setInterfaceTotal] = useState(0)
  const [interfaceLoading, setInterfaceLoading] = useState(false)
  const [interfaceQuery, setInterfaceQuery] = useState({
    keyword: '',
    status: 'all' as 'all' | '1' | '2',
    currentPage: 1,
    pageSize: DATA_RESOURCE_INTERFACE_PAGE_SIZE,
  })

  const [interfaceDialogOpen, setInterfaceDialogOpen] = useState(false)
  const [editingInterface, setEditingInterface] = useState<DataResourceInterface | null>(null)
  const [savingInterface, setSavingInterface] = useState(false)
  const [deletingInterface, setDeletingInterface] = useState<DataResourceInterface | null>(null)
  const [deletingInterfaceLoading, setDeletingInterfaceLoading] = useState(false)

  const resourceForm = useForm<ResourceFormValues>({
    resolver: zodResolver(resourceFormSchema),
    defaultValues: DEFAULT_RESOURCE_FORM,
    mode: 'onChange',
  })

  const interfaceForm = useForm<InterfaceFormValues>({
    resolver: zodResolver(interfaceFormSchema),
    defaultValues: DEFAULT_INTERFACE_FORM,
    mode: 'onChange',
  })

  const selectedResourceName = useMemo(() => {
    if (!selectedResource) return '-'
    return `${selectedResource.drsName || '-'}（${selectedResource.drsCode || '-'}）`
  }, [selectedResource])

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

  async function loadResources(activeQuery: DataResourceListQuery = query) {
    if (!activeQuery.systemId) {
      setResources([])
      setTotal(0)
      setSelectedResource(null)
      return
    }
    setLoading(true)
    try {
      const pageData = await listDataResources(activeQuery)
      const rows = pageData.data ?? []
      setResources(rows)
      setTotal(pageData.total ?? 0)
      setSelectedResource((prev) => {
        if (rows.length === 0) return null
        if (!prev?.id) return rows[0]
        return rows.find((item) => item.id === prev.id) ?? rows[0]
      })
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  async function loadInterfaces(resourceId: number, activeQuery = interfaceQuery) {
    setInterfaceLoading(true)
    try {
      const pageData = await listDataResourceInterfaces({
        drsId: resourceId,
        keyword: toOptionalString(activeQuery.keyword),
        status: activeQuery.status === 'all' ? undefined : Number(activeQuery.status),
        currentPage: activeQuery.currentPage,
        pageSize: activeQuery.pageSize,
      })
      setInterfaces(pageData.data ?? [])
      setInterfaceTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setInterfaceLoading(false)
    }
  }

  useEffect(() => {
    void loadSystems()
  }, [])

  useEffect(() => {
    void loadResources(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  useEffect(() => {
    if (!selectedResource?.id) {
      setInterfaces([])
      setInterfaceTotal(0)
      return
    }
    void loadInterfaces(selectedResource.id, interfaceQuery)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedResource?.id, interfaceQuery])

  function applyFilter() {
    setQuery((prev) => ({
      systemId: prev.systemId,
      status: statusInput === 'all' ? undefined : Number(statusInput),
      drsType: typeInput === 'all' ? undefined : Number(typeInput),
      keyword: toOptionalString(keywordInput),
      currentPage: 1,
      pageSize: prev.pageSize ?? DATA_RESOURCE_PAGE_SIZE,
    }))
  }

  function resetFilter() {
    setKeywordInput('')
    setStatusInput('all')
    setTypeInput('all')
    setQuery((prev) => ({
      systemId: prev.systemId,
      currentPage: 1,
      pageSize: prev.pageSize ?? DATA_RESOURCE_PAGE_SIZE,
    }))
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  function handleInterfacePageChange(page: number) {
    setInterfaceQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handleInterfacePageSizeChange(size: number) {
    setInterfaceQuery((prev) => ({ ...prev, currentPage: 1, pageSize: size }))
  }

  function openCreateResourceDialog() {
    setEditingResource(null)
    resourceForm.reset({
      ...DEFAULT_RESOURCE_FORM,
      systemId: query.systemId ? String(query.systemId) : systems[0]?.id ? String(systems[0].id) : '',
    })
    setResourceDialogOpen(true)
  }

  function openEditResourceDialog(resource: DataResource) {
    setEditingResource(resource)
    resourceForm.reset({
      systemId: String(resource.systemId ?? query.systemId ?? ''),
      drsName: resource.drsName ?? '',
      drsType: String(resource.drsType ?? 1) as '0' | '1' | '2' | '3',
      resourceUri: resource.resourceUri ?? '',
      ownerEstabId:
        resource.ownerEstabId == null || resource.ownerEstabId === 0 ? '' : String(resource.ownerEstabId),
      dataOwnerType: String(resource.dataOwnerType ?? 1) as '0' | '1' | '2',
      status: String(resource.status ?? 1) as '1' | '2',
      remark: resource.remark ?? '',
    })
    setResourceDialogOpen(true)
  }

  function closeResourceDialog() {
    setResourceDialogOpen(false)
    setEditingResource(null)
    resourceForm.reset(DEFAULT_RESOURCE_FORM)
  }

  async function submitResource(values: ResourceFormValues) {
    const systemId = parsePositiveLong(values.systemId)
    if (!systemId) {
      toast.error('请选择所属系统')
      return
    }

    setSavingResource(true)
    try {
      const basePayload: DataResourceUpdateRequest = {
        drsName: values.drsName.trim(),
        drsType: Number(values.drsType),
        resourceUri: toOptionalString(values.resourceUri),
        ownerEstabId: parsePositiveLong(values.ownerEstabId),
        dataOwnerType: Number(values.dataOwnerType),
        status: Number(values.status),
        remark: toOptionalString(values.remark),
      }

      if (editingResource?.id) {
        await updateDataResource(editingResource.id, basePayload)
        toast.success('数据资源更新成功')
      } else {
        const payload: DataResourceCreateRequest = {
          systemId,
          ...basePayload,
          drsName: values.drsName.trim(),
        }
        await createDataResource(payload)
        toast.success('数据资源创建成功')
      }

      closeResourceDialog()
      await loadResources(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingResource(false)
    }
  }

  async function confirmDeleteResource() {
    if (!deletingResource?.id) return
    setDeletingResourceLoading(true)
    try {
      await deleteDataResource(deletingResource.id)
      toast.success('数据资源删除成功')
      setDeletingResource(null)
      await loadResources(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingResourceLoading(false)
    }
  }

  function openCreateInterfaceDialog() {
    if (!selectedResource?.id) {
      toast.error('请先选择数据资源')
      return
    }
    setEditingInterface(null)
    interfaceForm.reset(DEFAULT_INTERFACE_FORM)
    setInterfaceDialogOpen(true)
  }

  function openEditInterfaceDialog(item: DataResourceInterface) {
    setEditingInterface(item)
    interfaceForm.reset({
      interfaceCode: item.interfaceCode ?? '',
      interfaceName: item.interfaceName ?? '',
      httpMethod: item.httpMethod ?? '',
      pathPattern: item.pathPattern ?? '',
      permissionKey: item.permissionKey ?? '',
      status: String(item.status ?? 1) as '1' | '2',
      sort: String(item.sort ?? 0),
    })
    setInterfaceDialogOpen(true)
  }

  function closeInterfaceDialog() {
    setInterfaceDialogOpen(false)
    setEditingInterface(null)
    interfaceForm.reset(DEFAULT_INTERFACE_FORM)
  }

  async function submitInterface(values: InterfaceFormValues) {
    if (!selectedResource?.id) {
      toast.error('请先选择数据资源')
      return
    }

    setSavingInterface(true)
    try {
      const basePayload: DataResourceInterfaceUpdateRequest = {
        interfaceCode: values.interfaceCode?.trim() || editingInterface?.interfaceCode || '',
        interfaceName: values.interfaceName.trim(),
        httpMethod: toOptionalString(values.httpMethod),
        pathPattern: toOptionalString(values.pathPattern),
        permissionKey: toOptionalString(values.permissionKey),
        status: Number(values.status),
        sort: toOptionalNumber(values.sort),
      }

      if (editingInterface?.id) {
        await updateDataResourceInterface(editingInterface.id, basePayload)
        toast.success('数据接口更新成功')
      } else {
        const createPayload: DataResourceInterfaceCreateRequest = {
          interfaceName: values.interfaceName.trim(),
          httpMethod: toOptionalString(values.httpMethod),
          pathPattern: toOptionalString(values.pathPattern),
          permissionKey: toOptionalString(values.permissionKey),
          status: Number(values.status),
          sort: toOptionalNumber(values.sort),
        }
        await createDataResourceInterface(selectedResource.id, createPayload)
        toast.success('数据接口创建成功')
      }

      closeInterfaceDialog()
      await loadInterfaces(selectedResource.id, interfaceQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingInterface(false)
    }
  }

  async function confirmDeleteInterface() {
    if (!deletingInterface?.id || !selectedResource?.id) return
    setDeletingInterfaceLoading(true)
    try {
      await deleteDataResourceInterface(deletingInterface.id)
      toast.success('数据接口删除成功')
      setDeletingInterface(null)
      await loadInterfaces(selectedResource.id, interfaceQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingInterfaceLoading(false)
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
        <Card className='py-3 gap-3'>
          <CardContent className='pt-0 grid gap-3 lg:grid-cols-[220px_180px_180px_1fr_auto]'>
            <Select
              value={query.systemId ? String(query.systemId) : ''}
              onValueChange={(value) =>
                setQuery((prev) => ({ ...prev, systemId: Number(value), currentPage: 1 }))
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

            <Select value={typeInput} onValueChange={(value) => setTypeInput(value as 'all' | '0' | '1' | '2' | '3')}>
              <SelectTrigger>
                <SelectValue placeholder='资源类型' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部类型</SelectItem>
                <SelectItem value='0'>数据库表</SelectItem>
                <SelectItem value='1'>接口资源</SelectItem>
                <SelectItem value='2'>文件</SelectItem>
                <SelectItem value='3'>其他</SelectItem>
              </SelectContent>
            </Select>

            <Input
              value={keywordInput}
              placeholder='请输入资源编码或名称'
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
              <Button type='button' className='gap-2' onClick={openCreateResourceDialog}>
                <Plus className='h-4 w-4' />
                新建资源
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className='mt-2 py-3 gap-3'>
          <CardContent className='pt-0'>
            <div className='overflow-hidden rounded-md border border-border/90'>
              <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                <TableHeader>
                  <TableRow className='bg-muted/30 hover:bg-muted/30'>
                    <TableHead>资源编码</TableHead>
                    <TableHead>资源名称</TableHead>
                    <TableHead>资源类型</TableHead>
                    <TableHead>归属类型</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead>资源标识</TableHead>
                    <TableHead className='w-[200px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={7}>
                        <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载数据资源...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : resources.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className='py-6 text-center text-muted-foreground'>
                        暂无数据资源
                      </TableCell>
                    </TableRow>
                  ) : (
                    resources.map((resource) => (
                      <TableRow
                        key={resource.id}
                        className={selectedResource?.id === resource.id ? 'bg-muted/40' : undefined}
                      >
                        <TableCell>{resource.drsCode || '-'}</TableCell>
                        <TableCell>{resource.drsName || '-'}</TableCell>
                        <TableCell>{toDrsTypeLabel(resource.drsType)}</TableCell>
                        <TableCell>{toOwnerTypeLabel(resource.dataOwnerType)}</TableCell>
                        <TableCell>
                          <Badge variant={resource.status === 1 ? 'default' : 'secondary'}>
                            {toStatusLabel(resource.status)}
                          </Badge>
                        </TableCell>
                        <TableCell className='max-w-[280px] truncate'>{resource.resourceUri || '-'}</TableCell>
                        <TableCell className='text-center'>
                          <div className='flex items-center justify-center gap-1'>
                            <Button
                              type='button'
                              variant='ghost'
                              size='sm'
                              onClick={() => setSelectedResource(resource)}
                            >
                              管理接口
                            </Button>
                            <Button
                              type='button'
                              variant='ghost'
                              size='sm'
                              className='gap-1'
                              onClick={() => openEditResourceDialog(resource)}
                            >
                              <Pencil className='h-3.5 w-3.5' />
                              编辑
                            </Button>
                            <Button
                              type='button'
                              variant='ghost'
                              size='sm'
                              className='gap-1 text-destructive hover:text-destructive'
                              onClick={() => setDeletingResource(resource)}
                            >
                              <Trash2 className='h-3.5 w-3.5' />
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
              page={query.currentPage ?? 1}
              size={query.pageSize ?? DATA_RESOURCE_PAGE_SIZE}
              total={total}
              loading={loading}
              onPageChange={handlePageChange}
              onPageSizeChange={handlePageSizeChange}
            />
          </CardContent>
        </Card>

        <Card className='mt-2 grow overflow-hidden py-3 gap-3'>
          <CardHeader className='pb-0'>
            <CardTitle className='text-base'>数据接口定义：{selectedResourceName}</CardTitle>
          </CardHeader>
          <CardContent className='pt-0 space-y-3'>
            <div className='grid gap-3 lg:grid-cols-[1fr_180px_auto]'>
              <Input
                value={interfaceQuery.keyword}
                placeholder='按接口编码/名称检索'
                onChange={(event) =>
                  setInterfaceQuery((prev) => ({ ...prev, keyword: event.target.value, currentPage: 1 }))
                }
              />
              <Select
                value={interfaceQuery.status}
                onValueChange={(value) =>
                  setInterfaceQuery((prev) => ({
                    ...prev,
                    status: value as 'all' | '1' | '2',
                    currentPage: 1,
                  }))
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder='状态' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='all'>全部状态</SelectItem>
                  <SelectItem value='1'>启用</SelectItem>
                  <SelectItem value='2'>停用</SelectItem>
                </SelectContent>
              </Select>
              <Button type='button' className='gap-2' onClick={openCreateInterfaceDialog} disabled={!selectedResource?.id}>
                <Plus className='h-4 w-4' />
                新建接口
              </Button>
            </div>

            <div className='overflow-hidden rounded-md border border-border/90'>
              <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
                <TableHeader>
                  <TableRow className='bg-muted/30 hover:bg-muted/30'>
                    <TableHead>接口编码</TableHead>
                    <TableHead>接口名称</TableHead>
                    <TableHead>方法</TableHead>
                    <TableHead>路径</TableHead>
                    <TableHead>权限标识</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead>排序</TableHead>
                    <TableHead className='w-[130px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {!selectedResource?.id ? (
                    <TableRow>
                      <TableCell colSpan={8} className='py-6 text-center text-muted-foreground'>
                        请先在上方选择一个数据资源
                      </TableCell>
                    </TableRow>
                  ) : interfaceLoading ? (
                    <TableRow>
                      <TableCell colSpan={8}>
                        <div className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载数据接口...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : interfaces.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} className='py-6 text-center text-muted-foreground'>
                        当前资源暂无接口定义
                      </TableCell>
                    </TableRow>
                  ) : (
                    interfaces.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell>{item.interfaceCode || '-'}</TableCell>
                        <TableCell>{item.interfaceName || '-'}</TableCell>
                        <TableCell>{item.httpMethod || '-'}</TableCell>
                        <TableCell>{item.pathPattern || '-'}</TableCell>
                        <TableCell>{item.permissionKey || '-'}</TableCell>
                        <TableCell>
                          <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                            {toStatusLabel(item.status)}
                          </Badge>
                        </TableCell>
                        <TableCell>{item.sort ?? '-'}</TableCell>
                        <TableCell className='text-center'>
                          <div className='flex items-center justify-center gap-1'>
                            <Button type='button' variant='ghost' size='sm' onClick={() => openEditInterfaceDialog(item)}>
                              编辑
                            </Button>
                            <Button
                              type='button'
                              variant='ghost'
                              size='sm'
                              className='text-destructive hover:text-destructive'
                              onClick={() => setDeletingInterface(item)}
                            >
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
              page={interfaceQuery.currentPage}
              size={interfaceQuery.pageSize}
              total={interfaceTotal}
              loading={interfaceLoading}
              onPageChange={handleInterfacePageChange}
              onPageSizeChange={handleInterfacePageSizeChange}
            />
          </CardContent>
        </Card>
      </Main>

      <Dialog open={resourceDialogOpen} onOpenChange={(open) => (open ? setResourceDialogOpen(true) : closeResourceDialog())}>
        <DialogContent className='sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>{editingResource?.id ? '编辑数据资源' : '新建数据资源'}</DialogTitle>
            <DialogDescription>资源编码由后端自动生成，前端只维护业务属性。</DialogDescription>
          </DialogHeader>
          <Form {...resourceForm}>
            <form onSubmit={resourceForm.handleSubmit(submitResource)} className='grid gap-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={resourceForm.control}
                  name='systemId'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>所属系统</FormLabel>
                      <Select
                        value={field.value}
                        onValueChange={field.onChange}
                        disabled={Boolean(editingResource?.id)}
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
                  control={resourceForm.control}
                  name='drsName'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>资源名称</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='请输入资源名称' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className='grid gap-4 md:grid-cols-3'>
                <FormField
                  control={resourceForm.control}
                  name='drsType'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>资源类型</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder='请选择类型' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='0'>数据库表</SelectItem>
                          <SelectItem value='1'>接口资源</SelectItem>
                          <SelectItem value='2'>文件</SelectItem>
                          <SelectItem value='3'>其他</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={resourceForm.control}
                  name='dataOwnerType'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>归属类型</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder='请选择归属类型' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='0'>平台</SelectItem>
                          <SelectItem value='1'>租户</SelectItem>
                          <SelectItem value='2'>用户</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={resourceForm.control}
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
              </div>

              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={resourceForm.control}
                  name='resourceUri'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>资源标识</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='如：/api/orders/** 或订单表名' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={resourceForm.control}
                  name='ownerEstabId'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>所属企业ID（可选）</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='留空默认为平台级' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={resourceForm.control}
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

              <DialogFooter>
                <Button type='button' variant='outline' onClick={closeResourceDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={savingResource}>
                  {savingResource ? <Loader2 className='mr-2 h-4 w-4 animate-spin' /> : null}
                  保存
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <Dialog open={interfaceDialogOpen} onOpenChange={(open) => (open ? setInterfaceDialogOpen(true) : closeInterfaceDialog())}>
        <DialogContent className='sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>{editingInterface?.id ? '编辑数据接口' : '新建数据接口'}</DialogTitle>
            <DialogDescription>接口编码由后端自动生成，建议重点维护权限标识和路径规范。</DialogDescription>
          </DialogHeader>
          <Form {...interfaceForm}>
            <form onSubmit={interfaceForm.handleSubmit(submitInterface)} className='grid gap-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={interfaceForm.control}
                  name='interfaceName'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>接口名称</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='请输入接口名称' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={interfaceForm.control}
                  name='httpMethod'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>HTTP 方法</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='GET / POST / PUT ...' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={interfaceForm.control}
                  name='pathPattern'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>路径模式</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='如：/users/**' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={interfaceForm.control}
                  name='permissionKey'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>权限标识</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='如：user:list:view' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={interfaceForm.control}
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
                  control={interfaceForm.control}
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

              <DialogFooter>
                <Button type='button' variant='outline' onClick={closeInterfaceDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={savingInterface}>
                  {savingInterface ? <Loader2 className='mr-2 h-4 w-4 animate-spin' /> : null}
                  保存
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deletingResource)}
        onOpenChange={(open) => {
          if (!open) setDeletingResource(null)
        }}
        title='确认删除数据资源'
        desc={`确定要删除资源「${deletingResource?.drsName || '-'}」吗？`}
        confirmText='删除'
        destructive
        isLoading={deletingResourceLoading}
        handleConfirm={confirmDeleteResource}
      />

      <ConfirmDialog
        open={Boolean(deletingInterface)}
        onOpenChange={(open) => {
          if (!open) setDeletingInterface(null)
        }}
        title='确认删除数据接口'
        desc={`确定要删除接口「${deletingInterface?.interfaceName || '-'}」吗？`}
        confirmText='删除'
        destructive
        isLoading={deletingInterfaceLoading}
        handleConfirm={confirmDeleteInterface}
      />
    </>
  )
}
