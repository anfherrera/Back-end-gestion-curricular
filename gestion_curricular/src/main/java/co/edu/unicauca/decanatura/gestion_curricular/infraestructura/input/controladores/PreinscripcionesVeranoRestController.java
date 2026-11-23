package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.NombreSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadoSolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.DecisionSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudCursoNuevoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.Aprobacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.CursoId;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.CursoNombre;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.CursoNuevoSolicitado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.CursoPreinscrito;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.ErrorObservaciones;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.Estudiante;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.Observaciones;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.ObservacionesActualizadas;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.PreinscripcionCurso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.PreinscripcionRechazada;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.PreinscripcionUsuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.Rechazo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.RolEstudiante;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.PreinscripcionesVeranoDTORespuesta.UsuarioSolicitante;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.ErroresCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

/**
 * Preinscripciones a cursos de verano y solicitudes de curso nuevo.
 * Rutas /preinscripciones y /solicitudes-curso-nuevo del prefijo de cursos de verano (EST-7, paso 8b).
 */
@RestController
@RequestMapping("/api/cursos-verano")
@RequiredArgsConstructor
public class PreinscripcionesVeranoRestController {

    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final CursosOfertadosMapperDominio cursoMapper;

    /**
     * Obtener preinscripciones de un usuario especifico (endpoint que espera el frontend)
     * GET /api/cursos-verano/preinscripciones?usuario={id}
     */
    @PreAuthorize("@autorizacion.esStaff() or @autorizacion.esUsuarioActual(#id)")
    @GetMapping(value = "/preinscripciones", params = {"usuario", "!curso"})
    public ResponseEntity<List<PreinscripcionUsuario>> obtenerPreinscripcionesPorUsuario(
            @Min(value = 1) @RequestParam("usuario") Integer id) {

        List<PreinscripcionUsuario> preinscripciones = new ArrayList<>();

        // Obtener preinscripciones reales de la base de datos
        List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(id);


        for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
            String estadoActual = preinscripcion.estadoActual("Enviado");
            EstadoSolicitud ultimoEstado =
                preinscripcion.ultimoRegistroEstado();
            String comentarioEstado = ultimoEstado == null ? null : ultimoEstado.getComentario();
            if (comentarioEstado != null && comentarioEstado.trim().isEmpty()) {
                comentarioEstado = null;
            }
            Integer estudianteId = preinscripcion.getObjUsuario().getIdUsuario();

            // Informacion del curso
            CursoId cursoId = null;
            CursoNombre cursoNombre = null;
            if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                cursoId = new CursoId(preinscripcion.getObjCursoOfertadoVerano().getIdCurso());
                if (preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                    cursoNombre = new CursoNombre(preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
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
            
            preinscripciones.add(new PreinscripcionUsuario(preinscripcion.getIdSolicitud(),
                preinscripcion.getFechaRegistroSolicitud(), estadoActual, comentarioEstado, "Preinscripcion",
                estudianteId, cursoId, cursoNombre, accionesDisponibles));
        }

        return ResponseEntity.ok(preinscripciones);
    }

    /** Sin filtro, o con los dos a la vez: 400 con lo que se admite. */
    @GetMapping("/preinscripciones")
    public ResponseEntity<Void> preinscripcionesSinFiltro() {
        throw new IllegalArgumentException("Filtro no válido: use usuario o curso, uno solo");
    }

    /**
     * Crear solicitud de curso nuevo
     * POST /api/cursos-verano/solicitudes-curso-nuevo
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/solicitudes-curso-nuevo")
    public ResponseEntity<CursoNuevoSolicitado> crearSolicitudCursoNuevo(@RequestBody SolicitudCursoNuevoDTOPeticion peticion) {
        return ErroresCursosVerano.alCrearCursoNuevo(() -> {

            // Mapear el DTO a nuestro modelo de dominio
            SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
            solicitudDominio.setNombreEstudiante(peticion.getNombreCompleto());
            solicitudDominio.setCodigoEstudiante(peticion.getCodigo());
            
            // Mapear la condicion con validacion; sin condición, el mismo 400 que con una inválida
            if (peticion.getCondicion() == null) {
                throw new IllegalArgumentException("La condicion es obligatoria. Valores validos: "
                        + Arrays.toString(CondicionSolicitudVerano.values()));
            }
            try {
                solicitudDominio.setCondicionSolicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Condicion invalida: " + peticion.getCondicion() + ". Valores validos: " + Arrays.toString(CondicionSolicitudVerano.values()));
            }
            
            solicitudDominio.setObservacion("Solicitud de apertura de curso: " + peticion.getCurso());
            
            solicitudDominio.setNombreSolicitud(NombreSolicitud.de(NombreSolicitud.CURSO_VERANO, peticion.getNombreCompleto()));
            solicitudDominio.setFechaRegistroSolicitud(new Date());
            
            solicitudDominio.setPeriodoAcademico(PeriodoAcademicoEnum.oActual(solicitudDominio.getPeriodoAcademico()));

            // Crear un curso con ID = 0 para indicar que es un curso nuevo
            CursoOfertadoVerano cursoTemporal = new CursoOfertadoVerano();
            cursoTemporal.setIdCurso(0); // ID = 0 indica curso nuevo
            solicitudDominio.setObjCursoOfertadoVerano(cursoTemporal);
            
            Usuario usuarioTemporal =
                new Usuario();
            usuarioTemporal.setIdUsuario(peticion.getIdUsuario());
            solicitudDominio.setObjUsuario(usuarioTemporal);

            // Llamar al caso de uso
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);
            

            // Crear respuesta JSON con la estructura esperada
            return ResponseEntity.ok(new CursoNuevoSolicitado(solicitudGuardada.getIdSolicitud(),
                peticion.getNombreCompleto(), peticion.getCodigo(), peticion.getCurso(), peticion.getCondicion(),
                solicitudGuardada.getFechaRegistroSolicitud(), "Pendiente",
                new UsuarioSolicitante(peticion.getIdUsuario(), peticion.getNombreCompleto())));
        });
    }

    /**
     * Crear preinscripcion a curso de verano
     * POST /api/cursos-verano/preinscripciones
     */
    @PreAuthorize("@autorizacion.esStaff() or #peticion.idUsuario == null or @autorizacion.esUsuarioActual(#peticion.idUsuario)")
    @PostMapping("/preinscripciones")
    public ResponseEntity<?> crearPreinscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        
        // Validar datos obligatorios
        if (peticion.getIdUsuario() == null || peticion.getIdCurso() == null) {
            return ResponseEntity.badRequest().body(new PreinscripcionRechazada(
                "Los campos idUsuario e idCurso son obligatorios", "VALIDATION_ERROR"));
        }
        
        // Mapear el DTO a nuestro modelo de dominio
        SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
        solicitudDominio.setNombreEstudiante("Estudiante"); // Valor por defecto
        solicitudDominio.setCodigoEstudiante("EST001"); // Valor por defecto
        solicitudDominio.setObservacion(peticion.getNombreSolicitud() != null ? peticion.getNombreSolicitud() : "Preinscripcion curso de verano");
        
        // Usar la condicion del frontend o valor por defecto
        if (peticion.getCondicion() != null && !peticion.getCondicion().trim().isEmpty()) {
            try {
                solicitudDominio.setCondicionSolicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                solicitudDominio.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
            }
        } else {
            solicitudDominio.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
        }
        
        
        // Crear curso con el ID real
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(peticion.getIdCurso());
        solicitudDominio.setObjCursoOfertadoVerano(curso);
        
        
        // Crear usuario
        Usuario usuario =
            new Usuario();
        usuario.setIdUsuario(peticion.getIdUsuario());
        solicitudDominio.setObjUsuario(usuario);

        
        // Verificar si ya existe una preinscripcion para este usuario y curso
        Solicitud preinscripcionExistente = solicitudCU.buscarPreinscripcionPorUsuarioYCurso(peticion.getIdUsuario(), peticion.getIdCurso());
        
        if (preinscripcionExistente != null) {
            return ResponseEntity.badRequest().body(new PreinscripcionRechazada(
                "Ya tienes una preinscripcion activa para este curso", "DUPLICATE_PREINSCRIPTION"));
        }
        

        // Llamar al gateway directamente para guardar en la base de datos
        SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

        var respuesta = new PreinscripcionCreada(solicitudGuardada.getIdSolicitud(), peticion.getIdUsuario(),
                peticion.getIdCurso(), peticion.getNombreSolicitud(), solicitudGuardada.getFechaRegistroSolicitud(),
                "Pendiente", "Preinscripcion creada exitosamente");
        return ResponseEntity.status(201).body(respuesta);
    }

