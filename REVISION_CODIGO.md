# Revisión de código: arquitectura hexagonal, duplicación y código muerto

Checklist para dejar el backend con un aspecto profesional. Cada punto tiene un ID (`ARQ-1`, `DUP-3`, …) para citarlo en los commits, por ejemplo: `Paz y salvo: mover reglas de documentos al dominio (ARQ-4)`.

**Cómo usarlo**

- Marca `[x]` cuando el cambio esté hecho **y** la suite (`mvnw verify`) pase en JDK 17 y 25.
- Al terminar una sesión de trabajo, añade una fila al registro de revisiones.
- Los puntos que cambian rutas o el JSON de respuesta (marcados con ⚠️ **frontend**) se coordinan antes con el frontend.
- Prioridad: **P1** = afecta la arquitectura o es un error; **P2** = duplicación o código muerto con impacto; **P3** = estilo y limpieza.

Rutas abreviadas: `…/` = `gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/`.

## Registro de revisiones

| # | Fecha | Revisor | Puntos cerrados | Notas |
|---|-------|---------|-----------------|-------|
| 0 | 2026-11-13 | Revisión inicial | — | Línea base: 283 clases, 29.4k líneas en `main`. |
| 1 | 2026-11-13 | Claude | ARQ-1, MUE-1, MUE-2, MUE-3, MUE-4, MUE-8, BUG-2, BUG-3, BUG-5, BUG-6 | Prueba de arquitectura con línea base. |
| 2 | 2026-11-13 | Claude | DUP-4, DUP-3 | Enums de estado sin cambiar valores guardados ni JSON. Nuevos BUG-7 y BUG-8, documentados y sin corregir. 458 pruebas en JDK 17 y 25. |
| 3 | 2026-11-13 | Claude | BUG-7, ARQ-2, DUP-2 | `ListarSolicitudesPorRolTest`, `ReglasCupoTest`. Regla de cupo única en `ReglasCupo`; queda pendiente decidir si se quita la verificación del controlador (cambia el JSON del 409). 477 pruebas en JDK 17 y 25. |
| 4 | 2026-11-13 | Claude | ARQ-7, DUP-1 | `ListadosPorRolCaracterizacionTest` (antes de tocar nada) y `ListadorSolicitudesTest`. Un `listar(FiltroSolicitudes)` por tipo; el flujo decide el estado. −1136 líneas netas en `main`. Única diferencia: coordinador en ECAES (BUG-10, acordada). Nuevos BUG-9 y BUG-10, sin corregir. 453 pruebas (1 omitida) en JDK 17 y 25. |
|   |       |         |                 |       |

## Resumen de la línea base

**Lo que ya está bien:**
- `dominio/` y `aplicacion/` no importan Spring, JPA ni infraestructura, y [ArquitecturaHexagonalTest](gestion_curricular/src/test/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/ArquitecturaHexagonalTest.java) lo comprueba.
- Los casos de uso se crean en [BeanConfiguration](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/configuracion/BeanConfiguration.java), sin anotaciones de Spring en el dominio.
- Existe un manejador global de errores ([RestApiExceptionHandler](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladorExcepciones/RestApiExceptionHandler.java)).

**Problemas principales:**
1. **Hay lógica de negocio en los controladores.** Hay 9 controladores con más de 470 líneas; el de Paz y Salvo tiene 1064 líneas y 118 `if`/`for`.
2. **Hay adaptadores de entrada que llaman a adaptadores de salida** (generadores de PDF/Excel, un gateway, repositorios JPA) sin pasar por un puerto.
3. **Muchos métodos se repiten por rol y por tipo de solicitud.** Por ejemplo, `listarSolicitudesToCoordinadorPorProgramaYPeriodo` existe en cuatro variantes en cada capa.
4. **Los estados se escriben como texto libre** (`"APROBADA_FUNCIONARIO"` aparece 35 veces) aunque existe el enum `EstadosSolicitud`, que nadie usa.

---

## 1. Arquitectura hexagonal

### P1

- [x] **ARQ-1. Ampliar la prueba de arquitectura antes de refactorizar.** *Hecho con una línea base de archivos:* la prueba pasa hoy, falla si aparece una violación nueva y también si un archivo de la línea base ya cumple (hay que quitarlo de la lista al cerrar ARQ-2 a ARQ-5). Añadir a [ArquitecturaHexagonalTest](gestion_curricular/src/test/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/ArquitecturaHexagonalTest.java) estas reglas:
  - `infraestructura/input` no importa `infraestructura.output`.
  - `infraestructura/output` no importa `aplicacion.input`.
  - `Security` no importa `persistencia.repositorios` ni `persistencia.entidades`.

  Al principio fallará. Sirve como lista viva de lo que falta y evita que el problema vuelva. Se puede valorar ArchUnit en lugar de regex.

