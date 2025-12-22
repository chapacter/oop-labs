// src/services/functionService.ts
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
const POINTS_API = '/api/points';

class FunctionService {
  async getAllFunctions(userId?: number, withPoints: boolean = false) {
    try {
      const params: any = {};
      if (userId) params.userId = userId;
      if (withPoints) params.withPoints = true;

      const response = await axios.get(API_URL, {
        params,
        headers: authService.getAuthHeaders()
      });

      // Проверяем, что response.data - это массив
      if (Array.isArray(response.data)) {
        return response.data;
      } else if (response.data && response.data.content) {
        // Для пагинации Spring Data
        return response.data.content;
      }
      return [];
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
        funcResult: request.funcResult || "Auto-generated"
      }, {
        headers: authService.getAuthHeaders()
      });

      const functionId = functionResponse.data.id;

      // Затем добавляем точки (параллельно)
      const pointsPromises = request.points.map((point: any) =>
        axios.post<PointDTO>(POINTS_API, {
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

  /**
   * Надёжно получает ВСЕ точки для функции:
   * - если сервер возвращает массив -> возвращаем его;
   * - если сервер возвращает Spring Page (data.content / content) -> загружаем все страницы;
   * - если сервер возвращает HAL (_embedded.points) -> используем embedded;
   * - в иных случаях пытаемся найти массив внутри объекта.
   */
  async getFunctionPoints(functionId: number) {
    try {
      const headers = authService.getAuthHeaders();

      // Первый запрос: пробуем получить либо весь массив, либо первую страницу
      const firstParams: any = { functionId, page: 0, size: 1000 }; // size большой по умолчанию
      const response = await axios.get(POINTS_API, {
        params: firstParams,
        headers
      });

      const data = response.data;

      // 1) Если сервер вернул прямой массив — используем его
      if (Array.isArray(data)) {
        return data as PointDTO[];
      }

      // 2) Spring Data Page-like: { content: [...], totalPages, totalElements, number, size }
      if (data && Array.isArray((data as any).content)) {
        const content = (data as any).content as PointDTO[];
        const totalPages = typeof (data as any).totalPages === 'number'
          ? (data as any).totalPages
          : Math.ceil(((data as any).totalElements || content.length) / (firstParams.size || content.length || 1));

        // если страниц больше одной — загружаем последовательно остальные
        if (totalPages > 1) {
          const all: PointDTO[] = [...content];
          for (let page = 1; page < totalPages; page++) {
            const resp = await axios.get(POINTS_API, {
              params: { functionId, page, size: firstParams.size },
              headers
            });
            const pageData = resp.data;
            if (pageData && Array.isArray((pageData as any).content)) {
              all.push(...(pageData as any).content);
            } else if (Array.isArray(pageData)) {
              all.push(...pageData);
            } else {
              // если неожиданный формат — прерываем
              break;
            }
          }
          return all;
        }

        // single page only
        return content;
      }

      // 3) HAL format: { _embedded: { points: [...] } }
      if (data && data._embedded) {
        // пробуем найти любой массив внутри _embedded
        const embeddedValues = Object.values(data._embedded).filter(v => Array.isArray(v));
        if (embeddedValues.length) {
          // объединяем все найденные массивы (обычно там только один)
          return (embeddedValues as any[]).flat() as PointDTO[];
        }
      }

      // 4) Попытка найти любое поле-массив внутри объекта
      if (data && typeof data === 'object') {
        const arrays = Object.values(data).filter(v => Array.isArray(v)) as any[];
        if (arrays.length === 1) {
          return arrays[0] as PointDTO[];
        } else if (arrays.length > 1) {
          // если несколько массивов — пробуем предпочесть очевидные имена
          const preferred = (data as any).content || (data as any).items || (data as any).points || (data as any).data;
          if (Array.isArray(preferred)) return preferred as PointDTO[];
          // иначе возвращаем самый длинный массив
          const longest = arrays.reduce((acc, cur) => (cur.length > acc.length ? cur : acc), arrays[0]);
          return longest as PointDTO[];
        }
      }

      // В противном случае — возвращаем пустой массив
      return [] as PointDTO[];
    } catch (error) {
      console.error(`Ошибка при получении точек для функции с ID ${functionId}:`, error);
      throw error;
    }
  }

  async addPoint(functionId: number, x: number, y: number) {
    try {
      const response = await axios.post<PointDTO>(POINTS_API, {
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
      await axios.delete(`${POINTS_API}/${pointId}`, {
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
      return points.map((point: any) => ({ x: point.x, y: point.y }));
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
        funcResult: funcResult || "Auto-generated"
      }, {
        headers: authService.getAuthHeaders()
      });

      const functionId = functionResponse.data.id;

      // Генерируем точки
      let points: { x: number; y: number }[] = [];
      const step = (intervalEnd - intervalStart) / (pointsCount - 1);

      for (let i = 0; i < pointsCount; i++) {
        const x = intervalStart + i * step;
        let y = 0;
        switch (mathFunctionType) {
          case 'identity': y = x; break;
          case 'square': y = x * x; break;
          case 'cube': y = x * x * x; break;
          case 'sqrt': y = x >= 0 ? Math.sqrt(x) : 0; break;
          case 'sin': y = Math.sin(x); break;
          case 'cos': y = Math.cos(x); break;
          case 'exp': y = Math.exp(x); break;
          default: y = x;
        }
        points.push({ x, y });
      }

      // Добавляем точки
      const pointsPromises = points.map(point =>
        axios.post<PointDTO>(POINTS_API, {
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
