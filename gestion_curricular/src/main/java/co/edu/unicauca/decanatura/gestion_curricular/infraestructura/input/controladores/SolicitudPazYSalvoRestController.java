package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort.RechazoCambioEstado.Motivo;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CrearSolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosPazYSalvoDTORespuesta.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.RechazoCambioEstadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudPazYSalvoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.ErroresPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ArchivoMultipart;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorArchivoPdf;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
@RestController
@RequestMapping("/api/solicitudes-pazysalvo")
@RequiredArgsConstructor
public class SolicitudPazYSalvoRestController {

    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final SolicitudPazYSalvoMapperDominio solicitudMapperDominio;
    private final GestionarArchivosCUIntPort gestionarArchivos;
    private final GestionarDocumentosCUIntPort documentosCU;
    private final DocumentosMapperDominio documentosMapperDominio;
    private final GenerarDocumentosCUIntPort documentGeneratorService;
    private final ObjectMapper objectMapper;
    private final Autorizacion autorizacion;

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping("/crearSolicitud-PazYSalvo")
    public ResponseEntity<?> crearSolicitudPazYSalvo(
            @Valid @RequestBody CrearSolicitudPazYSalvoDTOPeticion peticion) {
        return ErroresPazYSalvo.alCrear(() -> {
            if (!peticion.esObjeto()) {
                return respuestaBadRequest("Formato de solicitud no soportado");
            }
            if (peticion.estaVacia()) {
                return respuestaBadRequest("Los datos de la solicitud son requeridos");
            }

            // Un estudiante solo puede crear solicitudes a su nombre: el usuario sale del token JWT,
            // no del cuerpo de la petición.
            if (!autorizacion.esStaff()) {
                Integer idUsuarioActual = autorizacion.idUsuarioActual();
                if (idUsuarioActual == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Usuario no autenticado"));
                }
                peticion.asignarUsuario(idUsuarioActual);
            }

            if (peticion.traeIdUsuario()) {
                String idUsuario = peticion.getIdUsuario();
                if (idUsuario == null || idUsuario.isBlank()) {
                    return respuestaBadRequest("El campo idUsuario es obligatorio");
                }
                return guardarSolicitudDominio(construirSolicitudBasica(peticion));
            }

            SolicitudPazYSalvoDTOPeticion dtoPeticion = objectMapper.convertValue(peticion.getCampos(),
                    SolicitudPazYSalvoDTOPeticion.class);
            String mensajeValidacion = validarSolicitudDto(dtoPeticion);
            if (mensajeValidacion != null) {
                return respuestaBadRequest(mensajeValidacion);
            }

            return guardarSolicitudDesdeDto(dtoPeticion);
        });
    }

    private ResponseEntity<SolicitudPazYSalvoDTORespuesta> guardarSolicitudDesdeDto(
            SolicitudPazYSalvoDTOPeticion dtoPeticion) {

        SolicitudPazYSalvo solicitud = solicitudMapperDominio
                .mapearDeSolicitudDTOPeticionASolicitud(dtoPeticion);

        return guardarSolicitudDominio(solicitud);
    }

