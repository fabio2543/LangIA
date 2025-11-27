import { I18nProvider } from './context/I18nProvider';
import { LandingPage } from './pages/LandingPage';

function App() {
  return (
    <I18nProvider>
      <LandingPage />
    </I18nProvider>
  );
}

export default App;
