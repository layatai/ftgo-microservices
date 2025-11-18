import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Restaurant } from '../types';
import { BuildingStorefrontIcon } from '@heroicons/react/24/outline';

export default function RestaurantsPage() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadRestaurants();
  }, []);

  const loadRestaurants = async () => {
    try {
      setLoading(true);
      // Note: This endpoint might need to be implemented in the backend
      // For now, we'll handle the case where it doesn't exist
      const data = await apiClient.getAllRestaurants();
      setRestaurants(data);
    } catch (err) {
      // If endpoint doesn't exist, show empty state with message
      setError('Restaurant listing not available. Please create restaurants via API.');
      console.error('Failed to load restaurants:', err);
    } finally {
      setLoading(false);
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
          You can create restaurants using the API or add a restaurant creation form.
        </p>
      </div>
    );
  }

  if (restaurants.length === 0) {
    return (
      <div className="text-center py-12">
        <BuildingStorefrontIcon className="mx-auto h-12 w-12 text-gray-400" />
        <h3 className="mt-2 text-sm font-medium text-gray-900">No restaurants</h3>
        <p className="mt-1 text-sm text-gray-500">Get started by creating a restaurant.</p>
      </div>
    );
  }

  return (
    <div className="px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Restaurants</h1>
        <p className="mt-2 text-sm text-gray-600">
          Choose from our selection of restaurants
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {restaurants.map((restaurant) => (
          <Link
            key={restaurant.id}
            to={`/restaurants/${restaurant.id}`}
            className="bg-white rounded-lg shadow hover:shadow-md transition-shadow duration-200 overflow-hidden"
          >
            <div className="p-6">
              <div className="flex items-center justify-between">
                <BuildingStorefrontIcon className="h-10 w-10 text-primary-600" />
                <span className="text-sm text-gray-500">View Menu</span>
              </div>
              <h3 className="mt-4 text-xl font-semibold text-gray-900">{restaurant.name}</h3>
              <p className="mt-2 text-sm text-gray-600">{restaurant.address}</p>
              {restaurant.menuItems && (
                <p className="mt-2 text-sm text-primary-600">
                  {restaurant.menuItems.length} items available
                </p>
              )}
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