    private ResponseEntity<SolicitudPazYSalvoDTORespuesta> guardarSolicitudDominio(
            SolicitudPazYSalvo solicitud) {

        // El caso de uso carga el usuario si solo viene su id y pone el período actual si no viene
        SolicitudPazYSalvo solicitudCreada = solicitudPazYSalvoCU.guardar(solicitud);
        SolicitudPazYSalvoDTORespuesta respuesta = solicitudMapperDominio.mapearDeSolicitudARespuesta(solicitudCreada);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    private SolicitudPazYSalvo construirSolicitudBasica(CrearSolicitudPazYSalvoDTOPeticion peticion) {
        // Solo el id: el caso de uso comprueba que exista y carga el usuario completo
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(Integer.valueOf(peticion.getIdUsuario().trim()));
        SolicitudPazYSalvo solicitud = new SolicitudPazYSalvo();
        solicitud.setObjUsuario(usuario);
        // La fecha de registro y el nombre los pone el gateway al guardar (BUG-11)

        // Período académico: si viene, debe ser válido (si no viene, el caso de uso pone el actual)
        if (peticion.getPeriodoAcademico() != null) {
            String periodoAcademico = peticion.getPeriodoAcademico().trim();
            if (!periodoAcademico.isBlank()) {
                if (PeriodoAcademicoEnum.esValido(periodoAcademico)) {
                    solicitud.setPeriodoAcademico(periodoAcademico);
                } else {
                    throw new IllegalArgumentException("Período académico inválido. Use formato: YYYY-P (ej: 2024-2)");
                }
            }
        }

        // Guardar título y director del trabajo de grado si se proporcionan
        if (peticion.getTituloTrabajoGrado() != null) {
            String titulo = peticion.getTituloTrabajoGrado().trim();
            if (!titulo.isEmpty()) {
                solicitud.setTituloTrabajoGrado(titulo);
            }
        }

        if (peticion.getDirectorTrabajoGrado() != null) {
            String director = peticion.getDirectorTrabajoGrado().trim();
            if (!director.isEmpty()) {
                solicitud.setDirectorTrabajoGrado(director);
            }
        }

        return solicitud;
    }

    private String validarSolicitudDto(SolicitudPazYSalvoDTOPeticion dtoPeticion) {
        if (dtoPeticion == null) {
            return "Los datos de la solicitud son requeridos";
        }

        if (dtoPeticion.getObjUsuario() == null || dtoPeticion.getObjUsuario().getId_usuario() == null) {
            return "El usuario de la solicitud es obligatorio";
        }

        // Validar período académico si se proporciona
        if (dtoPeticion.getPeriodo_academico() != null && !dtoPeticion.getPeriodo_academico().trim().isEmpty()) {
            String periodoAcademico = dtoPeticion.getPeriodo_academico().trim();
            if (!PeriodoAcademicoEnum.esValido(periodoAcademico)) {
                return "Período académico inválido. Use formato: YYYY-P (ej: 2024-2)";
            }
        }

        // El nombre y la fecha de registro no se piden: los pone el gateway al guardar (BUG-11)
        return null;
    }

    private ResponseEntity<Map<String, String>> respuestaBadRequest(String mensaje) {
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    @GetMapping("/listarSolicitud-PazYSalvo")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvo() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudes();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mapearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    /** Sin período: el funcionario de paz y salvo ve las enviadas de todos los períodos. */
    @GetMapping("/listarSolicitud-PazYSalvo/Funcionario")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoFuncionario() {
        return responder(solicitudPazYSalvoCU.listar(
                FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.FUNCIONARIO, null, null)));
    }

    @GetMapping("/listarSolicitud-PazYSalvo/Coordinador")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoToCoordinador(
            @RequestParam(required = false) String periodoAcademico) {
        Integer idPrograma = autorizacion.idProgramaCoordinadorActual();
        if (idPrograma == null) {
            // Si no se puede obtener el programa, se devuelven todas, de todos los períodos (fallback)
            return responder(solicitudPazYSalvoCU.listar(
                    FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.COORDINADOR, null, null)));
        }
        return responder(solicitudPazYSalvoCU.listar(FiltroSolicitudes.pendientesDe(
                FlujoEstadosSolicitud.COORDINADOR, idPrograma, PeriodoDeConsulta.oActual(periodoAcademico))));
    }

    @GetMapping("/listarSolicitud-PazYSalvo/Secretaria")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoToSecretaria(
            @RequestParam(required = false) String periodoAcademico) {
        return responder(solicitudPazYSalvoCU.listar(FiltroSolicitudes.pendientesDe(
                FlujoEstadosSolicitud.SECRETARIO, null, PeriodoDeConsulta.oActual(periodoAcademico))));
    }

