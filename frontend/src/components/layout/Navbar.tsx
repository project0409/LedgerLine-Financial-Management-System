import React from 'react';
import { useAuth } from '../../hooks/useAuth';
import { Badge } from '../common/Badge';
import { Shield, Bell, User, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const { user, logout, isAdmin, isAuditor } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200 sticky top-0 z-30 flex items-center justify-between px-6">
      <div className="flex items-center space-x-3">
        <div className="w-9 h-9 rounded-lg bg-emerald-600 flex items-center justify-center text-white font-bold tracking-wider shadow-sm">
          LL
        </div>
        <div>
          <span className="font-bold text-slate-900 tracking-tight text-lg">LedgerLine</span>
          <span className="text-xs text-emerald-600 font-medium ml-2 px-1.5 py-0.5 bg-emerald-50 rounded">CORE</span>
        </div>
      </div>

      <div className="flex items-center space-x-4">
        {/* Security / Role pill */}
        <div className="hidden sm:flex items-center space-x-2">
          {isAdmin && <Badge variant="rose">SYSTEM ADMIN</Badge>}
          {isAuditor && <Badge variant="blue">AUDITOR</Badge>}
          {!isAdmin && !isAuditor && <Badge variant="emerald">ACCOUNT HOLDER</Badge>}
        </div>

        <div className="flex items-center text-xs text-slate-500 bg-slate-100 px-3 py-1.5 rounded-md border border-slate-200">
          <Shield className="w-3.5 h-3.5 text-emerald-600 mr-1.5" />
          <span>Security: Active (JWT)</span>
        </div>

        {/* Notifications placeholder */}
        <button
          title="Notifications"
          className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition"
        >
          <Bell className="w-4 h-4" />
        </button>

        {/* Profile Pill */}
        <div
          onClick={() => navigate('/profile')}
          className="flex items-center space-x-2.5 pl-2 cursor-pointer hover:opacity-80 transition"
        >
          <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-700 flex items-center justify-center text-xs font-semibold">
            <User className="w-4 h-4" />
          </div>
          <div className="hidden md:block text-left">
            <p className="text-xs font-semibold text-slate-800 leading-none">{user?.fullName}</p>
            <p className="text-[11px] text-slate-400 leading-none mt-1">{user?.email}</p>
          </div>
        </div>

        {/* Logout */}
        <button
          onClick={handleLogout}
          title="Sign out"
          className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
};
