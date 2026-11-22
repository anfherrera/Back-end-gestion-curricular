package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PdfSubidoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ArchivoMultipart;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorArchivoPdf;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

@Slf4j
@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Validated
public class ArchivosRestController {
    private final GestionarArchivosCUIntPort gestionarArchivos;
    private final GestionarDocumentosCUIntPort documentosCU;
    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCursoVeranoCU;
    private final GestionarSolicitudCUIntPort solicitudCU;
    private final Autorizacion autorizacion;

    /**
     * Sube un PDF y lo registra. Si trae el id de una solicitud ({@code inscripcionId}, {@code solicitudId} o
     * {@code idSolicitud}, en ese orden), el id se recorta y debe ser el de una solicitud existente (BUG-20):
     * 400 si no es un número y 404 si no existe. Una inscripción de verano se organiza en su carpeta; cualquier
     * otra solicitud (por ejemplo, un reingreso que sube la secretaría) se asocia tal cual.
     */
    @PostMapping
    public ResponseEntity<?> subirPDF(
        @RequestParam(name = "file", required = true) MultipartFile file,
        @RequestParam(name = "inscripcionId", required = false) String inscripcionId,
        @RequestParam(name = "solicitudId", required = false) String solicitudId,
        @RequestParam(name = "idSolicitud", required = false) String idSolicitudAlias,
        @RequestParam(name = "tipoSolicitud", required = false) String tipoSolicitud) {

        String inscripcionIdTexto = conTexto(inscripcionId);
        String solicitudIdTexto = inscripcionIdTexto != null ? inscripcionIdTexto
            : conTexto(solicitudId) != null ? conTexto(solicitudId) : conTexto(idSolicitudAlias);

        Integer idSolicitudDestino = null;
        if (solicitudIdTexto != null) {
            try {
                idSolicitudDestino = Integer.parseInt(solicitudIdTexto);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(new PdfSubidoDTORespuesta.Error("ID de solicitud inválido"));
            }
            // Solo el staff o el dueño pueden adjuntar archivos a una solicitud existente
            if (!autorizacion.esStaff() && !autorizacion.esDuenoSolicitud(idSolicitudDestino)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PdfSubidoDTORespuesta.Error("No puede adjuntar archivos a una solicitud que no es suya"));
            }
        }

        String nombreOriginal = file.getOriginalFilename();

        // Validar que sea un PDF real (contenido, no solo nombre o Content-Type) y el tamaño máximo
        ValidadorArchivoPdf.Rechazo rechazo = ValidadorArchivoPdf.validar(file, ValidadorArchivoPdf.MAXIMO_15MB);
        if (rechazo != null) {
            return ResponseEntity.status(rechazo.estado()).body(new PdfSubidoDTORespuesta.Error(rechazo.mensaje()));
        }

