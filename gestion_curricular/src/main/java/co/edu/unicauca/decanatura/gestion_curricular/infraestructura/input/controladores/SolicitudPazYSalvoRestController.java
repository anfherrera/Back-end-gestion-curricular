package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudPazYSalvoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
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
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequestMapping("/api/solicitudes-pazysalvo")
@RequiredArgsConstructor
public class SolicitudPazYSalvoRestController {

    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final SolicitudPazYSalvoMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final DocumentosMapperDominio documentosMapperDominio;
    private final co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.DocumentGeneratorService documentGeneratorService;
    private final ObjectMapper objectMapper;
    private final GestionarUsuarioGatewayIntPort usuarioGateway;

    @PostMapping("/crearSolicitud-PazYSalvo")
    public ResponseEntity<?> crearSolicitudPazYSalvo(
            @RequestBody Object peticion) {
        try {
            if (peticion == null) {
                return respuestaBadRequest("Los datos de la solicitud son requeridos");
            }

            if (peticion instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapPeticion = (Map<String, Object>) peticion;

                if (mapPeticion.isEmpty()) {
                    return respuestaBadRequest("Los datos de la solicitud son requeridos");
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
            } else if (peticion instanceof SolicitudPazYSalvoDTOPeticion dtoPeticion) {
                String mensajeValidacion = validarSolicitudDto(dtoPeticion);
                if (mensajeValidacion != null) {
                    return respuestaBadRequest(mensajeValidacion);
                }
                return guardarSolicitudDesdeDto(dtoPeticion);
            }

            return respuestaBadRequest("Formato de solicitud no soportado");
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
                Usuario usuarioCompleto = usuarioGateway.obtenerUsuarioPorId(solicitud.getObjUsuario().getId_usuario());
                if (usuarioCompleto != null) {
                    solicitud.setObjUsuario(usuarioCompleto);
                }
            }
            
            // Generar nombre descriptivo con el nombre del estudiante si aún no está establecido correctamente
            if (solicitud.getNombre_solicitud() == null || 
                solicitud.getNombre_solicitud().trim().isEmpty() || 
                solicitud.getNombre_solicitud().equals("Paz y Salvo") ||
                solicitud.getNombre_solicitud().equals("Solicitud Paz y Salvo")) {
                String nombreSolicitud = "Solicitud Paz y Salvo";
                if (solicitud.getObjUsuario().getNombre_completo() != null && 
                    !solicitud.getObjUsuario().getNombre_completo().trim().isEmpty()) {
                    nombreSolicitud = "Solicitud Paz y Salvo - " + solicitud.getObjUsuario().getNombre_completo();
                }
                solicitud.setNombre_solicitud(nombreSolicitud);
            }
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
        Usuario usuarioCompleto = usuarioGateway.obtenerUsuarioPorId(idUsuario);
        if (usuarioCompleto == null) {
            throw new EntidadNoExisteException("Usuario no encontrado con ID: " + idUsuario);
        }

        SolicitudPazYSalvo solicitud = new SolicitudPazYSalvo();
        
        // Generar nombre descriptivo con el nombre del estudiante
        String nombreSolicitud = "Solicitud Paz y Salvo";
        if (usuarioCompleto.getNombre_completo() != null && !usuarioCompleto.getNombre_completo().trim().isEmpty()) {
            nombreSolicitud = "Solicitud Paz y Salvo - " + usuarioCompleto.getNombre_completo();
        }
        solicitud.setNombre_solicitud(nombreSolicitud);

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
        Integer idPrograma = obtenerProgramaCoordinadorAutenticado();
        
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

    /**
     * Listar solicitudes de Paz y Salvo ya procesadas por la secretaría (estado APROBADA)
     * GET /api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Secretaria/Aprobadas
     * 
     * Este endpoint permite a la secretaría ver un historial de las solicitudes 
     * que ya ha procesado y enviado al estudiante.
     */
    @GetMapping("/listarSolicitud-PazYSalvo/Secretaria/Aprobadas")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoAprobadasToSecretaria(
            @RequestParam(required = false) String periodoAcademico) {
        List<SolicitudPazYSalvo> solicitudes;
        
        // Si se solicita "todos" o está vacío/null, mostrar todas las solicitudes sin filtrar
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty() || 
            periodoAcademico.trim().equalsIgnoreCase("todos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los periodos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los períodos")) {
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToSecretaria();
        } else {
            // Filtrar por período académico específico
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToSecretariaPorPeriodo(periodoAcademico.trim());
        }
        
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Listar solicitudes de Paz y Salvo ya procesadas por el funcionario (estado APROBADA_FUNCIONARIO)
     * GET /api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Funcionario/Aprobadas
     * 
     * Este endpoint permite al funcionario ver un historial de las solicitudes 
     * que ya ha procesado y enviado al coordinador.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     */
    @GetMapping("/listarSolicitud-PazYSalvo/Funcionario/Aprobadas")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoAprobadasToFuncionario(
            @RequestParam(required = false) String periodoAcademico) {
        List<SolicitudPazYSalvo> solicitudes;
        
        // Si se solicita "todos" o está vacío/null, mostrar todas las solicitudes sin filtrar
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty() || 
            periodoAcademico.trim().equalsIgnoreCase("todos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los periodos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los períodos")) {
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToFuncionario();
        } else {
            // Filtrar por período académico específico
            solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToFuncionarioPorPeriodo(periodoAcademico.trim());
        }
        
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Listar solicitudes de Paz y Salvo ya procesadas por el coordinador (estado APROBADA_COORDINADOR)
     * GET /api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Coordinador/Aprobadas
     * 
     * Este endpoint permite al coordinador ver un historial de las solicitudes 
     * que ya ha procesado y enviado a la secretaría.
     */
    @GetMapping("/listarSolicitud-PazYSalvo/Coordinador/Aprobadas")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoAprobadasToCoordinador(
            @RequestParam(required = false) String periodoAcademico) {
        // Obtener el programa del coordinador autenticado
        Integer idPrograma = obtenerProgramaCoordinadorAutenticado();
        
        List<SolicitudPazYSalvo> solicitudes;
        
        // Si se solicita "todos" o está vacío/null, mostrar todas las solicitudes sin filtrar por período
        boolean mostrarTodos = periodoAcademico == null || periodoAcademico.trim().isEmpty() || 
            periodoAcademico.trim().equalsIgnoreCase("todos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los periodos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los períodos");
        
        if (idPrograma != null) {
            if (mostrarTodos) {
                // Filtrar solo por programa, sin período
                solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToCoordinadorPorPrograma(idPrograma);
            } else {
                // Filtrar por programa y período del coordinador
                solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico.trim());
            }
        } else {
            // Si no se puede obtener el programa, retornar todas (fallback)
            if (mostrarTodos) {
                solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToCoordinador();
            } else {
                // Filtrar solo por período si no hay programa
                solicitudes = solicitudPazYSalvoCU.listarSolicitudesAprobadasToCoordinador();
            }
        }
        
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/porRol")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPorRol(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario) {

        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudesPorRol(rol, idUsuario);

        List<SolicitudPazYSalvoDTORespuesta> respuesta =
                solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/id/{id}")
    public ResponseEntity<?> listarPazYSalvoById(@PathVariable Integer id) {
        try {
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(id);
            SolicitudPazYSalvoDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudARespuesta(solicitud);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/listarSolicitud-PazYSalvo/{id}")
    public ResponseEntity<?> listarPazYSalvoByIdOrRole(
            @PathVariable String id,
            @RequestParam(required = false) String periodoAcademico) {
        try {
            if (esIdentificadorNumerico(id)) {
                Integer idNumero = Integer.valueOf(id);
                try {
                    SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idNumero);
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

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

            CambioEstadoSolicitud solicitud = solicitudMapper
                    .mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);

            // Usar el método sobrecargado que acepta comentario
            solicitudPazYSalvoCU.cambiarEstadoSolicitud(
                solicitud.getIdSolicitud(), 
                solicitud.getNuevoEstado(),
                solicitud.getComentario()
            );

            return ResponseEntity.noContent().build();
        } catch (EntidadNoExisteException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    /**
     * Cambiar estado de solicitud por ID (alias para compatibilidad con pruebas)
     * PUT /api/solicitudes-pazysalvo/cambiarEstadoSolicitud/{id}
     */
    @PutMapping("/cambiarEstadoSolicitud/{id}")
    public ResponseEntity<Map<String, Object>> cambiarEstadoSolicitudPorId(
            @PathVariable Integer id,
            @RequestBody Map<String, String> requestBody) {
        String nuevoEstado = requestBody.get("nuevoEstado");
        if (nuevoEstado == null || nuevoEstado.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "nuevoEstado es requerido"));
        }
        
        // Obtener el comentario si está presente
        String comentario = requestBody.get("comentario");
        
        // Usar el método sobrecargado que acepta comentario
        solicitudPazYSalvoCU.cambiarEstadoSolicitud(id, nuevoEstado, comentario);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("idSolicitud", id);
        respuesta.put("nuevoEstado", nuevoEstado);
        if (comentario != null && !comentario.trim().isEmpty()) {
            respuesta.put("comentario", comentario);
        }
        respuesta.put("mensaje", "Estado actualizado exitosamente");
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Generar documento PDF de Paz y Salvo
     * GET /api/solicitudes-pazysalvo/generarDocumentoPazYSalvo/{id}/pdf
     */
    @GetMapping("/generarDocumentoPazYSalvo/{id}/pdf")
    public ResponseEntity<byte[]> generarDocumentoPazYSalvoPDF(
            @PathVariable Integer id,
            @RequestParam(value = "tituloTrabajoGrado", required = false) String tituloTrabajoGrado,
            @RequestParam(value = "directorTrabajoGrado", required = false) String directorTrabajoGrado) {
        try {
            // Buscar la solicitud
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(id);
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el usuario esté cargado
            if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null) {
                return ResponseEntity.internalServerError().build();
            }
            
            // Obtener el usuario completo desde el gateway (igual que en construirSolicitudBasica)
            // Esto asegura que tengamos todos los datos del usuario, incluyendo la cédula
            Usuario usuarioCompleto = usuarioGateway.obtenerUsuarioPorId(solicitud.getObjUsuario().getId_usuario());
            if (usuarioCompleto == null) {
                return ResponseEntity.internalServerError().build();
            }
            
            // Crear datos del documento
            Map<String, Object> datosDocumento = new HashMap<>();
            datosDocumento.put("numeroDocumento", "PYS-" + id);
            datosDocumento.put("fechaDocumento", java.time.LocalDate.now().toString());
            datosDocumento.put("observaciones", "Paz y Salvo generado");
            
            Map<String, Object> datosSolicitud = new HashMap<>();
            datosSolicitud.put("nombreEstudiante", usuarioCompleto.getNombre_completo());
            datosSolicitud.put("codigoEstudiante", usuarioCompleto.getCodigo());
            datosSolicitud.put("programa", usuarioCompleto.getObjPrograma() != null ? 
                usuarioCompleto.getObjPrograma().getNombre_programa() : "Ingeniería Electrónica y Telecomunicaciones");
            // Formatear fecha correctamente
            if (solicitud.getFecha_registro_solicitud() != null) {
                java.time.LocalDate fechaLocal = solicitud.getFecha_registro_solicitud().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                datosSolicitud.put("fechaSolicitud", fechaLocal);
            } else {
                datosSolicitud.put("fechaSolicitud", java.time.LocalDate.now());
            }
            // Agregar datos adicionales para Paz y Salvo - usar el usuario completo para obtener la cédula
            String cedulaFinal = usuarioCompleto.getCedula() != null && !usuarioCompleto.getCedula().trim().isEmpty() 
                ? usuarioCompleto.getCedula() : "No especificada";
            datosSolicitud.put("cedulaEstudiante", cedulaFinal);
            // Obtener título y director de la solicitud guardada, o de los parámetros, o usar valores por defecto
            String tituloFinal = null;
            if (tituloTrabajoGrado != null && !tituloTrabajoGrado.trim().isEmpty()) {
                tituloFinal = tituloTrabajoGrado.trim();
            } else if (solicitud.getTitulo_trabajo_grado() != null && !solicitud.getTitulo_trabajo_grado().trim().isEmpty()) {
                tituloFinal = solicitud.getTitulo_trabajo_grado().trim();
            } else {
                tituloFinal = "Trabajo de grado";
            }
            
            String directorFinal = null;
            if (directorTrabajoGrado != null && !directorTrabajoGrado.trim().isEmpty()) {
                directorFinal = directorTrabajoGrado.trim();
            } else if (solicitud.getDirector_trabajo_grado() != null && !solicitud.getDirector_trabajo_grado().trim().isEmpty()) {
                directorFinal = solicitud.getDirector_trabajo_grado().trim();
            } else {
                directorFinal = "Director asignado";
            }
            
            datosSolicitud.put("tituloTrabajoGrado", tituloFinal);
            datosSolicitud.put("directorTrabajoGrado", directorFinal);
            
            // Crear request para generador de documentos
            co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest request = 
                new co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest();
            request.setTipoDocumento("PAZ_SALVO");
            request.setDatosDocumento(datosDocumento);
            request.setDatosSolicitud(datosSolicitud);
            
            // Generar documento
            java.io.ByteArrayOutputStream documentBytes = documentGeneratorService.generarDocumento(request);
            
            String nombreArchivo = String.format("PazYSalvo_%s.pdf", id);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(documentBytes.toByteArray());
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generar documento DOCX de Paz y Salvo
     * GET /api/solicitudes-pazysalvo/generarDocumentoPazYSalvo/{id}/docx
     */
    @GetMapping("/generarDocumentoPazYSalvo/{id}/docx")
    public ResponseEntity<byte[]> generarDocumentoPazYSalvoDOCX(
            @PathVariable Integer id,
            @RequestParam(value = "tituloTrabajoGrado", required = false) String tituloTrabajoGrado,
            @RequestParam(value = "directorTrabajoGrado", required = false) String directorTrabajoGrado) {
        try {
            // Buscar la solicitud
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(id);
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el usuario esté cargado
            if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null) {
                return ResponseEntity.internalServerError().build();
            }
            
            // Obtener el usuario completo desde el gateway para asegurar que tengamos todos los datos, incluyendo la cédula
            Usuario usuarioCompleto = usuarioGateway.obtenerUsuarioPorId(solicitud.getObjUsuario().getId_usuario());
            if (usuarioCompleto == null) {
                return ResponseEntity.internalServerError().build();
            }
            
            // Crear datos del documento
            Map<String, Object> datosDocumento = new HashMap<>();
            datosDocumento.put("numeroDocumento", "PYS-" + id);
            datosDocumento.put("fechaDocumento", java.time.LocalDate.now().toString());
            datosDocumento.put("observaciones", "Paz y Salvo generado");
            
            Map<String, Object> datosSolicitud = new HashMap<>();
            datosSolicitud.put("nombreEstudiante", usuarioCompleto.getNombre_completo());
            datosSolicitud.put("codigoEstudiante", usuarioCompleto.getCodigo());
            datosSolicitud.put("programa", usuarioCompleto.getObjPrograma() != null ? 
                usuarioCompleto.getObjPrograma().getNombre_programa() : "Ingeniería Electrónica y Telecomunicaciones");
            // Formatear fecha correctamente
            if (solicitud.getFecha_registro_solicitud() != null) {
                java.time.LocalDate fechaLocal = solicitud.getFecha_registro_solicitud().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                datosSolicitud.put("fechaSolicitud", fechaLocal);
            } else {
                datosSolicitud.put("fechaSolicitud", java.time.LocalDate.now());
            }
            // Agregar datos adicionales para Paz y Salvo - usar el usuario completo para obtener la cédula
            String cedulaFinal = usuarioCompleto.getCedula() != null && !usuarioCompleto.getCedula().trim().isEmpty() 
                ? usuarioCompleto.getCedula() : "No especificada";
            datosSolicitud.put("cedulaEstudiante", cedulaFinal);
            // Obtener título y director de la solicitud guardada, o de los parámetros, o usar valores por defecto
            String tituloFinal = null;
            if (tituloTrabajoGrado != null && !tituloTrabajoGrado.trim().isEmpty()) {
                tituloFinal = tituloTrabajoGrado.trim();
            } else if (solicitud.getTitulo_trabajo_grado() != null && !solicitud.getTitulo_trabajo_grado().trim().isEmpty()) {
                tituloFinal = solicitud.getTitulo_trabajo_grado().trim();
            } else {
                tituloFinal = "Trabajo de grado";
            }
            
            String directorFinal = null;
            if (directorTrabajoGrado != null && !directorTrabajoGrado.trim().isEmpty()) {
                directorFinal = directorTrabajoGrado.trim();
            } else if (solicitud.getDirector_trabajo_grado() != null && !solicitud.getDirector_trabajo_grado().trim().isEmpty()) {
                directorFinal = solicitud.getDirector_trabajo_grado().trim();
            } else {
                directorFinal = "Director asignado";
            }
            
            datosSolicitud.put("tituloTrabajoGrado", tituloFinal);
            datosSolicitud.put("directorTrabajoGrado", directorFinal);
            
            // Crear request para generador de documentos
            co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest request = 
                new co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest();
            request.setTipoDocumento("PAZ_SALVO");
            request.setDatosDocumento(datosDocumento);
            request.setDatosSolicitud(datosSolicitud);
            
            // Generar documento
            java.io.ByteArrayOutputStream documentBytes = documentGeneratorService.generarDocumento(request);
            
            String nombreArchivo = String.format("PazYSalvo_%s.docx", id);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(documentBytes.toByteArray());
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint de prueba para verificar que el controlador funciona
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controlador de Paz y Salvo funcionando correctamente");
    }
    
    /**
     * Endpoint de diagnóstico para ver qué headers está enviando el frontend
     */
    @PostMapping("/debug-upload")
    public ResponseEntity<Map<String, Object>> debugUpload(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        Map<String, Object> debug = new HashMap<>();
        
        
        debug.put("content_type", request.getContentType());
        debug.put("method", request.getMethod());
        debug.put("content_length", request.getContentLength());
        
        Map<String, String> headers = new HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        debug.put("headers", headers);
        
        if (file != null && !file.isEmpty()) {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("nombre", file.getOriginalFilename());
            fileInfo.put("tamaño", file.getSize());
            fileInfo.put("content_type", file.getContentType());
            debug.put("archivo_recibido", fileInfo);
            
        } else {
            debug.put("archivo_recibido", "NO SE RECIBIÓ ARCHIVO");
        }
        
        return ResponseEntity.ok(debug);
    }

    /**
     * Subir archivo para paz y salvo (SIN asociar a solicitud - como en homologación)
     * Los documentos se asocian automáticamente cuando se crea la solicitud
     */
    @PostMapping("/subir-documento")
    public ResponseEntity<Map<String, Object>> subirDocumentoPazSalvo(
            @RequestParam("file") MultipartFile file) {
        try {
            String nombreOriginal = file.getOriginalFilename();
            
            // Validaciones básicas
            if (!nombreOriginal.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Solo se permiten archivos PDF"));
            }
            
            // Validar peso máximo (10MB)
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "Archivo demasiado grande. Máximo 10MB"));
            }
            
            // Guardar archivo (sin ID de solicitud, se guarda en raíz para documentos huérfanos)
            // Nota: Este endpoint es para documentos sin asociar, se mantiene en raíz
            String rutaArchivo = this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf");
            
            // Crear documento SIN asociar a solicitud (como en homologación)
            Documento doc = new Documento();
            doc.setNombre(nombreOriginal);
            doc.setRuta_documento(rutaArchivo);
            doc.setFecha_documento(new Date());
            doc.setEsValido(true);
            // NO agregar comentario automático - solo funcionarios/coordinadores pueden comentar
            // NO asociar a solicitud - esto se hace después como en homologación
            
            Documento documentoGuardado = this.objGestionarDocumentosGateway.crearDocumento(doc);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Documento subido exitosamente (sin asociar)");
            respuesta.put("documento_id", documentoGuardado.getId_documento());
            respuesta.put("nombre", nombreOriginal);
            respuesta.put("fecha", documentoGuardado.getFecha_documento());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.ok(errorInfo);
        }
    }

    /**
     * Descargar documento específico por nombre
     * Busca el documento en la BD para obtener su ruta completa (incluye subcarpetas)
     */
    @GetMapping("/descargar-documento")
    public ResponseEntity<byte[]> descargarDocumento(@RequestParam("filename") String filename) {
        try {
            
            // Buscar el documento en la BD por nombre para obtener su ruta completa
            String rutaArchivo = filename; // Por defecto usar el nombre recibido
            
            // Buscar en todas las solicitudes de paz y salvo
            List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudes();
            for (SolicitudPazYSalvo solicitud : solicitudes) {
                if (solicitud.getDocumentos() != null) {
                    for (Documento doc : solicitud.getDocumentos()) {
                        // Comparar por nombre (sin ruta)
                        String nombreDoc = doc.getNombre();
                        if (nombreDoc != null && nombreDoc.equals(filename)) {
                            // Usar la ruta completa si está disponible
                            if (doc.getRuta_documento() != null && !doc.getRuta_documento().isEmpty()) {
                                rutaArchivo = doc.getRuta_documento();
                            }
                            break;
                        }
                    }
                }
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
            
            // Configurar Content-Disposition con filename y filename* (UTF-8)
            String encoded = java.net.URLEncoder.encode(nombreArchivo, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
            String contentDisposition = "attachment; filename=\"" + nombreArchivo + "\"; filename*=UTF-8''" + encoded;

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Generar documento de paz y salvo usando plantilla (igual que homologación)
     */
    @PostMapping("/generar-documento/{idSolicitud}")
    public ResponseEntity<byte[]> generarDocumentoPazSalvo(
            @PathVariable Integer idSolicitud,
            @RequestParam("numeroDocumento") String numeroDocumento,
            @RequestParam("fechaDocumento") String fechaDocumento,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "cedulaEstudiante", required = false) String cedulaEstudiante,
            @RequestParam(value = "tituloTrabajoGrado", required = false) String tituloTrabajoGrado,
            @RequestParam(value = "directorTrabajoGrado", required = false) String directorTrabajoGrado) {
        try {
            // Obtener la solicitud
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el usuario esté cargado
            if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null) {
                return ResponseEntity.internalServerError().build();
            }
            
            // Obtener el usuario completo desde el gateway para asegurar que tengamos todos los datos, incluyendo la cédula
            Usuario usuarioCompleto = usuarioGateway.obtenerUsuarioPorId(solicitud.getObjUsuario().getId_usuario());
            if (usuarioCompleto == null) {
                return ResponseEntity.internalServerError().build();
            }
            
            
            // Crear request para el generador de documentos (igual que homologación)
            Map<String, Object> datosDocumento = new HashMap<>();
            datosDocumento.put("numeroDocumento", numeroDocumento);
            // Asegurar que la fecha se pase como string en formato YYYY-MM-DD
            String fechaDocumentoStr = null;
            if (fechaDocumento != null && !fechaDocumento.trim().isEmpty()) {
                fechaDocumentoStr = fechaDocumento.trim();
                if (fechaDocumentoStr.contains("T")) {
                    // Si viene con hora, tomar solo la fecha (YYYY-MM-DD)
                    fechaDocumentoStr = fechaDocumentoStr.substring(0, 10);
                }
                // Validar que tenga el formato correcto YYYY-MM-DD
                if (!fechaDocumentoStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    fechaDocumentoStr = null;
                }
            }
            // Si no se proporcionó fecha o es inválida, usar la fecha actual del sistema (sin zona horaria)
            if (fechaDocumentoStr == null) {
                fechaDocumentoStr = java.time.LocalDate.now().toString();
            }
            datosDocumento.put("fechaDocumento", fechaDocumentoStr);
            datosDocumento.put("observaciones", observaciones != null ? observaciones : "");
            
            Map<String, Object> datosSolicitud = new HashMap<>();
            datosSolicitud.put("nombreEstudiante", usuarioCompleto.getNombre_completo());
            datosSolicitud.put("codigoEstudiante", usuarioCompleto.getCodigo());
            datosSolicitud.put("programa", usuarioCompleto.getObjPrograma() != null ? 
                usuarioCompleto.getObjPrograma().getNombre_programa() : "Ingeniería Electrónica y Telecomunicaciones");
            datosSolicitud.put("fechaSolicitud", solicitud.getFecha_registro_solicitud());
            // Usar la cédula del usuario completo, o el parámetro si se proporciona, o "No especificada" como último recurso
            String cedulaFinal = cedulaEstudiante != null && !cedulaEstudiante.trim().isEmpty() 
                ? cedulaEstudiante 
                : (usuarioCompleto.getCedula() != null && !usuarioCompleto.getCedula().trim().isEmpty() 
                    ? usuarioCompleto.getCedula() 
                    : "No especificada");
            datosSolicitud.put("cedulaEstudiante", cedulaFinal);
            // Obtener título y director de la solicitud guardada, o de los parámetros, o usar valores por defecto
            String tituloFinal = null;
            if (tituloTrabajoGrado != null && !tituloTrabajoGrado.trim().isEmpty()) {
                tituloFinal = tituloTrabajoGrado.trim();
            } else if (solicitud.getTitulo_trabajo_grado() != null && !solicitud.getTitulo_trabajo_grado().trim().isEmpty()) {
                tituloFinal = solicitud.getTitulo_trabajo_grado().trim();
            } else {
                tituloFinal = "Trabajo de grado";
            }
            
            String directorFinal = null;
            if (directorTrabajoGrado != null && !directorTrabajoGrado.trim().isEmpty()) {
                directorFinal = directorTrabajoGrado.trim();
            } else if (solicitud.getDirector_trabajo_grado() != null && !solicitud.getDirector_trabajo_grado().trim().isEmpty()) {
                directorFinal = solicitud.getDirector_trabajo_grado().trim();
            } else {
                directorFinal = "Director asignado";
            }
            
            datosSolicitud.put("tituloTrabajoGrado", tituloFinal);
            datosSolicitud.put("directorTrabajoGrado", directorFinal);
            
            
            // Crear el request (igual que homologación)
            co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest request = 
                new co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest();
            request.setTipoDocumento("PAZ_SALVO");
            request.setDatosDocumento(datosDocumento);
            request.setDatosSolicitud(datosSolicitud);
            
            // Generar documento usando el servicio (igual que homologación)
            java.io.ByteArrayOutputStream documentBytes = documentGeneratorService.generarDocumento(request);
            
            // Generar nombre del archivo (igual que homologación)
            String nombreEstudiante = solicitud.getObjUsuario().getNombre_completo();
            String nombreLimpio = nombreEstudiante.replaceAll("[^a-zA-Z0-9]", "_");
            String nombreArchivo = String.format("PAZ_SALVO_%s_%s.docx", nombreLimpio, numeroDocumento);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(documentBytes.toByteArray());
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener plantillas disponibles para paz y salvo (usa el servicio real como homologación)
     */
    @GetMapping("/plantillas-disponibles")
    public ResponseEntity<?> obtenerPlantillasDisponibles() {
        try {
            // Usar el servicio real igual que homologación
            List<?> plantillas = documentGeneratorService.getTemplates("paz-salvo");
            
            return ResponseEntity.ok(plantillas);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Descargar oficio por ID de solicitud de paz y salvo
     */
    @GetMapping("/descargarOficio/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficioPazSalvo(@PathVariable Integer idSolicitud) {
        try {
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
                    
                    // Filtrar solo archivos que parecen ser oficios/resoluciones
                    boolean esOficio = nombreArchivo.contains("oficio") || 
                                     nombreArchivo.contains("resolucion") || 
                                     nombreArchivo.contains("paz") ||
                                     nombreArchivo.contains("salvo") ||
                                     nombreArchivo.contains("aprobacion");
                    
                    if (esOficio) {
                        try {
                            // Usar getFile que maneja tanto rutas simples como rutas con subcarpetas
                            byte[] archivo = objGestionarArchivos.getFile(
                                documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre()
                            );
                            
                            // Configurar el header Content-Disposition con filename y filename* (UTF-8)
                            String original = documento.getNombre();
                            String encoded = java.net.URLEncoder.encode(original, java.nio.charset.StandardCharsets.UTF_8)
                                .replace("+", "%20");
                            String contentDisposition = "attachment; filename=\"" + original + "\"; filename*=UTF-8''" + encoded;

                            return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(archivo);
                                
                        } catch (Exception e) {
                            continue; // Probar el siguiente documento
                        }
                    }
                }
            }
            
            return ResponseEntity.notFound().build();
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener TODOS los documentos de una solicitud de paz y salvo (incluyendo los del estudiante)
     * Para funcionarios
     */
    @GetMapping("/obtenerDocumentos/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerDocumentosPazSalvo(@PathVariable Integer idSolicitud) {
        try {
            // Obtener la solicitud con sus documentos
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Buscar documentos asociados a esta solicitud
            List<Documento> documentos = solicitud.getDocumentos();
            
            if (documentos == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            if (documentos.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>()); // Retornar lista vacía
            }
            
            // Crear lista con TODOS los documentos (incluyendo los del estudiante)
            List<Map<String, Object>> todosDocumentos = new ArrayList<>();
            for (Documento documento : documentos) {
                if (documento.getNombre() != null) {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("id", documento.getId_documento());
                    doc.put("nombre", documento.getNombre());
                    doc.put("nombreArchivo", documento.getNombre());
                    doc.put("ruta", documento.getRuta_documento());
                    doc.put("fecha", documento.getFecha_documento());
                    doc.put("esValido", documento.isEsValido());
                    doc.put("comentario", documento.getComentario());
                    
                    // Determinar el tipo de documento
                    String nombreArchivo = documento.getNombre().toLowerCase();
                    String tipoDocumento = "Documento del Estudiante";
                    
                    if (nombreArchivo.contains("oficio") || nombreArchivo.contains("resolucion") || 
                        nombreArchivo.contains("paz") || nombreArchivo.contains("salvo") || 
                        nombreArchivo.contains("aprobacion")) {
                        tipoDocumento = "Oficio/Resolución";
                    } else if (nombreArchivo.contains("formato") || nombreArchivo.contains("pm_fo_4_for_27")) {
                        tipoDocumento = "Formato PM-FO-4-FOR-27";
                    } else if (nombreArchivo.contains("autorizacion") || nombreArchivo.contains("publicar")) {
                        tipoDocumento = "Autorización de Publicación";
                    } else if (nombreArchivo.contains("hoja") && nombreArchivo.contains("vida")) {
                        tipoDocumento = "Hoja de Vida Académica";
                    } else if (nombreArchivo.contains("comprobante") || nombreArchivo.contains("pago")) {
                        tipoDocumento = "Comprobante de Pago";
                    } else if (nombreArchivo.contains("trabajo") && nombreArchivo.contains("grado")) {
                        tipoDocumento = "Documento de Trabajo de Grado";
                    } else if (nombreArchivo.contains("saber") && nombreArchivo.contains("pro")) {
                        tipoDocumento = "Resultado Saber Pro";
                    }
                    
                    doc.put("tipo", tipoDocumento);
                    todosDocumentos.add(doc);
                }
            }
            
            return ResponseEntity.ok(todosDocumentos);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint de debugging para verificar datos en la base de datos
     */
    @GetMapping("/debug/documentos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> debugDocumentos(@PathVariable Integer idSolicitud) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Verificar si la solicitud existe
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                debugInfo.put("error", "Solicitud no encontrada");
                debugInfo.put("solicitud_existe", false);
                return ResponseEntity.ok(debugInfo);
            }
            
            debugInfo.put("solicitud_existe", true);
            debugInfo.put("solicitud_id", solicitud.getId_solicitud());
            debugInfo.put("solicitud_nombre", solicitud.getNombre_solicitud());
            debugInfo.put("solicitud_fecha", solicitud.getFecha_registro_solicitud());
            
            // Verificar documentos
            List<Documento> documentos = solicitud.getDocumentos();
            debugInfo.put("documentos_es_null", documentos == null);
            debugInfo.put("documentos_cantidad", documentos != null ? documentos.size() : 0);
            
            if (documentos != null && !documentos.isEmpty()) {
                List<Map<String, Object>> docsInfo = new ArrayList<>();
                for (Documento doc : documentos) {
                    Map<String, Object> docInfo = new HashMap<>();
                    docInfo.put("id", doc.getId_documento());
                    docInfo.put("nombre", doc.getNombre());
                    docInfo.put("ruta", doc.getRuta_documento());
                    docInfo.put("fecha", doc.getFecha_documento());
                    docInfo.put("esValido", doc.isEsValido());
                    docsInfo.add(docInfo);
                }
                debugInfo.put("documentos_detalle", docsInfo);
            }
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("stack_trace", e.getStackTrace());
            return ResponseEntity.ok(errorInfo);
        }
    }

    /**
     * Endpoint para verificar y corregir documentos sin asociar
     */
    @GetMapping("/debug/documentos-sin-asociar")
    public ResponseEntity<Map<String, Object>> verificarDocumentosSinAsociar() {
        try {
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Buscar documentos sin solicitud asociada
            List<Documento> documentosSinSolicitud = objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
            
            resultado.put("documentos_sin_asociar", documentosSinSolicitud.size());
            
            if (!documentosSinSolicitud.isEmpty()) {
                List<Map<String, Object>> docsInfo = new ArrayList<>();
                for (Documento doc : documentosSinSolicitud) {
                    Map<String, Object> docInfo = new HashMap<>();
                    docInfo.put("id", doc.getId_documento());
                    docInfo.put("nombre", doc.getNombre());
                    docInfo.put("ruta", doc.getRuta_documento());
                    docInfo.put("fecha", doc.getFecha_documento());
                    docsInfo.add(docInfo);
                }
                resultado.put("documentos_detalle", docsInfo);
            }
            
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.ok(errorInfo);
        }
    }

    /**
     * Endpoint para asociar documentos huérfanos a una solicitud
     */
    @PostMapping("/asociar-documentos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> asociarDocumentosHuérfanos(@PathVariable Integer idSolicitud) {
        try {
            
            Map<String, Object> resultado = new HashMap<>();
            
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                resultado.put("error", "Solicitud no encontrada");
                return ResponseEntity.ok(resultado);
            }
            
            List<Documento> documentosSinSolicitud = objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
            
            int documentosAsociados = 0;
            for (Documento doc : documentosSinSolicitud) {
                doc.setObjSolicitud(solicitud);
                objGestionarDocumentosGateway.actualizarDocumento(doc);
                documentosAsociados++;
            }
            
            resultado.put("documentos_asociados", documentosAsociados);
            resultado.put("solicitud_id", idSolicitud);
            resultado.put("mensaje", "Documentos asociados exitosamente");
            
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.ok(errorInfo);
        }
    }


    /**
     * Endpoint para asociar documentos huérfanos a una solicitud específica (como en homologación)
     */
    @PostMapping("/asociar-documentos-huerfanos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> asociarDocumentosHuerfanos(@PathVariable Integer idSolicitud) {
        try {
            
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Solicitud ID " + idSolicitud + " no encontrada");
                return ResponseEntity.ok(error);
            }
            
            List<Documento> documentosSinSolicitud = objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
            
            
            int documentosAsociados = 0;
            for (Documento doc : documentosSinSolicitud) {
                doc.setObjSolicitud(solicitud);
                objGestionarDocumentosGateway.actualizarDocumento(doc);
                documentosAsociados++;
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("documentos_asociados", documentosAsociados);
            resultado.put("solicitud_id", idSolicitud);
            resultado.put("mensaje", "Documentos asociados exitosamente");
            
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.ok(errorInfo);
        }
    }

    /**
     * Obtener TODOS los documentos de una solicitud de paz y salvo (incluyendo los del estudiante)
     * Para coordinadores
     */
    @GetMapping("/obtenerDocumentos/coordinador/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerDocumentosPazSalvoCoordinador(@PathVariable Integer idSolicitud) {
        try {
            
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<Documento> documentos = solicitud.getDocumentos();
            if (documentos == null || documentos.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            List<Map<String, Object>> todosDocumentos = new ArrayList<>();
            for (Documento documento : documentos) {
                if (documento.getNombre() != null) {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("id", documento.getId_documento());
                    doc.put("nombre", documento.getNombre());
                    doc.put("nombreArchivo", documento.getNombre());
                    doc.put("ruta", documento.getRuta_documento());
                    doc.put("fecha", documento.getFecha_documento());
                    doc.put("esValido", documento.isEsValido());
                    doc.put("comentario", documento.getComentario());
                    
                    String nombreArchivo = documento.getNombre().toLowerCase();
                    String tipoDocumento = "Documento del Estudiante";
                    
                    if (nombreArchivo.contains("oficio") || nombreArchivo.contains("resolucion") || 
                        nombreArchivo.contains("paz") || nombreArchivo.contains("salvo") || 
                        nombreArchivo.contains("aprobacion")) {
                        tipoDocumento = "Oficio/Resolución";
                    } else if (nombreArchivo.contains("formato") || nombreArchivo.contains("pm_fo_4_for_27")) {
                        tipoDocumento = "Formato PM-FO-4-FOR-27";
                    } else if (nombreArchivo.contains("autorizacion") || nombreArchivo.contains("publicar")) {
                        tipoDocumento = "Autorización de Publicación";
                    } else if (nombreArchivo.contains("hoja") && nombreArchivo.contains("vida")) {
                        tipoDocumento = "Hoja de Vida Académica";
                    } else if (nombreArchivo.contains("comprobante") || nombreArchivo.contains("pago")) {
                        tipoDocumento = "Comprobante de Pago";
                    } else if (nombreArchivo.contains("trabajo") && nombreArchivo.contains("grado")) {
                        tipoDocumento = "Documento de Trabajo de Grado";
                    } else if (nombreArchivo.contains("saber") && nombreArchivo.contains("pro")) {
                        tipoDocumento = "Resultado Saber Pro";
                    }
                    
                    doc.put("tipo", tipoDocumento);
                    todosDocumentos.add(doc);
                }
            }
            
            return ResponseEntity.ok(todosDocumentos);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener solo los comentarios existentes de una solicitud de paz y salvo
     * Devuelve:
     * - Comentario de rechazo (si existe)
     * - Documentos que tienen comentarios (solo los que tienen comentario)
     * GET /api/solicitudes-pazysalvo/obtenerComentarios/{idSolicitud}
     */
    @GetMapping("/obtenerComentarios/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> obtenerComentariosSolicitud(@PathVariable Integer idSolicitud) {
        try {
            
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener oficios disponibles para una solicitud de paz y salvo (solo oficios/resoluciones)
     */
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerOficiosPazSalvo(@PathVariable Integer idSolicitud) {
        try {
            
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
                    
                    boolean esOficio = nombreArchivo.contains("oficio") || 
                                     nombreArchivo.contains("resolucion") || 
                                     nombreArchivo.contains("paz") ||
                                     nombreArchivo.contains("salvo") ||
                                     nombreArchivo.contains("aprobacion");
                    
                    if (esOficio) {
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
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Validar documentos requeridos para paz y salvo
     */
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> validarDocumentosRequeridosPazSalvo(@PathVariable Integer idSolicitud) {
        try {
            
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
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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
    
    try {
        
        String nombreOriginal = file.getOriginalFilename();
        
        // Validaciones
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        
        if (!nombreOriginal.toLowerCase().endsWith(".docx")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null);
        }
        
            // Guardar archivo organizado en subcarpetas
            String rutaArchivo = this.objGestionarArchivos.saveFile(file, nombreOriginal, "docx", "pazysalvo", idSolicitud);
        
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
        Documento documentoGuardado = this.objGestionarDocumentosGateway.crearDocumento(doc);
        
        ResponseEntity<DocumentosDTORespuesta> respuesta = new ResponseEntity<>(
            documentosMapperDominio.mappearDeDocumentoADTORespuesta(documentoGuardado), 
            HttpStatus.CREATED
        );
        
        return respuesta;
        
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    /**
     * Subir oficio PDF de Secretaría y asociarlo a una solicitud de paz y salvo
     */
    @PostMapping("/subir-oficio-pdf/{idSolicitud}")
    public ResponseEntity<DocumentosDTORespuesta> subirOficioPdfPazYSalvo(
            @PathVariable Integer idSolicitud,
            @RequestParam("file") MultipartFile file) {
        try {

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String nombreOriginal = file.getOriginalFilename();
            if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }

            // Validar que la solicitud exista
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }

            // Guardar archivo organizado en subcarpetas
            String rutaArchivo = objGestionarArchivos.saveFile(file, nombreOriginal, "pdf", "pazysalvo", idSolicitud);

            // Registrar documento y asociar
            Documento documento = new Documento();
            documento.setNombre(nombreOriginal);
            documento.setRuta_documento(rutaArchivo); // Guardar ruta completa con subcarpetas
            documento.setFecha_documento(new Date());
            documento.setEsValido(true);

            Solicitud objSolicitud = new Solicitud();
            objSolicitud.setId_solicitud(idSolicitud);
            documento.setObjSolicitud(objSolicitud);

            Documento documentoGuardado = objGestionarDocumentosGateway.crearDocumento(documento);

            return new ResponseEntity<>(
                    documentosMapperDominio.mappearDeDocumentoADTORespuesta(documentoGuardado),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el ID del programa académico del coordinador autenticado.
     * @return ID del programa o null si no se puede obtener
     */
    private Integer obtenerProgramaCoordinadorAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String email = authentication.getName();
            Usuario usuarioAutenticado = usuarioGateway.buscarUsuarioPorCorreo(email);
            
            if (usuarioAutenticado == null) {
                return null;
            }

            // Verificar que sea coordinador
            String rolNombre = usuarioAutenticado.getObjRol() != null 
                    ? usuarioAutenticado.getObjRol().getNombre() 
                    : null;
            
            if (!"Coordinador".equals(rolNombre)) {
                return null;
            }

            // Obtener el programa del coordinador
            if (usuarioAutenticado.getObjPrograma() != null && usuarioAutenticado.getObjPrograma().getId_programa() != null) {
                return usuarioAutenticado.getObjPrograma().getId_programa();
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
