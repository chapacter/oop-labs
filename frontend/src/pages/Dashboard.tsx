import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Grid, Card, CardContent, CardActions, Button,
  CircularProgress, IconButton, Tooltip, Container, TextField, useTheme, useMediaQuery
} from '@mui/material';
import {
  Edit as EditIcon, Delete as DeleteIcon, ShowChart as ShowChartIcon,
  Add as AddIcon, Refresh as RefreshIcon, Search as SearchIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import functionService from '../services/functionService';
import toast from 'react-hot-toast';
import authService from '../services/authService';

const Dashboard: React.FC = () => {
  const [functions, setFunctions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  useEffect(() => {
    const fetchFunctions = async () => {
      try {
        setLoading(true);
        const currentUser = authService.getCurrentUser();
        if (!currentUser) {
          navigate('/login');
          return;
        }
        const data = await functionService.getAllFunctions(currentUser.id);
        setFunctions(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error('Ошибка при загрузке функций:', error);
        toast.error('Не удалось загрузить функции');
      } finally {
        setLoading(false);
      }
    };

    fetchFunctions();
  }, [navigate]);

  const handleDelete = async (id: number) => {
    if (window.confirm('Вы уверены, что хотите удалить эту функцию?')) {
      try {
        await functionService.deleteFunction(id);
        setFunctions(functions.filter(f => f.id !== id));
        toast.success('Функция успешно удалена');
      } catch (error) {
        console.error('Ошибка при удалении функции:', error);
        toast.error('Не удалось удалить функцию');
      }
    }
  };

  const handleRefresh = async () => {
    try {
      setLoading(true);
      const currentUser = authService.getCurrentUser();
      if (currentUser) {
        const data = await functionService.getAllFunctions(currentUser.id);
        setFunctions(Array.isArray(data) ? data : []);
        toast.success('Данные обновлены');
      }
    } catch (error) {
      console.error('Ошибка при обновлении данных:', error);
      toast.error('Не удалось обновить данные');
    } finally {
      setLoading(false);
    }
  };

  const filteredFunctions = functions.filter(func =>
    func.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    func.funcResult?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    func.user?.name?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Typography variant="h4" component="h1" fontWeight="bold">
          Мои функции
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={handleRefresh}
            disabled={loading}
          >
            Обновить
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/functions/new')}
            sx={{ fontWeight: 'bold' }}
          >
            {isMobile ? 'Создать' : 'Создать функцию'}
          </Button>
        </Box>
      </Box>

      <Box sx={{ mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Поиск функций..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
          }}
        />
      </Box>

      {filteredFunctions.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 4, border: '1px dashed', borderColor: 'divider', borderRadius: 2 }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {searchTerm ? 'Не найдено функций по вашему запросу' : 'У вас нет созданных функций'}
          </Typography>
          <Button
            variant="outlined"
            sx={{ mt: 2 }}
            startIcon={<AddIcon />}
            onClick={() => navigate('/functions/new')}
          >
            Создать первую функцию
          </Button>
        </Box>
      ) : (
        <Grid container spacing={2}>
          {filteredFunctions.map(func => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={func.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.3s, box-shadow 0.3s',
                  '&:hover': {
                    transform: 'translateY(-2px)',
                    boxShadow: 4
                  }
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" component="div" fontWeight="bold" gutterBottom>
                    {func.name || 'Без названия'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1, minHeight: 40 }}>
                    Результат: {func.funcResult || 'Нет данных'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    <strong>Пользователь:</strong> {func.user?.name || 'Неизвестен'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    <strong>ID:</strong> {func.id}
                  </Typography>
                </CardContent>
                <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                  <Tooltip title="Редактировать">
                    <IconButton
                      color="primary"
                      onClick={() => navigate(`/functions/${func.id}/edit`)}
                      sx={{ mx: 0.5 }}
                    >
                      <EditIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="График">
                    <IconButton
                      color="info"
                      onClick={() => navigate(`/functions/${func.id}/graph`)}
                      sx={{ mx: 0.5 }}
                    >
                      <ShowChartIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Удалить">
                    <IconButton
                      color="error"
                      onClick={() => handleDelete(func.id)}
                      sx={{ mx: 0.5 }}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </Tooltip>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default Dashboard;