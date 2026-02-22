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
  createModel,
  deleteModel,
  listAllProviders,
  listModels,
  updateModel,
  type AiModel,
  type AiModelCreateRequest,
  type AiModelListQuery,
  type AiModelUpdateRequest,
  type AiProvider,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const MODEL_TYPE_OPTIONS = [
  { value: '1', label: '聊天' },
  { value: '2', label: '嵌入' },
  { value: '3', label: '图像生成' },
  { value: '4', label: '语音转文字' },
  { value: '5', label: '文字转语音' },
  { value: '6', label: '重排序' },
  { value: '7', label: '内容审核' },
] as const

function toModelTypeLabel(value?: number): string {
  const found = MODEL_TYPE_OPTIONS.find((o) => o.value === String(value))
  return found?.label ?? '-'
}

const modelFormSchema = z.object({
  providerId: z.string().min(1, '请选择供应商。'),
  modelCode: z
    .string()
    .trim()
    .min(1, '模型编码不能为空。')
    .max(128, '模型编码长度不能超过 128 个字符。'),
  modelName: z
    .string()
    .trim()
    .min(1, '模型名称不能为空。')
    .max(128, '模型名称长度不能超过 128 个字符。'),
  modelType: z.string().optional(),
  capVision: z.boolean(),
  capToolCall: z.boolean(),
  capStructuredOutput: z.boolean(),
  capStreaming: z.boolean(),
  capReasoning: z.boolean(),
  maxContextWindow: z.string().trim().optional(),
  maxOutputTokens: z.string().trim().optional(),
  inputPrice: z.string().trim().optional(),
  outputPrice: z.string().trim().optional(),
  status: z.enum(['0', '1']),
  sort: z.string().trim().max(6, '排序值过大。').optional(),
  remark: z.string().trim().max(255, '备注长度不能超过 255 个字符。').optional(),
})

type ModelFormValues = z.infer<typeof modelFormSchema>

const DEFAULT_FORM: ModelFormValues = {
  providerId: '',
  modelCode: '',
  modelName: '',
  modelType: '1',
  capVision: false,
  capToolCall: false,
  capStructuredOutput: false,
  capStreaming: true,
  capReasoning: false,
  maxContextWindow: '',
  maxOutputTokens: '',
  inputPrice: '',
  outputPrice: '',
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
  return parsed
}

function toOptionalInt(value?: string): number | undefined {
  const n = toOptionalNumber(value)
  return n != null ? Math.floor(n) : undefined
}

