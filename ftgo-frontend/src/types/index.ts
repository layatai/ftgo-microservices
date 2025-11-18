// Common types
export interface Address {
  street1: string;
  street2?: string;
  city: string;
  state: string;
  zip: string;
  country: string;
}

export interface Money {
  amount: number;
  currency: string;
}

// Customer types
export interface Customer {
  id: string;
  name: string;
  email: string;
  address: Address;
  paymentMethods?: PaymentMethod[];
}

export interface CreateCustomerRequest {
  name: string;
  email: string;
  address: Address;
}

export interface PaymentMethod {
  id: string;
  cardNumber: string;
  expiryMonth: number;
  expiryYear: number;
}

export interface AddPaymentMethodRequest {
  cardNumber: string;
  expiryMonth: number;
  expiryYear: number;
  cvv: string;
}

// Restaurant types
export interface Restaurant {
  id: string;
  name: string;
  address: string;
  menuItems?: MenuItem[];  // Direct property from backend
}

export interface MenuItem {
  id: string;
  name: string;
  price: string;      // Backend sends price as string
  currency: string;   // Backend sends currency separately
}

export interface CreateRestaurantRequest {
  name: string;
  address: string;
}

export interface UpdateMenuRequest {
  menuItems: Omit<MenuItem, 'id'>[];
}

// Order types
export interface Order {
  id: string;
  customerId: string;
  restaurantId: string;
  state: OrderState;
  orderTotal: string;      // Backend sends as string
  currency: string;       // Backend sends currency separately
  lineItems: OrderLineItem[];
  deliveryAddress: string;
  deliveryTime?: string;
  createdAt: string;
}

export type OrderState = 
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'PREPARING'
  | 'READY'
  | 'PICKED_UP'
  | 'DELIVERED';

export interface OrderLineItem {
  menuItemId: string;
  name: string;
  quantity: number;
  price: string;      // Backend expects string
  currency: string;  // Backend expects currency separately
}

export interface CreateOrderRequest {
  customerId: string;
  restaurantId: string;
  lineItems: OrderLineItem[];
  deliveryAddress: string;
  deliveryTime?: string;
}

// Cart types
export interface CartItem {
  menuItem: MenuItem;
  quantity: number;
}

// API Error
export interface ApiError {
  message: string;
  timestamp?: string;
}

