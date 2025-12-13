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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/programas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Programas Académicos", description = "API para la gestión de programas académicos de la FIET")
public class ProgramaRestController {

    private final GestionarProgramasCUIntPort objProgramaCUIntPort;
    private final ProgramaMapperDominio objProgramaMapperDominio;

    @Operation(
        summary = "Crear un nuevo programa académico",
        description = "Crea un nuevo programa académico en el sistema. El código del programa debe ser único."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Programa creado exitosamente",
            content = @Content(schema = @Schema(implementation = ProgramaDTORespuesta.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o programa ya existe",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Ya existe un programa con ese código",
            content = @Content)
    })
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

    @Operation(
        summary = "Listar todos los programas académicos",
        description = "Obtiene una lista de todos los programas académicos registrados en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ProgramaDTORespuesta.class)))
    })
    @GetMapping("/listarProgramas")
    public ResponseEntity<List<ProgramaDTORespuesta>> listarProgramas() {
        List<Programa> programas = objProgramaCUIntPort.listarProgramas();
        List<ProgramaDTORespuesta> respuesta = programas.stream()
                .map(objProgramaMapperDominio::mappearDeProgramaAProgramaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}

