package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.DecisionSolicitudDTOPeticion;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoNuevoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Preinscripciones a cursos de verano y solicitudes de curso nuevo.
 * Ruta base compartida con los demás controladores de cursos de verano.
 */
@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
@Slf4j
public class PreinscripcionesVeranoRestController {

    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final CursosOfertadosMapperDominio cursoMapper;
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudMapper;

    /**
     * Obtener solicitudes de un estudiante especifico
     * GET /api/cursos-intersemestrales/estudiante/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id)")
    @GetMapping("/estudiante/{id}")
    public ResponseEntity<List<SolicitudCursoVeranoPreinscripcionDTORespuesta>> obtenerSolicitudesEstudiante(
            @Min(value = 1) @PathVariable Integer id) {
        List<SolicitudCursoVeranoPreinscripcion> solicitudes = solicitudCU.buscarSolicitudesPorUsuario(id);
        List<SolicitudCursoVeranoPreinscripcionDTORespuesta> respuesta = solicitudes.stream()
                .map(solicitudMapper::mappearDeSolicitudCursoVeranoPreinscripcionARespuesta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener preinscripciones de un usuario especifico (endpoint que espera el frontend)
     * GET /api/cursos-intersemestrales/preinscripciones/usuario/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id)")
    @GetMapping("/preinscripciones/usuario/{id}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPreinscripcionesPorUsuario(
            @Min(value = 1) @PathVariable Integer id) {
        
        List<Map<String, Object>> preinscripciones = new ArrayList<>();
        
        // Obtener preinscripciones reales de la base de datos
        List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(id);
        
        
        for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
            Map<String, Object> preinscripcionMap = new HashMap<>();
            preinscripcionMap.put("id", preinscripcion.getId_solicitud());
            preinscripcionMap.put("fecha", preinscripcion.getFecha_registro_solicitud());
            String estadoActual = preinscripcion.estadoActual("Enviado");
            preinscripcionMap.put("estado", estadoActual);
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimoEstado =
                preinscripcion.ultimoRegistroEstado();
            String comentarioEstado = ultimoEstado == null ? null : ultimoEstado.getComentario();
            if (comentarioEstado != null && !comentarioEstado.trim().isEmpty()) {
                preinscripcionMap.put("comentarioEstado", comentarioEstado);
            }
            preinscripcionMap.put("tipo", "Preinscripcion");
            preinscripcionMap.put("estudianteId", preinscripcion.getObjUsuario().getId_usuario());
            
            // Informacion del curso
            if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                preinscripcionMap.put("cursoId", preinscripcion.getObjCursoOfertadoVerano().getId_curso());
                if (preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                    preinscripcionMap.put("cursoNombre", preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
                }
            }
            
            // Acciones disponibles basadas en el estado
            List<String> accionesDisponibles = new ArrayList<>();
            
            if (EstadoSolicitudCursoVerano.APROBADO.texto().equals(estadoActual)) {
                accionesDisponibles.add("inscribirse");
            } else if (EstadoSolicitudCursoVerano.RECHAZADO.texto().equals(estadoActual)) {
                accionesDisponibles.add("ver_motivo");
            } else {
                accionesDisponibles.add("esperando_respuesta");
            }
            
            preinscripcionMap.put("accionesDisponibles", accionesDisponibles);
            
            preinscripciones.add(preinscripcionMap);
        }
        
        return ResponseEntity.ok(preinscripciones);
    }

    /**
     * Obtener preinscripciones por estudiante (alias para compatibilidad con pruebas)
     * GET /api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/{id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id)")
    @GetMapping("/solicitudes/preinscripcion/estudiante/{id}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPreinscripcionesPorEstudianteAlias(@PathVariable Integer id) {
        return obtenerPreinscripcionesPorUsuario(id);
    }

    /**
     * Crear solicitud de curso nuevo
     * POST /api/cursos-intersemestrales/solicitudes-curso-nuevo
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/solicitudes-curso-nuevo")
    public ResponseEntity<Map<String, Object>> crearSolicitudCursoNuevo(@RequestBody SolicitudCursoNuevoDTOPeticion peticion) {
        try {

            // Mapear el DTO a nuestro modelo de dominio
            SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
            solicitudDominio.setNombre_estudiante(peticion.getNombreCompleto());
            solicitudDominio.setCodigo_estudiante(peticion.getCodigo());
            
            // Mapear la condicion con validacion
            try {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Condicion invalida: " + peticion.getCondicion() + ". Valores validos: " + java.util.Arrays.toString(CondicionSolicitudVerano.values()));
            }
            
            solicitudDominio.setObservacion("Solicitud de apertura de curso: " + peticion.getCurso());
            
            solicitudDominio.setNombre_solicitud(co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos
                    .NombreSolicitud.de(co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos
                            .NombreSolicitud.CURSO_VERANO, peticion.getNombreCompleto()));
            solicitudDominio.setFecha_registro_solicitud(new java.util.Date());
            
            solicitudDominio.setPeriodo_academico(PeriodoAcademicoEnum.completarSiFalta(solicitudDominio.getPeriodo_academico()));

            // Crear un curso con ID = 0 para indicar que es un curso nuevo
            CursoOfertadoVerano cursoTemporal = new CursoOfertadoVerano();
            cursoTemporal.setId_curso(0); // ID = 0 indica curso nuevo
            solicitudDominio.setObjCursoOfertadoVerano(cursoTemporal);
            
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuarioTemporal = 
                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
            usuarioTemporal.setId_usuario(peticion.getIdUsuario());
            solicitudDominio.setObjUsuario(usuarioTemporal);

            // Llamar al caso de uso
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);
            

            // Crear respuesta JSON con la estructura esperada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_solicitud", solicitudGuardada.getId_solicitud());
            respuesta.put("nombreCompleto", peticion.getNombreCompleto());
            respuesta.put("codigo", peticion.getCodigo());
            respuesta.put("curso", peticion.getCurso());
            respuesta.put("condicion", peticion.getCondicion());
            respuesta.put("fecha", solicitudGuardada.getFecha_registro_solicitud().toString());
            respuesta.put("estado", "Pendiente");
            
            // Crear objeto usuario
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id_usuario", peticion.getIdUsuario());
            usuario.put("nombre_completo", peticion.getNombreCompleto());
            respuesta.put("objUsuario", usuario);
            
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error de validacion: " + e.getMessage());
            error.put("tipo", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Crear preinscripcion a curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano/preinscripciones
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/cursos-verano/preinscripciones")
    public ResponseEntity<?> crearPreinscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        
        // Validar datos obligatorios
        if (peticion.getIdUsuario() == null || peticion.getIdCurso() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Los campos idUsuario e idCurso son obligatorios");
            error.put("codigo", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Mapear el DTO a nuestro modelo de dominio
        SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
        solicitudDominio.setNombre_estudiante("Estudiante"); // Valor por defecto
        solicitudDominio.setCodigo_estudiante("EST001"); // Valor por defecto
        solicitudDominio.setObservacion(peticion.getNombreSolicitud() != null ? peticion.getNombreSolicitud() : "Preinscripcion curso de verano");
        
        // Usar la condicion del frontend o valor por defecto
        if (peticion.getCondicion() != null && !peticion.getCondicion().trim().isEmpty()) {
            try {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
            }
        } else {
            solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
        }
        
        
        // Crear curso con el ID real
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(peticion.getIdCurso());
        solicitudDominio.setObjCursoOfertadoVerano(curso);
        
        
        // Crear usuario
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
        usuario.setId_usuario(peticion.getIdUsuario());
        solicitudDominio.setObjUsuario(usuario);

        
        // Verificar si ya existe una preinscripcion para este usuario y curso
        Solicitud preinscripcionExistente = solicitudCU.buscarPreinscripcionPorUsuarioYCurso(peticion.getIdUsuario(), peticion.getIdCurso());
        
        if (preinscripcionExistente != null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ya tienes una preinscripcion activa para este curso");
            error.put("codigo", "DUPLICATE_PREINSCRIPTION");
            return ResponseEntity.badRequest().body(error);
        }
        

        // Llamar al gateway directamente para guardar en la base de datos
        SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

        var respuesta = new PreinscripcionCreada(solicitudGuardada.getId_solicitud(), peticion.getIdUsuario(),
                peticion.getIdCurso(), peticion.getNombreSolicitud(), solicitudGuardada.getFecha_registro_solicitud(),
                "Pendiente", "Preinscripcion creada exitosamente");
        return ResponseEntity.status(201).body(respuesta);
    }

    record PreinscripcionCreada(Integer id_preinscripcion, Integer idUsuario, Integer idCurso, String nombreSolicitud,
                                java.util.Date fecha, String estado, String mensaje) {
    }

    /**
     * Crear preinscripcion a curso de verano (alias para compatibilidad con pruebas)
     * POST /api/cursos-intersemestrales/solicitudes/preinscripcion
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/solicitudes/preinscripcion")
    public ResponseEntity<?> crearPreinscripcionAlias(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        return crearPreinscripcion(peticion);
    }

    // ==================== ENDPOINTS PARA GESTION DE PREINSCRIPCIONES ====================

    /**
     * Obtener preinscripciones por curso (datos reales de la base de datos)
     * GET /api/cursos-intersemestrales/preinscripciones/curso/{idCurso}
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping("/preinscripciones/curso/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> getPreinscripcionesPorCurso(
            @PathVariable Long idCurso) {
        
        List<Map<String, Object>> preinscripciones = new ArrayList<>();
        
        // Obtener preinscripciones reales de la base de datos
        List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarPreinscripcionesPorCurso(idCurso.intValue());
        
        
        for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
            Map<String, Object> preinscripcionMap = new HashMap<>();
            
            // Informacion basica de la preinscripcion
            preinscripcionMap.put("id_preinscripcion", preinscripcion.getId_solicitud());
            preinscripcionMap.put("id_solicitud", preinscripcion.getId_solicitud()); // Para compatibilidad con frontend
            preinscripcionMap.put("fecha_preinscripcion", preinscripcion.getFecha_registro_solicitud());
            
            // Estado de la solicitud
            String estado = preinscripcion.estadoActual("Pendiente"); // Valor por defecto
            preinscripcionMap.put("estado", estado);
            
            // Observaciones
            preinscripcionMap.put("observaciones", preinscripcion.getObservacion() != null ? preinscripcion.getObservacion() : "");
            
            // Condicion de la solicitud
            preinscripcionMap.put("condicion", preinscripcion.getCodicion_solicitud() != null ? 
                preinscripcion.getCodicion_solicitud().toString() : "Primera_Vez");
            
            // Informacion del usuario/estudiante
            if (preinscripcion.getObjUsuario() != null) {
                Map<String, Object> usuarioMap = new HashMap<>();
                usuarioMap.put("id_usuario", preinscripcion.getObjUsuario().getId_usuario());
                usuarioMap.put("nombre_completo", preinscripcion.getObjUsuario().getNombre_completo());
                usuarioMap.put("correo", preinscripcion.getObjUsuario().getCorreo());
                usuarioMap.put("codigo", preinscripcion.getObjUsuario().getCodigo());
                usuarioMap.put("codigo_estudiante", preinscripcion.getCodigo_estudiante());
                
                // Informacion del rol
                if (preinscripcion.getObjUsuario().getObjRol() != null) {
                    Map<String, Object> rolMap = new HashMap<>();
                    rolMap.put("id_rol", preinscripcion.getObjUsuario().getObjRol().getId_rol());
                    rolMap.put("nombre", preinscripcion.getObjUsuario().getObjRol().getNombre());
                    usuarioMap.put("objRol", rolMap);
                }
                
                preinscripcionMap.put("objUsuario", usuarioMap);
            }
            
            // Informacion del curso usando el mapper existente
            if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mappearDeCursoOfertadoARespuesta(preinscripcion.getObjCursoOfertadoVerano());
                
                Map<String, Object> cursoMap = new HashMap<>();
                cursoMap.put("id_curso", cursoDTO.getId_curso());
                cursoMap.put("nombre_curso", cursoDTO.getNombre_curso());
                cursoMap.put("codigo_curso", cursoDTO.getCodigo_curso());
                cursoMap.put("descripcion", cursoDTO.getDescripcion());
                cursoMap.put("fecha_inicio", cursoDTO.getFecha_inicio());
                cursoMap.put("fecha_fin", cursoDTO.getFecha_fin());
                cursoMap.put("cupo_maximo", cursoDTO.getCupo_maximo());
                cursoMap.put("cupo_estimado", cursoDTO.getCupo_estimado());
                cursoMap.put("cupo_disponible", cursoDTO.getCupo_disponible());
                cursoMap.put("espacio_asignado", cursoDTO.getEspacio_asignado());
                cursoMap.put("estado", cursoDTO.getEstado());
                cursoMap.put("objMateria", cursoDTO.getObjMateria());
                cursoMap.put("objDocente", cursoDTO.getObjDocente());
                
                preinscripcionMap.put("objCurso", cursoMap);
            }
            
            preinscripciones.add(preinscripcionMap);
        }
        
        
        return ResponseEntity.ok(preinscripciones);
    }

    /**
     * Actualizar observaciones de preinscripcion
     * PUT /api/cursos-intersemestrales/preinscripciones/{idPreinscripcion}/observaciones
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idPreinscripcion}/observaciones")
    public ResponseEntity<Map<String, Object>> actualizarObservacionesPreinscripcion(
            @PathVariable Long idPreinscripcion,
            @RequestBody @Valid DecisionSolicitudDTOPeticion request) {
        String observaciones = request.getObservaciones();
        
        if (observaciones == null || observaciones.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Las observaciones no pueden estar vacias");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Buscar la solicitud en la base de datos
        SolicitudCursoVeranoPreinscripcion solicitud = solicitudCU.buscarSolicitudPorId(idPreinscripcion.intValue());
        if (solicitud == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No se encontro la preinscripcion con ID: " + idPreinscripcion);
            return ResponseEntity.notFound().build();
        }
        
        // Actualizar las observaciones directamente usando el gateway de solicitudes de curso verano
        solicitud.setObservacion(observaciones);
        SolicitudCursoVeranoPreinscripcion solicitudActualizada = solicitudCU.actualizarObservacionesPreinscripcion(solicitud);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("message", "Observaciones actualizadas exitosamente");
        respuesta.put("success", true);
        
        Map<String, Object> preinscripcion = new HashMap<>();
        preinscripcion.put("id_preinscripcion", solicitudActualizada.getId_solicitud());
        preinscripcion.put("observaciones", solicitudActualizada.getObservacion());
        preinscripcion.put("estado", solicitudActualizada.estadoActual("Pendiente"));
        
        respuesta.put("preinscripcion", preinscripcion);
        
        return ResponseEntity.ok(respuesta);
    }

    

    // ==================== ENDPOINTS PARA APROBAR Y RECHAZAR PREINSCRIPCIONES ====================

    /**
     * Aprobar preinscripcion (para funcionarios y coordinadores)
     * PUT /api/cursos-intersemestrales/preinscripciones/{idSolicitud}/aprobar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idSolicitud}/aprobar")
    public ResponseEntity<Map<String, Object>> aprobarPreinscripcion(
            @PathVariable Integer idSolicitud,
            @RequestBody(required = false) @Valid DecisionSolicitudDTOPeticion requestBody) {
        String comentarios = requestBody != null ? requestBody.getComentarios() : null;
        SolicitudCursoVeranoPreinscripcion solicitudAprobada = solicitudCU.aprobarPreinscripcion(idSolicitud, comentarios);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Preinscripcion aprobada exitosamente");
        respuesta.put("id_solicitud", solicitudAprobada.getId_solicitud());
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechazar preinscripcion (para funcionarios y coordinadores)
     * PUT /api/cursos-intersemestrales/preinscripciones/{idSolicitud}/rechazar
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idSolicitud}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarPreinscripcion(
            @PathVariable Integer idSolicitud,
            @RequestBody @Valid DecisionSolicitudDTOPeticion requestBody) {
        String motivo = requestBody.getMotivo();
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar un motivo para el rechazo"));
        }
        motivo = motivo.trim();
        
        SolicitudCursoVeranoPreinscripcion solicitudRechazada = solicitudCU.rechazarPreinscripcion(idSolicitud, motivo);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Preinscripcion rechazada");
        respuesta.put("id_solicitud", solicitudRechazada.getId_solicitud());
        respuesta.put("motivo", motivo);
        
        return ResponseEntity.ok(respuesta);
    }
}
