// src/pages/Settings.tsx
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Card, CardContent, Button, Grid, Switch, FormControlLabel,
  TextField, MenuItem, Container, useTheme, useMediaQuery, IconButton,
  List, ListItem, ListItemIcon, ListItemText, Divider, Tooltip, Chip
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon, DarkMode as DarkModeIcon,
  LightMode as LightModeIcon, Save as SaveIcon, Refresh as RefreshIcon,
  Info as InfoIcon, Settings as SettingsIcon, CloudDownload as CloudDownloadIcon,
  CloudUpload as CloudUploadIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import notify from '../utils/notify';
import { useTranslation } from '../i18n';

type FactoryType = 'array' | 'linked_list';
type ThemeMode = 'light' | 'dark';
type ExportFormat = 'json' | 'csv' | 'xml';
type Lang = 'ru' | 'en';

const APP_SETTINGS_KEY = 'appSettings';

const objectToCSV = (obj: any) => {
  const rows: string[] = [];
  for (const key of Object.keys(obj)) {
    const val = obj[key];
    const value = (typeof val === 'object' && val !== null) ? JSON.stringify(val) : String(val);
    const safe = value.includes(',') || value.includes('"') ? `"${value.replace(/"/g, '""')}"` : value;
    rows.push(`${key},${safe}`);
  }
  return rows.join('\n');
};

const objectToXML = (obj: any) => {
  const walk = (o: any): string => {
    if (o === null || o === undefined) return '';
    if (typeof o !== 'object') return String(o)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    if (Array.isArray(o)) {
      return o.map(item => `<item>${walk(item)}</item>`).join('');
    }
    return Object.keys(o).map(k => `<${k}>${walk(o[k])}</${k}>`).join('');
  };
  return `<root>${walk(obj)}</root>`;
};

