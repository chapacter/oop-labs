import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Grid, Card, CardContent, CardActions, Button, Select, MenuItem,
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
  Close as CloseIcon,
  Add as AddIcon,
  Remove as RemoveIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import functionService from '../services/functionService';
import { FunctionDTO, PointDTO } from '../models';
import authService from '../services/authService';
import toast from 'react-hot-toast';

const FunctionOperations: React.FC = () => {
  const [functions, setFunctions] = useState<FunctionDTO[]>([]);
  const [firstFunction, setFirstFunction] = useState<FunctionDTO | null>(null);
  const [secondFunction, setSecondFunction] = useState<FunctionDTO | null>(null);
  const [firstPoints, setFirstPoints] = useState<PointDTO[]>([]);
  const [secondPoints, setSecondPoints] = useState<PointDTO[]>([]);
  const [resultPoints, setResultPoints] = useState<PointDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [operation, setOperation] = useState<'add' | 'subtract' | 'multiply' | 'divide' | null>(null);
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
        setFunctions(data);
      } catch (error) {
        console.error('Ошибка при загрузке функций:', error);
        toast.error('Не удалось загрузить функции');
      } finally {
        setLoading(false);
      }
    };
    loadFunctions();
  }, [navigate]);

  const loadFunctionPoints = async (functionId: number): Promise<PointDTO[]> => {
    try {
      return await functionService.getFunctionPoints(functionId);
    } catch (error) {
      console.error(`Ошибка при загрузке точек функции ${functionId}:`, error);
      toast.error('Не удалось загрузить точки функции');
      return [];
    }
  };

  const handleFirstFunctionChange = async (e: React.ChangeEvent<{ value: unknown }>) => {
    const funcId = e.target.value as number;
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

  const handleSecondFunctionChange = async (e: React.ChangeEvent<{ value: unknown }>) => {
    const funcId = e.target.value as number;
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
    const temp = firstFunction;
    setFirstFunction(secondFunction);
    setSecondFunction(temp);
    const tempPoints = firstPoints;
    setFirstPoints(secondPoints);
    setSecondPoints(tempPoints);
    setSwapped(!swapped);
  };

  const validateOperations = (): boolean => {
    if (!firstFunction || !secondFunction || firstPoints.length === 0 || secondPoints.length === 0) {
      setError('Выберите обе функции и убедитесь, что у них есть точки');
      return false;
    }

    if (firstPoints.length !== secondPoints.length) {
      setError('Функции должны иметь одинаковое количество точек');
      return false;
    }

    // Проверяем, что точки имеют одинаковые x-значения
    for (let i = 0; i < firstPoints.length; i++) {
      if (Math.abs(firstPoints[i].x - secondPoints[i].x) > 0.001) {
        setError('Функции должны иметь одинаковые значения X для всех точек');
        return false;
      }
    }

    if (!operation) {
      setError('Выберите операцию');
      return false;
    }

    return true;
  };

  const performOperation = () => {
    if (!validateOperations()) {
      return;
    }

    try {
      setError(null);
      let newPoints: PointDTO[] = [];

      switch (operation) {
        case 'add':
          newPoints = firstPoints.map((point, index) => ({
            ...point,
            y: point.y + secondPoints[index].y
          }));
          break;
        case 'subtract':
          newPoints = firstPoints.map((point, index) => ({
            ...point,
            y: point.y - secondPoints[index].y
          }));
          break;
        case 'multiply':
          newPoints = firstPoints.map((point, index) => ({
            ...point,
            y: point.y * secondPoints[index].y
          }));
          break;
        case 'divide':
          // Проверяем деление на ноль
          for (const point of secondPoints) {
            if (Math.abs(point.y) < 0.001) {
              throw new Error('Обнаружено деление на ноль');
            }
          }
          newPoints = firstPoints.map((point, index) => ({
            ...point,
            y: point.y / secondPoints[index].y
          }));
          break;
        default:
          setError('Выберите операцию');
          return;
      }

      setResultPoints(newPoints);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ошибка при выполнении операции');
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

      // Создаем новую функцию для результата
      const operationNames = {
        'add': 'Сложение',
        'subtract': 'Вычитание',
        'multiply': 'Умножение',
        'divide': 'Деление'
      };

      const functionName = `${operationNames[operation]}: ${firstFunction?.name} и ${secondFunction?.name}`;

      const resultFunction = await functionService.createFunction({
        name: functionName,
        format: null,
        userId: currentUser.id,
        funcResult: `Результат операции ${operationNames[operation]}`
      });

      // Добавляем точки к новой функции
      for (const point of resultPoints) {
        await functionService.addPoint(resultFunction.id, point.x, point.y);
      }

      toast.success('Результат успешно сохранен');
      navigate(`/functions/${resultFunction.id}/graph`);
    } catch (error) {
      console.error('Ошибка при сохранении результата:', error);
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
                      value={firstFunction?.id || ''}
                      onChange={handleFirstFunctionChange}
                      label="Первое слагаемое (уменьшаемое)"
                    >
                      {functions.map(func => (
                        <MenuItem key={func.id} value={func.id}>
                          {func.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={2} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                  <FormControl fullWidth required>
                    <Select
                      value={operation || ''}
                      onChange={(e) => setOperation(e.target.value as any)}
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
                      value={secondFunction?.id || ''}
                      onChange={handleSecondFunctionChange}
                      label="Второе слагаемое (вычитаемое)"
                    >
                      {functions.map(func => (
                        <MenuItem key={func.id} value={func.id}>
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
                          <TableCell>{point.x.toFixed(3)}</TableCell>
                          <TableCell>{point.y.toFixed(3)}</TableCell>
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
                          <TableCell>{point.x.toFixed(3)}</TableCell>
                          <TableCell>{point.y.toFixed(3)}</TableCell>
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
                      {resultPoints.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>{point.x.toFixed(3)}</TableCell>
                          <TableCell sx={{ color: 'primary.main', fontWeight: 'bold' }}>
                            {point.y.toFixed(4)}
                          </TableCell>
                          <TableCell>{firstPoints[index]?.y.toFixed(4)}</TableCell>
                          <TableCell>{secondPoints[index]?.y.toFixed(4)}</TableCell>
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