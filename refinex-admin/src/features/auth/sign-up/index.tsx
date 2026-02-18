import { Link } from '@tanstack/react-router'
import { AuthLayout } from '../auth-layout'
import { SignUpForm } from './components/sign-up-form'

export function SignUp() {
  return (
    <AuthLayout>
      <div className='space-y-2'>
        <h2 className='text-2xl font-semibold tracking-tight'>创建账号</h2>
        <p className='text-sm text-muted-foreground'>
          仅支持手机号验证码、邮箱验证码注册。用户名密码与邮箱密码登录需注册后在后台设置。已有账号？
          <Link to='/sign-in' className='ms-1 underline underline-offset-4 hover:text-primary'>
            去登录
          </Link>
        </p>
      </div>
      <SignUpForm />
      <p className='pt-1 text-center text-sm text-muted-foreground'>
        创建账号即表示你同意我们的{' '}
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
