import { Link, useSearch } from '@tanstack/react-router'
import { AuthLayout } from '../auth-layout'
import { UserAuthForm } from './components/user-auth-form'

export function SignIn() {
  const { redirect } = useSearch({ from: '/(auth)/sign-in' })

  return (
    <AuthLayout>
      <div className='space-y-2'>
        <h2 className='text-2xl font-semibold tracking-tight'>登录</h2>
        <p className='text-sm text-muted-foreground'>
          支持用户名密码、手机号验证码、邮箱密码、邮箱验证码
        </p>
      </div>
      <UserAuthForm redirectTo={redirect} />
      <p className='pt-1 text-center text-sm text-muted-foreground'>
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
    </AuthLayout>
  )
}
