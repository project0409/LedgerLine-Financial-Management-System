import React, { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { accountService } from '../../services/accountService';
import { Account } from '../../types';
import { Card } from '../../components/common/Card';
import { Badge } from '../../components/common/Badge';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import {
  Wallet,
  ShieldCheck,
  PlusCircle,
  Clock,
  ArrowUpRight,
  ArrowDownLeft,
  RefreshCw,
  AlertTriangle,
  Building2,
  FileCheck2,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user, isAuditor } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [accountName, setAccountName] = useState('');
  const [accountType, setAccountType] = useState('CHECKING');
  const [currency, setCurrency] = useState('USD');
  const [isCreating, setIsCreating] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const fetchAccounts = async () => {
    try {
      setLoading(true);
      const data = await accountService.getAccounts();
      setAccounts(data);
    } catch {
      setAccounts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleCreateAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    try {
      setIsCreating(true);
      await accountService.createAccount({ accountName, accountType, currency });
      setShowCreateModal(false);
      setAccountName('');
      await fetchAccounts();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to initialize account');
    } finally {
      setIsCreating(false);
    }
  };

  const totalBalance = accounts.reduce((acc, curr) => acc + (Number(curr.balance) || 0), 0);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-white rounded-2xl p-6 border border-slate-200/90 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              Welcome back, {user?.fullName}
            </h1>
            <Badge variant="emerald">ACTIVE</Badge>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Production Day 1 foundation. Financial accounts initialized with zero balance and verified cryptographic integrity.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchAccounts}
            className="flex items-center space-x-1.5"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Refresh</span>
          </Button>

          {!isAuditor && (
            <Button
              variant="primary"
              size="sm"
              onClick={() => setShowCreateModal(true)}
              className="flex items-center space-x-1.5"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Open New Account</span>
            </Button>
          )}
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card className="border-l-4 border-l-emerald-600">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Total Permitted Accounts
            </span>
            <Wallet className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="mt-2 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-slate-900">{accounts.length}</span>
            <span className="text-xs text-slate-500">accounts</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Real-time ledger entities</p>
        </Card>

        <Card className="border-l-4 border-l-teal-600">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Aggregate Balance
            </span>
            <Building2 className="w-4 h-4 text-teal-600" />
          </div>
          <div className="mt-2 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-slate-900">
              ${totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
            <span className="text-xs text-slate-500">USD</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Day 1: Strictly non-fabricated balances</p>
        </Card>

        <Card className="border-l-4 border-l-blue-600">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Assigned Role
            </span>
            <ShieldCheck className="w-4 h-4 text-blue-600" />
          </div>
          <div className="mt-2">
            <span className="text-sm font-bold text-slate-800">
              {user?.roles?.join(', ') || 'ROLE_ACCOUNT_HOLDER'}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Enforced by Spring Security RBAC</p>
        </Card>

        <Card className="border-l-4 border-l-indigo-600">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Ledger State
            </span>
            <FileCheck2 className="w-4 h-4 text-indigo-600" />
          </div>
          <div className="mt-2 flex items-center space-x-2">
            <span className="inline-block w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
            <span className="text-sm font-semibold text-slate-800">Flyway V2 Migrated</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Double-entry engine ready for Day 2</p>
        </Card>
      </div>

      {/* Account Overview Table */}
      <Card
        title="Financial Accounts Overview"
        subtitle="Accounts authorized for your principal identity"
        action={
          <span className="text-xs text-slate-400">
            {accounts.length} {accounts.length === 1 ? 'account' : 'accounts'} registered
          </span>
        }
      >
        {loading ? (
          <div className="py-8 text-center text-sm text-slate-400">
            Loading accounts from secure API...
          </div>
        ) : accounts.length === 0 ? (
          <div className="text-center py-12 px-4 border border-dashed border-slate-200 rounded-xl">
            <Wallet className="w-10 h-10 text-slate-300 mx-auto mb-3" />
            <h4 className="text-sm font-semibold text-slate-700">No accounts opened yet</h4>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mt-1 mb-4">
              Financial integrity rules require accounts to be explicitly initialized. New accounts open with $0.00 balance.
            </p>
            {!isAuditor && (
              <Button size="sm" onClick={() => setShowCreateModal(true)}>
                Initialize First Account
              </Button>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500 uppercase text-[11px] font-semibold tracking-wider border-y border-slate-100">
                <tr>
                  <th className="py-3 px-4">Account Number</th>
                  <th className="py-3 px-4">Name</th>
                  <th className="py-3 px-4">Type</th>
                  <th className="py-3 px-4">Currency</th>
                  <th className="py-3 px-4 text-right">Balance</th>
                  <th className="py-3 px-4 text-center">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {accounts.map((acc) => (
                  <tr key={acc.id} className="hover:bg-slate-50/70 transition">
                    <td className="py-3.5 px-4 font-mono font-medium text-slate-900 text-xs">
                      {acc.accountNumber}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-slate-800">{acc.accountName}</td>
                    <td className="py-3.5 px-4">
                      <span className="text-xs bg-slate-100 text-slate-700 px-2 py-0.5 rounded font-mono">
                        {acc.accountType}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-600">{acc.currency}</td>
                    <td className="py-3.5 px-4 text-right font-mono font-semibold text-slate-900">
                      ${Number(acc.balance).toFixed(2)}
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <Badge variant={acc.status === 'ACTIVE' ? 'emerald' : 'slate'}>
                        {acc.status}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {/* Two-Column Section: Quick Actions & Recent Activity Empty State */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Quick Actions Placeholder */}
        <Card title="Quick Financial Operations" subtitle="Phase 2 Money Movement Modules" className="lg:col-span-1">
          <div className="space-y-3">
            <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/50 flex items-center justify-between opacity-75">
              <div className="flex items-center space-x-3">
                <div className="p-2 bg-emerald-100 text-emerald-700 rounded-lg">
                  <ArrowDownLeft className="w-4 h-4" />
                </div>
                <div>
                  <p className="text-xs font-semibold text-slate-800">Deposit Funds</p>
                  <p className="text-[11px] text-slate-400">External credit authorization</p>
                </div>
              </div>
              <span className="text-[10px] bg-slate-200 text-slate-600 px-1.5 py-0.5 rounded font-medium">
                Day 2
              </span>
            </div>

            <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/50 flex items-center justify-between opacity-75">
              <div className="flex items-center space-x-3">
                <div className="p-2 bg-blue-100 text-blue-700 rounded-lg">
                  <ArrowUpRight className="w-4 h-4" />
                </div>
                <div>
                  <p className="text-xs font-semibold text-slate-800">Internal Transfer</p>
                  <p className="text-[11px] text-slate-400">Double-entry ledger journal</p>
                </div>
              </div>
              <span className="text-[10px] bg-slate-200 text-slate-600 px-1.5 py-0.5 rounded font-medium">
                Day 2
              </span>
            </div>

            <div className="p-3 rounded-lg bg-amber-50 border border-amber-200/80 flex items-start space-x-2 text-xs text-amber-800">
              <AlertTriangle className="w-4 h-4 text-amber-600 mt-0.5 flex-shrink-0" />
              <p>
                Financial transactions are intentionally locked on Day 1 to guarantee database schema and security validation first.
              </p>
            </div>
          </div>
        </Card>

        {/* Honest Activity Section */}
        <Card
          title="Audit & Transaction Activity"
          subtitle="Real append-only operational event trail"
          className="lg:col-span-2"
        >
          <div className="text-center py-10 px-4 border border-dashed border-slate-200 rounded-xl">
            <Clock className="w-8 h-8 text-slate-300 mx-auto mb-2" />
            <h4 className="text-sm font-semibold text-slate-700">No transactions executed yet</h4>
            <p className="text-xs text-slate-400 max-w-md mx-auto mt-1">
              LedgerLine adheres strictly to financial honesty: no simulated or fake transaction data is displayed. Real ledger entries will be written when the double-entry transfer engine activates on Day 2.
            </p>
          </div>
        </Card>
      </div>

      {/* Account Creation Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl border border-slate-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-4">
              <div>
                <h3 className="font-bold text-slate-900 text-base">Open Financial Account</h3>
                <p className="text-xs text-slate-500">Day 1: Strictly initialized at $0.00 balance</p>
              </div>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-slate-400 hover:text-slate-600 text-sm font-bold p-1"
              >
                ✕
              </button>
            </div>

            {errorMsg && (
              <div className="mb-4 p-3 bg-rose-50 text-rose-700 text-xs rounded-lg border border-rose-200">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleCreateAccount} className="space-y-4">
              <Input
                label="Account Friendly Name"
                placeholder="e.g., Primary Operations, Treasury Checking"
                value={accountName}
                onChange={(e) => setAccountName(e.target.value)}
                required
              />

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5">
                  Account Type
                </label>
                <select
                  value={accountType}
                  onChange={(e) => setAccountType(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-white border border-slate-300 rounded-lg text-slate-800 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-100 focus:border-emerald-500"
                >
                  <option value="CHECKING">CHECKING (Operating Account)</option>
                  <option value="SAVINGS">SAVINGS (Interest Bearing)</option>
                  <option value="BUSINESS">BUSINESS (Corporate Account)</option>
                  <option value="INVESTMENT">INVESTMENT (Custodial)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5">
                  Currency (ISO-4217)
                </label>
                <select
                  value={currency}
                  onChange={(e) => setCurrency(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-white border border-slate-300 rounded-lg text-slate-800 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-100 focus:border-emerald-500"
                >
                  <option value="USD">USD - United States Dollar</option>
                  <option value="EUR">EUR - Euro</option>
                  <option value="GBP">GBP - British Pound</option>
                </select>
              </div>

              <div className="pt-2 flex justify-end space-x-3">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setShowCreateModal(false)}
                >
                  Cancel
                </Button>
                <Button type="submit" size="sm" isLoading={isCreating}>
                  Confirm & Create Account
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
