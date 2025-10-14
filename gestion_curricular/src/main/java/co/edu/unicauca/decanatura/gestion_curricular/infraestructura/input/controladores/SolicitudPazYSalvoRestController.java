package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudPazYSalvoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Date;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
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

    @PostMapping("/crearSolicitud-PazYSalvo")
    public ResponseEntity<SolicitudPazYSalvoDTORespuesta> crearSolicitudPazYSalvo(
            @Valid @RequestBody SolicitudPazYSalvoDTOPeticion peticion) {

        SolicitudPazYSalvo solicitud = solicitudMapperDominio
                .mappearDeSolicitudDTOPeticionASolicitud(peticion);

        SolicitudPazYSalvo solicitudCreada = solicitudPazYSalvoCU.guardar(solicitud);

        ResponseEntity<SolicitudPazYSalvoDTORespuesta> respuesta = new ResponseEntity<>(
                solicitudMapperDominio.mappearDeSolicitudARespuesta(solicitudCreada),
                HttpStatus.CREATED);

        return respuesta;
    }

    @GetMapping("/listarSolicitud-PazYSalvo")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvo() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudes();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/funcionario")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoFuncionario() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudesToFuncionario();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/coordinador")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoToCoordinador() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudesToCoordinador();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/secretaria")
    public ResponseEntity<List<SolicitudPazYSalvoDTORespuesta>> listarSolicitudPazYSalvoToSecretaria() {
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudesToSecretaria();
        List<SolicitudPazYSalvoDTORespuesta> respuesta = solicitudMapperDominio.mappearListaDeSolicitudesARespuesta(solicitudes);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/listarSolicitud-PazYSalvo/{id}")
    public ResponseEntity<SolicitudPazYSalvoDTORespuesta> listarPazYSalvoById(@PathVariable Integer id) {
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(id);
        SolicitudPazYSalvoDTORespuesta respuesta = solicitudMapperDominio.mappearDeSolicitudARespuesta(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/actualizarEstadoSolicitud")
    public ResponseEntity<Void> actualizarEstadoSolicitudPazYSalvo(
            @RequestBody CambioEstadoSolicitudDTOPeticion peticion) {

        CambioEstadoSolicitud solicitud = solicitudMapper
                .mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(peticion);

        solicitudPazYSalvoCU.cambiarEstadoSolicitud(solicitud.getIdSolicitud(), solicitud.getNuevoEstado());

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint de prueba para verificar que el controlador funciona
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("✅ Controlador de Paz y Salvo funcionando correctamente");
    }

    /**
     * Subir archivo para paz y salvo (SIN asociar a solicitud - como en homologación)
     * Los documentos se asocian automáticamente cuando se crea la solicitud
     */
    @PostMapping("/subir-documento")
    public ResponseEntity<Map<String, Object>> subirDocumentoPazSalvo(
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("📁 [PAZ Y SALVO] Subiendo documento sin asociar (como en homologación)...");
            
            String nombreOriginal = file.getOriginalFilename();
            System.out.println("📁 [PAZ Y SALVO] Archivo: " + nombreOriginal);
            
            // Validaciones básicas
            if (!nombreOriginal.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Solo se permiten archivos PDF"));
            }
            
            // Validar peso máximo (10MB)
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxFileSize) {
                System.err.println("❌ [PAZ Y SALVO] Archivo demasiado grande: " + file.getSize() + " bytes");
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "Archivo demasiado grande. Máximo 10MB"));
            }
            
            // Guardar archivo
            this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf");
            
            // Crear documento SIN asociar a solicitud (como en homologación)
            Documento doc = new Documento();
            doc.setNombre(nombreOriginal);
            doc.setRuta_documento(nombreOriginal);
            doc.setFecha_documento(new Date());
            doc.setEsValido(true);
            // NO agregar comentario automático - solo funcionarios/coordinadores pueden comentar
            // NO asociar a solicitud - esto se hace después como en homologación
            
            Documento documentoGuardado = this.objGestionarDocumentosGateway.crearDocumento(doc);
            
            System.out.println("✅ [PAZ Y SALVO] Documento creado sin asociar: " + documentoGuardado.getId_documento());
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Documento subido exitosamente (sin asociar)");
            respuesta.put("documento_id", documentoGuardado.getId_documento());
            respuesta.put("nombre", nombreOriginal);
            respuesta.put("fecha", documentoGuardado.getFecha_documento());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.err.println("❌ [PAZ Y SALVO] Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.ok(errorInfo);
        }
    }

    /**
     * Descargar documento específico por nombre (igual que homologación)
     */
    @GetMapping("/descargar-documento")
    public ResponseEntity<byte[]> descargarDocumento(@RequestParam("filename") String filename) {
        try {
            System.out.println("📥 Descargando documento: " + filename);
            
            // Obtener el archivo usando el servicio de archivos
            byte[] archivo = objGestionarArchivos.getFile(filename);
            
            if (archivo == null || archivo.length == 0) {
                System.err.println("❌ Archivo no encontrado: " + filename);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("✅ Documento encontrado: " + filename);
            System.out.println("📄 Tamaño del archivo: " + archivo.length + " bytes");
            
            // Configurar respuesta igual que en homologación
            String contentDisposition = "attachment; filename=\"" + filename + "\"";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
                
        } catch (Exception e) {
            System.err.println("❌ Error al descargar documento: " + e.getMessage());
            e.printStackTrace();
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
            @RequestParam(value = "observaciones", required = false) String observaciones) {
        try {
            System.out.println("📄 Generando documento de paz y salvo para solicitud: " + idSolicitud);
            
            // Obtener la solicitud
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                System.err.println("❌ Solicitud no encontrada: " + idSolicitud);
                return ResponseEntity.notFound().build();
            }
            
            // Crear request para el generador de documentos (igual que homologación)
            Map<String, Object> datosDocumento = new HashMap<>();
            datosDocumento.put("numeroDocumento", numeroDocumento);
            datosDocumento.put("fechaDocumento", fechaDocumento);
            datosDocumento.put("observaciones", observaciones != null ? observaciones : "");
            
            Map<String, Object> datosSolicitud = new HashMap<>();
            datosSolicitud.put("nombreEstudiante", solicitud.getObjUsuario().getNombre_completo());
            datosSolicitud.put("codigoEstudiante", solicitud.getObjUsuario().getCodigo());
            datosSolicitud.put("programa", "Ingeniería Electrónica y Telecomunicaciones");
            datosSolicitud.put("fechaSolicitud", solicitud.getFecha_registro_solicitud());
            
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
            
            System.out.println("✅ Documento de paz y salvo generado: " + nombreArchivo);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(documentBytes.toByteArray());
                
        } catch (Exception e) {
            System.err.println("❌ Error al generar documento de paz y salvo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener plantillas disponibles para paz y salvo
     */
    @GetMapping("/plantillas-disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerPlantillasDisponibles() {
        try {
            System.out.println("📋 Obteniendo plantillas disponibles para paz y salvo");
            
            List<Map<String, Object>> plantillas = new ArrayList<>();
            
            Map<String, Object> plantillaPazSalvo = new HashMap<>();
            plantillaPazSalvo.put("id", "PAZ_SALVO");
            plantillaPazSalvo.put("nombre", "Paz y Salvo");
            plantillaPazSalvo.put("descripcion", "Documento que certifica que el estudiante no tiene pendientes académicos");
            plantillaPazSalvo.put("camposRequeridos", Arrays.asList("numeroDocumento", "fechaDocumento"));
            plantillaPazSalvo.put("camposOpcionales", Arrays.asList("observaciones"));
            
            plantillas.add(plantillaPazSalvo);
            
            System.out.println("✅ Plantillas obtenidas: " + plantillas.size());
            return ResponseEntity.ok(plantillas);
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener plantillas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Descargar oficio por ID de solicitud de paz y salvo
     */
    @GetMapping("/descargarOficio/{idSolicitud}")
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
     * Obtener TODOS los documentos de una solicitud de paz y salvo (incluyendo los del estudiante)
     * Para funcionarios
     */
    @GetMapping("/obtenerDocumentos/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerDocumentosPazSalvo(@PathVariable Integer idSolicitud) {
        try {
            System.out.println("🔍 [DEBUG] ===== INICIANDO OBTENCIÓN DE DOCUMENTOS =====");
            System.out.println("🔍 [DEBUG] ID Solicitud recibido: " + idSolicitud);
            
            // Obtener la solicitud con sus documentos
            System.out.println("🔍 [DEBUG] Llamando a solicitudPazYSalvoCU.buscarPorId(" + idSolicitud + ")");
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            
            if (solicitud == null) {
                System.err.println("❌ [DEBUG] Solicitud de paz y salvo no encontrada: " + idSolicitud);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("✅ [DEBUG] Solicitud encontrada:");
            System.out.println("✅ [DEBUG] - ID: " + solicitud.getId_solicitud());
            System.out.println("✅ [DEBUG] - Nombre: " + solicitud.getNombre_solicitud());
            
            // Buscar documentos asociados a esta solicitud
            System.out.println("🔍 [DEBUG] Obteniendo documentos de la solicitud...");
            List<Documento> documentos = solicitud.getDocumentos();
            
            System.out.println("🔍 [DEBUG] Documentos obtenidos: " + (documentos != null ? documentos.size() : "null"));
            
            if (documentos == null) {
                System.err.println("❌ [DEBUG] La lista de documentos es NULL");
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            if (documentos.isEmpty()) {
                System.err.println("❌ [DEBUG] La lista de documentos está VACÍA");
                System.err.println("❌ [DEBUG] No hay documentos asociados a la solicitud de paz y salvo: " + idSolicitud);
                return ResponseEntity.ok(new ArrayList<>()); // Retornar lista vacía
            }
            
            System.out.println("✅ [DEBUG] Documentos encontrados: " + documentos.size());
            
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
                    System.out.println("📋 Agregando documento: " + documento.getNombre() + " (Tipo: " + tipoDocumento + ")");
                }
            }
            
            System.out.println("✅ [DEBUG] Total documentos procesados: " + todosDocumentos.size());
            System.out.println("✅ [DEBUG] ===== FINALIZANDO OBTENCIÓN DE DOCUMENTOS =====");
            
            if (todosDocumentos.isEmpty()) {
                System.err.println("⚠️ [DEBUG] ADVERTENCIA: Se procesaron documentos pero la lista final está vacía");
            }
            
            return ResponseEntity.ok(todosDocumentos);
            
        } catch (Exception e) {
            System.err.println("❌ [DEBUG] ERROR al obtener documentos de paz y salvo: " + e.getMessage());
            System.err.println("❌ [DEBUG] Stack trace:");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint de debugging para verificar datos en la base de datos
     */
    @GetMapping("/debug/documentos/{idSolicitud}")
    public ResponseEntity<Map<String, Object>> debugDocumentos(@PathVariable Integer idSolicitud) {
        try {
            System.out.println("🔍 [DEBUG-ENDPOINT] Verificando datos para solicitud: " + idSolicitud);
            
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
            System.err.println("❌ [DEBUG-ENDPOINT] Error: " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("🔍 [DEBUG] Verificando documentos sin asociar...");
            
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
            
            System.out.println("🔍 [DEBUG] Documentos sin asociar encontrados: " + documentosSinSolicitud.size());
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("❌ [DEBUG] Error al verificar documentos sin asociar: " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("🔍 [DEBUG] Asociando documentos huérfanos a solicitud: " + idSolicitud);
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Verificar que la solicitud existe
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                resultado.put("error", "Solicitud no encontrada");
                return ResponseEntity.ok(resultado);
            }
            
            // Buscar documentos sin solicitud
            List<Documento> documentosSinSolicitud = objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
            
            int documentosAsociados = 0;
            for (Documento doc : documentosSinSolicitud) {
                // Asociar documento a la solicitud
                doc.setObjSolicitud(solicitud);
                objGestionarDocumentosGateway.actualizarDocumento(doc);
                documentosAsociados++;
                System.out.println("✅ [DEBUG] Documento asociado: " + doc.getNombre());
            }
            
            resultado.put("documentos_asociados", documentosAsociados);
            resultado.put("solicitud_id", idSolicitud);
            resultado.put("mensaje", "Documentos asociados exitosamente");
            
            System.out.println("✅ [DEBUG] Total documentos asociados: " + documentosAsociados);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("❌ [DEBUG] Error al asociar documentos: " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("🔍 [ASOCIACIÓN] Asociando documentos huérfanos a solicitud ID: " + idSolicitud);
            
            // Obtener la solicitud
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Solicitud ID " + idSolicitud + " no encontrada");
                return ResponseEntity.ok(error);
            }
            
            // Buscar documentos sin asociar (como en homologación)
            List<Documento> documentosSinSolicitud = objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
            
            System.out.println("🔍 [ASOCIACIÓN] Documentos sin asociar encontrados: " + documentosSinSolicitud.size());
            
            int documentosAsociados = 0;
            for (Documento doc : documentosSinSolicitud) {
                // Asociar documento a la solicitud (igual que en homologación)
                doc.setObjSolicitud(solicitud);
                objGestionarDocumentosGateway.actualizarDocumento(doc);
                documentosAsociados++;
                System.out.println("✅ [ASOCIACIÓN] Documento asociado: " + doc.getNombre());
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("documentos_asociados", documentosAsociados);
            resultado.put("solicitud_id", idSolicitud);
            resultado.put("mensaje", "Documentos asociados exitosamente");
            
            System.out.println("✅ [ASOCIACIÓN] Total documentos asociados: " + documentosAsociados);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("❌ [ASOCIACIÓN] Error: " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("📋 [COORDINADOR] Obteniendo TODOS los documentos de paz y salvo para solicitud: " + idSolicitud);
            
            // Obtener la solicitud con sus documentos
            SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
            if (solicitud == null) {
                System.err.println("❌ [COORDINADOR] Solicitud de paz y salvo no encontrada: " + idSolicitud);
                return ResponseEntity.notFound().build();
            }
            
            // Buscar documentos asociados a esta solicitud
            List<Documento> documentos = solicitud.getDocumentos();
            if (documentos == null || documentos.isEmpty()) {
                System.err.println("❌ [COORDINADOR] No hay documentos asociados a la solicitud de paz y salvo: " + idSolicitud);
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
                    System.out.println("📋 [COORDINADOR] Agregando documento: " + documento.getNombre() + " (Tipo: " + tipoDocumento + ")");
                }
            }
            
            System.out.println("✅ [COORDINADOR] Total documentos encontrados: " + todosDocumentos.size());
            return ResponseEntity.ok(todosDocumentos);
            
        } catch (Exception e) {
            System.err.println("❌ [COORDINADOR] Error al obtener documentos de paz y salvo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener oficios disponibles para una solicitud de paz y salvo (solo oficios/resoluciones)
     */
    @GetMapping("/obtenerOficios/{idSolicitud}")
    public ResponseEntity<List<Map<String, Object>>> obtenerOficiosPazSalvo(@PathVariable Integer idSolicitud) {
        try {
            System.out.println("📋 Obteniendo oficios de paz y salvo para solicitud: " + idSolicitud);
            
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
                return ResponseEntity.ok(new ArrayList<>()); // Retornar lista vacía
            }
            
            // Crear lista de oficios basada en los documentos reales (solo oficios/resoluciones)
            List<Map<String, Object>> oficios = new ArrayList<>();
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
                        Map<String, Object> oficio = new HashMap<>();
                        oficio.put("id", idSolicitud);
                        oficio.put("nombre", documento.getNombre());
                        oficio.put("nombreArchivo", documento.getNombre());
                        oficio.put("ruta", documento.getRuta_documento());
                        oficios.add(oficio);
                        System.out.println("📋 Agregando oficio/resolución de paz y salvo: " + documento.getNombre());
                    } else {
                        System.out.println("⏭️ Saltando archivo del estudiante: " + documento.getNombre());
                    }
                }
            }
            
            System.out.println("✅ Oficios de paz y salvo encontrados: " + oficios.size());
            return ResponseEntity.ok(oficios);
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener oficios de paz y salvo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Validar documentos requeridos para paz y salvo
     */
    @GetMapping("/validarDocumentosRequeridos/{idSolicitud}")
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
        System.out.println("📄 Guardando oficio de paz y salvo para solicitud: " + idSolicitud);
        System.out.println("📄 Tipo documento: " + tipoDocumento);
        System.out.println("📄 Número documento: " + numeroDocumento);
        System.out.println("📄 Fecha documento: " + fechaDocumento);
        
        String nombreOriginal = file.getOriginalFilename();
        System.out.println("📄 Nombre archivo: " + nombreOriginal);
        
        // Validaciones
        if (file.isEmpty()) {
            System.err.println("❌ Archivo vacío");
            return ResponseEntity.badRequest().body(null);
        }
        
        if (!nombreOriginal.toLowerCase().endsWith(".docx")) {
            System.err.println("❌ Tipo de archivo no válido: " + nombreOriginal);
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null);
        }
        
        // Guardar archivo
        this.objGestionarArchivos.saveFile(file, nombreOriginal, "docx");
        
        // Crear documento
        Documento doc = new Documento();
        doc.setNombre(nombreOriginal);
        doc.setRuta_documento(nombreOriginal);
        doc.setFecha_documento(new Date());
        doc.setEsValido(true);
        
        // Asociar solicitud
        SolicitudPazYSalvo solicitud = solicitudPazYSalvoCU.buscarPorId(idSolicitud);
        if (solicitud != null) {
            Solicitud objSolicitud = new Solicitud();
            objSolicitud.setId_solicitud(idSolicitud);
            doc.setObjSolicitud(objSolicitud);
            System.out.println("�� Asociando oficio '" + nombreOriginal + "' a solicitud de paz y salvo ID: " + idSolicitud);
        } else {
            System.err.println("❌ No se encontró la solicitud de paz y salvo con ID: " + idSolicitud);
            return ResponseEntity.notFound().build();
        }
        
        // Guardar documento en BD
        Documento documentoGuardado = this.objGestionarDocumentosGateway.crearDocumento(doc);
        
        ResponseEntity<DocumentosDTORespuesta> respuesta = new ResponseEntity<>(
            documentosMapperDominio.mappearDeDocumentoADTORespuesta(documentoGuardado), 
            HttpStatus.CREATED
        );
        
        System.out.println("✅ Oficio de paz y salvo guardado exitosamente: " + nombreOriginal);
        return respuesta;
        
    } catch (Exception e) {
        System.err.println("❌ Error al guardar oficio de paz y salvo: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
}
