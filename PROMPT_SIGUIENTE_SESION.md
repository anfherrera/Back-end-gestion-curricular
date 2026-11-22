Sigue la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`): ahí van todos los commits del frontend de las revisiones 25 y 26. Puedes cambiar ese código cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, la fila 26 del registro y la entrada EST-7 (convención de rutas, plan de migración y "Migración hecha").

## De dónde partimos

En la revisión 26 ya se cerraron BUG-46 (el hash de la contraseña ya no pasa al dominio) y BUG-45 (`GET /api/solicitudes-pazysalvo?rol=…&bandeja=procesadas`). También están hechos los pasos 1 y 2 de la migración de rutas de EST-7: roles, programas, docentes, usuarios, períodos académicos y tipos de documento. El último `clean verify`, en JDK 25 y 17, dio 604 pruebas, 1 omitida y 0 fallos (116 XML de surefire), y el frontend, 764 pruebas sin fallos. Todo está subido.

Quedan abiertos:
- OPC-1, que es opcional.
- BUG-47: el mensaje de un 400 de validación no es determinista cuando un campo viola dos restricciones. Falta elegir la política.
- BUG-48: la página "Configurar período" llama a `/api/periodos-academicos/admin/periodo-activo`, que no existe. Falta decidir si se crea la función o se quita la página.

No los toques sin preguntar.

## Ahora: paso 3 de EST-7

El paso 3 son tres controladores:
- `DocumentoRestController`: `GET /buscarPorId/{id}`, `PUT /agregarComentario` y `GET /{id}/descargar`.
- `GeneradorDocumentosRestController`: `POST /generar` y `GET /templates/{proceso}`. Comparte el prefijo `/api/documentos` con el anterior.
- `ArchivosRestController`: `POST /subir/pdf`, `GET /descargar/pdf?filename=` y `GET /descargar/pdf/inscripcion/{id}`.

Antes de tocar nada, pregunta al usuario:
1. El generador comparte `/api/documentos`, pero la convención exige un controlador por prefijo. Las opciones son pasarlo a `/api/documentos-generados` o unirlo al controlador de documentos.
2. Qué forma dar a las rutas de archivos. El plan propone `POST /api/archivos` y `GET /api/archivos/{nombre}`; el nombre va hoy como parámetro de consulta y puede llevar caracteres raros. Y qué hacer con la descarga del comprobante de una inscripción.

Tenlo en cuenta en este paso:
- `SeguridadConfig` tiene reglas para `/api/documentos/generar` y `/api/documentos/templates/**`, y hay que cambiarlas con las rutas.
- `AutorizacionPorRolTest` usa `/api/documentos/buscarPorId/…`, `/api/documentos/{id}/descargar` y `/api/archivos/descargar/pdf`. Esas pruebas fijan la seguridad por dueño, y deben seguir haciéndolo con las rutas nuevas.
- Ya hay caracterizaciones de descargas y subidas (`DescargasCaracterizacionTest`, `DescargaDocumentoPorIdCaracterizacionTest`, `SubirPdf*`, `ComentarioDocumentoCaracterizacionTest`, `documentos/generacion.json`). Si sus referencias guardan la `url` de un error, hará falta una corrección `-est7`.

Después vienen los pasos 4 (notificaciones), 5 (solicitudes) y 6 (ECAES). Los pasos 7, 8 y 9 son más grandes. El 8 necesita que el usuario decida antes el prefijo único de cursos de verano.

## Cómo se migra un controlador (método de los pasos 1 y 2)

1. Busca todos los consumidores de las rutas: el servicio del frontend, `api-endpoints.ts`, specs, Cypress, pruebas del backend, `SeguridadConfig` y `OpenAPIConfig`. Busca también comprobaciones de URL con `includes(...)` en interceptores: en el paso 2, el interceptor de errores esperaba una barra final.
2. Crea una caracterización standalone (`SoporteMvc.standaloneSetup` + `RestApiExceptionHandler`, casos de uso simulados y mappers reales). Debe pedir cada operación por la ruta vieja y por la nueva, y guardar `status`, `tipo` y `cuerpo` con la clave "MÉTODO ruta cuerpo". Ejemplos: `RutasCatalogosCaracterizacionTest`, `RutasUsuarioCaracterizacionTest` y `RutasPeriodosTiposCaracterizacionTest`.
   - Grábala contra el código anterior con `-DgrabarReferencia=true` y pásala tres veces más sin grabar, para comprobar que es estable.
   - Usa cuerpos inválidos que violen una sola restricción, por BUG-47.
   - Sustituye por marcadores los datos que dependan de la fecha.
3. Migra el controlador.
   - El id va restringido a dígitos, `/{id:\\d+}`: en Java hacen falta dos barras. Así las rutas viejas responden 404 y no se toman por un id.
   - En `PUT /{id}`, el id de la ruta manda sobre el del cuerpo. Si hay un `@PreAuthorize` que mira el id, debe evaluar `#id` y no `#peticion.…`, y lo fija un caso de `AutorizacionPorRolTest`.
   - Un único `GET /` atiende el listado y las búsquedas por parámetro, y conserva el tipo de cada respuesta: un objeto o una lista.
4. Deriva la corrección `rutas/correcciones/<recurso>-est7.json` con Node.
   - Borra la referencia en una copia del scratchpad, porque `GrafoObjetos` solo graba si no existe. Graba ahí la salida nueva y compárala con la referencia del repositorio.
   - Usa un archivo de pares `{"clave nueva": "clave vieja"}`. El script comprueba que cada clave nueva responde igual que la vieja, sin contar la `url` de los errores; que la vieja da 404; y lista los cambios fuera de los pares para revisarlos.
   - Compara sin tener en cuenta el orden de las claves (JSON canónico con las claves ordenadas): Jackson compara así, y algunas referencias están escritas en otro orden.
   - Escribe las claves de la corrección en orden de `TreeMap`.
   - El script estaba en el scratchpad y puede haberse borrado; rehazlo con esa lógica.
5. Las pruebas existentes que llamen a las rutas viejas pasan a las nuevas. Su referencia no se regraba: se añade una corrección `-est7` encadenada al final.
6. En el frontend, cambia el servicio y escribe o ajusta su spec, que debe fijar el método y la URL de cada llamada.
7. Añade el punto a "Migración hecha" en la entrada EST-7 y luego verifica: `clean verify` en JDK 25 y 17 (las dos copias pueden correr a la vez) y `npx ng test --watch=false` en el frontend.
8. Haz un commit por controlador en cada repositorio, con el mensaje en ASCII y el ID (`EST-7, paso N`). Si dos controladores comparten caracterizaciones, puede ir uno solo; dilo en la nota. Al terminar, completa la fila 26 del registro.

## Reglas que siguen vigentes

- Antes de cambiar algo, pasa una vez la caracterización que lo cubre. Si nada lo cubre, crea una y grábala contra el código anterior.
- Nunca se regraba una referencia. Un cambio de respuesta se fija con una corrección `correcciones/<ref>-<punto>.json` (`soporte/Correcciones`: claves de primer nivel o JSON Pointer; el padre debe ser un objeto). Las correcciones se encadenan en orden, y `GrafoObjetos` y `ProyeccionValores` aceptan varias.
- `ListadosPorRolCaracterizacionTest` regraba todas sus referencias si se pasa `-DgrabarReferencia=true`. Para grabar ahí, ejecuta solo el método nuevo (`-Dtest='Clase#metodo'`).
- Mockito no puede simular clases concretas en este proyecto con JDK 25 (`JwtUtil`, `SecurityAuditService`, `LoginRateLimiter`). Usa instancias reales o interfaces.
- Si renombras o agregas un campo en un modelo del dominio, revisa `infraestructura/configuracion/NombresJsonDominio` (OPC-2).
- Si el usuario tiene la aplicación abierta (`spring-boot:run` o desde VS Code), no toques `gestion_curricular/target`. Compila siempre en una copia: `tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <scratchpad>/<dir>` y luego `./mvnw -o -q clean verify`. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- No hay Python: usa Edit, sed, perl o Node. La copia de trabajo mezcla LF y CRLF. Al usar perl, no supongas un fin de línea y revisa el `git diff`; los escapes de regex se pierden con facilidad entre el shell y perl.
- Pregunta antes de lo que cambie un contrato más allá de la ruta: quitar rutas, unir respuestas distintas o cambiar la semántica.

## Para probar a mano

- Backend: `SPRING_DATASOURCE_PASSWORD=root JAVA_HOME="C:/Program Files/Java/jdk-17" ./mvnw -o spring-boot:run` en `gestion_curricular` (puerto 5000, MySQL `bdcurricular`, usuario y contraseña `root`).
- Frontend: `npx ng serve` (http://localhost:4200).
- Usuarios de prueba, todos con la contraseña `password123` y el dominio `@unicauca.edu.co`:
  - `admin@`
  - `josefina@` (coordinadora)
  - `juanp@` (funcionario)
  - `maria@` (secretaria)
  - `decano@`
  - `dcisneros@` y `pepa@` (estudiantes)
