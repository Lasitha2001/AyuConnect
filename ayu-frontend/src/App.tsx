import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './contexts/ThemeContext';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Notices from './pages/Notices';
import Chat from './pages/Chat';
import Files from './pages/Files';
import Profile from './pages/Profile';
import Layout from './components/Layout';
export function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const handleLogin = () => {
    setIsAuthenticated(true);
  };
  return <ThemeProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={!isAuthenticated ? <Login onLogin={handleLogin} /> : <Navigate to="/dashboard" />} />
          <Route path="/" element={isAuthenticated ? <Layout /> : <Navigate to="/login" />}>
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="notices" element={<Notices />} />
            <Route path="chat" element={<Chat />} />
            <Route path="files" element={<Files />} />
            <Route path="profile" element={<Profile />} />
            <Route path="/" element={<Navigate to="/dashboard" />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>;
}