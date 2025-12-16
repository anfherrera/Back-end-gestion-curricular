package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

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
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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




@RestController
@RequestMapping("/api/solicitudes-homologacion")
@RequiredArgsConstructor
@Slf4j
public class SolicitudHomologacionRestController {
    private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
    private final SolicitudHomologacioneMapperDominio solicitudMapperDominio;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarUsuarioGatewayIntPort usuarioGateway;

    @PostMapping("/crearSolicitud-Homologacion")
    public ResponseEntity<SolicitudHomologacionDTORespuesta> crearSolicitudHomologacion(@Valid @RequestBody SolicitudHomologacionDTOPeticion peticion) {
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
        Integer idPrograma = obtenerProgramaCoordinadorAutenticado();
        
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



    @GetMapping("/listarSolicitud-Homologacion/porRol")
    public ResponseEntity<List<SolicitudHomologacionDTORespuesta>> listarSolicitudPorRol(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario,
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
        Integer idPrograma = obtenerProgramaCoordinadorAutenticado();
        
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


    @GetMapping("/listarSolicitud-Homologacion/{id}")
    public ResponseEntity<SolicitudHomologacionDTORespuesta> listarHomologacionById(@PathVariable Integer id) {
        SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(id);
        SolicitudHomologacionDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Void> actualizarEstadoSolicitudHomologacion(@RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        solicitudHomologacionCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    // @GetMapping(" ")
    // public ResponseEntity<SolicitudHomologacionDTORespuesta> obtenerSolicitudHomologacionSeleccionada() {
    //    SolicitudHomologacion solicitud = solicitudHomologacionCU. obtenerSolicitudHomologacion();
    //    SolicitudHomologacionDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(solicitud);
    //    return ResponseEntity.ok(respuesta);
    // }

    /**
     * Descargar oficio por ID de solicitud
     */
    @GetMapping("/descargarOficio/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficio(@PathVariable Integer idSolicitud) {
        try {
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
            
            // Buscar documentos que sean oficios/resoluciones (subidos por secretaria)
            for (Documento documento : documentos) {
                if (documento.getNombre() != null) {
                    String nombreArchivo = documento.getNombre().toLowerCase(Locale.ROOT);
                    
                    // Filtrar solo archivos que parecen ser oficios/resoluciones
                    boolean esOficio = nombreArchivo.contains("oficio") || 
                                     nombreArchivo.contains("resolucion") || 
                                     nombreArchivo.contains("homologacion") ||
                                     nombreArchivo.contains("aprobacion");
                    
                    if (esOficio) {
                        try {
                            byte[] archivo = objGestionarArchivos.getFile(documento.getNombre());
                            
                            // Configurar el header Content-Disposition correctamente
                            String contentDisposition = "attachment; filename=\"" + documento.getNombre() + "\"";
                            
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
     * Obtener oficios disponibles para una solicitud
     */
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerOficios(@PathVariable Integer idSolicitud) {
        try {
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
            
            // Crear lista de oficios basada en los documentos reales (solo oficios/resoluciones)
            List<Map<String, Object>> oficios = new ArrayList<>();
            for (Documento documento : documentos) {
                if (documento.getNombre() != null) {
                    String nombreArchivo = documento.getNombre().toLowerCase(Locale.ROOT);
                    
                    // Filtrar solo archivos que parecen ser oficios/resoluciones
                    boolean esOficio = nombreArchivo.contains("oficio") || 
                                     nombreArchivo.contains("resolucion") || 
                                     nombreArchivo.contains("homologacion") ||
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
     * Validar documentos requeridos para homologación
     */
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> validarDocumentosRequeridos(@PathVariable Integer idSolicitud) {
        try {
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
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
                log.warn("No hay usuario autenticado");
                return null;
            }

            String email = authentication.getName();
            Usuario usuarioAutenticado = usuarioGateway.buscarUsuarioPorCorreo(email);
            
            if (usuarioAutenticado == null) {
                log.warn("Usuario autenticado no encontrado: {}", email);
                return null;
            }

            // Verificar que sea coordinador
            String rolNombre = usuarioAutenticado.getObjRol() != null 
                    ? usuarioAutenticado.getObjRol().getNombre() 
                    : null;
            
            if (!"Coordinador".equals(rolNombre)) {
                log.warn("El usuario autenticado no es coordinador: {}", rolNombre);
                return null;
            }

            // Obtener el programa del coordinador
            if (usuarioAutenticado.getObjPrograma() != null && usuarioAutenticado.getObjPrograma().getId_programa() != null) {
                return usuarioAutenticado.getObjPrograma().getId_programa();
            }

            log.warn("El coordinador no tiene programa asignado");
            return null;
        } catch (Exception e) {
            log.error("Error al obtener programa del coordinador autenticado", e);
            return null;
        }
    }

}
