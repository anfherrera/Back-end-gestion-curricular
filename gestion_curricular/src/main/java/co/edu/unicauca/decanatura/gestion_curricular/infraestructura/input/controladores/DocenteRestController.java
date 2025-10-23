package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.DocenteDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocenteDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocenteMapperDominio;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
@Validated
public class DocenteRestController {

    private final GestionarDocentesCUIntPort objDocenteCUIntPort;
    private final DocenteMapperDominio objDocenteMapperDominio;

    @PostMapping("/crearDocente")
    public ResponseEntity<DocenteDTORespuesta> crearDocente(@RequestBody @Valid DocenteDTOPeticion peticion) {
        Docente docente = objDocenteMapperDominio.mappearDeDocenteDTOPeticionADocente(peticion);
        Docente docenteCreado = objDocenteCUIntPort.guardarDocente(docente);
        return new ResponseEntity<>(
                objDocenteMapperDominio.mappearDeDocenteADocenteDTORespuesta(docenteCreado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarDocente")
    public ResponseEntity<DocenteDTORespuesta> actualizarDocente(@RequestBody @Valid DocenteDTOPeticion peticion) {
        Docente docente = objDocenteMapperDominio.mappearDeDocenteDTOPeticionADocente(peticion);
        Docente docenteActualizado = objDocenteCUIntPort.actualizarDocente(docente);
        return new ResponseEntity<>(
                objDocenteMapperDominio.mappearDeDocenteADocenteDTORespuesta(docenteActualizado),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/buscarDocentePorId/{id}")
    public ResponseEntity<DocenteDTORespuesta> buscarDocentePorId(@Min(value = 1) @PathVariable Integer id) {
        Docente docente = objDocenteCUIntPort.obtenerDocentePorId(id);
        if (docente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objDocenteMapperDominio.mappearDeDocenteADocenteDTORespuesta(docente),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/eliminarDocente/{id}")
    public ResponseEntity<Boolean> eliminarDocente(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminado = objDocenteCUIntPort.eliminarDocente(id);
        return new ResponseEntity<>(eliminado ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/buscarPorNombre")
    public ResponseEntity<List<DocenteDTORespuesta>> buscarPorNombre(@RequestParam(name = "nombre", required = true) String nombre) {
        List<Docente> docentes = objDocenteCUIntPort.buscarPorNombreParcial(nombre);
        List<DocenteDTORespuesta> respuesta = docentes.stream()
                .map(objDocenteMapperDominio::mappearDeDocenteADocenteDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorCodigo")
    public ResponseEntity<DocenteDTORespuesta> buscarPorCodigo(@RequestParam(name = "codigo", required = true) String codigo) {
        Docente docente = objDocenteCUIntPort.obtenerDocentePorCodigo(codigo);
        if (docente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objDocenteMapperDominio.mappearDeDocenteADocenteDTORespuesta(docente),
                HttpStatus.OK
        );
    }

    @GetMapping("/listarDocentes")
    public ResponseEntity<List<DocenteDTORespuesta>> listarDocentes() {
        List<Docente> docentes = objDocenteCUIntPort.listarDocentes();
        List<DocenteDTORespuesta> respuesta = docentes.stream()
                .map(objDocenteMapperDominio::mappearDeDocenteADocenteDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}

