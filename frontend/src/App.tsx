import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { I18nProvider } from './context/I18nProvider';
import { AuthProvider } from './context/AuthContext';
import { TrailProvider } from './context/TrailContext';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/ResetPasswordPage';
import { VerifyEmailPage } from './pages/VerifyEmailPage';
import { EmailConfirmationPage } from './pages/EmailConfirmationPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { OnboardingPage } from './pages/OnboardingPage';
import { TrailsPage } from './pages/TrailsPage';
import { TrailDetailPage } from './pages/TrailDetailPage';
import { ModulePage } from './pages/ModulePage';
import { LessonPage } from './pages/LessonPage';
import { PersonalDataTab } from './components/profile/PersonalDataTab';
import { LearningPreferencesTab } from './components/profile/LearningPreferencesTab';
import { SkillAssessmentTab } from './components/profile/SkillAssessmentTab';
import { NotificationSettingsTab } from './components/profile/NotificationSettingsTab';
import {
  WelcomeStep,
  LanguageStep,
  PreferencesStep,
  AssessmentStep,
  CompletionStep,
} from './components/onboarding';

function App() {
  return (
    <BrowserRouter>
      <I18nProvider>
        <AuthProvider>
          <TrailProvider>
            <Routes>
              <Route path="/" element={<LandingPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignupPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />
              <Route path="/reset-password" element={<ResetPasswordPage />} />
              <Route path="/verify-email" element={<VerifyEmailPage />} />
              <Route path="/email-confirmed" element={<EmailConfirmationPage />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              {/* Onboarding Routes */}
              <Route path="/onboarding" element={<OnboardingPage />}>
                <Route index element={<Navigate to="welcome" replace />} />
                <Route path="welcome" element={<WelcomeStep />} />
                <Route path="language" element={<LanguageStep />} />
                <Route path="preferences" element={<PreferencesStep />} />
                <Route path="assessment" element={<AssessmentStep />} />
                <Route path="complete" element={<CompletionStep />} />
              </Route>
              <Route path="/profile" element={<ProfilePage />}>
                <Route index element={<PersonalDataTab />} />
                <Route path="learning" element={<LearningPreferencesTab />} />
                <Route path="assessment" element={<SkillAssessmentTab />} />
                <Route path="notifications" element={<NotificationSettingsTab />} />
              </Route>
              {/* Trail Routes */}
              <Route path="/trails" element={<TrailsPage />} />
              <Route path="/trails/:id" element={<TrailDetailPage />} />
              <Route path="/trails/:id/modules/:moduleId" element={<ModulePage />} />
              <Route path="/lessons/:lessonId" element={<LessonPage />} />
            </Routes>
          </TrailProvider>
        </AuthProvider>
      </I18nProvider>
    </BrowserRouter>
  );
}

export default App;
