import api, { setAuthToken } from './api';
import { ApiResponse, AuthResponse, LoginPayload, RegisterPayload, User } from '../types';

export const authService = {
  async register(payload: RegisterPayload): Promise<ApiResponse<User>> {
    const response = await api.post<ApiResponse<User>>('/auth/register', payload);
    return response.data;
  },

  async login(payload: LoginPayload): Promise<ApiResponse<AuthResponse>> {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', payload);
    if (response.data.data?.token) {
      setAuthToken(response.data.data.token);
    }
    return response.data;
  },

  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout');
    } finally {
      setAuthToken(null);
    }
  },

  async getCurrentUser(): Promise<User> {
    const response = await api.get<ApiResponse<User>>('/auth/me');
    return response.data.data;
  },
};
