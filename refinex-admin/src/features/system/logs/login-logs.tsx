import { useEffect, useState } from 'react'
import { Eye, Loader2, RefreshCw, Search as SearchIcon } from 'lucide-react'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
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
import {
  getLoginLog,
  listLoginLogs,
  type LoginLog,
  type LoginLogListQuery,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'
import { formatDateTime } from './common'

const LOGIN_TYPE_LABEL: Record<number, string> = {
  1: '用户名密码',
  2: '短信验证码',
  3: '邮箱密码',
  4: '邮箱验证码',
}

const SOURCE_TYPE_LABEL: Record<number, string> = {
  1: 'Web',
  2: 'APP',
  3: 'H5',
  4: '小程序',
  5: 'API',
}

export function LoginLogsPage() {
  const [logs, setLogs] = useState<LoginLog[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [successInput, setSuccessInput] = useState<'all' | '1' | '0'>('all')
  const [loginTypeInput, setLoginTypeInput] = useState<'all' | '1' | '2' | '3' | '4'>('all')
  const [sourceTypeInput, setSourceTypeInput] = useState<'all' | '1' | '2' | '3' | '4' | '5'>('all')
  const [startTimeInput, setStartTimeInput] = useState('')
  const [endTimeInput, setEndTimeInput] = useState('')
  const [query, setQuery] = useState<LoginLogListQuery>({ currentPage: 1, pageSize: 10 })

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detail, setDetail] = useState<LoginLog | null>(null)

  async function loadLogs(activeQuery: LoginLogListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listLoginLogs(activeQuery)
      setLogs(pageData.data ?? [])
      setTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadLogs(query)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  function applyFilter() {
    setQuery({
      success: successInput === 'all' ? undefined : Number(successInput),
      loginType: loginTypeInput === 'all' ? undefined : Number(loginTypeInput),
      sourceType: sourceTypeInput === 'all' ? undefined : Number(sourceTypeInput),
      startTime: startTimeInput || undefined,
      endTime: endTimeInput || undefined,
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function resetFilter() {
    setSuccessInput('all')
    setLoginTypeInput('all')
    setSourceTypeInput('all')
    setStartTimeInput('')
    setEndTimeInput('')
    setQuery({ currentPage: 1, pageSize: query.pageSize ?? 10 })
  }

  function handlePageChange(page: number) {
    setQuery((prev) => ({ ...prev, currentPage: page }))
  }

  function handlePageSizeChange(size: number) {
    setQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
  }

  async function openDetail(logId?: number) {
    if (!logId) return
    setDetailOpen(true)
    setDetailLoading(true)
    try {
      const data = await getLoginLog(logId)
      setDetail(data)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDetailLoading(false)
    }
  }

  return (
    <>
      <Card className='py-3 gap-3'>
        <CardContent className='pt-0 grid gap-3 xl:grid-cols-[140px_140px_140px_1fr_1fr_auto]'>
          <Select value={successInput} onValueChange={(value) => setSuccessInput(value as 'all' | '1' | '0')}>
            <SelectTrigger>
              <SelectValue placeholder='结果' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>全部结果</SelectItem>
              <SelectItem value='1'>成功</SelectItem>
              <SelectItem value='0'>失败</SelectItem>
            </SelectContent>
          </Select>
          <Select value={loginTypeInput} onValueChange={(value) => setLoginTypeInput(value as 'all' | '1' | '2' | '3' | '4')}>
            <SelectTrigger>
              <SelectValue placeholder='登录方式' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>全部方式</SelectItem>
              <SelectItem value='1'>用户名密码</SelectItem>
              <SelectItem value='2'>短信验证码</SelectItem>
              <SelectItem value='3'>邮箱密码</SelectItem>
              <SelectItem value='4'>邮箱验证码</SelectItem>
            </SelectContent>
          </Select>
          <Select value={sourceTypeInput} onValueChange={(value) => setSourceTypeInput(value as 'all' | '1' | '2' | '3' | '4' | '5')}>
            <SelectTrigger>
              <SelectValue placeholder='来源' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>全部来源</SelectItem>
              <SelectItem value='1'>Web</SelectItem>
              <SelectItem value='2'>APP</SelectItem>
              <SelectItem value='3'>H5</SelectItem>
              <SelectItem value='4'>小程序</SelectItem>
              <SelectItem value='5'>API</SelectItem>
            </SelectContent>
          </Select>
          <Input
            type='datetime-local'
            value={startTimeInput}
            onChange={(event) => setStartTimeInput(event.target.value)}
          />
          <Input
            type='datetime-local'
            value={endTimeInput}
            onChange={(event) => setEndTimeInput(event.target.value)}
          />
          <div className='flex items-center gap-2'>
            <Button type='button' variant='outline' onClick={applyFilter} className='gap-2'>
              <SearchIcon className='h-4 w-4' />
              查询
            </Button>
            <Button type='button' variant='outline' onClick={resetFilter} className='gap-2'>
              <RefreshCw className='h-4 w-4' />
              重置
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card className='mt-2 overflow-hidden py-3 gap-3'>
        <CardContent className='pt-0'>
          <div className='overflow-hidden rounded-md border border-border/90'>
            <Table className='[&_td]:border-r [&_td]:border-border/70 [&_td:last-child]:border-r-0 [&_th]:border-r [&_th]:border-border/70 [&_th:last-child]:border-r-0'>
              <TableHeader>
                <TableRow className='bg-muted/30 hover:bg-muted/30'>
                <TableHead className='w-[140px]'>用户名</TableHead>
                <TableHead className='w-[120px]'>企业ID</TableHead>
                <TableHead>登录方式</TableHead>
                <TableHead>来源</TableHead>
                <TableHead className='w-[100px] text-center'>结果</TableHead>
                <TableHead>失败原因</TableHead>
                <TableHead>IP</TableHead>
                <TableHead>登录时间</TableHead>
                <TableHead className='w-[80px] text-center'>详情</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={9}>
                      <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                        <Loader2 className='h-4 w-4 animate-spin' />
                        正在加载登录日志...
                      </div>
                    </TableCell>
                  </TableRow>
                ) : logs.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={9} className='py-8 text-center text-muted-foreground'>
                      暂无登录日志
                    </TableCell>
                  </TableRow>
                ) : (
                  logs.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.username || '-'}</TableCell>
                      <TableCell>{item.estabId ?? '-'}</TableCell>
                      <TableCell>{item.loginType == null ? '-' : (LOGIN_TYPE_LABEL[item.loginType] ?? item.loginType)}</TableCell>
                      <TableCell>{item.sourceType == null ? '-' : (SOURCE_TYPE_LABEL[item.sourceType] ?? item.sourceType)}</TableCell>
                      <TableCell className='text-center'>
                        <Badge variant={item.success === 1 ? 'default' : 'secondary'}>
                          {item.success === 1 ? '成功' : '失败'}
                        </Badge>
                      </TableCell>
                      <TableCell className='max-w-[260px] truncate'>{item.failureReason || '-'}</TableCell>
                      <TableCell>{item.ip || '-'}</TableCell>
                      <TableCell>{formatDateTime(item.gmtCreate)}</TableCell>
                      <TableCell>
                        <div className='flex justify-center'>
                          <Button type='button' variant='ghost' size='icon' className='h-8 w-8' onClick={() => void openDetail(item.id)}>
                            <Eye className='h-4 w-4' />
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

      <Dialog open={detailOpen} onOpenChange={setDetailOpen}>
        <DialogContent className='sm:max-w-4xl'>
          <DialogHeader>
            <DialogTitle>登录日志详情</DialogTitle>
          </DialogHeader>
          {detailLoading ? (
            <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              正在加载详情...
            </div>
          ) : (
            <div className='grid gap-3 md:grid-cols-2'>
              <Input disabled value={`日志ID：${detail?.id ?? '-'}`} />
              <Input disabled value={`用户名：${detail?.username || '-'}`} />
              <Input disabled value={`企业ID：${detail?.estabId ?? '-'}`} />
              <Input disabled value={`身份ID：${detail?.identityId ?? '-'}`} />
              <Input disabled value={`登录方式：${detail?.loginType == null ? '-' : (LOGIN_TYPE_LABEL[detail.loginType] ?? detail.loginType)}`} />
              <Input disabled value={`来源：${detail?.sourceType == null ? '-' : (SOURCE_TYPE_LABEL[detail.sourceType] ?? detail.sourceType)}`} />
              <Input disabled value={`结果：${detail?.success === 1 ? '成功' : '失败'}`} />
              <Input disabled value={`登录IP：${detail?.ip || '-'}`} />
              <Input disabled value={`设备ID：${detail?.deviceId || '-'}`} />
              <Input disabled value={`客户端ID：${detail?.clientId || '-'}`} />
              <Input disabled value={`RequestId：${detail?.requestId || '-'}`} />
              <Input disabled value={`登录时间：${formatDateTime(detail?.gmtCreate)}`} />
              <div className='md:col-span-2'>
                <Input disabled value={`失败原因：${detail?.failureReason || '-'}`} />
              </div>
              <div className='md:col-span-2'>
                <Input disabled value={`User-Agent：${detail?.userAgent || '-'}`} />
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  )
}
