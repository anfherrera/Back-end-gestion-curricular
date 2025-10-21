package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;

import org.springframework.web.multipart.MultipartFile;



@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Validated
public class ArchivosRestController {
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCursoVeranoCU;
    @PostMapping("/subir/pdf")
    public ResponseEntity<Map<String, Object>> subirPDF(
            @RequestParam(name = "file", required = true) MultipartFile file,
            @RequestParam(name = "inscripcionId", required = false) String inscripcionId) {
        try {
            System.out.println("📁 [INSCRIPCIONES] ===== INICIANDO SUBIDA DE ARCHIVO PDF =====");
            System.out.println("📁 [INSCRIPCIONES] Archivo: " + file.getOriginalFilename());
            System.out.println("📁 [INSCRIPCIONES] Tamaño: " + file.getSize() + " bytes");
            System.out.println("📁 [INSCRIPCIONES] Tipo: " + file.getContentType());
            System.out.println("📁 [INSCRIPCIONES] Inscripción ID: " + inscripcionId);
            System.out.println("📁 [INSCRIPCIONES] Archivo vacío: " + file.isEmpty());
            
            String nombreOriginal = file.getOriginalFilename();
            System.out.println("📁 [INSCRIPCIONES] Nombre original procesado: " + nombreOriginal);
            
            // 1. Validar que se proporcionó archivo
            if (file.isEmpty()) {
                System.err.println("❌ [INSCRIPCIONES] No se proporcionó archivo");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se proporcionó archivo");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // 2. Validar peso máximo (15MB = 15 * 1024 * 1024 bytes)
            long maxFileSize = 15 * 1024 * 1024; // 15MB
            if (file.getSize() > maxFileSize) {
                System.err.println("❌ [INSCRIPCIONES] Archivo demasiado grande: " + file.getSize() + " bytes (máximo: " + maxFileSize + " bytes)");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Archivo demasiado grande. Máximo permitido: 15MB");
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
            }
            
            // 3. Validar tipo de archivo
            if (!file.getContentType().equals("application/pdf")) {
                System.err.println("❌ [INSCRIPCIONES] Tipo de archivo no válido: " + file.getContentType());
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Solo se permiten archivos PDF");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            System.out.println("✅ [INSCRIPCIONES] Validaciones pasadas, guardando archivo...");
            
            // 4. Guardar archivo con manejo de errores mejorado
            String nombreArchivo;
            try {
                // Generar nombre usando el nombre original del archivo + nombre del estudiante
                String nombreUnico = nombreOriginal;
                if (inscripcionId != null && !inscripcionId.trim().isEmpty()) {
                    try {
                        // Buscar la inscripción para obtener el nombre del estudiante
                        List<SolicitudCursoVeranoIncripcion> todasLasInscripciones = solicitudCursoVeranoCU.buscarInscripcionesPorCurso(1); // Buscar en curso 1
                        SolicitudCursoVeranoIncripcion inscripcion = null;
                        
                        for (SolicitudCursoVeranoIncripcion ins : todasLasInscripciones) {
                            if (ins.getId_solicitud().equals(Integer.parseInt(inscripcionId))) {
                                inscripcion = ins;
                                break;
                            }
                        }
                        
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
                                System.out.println("✅ [INSCRIPCIONES] Nombre generado con estudiante: " + nombreUnico);
                            } else {
                                // Si no se puede obtener el nombre del estudiante, usar solo el ID
                                String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
                                String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
                                nombreUnico = nombreSinExtension + "_" + inscripcionId + extension;
                                System.out.println("⚠️ [INSCRIPCIONES] No se pudo obtener nombre del estudiante, usando solo ID: " + nombreUnico);
                            }
                        } else {
                            // Si no se encuentra la inscripción, usar solo el ID
                            String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
                            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
                            nombreUnico = nombreSinExtension + "_" + inscripcionId + extension;
                            System.out.println("⚠️ [INSCRIPCIONES] Inscripción no encontrada, usando solo ID: " + nombreUnico);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ [INSCRIPCIONES] Error al obtener información del estudiante: " + e.getMessage());
                        // En caso de error, usar solo el ID
                        String nombreSinExtension = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
                        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
                        nombreUnico = nombreSinExtension + "_" + inscripcionId + extension;
                    }
                }
                nombreArchivo = this.objGestionarArchivos.saveFile(file, nombreUnico, "pdf");
                System.out.println("✅ [INSCRIPCIONES] Archivo guardado exitosamente: " + nombreArchivo);
            } catch (Exception saveError) {
                System.err.println("❌ [INSCRIPCIONES] Error al guardar archivo: " + saveError.getMessage());
                saveError.printStackTrace();
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error al guardar archivo: " + saveError.getMessage());
                error.put("detalle", "Verificar permisos de escritura en carpeta de archivos");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
            
            // 5. Si se proporciona ID de inscripción, crear documento en BD
            if (inscripcionId != null && !inscripcionId.trim().isEmpty()) {
                try {
                    System.out.println("📋 [INSCRIPCIONES] Creando documento en BD para inscripción ID: " + inscripcionId);
                    
                    // Crear documento
                    Documento documento = new Documento();
                    documento.setNombre(nombreArchivo);
                    documento.setRuta_documento(nombreArchivo);
                    documento.setFecha_documento(new java.util.Date());
                    documento.setEsValido(true);
                    documento.setComentario("Comprobante de pago - Inscripción curso verano");
                    
                    // Asociar a inscripción
                    co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion solicitud = 
                        new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion();
                    solicitud.setId_solicitud(Integer.parseInt(inscripcionId));
                    documento.setObjSolicitud(solicitud);
                    
                    // Guardar documento en BD
                    Documento documentoGuardado = objGestionarDocumentosGateway.crearDocumento(documento);
                    
                    if (documentoGuardado != null) {
                        System.out.println("✅ [INSCRIPCIONES] Documento guardado en BD ID: " + documentoGuardado.getId_documento());
                    } else {
                        System.out.println("⚠️ [INSCRIPCIONES] Error al guardar documento en BD");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ [INSCRIPCIONES] Error al crear documento en BD: " + e.getMessage());
                    e.printStackTrace();
                    // No fallar la operación por esto, pero logear el error
                }
            }
            
            // 6. Crear respuesta en el formato requerido
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", System.currentTimeMillis()); // ID temporal
            respuesta.put("nombre", nombreOriginal);
            respuesta.put("ruta", "/uploads/archivos/" + nombreArchivo);
            respuesta.put("tamaño", file.getSize());
            respuesta.put("tipo", file.getContentType());
            respuesta.put("fechaSubida", new java.util.Date().toString());
            
            if (inscripcionId != null && !inscripcionId.trim().isEmpty()) {
                respuesta.put("inscripcionId", inscripcionId);
            }
            
            System.out.println("✅ [INSCRIPCIONES] Archivo subido exitosamente: " + nombreOriginal);
            return ResponseEntity.ok(respuesta);
                    } catch (Exception e) {
            System.err.println("❌ [INSCRIPCIONES] Error al subir PDF: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint de prueba para verificar que el manejo de archivos multipart funciona
     * POST /api/archivos/test-upload
     */
    @PostMapping("/test-upload")
    public ResponseEntity<Map<String, Object>> testUpload(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "inscripcionId", required = false) String inscripcionId) {
        try {
            System.out.println("🧪 [TEST] Endpoint de prueba de subida de archivos");
            System.out.println("🧪 [TEST] Archivo recibido: " + (file != null ? "SÍ" : "NO"));
            System.out.println("🧪 [TEST] Inscripción ID: " + inscripcionId);
            
            if (file != null) {
                System.out.println("🧪 [TEST] Nombre archivo: " + file.getOriginalFilename());
                System.out.println("🧪 [TEST] Tamaño: " + file.getSize() + " bytes");
                System.out.println("🧪 [TEST] Tipo: " + file.getContentType());
                System.out.println("🧪 [TEST] Vacío: " + file.isEmpty());
            }
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Endpoint de prueba funcionando correctamente");
            respuesta.put("archivo_recibido", file != null);
            respuesta.put("inscripcion_id", inscripcionId);
            
            if (file != null) {
                respuesta.put("nombre_archivo", file.getOriginalFilename());
                respuesta.put("tamaño_archivo", file.getSize());
                respuesta.put("tipo_archivo", file.getContentType());
            }
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.err.println("❌ [TEST] Error en endpoint de prueba: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en endpoint de prueba: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/descargar/pdf")
    public ResponseEntity<byte[]> bajarPDF(@RequestParam(name = "filename", required = true) String filename) {
        byte[] archivos = null;
        try {
            archivos = this.objGestionarArchivos.getFile(filename);
            return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(archivos);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Descargar oficio de paz y salvo por ID de solicitud
     */
    @GetMapping("/descargarOficioPazSalvo/{idSolicitud}")
    public ResponseEntity<byte[]> descargarOficioPazSalvo(@PathVariable Integer idSolicitud) {
        try {
            System.out.println("📥 Descargando oficio de paz y salvo para solicitud: " + idSolicitud);
            
            // Obtener la solicitud con sus documentos
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                System.err.println("❌ Solicitud de paz y salvo no encontrada: " + idSolicitud);
                return ResponseEntity.notFound().build();
            }
            
            // Buscar documentos asociados a esta solicitud
            List<Documento> documentos = solicitud.getDocumentos();
            if (documentos == null || documentos.isEmpty()) {
                System.err.println("❌ No hay documentos asociados a la solicitud de paz y salvo: " + idSolicitud);
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
                            System.out.println("🔍 Probando oficio/resolución de paz y salvo: " + documento.getNombre());
                            byte[] archivo = objGestionarArchivos.getFile(documento.getNombre());
                            
                            System.out.println("✅ Oficio/resolución de paz y salvo encontrado: " + documento.getNombre());
                            
                            System.out.println("📄 Configurando respuesta para archivo: " + documento.getNombre());
                            System.out.println("📄 Tamaño del archivo: " + archivo.length + " bytes");
                            
                            // Configurar el header Content-Disposition correctamente
                            String contentDisposition = "attachment; filename=\"" + documento.getNombre() + "\"";
                            System.out.println("📄 Content-Disposition: " + contentDisposition);
                            
                            return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(archivo);
                                
                        } catch (Exception e) {
                            System.out.println("❌ No encontrado: " + documento.getNombre());
                            continue; // Probar el siguiente documento
                        }
                    } else {
                        System.out.println("⏭️ Saltando archivo del estudiante: " + documento.getNombre());
                    }
                }
            }
            
            System.err.println("❌ No se encontró ningún archivo PDF para la solicitud de paz y salvo: " + idSolicitud);
            return ResponseEntity.notFound().build();
                
        } catch (Exception e) {
            System.err.println("❌ Error al descargar oficio de paz y salvo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Validar documentos requeridos para paz y salvo
     */
    @GetMapping("/validarDocumentosRequeridosPazSalvo/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> validarDocumentosRequeridosPazSalvo(@PathVariable Integer idSolicitud) {
        try {
            System.out.println("📋 Validando documentos requeridos para solicitud de paz y salvo: " + idSolicitud);
            
            // Obtener la solicitud con sus documentos
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<Documento> documentos = solicitud.getDocumentos();
            if (documentos == null) {
                documentos = new ArrayList<>();
            }
            
            // Documentos requeridos para paz y salvo
            Map<String, Boolean> documentosRequeridos = new HashMap<>();
            documentosRequeridos.put("formato_pm_fo_4_for_27", false);
            documentosRequeridos.put("autorizacion_publicar", false);
            documentosRequeridos.put("hoja_vida_academica", false);
            documentosRequeridos.put("comprobante_pago", false);
            documentosRequeridos.put("documento_trabajo_grado", false);
            documentosRequeridos.put("resultado_saber_pro", false); // Opcional
            
            // Verificar qué documentos están presentes
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
            
            // Calcular si todos los documentos obligatorios están presentes
            boolean todosCompletos = documentosRequeridos.get("formato_pm_fo_4_for_27") &&
                                   documentosRequeridos.get("autorizacion_publicar") &&
                                   documentosRequeridos.get("hoja_vida_academica") &&
                                   documentosRequeridos.get("comprobante_pago") &&
                                   documentosRequeridos.get("documento_trabajo_grado");
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("documentosRequeridos", documentosRequeridos);
            resultado.put("todosCompletos", todosCompletos);
            resultado.put("totalDocumentos", documentos.size());
            
            System.out.println("✅ Validación de paz y salvo completada. Todos completos: " + todosCompletos);
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("❌ Error al validar documentos de paz y salvo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
