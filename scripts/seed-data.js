#!/usr/bin/env node

/**
 * Seed script to initialize test data for FTGO microservices
 * 
 * Usage:
 *   node scripts/seed-data.js                    # Seed everything (restaurants, menus, customers)
 *   node scripts/seed-data.js --menus-only        # Seed menus only (for existing restaurants)
 *   node scripts/seed-data.js --orders-only       # Seed orders only (for testing order flow)
 *   node scripts/seed-data.js --api-url http://localhost:8080/api
 */

const axios = require('axios');

const API_BASE_URL = process.env.API_BASE_URL || process.argv.find(arg => arg.startsWith('--api-url'))?.split('=')[1] || 'http://localhost:8080/api';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Test data
const restaurants = [
  {
    name: 'Pizza Palace',
    address: '123 Main St, San Francisco, CA 94102',
    menuItems: [
      { name: 'Margherita Pizza', price: '15.99', currency: 'USD' },
      { name: 'Pepperoni Pizza', price: '17.99', currency: 'USD' },
      { name: 'Hawaiian Pizza', price: '18.99', currency: 'USD' },
      { name: 'Supreme Pizza', price: '20.99', currency: 'USD' },
      { name: 'Caesar Salad', price: '12.99', currency: 'USD' },
      { name: 'Garlic Bread', price: '6.99', currency: 'USD' },
      { name: 'Coca Cola', price: '2.99', currency: 'USD' },
    ],
  },
  {
    name: 'Burger House',
    address: '456 Market St, San Francisco, CA 94103',
    menuItems: [
      { name: 'Classic Burger', price: '12.99', currency: 'USD' },
      { name: 'Cheeseburger', price: '13.99', currency: 'USD' },
      { name: 'Bacon Burger', price: '15.99', currency: 'USD' },
      { name: 'Veggie Burger', price: '11.99', currency: 'USD' },
      { name: 'French Fries', price: '4.99', currency: 'USD' },
      { name: 'Onion Rings', price: '5.99', currency: 'USD' },
      { name: 'Milkshake', price: '6.99', currency: 'USD' },
    ],
  },
  {
    name: 'Sushi Express',
    address: '789 Mission St, San Francisco, CA 94105',
    menuItems: [
      { name: 'Salmon Roll', price: '8.99', currency: 'USD' },
      { name: 'Tuna Roll', price: '9.99', currency: 'USD' },
      { name: 'California Roll', price: '7.99', currency: 'USD' },
      { name: 'Dragon Roll', price: '12.99', currency: 'USD' },
      { name: 'Miso Soup', price: '4.99', currency: 'USD' },
      { name: 'Edamame', price: '5.99', currency: 'USD' },
      { name: 'Green Tea', price: '2.99', currency: 'USD' },
    ],
  },
  {
    name: 'Taco Fiesta',
    address: '321 Castro St, San Francisco, CA 94114',
    menuItems: [
      { name: 'Beef Taco', price: '4.99', currency: 'USD' },
      { name: 'Chicken Taco', price: '4.99', currency: 'USD' },
      { name: 'Fish Taco', price: '5.99', currency: 'USD' },
      { name: 'Veggie Taco', price: '3.99', currency: 'USD' },
      { name: 'Nachos', price: '8.99', currency: 'USD' },
      { name: 'Guacamole', price: '6.99', currency: 'USD' },
      { name: 'Horchata', price: '3.99', currency: 'USD' },
    ],
  },
  {
    name: 'Pasta Paradise',
    address: '555 Union St, San Francisco, CA 94133',
    menuItems: [
      { name: 'Spaghetti Carbonara', price: '16.99', currency: 'USD' },
      { name: 'Fettuccine Alfredo', price: '15.99', currency: 'USD' },
      { name: 'Penne Arrabbiata', price: '14.99', currency: 'USD' },
      { name: 'Lasagna', price: '18.99', currency: 'USD' },
      { name: 'Caesar Salad', price: '11.99', currency: 'USD' },
      { name: 'Garlic Bread', price: '5.99', currency: 'USD' },
      { name: 'Tiramisu', price: '7.99', currency: 'USD' },
    ],
  },
];