    record PreinscripcionCreada(Integer id_preinscripcion, Integer idUsuario, Integer idCurso, String nombreSolicitud,
                                Date fecha, String estado, String mensaje) {
    }

    // ==================== ENDPOINTS PARA GESTION DE PREINSCRIPCIONES ====================

    /**
     * Obtener preinscripciones por curso (datos reales de la base de datos)
     * GET /api/cursos-verano/preinscripciones?curso={idCurso}
     */
    @PreAuthorize("hasAnyRole(" + Autorizacion.ROLES_STAFF + ")")
    @GetMapping(value = "/preinscripciones", params = {"curso", "!usuario"})
    public ResponseEntity<List<PreinscripcionCurso>> getPreinscripcionesPorCurso(
            @RequestParam("curso") Long idCurso) {

        List<PreinscripcionCurso> preinscripciones = new ArrayList<>();

        // Obtener preinscripciones reales de la base de datos
        List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarPreinscripcionesPorCurso(idCurso.intValue());


        for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
            // Estado de la solicitud
            String estado = preinscripcion.estadoActual("Pendiente"); // Valor por defecto

            // Informacion del usuario/estudiante
            Estudiante estudiante = null;
            if (preinscripcion.getObjUsuario() != null) {
                Usuario usuario = preinscripcion.getObjUsuario();
                // Informacion del rol
                RolEstudiante rol = usuario.getObjRol() == null ? null
                    : new RolEstudiante(usuario.getObjRol().getIdRol(), usuario.getObjRol().getNombre());
                estudiante = new Estudiante(usuario.getIdUsuario(), usuario.getNombreCompleto(), usuario.getCorreo(),
                    usuario.getCodigo(), preinscripcion.getCodigoEstudiante(), rol);
            }

            // Informacion del curso usando el mapper existente
            CursoPreinscrito curso = null;
            if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mapearDeCursoOfertadoARespuesta(preinscripcion.getObjCursoOfertadoVerano());
                curso = new CursoPreinscrito(cursoDTO.getId_curso(), cursoDTO.getNombre_curso(),
                    cursoDTO.getCodigo_curso(), cursoDTO.getDescripcion(), cursoDTO.getFecha_inicio(),
                    cursoDTO.getFecha_fin(), cursoDTO.getCupo_maximo(), cursoDTO.getCupo_estimado(),
                    cursoDTO.getCupo_disponible(), cursoDTO.getEspacio_asignado(), cursoDTO.getEstado(),
                    cursoDTO.getObjMateria(), cursoDTO.getObjDocente());
            }

