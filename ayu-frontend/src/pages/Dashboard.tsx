import { ArrowRight } from 'lucide-react';
import Button from '../components/Button';
import { useNavigate } from 'react-router-dom';
const Dashboard = () => {
  const userAvatar = "/2.png";
  const recentNotices = [{
    id: 1,
    title: 'Campus Fest 2025 - Registration Open',
    author: 'Sarah Johnson',
    authorRole: 'Student Affairs',
    avatar: "/2.png",
    date: '2 hours ago',
    category: 'Events',
    excerpt: 'Annual campus festival registration is now open! Join us for 3 days of cultural events, competitions, and performances.'
  }, {
    id: 2,
    title: 'Library Hours Extended During Finals Week',
    author: 'Michael Chen',
    authorRole: 'Library Services',
    avatar: "/1.png",
    date: 'Yesterday',
    category: 'Academic',
    excerpt: 'The main library will remain open 24/7 during finals week to accommodate student study needs.'
  }, {
    id: 3,
    title: 'New Online Course Registration System',
    author: 'Amanda Rodriguez',
    authorRole: 'IT Department',
    avatar: "/3.png",
    date: '3 days ago',
    category: 'Services',
    excerpt: "We're launching a new course registration system next semester. Join our workshop to learn how to use it."
  }];
  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'Events':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
      case 'Academic':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'Services':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'Workshop':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
    }
  };
  const navigate = useNavigate();
  return <div className="space-y-6">
      {/* Welcome Section */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-800 dark:text-white">
              Hello, User 👋
            </h2>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Welcome to your MyUni dashboard
            </p>
          </div>
          <img src={userAvatar} alt="User Avatar" className="h-16 w-16 rounded-full object-cover" />
        </div>
      </div>
      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div className="text-blue-500 dark:text-blue-400 text-2xl font-bold">
            12
          </div>
          <div className="text-gray-600 dark:text-gray-400 mt-1">
            Notices Posted
          </div>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div className="text-green-500 dark:text-green-400 text-2xl font-bold">
            47
          </div>
          <div className="text-gray-600 dark:text-gray-400 mt-1">
            Files Uploaded
          </div>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div className="text-yellow-500 dark:text-yellow-400 text-2xl font-bold">
            234
          </div>
          <div className="text-gray-600 dark:text-gray-400 mt-1">
            Messages Sent
          </div>
        </div>
      </div>
      {/* Latest Notices */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-semibold text-gray-800 dark:text-white">
            Latest Notices
          </h3>
          <Button variant="outline" size="sm" icon={<ArrowRight className="h-4 w-4" />} onClick={() => navigate('/notices')}>
            View All
          </Button>
        </div>
        <div className="space-y-4">
          {recentNotices.map(notice => <div key={notice.id} className="border border-gray-100 dark:border-gray-700 rounded-xl p-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
              <div className="flex items-center mb-3">
                <img src={notice.avatar} alt={notice.author} className="h-10 w-10 rounded-full object-cover mr-3" />
                <div>
                  <div className="font-medium text-gray-900 dark:text-white">
                    {notice.author}
                  </div>
                  <div className="text-sm text-gray-500 dark:text-gray-400 flex items-center">
                    <span>{notice.authorRole}</span>
                    <span className="mx-1.5">•</span>
                    <span>{notice.date}</span>
                  </div>
                </div>
                <span className={`ml-auto text-xs px-2.5 py-1 rounded-full ${getCategoryColor(notice.category)}`}>
                  {notice.category}
                </span>
              </div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-1">
                {notice.title}
              </h4>
              <p className="text-gray-600 dark:text-gray-400 text-sm">
                {notice.excerpt}
              </p>
            </div>)}
        </div>
      </div>
    </div>;
};
export default Dashboard;