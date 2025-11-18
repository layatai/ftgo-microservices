# FTGO Frontend

A modern React + TypeScript frontend for the FTGO (Food to Go) microservices application.

## Features

- 🍕 **Restaurant Browsing**: Browse available restaurants and view menus
- 🛒 **Shopping Cart**: Add items to cart and manage quantities
- 📦 **Order Management**: Create orders and track their status
- 👤 **Customer Registration**: Simple registration flow
- 🎨 **Modern UI**: Built with Tailwind CSS and Headless UI
- ⚡ **Fast**: Powered by Vite for lightning-fast development

## Tech Stack

- **React 19**: UI library
- **TypeScript**: Type safety
- **Vite**: Build tool and dev server
- **React Router**: Client-side routing
- **Zustand**: State management
- **Axios**: HTTP client
- **Tailwind CSS**: Styling
- **Headless UI**: Accessible UI components
- **Heroicons**: Icon library

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend services running (see main README)

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will be available at `http://localhost:5173` (default Vite port).

### Environment Variables

Create a `.env` file in the frontend directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Building for Production

```bash
npm run build
```

The built files will be in the `dist/` directory.

## Project Structure

```
ftgo-frontend/
├── src/
│   ├── api/           # API client and types
│   ├── components/   # Reusable components
│   ├── pages/        # Page components
│   ├── store/         # Zustand stores
│   ├── types/         # TypeScript types
│   ├── App.tsx       # Main app component
│   └── main.tsx      # Entry point
├── public/           # Static assets
└── package.json
```

## Available Pages

- `/` - Home page
- `/register` - Customer registration
- `/restaurants` - List of restaurants
- `/restaurants/:id` - Restaurant menu
- `/cart` - Shopping cart (requires authentication)
- `/orders` - Order history (requires authentication)
- `/orders/:id` - Order details (requires authentication)

## API Integration

The frontend communicates with the backend through the API Gateway at `http://localhost:8080/api`.

### Available Endpoints

- `POST /api/customers` - Create customer
- `GET /api/customers/:id` - Get customer
- `GET /api/restaurants` - List restaurants (may need backend implementation)
- `GET /api/restaurants/:id` - Get restaurant with menu
- `POST /api/orders` - Create order
- `GET /api/orders/:id` - Get order
- `PUT /api/orders/:id/cancel` - Cancel order

## Development

### Code Style

- Use TypeScript for all new files
- Follow React best practices (hooks, functional components)
- Use Tailwind CSS for styling
- Keep components small and focused

### State Management

- **Zustand** for global state (auth, cart)
- **React Router** for navigation state
- **Local state** for component-specific state

## Notes

- The restaurant listing endpoint (`GET /api/restaurants`) may need to be implemented in the backend
- Order history requires a `GET /api/orders?customerId={id}` endpoint
- Authentication is currently simplified (no JWT tokens)
