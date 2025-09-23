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
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
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
    @PostMapping("/subir/pdf")
    public ResponseEntity<DocumentosDTORespuesta> subirPDF(
            @RequestParam(name = "file", required = true) MultipartFile file,
            @RequestParam(name = "idSolicitud", required = false) Integer idSolicitud) {
        try {
            String nombreOriginal = file.getOriginalFilename(); // ← nombre real del archivo
            this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf"); // ← guardar archivo
            
            Documento doc = new Documento();
            doc.setNombre(nombreOriginal);
            doc.setRuta_documento(nombreOriginal);
            doc.setFecha_documento(new Date());
            doc.setEsValido(true);
            
            // Asociar solicitud si se proporciona idSolicitud
            if (idSolicitud != null) {
                try {
                    // Obtener la solicitud de homologación
                    SolicitudHomologacion solicitud = solicitudHomologacionCU.buscarPorId(idSolicitud);
                    if (solicitud != null) {
                        // Crear objeto Solicitud para asociar
                        Solicitud objSolicitud = new Solicitud();
                        objSolicitud.setId_solicitud(idSolicitud);
                        doc.setObjSolicitud(objSolicitud);
                        System.out.println("📎 Asociando archivo '" + nombreOriginal + "' a solicitud ID: " + idSolicitud);
                    } else {
                        System.err.println("❌ No se encontró la solicitud con ID: " + idSolicitud);
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
    


}
