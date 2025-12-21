import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Card, CardContent, Button, Grid, Switch, FormControlLabel,
  TextField, MenuItem, Container, useTheme, useMediaQuery, IconButton,
  List, ListItem, ListItemIcon, ListItemText, Divider, Paper, Tooltip
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon, DarkMode as DarkModeIcon,
  LightMode as LightModeIcon, Notifications as NotificationsIcon,
  Save as SaveIcon, Refresh as RefreshIcon, Info as InfoIcon,
  Settings as SettingsIcon, CloudDownload as CloudDownloadIcon,
  CloudUpload as CloudUploadIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import authService from '../services/authService';

const Settings: React.FC = () => {
  const [factoryType, setFactoryType] = useState<'array' | 'linked_list'>('array');
  const [themeMode, setThemeMode] = useState<'light' | 'dark'>('dark');
  const [notificationSound, setNotificationSound] = useState(true);
  const [dataExportFormat, setDataExportFormat] = useState<'json' | 'csv' | 'xml'>('json');
  const [autoSave, setAutoSave] = useState(true);
  const [language, setLanguage] = useState<'ru' | 'en'>('ru');
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  useEffect(() => {
    // Загружаем настройки из localStorage при монтировании
    const savedSettings = JSON.parse(localStorage.getItem('appSettings') || '{}');
    if (savedSettings.factoryType) setFactoryType(savedSettings.factoryType);
    if (savedSettings.themeMode) setThemeMode(savedSettings.themeMode);
    if (savedSettings.notificationSound !== undefined) setNotificationSound(savedSettings.notificationSound);
    if (savedSettings.dataExportFormat) setDataExportFormat(savedSettings.dataExportFormat);
    if (savedSettings.autoSave !== undefined) setAutoSave(savedSettings.autoSave);
    if (savedSettings.language) setLanguage(savedSettings.language);
  }, []);

  const handleSaveSettings = () => {
    const settings = {
      factoryType,
      themeMode,
      notificationSound,
      dataExportFormat,
      autoSave,
      language
    };

    localStorage.setItem('appSettings', JSON.stringify(settings));

    // Применяем тему
    if (themeMode === 'dark') {
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
    }

    toast.success('Настройки успешно сохранены');
  };

  const handleResetSettings = () => {
    if (window.confirm('Вы уверены, что хотите сбросить все настройки на значения по умолчанию?')) {
      localStorage.removeItem('appSettings');

      // Сбрасываем на значения по умолчанию
      setFactoryType('array');
      setThemeMode('dark');
      setNotificationSound(true);
      setDataExportFormat('json');
      setAutoSave(true);
      setLanguage('ru');

      toast.success('Настройки сброшены');
    }
  };

  const handleExportData = () => {
    try {
      const data = {
        settings: JSON.parse(localStorage.getItem('appSettings') || '{}'),
        user: authService.getCurrentUser(),
        timestamp: new Date().toISOString()
      };

      const dataStr = JSON.stringify(data, null, 2);
      const dataUri = `data:application/json;charset=utf-8,${encodeURIComponent(dataStr)}`;

      const exportFileDefaultName = `function-manager-settings-${new Date().toISOString().split('T')[0]}.json`;

      const linkElement = document.createElement('a');
      linkElement.setAttribute('href', dataUri);
      linkElement.setAttribute('download', exportFileDefaultName);
      linkElement.click();

      toast.success('Настройки успешно экспортированы');
    } catch (error) {
      console.error('Ошибка при экспорте данных:', error);
      toast.error('Не удалось экспортировать настройки');
    }
  };

  const handleImportData = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const content = event.target?.result as string;
        const importedData = JSON.parse(content);

        if (importedData.settings) {
          localStorage.setItem('appSettings', JSON.stringify(importedData.settings));

          // Применяем импортированные настройки
          setFactoryType(importedData.settings.factoryType || 'array');
          setThemeMode(importedData.settings.themeMode || 'dark');
          setNotificationSound(importedData.settings.notificationSound !== undefined ? importedData.settings.notificationSound : true);
          setDataExportFormat(importedData.settings.dataExportFormat || 'json');
          setAutoSave(importedData.settings.autoSave !== undefined ? importedData.settings.autoSave : true);
          setLanguage(importedData.settings.language || 'ru');

          toast.success('Настройки успешно импортированы');
        } else {
          throw new Error('Неверный формат файла');
        }
      } catch (error) {
        console.error('Ошибка при импорте данных:', error);
        toast.error('Не удалось импортировать настройки. Проверьте формат файла.');
      }
    };
    reader.readAsText(file);
  };

  const handleClearCache = () => {
    if (window.confirm('Вы уверены, что хотите очистить кеш приложения? Это удалит все сохраненные данные, включая настройки и данные о пользователе.')) {
      localStorage.clear();
      toast.success('Кеш успешно очищен. Приложение будет перезагружено.');
      setTimeout(() => {
        window.location.reload();
      }, 1000);
    }
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <IconButton onClick={() => navigate('/dashboard')} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1" fontWeight="bold">
          Настройки
        </Typography>
        <Box sx={{ ml: 'auto', display: 'flex', gap: 1 }}>
          <Tooltip title="Сбросить настройки">
            <IconButton onClick={handleResetSettings} color="error">
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                <SettingsIcon sx={{ mr: 1 }} /> Основные настройки
              </Typography>

              <Box sx={{ mb: 3 }}>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  Тип фабрики для создания функций
                </Typography>
                <TextField
                  select
                  fullWidth
                  value={factoryType}
                  onChange={(e) => setFactoryType(e.target.value as 'array' | 'linked_list')}
                  label="Тип фабрики"
                >
                  <MenuItem value="array">Массив (быстрый доступ)</MenuItem>
                  <MenuItem value="linked_list">Связный список (гибкое изменение)</MenuItem>
                </TextField>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  Выберите способ хранения табулированных функций. Массив обеспечивает более быстрый доступ к элементам, а связный список — более эффективное добавление и удаление точек.
                </Typography>
              </Box>

              <Box sx={{ mb: 3 }}>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  Тема интерфейса
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <IconButton
                    onClick={() => setThemeMode('light')}
                    color={themeMode === 'light' ? 'primary' : 'default'}
                    sx={{ bgcolor: themeMode === 'light' ? 'primary.light' : 'background.paper' }}
                  >
                    <LightModeIcon />
                  </IconButton>
                  <Switch
                    checked={themeMode === 'dark'}
                    onChange={(e) => setThemeMode(e.target.checked ? 'dark' : 'light')}
                  />
                  <IconButton
                    onClick={() => setThemeMode('dark')}
                    color={themeMode === 'dark' ? 'primary' : 'default'}
                    sx={{ bgcolor: themeMode === 'dark' ? 'primary.light' : 'background.paper' }}
                  >
                    <DarkModeIcon />
                  </IconButton>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {themeMode === 'dark'
                    ? 'Включена темная тема для комфортной работы в условиях слабого освещения.'
                    : 'Включена светлая тема для работы в хорошо освещенных помещениях.'}
                </Typography>
              </Box>

              <Box>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  Звуковые уведомления
                </Typography>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notificationSound}
                      onChange={(e) => setNotificationSound(e.target.checked)}
                      color="primary"
                    />
                  }
                  label={notificationSound ? 'Включены' : 'Отключены'}
                />
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {notificationSound
                    ? 'Воспроизводить звуковые сигналы при успешных операциях и ошибках.'
                    : 'Отключить все звуковые уведомления.'}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                <CloudDownloadIcon sx={{ mr: 1 }} /> Импорт/Экспорт
              </Typography>

              <Box sx={{ mb: 3 }}>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  Формат экспорта данных
                </Typography>
                <TextField
                  select
                  fullWidth
                  value={dataExportFormat}
                  onChange={(e) => setDataExportFormat(e.target.value as 'json' | 'csv' | 'xml')}
                  label="Формат экспорта"
                >
                  <MenuItem value="json">JSON (рекомендуется)</MenuItem>
                  <MenuItem value="csv">CSV (таблица)</MenuItem>
                  <MenuItem value="xml">XML (структурированный)</MenuItem>
                </TextField>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  Выберите формат для экспорта данных функций и настроек.
                </Typography>
              </Box>

              <Box sx={{ mb: 3 }}>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  Автоматическое сохранение
                </Typography>
                <FormControlLabel
                  control={
                    <Switch
                      checked={autoSave}
                      onChange={(e) => setAutoSave(e.target.checked)}
                      color="primary"
                    />
                  }
                  label={autoSave ? 'Включено' : 'Отключено'}
                />
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {autoSave
                    ? 'Автоматически сохранять изменения при работе с функциями.'
                    : 'Требовать подтверждения перед сохранением изменений.'}
                </Typography>
              </Box>

              <Box>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  Язык интерфейса
                </Typography>
                <TextField
                  select
                  fullWidth
                  value={language}
                  onChange={(e) => setLanguage(e.target.value as 'ru' | 'en')}
                  label="Язык"
                >
                  <MenuItem value="ru">Русский (Russian)</MenuItem>
                  <MenuItem value="en">Английский (English)</MenuItem>
                </TextField>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                <InfoIcon sx={{ mr: 1 }} /> Системная информация
              </Typography>

              <Grid container spacing={2}>
                <Grid item xs={12} sm={6} md={3}>
                  <Typography variant="body1">
                    <strong>Версия приложения:</strong> 1.0.0
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                  <Typography variant="body1">
                    <strong>Backend:</strong> Spring Boot
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                  <Typography variant="body1">
                    <strong>API Endpoint:</strong> /api
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                  <Typography variant="body1">
                    <strong>База данных:</strong> PostgreSQL
                  </Typography>
                </Grid>
              </Grid>

              <Divider sx={{ my: 2 }} />

              <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                Производительность
              </Typography>

              <List dense>
                <ListItem>
                  <ListItemIcon>
                    <CloudDownloadIcon color="success" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Экспорт настроек"
                    secondary="Сохранить текущие настройки в файл"
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={handleExportData}
                    startIcon={<CloudDownloadIcon />}
                  >
                    Экспорт
                  </Button>
                </ListItem>

                <ListItem>
                  <ListItemIcon>
                    <CloudUploadIcon color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Импорт настроек"
                    secondary="Загрузить настройки из файла"
                  />
                  <input
                    accept=".json"
                    style={{ display: 'none' }}
                    id="import-settings"
                    type="file"
                    onChange={handleImportData}
                  />
                  <label htmlFor="import-settings">
                    <Button
                      variant="outlined"
                      size="small"
                      component="span"
                      startIcon={<CloudUploadIcon />}
                    >
                      Импорт
                    </Button>
                  </label>
                </ListItem>

                <ListItem>
                  <ListItemIcon>
                    <DeleteIcon color="error" />
                  </ListItemIcon>
                  <ListItemText
                    primary="Очистка кеша"
                    secondary="Удалить все локальные данные приложения"
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    color="error"
                    onClick={handleClearCache}
                    startIcon={<DeleteIcon />}
                  >
                    Очистить
                  </Button>
                </ListItem>
              </List>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, flexWrap: 'wrap' }}>
            <Button
              variant="outlined"
              color="error"
              onClick={handleResetSettings}
              startIcon={<RefreshIcon />}
            >
              Сбросить настройки
            </Button>
            <Button
              variant="contained"
              onClick={handleSaveSettings}
              startIcon={<SaveIcon />}
              sx={{ fontWeight: 'bold' }}
            >
              Сохранить изменения
            </Button>
          </Box>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Settings;