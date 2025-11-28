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
          </Routes>
        </AuthProvider>
      </I18nProvider>
    </BrowserRouter>
  );
}

export default App;
