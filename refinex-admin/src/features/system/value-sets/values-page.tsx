import { useEffect, useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowLeft,
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search as SearchIcon,
  Trash2,
} from 'lucide-react'
import { useForm } from 'react-hook-form'
import { useNavigate } from '@tanstack/react-router'
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
  createValue,
  deleteValue,
  listValues,
  listValueSets,
  updateValue,
  type ValueCreateRequest,
  type ValueItem,
  type ValueUpdateRequest,
} from '@/features/system/api'
import { handleServerError } from '@/lib/handle-server-error'

const valueItemFormSchema = z.object({
  valueCode: z
    .string()
    .trim()
    .min(1, '值编码不能为空。')
    .max(64, '值编码长度不能超过 64 个字符。'),
  valueName: z
    .string()
    .trim()
    .min(1, '值名称不能为空。')
    .max(128, '值名称长度不能超过 128 个字符。'),
  status: z.enum(['1', '0']),
  isDefault: z.enum(['1', '0']),
  sort: z.string().trim().max(6, '排序值过大。').optional(),
  valueDesc: z
    .string()
    .trim()
    .max(255, '值描述长度不能超过 255 个字符。')
    .optional(),
})

type ValueItemFormValues = z.infer<typeof valueItemFormSchema>

const DEFAULT_VALUE_ITEM_FORM: ValueItemFormValues = {
  valueCode: '',
  valueName: '',
  status: '1',
  isDefault: '0',
  sort: '0',
  valueDesc: '',
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

function toStatusLabel(value?: number): string {
  return value === 1 ? '启用' : '停用'
}

type ValueSetValuesPageProps = {
  setCode: string
  setName?: string
}

export function ValueSetValuesPage({ setCode, setName }: ValueSetValuesPageProps) {
  const navigate = useNavigate()
  const [values, setValues] = useState<ValueItem[]>([])
  const [loading, setLoading] = useState(false)
  const [setDisplayName, setSetDisplayName] = useState(setName || '')

  const [keywordInput, setKeywordInput] = useState('')
  const [statusInput, setStatusInput] = useState<'all' | '1' | '0'>('all')
  const [query, setQuery] = useState<{ keyword?: string; status?: number }>({})

  const [dialogOpen, setDialogOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [editingValueItem, setEditingValueItem] = useState<ValueItem | null>(null)
  const [deletingValueItem, setDeletingValueItem] = useState<ValueItem | null>(null)
  const [deletingLoading, setDeletingLoading] = useState(false)

  const form = useForm<ValueItemFormValues>({
    resolver: zodResolver(valueItemFormSchema),
    defaultValues: DEFAULT_VALUE_ITEM_FORM,
    mode: 'onChange',
  })

  const titleText = useMemo(() => {
    if (setDisplayName) {
      return `${setDisplayName}（${setCode}）`
    }
    return setCode
  }, [setCode, setDisplayName])

  async function loadSetMeta() {
    try {
      const list = await listValueSets({ keyword: setCode })
      const matched = list.find((item) => item.setCode === setCode)
      if (matched?.setName) {
        setSetDisplayName(matched.setName)
      }
    } catch {
      // ignore metadata errors
    }
  }

  async function loadValues(activeQuery: { keyword?: string; status?: number } = query) {
    setLoading(true)
    try {
      const data = await listValues({
        setCode,
        keyword: activeQuery.keyword,
        status: activeQuery.status,
      })
      setValues(data)
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadSetMeta()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [setCode])

  useEffect(() => {
    void loadValues(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [setCode, query])

  function applyFilter() {
    setQuery({
      keyword: toOptionalString(keywordInput),
      status: statusInput === 'all' ? undefined : Number(statusInput),
    })
  }

  function resetFilter() {
    setKeywordInput('')
    setStatusInput('all')
    setQuery({})
  }

  function openCreateDialog() {
    setEditingValueItem(null)
    form.reset(DEFAULT_VALUE_ITEM_FORM)
    setDialogOpen(true)
  }

  function openEditDialog(valueItem: ValueItem) {
    setEditingValueItem(valueItem)
    form.reset({
      valueCode: valueItem.valueCode ?? '',
      valueName: valueItem.valueName ?? '',
      status: String(valueItem.status ?? 1) as '1' | '0',
      isDefault: String(valueItem.isDefault ?? 0) as '1' | '0',
      sort: String(valueItem.sort ?? 0),
      valueDesc: valueItem.valueDesc ?? '',
    })
    setDialogOpen(true)
  }

  function closeDialog() {
    setDialogOpen(false)
    setEditingValueItem(null)
    form.reset(DEFAULT_VALUE_ITEM_FORM)
  }

  async function submitValueItem(values: ValueItemFormValues) {
    setSaving(true)
    try {
      const payload: ValueUpdateRequest = {
        valueCode: values.valueCode.trim(),
        valueName: values.valueName.trim(),
        valueDesc: toOptionalString(values.valueDesc),
        status: Number(values.status),
        isDefault: Number(values.isDefault),
        sort: toOptionalNumber(values.sort),
      }

      if (editingValueItem?.id) {
        await updateValue(editingValueItem.id, payload)
        toast.success('集合值更新成功。')
      } else {
        const createPayload: ValueCreateRequest = {
          ...payload,
          valueCode: values.valueCode.trim(),
          valueName: values.valueName.trim(),
        }
        await createValue(setCode, createPayload)
        toast.success('集合值创建成功。')
      }

      closeDialog()
      await loadValues(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function confirmDeleteValueItem() {
    if (!deletingValueItem?.id) return
    setDeletingLoading(true)
    try {
      await deleteValue(deletingValueItem.id)
      toast.success('集合值已删除。')
      setDeletingValueItem(null)
      await loadValues(query)
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
        <Card>
          <CardHeader className='flex flex-row items-center justify-between gap-3'>
            <div className='space-y-1'>
              <CardTitle>集合值定义</CardTitle>
              <p className='text-sm text-muted-foreground'>当前集合：{titleText}</p>
            </div>
            <Button
              type='button'
              variant='outline'
              className='gap-2'
              onClick={() => void navigate({ to: '/system-management/value-sets' })}
            >
              <ArrowLeft className='h-4 w-4' />
              返回集合列表
            </Button>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='grid gap-3 md:grid-cols-[1fr_180px_auto]'>
              <Input
                value={keywordInput}
                placeholder='请输入值编码或值名称'
                onChange={(event) => setKeywordInput(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault()
                    applyFilter()
                  }
                }}
              />
              <Select
                value={statusInput}
                onValueChange={(value) => setStatusInput(value as 'all' | '1' | '0')}
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
                  新建集合值
                </Button>
              </div>
            </div>

            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>值编码</TableHead>
                  <TableHead>值名称</TableHead>
                  <TableHead className='w-[88px] text-center'>默认</TableHead>
                  <TableHead className='w-[88px] text-center'>状态</TableHead>
                  <TableHead className='w-[80px] text-center'>排序</TableHead>
                  <TableHead>值描述</TableHead>
                  <TableHead className='w-[108px] text-center'>操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                        <Loader2 className='h-4 w-4 animate-spin' />
                        正在加载集合值...
                      </div>
                    </TableCell>
                  </TableRow>
                ) : values.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className='py-8 text-center text-muted-foreground'>
                      当前集合暂无集合值
                    </TableCell>
                  </TableRow>
                ) : (
                  values.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className='font-medium'>{item.valueCode || '-'}</TableCell>
                      <TableCell>{item.valueName || '-'}</TableCell>
                      <TableCell className='text-center'>
                        <Badge variant={item.isDefault === 1 ? 'default' : 'outline'}>
                          {item.isDefault === 1 ? '默认' : '普通'}
                        </Badge>
                      </TableCell>
                      <TableCell className='text-center'>
                        <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                          {toStatusLabel(item.status)}
                        </Badge>
                      </TableCell>
                      <TableCell className='text-center'>{item.sort ?? '-'}</TableCell>
                      <TableCell className='max-w-[480px] truncate'>{item.valueDesc || '-'}</TableCell>
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
                            onClick={() => setDeletingValueItem(item)}
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
          </CardContent>
        </Card>
      </Main>

      <Dialog open={dialogOpen} onOpenChange={(open) => (open ? setDialogOpen(true) : closeDialog())}>
        <DialogContent className='sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>{editingValueItem ? '编辑集合值' : '新建集合值'}</DialogTitle>
            <DialogDescription>当前集合：{titleText}</DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(submitValueItem)} className='grid gap-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='valueCode'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>值编码</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='例如：ENABLED' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='valueName'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>值名称</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='例如：启用' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className='grid gap-4 md:grid-cols-3'>
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
                          <SelectItem value='0'>停用</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='isDefault'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>默认值</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder='请选择默认标识' />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value='1'>默认</SelectItem>
                          <SelectItem value='0'>普通</SelectItem>
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
                        <Input {...field} value={field.value ?? ''} placeholder='默认 0' />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <FormField
                control={form.control}
                name='valueDesc'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>值描述</FormLabel>
                    <FormControl>
                      <Textarea {...field} value={field.value ?? ''} rows={3} placeholder='可填写使用说明' />
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
        open={Boolean(deletingValueItem)}
        onOpenChange={(open) => {
          if (!open) setDeletingValueItem(null)
        }}
        title='删除集合值'
        desc={`确认删除集合值 ${deletingValueItem?.valueName || '-'}（${deletingValueItem?.valueCode || '-'}）吗？`}
        confirmText='确认删除'
        destructive
        isLoading={deletingLoading}
        handleConfirm={confirmDeleteValueItem}
      />
    </>
  )
}
