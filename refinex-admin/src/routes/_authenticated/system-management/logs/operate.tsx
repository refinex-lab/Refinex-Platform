import { createFileRoute } from '@tanstack/react-router'
import { LogLayout } from '@/features/system/logs/log-layout'
import { OperateLogsPage } from '@/features/system/logs/operate-logs'

export const Route = createFileRoute('/_authenticated/system-management/logs/operate')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout title='操作日志' desc='审计关键业务操作记录，支持按模块、请求路径与执行结果筛选。'>
      <OperateLogsPage />
    </LogLayout>
  )
}
