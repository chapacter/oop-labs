const fs = require('fs');
const path = require('path');

function parseArgs() {
  const args = process.argv.slice(2);
  const out = {};
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--input') out.input = args[++i];
    if (args[i] === '--out-details') out.details = args[++i];
    if (args[i] === '--out-agg') out.agg = args[++i];
  }
  if (!out.input) throw new Error('--input is required');
  return out;
}

function median(arr) {
  if (!arr.length) return null;
  const s = arr.slice().sort((a,b) => a-b);
  const mid = Math.floor(s.length/2);
  return s.length % 2 ? s[mid] : (s[mid-1]+s[mid])/2;
}

function mean(arr) {
  if (!arr.length) return null;
  return arr.reduce((a,b)=>a+b,0)/arr.length;
}

(async () => {
  const args = parseArgs();
  const json = JSON.parse(fs.readFileSync(args.input, 'utf8'));
  const executions = (json.run && json.run.executions) || [];

   const detailRows = [];
  const byName = {};

  executions.forEach(ex => {
    const name = (ex.item && ex.item.name) || '(unknown)';
    const time = ex.response && ex.response.responseTime || (ex.timings && ex.timings.completed);
    const code = ex.response && ex.response.code || (ex.responseCode && ex.responseCode.code) || '';
    const iteration = ex.cursor && ex.cursor.iteration !== undefined ? ex.cursor.iteration : '';
    detailRows.push({ name, iteration, method: ex.request && ex.request.method, url: ex.request && (ex.request.url && (typeof ex.request.url === 'string' ? ex.request.url : ex.request.url.raw)), code, time });

    if (!byName[name]) byName[name] = [];
    if (typeof time === 'number') byName[name].push(time);
  });

  const detPath = args.details || path.join(path.dirname(args.input), 'details.csv');
  const detHeader = ['requestName','iteration','method','url','statusCode','responseTimeMs'];
  const detLines = [detHeader.join(',')].concat(detailRows.map(r => {
    const escape = s => (s===null||s===undefined)?'':('"' + String(s).replace(/"/g,'""') + '"');
    return [escape(r.name), r.iteration, escape(r.method), escape(r.url), r.code, r.time].join(',');
  }));
  fs.writeFileSync(detPath, detLines.join('\n'), 'utf8');

  const aggPath = args.agg || path.join(path.dirname(args.input), 'aggregates.csv');
  const aggHeader = ['requestName','count','minMs','medianMs','meanMs','maxMs'];
  const aggLines = [aggHeader.join(',')];
  Object.keys(byName).forEach(name => {
    const arr = byName[name];
    arr.sort((a,b)=>a-b);
    const min = arr.length?arr[0]:'';
    const max = arr.length?arr[arr.length-1]:'';
    const med = arr.length?median(arr):'';
    const avg = arr.length?mean(arr):'';
    aggLines.push([ `"${name.replace(/"/g,'""')}"`, arr.length, min, med, avg, max ].join(','));
  });
  fs.writeFileSync(aggPath, aggLines.join('\n'), 'utf8');

  console.log('Wrote', detPath, 'and', aggPath);
})();
