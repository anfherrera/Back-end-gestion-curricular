# Prompt: Comentarios acumulativos por documento (sin sobrescribir)

## Contexto del proyecto

- **Backend**: Spring Boot, arquitectura hexagonal.
- **Estructura**:
  - **Puertos de aplicación**: `aplicacion/input` (casos de uso), `aplicacion/output` (gateways).
  - **Dominio**: `dominio/modelos`, `dominio/casosDeUso` (adaptadores que implementan los puertos de entrada).
  - **Infraestructura**: `infraestructura/input/controladores`, `infraestructura/input/DTOPeticion`, `infraestructura/input/DTORespuesta`, `infraestructura/output/persistencia/gateway`, `infraestructura/output/persistencia/entidades`.
- **Documentos**: El modelo de dominio `Documento` y la entidad `DocumentoEntity` tienen un único campo `comentario` (String). El endpoint `PUT /api/documentos/añadirComentario` recibe `idDocumento` y `comentario`; el caso de uso actual **reemplaza** el comentario del documento por el nuevo y luego llama a `actualizarDocumento`. Las solicitudes (OPAZ y salvo, ECAES, reingreso, homologación) asocian documentos; funcionario y coordinador pueden añadir comentarios a esos documentos. Hoy, si el coordinador añade un comentario a un documento que ya tenía comentario del funcionario, **se sobrescribe** y solo se ve el último.

## Objetivo del cambio

Sin cambiar de golpe toda la estructura ni romper los procesos actuales:

1. **No sobrescribir**: Al añadir un comentario a un documento que ya tiene comentario, **concatenar** el nuevo al existente (por ejemplo en una nueva línea), en lugar de reemplazarlo.
2. **Mostrar historial en el recuadro**: Cuando se abra el recuadro/cuadro de “añadir comentario” para un documento, que se **muestre el comentario anterior** (o los anteriores) en ese mismo recuadro, y que el usuario pueda **escribir en la siguiente línea** para añadir otro comentario.
3. **Indicar quién comentó (si es posible)**: Que cada bloque de comentario pueda incluir **qué usuario comentó** (por ejemplo “Rol - Nombre” o “Correo”), de forma opcional y sin romper lo ya existente.

## Restricciones

- Respetar la **arquitectura hexagonal** del back (puertos, adaptadores, dominio sin depender de infra).
- **No afectar** el correcto funcionamiento actual: aprobaciones, rechazos, listados, descargas y todo lo que ya usa `comentario` o `añadirComentario` debe seguir funcionando.
- Minimizar cambios estructurales: se prefiere seguir usando el **mismo campo `comentario`** (String) en `Documento`/`DocumentoEntity`, guardando un texto multilínea con el historial (por ejemplo cada línea o bloque con formato `[fecha] Rol - Nombre: texto`), en lugar de crear de entrada una nueva tabla de “comentarios por documento” a menos que sea estrictamente necesario.

## Tareas concretas (backend)

1. **Caso de uso `añadirComentario`**  
   - En lugar de hacer `documento.setComentario(comentario)` y reemplazar, obtener el comentario actual del documento (`documento.getComentario()`), construir el **nuevo bloque** a añadir (solo el texto nuevo que envía el front) y asignar al documento el resultado de **concatenar** comentario actual + separador (por ejemplo `\n`) + nuevo bloque.  
   - Opcional: el nuevo bloque puede tener formato `[dd/MM/yyyy HH:mm] Rol - NombreUsuario: <texto nuevo>`. Para ello, el caso de uso debe recibir parámetros opcionales (por ejemplo `nombreUsuario`, `rolUsuario`); si no se pasan, concatenar solo `[fecha] <texto nuevo>` o solo `<texto nuevo>` para no romper compatibilidad.

2. **Puerto de entrada (aplicación)**  
   - Extender la firma del puerto del caso de uso (por ejemplo `GestionarDocumentosCUIntPort.añadirComentario`) para aceptar, además de `idDocumento` y `comentario`, parámetros opcionales como `nombreUsuario` y `rolUsuario` (o un DTO mínimo con esos campos), de forma que el controlador pueda pasar el usuario autenticado cuando exista.

