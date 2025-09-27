package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Validated
public class NotificacionRestController {

    private final GestionarNotificacionCUIntPort notificacionCU;

    /**
     * Obtener notificaciones de un usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesPorUsuario(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarPorUsuario(idUsuario);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificaciones de un usuario con paginación
     */
    @GetMapping("/usuario/{idUsuario}/paginado")
    public ResponseEntity<Page<Notificacion>> obtenerNotificacionesPorUsuarioPaginado(
            @Min(value = 1) @PathVariable Integer idUsuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Notificacion> notificaciones = notificacionCU.buscarPorUsuarioPaginado(idUsuario, pageable);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificaciones no leídas de un usuario
     */
    @GetMapping("/usuario/{idUsuario}/no-leidas")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesNoLeidas(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarNoLeidasPorUsuario(idUsuario);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Contar notificaciones no leídas de un usuario
     */
    @GetMapping("/usuario/{idUsuario}/contar-no-leidas")
    public ResponseEntity<Map<String, Object>> contarNotificacionesNoLeidas(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            Integer count = notificacionCU.contarNoLeidasPorUsuario(idUsuario);
            return ResponseEntity.ok(Map.of("notificacionesNoLeidas", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener dashboard de un usuario
     */
    @GetMapping("/dashboard/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerDashboardUsuario(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            Map<String, Object> dashboard = notificacionCU.obtenerDashboardUsuario(idUsuario);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marcar notificación como leída
     */
    @PutMapping("/{idNotificacion}/marcar-leida")
    public ResponseEntity<Map<String, Object>> marcarComoLeida(
            @Min(value = 1) @PathVariable Integer idNotificacion) {
        try {
            boolean resultado = notificacionCU.marcarComoLeida(idNotificacion);
            if (resultado) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Notificación marcada como leída"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marcar notificación como no leída
     */
    @PutMapping("/{idNotificacion}/marcar-no-leida")
    public ResponseEntity<Map<String, Object>> marcarComoNoLeida(
            @Min(value = 1) @PathVariable Integer idNotificacion) {
        try {
            boolean resultado = notificacionCU.marcarComoNoLeida(idNotificacion);
            if (resultado) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Notificación marcada como no leída"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marcar todas las notificaciones de un usuario como leídas
     */
    @PutMapping("/usuario/{idUsuario}/marcar-todas-leidas")
    public ResponseEntity<Map<String, Object>> marcarTodasComoLeidas(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            boolean resultado = notificacionCU.marcarTodasComoLeidas(idUsuario);
            return ResponseEntity.ok(Map.of("success", resultado, "message", "Todas las notificaciones marcadas como leídas"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificaciones por tipo de solicitud
     */
    @GetMapping("/tipo-solicitud/{tipoSolicitud}")
    public ResponseEntity<List<Notificacion>> obtenerPorTipoSolicitud(
            @PathVariable String tipoSolicitud) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarPorTipoSolicitud(tipoSolicitud);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificaciones por tipo de notificación
     */
    @GetMapping("/tipo-notificacion/{tipoNotificacion}")
    public ResponseEntity<List<Notificacion>> obtenerPorTipoNotificacion(
            @PathVariable String tipoNotificacion) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarPorTipoNotificacion(tipoNotificacion);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificaciones urgentes
     */
    @GetMapping("/urgentes")
    public ResponseEntity<List<Notificacion>> obtenerUrgentes() {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarUrgentes();
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificaciones por rango de fechas
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<Notificacion>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarPorRangoFechas(fechaInicio, fechaFin);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener estadísticas generales
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = notificacionCU.obtenerEstadisticasGenerales();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificación por ID
     */
    @GetMapping("/{idNotificacion}")
    public ResponseEntity<Notificacion> obtenerNotificacionPorId(
            @Min(value = 1) @PathVariable Integer idNotificacion) {
        try {
            Notificacion notificacion = notificacionCU.obtenerNotificacionPorId(idNotificacion);
            if (notificacion != null) {
                return ResponseEntity.ok(notificacion);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar notificación
     */
    @DeleteMapping("/{idNotificacion}")
    public ResponseEntity<Map<String, Object>> eliminarNotificacion(
            @Min(value = 1) @PathVariable Integer idNotificacion) {
        try {
            boolean resultado = notificacionCU.eliminarNotificacion(idNotificacion);
            if (resultado) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Notificación eliminada"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar notificaciones antiguas
     */
    @DeleteMapping("/limpiar-antiguas")
    public ResponseEntity<Map<String, Object>> eliminarNotificacionesAntiguas(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaLimite) {
        try {
            notificacionCU.eliminarNotificacionesAntiguas(fechaLimite);
            return ResponseEntity.ok(Map.of("success", true, "message", "Notificaciones antiguas eliminadas"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoints específicos para cursos de verano
    /**
     * Obtener notificaciones de cursos de verano para un usuario
     */
    @GetMapping("/usuario/{idUsuario}/cursos-verano")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesCursosVerano(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarPorUsuarioYTipoSolicitud(idUsuario, "CURSO_VERANO");
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
