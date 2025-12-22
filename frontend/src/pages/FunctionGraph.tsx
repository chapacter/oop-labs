// src/components/FunctionGraph.tsx
import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  Box, Typography, Card, CardContent, IconButton, Button, Grid,
  ToggleButtonGroup, ToggleButton, useMediaQuery, useTheme, Container,
  Slider, Tooltip, Paper, CircularProgress, Chip
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon, ShowChart as ShowChartIcon,
  ScatterPlot as ScatterPlotIcon, BarChart as BarChartIcon,
  ZoomIn as ZoomInIcon, ZoomOut as ZoomOutIcon,
  RestartAlt as RestartAltIcon, Fullscreen as FullscreenIcon
} from '@mui/icons-material';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement,
  BarElement, Title, Tooltip as ChartTooltip, Legend, Filler, ChartOptions
} from 'chart.js';
import zoomPlugin from 'chartjs-plugin-zoom';
import { useNavigate, useParams } from 'react-router-dom';
import functionService from '../services/functionService';
import { FunctionDTO, PointDTO, ChartData as CustomChartData } from '../models';
import toast from 'react-hot-toast';
import authService from '../services/authService';

// Регистрируем компоненты Chart.js + плагин zoom
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, ChartTooltip, Legend, Filler, zoomPlugin);

