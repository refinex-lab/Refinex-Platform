import { createFileRoute } from '@tanstack/react-router'
import { DataResourceManagementPage } from '@/features/system/data-resource-management'

export const Route = createFileRoute('/_authenticated/system-management/data-resources')({
  component: DataResourceManagementPage,
})
