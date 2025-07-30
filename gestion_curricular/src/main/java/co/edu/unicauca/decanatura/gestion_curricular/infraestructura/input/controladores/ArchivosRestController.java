package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;

import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Validated
public class ArchivosRestController {
    private final GestionarArchivosCUIntPort objGestionarArchivos;

    // @PostMapping("/subir/pdf")
    // public ResponseEntity<String> subirPDF(@RequestParam(name = "file", required = true) MultipartFile file) {
    //     String filename = null;
    //     try {
    //         filename = this.objGestionarArchivos.saveFile(file, "prueba", "pdf");
    //         return ResponseEntity.ok("Archivo subido correctamente"+ filename);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body("Error al subir el archivo" + e.getMessage());
    //     }
    // }
    @PostMapping("/subir/pdf")
    public ResponseEntity<String> subirPDF(@RequestParam(name = "file", required = true) MultipartFile file) {
        String filename = null; 
        try {
            String nombreOriginal = file.getOriginalFilename(); // ← nombre real del archivo
            filename = this.objGestionarArchivos.saveFile(file, nombreOriginal, "pdf"); // ← úsalo aquí
            return ResponseEntity.ok("Archivo subido correctamente: " + nombreOriginal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al subir el archivo: " + e.getMessage());
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
