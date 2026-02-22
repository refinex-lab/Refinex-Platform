import { createFileRoute } from '@tanstack/react-router'
import { ProvidersPage } from '@/features/system/ai-management'

export const Route = createFileRoute('/_authenticated/system-management/ai-providers')({
  component: ProvidersPage,
})
