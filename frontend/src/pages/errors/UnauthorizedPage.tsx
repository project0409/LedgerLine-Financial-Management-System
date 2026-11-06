import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldX, ArrowLeft } from 'lucide-react';
import { Button } from '../../components/common/Button';

export const UnauthorizedPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-2xl p-8 border border-slate-200/90 shadow-sm text-center">
        <div className="w-14 h-14 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mx-auto mb-4 border border-rose-100">
          <ShieldX className="w-7 h-7" />
        </div>
        <h1 className="text-xl font-bold text-slate-900">403 – Access Denied</h1>
        <p className="text-xs text-slate-500 mt-2 leading-relaxed">
          Your authenticated principal identity does not possess the required role or account permission to inspect or execute operations on this resource.
        </p>

        <div className="mt-6 pt-6 border-t border-slate-100 flex justify-center">
          <Link to="/dashboard">
            <Button size="sm" className="flex items-center space-x-2">
              <ArrowLeft className="w-4 h-4" />
              <span>Return to Secure Dashboard</span>
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
};
