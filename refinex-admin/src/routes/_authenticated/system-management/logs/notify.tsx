import { createFileRoute } from '@tanstack/react-router'
import { LogLayout } from '@/features/system/logs/log-layout'
import { NotifyLogsPage } from '@/features/system/logs/notify-logs'

export const Route = createFileRoute('/_authenticated/system-management/logs/notify')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout>
      <NotifyLogsPage />
    </LogLayout>
  )
}
