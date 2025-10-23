package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarProgramasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.ProgramaDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.ProgramaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominio;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/programas")
@RequiredArgsConstructor
@Validated
public class ProgramaRestController {

    private final GestionarProgramasCUIntPort objProgramaCUIntPort;
    private final ProgramaMapperDominio objProgramaMapperDominio;

    @PostMapping("/crearPrograma")
    public ResponseEntity<ProgramaDTORespuesta> crearPrograma(@RequestBody @Valid ProgramaDTOPeticion peticion) {
        Programa programa = objProgramaMapperDominio.mappearDeProgramaDTOPeticionAPrograma(peticion);
        Programa programaCreado = objProgramaCUIntPort.guardarPrograma(programa);
        return new ResponseEntity<>(
                objProgramaMapperDominio.mappearDeProgramaAProgramaDTORespuesta(programaCreado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarPrograma")
    public ResponseEntity<ProgramaDTORespuesta> actualizarPrograma(@RequestBody @Valid ProgramaDTOPeticion peticion) {
        Programa programa = objProgramaMapperDominio.mappearDeProgramaDTOPeticionAPrograma(peticion);
        Programa programaActualizado = objProgramaCUIntPort.actualizarPrograma(programa);
        return new ResponseEntity<>(
                objProgramaMapperDominio.mappearDeProgramaAProgramaDTORespuesta(programaActualizado),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/buscarProgramaPorId/{id}")
    public ResponseEntity<ProgramaDTORespuesta> buscarProgramaPorId(@Min(value = 1) @PathVariable Integer id) {
        Programa programa = objProgramaCUIntPort.obtenerProgramaPorId(id);
        if (programa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objProgramaMapperDominio.mappearDeProgramaAProgramaDTORespuesta(programa),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/eliminarPrograma/{id}")
    public ResponseEntity<Boolean> eliminarPrograma(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminado = objProgramaCUIntPort.eliminarPrograma(id);
        return new ResponseEntity<>(eliminado ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/buscarPorNombre")
    public ResponseEntity<List<ProgramaDTORespuesta>> buscarPorNombre(@RequestParam(name = "nombre", required = true) String nombre) {
        List<Programa> programas = objProgramaCUIntPort.buscarPorNombreParcial(nombre);
        List<ProgramaDTORespuesta> respuesta = programas.stream()
                .map(objProgramaMapperDominio::mappearDeProgramaAProgramaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscarPorCodigo")
    public ResponseEntity<ProgramaDTORespuesta> buscarPorCodigo(@RequestParam(name = "codigo", required = true) String codigo) {
        Programa programa = objProgramaCUIntPort.obtenerProgramaPorCodigo(codigo);
        if (programa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                objProgramaMapperDominio.mappearDeProgramaAProgramaDTORespuesta(programa),
                HttpStatus.OK
        );
    }

    @GetMapping("/listarProgramas")
    public ResponseEntity<List<ProgramaDTORespuesta>> listarProgramas() {
        List<Programa> programas = objProgramaCUIntPort.listarProgramas();
        List<ProgramaDTORespuesta> respuesta = programas.stream()
                .map(objProgramaMapperDominio::mappearDeProgramaAProgramaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}

