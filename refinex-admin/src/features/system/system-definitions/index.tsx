import {useEffect, useState} from 'react'
import {zodResolver} from '@hookform/resolvers/zod'
import {Loader2, Pencil, Plus, RefreshCw, Search as SearchIcon} from 'lucide-react'
import {useForm} from 'react-hook-form'
import {toast} from 'sonner'
import {z} from 'zod'
import {
    createSystem,
    listSystems,
  type SystemCreateRequest,
  type SystemDefinition,
  type SystemListQuery,
  type SystemUpdateRequest,
  updateSystem,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import {handleServerError} from '@/lib/handle-server-error'
import {Badge} from '@/components/ui/badge'
import {Button} from '@/components/ui/button'
import {Card, CardContent} from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage,} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from '@/components/ui/select'
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table'
import {Textarea} from '@/components/ui/textarea'
import {ConfigDrawer} from '@/components/config-drawer'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {ThemeSwitch} from '@/components/theme-switch'

const systemFormSchema = z.object({
    systemCode: z
        .string()
        .trim()
        .min(1, '系统编码不能为空。')
        .max(64, '系统编码长度不能超过 64 个字符。'),
    systemName: z
        .string()
        .trim()
        .min(1, '系统名称不能为空。')
        .max(128, '系统名称长度不能超过 128 个字符。'),
    systemType: z.enum(['0', '1', '2']),
    status: z.enum(['1', '2']),
    baseUrl: z.string().trim().max(255, '系统地址长度不能超过 255 个字符。').optional(),
    sort: z.string().trim().max(6, '排序值过大。').optional(),
    remark: z.string().trim().max(255, '备注长度不能超过 255 个字符。').optional(),
})

type SystemFormValues = z.infer<typeof systemFormSchema>

