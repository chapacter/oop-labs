// src/components/FunctionEditor.tsx
import React, { useEffect, useRef, useState } from 'react';
import {
  Box, Typography, TextField, Button, Grid, Card, CardContent,
  IconButton, MenuItem, FormControl, InputLabel, Select,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, CircularProgress, Tooltip, Divider, Collapse
} from '@mui/material';
import {
  Add as AddIcon, Delete as DeleteIcon, Save as SaveIcon,
  ArrowBack as ArrowBackIcon, Functions as FunctionsIcon, DataArray as DataArrayIcon,
  UploadFile as UploadFileIcon
} from '@mui/icons-material';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import functionService from '../services/functionService';
import toast from 'react-hot-toast';

interface FunctionEditorProps {
  mathFunctionMode?: boolean;
}

const MAX_POINTS = 1000;

const parseCsvTextToPoints = (text: string): { x: number; y: number }[] => {
  // Разрешаем разделители: ',' ';' '\t' или пробелы
  const lines = text.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
  if (!lines.length) return [];

  // Если первая строка содержит буквы 'x' или 'y' — считаем её заголовком
  const headerParts = lines[0].split(/[,;\t\s]+/).map(s => s.trim().toLowerCase());
  let startIndex = 0;
  if (headerParts.length >= 2 && (headerParts[0] === 'x' || headerParts[1] === 'y' || headerParts.includes('x'))) {
    startIndex = 1;
  }

  const points: { x: number; y: number }[] = [];
  for (let i = startIndex; i < lines.length; i++) {
    const parts = lines[i].split(/[,;\t\s]+/).map(s => s.trim()).filter(Boolean);
    if (parts.length < 2) continue;
    const x = parseFloat(parts[0]);
    const y = parseFloat(parts[1]);
    if (!Number.isFinite(x) || !Number.isFinite(y)) continue;
    points.push({ x, y });
  }
  return points;
};

