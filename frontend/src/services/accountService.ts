import api from './api';
import { Account, ApiResponse, CreateAccountPayload } from '../types';

export const accountService = {
  async getAccounts(): Promise<Account[]> {
    const response = await api.get<ApiResponse<Account[]>>('/accounts');
    return response.data.data;
  },

  async getAccountById(id: number): Promise<Account> {
    const response = await api.get<ApiResponse<Account>>(`/accounts/${id}`);
    return response.data.data;
  },

  async createAccount(payload: CreateAccountPayload): Promise<Account> {
    const response = await api.post<ApiResponse<Account>>('/accounts', payload);
    return response.data.data;
  },
};
