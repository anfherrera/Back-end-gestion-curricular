package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCurosoVeranoPreinscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoVeranoInscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CursosOfertadosDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoInscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoInscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solicitudes-curso-verano")
@RequiredArgsConstructor
public class SolicitudCursoVeranoRestController {

   
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudCursoVeranoPreinscripcionMapper;
    private final SolicitudCursoDeVeranoInscripcionMapperDominio solicitudCursoDeVeranoInscripcionMapper;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;


    
    @PostMapping("/crearCursoVeranoPreinscripcion")
    public ResponseEntity<SolicitudCursoVeranoPreinscripcionDTORespuesta> crearSolicitudCursoVeranoPreinscripcion(
            @RequestBody SolicitudCurosoVeranoPreinscripcionDTOPeticion solicitud) {

        SolicitudCursoVeranoPreinscripcion solicitudDominio = solicitudCursoVeranoPreinscripcionMapper
                .mappearDePeticionASolicitudCursoVeranoPreinscripcion(solicitud);

        SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU
                .crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

        SolicitudCursoVeranoPreinscripcionDTORespuesta respuesta = solicitudCursoVeranoPreinscripcionMapper
                .mappearDeSolicitudCursoVeranoPreinscripcionARespuesta(solicitudGuardada);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    @PostMapping("/crearCursoVeranoInscripcion")
    public ResponseEntity<SolicitudCursoVeranoInscripcionDTORespuesta> crearSolicitudCursoVeranoInscripcion(
            @RequestBody SolicitudCursoVeranoInscripcionDTOPeticion solicitud) {

        SolicitudCursoVeranoIncripcion solicitudDominio = solicitudCursoDeVeranoInscripcionMapper
                .mappearDePeticionASolicitudCursoVeranoIncripcion(solicitud);

        SolicitudCursoVeranoIncripcion solicitudGuardada = solicitudCU
                .crearSolicitudCursoVeranoInscripcion(solicitudDominio);

        SolicitudCursoVeranoInscripcionDTORespuesta respuesta = solicitudCursoDeVeranoInscripcionMapper
                .mappearDeSolicitudCursoVeranoIncripcionARespuesta(solicitudGuardada);

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * Subir comprobante de pago para inscripción
     */
    @PostMapping("/subirComprobantePago")
    public ResponseEntity<Map<String, Object>> subirComprobantePago(
            @RequestParam(name = "file", required = true) MultipartFile file,
            @RequestParam(name = "idSolicitud", required = true) @Min(value = 1) Integer idSolicitud) {
        try {
            System.out.println("📤 [SUBIR_COMPROBANTE] Iniciando subida de comprobante...");
            System.out.println("📤 [SUBIR_COMPROBANTE] ID Solicitud: " + idSolicitud);
            System.out.println("📤 [SUBIR_COMPROBANTE] Archivo: " + file.getOriginalFilename());
            
            String nombreOriginal = file.getOriginalFilename();
            
            if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nombre de archivo no válido"));
            }
            
            if (!nombreOriginal.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Solo se permiten archivos PDF"));
            }
            
            // 1. Verificar que la inscripción existe
            System.out.println("🔍 [SUBIR_COMPROBANTE] Verificando inscripción ID: " + idSolicitud);
            SolicitudCursoVeranoIncripcion inscripcionExistente = solicitudCU.buscarPorIdInscripcion(idSolicitud);
            
            if (inscripcionExistente == null) {
                System.out.println("❌ [SUBIR_COMPROBANTE] Inscripción no encontrada: " + idSolicitud);
                return ResponseEntity.badRequest().body(Map.of("error", "Inscripción no encontrada"));
            }
            
            System.out.println("✅ [SUBIR_COMPROBANTE] Inscripción encontrada: " + inscripcionExistente.getNombre_solicitud());
            
            // 2. Guardar archivo
            System.out.println("💾 [SUBIR_COMPROBANTE] Guardando archivo: " + nombreOriginal);
            this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf");
            
            // 3. Crear documento
            System.out.println("📄 [SUBIR_COMPROBANTE] Creando documento...");
            Documento doc = new Documento();
            doc.setNombre(nombreOriginal);
            doc.setRuta_documento(nombreOriginal);
            doc.setFecha_documento(new Date());
            doc.setEsValido(true);
            doc.setComentario("Comprobante de pago - Curso de Verano");
            
            // 4. Asociar a la inscripción REAL (no crear una nueva)
            System.out.println("🔗 [SUBIR_COMPROBANTE] Asociando documento a inscripción real...");
            doc.setObjSolicitud(inscripcionExistente);
            
            // 5. Guardar documento
            System.out.println("💾 [SUBIR_COMPROBANTE] Guardando documento en BD...");
            Documento documentoGuardado = this.objGestionarDocumentosGateway.crearDocumento(doc);
            
            System.out.println("✅ [SUBIR_COMPROBANTE] Documento guardado con ID: " + documentoGuardado.getId_documento());
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Comprobante de pago subido exitosamente");
            respuesta.put("nombreArchivo", nombreOriginal);
            respuesta.put("idSolicitud", idSolicitud);
            respuesta.put("documentoId", documentoGuardado.getId_documento());
            
            System.out.println("✅ [SUBIR_COMPROBANTE] Proceso completado exitosamente");
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.out.println("❌ [SUBIR_COMPROBANTE] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al subir archivo: " + e.getMessage()));
        }
    }

