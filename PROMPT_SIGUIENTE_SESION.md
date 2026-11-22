Sigue la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`): ahí van todos los commits del frontend de las revisiones 25 y 26, y puedes cambiar ese código cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, la fila 26 del registro y la entrada EST-7 (convención de rutas, plan de migración y "Migración hecha").

## De dónde partimos

En la revisión 26 se cerraron BUG-46 y BUG-45, y se hicieron los pasos 1 a 5 de la migración de rutas de EST-7:
1. Roles, programas, docentes y usuarios.
2. Períodos académicos y tipos de documento.
3. Documentos, generador (`/api/documentos-generados`) y archivos (`POST /api/archivos`, `GET /api/archivos?nombre=`, `GET /api/archivos/inscripciones/{id}`).
4. Notificaciones: todo bajo `/api/notificaciones` con `?usuario=`, y `PUT /{id}/leida` y `PUT /leidas?usuario=` sin cuerpo.
5. Solicitudes: `GET /`, `GET ?nombre=`, `GET|DELETE /{id}` y `GET /historial?formato=pdf`.

El último `clean verify`, tras el paso 5, dio en JDK 25 y 17 608 pruebas, 1 omitida y 0 fallos (119 XML de surefire); el frontend, 773 pruebas sin fallos. Los pasos 1 a 4 están subidos. El paso 5 está en un commit en cada repositorio, pero sin subir: compruébalo con `git status -sb` y súbelo si el usuario lo pide.

Quedan abiertos, y no los toques sin preguntar:
- OPC-1, que es opcional.
- BUG-47: el mensaje de un 400 de validación no es determinista cuando un campo viola dos restricciones. Falta elegir la política.
- BUG-48: la página "Configurar período" llama a `/api/periodos-academicos/admin/periodo-activo`, que no existe. Falta decidir si se crea la función o se quita la página.
- BUG-49: la página "Detalle de solicitud" llama a `GET /api/solicitudes/{id}/historial`, que no existe. Falta decidir si se crea la ruta o si la página usa `GET /api/solicitudes/{id}`.
- `GET /api/archivos/inscripciones/{id}` duplica `/api/cursos-intersemestrales/inscripciones/{id}/comprobante`; se revisa en el paso 8.

## Lo que falta de EST-7

- **Paso 6, `SolicitudEcaesRestController`** (10 rutas): `/crearSolicitud-Ecaes`, `/listarSolicitudes-Ecaes…` y `/listarSolicitud-ecaes/porRol` → `POST /`, `GET /?rol=`. Las fechas (`/publicarFechasEcaes`, `/listarFechasEcaes`, `/buscarFechasPorPeriodo/{p}`, `/actualizarFechasEcaes`) → `/fechas` y `/fechas/{periodo}`. `/actualizarEstadoSolicitud` → `PUT /{id}/estado`. Ya hay caracterizaciones de ECAES (`ecaes/…`, por ejemplo `TiposDocumentoCaracterizacionTest`) y el frontend usa `pruebas-ecaes.service.ts`. Antes de tocarlo, pregunta lo que cambie el contrato más allá de la ruta. Por ejemplo: si `actualizarEstadoSolicitud` lleva el id en el cuerpo, en `PUT /{id}/estado` manda el de la ruta, y el `@PreAuthorize` debe mirar `#id`.
- **Paso 7, Reingreso, Homologación y Paz y Salvo juntos** (50 rutas), porque el frontend los usa a través de `SolicitudesBaseService`.
- **Paso 8, cursos de verano** (seis controladores). Antes hay que pedir al usuario que decida el prefijo único.
- **Paso 9, estadísticas** (30 rutas y reglas de `SeguridadConfig`).

## Cómo se migra un controlador

1. **Busca todos los consumidores de las rutas:** el servicio del frontend, `api-endpoints.ts`, specs, Cypress (`cy.intercept`), pruebas del backend, `SeguridadConfig` y `OpenAPIConfig`. Busca también comprobaciones de URL con `includes(...)` en los interceptores. Si aparece una ruta del frontend que el backend no tiene, anótala como BUG nuevo sin corregirla, como BUG-48 y BUG-49.
2. **Pregunta al usuario** lo que la convención no resuelva sola: un prefijo que ya es de otro controlador, un cuerpo nuevo obligatorio, quitar rutas o unir respuestas distintas. Usa opciones con una recomendada.
3. **Crea una caracterización `Rutas<Recurso>CaracterizacionTest`** que pida cada operación por la ruta vieja y por la nueva. Guarda `status`, `tipo`, `cuerpo` y las llamadas del caso de uso que cambian datos, con la clave "MÉTODO ruta cuerpo [quién]".
   - Ejemplos standalone: `RutasDocumentosArchivosCaracterizacionTest` (con descargas asíncronas) y `RutasSolicitudesCaracterizacionTest` (texto de un PDF).
   - Si la autorización vive en el código o en `@PreAuthorize` por método, usa un `@SpringBootTest` con seguridad real, como `RutasNotificacionesCaracterizacionTest`. Ahí se simulan `GestionarUsuarioGatewayIntPort` y, además, `UserDetailsService`, porque el adaptador del gateway es también el `UserDetailsService`. Si se simula un caso de uso de usuarios, el contexto no arranca.
   - Grábala contra el código anterior en una copia, con `-DgrabarReferencia=true`, y pásala tres veces más sin grabar.
   - Usa cuerpos inválidos que violen una sola restricción, por BUG-47. Sustituye por marcadores los datos que dependen de la fecha o del reloj.
