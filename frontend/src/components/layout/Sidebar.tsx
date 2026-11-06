import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  LayoutDashboard,
  Wallet,
  Users,
  ShieldAlert,
  FileText,
  UserCircle,
  HelpCircle,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { isAdmin, isAuditor } = useAuth();

  const navClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center space-x-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition ${
      isActive
        ? 'bg-emerald-50 text-emerald-800 border-l-4 border-emerald-600'
        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
    }`;

  return (
    <aside className="w-64 bg-white border-r border-slate-200 min-h-[calc(100vh-4rem)] flex flex-col justify-between p-4">
      <div className="space-y-6">
        <div>
          <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            Main Navigation
          </p>
          <nav className="space-y-1">
            <NavLink to="/dashboard" className={navClass}>
              <LayoutDashboard className="w-4 h-4" />
              <span>Dashboard</span>
            </NavLink>
            <NavLink to="/profile" className={navClass}>
              <UserCircle className="w-4 h-4" />
              <span>User Profile</span>
            </NavLink>
          </nav>
        </div>

        {/* Administration Section (Role-Gated) */}
        {isAdmin && (
          <div>
            <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-rose-500 mb-2">
              System Administration
            </p>
            <nav className="space-y-1">
              <div className="flex items-center space-x-3 px-3.5 py-2 rounded-lg text-xs font-semibold text-slate-700 bg-rose-50/50 border border-rose-100">
                <Users className="w-4 h-4 text-rose-600" />
                <span>User Directory</span>
              </div>
              <div className="flex items-center space-x-3 px-3.5 py-2 rounded-lg text-xs font-semibold text-slate-700 bg-rose-50/50 border border-rose-100 mt-1">
                <ShieldAlert className="w-4 h-4 text-rose-600" />
                <span>Role Governance</span>
              </div>
            </nav>
          </div>
        )}

        {/* Auditor Section */}
        {isAuditor && (
          <div>
            <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-blue-500 mb-2">
              Auditor Compliance
            </p>
            <nav className="space-y-1">
              <div className="flex items-center space-x-3 px-3.5 py-2 rounded-lg text-xs font-semibold text-slate-700 bg-blue-50/50 border border-blue-100">
                <FileText className="w-4 h-4 text-blue-600" />
                <span>Audit Trail (Read Only)</span>
              </div>
            </nav>
          </div>
        )}

        {/* Financial Modules Roadmap */}
        <div>
          <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            Ledger Modules (Upcoming)
          </p>
          <div className="space-y-1.5 px-3">
            <div className="flex items-center justify-between text-xs text-slate-400 py-1">
              <span className="flex items-center space-x-2">
                <Wallet className="w-3.5 h-3.5" />
                <span>Double-Entry Ledger</span>
              </span>
              <span className="text-[10px] bg-slate-100 px-1.5 py-0.5 rounded text-slate-500">Day 2</span>
            </div>
            <div className="flex items-center justify-between text-xs text-slate-400 py-1">
              <span className="flex items-center space-x-2">
                <FileText className="w-3.5 h-3.5" />
                <span>Transfers & Deposits</span>
              </span>
              <span className="text-[10px] bg-slate-100 px-1.5 py-0.5 rounded text-slate-500">Day 2</span>
            </div>
          </div>
        </div>
      </div>

      <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
        <div className="flex items-center space-x-2 text-xs font-medium text-slate-700 mb-1">
          <HelpCircle className="w-4 h-4 text-emerald-600" />
          <span>LedgerLine v1.0.0</span>
        </div>
        <p className="text-[11px] text-slate-500 leading-tight">
          Day 1 Foundation with Flyway migrations & strict zero-balance account opening.
        </p>
      </div>
    </aside>
  );
};
