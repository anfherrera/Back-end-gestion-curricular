Sigue la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`), y puedes cambiarlo cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, la fila 26 del registro y la entrada EST-7 (cerrada): la convención y "Migración hecha" (el paso 9 es el modelo más reciente). Respóndeme siempre en español.

## De dónde partimos

- EST-7 está cerrado (`[x]`): los pasos 1 a 9 están hechos y cada una de las unas 206 rutas sigue la convención o se retiró. El paso 9 (estadísticas) es el modelo más reciente en "Migración hecha".
- BUG-50, BUG-47 y BUG-51 están cerrados (2026-11-24), con un commit cada uno; BUG-51 también en el frontend. Ver sus cierres en `REVISION_CODIGO.md`.
- Último `clean verify` (BUG-51), en JDK 25 y 17: 609 pruebas, 1 omitida y 0 fallos (125 XML de surefire). Frontend: 785 pruebas sin fallos.
- El frontend tiene la estructura `src/app/features/<proceso>/{estudiante,revision}` y Husky con lint-staged: cada commit pasa ESLint sobre los `.ts` y `.html` de `src/` preparados.
- Solo queda abierto OPC-1; no lo toques sin preguntar.
- Nada está subido: el frontend va 10 commits por delante de su remoto y el backend 6. Pregunta antes de subir.
- Visto en el paso 9 y sin anotar como punto: `GestionCurricularApplication` declara `@ComponentScan(basePackages = "co.edu.unicauca.decanatura")` sin el `TypeExcludeFilter`, así que las `@TestConfiguration` anidadas de las pruebas (por ejemplo, la de datos fijos de `EstadisticasEndpointsCaracterizacionTest`) entran en el contexto de todas las `@SpringBootTest`. Por eso las pruebas nuevas usan `@MockBean`, y un controlador de prueba lleva un `@Profile` que nunca se activa. Pregunta al usuario si lo quiere como punto nuevo.

## Lo siguiente

- Pregunta al usuario: OPC-1, el punto del `@ComponentScan` o subir las ramas.

## Método (el de los pasos 7 a 9)

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
