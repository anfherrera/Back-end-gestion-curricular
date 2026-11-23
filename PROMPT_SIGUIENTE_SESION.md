Sigue la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`), y puedes cambiarlo cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, la fila 26 del registro y la entrada EST-7, sobre todo la convención, el plan (pasos 8 y 9) y "Migración hecha" (los pasos 7a y 7b son el modelo más reciente). Respóndeme siempre en español.

## De dónde partimos

- EST-7: pasos 1 a 7 hechos, con 121 de unas 206 rutas. Los dos repositorios están subidos hasta el paso 7b: backend `04c3e05` (7a) y `28f5476` (7b); frontend `3817b9e` (7a) y `4994acc` (7b).
- Último `clean verify`, en JDK 25 y 17: 606 pruebas, 1 omitida y 0 fallos (122 XML de surefire). Frontend: 778 pruebas sin fallos.
- En el frontend, `SolicitudesBaseService.cambiarEstado` usa ya `PUT /{id}/estado` para todos los procesos.

Quedan abiertos, y no los toques sin preguntar: BUG-47 (mensaje de validación no determinista) y OPC-1 (opcional).

## Paso 8: cursos de verano (pide antes las decisiones al usuario)

Seis controladores, unas 55 rutas: `CursoOfertadoRestController`, `CursosVeranoRestController`, `PreinscripcionesVeranoRestController`, `InscripcionesVeranoRestController`, `CatalogosCursosVeranoRestController` y `ReportesCursosVeranoRestController`. Cinco comparten `/api/cursos-intersemestrales` y otro usa `/api/cursos-verano`, con rutas duplicadas. Antes de tocar nada:
- Pregunta al usuario el prefijo único (el plan propone `/api/cursos-verano`) y sus subrecursos (`/cursos`, `/preinscripciones`, `/inscripciones`, `/catalogos`, `/reportes`).
- Pregunta qué hacer con los duplicados (`/cursos-verano/disponibles` y `/disponibles`, `aprobar` y `rechazar` de preinscripciones en dos controladores, `/api/archivos/inscripciones/{id}` y `/inscripciones/{id}/comprobante`).
- Pregunta si se reparte en varios commits (por controlador o por subrecurso), como el 7.
- Busca qué rutas no usa el frontend y propón retirarlas.

## Método (el de los pasos 7a y 7b)

1. Caracterización previa `@SpringBootTest` con varios roles (modelo: `RutasReingresoHomologacionCaracterizacionTest`), grabada en una copia contra el código actual con `-DgrabarReferencia=true` y pasada tres veces más sin grabar.
2. Antes del cambio, pasa contra el código actual las pruebas que usan esas rutas (busca también segmentos sueltos).
3. Migra; `/{id:\\d+}` con Edit y compruébalo con `grep`.
4. Pares (`herramientas/pares-*.json`, generados con reglas de sustitución y revisados) y `node herramientas/derivar-est7.js`; revisa "FUERA DE LOS PARES". Para obtener la salida nueva, en la copia cambia el nombre de la referencia y graba. En Git Bash, los argumentos que empiezan por `/` se convierten en rutas de Windows: usa `MSYS_NO_PATHCONV=1` y rutas `C:/…` para los scripts.
5. Pruebas existentes sin regrabar: conserva la clave y pide la nueva; fija lo retirado con correcciones `-est7` encadenadas; en las funcionales, pasa cada paso a su equivalente o bórralo. Para derivar correcciones, en la copia haz que las pruebas vuelquen lo que obtienen (y que `Correcciones.aplicar` devuelva la referencia si falta el archivo). Ojo: en `PazYSalvoCaracterizacionTest` y `ListadosPorRolCaracterizacionTest`, `-DgrabarReferencia=true` sobrescribe las referencias.
6. Frontend: servicios, specs con URL y método exactos, `cy.intercept` (un comodín `/**` no encaja con la raíz con parámetros: usa expresiones regulares) e interceptores.
7. "Migración hecha" y fila 26; `clean verify` en JDK 25 y 17 en dos copias a la vez; `npx ng test --watch=false`, prettier, lint y `tsc` de lo tocado (el lint y prettier de varios `cypress/e2e/*.cy.ts` y el `tsc` de Cypress ya fallaban antes); un commit por repositorio, en ASCII y con el ID. Di cuántas rutas quedan.

## Reglas que siguen vigentes

- Nunca regrabes una referencia que ya está en el repositorio: usa correcciones `-est7` encadenadas.
- Compila siempre en una copia del scratchpad (`tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <dir>` y `./mvnw -o -q …`), nunca en `gestion_curricular/target`. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- No hay Python: usa Edit, perl o Node. Para sustituciones de varias líneas, mejor Edit; revisa el `git diff` después de cada sustitución.
- Pregunta antes de cambiar un contrato más allá de lo decidido.
- No subas nada hasta que el usuario lo pida.
- Mockito no puede simular clases concretas en JDK 25: usa interfaces o instancias reales.
- EST-7 se puede dejar a medias: cada paso es completo por sí solo. Después del 8 queda el 9 (estadísticas, 30 rutas, con las reglas de `SeguridadConfig` para `/api/estadisticas/**`).
