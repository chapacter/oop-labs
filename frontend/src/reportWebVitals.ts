// src/reportWebVitals.ts
// Универсальный helper для reportWebVitals — проверяет разные экспортируемые имена у пакета web-vitals
const reportWebVitals = (onPerfEntry?: (metric: any) => void) => {
  if (onPerfEntry && onPerfEntry instanceof Function) {
    import('web-vitals')
      .then((mod: any) => {
        // модуль может экспортировать разные имена в зависимости от версии / сборки
        const getCLS = mod.getCLS || mod.onCLS || mod.onReportCLS;
        const getFID = mod.getFID || mod.onFID || mod.onReportFID;
        const getFCP = mod.getFCP || mod.onFCP || mod.onReportFCP;
        const getLCP = mod.getLCP || mod.onLCP || mod.onReportLCP;
        const getTTFB = mod.getTTFB || mod.onTTFB || mod.onReportTTFB;

        try { if (typeof getCLS === 'function') getCLS(onPerfEntry); } catch (e) { /* ignore */ }
        try { if (typeof getFID === 'function') getFID(onPerfEntry); } catch (e) { /* ignore */ }
        try { if (typeof getFCP === 'function') getFCP(onPerfEntry); } catch (e) { /* ignore */ }
        try { if (typeof getLCP === 'function') getLCP(onPerfEntry); } catch (e) { /* ignore */ }
        try { if (typeof getTTFB === 'function') getTTFB(onPerfEntry); } catch (e) { /* ignore */ }
      })
      .catch(() => {
        // пакет web-vitals недоступен — ничего плохого не произойдёт
      });
  }
};

export default reportWebVitals;
