import React, { useState } from 'react';
import { Box, Typography, TextField, Button, Container, Paper, CircularProgress, Link } from '@mui/material';
import { LockOpen as LockOpenIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

interface LoginProps {
  onLogin: (username: string, password: string) => Promise<boolean>;
  onSwitchToRegister: () => void;
}

const Login: React.FC<LoginProps> = ({ onLogin, onSwitchToRegister }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  const handleSwitchToRegister = () => {
    navigate('/register');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Просто сохраняем учетные данные без проверки на сервере
      localStorage.setItem('auth', JSON.stringify({ username, password }));

      // Создаем "виртуального" пользователя с правами администратора
      localStorage.setItem('user', JSON.stringify({
        name: username,
        id: 1,
        roles: ['ROLE_USER', 'ROLE_ADMIN']
      }));

      // Вызываем onLogin для обновления состояния аутентификации
      const success = await onLogin(username, password);
      
      if (success) {
        toast.success('Вы успешно вошли в систему!');
      }
    } catch (error) {
      console.error('Ошибка входа:', error);
      toast.error('Неверное имя пользователя или пароль');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs" sx={{ mt: 8 }}>
      <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <LockOpenIcon sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
          <Typography component="h1" variant="h5" gutterBottom>
            Вход в систему
          </Typography>

          <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%', mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Имя пользователя"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Пароль"
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Войти'}
            </Button>
          </Box>

          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Для тестирования используйте любые учетные данные
            </Typography>
            
            <Typography variant="body2" sx={{ mt: 1 }}>
              Нет аккаунта?{' '}
              <Link component="button" variant="body2" onClick={handleSwitchToRegister}>
                Зарегистрироваться
              </Link>
            </Typography>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default Login;