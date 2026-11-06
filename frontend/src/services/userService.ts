import api from './api';
import { ApiResponse, Role, User } from '../types';

export const userService = {
  async getProfile(): Promise<User> {
    const response = await api.get<ApiResponse<User>>('/users/me');
    return response.data.data;
  },

  async updateProfile(payload: { fullName: string; phoneNumber?: string }): Promise<User> {
    const response = await api.put<ApiResponse<User>>('/users/me', payload);
    return response.data.data;
  },

  async getAllUsers(): Promise<User[]> {
    const response = await api.get<ApiResponse<User[]>>('/admin/users');
    return response.data.data;
  },

  async getAllRoles(): Promise<Role[]> {
    const response = await api.get<ApiResponse<Role[]>>('/admin/roles');
    return response.data.data;
  },
};
