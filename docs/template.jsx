/**

- ============================================
- LANGIA - LANDING PAGE (VERSÃO AZUL HÍBRIDA)
- ============================================
- 
- INSTRUÇÕES PARA CLAUDE CODE:
- 
- 1. Este é o layout aprovado da landing page do LangIA
- 1. Mantenha EXATAMENTE as cores, espaçamentos e estrutura
- 1. Tecnologias sugeridas: React + Tailwind CSS (ou CSS Modules)
- 1. O código está em inline styles para facilitar visualização,
- mas deve ser convertido para Tailwind/CSS no projeto real
- 
- PALETA DE CORES (manter exata):
- - Primary:      #6366F1 (índigo principal)
- - Primary Dark: #4F46E5 (hover states)
- - Primary Light:#E0E7FF (backgrounds claros)
- - Accent:       #818CF8 (destaques secundários)
- - Background:   #EEF2FF (seções alternadas)
- - Bg Warm:      #FDF8F3 (fundo principal quente)
- - Text:         #1E1B4B (texto principal/navbar)
- - Text Light:   #6B7280 (texto secundário)
- 
- TIPOGRAFIA:
- - Títulos: Georgia, serif (italic para destaques)
- - Corpo: Segoe UI, -apple-system, sans-serif
- 
- ESTRUTURA DE SEÇÕES:
- 1. Navbar (dark)
- 1. Hero Section
- 1. Benefits Bar
- 1. Goals + Track Cards
- 1. Values Section
- 1. Teachers Section
- 1. Testimonial Section
- 1. FAQ Section
- 1. CTA Section
- 1. Footer (dark)
- 
- ============================================
  */

import React, { useState } from ‘react’;

// ============================================
// CONSTANTES E DADOS
// ============================================

/**

- Paleta de cores do projeto
- Usar como referência para variáveis CSS/Tailwind
  */
  const COLORS = {
  primary: ‘#6366F1’,
  primaryDark: ‘#4F46E5’,
  primaryLight: ‘#E0E7FF’,
  accent: ‘#818CF8’,
  bg: ‘#EEF2FF’,
  bgWarm: ‘#FDF8F3’,
  text: ‘#1E1B4B’,
  textLight: ‘#6B7280’,
  };

/**

- Links do menu de navegação
  */
  const NAV_LINKS = [‘Method’, ‘Tutors’, ‘Courses’, ‘Pricing’, ‘Business’];

/**

- Benefícios exibidos na barra abaixo do hero
  */
  const BENEFITS = [
  { icon: ‘📊’, text: ‘All levels welcome’ },
  { icon: ‘🤖’, text: ‘AI-powered feedback’ },
  { icon: ‘🌍’, text: ‘Anytime, anywhere, 24/7’ },
  ];

/**

- Objetivos de aprendizado (seção Goals)
  */
  const LEARNING_GOALS = [
  { id: ‘career’, emoji: ‘💼’, label: ‘Grow your career’, active: true },
  { id: ‘university’, emoji: ‘🎓’, label: ‘Thrive at university’, active: false },
  { id: ‘test’, emoji: ‘📝’, label: ‘Prepare for a test’, active: false },
  { id: ‘travel’, emoji: ‘🌍’, label: ‘Travel abroad’, active: true },
  { id: ‘fun’, emoji: ‘🎉’, label: ‘Just for fun’, active: false },
  ];

/**

- Trilhas de progresso
  */
  const PROGRESS_TRACKS = [
  { icon: ‘🎓’, name: ‘Grammar’, progress: 75 },
  { icon: ‘💬’, name: ‘Coherence’, progress: 60 },
  { icon: ‘📚’, name: ‘Vocabulary’, progress: 85 },
  ];

