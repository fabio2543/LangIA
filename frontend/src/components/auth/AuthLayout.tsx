import { type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from '../../i18n';

interface AuthLayoutProps {
  children: ReactNode;
  title: string;
  subtitle: string;
}

export const AuthLayout = ({ children, title, subtitle }: AuthLayoutProps) => {
  const { locale, setLocale } = useTranslation();

  return (
    <div className="min-h-screen flex">
      {/* Lado Esquerdo - Formulário */}
      <div className="w-full lg:w-1/2 flex flex-col bg-bg-warm">
        {/* Header com Logo e Seletor de Idioma */}
        <header className="flex items-center justify-between p-6 lg:p-8">
          <Link to="/" className="flex items-center gap-1 text-2xl font-bold text-text">
            Lang<span className="text-accent">IA</span>
          </Link>

          {/* Seletor de Idioma */}
          <div className="flex items-center gap-2 bg-white rounded-full px-3 py-1.5 shadow-card">
            {(['pt', 'en', 'es'] as const).map((lang) => (
              <button
                key={lang}
                onClick={() => setLocale(lang)}
                className={`px-2 py-1 text-sm font-medium rounded-full transition-colors ${
                  locale === lang
                    ? 'bg-primary text-white'
                    : 'text-text-light hover:text-text'
                }`}
              >
                {lang.toUpperCase()}
              </button>
            ))}
          </div>
        </header>

        {/* Conteúdo do Formulário */}
        <main className="flex-1 flex items-center justify-center px-6 py-8 lg:px-16">
          <div className="w-full max-w-md">
            {/* Título e Subtítulo */}
            <div className="text-center mb-8">
              <h1 className="text-3xl lg:text-4xl font-serif italic text-text mb-3">
                {title}
              </h1>
              <p className="text-text-light">{subtitle}</p>
            </div>

            {/* Formulário */}
            {children}
          </div>
        </main>

        {/* Footer */}
        <footer className="p-6 text-center text-sm text-text-light">
          © {new Date().getFullYear()} LangIA. All rights reserved.
        </footer>
      </div>

      {/* Lado Direito - Ilustração (hidden em mobile) */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary to-primary-dark relative overflow-hidden">
        {/* Círculos decorativos */}
        <div className="absolute top-20 left-10 w-32 h-32 bg-white/10 rounded-full" />
        <div className="absolute bottom-40 right-20 w-48 h-48 bg-white/10 rounded-full" />
        <div className="absolute top-1/2 left-1/3 w-24 h-24 bg-white/5 rounded-full" />

        {/* Conteúdo central */}
        <div className="flex flex-col items-center justify-center w-full px-12 text-white z-10">
          <div className="text-8xl mb-8">🌍</div>
          <h2 className="text-3xl font-serif italic text-center mb-4">
            Aprenda idiomas com inteligência
          </h2>
          <p className="text-white/80 text-center max-w-sm">
            Junte-se a milhares de estudantes que estão transformando a forma de aprender idiomas com IA.
          </p>

          {/* Stats */}
          <div className="flex gap-12 mt-12">
            <div className="text-center">
              <div className="text-4xl font-bold">10+</div>
              <div className="text-sm text-white/70">Cursos</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold">500+</div>
              <div className="text-sm text-white/70">Alunos</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold">24/7</div>
              <div className="text-sm text-white/70">Disponível</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
