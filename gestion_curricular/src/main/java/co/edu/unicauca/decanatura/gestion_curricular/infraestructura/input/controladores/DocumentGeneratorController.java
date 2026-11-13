package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarDocumentosCUIntPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentGeneratorController {

    private final GenerarDocumentosCUIntPort documentGeneratorService;

    /**
     * Generar documento usando plantilla
     */
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generarDocumento(@RequestBody DocumentRequest request) throws IOException {
        byte[] documentBytes = documentGeneratorService.generarDocumento(request.getIdSolicitud(), request.getTipoDocumento(),
                request.getDatosDocumento(), request.getDatosSolicitud());
        String nombreArchivo = generarNombreArchivo(request);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(documentBytes);
    }

    /**
     * Obtener plantillas disponibles para un proceso
     */
    @GetMapping("/templates/{proceso}")
    public ResponseEntity<?> getTemplates(@PathVariable String proceso) {
        return ResponseEntity.ok(documentGeneratorService.getTemplates(proceso));
    }

    private String generarNombreArchivo(DocumentRequest request) {
        String tipoDocumento = request.getTipoDocumento() != null ? request.getTipoDocumento() : "DOCUMENTO";
        
        String nombreEstudiante = "Estudiante";
        if (request.getDatosSolicitud() != null && request.getDatosSolicitud().get("nombreEstudiante") != null) {
            nombreEstudiante = request.getDatosSolicitud().get("nombreEstudiante").toString();
        }
        
        String numeroDocumento = "001-2025";
        if (request.getDatosDocumento() != null && request.getDatosDocumento().get("numeroDocumento") != null) {
            numeroDocumento = request.getDatosDocumento().get("numeroDocumento").toString();
        }
        
        // Limpiar nombre para archivo
        String nombreLimpio = nombreEstudiante.replaceAll("[^a-zA-Z0-9]", "_");
        
        return String.format("%s_%s_%s.docx", tipoDocumento, nombreLimpio, numeroDocumento);
    }
}

