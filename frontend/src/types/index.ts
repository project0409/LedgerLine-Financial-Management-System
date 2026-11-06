export interface User {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string;
  enabled: boolean;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface Account {
  id: number;
  accountNumber: string;
  accountName: string;
  accountType: 'CHECKING' | 'SAVINGS' | 'BUSINESS' | 'INVESTMENT';
  currency: string;
  balance: number;
  status: 'ACTIVE' | 'FROZEN' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: number;
  roleName: string;
  description: string;
  createdAt: string;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  phoneNumber?: string;
  password: string;
  confirmPassword: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface CreateAccountPayload {
  accountName: string;
  accountType: string;
  currency: string;
}
