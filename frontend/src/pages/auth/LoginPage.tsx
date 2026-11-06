import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { Input } from '../../components/common/Input';
import { Button } from '../../components/common/Button';
import { ShieldCheck, AlertCircle } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!email.trim() || !password) {
      setErrorMessage('Please provide both email and password');
      return;
    }

    try {
      setIsLoading(true);
      await login({ email: email.trim(), password });
      navigate('/dashboard');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Invalid email or password. Please verify credentials.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const fillCredentials = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setErrorMessage(null);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-emerald-600 text-white font-bold text-xl shadow-md mb-3">
          LL
        </div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">LedgerLine Core</h2>
        <p className="mt-1 text-sm text-slate-500">
          Financial transaction and double-entry ledger platform
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <div className="bg-white py-8 px-6 shadow-sm border border-slate-200/90 rounded-2xl sm:px-10">
          <div className="mb-6 pb-4 border-b border-slate-100 flex items-center justify-between">
            <div>
              <h3 className="font-semibold text-slate-800 text-lg">Sign In</h3>
              <p className="text-xs text-slate-500">Enter your authorized system credentials</p>
            </div>
            <ShieldCheck className="w-6 h-6 text-emerald-600" />
          </div>

          {errorMessage && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 rounded-lg flex items-start space-x-2.5">
              <AlertCircle className="w-4 h-4 text-rose-600 mt-0.5 flex-shrink-0" />
              <p className="text-xs text-rose-700 leading-relaxed">{errorMessage}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="relative">
              <Input
                label="Email Address"
                type="email"
                placeholder="you@domain.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="relative">
              <Input
                label="Password"
                type="password"
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <Button type="submit" className="w-full mt-2" isLoading={isLoading}>
              Authenticate & Access Platform
            </Button>
          </form>

          {/* Quick Demo Credentials */}
          <div className="mt-6 pt-5 border-t border-slate-100">
            <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-2">
              Default Seed Accounts (Day 1)
            </p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => fillCredentials('admin@ledgerline.com', 'Admin@12345')}
                className="p-2 text-left rounded-lg border border-slate-200 hover:border-emerald-300 hover:bg-emerald-50/40 transition"
              >
                <span className="font-medium text-slate-800 block">Administrator</span>
                <span className="text-[11px] text-slate-500">admin@ledgerline.com</span>
              </button>
              <button
                type="button"
                onClick={() => fillCredentials('auditor@ledgerline.com', 'Auditor@12345')}
                className="p-2 text-left rounded-lg border border-slate-200 hover:border-emerald-300 hover:bg-emerald-50/40 transition"
              >
                <span className="font-medium text-slate-800 block">Auditor</span>
                <span className="text-[11px] text-slate-500">auditor@ledgerline.com</span>
              </button>
            </div>
          </div>

          <div className="mt-6 text-center text-xs text-slate-500">
            Don't have an account?{' '}
            <Link to="/register" className="font-semibold text-emerald-600 hover:text-emerald-700 hover:underline">
              Create an Account
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
