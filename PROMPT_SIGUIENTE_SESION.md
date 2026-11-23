Sigue la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`), y puedes cambiarlo cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, la fila 26 del registro y la entrada EST-7: la convención, el plan y "Migración hecha" (el paso 8a es el modelo más reciente). Respóndeme siempre en español.

## De dónde partimos

- EST-7: pasos 1 a 7 y 8a hechos, con 145 de unas 206 rutas. 8a tiene un commit en cada repositorio, sin subir: backend `6208fd2` y frontend `41bc948`.
- Último `clean verify` (8a), en JDK 25 y 17: 601 pruebas, 1 omitida y 0 fallos (121 XML de surefire). Frontend: 779 pruebas sin fallos.
- Quedan abiertos, y no los toques sin preguntar: BUG-47 y OPC-1.

## Decisiones del usuario para el paso 8 (ya tomadas, no las vuelvas a preguntar)

- Prefijo único `/api/cursos-verano`, con los subrecursos `/cursos`, `/preinscripciones`, `/inscripciones`, `/catalogos` y `/reportes`.
- De cada ruta duplicada se conserva la que usa el frontend. Se retiran las 23 rutas sin uso (404 y código del controlador borrado; los métodos del caso de uso sin uso se anotan como código muerto).
- Tres commits por repositorio: 8a (cursos, hecho), 8b (preinscripciones e inscripciones, en curso) y 8c (catálogos y reportes).
- 8b: las decisiones pasan a `PUT /{id}/estado`, con `nuevoEstado` y los mismos campos de hoy:
  - preinscripciones: `aprobada` con `comentarios`, `rechazada` con `motivo`;
  - inscripciones: `aceptada` con `observaciones`, `rechazada` con `motivo`.
- 8b, el resto de las rutas: `GET /preinscripciones?usuario=` y `?curso=`; los elegibles pasan a `GET /cursos/{id}/estudiantes-elegibles`, en `CursosVeranoRestController`; `POST /preinscripciones`, `POST /inscripciones` y `POST /solicitudes-curso-nuevo`.

## Paso 8b: lo hecho (sin commit, en la copia de trabajo del backend)

- Código:
  - `PreinscripcionesVeranoRestController` (base `/api/cursos-verano`) y `InscripcionesVeranoRestController` (base `/api/cursos-verano/inscripciones`) migrados.
  - Elegibles movidos a `CursosVeranoRestController`.
  - `DecisionSolicitudDTOPeticion` lee `nuevoEstado`.
  - Constructores sin `solicitudMapper`, `gestionarArchivos` ni `solicitudGeneralCU`.
  - Un `nuevoEstado` desconocido o ausente responde 400 GC-0004. Sin filtros, o con `usuario` y `curso` a la vez, `GET /preinscripciones` responde 400.
- Caracterización previa: `RutasPreinscripcionesInscripcionesCaracterizacionTest` (`rutas/preinscripciones-verano.json` y `rutas/inscripciones-verano.json`).
  - Se grabó contra el código anterior y pasó tres veces más.
  - Sus 25 y 16 pares (`herramientas/pares-*-verano.json`) se comprobaron con `derivar-est7.js`: 0 errores.
  - Las correcciones `rutas/correcciones/*-verano-est7.json` ya están en su sitio. Falta encadenarlas en la prueba (en `compararConReferencia`, como en 8a).
- Pruebas existentes ya adaptadas:
  - Piden la ruta nueva y conservan la clave: `PreinscripcionesVeranoRespuestas…`, `InscripcionesVeranoRespuestas…` (monta también `CursosVeranoRestController` para los elegibles), `DecisionesVerano…` (añade `nuevoEstado` a los cuerpos que son objetos), `ErroresLocales…`, `Descargas…` (el comprobante pide `/api/archivos/inscripciones/7`), `CursoNuevoFecha…` y `PeriodoPredeterminado…`.
  - Usan las rutas nuevas: `AutorizacionPorRolTest`, aceptación, integración y la funcional de cursos. En la funcional se quitó el paso de `GET /inscripciones`, sin equivalente.
- Correcciones derivadas y copiadas, PENDIENTES DE REVISAR:
  - `verano-endpoints/correcciones/decisiones-est7-paso8b.json` (20: los cuerpos `""`, `null`, `[]` y `{"extra":…}` dan 400 y cambia la `url`).
  - `preinscripciones-respuestas-est7-paso8b.json` (7) e `inscripciones-respuestas-est7-paso8b.json` (13).
  - `errores/correcciones/errores-locales-est7-paso8b.json` (44, deberían ser solo `url`).
  - `documentos/correcciones/descargas-est7-paso8b.json` (2: el comprobante de `/api/archivos` añade `Cache-Control: private, max-age=3600`).

## Paso 8b: lo que falta

1. Comprobar que cada corrección pendiente solo cambia lo dicho. Para eso, compara cada entrada con la referencia más su cadena, sin `body.url`. `herramientas/derivar-cadena-est7.js` aplica la cadena (`node … <obtenido> <salida> <referencia> [correcciones…]`).
2. Encadenar `descargas-est7-paso8b.json` en `DescargasCaracterizacionTest`, que aún no la aplica.
3. En la copia, pasar el grupo de verano (la lista está en "Método") y la caracterización nueva.
4. Documentar dos errores nuevos y abiertos; el siguiente número libre es BUG-50:
   - `GET /api/cursos-verano/preinscripciones?usuario=0` responde 500 y no 400. El `@Min` pasa igual en la ruta vieja y en la nueva.
   - El frontend pide `GET /api/cursos-intersemestrales/periodos-registrados` (`getPeriodosRegistrados`, `periodo-filtro-selector`), que no existe en el backend.
5. Frontend del 8b:
   - En `cursos-intersemestrales.service.ts` y `api-endpoints.ts`: `PREINSCRIPCIONES`, `INSCRIPCIONES`, `SOLICITUDES_CURSO_NUEVO`, `ESTUDIANTES_ELEGIBLES`, `preinscripciones/usuario|curso`, `aprobar|rechazar|aceptar` → `PUT /{id}/estado` con `nuevoEstado`, `observaciones`.
   - Borrar las constantes muertas (`APROBAR_PREINSCRIPCION`, `VALIDAR_PAGO`, `COMPLETAR_INSCRIPCION`, `PREINSCRIPCIONES_CURSO`, `INSCRIPCIONES_CURSO`, `SOLICITUDES_CURSO_NUEVO_USUARIO`…).
   - Actualizar su spec, el spec de aceptación del estudiante y `cypress/e2e/03-cursos-intersemestrales.cy.ts` (POST y GET de preinscripciones).
6. `REVISION_CODIGO.md`: el 8b en "Migración hecha", con su código muerto (p. ej. `contarInscripcionesPorEstado` y `buscarSolicitudesPorUsuario`, si quedan sin uso, y los DTO de las rutas retiradas), y la fila 26.
7. `clean verify` en JDK 25 y 17, `ng test`, prettier, lint y `tsc`; un commit por repositorio. Quedarán unas 61 − 20 = 41 rutas.

## Paso 8c (después)

- `CatalogosCursosVeranoRestController` → `/api/cursos-verano/catalogos/…`: `materias`, `materias-disponibles`, `materias-filtro`, `docentes`, `salones` y `condiciones`. Se retira `permisos-estado/{estado}/{rol}`, sin uso.
- `ReportesCursosVeranoRestController` → `/api/cursos-verano/reportes/…`: `solicitudes` (`?formato=excel`), `dashboard` y el seguimiento (`/seguimiento?usuario=`).
- En el frontend, la demo de usabilidad ya intercepta `/api/(cursos-intersemestrales|cursos-verano)/`. Simplifícala a `cursos-verano` y quita las constantes `CURSOS_INTERSEMESTRALES.BY_ID`, `BY_ESTADO`… si nadie las usa.
- Después queda el paso 9 (estadísticas).

## Método (el de los pasos 7 y 8a)

1. Caracterización previa `@SpringBootTest` con varios roles, grabada en una copia contra el código actual con `-DgrabarReferencia=true` y pasada tres veces más sin grabar.
2. Antes del cambio, pasa contra el código actual las pruebas que usan esas rutas. La lista del grupo de verano: `AutorizacionPorRolTest`, `Catalogos…`, `CursoNuevoFecha…`, `CursosVerano{Aceptacion,Integracion,Endpoints…,Respuestas…}`, `Decisiones…`, `Descargas…`, `ErroresLocales…`, `GestionCursosVeranoFuncionalTest`, `Inscripciones…`, `NombresApi…`, `PeriodoPredeterminado…`, `Preinscripciones…`, `Rutas*Verano*`, `SalonInexistente…`, `SeguimientoDashboard…`, `SolicitudesVeranoReporte…` y `EstadisticasCursosVerano…`.
3. Migra; `/{id:\\d+}` con Edit y compruébalo con `grep` (un `\d` sin escapar no compila).
4. Pares y `node herramientas/derivar-est7.js`; revisa "FUERA DE LOS PARES". Si la comparación falla, la salida nueva queda en `target/referencias-obtenidas/`. Las claves que empiezan por `/` se escriben en la corrección como JSON Pointer (`/~1api~1…`).
5. Para derivar correcciones, en la copia haz que `Correcciones.aplicar` devuelva la referencia si falta el archivo y que las pruebas vuelquen lo que obtienen a `target/obtenido-*.json`. Ojo: varias caracterizaciones sobrescriben su referencia con `-DgrabarReferencia=true`; graba solo con `-Dtest=` de la nueva.
6. Frontend: servicios, specs con URL y método exactos, y `cy.intercept` con expresiones regulares (escríbelas con Node: perl pierde las barras invertidas).
7. Documenta en "Migración hecha" y en la fila 26. Pasa `clean verify` en JDK 25 y 17 en dos copias a la vez; en el frontend, `npx ng test --watch=false`, prettier, lint y `tsc` de lo tocado (el lint de `cypress/e2e/*.cy.ts` y prettier de `api-endpoints.ts` ya fallaban antes). Un commit por repositorio, en ASCII y con el ID.

## Reglas que siguen vigentes

- Nunca regrabes una referencia que ya está en el repositorio: usa correcciones `-est7` encadenadas.
- Compila siempre en una copia del scratchpad (`tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <dir>` y `./mvnw -o -q …`), nunca en `gestion_curricular/target`. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- No hay Python: usa Edit, perl o Node; revisa el `git diff` después de cada sustitución.
- Pregunta antes de cambiar un contrato más allá de lo decidido.
- No subas nada hasta que el usuario lo pida.
- Mockito no puede simular clases concretas en JDK 25: usa interfaces o instancias reales.
- Al empezar esta sesión, `PROMPT_SIGUIENTE_SESION.md` apareció borrado en la copia de trabajo (no se incluyó en el commit de 8a); este archivo lo sustituye.
