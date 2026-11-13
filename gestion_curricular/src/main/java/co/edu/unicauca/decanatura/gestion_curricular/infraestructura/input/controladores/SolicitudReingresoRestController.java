package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorCambioEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudReingresoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudReingresoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudReingresoMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UsuarioDTOPeticion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/solicitudes-reingreso")   
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
public class SolicitudReingresoRestController {

    private final SolicitudReingresoMapperDominio solicitudReingresoMapper;
    private final GestionarSolicitudReingresoCUIntPort solicitudService;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final Autorizacion autorizacion;
    private final ValidadorCambioEstado validadorCambioEstado;

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping("/crearSolicitud-Reingreso")
    public ResponseEntity<SolicitudReingresoDTORespuesta> crearSolicitudReingreso(@RequestBody SolicitudReingresoDTOPeticion solicitudDTO) {
        // El solicitante sale del token, no de lo que envía el cliente
        if (solicitudDTO.getObjUsuario() == null) {
            solicitudDTO.setObjUsuario(new UsuarioDTOPeticion());
        }
        solicitudDTO.getObjUsuario().setId_usuario(autorizacion.idSolicitante(solicitudDTO.getObjUsuario().getId_usuario()));
        SolicitudReingreso solicitud = solicitudReingresoMapper.mappearDeSolicitudReingresoDTOPeticionASolicitudReingreso(solicitudDTO);
        SolicitudReingreso solicitudCreada = solicitudService.crearSolicitudReingreso(solicitud);
        ResponseEntity<SolicitudReingresoDTORespuesta> respuesta = new ResponseEntity<>
        (solicitudReingresoMapper.mappearDeSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    @GetMapping("/listarSolicitudes-Reingreso")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudesReingreso() {
        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesReingreso();
        List<SolicitudReingresoDTORespuesta> solicitudesDTO = solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);
        return ResponseEntity.ok(solicitudesDTO);
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listarSolicitud-Reingreso/porUser")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudPorUser(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String periodoAcademico) {

        // Quien no es staff solo puede ver sus propias solicitudes
        if (!autorizacion.esStaff()) {
            rol = "ESTUDIANTE";
            idUsuario = autorizacion.idUsuarioActual();
            if (idUsuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        String periodo = PeriodoDeConsulta.oActual(periodoAcademico);
        if ("ESTUDIANTE".equals(rol) && idUsuario != null && periodo != null) {
            return responder(solicitudService.listar(FiltroSolicitudes.deEstudiante(idUsuario, periodo)));
        }
        return responder(solicitudService.listarSolicitudesReingresoPorRol(rol, idUsuario));
    }

    @GetMapping("/listarSolicitud-Reingreso/Funcionario")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoToFuncionario(
            @RequestParam(required = false) String periodoAcademico) {
        return responder(solicitudService.listar(FiltroSolicitudes.pendientesDe(
                FlujoEstadosSolicitud.FUNCIONARIO, null, PeriodoDeConsulta.oActual(periodoAcademico))));
    }

    @GetMapping("/listarSolicitud-Reingreso/Coordinador")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoToCoordinador(
            @RequestParam(required = false) String periodoAcademico) {
        Integer idPrograma = autorizacion.idProgramaCoordinadorActual();
        if (idPrograma == null) {
            // Si no se puede obtener el programa, se devuelven todas, de todos los períodos (fallback)
            log.warn("No se pudo obtener el programa del coordinador, retornando todas las solicitudes");
            return responder(solicitudService.listar(
                    FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.COORDINADOR, null, null)));
        }
        return responder(solicitudService.listar(FiltroSolicitudes.pendientesDe(
                FlujoEstadosSolicitud.COORDINADOR, idPrograma, PeriodoDeConsulta.oActual(periodoAcademico))));
    }

    @GetMapping("/listarSolicitud-Reingreso/Secretaria")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoToSecretaria(
            @RequestParam(required = false) String periodoAcademico) {
        return responder(solicitudService.listar(FiltroSolicitudes.pendientesDe(
                FlujoEstadosSolicitud.SECRETARIO, null, PeriodoDeConsulta.oActual(periodoAcademico))));
    }

    /**
     * Solicitudes que la secretaría ya aprobó (pasaron por APROBADA).
     * @param periodoAcademico opcional; vacío o "todos" muestra todos los períodos
     */
    @GetMapping("/listarSolicitud-Reingreso/Secretaria/Aprobadas")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoAprobadasToSecretaria(
            @RequestParam(required = false) String periodoAcademico) {
        return responder(solicitudService.listar(FiltroSolicitudes.procesadasPor(
                FlujoEstadosSolicitud.SECRETARIO, null, PeriodoDeConsulta.oTodos(periodoAcademico))));
    }

    /**
     * Solicitudes que el funcionario ya procesó y envió al coordinador (pasaron por APROBADA_FUNCIONARIO).
     * @param periodoAcademico opcional ("2025-2"); vacío o "todos" muestra todos los períodos
     */
    @GetMapping("/listarSolicitud-Reingreso/Funcionario/Aprobadas")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoAprobadasToFuncionario(
            @RequestParam(required = false) String periodoAcademico) {
        return responder(solicitudService.listar(FiltroSolicitudes.procesadasPor(
                FlujoEstadosSolicitud.FUNCIONARIO, null, PeriodoDeConsulta.oTodos(periodoAcademico))));
    }

    /**
     * Solicitudes del programa del coordinador que él ya procesó y envió a la secretaría
     * (pasaron por APROBADA_COORDINADOR).
     * @param periodoAcademico opcional ("2025-2"); vacío o "todos" muestra todos los períodos
     */
    @GetMapping("/listarSolicitud-Reingreso/Coordinador/Aprobadas")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoAprobadasToCoordinador(
            @RequestParam(required = false) String periodoAcademico) {
        Integer idPrograma = autorizacion.idProgramaCoordinadorActual();
        if (idPrograma == null) {
            // Si no se puede obtener el programa, se devuelven todas, de todos los períodos (fallback)
            log.warn("No se pudo obtener el programa del coordinador, retornando todas las solicitudes");
            return responder(solicitudService.listar(
                    FiltroSolicitudes.procesadasPor(FlujoEstadosSolicitud.COORDINADOR, null, null)));
        }
        return responder(solicitudService.listar(FiltroSolicitudes.procesadasPor(
                FlujoEstadosSolicitud.COORDINADOR, idPrograma, PeriodoDeConsulta.oTodos(periodoAcademico))));
    }

    private ResponseEntity<List<SolicitudReingresoDTORespuesta>> responder(List<SolicitudReingreso> solicitudes) {
        return ResponseEntity.ok(
                solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes));
    }

    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @GetMapping("/listarSolicitud-Reingreo/{id}")
    public ResponseEntity<SolicitudReingresoDTORespuesta> listarReingresoById(@PathVariable Integer id) {
        SolicitudReingreso solicitud = solicitudService.obtenerSolicitudReingresoPorId(id);
        SolicitudReingresoDTORespuesta respuesta = solicitudReingresoMapper.mappearDeSolicitudReingresoASolicitudReingresoDTORespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud-Reingreso")
    public ResponseEntity<Map<String, Object>> actualizarEstadoSolicitudReingreso(@RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        // Solo el rol responsable del paso puede hacerlo (Funcionario → Coordinador → Secretario)
        String estadoActual = FlujoEstadosSolicitud.ultimoEstado(
                solicitudService.obtenerSolicitudReingresoPorId(solicitud.getIdSolicitud()).getEstadosSolicitud());
        ResponseEntity<Map<String, Object>> rechazo = validadorCambioEstado.validar(
                FlujoEstadosSolicitud.TRES_APROBACIONES, estadoActual, solicitud.getNuevoEstado());
        if (rechazo != null) {
            return rechazo;
        }
        solicitudService.cambiarEstadoSolicitudReingreso(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    // ================================
    // Endpoints para oficios/resoluciones de reingreso
    // ================================

    /**
     * Descargar oficio por ID de solicitud de reingreso
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/descargarOficio/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficio(@PathVariable Integer idSolicitud) {
        log.debug("Descargando oficio de reingreso para solicitud: {}", idSolicitud);
        
        // Obtener la solicitud con sus documentos
        SolicitudReingreso solicitud = solicitudService.obtenerSolicitudReingresoPorId(idSolicitud);
        if (solicitud == null) {
            log.warn("Solicitud de reingreso no encontrada: {}", idSolicitud);
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos asociados a esta solicitud
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            log.warn("No hay documentos asociados a la solicitud de reingreso: {}", idSolicitud);
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos que sean oficios/resoluciones (subidos por secretaria)
        for (Documento documento : documentos) {
            if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                String nombreArchivo = documento.getNombre().toLowerCase();
                
                // Filtrar solo archivos que parecen ser oficios/resoluciones de reingreso
                boolean esOficio = nombreArchivo.contains("oficio") || 
                                 nombreArchivo.contains("resolucion") || 
                                 nombreArchivo.contains("reingreso") ||
                                 nombreArchivo.contains("aprobacion");
                
                if (esOficio) {
                    try {
                        log.debug("Probando oficio/resolución de reingreso: {}", documento.getNombre());
                        
                        // Lógica adaptativa: usar ruta completa si está organizada, sino usar nombre
                        String rutaDocumento = documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre();
                        byte[] archivo;
                        
                        if (rutaDocumento != null && rutaDocumento.contains("/")) {
                            // Ruta organizada (nueva estructura)
                            archivo = objGestionarArchivos.getFileByPath(rutaDocumento);
                        } else {
                            // Ruta simple (compatibilidad hacia atrás)
                            archivo = objGestionarArchivos.getFile(documento.getNombre());
                        }
                        
                        log.debug("Oficio/resolución de reingreso encontrado: {}, tamaño: {} bytes", 
                            documento.getNombre(), archivo.length);
                        
                        // Configurar el header Content-Disposition correctamente
                        String contentDisposition = RespuestasArchivo.adjunto(documento.getNombre());
                        log.debug("Content-Disposition: {}", contentDisposition);
                        
                        return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(archivo);
                            
                    } catch (Exception e) {
                        log.warn("No encontrado: {}", documento.getNombre());
                        continue; // Probar el siguiente documento
                    }
                } else {
                    log.debug("Saltando archivo del estudiante: {}", documento.getNombre());
                }
            }
        }
        
        log.warn("No se encontró ningún archivo PDF para la solicitud de reingreso: {}", idSolicitud);
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Obtener oficios disponibles para una solicitud de reingreso
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerOficios(@PathVariable Integer idSolicitud) {
        log.debug("Obteniendo oficios de reingreso para solicitud: {}", idSolicitud);
        
        // Obtener la solicitud con sus documentos
        SolicitudReingreso solicitud = solicitudService.obtenerSolicitudReingresoPorId(idSolicitud);
        if (solicitud == null) {
            log.warn("Solicitud de reingreso no encontrada: {}", idSolicitud);
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos asociados a esta solicitud
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            log.warn("No hay documentos asociados a la solicitud de reingreso: {}", idSolicitud);
            return ResponseEntity.ok(new ArrayList<>()); // Retornar lista vacía
        }
        
        // Crear lista de oficios basada en los documentos reales (solo oficios/resoluciones)
        List<Map<String, Object>> oficios = new ArrayList<>();
        for (Documento documento : documentos) {
            if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                String nombreArchivo = documento.getNombre().toLowerCase();
                
                // Filtrar solo archivos que parecen ser oficios/resoluciones de reingreso
                boolean esOficio = nombreArchivo.contains("oficio") || 
                                 nombreArchivo.contains("resolucion") || 
                                 nombreArchivo.contains("reingreso") ||
                                 nombreArchivo.contains("aprobacion");
                
                if (esOficio) {
                    Map<String, Object> oficio = new HashMap<>();
                    oficio.put("id", idSolicitud);
                    oficio.put("nombre", documento.getNombre());
                    oficio.put("nombreArchivo", documento.getNombre());
                    oficio.put("ruta", documento.getRuta_documento());
                    oficios.add(oficio);
                    log.debug("Agregando oficio/resolución de reingreso: {}", documento.getNombre());
                } else {
                    log.debug("Saltando archivo del estudiante: {}", documento.getNombre());
                }
            }
        }
        
        log.debug("Oficios de reingreso encontrados: {}", oficios.size());
        return ResponseEntity.ok(oficios);
    }
    
    /**
     * Validar documentos requeridos para reingreso
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> validarDocumentosRequeridos(@PathVariable Integer idSolicitud) {
        log.debug("Validando documentos requeridos para solicitud de reingreso: {}", idSolicitud);
        
        // Obtener la solicitud con sus documentos
        SolicitudReingreso solicitud = solicitudService.obtenerSolicitudReingresoPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null) {
            documentos = new ArrayList<>();
        }
        
        // Documentos requeridos para reingreso
        Map<String, Boolean> documentosRequeridos = new HashMap<>();
        documentosRequeridos.put("carta_solicitud_reingreso", false);
        documentosRequeridos.put("certificado_notas", false);
        documentosRequeridos.put("documento_identidad", false);
        documentosRequeridos.put("justificacion_ausencia", false);
        documentosRequeridos.put("carnet_estudiantil", false); // Opcional
        
        // Verificar qué documentos están presentes
        for (Documento documento : documentos) {
            if (documento.getNombre() != null) {
                String nombre = documento.getNombre().toLowerCase();
                
                if (nombre.contains("carta") && nombre.contains("solicitud") && nombre.contains("reingreso")) {
                    documentosRequeridos.put("carta_solicitud_reingreso", true);
                } else if (nombre.contains("certificado") && nombre.contains("notas")) {
                    documentosRequeridos.put("certificado_notas", true);
                } else if (nombre.contains("documento") && nombre.contains("identidad")) {
                    documentosRequeridos.put("documento_identidad", true);
                } else if (nombre.contains("justificacion") && nombre.contains("ausencia")) {
                    documentosRequeridos.put("justificacion_ausencia", true);
                } else if (nombre.contains("carnet") && nombre.contains("estudiantil")) {
                    documentosRequeridos.put("carnet_estudiantil", true);
                }
            }
        }
        
        // Calcular si todos los documentos obligatorios están presentes
        boolean todosCompletos = documentosRequeridos.get("carta_solicitud_reingreso") &&
                               documentosRequeridos.get("certificado_notas") &&
                               documentosRequeridos.get("documento_identidad") &&
                               documentosRequeridos.get("justificacion_ausencia");
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("documentosRequeridos", documentosRequeridos);
        resultado.put("todosCompletos", todosCompletos);
        resultado.put("totalDocumentos", documentos.size());
        
        log.debug("Validación de reingreso completada. Todos completos: {}", todosCompletos);
        return ResponseEntity.ok(resultado);
    }

}
