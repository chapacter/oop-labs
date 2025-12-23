// src/App.tsx
import React, { useEffect, useMemo, useState, createContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, Box, CircularProgress } from '@mui/material';
import authService from './services/authService';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import FunctionEditor from './pages/FunctionEditor';
import FunctionGraph from './pages/FunctionGraph';
import FunctionOperations from './pages/FunctionOperations';
import Settings from './pages/Settings';
import FunctionCreator from './pages/FunctionCreator';
import toast, { Toaster } from 'react-hot-toast';
import { darkTheme, lightTheme } from './theme';
import { setLanguage as setI18nLanguage } from './i18n';
import notify from './utils/notify';

type FactoryType = 'array' | 'linked_list';
type ThemeMode = 'light' | 'dark';
type ExportFormat = 'json' | 'csv' | 'xml';
type Lang = 'ru' | 'en';

export interface AppSettings {
  factoryType: FactoryType;
  themeMode: ThemeMode;
  notificationSound: boolean;
  dataExportFormat: ExportFormat;
  autoSave: boolean;
  language: Lang;
}

export const AppSettingsContext = createContext<{
  settings: AppSettings;
  setSettings: (s: Partial<AppSettings>) => void;
}>({
  settings: {
    factoryType: 'array',
    themeMode: 'dark',
    notificationSound: true,
    dataExportFormat: 'json',
    autoSave: true,
    language: 'ru'
  },
  setSettings: () => {}
});

