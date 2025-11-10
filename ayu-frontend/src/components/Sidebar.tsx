import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Bell, MessageSquare, FileText, User, LogOut } from 'lucide-react';
const Sidebar = () => {
  const navigate = useNavigate();
  const handleLogout = () => {
    navigate('/login');
  };
  return <div className="w-64 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 h-full flex flex-col">
      <div className="p-4 border-b border-gray-200 dark:border-gray-700">
        <h1 className="text-xl font-bold text-blue-500 dark:text-blue-400">
          MyUni
        </h1>
      </div>
      <nav className="flex-1 p-4">
        <ul className="space-y-2">
          <li>
            <NavLink to="/dashboard" className={({
            isActive
          }) => `flex items-center p-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-gray-700 ${isActive ? 'bg-blue-50 dark:bg-gray-700' : ''}`}>
              <LayoutDashboard className="h-5 w-5 mr-3" />
              <span>Dashboard</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/notices" className={({
            isActive
          }) => `flex items-center p-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-gray-700 ${isActive ? 'bg-blue-50 dark:bg-gray-700 text-blue-500 dark:text-blue-400' : ''}`}>
              <Bell className="h-5 w-5 mr-3" />
              <span>Notices</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/chat" className={({
            isActive
          }) => `flex items-center p-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-gray-700 ${isActive ? 'bg-blue-50 dark:bg-gray-700' : ''}`}>
              <MessageSquare className="h-5 w-5 mr-3" />
              <span>Chat</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/files" className={({
            isActive
          }) => `flex items-center p-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-gray-700 ${isActive ? 'bg-blue-50 dark:bg-gray-700' : ''}`}>
              <FileText className="h-5 w-5 mr-3" />
              <span>Files</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/profile" className={({
            isActive
          }) => `flex items-center p-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-gray-700 ${isActive ? 'bg-blue-50 dark:bg-gray-700' : ''}`}>
              <User className="h-5 w-5 mr-3" />
              <span>Profile</span>
            </NavLink>
          </li>
        </ul>
      </nav>
      <div className="p-4 border-t border-gray-200 dark:border-gray-700">
        <button onClick={handleLogout} className="flex items-center p-3 rounded-lg text-red-500 dark:text-red-400 hover:bg-red-50 dark:hover:bg-gray-700 w-full">
          <LogOut className="h-5 w-5 mr-3" />
          <span>Logout</span>
        </button>
      </div>
    </div>;
};
export default Sidebar;