import axios, { type AxiosInstance, type AxiosError } from 'axios';
import type {
  Customer,
  CreateCustomerRequest,
  Restaurant,
  CreateRestaurantRequest,
  UpdateMenuRequest,
  Order,
  CreateOrderRequest,
  PaymentMethod,
  AddPaymentMethodRequest,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor for logging
    this.client.interceptors.request.use(
      (config) => {
        console.log(`[API] ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<{ message?: string }>) => {
        const message = error.response?.data?.message || error.message || 'An error occurred';
        console.error(`[API Error] ${error.config?.method?.toUpperCase()} ${error.config?.url}:`, message);
        return Promise.reject(new Error(message));
      }
    );
  }

  // Customer API
  async createCustomer(data: CreateCustomerRequest): Promise<Customer> {
    const response = await this.client.post<Customer>('/customers', data);
    return response.data;
  }

  async login(email: string): Promise<Customer> {
    const response = await this.client.post<Customer>('/customers/login', { email });
    return response.data;
  }

  async getCustomer(customerId: string): Promise<Customer> {
    const response = await this.client.get<Customer>(`/customers/${customerId}`);
    return response.data;
  }

  async addPaymentMethod(customerId: string, data: AddPaymentMethodRequest): Promise<PaymentMethod> {
    const response = await this.client.post<PaymentMethod>(`/customers/${customerId}/payment-methods`, data);
    return response.data;
  }

  // Restaurant API
  async createRestaurant(data: CreateRestaurantRequest): Promise<Restaurant> {
    const response = await this.client.post<Restaurant>('/restaurants', data);
    return response.data;
  }

  async getRestaurant(restaurantId: string): Promise<Restaurant> {
    const response = await this.client.get<Restaurant>(`/restaurants/${restaurantId}`);
    return response.data;
  }

  async getAllRestaurants(): Promise<Restaurant[]> {
    const response = await this.client.get<Restaurant[]>('/restaurants');
    return response.data;
  }

  async updateMenu(restaurantId: string, data: UpdateMenuRequest): Promise<void> {
    await this.client.put(`/restaurants/${restaurantId}/menu`, data);
  }

  // Order API
  async createOrder(data: CreateOrderRequest, idempotencyKey?: string): Promise<Order> {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const response = await this.client.post<Order>('/orders', data, { headers });
    return response.data;
  }

  async getOrder(orderId: string): Promise<Order> {
    const response = await this.client.get<Order>(`/orders/${orderId}`);
    return response.data;
  }

  async cancelOrder(orderId: string, reason?: string): Promise<void> {
    const params = reason ? { reason } : {};
    await this.client.put(`/orders/${orderId}/cancel`, null, { params });
  }
}

export const apiClient = new ApiClient();