        // La solicitud de destino: una inscripción de verano o, si no lo es, cualquier otra solicitud
        SolicitudCursoVeranoInscripcion inscripcion = null;
        Solicitud destino = null;
        if (idSolicitudDestino != null) {
            try {
                inscripcion = solicitudCursoVeranoCU.buscarPorIdInscripcion(idSolicitudDestino);
            } catch (Exception e) {
                log.warn("No se pudo consultar la inscripción {}", idSolicitudDestino, e);
            }
            destino = inscripcion != null ? inscripcion : solicitudCU.obtenerSolicitudPorId(idSolicitudDestino);
            if (destino == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new PdfSubidoDTORespuesta.Error("Solicitud no encontrada"));
            }
        }

        // Con inscripcionId, el nombre lleva el del estudiante (si lo hay) y el id
        String nombreUnico = nombreOriginal;
        if (inscripcionIdTexto != null) {
            String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
            String nombreEstudiante = inscripcion != null && inscripcion.getObjUsuario() != null
                ? inscripcion.getObjUsuario().getNombreCompleto() : null;
            nombreUnico = nombreEstudiante != null && !nombreEstudiante.trim().isEmpty()
                ? nombreSinExtension + "_" + nombreEstudiante.replaceAll("[^a-zA-Z0-9]", "_") + "_"
                    + inscripcionIdTexto + extension
                : nombreSinExtension + "_" + inscripcionIdTexto + extension;
        }

        // Carpeta: la de tipoSolicitud si viene con id; sin tipo, la de la inscripción de verano; si no, la raíz
        String nombreArchivo;
        if (conTexto(tipoSolicitud) != null && idSolicitudDestino != null) {
            nombreArchivo = this.gestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf", conTexto(tipoSolicitud),
                idSolicitudDestino);
        } else if (inscripcionIdTexto != null && inscripcion != null) {
            try {
                nombreArchivo = this.gestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf",
                    "curso-verano", inscripcion.getIdSolicitud());
            } catch (Exception e) {
                // Si falla la carpeta de la inscripción, se reintenta en la raíz
                nombreArchivo = this.gestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf");
            }
        } else {
            nombreArchivo = this.gestionarArchivos.saveFile(ArchivoMultipart.de(file), nombreUnico, "pdf");
        }

        boolean documentoGuardadoEnBD = false;
        try {
            // Si no trae solicitud, se asociará a la próxima solicitud que cree este mismo usuario
            Integer idUsuarioSubio = autorizacion.idUsuarioActual();
            documentoGuardadoEnBD = documentosCU.registrarArchivoGuardado(nombreArchivo, destino, idUsuarioSubio) != null;
        } catch (Exception e) {
            log.error("Error al guardar documento en BD: {}", nombreArchivo, e);
        }

        return ResponseEntity.ok(new PdfSubidoDTORespuesta(
            System.currentTimeMillis(),
            nombreOriginal,
            "/uploads/archivos/" + nombreArchivo,
            file.getSize(),
            file.getContentType(),
            new Date(),
            documentoGuardadoEnBD,
            inscripcionIdTexto,
            solicitudIdTexto,
            conTexto(tipoSolicitud)));
    }

    /** El valor recortado si tiene texto; si no, null (y la clave se omite en la respuesta). */
    private static String conTexto(String valor) {
        return valor != null && !valor.trim().isEmpty() ? valor.trim() : null;
    }

    /** Descarga un PDF por su nombre: {@code GET /api/archivos?nombre=…}. */
    @GetMapping
    public ResponseEntity<StreamingResponseBody> bajarPDF(@RequestParam(name = "nombre", required = true) String filename) {
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
            Path filePath = gestionarArchivos.getFileAsPath(rutaArchivo);
            
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
     * GET /api/archivos/inscripciones/{idInscripcion}
     * Este endpoint es el que usa el frontend actualmente
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esDuenoSolicitud(#idInscripcion.intValue())")
    @GetMapping("/inscripciones/{idInscripcion:\\d+}")
    public ResponseEntity<?> descargarComprobantePorInscripcion(@PathVariable Long idInscripcion) {
        
        // Validar ID
        if (idInscripcion == null || idInscripcion <= 0) {
            log.error("ERROR: [ARCHIVOS] ID de inscripcion invalido: {}", idInscripcion);
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\":\"ID de inscripcion invalido\"}");
        }
        
        // 1. Buscar la inscripcion
        SolicitudCursoVeranoInscripcion inscripcion = solicitudCursoVeranoCU.buscarPorIdInscripcion(idInscripcion.intValue());
        
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
                    String rutaDocumento = documento.getRutaDocumento() != null ? documento.getRutaDocumento() : documento.getNombre();
                    Path filePathTemp;
                    
                    try {
                        if (rutaDocumento != null && rutaDocumento.contains("/")) {
                            // Ruta organizada (nueva estructura)
                            filePathTemp = gestionarArchivos.getFileByPathAsPath(rutaDocumento);
                        } else {
                            // Ruta simple (compatibilidad hacia atrás)
                            filePathTemp = gestionarArchivos.getFileAsPath(documento.getNombre());
                        }
                    } catch (Exception e) {
                        // Si falla con la ruta, intentar con el nombre como fallback
                        if (documento.getRutaDocumento() != null && !documento.getRutaDocumento().equals(documento.getNombre())) {
                            filePathTemp = gestionarArchivos.getFileAsPath(documento.getNombre());
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
