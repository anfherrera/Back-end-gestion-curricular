package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.ComentarioDocumentoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DocumentoRestController {

    private final GestionarDocumentosCUIntPort documentoCU;
    private final DocumentosMapperDominio mapperDocumento;
    private final GestionarArchivosGatewayIntPort objGestionarArchivos;

    @GetMapping("/buscarPorId/{id}")
    public ResponseEntity<DocumentosDTORespuesta> buscarDocumentoPorId(@Min(1) @PathVariable Integer id) {
        Documento documento = documentoCU.buscarDocumentoId(id);
        if (documento == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                mapperDocumento.mappearDeDocumentoADTORespuesta(documento),
                HttpStatus.OK
        );
    }

    @PutMapping("/añadirComentario")
    public ResponseEntity<Void> añadirComentario(@RequestBody ComentarioDocumentoDTOPeticion peticion) {
        documentoCU.añadirComentario(peticion.getIdDocumento(), peticion.getComentario());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Descarga el archivo de un documento por su ID.
     * GET /api/documentos/{id}/descargar
     * Usa ruta_documento si existe; si no, nombre. Devuelve 404 si no existe documento o archivo.
     */
    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargarDocumentoPorId(@Min(1) @PathVariable Integer id) {
        Documento documento = documentoCU.buscarDocumentoId(id);
        if (documento == null) {
            log.warn("Descarga documento: documento no encontrado, id={}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String ruta = documento.getRuta_documento() != null && !documento.getRuta_documento().trim().isEmpty()
                ? documento.getRuta_documento()
                : documento.getNombre();
        if (ruta == null || ruta.trim().isEmpty()) {
            log.warn("Descarga documento: documento sin ruta ni nombre, id={}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            byte[] contenido = ruta.contains("/")
                    ? objGestionarArchivos.getFileByPath(ruta)
                    : objGestionarArchivos.getFile(ruta);
            String nombreArchivo = documento.getNombre() != null ? documento.getNombre() : "documento";
            String encoded = URLEncoder.encode(nombreArchivo, StandardCharsets.UTF_8).replace("+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + nombreArchivo + "\"; filename*=UTF-8''" + encoded);
            MediaType mediaType = nombreArchivo.toLowerCase().endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF
                    : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(contenido);
        } catch (IOException e) {
            log.error("Descarga documento: error al leer archivo, id={}, ruta={}", id, ruta, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