    /**
     * Obtener solicitudes de un usuario para seguimiento
     */
    @GetMapping("/misSolicitudes/{idUsuario}")
    public ResponseEntity<List<SolicitudCursoVeranoPreinscripcionDTORespuesta>> obtenerSolicitudesPorUsuario(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            List<SolicitudCursoVeranoPreinscripcion> solicitudes = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            List<SolicitudCursoVeranoPreinscripcionDTORespuesta> respuesta = solicitudes.stream()
                .map(solicitudCursoVeranoPreinscripcionMapper::mappearDeSolicitudCursoVeranoPreinscripcionARespuesta)
                .toList();
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener seguimiento de una solicitud específica
     */
    @GetMapping("/seguimiento/{idSolicitud}")
    public ResponseEntity<SolicitudCursoVeranoPreinscripcionDTORespuesta> obtenerSeguimientoSolicitud(
            @Min(value = 1) @PathVariable Integer idSolicitud) {
        try {
            SolicitudCursoVeranoPreinscripcion solicitud = solicitudCU.buscarSolicitudPorId(idSolicitud);
            if (solicitud == null) {
                return ResponseEntity.notFound().build();
            }
            SolicitudCursoVeranoPreinscripcionDTORespuesta respuesta = 
                solicitudCursoVeranoPreinscripcionMapper.mappearDeSolicitudCursoVeranoPreinscripcionARespuesta(solicitud);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ====== ENDPOINTS PARA SOLICITUDES DE CURSO NUEVO ======
    
    /**
     * Crear solicitud de curso nuevo (no ofertado)
     */
    @PostMapping("/solicitud-curso-nuevo")
    public ResponseEntity<Map<String, Object>> crearSolicitudCursoNuevo(
            @RequestBody Map<String, Object> payload) {
        try {
            // Extraer datos del payload
            String nombreCompleto = (String) payload.get("nombreCompleto");
            String codigo = (String) payload.get("codigo");
            String curso = (String) payload.get("curso");
            String condicion = (String) payload.get("condicion");
            Integer idUsuario = (Integer) payload.get("idUsuario");

            // Validaciones básicas
            if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nombre completo es requerido"));
            }
            if (codigo == null || codigo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Código es requerido"));
            }
            if (curso == null || curso.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nombre del curso es requerido"));
            }
            if (idUsuario == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de usuario es requerido"));
            }

            // Crear solicitud de preinscripción con los datos
            SolicitudCurosoVeranoPreinscripcionDTOPeticion solicitudDTO = new SolicitudCurosoVeranoPreinscripcionDTOPeticion();
            solicitudDTO.setNombre_estudiante(nombreCompleto);
            solicitudDTO.setCodigo_estudiante(codigo);
            solicitudDTO.setCodicion_solicitud(condicion);
            solicitudDTO.setObservacion("Solicitud de apertura de curso: " + curso);
            solicitudDTO.setUsuario_id(idUsuario);

            // Crear un curso temporal para la solicitud
            CursosOfertadosDTOPeticion cursoDTO = new CursosOfertadosDTOPeticion();
            cursoDTO.setId_curso(0); // ID temporal
            solicitudDTO.setObjCursoOfertado(cursoDTO);

            // Crear la solicitud
            SolicitudCursoVeranoPreinscripcion solicitudDominio = solicitudCursoVeranoPreinscripcionMapper
                    .mappearDePeticionASolicitudCursoVeranoPreinscripcion(solicitudDTO);

            System.out.println("🔍 DEBUG: Creando solicitud con datos:");
            System.out.println("  - Nombre: " + nombreCompleto);
            System.out.println("  - Código: " + codigo);
            System.out.println("  - Curso: " + curso);
            System.out.println("  - Usuario ID: " + idUsuario);

            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU
                    .crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

            System.out.println("🔍 DEBUG: Solicitud guardada con ID: " + (solicitudGuardada != null ? solicitudGuardada.getId_solicitud() : "NULL"));

            // Respuesta exitosa
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Solicitud de curso nuevo creada exitosamente");
            respuesta.put("id_solicitud", solicitudGuardada.getId_solicitud());
            respuesta.put("fecha", solicitudGuardada.getFecha_registro_solicitud());
            respuesta.put("codigo", codigo);
            respuesta.put("estado", "Pendiente");
            respuesta.put("objUsuario", Map.of("id_usuario", idUsuario));
            respuesta.put("curso", curso);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al crear solicitud: " + e.getMessage()));
        }
    }

    /**
     * Obtener solicitudes de curso nuevo de un usuario
     */
    @GetMapping("/solicitudes-curso-nuevo/{idUsuario}")
    public ResponseEntity<List<Map<String, Object>>> obtenerSolicitudesCursoNuevoUsuario(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            List<SolicitudCursoVeranoPreinscripcion> solicitudes = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            
            List<Map<String, Object>> respuesta = solicitudes.stream()
                    .map(solicitud -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id_solicitud", solicitud.getId_solicitud());
                        item.put("nombreCompleto", solicitud.getNombre_estudiante());
                        item.put("codigo", solicitud.getCodigo_estudiante());
                        item.put("curso", "Curso solicitado"); // Por ahora genérico
                        item.put("condicion", solicitud.getCodicion_solicitud());
                        item.put("fecha", solicitud.getFecha_registro_solicitud());
                        item.put("estado", "Pendiente");
                        item.put("objUsuario", Map.of("id_usuario", idUsuario));
                        return item;
                    })
                    .toList();
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener todas las solicitudes de curso nuevo (para funcionarios)
     */
    @GetMapping("/solicitudes-curso-nuevo")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodasLasSolicitudesCursoNuevo() {
        try {
            // Por ahora retornamos una lista vacía, se puede implementar después
            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
