import { createFileRoute } from '@tanstack/react-router'
import { MenuManagementPage } from '@/features/system/menu-management'

export const Route = createFileRoute('/_authenticated/system-management/menus')({
  component: MenuManagementPage,
})