const FunctionGraph: React.FC = () => {
  const [funcData, setFuncData] = useState<FunctionDTO | null>(null);
  const [pointsRaw, setPointsRaw] = useState<any>(null); // оригинал от API (для отладки)
  const [points, setPoints] = useState<PointDTO[]>([]);
  const [chartData, setChartData] = useState<CustomChartData[]>([]);
  const [loading, setLoading] = useState(true);
  const [zoomLevel, setZoomLevel] = useState(1);
  const [panOffset, setPanOffset] = useState({ x: 0, y: 0 });
  const [chartType, setChartType] = useState<'line' | 'scatter' | 'bar'>('line');
  const [fullscreen, setFullscreen] = useState(false);

  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const chartRef = useRef<any>(null);

  useEffect(() => {
    if (id) loadFunctionData(Number(id));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // Универсальная нормализация ответа API — пытается извлечь массив точек в различных форматах
  const normalizePoints = (raw: any): any[] => {
    if (!raw) return [];
    if (Array.isArray(raw)) return raw;
    if (raw.data && Array.isArray(raw.data)) return raw.data;
    if (raw.items && Array.isArray(raw.items)) return raw.items;
    if (raw.content && Array.isArray(raw.content)) return raw.content;
    if (raw.points && Array.isArray(raw.points)) return raw.points;
    if (raw.result && Array.isArray(raw.result)) return raw.result;
    // Иногда API возвращает { page: {...}, elements: [...] }
    if (raw.elements && Array.isArray(raw.elements)) return raw.elements;
    // Попытка собрать все массивы внутри объекта
    const arrays = Object.values(raw).filter(v => Array.isArray(v)).flat();
    if (arrays.length) return arrays;
    // Если ничего не подошло — возвращаем пустой массив
    return [];
  };

  const loadFunctionData = async (functionId: number) => {
    try {
      setLoading(true);
      const currentUser = authService.getCurrentUser();
      if (!currentUser) {
        navigate('/login');
        return;
      }

      const func = await functionService.getFunctionById(functionId, true);
      setFuncData(func);

      // Получаем "сырые" точки (возможен разный формат)
      const ptsRaw = await functionService.getFunctionPoints(functionId);
      setPointsRaw(ptsRaw);

      const normalized = normalizePoints(ptsRaw);

      // Приводим к нужной форме и фильтруем NaN
      const numeric = normalized
        .map((p: any) => ({ x: Number(p.x), y: Number(p.y) }))
        .filter((pt: any) => Number.isFinite(pt.x) && Number.isFinite(pt.y));

      // Журналируем для отладки
      console.info('[FunctionGraph] нормализовано точек (raw -> normalized -> numeric):', {
        rawLength: (Array.isArray(ptsRaw) ? ptsRaw.length : undefined),
        normalizedLength: normalized.length,
        numericLength: numeric.length
      });

      setPoints(Array.isArray(normalized) ? normalized : []);
      setChartData(numeric);
    } catch (error) {
      console.error('Ошибка при загрузке данных функции:', error);
      toast.error('Не удалось загрузить данные функции');
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleZoomIn = () => setZoomLevel(prev => Math.min(prev * 1.5, 20));
  const handleZoomOut = () => setZoomLevel(prev => Math.max(prev / 1.5, 0.1));
  const handleResetZoom = () => {
    setZoomLevel(1);
    setPanOffset({ x: 0, y: 0 });
    try { chartRef.current?.resetZoom?.(); } catch (e) { /* ignore */ }
  };

  // Сортировка по X — обязательно
  const sortedData = useMemo(() => {
    return chartData.slice().sort((a, b) => Number(a.x) - Number(b.x));
  }, [chartData]);

  // тензия (сглаживание) отключаем при большом количестве точек
  const tension = sortedData.length > 500 ? 0 : 0.4;

  // Отключаем decimation явно (чтобы Chart.js не "подрезал" точки)
  // И конфиг для zoom/pan
  const options: ChartOptions<'line'> = useMemo(() => {
    const xValues = sortedData.map(p => Number((p as any).x)).filter(v => !isNaN(v));
    const yValues = sortedData.map(p => Number((p as any).y)).filter(v => !isNaN(v));

    const defaultMinX = xValues.length ? Math.min(...xValues) : -1;
    const defaultMaxX = xValues.length ? Math.max(...xValues) : 1;
    const defaultMinY = yValues.length ? Math.min(...yValues) : -1;
    const defaultMaxY = yValues.length ? Math.max(...yValues) : 1;

    const xRange = (defaultMaxX - defaultMinX) || 1;
    const yRange = (defaultMaxY - defaultMinY) || 1;

    const effectiveXRange = xRange / Math.max(zoomLevel, 0.0001);
    const effectiveYRange = yRange / Math.max(zoomLevel, 0.0001);
    const panShiftX = (panOffset.x / 100) * xRange;
    const panShiftY = (panOffset.y / 100) * yRange;

    const minX = (defaultMinX + defaultMaxX) / 2 - effectiveXRange / 2 + panShiftX;
    const maxX = (defaultMinX + defaultMaxX) / 2 + effectiveXRange / 2 + panShiftX;
    const minY = (defaultMinY + defaultMaxY) / 2 - effectiveYRange / 2 + panShiftY;
    const maxY = (defaultMinY + defaultMaxY) / 2 + effectiveYRange / 2 + panShiftY;

    const isScatterChart = chartType === 'scatter';
    const isBarChart = chartType === 'bar';

    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        title: {
          display: true,
          text: funcData ? `График функции: ${funcData.name}` : 'График функции',
          font: { size: 18, weight: 'bold' },
          color: theme.palette.text.primary
        },
        tooltip: {
          mode: isScatterChart ? 'nearest' : 'index',
          intersect: isScatterChart,
        },
        legend: { display: false },
        // явное отключение decimation (иногда включается глобально)
        decimation: { enabled: false },
        // zoom/pan конфиг
        zoom: {
          // @ts-ignore
          zoom: {
            wheel: { enabled: true },
            pinch: { enabled: true },
            mode: 'xy'
          },
          // @ts-ignore
          pan: { enabled: true, mode: 'xy' },
          limits: { x: { min: -1e12, max: 1e12 }, y: { min: -1e12, max: 1e12 } }
        }
      },
      scales: {
        x: {
          type: 'linear',
          position: 'bottom',
          title: { display: true, text: 'X', font: { weight: 'bold' } },
          grid: { color: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.08)' },
          ticks: { color: theme.palette.text.secondary },
          min: minX,
          max: maxX
        },
        y: {
          title: { display: true, text: 'Y', font: { weight: 'bold' } },
          grid: { color: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.08)' },
          ticks: { color: theme.palette.text.secondary },
          min: minY,
          max: maxY,
          ...(isBarChart ? { beginAtZero: true } : {})
        }
      },
      elements: {
        point: {
          radius: chartType === 'scatter' ? 5 : 4,
          hoverRadius: chartType === 'scatter' ? 8 : 6,
          backgroundColor: theme.palette.primary.main,
          borderColor: theme.palette.background.paper,
          borderWidth: 2
        },
        line: {
          borderWidth: 3,
          borderColor: theme.palette.primary.main,
          tension,
          fill: false
        },
        bar: {
          backgroundColor: theme.palette.primary.main,
          borderColor: theme.palette.primary.dark,
          borderWidth: 1,
          borderRadius: 4,
          borderSkipped: false
        }
      },
      interaction: {
        mode: isScatterChart ? 'nearest' : 'index',
        axis: 'x',
        intersect: isScatterChart
      },
      animation: { duration: 0 },
      hover: {
        mode: isScatterChart ? 'nearest' : 'index',
        intersect: isScatterChart
      }
    } as ChartOptions<'line'>;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sortedData, zoomLevel, panOffset, funcData, chartType, theme.palette.mode, theme.palette.primary.main, theme.palette.text.primary, theme.palette.text.secondary]);

  // Подготовка датасета (отображаем все sortedData)
  const data = useMemo(() => {
    const isLineChart = chartType === 'line';
    const isScatterChart = chartType === 'scatter';
    const isBarChart = chartType === 'bar';

    const ds: any = {
      label: funcData?.name || 'Функция',
      data: sortedData.map(pt => ({ x: Number(pt.x), y: Number(pt.y) })),
      parsing: { xAxisKey: 'x', yAxisKey: 'y' },
      borderColor: theme.palette.primary.main,
      backgroundColor: `${theme.palette.primary.main}22`,
      borderWidth: 3,
      fill: isLineChart,
      tension: isLineChart ? tension : 0,
      pointBackgroundColor: theme.palette.primary.main,
      pointBorderColor: theme.palette.background.paper,
      pointBorderWidth: 2,
      pointRadius: isScatterChart ? 5 : 4,
      pointHoverRadius: isScatterChart ? 8 : 6
    };

    if (isBarChart) ds.type = 'bar';
    if (isScatterChart) { ds.type = 'scatter'; ds.showLine = false; }
    if (isLineChart) ds.type = 'line';

    return { datasets: [ds] };
  }, [sortedData, funcData?.name, chartType, tension, theme.palette.primary.main, theme.palette.background.paper]);

  const toggleFullscreen = () => setFullscreen(prev => !prev);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Container maxWidth={fullscreen ? false : 'xl'} sx={{ mt: fullscreen ? 0 : 3 }}>
      <Box sx={{
        display: 'flex', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2,
        position: 'sticky', top: 0, bgcolor: 'background.paper', zIndex: 1000, py: 1
      }}>
        <IconButton onClick={() => navigate('/dashboard')} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>

        <Typography variant="h4" component="h1" fontWeight="bold">График функции</Typography>

        <Box sx={{ ml: 2 }}>
          {/* Отладочная информация: сколько точек пришло и сколько используется */}
          <Chip label={`API: ${Array.isArray(pointsRaw) ? pointsRaw.length : (pointsRaw ? 'obj' : 0)} → Используется: ${chartData.length}`} size="small" />
        </Box>

        <Box sx={{ ml: 'auto', display: 'flex', gap: 1 }}>
          <Tooltip title="Полный экран"><IconButton onClick={toggleFullscreen}><FullscreenIcon /></IconButton></Tooltip>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Card sx={{ height: fullscreen ? 'calc(100vh - 64px)' : 500, transition: 'all 0.3s ease' }}>
            <CardContent sx={{ height: '100%', position: 'relative' }}>
              {sortedData.length > 0 ? (
                <Box sx={{ height: '100%' }}>
                  <Line ref={chartRef} options={options} data={data} />
                </Box>
              ) : (
                <Box sx={{
                  display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center',
                  height: '100%', textAlign: 'center', p: 3
                }}>
                  <Typography variant="h6" color="text.secondary" gutterBottom>Нет данных для отображения графика</Typography>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                    Эта функция не содержит точек или произошла ошибка при загрузке данных.
                  </Typography>
                  <Button variant="outlined" onClick={() => navigate(`/functions/${id}/edit`)}>Добавить точки</Button>
                </Box>
              )}

              {/* Панель управления графиком */}
              <Paper sx={{
                position: 'absolute', bottom: 16, left: 16, right: 16, p: 1,
                bgcolor: 'background.paper', borderRadius: 2, boxShadow: 3,
                display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'center'
              }}>
                <ToggleButtonGroup
                  value={chartType}
                  exclusive
                  onChange={(_, newType) => newType && setChartType(newType)}
                  size="small"
                  sx={{ mr: 2 }}
                >
                  <ToggleButton value="line" title="Линейный график"><ShowChartIcon /></ToggleButton>
                  <ToggleButton value="scatter" title="Точечный график"><ScatterPlotIcon /></ToggleButton>
                  <ToggleButton value="bar" title="Столбчатая диаграмма"><BarChartIcon /></ToggleButton>
                </ToggleButtonGroup>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Tooltip title="Уменьшить (локально)"><IconButton onClick={handleZoomOut} size="small"><ZoomOutIcon /></IconButton></Tooltip>
                  <Box sx={{ width: isMobile ? 100 : 150, mx: 1 }}>
                    <Slider value={zoomLevel} min={0.1} max={10} step={0.1}
                      onChange={(_, value) => setZoomLevel(value as number)}
                      aria-labelledby="zoom-slider" size="small" />
                  </Box>
                  <Tooltip title="Увеличить (локально)"><IconButton onClick={handleZoomIn} size="small"><ZoomInIcon /></IconButton></Tooltip>
                  <Tooltip title="Сбросить (и плагин-зум)"><IconButton onClick={handleResetZoom} size="small"><RestartAltIcon /></IconButton></Tooltip>
                </Box>
              </Paper>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default FunctionGraph;
