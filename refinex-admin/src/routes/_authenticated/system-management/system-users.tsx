import { createFileRoute } from '@tanstack/react-router'
import { SystemUsersPage } from '@/features/system/user-management'

export const Route = createFileRoute('/_authenticated/system-management/system-users')({
  component: SystemUsersPage,
})
