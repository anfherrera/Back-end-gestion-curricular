package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;

import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Validated
public class ArchivosRestController {
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final DocumentosMapperDominio documentosMapperDominio;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    @PostMapping("/subir/pdf")
    public ResponseEntity<DocumentosDTORespuesta> subirPDF(@RequestParam(name = "file", required = true) MultipartFile file) {
        try {
            String nombreOriginal = file.getOriginalFilename(); // ← nombre real del archivo
            this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf"); // ← guardar archivo
            // DocumentosDTOPeticion doc = new DocumentosDTOPeticion();
            Documento doc = new Documento();
            doc.setNombre(nombreOriginal);
            doc.setRuta_documento(nombreOriginal);
            doc.setFecha_documento(new Date());
            doc.setEsValido(true);  
            //Documento documento = documentosMapperDominio.mappearDeDTOPeticionADocumento(doc);
            Documento documentoGuardado = this.objGestionarDocumentosGateway.crearDocumento(doc);
            ResponseEntity<DocumentosDTORespuesta> respuesta = new ResponseEntity<DocumentosDTORespuesta>(
                documentosMapperDominio.mappearDeDocumentoADTORespuesta(documentoGuardado), HttpStatus.CREATED
            );
            return respuesta;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/descargar/pdf/{ruta}")
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
    


}
