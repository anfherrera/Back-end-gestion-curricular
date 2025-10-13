package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

    /**
     * Obtener notificaciones del header para cualquier usuario (estudiantes y funcionarios)
     * GET /api/notificaciones/header/{idUsuario}
     */
    @GetMapping("/header/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerNotificacionesHeader(@PathVariable Integer idUsuario) {
        try {
            System.out.println("🔔 [NOTIFICACIONES_HEADER_GENERAL] Obteniendo notificaciones del header para usuario: " + idUsuario);
            
            // Obtener notificaciones no leídas del usuario
            List<Notificacion> notificacionesNoLeidas = notificacionCU.buscarNoLeidasPorUsuario(idUsuario);
            Integer totalNoLeidas = notificacionCU.contarNoLeidasPorUsuario(idUsuario);
            
            // Crear notificaciones mejoradas con más información
            List<Map<String, Object>> notificacionesMejoradas = new ArrayList<>();
            
            for (Notificacion notificacion : notificacionesNoLeidas) {
                Map<String, Object> notificacionMejorada = new HashMap<>();
                notificacionMejorada.put("id", notificacion.getId_notificacion());
                notificacionMejorada.put("titulo", notificacion.getTitulo());
                notificacionMejorada.put("mensaje", notificacion.getMensaje());
                notificacionMejorada.put("tipoSolicitud", notificacion.getTipoSolicitud());
                notificacionMejorada.put("tipoNotificacion", notificacion.getTipoNotificacion());
                notificacionMejorada.put("fechaCreacion", notificacion.getFechaCreacion());
                notificacionMejorada.put("esUrgente", notificacion.getEsUrgente());
                notificacionMejorada.put("accion", notificacion.getAccion());
                notificacionMejorada.put("urlAccion", notificacion.getUrlAccion());
                
                // Agregar información adicional según el tipo
                if ("CURSO_VERANO".equals(notificacion.getTipoSolicitud())) {
                    notificacionMejorada.put("categoria", "Cursos Intersemestrales");
                    notificacionMejorada.put("icono", "graduation-cap");
                    notificacionMejorada.put("color", "blue");
                } else if ("ECAES".equals(notificacion.getTipoSolicitud())) {
                    notificacionMejorada.put("categoria", "ECAES");
                    notificacionMejorada.put("icono", "book");
                    notificacionMejorada.put("color", "green");
                } else if ("REINGRESO".equals(notificacion.getTipoSolicitud())) {
                    notificacionMejorada.put("categoria", "Reingreso");
                    notificacionMejorada.put("icono", "user-plus");
                    notificacionMejorada.put("color", "orange");
                } else if ("HOMOLOGACION".equals(notificacion.getTipoSolicitud())) {
                    notificacionMejorada.put("categoria", "Homologación");
                    notificacionMejorada.put("icono", "exchange-alt");
                    notificacionMejorada.put("color", "purple");
                } else if ("PAZ_SALVO".equals(notificacion.getTipoSolicitud())) {
                    notificacionMejorada.put("categoria", "Paz y Salvo");
                    notificacionMejorada.put("icono", "check-circle");
                    notificacionMejorada.put("color", "green");
                } else {
                    notificacionMejorada.put("categoria", "General");
                    notificacionMejorada.put("icono", "bell");
                    notificacionMejorada.put("color", "gray");
                }
                
                // Agregar tiempo transcurrido
                long tiempoTranscurrido = System.currentTimeMillis() - notificacion.getFechaCreacion().getTime();
                long minutos = tiempoTranscurrido / (1000 * 60);
                long horas = minutos / 60;
                long dias = horas / 24;
                
                if (dias > 0) {
                    notificacionMejorada.put("tiempoTranscurrido", dias + " día" + (dias > 1 ? "s" : "") + " atrás");
                } else if (horas > 0) {
                    notificacionMejorada.put("tiempoTranscurrido", horas + " hora" + (horas > 1 ? "s" : "") + " atrás");
                } else if (minutos > 0) {
                    notificacionMejorada.put("tiempoTranscurrido", minutos + " minuto" + (minutos > 1 ? "s" : "") + " atrás");
                } else {
                    notificacionMejorada.put("tiempoTranscurrido", "Hace un momento");
                }
                
                notificacionesMejoradas.add(notificacionMejorada);
            }
            
            // Crear respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("totalNoLeidas", totalNoLeidas);
            respuesta.put("notificaciones", notificacionesMejoradas);
            respuesta.put("categorias", Map.of(
                "CURSO_VERANO", notificacionesNoLeidas.stream().filter(n -> "CURSO_VERANO".equals(n.getTipoSolicitud())).count(),
                "ECAES", notificacionesNoLeidas.stream().filter(n -> "ECAES".equals(n.getTipoSolicitud())).count(),
                "REINGRESO", notificacionesNoLeidas.stream().filter(n -> "REINGRESO".equals(n.getTipoSolicitud())).count(),
                "HOMOLOGACION", notificacionesNoLeidas.stream().filter(n -> "HOMOLOGACION".equals(n.getTipoSolicitud())).count(),
                "PAZ_SALVO", notificacionesNoLeidas.stream().filter(n -> "PAZ_SALVO".equals(n.getTipoSolicitud())).count()
            ));
            
            System.out.println("✅ [NOTIFICACIONES_HEADER_GENERAL] Notificaciones obtenidas: " + totalNoLeidas + " total");
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICACIONES_HEADER_GENERAL] Error obteniendo notificaciones: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo notificaciones: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Marcar notificaciones como leídas desde el header (para cualquier usuario)
     * PUT /api/notificaciones/header/{idUsuario}/marcar-leidas
     */
    @PutMapping("/header/{idUsuario}/marcar-leidas")
    public ResponseEntity<Map<String, Object>> marcarNotificacionesComoLeidas(@PathVariable Integer idUsuario) {
        try {
            System.out.println("✅ [NOTIFICACIONES_HEADER_GENERAL] Marcando todas las notificaciones como leídas para usuario: " + idUsuario);
            
            boolean resultado = notificacionCU.marcarTodasComoLeidas(idUsuario);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", resultado);
            respuesta.put("message", resultado ? "Todas las notificaciones han sido marcadas como leídas" : "Error al marcar las notificaciones");
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICACIONES_HEADER_GENERAL] Error marcando notificaciones como leídas: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error marcando notificaciones como leídas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Crear una notificación de prueba (para testing)
     * POST /api/notificaciones/prueba/{idUsuario}
     */
    @PostMapping("/prueba/{idUsuario}")
    public ResponseEntity<Map<String, Object>> crearNotificacionPrueba(@PathVariable Integer idUsuario) {
        try {
            System.out.println("🧪 [NOTIFICACIONES_PRUEBA] Creando notificación de prueba para usuario: " + idUsuario);
            
            Notificacion notificacion = notificacionCU.crearNotificacionAlerta(
                "CURSO_VERANO", 
                idUsuario, 
                "Notificación de Prueba", 
                "Esta es una notificación de prueba para verificar que el sistema funciona correctamente.",
                false
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación de prueba creada exitosamente");
            response.put("notificacion", notificacion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICACIONES_PRUEBA] Error creando notificación de prueba: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear notificación de prueba: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
