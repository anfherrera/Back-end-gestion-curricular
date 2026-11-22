Empieza la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular` (rama `main`). Puedes cambiar ese código cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, el registro de revisiones y las entradas que se citan abajo.

## De dónde partimos

La revisión 25 cerró, con un commit por punto: DUP-8, DUP-9, BUG-8, BUG-9, BUG-10, BUG-11, BUG-12, BUG-14, BUG-15, BUG-19, BUG-20, BUG-4, EST-7 y OPC-2 (seis commits, de `5f9f53d` a `5fcf0c5`). El último `clean verify`, en JDK 25 y 17, dio 596 pruebas, 1 omitida y 0 fallos.

Lo que conviene saber de esos cambios:
- **OPC-2:** los modelos del dominio usan camelCase; los DTO y las entidades JPA siguen en snake_case. Algunas respuestas serializan el dominio tal cual, y para ellas `infraestructura/configuracion/NombresJsonDominio` (un módulo de Jackson) conserva el nombre JSON histórico y el orden de las propiedades. Si renombras o agregas un campo en un modelo del dominio, revisa esa tabla. Las pruebas sin Spring Boot usan `soporte/SoporteMvc.standaloneSetup`. Un `ObjectMapper` propio de una prueba que vuelque objetos del dominio debe registrar `NombresJsonDominio.modulo()`. `GrafoObjetos` usa la misma tabla.
- **EST-7:** la convención de rutas REST y el plan de migración, por controlador y sin hacer, están en la entrada EST-7. Toda ruta nueva sigue esa convención.
- **BUG-4:** las rutas nuevas de Paz y Salvo son `GET /api/solicitudes-pazysalvo/{idSolicitud}` y `GET /api/solicitudes-pazysalvo/usuarios/{idUsuario}/solicitudes`.

## Primero, pregunta al usuario (sin política decidida)

1. **BUG-46 (seguridad).** Las respuestas que serializan el `Usuario` del dominio, como `crearInscripcion` de verano, llevan `password` (el hash), `cedula` y `objRol`. Pasa porque `UsuarioMapperPersistencia` copia el hash al dominio. Las opciones son dejar de cargar el hash salvo para el login, o responder con un DTO. Las dos cambian el JSON: busca qué respuestas lo incluyen y si el frontend usa algo de ese objeto.
2. **BUG-45.** El historial de Paz y Salvo del frontend (`getSolicitudesProcesadas*` en `paz-salvo.service.ts`) llama a `/listarSolicitud-PazYSalvo/{Funcionario|Coordinador|Secretaria}/Aprobadas`, que no existe en el backend. Por eso la pestaña de procesadas de secretaría sale siempre vacía. Las opciones son crear las tres rutas, siguiendo la convención de EST-7 o como en Homologación y Reingreso, o quitar la pestaña.
3. **Frontend sin commit.** Su copia de trabajo tiene tres cambios de la revisión 25:
   - DUP-8: borra cuatro constantes `ESTADISTICAS_*` de `api-endpoints.ts`.
   - BUG-4: un comentario en `paz-salvo.service.ts`.
   - EST-7: `documentos/añadirComentario` → `documentos/agregarComentario` en `solicitudes-base.service.ts` y tres `spec.ts`. Sin este cambio, el frontend no puede comentar documentos con el backend actual.

   Propón hacer el commit en una rama nueva, no en `main`, y espera su respuesta.

## Después, si el usuario quiere seguir

- Migración de rutas de EST-7, por pasos y en el orden de su tabla. Cada controlador va en un commit, junto con su servicio del frontend. Las rutas viejas responden 404 y lo fija una corrección.
- La errata `/listarSolicitud-Reingreo/{id}` y el `/{id}` de Homologación, que tiene la misma ambigüedad que BUG-4. Están en el plan de EST-7, paso 7.
- Los puntos `[ ]` que quedan en `REVISION_CODIGO.md`. Lístalos y propón un orden antes de empezar.

## Método (obligatorio)

- Antes de cambiar algo, pasa una vez la caracterización que lo cubre. Si nada lo cubre, crea una caracterización y grábala contra el código anterior, con `-DgrabarReferencia=true`.
- Nunca se regraba una referencia. Un cambio de respuesta se fija con una corrección `correcciones/<ref>-<punto>.json` (`soporte/Correcciones`: claves de primer nivel o JSON Pointer; el padre debe ser un objeto). Las correcciones se encadenan en orden.
- Para derivar las correcciones, graba la salida nueva en una copia del scratchpad y compárala con Node contra la referencia del repositorio. Conserva el texto exacto de los números (Jackson distingue `0.0` de `0`; usa `JSON.parse` con `context.source`). Escribe las claves en el orden de `TreeMap`, porque las caracterizaciones de Estadísticas y Paz y Salvo comparan el texto.
- Cada commit lleva:
  - un mensaje en ASCII con el ID del punto;
  - la entrada del punto marcada `[x]` con su "*Cierre revisión 26:*";
  - `clean verify` en JDK 25 (el predeterminado) y en JDK 17 (`C:\Program Files\Java\jdk-17`).
- Si el usuario tiene la aplicación abierta (`spring-boot:run` o desde VS Code), no toques `gestion_curricular/target`. Compila en una copia: `tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <scratchpad>/<dir>` y luego `./mvnw -o -q clean verify`. Las copias de JDK 25 y 17 pueden correr a la vez.
- No hay Python: usa Edit, sed o Node. La copia de trabajo mezcla LF y CRLF (git normaliza con `text=auto`). Al editar con Node, no supongas un fin de línea.
- Al empezar, agrega la fila 26 al registro de revisiones. Al terminar, complétala.

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
