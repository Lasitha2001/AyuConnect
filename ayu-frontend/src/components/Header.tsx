import { useLocation } from 'react-router-dom';
import { BellIcon, SunIcon, MoonIcon } from 'lucide-react';
import { useTheme } from '../contexts/ThemeContext';
const Header = () => {
  const location = useLocation();
  const {
    theme,
    toggleTheme
  } = useTheme();
  const userAvatar = "/2.png";
  const getPageTitle = () => {
    switch (location.pathname) {
      case '/dashboard':
        return 'Dashboard';
      case '/notices':
        return 'Notices';
      case '/chat':
        return 'Chat';
      case '/files':
        return 'Files';
      case '/profile':
        return 'Profile';
      default:
        return 'Dashboard';
    }
  };
  return <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 py-4 px-6 flex items-center justify-between">
      <h1 className="text-xl font-semibold text-gray-800 dark:text-white">
        {getPageTitle()}
      </h1>
      <div className="flex items-center space-x-4">
        <button onClick={toggleTheme} className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          {theme === 'light' ? <MoonIcon className="h-5 w-5 text-gray-600 dark:text-gray-300" /> : <SunIcon className="h-5 w-5 text-gray-600 dark:text-gray-300" />}
        </button>
        <button className="relative p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700">
          <BellIcon className="h-5 w-5 text-gray-600 dark:text-gray-300" />
          <span className="absolute top-1 right-1 bg-red-500 rounded-full w-2 h-2"></span>
        </button>
        <div className="flex items-center">
          <div className="mr-3 text-right">
            <div className="text-sm font-medium text-gray-900 dark:text-white">
              User
            </div>
            <div className="text-xs text-gray-500 dark:text-gray-400">
              Student
            </div>
          </div>
          <img src={userAvatar} alt="User Avatar" className="h-10 w-10 rounded-full object-cover" />
        </div>
      </div>
    </header>;
};
export default Header;