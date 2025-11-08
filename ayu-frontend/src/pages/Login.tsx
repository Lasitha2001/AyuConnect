import React, { useState } from 'react';
import Button from '../components/Button';
interface LoginProps {
  onLogin: () => void;
}
const Login: React.FC<LoginProps> = ({
  onLogin
}) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showRegister, setShowRegister] = useState(false);
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onLogin();
  };
  return <div className="flex h-screen bg-[#0d1b2a]">
      <div className="w-full md:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <div className="text-center mb-10">
            <h1 className="text-3xl font-bold text-[#00D4FF]">MyUni</h1>
            <p className="text-gray-400 mt-2">
              Welcome back! Please login to your account.
            </p>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label htmlFor="email" className="block text-gray-300 font-medium mb-2">
                Email Address
              </label>
              <input type="email" id="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com" className="w-full px-4 py-3 rounded-lg bg-[#1a2942] border border-[#2a3f5f] text-white placeholder-gray-500 focus:bg-[#0A1628] focus:outline-none focus:ring-2 focus:ring-[#00D4FF] focus:border-transparent" required />
            </div>
            <div className="mb-6">
              <label htmlFor="password" className="block text-gray-300 font-medium mb-2">
                Password
              </label>
              <input type="password" id="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" className="w-full px-4 py-3 rounded-lg bg-[#1a2942] border border-[#2a3f5f] text-white placeholder-gray-500 focus:bg-[#0A1628] focus:outline-none focus:ring-2 focus:ring-[#00D4FF] focus:border-transparent" required />
            </div>
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center">
                <input type="checkbox" id="remember" className="h-4 w-4 text-[#00D4FF] bg-[#1a2942] border-[#2a3f5f] rounded focus:ring-[#00D4FF]" />
                <label htmlFor="remember" className="ml-2 text-gray-300">
                  Remember me
                </label>
              </div>
              <a href="#" className="text-[#00D4FF] hover:text-[#00bce6] text-sm">
                Forgot Password?
              </a>
            </div>
            <Button type="submit" fullWidth>
              Login
            </Button>
          </form>
          <div className="text-center mt-8">
            <p className="text-gray-400">
              Don't have an account?{' '}
              <button onClick={() => setShowRegister(true)} className="text-[#00D4FF] hover:text-[#00bce6] font-medium">
                Register here
              </button>
            </p>
          </div>
        </div>
      </div>
      <div className="hidden md:flex md:w-1/2 bg-gradient-to-br from-[#00D4FF] via-[#0099cc] to-[#0A1628] p-12 text-white flex-col justify-center relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 right-20 w-64 h-64 bg-white rounded-full blur-3xl"></div>
          <div className="absolute bottom-20 left-20 w-96 h-96 bg-white rounded-full blur-3xl"></div>
        </div>
        <div className="relative z-10">
          <h2 className="text-3xl font-bold mb-4">Connect, Share, Succeed</h2>
          <p className="text-xl mb-6 text-gray-100">
            Your campus communication hub for notices, files, and real-time
            collaboration.
          </p>
          <div className="bg-white/10 rounded-lg p-6 backdrop-blur-sm border border-white/20">
            <p className="italic">
              "MyUni has transformed how our university communicates.
              It's now easier than ever to stay updated and connected."
            </p>
            <p className="mt-4 font-medium">- University Dean</p>
          </div>
        </div>
      </div>
      {showRegister && <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50 p-4">
          <div className="bg-[#0A1628] rounded-lg max-w-md w-full p-6 relative border border-[#1a2942]">
            <button onClick={() => setShowRegister(false)} className="absolute top-4 right-4 text-gray-400 hover:text-white">
              ✕
            </button>
            <h2 className="text-2xl font-bold mb-1 text-white">
              Create Your Account
            </h2>
            <p className="text-gray-400 mb-6">
              Join MyUni to stay updated with campus notices and
              communications.
            </p>
            <form>
              <div className="mb-4">
                <label htmlFor="fullName" className="block text-gray-300 font-medium mb-2">
                  Full Name
                </label>
                <input type="text" id="fullName" placeholder="John Doe" className="w-full px-4 py-3 rounded-lg bg-[#1a2942] border border-[#2a3f5f] text-white placeholder-gray-500 focus:bg-[#0A1628] focus:outline-none focus:ring-2 focus:ring-[#00D4FF]" required />
              </div>
              <div className="mb-4">
                <label htmlFor="regEmail" className="block text-gray-300 font-medium mb-2">
                  Email Address
                </label>
                <input type="email" id="regEmail" placeholder="you@example.com" className="w-full px-4 py-3 rounded-lg bg-[#1a2942] border border-[#2a3f5f] text-white placeholder-gray-500 focus:bg-[#0A1628] focus:outline-none focus:ring-2 focus:ring-[#00D4FF]" required />
              </div>
              <div className="mb-4">
                <label htmlFor="regPassword" className="block text-gray-300 font-medium mb-2">
                  Password
                </label>
                <input type="password" id="regPassword" placeholder="••••••••" className="w-full px-4 py-3 rounded-lg bg-[#1a2942] border border-[#2a3f5f] text-white placeholder-gray-500 focus:bg-[#0A1628] focus:outline-none focus:ring-2 focus:ring-[#00D4FF]" required />
              </div>
              <div className="mb-6">
                <label htmlFor="confirmPassword" className="block text-gray-300 font-medium mb-2">
                  Confirm Password
                </label>
                <input type="password" id="confirmPassword" placeholder="••••••••" className="w-full px-4 py-3 rounded-lg bg-[#1a2942] border border-[#2a3f5f] text-white placeholder-gray-500 focus:bg-[#0A1628] focus:outline-none focus:ring-2 focus:ring-[#00D4FF]" required />
              </div>
              <Button type="submit" fullWidth>
                Create Account
              </Button>
            </form>
          </div>
        </div>}
    </div>;
};
export default Login;