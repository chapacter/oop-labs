const reportWebVitals = (onPerfEntry?: (metric: any) => void) => {
  if (onPerfEntry && onPerfEntry instanceof Function) {
    import('web-vitals')
      .then((mod: any) => {
        const getCLS = mod.getCLS || mod.onCLS || mod.onReportCLS;
        const getFID = mod.getFID || mod.onFID || mod.onReportFID;
        const getFCP = mod.getFCP || mod.onFCP || mod.onReportFCP;
        const getLCP = mod.getLCP || mod.onLCP || mod.onReportLCP;
        const getTTFB = mod.getTTFB || mod.onTTFB || mod.onReportTTFB;

        try { if (typeof getCLS === 'function') getCLS(onPerfEntry); } catch (e) { }
        try { if (typeof getFID === 'function') getFID(onPerfEntry); } catch (e) { }
        try { if (typeof getFCP === 'function') getFCP(onPerfEntry); } catch (e) { }
        try { if (typeof getLCP === 'function') getLCP(onPerfEntry); } catch (e) { }
        try { if (typeof getTTFB === 'function') getTTFB(onPerfEntry); } catch (e) { }
      })
      .catch(() => {
      });
  }
};

export default reportWebVitals;
