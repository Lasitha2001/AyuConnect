import { useState, useEffect } from "react";
import { PlusIcon, Search, Trash2 } from "lucide-react";
import Button from "../components/Button";

const Notices = () => {
  const [notices, setNotices] = useState<any[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const API_URL = "http://localhost:8080/api/notices";

  useEffect(() => {
    fetchNotices();
  }, []);

  const fetchNotices = async () => {
    try {
      const res = await fetch(API_URL);
      if (!res.ok) throw new Error("Failed to fetch notices");
      const data = await res.json();
      setNotices(data);
    } catch (error) {
      console.error(error);
      showToast("Failed to load notices");
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    const title = (form.elements.namedItem("noticeTitle") as HTMLInputElement).value;
    const content = (form.elements.namedItem("noticeContent") as HTMLTextAreaElement).value;

    const newNotice = { title, content, author: "User" };

    try {
      const res = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newNotice),
      });

      if (!res.ok) throw new Error("Failed to create notice");
      const savedNotice = await res.json();
      setNotices((prev) => [savedNotice, ...prev]);
      setShowCreateModal(false);
      showToast("✅ Notice created!");
      form.reset();
    } catch (error) {
      console.error(error);
      showToast("❌ Failed to create notice");
    }
  };

  const deleteNotice = async (id: number) => {
    if (!window.confirm("Are you sure?")) return;
    try {
      const res = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
      if (res.ok) {
        setNotices((prev) => prev.filter((n) => n.id !== id));
        showToast("🗑️ Notice deleted!");
      } else {
        showToast("❌ Failed to delete");
      }
    } catch (err) {
      console.error(err);
      showToast("❌ Network error");
    }
  };

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 2500);
  };

  const filteredNotices = notices.filter(
    (n) =>
      n.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      n.content?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      n.author?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div>
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Notice Board</h1>
          <p className="text-gray-600 mt-1">Stay updated with announcements</p>
        </div>
        <Button onClick={() => setShowCreateModal(true)} icon={<PlusIcon className="h-5 w-5" />}>
          New Notice
        </Button>
      </div>

      <div className="relative mb-6">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <Search className="h-5 w-5 text-gray-400" />
        </div>
        <input
          type="text"
          placeholder="Search notices..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-10 w-full px-4 py-3 rounded-full border border-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div className="space-y-6">
        {filteredNotices.length > 0 ? (
          filteredNotices.map((notice) => (
            <div key={notice.id} className="bg-white rounded-2xl shadow-sm p-6">
              <div className="flex justify-between items-start mb-2">
                <div>
                  <h2 className="text-xl font-semibold">{notice.title}</h2>
                  <p className="text-gray-600 mt-1">{notice.content}</p>
                </div>
                <button onClick={() => deleteNotice(notice.id)} className="text-red-500 hover:text-red-700">
                  <Trash2 className="h-5 w-5" />
                </button>
              </div>
              <div className="text-sm text-gray-500 mt-3">
                Posted by <span className="font-medium">{notice.author || "Unknown"}</span> •{" "}
                {notice.postedAt ? new Date(notice.postedAt).toLocaleString() : "Just now"}
              </div>
            </div>
          ))
        ) : (
          <p className="text-gray-500 text-center">No notices found.</p>
        )}
      </div>

      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full p-6 relative">
            <button onClick={() => setShowCreateModal(false)} className="absolute top-6 right-6 text-gray-500 hover:text-gray-700">
              ✕
            </button>
            <h2 className="text-2xl font-bold mb-4">Create New Notice</h2>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label htmlFor="noticeTitle" className="block text-gray-700 font-medium mb-2">Title</label>
                <input type="text" id="noticeTitle" name="noticeTitle" required className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500" />
              </div>
              <div className="mb-6">
                <label htmlFor="noticeContent" className="block text-gray-700 font-medium mb-2">Content</label>
                <textarea id="noticeContent" name="noticeContent" required rows={6} className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500" />
              </div>
              <div className="flex justify-end space-x-4">
                <Button type="button" variant="outline" onClick={() => setShowCreateModal(false)}>Cancel</Button>
                <Button type="submit">Publish</Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {toastMessage && (
        <div className="fixed bottom-6 right-6 bg-gray-900 text-white px-4 py-2 rounded-md shadow-lg z-50">{toastMessage}</div>
      )}
    </div>
  );
};

export default Notices;