const APP_SETTINGS_KEY = 'appSettings';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loadingAuth, setLoadingAuth] = useState(true);

  const [settings, setSettingsState] = useState<AppSettings>(() => {
    try {
      const saved = JSON.parse(localStorage.getItem(APP_SETTINGS_KEY) || 'null');
      if (saved && typeof saved === 'object') {
        return {
          factoryType: (saved.factoryType as FactoryType) || 'array',
          themeMode: (saved.themeMode as ThemeMode) || 'dark',
          notificationSound: saved.notificationSound !== undefined ? Boolean(saved.notificationSound) : true,
          dataExportFormat: (saved.dataExportFormat as ExportFormat) || 'json',
          autoSave: saved.autoSave !== undefined ? Boolean(saved.autoSave) : true,
          language: (saved.language as Lang) || 'ru'
        };
      }
    } catch (e) {
      // ignore parse errors
    }
    return {
      factoryType: 'array',
      themeMode: 'dark',
      notificationSound: true,
      dataExportFormat: 'json',
      autoSave: true,
      language: 'ru'
    };
  });

  const setSettings = (partial: Partial<AppSettings>) => {
    setSettingsState(prev => {
      const next = { ...prev, ...partial };
      try {
        localStorage.setItem(APP_SETTINGS_KEY, JSON.stringify(next));
      } catch (e) {
        console.warn('Unable to persist app settings', e);
      }
      window.dispatchEvent(new CustomEvent('appSettingsChanged', { detail: next }));
      return next;
    });
  };

  const theme = useMemo(() => {
    return settings.themeMode === 'dark' ? darkTheme : lightTheme;
  }, [settings.themeMode]);

  useEffect(() => {
    try {
      document.documentElement.setAttribute('data-theme', settings.themeMode);
      if (settings.themeMode === 'dark') {
        document.documentElement.classList.add('theme-dark');
        document.documentElement.classList.remove('theme-light');
      } else {
        document.documentElement.classList.add('theme-light');
        document.documentElement.classList.remove('theme-dark');
      }
    } catch (e) {
      console.warn('Failed to apply theme to documentElement', e);
    }
  }, [settings.themeMode]);

  useEffect(() => {
    const handler = (ev: any) => {
      const incoming = (ev && ev.detail) || {};
      const partial: Partial<AppSettings> = {};
      if (incoming.factoryType) partial.factoryType = incoming.factoryType;
      if (incoming.themeMode) partial.themeMode = incoming.themeMode;
      if (incoming.notificationSound !== undefined) partial.notificationSound = Boolean(incoming.notificationSound);
      if (incoming.dataExportFormat) partial.dataExportFormat = incoming.dataExportFormat;
      if (incoming.autoSave !== undefined) partial.autoSave = Boolean(incoming.autoSave);
      if (incoming.language) partial.language = incoming.language;

      if (Object.keys(partial).length) {
        setSettingsState(prev => {
          const next = { ...prev, ...partial };
          try { localStorage.setItem(APP_SETTINGS_KEY, JSON.stringify(next)); } catch (e) { /* ignore */ }
          if (partial.language) {
            setI18nLanguage(partial.language as Lang);
          }
          return next;
        });
      }
    };

    window.addEventListener('appSettingsChanged', handler);
    const onLoaded = (ev: any) => handler(ev);
    window.addEventListener('appSettingsLoaded', onLoaded);

    return () => {
      window.removeEventListener('appSettingsChanged', handler);
      window.removeEventListener('appSettingsLoaded', onLoaded);
    };
  }, []);

  useEffect(() => {
    try {
      setI18nLanguage(settings.language);
    } catch (e) { /* ignore */ }
  }, []); // one-time

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const currentUser = authService.getCurrentUser();
        if (currentUser) {
          setIsAuthenticated(true);
        } else {
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error('Ошибка проверки аутентификации:', error);
        notify.error('Ошибка проверки аутентификации');
      } finally {
        setLoadingAuth(false);
      }
    };

    checkAuth();
  }, []);

  const handleLogin = async (username: string, password: string) => {
    try {
      await authService.login(username, password);
      setIsAuthenticated(true);
      notify.success('Вы успешно вошли в систему!');
      return true;
    } catch (error) {
      console.error('Ошибка входа:', error);
      notify.error('Неверное имя пользователя или пароль');
      return false;
    }
  };

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    notify.success('Вы успешно вышли из системы');
  };

  if (loadingAuth) {
    return (
      <ThemeProvider theme={theme}>
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
    <AppSettingsContext.Provider value={{ settings, setSettings }}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Toaster position="top-right" />
        <Router>
          {isAuthenticated && <Navbar onLogout={handleLogout} />}
          <Box component="main" sx={{ mt: isAuthenticated ? 8 : 0, p: 3 }}>
            <Routes>
              <Route path="/login" element={!isAuthenticated ? <Login onLogin={handleLogin} onSwitchToRegister={() => window.location.href = '/register'} /> : <Navigate to="/dashboard" />} />
              <Route path="/dashboard" element={isAuthenticated ? <Dashboard /> : <Navigate to="/login" />} />
              <Route path="/functions/new" element={isAuthenticated ? <FunctionCreator /> : <Navigate to="/login" />} />
              <Route path="/functions/from-array" element={isAuthenticated ? <FunctionEditor /> : <Navigate to="/login" />} />
              <Route path="/functions/from-math" element={isAuthenticated ? <FunctionEditor mathFunctionMode={true} /> : <Navigate to="/login" />} />
              <Route path="/functions/:id/edit" element={isAuthenticated ? <FunctionEditor /> : <Navigate to="/login" />} />
              <Route path="/functions/:id/graph" element={isAuthenticated ? <FunctionGraph /> : <Navigate to="/login" />} />
              <Route path="/operations" element={isAuthenticated ? <FunctionOperations /> : <Navigate to="/login" />} />
              <Route path="/settings" element={isAuthenticated ? <Settings /> : <Navigate to="/login" />} />
              <Route path="/" element={isAuthenticated ? <Navigate to="/dashboard" /> : <Navigate to="/login" />} />
              <Route path="/register" element={!isAuthenticated ? <Register onRegister={handleLogin} onSwitchToLogin={() => window.location.href = '/login'} /> : <Navigate to="/dashboard" />} />
            </Routes>
          </Box>
        </Router>
      </ThemeProvider>
    </AppSettingsContext.Provider>
  );
}

export default App;