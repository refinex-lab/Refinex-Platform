import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

type PageToolbarProps = {
  page: number
  size: number
  total: number
  loading?: boolean
  onPageChange: (page: number) => void
  onPageSizeChange: (size: number) => void
}

export function PageToolbar({
  page,
  size,
  total,
  loading,
  onPageChange,
  onPageSizeChange,
}: PageToolbarProps) {
  const safeSize = size > 0 ? size : 10
  const totalPages = Math.max(1, Math.ceil(total / safeSize))
  const safePage = Math.min(Math.max(page, 1), totalPages)

  return (
    <div className='mt-4 flex flex-wrap items-center justify-end gap-3 text-sm text-muted-foreground'>
      <span>共 {total} 条</span>
      <span>
        第 {safePage} / {totalPages} 页
      </span>
      <div className='flex items-center gap-2'>
        <span>每页</span>
        <Select
          value={String(safeSize)}
          onValueChange={(value) => onPageSizeChange(Number(value))}
        >
          <SelectTrigger className='h-8 w-[88px]'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='10'>10</SelectItem>
            <SelectItem value='20'>20</SelectItem>
            <SelectItem value='50'>50</SelectItem>
            <SelectItem value='100'>100</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <Button
        type='button'
        variant='outline'
        size='icon'
        className='h-8 w-8'
        disabled={loading || safePage <= 1}
        onClick={() => onPageChange(safePage - 1)}
      >
        <ChevronLeft className='h-4 w-4' />
      </Button>
      <Button
        type='button'
        variant='outline'
        size='icon'
        className='h-8 w-8'
        disabled={loading || safePage >= totalPages}
        onClick={() => onPageChange(safePage + 1)}
      >
        <ChevronRight className='h-4 w-4' />
      </Button>
    </div>
  )
}
