package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;

import org.springframework.web.multipart.MultipartFile;



@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Validated
public class ArchivosRestController {
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final DocumentosMapperDominio documentosMapperDominio;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
    private final GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;
    private final GestionarSolicitudReingresoCUIntPort solicitudReingresoCU;
    @PostMapping("/subir/pdf")
    public ResponseEntity<DocumentosDTORespuesta> subirPDF(
            @RequestParam(name = "file", required = true) MultipartFile file,
            @RequestParam(name = "idSolicitud", required = false) Integer idSolicitud) {
        try {
            String nombreOriginal = file.getOriginalFilename(); // ← nombre real del archivo
            
            // Validaciones
            System.out.println("📁 Validando archivo: " + nombreOriginal);
            
            // 1. Validar peso máximo (10MB = 10 * 1024 * 1024 bytes)
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxFileSize) {
                System.err.println("❌ Archivo demasiado grande: " + file.getSize() + " bytes (máximo: " + maxFileSize + " bytes)");
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(null);
            }
            
            // 2. Validar que no sea un archivo duplicado
            if (idSolicitud != null) {
                try {
                    // Intentar buscar en solicitudes de homologación
                    try {
                        SolicitudHomologacion solicitudHomologacion = solicitudHomologacionCU.buscarPorId(idSolicitud);
                        if (solicitudHomologacion != null && solicitudHomologacion.getDocumentos() != null) {
                            for (Documento doc : solicitudHomologacion.getDocumentos()) {
                                if (doc.getNombre() != null && doc.getNombre().equals(nombreOriginal)) {
                                    System.err.println("❌ Archivo duplicado en homologación: " + nombreOriginal);
                                    return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(null);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Si no es homologación, intentar con paz y salvo
                        try {
                            SolicitudPazYSalvo solicitudPazSalvo = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
                            if (solicitudPazSalvo != null && solicitudPazSalvo.getDocumentos() != null) {
                                for (Documento doc : solicitudPazSalvo.getDocumentos()) {
                                    if (doc.getNombre() != null && doc.getNombre().equals(nombreOriginal)) {
                                        System.err.println("❌ Archivo duplicado en paz y salvo: " + nombreOriginal);
                                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                            .body(null);
                                    }
                                }
                            }
                        } catch (Exception e2) {
                            // Si no es paz y salvo, intentar con reingreso
                            try {
                                SolicitudReingreso solicitudReingreso = solicitudReingresoCU.obtenerSolicitudReingresoPorId(idSolicitud);
                                if (solicitudReingreso != null && solicitudReingreso.getDocumentos() != null) {
                                    for (Documento doc : solicitudReingreso.getDocumentos()) {
                                        if (doc.getNombre() != null && doc.getNombre().equals(nombreOriginal)) {
                                            System.err.println("❌ Archivo duplicado en reingreso: " + nombreOriginal);
                                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                                .body(null);
                                        }
                                    }
                                }
                            } catch (Exception e3) {
                                System.err.println("⚠️ Error al verificar duplicados en reingreso: " + e3.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error al verificar duplicados: " + e.getMessage());
                }
            }
            
            // 3. Validar tipo de archivo
            if (!nombreOriginal.toLowerCase().endsWith(".pdf")) {
                System.err.println("❌ Tipo de archivo no válido: " + nombreOriginal);
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(null);
            }
            
            System.out.println("✅ Validaciones pasadas, guardando archivo...");
            this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf"); // ← guardar archivo
            
            Documento doc = new Documento();
            doc.setNombre(nombreOriginal);
            doc.setRuta_documento(nombreOriginal);
            doc.setFecha_documento(new Date());
            doc.setEsValido(true);
            
            // Asociar solicitud si se proporciona idSolicitud
            if (idSolicitud != null) {
                try {
                    // Intentar obtener la solicitud de homologación primero
                    try {
                        SolicitudHomologacion solicitudHomologacion = solicitudHomologacionCU.buscarPorId(idSolicitud);
                        if (solicitudHomologacion != null) {
                            // Crear objeto Solicitud para asociar
                            Solicitud objSolicitud = new Solicitud();
                            objSolicitud.setId_solicitud(idSolicitud);
                            doc.setObjSolicitud(objSolicitud);
                            System.out.println("📎 Asociando archivo '" + nombreOriginal + "' a solicitud de homologación ID: " + idSolicitud);
                        }
                    } catch (Exception e) {
                        // Si no es homologación, intentar con paz y salvo
                        try {
                            SolicitudPazYSalvo solicitudPazSalvo = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
                            if (solicitudPazSalvo != null) {
                                // Crear objeto Solicitud para asociar
                                Solicitud objSolicitud = new Solicitud();
                                objSolicitud.setId_solicitud(idSolicitud);
                                doc.setObjSolicitud(objSolicitud);
                                System.out.println("📎 Asociando archivo '" + nombreOriginal + "' a solicitud de paz y salvo ID: " + idSolicitud);
                            } else {
                                // Si no es paz y salvo, intentar con reingreso
                                try {
                                    System.out.println("🔍 Intentando obtener solicitud de reingreso con ID: " + idSolicitud);
                                    SolicitudReingreso solicitudReingreso = solicitudReingresoCU.obtenerSolicitudReingresoPorId(idSolicitud);
                                    if (solicitudReingreso != null) {
                                        // Crear objeto Solicitud para asociar
                                        Solicitud objSolicitud = new Solicitud();
                                        objSolicitud.setId_solicitud(idSolicitud);
                                        doc.setObjSolicitud(objSolicitud);
                                        System.out.println("✅ Asociando archivo '" + nombreOriginal + "' a solicitud de reingreso ID: " + idSolicitud);
                                        System.out.println("📋 Documento antes de guardar - idSolicitud: " + (doc.getObjSolicitud() != null ? doc.getObjSolicitud().getId_solicitud() : "NULL"));
                                    } else {
                                        System.out.println("❌ No se encontró solicitud de reingreso con ID: " + idSolicitud);
                                    }
                                } catch (Exception e3) {
                                    System.err.println("❌ Error al obtener solicitud de reingreso: " + e3.getMessage());
                                    e3.printStackTrace();
                                }
                            }
                        } catch (Exception e2) {
                            // Si no es paz y salvo, intentar con reingreso
                            try {
                                SolicitudReingreso solicitudReingreso = solicitudReingresoCU.obtenerSolicitudReingresoPorId(idSolicitud);
                                if (solicitudReingreso != null) {
                                    // Crear objeto Solicitud para asociar
                                    Solicitud objSolicitud = new Solicitud();
                                    objSolicitud.setId_solicitud(idSolicitud);
                                    doc.setObjSolicitud(objSolicitud);
                                    System.out.println("📎 Asociando archivo '" + nombreOriginal + "' a solicitud de reingreso ID: " + idSolicitud);
                                }
                            } catch (Exception e3) {
                                System.err.println("❌ Error al obtener solicitud de reingreso: " + e3.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al obtener solicitud: " + e.getMessage());
                }
            }
            
            Documento documentoGuardado = this.objGestionarDocumentosGateway.crearDocumento(doc);
            ResponseEntity<DocumentosDTORespuesta> respuesta = new ResponseEntity<DocumentosDTORespuesta>(
                documentosMapperDominio.mappearDeDocumentoADTORespuesta(documentoGuardado), HttpStatus.CREATED
            );
            return respuesta;
        } catch (Exception e) {
            System.err.println("❌ Error al subir PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