export function ModelsPage() {
  const [models, setModels] = useState<AiModel[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [allProviders, setAllProviders] = useState<AiProvider[]>([])

  const [keywordInput, setKeywordInput] = useState('')
  const [statusInput, setStatusInput] = useState<'all' | '0' | '1'>('all')
  const [providerIdInput, setProviderIdInput] = useState<string>('all')
  const [modelTypeInput, setModelTypeInput] = useState<string>('all')
  const [query, setQuery] = useState<AiModelListQuery>({ currentPage: 1, pageSize: 10 })

  const [dialogOpen, setDialogOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [editingModel, setEditingModel] = useState<AiModel | null>(null)
  const [deletingModel, setDeletingModel] = useState<AiModel | null>(null)
  const [deletingLoading, setDeletingLoading] = useState(false)

  const form = useForm<ModelFormValues>({
    resolver: zodResolver(modelFormSchema),
    defaultValues: DEFAULT_FORM,
    mode: 'onChange',
  })

  useEffect(() => {
    void listAllProviders().then(setAllProviders).catch(handleServerError)
  }, [])

  const providerMap = new Map(allProviders.map((p) => [p.id, p]))

  async function loadModels(activeQuery: AiModelListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listModels(activeQuery)
      setModels(pageData.data ?? [])
      setTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadModels(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  function applyFilter() {
    setQuery({
      keyword: toOptionalString(keywordInput),
      status: statusInput === 'all' ? undefined : Number(statusInput),
      providerId: providerIdInput === 'all' ? undefined : Number(providerIdInput),
      modelType: modelTypeInput === 'all' ? undefined : Number(modelTypeInput),
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function resetFilter() {
    setKeywordInput('')
    setStatusInput('all')
    setProviderIdInput('all')
    setModelTypeInput('all')
    setQuery({ currentPage: 1, pageSize: query.pageSize ?? 10 })
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  function openCreateDialog() {
    setEditingModel(null)
    form.reset(DEFAULT_FORM)
    setDialogOpen(true)
  }

  function openEditDialog(model: AiModel) {
    setEditingModel(model)
    form.reset({
      providerId: String(model.providerId ?? ''),
      modelCode: model.modelCode ?? '',
      modelName: model.modelName ?? '',
      modelType: String(model.modelType ?? 1),
      capVision: model.capVision === 1,
      capToolCall: model.capToolCall === 1,
      capStructuredOutput: model.capStructuredOutput === 1,
      capStreaming: model.capStreaming === 1,
      capReasoning: model.capReasoning === 1,
      maxContextWindow: model.maxContextWindow != null ? String(model.maxContextWindow) : '',
      maxOutputTokens: model.maxOutputTokens != null ? String(model.maxOutputTokens) : '',
      inputPrice: model.inputPrice != null ? String(model.inputPrice) : '',
      outputPrice: model.outputPrice != null ? String(model.outputPrice) : '',
      status: String(model.status ?? 1) as '0' | '1',
      sort: String(model.sort ?? 0),
      remark: model.remark ?? '',
    })
    setDialogOpen(true)
  }

  function closeDialog() {
    setDialogOpen(false)
    setEditingModel(null)
    form.reset(DEFAULT_FORM)
  }

  async function onSubmit(values: ModelFormValues) {
    setSaving(true)
    try {
      const payload: AiModelUpdateRequest = {
        modelName: values.modelName.trim(),
        modelType: values.modelType ? Number(values.modelType) : undefined,
        capVision: values.capVision ? 1 : 0,
        capToolCall: values.capToolCall ? 1 : 0,
        capStructuredOutput: values.capStructuredOutput ? 1 : 0,
        capStreaming: values.capStreaming ? 1 : 0,
        capReasoning: values.capReasoning ? 1 : 0,
        maxContextWindow: toOptionalInt(values.maxContextWindow),
        maxOutputTokens: toOptionalInt(values.maxOutputTokens),
        inputPrice: toOptionalNumber(values.inputPrice),
        outputPrice: toOptionalNumber(values.outputPrice),
        status: Number(values.status),
        sort: toOptionalInt(values.sort),
        remark: toOptionalString(values.remark),
      }

      if (editingModel?.id) {
        await updateModel(editingModel.id, payload)
        toast.success('模型更新成功。')
      } else {
        const createPayload: AiModelCreateRequest = {
          ...payload,
          providerId: Number(values.providerId),
          modelCode: values.modelCode.trim(),
          modelName: values.modelName.trim(),
        }
        await createModel(createPayload)
        toast.success('模型创建成功。')
      }

      closeDialog()
      await loadModels(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function confirmDelete() {
    if (!deletingModel?.id) return
    setDeletingLoading(true)
    try {
      await deleteModel(deletingModel.id)
      toast.success('模型已删除。')
      setDeletingModel(null)
      await loadModels(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingLoading(false)
    }
  }

  function renderCapBadges(model: AiModel) {
    const caps: { key: keyof AiModel; label: string }[] = [
      { key: 'capVision', label: '视觉' },
      { key: 'capToolCall', label: '工具' },
      { key: 'capStructuredOutput', label: '结构化' },
      { key: 'capStreaming', label: '流式' },
      { key: 'capReasoning', label: '推理' },
    ]
    const active = caps.filter((c) => model[c.key] === 1)
    if (active.length === 0) return <span className='text-muted-foreground'>-</span>
    return (
      <div className='flex flex-wrap gap-1'>
        {active.map((c) => (
          <Badge key={c.key} variant='outline' className='text-xs'>
            {c.label}
          </Badge>
        ))}
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
          <CardContent className='pt-0 grid gap-3 md:grid-cols-[1fr_180px_180px_180px_auto]'>
            <Input
              value={keywordInput}
              placeholder='请输入模型编码或名称'
              onChange={(e) => setKeywordInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault()
                  applyFilter()
                }
              }}
            />
            <Select
              value={providerIdInput}
              onValueChange={setProviderIdInput}
            >
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='供应商' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部供应商</SelectItem>
                {allProviders.map((p) => (
                  <SelectItem key={p.id} value={String(p.id)}>
                    {p.providerName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={modelTypeInput}
              onValueChange={setModelTypeInput}
            >
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='模型类型' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>全部类型</SelectItem>
                {MODEL_TYPE_OPTIONS.map((o) => (
                  <SelectItem key={o.value} value={o.value}>
                    {o.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
                新建模型
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
                    <TableHead>模型编码</TableHead>
                    <TableHead>模型名称</TableHead>
                    <TableHead>供应商</TableHead>
                    <TableHead>模型类型</TableHead>
                    <TableHead>能力</TableHead>
                    <TableHead className='w-[88px] text-center'>状态</TableHead>
                    <TableHead className='w-[120px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={7}>
                        <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载模型...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : models.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className='py-8 text-center text-muted-foreground'>
                        暂无模型数据
                      </TableCell>
                    </TableRow>
                  ) : (
                    models.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell className='font-medium'>{item.modelCode || '-'}</TableCell>
                        <TableCell>{item.modelName || '-'}</TableCell>
                        <TableCell>
                          {providerMap.get(item.providerId)?.providerName ?? String(item.providerId ?? '-')}
                        </TableCell>
                        <TableCell>{toModelTypeLabel(item.modelType)}</TableCell>
                        <TableCell>{renderCapBadges(item)}</TableCell>
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
                              onClick={() => setDeletingModel(item)}
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
        <DialogContent className='sm:max-w-2xl max-h-[90vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>{editingModel ? '编辑模型' : '新建模型'}</DialogTitle>
            <DialogDescription>供应商和模型编码创建后不可修改。</DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className='grid gap-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='providerId'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>供应商</FormLabel>
                      <Select
                        value={field.value}
                        onValueChange={field.onChange}
                        disabled={Boolean(editingModel)}
                      >
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue placeholder='请选择供应商' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {allProviders.map((p) => (
                            <SelectItem key={p.id} value={String(p.id)}>
                              {p.providerName}
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
                  name='modelCode'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>模型编码</FormLabel>
                      <FormControl>
                        <Input
                          {...field}
                          placeholder='例如：gpt-4o'
                          disabled={Boolean(editingModel)}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='modelName'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>模型名称</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='例如：GPT-4o' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='modelType'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>模型类型</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className='w-full'>
                            <SelectValue placeholder='请选择类型' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {MODEL_TYPE_OPTIONS.map((o) => (
                            <SelectItem key={o.value} value={o.value}>
                              {o.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className='grid grid-cols-5 gap-4'>
                {(
                  [
                    ['capVision', '视觉理解'],
                    ['capToolCall', '工具调用'],
                    ['capStructuredOutput', '结构化输出'],
                    ['capStreaming', '流式输出'],
                    ['capReasoning', '深度推理'],
                  ] as const
                ).map(([name, label]) => (
                  <FormField
                    key={name}
                    control={form.control}
                    name={name}
                    render={({ field }) => (
                      <FormItem className='flex flex-col items-center gap-1'>
                        <FormLabel className='text-xs'>{label}</FormLabel>
                        <FormControl>
                          <Switch
                            checked={field.value}
                            onCheckedChange={field.onChange}
                          />
                        </FormControl>
                      </FormItem>
                    )}
                  />
                ))}
              </div>

              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='maxContextWindow'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>最大上下文窗口</FormLabel>
                      <FormControl>
                        <Input {...field} value={field.value ?? ''} placeholder='Token 数' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='maxOutputTokens'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>最大输出 Token</FormLabel>
                      <FormControl>
                        <Input {...field} value={field.value ?? ''} placeholder='Token 数' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='inputPrice'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>输入价格（每百万 Token）</FormLabel>
                      <FormControl>
                        <Input {...field} value={field.value ?? ''} placeholder='美元' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='outputPrice'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>输出价格（每百万 Token）</FormLabel>
                      <FormControl>
                        <Input {...field} value={field.value ?? ''} placeholder='美元' />
                      </FormControl>
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
                          <SelectItem value='0'>停用</SelectItem>
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
        open={Boolean(deletingModel)}
        onOpenChange={(open) => {
          if (!open) setDeletingModel(null)
        }}
        title='删除模型'
        desc={`确认删除模型 ${deletingModel?.modelName || '-'}（${deletingModel?.modelCode || '-'}）吗？`}
        confirmText='确认删除'
        destructive
        isLoading={deletingLoading}
        handleConfirm={confirmDelete}
      />
    </>
  )
}
