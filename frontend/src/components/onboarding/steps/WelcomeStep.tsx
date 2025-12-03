import { useState, useEffect } from 'react';
import { useOnboarding } from '../../../context/OnboardingContext';
import { profileService } from '../../../services/profileService';
import { Button } from '../../common/Button';
import type { UserProfileDetails, UpdatePersonalDataRequest } from '../../../types';

export const WelcomeStep = () => {
  const { nextStep, isLoading: contextLoading } = useOnboarding();
  const [profile, setProfile] = useState<UserProfileDetails | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    fullName: '',
    nativeLanguage: 'pt-BR',
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
  });

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const data = await profileService.getProfileDetails();
        setProfile(data);
        setFormData({
          fullName: data.fullName || '',
          nativeLanguage: data.nativeLanguage || 'pt-BR',
          timezone: data.timezone || Intl.DateTimeFormat().resolvedOptions().timeZone,
        });
      } catch (err) {
        console.error('Erro ao carregar perfil:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadProfile();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.fullName.trim()) {
      setError('Por favor, informe seu nome');
      return;
    }

    try {
      setIsSaving(true);
      const request: UpdatePersonalDataRequest = {
        fullName: formData.fullName,
        nativeLanguage: formData.nativeLanguage,
        timezone: formData.timezone,
      };
      await profileService.updateProfileDetails(request);
      nextStep();
    } catch (err) {
      setError('Erro ao salvar dados. Tente novamente.');
      console.error('Erro ao salvar:', err);
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="text-4xl mb-4 animate-bounce">...</div>
          <p className="text-text-light">Carregando...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto">
      <div className="text-center mb-8">
        <div className="text-6xl mb-4">👋</div>
        <h1 className="text-3xl font-serif italic text-text mb-2">
          Bem-vindo ao LangIA!
        </h1>
        <p className="text-text-light">
          Vamos configurar seu perfil de aprendizado em alguns passos simples.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-card p-6 space-y-6">
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm">
            {error}
          </div>
        )}

        <div>
          <label htmlFor="fullName" className="block text-sm font-medium text-text mb-2">
            Como podemos te chamar?
          </label>
          <input
            type="text"
            id="fullName"
            value={formData.fullName}
            onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
            className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all"
            placeholder="Seu nome completo"
          />
        </div>

        <div>
          <label htmlFor="nativeLanguage" className="block text-sm font-medium text-text mb-2">
            Qual e seu idioma nativo?
          </label>
          <select
            id="nativeLanguage"
            value={formData.nativeLanguage}
            onChange={(e) => setFormData({ ...formData, nativeLanguage: e.target.value })}
            className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all"
          >
            <option value="pt-BR">Portugues (Brasil)</option>
            <option value="pt-PT">Portugues (Portugal)</option>
            <option value="en-US">English (US)</option>
            <option value="es-ES">Espanol</option>
          </select>
        </div>

        <div>
          <label htmlFor="timezone" className="block text-sm font-medium text-text mb-2">
            Fuso horario
          </label>
          <select
            id="timezone"
            value={formData.timezone}
            onChange={(e) => setFormData({ ...formData, timezone: e.target.value })}
            className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all"
          >
            <option value="America/Sao_Paulo">Brasilia (GMT-3)</option>
            <option value="America/Manaus">Manaus (GMT-4)</option>
            <option value="America/Noronha">Fernando de Noronha (GMT-2)</option>
            <option value="America/Rio_Branco">Acre (GMT-5)</option>
            <option value="America/New_York">New York (EST)</option>
            <option value="Europe/London">London (GMT)</option>
            <option value="Europe/Madrid">Madrid (CET)</option>
          </select>
        </div>

        <div className="pt-4">
          <Button
            type="submit"
            variant="primary"
            size="lg"
            className="w-full"
            disabled={isSaving || contextLoading}
          >
            {isSaving ? 'Salvando...' : 'Continuar'}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default WelcomeStep;