const FunctionEditor: React.FC<FunctionEditorProps> = ({ mathFunctionMode = false }) => {
  const [name, setName] = useState('');
  const [points, setPoints] = useState<{ x: number; y: number }[]>([{ x: 0, y: 0 }]);
  const [loading, setLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [mathFunctionType, setMathFunctionType] = useState('identity');
  const [intervalStart, setIntervalStart] = useState(0);
  const [intervalEnd, setIntervalEnd] = useState(10);
  const [pointsCount, setPointsCount] = useState(10);

  // Bulk add UI state
  const [bulkOpen, setBulkOpen] = useState(false);
  const [bulkText, setBulkText] = useState('');

  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const state = location.state as { fromDashboard?: boolean } | null;

  // refs to inputs for keyboard navigation: keys 'x-INDEX' and 'y-INDEX'
  const inputRefs = useRef<Record<string, HTMLInputElement | null>>({});

  useEffect(() => {
    if (id) {
      loadFunctionData(Number(id));
    } else if (!mathFunctionMode) {
      setPoints([
        { x: 0, y: 0 },
        { x: 1, y: 1 }
      ]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, mathFunctionMode]);

  const loadFunctionData = async (functionId: number) => {
    try {
      setLoading(true);
      const funcData = await functionService.getFunctionById(functionId, true);
      const pointsData = await functionService.getFunctionPoints(functionId);

      setName(funcData.name);
      setPoints(pointsData.map((p: any) => ({ x: Number(p.x), y: Number(p.y) })));
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
    if (points.length >= MAX_POINTS) {
      toast.error(`Максимальное количество точек: ${MAX_POINTS}`);
      return;
    }
    setPoints(prev => [...prev, { x: 0, y: 0 }]);
  };

  const handleBulkAdd = () => {
    if (!bulkText.trim()) {
      toast.error('Вставьте точки в формате "x,y" — по одной точке в строке.');
      return;
    }

    const lines = bulkText.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
    const parsed: { x: number; y: number }[] = [];
    const invalidLines: number[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const parts = line.split(/[,;\s]+/).filter(Boolean);
      if (parts.length < 2) {
        invalidLines.push(i + 1);
        continue;
      }
      const x = parseFloat(parts[0]);
      const y = parseFloat(parts[1]);
      if (!Number.isFinite(x) || !Number.isFinite(y)) {
        invalidLines.push(i + 1);
        continue;
      }
      parsed.push({ x, y });
    }

    if (invalidLines.length) {
      toast.error(`Некорректные строки: ${invalidLines.slice(0, 10).join(', ')}${invalidLines.length > 10 ? ', ...' : ''}`);
      return;
    }

    if (points.length + parsed.length > MAX_POINTS) {
      toast.error(`Нельзя добавить: превысится лимит ${MAX_POINTS} точек (текущие: ${points.length}, добавляемые: ${parsed.length})`);
      return;
    }

    setPoints(prev => [...prev, ...parsed]);
    setBulkText('');
    setBulkOpen(false);
    toast.success(`Добавлено ${parsed.length} точек`);
    setTimeout(() => {
      const idx = points.length;
      inputRefs.current[`x-${idx}`]?.focus();
      inputRefs.current[`x-${idx}`]?.select();
    }, 50);
  };

  // CSV file upload handler
  const handleCsvUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (ev) => {
      const content = String(ev.target?.result || '');
      const parsed = parseCsvTextToPoints(content);
      if (!parsed.length) {
        toast.error('CSV не содержит корректных точек (ожидается 2 колонки x и y).');
        return;
      }
      if (points.length + parsed.length > MAX_POINTS) {
        toast.error(`Нельзя добавить: превысится лимит ${MAX_POINTS} точек (текущие: ${points.length}, добавляемые: ${parsed.length})`);
        return;
      }
      setPoints(prev => [...prev, ...parsed]);
      toast.success(`Импортировано ${parsed.length} точек из CSV`);
    };
    reader.readAsText(file);
    // очистим значение чтобы можно было загрузить тот же файл повторно
    (e.target as HTMLInputElement).value = '';
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
    setTimeout(() => {
      const nextIndex = Math.min(index, newPoints.length - 1);
      inputRefs.current[`x-${nextIndex}`]?.focus();
    }, 0);
  };

  const handleInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, index: number, field: 'x' | 'y') => {
    const key = e.key.toLowerCase();
    if (!['w', 'a', 's', 'd', 'arrowup', 'arrowdown', 'arrowleft', 'arrowright'].includes(key)) return;
    e.preventDefault();

    const total = points.length;
    const col = field;
    if (key === 'a' || key === 'arrowleft') {
      const targetKey = `x-${index}`;
      inputRefs.current[targetKey]?.focus();
      inputRefs.current[targetKey]?.select();
      return;
    }
    if (key === 'd' || key === 'arrowright') {
      const targetKey = `y-${index}`;
      inputRefs.current[targetKey]?.focus();
      inputRefs.current[targetKey]?.select();
      return;
    }
    if (key === 'w' || key === 'arrowup') {
      const prevIndex = Math.max(0, index - 1);
      const targetKey = `${col}-${prevIndex}`;
      inputRefs.current[targetKey]?.focus();
      inputRefs.current[targetKey]?.select();
      return;
    }
    if (key === 's' || key === 'arrowdown') {
      const nextIndex = Math.min(total - 1, index + 1);
      const targetKey = `${col}-${nextIndex}`;
      inputRefs.current[targetKey]?.focus();
      inputRefs.current[targetKey]?.select();
      return;
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (points.length < 1) {
      toast.error('Функция должна содержать хотя бы одну точку');
      return;
    }

    const sortedPoints = [...points].sort((a, b) => a.x - b.x);
    setPoints(sortedPoints);

    try {
      setLoading(true);

      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      const userId = currentUser.id || 1;

      if (mathFunctionMode && !isEditing) {
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
        await functionService.updateFunction(Number(id), name, "Auto-generated");
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
        if (error.response.data) {
          errorMessage = error.response.data.message || error.response.data.error || errorMessage;
        }
      }
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

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

                    <Box sx={{ mb: 1, display: 'flex', gap: 1, alignItems: 'center' }}>
                      <Button variant="outlined" startIcon={<AddIcon />} onClick={handleAddPoint} disabled={points.length >= MAX_POINTS}>
                        Добавить точку
                      </Button>

                      <Button variant="outlined" onClick={() => setBulkOpen(prev => !prev)}>
                        {bulkOpen ? 'Скрыть массовое добавление' : 'Добавить несколько точек'}
                      </Button>

                      <input
                        accept=".csv,text/csv"
                        id="csv-upload"
                        type="file"
                        style={{ display: 'none' }}
                        onChange={handleCsvUpload}
                      />
                      <label htmlFor="csv-upload">
                        <Button variant="outlined" component="span" startIcon={<UploadFileIcon />}>
                          Импорт CSV
                        </Button>
                      </label>

                      <Typography variant="body2" color="text.secondary" sx={{ ml: 'auto' }}>
                        Всего точек: {points.length} (максимум {MAX_POINTS})
                      </Typography>
                    </Box>

                    <Collapse in={bulkOpen}>
                      <Box sx={{ mb: 2 }}>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                          Вставьте точки построчно в формате <code>x,y</code> или <code>x y</code>. Пример:
                          <br />
                          <code>0,0</code><br />
                          <code>0.1,0.0998</code><br />
                          <code>0.2,0.1987</code>
                        </Typography>
                        <TextField
                          multiline
                          minRows={3}
                          fullWidth
                          placeholder="x,y (каждая точка в новой строке)"
                          value={bulkText}
                          onChange={(e) => setBulkText(e.target.value)}
                        />
                        <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                          <Button variant="contained" onClick={handleBulkAdd}>Добавить точки</Button>
                          <Button variant="outlined" onClick={() => { setBulkText(''); setBulkOpen(false); }}>Отмена</Button>
                        </Box>
                      </Box>
                    </Collapse>

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
                                  inputRef={(el) => { inputRefs.current[`x-${index}`] = el; }}
                                  type="number"
                                  value={point.x}
                                  onChange={(e) => handlePointChange(index, 'x', e.target.value)}
                                  size="small"
                                  fullWidth
                                  inputProps={{ step: "any" }}
                                  onKeyDown={(e) => handleInputKeyDown(e as any, index, 'x')}
                                />
                              </TableCell>
                              <TableCell>
                                <TextField
                                  inputRef={(el) => { inputRefs.current[`y-${index}`] = el; }}
                                  type="number"
                                  value={point.y}
                                  onChange={(e) => handlePointChange(index, 'y', e.target.value)}
                                  size="small"
                                  fullWidth
                                  inputProps={{ step: "any" }}
                                  onKeyDown={(e) => handleInputKeyDown(e as any, index, 'y')}
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
