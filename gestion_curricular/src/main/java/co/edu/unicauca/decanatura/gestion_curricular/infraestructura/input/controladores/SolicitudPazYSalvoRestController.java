package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ArchivoMultipart;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort.DatosDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort.RechazoCambioEstado.Motivo;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudPazYSalvoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorArchivoPdf;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Date;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;

@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
@Slf4j
@RestController
@RequestMapping("/api/solicitudes-pazysalvo")
@RequiredArgsConstructor
public class SolicitudPazYSalvoRestController {

    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final SolicitudPazYSalvoMapperDominio solicitudMapperDominio;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosCUIntPort documentosCU;
    private final DocumentosMapperDominio documentosMapperDominio;
    private final co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.DocumentGeneratorService documentGeneratorService;
    private final ObjectMapper objectMapper;
    private final GestionarUsuarioCUIntPort usuarioCU;
    private final Autorizacion autorizacion;

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping("/crearSolicitud-PazYSalvo")
    public ResponseEntity<?> crearSolicitudPazYSalvo(
            @RequestBody Object peticion) {
        try {
            if (peticion == null) {
                return respuestaBadRequest("Los datos de la solicitud son requeridos");
            }

            if (peticion instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapPeticion = new HashMap<>((Map<String, Object>) peticion);

                if (mapPeticion.isEmpty()) {
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
                    mapPeticion.remove("objUsuario");
                    mapPeticion.put("idUsuario", idUsuarioActual);
                }

                if (mapPeticion.containsKey("idUsuario")) {
                    Object idUsuario = mapPeticion.get("idUsuario");
                    if (idUsuario == null || idUsuario.toString().isBlank()) {
                        return respuestaBadRequest("El campo idUsuario es obligatorio");
                    }

                    SolicitudPazYSalvo solicitud = construirSolicitudBasica(mapPeticion);
                    return guardarSolicitudDominio(solicitud);
                }

                SolicitudPazYSalvoDTOPeticion dtoPeticion = objectMapper.convertValue(mapPeticion,
                        SolicitudPazYSalvoDTOPeticion.class);
                String mensajeValidacion = validarSolicitudDto(dtoPeticion);
                if (mensajeValidacion != null) {
                    return respuestaBadRequest(mensajeValidacion);
                }

                return guardarSolicitudDesdeDto(dtoPeticion);
            }

            return respuestaBadRequest("Formato de solicitud no soportado");
        } catch (ReglaNegocioExcepcion e) {
            // Ej.: el estudiante ya tiene una solicitud en trámite
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.formatException(), "codigo", "REGLA_NEGOCIO"));
        } catch (IllegalArgumentException e) {
            return respuestaBadRequest("Formato de solicitud inválido: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<SolicitudPazYSalvoDTORespuesta> guardarSolicitudDesdeDto(
            SolicitudPazYSalvoDTOPeticion dtoPeticion) {

        SolicitudPazYSalvo solicitud = solicitudMapperDominio
                .mappearDeSolicitudDTOPeticionASolicitud(dtoPeticion);

        return guardarSolicitudDominio(solicitud);
    }

    private ResponseEntity<SolicitudPazYSalvoDTORespuesta> guardarSolicitudDominio(
            SolicitudPazYSalvo solicitud) {

        // Asegurar que el usuario esté completo (cargar desde la base de datos si solo tiene el ID)
        if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getId_usuario() != null) {
            if (solicitud.getObjUsuario().getNombre_completo() == null || solicitud.getObjUsuario().getNombre_completo().trim().isEmpty()) {
                Usuario usuarioCompleto = usuarioCU.buscarUsuarioPorId(solicitud.getObjUsuario().getId_usuario()).orElse(null);
                if (usuarioCompleto != null) {
                    solicitud.setObjUsuario(usuarioCompleto);
                }
            }
            // El nombre de la solicitud lo pone el gateway al guardar (NombreSolicitud)
        }

        // Si no se proporcionó período académico, establecer el período actual automáticamente
        if (solicitud.getPeriodo_academico() == null || solicitud.getPeriodo_academico().trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                solicitud.setPeriodo_academico(periodoActual.getValor());
            }
        }

        SolicitudPazYSalvo solicitudCreada = solicitudPazYSalvoCU.guardar(solicitud);
        SolicitudPazYSalvoDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudARespuesta(solicitudCreada);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    private SolicitudPazYSalvo construirSolicitudBasica(Map<String, Object> mapPeticion) {
        Integer idUsuario = Integer.valueOf(mapPeticion.get("idUsuario").toString().trim());

        // Cargar el usuario completo para obtener el nombre
        Usuario usuarioCompleto = usuarioCU.buscarUsuarioPorId(idUsuario).orElse(null);
        if (usuarioCompleto == null) {
            throw new EntidadNoExisteException("Usuario no encontrado con ID: " + idUsuario);
        }

        // El nombre de la solicitud lo pone el gateway al guardar (NombreSolicitud)
        SolicitudPazYSalvo solicitud = new SolicitudPazYSalvo();
        solicitud.setObjUsuario(usuarioCompleto);

        if (mapPeticion.containsKey("fecha_solicitud") && mapPeticion.get("fecha_solicitud") != null) {
            String fechaSolicitud = mapPeticion.get("fecha_solicitud").toString();
            if (!fechaSolicitud.isBlank()) {
                try {
                    LocalDate fecha = LocalDate.parse(fechaSolicitud);
                    solicitud.setFecha_registro_solicitud(java.sql.Date.valueOf(fecha));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("fecha_solicitud con formato inválido (yyyy-MM-dd)");
                }
            }
        }

        if (solicitud.getFecha_registro_solicitud() == null) {
            solicitud.setFecha_registro_solicitud(new Date());
        }

        // Agregar período académico: si se proporciona, validarlo; si no, usar el período actual automáticamente
        if (mapPeticion.containsKey("periodo_academico") && mapPeticion.get("periodo_academico") != null) {
            String periodoAcademico = mapPeticion.get("periodo_academico").toString().trim();
            if (!periodoAcademico.isBlank()) {
                // Validar formato del período académico
                if (PeriodoAcademicoEnum.esValido(periodoAcademico)) {
                    solicitud.setPeriodo_academico(periodoAcademico);
                } else {
                    throw new IllegalArgumentException("Período académico inválido. Use formato: YYYY-P (ej: 2024-2)");
                }
            }
        }
        
        // Si no se proporcionó período académico, establecer el período actual automáticamente
        if (solicitud.getPeriodo_academico() == null || solicitud.getPeriodo_academico().trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                solicitud.setPeriodo_academico(periodoActual.getValor());
            }
        }
        
        // Guardar título y director del trabajo de grado si se proporcionan
        if (mapPeticion.containsKey("titulo_trabajo_grado") && mapPeticion.get("titulo_trabajo_grado") != null) {
            String titulo = mapPeticion.get("titulo_trabajo_grado").toString().trim();
            if (!titulo.isEmpty()) {
                solicitud.setTitulo_trabajo_grado(titulo);
            }
        }
        
        if (mapPeticion.containsKey("director_trabajo_grado") && mapPeticion.get("director_trabajo_grado") != null) {
            String director = mapPeticion.get("director_trabajo_grado").toString().trim();
            if (!director.isEmpty()) {
                solicitud.setDirector_trabajo_grado(director);
            }
        }

        return solicitud;
    }

    private boolean esIdentificadorNumerico(String valor) {
        if (valor == null) {
            return false;
        }
        try {
            Integer.parseInt(valor.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

        if (dtoPeticion.getNombre_solicitud() == null || dtoPeticion.getNombre_solicitud().isBlank()) {
            return "El nombre de la solicitud es obligatorio";
        }

        if (dtoPeticion.getFecha_registro_solicitud() == null) {
            return "La fecha de la solicitud es obligatoria";
        }

        return null;
    }

    private ResponseEntity<Map<String, String>> respuestaBadRequest(String mensaje) {
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    private List<SolicitudPazYSalvo> obtenerSolicitudesPorRol(String rol) {
        String rolNormalizado = rol != null ? rol.trim().toLowerCase() : "";

        switch (rolNormalizado) {
            case "coordinador":
                return solicitudPazYSalvoCU.listarSolicitudesToCoordinador();
            case "funcionario":
                return solicitudPazYSalvoCU.listarSolicitudesToFuncionario();
            case "secretaria":
                return solicitudPazYSalvoCU.listarSolicitudesToSecretaria();
            case "":
                return solicitudPazYSalvoCU.listarSolicitudes();
            default:
                List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudesPorRol(rol, null);
                return solicitudes != null ? solicitudes : Collections.emptyList();
        }
    }

    @GetMapping("/listarSolicitud-PazYSalvo")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvo() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudes();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/Funcionario")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoFuncionario() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudesToFuncionario();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/Coordinador")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoToCoordinador(
            @RequestParam(required = false) String periodoAcademico) {
        // Obtener el programa del coordinador autenticado
        Integer idPrograma = autorizacion.idProgramaCoordinadorActual();
        
        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
            }
        }
        
        List<SolicitudPazYSalvo> solicitudes;
        if (idPrograma != null) {
            // Filtrar por programa y período del coordinador
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                solicitudes = solicitudPazYSalvoCU.listarSolicitudesToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico.trim());
            } else {
                solicitudes = solicitudPazYSalvoCU.listarSolicitudesToCoordinadorPorPrograma(idPrograma);
            }
        } else {
            // Si no se puede obtener el programa, retornar todas (fallback)
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesToCoordinador();
        }
        
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/Secretaria")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoToSecretaria(
            @RequestParam(required = false) String periodoAcademico) {
        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
            }
        }
        
        List<SolicitudPazYSalvo> solicitudes;
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesToSecretariaPorPeriodo(periodoAcademico.trim());
        } else {
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesToSecretaria();
        }
        
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
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
                solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);

        return ResponseEntity.ok(respuesta);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listarSolicitud-PazYSalvo/{id}")
    public ResponseEntity<?> listarPazYSalvoByIdOrRole(
            @PathVariable String id,
            @RequestParam(required = false) String periodoAcademico) {
        boolean esStaff = autorizacion.esStaff();
        if (!esStaff && !esIdentificadorNumerico(id)) {
            // Listar por rol (coordinador, funcionario, secretaria) es solo para staff
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acceso denegado"));
        }
        if (esIdentificadorNumerico(id)) {
            Integer idNumero = Integer.valueOf(id);
            if (!esStaff
                    && !autorizacion.esDuenoSolicitud(idNumero)
                    && !autorizacion.esUsuarioActual(idNumero)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acceso denegado"));
            }
            try {
                SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idNumero);
                if (!esStaff && !autorizacion.esDuenoSolicitud(idNumero)) {
                    // El id corresponde a una solicitud ajena; para el estudiante se interpreta como su id de usuario
                    throw new EntidadNoExisteException("Solicitud no encontrada");
                }
                SolicitudPazYSalvoDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudARespuesta(solicitud);
                return ResponseEntity.ok(respuesta);
            } catch (EntidadNoExisteException ex) {
                // Si no se proporciona período, usar el período académico actual
                if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
                    PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
                    if (periodoActual != null) {
                        periodoAcademico = periodoActual.getValor();
                    }
                }
                
                List<SolicitudPazYSalvo> solicitudes;
                if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                    solicitudes = solicitudPazYSalvoCU.listarSolicitudesPorUsuarioYPeriodo(idNumero, periodoAcademico.trim());
                } else {
                    solicitudes = solicitudPazYSalvoCU.listarSolicitudesPorRol("ESTUDIANTE", idNumero);
                }
                
                List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio
                        .mappearListaDeSolicitudesARespuesta(solicitudes);
                return ResponseEntity.ok(respuesta);
            }
        }

        List<SolicitudPazYSalvo> solicitudes = obtenerSolicitudesPorRol(id);
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio
                .mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Secretario', 'Administrador')")
    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<?> actualizarEstadoSolicitudPazYSalvo(
            @RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        try {
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
                Map<String, Object> cuerpo = new HashMap<>();
                cuerpo.put("error", detalle.mensaje());
                cuerpo.put("codigo", detalle.motivo().name());
                cuerpo.put("estadoActual", detalle.estadoActual());
                return ResponseEntity.status(estado).body(cuerpo);
            }

            return ResponseEntity.noContent().build();
        } catch (EntidadNoExisteException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== Documento de Paz y Salvo (plantilla PAZ_SALVO de DocumentGeneratorService) =====

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
        var datos = solicitudPazYSalvoCU.prepararDocumento(id, null, titulo, director, true);
        if (datos.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }
        String numero = "PYS-" + id;
        String fecha = LocalDate.now().toString();
        String observaciones = "Paz y Salvo generado";
        byte[] contenido = MediaType.APPLICATION_PDF.equals(tipo)
                ? documentGeneratorService.generarPazYSalvoPdf(numero, fecha, observaciones, datos.get())
                : generarDocumentoPazYSalvo(numero, fecha, observaciones, datos.get());
        return descarga(contenido, nombreArchivo, tipo);
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
        var datos = solicitudPazYSalvoCU.prepararDocumento(idSolicitud, cedulaEstudiante, tituloTrabajoGrado,
                directorTrabajoGrado, false);
        if (datos.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }

        String nombreLimpio = datos.get().nombreArchivo().replaceAll("[^a-zA-Z0-9]", "_");
        String nombreArchivo = String.format("PAZ_SALVO_%s_%s.docx", nombreLimpio, numeroDocumento);
        return descarga(generarDocumentoPazYSalvo(numeroDocumento, fechaDocumentoNormalizada(fechaDocumento),
                observaciones == null ? "" : observaciones, datos.get()), nombreArchivo,
                MediaType.APPLICATION_OCTET_STREAM);
    }

    /** "YYYY-MM-DD" (si viene con hora se toma solo la fecha); si falta o no tiene ese formato, la fecha de hoy. */
    private static String fechaDocumentoNormalizada(String fechaDocumento) {
        if (fechaDocumento != null && !fechaDocumento.trim().isEmpty()) {
            String fecha = fechaDocumento.trim();
            if (fecha.contains("T")) {
                fecha = fecha.substring(0, 10);
            }
            if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return fecha;
            }
        }
        return LocalDate.now().toString();
    }

    private byte[] generarDocumentoPazYSalvo(String numero, String fecha, String observaciones, DatosDocumento datos)
            throws java.io.IOException {
        return documentGeneratorService.generarPazYSalvo(numero, fecha, observaciones, datos).toByteArray();
    }

    private static ResponseEntity<byte[]> descarga(byte[] contenido, String nombreArchivo, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
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

        // Guardar archivo (sin ID de solicitud, se guarda en raíz para documentos huérfanos)
        // Nota: Este endpoint es para documentos sin asociar, se mantiene en raíz
        String rutaArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreOriginal, "pdf");
        
        // Crear documento SIN asociar a solicitud (como en homologación)
        Documento doc = new Documento();
        doc.setNombre(nombreOriginal);
        doc.setRuta_documento(rutaArchivo);
        doc.setFecha_documento(new Date());
        doc.setEsValido(true);
        // Sin solicitud todavía: se asociará a la próxima solicitud que cree este mismo usuario
        doc.setIdUsuarioSubio(autorizacion.idUsuarioActual());
        
        Documento documentoGuardado = documentosCU.registrarDocumento(doc);
        
        return ResponseEntity.ok(new DocumentoSubido(true, "Documento subido exitosamente (sin asociar)",
                documentoGuardado.getId_documento(), nombreOriginal, documentoGuardado.getFecha_documento()));
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
                        if (doc.getRuta_documento() != null && !doc.getRuta_documento().isEmpty()) {
                            rutaArchivo = doc.getRuta_documento();
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
        byte[] archivo = objGestionarArchivos.getFile(rutaArchivo);
        
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
        String codificado = java.net.URLEncoder.encode(nombre, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombre + "\"; filename*=UTF-8''" + codificado)
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    /**
     * Descargar oficio por ID de solicitud de paz y salvo
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/descargarOficio/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficioPazSalvo(@PathVariable Integer idSolicitud) {
        // Obtener la solicitud con sus documentos
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos asociados a esta solicitud
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos que sean oficios/resoluciones (subidos por secretaria)
        for (Documento documento : documentos) {
            if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                String nombreArchivo = documento.getNombre().toLowerCase();
                
                if (esOficio(nombreArchivo)) {
                    try {
                        // Usar getFile que maneja tanto rutas simples como rutas con subcarpetas
                        byte[] archivo = objGestionarArchivos.getFile(
                            documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre()
                        );
                        
                        return descargaPdfConNombreUtf8(archivo, documento.getNombre());
                    } catch (Exception e) {
                        continue; // Probar el siguiente documento
                    }
                }
            }
        }
        
        return ResponseEntity.notFound().build();
    }
    

    /** Todos los documentos de la solicitud (los del estudiante y los oficios), con su tipo. */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/obtenerDocumentos/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerDocumentosPazSalvo(@PathVariable Integer idSolicitud) {
        return documentosConTipo(idSolicitud);
    }

    /** Igual que /obtenerDocumentos/{id}, solo para el personal (es la que usa la bandeja del coordinador). */
    @GetMapping("/obtenerDocumentos/coordinador/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerDocumentosPazSalvoCoordinador(@PathVariable Integer idSolicitud) {
        return documentosConTipo(idSolicitud);
    }

    private ResponseEntity<List<Map<String, Object>>> documentosConTipo(Integer idSolicitud) {
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        List<Map<String, Object>> documentos = new ArrayList<>();
        for (Documento documento : solicitud.getDocumentos() != null ? solicitud.getDocumentos() : List.<Documento>of()) {
            if (documento.getNombre() == null) {
                continue;
            }
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", documento.getId_documento());
            doc.put("nombre", documento.getNombre());
            doc.put("nombreArchivo", documento.getNombre());
            doc.put("ruta", documento.getRuta_documento());
            doc.put("fecha", documento.getFecha_documento());
            doc.put("esValido", documento.isEsValido());
            doc.put("comentario", documento.getComentario());
            doc.put("tipo", tipoDeDocumento(documento.getNombre()));
            documentos.add(doc);
        }
        return ResponseEntity.ok(documentos);
    }

    /** Oficio o resolución: lo que sube la secretaría (por el nombre del archivo). */
    private static boolean esOficio(String nombreArchivo) {
        String n = nombreArchivo.toLowerCase();
        return n.contains("oficio") || n.contains("resolucion") || n.contains("paz") || n.contains("salvo") || n.contains("aprobacion");
    }

    /** Tipo de documento según el nombre del archivo. */
    private static String tipoDeDocumento(String nombreArchivo) {
        String n = nombreArchivo.toLowerCase();
        if (esOficio(n)) {
            return "Oficio/Resolución";
        } else if (n.contains("formato") || n.contains("pm_fo_4_for_27")) {
            return "Formato PM-FO-4-FOR-27";
        } else if (n.contains("autorizacion") || n.contains("publicar")) {
            return "Autorización de Publicación";
        } else if (n.contains("hoja") && n.contains("vida")) {
            return "Hoja de Vida Académica";
        } else if (n.contains("comprobante") || n.contains("pago")) {
            return "Comprobante de Pago";
        } else if (n.contains("trabajo") && n.contains("grado")) {
            return "Documento de Trabajo de Grado";
        } else if (n.contains("saber") && n.contains("pro")) {
            return "Resultado Saber Pro";
        }
        return "Documento del Estudiante";
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
    public ResponseEntity<Map<String, Object>> obtenerComentariosSolicitud(@PathVariable Integer idSolicitud) {
        
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> respuesta = new HashMap<>();
        
        // 1. Buscar comentario de rechazo en los estados
        String comentarioRechazo = null;
        if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
            // Buscar el último estado de rechazo
            List<EstadoSolicitud> estadosRechazados = solicitud.getEstadosSolicitud().stream()
                .filter(estado -> "RECHAZADA".equalsIgnoreCase(estado.getEstado_actual()) || 
                                "RECHAZADO".equalsIgnoreCase(estado.getEstado_actual()))
                .sorted((a, b) -> b.getFecha_registro_estado().compareTo(a.getFecha_registro_estado()))
                .collect(java.util.stream.Collectors.toList());
            
            if (!estadosRechazados.isEmpty()) {
                EstadoSolicitud ultimoRechazo = estadosRechazados.get(0);
                if (ultimoRechazo.getComentario() != null && !ultimoRechazo.getComentario().trim().isEmpty()) {
                    comentarioRechazo = ultimoRechazo.getComentario().trim();
                }
            }
        }
        
        // Solo agregar comentario de rechazo si existe
        if (comentarioRechazo != null && !comentarioRechazo.isEmpty()) {
            respuesta.put("comentarioRechazo", comentarioRechazo);
        }
        
        // 2. Buscar documentos que tienen comentarios
        List<Map<String, Object>> documentosConComentarios = new ArrayList<>();
        if (solicitud.getDocumentos() != null && !solicitud.getDocumentos().isEmpty()) {
            for (Documento documento : solicitud.getDocumentos()) {
                if (documento.getComentario() != null && !documento.getComentario().trim().isEmpty()) {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("id", documento.getId_documento());
                    doc.put("nombre", documento.getNombre());
                    doc.put("comentario", documento.getComentario().trim());
                    documentosConComentarios.add(doc);
                }
            }
        }
        
        // Solo agregar documentos con comentarios si hay alguno
        if (!documentosConComentarios.isEmpty()) {
            respuesta.put("documentosConComentarios", documentosConComentarios);
        }
        
        // Si no hay ningún comentario, devolver objeto vacío
        if (respuesta.isEmpty()) {
            respuesta.put("mensaje", "No hay comentarios para esta solicitud");
        }
        
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener oficios disponibles para una solicitud de paz y salvo (solo oficios/resoluciones)
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerOficiosPazSalvo(@PathVariable Integer idSolicitud) {
        
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        List<Map<String, Object>> oficios = new ArrayList<>();
        for (Documento documento : documentos) {
            if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                String nombreArchivo = documento.getNombre().toLowerCase();

                if (esOficio(nombreArchivo)) {
                    Map<String, Object> oficio = new HashMap<>();
                    oficio.put("id", idSolicitud);
                    oficio.put("nombre", documento.getNombre());
                    oficio.put("nombreArchivo", documento.getNombre());
                    oficio.put("ruta", documento.getRuta_documento());
                    oficios.add(oficio);
                }
            }
        }
        
        return ResponseEntity.ok(oficios);
    }
    
    /**
     * Validar documentos requeridos para paz y salvo
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> validarDocumentosRequeridosPazSalvo(@PathVariable Integer idSolicitud) {
        
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null) {
            documentos = new ArrayList<>();
        }
        
        Map<String, Boolean> documentosRequeridos = new HashMap<>();
        documentosRequeridos.put("formato_pm_fo_4_for_27", false);
        documentosRequeridos.put("autorizacion_publicar", false);
        documentosRequeridos.put("hoja_vida_academica", false);
        documentosRequeridos.put("comprobante_pago", false);
        documentosRequeridos.put("documento_trabajo_grado", false);
        documentosRequeridos.put("resultado_saber_pro", false);
        
        for (Documento documento : documentos) {
            if (documento.getNombre() != null) {
                String nombre = documento.getNombre().toLowerCase();
                
                if (nombre.contains("formato") && nombre.contains("pm-fo-4-for-27")) {
                    documentosRequeridos.put("formato_pm_fo_4_for_27", true);
                } else if (nombre.contains("autorizacion") && nombre.contains("publicar")) {
                    documentosRequeridos.put("autorizacion_publicar", true);
                } else if (nombre.contains("hoja") && nombre.contains("vida") && nombre.contains("academica")) {
                    documentosRequeridos.put("hoja_vida_academica", true);
                } else if (nombre.contains("comprobante") && nombre.contains("pago")) {
                    documentosRequeridos.put("comprobante_pago", true);
                } else if (nombre.contains("documento") && nombre.contains("trabajo") && nombre.contains("grado")) {
                    documentosRequeridos.put("documento_trabajo_grado", true);
                } else if (nombre.contains("resultado") && nombre.contains("saber")) {
                    documentosRequeridos.put("resultado_saber_pro", true);
                }
            }
        }
        
        boolean todosCompletos = documentosRequeridos.get("formato_pm_fo_4_for_27") &&
                               documentosRequeridos.get("autorizacion_publicar") &&
                               documentosRequeridos.get("hoja_vida_academica") &&
                               documentosRequeridos.get("comprobante_pago") &&
                               documentosRequeridos.get("documento_trabajo_grado");
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("documentosRequeridos", documentosRequeridos);
        resultado.put("todosCompletos", todosCompletos);
        resultado.put("totalDocumentos", documentos.size());
        
        return ResponseEntity.ok(resultado);
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
    
        // Guardar archivo organizado en subcarpetas
        String rutaArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreOriginal, "docx", "pazysalvo", idSolicitud);
    
    // Crear documento
    Documento doc = new Documento();
    doc.setNombre(nombreOriginal);
    doc.setRuta_documento(rutaArchivo); // Guardar ruta completa con subcarpetas
    doc.setFecha_documento(new Date());
    doc.setEsValido(true);
    
    // Asociar solicitud
    SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
    if (solicitud != null) {
        Solicitud objSolicitud = new Solicitud();
        objSolicitud.setId_solicitud(idSolicitud);
        doc.setObjSolicitud(objSolicitud);
    } else {
        return ResponseEntity.notFound().build();
    }
    
    // Guardar documento en BD
    Documento documentoGuardado = documentosCU.registrarDocumento(doc);
    
    ResponseEntity<DocumentosDTORespuesta> respuesta = new ResponseEntity<>(
        documentosMapperDominio.mappearDeDocumentoADTORespuesta(documentoGuardado), 
        HttpStatus.CREATED
    );
    
    return respuesta;
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

        // Guardar archivo organizado en subcarpetas
        String rutaArchivo = objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreOriginal, "pdf", "pazysalvo", idSolicitud);

        // Registrar documento y asociar
        Documento documento = new Documento();
        documento.setNombre(nombreOriginal);
        documento.setRuta_documento(rutaArchivo); // Guardar ruta completa con subcarpetas
        documento.setFecha_documento(new Date());
        documento.setEsValido(true);

        Solicitud objSolicitud = new Solicitud();
        objSolicitud.setId_solicitud(idSolicitud);
        documento.setObjSolicitud(objSolicitud);

        Documento documentoGuardado = documentosCU.registrarDocumento(documento);

        return new ResponseEntity<>(
                documentosMapperDominio.mappearDeDocumentoADTORespuesta(documentoGuardado),
                HttpStatus.CREATED);
    }
}
