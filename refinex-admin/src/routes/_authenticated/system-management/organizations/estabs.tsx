import { createFileRoute } from '@tanstack/react-router'
import { EstabsPage } from '@/features/system/organization'

export const Route = createFileRoute('/_authenticated/system-management/organizations/estabs')({
  component: EstabsPage,
})
