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

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCurosoVeranoPreinscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoVeranoInscripcionDTOPeticion;
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
            String nombreOriginal = file.getOriginalFilename();
            
            if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nombre de archivo no válido"));
            }
            
            if (!nombreOriginal.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Solo se permiten archivos PDF"));
            }
            
            // Guardar archivo
            this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf");
            
            // Crear documento
            Documento doc = new Documento();
            doc.setNombre(nombreOriginal);
            doc.setRuta_documento(nombreOriginal);
            doc.setFecha_documento(new Date());
            doc.setEsValido(true);
            doc.setComentario("Comprobante de pago - Curso de Verano");
            
            // Asociar a solicitud
            SolicitudCursoVeranoIncripcion solicitud = new SolicitudCursoVeranoIncripcion();
            solicitud.setId_solicitud(idSolicitud);
            doc.setObjSolicitud(solicitud);
            
            this.objGestionarDocumentosGateway.crearDocumento(doc);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Comprobante de pago subido exitosamente");
            respuesta.put("nombreArchivo", nombreOriginal);
            respuesta.put("idSolicitud", idSolicitud);
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
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
}
