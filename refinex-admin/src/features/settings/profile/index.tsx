import { ContentSection } from '../components/content-section'
import { ProfileForm } from './profile-form'

export function SettingsProfile() {
  return (
    <ContentSection
      title='个人资料'
      desc='维护对外展示信息（显示名称、昵称、头像、性别、生日）。'
    >
      <ProfileForm />
    </ContentSection>
  )
}
