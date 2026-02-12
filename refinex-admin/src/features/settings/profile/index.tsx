import { ContentSection } from '../components/content-section'
import { ProfileForm } from './profile-form'

export function SettingsProfile() {
  return (
    <ContentSection
      title='个人资料'
      desc='这将影响其他人在系统中看到的你的信息。'
    >
      <ProfileForm />
    </ContentSection>
  )
}