- [x] **ARQ-2. Un controlador usa una constante del gateway.** [InscripcionesVeranoRestController.java:670](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/InscripcionesVeranoRestController.java#L670) usa `GestionarSolicitudCursoVeranoGatewayImplAdapter.ESTADOS_INSCRIPCION_QUE_OCUPAN_CUPO`. Esa lista es una regla de negocio. Hay que moverla al dominio (por ejemplo `SolicitudCursoVeranoIncripcion.ocupaCupo()` o una clase `ReglasCupo`) y que el gateway y el controlador la lean de ahí. Ver también DUP-2.
  *Hecho:*
  - Nueva [ReglasCupo](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/ReglasCupo.java) con `ESTADOS_QUE_OCUPAN_CUPO` (misma lista y orden), `estadoOcupaCupo(String)` y `ocupaCupo(Solicitud)`. El gateway la usa para contar y en `yaOcupaCupo`; el controlador usa `ReglasCupo.ocupaCupo` y se borró su helper privado.
  - `InscripcionesVeranoRestController` salió de la línea base de `ArquitecturaHexagonalTest`.
  - Única diferencia: antes, si el último registro tenía `estado_actual` null, `List.of(...).contains(null)` lanzaba `NullPointerException` en el controlador y en el gateway; ahora se considera que no ocupa cupo y la petición sigue a la validación del caso de uso, que la rechaza como regla de negocio. Igual que en DUP-3, no ocurre en la práctica.
  - Prueba: `ReglasCupoTest`.

- [ ] **ARQ-3. Los generadores de documentos no tienen puerto.** Los controladores inyectan clases concretas de `infraestructura/output/formateador`:
  - [CursosVeranoRestController.java:40](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/CursosVeranoRestController.java#L40): `GeneradorPdfCursosVerano`
  - [EstadisticasRestController.java:7](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/EstadisticasRestController.java#L7): `GeneradorReportesEstadisticas`
  - [ReportesCursosVeranoRestController.java:27](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/ReportesCursosVeranoRestController.java#L27): `GeneradorExcelSolicitudesVerano`
  - [SolicitudPazYSalvoRestController.java:70](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L70) y [DocumentGeneratorController.java:4](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/DocumentGeneratorController.java#L4): `DocumentGeneratorService`

  Hay que crear puertos de salida en `aplicacion/output` (por ejemplo `GenerarReportesIntPort` y `GenerarDocumentosIntPort`), hacer que los generadores los implementen y que el caso de uso los invoque. El controlador solo pide "dame el PDF" al puerto de entrada.

- [ ] **ARQ-4. `DocumentGeneratorService` depende de casos de uso.** [DocumentGeneratorService.java:27-28](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/formateador/DocumentGeneratorService.java#L27-L28) inyecta `GestionarSolicitudHomologacionCUIntPort` y `GestionarSolicitudReingresoCUIntPort`, y un adaptador de salida no debe llamar al núcleo. El caso de uso debe reunir los datos y pasárselos al generador ya armados, como se hace con `DatosDocumento` en Paz y Salvo.

- [ ] **ARQ-5. `Autorizacion` consulta JPA directamente.** [Autorizacion.java:43-44](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/Security/Autorizacion.java#L43-L44) usa `SolicitudRepositoryInt`, `DocumentoRepositoryInt` y entidades JPA. Debe usar los puertos `GestionarSolicitudGatewayIntPort` y `GestionarDocumentosGatewayIntPort`, añadiendo los métodos que falten (por ejemplo `idDuenoDeSolicitud(id)`).

- [ ] **ARQ-6. Los controladores contienen reglas de negocio.** Hay que moverlas a casos de uso o servicios de dominio. Los casos más claros:
  - Paz y Salvo [SolicitudPazYSalvoRestController.java](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java):
    - clasificación de documentos por nombre (`esOficio`, `tipoDeDocumento`, L772-797)
    - documentos requeridos y `todosCompletos` (L908-961)
    - búsqueda del último comentario de rechazo (L817-833)
    - período académico por defecto (L165-171 y L221-227)
    - carga del usuario completo antes de guardar (L154-163)
  - Estadísticas [EstadisticasRestController.java](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/EstadisticasRestController.java): `calcularTasaProcesamiento`, `calcularEficienciaAprobacion`, `aplicarKpisDelProceso`, `normalizarProceso` y `normalizarPeriodoAcademico` (L383-610).
  - Cursos de verano: la validación de cupo en el controlador (ver DUP-2) y la elección de salón dentro de [CursosVeranoRestController.java](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/CursosVeranoRestController.java) (L129-140, L466, L581).

  Meta orientativa: ningún controlador por encima de ~250 líneas, y cada método solo valida la entrada, llama al caso de uso y mapea el resultado.

- [x] **ARQ-7. Qué estado ve cada rol está decidido en los gateways.** Por ejemplo, [GestionarSolicitudHomologacionGatewayImplAdapter](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/persistencia/gateway/GestionarSolicitudHomologacionGatewayImplAdapter.java) usa `findByUltimoEstado...("APROBADA_FUNCIONARIO", …)` para el coordinador. Hay que llevar esa decisión a [FlujoEstadosSolicitud](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/FlujoEstadosSolicitud.java) (por ejemplo `estadoPendientePara(rol)`). El gateway solo debe recibir el estado ya decidido. Se resuelve junto con DUP-1.
  *Hecho:*
  - `FlujoEstadosSolicitud.estadoPendientePara(rol)` y `estadoAprobadoPor(rol)` salen del mapa de pasos del flujo; devuelven vacío si el rol no decide ningún paso (el coordinador en ECAES).
  - [ListadorSolicitudes](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/servicios/ListadorSolicitudes.java) (dominio) traduce un `FiltroSolicitudes` al estado del flujo y le pasa al gateway solo `EstadosSolicitud.textoGuardado()` (así `ENVIADA` llega como `"Enviada"`, igual que antes).
  - Los gateways ya no nombran ningún estado al listar. El único uso de `EstadosSolicitud` que les queda es el estado inicial al guardar paz y salvo.
  - Prueba: `ListadorSolicitudesTest`.

### P2

- [ ] **ARQ-8. El dominio llega a la API sin DTO.** [EstadisticasRestController.java:39](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/EstadisticasRestController.java#L39) y la L48 reciben `@RequestBody Estadistica`, que es el modelo de dominio. Ya existe `EstadisticaDTOPeticion` y nadie lo usa; hay que usarlo.

- [ ] **ARQ-9. Hay peticiones sin tipo (`@RequestBody Object` / `Map`).** Aparecen en [SolicitudPazYSalvoRestController.java:78](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L78), [CursoOfertadoRestController.java:157](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/CursoOfertadoRestController.java#L157) y L181, [InscripcionesVeranoRestController.java:288](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/InscripcionesVeranoRestController.java#L288) y L364, y [PreinscripcionesVeranoRestController.java:385](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/PreinscripcionesVeranoRestController.java#L385) y L453. Hay que reemplazarlas por DTOs con `@Valid`.

- [ ] **ARQ-10. Hay respuestas sin tipo (`Map<String, Object>`).** Son 91 en total. Las que más tienen son Estadísticas (22), Inscripciones (10), Preinscripciones (7) y PeriodoAcademico (7). Hay que pasarlas a `record` o DTOs de respuesta, siguiendo el mismo camino que ya se hizo en Cursos de verano (commits `53af9a6` y `3128386`). ⚠️ **frontend**: el JSON debe quedar idéntico; las pruebas de caracterización de `test/resources/referencias` lo comprueban.

- [ ] **ARQ-11. Los controladores capturan errores por su cuenta.** Repiten `try/catch` para excepciones que ya resuelve `RestApiExceptionHandler`: Paz y Salvo (5), Preinscripciones (3), Usuario, Inscripciones y CursoOfertado. También hay 86 `catch (Exception e)` en `main`. Hay que quitar los locales y dejar que el manejador global responda. Revisar sobre todo los que se tragan el error:
  - [SolicitudPazYSalvoRestController.java:724](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L724) (`catch (Exception e) { continue; }`)
  - [SolicitudPazYSalvoRestController.java:137](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L137) (devuelve 400 con el mensaje interno de cualquier excepción)

- [ ] **ARQ-12. `FormateadorResultadosIntPort` solo lanza excepciones.** [FormateadorResultadosIntPort](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/aplicacion/output/FormateadorResultadosIntPort.java) es un puerto de salida cuya única función es lanzar `EntidadYaExisteException`, `ReglaNegocioExcepcion` o `EntidadNoExisteException`, que ya son excepciones del dominio. Los 16 casos de uso podrían lanzarlas directamente. *Decisión pendiente*: si la plantilla del curso exige este patrón, se mantiene y se documenta; si no, se elimina.

---

## 2. Código duplicado

### P1

- [x] **DUP-1. Listados por rol, programa y período.** Cada tipo de solicitud repite la misma combinación en el puerto de entrada, el caso de uso, el puerto de salida, el gateway y el controlador:
  - Homologación: 19 métodos de listado en el gateway y 9 endpoints `/listar…`.
  - Reingreso: 19 y 9.
  - Paz y Salvo: 11 y 6.
  - ECAES: 13 y 4.

  Los métodos del caso de uso solo delegan en el gateway (por ejemplo `listarSolicitudesToCoordinadorPorProgramaYPeriodo`). La propuesta es un único `listar(FiltroSolicitudes filtro)`, donde `FiltroSolicitudes` es un `record` del dominio con rol, idPrograma, periodo e idUsuario. El estado que corresponde a cada rol lo decide `FlujoEstadosSolicitud` (ARQ-7). ⚠️ **frontend**: se pueden mantener las rutas actuales llamando todas al nuevo método, y retirarlas después.
  *Hecho, un commit por tipo. Rutas y JSON sin cambios, salvo lo indicado para ECAES:*
  - [FiltroSolicitudes](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/FiltroSolicitudes.java) tiene un quinto campo, `procesadas`: los listados `…/Aprobadas` buscan el estado en todo el historial y no solo el último, así que no bastaba con el rol. Fábricas: `pendientesDe`, `procesadasPor`, `deEstudiante` y `porRol` (traduce `ESTUDIANTE`, `FUNCIONARIO`, `COORDINADOR` y `SECRETARIA`).
  - Cada caso de uso tiene un solo `listar(filtro)`; los `listarSolicitudesPorRol` pasan también por él. Cada gateway implementa [ConsultarSolicitudesGatewayIntPort](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/aplicacion/output/ConsultarSolicitudesGatewayIntPort.java): `listarPorUsuario`, `listarPorUltimoEstado` y, solo en homologación y reingreso, `listarPorEstadoEnHistorial`.
  - En cada capa (puerto de entrada, caso de uso, puerto de salida y gateway), los métodos de listado por rol pasan de 15 a 1 en homologación y reingreso, de 7 a 1 en paz y salvo y de 4 a 1 en ECAES. También se borraron las consultas `findByUltimoEstado` y `findByUltimoEstadoAndPrograma`, que equivalían a las variantes con período null. Balance en `main`: −1595 / +459 líneas.
  - El período por defecto y el alias "todos" de los cuatro controladores quedan en `PeriodoDeConsulta` (paquete de controladores). Es un avance de DUP-6, que sigue abierto para el resto de controladores.
  - Se conservan a propósito: el coordinador sin programa ve todo, de todos los períodos; el funcionario de paz y salvo no filtra por período; `listarSolicitudesPorRol` no filtra por período ni por programa; homologación y reingreso devuelven `[]` con `SECRETARIA` (BUG-9).
  - Diferencias:
    - **ECAES, acordada:** `porRol?rol=COORDINADOR` devuelve `[]` sin consultar, porque el flujo ECAES no tiene paso del coordinador. Antes buscaba último estado `APROBADA_FUNCIONARIO`, que el flujo no permite, así que solo cambia con datos fuera del flujo (BUG-10).
    - `listarSolicitudesPorRol` filtraba en memoria el último registro de la lista y ahora consulta la base, que toma el estado más reciente por fecha. Solo difiere si un registro posterior tiene una fecha anterior, o si dos tienen la misma fecha. Con `ESTUDIANTE`, una solicitud sin usuario lanzaba `NullPointerException`; ahora no sale.
  - Pruebas: `ListadosPorRolCaracterizacionTest` fija qué devuelve cada endpoint `/listar…` de los cuatro tipos (referencias en `test/resources/referencias/listados/`). `ListarSolicitudesPorRolTest` conserva sus datos y aserciones; su gateway simulado ahora responde a la consulta por último estado.

- [x] **DUP-2. La regla de cupo está en cuatro lugares:**
  - [InscripcionesVeranoRestController.java:648-660](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/InscripcionesVeranoRestController.java#L648-L660) (`validarCupoDisponible`)
  - [GestionarSolicitudCursoVeranoCUAdapter.java:199](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/casosDeUso/GestionarSolicitudCursoVeranoCUAdapter.java#L199) y [L390](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/casosDeUso/GestionarSolicitudCursoVeranoCUAdapter.java#L390)
  - [GestionarSolicitudCursoVeranoGatewayImplAdapter.java:525](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/persistencia/gateway/GestionarSolicitudCursoVeranoGatewayImplAdapter.java#L525) (con bloqueo, necesario por concurrencia)

  Debe quedar una sola regla en el dominio, y el gateway se encarga solo del bloqueo y el conteo. Las pruebas `CupoConcurrenteTest` y `CupoCursoVeranoTest` deben seguir pasando.
  *Hecho, sin cambios de comportamiento:*
  - La regla es `ReglasCupo.exigirCupoLibre(cupo, contarOcupados)`: sin cupo definido (null o ≤ 0) no limita ni cuenta; un conteo null vale 0; si los ocupados llegan al cupo lanza `CupoAgotadoException`.
  - El caso de uso la aplica en `verificarCupoDisponible(curso)` y `verificarCupoParaAceptar(inscripcion)` (nuevos en `GestionarSolicitudCursoVeranoCUIntPort`), que usan también `crearSolicitudCursoVeranoInscripcion` y `validarPago`.
  - El gateway solo bloquea el curso y pasa el conteo a `exigirCupoLibre`.
  - El controlador llama al caso de uso y convierte la excepción con `SoporteCursosVerano.respuestaCupoAgotado`, así que el 409 conserva su JSON (`codigo`, `cupo`, `ocupados`). Se borró `validarCupoDisponible`.
  - Sigue habiendo varios **puntos de control** a propósito: el controlador valida antes del caso de uso porque su 409 tiene un JSON distinto del de `RestApiExceptionHandler` (que no trae `cupo` ni `ocupados`), y el gateway vuelve a contar con bloqueo por concurrencia. Quitar la verificación del controlador cambiaría el JSON del 409 al crear y el código de respuesta al aceptar una inscripción que no está en `Enviada` con el curso lleno (hoy 409, pasaría a 400). *Decisión pendiente*, junto con ARQ-11. ⚠️ **frontend**
  - `GestionarSolicitudCursoVeranoCUIntPort.contarInscripcionesAceptadasPorCurso` ya no lo usa ningún controlador.
  - Pruebas: `ReglasCupoTest` y dos casos nuevos en `CupoCursoVeranoTest`.

- [x] **DUP-3. El último estado se calcula a mano 21 veces** (`getEstadosSolicitud().get(size() - 1)`), aunque ya existe [FlujoEstadosSolicitud.ultimoEstado(...)](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/FlujoEstadosSolicitud.java#L128). Hay que añadir `Solicitud.estadoActual()` y usarlo en todas partes. Dónde aparece: ReportesCursosVerano (7), GestionarSolicitudCursoVeranoCU (6), InscripcionesVerano (6), PreinscripcionesVerano (4), GestionarCursoOfertadoVeranoCU (3), CursosVerano (2) y otros 4.
  *Hecho:*
  - [Solicitud](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/Solicitud.java) tiene `ultimoRegistroEstado()`, `estadoActual()` y `estadoActual(siNoHayEstados)`. Se usan en los casos de uso, en los controladores de verano y en los cuatro `listarSolicitudesPorRol`. Se borró el helper privado `ultimoEstado(List)` de `GestionarSolicitudCursoVeranoCUAdapter`.
  - **No delega en `FlujoEstadosSolicitud.ultimoEstado`**, que salta los registros con estado vacío; el código anterior no lo hacía. Así no cambia ningún resultado. Unificar los dos queda como *decisión pendiente*.
  - Única diferencia: antes, si la lista de estados era null o el último `estado_actual` era null, cuatro validaciones de `GestionarSolicitudCursoVeranoCUAdapter` (aprobar, rechazar, completar inscripción) lanzaban `NullPointerException`; ahora responden con el error de regla de negocio. En la práctica no ocurre, porque la lista nunca es null y el constructor pone `"Enviado"` por defecto.
  - Fuera de alcance: los estados de **curso** (`getEstadosCursoOfertados`, 8 sitios) ya tienen `TransicionEstadoCurso.estadoActual`, que devuelve `SIN_ESTADO` y no null. `yaOcupaCupo` del gateway trabaja sobre la entidad JPA y se revisa con DUP-2.
  - Prueba: `SolicitudEstadoActualTest`.

- [x] **DUP-4. Los estados se escriben como texto.** `"APROBADA_FUNCIONARIO"` aparece 35 veces, `"APROBADA_COORDINADOR"` 30, `"APROBADA"` 30, `"ENVIADA"` 24, `"RECHAZADA"` 22 y `"Pago_Validado"` 11. Hay que usar el enum [EstadosSolicitud](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/Enums/EstadosSolicitud.java), que hoy no se usa en ningún sitio, y crear uno para los estados de cursos de verano (`Pago_Validado`, `Inscrito`, …).
  *Hecho. Los valores guardados y los del JSON no cambian:*
  - `EstadosSolicitud` tiene dos formas de cada estado: `name()` es la canónica (estadísticas, `FlujoEstadosSolicitud`, JSON) y `textoGuardado()` es la de la BD. Solo difieren en `ENVIADA`: al crear una solicitud se guarda `"Enviada"`, y los cambios de estado guardan la forma canónica.
  - Nuevo [EstadoSolicitudCursoVerano](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/Enums/EstadoSolicitudCursoVerano.java) con los seis estados que el código escribe: `Enviada`, `Aprobado`, `Rechazado`, `Pago_Validado`, `Pago_Rechazado` e `Inscripcion_Completada`.
  - `"Inscrito"`, `"Preinscrito"`, `"Pendiente"` y `"Enviado"` no se añadieron al enum: no se guardan en la BD, son etiquetas por defecto de las respuestas cuando la solicitud no tiene estados.
  - Siguen como texto, a propósito:
    - los alias de entrada de `ReglasEstadisticas.normalizarEstado` (`case "ENVIADO"`, `"PREINSCRITO"`…), porque son la tabla de equivalencias;
    - las variantes antiguas `"APROBADA"` y `"Aprobada"` en la regla de cupo y en `preinscripcionAprobada`;
    - `"Rechazada"` en Reportes (BUG-8).
  - No hay consultas JPA con estados escritos: todas reciben el estado como parámetro.
  - Pendiente fuera de este punto: los estados de curso (`Abierto`, `Publicado`, `Preinscripcion`, `Inscripcion`, `Borrador`, `Cerrado`) siguen como texto. Además, los constructores de `EstadoSolicitud` y `EstadoSolicitudEntity` ponen `"Enviado"` por defecto, una tercera grafía. Hoy siempre se sobrescribe, así que no se cambió.

### P2

- [ ] **DUP-5. El registro de documentos se repite.** `new Documento()` seguido de los mismos 4 o 5 `set…` aparece en [ArchivosRestController.java:170](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/ArchivosRestController.java#L170), [SolicitudHomologacionRestController.java:523](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudHomologacionRestController.java#L523) y [SolicitudPazYSalvoRestController.java](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java) L602, L990 y L1041. Hay que crear en `GestionarDocumentosCUIntPort` los métodos `subirDocumento(archivo, idUsuario)` y `adjuntarASolicitud(archivo, idSolicitud, carpeta)`, de modo que guardar el archivo y registrarlo sea un solo caso de uso.

- [ ] **DUP-6. El período académico por defecto se calcula en cada controlador.** El patrón "si no viene período, usar `PeriodoAcademicoEnum.getPeriodoActual()`" está en 11 archivos de infraestructura (5 veces solo en Paz y Salvo). Debe vivir en el caso de uso o en `PeriodoAcademicoEnum.oActual(String)`.
  *Avance con DUP-1:* los listados de homologación, reingreso, ECAES y paz y salvo usan `PeriodoDeConsulta.oActual` / `oTodos` (en el paquete de controladores). Quedan los demás usos, entre ellos los de crear solicitud en paz y salvo.

- [ ] **DUP-7. Las cabeceras de descarga se arman en cada controlador** (`Content-Disposition`, nombre en UTF-8): `descarga` y `descargaPdfConNombreUtf8` en Paz y Salvo, `archivo` en Estadísticas, y otros parecidos en Reportes y Archivos. Hay que crear una sola utilidad, por ejemplo `infraestructura/input/RespuestasArchivo`.

- [ ] **DUP-8. Hay endpoints de estadísticas que parecen repetidos.** En [EstadisticasRestController](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/EstadisticasRestController.java) hay que confirmar con el frontend cuál de cada par se usa:
  - `/estado/{estado}` ↔ `/estado-solicitudes`
  - `/periodo` ↔ `/por-periodo`
  - `/programa/{id}` ↔ `/por-programa`
  - `/proceso/{tipo}` ↔ `/estadisticas-por-proceso`
  - `/tendencias` ↔ `/tendencias-comparativas`

  ⚠️ **frontend**

- [ ] **DUP-9. Hay dos rutas para lo mismo:** `/obtenerDocumentos/{id}` y `/obtenerDocumentos/coordinador/{id}` en Paz y Salvo (L737-746). ⚠️ **frontend**

- [ ] **DUP-10. El mapeo de objetos se hace de tres formas:** con `ModelMapper` genérico ([Mapper](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/persistencia/mappers/Mapper.java)), con un segundo bean `ModelMapper` llamado `mapperCurso` ([MapperCursosOfertados](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/persistencia/mappers/MapperCursosOfertados.java)) y con mapeo manual. Hay que elegir una sola estrategia. Lo más recomendable es MapStruct o mapeo manual explícito, que da errores en compilación en lugar de en ejecución.

---

## 3. Código muerto

### P1

- [x] **MUE-1. Una rama de `crearSolicitudPazYSalvo` nunca se ejecuta.** En [SolicitudPazYSalvoRestController.java:122-128](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L122-L128), Jackson deserializa `@RequestBody Object` siempre como `Map`, así que la rama `instanceof SolicitudPazYSalvoDTOPeticion` es inalcanzable. Se resuelve junto con ARQ-9.

- [x] **MUE-2. Hay clases que nadie usa:** *Borradas las tres sin uso; las otras dos siguen abiertas en ARQ-8 y DUP-4.*
  - [EstadisticaDTOPeticion](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/DTOPeticion/EstadisticaDTOPeticion.java): hay que usarla (ARQ-8), no borrarla.
  - [SolicitudCurosoVeranoPreinscripcionDTOPeticion](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/DTOPeticion/SolicitudCurosoVeranoPreinscripcionDTOPeticion.java)
  - [EstadosSolicitud](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/modelos/Enums/EstadosSolicitud.java): hay que usarlo (DUP-4), no borrarlo.
  - [solicitudEcaesSalidaDto](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/DTORespuesta/solicitudEcaesSalidaDto.java) y [documentoDto](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/DTOPeticion/documentoDto.java): solo se referencian entre sí y desde un bloque comentado de `Mapper`.

- [x] **MUE-3. Hay bloques de código comentado:** *También se quitaron los que no estaban listados: puertos `GestionarSolicitud*`, gateways de cursos de verano y archivos, y `GestionarSolicitudEcaesCUAdapter`.* [Mapper.java](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/persistencia/mappers/Mapper.java) (36 líneas), [GestionarCursoOfertadoVeranoCUAdapter](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/casosDeUso/GestionarCursoOfertadoVeranoCUAdapter.java) (7), [JwtUtil](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/Security/JwtUtil.java) (4), [GestionarUsuarioGatewayImplAdapter](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/persistencia/gateway/GestionarUsuarioGatewayImplAdapter.java) (3), [SolicitudEcaesEntity](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/output/persistencia/entidades/SolicitudEcaesEntity.java) (3) y [LoginDTOPeticion](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/DTOPeticion/LoginDTOPeticion.java) (2). Hay que borrarlos; el historial ya está en git.

- [x] **MUE-4. Hay archivos que no deberían estar en el repositorio:** *`.vscode/` solo estaba ignorado en `gestion_curricular/.gitignore`, que no aplica a la raíz; se añadió al `.gitignore` raíz.*
  - `gestion_curricular/src/main/resources/templates/~$icio-homologacion.docx` y `~$solucion-reingreso.docx`: son archivos de bloqueo de Word. Hay que añadir `~$*` al `.gitignore`.
  - `gestion_curricular/Archivos/reingreso/solicitud_3/prueba.pdf`: es un archivo subido en pruebas. Hay que añadir `Archivos/` al `.gitignore`.
  - `.vscode/settings.json`: `.vscode/` está en el `.gitignore` pero el archivo ya estaba versionado. Hay que ejecutar `git rm --cached`.

### P2

- [ ] **MUE-5. Hay plantillas que ningún código usa:** `oficio-homologacion-nuevo.docx`, `oficio-homologacion-template.txt` y `resolucion-reingreso-template.txt` en `src/main/resources/templates/`.

- [ ] **MUE-6. Hay una migración fuera de Flyway.** [migraciones/2026-11-11_documentos_usuario_subio.sql](gestion_curricular/src/main/resources/migraciones/2026-11-11_documentos_usuario_subio.sql) agrega una columna que ya está en `V1__esquema_inicial.sql`. Producción tiene `spring.flyway.enabled=false`. *Decisión pendiente*: activar Flyway en producción con `baseline-on-migrate`, de modo que los cambios futuros sean `V3__…`, `V4__…`, y retirar la carpeta `migraciones/`.

- [ ] **MUE-7. Métodos sin uso.**
  - ~~`GestionarSolicitudHomologacionCUIntPort.obtenerSolicitudSeleccionada`~~: era una línea comentada; se borró con MUE-3.
  - `@Slf4j` sin ningún `log.`: aparece en 12 clases. Algunas: GestionarSolicitudCursoVeranoCU, GestionarSolicitudEcaesCU, GestionarSolicitudHomologacionCU, EstadisticasRestController, NotificacionRestController, Preinscripciones, Reportes, SolicitudPazYSalvoRestController, SoporteCursosVerano y SecurityAuditService.
  - Import no usado: `Collectors` en ReportesCursosVeranoRestController.

- [x] **MUE-8. Javadoc huérfano** al final de [SolicitudPazYSalvoRestController.java:1058-1063](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L1058-L1063): documenta un método que ya no existe.

- [ ] **MUE-9. Hay dos enums de tipo de documento de identidad:** `TipoDocumento` (CC, CE; lo usa `SolicitudEcaes`) y `TipoDocumentoEnum` (7 tipos; lo usa `TipoDocumentoController`), además de `TipoDocumentoEntity`. Hay que dejar uno solo.

---

## 4. Errores encontrados durante la revisión

- [ ] **BUG-1. Las dos funciones de Paz y Salvo esperan nombres de formato distintos.** `tipoDeDocumento` busca `pm_fo_4_for_27` con guion bajo ([L783](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L783)). `validarDocumentosRequeridos` busca `pm-fo-4-for-27` con guion ([L933](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L933)). Un archivo puede salir clasificado como formato y aun así quedar como "falta". Se corrige al moverlo al dominio (ARQ-6), con una prueba.

- [x] **BUG-2. La clave JWT tiene un valor por defecto en el código.** En [JwtUtil.java:21](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/Security/JwtUtil.java#L21) está `app.jwt.secret` con un default. Si falta la variable en producción, la app arranca con una clave pública. Hay que quitar el default y fallar al arrancar si no está definida. *Además se exige un mínimo de 32 bytes.*

- [x] **BUG-3. `DataInitializer` hace `Thread.sleep(1000)`** "para que la BD esté lista" en [DataInitializer](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/configuracion/DataInitializer.java). `CommandLineRunner` ya se ejecuta con el contexto y la BD listos, así que se puede eliminar.

- [ ] **BUG-4. `GET /listarSolicitud-PazYSalvo/{id}` tiene tres significados.** Según el caso, `{id}` es un id de solicitud, un id de usuario (si la solicitud no existe) o un nombre de rol ([L400-450](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/SolicitudPazYSalvoRestController.java#L400-L450)). Es confuso y difícil de autorizar bien. Hay que separarlo en rutas explícitas. ⚠️ **frontend**

- [x] **BUG-5. `DataInitializer` restablece la contraseña del administrador en cada arranque.** *Hecho: solo crea la cuenta si no existe, con `ADMIN_CEDULA` y `ADMIN_PASSWORD` (en `dev`, `password123` por defecto), y no la toca después. `test-data.sql` traía un hash que no era de `password123` y funcionaba solo porque el inicializador lo sobrescribía; se corrigió.* En [DataInitializer](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/configuracion/DataInitializer.java) (`crearUsuarioAdminSiNoExiste`), si `admin@unicauca.edu.co` existe y su contraseña no es `password123`, la vuelve a poner en `password123`. Cambiar la contraseña del admin no sirve: al reiniciar queda la conocida. *Decisión pendiente*: crear el admin solo si no existe, con la contraseña tomada de una variable de entorno (como ya se hace con la cédula), y no tocarlo después.

- [x] **BUG-6. Una prueba fallaba en JDK 17 en Windows.** `test-data.sql` se cargaba con el charset de la plataforma (Windows-1252), así que "Juan Pérez" llegaba corrupto y `listarEstudiantesConPreinscripcionEInscripcion` fallaba. Se añadió `spring.sql.init.encoding=UTF-8` en `application-test.properties`. No afecta a producción.

- [x] **BUG-7. El funcionario no ve solicitudes nuevas en `listarSolicitudesPorRol`.** *Hecho: se compara con `ENVIADA.textoGuardado()`, el mismo criterio exacto que usan los gateways en `listarSolicitudesToFuncionario`. Prueba: `ListarSolicitudesPorRolTest` (falla sin el cambio en los tres casos de uso). Con `rol=FUNCIONARIO` las respuestas ahora incluyen las solicitudes enviadas; el resto no cambia. El filtro sigue en los casos de uso hasta ARQ-7/DUP-1 (ya resuelto: ahora lo hace la consulta).* En homologación, reingreso y ECAES ([GestionarSolicitudHomologacionCUAdapter](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/casosDeUso/GestionarSolicitudHomologacionCUAdapter.java), [GestionarSolicitudReingresoCUAdapter](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/casosDeUso/GestionarSolicitudReingresoCUAdapter.java) y [GestionarSolicitudEcaesCUAdapter](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/dominio/casosDeUso/GestionarSolicitudEcaesCUAdapter.java)), el filtro del rol `FUNCIONARIO` compara el último estado con `"ENVIADA"`. Pero al crear la solicitud se guarda `"Enviada"`, así que nunca coincide. Paz y salvo compara con `"Enviada"` y funciona. Afecta a `GET /listarSolicitud-Homologacion/porRol` y `GET /listarSolicitud-ecaes/porRol` con `rol=FUNCIONARIO`. En reingreso, ningún controlador llama a ese método. *(Corrección en la revisión 4: sí lo llama `GET /listarSolicitud-Reingreso/porUser`, así que BUG-7 también cambió esa respuesta con `rol=FUNCIONARIO`.)* Se corrige comparando con `FlujoEstadosSolicitud.normalizar(...)` o con `ENVIADA.textoGuardado()`. Se resuelve junto con ARQ-7 y DUP-1, con una prueba. Está marcado con `// BUG-7` en el código.

- [ ] **BUG-8. Reportes de verano: las preinscripciones rechazadas no ofrecen "revisar_motivo_rechazo".** [ReportesCursosVeranoRestController](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/ReportesCursosVeranoRestController.java) compara con `"Rechazada"`, pero la preinscripción rechazada se guarda como `"Rechazado"`. La acción no aparece nunca. Se corrige usando `EstadoSolicitudCursoVerano.RECHAZADO`; cambia el JSON (aparece la acción), así que se coordina con el frontend. ⚠️ **frontend**

---

- [ ] **BUG-9. El listado "por rol" de homologación y reingreso no incluye a la secretaría.** `listarSolicitudesPorRol` con `SECRETARIA` devuelve `[]` en homologación (`GET /listarSolicitud-Homologacion/porRol`) y reingreso (`GET /listarSolicitud-Reingreso/porUser`), mientras que en paz y salvo devuelve las de último estado `APROBADA_COORDINADOR`. Se conservó tal cual en DUP-1 (marcado con `// BUG-9` en los dos casos de uso). Quitar ese `if` hace que la secretaría vea sus pendientes; cambia el JSON con `rol=SECRETARIA`, así que se coordina con el frontend. ⚠️ **frontend**

- [ ] **BUG-10. El coordinador no tiene paso en ECAES, pero hay una vista de coordinador.** `GET /listarSolicitud-ecaes/porRol?rol=COORDINADOR` buscaba ECAES con último estado `APROBADA_FUNCIONARIO`, que el flujo ECAES (`Enviada → PRE_REGISTRADO | RECHAZADA`) no permite, así que siempre salía vacía con datos válidos. Desde DUP-1 el flujo decide y la lista es `[]` sin consultar. *Decisión pendiente*: si el coordinador debe ver algo de ECAES (por ejemplo las `PRE_REGISTRADO` de su programa), hay que añadir ese paso al flujo; si no, el frontend puede dejar de pedirla. ⚠️ **frontend**

## 5. Nombres y estilo (P3)

- [ ] **EST-1. Errores de escritura en nombres:**
  - `SolicitudCursoVeranoIncripcion` → `…Inscripcion` (103 usos)
  - `SolicitudCurosoVerano…`
  - `SolicitudHomologacioneMapperDominio`
  - `bucarRolPorId`
  - `mappear…` → `mapear…` (todos los mappers)
  - `GestionarArchivosCUIAdapter` → `GestionarArchivosCUAdapter`
  - `GestionarSolicitudReingresoImplAdapter` → `…ReingresoGatewayImplAdapter`

  El IDE puede renombrar de forma segura, pero hay que hacerlo en un commit aparte para no mezclarlo con cambios de lógica.

- [ ] **EST-2. Hay paquetes con mayúscula:** `Security` → `seguridad`, `Enums` → `enums` (dos paquetes), `DTOPeticion` / `DTORespuesta` → `dto.peticion` / `dto.respuesta`, `controladorExcepciones` → `excepciones`.

- [ ] **EST-3. Hay clases que no siguen la convención de nombres:**
  - En minúscula: `documentoDto`, `solicitudEcaesSalidaDto`.
  - En inglés, cuando el resto del proyecto está en español: `DocumentGeneratorService`, `DocumentGeneratorController`, `DocumentRequest`, `DocumentTemplate`, `CreateCursoDTO`, `UpdateCursoDTO`.
  - `DocumentRequest` está en el paquete `DTORespuesta` aunque es una petición.

- [ ] **EST-4. `GestionClientesRuntimeException`** es un nombre heredado de la plantilla de otro proyecto. Debería llamarse, por ejemplo, `GestionCurricularException`.

- [ ] **EST-5. 59 campos usan el prefijo `obj`** (`objGestionarArchivos`, `objGestionarSolicitudGateway`, …). Se puede quitar el prefijo, por ejemplo `archivos` o `solicitudGateway`.

- [ ] **EST-6. Hay nombres completamente calificados dentro del código**, como `co.edu.unicauca…dominio.modelos.Materia`, en lugar de usar `import`. Aparecen en CatalogosCursosVerano (8), CursosVerano (4), InscripcionesVerano (5), PreinscripcionesVerano (3), CursoOfertado, GestionarSolicitudCursoVeranoCU (2), GestionarSolicitudPazYSalvoCU y GestionarNotificacionCUIntPort (2).

- [ ] **EST-7. Las rutas REST no siguen un mismo estilo:**
  - verbos en la URL: `/crearDocente`, `/eliminarDocente/{id}`, `/buscarPorId/{id}`
  - camelCase mezclado con kebab-case: `/crearSolicitud-PazYSalvo`
  - una ñ en `/añadirComentario`
  - dos controladores sobre `/api/cursos-intersemestrales`

  Hay que definir una convención (sustantivos en plural, kebab-case, verbo HTTP para la acción), aplicarla a las rutas nuevas y migrar las antiguas con el frontend. ⚠️ **frontend**

- [ ] **EST-8.** [DocumentGeneratorController](gestion_curricular/src/main/java/co/edu/unicauca/decanatura/gestion_curricular/infraestructura/input/controladores/DocumentGeneratorController.java) inyecta dependencias con `@Autowired` en campos. Hay que pasarlas por constructor, como en el resto del proyecto.

- [ ] **EST-9. Imports desordenados**, por ejemplo en Paz y Salvo: hay imports de Spring, de dominio y de `java.*` intercalados. Se puede aplicar un formateador (Spotless o el formato del IDE) a todo el proyecto en un solo commit.

## 6. Opcional, después de la entrega

- [ ] **OPC-1. Los modelos de dominio no tienen comportamiento.** Usan `@Data` con setters públicos y casi no tienen métodos; 23 de 28 no tienen ninguno. Mover reglas a los modelos (`solicitud.estadoActual()`, `curso.tieneCupo(ocupados)`, `solicitud.rechazar(comentario)`) completa lo que empiezan DUP-3 y DUP-2.
- [ ] **OPC-2. 56 campos del dominio usan snake_case** (`id_usuario`, `nombre_completo`), lo que genera getters como `getId_usuario()`. Cambiarlos afecta al JSON salvo que se use `@JsonProperty` en los DTOs. ⚠️ **frontend**
- [ ] **OPC-3. Dividir los servicios de estadísticas grandes.** `EstadisticasDashboard` (762 líneas), `EstadisticasGenerales` (529) y `PrediccionDemandaCursosVerano` (582) podrían separarse en clases más pequeñas por responsabilidad.

---

## Orden sugerido

1. **ARQ-1**: la prueba de arquitectura va primero, para medir el avance.
2. **MUE-1 a MUE-4, MUE-8, BUG-2, BUG-3**: limpieza rápida y sin riesgo.
3. **DUP-4 y DUP-3**: enums de estado y `estadoActual()`, que facilitan todo lo demás.
4. **ARQ-2, DUP-2**: cupo.
5. **ARQ-7 y DUP-1**: listados por rol. Es el cambio que más código elimina.
6. **ARQ-6 y BUG-1**: vaciar el controlador de Paz y Salvo, luego Estadísticas y luego Cursos de verano.
7. **ARQ-3, ARQ-4, ARQ-5**: puertos para generadores y autorización.
8. **ARQ-8 a ARQ-11, DUP-5 a DUP-10**: DTOs, manejo de errores y utilidades comunes.
9. **EST-***: renombrados al final, cada uno en su propio commit.
