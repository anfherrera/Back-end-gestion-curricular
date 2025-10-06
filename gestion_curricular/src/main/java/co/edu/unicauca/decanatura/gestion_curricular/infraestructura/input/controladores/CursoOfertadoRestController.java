package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

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
import java.util.ArrayList;

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

    /**
     * Obtener cursos disponibles para estudiantes
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosDisponibles() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar solo cursos que estén en estado "Publicado" o "Preinscripcion"
            List<CursoOfertadoVerano> cursosDisponibles = cursos.stream()
                    .filter(curso -> {
                        if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                            return false;
                        }
                        String estadoActual = curso.getEstadosCursoOfertados().get(0).getEstado_actual();
                        return "Publicado".equals(estadoActual) || 
                               "Preinscripcion".equals(estadoActual) ||
                               "Inscripcion".equals(estadoActual);
                    })
                    .collect(Collectors.toList());
            
            List<CursosOfertadosDTORespuesta> respuesta = cursosDisponibles.stream()
                    .map(CursoMapper::mappearDeCursoOfertadoARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener preinscripciones por curso (para funcionarios)
     */
    @GetMapping("/preinscripciones/{idCurso}")
    public ResponseEntity<Map<String, Object>> obtenerPreinscripcionesPorCurso(
            @Min(value = 1) @PathVariable Integer idCurso) {
        try {
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(idCurso);
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("idCurso", idCurso);
            respuesta.put("preinscripciones", preinscripciones);
            respuesta.put("total", preinscripciones.size());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener inscripciones por curso (para funcionarios)
     */
    @GetMapping("/inscripciones/{idCurso}")
    public ResponseEntity<Map<String, Object>> obtenerInscripcionesPorCurso(
            @Min(value = 1) @PathVariable Integer idCurso) {
        try {
            List<SolicitudCursoVeranoIncripcion> inscripciones = solicitudCU.buscarInscripcionesPorCurso(idCurso);
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("idCurso", idCurso);
            respuesta.put("inscripciones", inscripciones);
            respuesta.put("total", inscripciones.size());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Publicar cursos masivamente
     */
    @PostMapping("/publicarCursos")
    public ResponseEntity<Map<String, Object>> publicarCursos(
            @RequestBody List<Integer> idsCursos) {
        try {
            List<CursoOfertadoVerano> cursosPublicados = cursoCU.publicarCursosMasivamente(idsCursos);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("cursosPublicados", cursosPublicados.size());
            respuesta.put("totalSolicitados", idsCursos.size());
            respuesta.put("mensaje", "Cursos publicados exitosamente");
            respuesta.put("cursos", cursosPublicados.stream()
                .map(CursoMapper::mappearDeCursoOfertadoARespuesta)
                .toList());
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al publicar cursos: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener estadísticas de demanda por curso
     */
    @GetMapping("/estadisticasDemanda")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDemanda() {
        try {
            // Obtener cursos con alta demanda (más de 5 solicitudes)
            List<SolicitudCursoVeranoPreinscripcion> cursosAltaDemanda = solicitudCU.buscarCursosConAltaDemanda(5);
            
            // Obtener todos los cursos para calcular estadísticas
            List<CursoOfertadoVerano> todosLosCursos = cursoCU.listarTodos();
            
            int totalSolicitudes = 0;
            List<Map<String, Object>> cursosConPuntoEquilibrio = new ArrayList<>();
            
            for (CursoOfertadoVerano curso : todosLosCursos) {
                Integer solicitudesCurso = solicitudCU.contarSolicitudesPorCurso(curso.getId_curso());
                totalSolicitudes += solicitudesCurso;
                
                // Verificar si alcanzó el punto de equilibrio
                if (solicitudesCurso >= curso.getCupo_estimado()) {
                    Map<String, Object> cursoEquilibrio = new HashMap<>();
                    cursoEquilibrio.put("idCurso", curso.getId_curso());
                    cursoEquilibrio.put("nombreCurso", curso.getObjMateria().getNombre());
                    cursoEquilibrio.put("solicitudes", solicitudesCurso);
                    cursoEquilibrio.put("cupoEstimado", curso.getCupo_estimado());
                    cursosConPuntoEquilibrio.add(cursoEquilibrio);
                }
            }
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("cursosAltaDemanda", cursosAltaDemanda);
            respuesta.put("totalSolicitudes", totalSolicitudes);
            respuesta.put("cursosConPuntoEquilibrio", cursosConPuntoEquilibrio);
            respuesta.put("totalCursos", todosLosCursos.size());
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Aprobar preinscripción (para funcionarios)
     */
    @PutMapping("/preinscripciones/{idSolicitud}/aprobar")
    public ResponseEntity<Map<String, Object>> aprobarPreinscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String comentarios = requestBody != null ? requestBody.get("comentarios") : null;
            SolicitudCursoVeranoPreinscripcion solicitudAprobada = solicitudCU.aprobarPreinscripcion(idSolicitud, comentarios);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Preinscripción aprobada exitosamente");
            respuesta.put("solicitud", solicitudAprobada);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al aprobar preinscripción: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Rechazar preinscripción (para funcionarios)
     */
    @PutMapping("/preinscripciones/{idSolicitud}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarPreinscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud,
            @RequestBody Map<String, String> requestBody) {
        try {
            String motivo = requestBody.get("motivo");
            if (motivo == null || motivo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar un motivo para el rechazo"));
            }
            
            SolicitudCursoVeranoPreinscripcion solicitudRechazada = solicitudCU.rechazarPreinscripcion(idSolicitud, motivo);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Preinscripción rechazada");
            respuesta.put("solicitud", solicitudRechazada);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al rechazar preinscripción: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
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
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al validar pago: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Completar inscripción (para funcionarios)
     */
    @PutMapping("/inscripciones/{idSolicitud}/completar")
    public ResponseEntity<Map<String, Object>> completarInscripcion(
            @Min(value = 1) @PathVariable Integer idSolicitud) {
        try {
            SolicitudCursoVeranoIncripcion solicitudCompletada = solicitudCU.completarInscripcion(idSolicitud);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Inscripción completada exitosamente");
            respuesta.put("solicitud", solicitudCompletada);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al completar inscripción: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
