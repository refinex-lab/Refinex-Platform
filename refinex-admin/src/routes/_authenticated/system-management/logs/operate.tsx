import { createFileRoute } from '@tanstack/react-router'
import { LogLayout } from '@/features/system/logs/log-layout'
import { OperateLogsPage } from '@/features/system/logs/operate-logs'

export const Route = createFileRoute('/_authenticated/system-management/logs/operate')({
  component: RouteComponent,
})

function RouteComponent() {
  return (
    <LogLayout>
      <OperateLogsPage />
    </LogLayout>
  )
}
