import { useEffect, useState } from 'react'
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
import { Switch } from '@/components/ui/switch'
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
  createModelProvision,
  deleteModelProvision,
  listEstabs,
  listModels,
  listModelProvisions,
  updateModelProvision,
  type AiModel,
  type AiModelProvision,
  type AiModelProvisionCreateRequest,
  type AiModelProvisionListQuery,
  type AiModelProvisionUpdateRequest,
  type Estab,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const provisionFormSchema = z.object({
  estabId: z.string().min(1, '请选择组织。'),
  modelId: z.string().min(1, '请选择模型。'),
  apiKey: z.string().max(512, 'API Key 长度不能超过 512 个字符。').optional(),
  apiBaseUrl: z.string().trim().max(255, 'API 地址长度不能超过 255 个字符。').optional(),
  dailyQuota: z.string().trim().optional(),
  monthlyQuota: z.string().trim().optional(),
  isDefault: z.boolean(),
  status: z.enum(['0', '1']),
  remark: z.string().trim().max(255, '备注长度不能超过 255 个字符。').optional(),
})

type ProvisionFormValues = z.infer<typeof provisionFormSchema>

const DEFAULT_FORM: ProvisionFormValues = {
  estabId: '',
  modelId: '',
  apiKey: '',
  apiBaseUrl: '',
  dailyQuota: '',
  monthlyQuota: '',
  isDefault: false,
  status: '1',
  remark: '',
}

function toOptionalString(value?: string): string | undefined {
  const normalized = value?.trim()
  return normalized ? normalized : undefined
}

function toOptionalInt(value?: string): number | undefined {
  const normalized = value?.trim()
  if (!normalized) return undefined
  const parsed = Number(normalized)
  if (!Number.isFinite(parsed) || parsed < 0) return undefined
  return Math.floor(parsed)
}

