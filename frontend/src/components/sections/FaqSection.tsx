import { useState } from 'react';
import { useTranslation } from '../../i18n';

export const FaqSection = () => {
  const { t } = useTranslation();
  const [openIndex, setOpenIndex] = useState(5);

  const faqs = [
    { q: t.faq.q1, a: t.faq.a1 },
    { q: t.faq.q2, a: t.faq.a2 },
    { q: t.faq.q3, a: t.faq.a3 },
    { q: t.faq.q4, a: t.faq.a4 },
    { q: t.faq.q5, a: t.faq.a5 },
    { q: t.faq.q6, a: t.faq.a6 },
  ];

  return (
    <section className="px-6 lg:px-15 py-12 lg:py-15 bg-bg-warm">
      <p className="text-text-light text-xs font-semibold uppercase tracking-wider mb-2">
        {t.faq.sectionLabel}
      </p>
      <h2 className="text-3xl lg:text-4xl font-bold text-text font-serif mb-10">
        {t.faq.title}
      </h2>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {faqs.map((faq, i) => (
          <div
            key={i}
            onClick={() => setOpenIndex(openIndex === i ? -1 : i)}
            className={`p-5 lg:p-6 rounded-2xl cursor-pointer transition-all shadow-sm ${
              openIndex === i
                ? 'bg-primary'
                : 'bg-white hover:shadow-md'
            }`}
          >
            <div className="flex justify-between items-center gap-4">
              <span
                className={`font-medium text-sm lg:text-base ${
                  openIndex === i ? 'text-white' : 'text-text'
                }`}
              >
                {faq.q}
              </span>
              <span
                className={`w-7 h-7 rounded-full flex items-center justify-center text-base flex-shrink-0 ${
                  openIndex === i
                    ? 'bg-white text-primary'
                    : 'bg-primary text-white'
                }`}
              >
                {openIndex === i ? '−' : '+'}
              </span>
            </div>
            {openIndex === i && (
              <p className="text-white/90 mt-4 text-sm leading-relaxed">
                {faq.a}
              </p>
            )}
          </div>
        ))}
      </div>
    </section>
  );
};
