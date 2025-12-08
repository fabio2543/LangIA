import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { I18nProvider } from './context/I18nProvider';
import { AuthProvider } from './context/AuthContext';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/ResetPasswordPage';
import { VerifyEmailPage } from './pages/VerifyEmailPage';
import { EmailConfirmationPage } from './pages/EmailConfirmationPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { LessonsPage } from './pages/LessonsPage';
import { OnboardingPage } from './pages/OnboardingPage';
import { TutorsPage } from './pages/TutorsPage';
import { LessonViewPage } from './pages/LessonViewPage';
import { PersonalDataTab } from './components/profile/PersonalDataTab';
import { LearningPreferencesTab } from './components/profile/LearningPreferencesTab';
import { SkillAssessmentTab } from './components/profile/SkillAssessmentTab';
import { NotificationSettingsTab } from './components/profile/NotificationSettingsTab';

function App() {
  return (
    <BrowserRouter>
      <I18nProvider>
        <AuthProvider>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/verify-email" element={<VerifyEmailPage />} />
            <Route path="/email-confirmed" element={<EmailConfirmationPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/lessons" element={<LessonsPage />} />
            <Route path="/lesson/:id" element={<LessonViewPage />} />
            <Route path="/onboarding" element={<OnboardingPage />} />
            <Route path="/tutors" element={<TutorsPage />} />
            <Route path="/profile" element={<ProfilePage />}>
              <Route index element={<PersonalDataTab />} />
              <Route path="learning" element={<LearningPreferencesTab />} />
              <Route path="assessment" element={<SkillAssessmentTab />} />
              <Route path="notifications" element={<NotificationSettingsTab />} />
            </Route>
          </Routes>
        </AuthProvider>
      </I18nProvider>
    </BrowserRouter>
  );
}

export default App;
