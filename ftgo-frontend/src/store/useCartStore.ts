import { create } from 'zustand';
import type { CartItem, MenuItem } from '../types';

interface CartState {
  items: CartItem[];
  restaurantId: string | null;
  addItem: (menuItem: MenuItem, restaurantId: string) => void;
  removeItem: (menuItemId: string) => void;
  updateQuantity: (menuItemId: string, quantity: number) => void;
  clearCart: () => void;
  getTotal: () => number;
  getItemCount: () => number;
}

export const useCartStore = create<CartState>((set, get) => ({
  items: [],
  restaurantId: null,
  
  addItem: (menuItem, restaurantId) => {
    const state = get();
    
    // If adding from a different restaurant, clear cart
    if (state.restaurantId && state.restaurantId !== restaurantId) {
      set({ items: [{ menuItem, quantity: 1 }], restaurantId });
      return;
    }
    
    // Check if item already exists
    const existingItem = state.items.find(item => item.menuItem.id === menuItem.id);
    
    if (existingItem) {
      set({
        items: state.items.map(item =>
          item.menuItem.id === menuItem.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        ),
        restaurantId,
      });
    } else {
      set({
        items: [...state.items, { menuItem, quantity: 1 }],
        restaurantId,
      });
    }
  },
  
  removeItem: (menuItemId) => {
    set((state) => ({
      items: state.items.filter(item => item.menuItem.id !== menuItemId),
    }));
  },
  
  updateQuantity: (menuItemId, quantity) => {
    if (quantity <= 0) {
      get().removeItem(menuItemId);
      return;
    }
    
    set((state) => ({
      items: state.items.map(item =>
        item.menuItem.id === menuItemId
          ? { ...item, quantity }
          : item
      ),
    }));
  },
  
  clearCart: () => {
    set({ items: [], restaurantId: null });
  },
  
  getTotal: () => {
    return get().items.reduce((total, item) => {
      return total + (parseFloat(item.menuItem.price) * item.quantity);
    }, 0);
  },
  
  getItemCount: () => {
    return get().items.reduce((count, item) => count + item.quantity, 0);
  },
}));