const DEFAULT_FORM_VALUES: SystemFormValues = {
    systemCode: '',
    systemName: '',
    systemType: '2',
    status: '1',
    baseUrl: '',
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

function toSystemTypeLabel(value?: number): string {
    if (value === 0) return '平台系统'
    if (value === 1) return '租户系统'
    if (value === 2) return '业务子系统'
    return '-'
}

function toStatusLabel(value?: number): string {
    if (value === 1) return '启用'
    if (value === 2) return '停用'
    return '-'
}

export function SystemDefinitionsPage() {
    const [systems, setSystems] = useState<SystemDefinition[]>([])
    const [total, setTotal] = useState(0)
    const [loading, setLoading] = useState(false)
    const [query, setQuery] = useState<SystemListQuery>({ currentPage: 1, pageSize: 10 })
    const [keywordInput, setKeywordInput] = useState('')
    const [statusInput, setStatusInput] = useState<'all' | '1' | '2'>('all')
    const [dialogOpen, setDialogOpen] = useState(false)
    const [saving, setSaving] = useState(false)
    const [editingSystem, setEditingSystem] = useState<SystemDefinition | null>(null)

    const form = useForm<SystemFormValues>({
        resolver: zodResolver(systemFormSchema),
        defaultValues: DEFAULT_FORM_VALUES,
        mode: 'onChange',
    })

    async function loadSystems(activeQuery: SystemListQuery = query) {
        setLoading(true)
        try {
            const pageData = await listSystems(activeQuery)
            setSystems(pageData.data ?? [])
            setTotal(pageData.total ?? 0)
        } catch (error) {
            handleServerError(error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        void loadSystems(query)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [query])

    function openCreateDialog() {
        setEditingSystem(null)
        form.reset(DEFAULT_FORM_VALUES)
        setDialogOpen(true)
    }

    function openEditDialog(system: SystemDefinition) {
        setEditingSystem(system)
        form.reset({
            systemCode: system.systemCode ?? '',
            systemName: system.systemName ?? '',
            systemType: String(system.systemType ?? 2) as '0' | '1' | '2',
            status: String(system.status ?? 1) as '1' | '2',
            baseUrl: system.baseUrl ?? '',
            sort: String(system.sort ?? 0),
            remark: system.remark ?? '',
        })
        setDialogOpen(true)
    }

    function closeDialog() {
        setDialogOpen(false)
        setEditingSystem(null)
        form.reset(DEFAULT_FORM_VALUES)
    }

    async function onSubmit(values: SystemFormValues) {
        setSaving(true)
        try {
            const basePayload: SystemUpdateRequest = {
                systemName: values.systemName.trim(),
                systemType: Number(values.systemType),
                status: Number(values.status),
                baseUrl: toOptionalString(values.baseUrl),
                sort: toOptionalNumber(values.sort),
                remark: toOptionalString(values.remark),
            }

            if (editingSystem?.id) {
                await updateSystem(editingSystem.id, basePayload)
                toast.success('系统定义更新成功。')
            } else {
                const createPayload: SystemCreateRequest = {
                    ...basePayload,
                    systemCode: values.systemCode.trim(),
                    systemName: values.systemName.trim(),
                }
                await createSystem(createPayload)
                toast.success('系统定义创建成功。')
            }

            closeDialog()
            await loadSystems(query)
        } catch (error) {
            handleServerError(error)
        } finally {
            setSaving(false)
        }
    }

    function applyFilter() {
        const nextQuery: SystemListQuery = {
            keyword: toOptionalString(keywordInput),
            status: statusInput === 'all' ? undefined : Number(statusInput),
            currentPage: 1,
            pageSize: query.pageSize ?? 10,
        }
        setQuery(nextQuery)
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

    return (
        <>
            <Header>
                <Search/>
                <div className='ms-auto flex items-center gap-4'>
                    <ThemeSwitch/>
                    <ConfigDrawer/>
                    <ProfileDropdown/>
                </div>
            </Header>

            <Main fixed fluid>
                <Card className='py-3 gap-3'>
                    {/*<CardHeader>*/}
                    {/*  <CardTitle>筛选条件</CardTitle>*/}
                    {/*  <CardDescription>支持按系统状态和关键字（编码/名称）查询。</CardDescription>*/}
                    {/*</CardHeader>*/}
                    <CardContent className='pt-0 grid gap-3 md:grid-cols-[1fr_180px_auto]'>
                        <Input
                            value={keywordInput}
                            placeholder='请输入系统编码或名称'
                            onChange={(event) => setKeywordInput(event.target.value)}
                            onKeyDown={(event) => {
                                if (event.key === 'Enter') {
                                    event.preventDefault()
                                    applyFilter()
                                }
                            }}
                        />
                        <Select value={statusInput}
                                onValueChange={(value) => setStatusInput(value as 'all' | '1' | '2')}>
                            <SelectTrigger>
                                <SelectValue placeholder='状态'/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value='all'>全部状态</SelectItem>
                                <SelectItem value='1'>启用</SelectItem>
                                <SelectItem value='2'>停用</SelectItem>
                            </SelectContent>
                        </Select>
                        <div className='flex items-center gap-2'>
                            <Button type='button' variant='outline' onClick={applyFilter} className='gap-2'>
                                <SearchIcon className='h-4 w-4'/>
                                查询
                            </Button>
                            <Button type='button' variant='outline' onClick={resetFilter} className='gap-2'>
                                <RefreshCw className='h-4 w-4'/>
                                重置
                            </Button>
                            <Button type='button' onClick={openCreateDialog} className='gap-2'>
                                <Plus className='h-4 w-4'/>
                                新建系统
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
                                    <TableHead>系统编码</TableHead>
                                    <TableHead>系统名称</TableHead>
                                    <TableHead>系统类型</TableHead>
                                    <TableHead>基础地址</TableHead>
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
                                                <div
                                                    className='flex items-center justify-center gap-2 py-6 text-muted-foreground'>
                                                    <Loader2 className='h-4 w-4 animate-spin'/>
                                                    正在加载系统定义...
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ) : systems.length === 0 ? (
                                        <TableRow>
                                            <TableCell colSpan={8} className='py-6 text-center text-muted-foreground'>
                                                暂无系统定义数据
                                            </TableCell>
                                        </TableRow>
                                    ) : (
                                        systems.map((system) => (
                                            <TableRow key={system.id}>
                                                <TableCell>{system.systemCode || '-'}</TableCell>
                                                <TableCell>{system.systemName || '-'}</TableCell>
                                                <TableCell>{toSystemTypeLabel(system.systemType)}</TableCell>
                                                <TableCell>{system.baseUrl || '-'}</TableCell>
                                                <TableCell>
                                                    <Badge variant={system.status === 1 ? 'default' : 'secondary'}>
                                                        {toStatusLabel(system.status)}
                                                    </Badge>
                                                </TableCell>
                                                <TableCell>{system.sort ?? '-'}</TableCell>
                                                <TableCell
                                                    className='max-w-[280px] truncate'>{system.remark || '-'}</TableCell>
                                                <TableCell className='text-center'>
                                                    <Button
                                                        type='button'
                                                        variant='ghost'
                                                        size='sm'
                                                        className='mx-auto gap-1'
                                                        onClick={() => openEditDialog(system)}
                                                    >
                                                        <Pencil className='h-4 w-4'/>
                                                        编辑
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
                <DialogContent className='sm:max-w-2xl'>
                    <DialogHeader>
                        <DialogTitle>{editingSystem ? '编辑系统定义' : '新建系统定义'}</DialogTitle>
                        <DialogDescription>系统编码用于全局唯一标识，创建后建议保持稳定。</DialogDescription>
                    </DialogHeader>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className='grid gap-4'>
                            <div className='grid gap-4 md:grid-cols-2'>
                                <FormField
                                    control={form.control}
                                    name='systemCode'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>系统编码</FormLabel>
                                            <FormControl>
                                                <Input {...field} placeholder='例如：REFINEX_AUTH'
                                                       disabled={Boolean(editingSystem)}/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name='systemName'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>系统名称</FormLabel>
                                            <FormControl>
                                                <Input {...field} placeholder='请输入系统名称'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className='grid gap-4 md:grid-cols-2'>
                                <FormField
                                    control={form.control}
                                    name='systemType'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>系统类型</FormLabel>
                                            <Select value={field.value} onValueChange={field.onChange}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder='请选择系统类型'/>
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value='0'>平台系统</SelectItem>
                                                    <SelectItem value='1'>租户系统</SelectItem>
                                                    <SelectItem value='2'>业务子系统</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name='status'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>状态</FormLabel>
                                            <Select value={field.value} onValueChange={field.onChange}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder='请选择状态'/>
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value='1'>启用</SelectItem>
                                                    <SelectItem value='2'>停用</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <div className='grid gap-4 md:grid-cols-2'>
                                <FormField
                                    control={form.control}
                                    name='baseUrl'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>基础地址</FormLabel>
                                            <FormControl>
                                                <Input {...field} value={field.value ?? ''}
                                                       placeholder='例如：https://api.example.com'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name='sort'
                                    render={({field}) => (
                                        <FormItem>
                                            <FormLabel>排序</FormLabel>
                                            <FormControl>
                                                <Input {...field} value={field.value ?? ''}
                                                       placeholder='默认 0，数字越小越靠前'/>
                                            </FormControl>
                                            <FormMessage/>
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <FormField
                                control={form.control}
                                name='remark'
                                render={({field}) => (
                                    <FormItem>
                                        <FormLabel>备注</FormLabel>
                                        <FormControl>
                                            <Textarea {...field} value={field.value ?? ''} rows={3}
                                                      placeholder='可填写系统用途说明'/>
                                        </FormControl>
                                        <FormMessage/>
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
                                            <Loader2 className='mr-2 h-4 w-4 animate-spin'/>
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
        </>
    )
}
