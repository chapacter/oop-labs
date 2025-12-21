import axios from 'axios';
import {
  FunctionDTO,
  CreateFunctionRequest,
  CreateFunctionFromArrayRequest,
  PointDTO,
  ChartData
} from '../models';
import authService from './authService';

const API_URL = '/api/functions';

class FunctionService {
  async getAllFunctions(userId?: number, withPoints: boolean = false) {
    try {
      const params: any = {};
      if (userId) params.userId = userId;
      if (withPoints) params.withPoints = true;

      const response = await axios.get<FunctionDTO[]>(API_URL, {
        params,
        headers: authService.getAuthHeaders()
      });
      return response.data;
    } catch (error) {
      console.error('Ошибка при получении функций:', error);
      throw error;
    }
  }

  async getFunctionById(id: number, withPoints: boolean = false) {
    try {
      const params = withPoints ? { withPoints: 'true' } : {};
      const response = await axios.get<FunctionDTO>(`${API_URL}/${id}`, {
        params,
        headers: authService.getAuthHeaders()
      });
      return response.data;
    } catch (error) {
      console.error(`Ошибка при получении функции с ID ${id}:`, error);
      throw error;
    }
  }

  async createFunction(request: CreateFunctionRequest) {
    try {
      const response = await axios.post<FunctionDTO>(API_URL, request, {
        headers: authService.getAuthHeaders()
      });
      return response.data;
    } catch (error) {
      console.error('Ошибка при создании функции:', error);
      throw error;
    }
  }

  async createFunctionFromArray(request: CreateFunctionFromArrayRequest) {
    try {
      // Сначала создаем функцию
      const functionResponse = await axios.post<FunctionDTO>(API_URL, {
        name: request.name,
        format: null,
        userId: request.userId,
        funcResult: request.funcResult
      }, {
        headers: authService.getAuthHeaders()
      });

      const functionId = functionResponse.data.id;

      // Затем добавляем точки
      const pointsPromises = request.points.map(point =>
        axios.post<PointDTO>('/api/points', {
          functionId,
          indexInFunction: 0, // Будет установлено на сервере
          x: point.x,
          y: point.y
        }, {
          headers: authService.getAuthHeaders()
        })
      );

      await Promise.all(pointsPromises);

      return functionResponse.data;
    } catch (error) {
      console.error('Ошибка при создании функции из массива:', error);
      throw error;
    }
  }

  async updateFunction(id: number, name: string, funcResult: string) {
    try {
      const response = await axios.put<FunctionDTO>(`${API_URL}/${id}`, {
        name,
        funcResult
      }, {
        headers: authService.getAuthHeaders()
      });
      return response.data;
    } catch (error) {
      console.error(`Ошибка при обновлении функции с ID ${id}:`, error);
      throw error;
    }
  }

  async deleteFunction(id: number) {
    try {
      await axios.delete(`${API_URL}/${id}`, {
        headers: authService.getAuthHeaders()
      });
      return true;
    } catch (error) {
      console.error(`Ошибка при удалении функции с ID ${id}:`, error);
      throw error;
    }
  }

  async getFunctionPoints(functionId: number) {
    try {
      const response = await axios.get<PointDTO[]>(`/api/points?functionId=${functionId}`, {
        headers: authService.getAuthHeaders()
      });
      return response.data;
    } catch (error) {
      console.error(`Ошибка при получении точек для функции с ID ${functionId}:`, error);
      throw error;
    }
  }

  async addPoint(functionId: number, x: number, y: number) {
    try {
      const response = await axios.post<PointDTO>('/api/points', {
        functionId,
        indexInFunction: 0, // Сервер сам определит индекс
        x,
        y
      }, {
        headers: authService.getAuthHeaders()
      });
      return response.data;
    } catch (error) {
      console.error(`Ошибка при добавлении точки к функции с ID ${functionId}:`, error);
      throw error;
    }
  }

  async deletePoint(pointId: number) {
    try {
      await axios.delete(`/api/points/${pointId}`, {
        headers: authService.getAuthHeaders()
      });
      return true;
    } catch (error) {
      console.error(`Ошибка при удалении точки с ID ${pointId}:`, error);
      throw error;
    }
  }

  async getChartData(functionId: number): Promise<ChartData[]> {
    try {
      const points = await this.getFunctionPoints(functionId);
      return points.map(point => ({ x: point.x, y: point.y }));
    } catch (error) {
      console.error(`Ошибка при получении данных для графика функции с ID ${functionId}:`, error);
      throw error;
    }
  }

  async createFromMathFunction(name: string, userId: number, funcResult: string, mathFunctionType: string, intervalStart: number, intervalEnd: number, pointsCount: number) {
    try {
      // Создаем функцию
      const functionResponse = await axios.post<FunctionDTO>(API_URL, {
        name,
        format: null,
        userId,
        funcResult
      }, {
        headers: authService.getAuthHeaders()
      });

      const functionId = functionResponse.data.id;

      // Генерируем точки в зависимости от типа функции
      let points: { x: number; y: number }[] = [];
      const step = (intervalEnd - intervalStart) / (pointsCount - 1);

      for (let i = 0; i < pointsCount; i++) {
        const x = intervalStart + i * step;
        let y = 0;

        switch (mathFunctionType) {
          case 'identity':
            y = x;
            break;
          case 'square':
            y = x * x;
            break;
          case 'cube':
            y = x * x * x;
            break;
          case 'sqrt':
            y = x >= 0 ? Math.sqrt(x) : 0;
            break;
          case 'sin':
            y = Math.sin(x);
            break;
          case 'cos':
            y = Math.cos(x);
            break;
          case 'exp':
            y = Math.exp(x);
            break;
          default:
            y = x; // по умолчанию тождественная функция
        }

        points.push({ x, y });
      }

      // Добавляем точки
      const pointsPromises = points.map(point =>
        axios.post<PointDTO>('/api/points', {
          functionId,
          indexInFunction: 0,
          x: point.x,
          y: point.y
        }, {
          headers: authService.getAuthHeaders()
        })
      );

      await Promise.all(pointsPromises);

      return functionResponse.data;
    } catch (error) {
      console.error('Ошибка при создании функции из математической функции:', error);
      throw error;
    }
  }
}

export default new FunctionService();
