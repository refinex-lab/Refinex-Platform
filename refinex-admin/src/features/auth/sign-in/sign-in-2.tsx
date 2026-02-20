import { Link, useSearch } from '@tanstack/react-router'
import { Logo } from '@/assets/logo'
import { appConfig } from '@/config/app-config'
import { cn } from '@/lib/utils'
import dashboardDark from './assets/dashboard-dark.png'
import dashboardLight from './assets/dashboard-light.png'
import { UserAuthForm } from './components/user-auth-form'

export function SignIn2() {
  const { redirect } = useSearch({ from: '/(auth)/sign-in-2' })

  return (
    <div className='relative container grid h-svh flex-col items-center justify-center lg:max-w-none lg:grid-cols-2 lg:px-0'>
      <div className='lg:p-8'>
        <div className='mx-auto flex w-full flex-col justify-center space-y-2 py-8 sm:w-[480px] sm:p-8'>
          <div className='mb-4 flex items-center justify-center'>
            <Logo className='me-2' />
            <h1 className='text-xl font-medium'>{appConfig.app.name}</h1>
          </div>
        </div>
        <div className='mx-auto flex w-full max-w-sm flex-col justify-center space-y-2'>
          <div className='flex flex-col space-y-2 text-start'>
            <h2 className='text-lg font-semibold tracking-tight'>登录</h2>
            <p className='text-sm text-muted-foreground'>
              支持手机号验证码、邮箱密码、邮箱验证码<br />
              请选择一种方式登录
            </p>
          </div>
          <UserAuthForm redirectTo={redirect} />
          <p className='px-8 text-center text-sm text-muted-foreground'>
            点击“登录”即表示你同意我们的{' '}
            <Link to='/terms' className='underline underline-offset-4 hover:text-primary'>
              服务条款
            </Link>{' '}
            和{' '}
            <Link to='/privacy' className='underline underline-offset-4 hover:text-primary'>
              隐私政策
            </Link>
            。
          </p>
        </div>
      </div>

      <div
        className={cn(
          'relative h-full overflow-hidden bg-muted max-lg:hidden',
          '[&>img]:absolute [&>img]:top-[15%] [&>img]:left-20 [&>img]:h-full [&>img]:w-full [&>img]:object-cover [&>img]:object-top-left [&>img]:select-none'
        )}
      >
        <img
          src={dashboardLight}
          className='dark:hidden'
          width={1024}
          height={1151}
          alt={appConfig.app.name}
        />
        <img
          src={dashboardDark}
          className='hidden dark:block'
          width={1024}
          height={1138}
          alt={appConfig.app.name}
        />
      </div>
    </div>
  )
}