/**

- Dados dos professores
  */
  const TEACHERS = [
  {
  name: ‘Christian Howard’,
  flag: ‘🇮🇹’,
  role: ‘Italian teacher’,
  accent: ‘Native Italian’,
  reviews: ‘100%’,
  tags: [‘Business’, ‘Culture’],
  superTutor: true,
  bgColor: ‘#DBEAFE’
  },
  {
  name: ‘Sandra Wilson’,
  flag: ‘🇪🇸’,
  role: ‘Spanish teacher’,
  accent: ‘Latin American’,
  reviews: ‘99%’,
  tags: [‘Travel’, ‘Conversation’],
  superTutor: true,
  bgColor: ‘#FCE7F3’
  },
  {
  name: ‘Jimmy Cooper’,
  flag: ‘🇬🇧’,
  role: ‘English teacher’,
  accent: ‘British accent’,
  reviews: ‘98%’,
  tags: [‘IELTS’, ‘Academic’],
  superTutor: false,
  bgColor: ‘#E0E7FF’
  },
  ];

/**

- Valores/diferenciais da plataforma
  */
  const VALUES = [
  {
  icon: ‘▶️’,
  title: ‘Speaking clubs’,
  desc: ‘Practice your speaking skills and get positive emotions!’
  },
  {
  icon: ‘💎’,
  title: ‘Quality control’,
  desc: ‘Teachers monitored thoroughly for best experience.’
  },
  {
  icon: ‘📊’,
  title: ‘Progress analysis’,
  desc: ‘CEF Reference to track your success in studying.’
  },
  ];

/**

- Perguntas frequentes (FAQ)
  */
  const FAQS = [
  {
  q: ‘How can I know my level of knowledge?’,
  a: ‘We offer a free assessment test that takes about 15 minutes.’
  },
  {
  q: ‘Do I need to buy materials for lessons?’,
  a: ‘No, all materials are provided digitally through our platform.’
  },
  {
  q: ‘Can I do it individually or only in a group?’,
  a: ‘You can choose between individual and group lessons.’
  },
  {
  q: ‘Are you adjusting to the student's schedule?’,
  a: ‘Yes, we offer flexible scheduling options.’
  },
  {
  q: ‘What is the maximum group size?’,
  a: ‘Our groups have a maximum of 6 students.’
  },
  {
  q: ‘How the first lesson with teacher will be?’,
  a: ‘By the end of the trial lesson, you will determine if this is right for you.’
  },
  ];

/**

- Links do footer organizados por categoria
  */
  const FOOTER_LINKS = {
  company: [‘Careers’, ‘Teachers’, ‘Support’, ‘Contact’],
  product: [‘Courses’, ‘Pricing’, ‘Blog’],
  legal: [‘Terms & Conditions’, ‘Privacy policy’],
  };

// ============================================
// COMPONENTE PRINCIPAL
// ============================================

