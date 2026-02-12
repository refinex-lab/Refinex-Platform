import { ContentSection } from '../components/content-section'
import { AppearanceForm } from './appearance-form'

export function SettingsAppearance() {
  return (
    <ContentSection
      title='外观'
      desc='自定义应用外观，并可在浅色/深色主题间自动切换。'
    >
      <AppearanceForm />
    </ContentSection>
  )
}
