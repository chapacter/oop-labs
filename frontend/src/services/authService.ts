import axios from 'axios';

class AuthService {
  async login(username: string, password: string) {
    try {
      // Создаем заголовок Basic Auth
      const authHeader = `Basic ${btoa(`${username}:${password}`)}`;

      // Проверяем аутентификацию, делая запрос к защищенному ресурсу
      const response = await axios.get('/api/users/me', {
        headers: {
          Authorization: authHeader
        }
      });

      // Сохраняем учетные данные
      localStorage.setItem('auth', JSON.stringify({ username, password }));
      localStorage.setItem('user', JSON.stringify(response.data));

      return response.data;
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
      try {
        return JSON.parse(userStr);
      } catch (e) {
        console.error('Error parsing user from localStorage:', e);
        return null;
      }
    }
    return null;
  }

  isAuthenticated(): boolean {
    const authStr = localStorage.getItem('auth');
    return !!authStr;
  }

  getAuthHeaders() {
    const authStr = localStorage.getItem('auth');
    if (authStr) {
      try {
        const { username, password } = JSON.parse(authStr);
        return {
          Authorization: `Basic ${btoa(`${username}:${password}`)}`
        };
      } catch (e) {
        console.error('Error parsing auth from localStorage:', e);
        return {};
      }
    }
    return {};
  }
}

export default new AuthService();