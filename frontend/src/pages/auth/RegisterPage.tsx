import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { RegistrationAutoLoginError } from '../../context/AuthContext';
import { Input } from '../../components/common/Input';
import { Button } from '../../components/common/Button';
import { UserPlus, Shield, AlertCircle, CheckCircle2, ArrowRight } from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successNotice, setSuccessNotice] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isLoading) return; // Prevent repeated submissions

    setErrorMessage(null);
    setSuccessNotice(null);
    setValidationErrors({});

    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match');
      return;
    }

    if (password.length < 8) {
      setErrorMessage('Password must be at least 8 characters long');
      return;
    }

    try {
      setIsLoading(true);
      await register({
        fullName: fullName.trim(),
        email: email.trim().toLowerCase(),
        phoneNumber: phoneNumber.trim() || undefined,
        password,
        confirmPassword,
      });
      navigate('/dashboard');
    } catch (err: any) {
      if (err instanceof RegistrationAutoLoginError || err.name === 'RegistrationAutoLoginError') {
        setSuccessNotice('Registration succeeded! Your account is created, but automatic sign-in could not be completed. Please sign in below.');
        return;
      }

      if (!err.response) {
        setErrorMessage('Backend service is currently unavailable. Please verify the server is running at http://localhost:8080.');
        return;
      }

      const status = err.response.status;
      const data = err.response.data;

      if (status === 409) {
        setErrorMessage(data?.message || 'This email address is already registered. Please sign in.');
      } else if (data?.validationErrors && Object.keys(data.validationErrors).length > 0) {
        setValidationErrors(data.validationErrors);
        setErrorMessage(data.message || 'Please correct the validation errors below.');
      } else if (status === 500) {
        setErrorMessage('An unexpected server error occurred. Please try again later.');
      } else {
        setErrorMessage(data?.message || 'Registration failed. Please check your information and try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-emerald-600 text-white font-bold text-xl shadow-md mb-3">
          LL
        </div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Open Your Account</h2>
        <p className="mt-1 text-sm text-slate-500">
          Join the LedgerLine financial ecosystem
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <div className="bg-white py-8 px-6 shadow-sm border border-slate-200/90 rounded-2xl sm:px-10">
          <div className="mb-6 pb-4 border-b border-slate-100 flex items-center justify-between">
            <div>
              <h3 className="font-semibold text-slate-800 text-lg">Registration</h3>
              <p className="text-xs text-slate-500">Assigned role: ACCOUNT_HOLDER</p>
            </div>
            <UserPlus className="w-6 h-6 text-emerald-600" />
          </div>

          <div className="mb-5 p-3 bg-emerald-50/60 border border-emerald-100 rounded-lg flex items-center space-x-2 text-xs text-emerald-800">
            <Shield className="w-4 h-4 text-emerald-600 flex-shrink-0" />
            <span>Public registration automatically assigns non-administrative ROLE_ACCOUNT_HOLDER.</span>
          </div>

          {/* Success notice if registration succeeded but auto-login failed */}
          {successNotice && (
            <div className="mb-5 p-4 bg-emerald-50 border border-emerald-200 rounded-xl space-y-2">
              <div className="flex items-start space-x-2.5">
                <CheckCircle2 className="w-5 h-5 text-emerald-600 mt-0.5 flex-shrink-0" />
                <div>
                  <h4 className="text-sm font-semibold text-emerald-900">Registration Successful!</h4>
                  <p className="text-xs text-emerald-700 mt-0.5">{successNotice}</p>
                </div>
              </div>
              <div className="pt-2 flex justify-end">
                <Link to="/login">
                  <Button size="sm" className="flex items-center space-x-1.5">
                    <span>Proceed to Sign In</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Button>
                </Link>
              </div>
            </div>
          )}

          {errorMessage && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 rounded-lg flex items-start space-x-2.5">
              <AlertCircle className="w-4 h-4 text-rose-600 mt-0.5 flex-shrink-0" />
              <p className="text-xs text-rose-700 leading-relaxed">{errorMessage}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Legal Full Name"
              type="text"
              placeholder="Alexander Wright"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              error={validationErrors.fullName}
              disabled={isLoading}
              required
            />

            <Input
              label="Email Address"
              type="email"
              placeholder="alexander@domain.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={validationErrors.email}
              disabled={isLoading}
              required
            />

            <Input
              label="Contact Phone (Optional)"
              type="tel"
              placeholder="+1-555-0123"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              error={validationErrors.phoneNumber}
              disabled={isLoading}
            />

            <Input
              label="Password (min 8 chars)"
              type="password"
              placeholder="••••••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={validationErrors.password}
              disabled={isLoading}
              required
            />

            <Input
              label="Confirm Password"
              type="password"
              placeholder="••••••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              error={validationErrors.confirmPassword}
              disabled={isLoading}
              required
            />

            <Button
              type="submit"
              className="w-full mt-3"
              isLoading={isLoading}
              disabled={isLoading}
            >
              Complete Registration & Sign In
            </Button>
          </form>

          <div className="mt-6 text-center text-xs text-slate-500">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-emerald-600 hover:text-emerald-700 hover:underline">
              Sign In
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