3. **Controlador (infraestructura de entrada)**  
   - En `DocumentoRestController` (o el que expone `PUT /api/documentos/añadirComentario`): obtener el usuario autenticado con `SecurityContextHolder.getContext().getAuthentication()` (como ya se hace en otros controladores del proyecto, p. ej. `SolicitudHomologacionRestController`). Si el proyecto tiene un puerto/servicio para obtener el usuario por correo (por ejemplo `GestionarUsuarioGatewayIntPort.buscarUsuarioPorCorreo`), usarlo para obtener nombre y rol del usuario autenticado y pasarlos al caso de uso. Si no se puede resolver el usuario, llamar al caso de uso pasando `null` en nombre/rol para que solo se concatene el texto con fecha (o solo el texto).

4. **Persistencia**  
   - El campo `comentario` en `DocumentoEntity` tiene actualmente `length = 100`; para soportar varios comentarios concatenados, **aumentar la longitud** (por ejemplo 1000 o 2000 caracteres) en la entidad y, si la base de datos aplica límites, ajustar el esquema (migración o script SQL) para que no se trunque el texto.

5. **Gateway**  
   - Mantener el flujo actual: el caso de uso actualiza el modelo de dominio y llama a `actualizarDocumento(documento)` en el gateway. No es necesario usar el método `añadirComentario` del gateway para esta lógica; la concatenación se hace en el caso de uso y el gateway sigue persistiendo el `comentario` completo del dominio. Verificar que `actualizarDocumento` siga actualizando solo los campos editables (incluido `comentario`) sin sobrescribir `objSolicitud` cuando venga null (comportamiento ya corregido en el proyecto).

6. **Compatibilidad**  
   - Documentos que hoy tienen un solo comentario deben seguir mostrándose igual; el único cambio es que a partir de ahora los “nuevos” comentarios se **añaden** en lugar de reemplazar. Los listados y vistas que ya muestran `documento.getComentario()` seguirán mostrando el contenido completo (ahora posiblemente multilínea).

## Frontend (qué cambiaría)

- **Al abrir el recuadro de “añadir comentario”** para un documento: obtener el documento actual (por ejemplo con `GET /api/documentos/buscarPorId/{id}` si no se tiene ya) y **mostrar en el cuadro de texto** el valor actual de `comentario` (puede ser una o varias líneas). Así el usuario ve el historial.
- **Al enviar**: el front debe enviar en `PUT /api/documentos/añadirComentario` **solo el texto nuevo** que el usuario escribió (las líneas que añadió en esta vez), no todo el contenido del cuadro. El backend se encargará de concatenar ese nuevo texto al comentario existente (y de añadir fecha y, si aplica, “Rol - Nombre”).
- Opciones de UX: (a) Un solo cuadro que muestra el historial y donde el usuario escribe debajo; al enviar, el front envía solo la parte “nueva” (por ejemplo desde la última línea conocida o desde un marcador). (b) Mostrar el historial en zona de solo lectura y un segundo campo “Nuevo comentario” que es lo que se envía. Cualquiera de las dos es válida; lo importante es que el **body del PUT envíe solo el comentario nuevo** para que el backend lo concatene.

## Resumen

- **Backend**: concatenar en el caso de uso en lugar de reemplazar; opcionalmente prefijar cada nuevo comentario con fecha y “Rol - Nombre”; ampliar longitud de `comentario` en entidad/BD; pasar usuario autenticado desde el controlador al caso de uso; mantener arquitectura hexagonal y no tocar la lógica que ya funciona.
- **Frontend**: cargar y mostrar el `comentario` actual del documento en el recuadro y enviar solo el **texto nuevo** en `PUT /api/documentos/añadirComentario`.

Si algo no está claro en la estructura del proyecto (nombres exactos de paquetes o clases), revisar primero la estructura real en `gestion_curricular/src/main/java` (puertos en `aplicacion`, casos de uso en `dominio/casosDeUso`, controladores en `infraestructura/input/controladores`, gateway y entidades en `infraestructura/output`).
