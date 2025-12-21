import React, { useState } from 'react';
import { Box, Typography, TextField, Button, Container, Paper, CircularProgress } from '@mui/material';
import { LockOpen as LockOpenIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';

interface LoginProps {
  onLogin: (username: string, password: string) => Promise<boolean>;
}

const Login: React.FC<LoginProps> = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

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
      await onLogin(username, password);

      toast.success('Вы успешно вошли в систему!');
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

          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Для тестирования используйте любые учетные данные
          </Typography>
        </Box>
      </Paper>
    </Container>
  );
};

export default Login;