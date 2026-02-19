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
  getNotifyLog,
  listNotifyLogs,
  type NotifyLog,
  type NotifyLogListQuery,
} from '@/features/system/api'
import { handleServerError } from '@/lib/handle-server-error'
import { formatDateTime, toOptionalString } from './common'

const CHANNEL_LABEL: Record<number, string> = {
  1: '短信',
  2: '邮件',
  3: '站内信',
  4: '推送',
}

const SEND_STATUS_LABEL: Record<number, string> = {
  0: '待发送',
  1: '成功',
  2: '失败',
}

export function NotifyLogsPage() {
  const [logs, setLogs] = useState<NotifyLog[]>([])
  const [loading, setLoading] = useState(false)

  const [channelInput, setChannelInput] = useState<'all' | '1' | '2' | '3' | '4'>('all')
  const [statusInput, setStatusInput] = useState<'all' | '0' | '1' | '2'>('all')
  const [sceneCodeInput, setSceneCodeInput] = useState('')
  const [receiverInput, setReceiverInput] = useState('')
  const [startTimeInput, setStartTimeInput] = useState('')
  const [endTimeInput, setEndTimeInput] = useState('')
  const [query, setQuery] = useState<NotifyLogListQuery>({ limit: 200 })

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detail, setDetail] = useState<NotifyLog | null>(null)

  async function loadLogs(activeQuery: NotifyLogListQuery = query) {
    setLoading(true)
    try {
      const data = await listNotifyLogs(activeQuery)
      setLogs(data)
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
      channelType: channelInput === 'all' ? undefined : Number(channelInput),
      sendStatus: statusInput === 'all' ? undefined : Number(statusInput),
      sceneCode: toOptionalString(sceneCodeInput),
      receiver: toOptionalString(receiverInput),
      startTime: startTimeInput || undefined,
      endTime: endTimeInput || undefined,
      limit: 200,
    })
  }

  function resetFilter() {
    setChannelInput('all')
    setStatusInput('all')
    setSceneCodeInput('')
    setReceiverInput('')
    setStartTimeInput('')
    setEndTimeInput('')
    setQuery({ limit: 200 })
  }

  async function openDetail(logId?: number) {
    if (!logId) return
    setDetailOpen(true)
    setDetailLoading(true)
    try {
      const data = await getNotifyLog(logId)
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
        <CardContent className='grid gap-3 xl:grid-cols-[130px_130px_180px_1fr_1fr_1fr_auto]'>
          <Select value={channelInput} onValueChange={(value) => setChannelInput(value as 'all' | '1' | '2' | '3' | '4')}>
            <SelectTrigger>
              <SelectValue placeholder='通知渠道' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>全部渠道</SelectItem>
              <SelectItem value='1'>短信</SelectItem>
              <SelectItem value='2'>邮件</SelectItem>
              <SelectItem value='3'>站内信</SelectItem>
              <SelectItem value='4'>推送</SelectItem>
            </SelectContent>
          </Select>
          <Select value={statusInput} onValueChange={(value) => setStatusInput(value as 'all' | '0' | '1' | '2')}>
            <SelectTrigger>
              <SelectValue placeholder='发送状态' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>全部状态</SelectItem>
              <SelectItem value='0'>待发送</SelectItem>
              <SelectItem value='1'>成功</SelectItem>
              <SelectItem value='2'>失败</SelectItem>
            </SelectContent>
          </Select>
          <Input placeholder='场景编码' value={sceneCodeInput} onChange={(event) => setSceneCodeInput(event.target.value)} />
          <Input placeholder='接收方' value={receiverInput} onChange={(event) => setReceiverInput(event.target.value)} />
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
                <TableHead>渠道</TableHead>
                <TableHead>场景编码</TableHead>
                <TableHead>接收方</TableHead>
                <TableHead>主题</TableHead>
                <TableHead className='w-[100px] text-center'>发送状态</TableHead>
                <TableHead>模板编码</TableHead>
                <TableHead>发送时间</TableHead>
                <TableHead className='w-[80px] text-center'>详情</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={8}>
                    <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                      <Loader2 className='h-4 w-4 animate-spin' />
                      正在加载通知日志...
                    </div>
                  </TableCell>
                </TableRow>
              ) : logs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className='py-8 text-center text-muted-foreground'>
                    暂无通知日志
                  </TableCell>
                </TableRow>
              ) : (
                logs.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.channelType == null ? '-' : (CHANNEL_LABEL[item.channelType] ?? item.channelType)}</TableCell>
                    <TableCell>{item.sceneCode || '-'}</TableCell>
                    <TableCell>{item.receiver || '-'}</TableCell>
                    <TableCell className='max-w-[220px] truncate'>{item.subject || '-'}</TableCell>
                    <TableCell className='text-center'>
                      <Badge
                        variant={
                          item.sendStatus === 1
                            ? 'default'
                            : item.sendStatus === 2
                              ? 'destructive'
                              : 'secondary'
                        }
                      >
                        {item.sendStatus == null ? '-' : (SEND_STATUS_LABEL[item.sendStatus] ?? item.sendStatus)}
                      </Badge>
                    </TableCell>
                    <TableCell>{item.templateCode || '-'}</TableCell>
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
        </CardContent>
      </Card>

      <Dialog open={detailOpen} onOpenChange={setDetailOpen}>
        <DialogContent className='sm:max-w-4xl'>
          <DialogHeader>
            <DialogTitle>通知日志详情</DialogTitle>
          </DialogHeader>
          {detailLoading ? (
            <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              正在加载详情...
            </div>
          ) : (
            <div className='grid gap-3 md:grid-cols-2'>
              <Input disabled value={`日志ID：${detail?.id ?? '-'}`} />
              <Input disabled value={`渠道：${detail?.channelType == null ? '-' : (CHANNEL_LABEL[detail.channelType] ?? detail.channelType)}`} />
              <Input disabled value={`发送状态：${detail?.sendStatus == null ? '-' : (SEND_STATUS_LABEL[detail.sendStatus] ?? detail.sendStatus)}`} />
              <Input disabled value={`场景编码：${detail?.sceneCode || '-'}`} />
              <Input disabled value={`接收方：${detail?.receiver || '-'}`} />
              <Input disabled value={`主题：${detail?.subject || '-'}`} />
              <Input disabled value={`服务商：${detail?.provider || '-'}`} />
              <Input disabled value={`模板编码：${detail?.templateCode || '-'}`} />
              <Input disabled value={`业务ID：${detail?.bizId || '-'}`} />
              <Input disabled value={`用户ID：${detail?.userId ?? '-'}`} />
              <Input disabled value={`企业ID：${detail?.estabId ?? '-'}`} />
              <Input disabled value={`发送时间：${formatDateTime(detail?.gmtCreate)}`} />
              <div className='md:col-span-2'>
                <Input disabled value={`摘要：${detail?.contentDigest || '-'}`} />
              </div>
              <div className='md:col-span-2'>
                <Input disabled value={`错误信息：${detail?.errorMessage || '-'}`} />
              </div>
              <div className='md:col-span-2'>
                <Input disabled value={`RequestId：${detail?.requestId || '-'} / IP：${detail?.ip || '-'}`} />
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
