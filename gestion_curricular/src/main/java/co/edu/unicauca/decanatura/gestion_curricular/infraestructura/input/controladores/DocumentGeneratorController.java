package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentRequest;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.DocumentGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentGeneratorController {

    @Autowired
    private DocumentGeneratorService documentGeneratorService;

    /**
     * Generar documento usando plantilla
     */
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generarDocumento(@RequestBody DocumentRequest request) {
        try {
            ByteArrayOutputStream documentBytes = documentGeneratorService.generarDocumento(request);
            String nombreArchivo = generarNombreArchivo(request);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(documentBytes.toByteArray());
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener plantillas disponibles para un proceso
     */
    @GetMapping("/templates/{proceso}")
    public ResponseEntity<?> getTemplates(@PathVariable String proceso) {
        try {
            return ResponseEntity.ok(documentGeneratorService.getTemplates(proceso));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private String generarNombreArchivo(DocumentRequest request) {
        String tipoDocumento = request.getTipoDocumento();
        String nombreEstudiante = request.getDatosSolicitud().get("nombreEstudiante").toString();
        String numeroDocumento = request.getDatosDocumento().get("numeroDocumento").toString();
        
        // Limpiar nombre para archivo
        String nombreLimpio = nombreEstudiante.replaceAll("[^a-zA-Z0-9]", "_");
        
        return String.format("%s_%s_%s.docx", tipoDocumento, nombreLimpio, numeroDocumento);
    }
}

