Sigue la revisión de código del backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`), y puedes cambiarlo cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md` y la fila 27 del registro. Respóndeme siempre en español.

## De dónde partimos (2026-11-24)

- La revisión 27 está terminada en local, en tres commits más este documento, y sin subir. El frontend no cambió.
- EST-7 está cerrado: los pasos 1 a 9 de la migración de rutas están hechos, y cada una de las unas 206 rutas sigue la convención o se retiró. La convención y "Migración hecha" están en la entrada EST-7.
- BUG-47, BUG-50 y BUG-51 están cerrados, con un commit cada uno; BUG-51 también en el frontend. Sus cierres están en `REVISION_CODIGO.md`.
- OPC-1 está cerrado (tramos 1 a 3; ver su entrada). Frontend: 785 pruebas sin fallos.
- Último `clean verify`, en JDK 25 y 17: 611 pruebas, 1 omitida y 0 fallos (127 XML de surefire).
- Solo quedan abiertos BUG-52 y BUG-53, los dos sin decidir y con su descripción en `REVISION_CODIGO.md`.

## Lo siguiente

Pregunta al usuario qué quiere abordar. Hay dos opciones:

1. **BUG-52 o BUG-53.** Propón políticas (BUG-52: corregir o borrar como código muerto; BUG-53: cómo tratar un estado sin texto) y deja que el usuario elija.
2. **Un punto nuevo, si el usuario lo quiere.** `GestionCurricularApplication` declara `@ComponentScan(basePackages = "co.edu.unicauca.decanatura")` sin el `TypeExcludeFilter`. Por eso las `@TestConfiguration` anidadas de las pruebas entran en el contexto de todas las `@SpringBootTest`. Por ejemplo, el caso de uso de estadísticas con datos fijos de `EstadisticasEndpointsCaracterizacionTest` es `@Primary` en todas ellas. Mientras no se decida, las pruebas nuevas usan `@MockBean`, y si necesitan un controlador propio le ponen un `@Profile` que nunca se activa (ver `ValidacionSinValidatedTest`). Corregirlo puede cambiar lo que ven otras pruebas: hay que caracterizar antes.

## Método

1. Antes de cambiar comportamiento, una caracterización grabada contra el código actual. Se graba en una copia con `-DgrabarReferencia=true` y `-Dtest=` solo de la nueva, porque otras sobrescriben su referencia. Después se pasa tres veces más sin grabar. Si el resultado no es determinista (como en BUG-47), escribe una prueba que falle con el código anterior.
2. Antes del cambio, pasa contra el código actual las pruebas existentes que tocan lo mismo.
3. Las diferencias se fijan con correcciones encadenadas (`Correcciones`, `GrafoObjetos.compararConReferencia(..., correccion...)`). Para derivarlas usa `herramientas/derivar-est7.js` (pares de rutas) o `herramientas/derivar-cadena-est7.js` (referencia más su cadena). Revisa cada entrada nueva contra la referencia y su cadena. Las claves que empiezan por `/` se escriben como JSON Pointer (`/~1api~1…`).
4. Si la salida guarda fechas que vienen de parámetros, guárdalas como texto en la zona de la JVM y comprueba que pasa con `-DargLine=-Duser.timezone=UTC`.
5. Frontend: specs con URL y método exactos (`expectOne({ method, url })`). Pasa `npx ng test --watch=false`, prettier, lint y `tsc` de lo tocado. El prettier de `api-endpoints.ts` (línea `DESCARGAR_PDF`) y de `cypress/e2e/03-cursos-intersemestrales.cy.ts`, y el lint de `cypress/e2e/*.cy.ts`, ya fallaban antes. Husky con lint-staged pasa ESLint en cada commit.
6. Documenta el cierre en la entrada del punto y en la fila del registro. Pasa `clean verify` en JDK 25 y 17, en dos copias a la vez. Haz un commit por punto y por repositorio, con el mensaje en ASCII, con el ID del punto y pasado con `-F archivo`.

## Reglas

- Nunca regrabes una referencia que ya está en el repositorio: usa correcciones encadenadas.
- Compila siempre en una copia del scratchpad (`tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <dir>` y `./mvnw -o -q …`), nunca en `gestion_curricular/target`. Al resincronizar la copia, borra antes su `src`, para que no queden archivos borrados. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- No hay Python: usa Edit, perl o Node, y revisa el `git diff` después de cada sustitución.
- Pregunta antes de cambiar un contrato más allá de lo decidido, y deja que el usuario elija la política de cada punto.
- No subas nada hasta que el usuario lo pida.
- Mockito no puede simular clases concretas en JDK 25: usa interfaces o instancias reales.
