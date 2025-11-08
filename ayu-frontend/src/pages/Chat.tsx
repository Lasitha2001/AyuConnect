import React, { useState } from 'react';
import { Send, Smile, Paperclip, Hash, Info } from 'lucide-react';
const Chat = () => {
  const [message, setMessage] = useState('');
  const users = [{
    id: 1,
    name: 'Sarah Johnson',
    role: 'Student Affairs',
    avatar: "/2.png",
    online: true
  }, {
    id: 2,
    name: 'Michael Chen',
    role: 'Library Services',
    avatar: "/1.png",
    online: true
  }, {
    id: 3,
    name: 'Amanda Rodriguez',
    role: 'IT Department',
    avatar: "/3.png",
    online: true
  }, {
    id: 4,
    name: 'David Wilson',
    role: 'Career Center',
    avatar: "/4.png",
    online: false
  }, {
    id: 5,
    name: 'Emily Zhang',
    role: 'Student',
    avatar: "/5.png",
    online: false
  }];
  const channels = [{
    id: 1,
    name: 'general',
    unread: false
  }, {
    id: 2,
    name: 'announcements',
    unread: true
  }, {
    id: 3,
    name: 'events',
    unread: false
  }, {
    id: 4,
    name: 'help-desk',
    unread: false
  }];
  const messages = [{
    id: 1,
    userId: 1,
    username: 'Sarah Johnson',
    userColor: '#8B5CF6',
    avatar: "/2.png",
    time: '10:24 AM',
    content: 'Good morning everyone! Just a reminder that we have a campus tour scheduled today at 2 PM.',
    isCurrentUser: false
  }, {
    id: 2,
    userId: 3,
    username: 'Amanda Rodriguez',
    userColor: '#EC4899',
    avatar: "/3.png",
    time: '10:26 AM',
    content: 'Thanks for the reminder! Will the tour be starting from the main hall?',
    isCurrentUser: false
  }, {
    id: 3,
    userId: 1,
    username: 'Sarah Johnson',
    userColor: '#8B5CF6',
    avatar: "/2.png",
    time: '10:27 AM',
    content: "Yes, we'll meet at the main entrance of the administration building.",
    isCurrentUser: false
  }, {
    id: 4,
    userId: 0,
    username: 'You',
    userColor: '#3B82F6',
    avatar: "/4.png",
    time: '10:30 AM',
    content: "I'll be there! Is there anything specific we should prepare for the tour?",
    isCurrentUser: true
  }, {
    id: 5,
    userId: 2,
    username: 'Michael Chen',
    userColor: '#10B981',
    avatar: "/1.png",
    time: '10:32 AM',
    content: 'Just a heads up everyone - the library will be closing early today at 6 PM for system maintenance.',
    isCurrentUser: false
  }, {
    id: 6,
    userId: 0,
    username: 'You',
    userColor: '#3B82F6',
    avatar: "/4.png",
    time: '10:33 AM',
    content: 'Thanks for letting us know, Michael!',
    isCurrentUser: true
  }];
  const systemMessages = [{
    id: 1,
    content: 'New notice posted: "Campus Fest 2025 - Registration Open"',
    time: '9:45 AM'
  }];
  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (message.trim()) {
      // In a real app, this would send the message to a backend
      setMessage('');
    }
  };
  // Group messages by user for consecutive messages
  const groupedMessages: any[] = [];
  messages.forEach(msg => {
    const lastGroup = groupedMessages[groupedMessages.length - 1];
    if (lastGroup && lastGroup.userId === msg.userId) {
      lastGroup.messages.push(msg);
    } else {
      groupedMessages.push({
        userId: msg.userId,
        username: msg.username,
        userColor: msg.userColor,
        avatar: msg.avatar,
        isCurrentUser: msg.isCurrentUser,
        messages: [msg]
      });
    }
  });
  return <div className="flex h-full -mt-6 -mb-6 -mr-6 -ml-6">
      {/* Sidebar */}
      <div className="w-64 bg-[#2F3136] flex flex-col h-full">
        {/* Channels */}
        <div className="p-4">
          <h3 className="text-gray-300 uppercase text-xs font-semibold mb-2 tracking-wider">
            Channels
          </h3>
          <ul className="space-y-1">
            {channels.map(channel => <li key={channel.id}>
                <button className="flex items-center w-full rounded px-2 py-1.5 text-gray-300 hover:bg-gray-700 transition-colors text-left">
                  <Hash className="h-4 w-4 mr-1.5 text-gray-400" />
                  <span>{channel.name}</span>
                  {channel.unread && <span className="ml-auto bg-blue-500 rounded-full h-2 w-2"></span>}
                </button>
              </li>)}
          </ul>
        </div>
        {/* Online Members */}
        <div className="p-4 border-t border-gray-700 flex-1 overflow-y-auto">
          <h3 className="text-gray-300 uppercase text-xs font-semibold mb-2 tracking-wider">
            Online — {users.filter(u => u.online).length}
          </h3>
          <ul className="space-y-1">
            {users.filter(user => user.online).map(user => <li key={user.id}>
                  <button className="flex items-center w-full rounded px-2 py-1.5 text-gray-300 hover:bg-gray-700 transition-colors">
                    <div className="relative mr-2">
                      <img src={user.avatar} alt={user.name} className="h-8 w-8 rounded-full" />
                      <span className="absolute bottom-0 right-0 bg-green-500 rounded-full h-2.5 w-2.5 border-2 border-[#2F3136]"></span>
                    </div>
                    <div className="text-left">
                      <div className="text-sm font-medium">{user.name}</div>
                      <div className="text-xs text-gray-400">{user.role}</div>
                    </div>
                  </button>
                </li>)}
          </ul>
          <h3 className="text-gray-300 uppercase text-xs font-semibold mb-2 mt-4 tracking-wider">
            Offline — {users.filter(u => !u.online).length}
          </h3>
          <ul className="space-y-1">
            {users.filter(user => !user.online).map(user => <li key={user.id}>
                  <button className="flex items-center w-full rounded px-2 py-1.5 text-gray-400 hover:bg-gray-700 transition-colors">
                    <div className="relative mr-2">
                      <img src={user.avatar} alt={user.name} className="h-8 w-8 rounded-full grayscale opacity-70" />
                    </div>
                    <div className="text-left">
                      <div className="text-sm font-medium">{user.name}</div>
                      <div className="text-xs text-gray-500">{user.role}</div>
                    </div>
                  </button>
                </li>)}
          </ul>
        </div>
      </div>
      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col bg-white">
        {/* Channel Header */}
        <div className="border-b border-gray-200 p-4 flex items-center justify-between">
          <div className="flex items-center">
            <Hash className="h-5 w-5 text-gray-500 mr-2" />
            <h2 className="font-semibold text-gray-800">general</h2>
          </div>
          <div className="flex items-center text-gray-500">
            <button className="hover:bg-gray-100 p-2 rounded-full">
              <Info className="h-5 w-5" />
            </button>
          </div>
        </div>
        {/* Messages */}
        <div className="flex-1 overflow-y-auto p-4 space-y-6">
          {/* System Message */}
          {systemMessages.map(msg => <div key={msg.id} className="flex justify-center">
              <div className="bg-purple-100 text-purple-800 text-sm px-3 py-1.5 rounded-full">
                {msg.content} • {msg.time}
              </div>
            </div>)}
          {/* Chat Messages */}
          {groupedMessages.map((group, index) => <div key={index} className={`flex ${group.isCurrentUser ? 'justify-end' : ''}`}>
              {!group.isCurrentUser && <img src={group.avatar} alt={group.username} className="h-10 w-10 rounded-full mr-3 mt-1" />}
              <div className={`max-w-[80%] ${group.isCurrentUser ? 'items-end' : 'items-start'}`}>
                {!group.isCurrentUser && <div className="flex items-center mb-1">
                    <span className="font-medium" style={{
                color: group.userColor
              }}>
                      {group.username}
                    </span>
                    <span className="text-xs text-gray-500 ml-2">
                      {group.messages[0].time}
                    </span>
                  </div>}
                <div className="space-y-1">
                  {group.messages.map((msg: any) => <div key={msg.id} className={`rounded-2xl px-4 py-2 ${group.isCurrentUser ? 'bg-blue-500 text-white rounded-tr-none' : 'bg-gray-100 text-gray-800 rounded-tl-none'}`}>
                      {msg.content}
                    </div>)}
                </div>
              </div>
            </div>)}
        </div>
        {/* Message Input */}
        <div className="border-t border-gray-200 p-4">
          <form onSubmit={handleSendMessage} className="flex items-center">
            <button type="button" className="p-2 text-gray-500 hover:text-gray-700">
              <Paperclip className="h-5 w-5" />
            </button>
            <input type="text" value={message} onChange={e => setMessage(e.target.value)} placeholder="Type a message..." className="flex-1 border-none outline-none focus:ring-0 px-3 py-2" />
            <button type="button" className="p-2 text-gray-500 hover:text-gray-700 mr-1">
              <Smile className="h-5 w-5" />
            </button>
            <button type="submit" className={`p-2 rounded-full ${message.trim() ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-500'}`} disabled={!message.trim()}>
              <Send className="h-5 w-5" />
            </button>
          </form>
        </div>
      </div>
    </div>;
};
export default Chat;