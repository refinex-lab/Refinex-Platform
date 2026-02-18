import { ContentSection } from '../components/content-section'
import { AccountForm } from './account-form'

export function SettingsAccount() {
  return (
    <ContentSection
      title='账号'
      desc='管理账号安全信息（登录身份、验证状态、密码变更）。'
    >
      <AccountForm />
    </ContentSection>
  )
}
