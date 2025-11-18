import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { useCartStore } from '../store/useCartStore';
import { useAuthStore } from '../store/useAuthStore';
import { PlusIcon, MinusIcon, TrashIcon } from '@heroicons/react/24/outline';

export default function CartPage() {
  const navigate = useNavigate();
  const { customer } = useAuthStore();
  const { items, restaurantId, updateQuantity, removeItem, clearCart, getTotal } = useCartStore();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCheckout = async () => {
    if (!customer || !restaurantId || items.length === 0) return;

    setLoading(true);
    setError(null);

    try {
      const idempotencyKey = `order-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      
      const order = await apiClient.createOrder(
        {
          customerId: customer.id,
          restaurantId,
          lineItems: items.map(item => ({
            menuItemId: item.menuItem.id,
            name: item.menuItem.name,
            quantity: item.quantity,
            price: item.menuItem.price,  // Backend expects string
            currency: item.menuItem.currency || 'USD',  // Default to USD if not provided
          })),
          deliveryAddress: `${customer.address.street1}, ${customer.address.city}, ${customer.address.state} ${customer.address.zip}`,
        },
        idempotencyKey
      );

      clearCart();
      navigate(`/orders/${order.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create order');
    } finally {
      setLoading(false);
    }
  };

  if (items.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500 text-lg">Your cart is empty</p>
        <button
          onClick={() => navigate('/restaurants')}
          className="mt-4 text-primary-600 hover:text-primary-700"
        >
          Browse Restaurants
        </button>
      </div>
    );
  }

  const total = getTotal();

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Shopping Cart</h1>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <div className="bg-white shadow rounded-lg overflow-hidden">
        <div className="divide-y divide-gray-200">
          {items.map((item) => (
            <div key={item.menuItem.id} className="p-6 flex items-center justify-between">
              <div className="flex-1">
                <h3 className="text-lg font-medium text-gray-900">{item.menuItem.name}</h3>
                <p className="mt-1 text-sm text-gray-500">
                  ${parseFloat(item.menuItem.price).toFixed(2)} {item.menuItem.currency} each
                </p>
              </div>

              <div className="flex items-center space-x-4">
                <div className="flex items-center space-x-3">
                  <button
                    onClick={() => updateQuantity(item.menuItem.id, item.quantity - 1)}
                    className="p-1 rounded-full bg-gray-200 hover:bg-gray-300"
                  >
                    <MinusIcon className="h-5 w-5" />
                  </button>
                  <span className="text-lg font-medium w-8 text-center">{item.quantity}</span>
                  <button
                    onClick={() => updateQuantity(item.menuItem.id, item.quantity + 1)}
                    className="p-1 rounded-full bg-gray-200 hover:bg-gray-300"
                  >
                    <PlusIcon className="h-5 w-5" />
                  </button>
                </div>

                <div className="text-right">
                  <p className="text-lg font-semibold text-gray-900">
                    ${(parseFloat(item.menuItem.price) * item.quantity).toFixed(2)}
                  </p>
                </div>

                <button
                  onClick={() => removeItem(item.menuItem.id)}
                  className="ml-4 p-2 text-red-600 hover:text-red-700"
                >
                  <TrashIcon className="h-5 w-5" />
                </button>
              </div>
            </div>
          ))}
        </div>

        <div className="bg-gray-50 px-6 py-4">
          <div className="flex justify-between items-center mb-4">
            <span className="text-lg font-medium text-gray-900">Total</span>
            <span className="text-2xl font-bold text-gray-900">
              ${total.toFixed(2)} USD
            </span>
          </div>
          
          <div className="flex space-x-4">
            <button
              onClick={() => navigate('/restaurants')}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Continue Shopping
            </button>
            <button
              onClick={handleCheckout}
              disabled={loading}
              className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"
            >
              {loading ? 'Placing Order...' : 'Place Order'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

