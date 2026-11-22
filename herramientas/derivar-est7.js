// Uso: node derivar-est7.js <referencia> <salida nueva> <pares.json> <corrección a escribir>
const fs = require('fs');
const [ref, nuevo, pares] = process.argv.slice(2, 5).map(p => JSON.parse(fs.readFileSync(p, "utf8")));
const destino = process.argv[5];
const canon = v => Array.isArray(v) ? v.map(canon) : v && typeof v === 'object'
  ? Object.fromEntries(Object.keys(v).sort().map(k => [k, canon(v[k])])) : v;
const igual = (a, b) => JSON.stringify(canon(a)) === JSON.stringify(canon(b));
const sinUrl = c => { const x = JSON.parse(JSON.stringify(c)); if (x && x.cuerpo && typeof x.cuerpo === 'object') delete x.cuerpo.url; return x; };
let errores = 0;
const err = m => { errores++; console.log('ERROR', m); };
if (!igual(Object.keys(ref).sort(), Object.keys(nuevo).sort())) err('las claves no coinciden');
const viejas = new Set(Object.values(pares));
for (const [n, v] of Object.entries(pares)) {
  if (!(n in nuevo) || !(v in ref)) { err('par sin clave: ' + n + ' <- ' + v); continue; }
  if (!igual(sinUrl(nuevo[n]), sinUrl(ref[v]))) err('distinta: ' + n + ' <- ' + v + '\n  nuevo ' + JSON.stringify(nuevo[n]) + '\n  viejo ' + JSON.stringify(ref[v]));
}
for (const v of viejas) {
  const c = nuevo[v];
  if (!c || c.status !== 404 || !c.cuerpo || c.cuerpo.codigoError !== 'GC-0404') err('la vieja no da 404: ' + v + ' ' + JSON.stringify(c));
}
const correccion = {};
for (const k of Object.keys(nuevo).sort()) {
  if (igual(nuevo[k], ref[k])) continue;
  correccion[k] = nuevo[k];
  if (!(k in pares) && !viejas.has(k)) console.log('FUERA DE LOS PARES: ' + k + '\n  antes   ' + JSON.stringify(ref[k]) + '\n  despues ' + JSON.stringify(nuevo[k]));
}
fs.writeFileSync(destino, JSON.stringify(correccion, null, 2));
console.log(Object.keys(correccion).length + ' entradas en la corrección; errores: ' + errores);
process.exit(errores ? 1 : 0);
