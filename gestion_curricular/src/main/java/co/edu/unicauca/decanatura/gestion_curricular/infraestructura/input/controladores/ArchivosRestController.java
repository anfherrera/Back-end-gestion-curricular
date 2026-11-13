package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ArchivoMultipart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;

import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorArchivoPdf;

import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Validated
public class ArchivosRestController {
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosCUIntPort documentosCU;
    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCursoVeranoCU;
    private final Autorizacion autorizacion;

    @PostMapping("/subir/pdf")
    public ResponseEntity<Map<String, Object>> subirPDF(
        @RequestParam(name = "file", required = true) MultipartFile file,
        @RequestParam(name = "inscripcionId", required = false) String inscripcionId,
        @RequestParam(name = "solicitudId", required = false) String solicitudId,
        @RequestParam(name = "idSolicitud", required = false) String idSolicitudAlias,
        @RequestParam(name = "tipoSolicitud", required = false) String tipoSolicitud) {
        
        String solicitudIdUnificado = null;
        if (inscripcionId != null && !inscripcionId.trim().isEmpty()) {
            solicitudIdUnificado = inscripcionId;
        } else if (solicitudId != null && !solicitudId.trim().isEmpty()) {
            solicitudIdUnificado = solicitudId;
        } else if (idSolicitudAlias != null && !idSolicitudAlias.trim().isEmpty()) {
            solicitudIdUnificado = idSolicitudAlias;
        }

        // Solo el staff o el dueño pueden adjuntar archivos a una solicitud existente
        if (solicitudIdUnificado != null && !autorizacion.esStaff()) {
            Integer idSolicitudDestino;
            try {
                idSolicitudDestino = Integer.parseInt(solicitudIdUnificado.trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de solicitud inválido"));
            }
            if (!autorizacion.esDuenoSolicitud(idSolicitudDestino)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No puede adjuntar archivos a una solicitud que no es suya"));
            }
        }

        String nombreOriginal = file.getOriginalFilename();

        // Validar que sea un PDF real (contenido, no solo nombre o Content-Type) y el tamaño máximo
        ValidadorArchivoPdf.Rechazo rechazo = ValidadorArchivoPdf.validar(file, ValidadorArchivoPdf.MAXIMO_15MB);
        if (rechazo != null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", rechazo.mensaje());
            return ResponseEntity.status(rechazo.estado()).body(error);
        }
        
        String nombreArchivo;
        // Generar nombre usando el nombre original del archivo + nombre del estudiante
        String nombreUnico = nombreOriginal;
        if (inscripcionId != null && !inscripcionId.trim().isEmpty()) {
            try {
                SolicitudCursoVeranoIncripcion inscripcion = solicitudCursoVeranoCU.buscarPorIdInscripcion(Integer.parseInt(inscripcionId));
                
                if (inscripcion != null && inscripcion.getObjUsuario() != null) {
                    // Obtener el nombre del estudiante
                    String nombreEstudiante = inscripcion.getObjUsuario().getNombre_completo();
                    if (nombreEstudiante != null && !nombreEstudiante.trim().isEmpty()) {
                        // Limpiar el nombre del estudiante para usar en archivo
                        String nombreLimpio = nombreEstudiante.replaceAll("[^a-zA-Z0-9]", "_");
                        
                        // Extraer solo el nombre del archivo sin extensión
                        String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
                        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
                        
                        // Crear nombre: nombreOriginal_nombreEstudiante_inscripcionId.extension
                        nombreUnico = nombreSinExtension + "_" + nombreLimpio + "_" + inscripcionId + extension;
                    } else {
                        // Si no se puede obtener el nombre del estudiante, usar solo el ID
                        String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
                        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
                        nombreUnico = nombreSinExtension + "_" + inscripcionId + extension;
                    }
                } else {
                    // Si no se encuentra la inscripción, usar solo el ID
                    String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
                    String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
                    nombreUnico = nombreSinExtension + "_" + inscripcionId + extension;
                }
            } catch (Exception e) {
                // En caso de error, usar solo el ID
                String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
                String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
                nombreUnico = nombreSinExtension + "_" + inscripcionId + extension;
            }
        }
        
        // Guardar archivo organizado si se proporciona tipoSolicitud e ID
        if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty() && 
            solicitudIdUnificado != null && !solicitudIdUnificado.trim().isEmpty()) {
            try {
                Integer idSolicitudInt = Integer.parseInt(solicitudIdUnificado);
                nombreArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf", tipoSolicitud, idSolicitudInt);
            } catch (NumberFormatException e) {
                // Si no se puede parsear el ID, usar método alternativo
                nombreArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf");
            }
        } else if (inscripcionId != null && !inscripcionId.trim().isEmpty()) {
            // Si hay inscripcionId pero no tipoSolicitud, inferir que es curso-verano y usar organización
            try {
                Integer idInscripcionInt = Integer.parseInt(inscripcionId);
                // Verificar que la inscripción existe antes de organizar
                try {
                    SolicitudCursoVeranoIncripcion inscripcion = solicitudCursoVeranoCU.buscarPorIdInscripcion(idInscripcionInt);
                    if (inscripcion != null) {
                        nombreArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf", "curso-verano", inscripcion.getId_solicitud());
                    } else {
                        // Inscripción no existe aún, guardar en raíz (se moverá después)
                        nombreArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf");
                    }
                } catch (Exception e) {
                    // Inscripción no existe o error, guardar en raíz (se moverá después)
                    nombreArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf");
                }
            } catch (NumberFormatException e) {
                nombreArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf");
            }
        } else {
            // Si no hay tipoSolicitud ni inscripcionId, guardar en raíz (retrocompatibilidad)
            nombreArchivo = this.objGestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf");
        }
        
        boolean documentoGuardadoEnBD = false;
        try {
            Documento documento = new Documento();
            // Extraer solo el nombre del archivo (sin la ruta) para el campo nombre
            String nombreArchivoSimple = nombreArchivo.contains("/") 
                ? nombreArchivo.substring(nombreArchivo.lastIndexOf("/") + 1) 
                : nombreArchivo;
            documento.setNombre(nombreArchivoSimple);
            // Guardar la ruta completa en ruta_documento
            documento.setRuta_documento(nombreArchivo);
            documento.setFecha_documento(new java.util.Date());
            documento.setEsValido(true);
            // Si no trae solicitud, se asociará a la próxima solicitud que cree este mismo usuario
            documento.setIdUsuarioSubio(autorizacion.idUsuarioActual());

            if (solicitudIdUnificado != null && !solicitudIdUnificado.trim().isEmpty()) {
                Integer solicitudIdParsed = null;
                try {
                    solicitudIdParsed = Integer.parseInt(solicitudIdUnificado);
                } catch (NumberFormatException e) {
                }

                if (solicitudIdParsed != null) {
                    SolicitudCursoVeranoIncripcion inscripcionReal = null;
                    try {
                        inscripcionReal = solicitudCursoVeranoCU.buscarPorIdInscripcion(solicitudIdParsed);
                    } catch (Exception e) {
                    }

                    if (inscripcionReal != null) {
                        documento.setObjSolicitud(inscripcionReal);
                    } else {
                        Solicitud solicitudGenerica = new Solicitud();
                        solicitudGenerica.setId_solicitud(solicitudIdParsed);
                        documento.setObjSolicitud(solicitudGenerica);
                    }
                }
            }
            
            Documento documentoGuardado = documentosCU.registrarDocumento(documento);
            
            if (documentoGuardado != null) {
                documentoGuardadoEnBD = true;
            } else {
            }
            
        } catch (Exception e) {
            log.error("Error al guardar documento en BD: {}", nombreArchivo, e);
        }
        
        // 6. Crear respuesta en el formato requerido
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", System.currentTimeMillis());
        respuesta.put("nombre", nombreOriginal);
        respuesta.put("ruta", "/uploads/archivos/" + nombreArchivo);
        respuesta.put("tamaño", file.getSize());
        respuesta.put("tipo", file.getContentType());
        respuesta.put("fechaSubida", new java.util.Date().toString());
        respuesta.put("guardadoEnBD", documentoGuardadoEnBD);
        
        if (inscripcionId != null && !inscripcionId.trim().isEmpty()) {
            respuesta.put("inscripcionId", inscripcionId);
        }
        
        if (solicitudIdUnificado != null && !solicitudIdUnificado.trim().isEmpty()) {
            respuesta.put("solicitudId", solicitudIdUnificado);
        }
        
        if (tipoSolicitud != null && !tipoSolicitud.trim().isEmpty()) {
            respuesta.put("tipoSolicitud", tipoSolicitud);
        }
        
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/descargar/pdf")
    public ResponseEntity<StreamingResponseBody> bajarPDF(@RequestParam(name = "filename", required = true) String filename) {
        try {
            // Solo se entrega un archivo registrado que el usuario pueda ver (el staff ve todos).
            // Si dos estudiantes subieron archivos con el mismo nombre, cada uno recibe el suyo.
            java.util.Optional<String> rutaVisible = autorizacion.rutaArchivoVisible(filename);
            if (rutaVisible.isEmpty() && !autorizacion.esStaff()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Staff: si el archivo no está registrado en BD, se busca por el nombre recibido (comportamiento anterior)
            String rutaArchivo = rutaVisible.orElse(filename);

            // OPTIMIZACIÓN: Usar streaming en lugar de cargar todo en memoria
            Path filePath = objGestionarArchivos.getFileAsPath(rutaArchivo);
            
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            // Extraer solo el nombre del archivo para el header (sin la ruta)
            String nombreArchivo = filename;
            if (rutaArchivo.contains("/")) {
                nombreArchivo = rutaArchivo.substring(rutaArchivo.lastIndexOf("/") + 1);
            }
            
            // Configurar Content-Disposition con filename y filename* (UTF-8) - igual que Paz y Salvo
            String contentDisposition = RespuestasArchivo.adjuntoUtf8(nombreArchivo);

            // Crear StreamingResponseBody para transferencia eficiente
            StreamingResponseBody stream = outputStream -> {
                try (InputStream inputStream = Files.newInputStream(filePath)) {
                    byte[] buffer = new byte[8192]; // Buffer de 8KB para transferencia eficiente
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            };

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(Files.size(filePath))
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600") // Cache por 1 hora
                .body(stream);
                
        } catch (Exception e) {
            log.error("Error al descargar archivo: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Descargar comprobante de pago por ID de inscripción
     * GET /api/archivos/descargar/pdf/inscripcion/{idInscripcion}
     * Este endpoint es el que usa el frontend actualmente
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idInscripcion.intValue())")
    @GetMapping("/descargar/pdf/inscripcion/{idInscripcion}")
    public ResponseEntity<?> descargarComprobantePorInscripcion(@PathVariable Long idInscripcion) {
        
        // Validar ID
        if (idInscripcion == null || idInscripcion <= 0) {
            log.error("ERROR: [ARCHIVOS] ID de inscripcion invalido: {}", idInscripcion);
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\":\"ID de inscripcion invalido\"}");
        }
        
        // 1. Buscar la inscripcion
        SolicitudCursoVeranoIncripcion inscripcion = solicitudCursoVeranoCU.buscarPorIdInscripcion(idInscripcion.intValue());
        
        if (inscripcion == null) {
            log.error("ERROR: [ARCHIVOS] Inscripcion no encontrada: {}", idInscripcion);
            return ResponseEntity.status(404)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Inscripcion no encontrada\",\"idInscripcion\":" + idInscripcion + "}");
        }
        
        
        // 2. Buscar documentos asociados
        List<Documento> documentos = inscripcion.getDocumentos();
        if (documentos == null || documentos.isEmpty()) {
            log.error("ERROR: [ARCHIVOS] No hay documentos asociados a la inscripcion: {}", idInscripcion);
            return ResponseEntity.status(404)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"No hay documentos asociados a esta inscripcion\",\"idInscripcion\":" + idInscripcion + "}");
        }
        
        
        // 3. Buscar el primer documento PDF (comprobante de pago)
        for (Documento documento : documentos) {
            
            if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                try {
                    // Lógica adaptativa: usar ruta completa si está organizada, sino usar nombre
                    String rutaDocumento = documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre();
                    Path filePathTemp;
                    
                    try {
                        if (rutaDocumento != null && rutaDocumento.contains("/")) {
                            // Ruta organizada (nueva estructura)
                            filePathTemp = objGestionarArchivos.getFileByPathAsPath(rutaDocumento);
                        } else {
                            // Ruta simple (compatibilidad hacia atrás)
                            filePathTemp = objGestionarArchivos.getFileAsPath(documento.getNombre());
                        }
                    } catch (Exception e) {
                        // Si falla con la ruta, intentar con el nombre como fallback
                        if (documento.getRuta_documento() != null && !documento.getRuta_documento().equals(documento.getNombre())) {
                            filePathTemp = objGestionarArchivos.getFileAsPath(documento.getNombre());
                        } else {
                            throw e; // Re-lanzar si no hay alternativa
                        }
                    }
                    
                    if (!Files.exists(filePathTemp) || !Files.isRegularFile(filePathTemp)) {
                        continue; // Probar el siguiente documento
                    }
                    
                    // Usar Resource para que Spring pueda serializar correctamente como application/pdf
                    Resource resource = new InputStreamResource(Files.newInputStream(filePathTemp));
                    
                    // Configurar headers para descarga
                    String contentDisposition = RespuestasArchivo.adjunto(documento.getNombre());
                    
                    return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                        .contentType(MediaType.APPLICATION_PDF)
                        .contentLength(Files.size(filePathTemp))
                        .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                        .body(resource);
                        
                } catch (Exception e) {
                    log.error("ERROR: [ARCHIVOS] Error procesando documento: {} - {}", 
                        documento.getNombre(), e.getMessage(), e);
                    continue; // Probar el siguiente documento
                }
            } else {
            }
        }
        
        log.error("ERROR: [ARCHIVOS] No se encontro ningun documento PDF valido para la inscripcion: {}", idInscripcion);
        return ResponseEntity.status(404)
            .header("Content-Type", "application/json")
            .body("{\"error\":\"No se encontro ningun comprobante PDF valido\",\"idInscripcion\":" + idInscripcion + "}");
    }
    
}
