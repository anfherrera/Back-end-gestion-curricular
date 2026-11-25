package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.ComentarioDocumentoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DocumentoRestController {

    private final GestionarDocumentosCUIntPort documentoCU;
    private final DocumentosMapperDominio mapperDocumento;
    private final GestionarArchivosCUIntPort gestionarArchivos;
    private final Autorizacion autorizacion;

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("@autorizacion.puedeVerDocumento(#id)")
    public ResponseEntity<DocumentosDTORespuesta> buscarDocumentoPorId(@Min(1) @PathVariable Integer id) {
        Documento documento = documentoCU.buscarDocumentoId(id);
        if (documento == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                mapperDocumento.mapearDeDocumentoADTORespuesta(documento),
                HttpStatus.OK
        );
    }

    /**
     * Solo funcionarios, coordinadores y demás staff comentan documentos de una solicitud.
     * El documento es el de la ruta; si el cuerpo trae {@code idDocumento}, se ignora.
     */
    @PutMapping("/{id:\\d+}/comentarios")
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    public ResponseEntity<Void> agregarComentario(@PathVariable Integer id,
            @RequestBody ComentarioDocumentoDTOPeticion peticion) {
        Usuario usuario = autorizacion.usuarioActual();
        String nombreUsuario = usuario != null ? usuario.getNombreCompleto() : null;
        String rolUsuario = usuario != null && usuario.getObjRol() != null ? usuario.getObjRol().getNombre() : null;
        String comentarioNuevo = peticion.getComentario();
        documentoCU.añadirComentario(id, comentarioNuevo, nombreUsuario, rolUsuario);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Descarga el archivo de un documento por su ID.
     * GET /api/documentos/{id}/archivo
     * Usa ruta_documento si existe; si no, nombre. Devuelve 404 si no existe documento o archivo.
     */
    @GetMapping("/{id:\\d+}/archivo")
    @PreAuthorize("@autorizacion.puedeVerDocumento(#id)")
    public ResponseEntity<byte[]> descargarDocumentoPorId(@Min(1) @PathVariable Integer id) {
        Documento documento = documentoCU.buscarDocumentoId(id);
        if (documento == null) {
            log.warn("Descarga documento: documento no encontrado, id={}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String ruta = documento.getRutaDocumento() != null && !documento.getRutaDocumento().trim().isEmpty()
                ? documento.getRutaDocumento()
                : documento.getNombre();
        if (ruta == null || ruta.trim().isEmpty()) {
            log.warn("Descarga documento: documento sin ruta ni nombre, id={}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            byte[] contenido = ruta.contains("/")
                    ? gestionarArchivos.getFileByPath(ruta)
                    : gestionarArchivos.getFile(ruta);
            String nombreArchivo = documento.getNombre() != null ? documento.getNombre() : "documento";
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.adjuntoUtf8(nombreArchivo));
            MediaType mediaType = nombreArchivo.toLowerCase().endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF
                    : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(contenido);
        } catch (RuntimeException e) {
            // El caso de uso informa el error de lectura como excepción de regla de negocio
            log.error("Descarga documento: error al leer archivo, id={}, ruta={}", id, ruta, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
