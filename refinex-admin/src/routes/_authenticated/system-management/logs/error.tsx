import { createFileRoute } from '@tanstack/react-router'
import { ErrorLogsPage } from '@/features/system/logs/error-logs'
import { LogLayout } from '@/features/system/logs/log-layout'

export const Route = createFileRoute('/_authenticated/system-management/logs/error')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout title='错误日志' desc='集中查看运行异常，支持按服务、错误级别与路径定位问题。'>
      <ErrorLogsPage />
    </LogLayout>
  )
}
