import { Logo } from '@/assets/logo'
import { appConfig } from '@/config/app-config'
import { cn } from '@/lib/utils'
import dashboardDark from '@/features/auth/sign-in/assets/dashboard-dark.png'
import dashboardLight from '@/features/auth/sign-in/assets/dashboard-light.png'

type AuthLayoutProps = {
  children: React.ReactNode
}

export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className='relative grid min-h-svh lg:grid-cols-2'>
      <div className='flex items-center justify-center px-6 py-10 lg:px-10'>
        <div className='w-full max-w-[620px] space-y-6'>
          <div className='flex items-center justify-center lg:justify-start'>
            <Logo className='me-2 size-7' />
            <h1 className='text-2xl font-semibold tracking-tight'>{appConfig.app.name}</h1>
          </div>
          <div className='rounded-2xl border bg-card p-7 shadow-sm sm:p-8'>
            <div className='space-y-5'>{children}</div>
          </div>
        </div>
      </div>

      <div
        className={cn(
          'relative h-full overflow-hidden bg-muted max-lg:hidden',
          '[&>img]:absolute [&>img]:top-[12%] [&>img]:left-16 [&>img]:h-[92%] [&>img]:w-[92%] [&>img]:object-contain [&>img]:select-none'
        )}
      >
        <img src={dashboardLight} className='dark:hidden' width={1024} height={1151} alt={appConfig.app.name} />
        <img src={dashboardDark} className='hidden dark:block' width={1024} height={1138} alt={appConfig.app.name} />
      </div>
    </div>
  )
}
