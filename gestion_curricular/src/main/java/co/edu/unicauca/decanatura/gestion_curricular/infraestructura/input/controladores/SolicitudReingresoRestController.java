package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudReingresoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.UsuarioDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosSolicitudDTORespuesta.Oficio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.RechazoCambioEstadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudReingresoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudReingresoMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorCambioEstado;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;


@RestController
@RequestMapping("/api/solicitudes-reingreso")   
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
public class SolicitudReingresoRestController {

    private final SolicitudReingresoMapperDominio solicitudReingresoMapper;
    private final GestionarSolicitudReingresoCUIntPort solicitudService;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort gestionarArchivos;
    private final Autorizacion autorizacion;
    private final ValidadorCambioEstado validadorCambioEstado;

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping
    public ResponseEntity<SolicitudReingresoDTORespuesta> crearSolicitudReingreso(@RequestBody SolicitudReingresoDTOPeticion solicitudDTO) {
        // El solicitante sale del token, no de lo que envía el cliente
        if (solicitudDTO.getObjUsuario() == null) {
            solicitudDTO.setObjUsuario(new UsuarioDTOPeticion());
        }
        solicitudDTO.getObjUsuario().setId_usuario(autorizacion.idSolicitante(solicitudDTO.getObjUsuario().getId_usuario()));
        SolicitudReingreso solicitud = solicitudReingresoMapper.mapearDeSolicitudReingresoDTOPeticionASolicitudReingreso(solicitudDTO);
        SolicitudReingreso solicitudCreada = solicitudService.crearSolicitudReingreso(solicitud);
        ResponseEntity<SolicitudReingresoDTORespuesta> respuesta = new ResponseEntity<>
        (solicitudReingresoMapper.mapearDeSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    /**
     * Sin {@code rol} no hay listado: responde el mismo 400 que da un parámetro obligatorio que falta. El listado
     * completo sin filtros se retiró en EST-7 (no tenía uso).
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Void> listarSinRol() throws MissingServletRequestParameterException {
        throw new MissingServletRequestParameterException("rol", "String");
    }

    /** Antes {@code /listarSolicitud-Reingreso/porUser}. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = {"rol", "!bandeja"})
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

    /**
     * Bandeja de un rol (EST-7): {@code bandeja=pendientes} son las que esperan su revisión (antes
     * {@code /listarSolicitud-Reingreso/{Rol}}) y {@code bandeja=procesadas}, las que ya aprobó alguna vez (antes
     * {@code /{Rol}/Aprobadas}).
     *
     * @param rol              funcionario, coordinador o secretaria
     * @param bandeja          pendientes o procesadas
     * @param periodoAcademico opcional ("2025-2"). En pendientes, si falta, el actual; en procesadas, vacío o
     *                         "todos" muestra todos los períodos. El coordinador sin programa ve todas, de todos los
     *                         períodos.
     */
    @GetMapping(params = {"rol", "bandeja"})
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarBandeja(
            @RequestParam String rol,
            @RequestParam String bandeja,
            @RequestParam(required = false) String periodoAcademico) {
        boolean procesadas = switch (bandeja) {
            case "pendientes" -> false;
            case "procesadas" -> true;
            default -> throw new IllegalArgumentException("Bandeja no válida: use pendientes o procesadas");
        };
        String paso = switch (rol) {
            case "funcionario" -> FlujoEstadosSolicitud.FUNCIONARIO;
            case "coordinador" -> FlujoEstadosSolicitud.COORDINADOR;
            case "secretaria" -> FlujoEstadosSolicitud.SECRETARIO;
            default -> throw new IllegalArgumentException("Rol no válido: use funcionario, coordinador o secretaria");
        };
        String periodo = procesadas ? PeriodoDeConsulta.oTodos(periodoAcademico)
                : PeriodoDeConsulta.oActual(periodoAcademico);
        Integer idPrograma = null;
        if (FlujoEstadosSolicitud.COORDINADOR.equals(paso)) {
            idPrograma = autorizacion.idProgramaCoordinadorActual();
            if (idPrograma == null) {
                // Si no se puede obtener el programa, se devuelven todas, de todos los períodos (fallback)
                log.warn("No se pudo obtener el programa del coordinador, retornando todas las solicitudes");
                periodo = null;
            }
        }
        return responder(solicitudService.listar(procesadas
                ? FiltroSolicitudes.procesadasPor(paso, idPrograma, periodo)
                : FiltroSolicitudes.pendientesDe(paso, idPrograma, periodo)));
    }

    private ResponseEntity<List<SolicitudReingresoDTORespuesta>> responder(List<SolicitudReingreso> solicitudes) {
        return ResponseEntity.ok(
                solicitudReingresoMapper.mapearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes));
    }

    /** La solicitud es la de la ruta: un {@code idSolicitud} en el cuerpo se ignora. */
    @PutMapping("/{idSolicitud:\\d+}/estado")
    public ResponseEntity<RechazoCambioEstadoDTORespuesta> actualizarEstadoSolicitudReingreso(
            @PathVariable Integer idSolicitud, @RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        peticion.setIdSolicitud(idSolicitud);
        CambioEstadoSolicitud solicitud = solicitudMapper.mapearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        // Solo el rol responsable del paso puede hacerlo (Funcionario → Coordinador → Secretario)
        String estadoActual = FlujoEstadosSolicitud.ultimoEstado(
                solicitudService.obtenerSolicitudReingresoPorId(solicitud.getIdSolicitud()).getEstadosSolicitud());
        ResponseEntity<RechazoCambioEstadoDTORespuesta> rechazo = validadorCambioEstado.validar(
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
    @GetMapping("/{idSolicitud:\\d+}/oficios/archivo")
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
                
                if (esOficioReingreso(nombreArchivo)) {
                    try {
                        log.debug("Probando oficio/resolución de reingreso: {}", documento.getNombre());
                        
                        // Lógica adaptativa: usar ruta completa si está organizada, sino usar nombre
                        String rutaDocumento = documento.getRutaDocumento() != null ? documento.getRutaDocumento() : documento.getNombre();
                        byte[] archivo;
                        
                        if (rutaDocumento != null && rutaDocumento.contains("/")) {
                            // Ruta organizada (nueva estructura)
                            archivo = gestionarArchivos.getFileByPath(rutaDocumento);
                        } else {
                            // Ruta simple (compatibilidad hacia atrás)
                            archivo = gestionarArchivos.getFile(documento.getNombre());
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
    @GetMapping("/{idSolicitud:\\d+}/oficios")
    public ResponseEntity<List<Oficio>> obtenerOficios(@PathVariable Integer idSolicitud) {
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
        List<Oficio> oficios = new ArrayList<>();
        for (Documento documento : documentos) {
            if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                String nombreArchivo = documento.getNombre().toLowerCase();
                
                if (esOficioReingreso(nombreArchivo)) {
                    oficios.add(new Oficio(idSolicitud, documento.getNombre(), documento.getNombre(),
                            documento.getRutaDocumento()));
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
     * Indica si un PDF, con el nombre ya en minúsculas, parece un oficio o resolución de reingreso. La carta de
     * solicitud del estudiante también lleva "reingreso" en el nombre, pero es un documento requerido y no un
     * oficio: lleva a la vez "carta", "solicitud" y "reingreso".
     */
    private static boolean esOficioReingreso(String nombreArchivo) {
        boolean esCartaEstudiante = nombreArchivo.contains("carta") && nombreArchivo.contains("solicitud")
                && nombreArchivo.contains("reingreso");
        return !esCartaEstudiante && (nombreArchivo.contains("oficio") || nombreArchivo.contains("resolucion")
                || nombreArchivo.contains("reingreso") || nombreArchivo.contains("aprobacion"));
    }
    
}
