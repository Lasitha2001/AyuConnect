import { useState } from 'react';
import { PlusIcon, Search, Heart, MessageSquare, Share2, Bookmark } from 'lucide-react';
import Button from '../components/Button';
const Notices = () => {
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [likedNotices, setLikedNotices] = useState<number[]>([]);
  const [savedNotices, setSavedNotices] = useState<number[]>([]);
  const [shareMenuOpen, setShareMenuOpen] = useState<number | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const notices = [{
    id: 1,
    title: 'Campus Fest 2025 - Registration Open',
    author: 'Sarah Johnson',
    authorRole: 'Student Affairs',
    avatar: "/2.png",
    date: '2 hours ago',
    category: 'Events',
    content: 'Annual campus festival registration is now open! Join us for 3 days of cultural events, competitions, and performances. This year we have amazing guest performers and exciting new activities planned.',
    likes: 127,
    comments: 23,
    shares: 15
  }, {
    id: 2,
    title: 'Library Hours Extended During Finals Week',
    author: 'Michael Chen',
    authorRole: 'Library Services',
    avatar: "/1.png",
    date: 'Yesterday',
    category: 'Academic',
    content: 'The main library will remain open 24/7 during finals week to accommodate student study needs. Additional study rooms have been made available for group study sessions.',
    likes: 84,
    comments: 12,
    shares: 7
  }, {
    id: 3,
    title: 'New Online Course Registration System',
    author: 'Amanda Rodriguez',
    authorRole: 'IT Department',
    avatar: "/3.png",
    date: '3 days ago',
    category: 'Services',
    content: "We're launching a new course registration system next semester. Join our workshop to learn how to use it effectively and get your questions answered.",
    likes: 56,
    comments: 34,
    shares: 9
  }, {
    id: 4,
    title: 'Career Development Workshop',
    author: 'David Wilson',
    authorRole: 'Career Center',
    avatar: "/4.png",
    date: '5 days ago',
    category: 'Workshop',
    content: 'Join us for a resume building and interview skills workshop this Friday. Learn how to make your application stand out and practice your interview techniques with industry professionals.',
    likes: 92,
    comments: 18,
    shares: 22
  }];
  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'Events':
        return 'bg-purple-100 text-purple-800';
      case 'Academic':
        return 'bg-blue-100 text-blue-800';
      case 'Services':
        return 'bg-green-100 text-green-800';
      case 'Workshop':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };
  const handleLike = (id: number) => {
    if (likedNotices.includes(id)) {
      setLikedNotices(likedNotices.filter(noticeId => noticeId !== id));
    } else {
      setLikedNotices([...likedNotices, id]);
    }
  };
  const handleSave = (id: number) => {
    if (savedNotices.includes(id)) {
      setSavedNotices(savedNotices.filter(noticeId => noticeId !== id));
    } else {
      setSavedNotices([...savedNotices, id]);
    }
  };
  const toggleShareMenu = (id: number) => {
    setShareMenuOpen(prev => (prev === id ? null : id));
  };
  const closeShareMenu = () => setShareMenuOpen(null);
  const showToast = (msg: string) => {
    setToastMessage(msg);
    window.setTimeout(() => setToastMessage(null), 2000);
  };
  const copyLink = async (id: number) => {
    try {
      const url = `${window.location.origin}${window.location.pathname}#/notices?notice=${id}`;
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(url);
      } else {
        const el = document.createElement('textarea');
        el.value = url;
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);
      }
      showToast('Link copied to clipboard');
      closeShareMenu();
    } catch (err) {
      showToast('Unable to copy link');
      closeShareMenu();
    }
  };
  const doWebShare = async (id: number) => {
    try {
      const url = `${window.location.origin}${window.location.pathname}#/notices?notice=${id}`;
      if ((navigator as any).share) {
        await (navigator as any).share({ title: 'MyUni Notice', url });
        closeShareMenu();
      } else {
        showToast('Web Share not supported');
      }
    } catch (err) {
      showToast('Share failed');
    }
  };
  const filteredNotices = notices.filter(notice => notice.title.toLowerCase().includes(searchQuery.toLowerCase()) || notice.content.toLowerCase().includes(searchQuery.toLowerCase()) || notice.author.toLowerCase().includes(searchQuery.toLowerCase()));
  return <div>
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">MyUni Notices</h1>
          <p className="text-gray-600 mt-1">
            Stay updated with campus announcements
          </p>
        </div>
        <Button onClick={() => setShowCreateModal(true)} icon={<PlusIcon className="h-5 w-5" />} className="mt-4 md:mt-0">
          Post Notice
        </Button>
      </div>
      {/* Search */}
      <div className="relative mb-6">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <Search className="h-5 w-5 text-gray-400" />
        </div>
        <input type="text" placeholder="Search notices..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} className="pl-10 w-full px-4 py-3 rounded-full bg-white border border-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500" />
      </div>
      {/* Notices Feed */}
      <div className="space-y-6">
        {filteredNotices.map(notice => <div key={notice.id} className="bg-white rounded-2xl shadow-sm overflow-hidden">
            <div className="p-6">
              {/* Author info */}
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center">
                  <img src={notice.avatar} alt={notice.author} className="h-12 w-12 rounded-full object-cover mr-3" />
                  <div>
                    <div className="font-medium text-gray-900">
                      {notice.author}
                    </div>
                    <div className="text-sm text-gray-500 flex items-center">
                      <span>{notice.authorRole}</span>
                      <span className="mx-1.5">•</span>
                      <span>{notice.date}</span>
                    </div>
                  </div>
                </div>
                <button className="text-gray-400 hover:text-gray-600">
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
                  </svg>
                </button>
              </div>
              {/* Category */}
              <div className="mb-3">
                <span className={`text-xs px-2.5 py-1 rounded-full ${getCategoryColor(notice.category)}`}>
                  {notice.category}
                </span>
              </div>
              {/* Content */}
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                {notice.title}
              </h3>
              <p className="text-gray-700">{notice.content}</p>
              {/* Engagement stats */}
              <div className="flex items-center mt-4 text-sm text-gray-500">
                <span>{notice.likes} likes</span>
                <span className="mx-2">•</span>
                <span>{notice.comments} comments</span>
                <span className="mx-2">•</span>
                <span>{notice.shares} shares</span>
              </div>
            </div>
            {/* Action buttons */}
            <div className="border-t border-gray-100 px-6 py-3 flex items-center justify-between">
              <button onClick={() => handleLike(notice.id)} className={`flex items-center ${likedNotices.includes(notice.id) ? 'text-red-500' : 'text-gray-500 hover:text-red-500'}`}>
                <Heart className={`h-5 w-5 mr-1.5 ${likedNotices.includes(notice.id) ? 'fill-current' : ''}`} />
                <span>Like</span>
              </button>
              <button className="flex items-center text-gray-500 hover:text-blue-500">
                <MessageSquare className="h-5 w-5 mr-1.5" />
                <span>Comment</span>
              </button>
                <div className="relative">
                  <button onClick={() => toggleShareMenu(notice.id)} className="flex items-center text-gray-500 hover:text-green-500">
                    <Share2 className="h-5 w-5 mr-1.5" />
                    <span>Share</span>
                  </button>
                  {shareMenuOpen === notice.id && <div className="absolute right-0 mt-2 w-44 bg-white border border-gray-200 rounded-lg shadow-lg z-20 text-sm">
                      <button onClick={() => copyLink(notice.id)} className="w-full text-left px-3 py-2 hover:bg-gray-50">Copy Link</button>
                      <button onClick={() => doWebShare(notice.id)} className="w-full text-left px-3 py-2 hover:bg-gray-50">Share...</button>
                    </div>}
                </div>
              <button onClick={() => handleSave(notice.id)} className={`flex items-center ${savedNotices.includes(notice.id) ? 'text-blue-500' : 'text-gray-500 hover:text-blue-500'}`}>
                <Bookmark className={`h-5 w-5 mr-1.5 ${savedNotices.includes(notice.id) ? 'fill-current' : ''}`} />
                <span>Save</span>
              </button>
            </div>
          </div>)}
      </div>
      {/* Create Notice Modal */}
      {showCreateModal && <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full p-6 relative">
            <button onClick={() => setShowCreateModal(false)} className="absolute top-6 right-6 text-gray-500 hover:text-gray-700">
              ✕
            </button>
            <h2 className="text-2xl font-bold mb-1">Create New Notice</h2>
            <p className="text-gray-600 mb-6">
              Share important information with the campus community
            </p>
            <form>
              <div className="mb-4">
                <label htmlFor="noticeTitle" className="block text-gray-700 font-medium mb-2">
                  Notice Title
                </label>
                <input type="text" id="noticeTitle" placeholder="Enter notice title..." className="w-full px-4 py-3 rounded-lg bg-gray-100 border border-gray-200 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500" required />
              </div>
              <div className="mb-4">
                <label htmlFor="noticeContent" className="block text-gray-700 font-medium mb-2">
                  Content
                </label>
                <textarea id="noticeContent" placeholder="Enter notice content..." rows={6} className="w-full px-4 py-3 rounded-lg bg-gray-100 border border-gray-200 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500" required />
              </div>
              <div className="mb-6">
                <label className="block text-gray-700 font-medium mb-2">
                  Attach Image (Optional)
                </label>
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
                  <div className="flex flex-col items-center">
                    <svg className="h-10 w-10 text-gray-400 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                    </svg>
                    <p className="text-gray-600 mb-1">
                      Click to upload or drag and drop
                    </p>
                    <p className="text-xs text-gray-500">PNG, JPG up to 10MB</p>
                  </div>
                </div>
              </div>
              <div className="flex justify-end space-x-4">
                <Button variant="outline" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </Button>
                <Button type="submit">Publish Notice</Button>
              </div>
            </form>
          </div>
        </div>}
      {toastMessage && <div className="fixed bottom-6 right-6 bg-gray-900 text-white px-4 py-2 rounded-md shadow-lg z-50">
        {toastMessage}
      </div>}
    </div>;
};
export default Notices;