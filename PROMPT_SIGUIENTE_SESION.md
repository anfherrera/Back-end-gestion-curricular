Sigue la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`), y puedes cambiarlo cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, la fila 26 del registro y la entrada EST-7: la convención, el plan y "Migración hecha" (el paso 8c es el modelo más reciente). Respóndeme siempre en español.

## De dónde partimos

- EST-7: pasos 1 a 8 hechos (8a, 8b y 8c), con 176 de unas 206 rutas. Ya no queda ninguna ruta en `/api/cursos-intersemestrales`. EST-7 está sin marcar (`[ ]`) hasta terminar el paso 9.
- Último `clean verify` (8c), en JDK 25 y 17: 604 pruebas, 1 omitida y 0 fallos (123 XML de surefire). Frontend: 785 pruebas sin fallos.
- El frontend tiene ahora la estructura `src/app/features/<proceso>/{estudiante,revision}` y Husky con lint-staged: cada commit pasa ESLint sobre los `.ts` y `.html` de `src/` preparados.
- Quedan abiertos, y no los toques sin preguntar: BUG-47, BUG-50, BUG-51 y OPC-1.

## Paso 9 (lo siguiente): `EstadisticasRestController`

- Son unas 30 rutas en `/api/estadisticas`, con muchos consumidores en el frontend (`estadisticas.service.ts`, los dashboards del módulo estadístico y el de cursos de verano).
- Lee la fila 9 del plan. CRUD con verbos (`/crear`, `/actualizar`, `/eliminar/{id}`, `/buscarPorId/{id}`), consultas en camelCase (`/porPeriodoEstadoPrograma`) junto a otras en kebab-case, y exportaciones `/export/pdf…` y `/export/excel…` (el plan propone `GET /reportes?formato=&alcance=`).
- `SeguridadConfig` tiene reglas de URL para `/api/estadisticas/**` (POST `/crear`, PUT y DELETE solo Administrador; el resto, roles administrativos): revísalas en el mismo paso, porque cambiar las rutas puede abrirlas o cerrarlas.
- Antes de migrar, haz el inventario (rutas, quién las usa en el frontend, cuáles no usa nadie) y pregunta al usuario las decisiones, como en los pasos 6 a 8: la forma de las consultas, las exportaciones, qué se retira y si se reparte en varios commits.
- Al terminar el paso 9, EST-7 vuelve a `[x]`.

## Método (el de los pasos 7 y 8; en el 8c se usó tal cual)

1. Caracterización previa `@SpringBootTest` con varios roles, grabada en una copia contra el código actual con `-DgrabarReferencia=true` y pasada tres veces más sin grabar.
2. Antes del cambio, pasa contra el código actual las pruebas que usan esas rutas. La lista del grupo de verano: `AutorizacionPorRolTest`, `Catalogos…`, `CursoNuevoFecha…`, `CursosVerano{Aceptacion,Integracion,Endpoints…,Respuestas…}`, `Decisiones…`, `Descargas…`, `ErroresLocales…`, `GestionCursosVeranoFuncionalTest`, `Inscripciones…`, `NombresApi…`, `PeriodoPredeterminado…`, `Preinscripciones…`, `Rutas*Verano*`, `RutasPreinscripcionesInscripciones…`, `SalonInexistente…`, `SeguimientoDashboard…`, `SolicitudesVeranoReporte…` y `EstadisticasCursosVerano…`.
3. Migra; `/{id:\\d+}` con Edit y compruébalo con `grep` (un `\d` sin escapar no compila).
4. Pares y `node herramientas/derivar-est7.js`; revisa "FUERA DE LOS PARES". Si la comparación falla, la salida nueva queda en `target/referencias-obtenidas/`. Las claves que empiezan por `/` se escriben en la corrección como JSON Pointer (`/~1api~1…`).
5. Para derivar correcciones, en la copia haz que `Correcciones.aplicar` devuelva la referencia si falta el archivo y que las pruebas vuelquen lo que obtienen a `target/obtenido-*.json`; `herramientas/derivar-cadena-est7.js` aplica la cadena. Revisa cada corrección nueva entrada por entrada contra su referencia más la cadena anterior, sin `body.url`, antes de encadenarla. Ojo: varias caracterizaciones sobrescriben su referencia con `-DgrabarReferencia=true`; graba solo con `-Dtest=` de la nueva. Las claves de las correcciones que empiezan por `/` (por ejemplo `/materias falla`) se escriben como JSON Pointer (`/~1materias falla`): `derivar-cadena-est7.js` no lo hace solo. Si la caracterización guarda fechas que vienen de parámetros, guárdalas como texto en la zona de la JVM y comprueba que pasa con `-DargLine=-Duser.timezone=UTC`.
6. Frontend: servicios, specs con URL y método exactos, y `cy.intercept` con expresiones regulares (escríbelas con Edit o Node: perl pierde las barras invertidas).
7. Documenta en "Migración hecha" y en la fila 26. Pasa `clean verify` en JDK 25 y 17 en dos copias a la vez; en el frontend, `npx ng test --watch=false`, prettier, lint y `tsc` de lo tocado (prettier de `api-endpoints.ts` y de `cypress/e2e/03-cursos-intersemestrales.cy.ts`, y el lint de `cypress/e2e/*.cy.ts`, ya fallaban antes). Un commit por repositorio, en ASCII y con el ID. En PowerShell, pasa el mensaje del commit con `-F archivo` (un here-string como argumento se toma como ruta).

## Reglas que siguen vigentes

- Nunca regrabes una referencia que ya está en el repositorio: usa correcciones `-est7` encadenadas.
- Compila siempre en una copia del scratchpad (`tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <dir>` y `./mvnw -o -q …`), nunca en `gestion_curricular/target`. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- No hay Python: usa Edit, perl o Node; revisa el `git diff` después de cada sustitución.
- Pregunta antes de cambiar un contrato más allá de lo decidido.
- No subas nada hasta que el usuario lo pida.
- Mockito no puede simular clases concretas en JDK 25: usa interfaces o instancias reales.
