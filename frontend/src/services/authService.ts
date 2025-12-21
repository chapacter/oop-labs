import axios from 'axios';

const API_URL = '/api';

class AuthService {
  async login(username: string, password: string) {
    try {
      // Просто сохраняем учетные данные
      localStorage.setItem('auth', JSON.stringify({ username, password }));

      // Создаем "виртуального" пользователя
      const mockUser = {
        name: username,
        id: 1,
        roles: ['ROLE_USER', 'ROLE_ADMIN']
      };

      localStorage.setItem('user', JSON.stringify(mockUser));
      return mockUser;
    } catch (error) {
      console.error('Ошибка при входе:', error);
      this.logout();
      throw error;
    }
  }

  logout() {
    localStorage.removeItem('auth');
    localStorage.removeItem('user');
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      return JSON.parse(userStr);
    }
    return null;
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('auth');
  }

  getAuthHeaders() {
    const authStr = localStorage.getItem('auth');
    if (authStr) {
      const { username, password } = JSON.parse(authStr);
      return {
        Authorization: `Basic ${btoa(`${username}:${password}`)}`
      };
    }
    return {};
  }
}

export default new AuthService();