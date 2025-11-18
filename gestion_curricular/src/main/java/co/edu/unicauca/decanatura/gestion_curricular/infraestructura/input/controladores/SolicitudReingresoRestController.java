package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

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
import org.springframework.web.multipart.MultipartFile;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/solicitudes-reingreso")   
@RequiredArgsConstructor
@Slf4j
public class SolicitudReingresoRestController {

    private final SolicitudReingresoMapperDominio solicitudReingresoMapper;
    private final GestionarSolicitudReingresoCUIntPort solicitudService;
    private final SolicitudMapperDominio solicitudMapper;
    private final GestionarArchivosCUIntPort objGestionarArchivos;

    @PostMapping("/crearSolicitud-Reingreso")
    public ResponseEntity<SolicitudReingresoDTORespuesta> crearSolicitudReingreso(@RequestBody SolicitudReingresoDTOPeticion solicitudDTO) {
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
    
    @GetMapping("/listarSolicitud-Reingreso/porUser")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudPorUser(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idUsuario) {

        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesReingresoPorRol(rol, idUsuario);

        List<SolicitudReingresoDTORespuesta> respuesta =
                solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);

        return ResponseEntity.ok(respuesta);
    }

        @GetMapping("/listarSolicitud-Reingreso/Funcionario")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoToFuncionario() {
        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesReingresoToFuncionario();
        List<SolicitudReingresoDTORespuesta> respuesta = solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Reingreso/Coordinador")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoToCoordinador() {
        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesReingresoToCoordinador();
        List<SolicitudReingresoDTORespuesta> respuesta = solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Reingreso/Secretaria")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoToSecretaria() {
        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesReingresoToSecretaria();
        List<SolicitudReingresoDTORespuesta> respuesta = solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Reingreso/Secretaria/Aprobadas")
    public ResponseEntity<List<SolicitudReingresoDTORespuesta>> listarSolicitudReingresoAprobadasToSecretaria() {
        List<SolicitudReingreso> solicitudes = solicitudService.listarSolicitudesAprobadasToSecretaria();
        List<SolicitudReingresoDTORespuesta> respuesta = solicitudReingresoMapper.mappearDeListaSolicitudReingresoASolicitudReingresoDTORespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-Reingreo/{id}")
    public ResponseEntity<SolicitudReingresoDTORespuesta> listarReingresoById(@PathVariable Integer id) {
        SolicitudReingreso solicitud = solicitudService.obtenerSolicitudReingresoPorId(id);
        SolicitudReingresoDTORespuesta respuesta = solicitudReingresoMapper.mappearDeSolicitudReingresoASolicitudReingresoDTORespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud-Reingreso")
    public ResponseEntity<Void> actualizarEstadoSolicitudReingreso(@RequestBody CambioEstadoSolicitudDTOPeticion peticion) {
        CambioEstadoSolicitud solicitud = solicitudMapper.mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);
        solicitudService.cambiarEstadoSolicitudReingreso(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());
        return ResponseEntity.noContent().build();
    }

    // ================================
    // Endpoints para oficios/resoluciones de reingreso
    // ================================

    /**
     * Descargar oficio por ID de solicitud de reingreso
     */
    @GetMapping("/descargarOficio/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficio(@PathVariable Integer idSolicitud) {
        try {
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
                            // Usar ruta completa si está disponible, sino usar nombre
                            byte[] archivo = objGestionarArchivos.getFile(
                                documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre()
                            );
                            
                            log.debug("Oficio/resolución de reingreso encontrado: {}, tamaño: {} bytes", 
                                documento.getNombre(), archivo.length);
                            
                            // Configurar el header Content-Disposition correctamente
                            String contentDisposition = "attachment; filename=\"" + documento.getNombre() + "\"";
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
                
        } catch (Exception e) {
            log.error("Error al descargar oficio de reingreso: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener oficios disponibles para una solicitud de reingreso
     */
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerOficios(@PathVariable Integer idSolicitud) {
        try {
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
            
        } catch (Exception e) {
            log.error("Error al obtener oficios de reingreso: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Subir archivo PDF asociado a una solicitud de reingreso
     */
    @PostMapping("/{idSolicitud}/subir-archivo")
    public ResponseEntity<Map<String, Object>> subirArchivo(@PathVariable Integer idSolicitud, 
                                                           @RequestParam("file") MultipartFile archivo) {
        try {
            log.debug("Subiendo archivo para solicitud de reingreso: {}, nombre: {}, tamaño: {} bytes", 
                idSolicitud, archivo.getOriginalFilename(), archivo.getSize());

            // Validar que la solicitud existe
            SolicitudReingreso solicitud = solicitudService.obtenerSolicitudReingresoPorId(idSolicitud);
            if (solicitud == null) {
                log.warn("Solicitud de reingreso no encontrada: {}", idSolicitud);
                return ResponseEntity.notFound().build();
            }

            // Validar que el archivo no esté vacío
            if (archivo.isEmpty()) {
                log.warn("El archivo está vacío");
                return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío"));
            }

            // Validar que sea un PDF
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                log.warn("El archivo no es un PDF. Tipo: {}", contentType);
                return ResponseEntity.badRequest().body(Map.of("error", "Solo se permiten archivos PDF"));
            }

            // Guardar el archivo usando el servicio de archivos
            String nombreArchivo = archivo.getOriginalFilename();
            if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
                nombreArchivo = "archivo_reingreso_" + idSolicitud + ".pdf";
            }

            log.debug("Guardando archivo: {}", nombreArchivo);
            // Guardar archivo organizado en subcarpetas
            String rutaArchivo = objGestionarArchivos.saveFile(archivo, nombreArchivo, "pdf", "reingreso", idSolicitud);

            // Crear documento y asociarlo a la solicitud
            Documento documento = new Documento();
            documento.setNombre(nombreArchivo);
            documento.setRuta_documento(rutaArchivo); // Guardar ruta completa con subcarpetas
            documento.setFecha_documento(new Date());
            documento.setEsValido(true);
            documento.setComentario("Archivo subido por secretaría");

            // Agregar el documento a la solicitud
            if (solicitud.getDocumentos() == null) {
                solicitud.setDocumentos(new ArrayList<>());
            }
            solicitud.getDocumentos().add(documento);

            // Guardar la solicitud actualizada
            solicitudService.crearSolicitudReingreso(solicitud);

            log.debug("Archivo subido exitosamente para solicitud: {}, documento creado: {}", 
                idSolicitud, documento.getNombre());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Archivo subido exitosamente");
            respuesta.put("nombreArchivo", nombreArchivo);
            respuesta.put("idSolicitud", idSolicitud);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error al subir archivo de reingreso: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Validar documentos requeridos para reingreso
     */
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> validarDocumentosRequeridos(@PathVariable Integer idSolicitud) {
        try {
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
            
        } catch (Exception e) {
            log.error("Error al validar documentos de reingreso: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