4. **Migra el controlador.**
   - El id va restringido a dígitos: `/{id:\\d+}`, con dos barras en Java. Perl y sed pierden las barras con facilidad. Escribe esa línea con Edit y compruébala con `grep`.
   - Para que un `GET /` atienda varios filtros sin perder el `@PreAuthorize` ni el mensaje de `@Min` de cada operación, separa los métodos con condiciones `params` en `@GetMapping` (`params = {"usuario", "leida=false", "!urgente"}`). Añade un `@GetMapping` sin condiciones que lance `IllegalArgumentException` (400) para lo que nada atienda; sin él, Spring respondería 500.
   - El mensaje de un 400 de `@Min` lleva el nombre del método Java y del parámetro. Consérvalos con `@RequestParam(name = "nuevo") Integer nombreViejo`.
   - Si la ruta vieja ignoraba los parámetros desconocidos, no conviertas un valor desconocido en 400: el paso 5 dejó que solo `formato=pdf` cambie la respuesta.
5. **Deriva la corrección `rutas/correcciones/<recurso>-est7.json`** con `herramientas/derivar-est7.js`:
   ```
   node herramientas/derivar-est7.js <referencia vieja> <salida nueva> <pares.json> <corrección>
   ```
   - La salida nueva se graba en otra copia, sin la referencia y con la comparación sin corrección.
   - Los pares son `{"clave nueva": "clave vieja"}`; hay ejemplos en `herramientas/pares-*.json`.
   - El script falla si un par responde distinto (sin contar la `url` de los errores) o si una ruta vieja no da 404. También lista para revisión los cambios fuera de los pares. La comparación ignora el orden de las claves y la corrección sale en orden de `TreeMap`.
6. **Pasa a las rutas nuevas las pruebas existentes** que usen las viejas, sin regrabar sus referencias.
   - Si solo cambia la `url` de un error, añade una corrección `-est7` encadenada.
   - Si la clave de la referencia lleva la ruta, conserva la clave y pide la ruta nueva. `DescargasCaracterizacionTest` lo hace así, porque una corrección no puede renombrar claves.
   - Si el paso cambia una regla de seguridad, añade un caso en `AutorizacionPorRolTest` y comprueba que falla al quitar la regla.
7. **En el frontend**, cambia el servicio y escribe o ajusta su spec, que debe fijar la URL exacta y el método de cada llamada.
8. **Añade el punto a "Migración hecha"** en la entrada EST-7 y verifica:
   - `clean verify` en JDK 25 y 17, en dos copias a la vez.
   - `npx ng test --watch=false` en el frontend.
   - Después completa la fila 26 del registro.
9. **Haz un commit por paso en cada repositorio**, con el mensaje en ASCII y el ID (`EST-7, paso N`), y sube los dos repositorios cuando el usuario lo pida.

## Reglas que siguen vigentes

- Antes de cambiar algo, pasa una vez la caracterización que lo cubre. Si nada lo cubre, crea una y grábala contra el código anterior.
- Nunca se regraba una referencia. Un cambio de respuesta se fija con una corrección `correcciones/<ref>-<punto>.json`, con claves de primer nivel o JSON Pointer; el padre debe ser un objeto. Las correcciones se encadenan en orden.
- `ListadosPorRolCaracterizacionTest` regraba todas sus referencias si se pasa `-DgrabarReferencia=true`. Para grabar un método nuevo, ejecuta solo ese método (`-Dtest='Clase#metodo'`).
- Mockito no puede simular clases concretas en JDK 25 (`JwtUtil`, `SecurityAuditService`, `LoginRateLimiter`, adaptadores). Usa instancias reales, interfaces o una subclase anónima de `Autorizacion`.
- Si renombras o agregas un campo en un modelo del dominio, revisa `infraestructura/configuracion/NombresJsonDominio` (OPC-2).
- Si el usuario tiene la aplicación abierta (`spring-boot:run` o VS Code), no toques `gestion_curricular/target`. Compila siempre en una copia del scratchpad: `tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <scratchpad>/<dir>` y luego `./mvnw -o -q clean verify`. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- No hay Python: usa Edit, sed, perl o Node. La copia de trabajo mezcla LF y CRLF. Revisa siempre el `git diff` después de perl; una sustitución puede tocar más líneas de las previstas, como pasó en el paso 5 con `new HttpParams()`.
- Pregunta antes de lo que cambie un contrato más allá de la ruta.
- Las respuestas al usuario van en español.

## Para probar a mano

- Backend: `SPRING_DATASOURCE_PASSWORD=root JAVA_HOME="C:/Program Files/Java/jdk-17" ./mvnw -o spring-boot:run` en `gestion_curricular` (puerto 5000; MySQL `bdcurricular` con usuario y contraseña `root`).
- Frontend: `npx ng serve` (http://localhost:4200).
- Usuarios de prueba, todos con la contraseña `password123` y el dominio `@unicauca.edu.co`:
  - `admin@`
  - `josefina@` (coordinadora)
  - `juanp@` (funcionario)
  - `maria@` (secretaria)
  - `decano@`
  - `dcisneros@` y `pepa@` (estudiantes)
