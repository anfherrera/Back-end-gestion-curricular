Sigue la revisión 26 de `REVISION_CODIGO.md` en el backend (`D:\Cursos\Trabajo de grado\Back-end-gestion-curricular`, rama `seguridad-y-arquitectura`). El frontend está en `D:\Cursos\Trabajo de grado\Front-end-gestion-curricular`, rama `revision-25-backend` (no `main`), y puedes cambiarlo cuando haga falta. Lee primero la cabecera de `REVISION_CODIGO.md`, la fila 26 del registro y la entrada EST-7, sobre todo la convención, el plan y "Migración hecha". Respóndeme siempre en español.

## De dónde partimos

- Cerrados en la revisión 26: BUG-46, BUG-45, BUG-49 y BUG-48.
  - BUG-49: "Detalle de solicitud" usa `GET /api/solicitudes/{id}` y se enlaza desde el historial para funcionario, coordinador y decano.
  - BUG-48: se quitó la página "Configurar período".
- EST-7: pasos 1 a 6 hechos, con 71 de unas 206 rutas. El paso 6 fue ECAES.
- Último `clean verify`, en JDK 25 y 17: 609 pruebas, 1 omitida y 0 fallos (120 XML de surefire). Frontend: 766 pruebas sin fallos.
- Los dos repositorios están subidos hasta el paso 6.
- Hay cosas sin commit en el backend:
  - `gestion_curricular/src/test/java/.../infraestructura/RutasPazYSalvoCaracterizacionTest.java`, nueva. Es la caracterización previa del paso 7a, todavía con la comparación sin corrección (`compararConReferencia(casos, "rutas/pazysalvo.json")`).
  - `PROMPT_SIGUIENTE_SESION.md` (este archivo).
  - La referencia `rutas/pazysalvo.json` **no está en el repositorio**: solo se grabó una vez en una copia del scratchpad, que puede haberse borrado. Como todavía no es una referencia del repositorio, grabarla es grabarla por primera vez, no regrabarla. Hay que hacerlo contra el código actual, que aún no está migrado.

Quedan abiertos, y no los toques sin preguntar: BUG-47 (mensaje de validación no determinista) y OPC-1 (opcional).

## Posible BUG nuevo (investigar y anotar, sin corregir)

En la referencia de Paz y Salvo, `POST /crearSolicitud-PazYSalvo` como estudiante 5 responde 201 con cualquiera de estos dos cuerpos:
- `{"idUsuario":"6","tituloTrabajoGrado":"Tesis"}`: el caso de uso recibe `usuario 5, periodo null, titulo null`. El usuario forzado a 5 es lo esperado, pero el título se pierde.
- `{"idUsuario":"5","periodoAcademico":"2024-9"}`: un período inválido no da 400.

Parece que `peticion.asignarUsuario(...)` (`CrearSolicitudPazYSalvoDTOPeticion`) descarta los demás campos cuando quien crea no es staff. Compruébalo y, si se confirma, anótalo como BUG-50.

## Paso 7: decisiones ya tomadas por el usuario

Se reparte en dos commits por repositorio: **7a, Paz y Salvo** (21 rutas; su servicio del frontend es independiente) y **7b, Reingreso y Homologación** (29 rutas; comparten `SolicitudesAprobacionService`).

**Forma común de los tres controladores**, la de ECAES y BUG-45, conservando la conducta de cada ruta (períodos por defecto, programa del coordinador y regla para quien no es staff):
```
POST /                          crear
GET  /?rol=…&idUsuario=&periodoAcademico=      porRol (y porUser de Reingreso)
GET  /?rol=funcionario|coordinador|secretaria&bandeja=pendientes   /Funcionario, /Coordinador, /Secretaria
GET  /?rol=…&bandeja=procesadas                /{Rol}/Aprobadas (Paz y Salvo ya lo tiene)
PUT  /{id}/estado               id de la ruta; idSolicitud del cuerpo se ignora
GET  /{id}/oficios              obtenerOficios
GET  /{id}/oficios/archivo      descargarOficio
Homologación: POST /{id}/resoluciones   (antes /{idSolicitud}/subir-resolucion)
```

**Rutas propias de Paz y Salvo:**
```
POST /documentos                subir-documento (sin asociar)
GET  /documentos?nombre=        descargar-documento?filename=
GET  /{id}/documentos           obtenerDocumentos/coordinador/{id}
GET  /{id}/comentarios          obtenerComentarios/{id}
POST /{id}/oficios              subir-oficio-pdf/{id}
POST /{id}/documento-generado   generar-documento/{id} (los mismos parámetros)
```
`GET /{idSolicitud}` y `GET /usuarios/{idUsuario}/solicitudes` ya cumplen la convención y no cambian.

**Se retiran 11 rutas sin uso**, que pasan a 404 y cuyo código de controlador se borra:
- Reingreso: el listado completo `/listarSolicitudes-Reingreso`, `GET /listarSolicitud-Reingreo/{id}` y `/validarDocumentosRequeridos/{id}`.
- Homologación: el listado completo `/listarSolicitud-Homologacion`, `GET /listarSolicitud-Homologacion/{id}` y `/validarDocumentosRequeridos/{id}`.
- Paz y Salvo: el listado completo `/listarSolicitud-PazYSalvo`, `/validarDocumentosRequeridos/{id}`, `/generarDocumentoPazYSalvo/{id}/pdf` y `/docx`, y `/guardarOficio`.
- En el frontend se borran también `listarSolicitudes()` de Reingreso y Homologación, y `generarOficio()` de `SolicitudesAprobacionService`, que nadie llama y apunta a una ruta inexistente.
- Los métodos del caso de uso que queden sin uso (por ejemplo, `revisarDocumentosRequeridos`) se anotan como código muerto, sin borrarlos en este paso.