const Settings: React.FC = () => {
  const { t, changeLanguage } = useTranslation();

  const [factoryType, setFactoryType] = useState<FactoryType>('array');
  const [themeMode, setThemeMode] = useState<ThemeMode>('dark');
  const [notificationSound, setNotificationSound] = useState(true);
  const [dataExportFormat, setDataExportFormat] = useState<ExportFormat>('json');
  const [autoSave, setAutoSave] = useState(true);
  const [language, setLanguage] = useState<Lang>('ru');

  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  useEffect(() => {
    const savedSettings = JSON.parse(localStorage.getItem(APP_SETTINGS_KEY) || '{}');
    if (savedSettings.factoryType) setFactoryType(savedSettings.factoryType);
    if (savedSettings.themeMode) setThemeMode(savedSettings.themeMode);
    if (savedSettings.notificationSound !== undefined) setNotificationSound(savedSettings.notificationSound);
    if (savedSettings.dataExportFormat) setDataExportFormat(savedSettings.dataExportFormat);
    if (savedSettings.autoSave !== undefined) setAutoSave(savedSettings.autoSave);
    if (savedSettings.language) {
      setLanguage(savedSettings.language);
      changeLanguage(savedSettings.language);
    }
  }, [changeLanguage]);

  const getSettingsObject = () => ({
    factoryType, themeMode, notificationSound, dataExportFormat, autoSave, language
  });

  const applyThemeLocally = (mode: ThemeMode) => {
    setThemeMode(mode);
    try {
      document.documentElement.setAttribute('data-theme', mode);
      if (mode === 'dark') {
        document.documentElement.classList.add('theme-dark');
        document.documentElement.classList.remove('theme-light');
      } else {
        document.documentElement.classList.add('theme-light');
        document.documentElement.classList.remove('theme-dark');
      }
    } catch (e) {
      // ignore
    }
  };

  const persistAndBroadcast = (partial: Partial<Record<string, any>>) => {
    try {
      const raw = localStorage.getItem(APP_SETTINGS_KEY) || '{}';
      const obj = JSON.parse(raw || '{}');
      const next = { ...obj, ...partial };
      localStorage.setItem(APP_SETTINGS_KEY, JSON.stringify(next));
      window.dispatchEvent(new CustomEvent('appSettingsChanged', { detail: next }));
      return next;
    } catch (e) {
      console.warn('Failed to persist settings', e);
      return partial;
    }
  };

  const handleSaveSettings = () => {
    const settings = getSettingsObject();
    try {
      localStorage.setItem(APP_SETTINGS_KEY, JSON.stringify(settings));
      applyThemeLocally(settings.themeMode);
      changeLanguage(settings.language);
      notify.success(t('settings.save'));
    } catch (e) {
      console.error(e);
      notify.error(t('settings.export.failed'));
    }
  };

  const handleResetSettings = () => {
    if (!window.confirm(t('settings.reset.confirm'))) return;
    localStorage.removeItem(APP_SETTINGS_KEY);
    setFactoryType('array');
    applyThemeLocally('dark');
    setNotificationSound(true);
    setDataExportFormat('json');
    setAutoSave(true);
    setLanguage('ru');
    changeLanguage('ru');
    persistAndBroadcast({ factoryType: 'array', themeMode: 'dark', notificationSound: true, dataExportFormat: 'json', autoSave: true, language: 'ru' });
    notify.success(t('settings.reset'));
  };

  const handleExportData = () => {
    try {
      const dataObj = {
        settings: getSettingsObject(),
        user: authService.getCurrentUser(),
        timestamp: new Date().toISOString()
      };

      let dataStr = '';
      let mime = 'application/octet-stream';
      let ext = 'bin';

      if (dataExportFormat === 'json') {
        dataStr = JSON.stringify(dataObj, null, 2);
        mime = 'application/json;charset=utf-8';
        ext = 'json';
      } else if (dataExportFormat === 'csv') {
        dataStr = objectToCSV(dataObj);
        mime = 'text/csv;charset=utf-8';
        ext = 'csv';
      } else if (dataExportFormat === 'xml') {
        dataStr = objectToXML(dataObj);
        mime = 'application/xml;charset=utf-8';
        ext = 'xml';
      }

      const dataUri = `data:${mime},${encodeURIComponent(dataStr)}`;
      const exportFileDefaultName = `function-manager-settings-${new Date().toISOString().split('T')[0]}.${ext}`;

      const linkElement = document.createElement('a');
      linkElement.setAttribute('href', dataUri);
      linkElement.setAttribute('download', exportFileDefaultName);
      document.body.appendChild(linkElement);
      linkElement.click();
      linkElement.remove();

      notify.success(t('settings.export.success'));
    } catch (error) {
      console.error('Ошибка при экспорте данных:', error);
      notify.error(t('settings.export.failed'));
    }
  };

  const handleImportData = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const content = event.target?.result as string;
        const name = (file.name || '').toLowerCase();
        const isJson = name.endsWith('.json') || file.type.includes('json');
        const isCsv = name.endsWith('.csv') || file.type.includes('csv') || name.endsWith('.txt');
        const isXml = name.endsWith('.xml') || file.type.includes('xml');

        let importedSettings: any = null;

        if (isJson) {
          const imported = JSON.parse(content);
          importedSettings = imported.settings ?? imported;
        } else if (isCsv) {
          const lines = content.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
          const res: any = {};
          lines.forEach(line => {
            const parts = line.split(/[;,]+/).map(s => s.trim());
            if (parts.length >= 2) {
              res[parts[0]] = (() => {
                try { return JSON.parse(parts.slice(1).join(',')); } catch { return parts.slice(1).join(','); }
              })();
            }
          });
          importedSettings = res;
        } else if (isXml) {
          try {
            const parser = new DOMParser();
            const doc = parser.parseFromString(content, 'application/xml');
            const root = doc.documentElement;
            const res: any = {};
            root.childNodes.forEach((child: any) => {
              if (child.nodeType !== 1) return;
              const text = child.textContent || '';
              try { res[child.nodeName] = JSON.parse(text); } catch { res[child.nodeName] = text; }
            });
            importedSettings = res;
          } catch (err) {
            importedSettings = null;
          }
        } else {
          try {
            const imported = JSON.parse(content);
            importedSettings = imported.settings ?? imported;
          } catch {
            notify.error('Unknown file format. Supported JSON, CSV, XML.');
            return;
          }
        }

        if (!importedSettings || typeof importedSettings !== 'object') {
          notify.error(t('settings.import.failed'));
          return;
        }

        const settings = {
          factoryType: (importedSettings.factoryType as FactoryType) || 'array',
          themeMode: (importedSettings.themeMode as ThemeMode) || 'dark',
          notificationSound: importedSettings.notificationSound !== undefined ? Boolean(importedSettings.notificationSound) : true,
          dataExportFormat: (importedSettings.dataExportFormat as ExportFormat) || 'json',
          autoSave: importedSettings.autoSave !== undefined ? Boolean(importedSettings.autoSave) : true,
          language: (importedSettings.language as Lang) || 'ru'
        };

        localStorage.setItem(APP_SETTINGS_KEY, JSON.stringify(settings));

        setFactoryType(settings.factoryType);
        applyThemeLocally(settings.themeMode);
        setNotificationSound(settings.notificationSound);
        setDataExportFormat(settings.dataExportFormat);
        setAutoSave(settings.autoSave);
        setLanguage(settings.language);
        changeLanguage(settings.language);

        window.dispatchEvent(new CustomEvent('appSettingsChanged', { detail: settings }));

        notify.success(t('settings.import.success'));
      } catch (error) {
        console.error('Ошибка при импорте данных:', error);
        notify.error(t('settings.import.failed'));
      } finally {
        (e.target as HTMLInputElement).value = '';
      }
    };

    reader.readAsText(file);
  };

  const handleClearCache = () => {
    if (!window.confirm(t('settings.clearCache'))) return;
    localStorage.clear();
    notify.success(t('settings.clearCache'));
    setTimeout(() => window.location.reload(), 800);
  };

  return (
    <Container maxWidth="lg" sx={{ pb: 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <IconButton onClick={() => navigate('/dashboard')} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" component="h1" fontWeight="bold">
          {t('settings.title')}
        </Typography>
        <Box sx={{ ml: 'auto', display: 'flex', gap: 1 }}>
          <Tooltip title={t('settings.reset')}>
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
                <SettingsIcon sx={{ mr: 1 }} /> {t('settings.basic')}
              </Typography>

              <Box sx={{ mb: 3 }}>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  {t('settings.factoryType')}
                </Typography>
                <TextField
                  select
                  fullWidth
                  value={factoryType}
                  onChange={(e) => {
                    const val = e.target.value as FactoryType;
                    setFactoryType(val);
                    persistAndBroadcast({ ...getSettingsObject(), factoryType: val });
                  }}
                  label={t('settings.factoryType')}
                >
                  <MenuItem value="array">{t('settings.factory.array')}</MenuItem>
                  <MenuItem value="linked_list">{t('settings.factory.linked_list')}</MenuItem>
                </TextField>
              </Box>

              <Box sx={{ mb: 3 }}>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  {t('settings.theme')}
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <IconButton
                    onClick={() => { applyThemeLocally('light'); persistAndBroadcast({ ...getSettingsObject(), themeMode: 'light' }); }}
                    color={themeMode === 'light' ? 'primary' : 'default'}
                    sx={{ bgcolor: themeMode === 'light' ? 'primary.light' : 'background.paper' }}
                  >
                    <LightModeIcon />
                  </IconButton>
                  <Switch
                    checked={themeMode === 'dark'}
                    onChange={(e) => { const mode = e.target.checked ? 'dark' : 'light'; applyThemeLocally(mode); persistAndBroadcast({ ...getSettingsObject(), themeMode: mode }); }}
                  />
                  <IconButton
                    onClick={() => { applyThemeLocally('dark'); persistAndBroadcast({ ...getSettingsObject(), themeMode: 'dark' }); }}
                    color={themeMode === 'dark' ? 'primary' : 'default'}
                    sx={{ bgcolor: themeMode === 'dark' ? 'primary.light' : 'background.paper' }}
                  >
                    <DarkModeIcon />
                  </IconButton>
                </Box>
              </Box>

              <Box>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  {t('settings.sound')}
                </Typography>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notificationSound}
                      onChange={(e) => {
                        const newVal = e.target.checked;
                        // persist first so notify reads updated value
                        persistAndBroadcast({ ...getSettingsObject(), notificationSound: newVal });
                        setNotificationSound(newVal);
                        if (newVal) {
                          notify.success(t('settings.sound.enabled'));
                        } else {
                          // show info about disabled (notify won't play sound because persisted value is false)
                          notify.info(t('settings.sound.disabled'));
                        }
                      }}
                      color="primary"
                    />
                  }
                  label={notificationSound ? t('settings.sound.enabled') : t('settings.sound.disabled')}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                <CloudDownloadIcon sx={{ mr: 1 }} /> {t('settings.export.format')}
              </Typography>

              <Box sx={{ mb: 3 }}>
                <TextField
                  select
                  fullWidth
                  value={dataExportFormat}
                  onChange={(e) => { setDataExportFormat(e.target.value as ExportFormat); persistAndBroadcast({ ...getSettingsObject(), dataExportFormat: e.target.value }); }}
                  label={t('settings.export.format')}
                >
                  <MenuItem value="json">{t('settings.export.json')}</MenuItem>
                  <MenuItem value="csv">{t('settings.export.csv')}</MenuItem>
                  <MenuItem value="xml">{t('settings.export.xml')}</MenuItem>
                </TextField>
              </Box>

              <Box sx={{ mb: 3 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={autoSave}
                      onChange={(e) => { setAutoSave(e.target.checked); persistAndBroadcast({ ...getSettingsObject(), autoSave: e.target.checked }); }}
                      color="primary"
                    />
                  }
                  label={autoSave ? 'Auto' : 'Manual'}
                />
              </Box>

              <Box>
                <Typography variant="body1" gutterBottom sx={{ fontWeight: 'bold' }}>
                  {t('settings.language')}
                </Typography>
                <TextField
                  select
                  fullWidth
                  value={language}
                  onChange={(e) => {
                    const l = e.target.value as Lang;
                    setLanguage(l);
                    changeLanguage(l);
                    persistAndBroadcast({ ...getSettingsObject(), language: l });
                  }}
                  label={t('settings.language')}
                >
                  <MenuItem value="ru">Русский (Russian)</MenuItem>
                  <MenuItem value="en">English</MenuItem>
                </TextField>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                <InfoIcon sx={{ mr: 1 }} /> System
              </Typography>

              <Divider sx={{ my: 2 }} />

              <List dense>
                <ListItem>
                  <ListItemIcon>
                    <CloudDownloadIcon color="success" />
                  </ListItemIcon>
                  <ListItemText
                    primary={t('settings.exportBtn')}
                    secondary="Save current settings to a file"
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={handleExportData}
                    startIcon={<CloudDownloadIcon />}
                  >
                    {t('settings.exportBtn')}
                  </Button>
                </ListItem>

                <ListItem>
                  <ListItemIcon>
                    <CloudUploadIcon color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary={t('settings.importBtn')}
                    secondary="Load settings from file (JSON/CSV/XML)"
                  />
                  <input
                    accept=".json,.csv,.xml,text/*"
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
                      {t('settings.importBtn')}
                    </Button>
                  </label>
                </ListItem>

                <ListItem>
                  <ListItemIcon>
                    <DeleteIcon color="error" />
                  </ListItemIcon>
                  <ListItemText
                    primary={t('settings.clearCache')}
                    secondary="Remove all local data"
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    color="error"
                    onClick={handleClearCache}
                    startIcon={<DeleteIcon />}
                  >
                    {t('settings.clearCache')}
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
              {t('settings.reset')}
            </Button>
            <Button
              variant="contained"
              onClick={handleSaveSettings}
              startIcon={<SaveIcon />}
              sx={{ fontWeight: 'bold' }}
            >
              {t('settings.save')}
            </Button>
            <Chip label={`Export: ${dataExportFormat.toUpperCase()}`} />
          </Box>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Settings;
