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
  getOperateLog,
  listOperateLogs,
  type OperateLog,
  type OperateLogListQuery,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'
import { formatDateTime, toOptionalNumber, toOptionalString } from './common'

export function OperateLogsPage() {
  const [logs, setLogs] = useState<OperateLog[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [userIdInput, setUserIdInput] = useState('')
  const [successInput, setSuccessInput] = useState<'all' | '1' | '0'>('all')
  const [moduleCodeInput, setModuleCodeInput] = useState('')
  const [requestPathInput, setRequestPathInput] = useState('')
  const [startTimeInput, setStartTimeInput] = useState('')
  const [endTimeInput, setEndTimeInput] = useState('')
  const [query, setQuery] = useState<OperateLogListQuery>({ currentPage: 1, pageSize: 10 })

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detail, setDetail] = useState<OperateLog | null>(null)

  async function loadLogs(activeQuery: OperateLogListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listOperateLogs(activeQuery)
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
      userId: toOptionalNumber(userIdInput),
      success: successInput === 'all' ? undefined : Number(successInput),
      moduleCode: toOptionalString(moduleCodeInput),
      requestPath: toOptionalString(requestPathInput),
      startTime: startTimeInput || undefined,
      endTime: endTimeInput || undefined,
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function resetFilter() {
    setUserIdInput('')
    setSuccessInput('all')
    setModuleCodeInput('')
    setRequestPathInput('')
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
      const data = await getOperateLog(logId)
      setDetail(data)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDetailLoading(false)
    }
  }

  return (
    <>
      <Card>
        <CardContent className='grid gap-3 xl:grid-cols-[120px_130px_180px_1fr_1fr_1fr_auto]'>
          <Input placeholder='用户ID' value={userIdInput} onChange={(event) => setUserIdInput(event.target.value)} />
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
          <Input placeholder='模块编码' value={moduleCodeInput} onChange={(event) => setModuleCodeInput(event.target.value)} />
          <Input placeholder='请求路径' value={requestPathInput} onChange={(event) => setRequestPathInput(event.target.value)} />
          <Input type='datetime-local' value={startTimeInput} onChange={(event) => setStartTimeInput(event.target.value)} />
          <Input type='datetime-local' value={endTimeInput} onChange={(event) => setEndTimeInput(event.target.value)} />
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

      <Card className='mt-4 overflow-hidden'>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className='w-[90px]'>用户ID</TableHead>
                <TableHead>模块</TableHead>
                <TableHead>操作</TableHead>
                <TableHead>目标</TableHead>
                <TableHead className='w-[84px] text-center'>结果</TableHead>
                <TableHead>请求路径</TableHead>
                <TableHead className='w-[100px] text-right'>耗时(ms)</TableHead>
                <TableHead>操作时间</TableHead>
                <TableHead className='w-[80px] text-center'>详情</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={9}>
                    <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                      <Loader2 className='h-4 w-4 animate-spin' />
                      正在加载操作日志...
                    </div>
                  </TableCell>
                </TableRow>
              ) : logs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} className='py-8 text-center text-muted-foreground'>
                    暂无操作日志
                  </TableCell>
                </TableRow>
              ) : (
                logs.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.userId ?? '-'}</TableCell>
                    <TableCell>{item.moduleCode || '-'}</TableCell>
                    <TableCell>{item.operation || '-'}</TableCell>
                    <TableCell>{item.targetType && item.targetId ? `${item.targetType}:${item.targetId}` : '-'}</TableCell>
                    <TableCell className='text-center'>
                      <Badge variant={item.success === 1 ? 'default' : 'secondary'}>
                        {item.success === 1 ? '成功' : '失败'}
                      </Badge>
                    </TableCell>
                    <TableCell className='max-w-[260px] truncate'>{item.requestPath || '-'}</TableCell>
                    <TableCell className='text-right'>{item.durationMs ?? '-'}</TableCell>
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
            <DialogTitle>操作日志详情</DialogTitle>
          </DialogHeader>
          {detailLoading ? (
            <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              正在加载详情...
            </div>
          ) : (
            <div className='grid gap-3 md:grid-cols-2'>
              <Input disabled value={`日志ID：${detail?.id ?? '-'}`} />
              <Input disabled value={`用户ID：${detail?.userId ?? '-'}`} />
              <Input disabled value={`企业ID：${detail?.estabId ?? '-'}`} />
              <Input disabled value={`模块编码：${detail?.moduleCode || '-'}`} />
              <Input disabled value={`操作：${detail?.operation || '-'}`} />
              <Input disabled value={`结果：${detail?.success === 1 ? '成功' : '失败'}`} />
              <Input disabled value={`目标：${detail?.targetType && detail?.targetId ? `${detail.targetType}:${detail.targetId}` : '-'}`} />
              <Input disabled value={`耗时：${detail?.durationMs ?? '-'} ms`} />
              <Input disabled value={`请求方式：${detail?.requestMethod || '-'}`} />
              <Input disabled value={`请求路径：${detail?.requestPath || '-'}`} />
              <Input disabled value={`调用IP：${detail?.ip || '-'}`} />
              <Input disabled value={`操作时间：${formatDateTime(detail?.gmtCreate)}`} />
              <div className='md:col-span-2'>
                <Input disabled value={`失败原因：${detail?.failReason || '-'}`} />
              </div>
              <div className='md:col-span-2'>
                <Input disabled value={`TraceId：${detail?.traceId || '-'} / SpanId：${detail?.spanId || '-'}`} />
              </div>
              <div className='md:col-span-2'>
                <textarea
                  className='min-h-20 w-full rounded-md border bg-muted/30 p-3 text-sm'
                  readOnly
                  value={`请求参数：${detail?.requestParams || '-'}\n\n响应内容：${detail?.responseBody || '-'}`}
                />
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  )
}
