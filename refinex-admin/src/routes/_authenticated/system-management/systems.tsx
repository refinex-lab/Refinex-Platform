import { createFileRoute } from '@tanstack/react-router'
import { SystemDefinitionsPage } from '@/features/system/system-definitions'

export const Route = createFileRoute('/_authenticated/system-management/systems')({
  component: SystemDefinitionsPage,
})
