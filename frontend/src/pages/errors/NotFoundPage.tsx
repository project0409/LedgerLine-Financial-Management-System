import React from 'react';
import { Link } from 'react-router-dom';
import { FileQuestion, ArrowLeft } from 'lucide-react';
import { Button } from '../../components/common/Button';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-2xl p-8 border border-slate-200/90 shadow-sm text-center">
        <div className="w-14 h-14 rounded-2xl bg-slate-100 text-slate-600 flex items-center justify-center mx-auto mb-4 border border-slate-200">
          <FileQuestion className="w-7 h-7" />
        </div>
        <h1 className="text-xl font-bold text-slate-900">404 – Resource Not Found</h1>
        <p className="text-xs text-slate-500 mt-2 leading-relaxed">
          The financial ledger route or entity you requested does not exist or has been relocated.
        </p>

        <div className="mt-6 pt-6 border-t border-slate-100 flex justify-center">
          <Link to="/dashboard">
            <Button size="sm" className="flex items-center space-x-2">
              <ArrowLeft className="w-4 h-4" />
              <span>Return to Dashboard</span>
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
};
