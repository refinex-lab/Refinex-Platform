import { Link } from '@tanstack/react-router'
import { AuthLayout } from '../auth-layout'
import { ForgotPasswordForm } from './components/forgot-password-form'

export function ForgotPassword() {
  return (
    <AuthLayout>
      <div className='space-y-2'>
        <h2 className='text-2xl font-semibold tracking-tight'>重置密码</h2>
        <p className='text-sm text-muted-foreground'>
          支持手机号短信验证码、邮箱验证码重置密码，完成验证后可直接设置新密码。
        </p>
      </div>
      <ForgotPasswordForm />
      <p className='text-center text-sm text-muted-foreground'>
        还没有账号？
        <Link to='/sign-up' className='ms-1 underline underline-offset-4 hover:text-primary'>
          去注册
        </Link>
      </p>
    </AuthLayout>
  )
}
