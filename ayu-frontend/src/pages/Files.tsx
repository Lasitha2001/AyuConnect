import React, { useState } from 'react';
import { Upload, FileText, FileImage, FileArchive, Download, Trash2, BarChart3 } from 'lucide-react';
import Button from '../components/Button';
const Files = () => {
  const [isDragging, setIsDragging] = useState(false);
  const files = [{
    id: 1,
    name: 'Campus Fest Guidelines.pdf',
    type: 'pdf',
    size: '2.4 MB',
    uploadTime: '2 hours ago',
    author: 'Sarah Johnson',
    avatar: "/2.png",
    progress: 100
  }, {
    id: 2,
    name: 'Library Schedule.docx',
    type: 'doc',
    size: '568 KB',
    uploadTime: 'Yesterday',
    author: 'Michael Chen',
    avatar: "/1.png",
    progress: 100
  }, {
    id: 3,
    name: 'Course Registration Guide.pdf',
    type: 'pdf',
    size: '1.2 MB',
    uploadTime: '3 days ago',
    author: 'Amanda Rodriguez',
    avatar: "/3.png",
    progress: 100
  }, {
    id: 4,
    name: 'Campus Map.jpg',
    type: 'image',
    size: '3.8 MB',
    uploadTime: '5 days ago',
    author: 'David Wilson',
    avatar: "/4.png",
    progress: 100
  }, {
    id: 5,
    name: 'Project Resources.zip',
    type: 'archive',
    size: '8.7 MB',
    uploadTime: 'Just now',
    author: 'You',
    avatar: "/4.png",
    progress: 65
  }];
  const getFileIcon = (type: string) => {
    switch (type) {
      case 'pdf':
        return <div className="h-6 w-6 text-red-500" />;
      case 'doc':
        return <FileText className="h-6 w-6 text-blue-500" />;
      case 'image':
        return <FileImage className="h-6 w-6 text-green-500" />;
      case 'archive':
        return <FileArchive className="h-6 w-6 text-yellow-500" />;
      default:
        return <FileText className="h-6 w-6 text-gray-500" />;
    }
  };
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };
  const handleDragLeave = () => {
    setIsDragging(false);
  };
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    // In a real app, you would process the dropped files here
  };
  return <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-800">Files</h1>
        <p className="text-gray-600 mt-1">
          Upload, manage and share files with your campus
        </p>
      </div>
      {/* Upload Area */}
      <div className={`border-2 border-dashed rounded-2xl p-10 text-center transition-all ${isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-blue-400 hover:bg-gray-50'}`} onDragOver={handleDragOver} onDragLeave={handleDragLeave} onDrop={handleDrop}>
        <div className="flex flex-col items-center">
          <div className={`p-4 rounded-full ${isDragging ? 'bg-blue-100' : 'bg-gray-100'} mb-4`}>
            <Upload className={`h-8 w-8 ${isDragging ? 'text-blue-500' : 'text-gray-500'}`} />
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Upload your files
          </h3>
          <p className="text-gray-500 mb-4">
            Drag and drop files here, or click to browse
          </p>
          <Button variant="outline" size="sm">
            Browse Files
          </Button>
          <p className="text-xs text-gray-500 mt-4">
            Maximum file size: 50MB • Supported formats: PDF, DOC, JPG, PNG, ZIP
          </p>
        </div>
      </div>
      {/* Files List */}
      <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100">
          <h2 className="font-semibold text-gray-800">Your Files</h2>
        </div>
        <div className="divide-y divide-gray-100">
          {files.map(file => <div key={file.id} className="px-6 py-4 flex items-center">
              <div className="p-2.5 bg-gray-100 rounded-lg mr-4">
                {getFileIcon(file.type)}
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="text-sm font-medium text-gray-900 truncate">
                  {file.name}
                </h4>
                <div className="flex items-center mt-1">
                  <img src={file.avatar} alt={file.author} className="h-5 w-5 rounded-full mr-1.5" />
                  <span className="text-xs text-gray-500">{file.author}</span>
                  <span className="mx-1.5 text-gray-300">•</span>
                  <span className="text-xs text-gray-500">{file.size}</span>
                  <span className="mx-1.5 text-gray-300">•</span>
                  <span className="text-xs text-gray-500">
                    {file.uploadTime}
                  </span>
                </div>
                {file.progress < 100 && <div className="w-full bg-gray-200 rounded-full h-1.5 mt-2">
                    <div className="bg-blue-500 h-1.5 rounded-full" style={{
                width: `${file.progress}%`
              }}></div>
                  </div>}
              </div>
              <div className="flex items-center space-x-2 ml-4">
                {file.progress === 100 ? <>
                    <button className="p-2 text-gray-500 hover:text-blue-500 hover:bg-blue-50 rounded-full">
                      <Download className="h-5 w-5" />
                    </button>
                    <button className="p-2 text-gray-500 hover:text-red-500 hover:bg-red-50 rounded-full">
                      <Trash2 className="h-5 w-5" />
                    </button>
                  </> : <span className="text-sm text-blue-500">
                    {file.progress}%
                  </span>}
              </div>
            </div>)}
        </div>
      </div>
      {/* Storage Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-2xl p-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-gray-800">Storage Used</h3>
            <BarChart3 className="h-5 w-5 text-gray-400" />
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2.5">
            <div className="bg-blue-500 h-2.5 rounded-full" style={{
            width: '65%'
          }}></div>
          </div>
          <div className="flex items-center justify-between mt-2">
            <span className="text-sm text-gray-500">16.5 GB used</span>
            <span className="text-sm text-gray-500">25 GB total</span>
          </div>
        </div>
        <div className="bg-white rounded-2xl p-6 shadow-sm">
          <h3 className="font-semibold text-gray-800 mb-2">File Types</h3>
          <div className="space-y-2">
            <div className="flex items-center">
              <div className="w-3 h-3 rounded-full bg-blue-500 mr-2"></div>
              <span className="text-sm text-gray-600 flex-1">Documents</span>
              <span className="text-sm font-medium">45%</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 rounded-full bg-green-500 mr-2"></div>
              <span className="text-sm text-gray-600 flex-1">Images</span>
              <span className="text-sm font-medium">30%</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 rounded-full bg-yellow-500 mr-2"></div>
              <span className="text-sm text-gray-600 flex-1">Archives</span>
              <span className="text-sm font-medium">15%</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 rounded-full bg-red-500 mr-2"></div>
              <span className="text-sm text-gray-600 flex-1">Other</span>
              <span className="text-sm font-medium">10%</span>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-2xl p-6 shadow-sm">
          <h3 className="font-semibold text-gray-800 mb-2">Recent Activity</h3>
          <div className="space-y-3">
            <div className="text-sm">
              <p className="text-gray-800">You uploaded Campus Map.jpg</p>
              <p className="text-gray-500 text-xs">5 days ago</p>
            </div>
            <div className="text-sm">
              <p className="text-gray-800">
                Sarah shared Library Schedule.docx
              </p>
              <p className="text-gray-500 text-xs">Yesterday</p>
            </div>
            <div className="text-sm">
              <p className="text-gray-800">
                Michael downloaded Course Registration Guide.pdf
              </p>
              <p className="text-gray-500 text-xs">Just now</p>
            </div>
          </div>
        </div>
      </div>
    </div>;
};
export default Files;