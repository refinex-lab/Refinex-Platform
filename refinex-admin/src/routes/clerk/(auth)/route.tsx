import { createFileRoute, Link, Outlet } from '@tanstack/react-router'
import { ClerkFullLogo } from '@/assets/clerk-full-logo'
import { Logo } from '@/assets/logo'
import { LearnMore } from '@/components/learn-more'

export const Route = createFileRoute('/clerk/(auth)')({
  component: ClerkAuthLayout,
})

// eslint-disable-next-line react-refresh/only-export-components
function ClerkAuthLayout() {
  return (
    <div className='relative container grid h-svh flex-col items-center justify-center lg:max-w-none lg:grid-cols-2 lg:px-0'>
      <div className='relative hidden h-full flex-col bg-muted p-10 text-white lg:flex dark:border-e'>
        <div className='absolute inset-0 bg-slate-500' />
        <Link
          to='/'
          className='relative z-20 flex items-center text-lg font-medium'
        >
          <Logo className='me-2' />
          Refinex 管理后台
        </Link>

        <ClerkFullLogo className='relative m-auto size-96' />

        <div className='relative z-20 mt-auto'>
          <blockquote className='space-y-2'>
            <p className='text-lg'>
              &ldquo; 简洁高效的后台系统，让团队协作与数据管理更轻松。&rdquo;
            </p>
            <footer className='text-sm'>Refinex 团队</footer>
          </blockquote>
        </div>
      </div>
      <div className='lg:p-8'>
        <div className='relative mx-auto flex w-full flex-col items-center justify-center gap-4'>
          <LearnMore
            defaultOpen
            triggerProps={{
              className: 'absolute -top-12 end-0 sm:end-20 size-6',
            }}
            contentProps={{ side: 'top', align: 'end', className: 'w-auto' }}
          >
            欢迎来到 Clerk 认证示例页面。<br />
            返回{' '}
            <Link
              to='/'
              className='underline decoration-dashed underline-offset-2'
            >
              仪表盘
            </Link>{' '}
            吗？
          </LearnMore>
          <Outlet />
        </div>
      </div>
    </div>
  )
}
