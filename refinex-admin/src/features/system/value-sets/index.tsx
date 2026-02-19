import { useEffect, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  Eye,
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search as SearchIcon,
  Trash2,
} from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Outlet, useLocation, useNavigate } from '@tanstack/react-router'
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
  createValueSet,
  deleteValueSet,
  listValueSets,
  type ValueSet,
  type ValueSetCreateRequest,
  type ValueSetListQuery,
  type ValueSetUpdateRequest,
  updateValueSet,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'

const valueSetFormSchema = z.object({
  setCode: z
    .string()
    .trim()
    .min(1, '集合编码不能为空。')
    .max(64, '集合编码长度不能超过 64 个字符。'),
  setName: z
    .string()
    .trim()
    .min(1, '集合名称不能为空。')
    .max(128, '集合名称长度不能超过 128 个字符。'),
  status: z.enum(['1', '0']),
  sort: z.string().trim().max(6, '排序值过大。').optional(),
  description: z
    .string()
    .trim()
    .max(255, '描述长度不能超过 255 个字符。')
    .optional(),
})

type ValueSetFormValues = z.infer<typeof valueSetFormSchema>

const DEFAULT_VALUE_SET_FORM: ValueSetFormValues = {
  setCode: '',
  setName: '',
  status: '1',
  sort: '0',
  description: '',
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

export function ValueSetsPage() {
  const navigate = useNavigate()
  const pathname = useLocation({ select: (location) => location.pathname })
  const isValuesChildRoute = pathname.startsWith('/system-management/value-sets/')

  if (isValuesChildRoute) {
    return <Outlet />
  }

  const [valueSets, setValueSets] = useState<ValueSet[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [keywordInput, setKeywordInput] = useState('')
  const [statusInput, setStatusInput] = useState<'all' | '1' | '0'>('all')
  const [query, setQuery] = useState<ValueSetListQuery>({ currentPage: 1, pageSize: 10 })

  const [dialogOpen, setDialogOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [editingValueSet, setEditingValueSet] = useState<ValueSet | null>(null)
  const [deletingValueSet, setDeletingValueSet] = useState<ValueSet | null>(null)
  const [deletingLoading, setDeletingLoading] = useState(false)

  const form = useForm<ValueSetFormValues>({
    resolver: zodResolver(valueSetFormSchema),
    defaultValues: DEFAULT_VALUE_SET_FORM,
    mode: 'onChange',
  })

  async function loadValueSets(activeQuery: ValueSetListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listValueSets(activeQuery)
      setValueSets(pageData.data ?? [])
      setTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadValueSets(query)
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
    setQuery({
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  function openCreateDialog() {
    setEditingValueSet(null)
    form.reset(DEFAULT_VALUE_SET_FORM)
    setDialogOpen(true)
  }

  function openEditDialog(valueSet: ValueSet) {
    setEditingValueSet(valueSet)
    form.reset({
      setCode: valueSet.setCode ?? '',
      setName: valueSet.setName ?? '',
      status: String(valueSet.status ?? 1) as '1' | '0',
      sort: String(valueSet.sort ?? 0),
      description: valueSet.description ?? '',
    })
    setDialogOpen(true)
  }

  function closeDialog() {
    setDialogOpen(false)
    setEditingValueSet(null)
    form.reset(DEFAULT_VALUE_SET_FORM)
  }

  async function submitValueSet(values: ValueSetFormValues) {
    setSaving(true)
    try {
      const payload: ValueSetUpdateRequest = {
        setName: values.setName.trim(),
        status: Number(values.status),
        sort: toOptionalNumber(values.sort),
        description: toOptionalString(values.description),
      }

      if (editingValueSet?.id) {
        await updateValueSet(editingValueSet.id, payload)
        toast.success('集合定义更新成功。')
      } else {
        const createPayload: ValueSetCreateRequest = {
          ...payload,
          setCode: values.setCode.trim(),
          setName: values.setName.trim(),
        }
        await createValueSet(createPayload)
        toast.success('集合定义创建成功。')
      }

      closeDialog()
      await loadValueSets(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  async function confirmDeleteValueSet() {
    if (!deletingValueSet?.id) return
    setDeletingLoading(true)
    try {
      await deleteValueSet(deletingValueSet.id)
      toast.success('集合定义已删除。')
      setDeletingValueSet(null)
      await loadValueSets(query)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingLoading(false)
    }
  }

  function openValueItems(valueSet: ValueSet) {
    if (!valueSet.setCode) {
      toast.error('当前集合编码为空，无法进入集合值页面。')
      return
    }
    void navigate({
      to: '/system-management/value-sets/$setCode',
      params: { setCode: valueSet.setCode },
      search: { setName: valueSet.setName || '' },
    })
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
          <CardContent className='grid gap-3 md:grid-cols-[1fr_180px_auto]'>
            <Input
              value={keywordInput}
              placeholder='请输入集合编码或集合名称'
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
                新建集合
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className='mt-4 grow overflow-hidden'>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>集合编码</TableHead>
                  <TableHead>集合名称</TableHead>
                  <TableHead className='w-[88px] text-center'>状态</TableHead>
                  <TableHead className='w-[80px] text-center'>排序</TableHead>
                  <TableHead>描述</TableHead>
                  <TableHead className='w-[188px] text-center'>操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                        <Loader2 className='h-4 w-4 animate-spin' />
                        正在加载集合定义...
                      </div>
                    </TableCell>
                  </TableRow>
                ) : valueSets.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className='py-8 text-center text-muted-foreground'>
                      暂无集合定义
                    </TableCell>
                  </TableRow>
                ) : (
                  valueSets.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className='font-medium'>{item.setCode || '-'}</TableCell>
                      <TableCell>{item.setName || '-'}</TableCell>
                      <TableCell className='text-center'>
                        <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                          {toStatusLabel(item.status)}
                        </Badge>
                      </TableCell>
                      <TableCell className='text-center'>{item.sort ?? '-'}</TableCell>
                      <TableCell className='max-w-[420px] truncate'>{item.description || '-'}</TableCell>
                      <TableCell>
                        <div className='flex items-center justify-center gap-1'>
                          <Button
                            type='button'
                            variant='outline'
                            size='sm'
                            className='h-8 gap-1'
                            onClick={() => openValueItems(item)}
                          >
                            <Eye className='h-4 w-4' />
                            集合值
                          </Button>
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
                            onClick={() => setDeletingValueSet(item)}
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

      <Dialog open={dialogOpen} onOpenChange={(open) => (open ? setDialogOpen(true) : closeDialog())}>
        <DialogContent className='sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>{editingValueSet ? '编辑集合定义' : '新建集合定义'}</DialogTitle>
            <DialogDescription>集合编码创建后建议保持稳定，供业务模块与接口复用。</DialogDescription>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(submitValueSet)} className='grid gap-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <FormField
                  control={form.control}
                  name='setCode'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>集合编码</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='例如：user_status' disabled={Boolean(editingValueSet)} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name='setName'
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>集合名称</FormLabel>
                      <FormControl>
                        <Input {...field} placeholder='例如：用户状态' />
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
                name='description'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>描述</FormLabel>
                    <FormControl>
                      <Textarea {...field} value={field.value ?? ''} rows={3} placeholder='用于说明集合用途' />
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
        open={Boolean(deletingValueSet)}
        onOpenChange={(open) => {
          if (!open) setDeletingValueSet(null)
        }}
        title='删除集合定义'
        desc={`确认删除集合 ${deletingValueSet?.setName || '-'}（${deletingValueSet?.setCode || '-'}）吗？`}
        confirmText='确认删除'
        destructive
        isLoading={deletingLoading}
        handleConfirm={confirmDeleteValueSet}
      />
    </>
  )
}
