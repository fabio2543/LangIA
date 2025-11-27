import { Navbar, Footer } from '../components/layout';
import {
  Hero,
  BenefitsBar,
  GoalsSection,
  ValuesSection,
  TeachersSection,
  TestimonialSection,
  FaqSection,
  CtaSection,
} from '../components/sections';

export const LandingPage = () => {
  return (
    <div className="min-h-screen bg-bg-warm">
      <Navbar />
      <main>
        <Hero />
        <BenefitsBar />
        <GoalsSection />
        <ValuesSection />
        <TeachersSection />
        <TestimonialSection />
        <FaqSection />
        <CtaSection />
      </main>
      <Footer />
    </div>
  );
};