const customers = [
  {
    name: 'John Doe',
    email: 'john.doe@example.com',
    address: {
      street1: '100 First St',
      street2: 'Apt 5B',
      city: 'San Francisco',
      state: 'CA',
      zip: '94102',
      country: 'USA',
    },
  },
  {
    name: 'Jane Smith',
    email: 'jane.smith@example.com',
    address: {
      street1: '200 Second St',
      street2: '',
      city: 'San Francisco',
      state: 'CA',
      zip: '94103',
      country: 'USA',
    },
  },
  {
    name: 'Bob Johnson',
    email: 'bob.johnson@example.com',
    address: {
      street1: '300 Third St',
      street2: 'Suite 200',
      city: 'San Francisco',
      state: 'CA',
      zip: '94104',
      country: 'USA',
    },
  },
];

async function createRestaurant(restaurantData) {
  try {
    console.log(`Creating restaurant: ${restaurantData.name}...`);
    const response = await client.post('/restaurants', {
      name: restaurantData.name,
      address: restaurantData.address,
    });
    const restaurant = response.data;
    console.log(`✓ Created restaurant: ${restaurant.name} (ID: ${restaurant.id})`);

    // Update menu
    if (restaurantData.menuItems && restaurantData.menuItems.length > 0) {
      console.log(`  Adding ${restaurantData.menuItems.length} menu items...`);
      try {
        await client.put(`/restaurants/${restaurant.id}/menu`, {
          menuItems: restaurantData.menuItems,
        });
        console.log(`  ✓ Menu updated with ${restaurantData.menuItems.length} items`);
      } catch (menuError) {
        console.error(`  ✗ Failed to update menu:`, menuError.response?.data?.message || menuError.message);
        // Continue even if menu update fails
      }
    }

    return restaurant;
  } catch (error) {
    console.error(`✗ Failed to create restaurant ${restaurantData.name}:`, error.response?.data?.message || error.message);
    throw error;
  }
}

async function createCustomer(customerData) {
  try {
    console.log(`Creating customer: ${customerData.name}...`);
    const response = await client.post('/customers', customerData);
    const customer = response.data;
    console.log(`✓ Created customer: ${customer.name} (ID: ${customer.id})`);
    return customer;
  } catch (error) {
    console.error(`✗ Failed to create customer ${customerData.name}:`, error.response?.data?.message || error.message);
    throw error;
  }
}

async function updateMenuForRestaurant(restaurantId, menuItems) {
  try {
    console.log(`Updating menu for restaurant: ${restaurantId}...`);
    await client.put(`/restaurants/${restaurantId}/menu`, {
      menuItems: menuItems,
    });
    console.log(`✓ Menu updated with ${menuItems.length} items`);
    return true;
  } catch (error) {
    console.error(`✗ Failed to update menu:`, error.response?.data?.message || error.message);
    return false;
  }
}

async function seedMenusOnly() {
  console.log('🍽️  Seeding menus for existing restaurants...\n');
  console.log(`API Base URL: ${API_BASE_URL}\n`);

  try {
    // Get all restaurants
    console.log('Fetching existing restaurants...');
    const response = await client.get('/restaurants');
    const existingRestaurants = response.data;
    
    if (existingRestaurants.length === 0) {
      console.log('No restaurants found. Please run the full seed script first.');
      return;
    }

    console.log(`Found ${existingRestaurants.length} restaurants\n`);

    // Match restaurants by name and update menus
    let updated = 0;
    for (const existingRestaurant of existingRestaurants) {
      const restaurantData = restaurants.find(r => r.name === existingRestaurant.name);
      if (restaurantData && restaurantData.menuItems) {
        const success = await updateMenuForRestaurant(existingRestaurant.id, restaurantData.menuItems);
        if (success) updated++;
      }
    }

    console.log(`\n✅ Updated menus for ${updated}/${existingRestaurants.length} restaurants`);
  } catch (error) {
    console.error('\n❌ Error seeding menus:', error.response?.data?.message || error.message);
    process.exit(1);
  }
}

