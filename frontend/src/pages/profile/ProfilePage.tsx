import React, { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { userService } from '../../services/userService';
import { Card } from '../../components/common/Card';
import { Input } from '../../components/common/Input';
import { Button } from '../../components/common/Button';
import { Badge } from '../../components/common/Badge';
import { User, Shield, Calendar, CheckCircle2, AlertCircle } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user, refreshUser } = useAuth();

  const [fullName, setFullName] = useState(user?.fullName || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '');
  const [isLoading, setIsLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMsg(null);
    setErrorMsg(null);

    try {
      setIsLoading(true);
      await userService.updateProfile({ fullName, phoneNumber });
      setSuccessMsg('Profile updated successfully');
      await refreshUser();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="bg-white rounded-2xl p-6 border border-slate-200/90 shadow-sm flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <div className="w-14 h-14 rounded-2xl bg-emerald-600 text-white flex items-center justify-center font-bold text-xl shadow-sm">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-900">{user?.fullName}</h1>
            <p className="text-xs text-slate-500">{user?.email}</p>
            <div className="flex items-center space-x-2 mt-1.5">
              {user?.roles?.map((role) => (
                <Badge key={role} variant="emerald">
                  {role}
                </Badge>
              ))}
              <Badge variant="slate">{user?.enabled ? 'Active Account' : 'Disabled'}</Badge>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card title="Edit Contact Details" subtitle="Update legal name & phone" className="md:col-span-2">
          {successMsg && (
            <div className="mb-4 p-3 bg-emerald-50 text-emerald-800 text-xs rounded-lg border border-emerald-200 flex items-center space-x-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
              <span>{successMsg}</span>
            </div>
          )}

          {errorMsg && (
            <div className="mb-4 p-3 bg-rose-50 text-rose-800 text-xs rounded-lg border border-rose-200 flex items-center space-x-2">
              <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          <form onSubmit={handleUpdate} className="space-y-4">
            <Input
              label="Legal Full Name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
            />

            <Input
              label="Contact Phone"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              placeholder="+1-555-0123"
            />

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">
                Primary Email Address (Immutable)
              </label>
              <input
                disabled
                value={user?.email || ''}
                className="w-full px-3.5 py-2.5 bg-slate-100 border border-slate-200 rounded-lg text-slate-500 text-sm cursor-not-allowed"
              />
              <p className="text-[11px] text-slate-400 mt-1">
                Primary email handle cannot be modified directly to prevent identity hijacking.
              </p>
            </div>

            <Button type="submit" size="sm" isLoading={isLoading} className="mt-2">
              Save Profile Changes
            </Button>
          </form>
        </Card>

        <Card title="Security Credentials" subtitle="Cryptographic security overview">
          <div className="space-y-4 text-xs">
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <Shield className="w-3.5 h-3.5 text-emerald-600" />
                <span>Password Protection</span>
              </div>
              <p className="text-slate-500 text-[11px]">
                Salted & hashed with BCrypt 10 rounds. Passwords are never stored in plain text.
              </p>
            </div>

            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <div className="flex items-center space-x-2 text-slate-800 font-semibold mb-1">
                <Shield className="w-3.5 h-3.5 text-blue-600" />
                <span>Token Transport</span>
              </div>
              <p className="text-slate-500 text-[11px]">
                HMAC-SHA256 JWT issued via HttpOnly, SameSite=Lax cookie and in-memory authorization header.
              </p>
            </div>

            <div className="pt-2 border-t border-slate-100 space-y-2 text-slate-600">
              <div className="flex items-center justify-between">
                <span className="flex items-center space-x-1.5 text-slate-400">
                  <Calendar className="w-3.5 h-3.5" />
                  <span>Member Since</span>
                </span>
                <span className="font-mono text-slate-800">
                  {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
                </span>
              </div>

              <div className="flex items-center justify-between">
                <span className="flex items-center space-x-1.5 text-slate-400">
                  <User className="w-3.5 h-3.5" />
                  <span>Internal User ID</span>
                </span>
                <span className="font-mono text-slate-800">#{user?.id}</span>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};
