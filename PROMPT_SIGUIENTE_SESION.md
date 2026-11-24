Sigue la revisión de código del backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`), y puedes cambiarlo cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md` y la fila 27 del registro. Respóndeme siempre en español.

## De dónde partimos (2026-11-24)

- La revisión 27 está terminada y subida: las dos ramas están al día con su remoto. El frontend no cambió en ella.
- Toda la lista de `REVISION_CODIGO.md` está cerrada (EST-7, OPC-1 y los BUG hasta el 51), salvo **BUG-52** y **BUG-53**, que están abiertos y sin decidir.
- Último `clean verify`, en JDK 25 y 17: 611 pruebas, 1 omitida y 0 fallos (127 XML de surefire). Frontend: 785 pruebas sin fallos.

## Lo siguiente: cerrar el backend

Esta debería ser la última sesión de la revisión. Propón al usuario este orden y deja que elija la política de cada punto antes de tocar nada:

1. **Borrar el código muerto que dejó EST-7.** Cada paso de la migración anotó en su entrada lo que quedó sin uso ("Código muerto tras el paso, sin borrar"); búscalo en `REVISION_CODIGO.md`. Por ejemplo:
   - En Paz y Salvo: `revisarDocumentosRequeridos` y las ramas `porDefecto` y `pdf` de `generarPazYSalvo`.
   - En Reingreso y Homologación: los listados antiguos.
   - En cursos de verano: `crearCurso(CursoOfertadoVerano)`, `actualizarCurso`, `completarInscripcion`, `contarInscripcionesPorEstado`, `PermisosCursoVerano`, `EstadoCursoOfertadoMapper` y varios DTO y records.
   - En estadísticas: `EstadisticaMapperDominio`, tres DTO, `pdfGeneral` y `excelGeneral`, y los métodos del puerto que solo atendían rutas retiradas.

   Antes de borrar cada cosa, confirma con `grep` que nada de `main` la usa. Si solo la usan pruebas, cuenta cuáles. Propón un nuevo punto MUE (por ejemplo, MUE-11) con un commit por área. No cambia ninguna respuesta HTTP, así que las caracterizaciones deben pasar sin tocarlas. Si una prueba solo probaba código borrado, se borra con él y se anota.
2. **BUG-52.** `GestionarCursoOfertadoVeranoCUAdapter.actualizarCurso` mira el primer estado del curso, con la condición al revés, y ninguna ruta lo usa. Recomienda borrarlo dentro del punto 1, junto con su puerto y sus casos en `CursosVeranoUnidadTest`. Los casos de `ErroresCasosDeUsoCaracterizacionTest` se quitarían con una corrección encadenada, sin regrabar la referencia. La otra opción es corregir la condición.
3. **BUG-53.** Un curso cuyo último estado no tiene texto hace que `TransicionEstadoCurso.validar` lance un `NullPointerException` (500). Lo fija `estados/curso-verano.json`. Políticas posibles: tratarlo como `Sin_Estado`, como estado desconocido (400 "Estado desconocido") o dejarlo. El cambio se fija con una corrección encadenada sobre esa referencia, y la prueba debe fallar con el código anterior.
4. **Opcional:** `GestionCurricularApplication` declara `@ComponentScan(basePackages = "co.edu.unicauca.decanatura")` sin el `TypeExcludeFilter`. Por eso las `@TestConfiguration` anidadas de las pruebas entran en el contexto de todas las `@SpringBootTest`; por ejemplo, el caso de uso de estadísticas con datos fijos de `EstadisticasEndpointsCaracterizacionTest` es `@Primary` en todas ellas. Mientras no se decida, las pruebas nuevas usan `@MockBean`, y si necesitan un controlador propio le ponen un `@Profile` que nunca se activa (ver `ValidacionSinValidatedTest`). Corregirlo puede cambiar lo que ven otras pruebas: hay que caracterizar antes.

Al terminar, actualiza la cabecera de `REVISION_CODIGO.md` si la revisión queda cerrada, y añade la fila 28.

## Método

1. Antes de cambiar comportamiento, graba una caracterización contra el código actual. Se graba en una copia con `-DgrabarReferencia=true` y `-Dtest=` solo de la nueva, porque otras sobrescriben su referencia. Después se pasa tres veces más sin grabar. Si el resultado no es determinista (como en BUG-47), escribe una prueba que falle con el código anterior.
2. Antes del cambio, pasa contra el código actual las pruebas existentes que tocan lo mismo.
3. Las diferencias se fijan con correcciones encadenadas (`Correcciones`, `GrafoObjetos.compararConReferencia(..., correccion...)`). Para derivarlas usa `herramientas/derivar-est7.js` (pares de rutas) o `herramientas/derivar-cadena-est7.js` (referencia más su cadena). Revisa cada entrada nueva contra la referencia y su cadena. Las claves que empiezan por `/` se escriben como JSON Pointer (`/~1api~1…`).
4. Si la salida guarda fechas que vienen de parámetros, guárdalas como texto en la zona de la JVM y comprueba que pasa con `-DargLine=-Duser.timezone=UTC`.
5. Frontend, si hace falta: specs con URL y método exactos (`expectOne({ method, url })`). Pasa `npx ng test --watch=false`, prettier, lint y `tsc` de lo tocado. El prettier de `api-endpoints.ts` (línea `DESCARGAR_PDF`) y de `cypress/e2e/03-cursos-intersemestrales.cy.ts`, y el lint de `cypress/e2e/*.cy.ts`, ya fallaban antes. Husky con lint-staged pasa ESLint en cada commit.
6. Documenta el cierre en la entrada del punto y en la fila del registro. Pasa `clean verify` en JDK 25 y 17, en dos copias a la vez. Haz un commit por punto y por repositorio, con el mensaje en ASCII, con el ID del punto y pasado con `-F archivo`.

## Reglas

- Nunca regrabes una referencia que ya está en el repositorio: usa correcciones encadenadas.
- Compila siempre en una copia del scratchpad (`tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <dir>` y `./mvnw -o -q …`), nunca en `gestion_curricular/target`. Al resincronizar la copia, borra antes su `src`, para que no queden archivos borrados. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- Lanza en segundo plano las tandas largas de Maven (las tres pasadas y `clean verify`). Si algo se interrumpe, mira `git status` antes de seguir: en la revisión 27, un punto de control del editor devolvió los archivos al estado anterior al commit.
- No hay Python: usa Edit, perl o Node, y revisa el `git diff` después de cada sustitución.
- Pregunta antes de cambiar un contrato más allá de lo decidido, y deja que el usuario elija la política de cada punto.
- No subas nada hasta que el usuario lo pida.
- Mockito no puede simular clases concretas en JDK 25: usa interfaces o instancias reales.
