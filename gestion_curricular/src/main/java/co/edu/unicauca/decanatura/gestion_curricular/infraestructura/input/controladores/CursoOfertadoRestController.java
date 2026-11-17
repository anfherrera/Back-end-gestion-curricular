package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CursosOfertadosDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.DecisionSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.ValidacionPagoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursoOfertadoVeranoDTORespuesta.Decision;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursoOfertadoVeranoDTORespuesta.InscripcionesCurso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursoOfertadoVeranoDTORespuesta.PreinscripcionesCurso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursoOfertadoVeranoDTORespuesta.Rechazo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.ErroresCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadoCursoOfertadoMapper;

@RestController
@RequestMapping("/api/cursos-verano")
@RequiredArgsConstructor
@Validated
public class CursoOfertadoRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final CursosOfertadosMapperDominio CursoMapper;
    private final EstadoCursoOfertadoMapper estadoCursoMapper;

    @PostMapping("/crearCurso")
    public ResponseEntity<CursosOfertadosDTORespuesta> crearCurso(@RequestBody @Valid CursosOfertadosDTOPeticion peticion) {
        CursoOfertadoVerano curso = CursoMapper.mapearDeDTOPeticionACursoOfertado(peticion);
        CursoOfertadoVerano cursoCreado = cursoCU.crearCurso(curso);
        return new ResponseEntity<>(
                CursoMapper.mapearDeCursoOfertadoARespuesta(cursoCreado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarCurso")
    public ResponseEntity<CursosOfertadosDTORespuesta> actualizarCurso(@RequestBody @Valid CursosOfertadosDTOPeticion peticion) {
        if(peticion.getEstadoCursoOfertado() == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        EstadoCursoOfertado nuevoEstado = estadoCursoMapper.mapearDeDTOPeticionAEstadoCursoOfertado(peticion.getEstadoCursoOfertado());

        if (nuevoEstado == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CursoOfertadoVerano curso = CursoMapper.mapearDeDTOPeticionACursoOfertado(peticion);
        CursoOfertadoVerano cursoActualizado = cursoCU.actualizarCurso(curso, nuevoEstado); // sin nuevo estado
        return new ResponseEntity<>(
                CursoMapper.mapearDeCursoOfertadoARespuesta(cursoActualizado),
                HttpStatus.ACCEPTED
        );
    }

    /**
     * Obtener cursos disponibles para estudiantes (solo estados visibles)
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosDisponibles() {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        // Filtrar solo cursos que estén en estados visibles para estudiantes
        List<CursoOfertadoVerano> cursosDisponibles = cursos.stream()
                .filter(curso -> {
                    if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                        return false;
                    }
                    String estadoActual = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                    // Solo estados visibles para estudiantes: Publicado, Preinscripcion, Inscripcion
                    return "Publicado".equals(estadoActual) || 
                           "Preinscripcion".equals(estadoActual) ||
                           "Inscripcion".equals(estadoActual);
                })
                .collect(Collectors.toList());
        
        List<CursosOfertadosDTORespuesta> respuesta = cursosDisponibles.stream()
                .map(CursoMapper::mapearDeCursoOfertadoARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Obtener todos los cursos para funcionarios (incluye estados no visibles)
     */
    @GetMapping("/todos")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerTodosLosCursos() {
        List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
        // Para funcionarios, mostrar todos los cursos sin filtro de estado
        List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                .map(CursoMapper::mapearDeCursoOfertadoARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener preinscripciones por curso (para funcionarios)
     */
    @GetMapping("/preinscripciones/{idCurso}")
    public ResponseEntity<PreinscripcionesCurso> obtenerPreinscripcionesPorCurso(
            @Min(value = 1) @PathVariable Integer idCurso) {
        List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(idCurso);
        return ResponseEntity.ok(new PreinscripcionesCurso(idCurso, preinscripciones, preinscripciones.size()));
    }

    /**
     * Obtener inscripciones por curso (para funcionarios)
     */
    @GetMapping("/inscripciones/{idCurso}")
    public ResponseEntity<InscripcionesCurso> obtenerInscripcionesPorCurso(
            @Min(value = 1) @PathVariable Integer idCurso) {
        List<SolicitudCursoVeranoInscripcion> inscripciones = solicitudCU.buscarInscripcionesPorCurso(idCurso);
        return ResponseEntity.ok(new InscripcionesCurso(idCurso, inscripciones, inscripciones.size()));
    }

    /**
     * Aprobar preinscripción (para funcionarios)
     */
    @PutMapping("/preinscripciones/{idSolicitud}/aprobar")
    public ResponseEntity<Decision<SolicitudCursoVeranoPreinscripcion>> aprobarPreinscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody(required = false) @Valid DecisionSolicitudDTOPeticion requestBody) {
        String comentarios = requestBody != null ? requestBody.getComentarios() : null;
        SolicitudCursoVeranoPreinscripcion solicitudAprobada = solicitudCU.aprobarPreinscripcion(idSolicitud, comentarios);

        return ResponseEntity.ok(new Decision<>(true, "Preinscripción aprobada exitosamente", solicitudAprobada));
    }

    /**
     * Rechazar preinscripción (para funcionarios)
     */
    @PutMapping("/preinscripciones/{idSolicitud}/rechazar")
    public ResponseEntity<?> rechazarPreinscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody @Valid DecisionSolicitudDTOPeticion requestBody) {
        String motivo = requestBody.getMotivo();
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar un motivo para el rechazo"));
        }
        motivo = motivo.trim();
        
        SolicitudCursoVeranoPreinscripcion solicitudRechazada = solicitudCU.rechazarPreinscripcion(idSolicitud, motivo);

        return ResponseEntity.ok(new Rechazo(true, "Preinscripción rechazada", solicitudRechazada, motivo));
    }

    /**
     * Validar pago de inscripción (para funcionarios)
     */
    @PutMapping("/inscripciones/{idSolicitud}/validar-pago")
    public ResponseEntity<?> validarPago(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody @Valid ValidacionPagoDTOPeticion requestBody) {
        return ErroresCursosVerano.conCupo(() -> {
            Boolean esValido = requestBody.getEsValido();
            String observaciones = requestBody.getObservaciones();
            
            if (esValido == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe especificar si el pago es válido"));
            }
            
            SolicitudCursoVeranoInscripcion solicitudActualizada = solicitudCU.validarPago(idSolicitud, esValido, observaciones);

            return ResponseEntity.ok(new Decision<>(true,
                    esValido ? "Pago validado exitosamente" : "Pago rechazado", solicitudActualizada));
        });
    }

    /**
     * Completar inscripción (para funcionarios)
     */
    @PutMapping("/inscripciones/{idSolicitud}/completar")
    public ResponseEntity<Decision<SolicitudCursoVeranoInscripcion>> completarInscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud) {
        SolicitudCursoVeranoInscripcion solicitudCompletada = solicitudCU.completarInscripcion(idSolicitud);

        return ResponseEntity.ok(new Decision<>(true, "Inscripción completada exitosamente", solicitudCompletada));
    }
}
