import { useTranslation } from '../../i18n';

export const Footer = () => {
  const { t } = useTranslation();

  const companyLinks = [
    { label: t.footer.careers, href: '#careers' },
    { label: t.footer.teachers, href: '#teachers' },
    { label: t.footer.support, href: '#support' },
    { label: t.footer.contact, href: '#contact' },
  ];

  const productLinks = [
    { label: t.footer.courses, href: '#courses' },
    { label: t.footer.pricing, href: '#pricing' },
    { label: t.footer.blog, href: '#blog' },
  ];

  const legalLinks = [
    { label: t.footer.terms, href: '#terms' },
    { label: t.footer.privacy, href: '#privacy' },
  ];

  const socialIcons = [
    { icon: 'f', label: 'Facebook' },
    { icon: '𝕏', label: 'Twitter' },
    { icon: '📷', label: 'Instagram' },
  ];

  return (
    <footer className="bg-text text-white px-6 lg:px-15 py-12">
      <div className="flex flex-col lg:flex-row justify-between gap-10">
        {/* Logo & Contact */}
        <div>
          <a href="/" className="text-2xl font-bold">
            Lang<span className="text-accent">IA</span>
          </a>
          <p className="text-gray-400 text-sm mt-4">{t.footer.location}</p>
          <p className="text-gray-400 text-sm">{t.footer.phone}</p>
        </div>

        {/* Links Grid */}
        <div className="flex flex-wrap gap-12 lg:gap-20">
          {/* Company */}
          <div>
            <h4 className="font-semibold text-sm mb-4">{t.footer.company}</h4>
            {companyLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="block text-gray-400 text-sm mb-2.5 hover:text-white transition-colors"
              >
                {link.label}
              </a>
            ))}
          </div>

          {/* Product */}
          <div>
            <h4 className="font-semibold text-sm mb-4">{t.footer.product}</h4>
            {productLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="block text-gray-400 text-sm mb-2.5 hover:text-white transition-colors"
              >
                {link.label}
              </a>
            ))}
          </div>

          {/* Legal */}
          <div>
            <h4 className="font-semibold text-sm mb-4">{t.footer.legal}</h4>
            {legalLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="block text-gray-400 text-sm mb-2.5 hover:text-white transition-colors"
              >
                {link.label}
              </a>
            ))}
          </div>
        </div>

        {/* Social Icons */}
        <div className="flex gap-3 items-start">
          {socialIcons.map((social) => (
            <a
              key={social.label}
              href="#"
              aria-label={social.label}
              className="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center hover:bg-white/20 transition-colors"
            >
              {social.icon}
            </a>
          ))}
        </div>
      </div>
    </footer>
  );
};
