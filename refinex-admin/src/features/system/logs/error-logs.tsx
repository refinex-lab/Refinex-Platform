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
  getErrorLog,
  listErrorLogs,
  type ErrorLog,
  type ErrorLogListQuery,
} from '@/features/system/api'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'
import { formatDateTime, toOptionalString } from './common'

const ERROR_LEVEL_LABEL: Record<number, string> = {
  1: '提示',
  2: '警告',
  3: '错误',
  4: '严重',
}

export function ErrorLogsPage() {
  const [logs, setLogs] = useState<ErrorLog[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [serviceInput, setServiceInput] = useState('')
  const [errorLevelInput, setErrorLevelInput] = useState<'all' | '1' | '2' | '3' | '4'>('all')
  const [requestPathInput, setRequestPathInput] = useState('')
  const [startTimeInput, setStartTimeInput] = useState('')
  const [endTimeInput, setEndTimeInput] = useState('')
  const [query, setQuery] = useState<ErrorLogListQuery>({ currentPage: 1, pageSize: 10 })

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detail, setDetail] = useState<ErrorLog | null>(null)

  async function loadLogs(activeQuery: ErrorLogListQuery = query) {
    setLoading(true)
    try {
      const pageData = await listErrorLogs(activeQuery)
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
      serviceName: toOptionalString(serviceInput),
      errorLevel: errorLevelInput === 'all' ? undefined : Number(errorLevelInput),
      requestPath: toOptionalString(requestPathInput),
      startTime: startTimeInput || undefined,
      endTime: endTimeInput || undefined,
      currentPage: 1,
      pageSize: query.pageSize ?? 10,
    })
  }

  function resetFilter() {
    setServiceInput('')
    setErrorLevelInput('all')
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
      const data = await getErrorLog(logId)
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
        <CardContent className='grid gap-3 xl:grid-cols-[180px_130px_1fr_1fr_1fr_auto]'>
          <Input placeholder='服务名称' value={serviceInput} onChange={(event) => setServiceInput(event.target.value)} />
          <Select value={errorLevelInput} onValueChange={(value) => setErrorLevelInput(value as 'all' | '1' | '2' | '3' | '4')}>
            <SelectTrigger>
              <SelectValue placeholder='错误级别' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>全部级别</SelectItem>
              <SelectItem value='1'>提示</SelectItem>
              <SelectItem value='2'>警告</SelectItem>
              <SelectItem value='3'>错误</SelectItem>
              <SelectItem value='4'>严重</SelectItem>
            </SelectContent>
          </Select>
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
                <TableHead>服务</TableHead>
                <TableHead>错误码</TableHead>
                <TableHead className='w-[90px] text-center'>级别</TableHead>
                <TableHead>错误类型</TableHead>
                <TableHead>请求路径</TableHead>
                <TableHead>错误信息</TableHead>
                <TableHead>发生时间</TableHead>
                <TableHead className='w-[80px] text-center'>详情</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={8}>
                    <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                      <Loader2 className='h-4 w-4 animate-spin' />
                      正在加载错误日志...
                    </div>
                  </TableCell>
                </TableRow>
              ) : logs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className='py-8 text-center text-muted-foreground'>
                    暂无错误日志
                  </TableCell>
                </TableRow>
              ) : (
                logs.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.serviceName || '-'}</TableCell>
                    <TableCell>{item.errorCode || '-'}</TableCell>
                    <TableCell className='text-center'>
                      <Badge variant={item.errorLevel != null && item.errorLevel >= 3 ? 'destructive' : 'secondary'}>
                        {item.errorLevel == null ? '-' : (ERROR_LEVEL_LABEL[item.errorLevel] ?? item.errorLevel)}
                      </Badge>
                    </TableCell>
                    <TableCell>{item.errorType || '-'}</TableCell>
                    <TableCell className='max-w-[220px] truncate'>{item.requestPath || '-'}</TableCell>
                    <TableCell className='max-w-[260px] truncate'>{item.message || '-'}</TableCell>
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
        <DialogContent className='sm:max-w-5xl'>
          <DialogHeader>
            <DialogTitle>错误日志详情</DialogTitle>
          </DialogHeader>
          {detailLoading ? (
            <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              正在加载详情...
            </div>
          ) : (
            <div className='grid gap-3'>
              <div className='grid gap-3 md:grid-cols-2'>
                <Input disabled value={`日志ID：${detail?.id ?? '-'}`} />
                <Input disabled value={`服务名称：${detail?.serviceName || '-'}`} />
                <Input disabled value={`错误码：${detail?.errorCode || '-'}`} />
                <Input disabled value={`错误级别：${detail?.errorLevel == null ? '-' : (ERROR_LEVEL_LABEL[detail.errorLevel] ?? detail.errorLevel)}`} />
                <Input disabled value={`错误类型：${detail?.errorType || '-'}`} />
                <Input disabled value={`发生时间：${formatDateTime(detail?.gmtCreate)}`} />
                <Input disabled value={`用户ID：${detail?.userId ?? '-'}`} />
                <Input disabled value={`企业ID：${detail?.estabId ?? '-'}`} />
                <Input disabled value={`RequestId：${detail?.requestId || '-'}`} />
                <Input disabled value={`TraceId：${detail?.traceId || '-'}`} />
                <Input disabled value={`请求方式：${detail?.requestMethod || '-'}`} />
                <Input disabled value={`请求路径：${detail?.requestPath || '-'}`} />
              </div>
              <textarea className='min-h-20 w-full rounded-md border bg-muted/30 p-3 text-sm' readOnly value={`错误信息：${detail?.message || '-'}`} />
              <textarea className='min-h-20 w-full rounded-md border bg-muted/30 p-3 text-sm' readOnly value={`请求参数：${detail?.requestParams || '-'}`} />
              <textarea className='min-h-60 w-full rounded-md border bg-muted/30 p-3 font-mono text-xs' readOnly value={detail?.stackTrace || '-'} />
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  )
}
