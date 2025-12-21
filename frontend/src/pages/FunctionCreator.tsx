import React from 'react';
import { Box, Typography, Button, Grid, Card, CardContent } from '@mui/material';
import { ArrowBack as ArrowBackIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const FunctionCreator: React.FC = () => {
  const navigate = useNavigate();

  const handleCreateFromArray = () => {
    navigate('/functions/from-array');
  };

  const handleCreateFromMathFunction = () => {
    navigate('/functions/from-math');
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/dashboard')}
          sx={{ mr: 1 }}
        >
          Назад
        </Button>
        <Typography variant="h4" component="h1">
          Создание функции
        </Typography>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card
            sx={{
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              cursor: 'pointer',
              '&:hover': {
                boxShadow: 4,
                transform: 'translateY(-2px)'
              },
              transition: 'all 0.3s ease'
            }}
            onClick={handleCreateFromArray}
          >
            <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
              <Typography variant="h6" component="div" gutterBottom>
                Создать из массива точек
              </Typography>
              <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                Вручную задайте координаты точек функции
              </Typography>
              <Button variant="contained" color="primary">
                Создать из массива
              </Button>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card
            sx={{
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              cursor: 'pointer',
              '&:hover': {
                boxShadow: 4,
                transform: 'translateY(-2px)'
              },
              transition: 'all 0.3s ease'
            }}
            onClick={handleCreateFromMathFunction}
          >
            <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
              <Typography variant="h6" component="div" gutterBottom>
                Создать из математической функции
              </Typography>
              <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                Выберите готовую функцию (квадратичная, синус и др.)
              </Typography>
              <Button variant="contained" color="primary">
                Создать из функции
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default FunctionCreator;