    private ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> responder(List<SolicitudPazYSalvo> solicitudes) {
        return ResponseEntity.ok(solicitudMapperDominio.mapearListaDeSolicitudesARespuesta(solicitudes));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listarSolicitud-PazYSalvo/porRol")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPorRol(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario) {

        // Quien no es staff solo puede ver sus propias solicitudes
        if (!autorizacion.esStaff()) {
            rol = "ESTUDIANTE";
            idUsuario = autorizacion.idUsuarioActual();
            if (idUsuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudesPorRol(rol, idUsuario);

        List<SolicitudPazYSalvoDTORespuesta> respuesta =
                solicitudMapperDominio.mapearListaDeSolicitudesARespuesta(solicitudes);

        return ResponseEntity.ok(respuesta);
    }

    // BUG-4: /listarSolicitud-PazYSalvo/{id} se separa en estas dos rutas (convención EST-7).
    // Los nombres de rol ya no se aceptan ahí: las bandejas son /Funcionario, /Coordinador,
    // /Secretaria y /porRol.

    /** Una solicitud: el staff ve cualquiera y el estudiante solo las suyas. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{idSolicitud:\\d+}")
    public ResponseEntity<?> obtenerSolicitud(@PathVariable Integer idSolicitud) {
        if (!autorizacion.esStaff() && !autorizacion.esDuenoSolicitud(idSolicitud)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acceso denegado"));
        }
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        return ResponseEntity.ok(solicitudMapperDominio.mapearDeSolicitudARespuesta(solicitud));
    }

    /** Las solicitudes de un usuario en el período pedido o en el actual: el staff o el propio usuario. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/usuarios/{idUsuario:\\d+}/solicitudes")
    public ResponseEntity<?> listarSolicitudesDeUsuario(
            @PathVariable Integer idUsuario,
            @RequestParam(required = false) String periodoAcademico) {
        if (!autorizacion.esStaff() && !autorizacion.esUsuarioActual(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acceso denegado"));
        }
        return responder(solicitudPazYSalvoCU.listar(
                FiltroSolicitudes.deEstudiante(idUsuario, PeriodoDeConsulta.oActual(periodoAcademico))));
    }

    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Secretario', 'Administrador')")
    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<?> actualizarEstadoSolicitudPazYSalvo(
            @RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        return ErroresPazYSalvo.alCambiarEstado(() -> {
            if (peticion == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "La petición no puede ser nula"));
            }
            
            if (peticion.getIdSolicitud() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El ID de la solicitud es requerido"));
            }
            
            if (peticion.getNuevoEstado() == null || peticion.getNuevoEstado().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nuevo estado es requerido"));
            }

            var rechazo = solicitudPazYSalvoCU.cambiarEstadoSolicitud(
                    peticion.getIdSolicitud(), peticion.getNuevoEstado(), peticion.getComentario(),
                    rol -> autorizacion.tieneRol(rol) || autorizacion.tieneRol("Administrador"));
            if (rechazo.isPresent()) {
                var detalle = rechazo.get();
                HttpStatus estado = detalle.motivo() == Motivo.ROL_NO_AUTORIZADO
                        ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(estado).body(new RechazoCambioEstadoDTORespuesta(
                        detalle.mensaje(), detalle.motivo().name(), detalle.estadoActual()));
            }

            return ResponseEntity.noContent().build();
        });
    }

    // ===== Documento de Paz y Salvo (plantilla PAZ_SALVO de ServicioGeneracionDocumentos) =====

    /** GET /generarDocumentoPazYSalvo/{id}/pdf */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @GetMapping("/generarDocumentoPazYSalvo/{id}/pdf")
    public ResponseEntity<byte[]> generarDocumentoPazYSalvoPDF(
            @PathVariable Integer id,
            @RequestParam(value = "tituloTrabajoGrado", required = false) String tituloTrabajoGrado,
            @RequestParam(value = "directorTrabajoGrado", required = false) String directorTrabajoGrado)
            throws java.io.IOException {
        return documentoPazYSalvoPorDefecto(id, tituloTrabajoGrado, directorTrabajoGrado,
                "PazYSalvo_" + id + ".pdf", MediaType.APPLICATION_PDF);
    }

    /** GET /generarDocumentoPazYSalvo/{id}/docx */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @GetMapping("/generarDocumentoPazYSalvo/{id}/docx")
    public ResponseEntity<byte[]> generarDocumentoPazYSalvoDOCX(
            @PathVariable Integer id,
            @RequestParam(value = "tituloTrabajoGrado", required = false) String tituloTrabajoGrado,
            @RequestParam(value = "directorTrabajoGrado", required = false) String directorTrabajoGrado)
            throws java.io.IOException {
        return documentoPazYSalvoPorDefecto(id, tituloTrabajoGrado, directorTrabajoGrado,
                "PazYSalvo_" + id + ".docx", MediaType.APPLICATION_OCTET_STREAM);
    }

    /** Documento con número PYS-{id} y fecha de hoy. */
    private ResponseEntity<byte[]> documentoPazYSalvoPorDefecto(Integer id, String titulo, String director,
            String nombreArchivo, MediaType tipo) throws java.io.IOException {
        var datos = documentGeneratorService.generarPazYSalvo(id, null, titulo, director, true,
                MediaType.APPLICATION_PDF.equals(tipo), null, null, null);
        if (datos.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }
        return descarga(datos.get().contenido(), nombreArchivo, tipo);
    }

    /** POST /generar-documento/{idSolicitud}: documento con número, fecha y observaciones del funcionario. */
    @PostMapping("/generar-documento/{idSolicitud}")
    public ResponseEntity<byte[]> generarDocumentoPazSalvo(
            @PathVariable Integer idSolicitud,
            @RequestParam("numeroDocumento") String numeroDocumento,
            @RequestParam("fechaDocumento") String fechaDocumento,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "cedulaEstudiante", required = false) String cedulaEstudiante,
            @RequestParam(value = "tituloTrabajoGrado", required = false) String tituloTrabajoGrado,
            @RequestParam(value = "directorTrabajoGrado", required = false) String directorTrabajoGrado)
            throws java.io.IOException {
        var datos = documentGeneratorService.generarPazYSalvo(idSolicitud, cedulaEstudiante, tituloTrabajoGrado,
                directorTrabajoGrado, false, false, numeroDocumento, fechaDocumento, observaciones);
        if (datos.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }

        return descarga(datos.get().contenido(), datos.get().nombreArchivo(),
                MediaType.APPLICATION_OCTET_STREAM);
    }

    private static ResponseEntity<byte[]> descarga(byte[] contenido, String nombreArchivo, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.adjunto(nombreArchivo))
                .contentType(tipo)
                .body(contenido);
    }

