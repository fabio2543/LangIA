import { NavLink } from 'react-router-dom';
import { cn } from '../../utils/cn';
import { useTranslation } from '../../i18n';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  activeIcon: string;
}

export const BottomNav = () => {
  const { t } = useTranslation();

  const navItems: NavItem[] = [
    {
      path: '/dashboard',
      label: t.bottomNav?.home || 'Home',
      icon: '🏠',
      activeIcon: '🏠',
    },
    {
      path: '/lessons',
      label: t.bottomNav?.learn || 'Learn',
      icon: '📚',
      activeIcon: '📚',
    },
    {
      path: '/tutors',
      label: t.bottomNav?.tutors || 'Tutors',
      icon: '👨‍🏫',
      activeIcon: '👨‍🏫',
    },
    {
      path: '/profile',
      label: t.bottomNav?.profile || 'Profile',
      icon: '👤',
      activeIcon: '👤',
    },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 md:hidden bottom-nav z-50">
      <div className="flex items-center justify-around h-16">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              cn(
                'flex flex-col items-center justify-center flex-1 h-full transition-colors',
                isActive
                  ? 'text-primary'
                  : 'text-text-light hover:text-text'
              )
            }
          >
            {({ isActive }) => (
              <>
                <span className="text-xl mb-0.5">
                  {isActive ? item.activeIcon : item.icon}
                </span>
                <span
                  className={cn(
                    'text-xs font-medium',
                    isActive && 'font-semibold'
                  )}
                >
                  {item.label}
                </span>
                {isActive && (
                  <span className="absolute bottom-1 w-1 h-1 rounded-full bg-primary" />
                )}
              </>
            )}
          </NavLink>
        ))}
      </div>
    </nav>
  );
};
