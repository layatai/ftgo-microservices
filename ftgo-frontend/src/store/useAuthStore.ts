import { create } from 'zustand';
import type { Customer } from '../types';

interface AuthState {
  customer: Customer | null;
  setCustomer: (customer: Customer | null) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

export const useAuthStore = create<AuthState>((set) => ({
  customer: null,
  isAuthenticated: false,
  setCustomer: (customer) => set({ 
    customer, 
    isAuthenticated: customer !== null 
  }),
  logout: () => set({ 
    customer: null, 
    isAuthenticated: false 
  }),
}));