    /**
     * Subir archivo para paz y salvo (SIN asociar a solicitud - como en homologación)
     * Los documentos se asocian automáticamente cuando se crea la solicitud
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/subir-documento")
    public ResponseEntity<?> subirDocumentoPazSalvo(
            @RequestParam("file") MultipartFile file) {
        ValidadorArchivoPdf.Rechazo rechazo = ValidadorArchivoPdf.validar(file, ValidadorArchivoPdf.MAXIMO_10MB);
        if (rechazo != null) {
            return ResponseEntity.status(rechazo.estado()).body(Map.of("error", rechazo.mensaje()));
        }
        String nombreOriginal = file.getOriginalFilename();

        Documento documentoGuardado = documentosCU.subirDocumento(ArchivoMultipart.de(file), autorizacion.idUsuarioActual());

        return ResponseEntity.ok(new DocumentoSubido(true, "Documento subido exitosamente (sin asociar)",
                documentoGuardado.getIdDocumento(), nombreOriginal, documentoGuardado.getFechaDocumento()));
    }

    record DocumentoSubido(boolean success, String message, Integer documento_id, String nombre, Date fecha) {
    }

    /**
     * Descargar documento específico por nombre
     * Busca el documento en la BD para obtener su ruta completa (incluye subcarpetas)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/descargar-documento")
    public ResponseEntity<byte[]> descargarDocumento(@RequestParam("filename") String filename) {
        
        // Buscar el documento en la BD por nombre para obtener su ruta completa
        String rutaArchivo = filename; // Por defecto usar el nombre recibido
        
        // Staff: busca en todas las solicitudes de paz y salvo.
        // Estudiante: solo en las suyas, y el archivo debe pertenecer a una de ellas.
        boolean esStaff = autorizacion.esStaff();
        List<SolicitudPazYSalvo> solicitudes;
        if (esStaff) {
            solicitudes = solicitudPazYSalvoCU.listarSolicitudes();
        } else {
            Integer idUsuarioActual = autorizacion.idUsuarioActual();
            if (idUsuarioActual == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesPorRol("ESTUDIANTE", idUsuarioActual);
        }
        boolean encontrado = false;
        for (SolicitudPazYSalvo solicitud : solicitudes != null ? solicitudes : List.<SolicitudPazYSalvo>of()) {
            if (encontrado) {
                break;
            }
            if (solicitud.getDocumentos() != null) {
                for (Documento doc : solicitud.getDocumentos()) {
                    // Comparar por nombre (sin ruta)
                    String nombreDoc = doc.getNombre();
                    if (nombreDoc != null && nombreDoc.equals(filename)) {
                        // Usar la ruta completa si está disponible
                        if (doc.getRutaDocumento() != null && !doc.getRutaDocumento().isEmpty()) {
                            rutaArchivo = doc.getRutaDocumento();
                        }
                        encontrado = true;
                        break;
                    }
                }
            }
        }
        if (!esStaff && !encontrado) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Obtener el archivo usando la ruta (puede ser nombre simple o ruta completa)
        byte[] archivo = gestionarArchivos.getFile(rutaArchivo);
        
        if (archivo == null || archivo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        
        // Extraer solo el nombre del archivo para el header (sin la ruta)
        String nombreArchivo = filename;
        if (rutaArchivo.contains("/")) {
            nombreArchivo = rutaArchivo.substring(rutaArchivo.lastIndexOf("/") + 1);
        }
        
        return descargaPdfConNombreUtf8(archivo, nombreArchivo);
    }

    private static ResponseEntity<byte[]> descargaPdfConNombreUtf8(byte[] contenido, String nombre) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        RespuestasArchivo.adjuntoUtf8(nombre))
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    /**
     * Descargar oficio por ID de solicitud de paz y salvo
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/descargarOficio/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficioPazSalvo(@PathVariable Integer idSolicitud) {
        // El primer oficio en PDF (subido por la secretaría) cuyo archivo se pueda leer
        for (Documento documento : solicitudPazYSalvoCU.oficios(idSolicitud)) {
            try {
                // Usar getFile que maneja tanto rutas simples como rutas con subcarpetas
                byte[] archivo = gestionarArchivos.getFile(
                    documento.getRutaDocumento() != null ? documento.getRutaDocumento() : documento.getNombre()
                );

                return descargaPdfConNombreUtf8(archivo, documento.getNombre());
            } catch (Exception e) {
                continue; // Probar el siguiente documento
            }
        }

        return ResponseEntity.notFound().build();
    }
    

    /**
     * Todos los documentos de la solicitud (los del estudiante y los oficios), con su tipo. Solo para el personal
     * (es la que usa la bandeja del coordinador).
     */
    @GetMapping("/obtenerDocumentos/coordinador/{idSolicitud}")
    public ResponseEntity<List<DocumentoConTipo>> obtenerDocumentosPazSalvoCoordinador(@PathVariable Integer idSolicitud) {
        List<DocumentoConTipo> documentos = new ArrayList<>();
        for (var conTipo : solicitudPazYSalvoCU.documentosConTipo(idSolicitud)) {
            Documento documento = conTipo.documento();
            documentos.add(new DocumentoConTipo(documento.getIdDocumento(), documento.getNombre(),
                    documento.getNombre(), documento.getRutaDocumento(), documento.getFechaDocumento(),
                    documento.isEsValido(), documento.getComentario(), conTipo.tipo()));
        }
        return ResponseEntity.ok(documentos);
    }

