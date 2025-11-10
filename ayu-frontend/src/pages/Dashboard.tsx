import { ArrowRight } from 'lucide-react';
import Button from '../components/Button';
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';

const Dashboard = () => {
  const userAvatar = "/2.png";
  const [recentNotices, setRecentNotices] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  const API_URL = "http://localhost:8080/api/notices";
  
  useEffect(() => {
    fetchRecentNotices();
  }, []);
  
  const fetchRecentNotices = async () => {
    try {
      const res = await fetch(API_URL);
      if (!res.ok) throw new Error("Failed to fetch notices");
      const data = await res.json();
      // Get only the latest 3 notices
      setRecentNotices(data.slice(0, 3));
    } catch (error) {
      console.error("Error fetching notices:", error);
      setRecentNotices([]);
    } finally {
      setLoading(false);
    }
  };
  
  const getTimeAgo = (dateString: string) => {
    if (!dateString) return "Just now";
    const date = new Date(dateString);
    const now = new Date();
    const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);
    
    if (seconds < 60) return "Just now";
    if (seconds < 3600) return `${Math.floor(seconds / 60)} minutes ago`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours ago`;
    if (seconds < 604800) return `${Math.floor(seconds / 86400)} days ago`;
    return date.toLocaleDateString();
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
          {loading ? (
            <div className="text-center py-8 text-gray-500 dark:text-gray-400">
              Loading notices...
            </div>
          ) : recentNotices.length > 0 ? (
            recentNotices.map(notice => <div key={notice.id} className="border border-gray-100 dark:border-gray-700 rounded-xl p-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
              <div className="flex items-start mb-3">
                <div className="flex-1">
                  <div className="font-medium text-gray-900 dark:text-white mb-1">
                    {notice.author || "Unknown"}
                  </div>
                  <div className="text-sm text-gray-500 dark:text-gray-400">
                    {getTimeAgo(notice.postedAt)}
                  </div>
                </div>
              </div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-1">
                {notice.title}
              </h4>
              <p className="text-gray-600 dark:text-gray-400 text-sm line-clamp-2">
                {notice.content}
              </p>
            </div>)
          ) : (
            <div className="text-center py-8 text-gray-500 dark:text-gray-400">
              No notices available. Create one to get started!
            </div>
          )}
        </div>
      </div>
    </div>;
};
export default Dashboard;