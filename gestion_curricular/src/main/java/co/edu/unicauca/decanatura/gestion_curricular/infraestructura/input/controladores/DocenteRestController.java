package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.DocenteDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocenteDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocenteMapperDominio;

@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
@Validated
public class DocenteRestController {

    private final GestionarDocentesCUIntPort docenteCUIntPort;
    private final DocenteMapperDominio docenteMapperDominio;

    /** Rutas según la convención de EST-7: {@code POST /}, {@code GET /} con o sin búsqueda, {@code GET|PUT|DELETE /{id}}. */
    @PostMapping
    public ResponseEntity<DocenteDTORespuesta> crearDocente(@RequestBody @Valid DocenteDTOPeticion peticion) {
        Docente docente = docenteMapperDominio.mapearDeDocenteDTOPeticionADocente(peticion);
        Docente docenteCreado = docenteCUIntPort.guardarDocente(docente);
        return new ResponseEntity<>(
                docenteMapperDominio.mapearDeDocenteADocenteDTORespuesta(docenteCreado),
                HttpStatus.CREATED
        );
    }

    /** El id de la ruta manda sobre el del cuerpo. */
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<DocenteDTORespuesta> actualizarDocente(@PathVariable Integer id,
            @RequestBody @Valid DocenteDTOPeticion peticion) {
        peticion.setId_docente(id);
        Docente docente = docenteMapperDominio.mapearDeDocenteDTOPeticionADocente(peticion);
        Docente docenteActualizado = docenteCUIntPort.actualizarDocente(docente);
        return new ResponseEntity<>(
                docenteMapperDominio.mapearDeDocenteADocenteDTORespuesta(docenteActualizado),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<DocenteDTORespuesta> buscarDocentePorId(@Min(value = 1) @PathVariable Integer id) {
        Docente docente = docenteCUIntPort.obtenerDocentePorId(id);
        if (docente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                docenteMapperDominio.mapearDeDocenteADocenteDTORespuesta(docente),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Boolean> eliminarDocente(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminado = docenteCUIntPort.eliminarDocente(id);
        return new ResponseEntity<>(eliminado, eliminado ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    /**
     * Sin parámetros, todos los docentes; con {@code nombre}, los que lo contienen (una lista); con {@code codigo},
     * el docente con ese código (un objeto). Las dos búsquedas a la vez responden 400.
     */
    @GetMapping
    public ResponseEntity<?> listarOBuscar(@RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "codigo", required = false) String codigo) {
        if (nombre != null && codigo != null) {
            throw new IllegalArgumentException("Use nombre o codigo, no los dos");
        }
        if (nombre != null) {
            return buscarPorNombre(nombre);
        }
        return codigo != null ? buscarPorCodigo(codigo) : listarDocentes();
    }

    private ResponseEntity<List<DocenteDTORespuesta>> buscarPorNombre(String nombre) {
        List<Docente> docentes = docenteCUIntPort.buscarPorNombreParcial(nombre);
        List<DocenteDTORespuesta> respuesta = docentes.stream()
                .map(docenteMapperDominio::mapearDeDocenteADocenteDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    private ResponseEntity<DocenteDTORespuesta> buscarPorCodigo(String codigo) {
        Docente docente = docenteCUIntPort.obtenerDocentePorCodigo(codigo);
        if (docente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                docenteMapperDominio.mapearDeDocenteADocenteDTORespuesta(docente),
                HttpStatus.OK
        );
    }

    private ResponseEntity<List<DocenteDTORespuesta>> listarDocentes() {
        List<Docente> docentes = docenteCUIntPort.listarDocentes();
        List<DocenteDTORespuesta> respuesta = docentes.stream()
                .map(docenteMapperDominio::mapearDeDocenteADocenteDTORespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}