## Cómo seguir con 7a (Paz y Salvo)

1. Graba `rutas/pazysalvo.json` en una copia del scratchpad, con el código actual y `-DgrabarReferencia=true`. Revisa el resumen y pásala tres veces más sin grabar.
2. Antes del cambio, pasa una vez contra el código actual las pruebas que usan rutas de Paz y Salvo:
   - `PazYSalvoAceptacionTest`, `PazYSalvoIntegracionTest`, `GestionPazYSalvoFuncionalTest`, `PazYSalvoCaracterizacionTest` y `SoportePazYSalvo`.
   - `CrearPazYSalvoCuerpoCaracterizacionTest`, `ErroresPazYSalvoCaracterizacionTest` y `CambioEstadoPazYSalvoCaracterizacionTest`.
   - `ListadosPorRolCaracterizacionTest`, `GeneracionDocumentosCaracterizacionTest` y `RegistroDocumentosCaracterizacionTest`.
   - `CambioEstadoRechazos…` y `CambioEstadoSinEstado…` no tocan Paz y Salvo.
3. Migra el controlador.
   - Separa los `GET` de la raíz con condiciones `params`: `{"rol","!bandeja"}` para porRol, `{"rol","bandeja"}` para pendientes o procesadas, y uno sin condiciones para lo demás.
   - Ese último debe lanzar `new MissingServletRequestParameterException("rol", "String")`, para que `GET /` y `?bandeja=…` respondan el mismo 400 GC-0400 que hoy.
   - Hoy, `?rol=funcionario` sin `bandeja` da 400. Con la migración pasa a ser porRol, y es un cambio esperado.
   - Las pendientes del funcionario no filtran por período (`null`). Las del coordinador y la secretaría usan `oActual`, y el coordinador sin programa ve todas.
   - Escribe con Edit cada `/{id:\\d+}` y compruébalo con `grep`.
4. Deriva `rutas/correcciones/pazysalvo-est7.json` con `node herramientas/derivar-est7.js` y un `herramientas/pares-pazysalvo.json`.
   - Las rutas retiradas no entran en los pares.
   - El script exige que cada ruta vieja de un par dé 404 GC-0404. Las retiradas también deben dar 404, pero eso se revisa en la lista "FUERA DE LOS PARES".
   - Después cambia la prueba a `compararConReferencia(casos, "rutas/pazysalvo.json", "rutas/correcciones/pazysalvo-est7.json")`.
5. Pasa a las rutas nuevas las pruebas existentes, sin regrabar.
   - Si una prueba usa una ruta retirada, borra ese caso o fija el 404 con una corrección `-est7` encadenada, según sea prueba funcional o caracterización con referencia.
   - Si la clave lleva la ruta, conserva la clave y pide la nueva, como en el paso 6 con `ListadosPorRol` y `CambioEstado…`.
   - Si solo cambia la `url` de los errores, usa una corrección con JSON Pointer, como `tipos-documento-est7-paso6.json`.
6. En el frontend:
   - `paz-salvo.service.ts`: sobrescribe `cambiarEstado` como en `pruebas-ecaes.service.ts`.
   - `document-generator.service.ts`, que llama a `generar-documento`.
   - Los specs, con la URL y el método exactos.
   - Los `cy.intercept` de Paz y Salvo.
   - Comprueba también los interceptores con `includes(...)`.
7. Actualiza "Migración hecha" en EST-7 y la fila 26. Después:
   - `clean verify` en JDK 25 y 17, en dos copias a la vez.
   - `npx ng test --watch=false`, más prettier, lint y `tsc` de los archivos tocados.
   - Un commit por repositorio, en ASCII y con el ID (`EST-7, paso 7a`).
   - Al terminar, di cuántas rutas quedan.

Después sigue el 7b con el mismo método. Su caracterización es `RutasReingresoHomologacionCaracterizacionTest`, también un `@SpringBootTest` con varios roles, porque tiene seguridad por dueño.

## Reglas que siguen vigentes

- Nunca regrabes una referencia que ya está en el repositorio: usa correcciones `-est7` encadenadas. `ListadosPorRolCaracterizacionTest` regraba todas sus referencias con `-DgrabarReferencia=true`.
- Compila siempre en una copia del scratchpad (`tar -c --exclude=gestion_curricular/target gestion_curricular | tar -x -C <dir>` y `./mvnw -o -q …`), nunca en `gestion_curricular/target`. JDK 17 está en `C:\Program Files\Java\jdk-17`.
- No hay Python: usa Edit, perl o Node. Revisa el `git diff` después de cada sustitución; la copia de trabajo mezcla LF y CRLF.
- Pregunta antes de cambiar un contrato más allá de lo decidido arriba.
- No subas nada hasta que el usuario lo pida.
- Una caracterización con seguridad real es un `@SpringBootTest` que simula `GestionarUsuarioGatewayIntPort`, `UserDetailsService` y `GestionarSolicitudGatewayIntPort` (`idDuenoDeSolicitud`). Usa como modelo `RutasEcaesCaracterizacionTest` o `RutasPazYSalvoCaracterizacionTest`.
- Mockito no puede simular clases concretas en JDK 25: usa interfaces o instancias reales.
- EST-7 es una mejora de estilo, así que se puede dejar a medias: cada paso es completo por sí solo. Después del paso 7 quedan el 8 (cursos de verano; antes hay que pedir al usuario el prefijo único) y el 9 (estadísticas).
