import { createFileRoute } from '@tanstack/react-router'
import { TermsOfServicePage } from '@/features/legal/terms'

export const Route = createFileRoute('/(auth)/terms')({
  component: TermsOfServicePage,
})
