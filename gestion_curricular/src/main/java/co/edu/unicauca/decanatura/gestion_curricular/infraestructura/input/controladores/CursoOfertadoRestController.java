package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.ActualizarCursosOfertadosDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CursosOfertadosDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadoCursoOfertadoMapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominio;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cursos-verano")
@RequiredArgsConstructor
@Validated
public class CursoOfertadoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final CursosOfertadosMapperDominio CursoMapper;
    private final SolicitudMapperDominio solicitudMapper;
    private final EstadoCursoOfertadoMapper estadoCursoMapper;

    @PostMapping("/crearCurso")
    public ResponseEntity<CursosOfertadosDTORespuesta> crearCurso(@RequestBody @Valid CursosOfertadosDTOPeticion peticion) {
        CursoOfertadoVerano curso = CursoMapper.mappearDeDTOPeticionACursoOfertado(peticion);
        CursoOfertadoVerano cursoCreado = cursoCU.crearCurso(curso);
        return new ResponseEntity<>(
                CursoMapper.mappearDeCursoOfertadoARespuesta(cursoCreado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarCurso")
    public ResponseEntity<CursosOfertadosDTORespuesta> actualizarCurso(@RequestBody @Valid ActualizarCursosOfertadosDTOPeticion peticion) {
        CursoOfertadoVerano curso = CursoMapper.mappearDeDTOPeticionACursoOfertado(peticion.getCurso());
        EstadoCursoOfertado nuevoEstado = estadoCursoMapper.mappearDeDTOPeticionAEstadoCursoOfertado(peticion.getCurso().getEstadoCursoOfertado());
        List<Solicitud> solicitudesMapeadas = peticion.getSolicitudes().stream()
                .map(solicitudMapper::mappearDeSolicitudDTOPeticionASolicitud)
                .collect(Collectors.toList());
        CursoOfertadoVerano cursoActualizado = cursoCU.actualizarCurso(curso, nuevoEstado, solicitudesMapeadas); // sin nuevo estado
        return new ResponseEntity<>(
                CursoMapper.mappearDeCursoOfertadoARespuesta(cursoActualizado),
                HttpStatus.ACCEPTED
        );
    }

    @DeleteMapping("/eliminarCurso/{id}")
    public ResponseEntity<Boolean> eliminarCurso(@Min(value = 1) @PathVariable Integer id) {
        boolean eliminado = cursoCU.eliminarCurso(id);
        return new ResponseEntity<>(eliminado ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/buscarCursoPorId/{id}")
    public ResponseEntity<CursosOfertadosDTORespuesta> buscarCursoPorId(@Min(value = 1) @PathVariable Integer id) {
        CursoOfertadoVerano curso = cursoCU.obtenerCursoPorId(id);
        if (curso == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                CursoMapper.mappearDeCursoOfertadoARespuesta(curso),
                HttpStatus.OK
        );
    }

    @GetMapping("/listarCursos")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> listarCursos() {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(CursoMapper::mappearDeCursoOfertadoARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    
    
   
}
