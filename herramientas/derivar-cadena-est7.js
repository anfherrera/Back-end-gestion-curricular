// Uso: node derivar-cadena.js <obtenido> <salida> <referencia> [correcciones...]
const fs = require('fs');
const [obt, sal, ref, ...cadena] = process.argv.slice(2);
const leer = p => JSON.parse(fs.readFileSync(p, 'utf8'));
let esperado = leer(ref);
for (const c of cadena) {
  const cor = leer(c);
  for (const [k, v] of Object.entries(cor)) {
    if (k.startsWith('/')) {
      const partes = k.slice(1).split('/').map(s => s.replace(/~1/g, '/').replace(/~0/g, '~'));
      let o = esperado; for (const p of partes.slice(0, -1)) o = o[p]; o[partes.at(-1)] = v;
    } else esperado[k] = v;
  }
}
const nuevo = leer(obt);
const canon = v => Array.isArray(v) ? v.map(canon) : v && typeof v === 'object' ? Object.fromEntries(Object.keys(v).sort().map(k => [k, canon(v[k])])) : v;
const igual = (a, b) => JSON.stringify(canon(a)) === JSON.stringify(canon(b));
if (!igual(Object.keys(nuevo).sort(), Object.keys(esperado).sort())) console.log('ERROR: las claves no coinciden');
const out = {};
for (const k of Object.keys(nuevo)) if (!igual(nuevo[k], esperado[k])) { out[k] = nuevo[k]; console.log('CAMBIA', k, JSON.stringify(nuevo[k]).slice(0, 160)); }
fs.writeFileSync(sal, JSON.stringify(out, null, 2) + '\n');
console.log(Object.keys(out).length + ' entradas');
