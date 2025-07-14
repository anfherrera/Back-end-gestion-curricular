package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.MateriaDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.MateriaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.MateriaMapperDominio;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/materias")
@RequiredArgsConstructor
@Validated
public class MateriaRestController {

    private final GestionarMateriasCUIntPort objMateriaCUIntPort;
    private final MateriaMapperDominio objMateriaMapperDominio;

    @PostMapping("/crearMateria")
    public ResponseEntity<MateriaDTORespuesta> crearMateria(@RequestBody @Valid MateriaDTOPeticion peticion) {
        Materia materia = objMateriaMapperDominio.mappearDeMateriaDTOPeticionAMateria(peticion);
        Materia materiaCreada = objMateriaCUIntPort.guardarMateria(materia);
        return new ResponseEntity<>(
                objMateriaMapperDominio.mappearDeMateriaAMateriaDTORespuesta(materiaCreada),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarMateria")
    public ResponseEntity<MateriaDTORespuesta> actualizarMateria(@RequestBody @Valid MateriaDTOPeticion peticion) {
        Materia materia = objMateriaMapperDominio.mappearDeMateriaDTOPeticionAMateria(peticion);
        Materia materiaActualizada = objMateriaCUIntPort.actualizarMateria(materia);
        return new ResponseEntity<>(
                objMateriaMapperDominio.mappearDeMateriaAMateriaDTORespuesta(materiaActualizada),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/buscarMateriaPorId/{id}")
    public ResponseEntity<MateriaDTORespuesta> buscarMateriaPorId(@Min(value = 1) @PathVariable Integer id) {
        Materia materia = objMateriaCUIntPort.obtenerMateriaPorId(id);
        if (materia == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objMateriaMapperDominio.mappearDeMateriaAMateriaDTORespuesta(materia),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/eliminarMateria/{id}")
    public ResponseEntity<Boolean> eliminarMateria(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminada = objMateriaCUIntPort.eliminarMateria(id);
        return new ResponseEntity<>(eliminada ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/buscarPorNombre/{nombre}")
    public ResponseEntity<List<MateriaDTORespuesta>> buscarPorNombre(@PathVariable String nombre) {
        List<Materia> materias = objMateriaCUIntPort.buscarPorNombreParcial(nombre);
        List<MateriaDTORespuesta> respuesta = materias.stream()
                .map(objMateriaMapperDominio::mappearDeMateriaAMateriaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorCreditos/{creditos}")
    public ResponseEntity<List<MateriaDTORespuesta>> buscarPorCreditos(@PathVariable Integer creditos) {
        List<Materia> materias = objMateriaCUIntPort.buscarPorCreditos(creditos);
        List<MateriaDTORespuesta> respuesta = materias.stream()
                .map(objMateriaMapperDominio::mappearDeMateriaAMateriaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorCodigo/{codigo}")
    public ResponseEntity<MateriaDTORespuesta> buscarPorCodigo(@PathVariable String codigo) {
        Materia materia = objMateriaCUIntPort.obtenerMateriaPorCodigo(codigo);
        if (materia == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objMateriaMapperDominio.mappearDeMateriaAMateriaDTORespuesta(materia),
                HttpStatus.OK
        );
    }

    @GetMapping("/listarMaterias")
    public ResponseEntity<List<MateriaDTORespuesta>> listarMaterias() {
        List<Materia> materias = objMateriaCUIntPort.listarMaterias();
        List<MateriaDTORespuesta> respuesta = materias.stream()
                .map(objMateriaMapperDominio::mappearDeMateriaAMateriaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}
