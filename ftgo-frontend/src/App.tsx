import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/useAuthStore';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import RestaurantsPage from './pages/RestaurantsPage';
import RestaurantDetailPage from './pages/RestaurantDetailPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import OrderDetailPage from './pages/OrderDetailPage';

function App() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/restaurants" element={<RestaurantsPage />} />
          <Route path="/restaurants/:id" element={<RestaurantDetailPage />} />
          <Route 
            path="/cart" 
            element={isAuthenticated ? <CartPage /> : <Navigate to="/register" />} 
          />
          <Route 
            path="/orders" 
            element={isAuthenticated ? <OrdersPage /> : <Navigate to="/register" />} 
          />
          <Route 
            path="/orders/:id" 
            element={isAuthenticated ? <OrderDetailPage /> : <Navigate to="/register" />} 
          />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
