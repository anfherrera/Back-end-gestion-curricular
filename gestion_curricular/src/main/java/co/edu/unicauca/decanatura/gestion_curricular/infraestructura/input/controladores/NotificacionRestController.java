package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.NotificacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.NotificacionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.NotificacionMapperDominio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Validated
@Tag(name = "Notificaciones", description = "API para la gestión de notificaciones del sistema")
public class NotificacionRestController {

    private final GestionarNotificacionCUIntPort notificacionCU;
    private final NotificacionMapperDominio notificacionMapper;
    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final GestionarSolicitudGatewayIntPort solicitudGateway;

    @PostMapping("/crear")
    @Operation(summary = "Crear una notificación", description = "Crea una nueva notificación en el sistema")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<NotificacionDTORespuesta> crearNotificacion(
            @Valid @RequestBody NotificacionDTOPeticion peticion) {
        try {
            Notificacion notificacion = notificacionMapper.mappearDeNotificacionDTOPeticionANotificacion(peticion);
            
            // Resolver usuario
            Usuario usuario = usuarioGateway.obtenerUsuarioPorId(peticion.getIdUsuario());
            if (usuario == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            notificacion.setObjUsuario(usuario);
            
            // Resolver solicitud si existe
            if (peticion.getIdSolicitud() != null) {
                Solicitud solicitud = solicitudGateway.obtenerSolicitudPorId(peticion.getIdSolicitud());
                notificacion.setObjSolicitud(solicitud);
            }
            
            // Resolver curso si existe (se puede obtener de la solicitud o directamente)
            if (peticion.getIdCurso() != null && notificacion.getObjSolicitud() != null) {
                notificacion.setObjCurso(notificacion.getObjSolicitud().getObjCursoOfertadoVerano());
            }
            
            // Establecer fecha de creación si no viene
            if (peticion.getFechaCreacion() == null) {
                notificacion.setFechaCreacion(new Date());
            } else {
                notificacion.setFechaCreacion(peticion.getFechaCreacion());
            }
            
            // Establecer valores por defecto
            if (peticion.getLeida() == null) {
                notificacion.setLeida(false);
            } else {
                notificacion.setLeida(peticion.getLeida());
            }
            
            if (peticion.getEsUrgente() == null) {
                notificacion.setEsUrgente(false);
            } else {
                notificacion.setEsUrgente(peticion.getEsUrgente());
            }
            
            Notificacion notificacionCreada = notificacionCU.crearNotificacion(notificacion);
            return new ResponseEntity<>(
                    notificacionMapper.mappearDeNotificacionARespuesta(notificacionCreada),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear notificación", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener notificaciones por usuario", 
               description = "Obtiene todas las notificaciones de un usuario")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<List<NotificacionDTORespuesta>> obtenerPorUsuario(
            @Parameter(description = "ID del usuario") @Min(1) @PathVariable Integer idUsuario) {
        try {
            // Validar permisos: estudiantes solo pueden ver sus propias notificaciones
            if (!tienePermisoParaVerNotificaciones(idUsuario)) {
                log.warn("Acceso denegado: usuario no tiene permisos para ver notificaciones del usuario {}", idUsuario);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            
            List<Notificacion> notificaciones = notificacionCU.buscarPorUsuario(idUsuario);
            List<NotificacionDTORespuesta> respuesta = notificaciones.stream()
                    .map(notificacionMapper::mappearDeNotificacionARespuesta)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener notificaciones del usuario {}", idUsuario, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{idUsuario}/no-leidas")
    @Operation(summary = "Obtener notificaciones no leídas por usuario",
               description = "Obtiene todas las notificaciones no leídas de un usuario")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<List<NotificacionDTORespuesta>> obtenerNoLeidasPorUsuario(
            @Parameter(description = "ID del usuario") @Min(1) @PathVariable Integer idUsuario) {
        try {
            // Validar permisos: estudiantes solo pueden ver sus propias notificaciones
            if (!tienePermisoParaVerNotificaciones(idUsuario)) {
                log.warn("Acceso denegado: usuario no tiene permisos para ver notificaciones del usuario {}", idUsuario);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            
            List<Notificacion> notificaciones = notificacionCU.buscarNoLeidasPorUsuario(idUsuario);
            List<NotificacionDTORespuesta> respuesta = notificaciones.stream()
                    .map(notificacionMapper::mappearDeNotificacionARespuesta)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener notificaciones no leídas del usuario {}", idUsuario, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{idUsuario}/urgentes")
    @Operation(summary = "Obtener notificaciones urgentes por usuario",
               description = "Obtiene todas las notificaciones urgentes de un usuario")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<List<NotificacionDTORespuesta>> obtenerUrgentesPorUsuario(
            @Parameter(description = "ID del usuario") @Min(1) @PathVariable Integer idUsuario) {
        try {
            // Validar permisos: estudiantes solo pueden ver sus propias notificaciones
            if (!tienePermisoParaVerNotificaciones(idUsuario)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            
            List<Notificacion> notificaciones = notificacionCU.buscarUrgentesPorUsuario(idUsuario);
            List<NotificacionDTORespuesta> respuesta = notificaciones.stream()
                    .map(notificacionMapper::mappearDeNotificacionARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener notificaciones urgentes del usuario {}", idUsuario, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuario/{idUsuario}/contar-no-leidas")
    @Operation(summary = "Contar notificaciones no leídas por usuario",
               description = "Cuenta cuántas notificaciones no leídas tiene un usuario")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<Long> contarNoLeidasPorUsuario(
            @Parameter(description = "ID del usuario") @Min(1) @PathVariable Integer idUsuario) {
        try {
            // Validar permisos: estudiantes solo pueden ver sus propias notificaciones
            if (!tienePermisoParaVerNotificaciones(idUsuario)) {
                log.warn("Acceso denegado: usuario no tiene permisos para contar notificaciones del usuario {}", idUsuario);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            
            Long cantidad = notificacionCU.contarNoLeidasPorUsuario(idUsuario);
            return ResponseEntity.ok(cantidad);
        } catch (Exception e) {
            log.error("Error al contar notificaciones no leídas del usuario {}", idUsuario, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tipo-solicitud/{tipoSolicitud}")
    @Operation(summary = "Obtener notificaciones por tipo de solicitud",
               description = "Obtiene todas las notificaciones de un tipo de solicitud específico")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario')")
    public ResponseEntity<List<NotificacionDTORespuesta>> obtenerPorTipoSolicitud(
            @Parameter(description = "Tipo de solicitud") @PathVariable String tipoSolicitud) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarPorTipoSolicitud(tipoSolicitud);
            List<NotificacionDTORespuesta> respuesta = notificaciones.stream()
                    .map(notificacionMapper::mappearDeNotificacionARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener notificaciones por tipo de solicitud {}", tipoSolicitud, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/solicitud/{idSolicitud}")
    @Operation(summary = "Obtener notificaciones por solicitud",
               description = "Obtiene todas las notificaciones relacionadas con una solicitud")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<List<NotificacionDTORespuesta>> obtenerPorSolicitud(
            @Parameter(description = "ID de la solicitud") @Min(1) @PathVariable Integer idSolicitud) {
        try {
            List<Notificacion> notificaciones = notificacionCU.buscarPorSolicitud(idSolicitud);
            
            // Validar permisos: estudiantes solo pueden ver notificaciones de sus propias solicitudes
            if (!notificaciones.isEmpty()) {
                Integer idUsuarioNotificacion = notificaciones.get(0).getObjUsuario().getId_usuario();
                if (!tienePermisoParaVerNotificaciones(idUsuarioNotificacion)) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            
            List<NotificacionDTORespuesta> respuesta = notificaciones.stream()
                    .map(notificacionMapper::mappearDeNotificacionARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al obtener notificaciones de la solicitud {}", idSolicitud, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{idNotificacion}")
    @Operation(summary = "Obtener notificación por ID",
               description = "Obtiene una notificación específica por su ID")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<NotificacionDTORespuesta> obtenerPorId(
            @Parameter(description = "ID de la notificación") @Min(1) @PathVariable Integer idNotificacion) {
        try {
            Notificacion notificacion = notificacionCU.obtenerNotificacionPorId(idNotificacion);
            if (notificacion == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Validar permisos: estudiantes solo pueden ver sus propias notificaciones
            if (!tienePermisoParaVerNotificaciones(notificacion.getObjUsuario().getId_usuario())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            
            return ResponseEntity.ok(notificacionMapper.mappearDeNotificacionARespuesta(notificacion));
        } catch (Exception e) {
            log.error("Error al obtener notificación {}", idNotificacion, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{idNotificacion}/marcar-leida")
    @Operation(summary = "Marcar notificación como leída",
               description = "Marca una notificación específica como leída")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<Void> marcarComoLeida(
            @Parameter(description = "ID de la notificación") @Min(1) @PathVariable Integer idNotificacion) {
        try {
            // Validar permisos: estudiantes solo pueden marcar sus propias notificaciones
            Notificacion notificacion = notificacionCU.obtenerNotificacionPorId(idNotificacion);
            if (notificacion == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            if (!tienePermisoParaVerNotificaciones(notificacion.getObjUsuario().getId_usuario())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            
            notificacionCU.marcarComoLeida(idNotificacion);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error al marcar notificación {} como leída", idNotificacion, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/usuario/{idUsuario}/marcar-todas-leidas")
    @Operation(summary = "Marcar todas las notificaciones como leídas",
               description = "Marca todas las notificaciones de un usuario como leídas")
    @PreAuthorize("hasAnyRole('Administrador', 'Funcionario', 'Coordinador', 'Secretario', 'Estudiante')")
    public ResponseEntity<Void> marcarTodasComoLeidas(
            @Parameter(description = "ID del usuario") @Min(1) @PathVariable Integer idUsuario) {
        try {
            // Validar permisos: estudiantes solo pueden marcar sus propias notificaciones
            if (!tienePermisoParaVerNotificaciones(idUsuario)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            
            notificacionCU.marcarTodasComoLeidas(idUsuario);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error al marcar todas las notificaciones del usuario {} como leídas", idUsuario, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Valida si el usuario autenticado tiene permiso para ver/modificar notificaciones del usuario especificado.
     * - Los estudiantes solo pueden ver/modificar sus propias notificaciones
     * - Los funcionarios, coordinadores, secretarios y administradores pueden ver/modificar cualquier notificación
     */
    private boolean tienePermisoParaVerNotificaciones(Integer idUsuario) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            String email = authentication.getName();
            Usuario usuarioAutenticado = usuarioGateway.buscarUsuarioPorCorreo(email);
            
            if (usuarioAutenticado == null) {
                log.warn("Usuario autenticado no encontrado: {}", email);
                return false;
            }

            // Si es el mismo usuario, siempre puede ver sus notificaciones
            if (usuarioAutenticado.getId_usuario().equals(idUsuario)) {
                return true;
            }

            // Verificar si tiene rol administrativo
            String rolNombre = usuarioAutenticado.getObjRol() != null 
                    ? usuarioAutenticado.getObjRol().getNombre() 
                    : null;

            // Funcionarios, coordinadores, secretarios y administradores pueden ver notificaciones de otros
            return "Funcionario".equals(rolNombre) || 
                   "Coordinador".equals(rolNombre) || 
                   "Secretario".equals(rolNombre) ||
                   "Administrador".equals(rolNombre);
        } catch (Exception e) {
            log.error("Error al validar permisos para ver notificaciones", e);
            return false;
        }
    }
}

