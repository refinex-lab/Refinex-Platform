import { createFileRoute } from '@tanstack/react-router'
import { ErrorLogsPage } from '@/features/system/logs/error-logs'
import { LogLayout } from '@/features/system/logs/log-layout'

export const Route = createFileRoute('/_authenticated/system-management/logs/error')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout>
      <ErrorLogsPage />
    </LogLayout>
  )
}