            // id_solicitud repite id_preinscripcion para compatibilidad con el frontend
            preinscripciones.add(new PreinscripcionCurso(preinscripcion.getIdSolicitud(),
                preinscripcion.getIdSolicitud(), preinscripcion.getFechaRegistroSolicitud(), estado,
                preinscripcion.getObservacion() != null ? preinscripcion.getObservacion() : "",
                preinscripcion.getCondicionSolicitud() != null
                    ? preinscripcion.getCondicionSolicitud().toString() : "Primera_Vez",
                estudiante, curso));
        }
        
        
        return ResponseEntity.ok(preinscripciones);
    }

    /**
     * Actualizar observaciones de preinscripcion
     * PUT /api/cursos-verano/preinscripciones/{idPreinscripcion}/observaciones
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idPreinscripcion:\\d+}/observaciones")
    public ResponseEntity<?> actualizarObservacionesPreinscripcion(
            @PathVariable Long idPreinscripcion,
            @RequestBody @Valid DecisionSolicitudDTOPeticion request) {
        String observaciones = request.getObservaciones();

        if (observaciones == null || observaciones.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorObservaciones("Las observaciones no pueden estar vacias"));
        }

        // Buscar la solicitud en la base de datos (si no existe, 404 sin cuerpo)
        SolicitudCursoVeranoPreinscripcion solicitud = solicitudCU.buscarSolicitudPorId(idPreinscripcion.intValue());
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }

        // Actualizar las observaciones directamente usando el gateway de solicitudes de curso verano
        solicitud.setObservacion(observaciones);
        SolicitudCursoVeranoPreinscripcion solicitudActualizada = solicitudCU.actualizarObservacionesPreinscripcion(solicitud);

        return ResponseEntity.ok(new ObservacionesActualizadas("Observaciones actualizadas exitosamente", true,
            new Observaciones(solicitudActualizada.getIdSolicitud(), solicitudActualizada.getObservacion(),
                solicitudActualizada.estadoActual("Pendiente"))));
    }

    

    // ==================== ENDPOINTS PARA APROBAR Y RECHAZAR PREINSCRIPCIONES ====================

    /**
     * Aprobar o rechazar una preinscripcion (para funcionarios y coordinadores)
     * PUT /api/cursos-verano/preinscripciones/{idSolicitud}/estado, con {@code nuevoEstado} aprobada (y
     * {@code comentarios}) o rechazada (y {@code motivo}).
     */
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    @PutMapping("/preinscripciones/{idSolicitud:\\d+}/estado")
    public ResponseEntity<?> cambiarEstadoPreinscripcion(
            @PathVariable Integer idSolicitud,
            @RequestBody @Valid DecisionSolicitudDTOPeticion requestBody) {
        String nuevoEstado = requestBody.getNuevoEstado();
        if ("aprobada".equals(nuevoEstado)) {
            return aprobarPreinscripcion(idSolicitud, requestBody);
        }
        if ("rechazada".equals(nuevoEstado)) {
            return rechazarPreinscripcion(idSolicitud, requestBody);
        }
        throw new IllegalArgumentException("Estado no válido: use aprobada o rechazada");
    }

    private ResponseEntity<Aprobacion> aprobarPreinscripcion(Integer idSolicitud,
            DecisionSolicitudDTOPeticion requestBody) {
        String comentarios = requestBody.getComentarios();
        SolicitudCursoVeranoPreinscripcion solicitudAprobada = solicitudCU.aprobarPreinscripcion(idSolicitud, comentarios);

        return ResponseEntity.ok(new Aprobacion(true, "Preinscripcion aprobada exitosamente",
            solicitudAprobada.getIdSolicitud()));
    }

    private ResponseEntity<?> rechazarPreinscripcion(Integer idSolicitud, DecisionSolicitudDTOPeticion requestBody) {
        String motivo = requestBody.getMotivo();
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar un motivo para el rechazo"));
        }
        motivo = motivo.trim();
        
        SolicitudCursoVeranoPreinscripcion solicitudRechazada = solicitudCU.rechazarPreinscripcion(idSolicitud, motivo);
        
        return ResponseEntity.ok(new Rechazo(true, "Preinscripcion rechazada", solicitudRechazada.getIdSolicitud(),
            motivo));
    }
}
