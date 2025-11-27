# CLAUDE.md - LangIA Frontend

## Projeto

LangIA é uma plataforma de aprendizado de idiomas com IA. O frontend usa **React 18 + TypeScript + Tailwind CSS**.

-----

## Design System

### Cores (usar exatamente estes valores)

```js
// tailwind.config.js → theme.extend.colors
primary: '#6366F1'        // Botões, destaques, links
primaryDark: '#4F46E5'    // Hover states
primaryLight: '#E0E7FF'   // Backgrounds claros, badges
accent: '#818CF8'         // Destaques secundários
bgWarm: '#FDF8F3'         // Fundo principal das páginas
bg: '#EEF2FF'             // Seções alternadas
text: '#1E1B4B'           // Texto principal, navbar/footer dark
textLight: '#6B7280'      // Texto secundário, descrições
```

### Tipografia

```js
// tailwind.config.js → theme.extend.fontFamily
sans: ['Segoe UI', '-apple-system', 'sans-serif']  // Corpo, botões
serif: ['Georgia', 'serif']                         // Títulos h1, h2 (italic)
```

### Espaçamentos e Bordas

```
Seções: py-16 px-12 até py-20 px-16
Cards: p-6 até p-8
Border-radius: rounded-full (botões/inputs), rounded-2xl (cards), rounded-3xl (cards grandes)
Sombras: shadow-sm (cards), shadow-lg (badges flutuantes)
```

-----

## Estrutura de Pastas

```
src/
├── components/
│   ├── common/        → Button, Input, Card, Badge
│   ├── layout/        → Navbar, Footer, Container
│   └── sections/      → Hero, Benefits, Goals, Values, Teachers, Testimonials, FAQ, CTA
├── constants/         → colors.ts, content.ts, teachers.ts, faqs.ts
├── hooks/             → useMediaQuery, useScrollPosition
├── types/             → Teacher, FAQ, Goal, etc.
├── utils/             → cn.ts (classnames helper)
└── pages/             → LandingPage.tsx
```

-----

## Convenções

### Nomenclatura

```
Componentes:  PascalCase     → HeroSection.tsx
Funções:      camelCase      → handleClick, formatDate
Constantes:   UPPER_SNAKE    → API_BASE_URL, MAX_ITEMS
Props:        camelCase      → onClick, isLoading, variant
Estados:      camelCase      → isOpen, selectedId, activeIndex
```

### Ordem das Classes Tailwind

```
1. Layout      → flex, grid, relative
2. Tamanho     → w-full, h-16, p-4, m-2
3. Tipografia  → text-lg, font-bold
4. Cores       → bg-primary, text-white
5. Efeitos     → shadow-lg, rounded-2xl
6. Estados     → hover:bg-primaryDark
7. Responsivo  → md:flex-row, lg:p-8
```

-----

## Regras Obrigatórias

### Fazer

```
✓ Usar as cores exatas da paleta (nunca inventar)
✓ Títulos h1/h2 em font-serif italic
✓ Componentizar cada seção separadamente
✓ Mobile-first: começar pelo mobile, expandir com md: e lg:
✓ Alt em todas as imagens
✓ Aria-label em botões de ícone (←, →, ✕)
✓ Keys únicas em listas (usar id, nunca index)
✓ Dados estáticos em /constants (não hardcoded no componente)
✓ TypeScript com tipos explícitos (nada de any)
```

### Não Fazer

```
✗ Cores fora da paleta
✗ Inline styles (exceto valores dinâmicos calculados)
✗ Componentes com mais de 150 linhas
✗ console.log em código commitado
✗ Ignorar erros de TypeScript
✗ Divs quando existe tag semântica (section, nav, article, footer)
✗ !important no CSS
```

-----

## Componente Padrão

```tsx
// src/components/sections/Hero/Hero.tsx
import { Button } from '@/components/common/Button';
import { HERO_CONTENT } from '@/constants/content';

interface HeroProps {
  onCtaClick?: () => void;
}

export const Hero = ({ onCtaClick }: HeroProps) => {
  return (
    <section className="flex flex-col md:flex-row items-center gap-8 py-16 px-12 bg-gradient-to-br from-bg to-bgWarm">
      {/* conteúdo */}
    </section>
  );
};
```

-----

## Utilitário de Classes

```ts
// src/utils/cn.ts
import { clsx, ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export const cn = (...inputs: ClassValue[]) => twMerge(clsx(inputs));
```

-----

## Responsividade

```
Mobile:  < 768px   → Coluna única, menu hamburguer, cards empilhados
Tablet:  768px+    → md: duas colunas, ajustes de padding
Desktop: 1024px+   → lg: layout completo conforme design
```

-----

## Configuração Tailwind

```js
// tailwind.config.js
module.exports = {
  content: ['./src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: '#6366F1', dark: '#4F46E5', light: '#E0E7FF' },
        accent: '#818CF8',
        bg: { DEFAULT: '#EEF2FF', warm: '#FDF8F3' },
        text: { DEFAULT: '#1E1B4B', light: '#6B7280' },
      },
      fontFamily: {
        sans: ['Segoe UI', '-apple-system', 'sans-serif'],
        serif: ['Georgia', 'serif'],
      },
    },
  },
};
```

-----

## Checklist Final

```
□ Cores da paleta corretas
□ Tipografia serif em títulos
□ Responsivo (testar mobile/tablet/desktop)
□ Acessibilidade (alt, aria-label, semântica)
□ Sem console.log
□ TypeScript sem erros
□ Componentes < 150 linhas
```

-----

## Dependências

```json
{
  "react": "^18.2.0",
  "typescript": "^5.x",
  "tailwindcss": "^3.4.x",
  "clsx": "^2.x",
  "tailwind-merge": "^2.x"
}
```