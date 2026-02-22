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
  createProvider,
  deleteProvider,
  listProviders,
  updateProvider,
  type AiProvider,
  type AiProviderCreateRequest,
  type AiProviderListQuery,
  type AiProviderUpdateRequest,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const providerFormSchema = z.object({
  providerCode: z
    .string()
    .trim()
    .min(1, '供应商编码不能为空。')
    .max(64, '供应商编码长度不能超过 64 个字符。'),
  providerName: z
    .string()
    .trim()
    .min(1, '供应商名称不能为空。')
    .max(128, '供应商名称长度不能超过 128 个字符。'),
  protocol: z.string().optional(),
  baseUrl: z.string().trim().max(255, 'API 地址长度不能超过 255 个字符。').optional(),
  iconUrl: z.string().trim().max(255, '图标地址长度不能超过 255 个字符。').optional(),
  status: z.enum(['0', '1']),
  sort: z.string().trim().max(6, '排序值过大。').optional(),
  remark: z.string().trim().max(255, '备注长度不能超过 255 个字符。').optional(),
})

type ProviderFormValues = z.infer<typeof providerFormSchema>

const DEFAULT_FORM: ProviderFormValues = {
  providerCode: '',
  providerName: '',
  protocol: 'openai',
  baseUrl: '',
  iconUrl: '',
  status: '1',
  sort: '0',
  remark: '',
}

function toOptionalString(value?: string): string | undefined {
  const normalized = value?.trim()
  return normalized ? normalized : undefined
}

function toOptionalNumber(value?: string): number | undefined {
  const normalized = value?.trim()
  if (!normalized) return undefined
  const parsed = Number(normalized)
  if (!Number.isFinite(parsed) || parsed < 0) return undefined
  return Math.floor(parsed)
}

