import { createFileRoute } from '@tanstack/react-router'
import { ModelsPage } from '@/features/system/ai-management'

export const Route = createFileRoute('/_authenticated/system-management/ai-models')({
  component: ModelsPage,
})