export default function LangIALandingPage() {
// Estados do componente
const [selectedGoal, setSelectedGoal] = useState(‘career’);
const [openFaq, setOpenFaq] = useState(5);

return (
<div style={{
fontFamily: “‘Segoe UI’, -apple-system, sans-serif”,
background: COLORS.bgWarm,
minHeight: ‘100vh’
}}>

```
  {/* ==========================================
      SEÇÃO 1: NAVBAR (Dark Theme)
      - Fundo escuro (#1E1B4B)
      - Logo com accent
      - Botões: outline (Log in) e filled (Sign up)
      ========================================== */}
  <nav style={{
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '16px 60px',
    background: COLORS.text,
  }}>
    {/* Logo */}
    <div style={{ fontSize: '26px', fontWeight: '700', color: '#fff' }}>
      Lang<span style={{ color: COLORS.accent }}>IA</span>
    </div>
    
    {/* Menu Links */}
    <div style={{ display: 'flex', gap: '28px', alignItems: 'center' }}>
      {NAV_LINKS.map(item => (
        <a 
          key={item} 
          href="#" 
          style={{ 
            color: '#E5E7EB', 
            textDecoration: 'none', 
            fontSize: '14px', 
            fontWeight: '500' 
          }}
        >
          {item}
        </a>
      ))}
    </div>
    
    {/* Botões de ação */}
    <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
      <button style={{
        background: 'transparent',
        border: '1px solid #fff',
        color: '#fff',
        padding: '10px 20px',
        borderRadius: '25px',
        fontWeight: '600',
        cursor: 'pointer',
        fontSize: '14px',
      }}>
        Log in
      </button>
      <button style={{
        background: COLORS.primary,
        border: 'none',
        color: '#fff',
        padding: '10px 20px',
        borderRadius: '25px',
        fontWeight: '600',
        cursor: 'pointer',
        fontSize: '14px',
      }}>
        Sign up
      </button>
    </div>
  </nav>


  {/* ==========================================
      SEÇÃO 2: HERO
      - Título com tipografia serif italic
      - Palavra "unique" destacada em azul
      - Badges flutuantes (courses, students, testimonial)
      - Blob decorativo de fundo
      ========================================== */}
  <section style={{
    display: 'flex',
    alignItems: 'center',
    padding: '60px',
    background: `linear-gradient(135deg, ${COLORS.bg} 0%, ${COLORS.bgWarm} 100%)`,
    position: 'relative',
    overflow: 'hidden',
  }}>
    {/* Lado esquerdo - Texto */}
    <div style={{ flex: 1, maxWidth: '520px' }}>
      <h1 style={{ 
        fontSize: '52px', 
        fontWeight: '700', 
        lineHeight: '1.15', 
        color: COLORS.text, 
        marginBottom: '20px',
        fontFamily: 'Georgia, serif',
        fontStyle: 'italic',
      }}>
        A <span style={{ color: COLORS.primary }}>unique</span> approach to learning foreign languages online
      </h1>
      <p style={{ 
        color: COLORS.textLight, 
        fontSize: '17px', 
        lineHeight: '1.6', 
        marginBottom: '32px' 
      }}>
        Learn at your own pace, with lifetime access on mobile and desktop. Progress, not perfection.
      </p>
      <button style={{
        background: COLORS.primary,
        color: '#fff',
        border: 'none',
        padding: '16px 36px',
        borderRadius: '30px',
        fontWeight: '600',
        fontSize: '16px',
        cursor: 'pointer',
        boxShadow: `0 10px 30px ${COLORS.primary}40`,
      }}>
        Get started
      </button>
    </div>

    {/* Lado direito - Visual com badges */}
    <div style={{ 
      flex: 1, 
      position: 'relative', 
      display: 'flex', 
      justifyContent: 'center', 
      minHeight: '400px' 
    }}>
      {/* Blob decorativo de fundo */}
      <div style={{
        position: 'absolute',
        width: '380px',
        height: '380px',
        background: `linear-gradient(135deg, ${COLORS.primaryLight} 0%, ${COLORS.accent}30 100%)`,
        borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%',
        zIndex: 0,
      }} />
      
      {/* Placeholder da imagem principal */}
      <div style={{
        width: '280px',
        height: '320px',
        background: `linear-gradient(180deg, ${COLORS.primaryLight} 0%, ${COLORS.accent}20 100%)`,
        borderRadius: '20px',
        zIndex: 1,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '100px',
      }}>
        👩‍🎓
      </div>

      {/* Badge: 10+ Courses */}
      <div style={{
        position: 'absolute',
        top: '30px',
        right: '60px',
        background: '#fff',
        padding: '14px 18px',
        borderRadius: '14px',
        boxShadow: '0 8px 30px rgba(0,0,0,0.1)',
        zIndex: 2,
      }}>
        <div style={{ color: COLORS.primary, fontWeight: '700', fontSize: '18px' }}>
          10+ Courses
        </div>
        <div style={{ color: COLORS.textLight, fontSize: '12px' }}>
          Multiple Categories
        </div>
      </div>

      {/* Badge: Students count */}
      <div style={{
        position: 'absolute',
        bottom: '50px',
        right: '40px',
        background: '#fff',
        padding: '14px 18px',
        borderRadius: '14px',
        boxShadow: '0 8px 30px rgba(0,0,0,0.1)',
        zIndex: 2,
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
      }}>
        {/* Avatares empilhados */}
        <div style={{ display: 'flex' }}>
          {['😊', '😄', '🙂', '😃'].map((emoji, i) => (
            <div key={i} style={{
              width: '30px',
              height: '30px',
              background: COLORS.primaryLight,
              borderRadius: '50%',
              marginLeft: i > 0 ? '-8px' : '0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '2px solid #fff',
              fontSize: '14px',
            }}>
              {emoji}
            </div>
          ))}
        </div>
        <div>
          <div style={{ fontWeight: '600', color: COLORS.text, fontSize: '14px' }}>+ 50</div>
          <div style={{ fontSize: '10px', color: COLORS.textLight }}>Dedicated students per day</div>
        </div>
      </div>

      {/* Badge: Testimonial mini */}
      <div style={{
        position: 'absolute',
        top: '80px',
        left: '20px',
        background: '#fff',
        padding: '12px 16px',
        borderRadius: '12px',
        boxShadow: '0 8px 30px rgba(0,0,0,0.1)',
        zIndex: 2,
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
      }}>
        <div style={{
          width: '38px',
          height: '38px',
          background: COLORS.primaryLight,
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '18px',
        }}>
          👩
        </div>
        <div>
          <div style={{ fontWeight: '600', fontSize: '13px', color: COLORS.text }}>Jane Cooper</div>
          <div style={{ fontSize: '11px', color: COLORS.textLight }}>I loved the Italian course!</div>
        </div>
      </div>
    </div>
  </section>


  {/* ==========================================
      SEÇÃO 3: BENEFITS BAR
      - Fundo azul primário
      - 3 benefícios com ícones
      ========================================== */}
  <section style={{
    display: 'flex',
    justifyContent: 'center',
    gap: '80px',
    padding: '24px 60px',
    background: COLORS.primary,
  }}>
    {BENEFITS.map((benefit, i) => (
      <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <span style={{ fontSize: '24px' }}>{benefit.icon}</span>
        <span style={{ color: '#fff', fontWeight: '500', fontSize: '15px' }}>{benefit.text}</span>
      </div>
    ))}
  </section>


  {/* ==========================================
      SEÇÃO 4: GOALS + TRACK CARDS
      - Dois cards lado a lado
      - Card 1: Mockup de celular com goals
      - Card 2: Progresso com círculos
      ========================================== */}
  <section style={{ padding: '80px 60px' }}>
    {/* Título da seção */}
    <div style={{ textAlign: 'center', marginBottom: '50px' }}>
      <h2 style={{ 
        fontSize: '40px', 
        fontWeight: '700', 
        color: COLORS.text,
        fontFamily: 'Georgia, serif',
      }}>
        Start with your goals
      </h2>
      <p style={{ 
        color: COLORS.textLight, 
        fontSize: '16px', 
        marginTop: '12px', 
        maxWidth: '600px', 
        margin: '12px auto 0' 
      }}>
        We recommend lessons, topics, and activities to help you reach your goals.
      </p>
    </div>

    {/* Container dos cards */}
    <div style={{ display: 'flex', gap: '24px' }}>
      
      {/* Card 1: Goals com Mockup de celular */}
      <div style={{
        flex: 1,
        background: COLORS.primaryLight,
        borderRadius: '24px',
        padding: '32px',
        position: 'relative',
        overflow: 'hidden',
        minHeight: '420px',
      }}>
        <h3 style={{ fontSize: '22px', fontWeight: '600', color: COLORS.text, marginBottom: '30px' }}>
          Focus on your unique goals
        </h3>
        
        {/* Mockup de celular */}
        <div style={{
          width: '220px',
          background: '#fff',
          borderRadius: '30px',
          padding: '12px',
          boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
          margin: '0 auto',
        }}>
          {/* Barra de status do celular */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            padding: '8px 12px', 
            fontSize: '11px', 
            color: COLORS.textLight 
          }}>
            <span>9:41</span>
            <span>📶 🔋</span>
          </div>
          
          {/* Conteúdo do mockup */}
          <div style={{ padding: '16px', textAlign: 'center' }}>
            <p style={{ fontSize: '18px', fontWeight: '600', color: COLORS.text, marginBottom: '20px' }}>
              What are your learning goals?
            </p>
            
            {/* Lista de goals clicáveis */}
            {LEARNING_GOALS.map((goal) => (
              <div
                key={goal.id}
                onClick={() => setSelectedGoal(goal.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  padding: '10px 16px',
                  borderRadius: '20px',
                  marginBottom: '8px',
                  cursor: 'pointer',
                  background: selectedGoal === goal.id || goal.active ? COLORS.primary : '#F3F4F6',
                  color: selectedGoal === goal.id || goal.active ? '#fff' : COLORS.text,
                  fontSize: '13px',
                  fontWeight: '500',
                  justifyContent: 'center',
                }}
              >
                <span>{goal.emoji}</span>
                <span>{goal.label}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Card 2: Track Progress */}
      <div style={{
        flex: 1,
        background: COLORS.bg,
        borderRadius: '24px',
        padding: '32px',
        position: 'relative',
        overflow: 'hidden',
        minHeight: '420px',
      }}>
        <h3 style={{ fontSize: '22px', fontWeight: '600', color: COLORS.text, marginBottom: '20px' }}>
          Track how you grow
        </h3>
        
        {/* Placeholder de imagem */}
        <div style={{
          width: '100%',
          height: '200px',
          background: `linear-gradient(135deg, ${COLORS.primaryLight} 0%, #fff 100%)`,
          borderRadius: '16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '80px',
          marginBottom: '24px',
        }}>
          🎯
        </div>

        {/* Círculos de progresso */}
        <div style={{ display: 'flex', justifyContent: 'space-around' }}>
          {PROGRESS_TRACKS.map((track, i) => (
            <div key={i} style={{ textAlign: 'center' }}>
              <div style={{
                width: '70px',
                height: '70px',
                borderRadius: '50%',
                border: `4px solid ${COLORS.primary}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '28px',
                background: '#fff',
                margin: '0 auto 10px',
              }}>
                {track.icon}
              </div>
              <span style={{ fontSize: '13px', fontWeight: '600', color: COLORS.text }}>
                {track.name}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  </section>


  {/* ==========================================
      SEÇÃO 5: VALUES (Our Values)
      - 3 cards com ícones
      - Fundo branco
      ========================================== */}
  <section style={{ padding: '60px', textAlign: 'center', background: '#fff' }}>
    <p style={{ 
      color: COLORS.primary, 
      fontSize: '13px', 
      fontWeight: '600', 
      marginBottom: '8px', 
      textTransform: 'uppercase', 
      letterSpacing: '1px' 
    }}>
      WHY CHOOSE US
    </p>
    <h2 style={{ 
      fontSize: '36px', 
      fontWeight: '700', 
      color: COLORS.text, 
      marginBottom: '50px', 
      fontFamily: 'Georgia, serif' 
    }}>
      Our values
    </h2>
    
    <div style={{ display: 'flex', justifyContent: 'center', gap: '50px' }}>
      {VALUES.map((value, i) => (
        <div key={i} style={{ maxWidth: '280px', textAlign: 'center' }}>
          <div style={{
            width: '90px',
            height: '90px',
            background: COLORS.bg,
            borderRadius: '24px',
            margin: '0 auto 20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '36px',
          }}>
            {value.icon}
          </div>
          <h3 style={{ fontSize: '18px', fontWeight: '600', color: COLORS.text, marginBottom: '10px' }}>
            {value.title}
          </h3>
          <p style={{ color: COLORS.textLight, fontSize: '14px', lineHeight: '1.6' }}>
            {value.desc}
          </p>
        </div>
      ))}
    </div>
  </section>


  {/* ==========================================
      SEÇÃO 6: TEACHERS
      - Cards de professores com tags
      - Badge "Super Tutor"
      - Carousel arrows
      ========================================== */}
  <section style={{ padding: '60px', background: COLORS.bgWarm }}>
    {/* Header com título e setas */}
    <div style={{ 
      display: 'flex', 
      justifyContent: 'space-between', 
      alignItems: 'center', 
      marginBottom: '40px' 
    }}>
      <div>
        <p style={{ 
          color: COLORS.textLight, 
          fontSize: '12px', 
          fontWeight: '600', 
          marginBottom: '8px', 
          textTransform: 'uppercase', 
          letterSpacing: '1px' 
        }}>
          KEY PERSONS
        </p>
        <h2 style={{ 
          fontSize: '36px', 
          fontWeight: '700', 
          color: COLORS.text, 
          fontFamily: 'Georgia, serif' 
        }}>
          Meet the right tutors for you
        </h2>
      </div>
      <div style={{ display: 'flex', gap: '10px' }}>
        <button style={{ 
          width: '48px', 
          height: '48px', 
          borderRadius: '50%', 
          border: '1px solid #E5E7EB', 
          background: '#fff', 
          cursor: 'pointer', 
          fontSize: '18px' 
        }}>
          ←
        </button>
        <button style={{ 
          width: '48px', 
          height: '48px', 
          borderRadius: '50%', 
          border: 'none', 
          background: COLORS.primary, 
          color: '#fff', 
          cursor: 'pointer', 
          fontSize: '18px' 
        }}>
          →
        </button>
      </div>
    </div>

    {/* Grid de cards de professores */}
    <div style={{ display: 'flex', gap: '24px' }}>
      {TEACHERS.map((teacher, i) => (
        <div key={i} style={{ 
          flex: 1, 
          background: '#fff', 
          borderRadius: '20px', 
          overflow: 'hidden', 
          boxShadow: '0 4px 20px rgba(0,0,0,0.05)' 
        }}>
          {/* Área da imagem com tags flutuantes */}
          <div style={{
            height: '200px',
            background: teacher.bgColor,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '70px',
            position: 'relative',
          }}>
            👨‍🏫
            {/* Tags de especialidades */}
            {teacher.tags.map((tag, j) => (
              <span key={j} style={{
                position: 'absolute',
                top: j === 0 ? '20px' : 'auto',
                bottom: j === 1 ? '20px' : 'auto',
                right: j === 0 ? '20px' : 'auto',
                left: j === 1 ? '20px' : 'auto',
                background: COLORS.primary,
                color: '#fff',
                padding: '6px 14px',
                borderRadius: '20px',
                fontSize: '12px',
                fontWeight: '500',
              }}>
                {tag}
              </span>
            ))}
          </div>
          
          {/* Informações do professor */}
          <div style={{ padding: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <h3 style={{ fontSize: '18px', fontWeight: '600', color: COLORS.text }}>
                {teacher.name}
              </h3>
              <span style={{ fontSize: '18px' }}>{teacher.flag}</span>
            </div>
            
            {/* Badge Super Tutor */}
            {teacher.superTutor && (
              <span style={{
                display: 'inline-block',
                background: COLORS.primaryLight,
                color: COLORS.primary,
                padding: '4px 10px',
                borderRadius: '12px',
                fontSize: '11px',
                fontWeight: '600',
                marginBottom: '10px',
              }}>
                ⭐ SUPER TUTOR
              </span>
            )}
            
            <p style={{ color: COLORS.textLight, fontSize: '13px', marginBottom: '4px' }}>
              🗣️ {teacher.accent}
            </p>
            <p style={{ color: COLORS.textLight, fontSize: '13px', marginBottom: '12px' }}>
              👍 {teacher.reviews} positive reviews
            </p>
            
            <button style={{
              width: '100%',
              padding: '12px',
              border: `1px solid ${COLORS.primary}`,
              background: '#fff',
              color: COLORS.primary,
              borderRadius: '12px',
              fontWeight: '600',
              cursor: 'pointer',
              fontSize: '14px',
            }}>
              See profile
            </button>
          </div>
        </div>
      ))}
    </div>
  </section>


  {/* ==========================================
      SEÇÃO 7: TESTIMONIAL
      - Depoimento grande com foto
      - Tipografia serif no quote
      - Dots de navegação
      ========================================== */}
  <section style={{ padding: '80px 60px', background: '#fff' }}>
    <p style={{ 
      color: COLORS.textLight, 
      fontSize: '12px', 
      fontWeight: '600', 
      marginBottom: '8px', 
      textTransform: 'uppercase', 
      letterSpacing: '1px' 
    }}>
      TOP STUDYING
    </p>
    <h2 style={{ 
      fontSize: '36px', 
      fontWeight: '700', 
      color: COLORS.text, 
      marginBottom: '50px', 
      fontFamily: 'Georgia, serif' 
    }}>
      Our students say
    </h2>
    
    <div style={{ display: 'flex', gap: '60px', alignItems: 'center' }}>
      {/* Foto do estudante */}
      <div style={{
        width: '350px',
        height: '400px',
        background: `linear-gradient(135deg, ${COLORS.primaryLight} 0%, #fff 100%)`,
        borderRadius: '24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '100px',
        flexShrink: 0,
      }}>
        🧑‍💼
      </div>
      
      {/* Conteúdo do depoimento */}
      <div>
        <h3 style={{ fontSize: '28px', fontWeight: '600', color: COLORS.text, marginBottom: '8px' }}>
          Martin Watson
        </h3>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '24px' }}>
          <span>🇪🇸</span>
          <span style={{ color: COLORS.textLight, fontSize: '14px' }}>B2 course student</span>
        </div>
        <p style={{ 
          fontSize: '22px', 
          color: '#374151', 
          lineHeight: '1.6', 
          maxWidth: '500px',
          fontFamily: 'Georgia, serif',
          fontStyle: 'italic',
        }}>
          "There is no way I could have made the same progress learning Spanish without using LangIA. The best part is now learning Spanish has become one of my biggest hobbies."
        </p>
        
        {/* Dots de navegação */}
        <div style={{ display: 'flex', gap: '8px', marginTop: '30px' }}>
          {[0, 1, 2, 3].map(i => (
            <div key={i} style={{
              width: '10px',
              height: '10px',
              borderRadius: '50%',
              background: i === 1 ? COLORS.primary : '#E5E7EB',
            }} />
          ))}
        </div>
      </div>
    </div>
  </section>


  {/* ==========================================
      SEÇÃO 8: FAQ
      - Accordion expansível
      - Grid 2 colunas
      - Item ativo com fundo azul
      ========================================== */}
  <section style={{ padding: '60px', background: COLORS.bgWarm }}>
    <p style={{ 
      color: COLORS.textLight, 
      fontSize: '12px', 
      fontWeight: '600', 
      marginBottom: '8px', 
      textTransform: 'uppercase', 
      letterSpacing: '1px' 
    }}>
      MORE INFO
    </p>
    <h2 style={{ 
      fontSize: '36px', 
      fontWeight: '700', 
      color: COLORS.text, 
      marginBottom: '40px', 
      fontFamily: 'Georgia, serif' 
    }}>
      Common questions
    </h2>
    
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
      {FAQS.map((faq, i) => (
        <div 
          key={i}
          onClick={() => setOpenFaq(openFaq === i ? -1 : i)}
          style={{
            background: openFaq === i ? COLORS.primary : '#fff',
            padding: '24px',
            borderRadius: '16px',
            cursor: 'pointer',
            transition: 'all 0.3s',
            boxShadow: '0 2px 10px rgba(0,0,0,0.03)',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ 
              fontWeight: '500', 
              color: openFaq === i ? '#fff' : COLORS.text, 
              fontSize: '15px' 
            }}>
              {faq.q}
            </span>
            <span style={{
              width: '28px',
              height: '28px',
              borderRadius: '50%',
              background: openFaq === i ? '#fff' : COLORS.primary,
              color: openFaq === i ? COLORS.primary : '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '16px',
              flexShrink: 0,
            }}>
              {openFaq === i ? '−' : '+'}
            </span>
          </div>
          {openFaq === i && (
            <p style={{ 
              color: '#fff', 
              marginTop: '16px', 
              fontSize: '14px', 
              lineHeight: '1.6', 
              opacity: 0.9 
            }}>
              {faq.a}
            </p>
          )}
        </div>
      ))}
    </div>
  </section>


  {/* ==========================================
      SEÇÃO 9: CTA (Call to Action)
      - Input de email + botão
      - Fundo branco
      ========================================== */}
  <section style={{ padding: '80px 60px', textAlign: 'center', background: '#fff' }}>
    <h2 style={{ 
      fontSize: '40px', 
      fontWeight: '700', 
      color: COLORS.text, 
      marginBottom: '16px', 
      fontFamily: 'Georgia, serif' 
    }}>
      Get a free trial lesson today
    </h2>
    <p style={{ color: COLORS.textLight, marginBottom: '40px', fontSize: '16px' }}>
      Start fulfilling your language dreams with our school
    </p>
    <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
      <input
        type="email"
        placeholder="Enter your email"
        style={{
          padding: '16px 24px',
          borderRadius: '30px',
          border: '1px solid #E5E7EB',
          width: '320px',
          fontSize: '15px',
          outline: 'none',
        }}
      />
      <button style={{
        background: COLORS.primary,
        color: '#fff',
        border: 'none',
        padding: '16px 36px',
        borderRadius: '30px',
        fontWeight: '600',
        cursor: 'pointer',
        fontSize: '15px',
      }}>
        Send
      </button>
    </div>
  </section>


  {/* ==========================================
      SEÇÃO 10: FOOTER (Dark Theme)
      - Fundo escuro igual navbar
      - Links organizados em colunas
      - Ícones sociais
      ========================================== */}
  <footer style={{
    background: COLORS.text,
    padding: '50px 60px',
    color: '#fff',
  }}>
    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
      {/* Logo e contato */}
      <div>
        <div style={{ fontSize: '24px', fontWeight: '700', marginBottom: '16px' }}>
          Lang<span style={{ color: COLORS.accent }}>IA</span>
        </div>
        <p style={{ color: '#9CA3AF', fontSize: '13px' }}>São Paulo, SP, Brasil</p>
        <p style={{ color: '#9CA3AF', fontSize: '13px' }}>+55 (11) 99999-9999</p>
      </div>
      
      {/* Colunas de links */}
      <div style={{ display: 'flex', gap: '80px' }}>
        <div>
          <h4 style={{ fontWeight: '600', marginBottom: '16px', fontSize: '14px' }}>Company</h4>
          {FOOTER_LINKS.company.map(item => (
            <p key={item} style={{ color: '#9CA3AF', fontSize: '13px', marginBottom: '10px', cursor: 'pointer' }}>
              {item}
            </p>
          ))}
        </div>
        <div>
          <h4 style={{ fontWeight: '600', marginBottom: '16px', fontSize: '14px' }}>Product</h4>
          {FOOTER_LINKS.product.map(item => (
            <p key={item} style={{ color: '#9CA3AF', fontSize: '13px', marginBottom: '10px', cursor: 'pointer' }}>
              {item}
            </p>
          ))}
        </div>
        <div>
          <h4 style={{ fontWeight: '600', marginBottom: '16px', fontSize: '14px' }}>Legal</h4>
          {FOOTER_LINKS.legal.map(item => (
            <p key={item} style={{ color: '#9CA3AF', fontSize: '13px', marginBottom: '10px', cursor: 'pointer' }}>
              {item}
            </p>
          ))}
        </div>
      </div>

      {/* Ícones sociais */}
      <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
        {['f', '𝕏', '📷'].map((icon, i) => (
          <div key={i} style={{
            width: '40px',
            height: '40px',
            background: 'rgba(255,255,255,0.1)',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
          }}>
            {icon}
          </div>
        ))}
      </div>
    </div>
  </footer>
</div>
```

);
}