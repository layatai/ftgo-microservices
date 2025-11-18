import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { useCartStore } from '../store/useCartStore';
import { useAuthStore } from '../store/useAuthStore';
import type { Restaurant, MenuItem } from '../types';
import { PlusIcon, MinusIcon } from '@heroicons/react/24/outline';

export default function RestaurantDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { customer } = useAuthStore();
  const { addItem, items, restaurantId } = useCartStore();
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [quantities, setQuantities] = useState<Record<string, number>>({});

  useEffect(() => {
    if (id) {
      loadRestaurant();
    }
  }, [id]);

  const loadRestaurant = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      const data = await apiClient.getRestaurant(id);
      setRestaurant(data);
      
      // Initialize quantities from cart if this is the current restaurant
      if (restaurantId === id) {
        const cartQuantities: Record<string, number> = {};
        items.forEach(item => {
          cartQuantities[item.menuItem.id] = item.quantity;
        });
        setQuantities(cartQuantities);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load restaurant');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = (menuItem: MenuItem) => {
    if (!customer) {
      navigate('/register');
      return;
    }

    if (!id) return;

    const currentQuantity = quantities[menuItem.id] || 0;
    const newQuantity = currentQuantity + 1;
    
    setQuantities(prev => ({ ...prev, [menuItem.id]: newQuantity }));
    addItem(menuItem, id);
  };

  const handleRemoveFromCart = (menuItemId: string) => {
    const currentQuantity = quantities[menuItemId] || 0;
    if (currentQuantity > 0) {
      const newQuantity = currentQuantity - 1;
      setQuantities(prev => ({ ...prev, [menuItemId]: newQuantity }));
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (error || !restaurant) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">{error || 'Restaurant not found'}</p>
        <button
          onClick={() => navigate('/restaurants')}
          className="mt-4 text-primary-600 hover:text-primary-700"
        >
          Back to Restaurants
        </button>
      </div>
    );
  }

  return (
    <div className="px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <button
          onClick={() => navigate('/restaurants')}
          className="text-sm text-primary-600 hover:text-primary-700 mb-4"
        >
          ← Back to Restaurants
        </button>
        <h1 className="text-3xl font-bold text-gray-900">{restaurant.name}</h1>
        <p className="mt-2 text-gray-600">{restaurant.address}</p>
      </div>

      {!restaurant.menuItems || restaurant.menuItems.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500">No menu items available at this restaurant.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {restaurant.menuItems.map((item) => {
            const quantity = quantities[item.id] || 0;
            
            return (
              <div key={item.id} className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900">{item.name}</h3>
                <p className="mt-2 text-xl font-bold text-primary-600">
                  ${parseFloat(item.price).toFixed(2)} {item.currency}
                </p>
                
                <div className="mt-4 flex items-center justify-between">
                  {quantity > 0 ? (
                    <div className="flex items-center space-x-3">
                      <button
                        onClick={() => handleRemoveFromCart(item.id)}
                        className="p-1 rounded-full bg-gray-200 hover:bg-gray-300"
                      >
                        <MinusIcon className="h-5 w-5" />
                      </button>
                      <span className="text-lg font-medium">{quantity}</span>
                      <button
                        onClick={() => handleAddToCart(item)}
                        className="p-1 rounded-full bg-gray-200 hover:bg-gray-300"
                      >
                        <PlusIcon className="h-5 w-5" />
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => handleAddToCart(item)}
                      className="flex items-center space-x-2 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
                    >
                      <PlusIcon className="h-5 w-5" />
                      <span>Add to Cart</span>
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

