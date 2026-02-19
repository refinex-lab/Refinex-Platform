import { createFileRoute } from '@tanstack/react-router'
import { LogLayout } from '@/features/system/logs/log-layout'
import { LoginLogsPage } from '@/features/system/logs/login-logs'

export const Route = createFileRoute('/_authenticated/system-management/logs/login')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout>
      <LoginLogsPage />
    </LogLayout>
  )
}
