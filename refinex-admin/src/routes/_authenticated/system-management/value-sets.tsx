import { createFileRoute } from '@tanstack/react-router'
import { ValueSetsPage } from '@/features/system/value-sets'

export const Route = createFileRoute('/_authenticated/system-management/value-sets')({
  component: ValueSetsPage,
})