    /**
     * Obtener solo los comentarios existentes de una solicitud de paz y salvo
     * Devuelve:
     * - Comentario de rechazo (si existe)
     * - Documentos que tienen comentarios (solo los que tienen comentario)
     * GET /api/solicitudes-pazysalvo/obtenerComentarios/{idSolicitud}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/obtenerComentarios/{idSolicitud}")
    public ResponseEntity<Comentarios> obtenerComentariosSolicitud(@PathVariable Integer idSolicitud) {
        var comentarios = solicitudPazYSalvoCU.comentarios(idSolicitud);

        List<Comentario> documentos = comentarios.documentos().stream()
                .map(c -> new Comentario(c.idDocumento(), c.nombre(), c.comentario())).toList();
        String mensaje = comentarios.comentarioRechazo() == null && documentos.isEmpty()
                ? "No hay comentarios para esta solicitud" : null;
        return ResponseEntity.ok(new Comentarios(comentarios.comentarioRechazo(),
                documentos.isEmpty() ? null : documentos, mensaje));
    }

    /**
     * Obtener oficios disponibles para una solicitud de paz y salvo (solo oficios/resoluciones)
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Oficio>> obtenerOficiosPazSalvo(@PathVariable Integer idSolicitud) {
        List<Oficio> oficios = new ArrayList<>();
        for (Documento documento : solicitudPazYSalvoCU.oficios(idSolicitud)) {
            oficios.add(new Oficio(idSolicitud, documento.getNombre(), documento.getNombre(), documento.getRutaDocumento()));
        }
        return ResponseEntity.ok(oficios);
    }
    
    /**
     * Validar documentos requeridos para paz y salvo
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
    public ResponseEntity<Revision> validarDocumentosRequeridosPazSalvo(@PathVariable Integer idSolicitud) {
        var revision = solicitudPazYSalvoCU.revisarDocumentosRequeridos(idSolicitud);

        Map<String, Boolean> documentosRequeridos = new HashMap<>();
        revision.entregados().forEach((requerido, entregado) -> documentosRequeridos.put(requerido.clave(), entregado));

        return ResponseEntity.ok(new Revision(documentosRequeridos, revision.todosCompletos(), revision.totalDocumentos()));
    }
    /**
 * Guardar oficio generado para una solicitud de paz y salvo
 */
@PostMapping("/guardarOficio")
public ResponseEntity<DocumentosDTORespuesta> guardarOficioPazSalvo(
        @RequestParam("file") MultipartFile file,
        @RequestParam("idSolicitud") Integer idSolicitud,
        @RequestParam("tipoDocumento") String tipoDocumento,
        @RequestParam("numeroDocumento") String numeroDocumento,
        @RequestParam("fechaDocumento") String fechaDocumento,
        @RequestParam(value = "observaciones", required = false) String observaciones) {
    
    
    String nombreOriginal = file.getOriginalFilename();
    
    // Validaciones
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body(null);
    }
    
