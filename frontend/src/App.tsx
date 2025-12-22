import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, Box, CircularProgress } from '@mui/material';
import { darkTheme } from './theme';
import authService from './services/authService';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import FunctionEditor from './pages/FunctionEditor';
import FunctionGraph from './pages/FunctionGraph';
import FunctionOperations from './pages/FunctionOperations';
import Settings from './pages/Settings';
import FunctionCreator from './pages/FunctionCreator';
import toast, { Toaster } from 'react-hot-toast';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const currentUser = authService.getCurrentUser();
        if (currentUser) {
          setIsAuthenticated(true);
        }
      } catch (error) {
        console.error('Ошибка проверки аутентификации:', error);
        toast.error('Ошибка проверки аутентификации');
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  const handleLogin = async (username: string, password: string) => {
    try {
      await authService.login(username, password);
      setIsAuthenticated(true);
      toast.success('Вы успешно вошли в систему!');
      return true;
    } catch (error) {
      console.error('Ошибка входа:', error);
      toast.error('Неверное имя пользователя или пароль');
      return false;
    }
  };

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    toast.success('Вы успешно вышли из системы');
  };

  if (loading) {
    return (
      <ThemeProvider theme={darkTheme}>
        <CssBaseline />
        <Box sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          height: '100vh',
          bgcolor: 'background.default'
        }}>
          <CircularProgress />
        </Box>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={darkTheme}>
      <CssBaseline />
      <Toaster position="top-right" />
      <Router>
        {isAuthenticated && <Navbar onLogout={handleLogout} />}
        <Box component="main" sx={{ mt: isAuthenticated ? 8 : 0, p: 3 }}>
          <Routes>
            <Route path="/login" element={!isAuthenticated ? <Login onLogin={handleLogin} /> : <Navigate to="/dashboard" />} />
            <Route path="/dashboard" element={isAuthenticated ? <Dashboard /> : <Navigate to="/login" />} />
            <Route path="/functions/new" element={isAuthenticated ? <FunctionCreator /> : <Navigate to="/login" />} />
            <Route path="/functions/from-array" element={isAuthenticated ? <FunctionEditor /> : <Navigate to="/login" />} />
            <Route path="/functions/from-math" element={isAuthenticated ? <FunctionEditor mathFunctionMode={true} /> : <Navigate to="/login" />} />
            <Route path="/functions/:id/edit" element={isAuthenticated ? <FunctionEditor /> : <Navigate to="/login" />} />
            <Route path="/functions/:id/graph" element={isAuthenticated ? <FunctionGraph /> : <Navigate to="/login" />} />
            <Route path="/operations" element={isAuthenticated ? <FunctionOperations /> : <Navigate to="/login" />} />
            <Route path="/settings" element={isAuthenticated ? <Settings /> : <Navigate to="/login" />} />
            <Route path="/" element={isAuthenticated ? <Navigate to="/dashboard" /> : <Navigate to="/login" />} />
          </Routes>
        </Box>
      </Router>
    </ThemeProvider>
  );
}

export default App;