async function getRestaurantWithMenu(restaurantId) {
  try {
    const response = await client.get(`/restaurants/${restaurantId}`);
    return response.data;
  } catch (error) {
    console.error(`Failed to get restaurant ${restaurantId}:`, error.response?.data?.message || error.message);
    return null;
  }
}

async function createOrder(customerId, restaurantId, restaurant, numItems = 2) {
  try {
    // Get restaurant menu if not provided or has no menu items
    if (!restaurant || !restaurant.menuItems || restaurant.menuItems.length === 0) {
      restaurant = await getRestaurantWithMenu(restaurantId);
      if (!restaurant || !restaurant.menuItems || restaurant.menuItems.length === 0) {
        console.log(`  ⚠️  Restaurant ${restaurantId} has no menu items, skipping order`);
        return null;
      }
    }

    // Select menu items (use first few items)
    const menuItems = restaurant.menuItems;
    const selectedItems = [];
    const itemCount = Math.min(numItems, menuItems.length);
    
    for (let i = 0; i < itemCount; i++) {
      const menuItem = menuItems[i];
      selectedItems.push({
        menuItemId: menuItem.id,
        name: menuItem.name,
        quantity: Math.floor(Math.random() * 3) + 1, // 1-3 items
        price: menuItem.price.amount.toString(),
        currency: menuItem.price.currency,
      });
    }

    // Get customer for delivery address
    const customerResponse = await client.get(`/customers/${customerId}`);
    const customer = customerResponse.data;
    const deliveryAddress = `${customer.address.street1}${customer.address.street2 ? ', ' + customer.address.street2 : ''}, ${customer.address.city}, ${customer.address.state} ${customer.address.zip}`;

    // Set delivery time to 1 hour from now
    const deliveryTime = new Date(Date.now() + 60 * 60 * 1000).toISOString();

    // Create order with idempotency key
    const idempotencyKey = `order-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    const orderResponse = await client.post('/orders', {
      customerId: customerId,
      restaurantId: restaurantId,
      lineItems: selectedItems,
      deliveryAddress: deliveryAddress,
      deliveryTime: deliveryTime,
    }, {
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
    });

    return orderResponse.data;
  } catch (error) {
    console.error(`  ✗ Failed to create order:`, error.response?.data?.message || error.message);
    return null;
  }
}

async function seedOrders() {
  console.log('🛒 Seeding test orders...\n');
  console.log(`API Base URL: ${API_BASE_URL}\n`);

  try {
    // Get all restaurants
    console.log('Fetching restaurants...');
    const restaurantsResponse = await client.get('/restaurants');
    const existingRestaurants = restaurantsResponse.data;

    // Create customers (no list endpoint exists, so we create them)
    // Note: If customers already exist, this will fail, but we'll continue
    console.log('Creating test customers...');
    let existingCustomers = [];
    for (const customerData of customers) {
      try {
        const customer = await createCustomer(customerData);
        existingCustomers.push(customer);
      } catch (err) {
        // Customer may already exist - we can't look them up, so skip
        // In a real scenario, you'd have a customer lookup endpoint
        console.log(`  ⚠️  Could not create customer ${customerData.name} (may already exist)`);
      }
    }
    
    if (existingCustomers.length === 0) {
      console.log('\n⚠️  No customers available for order creation.');
      console.log('Please run the full seed script first to create customers, or create customers manually.');
      return;
    }

    if (existingRestaurants.length === 0) {
      console.log('No restaurants found. Please run the full seed script first.');
      return;
    }

    console.log(`Found ${existingRestaurants.length} restaurants and ${existingCustomers.length} customers\n`);

    // Fetch full restaurant details with menus
    console.log('Fetching restaurant menus...');
    const restaurantsWithMenus = [];
    for (const restaurant of existingRestaurants) {
      const fullRestaurant = await getRestaurantWithMenu(restaurant.id);
      if (fullRestaurant && fullRestaurant.menuItems && fullRestaurant.menuItems.length > 0) {
        restaurantsWithMenus.push(fullRestaurant);
      }
    }

    if (restaurantsWithMenus.length === 0) {
      console.log('No restaurants with menus found. Please seed menus first using --menus-only');
      return;
    }

    console.log(`Found ${restaurantsWithMenus.length} restaurants with menus\n`);

    // Create orders - 2-3 orders per customer
    const orders = [];
    for (const customer of existingCustomers) {
      const numOrders = Math.floor(Math.random() * 2) + 2; // 2-3 orders per customer
      
      for (let i = 0; i < numOrders && i < restaurantsWithMenus.length; i++) {
        const restaurant = restaurantsWithMenus[i];
        console.log(`Creating order for ${customer.name} at ${restaurant.name}...`);
        
        const order = await createOrder(customer.id, restaurant.id, restaurant);
        if (order) {
          orders.push(order);
          console.log(`  ✓ Created order ${order.id.slice(0, 8)}... (${order.state}) - Total: $${order.orderTotal.amount} ${order.orderTotal.currency}`);
        }
        
        // Small delay to avoid overwhelming the system
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }

    console.log(`\n✅ Created ${orders.length} test orders`);
    console.log('\nOrder Summary:');
    orders.forEach(order => {
      console.log(`  - Order ${order.id.slice(0, 8)}... (${order.state}) - $${order.orderTotal.amount}`);
    });
  } catch (error) {
    console.error('\n❌ Error seeding orders:', error.response?.data?.message || error.message);
    console.error(error.stack);
    process.exit(1);
  }
}

async function seedData() {
  console.log('🌱 Starting data seeding...\n');
  console.log(`API Base URL: ${API_BASE_URL}\n`);

  const results = {
    restaurants: [],
    customers: [],
    errors: [],
  };

  // Create restaurants
  console.log('📦 Creating restaurants...\n');
  for (const restaurantData of restaurants) {
    try {
      const restaurant = await createRestaurant(restaurantData);
      results.restaurants.push(restaurant);
    } catch (error) {
      results.errors.push({ type: 'restaurant', name: restaurantData.name, error: error.message });
    }
  }

  console.log('\n');

  // Create customers
  console.log('👥 Creating customers...\n');
  for (const customerData of customers) {
    try {
      const customer = await createCustomer(customerData);
      results.customers.push(customer);
    } catch (error) {
      results.errors.push({ type: 'customer', name: customerData.name, error: error.message });
    }
  }

  // Summary
  console.log('\n' + '='.repeat(50));
  console.log('📊 Seeding Summary');
  console.log('='.repeat(50));
  console.log(`✓ Restaurants created: ${results.restaurants.length}/${restaurants.length}`);
  console.log(`✓ Customers created: ${results.customers.length}/${customers.length}`);
  
  if (results.errors.length > 0) {
    console.log(`\n⚠️  Errors: ${results.errors.length}`);
    results.errors.forEach(err => {
      console.log(`  - ${err.type}: ${err.name} - ${err.error}`);
    });
  }

  console.log('\n✅ Seeding completed!');
  console.log('\nYou can now:');
  console.log('  - Browse restaurants at: http://localhost:5173/restaurants');
  console.log('  - Register as a customer at: http://localhost:5173/register');
  console.log('  - Use these test customers:');
  results.customers.forEach(customer => {
    console.log(`    - ${customer.name} (${customer.email})`);
  });
}

// Check command line arguments
const args = process.argv.slice(2);
const menuOnly = args.includes('--menus-only') || args.includes('-m');
const ordersOnly = args.includes('--orders-only') || args.includes('-o');

// Run the seed script
if (ordersOnly) {
  seedOrders().catch(error => {
    console.error('\n❌ Fatal error during order seeding:', error.message);
    process.exit(1);
  });
} else if (menuOnly) {
  seedMenusOnly().catch(error => {
    console.error('\n❌ Fatal error during menu seeding:', error.message);
    process.exit(1);
  });
} else {
  seedData().catch(error => {
    console.error('\n❌ Fatal error during seeding:', error.message);
    process.exit(1);
  });
}

