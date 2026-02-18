import { Link } from '@tanstack/react-router'
import { AuthLayout } from '../auth-layout'

export function Otp() {
  return (
    <AuthLayout>
      <div className='space-y-2'>
        <h2 className='text-2xl font-semibold tracking-tight'>高级认证</h2>
        <p className='text-sm text-muted-foreground'>
          TOTP / OAuth / OIDC / SAML / 微信扫码能力正在建设中，目前请使用用户名、手机号或邮箱方式登录。
        </p>
      </div>
      <div className='pt-1 text-sm text-muted-foreground'>
        <Link to='/sign-in' className='underline underline-offset-4 hover:text-primary'>
          返回登录
        </Link>
      </div>
    </AuthLayout>
  )
}
