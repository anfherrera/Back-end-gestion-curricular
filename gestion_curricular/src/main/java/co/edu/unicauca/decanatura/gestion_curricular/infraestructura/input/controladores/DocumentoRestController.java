package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.ComentarioDocumentoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocumentosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Validated
public class DocumentoRestController {

    private final GestionarDocumentosCUIntPort documentoCU;
    private final DocumentosMapperDominio mapperDocumento;

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
        System.out.println("Añadiendo comentario"+peticion.getComentario()+"al documento con ID: " + peticion.getIdDocumento());
        documentoCU.añadirComentario(peticion.getIdDocumento(), peticion.getComentario());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
