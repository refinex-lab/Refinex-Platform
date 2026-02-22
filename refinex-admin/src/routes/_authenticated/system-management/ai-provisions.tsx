import { createFileRoute } from '@tanstack/react-router'
import { ProvisionsPage } from '@/features/system/ai-management'

export const Route = createFileRoute('/_authenticated/system-management/ai-provisions')({
  component: ProvisionsPage,
})
