package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarProgramasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.ProgramaDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ProgramaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominio;

@RestController
@RequestMapping("/api/programas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Programas Académicos", description = "API para la gestión de programas académicos de la FIET")
public class ProgramaRestController {

    private final GestionarProgramasCUIntPort programaCUIntPort;
    private final ProgramaMapperDominio programaMapperDominio;

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
    @PostMapping
    public ResponseEntity<ProgramaDTORespuesta> crearPrograma(@RequestBody @Valid ProgramaDTOPeticion peticion) {
        Programa programa = programaMapperDominio.mapearDeProgramaDTOPeticionAPrograma(peticion);
        Programa programaCreado = programaCUIntPort.guardarPrograma(programa);
        return new ResponseEntity<>(
                programaMapperDominio.mapearDeProgramaAProgramaDTORespuesta(programaCreado),
                HttpStatus.CREATED
        );
    }

    /** El id de la ruta manda sobre el del cuerpo. */
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ProgramaDTORespuesta> actualizarPrograma(@PathVariable Integer id,
            @RequestBody @Valid ProgramaDTOPeticion peticion) {
        peticion.setId_programa(id);
        Programa programa = programaMapperDominio.mapearDeProgramaDTOPeticionAPrograma(peticion);
        Programa programaActualizado = programaCUIntPort.actualizarPrograma(programa);
        return new ResponseEntity<>(
                programaMapperDominio.mapearDeProgramaAProgramaDTORespuesta(programaActualizado),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ProgramaDTORespuesta> buscarProgramaPorId(@Min(value = 1) @PathVariable Integer id) {
        Programa programa = programaCUIntPort.obtenerProgramaPorId(id);
        if (programa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                programaMapperDominio.mapearDeProgramaAProgramaDTORespuesta(programa),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Boolean> eliminarPrograma(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminado = programaCUIntPort.eliminarPrograma(id);
        return new ResponseEntity<>(eliminado ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    /**
     * Sin parámetros, todos los programas; con {@code nombre}, los que lo contienen (una lista); con {@code codigo},
     * el programa con ese código (un objeto). Las dos búsquedas a la vez responden 400.
     */
    @Operation(
        summary = "Listar o buscar programas académicos",
        description = "Sin parámetros lista todos los programas; ?nombre= busca por nombre parcial y ?codigo= por código"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista o programa encontrado",
            content = @Content(schema = @Schema(implementation = ProgramaDTORespuesta.class)))
    })
    @GetMapping
    public ResponseEntity<?> listarOBuscar(@RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "codigo", required = false) String codigo) {
        if (nombre != null && codigo != null) {
            throw new IllegalArgumentException("Use nombre o codigo, no los dos");
        }
        if (nombre != null) {
            return buscarPorNombre(nombre);
        }
        return codigo != null ? buscarPorCodigo(codigo) : listarProgramas();
    }

    private ResponseEntity<List<ProgramaDTORespuesta>> buscarPorNombre(String nombre) {
        List<Programa> programas = programaCUIntPort.buscarPorNombreParcial(nombre);
        List<ProgramaDTORespuesta> respuesta = programas.stream()
                .map(programaMapperDominio::mapearDeProgramaAProgramaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    private ResponseEntity<ProgramaDTORespuesta> buscarPorCodigo(String codigo) {
        Programa programa = programaCUIntPort.obtenerProgramaPorCodigo(codigo);
        if (programa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                programaMapperDominio.mapearDeProgramaAProgramaDTORespuesta(programa),
                HttpStatus.OK
        );
    }

    private ResponseEntity<List<ProgramaDTORespuesta>> listarProgramas() {
        List<Programa> programas = programaCUIntPort.listarProgramas();
        List<ProgramaDTORespuesta> respuesta = programas.stream()
                .map(programaMapperDominio::mapearDeProgramaAProgramaDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}

