import { createFileRoute } from '@tanstack/react-router'
import { LogLayout } from '@/features/system/logs/log-layout'
import { NotifyLogsPage } from '@/features/system/logs/notify-logs'

export const Route = createFileRoute('/_authenticated/system-management/logs/notify')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout title='通知日志' desc='追踪短信、邮件等通知发送状态，定位通知链路异常。'>
      <NotifyLogsPage />
    </LogLayout>
  )
}
