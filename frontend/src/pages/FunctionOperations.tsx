// src/pages/FunctionOperations.tsx
import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Grid, Card, CardContent, Button, Select, MenuItem,
  FormControl, InputLabel, IconButton, Tooltip, Container,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, useMediaQuery, useTheme,
  CircularProgress
} from '@mui/material';
import {
  Functions as FunctionsIcon,
  SwapHoriz as SwapHorizIcon,
  ArrowBack as ArrowBackIcon,
  Save as SaveIcon,
  PlayArrow as PlayArrowIcon,
  Close as CloseIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { SelectChangeEvent } from '@mui/material';
import functionService from '../services/functionService';
import { FunctionDTO, PointDTO } from '../models';
import authService from '../services/authService';
import toast from 'react-hot-toast';

type Operation = 'add' | 'subtract' | 'multiply' | 'divide' | null;

interface ResultPoint {
  x: number;
  y: number;   // результат
  yA: number;  // значение A (первая функция) в этой x
  yB: number;  // значение B (вторая функция) в этой x
}

const EPS_X = 1e-9;
const EPS_Y = 1e-9;

const FunctionOperations: React.FC = () => {
  const [functions, setFunctions] = useState<FunctionDTO[]>([]);
  const [firstFunction, setFirstFunction] = useState<FunctionDTO | null>(null);
  const [secondFunction, setSecondFunction] = useState<FunctionDTO | null>(null);
  const [firstPoints, setFirstPoints] = useState<PointDTO[]>([]);
  const [secondPoints, setSecondPoints] = useState<PointDTO[]>([]);
  const [resultPoints, setResultPoints] = useState<ResultPoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [operation, setOperation] = useState<Operation>(null);
  const [error, setError] = useState<string | null>(null);
  const [swapped, setSwapped] = useState(false);

  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  useEffect(() => {
    const loadFunctions = async () => {
      try {
        setLoading(true);
        const currentUser = authService.getCurrentUser();
        if (!currentUser) {
          navigate('/login');
          return;
        }
        const data = await functionService.getAllFunctions(currentUser.id);
        setFunctions(data || []);
      } catch (err) {
        console.error('Ошибка при загрузке функций:', err);
        toast.error('Не удалось загрузить функции');
      } finally {
        setLoading(false);
      }
    };
    loadFunctions();
  }, [navigate]);

  const loadFunctionPoints = async (functionId: number): Promise<PointDTO[]> => {
    try {
      const pts = await functionService.getFunctionPoints(functionId);
      // Приводим к числам и сортируем по x
      const normalized = (pts || []).map((p: any) => ({ ...p, x: Number(p.x), y: Number(p.y) }))
        .filter((p: any) => Number.isFinite(p.x) && Number.isFinite(p.y))
        .sort((a: any, b: any) => a.x - b.x);
      return normalized;
    } catch (err) {
      console.error(`Ошибка при загрузке точек функции ${functionId}:`, err);
      toast.error('Не удалось загрузить точки функции');
      return [];
    }
  };

  const handleFirstFunctionChange = async (e: SelectChangeEvent<string>) => {
    const funcId = Number(e.target.value);
    const func = functions.find(f => f.id === funcId) || null;
    setFirstFunction(func);
    setResultPoints([]);
    setError(null);
    if (func) {
      const pts = await loadFunctionPoints(funcId);
      setFirstPoints(pts);
    } else {
      setFirstPoints([]);
    }
  };

  const handleSecondFunctionChange = async (e: SelectChangeEvent<string>) => {
    const funcId = Number(e.target.value);
    const func = functions.find(f => f.id === funcId) || null;
    setSecondFunction(func);
    setResultPoints([]);
    setError(null);
    if (func) {
      const pts = await loadFunctionPoints(funcId);
      setSecondPoints(pts);
    } else {
      setSecondPoints([]);
    }
  };

  const swapFunctions = () => {
    setFirstFunction(prev => {
      const next = secondFunction;
      return next;
    });
    setSecondFunction(prev => {
      const next = firstFunction;
      return next;
    });
    setFirstPoints(prev => {
      const next = secondPoints;
      return next;
    });
    setSecondPoints(prev => {
      const next = firstPoints;
      return next;
    });
    setSwapped(s => !s);
    setResultPoints([]);
    setError(null);
  };

  // Возвращает значение y функции (points) в точке x с интерполяцией/экстраполяцией
  const getYAtX = (points: PointDTO[], x: number): number => {
    if (!points || points.length === 0) return NaN;
    if (points.length === 1) return points[0].y;

    // Предполагаем, что points уже отсортированы по x (см. loadFunctionPoints)
    const n = points.length;
    if (x <= points[0].x + EPS_X) {
      // экстраполяция слева по первым двум точкам
      const p0 = points[0], p1 = points[1];
      const dx = p1.x - p0.x;
      if (Math.abs(dx) < EPS_X) return p0.y;
      const t = (x - p0.x) / dx;
      return p0.y + t * (p1.y - p0.y);
    }
    if (x >= points[n - 1].x - EPS_X) {
      // экстраполяция справа по последним двум точкам
      const p0 = points[n - 2], p1 = points[n - 1];
      const dx = p1.x - p0.x;
      if (Math.abs(dx) < EPS_X) return p1.y;
      const t = (x - p0.x) / dx;
      return p0.y + t * (p1.y - p0.y);
    }

    // поиск интервала
    // бинарный поиск для производительности
    let left = 0, right = n - 1;
    while (left <= right) {
      const mid = Math.floor((left + right) / 2);
      const xm = points[mid].x;
      if (Math.abs(xm - x) <= EPS_X) return points[mid].y;
      if (xm < x) left = mid + 1;
      else right = mid - 1;
    }
    // теперь right < left, интервал между points[right] и points[left]
    const i0 = Math.max(0, right);
    const i1 = Math.min(n - 1, left);
    const p0 = points[i0];
    const p1 = points[i1];
    if (!p0 || !p1) return NaN;
    const dx = p1.x - p0.x;
    if (Math.abs(dx) < EPS_X) return p0.y;
    const t = (x - p0.x) / dx;
    return p0.y + t * (p1.y - p0.y);
  };

  const validateOperations = (): boolean => {
    setError(null);
    if (!firstFunction || !secondFunction) {
      setError('Выберите обе функции');
      return false;
    }
    if (!firstPoints.length || !secondPoints.length) {
      setError('Обе функции должны содержать хотя бы одну точку');
      return false;
    }
    if (!operation) {
      setError('Выберите операцию');
      return false;
    }
    // деление: проверим, что нет деления на ноль на пересечении X-ов (мы проверим позже точнее)
    return true;
  };

  const performOperation = () => {
    setError(null);
    setResultPoints([]);
    if (!validateOperations()) return;

    // Собираем объединённый набор X-ов
    const xsSet = new Set<number>();
    firstPoints.forEach(p => xsSet.add(Number(p.x)));
    secondPoints.forEach(p => xsSet.add(Number(p.x)));
    const xs = Array.from(xsSet).sort((a, b) => a - b);

    // Если обе функции имеют непрерывные области с разным набором точек, интерполируем их значения на каждом x
    try {
      const results: ResultPoint[] = xs.map(x => {
        const yA = getYAtX(firstPoints, x);
        const yB = getYAtX(secondPoints, x);
        if (!Number.isFinite(yA) || !Number.isFinite(yB)) {
          throw new Error('Не удалось вычислить значение функции в некоторой точке');
        }
        let yRes = NaN;
        switch (operation) {
          case 'add':
            yRes = yA + yB;
            break;
          case 'subtract':
            yRes = yA - yB;
            break;
          case 'multiply':
            yRes = yA * yB;
            break;
          case 'divide':
            if (Math.abs(yB) < EPS_Y) {
              throw new Error(`Деление на ноль при x = ${x}`);
            }
            yRes = yA / yB;
            break;
          default:
            throw new Error('Неизвестная операция');
        }
        return { x, y: yRes, yA, yB };
      });

      setResultPoints(results);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Ошибка при выполнении операции';
      setError(message);
    }
  };

  const saveResult = async () => {
    if (!operation || resultPoints.length === 0) {
      toast.error('Нет результата для сохранения');
      return;
    }

    try {
      const currentUser = authService.getCurrentUser();
      if (!currentUser) {
        toast.error('Пользователь не авторизован');
        return;
      }

      const operationNames: Record<NonNullable<Operation>, string> = {
        add: 'Сложение',
        subtract: 'Вычитание',
        multiply: 'Умножение',
        divide: 'Деление'
      } as any;

      const functionName = `${operationNames[operation as NonNullable<Operation>]}: ${firstFunction?.name || 'A'} и ${secondFunction?.name || 'B'}`;

      const resultFunction = await functionService.createFunction({
        name: functionName,
        format: null,
        userId: currentUser.id,
        funcResult: `Результат операции ${operationNames[operation as NonNullable<Operation>]}`
      });

      // Добавляем точки (сервер сам назначит индексы)
      for (const p of resultPoints) {
        // если сервер ожидает числа, передаем числа
        await functionService.addPoint(resultFunction.id, Number(p.x), Number(p.y));
      }

      toast.success('Результат успешно сохранен');
      navigate(`/functions/${resultFunction.id}/graph`);
    } catch (err) {
      console.error('Ошибка при сохранении результата:', err);
      toast.error('Не удалось сохранить результат');
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <IconButton onClick={() => navigate('/dashboard')} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1" fontWeight="bold">
          Операции с функциями
        </Typography>
        <Box sx={{ ml: 'auto', display: 'flex', gap: 1 }}>
          <Tooltip title="Поменять функции местами">
            <IconButton onClick={swapFunctions} disabled={!firstFunction || !secondFunction}>
              <SwapHorizIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                <FunctionsIcon sx={{ mr: 1 }} /> Выберите операцию
              </Typography>
              <Grid container spacing={2} alignItems="center">
                <Grid item xs={12} sm={4}>
                  <FormControl fullWidth required>
                    <InputLabel>Первое слагаемое (уменьшаемое)</InputLabel>
                    <Select
                      value={firstFunction?.id?.toString() || ''}
                      onChange={handleFirstFunctionChange}
                      label="Первое слагаемое (уменьшаемое)"
                    >
                      {functions.map(func => (
                        <MenuItem key={func.id} value={String(func.id)}>
                          {func.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={2} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                  <FormControl fullWidth required>
                    <InputLabel>Операция</InputLabel>
                    <Select
                      value={operation || ''}
                      onChange={(e) => setOperation((e.target.value as Operation) || null)}
                      displayEmpty
                      sx={{ minWidth: 120 }}
                      label="Операция"
                    >
                      <MenuItem value="" disabled>
                        Операция
                      </MenuItem>
                      <MenuItem value="add">+ (сложение)</MenuItem>
                      <MenuItem value="subtract">− (вычитание)</MenuItem>
                      <MenuItem value="multiply">×</MenuItem>
                      <MenuItem value="divide">÷</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <FormControl fullWidth required>
                    <InputLabel>Второе слагаемое (вычитаемое)</InputLabel>
                    <Select
                      value={secondFunction?.id?.toString() || ''}
                      onChange={handleSecondFunctionChange}
                      label="Второе слагаемое (вычитаемое)"
                    >
                      {functions.map(func => (
                        <MenuItem key={func.id} value={String(func.id)}>
                          {func.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={2}>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={performOperation}
                    disabled={!firstFunction || !secondFunction || !operation}
                    startIcon={<PlayArrowIcon />}
                    sx={{ height: '100%' }}
                  >
                    {isMobile ? 'Вычислить' : 'Выполнить'}
                  </Button>
                </Grid>
              </Grid>

              {error && (
                <Box sx={{ mt: 2, p: 2, bgcolor: 'error.main', color: 'white', borderRadius: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <CloseIcon sx={{ mr: 1 }} />
                    <Typography>{error}</Typography>
                  </Box>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                Функция A: {firstFunction?.name || 'Не выбрана'}
              </Typography>
              {firstPoints.length > 0 ? (
                <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
                  <Table stickyHeader size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 'bold' }}>X</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>Y</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {firstPoints.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>{point.x.toFixed(6)}</TableCell>
                          <TableCell>{point.y.toFixed(6)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography color="text.secondary" align="center" sx={{ p: 2 }}>
                  {firstFunction ? 'Нет точек для отображения' : 'Выберите функцию'}
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                Функция B: {secondFunction?.name || 'Не выбрана'}
              </Typography>
              {secondPoints.length > 0 ? (
                <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
                  <Table stickyHeader size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 'bold' }}>X</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>Y</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {secondPoints.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>{point.x.toFixed(6)}</TableCell>
                          <TableCell>{point.y.toFixed(6)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography color="text.secondary" align="center" sx={{ p: 2 }}>
                  {secondFunction ? 'Нет точек для отображения' : 'Выберите функцию'}
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {resultPoints.length > 0 && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap', gap: 1 }}>
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    Результат операции: {operation === 'add' ? 'Сложение' : operation === 'subtract' ? 'Вычитание' : operation === 'multiply' ? 'Умножение' : 'Деление'}
                  </Typography>
                  <Button
                    variant="contained"
                    startIcon={<SaveIcon />}
                    onClick={saveResult}
                    sx={{ fontWeight: 'bold' }}
                  >
                    Сохранить результат
                  </Button>
                </Box>
                <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
                  <Table stickyHeader size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 'bold' }}>X</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>Y (Результат)</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>Y (Функция A)</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>Y (Функция B)</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {resultPoints.map((pt, index) => (
                        <TableRow key={index}>
                          <TableCell>{pt.x.toFixed(6)}</TableCell>
                          <TableCell sx={{ color: 'primary.main', fontWeight: 'bold' }}>{pt.y.toFixed(6)}</TableCell>
                          <TableCell>{pt.yA.toFixed(6)}</TableCell>
                          <TableCell>{pt.yB.toFixed(6)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Container>
  );
};

export default FunctionOperations;
