import { createFileRoute } from '@tanstack/react-router'
import { TeamsPage } from '@/features/system/organization'

export const Route = createFileRoute('/_authenticated/system-management/organizations/teams')({
  component: TeamsPage,
})
