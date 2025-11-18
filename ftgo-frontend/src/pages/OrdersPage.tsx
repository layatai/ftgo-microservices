import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import type { Order } from '../types';
import { apiClient } from '../api/client';

export default function OrdersPage() {
  const { customer } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Note: This would require a "get orders by customer" endpoint
    // For now, we'll show a message that orders need to be tracked differently
    setLoading(false);
    setError('Order history feature requires backend endpoint: GET /api/orders?customerId={id}');
  }, [customer]);

  const getStateColor = (state: string) => {
    switch (state) {
      case 'APPROVED':
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
      case 'PREPARING':
      case 'READY':
      case 'PICKED_UP':
        return 'bg-yellow-100 text-yellow-800';
      case 'REJECTED':
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">{error}</p>
        <p className="text-sm text-gray-400 mt-2">
          You can view individual orders by navigating to them directly.
        </p>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg">No orders yet</p>
        <Link
          to="/restaurants"
          className="mt-4 inline-block text-primary-600 hover:text-primary-700"
        >
          Browse Restaurants
        </Link>
      </div>
    );
  }

  return (
    <div className="px-4 sm:px-6 lg:px-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">My Orders</h1>

      <div className="space-y-4">
        {orders.map((order) => (
          <Link
            key={order.id}
            to={`/orders/${order.id}`}
            className="block bg-white rounded-lg shadow hover:shadow-md transition-shadow p-6"
          >
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-semibold text-gray-900">Order #{order.id.slice(0, 8)}</h3>
                <p className="text-sm text-gray-500 mt-1">
                  {new Date(order.createdAt).toLocaleDateString()} at{' '}
                  {new Date(order.createdAt).toLocaleTimeString()}
                </p>
                <p className="text-lg font-bold text-gray-900 mt-2">
                  ${order.orderTotal.amount.toFixed(2)} {order.orderTotal.currency}
                </p>
              </div>
              <span
                className={`px-3 py-1 rounded-full text-sm font-medium ${getStateColor(order.state)}`}
              >
                {order.state}
              </span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