export function ProvidersPage() {
  const [providers, setProviders] = useState<AiProvider[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [keywordInput, setKeywordInput] = useState('')
  const [statusInput, setStatusInput] = useState<'all' | '0' | '1'>('all')
  const [query, setQuery] = useState<AiProviderListQuery>({ currentPage: 1, pageSize: 10 })

  const [dialogOpen, setDialogOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [editingProvider, setEditingProvider] = useState<AiProvider | null>(null)
  const [deletingProvider, setDeletingProvider] = useState<AiProvider | null>(null)
  const [deletingLoading, setDeletingLoading] = useState(false)

  const form = useForm<ProviderFormValues>({
    resolver: zodResolver(providerFormSchema),
    defaultValues: DEFAULT_FORM,
    mode: 'onChange',
  })

  async function loadProviders(activeQuery: AiProviderListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listProviders(activeQuery)
      setProviders(pageData.data ?? [])
      setTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadProviders(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  function applyFilter() {
    setQuery({
      keyword: toOptionalString(keywordInput),
      status: statusInput === 'all' ? undefined : Number(statusInput),
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function resetFilter() {
    setKeywordInput('')
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
    setEditingProvider(null)
    form.reset(DEFAULT_FORM)
    setDialogOpen(true)
  }

  function openEditDialog(provider: AiProvider) {
    setEditingProvider(provider)
    form.reset({
      providerCode: provider.providerCode ?? '',
      providerName: provider.providerName ?? '',
      protocol: provider.protocol ?? 'openai',
      baseUrl: provider.baseUrl ?? '',
      iconUrl: provider.iconUrl ?? '',
      status: String(provider.status ?? 1) as '0' | '1',
      sort: String(provider.sort ?? 0),
      remark: provider.remark ?? '',
    })
    setDialogOpen(true)
  }

  function closeDialog() {
    setDialogOpen(false)
    setEditingProvider(null)
    form.reset(DEFAULT_FORM)
  }

  async function onSubmit(values: ProviderFormValues) {
    setSaving(true)
    try {
      const payload: AiProviderUpdateRequest = {
        providerName: values.providerName.trim(),
        protocol: toOptionalString(values.protocol),
        baseUrl: toOptionalString(values.baseUrl),
        iconUrl: toOptionalString(values.iconUrl),
        status: Number(values.status),
        sort: toOptionalNumber(values.sort),
        remark: toOptionalString(values.remark),
      }

      if (editingProvider?.id) {
        await updateProvider(editingProvider.id, payload)
        toast.success('供应商更新成功。')
      } else {
        const createPayload: AiProviderCreateRequest = {
          ...payload,
          providerCode: values.providerCode.trim(),
          providerName: values.providerName.trim(),
        }
        await createProvider(createPayload)
        toast.success('供应商创建成功。')
      }

      closeDialog()
      await loadProviders(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function confirmDelete() {
    if (!deletingProvider?.id) return
    setDeletingLoading(true)
    try {
      await deleteProvider(deletingProvider.id)
      toast.success('供应商已删除。')
      setDeletingProvider(null)
      await loadProviders(query)
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
          <CardContent className='pt-0 grid gap-3 md:grid-cols-[1fr_180px_auto]'>
            <Input
              value={keywordInput}
              placeholder='请输入供应商编码或名称'
              onChange={(e) => setKeywordInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault()
                  applyFilter()
                }
              }}
            />
            <Select
              value={statusInput}
              onValueChange={(v) => setStatusInput(v as 'all' | '0' | '1')}
            >
              <SelectTrigger>
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
                新建供应商
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
                    <TableHead>供应商编码</TableHead>
                    <TableHead>供应商名称</TableHead>
                    <TableHead>接口协议</TableHead>
                    <TableHead>API 地址</TableHead>
                    <TableHead className='w-[88px] text-center'>状态</TableHead>
                    <TableHead className='w-[80px] text-center'>排序</TableHead>
                    <TableHead className='w-[120px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={7}>
                        <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载供应商...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : providers.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className='py-8 text-center text-muted-foreground'>
                        暂无供应商数据
                      </TableCell>
                    </TableRow>
                  ) : (
                    providers.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell className='font-medium'>{item.providerCode || '-'}</TableCell>
                        <TableCell>{item.providerName || '-'}</TableCell>
                        <TableCell>{item.protocol || '-'}</TableCell>
                        <TableCell className='max-w-[280px] truncate'>
                          {item.baseUrl || '-'}
                        </TableCell>
                        <TableCell className='text-center'>
                          <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                            {item.status === 1 ? '启用' : '停用'}
                          </Badge>
                        </TableCell>
                        <TableCell className='text-center'>{item.sort ?? '-'}</TableCell>
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
                              onClick={() => setDeletingProvider(item)}
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
            <DialogTitle>{editingProvider ? '编辑供应商' : '新建供应商'}</DialogTitle>
            <DialogDescription>供应商编码创建后不可修改，请谨慎填写。</DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className='grid gap-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='providerCode'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>供应商编码</FormLabel>
                      <FormControl>
                        <Input
                          {...field}
                          placeholder='例如：openai'
                          disabled={Boolean(editingProvider)}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='providerName'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>供应商名称</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='例如：OpenAI' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='protocol'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>接口协议</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue placeholder='请选择协议' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='openai'>openai</SelectItem>
                          <SelectItem value='anthropic'>anthropic</SelectItem>
                          <SelectItem value='ollama'>ollama</SelectItem>
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
                name='baseUrl'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>API 地址</FormLabel>
                    <FormControl>
                      <Input {...field} value={field.value ?? ''} placeholder='例如：https://api.openai.com' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='iconUrl'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>图标地址</FormLabel>
                      <FormControl>
                        <Input {...field} value={field.value ?? ''} placeholder='供应商图标 URL' />
                      </FormControl>
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
                        <Input {...field} value={field.value ?? ''} placeholder='默认 0，越小越靠前' />
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
        open={Boolean(deletingProvider)}
        onOpenChange={(open) => {
          if (!open) setDeletingProvider(null)
        }}
        title='删除供应商'
        desc={`确认删除供应商 ${deletingProvider?.providerName || '-'}（${deletingProvider?.providerCode || '-'}）吗？`}
        confirmText='确认删除'
        destructive
        isLoading={deletingLoading}
        handleConfirm={confirmDelete}
      />
    </>
  )
}
