import { ContentSection } from '../components/content-section'
import { AccountForm } from './account-form'

export function SettingsAccount() {
  return (
    <ContentSection
      title='账号'
      desc='更新你的账号设置，并设定偏好语言和时区。'
    >
      <AccountForm />
    </ContentSection>
  )
}
