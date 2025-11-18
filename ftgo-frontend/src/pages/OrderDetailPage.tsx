import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Order } from '../types';

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    if (id) {
      loadOrder();
      // Poll for order updates
      const interval = setInterval(() => {
        loadOrder();
      }, 5000); // Poll every 5 seconds

      return () => clearInterval(interval);
    }
  }, [id]);

  const loadOrder = async () => {
    if (!id) return;

    try {
      const data = await apiClient.getOrder(id);
      setOrder(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load order');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!id || !order) return;

    if (!confirm('Are you sure you want to cancel this order?')) {
      return;
    }

    setCancelling(true);
    try {
      await apiClient.cancelOrder(id, 'Customer requested cancellation');
      await loadOrder(); // Reload to get updated state
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to cancel order');
    } finally {
      setCancelling(false);
    }
  };

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

  if (error || !order) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">{error || 'Order not found'}</p>
        <button
          onClick={() => navigate('/orders')}
          className="mt-4 text-primary-600 hover:text-primary-700"
        >
          Back to Orders
        </button>
      </div>
    );
  }

  const canCancel = order.state === 'PENDING' || order.state === 'APPROVED';

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <button
          onClick={() => navigate('/orders')}
          className="text-sm text-primary-600 hover:text-primary-700 mb-4"
        >
          ← Back to Orders
        </button>
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Order Details</h1>
            <p className="mt-2 text-sm text-gray-500">Order ID: {order.id}</p>
          </div>
          <span
            className={`px-4 py-2 rounded-full text-sm font-medium ${getStateColor(order.state)}`}
          >
            {order.state}
          </span>
        </div>
      </div>

      <div className="bg-white shadow rounded-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">Order Information</h2>
        </div>

        <div className="px-6 py-4 space-y-4">
          <div>
            <p className="text-sm text-gray-500">Order Date</p>
            <p className="text-base text-gray-900">
              {new Date(order.createdAt).toLocaleString()}
            </p>
          </div>

          <div>
            <p className="text-sm text-gray-500">Delivery Address</p>
            <p className="text-base text-gray-900">{order.deliveryAddress}</p>
          </div>

          {order.deliveryTime && (
            <div>
              <p className="text-sm text-gray-500">Scheduled Delivery</p>
              <p className="text-base text-gray-900">
                {new Date(order.deliveryTime).toLocaleString()}
              </p>
            </div>
          )}
        </div>

        <div className="px-6 py-4 border-t border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Items</h3>
          <div className="space-y-3">
            {order.lineItems.map((item, index) => (
              <div key={index} className="flex justify-between items-center">
                <div>
                  <p className="text-base font-medium text-gray-900">{item.name}</p>
                  <p className="text-sm text-gray-500">
                    ${parseFloat(item.price).toFixed(2)} {item.currency} × {item.quantity}
                  </p>
                </div>
                <p className="text-base font-semibold text-gray-900">
                  ${(parseFloat(item.price) * item.quantity).toFixed(2)}
                </p>
              </div>
            ))}
          </div>
        </div>

        <div className="px-6 py-4 bg-gray-50 border-t border-gray-200">
          <div className="flex justify-between items-center">
            <span className="text-lg font-medium text-gray-900">Total</span>
            <span className="text-2xl font-bold text-gray-900">
              ${parseFloat(order.orderTotal).toFixed(2)} {order.currency}
            </span>
          </div>
        </div>

        {canCancel && (
          <div className="px-6 py-4 border-t border-gray-200">
            <button
              onClick={handleCancel}
              disabled={cancelling}
              className="w-full px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
            >
              {cancelling ? 'Cancelling...' : 'Cancel Order'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

