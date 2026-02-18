import { createFileRoute } from '@tanstack/react-router'
import { PrivacyPolicyPage } from '@/features/legal/privacy'

export const Route = createFileRoute('/(auth)/privacy')({
  component: PrivacyPolicyPage,
})
