package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ArchivoMultipart;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudHomologacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudHomologacionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudHomologacioneMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorArchivoPdf;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorCambioEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;

import java.util.Date;




@RestController
@RequestMapping("/api/solicitudes-homologacion")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
public class SolicitudHomologacionRestController {
    private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
    private final SolicitudHomologacioneMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosCUIntPort documentosCU;
    private final Autorizacion autorizacion;
    private final ValidadorCambioEstado validadorCambioEstado;
    private static final String MARCADOR_OFICIO_HOMOLOGACION = "[OFICIO_HOMOLOGACION]";

    @PreAuthorize("hasAnyRole('Estudiante', 'Administrador')")
    @PostMapping("/crearSolicitud-Homologacion")
    public ResponseEntity<SolicitudHomologacionDTORespuesta> crearSolicitudHomologacion(@Valid @RequestBody SolicitudHomologacionDTOPeticion peticion) {
        // El solicitante sale del token, no de lo que envía el cliente
        peticion.getObjUsuario().setId_usuario(autorizacion.idSolicitante(peticion.getObjUsuario().getId_usuario()));
        SolicitudHomologacion solicitud = solicitudMapperDominio.mappearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(peticion);
        SolicitudHomologacion solicitudCreada = solicitudHomologacionCU.guardar(solicitud);
        ResponseEntity<SolicitudHomologacionDTORespuesta> respuesta = new ResponseEntity<>
        (solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitudCreada), HttpStatus.CREATED);
        return respuesta;
    }

    @GetMapping("/listarSolicitud-Homologacion")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacion() {
        List<SolicitudHomologacion> solicitudes = solicitudHomologacionCU.listarSolicitudes();
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }


    @GetMapping("/listarSolicitud-Homologacion/Funcionario")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacionToFuncionario(
            @RequestParam(required = false) String periodoAcademico) {
        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
                log.debug("Usando período académico actual automático: {}", periodoAcademico);
            }
        }
        
        List<SolicitudHomologacion> solicitudes;
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            solicitudes = solicitudHomologacionCU.listarSolicitudesToFuncionarioPorPeriodo(periodoAcademico.trim());
        } else {
            solicitudes = solicitudHomologacionCU.listarSolicitudesToFuncionario();
        }
        
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Homologacion/Coordinador")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacionToCoordinador(
            @RequestParam(required = false) String periodoAcademico) {
        // Obtener el programa del coordinador autenticado
        Integer idPrograma = autorizacion.idProgramaCoordinadorActual();
        
        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
                log.debug("Usando período académico actual automático: {}", periodoAcademico);
            }
        }
        
        List<SolicitudHomologacion> solicitudes;
        if (idPrograma != null) {
            // Filtrar por programa y período del coordinador
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                solicitudes = solicitudHomologacionCU.listarSolicitudesToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico.trim());
            } else {
                solicitudes = solicitudHomologacionCU.listarSolicitudesToCoordinadorPorPrograma(idPrograma);
            }
        } else {
            // Si no se puede obtener el programa, retornar todas (fallback)
            log.warn("No se pudo obtener el programa del coordinador, retornando todas las solicitudes");
            solicitudes = solicitudHomologacionCU.listarSolicitudesToCoordinador();
        }
        
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Homologacion/Secretaria")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacionToSecretaria(
            @RequestParam(required = false) String periodoAcademico) {
        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
                log.debug("Usando período académico actual automático: {}", periodoAcademico);
            }
        }
        
        List<SolicitudHomologacion> solicitudes;
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            solicitudes = solicitudHomologacionCU.listarSolicitudesToSecretariaPorPeriodo(periodoAcademico.trim());
        } else {
            solicitudes = solicitudHomologacionCU.listarSolicitudesToSecretaria();
        }
        
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }



    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listarSolicitud-Homologacion/porRol")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudPorRol(
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

        // Si no se proporciona período, usar el período académico actual basado en la fecha
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            if (periodoActual != null) {
                periodoAcademico = periodoActual.getValor();
                log.debug("Usando período académico actual automático: {}", periodoAcademico);
            }
        }

        List<SolicitudHomologacion> solicitudes;
        if ("ESTUDIANTE".equals(rol) && idUsuario != null && periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            solicitudes = solicitudHomologacionCU.listarSolicitudesPorUsuarioYPeriodo(idUsuario, periodoAcademico.trim());
        } else {
            solicitudes = solicitudHomologacionCU.listarSolicitudesPorRol(rol, idUsuario);
        }

        List<SolicitudHomologacionDTORespuesta> respuesta =
                solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Homologacion/Secretaria/Aprobadas")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacionAprobadasToSecretaria(
            @RequestParam(required = false) String periodoAcademico) {
        List<SolicitudHomologacion> solicitudes;
        
        // Si se solicita "todos" o está vacío/null, mostrar todas las solicitudes sin filtrar
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty() || 
            periodoAcademico.trim().equalsIgnoreCase("todos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los periodos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los períodos")) {
            log.debug("Mostrando todas las solicitudes procesadas sin filtrar por período");
            solicitudes = solicitudHomologacionCU.listarSolicitudesAprobadasToSecretaria();
        } else {
            // Filtrar por período académico específico
            solicitudes = solicitudHomologacionCU.listarSolicitudesAprobadasToSecretariaPorPeriodo(periodoAcademico.trim());
        }
        
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Listar solicitudes de Homologación ya procesadas por el funcionario (estado APROBADA_FUNCIONARIO)
     * GET /api/solicitudes-homologacion/listarSolicitud-Homologacion/Funcionario/Aprobadas
     * 
     * Este endpoint permite al funcionario ver un historial de las solicitudes 
     * que ya ha procesado y enviado al coordinador.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     */
    @GetMapping("/listarSolicitud-Homologacion/Funcionario/Aprobadas")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacionAprobadasToFuncionario(
            @RequestParam(required = false) String periodoAcademico) {
        List<SolicitudHomologacion> solicitudes;
        
        // Si se solicita "todos" o está vacío/null, mostrar todas las solicitudes sin filtrar
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty() || 
            periodoAcademico.trim().equalsIgnoreCase("todos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los periodos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los períodos")) {
            log.debug("Mostrando todas las solicitudes procesadas sin filtrar por período");
            solicitudes = solicitudHomologacionCU.listarSolicitudesAprobadasToFuncionario();
        } else {
            // Filtrar por período académico específico
            solicitudes = solicitudHomologacionCU.listarSolicitudesAprobadasToFuncionarioPorPeriodo(periodoAcademico.trim());
        }
        
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Listar solicitudes de Homologación ya procesadas por el coordinador (estado APROBADA_COORDINADOR)
     * GET /api/solicitudes-homologacion/listarSolicitud-Homologacion/Coordinador/Aprobadas
     * 
     * Este endpoint permite al coordinador ver un historial de las solicitudes 
     * que ya ha procesado y enviado a la secretaría.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     */
    @GetMapping("/listarSolicitud-Homologacion/Coordinador/Aprobadas")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudHomologacionAprobadasToCoordinador(
            @RequestParam(required = false) String periodoAcademico) {
        // Obtener el programa del coordinador autenticado
        Integer idPrograma = autorizacion.idProgramaCoordinadorActual();
        
        List<SolicitudHomologacion> solicitudes;
        
        // Si se solicita "todos" o está vacío/null, mostrar todas las solicitudes sin filtrar por período
        boolean mostrarTodos = periodoAcademico == null || periodoAcademico.trim().isEmpty() || 
            periodoAcademico.trim().equalsIgnoreCase("todos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los periodos") ||
            periodoAcademico.trim().equalsIgnoreCase("todos los períodos");
        
        if (idPrograma != null) {
            if (mostrarTodos) {
                // Filtrar solo por programa, sin período
                log.debug("Mostrando todas las solicitudes procesadas del programa {} sin filtrar por período", idPrograma);
                solicitudes = solicitudHomologacionCU.listarSolicitudesAprobadasToCoordinadorPorPrograma(idPrograma);
            } else {
                // Filtrar por programa y período del coordinador
                solicitudes = solicitudHomologacionCU.listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico.trim());
            }
        } else {
            // Si no se puede obtener el programa, retornar todas (fallback)
            log.warn("No se pudo obtener el programa del coordinador, retornando todas las solicitudes");
            solicitudes = solicitudHomologacionCU.listarSolicitudesAprobadasToCoordinador();
        }
        
        List<SolicitudHomologacionDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudHomologacionARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }


    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#id)")
    @GetMapping("/listarSolicitud-Homologacion/{id}")
    public ResponseEntity<SolicitudHomologacionDTORespuesta> listarHomologacionById(@PathVariable Integer id) {
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(id);
        SolicitudHomologacionDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Map<String, Object>> actualizarEstadoSolicitudHomologacion(@RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        // Solo el rol responsable del paso puede hacerlo (Funcionario → Coordinador → Secretario)
        String estadoActual = FlujoEstadosSolicitud.ultimoEstado(
                solicitudHomologacionCU.buscarPorId(solicitud.getIdSolicitud()).getEstadosSolicitud());
        ResponseEntity<Map<String, Object>> rechazo = validadorCambioEstado.validar(
                FlujoEstadosSolicitud.TRES_APROBACIONES, estadoActual, solicitud.getNuevoEstado());
        if (rechazo != null) {
            return rechazo;
        }
        solicitudHomologacionCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    /**
     * Descargar oficio por ID de solicitud
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/descargarOficio/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficio(@PathVariable Integer idSolicitud) {
        // Obtener la solicitud con sus documentos
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos asociados a esta solicitud
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Prioridad 1: documentos marcados explícitamente como oficio/resolución de homologación (subidos por secretaría)
        Documento documentoSeleccionado = null;
        for (Documento d : documentos) {
            if (d == null) continue;
            String comentario = d.getComentario();
            if (comentario != null && comentario.contains(MARCADOR_OFICIO_HOMOLOGACION)) {
                if (documentoSeleccionado == null) {
                    documentoSeleccionado = d;
                } else {
                    Date fSel = documentoSeleccionado.getFecha_documento();
                    Date fNew = d.getFecha_documento();
                    if (fSel == null && fNew != null) {
                        documentoSeleccionado = d;
                    } else if (fSel != null && fNew != null && fNew.after(fSel)) {
                        documentoSeleccionado = d;
                    }
                }
            }
        }

        // Prioridad 2 (fallback): heurística por nombre (resolucion/oficio) excluyendo formulario del estudiante
        if (documentoSeleccionado == null) {
            for (Documento d : documentos) {
                if (d != null && d.getNombre() != null && esDocumentoOficioHomologacion(d)) {
                    documentoSeleccionado = d;
                    break;
                }
            }
        }

        if (documentoSeleccionado == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String nombreDescarga = documentoSeleccionado.getNombre();
            String rutaDocumento = documentoSeleccionado.getRuta_documento() != null
                    ? documentoSeleccionado.getRuta_documento()
                    : nombreDescarga;
            byte[] archivo;
            if (rutaDocumento != null && rutaDocumento.contains("/")) {
                archivo = objGestionarArchivos.getFileByPath(rutaDocumento);
            } else {
                archivo = objGestionarArchivos.getFile(nombreDescarga);
            }
            String contentDisposition = "attachment; filename=\"" + nombreDescarga + "\"";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(archivo);
        } catch (Exception e) {
            log.debug("No se pudo cargar el oficio/resolución seleccionado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtener oficios disponibles para una solicitud
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerOficios(@PathVariable Integer idSolicitud) {
        // Obtener la solicitud con sus documentos
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Buscar documentos asociados a esta solicitud
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>()); // Retornar lista vacía
        }
        
        // Crear lista de oficios basada en los documentos reales.
        // Prioridad: marcados explícitamente como oficio/resolución de homologación, fallback a heurística por nombre.
        List<Map<String, Object>> oficios = new ArrayList<>();
        for (Documento documento : documentos) {
            if (documento == null || documento.getNombre() == null) continue;
            boolean esMarcado = documento.getComentario() != null
                    && documento.getComentario().contains(MARCADOR_OFICIO_HOMOLOGACION);
            if (esMarcado || esDocumentoOficioHomologacion(documento)) {
                Map<String, Object> oficio = new HashMap<>();
                oficio.put("id", idSolicitud);
                oficio.put("nombre", documento.getNombre());
                oficio.put("nombreArchivo", documento.getNombre());
                oficio.put("ruta", documento.getRuta_documento());
                oficios.add(oficio);
            }
        }
        
        return ResponseEntity.ok(oficios);
    }
    
    /**
     * Validar documentos requeridos para homologación
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idSolicitud)")
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> validarDocumentosRequeridos(@PathVariable Integer idSolicitud) {
        // Obtener la solicitud con sus documentos
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Documento> documentos = solicitud.getDocumentos();
        if (documentos == null) {
            documentos = new ArrayList<>();
        }
        
        // Documentos requeridos para homologación
        Map<String, Boolean> documentosRequeridos = new HashMap<>();
        documentosRequeridos.put("formulario_homologacion", false);
        documentosRequeridos.put("certificado_notas", false);
        documentosRequeridos.put("programa_academico", false);
        
        // Verificar qué documentos están presentes
        for (Documento documento : documentos) {
            if (documento.getNombre() != null) {
                String nombre = documento.getNombre().toLowerCase();
                
                if (nombre.contains("formulario") && nombre.contains("homologacion")) {
                    documentosRequeridos.put("formulario_homologacion", true);
                } else if (nombre.contains("certificado") && nombre.contains("notas")) {
                    documentosRequeridos.put("certificado_notas", true);
                } else if (nombre.contains("programa") && nombre.contains("academico")) {
                    documentosRequeridos.put("programa_academico", true);
                }
            }
        }
        
        // Calcular si todos los documentos están presentes
        boolean todosCompletos = documentosRequeridos.values().stream().allMatch(Boolean::booleanValue);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("documentosRequeridos", documentosRequeridos);
        resultado.put("todosCompletos", todosCompletos);
        resultado.put("totalDocumentos", documentos.size());
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Indica si el documento es un oficio/resolución (subido por secretaría).
     * Prioridad: si el comentario indica "secretaría", se considera oficio (cualquier nombre).
     * Fallback: por nombre (resolucion/oficio) excluyendo formulario del estudiante (solicitud+homologacion).
     */
    private static boolean esDocumentoOficioHomologacion(Documento documento) {
        if (documento == null) return false;
        // Nota: NO usar "secretar" como heurística: los comentarios de documentos pueden incluir roles (SECRETARIA)
        // por el historial de comentarios acumulativos, lo que produce falsos positivos.
        String nombre = documento.getNombre();
        if (nombre == null) return false;
        String nombreLower = nombre.toLowerCase(Locale.ROOT);
        boolean pareceFormularioEstudiante = nombreLower.contains("solicitud") && nombreLower.contains("homologacion");
        boolean esResolucionUOficio = nombreLower.contains("resolucion") || nombreLower.contains("oficio");
        return esResolucionUOficio && !pareceFormularioEstudiante;
    }

    /**
     * Subir resolución de homologación (secretaría). El documento se marca con comentario
     * "Archivo subido por secretaría" para que se liste y permita descargar al estudiante,
     * independiente del nombre del archivo.
     */
    @PostMapping("/{idSolicitud}/subir-resolucion")
    public ResponseEntity<Map<String, Object>> subirResolucion(
            @PathVariable Integer idSolicitud,
            @RequestParam("file") MultipartFile file) {
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        ValidadorArchivoPdf.Rechazo rechazo = ValidadorArchivoPdf.validar(file, ValidadorArchivoPdf.MAXIMO_10MB);
        if (rechazo != null) {
            return ResponseEntity.status(rechazo.estado()).body(Map.of("error", rechazo.mensaje()));
        }
        String nombreOriginal = file.getOriginalFilename();
        String rutaArchivo = objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreOriginal, "pdf", "homologacion", idSolicitud);
        String nombreArchivo = rutaArchivo.contains("/") ? rutaArchivo.substring(rutaArchivo.lastIndexOf("/") + 1) : rutaArchivo;

        Documento documento = new Documento();
        documento.setNombre(nombreArchivo);
        documento.setRuta_documento(rutaArchivo);
        documento.setFecha_documento(new Date());
        documento.setEsValido(true);
        documento.setComentario(MARCADOR_OFICIO_HOMOLOGACION + " Archivo subido por secretaría");
        documento.setObjSolicitud(solicitud);

        Documento guardado = documentosCU.registrarDocumento(documento);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Resolución subida correctamente");
        respuesta.put("idDocumento", guardado.getId_documento());
        respuesta.put("nombre", nombreArchivo);
        respuesta.put("idSolicitud", idSolicitud);
        return ResponseEntity.ok(respuesta);
    }

}
