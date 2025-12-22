import React, { useState, useEffect } from 'react';
import {
  Box, Typography, TextField, Button, Grid, Card, CardContent,
  IconButton, MenuItem, FormControl, InputLabel, Select,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, CircularProgress, Tooltip, Divider
} from '@mui/material';
import {
  Add as AddIcon, Delete as DeleteIcon, Save as SaveIcon,
  ArrowBack as ArrowBackIcon, Functions as FunctionsIcon, DataArray as DataArrayIcon
} from '@mui/icons-material';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import functionService from '../services/functionService';
import toast from 'react-hot-toast';

interface FunctionEditorProps {
  mathFunctionMode?: boolean;
}

const FunctionEditor: React.FC<FunctionEditorProps> = ({ mathFunctionMode = false }) => {
  const [name, setName] = useState('');
  const [points, setPoints] = useState<{ x: number; y: number }[]>([{ x: 0, y: 0 }]);
  const [loading, setLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [mathFunctionType, setMathFunctionType] = useState('identity');
  const [intervalStart, setIntervalStart] = useState(0);
  const [intervalEnd, setIntervalEnd] = useState(10);
  const [pointsCount, setPointsCount] = useState(10);
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const state = location.state as { fromDashboard?: boolean } | null;

  useEffect(() => {
    if (id) {
      loadFunctionData(Number(id));
    } else if (!mathFunctionMode) {
      // Если создаем новую функцию из массива, добавляем две начальные точки
      setPoints([
        { x: 0, y: 0 },
        { x: 1, y: 1 }
      ]);
    }
  }, [id, mathFunctionMode]);

  const loadFunctionData = async (functionId: number) => {
    try {
      setLoading(true);
      const funcData = await functionService.getFunctionById(functionId, true);
      const pointsData = await functionService.getFunctionPoints(functionId);

      setName(funcData.name);
      setPoints(pointsData.map(p => ({ x: p.x, y: p.y })));
      setIsEditing(true);
    } catch (error) {
      console.error('Ошибка при загрузке функции:', error);
      toast.error('Не удалось загрузить функцию');
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleAddPoint = () => {
    if (points.length >= 100) {
      toast.error('Максимальное количество точек: 100');
      return;
    }
    setPoints([...points, { x: 0, y: 0 }]);
  };

  const handlePointChange = (index: number, field: 'x' | 'y', value: string) => {
    const newPoints = [...points];
    if (value === '') {
      newPoints[index] = {
        ...newPoints[index],
        [field]: 0
      };
    } else {
      const numValue = parseFloat(value);
      newPoints[index] = {
        ...newPoints[index],
        [field]: isNaN(numValue) ? 0 : numValue
      };
    }
    setPoints(newPoints);
  };

  const handleRemovePoint = (index: number) => {
    if (points.length <= 1) {
      toast.error('Функция должна содержать хотя бы одну точку');
      return;
    }
    const newPoints = points.filter((_, i) => i !== index);
    setPoints(newPoints);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (points.length < 1) {
      toast.error('Функция должна содержать хотя бы одну точку');
      return;
    }

    // Сортируем точки по X
    const sortedPoints = [...points].sort((a, b) => a.x - b.x);
    setPoints(sortedPoints);

    try {
      setLoading(true);

      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = currentUser.id || 1;

      if (mathFunctionMode && !isEditing) {
        // Создание функции из математической функции
        const newFunction = await functionService.createFromMathFunction(
          name || `Функция ${mathFunctionType}`,
          userId,
          `Результат функции ${mathFunctionType}`,
          mathFunctionType,
          intervalStart,
          intervalEnd,
          pointsCount
        );

        toast.success('Функция успешно создана');
        navigate(`/functions/${newFunction.id}/graph`);
      } else if (isEditing && id) {
        // Обновление существующей функции
        await functionService.updateFunction(Number(id), name, "Auto-generated");

        // Для упрощения, пересоздадим все точки
        const existingPoints = await functionService.getFunctionPoints(Number(id));
        for (const point of existingPoints) {
          await functionService.deletePoint(point.id);
        }

        for (const point of sortedPoints) {
          await functionService.addPoint(Number(id), point.x, point.y);
        }

        toast.success('Функция успешно обновлена');
        navigate(`/functions/${id}/graph`);
      } else {
        // Создание новой функции из массива точек
        const newFunction = await functionService.createFunctionFromArray({
          name: name || 'Новая функция',
          userId: userId,
          funcResult: "Auto-generated",
          points: sortedPoints
        });

        toast.success('Функция успешно создана');
        navigate(`/functions/${newFunction.id}/graph`);
      }

    } catch (error: any) {
      console.error('Ошибка при сохранении функции:', error);

      let errorMessage = 'Не удалось сохранить функцию. Проверьте введенные данные.';

      if (error.response) {
        // Обработка ошибок от сервера
        if (error.response.data) {
          errorMessage = error.response.data.message || error.response.data.error || errorMessage;
        }
      }

      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate(state?.fromDashboard ? '/dashboard' : '/functions/new')} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1">
          {isEditing ? 'Редактировать функцию' : mathFunctionMode ? 'Создать из математической функции' : 'Создать из массива точек'}
        </Typography>
      </Box>

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              {!isEditing && mathFunctionMode && (
                <Grid item xs={12}>
                  <Card sx={{ mb: 3 }}>
                    <CardContent>
                      <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                        <FunctionsIcon sx={{ mr: 1 }} /> Параметры математической функции
                      </Typography>
                      <Grid container spacing={2}>
                        <Grid item xs={12} sm={6}>
                          <FormControl fullWidth required>
                            <InputLabel>Тип функции</InputLabel>
                            <Select
                              value={mathFunctionType}
                              onChange={(e) => setMathFunctionType(e.target.value as string)}
                              label="Тип функции"
                            >
                              <MenuItem value="identity">Тождественная (y = x)</MenuItem>
                              <MenuItem value="square">Квадратичная (y = x²)</MenuItem>
                              <MenuItem value="cube">Кубическая (y = x³)</MenuItem>
                              <MenuItem value="sqrt">Квадратный корень (y = √x)</MenuItem>
                              <MenuItem value="sin">Синус (y = sin(x))</MenuItem>
                              <MenuItem value="cos">Косинус (y = cos(x))</MenuItem>
                              <MenuItem value="exp">Экспонента (y = e^x)</MenuItem>
                            </Select>
                          </FormControl>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                          <TextField
                            fullWidth
                            label="Название функции"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                            error={!name.trim()}
                            helperText={!name.trim() ? "Обязательное поле" : ""}
                          />
                        </Grid>
                        <Grid item xs={12} sm={4}>
                          <TextField
                            fullWidth
                            type="number"
                            label="Начало интервала"
                            value={intervalStart}
                            onChange={(e) => setIntervalStart(parseFloat(e.target.value) || 0)}
                            required
                            inputProps={{ step: 0.1 }}
                          />
                        </Grid>
                        <Grid item xs={12} sm={4}>
                          <TextField
                            fullWidth
                            type="number"
                            label="Конец интервала"
                            value={intervalEnd}
                            onChange={(e) => setIntervalEnd(parseFloat(e.target.value) || 0)}
                            required
                            inputProps={{ step: 0.1 }}
                          />
                        </Grid>
                        <Grid item xs={12} sm={4}>
                          <TextField
                            fullWidth
                            type="number"
                            label="Количество точек"
                            value={pointsCount}
                            onChange={(e) => setPointsCount(Math.max(2, parseInt(e.target.value) || 2))}
                            required
                            inputProps={{ min: 2 }}
                            helperText="Минимум 2 точки"
                          />
                        </Grid>
                      </Grid>
                    </CardContent>
                  </Card>
                </Grid>
              )}

              {!mathFunctionMode && (
                <>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="Название функции"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      required
                      error={!name.trim()}
                      helperText={!name.trim() ? "Обязательное поле" : ""}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                      <DataArrayIcon sx={{ mr: 1 }} /> Точки функции
                    </Typography>
                    <TableContainer component={Paper}>
                      <Table>
                        <TableHead>
                          <TableRow>
                            <TableCell>Индекс</TableCell>
                            <TableCell>X</TableCell>
                            <TableCell>Y</TableCell>
                            <TableCell>Действия</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {points.map((point, index) => (
                            <TableRow key={index}>
                              <TableCell>{index + 1}</TableCell>
                              <TableCell>
                                <TextField
                                  type="number"
                                  value={point.x}
                                  onChange={(e) => handlePointChange(index, 'x', e.target.value)}
                                  size="small"
                                  fullWidth
                                  inputProps={{ step: "any" }}
                                />
                              </TableCell>
                              <TableCell>
                                <TextField
                                  type="number"
                                  value={point.y}
                                  onChange={(e) => handlePointChange(index, 'y', e.target.value)}
                                  size="small"
                                  fullWidth
                                  inputProps={{ step: "any" }}
                                />
                              </TableCell>
                              <TableCell>
                                <Tooltip title="Удалить точку">
                                  <IconButton
                                    color="error"
                                    onClick={() => handleRemovePoint(index)}
                                    disabled={points.length <= 1}
                                  >
                                    <DeleteIcon />
                                  </IconButton>
                                </Tooltip>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                    <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" color="text.secondary">
                        Всего точек: {points.length} (максимум 100)
                      </Typography>
                      <Button
                        variant="outlined"
                        startIcon={<AddIcon />}
                        onClick={handleAddPoint}
                        disabled={points.length >= 100}
                      >
                        Добавить точку
                      </Button>
                    </Box>
                  </Grid>
                </>
              )}

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
                  <Button
                    variant="outlined"
                    onClick={() => navigate(state?.fromDashboard ? '/dashboard' : '/functions/new')}
                  >
                    Отмена
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={<SaveIcon />}
                    disabled={loading}
                  >
                    {loading ? <CircularProgress size={24} /> : (isEditing ? 'Сохранить изменения' : 'Создать функцию')}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
};

export default FunctionEditor;