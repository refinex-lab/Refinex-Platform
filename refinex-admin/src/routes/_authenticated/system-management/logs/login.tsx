import { createFileRoute } from '@tanstack/react-router'
import { LogLayout } from '@/features/system/logs/log-layout'
import { LoginLogsPage } from '@/features/system/logs/login-logs'

export const Route = createFileRoute('/_authenticated/system-management/logs/login')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout title='登录日志' desc='审计用户登录行为，支持按登录方式、来源与时间窗口查询。'>
      <LoginLogsPage />
    </LogLayout>
  )
}
