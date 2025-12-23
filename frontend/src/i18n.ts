import { useCallback, useEffect, useState } from 'react';

export type Lang = 'ru' | 'en';

const translations: Record<Lang, Record<string, string>> = {
  ru: {
    'settings.title': 'Настройки',
    'settings.basic': 'Основные настройки',
    'settings.factoryType': 'Тип фабрики',
    'settings.factory.array': 'Массив (быстрый доступ)',
    'settings.factory.linked_list': 'Связный список (гибкое изменение)',
    'settings.theme': 'Тема интерфейса',
    'settings.theme.dark': 'Тёмная тема',
    'settings.theme.light': 'Светлая тема',
    'settings.sound': 'Звуковые уведомления',
    'settings.sound.enabled': 'Включены',
    'settings.sound.disabled': 'Отключены',
    'settings.export.format': 'Формат экспорта данных',
    'settings.export.json': 'JSON (рекомендуется)',
    'settings.export.csv': 'CSV (таблица)',
    'settings.export.xml': 'XML (структурированный)',
    'settings.autoSave': 'Автоматическое сохранение',
    'settings.language': 'Язык интерфейса',
    'settings.save': 'Сохранить изменения',
    'settings.reset': 'Сбросить настройки',
    'settings.exportBtn': 'Экспорт',
    'settings.importBtn': 'Импорт',
    'settings.clearCache': 'Очистить кеш',
    'settings.import.success': 'Настройки успешно импортированы',
    'settings.import.failed': 'Не удалось импортировать настройки. Проверьте формат файла.',
    'settings.export.success': 'Настройки успешно экспортированы',
    'settings.export.failed': 'Не удалось экспортировать настройки',
    'settings.reset.confirm': 'Вы уверены, что хотите сбросить все настройки на значения по умолчанию?',
    'function.create.success': 'Функция успешно создана',
    'function.create.error': 'Не удалось создать функцию',
    'function.update.success': 'Функция успешно обновлена',
    'function.update.error': 'Не удалось обновить функцию',
    'function.delete.success': 'Функция успешно удалена',
    'function.delete.error': 'Не удалось удалить функцию',
    'function.save.result.success': 'Результат успешно сохранён',
    'function.save.result.error': 'Не удалось сохранить результат',
    'auth.login.success': 'Вы успешно вошли в систему!',
    'auth.login.error': 'Неверное имя пользователя или пароль',
    'auth.check.error': 'Ошибка проверки аутентификации',
    'error.generic': 'Произошла ошибка',
    'ok': 'ОК'
  },
  en: {
    'settings.title': 'Settings',
    'settings.basic': 'Basic settings',
    'settings.factoryType': 'Factory type',
    'settings.factory.array': 'Array (fast access)',
    'settings.factory.linked_list': 'Linked list (flexible edits)',
    'settings.theme': 'UI theme',
    'settings.theme.dark': 'Dark theme',
    'settings.theme.light': 'Light theme',
    'settings.sound': 'Notification sounds',
    'settings.sound.enabled': 'Enabled',
    'settings.sound.disabled': 'Disabled',
    'settings.export.format': 'Export format',
    'settings.export.json': 'JSON (recommended)',
    'settings.export.csv': 'CSV (table)',
    'settings.export.xml': 'XML (structured)',
    'settings.autoSave': 'Auto-save',
    'settings.language': 'Language',
    'settings.save': 'Save changes',
    'settings.reset': 'Reset settings',
    'settings.exportBtn': 'Export',
    'settings.importBtn': 'Import',
    'settings.clearCache': 'Clear cache',
    'settings.import.success': 'Settings imported successfully',
    'settings.import.failed': 'Failed to import settings. Check file format.',
    'settings.export.success': 'Settings exported successfully',
    'settings.export.failed': 'Failed to export settings',
    'settings.reset.confirm': 'Are you sure you want to reset all settings to defaults?',
    'function.create.success': 'Function created successfully',
    'function.create.error': 'Failed to create function',
    'function.update.success': 'Function updated successfully',
    'function.update.error': 'Failed to update function',
    'function.delete.success': 'Function deleted successfully',
    'function.delete.error': 'Failed to delete function',
    'function.save.result.success': 'Result saved successfully',
    'function.save.result.error': 'Failed to save result',
    'auth.login.success': 'Logged in successfully!',
    'auth.login.error': 'Invalid username or password',
    'auth.check.error': 'Auth check failed',
    'error.generic': 'An error occurred',
    'ok': 'OK'
  }
};

let currentLang: Lang = ((): Lang => {
  try {
    const raw = localStorage.getItem('appSettings');
    if (raw) {
      const obj = JSON.parse(raw);
      if (obj && obj.language) return obj.language;
    }
  } catch (e) {}
  return 'ru';
})();

export function t(key: string, lang?: Lang): string {
  const L = lang || currentLang;
  return translations[L]?.[key] ?? translations['ru'][key] ?? key;
}

export function setLanguage(lang: Lang) {
  currentLang = lang;
  try {
    const raw = localStorage.getItem('appSettings') || '{}';
    const obj = JSON.parse(raw || '{}');
    obj.language = lang;
    localStorage.setItem('appSettings', JSON.stringify(obj));
  } catch (e) {
  }
  window.dispatchEvent(new CustomEvent('appLanguageChanged', { detail: lang }));
}

export function getLanguage(): Lang {
  return currentLang;
}

export function useTranslation() {
  const [lang, setLang] = useState<Lang>(currentLang);

  useEffect(() => {
    const h = (ev: any) => setLang(ev.detail || currentLang);
    window.addEventListener('appLanguageChanged', h);
    return () => window.removeEventListener('appLanguageChanged', h);
  }, []);

  const changeLanguage = useCallback((l: Lang) => {
    setLanguage(l);
    setLang(l);
  }, []);

  const translate = useCallback((key: string) => t(key, lang), [lang]);

  return { t: translate, lang, changeLanguage };
}
