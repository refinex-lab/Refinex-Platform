import { createFileRoute } from '@tanstack/react-router'
import { RoleManagementPage } from '@/features/system/role-management'

export const Route = createFileRoute('/_authenticated/system-management/roles')({
  component: RoleManagementPage,
})
