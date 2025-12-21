import React, { useState, useEffect, useRef } from 'react';
import {
  Box, Typography, Card, CardContent, IconButton, Button, Grid,
  ToggleButtonGroup, ToggleButton, useMediaQuery, useTheme, Container,
  Slider, Tooltip, Paper, CircularProgress
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon, Edit as EditIcon, Delete as DeleteIcon,
  Save as SaveIcon, Add as AddIcon, Remove as RemoveIcon,
  ZoomIn as ZoomInIcon, ZoomOut as ZoomOutIcon,
  RestartAlt as RestartAltIcon, Fullscreen as FullscreenIcon
} from '@mui/icons-material';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement,
  Title, Tooltip as ChartTooltip, Legend, Filler, ChartData, ChartOptions
} from 'chart.js';
import { useNavigate, useParams } from 'react-router-dom';
import functionService, { FunctionDTO, PointDTO, ChartData as CustomChartData } from '../services/functionService';
import toast from 'react-hot-toast';
import authService from '../services/authService';

// Регистрация компонентов Chart.js
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, ChartTooltip, Legend, Filler);

const FunctionGraph: React.FC = () => {
  const [funcData, setFuncData] = useState<FunctionDTO | null>(null);
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
    if (id) {
      loadFunctionData(Number(id));
    }
  }, [id]);

  const loadFunctionData = async (functionId: number) => {
    try {
      setLoading(true);

      // Проверяем аутентификацию
      const currentUser = authService.getCurrentUser();
      if (!currentUser) {
        navigate('/login');
        return;
      }

      // Загружаем данные функции
      const func = await functionService.getFunctionById(functionId, true);
      setFuncData(func);

      // Загружаем точки функции
      const pts = await functionService.getFunctionPoints(functionId);
      setPoints(pts);

      // Подготавливаем данные для графика
      const data = pts.map(p => ({ x: p.x, y: p.y }));
      setChartData(data);
    } catch (error) {
      console.error('Ошибка при загрузке данных функции:', error);
      toast.error('Не удалось загрузить данные функции');
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!id || !funcData) return;

    if (window.confirm('Вы уверены, что хотите удалить эту функцию?')) {
      try {
        await functionService.deleteFunction(Number(id));
        toast.success('Функция успешно удалена');
        navigate('/dashboard');
      } catch (error) {
        console.error('Ошибка при удалении функции:', error);
        toast.error('Не удалось удалить функцию');
      }
    }
  };

  const handleZoomIn = () => {
    setZoomLevel(prev => Math.min(prev * 1.5, 10));
  };

  const handleZoomOut = () => {
    setZoomLevel(prev => Math.max(prev / 1.5, 0.1));
  };

  const handleResetZoom = () => {
    setZoomLevel(1);
    setPanOffset({ x: 0, y: 0 });
  };

  const handlePan = (direction: 'left' | 'right' | 'up' | 'down') => {
    const step = 10;
    setPanOffset(prev => {
      switch (direction) {
        case 'left': return { ...prev, x: prev.x - step };
        case 'right': return { ...prev, x: prev.x + step };
        case 'up': return { ...prev, y: prev.y - step };
        case 'down': return { ...prev, y: prev.y + step };
        default: return prev;
      }
    });
  };

  const chartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      title: {
        display: true,
        text: funcData ? `График функции: ${funcData.name}` : 'График функции',
        font: {
          size: 18,
          weight: 'bold'
        },
        color: theme.palette.text.primary
      },
      tooltip: {
        mode: 'index',
        intersect: false,
      },
      legend: {
        display: false,
      },
    },
    scales: {
      x: {
        title: {
          display: true,
          text: 'X',
          font: {
            weight: 'bold'
          }
        },
        grid: {
          color: theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'
        },
        ticks: {
          color: theme.palette.text.secondary
        }
      },
      y: {
        title: {
          display: true,
          text: 'Y',
          font: {
            weight: 'bold'
          }
        },
        grid: {
          color: theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'
        },
        ticks: {
          color: theme.palette.text.secondary
        }
      }
    },
    elements: {
      point: {
        radius: 4,
        hoverRadius: 6,
        backgroundColor: theme.palette.primary.main,
        borderColor: theme.palette.background.paper,
        borderWidth: 2,
      },
      line: {
        borderWidth: 3,
        borderColor: theme.palette.primary.main,
        backgroundColor: `${theme.palette.primary.main}22`,
        fill: true,
        tension: 0.4
      }
    },
    interaction: {
      mode: 'nearest',
      axis: 'x',
      intersect: false
    },
    animation: {
      duration: 0
    },
    hover: {
      mode: 'nearest',
      intersect: false
    }
  };

  const data: ChartData<'line'> = {
    datasets: [
      {
        label: funcData?.name || 'Функция',
        data: chartData.map(point => ({
          x: point.x,
          y: point.y
        })),
        borderColor: theme.palette.primary.main,
        backgroundColor: `${theme.palette.primary.main}22`,
        borderWidth: 3,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: theme.palette.primary.main,
        pointBorderColor: theme.palette.background.paper,
        pointBorderWidth: 2,
      }
    ]
  };

  const toggleFullscreen = () => {
    setFullscreen(!fullscreen);
  };

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
        display: 'flex',
        alignItems: 'center',
        mb: 3,
        flexWrap: 'wrap',
        gap: 2,
        position: 'sticky',
        top: 0,
        bgcolor: 'background.paper',
        zIndex: 1000,
        py: 1
      }}>
        <IconButton onClick={() => navigate('/dashboard')} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1" fontWeight="bold">
          График функции
        </Typography>
        <Box sx={{ ml: 'auto', display: 'flex', gap: 1 }}>
          <Tooltip title="Полный экран">
            <IconButton onClick={toggleFullscreen}>
              <FullscreenIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Card sx={{
            height: fullscreen ? 'calc(100vh - 64px)' : 500,
            transition: 'all 0.3s ease'
          }}>
            <CardContent sx={{ height: '100%', position: 'relative' }}>
              {chartData.length > 0 ? (
                <Box sx={{ height: '100%' }}>
                  <Line
                    ref={chartRef}
                    options={chartOptions}
                    data={data}
                  />
                </Box>
              ) : (
                <Box sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'center',
                  alignItems: 'center',
                  height: '100%',
                  textAlign: 'center',
                  p: 3
                }}>
                  <Typography variant="h6" color="text.secondary" gutterBottom>
                    Нет данных для отображения графика
                  </Typography>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                    Эта функция не содержит точек или произошла ошибка при загрузке данных.
                  </Typography>
                  <Button
                    variant="outlined"
                    onClick={() => navigate(`/functions/${id}/edit`)}
                  >
                    Добавить точки
                  </Button>
                </Box>
              )}

              {/* Панель управления графиком */}
              <Paper sx={{
                position: 'absolute',
                bottom: 16,
                left: 16,
                right: 16,
                p: 1,
                bgcolor: 'background.paper',
                borderRadius: 2,
                boxShadow: 3,
                display: 'flex',
                flexWrap: 'wrap',
                gap: 1,
                justifyContent: 'center'
              }}>
                <ToggleButtonGroup
                  value={chartType}
                  exclusive
                  onChange={(_, newType) => newType && setChartType(newType)}
                  size="small"
                  sx={{ mr: 2 }}
                >
                  <ToggleButton value="line" title="Линейный график">
                    <ShowChartIcon />
                  </ToggleButton>
                  <ToggleButton value="scatter" title="Точечный график">
                    <AddIcon />
                  </ToggleButton>
                  <ToggleButton value="bar" title="Столбчатая диаграмма">
                    <RemoveIcon />
                  </ToggleButton>
                </ToggleButtonGroup>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Tooltip title="Уменьшить">
                    <IconButton onClick={handleZoomOut} size="small">
                      <ZoomOutIcon />
                    </IconButton>
                  </Tooltip>
                  <Box sx={{ width: 150, mx: 1 }}>
                    <Slider
                      value={zoomLevel}
                      min={0.1}
                      max={10}
                      step={0.1}
                      onChange={(_, value) => setZoomLevel(value as number)}
                      aria-labelledby="zoom-slider"
                      size="small"
                    />
                  </Box>
                  <Tooltip title="Увеличить">
                    <IconButton onClick={handleZoomIn} size="small">
                      <ZoomInIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Сбросить">
                    <IconButton onClick={handleResetZoom} size="small">
                      <RestartAltIcon />
                    </IconButton>
                  </Tooltip>
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