    if (!nombreOriginal.toLowerCase().endsWith(".docx")) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null);
    }
    
    // Comprueba la solicitud antes de guardar en subcarpetas: con 404 no queda ningún archivo en disco
    var adjunto = documentosCU.guardarYAdjuntarSiExiste(ArchivoMultipart.de(file), "docx",
            "pazysalvo", idSolicitud, () -> solicitudPazYSalvoCU.buscarPorId(idSolicitud) != null);
    if (!adjunto.solicitudExiste()) {
        return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(
        documentosMapperDominio.mapearDeDocumentoADTORespuesta(adjunto.documento()),
        HttpStatus.CREATED
    );
}

    /**
     * Subir oficio PDF de Secretaría y asociarlo a una solicitud de paz y salvo
     */
    @PostMapping("/subir-oficio-pdf/{idSolicitud}")
    public ResponseEntity<DocumentosDTORespuesta> subirOficioPdfPazYSalvo(
            @PathVariable Integer idSolicitud,
            @RequestParam("file") MultipartFile file) {
        ValidadorArchivoPdf.Rechazo rechazo = ValidadorArchivoPdf.validar(file, ValidadorArchivoPdf.MAXIMO_15MB);
        if (rechazo != null) {
            return ResponseEntity.status(rechazo.estado()).build();
        }

        String nombreOriginal = file.getOriginalFilename();

        // Validar que la solicitud exista
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }

        Solicitud objSolicitud = new Solicitud();
        objSolicitud.setIdSolicitud(idSolicitud);
        Documento documentoGuardado = documentosCU.adjuntarASolicitud(
                ArchivoMultipart.de(file), objSolicitud, "pazysalvo", null, false);
        return new ResponseEntity<>(
                documentosMapperDominio.mapearDeDocumentoADTORespuesta(documentoGuardado),
                HttpStatus.CREATED);
    }
}
