package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.DecisionSolicitudDTOPeticion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CursosOfertadosDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadoCursoOfertadoMapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

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
        CursoOfertadoVerano curso = CursoMapper.mappearDeDTOPeticionACursoOfertado(peticion);
        CursoOfertadoVerano cursoCreado = cursoCU.crearCurso(curso);
        return new ResponseEntity<>(
                CursoMapper.mappearDeCursoOfertadoARespuesta(cursoCreado),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizarCurso")
    public ResponseEntity<CursosOfertadosDTORespuesta> actualizarCurso(@RequestBody @Valid CursosOfertadosDTOPeticion peticion) {
        if(peticion.getEstadoCursoOfertado() == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        EstadoCursoOfertado nuevoEstado = estadoCursoMapper.mappearDeDTOPeticionAEstadoCursoOfertado(peticion.getEstadoCursoOfertado());

        if (nuevoEstado == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CursoOfertadoVerano curso = CursoMapper.mappearDeDTOPeticionACursoOfertado(peticion);
        CursoOfertadoVerano cursoActualizado = cursoCU.actualizarCurso(curso, nuevoEstado); // sin nuevo estado
        return new ResponseEntity<>(
                CursoMapper.mappearDeCursoOfertadoARespuesta(cursoActualizado),
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
                .map(CursoMapper::mappearDeCursoOfertadoARespuesta)
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
                .map(CursoMapper::mappearDeCursoOfertadoARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener preinscripciones por curso (para funcionarios)
     */
    @GetMapping("/preinscripciones/{idCurso}")
    public ResponseEntity<Map<String, Object>> obtenerPreinscripcionesPorCurso(
            @Min(value = 1) @PathVariable Integer idCurso) {
        List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(idCurso);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("idCurso", idCurso);
        respuesta.put("preinscripciones", preinscripciones);
        respuesta.put("total", preinscripciones.size());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener inscripciones por curso (para funcionarios)
     */
    @GetMapping("/inscripciones/{idCurso}")
    public ResponseEntity<Map<String, Object>> obtenerInscripcionesPorCurso(
            @Min(value = 1) @PathVariable Integer idCurso) {
        List<SolicitudCursoVeranoIncripcion> inscripciones = solicitudCU.buscarInscripcionesPorCurso(idCurso);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("idCurso", idCurso);
        respuesta.put("inscripciones", inscripciones);
        respuesta.put("total", inscripciones.size());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Aprobar preinscripción (para funcionarios)
     */
    @PutMapping("/preinscripciones/{idSolicitud}/aprobar")
    public ResponseEntity<Map<String, Object>> aprobarPreinscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody(required = false) @Valid DecisionSolicitudDTOPeticion requestBody) {
        String comentarios = requestBody != null ? requestBody.getComentarios() : null;
        SolicitudCursoVeranoPreinscripcion solicitudAprobada = solicitudCU.aprobarPreinscripcion(idSolicitud, comentarios);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Preinscripción aprobada exitosamente");
        respuesta.put("solicitud", solicitudAprobada);
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechazar preinscripción (para funcionarios)
     */
    @PutMapping("/preinscripciones/{idSolicitud}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarPreinscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody @Valid DecisionSolicitudDTOPeticion requestBody) {
        String motivo = requestBody.getMotivo();
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar un motivo para el rechazo"));
        }
        motivo = motivo.trim();
        
        SolicitudCursoVeranoPreinscripcion solicitudRechazada = solicitudCU.rechazarPreinscripcion(idSolicitud, motivo);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Preinscripción rechazada");
        respuesta.put("solicitud", solicitudRechazada);
        respuesta.put("motivo", motivo);
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Validar pago de inscripción (para funcionarios)
     */
    @PutMapping("/inscripciones/{idSolicitud}/validar-pago")
    public ResponseEntity<Map<String, Object>> validarPago(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Boolean esValido = (Boolean) requestBody.get("esValido");
            String observaciones = (String) requestBody.get("observaciones");
            
            if (esValido == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe especificar si el pago es válido"));
            }
            
            SolicitudCursoVeranoIncripcion solicitudActualizada = solicitudCU.validarPago(idSolicitud, esValido, observaciones);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", esValido ? "Pago validado exitosamente" : "Pago rechazado");
            respuesta.put("solicitud", solicitudActualizada);
            
            return ResponseEntity.ok(respuesta);
        } catch (co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException e) {
            return SoporteCursosVerano.respuestaCupoAgotado(e);
        }
    }

    /**
     * Completar inscripción (para funcionarios)
     */
    @PutMapping("/inscripciones/{idSolicitud}/completar")
    public ResponseEntity<Map<String, Object>> completarInscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud) {
        SolicitudCursoVeranoIncripcion solicitudCompletada = solicitudCU.completarInscripcion(idSolicitud);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Inscripción completada exitosamente");
        respuesta.put("solicitud", solicitudCompletada);
        
        return ResponseEntity.ok(respuesta);
    }
}