export function ProvisionsPage() {
  const [provisions, setProvisions] = useState<AiModelProvision[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  // 下拉数据源
  const [allEstabs, setAllEstabs] = useState<Estab[]>([])
  const [allModels, setAllModels] = useState<AiModel[]>([])

  // 筛选
  const [estabIdFilter, setEstabIdFilter] = useState<string>('all')
  const [modelIdFilter, setModelIdFilter] = useState<string>('all')
  const [statusInput, setStatusInput] = useState<'all' | '0' | '1'>('all')
  const [query, setQuery] = useState<AiModelProvisionListQuery>({ currentPage: 1, pageSize: 10 })

  const [dialogOpen, setDialogOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [editingProvision, setEditingProvision] = useState<AiModelProvision | null>(null)
  const [deletingProvision, setDeletingProvision] = useState<AiModelProvision | null>(null)
  const [deletingLoading, setDeletingLoading] = useState(false)

  const form = useForm<ProvisionFormValues>({
    resolver: zodResolver(provisionFormSchema),
    defaultValues: DEFAULT_FORM,
    mode: 'onChange',
  })

  // 加载企业和模型下拉数据
  useEffect(() => {
    async function loadDropdownData() {
      try {
        const [estabData, modelData] = await Promise.all([
          listEstabs({ pageSize: 200 }),
          listModels({ pageSize: 200 }),
        ])
        setAllEstabs(estabData.data ?? [])
        setAllModels(modelData.data ?? [])
      } catch (error) {
        handleServerError(error)
      }
    }
    void loadDropdownData()
  }, [])

  const estabMap = new Map(allEstabs.map((e) => [e.id, e]))
  const modelMap = new Map(allModels.map((m) => [m.id, m]))

  async function loadProvisions(activeQuery: AiModelProvisionListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listModelProvisions(activeQuery)
      setProvisions(pageData.data ?? [])
      setTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadProvisions(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  function applyFilter() {
    setQuery({
      estabId: estabIdFilter === 'all' ? undefined : Number(estabIdFilter),
      modelId: modelIdFilter === 'all' ? undefined : Number(modelIdFilter),
      status: statusInput === 'all' ? undefined : Number(statusInput),
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function resetFilter() {
    setEstabIdFilter('all')
    setModelIdFilter('all')
    setStatusInput('all')
    setQuery({ currentPage: 1, pageSize: query.pageSize ?? 10 })
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  function openCreateDialog() {
    setEditingProvision(null)
    form.reset(DEFAULT_FORM)
    setDialogOpen(true)
  }

  function openEditDialog(provision: AiModelProvision) {
    setEditingProvision(provision)
    form.reset({
      estabId: String(provision.estabId ?? ''),
      modelId: String(provision.modelId ?? ''),
      apiKey: '',
      apiBaseUrl: provision.apiBaseUrl ?? '',
      dailyQuota: provision.dailyQuota != null ? String(provision.dailyQuota) : '',
      monthlyQuota: provision.monthlyQuota != null ? String(provision.monthlyQuota) : '',
      isDefault: provision.isDefault === 1,
      status: String(provision.status ?? 1) as '0' | '1',
      remark: provision.remark ?? '',
    })
    setDialogOpen(true)
  }

  function closeDialog() {
    setDialogOpen(false)
    setEditingProvision(null)
    form.reset(DEFAULT_FORM)
  }

  async function onSubmit(values: ProvisionFormValues) {
    setSaving(true)
    try {
      const payload: AiModelProvisionUpdateRequest = {
        apiKey: toOptionalString(values.apiKey),
        apiBaseUrl: toOptionalString(values.apiBaseUrl),
        dailyQuota: toOptionalInt(values.dailyQuota),
        monthlyQuota: toOptionalInt(values.monthlyQuota),
        isDefault: values.isDefault ? 1 : 0,
        status: Number(values.status),
        remark: toOptionalString(values.remark),
      }

      if (editingProvision?.id) {
        await updateModelProvision(editingProvision.id, payload)
        toast.success('模型开通更新成功。')
      } else {
        const createPayload: AiModelProvisionCreateRequest = {
          ...payload,
          estabId: Number(values.estabId),
          modelId: Number(values.modelId),
        }
        await createModelProvision(createPayload)
        toast.success('模型开通创建成功。')
      }

      closeDialog()
      await loadProvisions(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function confirmDelete() {
    if (!deletingProvision?.id) return
    setDeletingLoading(true)
    try {
      await deleteModelProvision(deletingProvision.id)
      toast.success('模型开通已删除。')
      setDeletingProvision(null)
      await loadProvisions(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingLoading(false)
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
          <CardContent className='pt-0 grid gap-3 md:grid-cols-[1fr_1fr_180px_auto]'>
            <Select value={estabIdFilter} onValueChange={setEstabIdFilter}>
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='组织' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部组织</SelectItem>
                {allEstabs.map((e) => (
                  <SelectItem key={e.id} value={String(e.id)}>
                    {e.estabName}{e.estabShortName ? `（${e.estabShortName}）` : ''}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={modelIdFilter} onValueChange={setModelIdFilter}>
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='模型' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部模型</SelectItem>
                {allModels.map((m) => (
                  <SelectItem key={m.id} value={String(m.id)}>
                    {m.modelName ?? m.modelCode}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={statusInput}
              onValueChange={(v) => setStatusInput(v as 'all' | '0' | '1')}
            >
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='状态' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部状态</SelectItem>
                <SelectItem value='1'>启用</SelectItem>
                <SelectItem value='0'>停用</SelectItem>
              </SelectContent>
            </Select>
            <div className='flex items-center gap-2'>
              <Button type='button' variant='outline' onClick={applyFilter} className='gap-2'>
                <SearchIcon className='h-4 w-4' />
                查询
              </Button>
              <Button type='button' variant='outline' onClick={resetFilter} className='gap-2'>
                <RefreshCw className='h-4 w-4' />
                重置
              </Button>
              <Button type='button' onClick={openCreateDialog} className='gap-2'>
                <Plus className='h-4 w-4' />
                新建开通
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
                    <TableHead>组织</TableHead>
                    <TableHead>模型</TableHead>
                    <TableHead>API Key（脱敏）</TableHead>
                    <TableHead>自定义地址</TableHead>
                    <TableHead className='w-[88px] text-center'>日额度</TableHead>
                    <TableHead className='w-[88px] text-center'>月额度</TableHead>
                    <TableHead className='w-[88px] text-center'>默认</TableHead>
                    <TableHead className='w-[88px] text-center'>状态</TableHead>
                    <TableHead className='w-[120px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={9}>
                        <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载模型开通...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : provisions.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={9} className='py-8 text-center text-muted-foreground'>
                        暂无模型开通数据
                      </TableCell>
                    </TableRow>
                  ) : (
                    provisions.map((item) => {
                      const estab = estabMap.get(item.estabId)
                      const model = modelMap.get(item.modelId)
                      return (
                        <TableRow key={item.id}>
                          <TableCell>
                            {estab?.estabName ?? String(item.estabId ?? '-')}
                          </TableCell>
                          <TableCell>
                            {model?.modelName ?? model?.modelCode ?? String(item.modelId ?? '-')}
                          </TableCell>
                          <TableCell className='font-mono text-xs'>
                            {item.apiKeyMasked || '-'}
                          </TableCell>
                          <TableCell className='max-w-[200px] truncate'>
                            {item.apiBaseUrl || '-'}
                          </TableCell>
                          <TableCell className='text-center'>
                            {item.dailyQuota != null ? item.dailyQuota : '不限'}
                          </TableCell>
                          <TableCell className='text-center'>
                            {item.monthlyQuota != null ? item.monthlyQuota : '不限'}
                          </TableCell>
                          <TableCell className='text-center'>
                            <Badge variant={item.isDefault === 1 ? 'default' : 'outline'}>
                              {item.isDefault === 1 ? '是' : '否'}
                            </Badge>
                          </TableCell>
                          <TableCell className='text-center'>
                            <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                              {item.status === 1 ? '启用' : '停用'}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <div className='flex items-center justify-center gap-1'>
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='h-8 w-8'
                                onClick={() => openEditDialog(item)}
                              >
                                <Pencil className='h-4 w-4' />
                              </Button>
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='h-8 w-8 text-destructive hover:text-destructive'
                                onClick={() => setDeletingProvision(item)}
                              >
                                <Trash2 className='h-4 w-4' />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      )
                    })
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
        <DialogContent className='sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>{editingProvision ? '编辑模型开通' : '新建模型开通'}</DialogTitle>
            <DialogDescription>组织和模型创建后不可修改。</DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className='grid gap-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='estabId'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>组织</FormLabel>
                      <Select
                        value={field.value}
                        onValueChange={field.onChange}
                        disabled={Boolean(editingProvision)}
                      >
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue placeholder='请选择组织' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {allEstabs.map((e) => (
                            <SelectItem key={e.id} value={String(e.id)}>
                              {e.estabName}{e.estabShortName ? `（${e.estabShortName}）` : ''}
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
                  name='modelId'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>模型</FormLabel>
                      <Select
                        value={field.value}
                        onValueChange={field.onChange}
                        disabled={Boolean(editingProvision)}
                      >
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue placeholder='请选择模型' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {allModels.map((m) => (
                            <SelectItem key={m.id} value={String(m.id)}>
                              {m.modelName ?? m.modelCode}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <FormField
                control={form.control}
                name='apiKey'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      API Key{editingProvision ? '（留空则不修改）' : ''}
                    </FormLabel>
                    <FormControl>
                      <Input
                        {...field}
                        value={field.value ?? ''}
                        type='password'
                        placeholder='请输入 API Key'
                        autoComplete='off'
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='apiBaseUrl'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>自定义 API 地址</FormLabel>
                    <FormControl>
                      <Input
                        {...field}
                        value={field.value ?? ''}
                        placeholder='覆盖供应商默认地址（可选）'
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='dailyQuota'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>日调用额度</FormLabel>
                      <FormControl>
                        <Input {...field} value={field.value ?? ''} placeholder='留空表示不限' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='monthlyQuota'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>月调用额度</FormLabel>
                      <FormControl>
                        <Input {...field} value={field.value ?? ''} placeholder='留空表示不限' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='isDefault'
                  render={({ field }) => (
                    <FormItem className='flex items-center justify-between rounded-lg border p-3'>
                      <FormLabel>默认模型</FormLabel>
                      <FormControl>
                        <Switch
                          checked={field.value}
                          onCheckedChange={field.onChange}
                        />
                      </FormControl>
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
                          <SelectTrigger className='w-full'>
                            <SelectValue placeholder='请选择状态' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='1'>启用</SelectItem>
                          <SelectItem value='0'>停用</SelectItem>
                        </SelectContent>
                      </Select>
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
                      <Textarea {...field} value={field.value ?? ''} rows={2} placeholder='备注信息' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter>
                <Button type='button' variant='outline' onClick={closeDialog} disabled={saving}>
                  取消
                </Button>
                <Button type='submit' disabled={saving}>
                  {saving ? (
                    <>
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      保存中...
                    </>
                  ) : (
                    '保存'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deletingProvision)}
        onOpenChange={(open) => {
          if (!open) setDeletingProvision(null)
        }}
        title='删除模型开通'
        desc={`确认删除该模型开通记录（ID: ${deletingProvision?.id ?? '-'}）吗？`}
        confirmText='确认删除'
        destructive
        isLoading={deletingLoading}
        handleConfirm={confirmDelete}
      />
    </>
  )
}
