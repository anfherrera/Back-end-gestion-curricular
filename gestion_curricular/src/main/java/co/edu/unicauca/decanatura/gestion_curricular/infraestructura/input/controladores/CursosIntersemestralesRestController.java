package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoNuevoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CreateCursoDTO;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UpdateCursoDTO;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.servicios.InscripcionService;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
@Slf4j
public class CursosIntersemestralesRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GestionarSolicitudCursoVeranoGatewayIntPort solicitudGateway;
    private final GestionarMateriasCUIntPort materiaCU;
    private final GestionarDocentesCUIntPort docenteCU;
    private final CursosOfertadosMapperDominio cursoMapper;
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudMapper;
    private final InscripcionService inscripcionService;
    private final SolicitudRepositoryInt solicitudRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoRepository;
    private final GestionarArchivosCUIntPort objGestionarArchivos;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final EstadoCursoOfertadoRepositoryInt estadoRepository;
    private final co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort objGestionarUsuarioCU;

    /**
     * Obtener cursos de verano (endpoint principal que llama el frontend)
     * GET /api/cursos-intersemestrales/cursos-verano
     */
    @GetMapping("/cursos-verano")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosVerano() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            log.debug("Total de cursos encontrados: {}", cursos.size());
            
            List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                    .map(curso -> {
                        // Log para verificar docente antes del mapeo
                        if (curso.getObjDocente() == null) {
                            log.warn("Curso ID {} no tiene docente asignado", curso.getId_curso());
                        } else {
                            log.debug("Curso ID {} tiene docente: {}", curso.getId_curso(), curso.getObjDocente().getNombre_docente());
                        }
                        return cursoMapper.mappearDeCursoOfertadoARespuestaDisponible(curso);
                    })
                    .map(dto -> {
                        // Log para verificar docente despues del mapeo
                        if (dto.getObjDocente() == null) {
                            log.warn("DTO del curso ID {} no tiene docente en el mapeo", dto.getId_curso());
                        } else {
                            log.debug("DTO del curso ID {} tiene docente: {}", dto.getId_curso(), dto.getObjDocente().getNombre_docente());
                        }
                        return cursoMapper.postMapCurso(dto);
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error obteniendo cursos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener cursos de verano disponibles para estudiantes (solo estados visibles)
     * GET /api/cursos-intersemestrales/cursos-verano/disponibles
     */
    @GetMapping("/cursos-verano/disponibles")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosVeranoDisponibles() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar solo cursos visibles para estudiantes
            List<CursoOfertadoVerano> cursosDisponibles = cursos.stream()
                    .filter(curso -> {
                        if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                            return false;
                        }
                        String estadoActual = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                        // Estados visibles para estudiantes: Publicado, Preinscripcion, Inscripcion
                        return "Publicado".equals(estadoActual) || 
                               "Preinscripcion".equals(estadoActual) ||
                               "Inscripcion".equals(estadoActual);
                    })
                    .collect(Collectors.toList());
            
            List<CursosOfertadosDTORespuesta> respuesta = cursosDisponibles.stream()
                    .map(cursoMapper::mappearDeCursoOfertadoARespuestaDisponible)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todos los cursos de verano para funcionarios (incluye estados no visibles)
     * GET /api/cursos-intersemestrales/cursos-verano/todos
     */
    @GetMapping("/cursos-verano/todos")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerTodosLosCursosVerano() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Para funcionarios, mostrar todos los cursos sin filtro de estado
            List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                    .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener cursos disponibles para preinscripcion (solo cursos en estado Preinscripcion)
     * GET /api/cursos-intersemestrales/cursos/preinscripcion
     */
    @GetMapping("/cursos/preinscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosPreinscripcion() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar solo cursos en estado de preinscripcion
            List<CursoOfertadoVerano> cursosPreinscripcion = cursos.stream()
                    .filter(curso -> {
                        if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                            return false;
                        }
                        String estadoActual = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                        return "Preinscripcion".equals(estadoActual);
                    })
                    .collect(Collectors.toList());
            
            List<CursosOfertadosDTORespuesta> respuesta = cursosPreinscripcion.stream()
                    .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener cursos disponibles para inscripcion (solo cursos en estado Inscripcion)
     * GET /api/cursos-intersemestrales/cursos/inscripcion
     */
    @GetMapping("/cursos/inscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosInscripcion() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar solo cursos en estado de inscripcion
            List<CursoOfertadoVerano> cursosInscripcion = cursos.stream()
                    .filter(curso -> {
                        if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                            return false;
                        }
                        String estadoActual = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                        return "Inscripcion".equals(estadoActual);
                    })
                    .collect(Collectors.toList());
            
            List<CursosOfertadosDTORespuesta> respuesta = cursosInscripcion.stream()
                    .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener solicitudes de un estudiante especifico
     * GET /api/cursos-intersemestrales/estudiante/{id}
     */
    @GetMapping("/estudiante/{id}")
    public ResponseEntity<List<SolicitudCursoVeranoPreinscripcionDTORespuesta>> obtenerSolicitudesEstudiante(
            @Min(value = 1) @PathVariable Integer id) {
        try {
            List<SolicitudCursoVeranoPreinscripcion> solicitudes = solicitudCU.buscarSolicitudesPorUsuario(id);
            List<SolicitudCursoVeranoPreinscripcionDTORespuesta> respuesta = solicitudes.stream()
                    .map(solicitudMapper::mappearDeSolicitudCursoVeranoPreinscripcionARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint de debug simple para verificar la base de datos
     * GET /api/cursos-intersemestrales/debug/database-check
     */
    @GetMapping("/debug/database-check")
    public ResponseEntity<Map<String, Object>> debugDatabaseCheck() {
        try {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("status", "OK");
            resultado.put("message", "Debug endpoint funcionando");
            
            // Verificar solicitudes por usuario 2 de forma simple
            try {
                List<SolicitudEntity> solicitudesUsuario2 = solicitudRepository.buscarSolicitudesPorUsuario(2);
                resultado.put("solicitudes_usuario_2", solicitudesUsuario2.size());
            } catch (Exception e) {
                resultado.put("error_usuario_2", e.getMessage());
            }
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint de debug para verificar preinscripciones en la base de datos
     * GET /api/cursos-intersemestrales/debug/preinscripciones/{usuarioId}/{cursoId}
     */
    @GetMapping("/debug/preinscripciones/{usuarioId}/{cursoId}")
    public ResponseEntity<Map<String, Object>> debugPreinscripciones(
            @PathVariable Integer usuarioId, @PathVariable Integer cursoId) {
        try {
            log.debug("Verificando preinscripciones en la base de datos para el usuario {} y el curso {}", usuarioId, cursoId);
            
            Map<String, Object> resultado = new HashMap<>();
            
            // 1. Buscar por consulta directa
            Solicitud preinscripcionDirecta = solicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(usuarioId, cursoId);
            resultado.put("preinscripcion_directa", preinscripcionDirecta != null ? "ENCONTRADA" : "NO_ENCONTRADA");
            
            if (preinscripcionDirecta != null) {
                resultado.put("preinscripcion_directa_id", preinscripcionDirecta.getId_solicitud());
                if (preinscripcionDirecta instanceof SolicitudCursoVeranoPreinscripcion) {
                    SolicitudCursoVeranoPreinscripcion preinscripcion = (SolicitudCursoVeranoPreinscripcion) preinscripcionDirecta;
                    String estadoActual = "Sin estado";
                    if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                        estadoActual = preinscripcion.getEstadosSolicitud()
                            .get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                    }
                    resultado.put("preinscripcion_directa_estado", estadoActual);
                }
            }
            
            // 2. Buscar todas las preinscripciones del usuario
            List<SolicitudCursoVeranoPreinscripcion> preinscripcionesUsuario = solicitudCU.buscarSolicitudesPorUsuario(usuarioId);
            resultado.put("total_preinscripciones_usuario", preinscripcionesUsuario.size());
            
            List<Map<String, Object>> preinscripcionesDetalle = new ArrayList<>();
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesUsuario) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("id", preinscripcion.getId_solicitud());
                detalle.put("usuario", preinscripcion.getObjUsuario() != null ? preinscripcion.getObjUsuario().getId_usuario() : null);
                detalle.put("curso", preinscripcion.getObjCursoOfertadoVerano() != null ? preinscripcion.getObjCursoOfertadoVerano().getId_curso() : null);
                
                String estadoActual = "Sin estado";
                if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                    estadoActual = preinscripcion.getEstadosSolicitud()
                        .get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                }
                detalle.put("estado", estadoActual);
                preinscripcionesDetalle.add(detalle);
            }
            resultado.put("preinscripciones_detalle", preinscripcionesDetalle);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error revisando preinscripciones: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener preinscripciones de un usuario especifico (endpoint que espera el frontend)
     * GET /api/cursos-intersemestrales/preinscripciones/usuario/{id}
     */
    @GetMapping("/preinscripciones/usuario/{id}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPreinscripcionesPorUsuario(
            @Min(value = 1) @PathVariable Integer id) {
        try {
            log.debug("Preinscripciones - Obteniendo preinscripciones para usuario ID: {}", id);
            
            List<Map<String, Object>> preinscripciones = new ArrayList<>();
            
            // Obtener preinscripciones reales de la base de datos
            List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(id);
            
            log.debug("Preinscripciones - Preinscripciones encontradas: {}", preinscripcionesReales.size());
            
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
                Map<String, Object> preinscripcionMap = new HashMap<>();
                preinscripcionMap.put("id", preinscripcion.getId_solicitud());
                preinscripcionMap.put("fecha", preinscripcion.getFecha_registro_solicitud());
                String estadoActual = preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                    ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual()
                    : "Enviado";
                preinscripcionMap.put("estado", estadoActual);
                String comentarioEstado = null;
                if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                    co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimoEstado =
                        preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1);
                    comentarioEstado = ultimoEstado.getComentario();
                }
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
                
                if ("Aprobado".equals(estadoActual)) {
                    accionesDisponibles.add("inscribirse");
                } else if ("Rechazado".equals(estadoActual)) {
                    accionesDisponibles.add("ver_motivo");
                } else {
                    accionesDisponibles.add("esperando_respuesta");
                }
                
                preinscripcionMap.put("accionesDisponibles", accionesDisponibles);
                
                preinscripciones.add(preinscripcionMap);
            }
            
            log.debug("Preinscripciones - Respuesta preparada con {} preinscripciones", preinscripciones.size());
            return ResponseEntity.ok(preinscripciones);
            
        } catch (Exception e) {
            log.error("Preinscripciones - Error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener preinscripciones por estudiante (alias para compatibilidad con pruebas)
     * GET /api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/{id}
     */
    @GetMapping("/solicitudes/preinscripcion/estudiante/{id}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPreinscripcionesPorEstudianteAlias(@PathVariable Integer id) {
        // Delegar al metodo principal
        return obtenerPreinscripcionesPorUsuario(id);
    }

    /**
     * Obtener cursos ofertados (metodo legacy para compatibilidad)
     * GET /api/cursos-intersemestrales/cursos/ofertados
     */
    @GetMapping("/cursos/ofertados")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosOfertados() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                    .map(cursoMapper::mappearDeCursoOfertadoARespuesta)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener cursos disponibles para solicitud de curso nuevo
     * GET /api/cursos-intersemestrales/cursos-disponibles
     */
    @GetMapping("/cursos-disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerCursosDisponibles() {
        try {
            // Usar datos reales de la base de datos
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            List<Map<String, Object>> cursosDisponibles = new ArrayList<>();
            
            for (CursoOfertadoVerano curso : cursos) {
                Map<String, Object> cursoMap = new HashMap<>();
                cursoMap.put("id_curso", curso.getId_curso());
                cursoMap.put("nombre_curso", curso.getObjMateria() != null ? curso.getObjMateria().getNombre() : "Sin nombre");
                cursoMap.put("codigo_curso", curso.getObjMateria() != null ? curso.getObjMateria().getCodigo() : "Sin codigo");
                cursoMap.put("creditos", curso.getObjMateria() != null ? curso.getObjMateria().getCreditos() : 0);
                cursoMap.put("descripcion", "Curso de " + (curso.getObjMateria() != null ? curso.getObjMateria().getNombre() : "curso"));
                cursosDisponibles.add(cursoMap);
            }
            
            return ResponseEntity.ok(cursosDisponibles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener todas las materias disponibles para solicitud de cursos intersemestrales
     * GET /api/cursos-intersemestrales/materias-disponibles
     */
    @GetMapping("/materias-disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerMateriasDisponibles() {
        try {
            // Obtener todas las materias de la base de datos
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materias = 
                materiaCU.listarMaterias();
            
            List<Map<String, Object>> materiasDisponibles = new ArrayList<>();
            
            for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materias) {
                Map<String, Object> materiaMap = new HashMap<>();
                materiaMap.put("id_materia", materia.getId_materia());
                materiaMap.put("codigo", materia.getCodigo());
                materiaMap.put("nombre", materia.getNombre());
                materiaMap.put("creditos", materia.getCreditos());
                materiaMap.put("descripcion", materia.getNombre() + " (" + materia.getCodigo() + ") - " + materia.getCreditos() + " creditos");
                materiasDisponibles.add(materiaMap);
            }
            
            return ResponseEntity.ok(materiasDisponibles);
        } catch (Exception e) {
            log.error("Error obteniendo materias disponibles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener condiciones de solicitud disponibles
     * GET /api/cursos-intersemestrales/condiciones-solicitud
     */
    @GetMapping("/condiciones-solicitud")
    public ResponseEntity<String[]> obtenerCondicionesSolicitud() {
        try {
            CondicionSolicitudVerano[] condiciones = CondicionSolicitudVerano.values();
            String[] condicionesString = new String[condiciones.length];
            for (int i = 0; i < condiciones.length; i++) {
                condicionesString[i] = condiciones[i].name();
            }
            return ResponseEntity.ok(condicionesString);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear solicitud de curso nuevo
     * POST /api/cursos-intersemestrales/solicitudes-curso-nuevo
     */
    @PostMapping("/solicitudes-curso-nuevo")
    public ResponseEntity<Map<String, Object>> crearSolicitudCursoNuevo(@RequestBody SolicitudCursoNuevoDTOPeticion peticion) {
        try {
            log.debug("Se recibio una solicitud de curso nuevo: nombre={}, codigo={}, curso={}, condicion={}, idUsuario={}", 
                peticion.getNombreCompleto(), peticion.getCodigo(), peticion.getCurso(), 
                peticion.getCondicion(), peticion.getIdUsuario());

            // Mapear el DTO a nuestro modelo de dominio
            SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
            solicitudDominio.setNombre_estudiante(peticion.getNombreCompleto());
            solicitudDominio.setCodigo_estudiante(peticion.getCodigo());
            
            // Mapear la condicion con validacion
            try {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                log.warn("La condicion es invalida: {}", peticion.getCondicion());
                throw new IllegalArgumentException("Condicion invalida: " + peticion.getCondicion() + ". Valores validos: " + java.util.Arrays.toString(CondicionSolicitudVerano.values()));
            }
            
            solicitudDominio.setObservacion("Solicitud de apertura de curso: " + peticion.getCurso());
            
            // Inicializar campos obligatorios de la clase padre
            solicitudDominio.setNombre_solicitud("Solicitud de apertura de curso: " + peticion.getCurso());
            solicitudDominio.setFecha_registro_solicitud(new java.util.Date());
            solicitudDominio.setEsSeleccionado(false);
            
            // Crear un curso temporal con ID = 0 para indicar que es un curso nuevo
            CursoOfertadoVerano cursoTemporal = new CursoOfertadoVerano();
            cursoTemporal.setId_curso(0); // ID = 0 indica curso nuevo
            solicitudDominio.setObjCursoOfertadoVerano(cursoTemporal);
            
            // Crear usuario temporal
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuarioTemporal = 
                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
            usuarioTemporal.setId_usuario(peticion.getIdUsuario());
            solicitudDominio.setObjUsuario(usuarioTemporal);

            log.debug("La solicitud de dominio se creo; se invoca el caso de uso.");

            // Llamar al caso de uso
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);
            
            log.debug("Solicitud guardada con ID: {}", solicitudGuardada != null ? solicitudGuardada.getId_solicitud() : "NULL");

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
            
            log.debug("La respuesta de la solicitud se construyo correctamente.");
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            log.warn("Fallo una validacion: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error de validacion: " + e.getMessage());
            error.put("tipo", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error interno al procesar la solicitud: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            error.put("tipo", "INTERNAL_SERVER_ERROR");
            error.put("clase", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint de prueba simple para verificar conectividad
     * GET /api/cursos-intersemestrales/test-simple
     */
    @GetMapping("/test-simple")
    public ResponseEntity<Map<String, Object>> testSimple() {
        log.debug("Se invoco el endpoint de prueba simple.");
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Endpoint funcionando correctamente");
        respuesta.put("timestamp", new java.util.Date().toString());
        respuesta.put("puerto", "5000");
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint de prueba POST simple para simular la peticion del frontend
     * POST /api/cursos-intersemestrales/test-post-simple
     */
    @PostMapping("/test-post-simple")
    public ResponseEntity<Map<String, Object>> testPostSimple(@RequestBody Map<String, Object> datos) {
        try {
            log.debug("Se recibio una llamada al endpoint POST de prueba. Datos recibidos: {}", datos);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "POST funcionando correctamente");
            respuesta.put("datos_recibidos", datos);
            respuesta.put("timestamp", new java.util.Date().toString());
            respuesta.put("puerto", "5000");
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error en POST de prueba: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en POST de prueba: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint para verificar usuarios disponibles en la base de datos
     * GET /api/cursos-intersemestrales/usuarios-disponibles
     */
    @GetMapping("/usuarios-disponibles")
    public ResponseEntity<Map<String, Object>> verificarUsuariosDisponibles() {
        try {
            log.debug("Verificando usuarios disponibles...");
            
            // Consultar la lista de usuarios directamente en la base de datos
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario> usuarios = 
                objGestionarUsuarioCU.listarUsuarios();
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Usuarios encontrados");
            respuesta.put("total", usuarios.size());
            respuesta.put("usuarios", usuarios);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error al verificar usuarios: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error verificando usuarios: " + e.getMessage());
            error.put("tipo", "USUARIOS_ERROR");
            error.put("clase", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(error); // Devolver como 200 para ver el error
        }
    }

    /**
     * Endpoint de prueba que simula exactamente el endpoint problematico
     * POST /api/cursos-intersemestrales/test-solicitud-exacta
     */
    @PostMapping("/test-solicitud-exacta")
    public ResponseEntity<Map<String, Object>> testSolicitudExacta(@RequestBody SolicitudCursoNuevoDTOPeticion peticion) {
        try {
            log.debug("Se invoco el endpoint de test exacto. Peticion recibida: {}", peticion);
            
            // Simular el procesamiento sin llamar al caso de uso real
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_solicitud", 999);
            respuesta.put("nombreCompleto", peticion.getNombreCompleto());
            respuesta.put("codigo", peticion.getCodigo());
            respuesta.put("curso", peticion.getCurso());
            respuesta.put("condicion", peticion.getCondicion());
            respuesta.put("fecha", new java.util.Date().toString());
            respuesta.put("estado", "Pendiente");
            respuesta.put("mensaje", "Solicitud de prueba procesada correctamente");
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("Error en el endpoint de test exacto: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en test exacto: " + e.getMessage());
            error.put("tipo", "TEST_EXACTO_ERROR");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint de prueba para verificar el funcionamiento del endpoint de solicitudes
     * GET /api/cursos-intersemestrales/test-solicitud-curso-nuevo
     */
    @GetMapping("/test-solicitud-curso-nuevo")
    public ResponseEntity<Map<String, Object>> testSolicitudCursoNuevo() {
        try {
            log.debug("Se esta probando el endpoint de solicitud de curso nuevo.");
            
            // Crear datos de prueba
            SolicitudCursoNuevoDTOPeticion peticionPrueba = new SolicitudCursoNuevoDTOPeticion();
            peticionPrueba.setNombreCompleto("Juan Perez");
            peticionPrueba.setCodigo("104612345660");
            peticionPrueba.setCurso("Programacion Avanzada");
            peticionPrueba.setCondicion("Primera_Vez");
            peticionPrueba.setIdUsuario(1);
            
            log.debug("Datos de prueba generados; se llamara al endpoint principal.");
            
            // Llamar al metodo principal
            ResponseEntity<Map<String, Object>> respuesta = crearSolicitudCursoNuevo(peticionPrueba);
            
            log.debug("Respuesta del endpoint: {}", respuesta.getStatusCode());
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("mensaje", "Prueba completada");
            resultado.put("status", respuesta.getStatusCode().value());
            resultado.put("respuesta", respuesta.getBody());
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error durante la prueba del endpoint: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en prueba: " + e.getMessage());
            error.put("tipo", "TEST_ERROR");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Crear preinscripcion a curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano/preinscripciones
     */
    @PostMapping("/cursos-verano/preinscripciones")
    public ResponseEntity<Map<String, Object>> crearPreinscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        try {
            log.debug("Preinscripcion - Recibiendo preinscripcion: idUsuario={}, idCurso={}, nombreSolicitud={}, condicion={}", 
                peticion.getIdUsuario(), peticion.getIdCurso(), peticion.getNombreSolicitud(), peticion.getCondicion());
            
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
                    log.warn("La condicion recibida es invalida ({}); se usara Primera_Vez.", peticion.getCondicion());
                    solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
                }
            } else {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
            }
            
            log.debug("La solicitud de dominio se construyo correctamente.");
            
            // Crear curso con el ID real
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            curso.setId_curso(peticion.getIdCurso());
            solicitudDominio.setObjCursoOfertadoVerano(curso);
            
            log.debug("Curso asignado con ID: {}", peticion.getIdCurso());
            
            // Crear usuario
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario = 
                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
            usuario.setId_usuario(peticion.getIdUsuario());
            solicitudDominio.setObjUsuario(usuario);

            log.debug("Usuario asignado con ID: {}", peticion.getIdUsuario());
            
            // Verificar si ya existe una preinscripcion para este usuario y curso
            log.debug("Verificando preinscripciones existentes para evitar duplicados...");
            Solicitud preinscripcionExistente = solicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(peticion.getIdUsuario(), peticion.getIdCurso());
            
            if (preinscripcionExistente != null) {
                log.debug("Ya existe una preinscripcion registrada para este usuario y curso.");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Ya tienes una preinscripcion activa para este curso");
                error.put("codigo", "DUPLICATE_PREINSCRIPTION");
                return ResponseEntity.badRequest().body(error);
            }
            
            log.debug("No se encontraron preinscripciones previas; se guardara la nueva solicitud.");

            // Llamar al gateway directamente para guardar en la base de datos
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

            log.debug("La solicitud se guardo con ID: {}", solicitudGuardada != null ? solicitudGuardada.getId_solicitud() : "NULL");

            // Crear respuesta JSON con la estructura esperada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_preinscripcion", solicitudGuardada.getId_solicitud());
            respuesta.put("idUsuario", peticion.getIdUsuario());
            respuesta.put("idCurso", peticion.getIdCurso());
            respuesta.put("nombreSolicitud", peticion.getNombreSolicitud());
            respuesta.put("fecha", solicitudGuardada.getFecha_registro_solicitud());
            respuesta.put("estado", "Pendiente");
            respuesta.put("mensaje", "Preinscripcion creada exitosamente");
            
            log.debug("La respuesta para el cliente se preparo exitosamente.");
            return ResponseEntity.status(201).body(respuesta);
        } catch (Exception e) {
            log.error("Error al crear la preinscripcion: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Crear inscripcion a curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano/inscripciones
     */
    @PostMapping("/cursos-verano/inscripciones")
    public ResponseEntity<Map<String, Object>> crearInscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        try {
            log.debug("Recibiendo inscripcion: idUsuario={}, idCurso={}, nombreSolicitud={}", 
                peticion.getIdUsuario(), peticion.getIdCurso(), peticion.getNombreSolicitud());
            
            // 1. Verificar que existe una preinscripcion valida para este usuario y curso
            log.debug("Buscando preinscripcion aprobada...");
            
            // Buscar preinscripcion aprobada usando consulta especifica
            log.debug("Usando consulta especifica para buscar preinscripcion...");
            
            // Primero intentar buscar directamente por usuario y curso
            Solicitud preinscripcionExistente = solicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(peticion.getIdUsuario(), peticion.getIdCurso());
            log.debug("Preinscripcion encontrada por consulta directa: {}", preinscripcionExistente != null ? "SI" : "NO");
            
            SolicitudCursoVeranoPreinscripcion preinscripcionAprobada = null;
            
            if (preinscripcionExistente != null && preinscripcionExistente instanceof SolicitudCursoVeranoPreinscripcion) {
                preinscripcionAprobada = (SolicitudCursoVeranoPreinscripcion) preinscripcionExistente;
                log.debug("Preinscripcion encontrada: ID={}", preinscripcionAprobada.getId_solicitud());
                
                // Verificar si esta aprobada
                String estadoActual = "Sin estado";
                boolean estaAprobada = false;
                
                if (preinscripcionAprobada.getEstadosSolicitud() != null && !preinscripcionAprobada.getEstadosSolicitud().isEmpty()) {
                    estadoActual = preinscripcionAprobada.getEstadosSolicitud()
                        .get(preinscripcionAprobada.getEstadosSolicitud().size() - 1).getEstado_actual();
                    estaAprobada = "Aprobada".equals(estadoActual) || "Aprobado".equals(estadoActual);
                }
                
                log.debug("Estado actual: '{}', Esta aprobada: {}", estadoActual, estaAprobada);
                
                if (!estaAprobada) {
                    preinscripcionAprobada = null;
                    log.debug("Preinscripcion encontrada pero NO esta aprobada");
                }
            } else {
                log.debug("No se encontro preinscripcion para usuario {} y curso {}", peticion.getIdUsuario(), peticion.getIdCurso());
                
                // Fallback: buscar todas las preinscripciones del usuario
                log.debug("Intentando fallback: buscar todas las preinscripciones del usuario...");
                List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarSolicitudesPorUsuario(peticion.getIdUsuario());
                log.debug("Preinscripciones encontradas: {}", preinscripciones.size());
                
                for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripciones) {
                    log.debug("Preinscripcion: ID={}, Usuario={}, Curso={}", 
                        preinscripcion.getId_solicitud(),
                        preinscripcion.getObjUsuario() != null ? preinscripcion.getObjUsuario().getId_usuario() : "NULL",
                        preinscripcion.getObjCursoOfertadoVerano() != null ? preinscripcion.getObjCursoOfertadoVerano().getId_curso() : "NULL");
                    
                    // Verificar si coincide con el usuario y curso
                    boolean coincideUsuario = preinscripcion.getObjUsuario() != null && 
                        preinscripcion.getObjUsuario().getId_usuario().equals(peticion.getIdUsuario());
                    boolean coincideCurso = preinscripcion.getObjCursoOfertadoVerano() != null &&
                        preinscripcion.getObjCursoOfertadoVerano().getId_curso().equals(peticion.getIdCurso());
                    
                    log.debug("Coincide usuario: {}, Coincide curso: {}", coincideUsuario, coincideCurso);
                    
                    if (coincideUsuario && coincideCurso) {
                        // Verificar si esta aprobada
                        String estadoActual = "Sin estado";
                        boolean estaAprobada = false;
                        
                        if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                            estadoActual = preinscripcion.getEstadosSolicitud()
                                .get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                            estaAprobada = "Aprobada".equals(estadoActual) || "Aprobado".equals(estadoActual);
                        }
                        
                        log.debug("Estado actual: '{}', Esta aprobada: {}", estadoActual, estaAprobada);
                        
                        if (estaAprobada) {
                            preinscripcionAprobada = preinscripcion;
                            log.debug("Preinscripcion aprobada encontrada en fallback!");
                            break;
                        }
                    }
                }
            }
            
            if (preinscripcionAprobada == null) {
                log.debug("No se encontro preinscripcion aprobada");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se encontro una preinscripcion aprobada para este usuario y curso");
                error.put("codigo", "PREINSCRIPCION_NO_APROBADA");
                return ResponseEntity.badRequest().body(error);
            }
            
            log.debug("Preinscripcion aprobada encontrada ID: {}", preinscripcionAprobada.getId_solicitud());
            
            // 1.5. VALIDACIONES DE SEGURIDAD
            log.debug("Ejecutando validaciones de seguridad...");
            
            // Validacion 1: Verificar que no tenga una inscripcion activa para este curso
            List<SolicitudCursoVeranoIncripcion> inscripcionesExistentes = solicitudGateway.buscarInscripcionesPorUsuarioYCurso(
                peticion.getIdUsuario(), peticion.getIdCurso()
            );
            
            if (!inscripcionesExistentes.isEmpty()) {
                // Verificar si alguna no esta rechazada
                boolean tieneInscripcionActiva = inscripcionesExistentes.stream()
                    .anyMatch(insc -> {
                        if (insc.getEstadosSolicitud() == null || insc.getEstadosSolicitud().isEmpty()) {
                            return true; // Sin estados = activa
                        }
                        String ultimoEstado = insc.getEstadosSolicitud().get(insc.getEstadosSolicitud().size() - 1).getEstado_actual();
                        return !"Pago_Rechazado".equals(ultimoEstado);
                    });
                
                if (tieneInscripcionActiva) {
                    log.debug("Usuario ya tiene una inscripcion activa para este curso");
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Ya tienes una inscripcion activa para este curso");
                    error.put("codigo", "INSCRIPCION_DUPLICADA");
                    return ResponseEntity.badRequest().body(error);
                }
            }
            
            // Validacion 2: Verificar cupos disponibles (opcional - requiere obtener el curso)
            try {
                Integer inscripcionesAceptadas = solicitudGateway.contarInscripcionesAceptadasPorCurso(peticion.getIdCurso());
                log.debug("Inscripciones aceptadas en el curso: {}", inscripcionesAceptadas);
                
                // Nota: Aqui podrias agregar validacion de cupos si tienes acceso al cupo del curso
                // Por ahora solo logueamos la informacion
            } catch (Exception e) {
                log.warn("No se pudo verificar cupos: {}", e.getMessage());
                // No fallar la operacion por esto
            }
            
            log.debug("Validaciones de seguridad pasadas");
            
            // 2. Crear la inscripcion usando el modelo de dominio
            log.debug("Creando inscripcion...");
            
            SolicitudCursoVeranoIncripcion nuevaInscripcion = new SolicitudCursoVeranoIncripcion();
            nuevaInscripcion.setNombre_solicitud(peticion.getNombreSolicitud());
            nuevaInscripcion.setFecha_registro_solicitud(new java.util.Date());
            nuevaInscripcion.setObservacion("Inscripcion creada desde preinscripcion aprobada");
            nuevaInscripcion.setObjUsuario(preinscripcionAprobada.getObjUsuario());
            nuevaInscripcion.setObjCursoOfertadoVerano(preinscripcionAprobada.getObjCursoOfertadoVerano());
            
            // Campos obligatorios que faltaban
            nuevaInscripcion.setNombre_estudiante("Estudiante"); // Valor por defecto
            nuevaInscripcion.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez); // Valor por defecto
            
            // 3. Guardar la inscripcion en la base de datos
            log.debug("Guardando en base de datos...");
            SolicitudCursoVeranoIncripcion inscripcionGuardada = solicitudGateway.crearSolicitudCursoVeranoInscripcion(nuevaInscripcion);
            
            if (inscripcionGuardada == null || inscripcionGuardada.getId_solicitud() == null) {
                log.debug("Error al guardar inscripcion en BD");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error al guardar la inscripcion en la base de datos");
                return ResponseEntity.internalServerError().body(error);
            }
            
            log.debug("Inscripcion guardada exitosamente ID: {}", inscripcionGuardada.getId_solicitud());
            
            // 4. Asociar documentos sin solicitud a la inscripcion recien creada y moverlos a carpeta organizada
            log.debug("Asociando documentos a la inscripcion...");
            try {
                List<Documento> documentosSinSolicitud = objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
                log.debug("Documentos sin solicitud encontrados: {}", documentosSinSolicitud.size());
                
                for (Documento doc : documentosSinSolicitud) {
                    log.debug("Asociando documento: {}", doc.getNombre());
                    
                    // Mover archivo a carpeta organizada si está en la raíz
                    String rutaActual = doc.getRuta_documento() != null ? doc.getRuta_documento() : doc.getNombre();
                    if (rutaActual != null && !rutaActual.contains("/")) {
                        // El archivo está en la raíz, moverlo a carpeta organizada
                        String nuevaRuta = objGestionarArchivos.moverArchivoAOrganizado(
                            rutaActual, 
                            doc.getNombre(), 
                            "curso-verano", 
                            inscripcionGuardada.getId_solicitud()
                        );
                        if (nuevaRuta != null) {
                            doc.setRuta_documento(nuevaRuta);
                            log.debug("Archivo movido a: {}", nuevaRuta);
                        }
                    }
                    
                    doc.setObjSolicitud(inscripcionGuardada);
                    objGestionarDocumentosGateway.actualizarDocumento(doc);
                    log.debug("Documento asociado exitosamente");
                }
            } catch (Exception e) {
                log.error("Error asociando documentos: {}", e.getMessage(), e);
                // No fallar la operacion por esto
            }
            
            // 5. Asociar estudiante al curso en la tabla de relacion
            log.debug("Asociando estudiante al curso...");
            try {
                int resultado = cursoRepository.insertarCursoEstudiante(
                    peticion.getIdCurso(),
                    peticion.getIdUsuario()
                );
                
                if (resultado == 1) {
                    log.debug("Estudiante asociado exitosamente al curso");
                } else {
                    log.debug("El estudiante ya estaba asociado al curso");
                }
            } catch (Exception e) {
                log.error("Error asociando estudiante al curso: {}", e.getMessage(), e);
                // No fallar la operacion por esto
            }
            
            // 5. Preparar respuesta exitosa
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Inscripcion creada exitosamente");
            respuesta.put("id_solicitud", inscripcionGuardada.getId_solicitud());
            respuesta.put("id_usuario", peticion.getIdUsuario());
            respuesta.put("id_curso", peticion.getIdCurso());
            respuesta.put("nombre_solicitud", peticion.getNombreSolicitud());
            respuesta.put("fecha_inscripcion", inscripcionGuardada.getFecha_registro_solicitud());
            respuesta.put("estado", "Inscrito");
            
            log.debug("Inscripcion completada exitosamente");
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error al crear inscripcion: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Crear preinscripcion a curso de verano (alias para compatibilidad con pruebas)
     * POST /api/cursos-intersemestrales/solicitudes/preinscripcion
     */
    @PostMapping("/solicitudes/preinscripcion")
    public ResponseEntity<Map<String, Object>> crearPreinscripcionAlias(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        // Delegar al metodo principal
        return crearPreinscripcion(peticion);
    }

    /**
     * Obtener inscripciones con informacion completa
     * GET /api/cursos-intersemestrales/inscripciones
     */
    @GetMapping("/inscripciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerInscripciones() {
        try {
            // Obtener inscripciones reales de la base de datos
            List<Map<String, Object>> inscripciones = new ArrayList<>();
            log.debug("INFO Obteniendo inscripciones reales de la base de datos");
            
            // Obtener todas las inscripciones usando el servicio
            List<Map<String, Object>> inscripcionesReales = inscripcionService.findAll();
            log.debug("Inscripciones encontradas en BD: {}", inscripcionesReales.size());
            
            // Procesar inscripciones reales de la base de datos
            for (Map<String, Object> inscripcionReal : inscripcionesReales) {
                Map<String, Object> inscripcionFormateada = new HashMap<>();
                
                // Mapear datos de la inscripcion real
                inscripcionFormateada.put("id", inscripcionReal.get("id"));
                inscripcionFormateada.put("fecha", inscripcionReal.get("fecha_creacion"));
                inscripcionFormateada.put("estado", inscripcionReal.get("codicion_solicitud"));
                inscripcionFormateada.put("estudianteId", inscripcionReal.get("idfkUsuario"));
                inscripcionFormateada.put("cursoId", inscripcionReal.get("idfkCurso"));
                
                // Crear informacion del estudiante (simulada por ahora)
                Map<String, Object> estudiante = new HashMap<>();
                estudiante.put("id_usuario", inscripcionReal.get("idfkUsuario"));
                estudiante.put("nombre", inscripcionReal.get("nombre_estudiante"));
                estudiante.put("apellido", "Apellido"); // Por ahora hardcodeado
                estudiante.put("email", "estudiante@unicauca.edu.co"); // Por ahora hardcodeado
                estudiante.put("codigo_estudiante", "EST001"); // Por ahora hardcodeado
                inscripcionFormateada.put("estudiante", estudiante);
                
                // Informacion del archivo de pago (por ahora vacia)
                Map<String, Object> archivoPago = new HashMap<>();
                archivoPago.put("id_documento", null);
                archivoPago.put("nombre", "Sin comprobante");
                archivoPago.put("url", null);
                archivoPago.put("fecha", null);
                inscripcionFormateada.put("archivoPago", archivoPago);
                
                inscripciones.add(inscripcionFormateada);
            }
            
            log.debug("Inscripciones reales procesadas: {} inscripciones", inscripciones.size());
            
            return ResponseEntity.ok(inscripciones);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Obtener inscripciones reales de la base de datos filtradas por usuario
     * GET /api/cursos-intersemestrales/inscripciones-reales/{id_usuario}
     */
    @GetMapping("/inscripciones-reales/{id_usuario}")
    public ResponseEntity<List<Map<String, Object>>> obtenerInscripcionesReales(@PathVariable Integer id_usuario) {
        try {
            log.debug("Obteniendo inscripciones para el usuario con ID: {}", id_usuario);
            
            // Consultar las inscripciones guardadas en la base de datos
            List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarInscripcionesPorUsuario(id_usuario);
            
            List<Map<String, Object>> inscripciones = solicitudesEntity.stream().map(solicitud -> {
                Map<String, Object> inscripcion = new HashMap<>();
                inscripcion.put("id", solicitud.getId_solicitud());
                inscripcion.put("nombre_solicitud", solicitud.getNombre_solicitud());
                
                // Obtener fecha de registro
                inscripcion.put("fecha_solicitud", solicitud.getFecha_registro_solicitud());
                
                // Obtener estado mas reciente
                String estadoActual = "Sin Estado";
                if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                    estadoActual = solicitud.getEstadosSolicitud()
                        .get(solicitud.getEstadosSolicitud().size() - 1)
                        .getEstado_actual();
                }
                inscripcion.put("estado", estadoActual);
                
                // Obtener observaciones si es solicitud de curso de verano
                if (solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                    SolicitudCursoVeranoInscripcionEntity cursoInscripcion = (SolicitudCursoVeranoInscripcionEntity) solicitud;
                    inscripcion.put("observaciones", cursoInscripcion.getObservacion());
                } else {
                    inscripcion.put("observaciones", "");
                }
                
                // Obtener informacion del curso si existe
                if (solicitud.getObjCursoOfertadoVerano() != null) {
                    CursoOfertadoVeranoEntity curso = solicitud.getObjCursoOfertadoVerano();
                    inscripcion.put("curso_id", curso.getId_curso());
                    inscripcion.put("salon", curso.getSalon());
                    inscripcion.put("grupo", curso.getGrupo() != null ? curso.getGrupo().toString() : "");
                    
                    // Obtener informacion de la materia
                    if (curso.getObjMateria() != null) {
                        inscripcion.put("materia", curso.getObjMateria().getNombre());
                    }
                    
                    // Obtener informacion del docente
                    if (curso.getObjDocente() != null) {
                        inscripcion.put("docente", curso.getObjDocente().getNombre_docente());
                    }
                } else {
                    inscripcion.put("curso_id", null);
                    inscripcion.put("salon", "");
                    inscripcion.put("grupo", "");
                    inscripcion.put("materia", "");
                    inscripcion.put("docente", "");
                }
                
                return inscripcion;
            }).collect(Collectors.toList());
            
            log.debug("Inscripciones filtradas para usuario {}: {} encontradas", id_usuario, inscripciones.size());
            
            return ResponseEntity.ok(inscripciones);
        } catch (Exception e) {
            log.error("Error obteniendo inscripciones: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // ==================== METODOS AUXILIARES PARA VALIDACION DE ESTADOS ====================
    
    /**
     * Obtener el estado actual de un curso
     */
    private String obtenerEstadoActual(CursoOfertadoVeranoEntity curso) {
        if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
            return "Sin_Estado";
        }
        return curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
    }
    
    /**
     * Validar si una transicion de estado es valida
     */
    private Map<String, Object> validarTransicionEstado(String estadoActual, String nuevoEstado, CursoOfertadoVeranoEntity curso) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("valido", false);
        
        // Si es el mismo estado, es valido (no hay cambio)
        if (estadoActual.equals(nuevoEstado)) {
            resultado.put("valido", true);
            return resultado;
        }
        
        // Validar transiciones permitidas
        switch (estadoActual) {
            case "Sin_Estado":
                // Desde sin estado, solo puede ir a Borrador
                if ("Borrador".equals(nuevoEstado)) {
                    resultado.put("valido", true);
                } else {
                    resultado.put("error", "Transicion invalida");
                    resultado.put("message", "Desde 'Sin Estado' solo se puede cambiar a 'Borrador'");
                }
                break;
                
            case "Borrador":
                // Desde Borrador puede ir a Abierto o mantenerse en Borrador
                if ("Abierto".equals(nuevoEstado)) {
                    // Validar que el curso este completo
                    if (validarCompletitudCurso(curso)) {
                        resultado.put("valido", true);
                    } else {
                        resultado.put("error", "Curso incompleto");
                        resultado.put("message", "El curso debe tener materia, docente y cupo estimado para pasar a 'Abierto'");
                    }
                } else {
                    resultado.put("error", "Transicion invalida");
                    resultado.put("message", "Desde 'Borrador' solo se puede cambiar a 'Abierto'");
                }
                break;
                
            case "Abierto":
                // Desde Abierto puede ir a Publicado
                if ("Publicado".equals(nuevoEstado)) {
                    resultado.put("valido", true);
                } else if ("Borrador".equals(nuevoEstado)) {
                    // Permitir retroceder a Borrador para edicion
                    resultado.put("valido", true);
                } else {
                    resultado.put("error", "Transicion invalida");
                    resultado.put("message", "Desde 'Abierto' solo se puede cambiar a 'Publicado' o retroceder a 'Borrador'");
                }
                break;
                
            case "Publicado":
                // Desde Publicado puede ir a Preinscripcion
                if ("Preinscripcion".equals(nuevoEstado)) {
                    // Validar que haya solicitudes minimas para el curso
                    if (validarSolicitudesMinimas(curso.getId_curso())) {
                        resultado.put("valido", true);
                    } else {
                        resultado.put("error", "Solicitudes insuficientes");
                        resultado.put("message", "Debe haber al menos " + curso.getCupo_estimado() + " solicitudes para abrir preinscripciones");
                    }
                } else if ("Abierto".equals(nuevoEstado)) {
                    // Permitir retroceder a Abierto
                    resultado.put("valido", true);
                } else {
                    resultado.put("error", "Transicion invalida");
                    resultado.put("message", "Desde 'Publicado' solo se puede cambiar a 'Preinscripcion' o retroceder a 'Abierto'");
                }
                break;
                
            case "Preinscripcion":
                // Desde Preinscripcion puede ir a Inscripcion o Cerrado
                if ("Inscripcion".equals(nuevoEstado)) {
                    // Validar que haya preinscripciones aprobadas suficientes
                    if (validarPreinscripcionesAprobadas(curso.getId_curso())) {
                        resultado.put("valido", true);
                    } else {
                        resultado.put("error", "Preinscripciones insuficientes");
                        resultado.put("message", "Debe haber preinscripciones aprobadas suficientes para abrir inscripciones");
                    }
                } else if ("Cerrado".equals(nuevoEstado)) {
                    resultado.put("valido", true);
                } else if ("Publicado".equals(nuevoEstado)) {
                    // Permitir retroceder a Publicado
                    resultado.put("valido", true);
                } else {
                    resultado.put("error", "Transicion invalida");
                    resultado.put("message", "Desde 'Preinscripcion' solo se puede cambiar a 'Inscripcion', 'Cerrado' o retroceder a 'Publicado'");
                }
                break;
                
            case "Inscripcion":
                // Desde Inscripcion puede ir a Cerrado
                if ("Cerrado".equals(nuevoEstado)) {
                    resultado.put("valido", true);
                } else if ("Preinscripcion".equals(nuevoEstado)) {
                    // Permitir retroceder a Preinscripcion
                    resultado.put("valido", true);
                } else {
                    resultado.put("error", "Transicion invalida");
                    resultado.put("message", "Desde 'Inscripcion' solo se puede cambiar a 'Cerrado' o retroceder a 'Preinscripcion'");
                }
                break;
                
            case "Cerrado":
                // Desde Cerrado no se puede cambiar a ningun otro estado (solo consulta)
                resultado.put("error", "Estado final");
                resultado.put("message", "El curso esta cerrado y no se puede cambiar su estado");
                break;
                
            default:
                resultado.put("error", "Estado desconocido");
                resultado.put("message", "Estado actual '" + estadoActual + "' no reconocido");
                break;
        }
        
        return resultado;
    }
    
    /**
     * Validar que un curso este completo (tiene todos los campos obligatorios)
     */
    private boolean validarCompletitudCurso(CursoOfertadoVeranoEntity curso) {
        return curso.getObjMateria() != null && 
               curso.getObjDocente() != null && 
               curso.getCupo_estimado() != null && 
               curso.getCupo_estimado() > 0 &&
               curso.getSalon() != null && 
               !curso.getSalon().trim().isEmpty();
    }
    
    /**
     * Validar que haya solicitudes minimas para abrir preinscripciones
     */
    private boolean validarSolicitudesMinimas(Integer idCurso) {
        try {
            // Por ahora, permitir siempre (se puede implementar logica especifica)
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validar que haya preinscripciones aprobadas suficientes para abrir inscripciones
     */
    private boolean validarPreinscripcionesAprobadas(Integer idCurso) {
        try {
            // Por ahora, permitir siempre (se puede implementar logica especifica)
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validar permisos de operacion segun el estado del curso y el rol del usuario
     */
    private Map<String, Object> validarPermisosPorEstado(String estadoCurso, String rolUsuario, String operacion) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("permitido", false);
        
        // Definir matriz de permisos por estado y rol
        Map<String, List<String>> permisosPorEstado = new HashMap<>();
        
        // Estado: Borrador
        permisosPorEstado.put("Borrador", List.of(
            "FUNCIONARIO:ver,editar_completo,eliminar,cambiar_estado",
            "COORDINADOR:ver,editar_completo,eliminar,cambiar_estado",
            "ESTUDIANTE:ninguno"
        ));
        
        // Estado: Abierto
        permisosPorEstado.put("Abierto", List.of(
            "FUNCIONARIO:ver,editar_parcial,cambiar_estado",
            "COORDINADOR:ver,editar_parcial,cambiar_estado",
            "ESTUDIANTE:ninguno"
        ));
        
        // Estado: Publicado
        permisosPorEstado.put("Publicado", List.of(
            "FUNCIONARIO:ver,gestionar_solicitudes,cambiar_estado",
            "COORDINADOR:ver,gestionar_solicitudes,cambiar_estado",
            "ESTUDIANTE:ver,solicitar_curso_nuevo"
        ));
        
        // Estado: Preinscripcion
        permisosPorEstado.put("Preinscripcion", List.of(
            "FUNCIONARIO:ver,gestionar_preinscripciones,cambiar_estado",
            "COORDINADOR:ver,gestionar_preinscripciones,cambiar_estado",
            "ESTUDIANTE:ver,preinscribirse"
        ));
        
        // Estado: Inscripcion
        permisosPorEstado.put("Inscripcion", List.of(
            "FUNCIONARIO:ver,gestionar_inscripciones,cambiar_estado",
            "COORDINADOR:ver,gestionar_inscripciones,cambiar_estado",
            "ESTUDIANTE:ver,inscribirse"
        ));
        
        // Estado: Cerrado
        permisosPorEstado.put("Cerrado", List.of(
            "FUNCIONARIO:ver,consultar",
            "COORDINADOR:ver,consultar",
            "ESTUDIANTE:ver,consultar"
        ));
        
        // Obtener permisos para el estado actual
        List<String> permisosEstado = permisosPorEstado.get(estadoCurso);
        if (permisosEstado == null) {
            resultado.put("error", "Estado desconocido");
            resultado.put("message", "No se pueden determinar permisos para el estado: " + estadoCurso);
            return resultado;
        }
        
        // Buscar permisos para el rol especifico
        String permisosRol = permisosEstado.stream()
            .filter(permiso -> permiso.startsWith(rolUsuario + ":"))
            .findFirst()
            .orElse(null);
        
        if (permisosRol == null) {
            resultado.put("error", "Rol no valido");
            resultado.put("message", "No se encontraron permisos para el rol: " + rolUsuario);
            return resultado;
        }
        
        // Extraer las operaciones permitidas
        String operacionesPermitidas = permisosRol.split(":")[1];
        
        if ("ninguno".equals(operacionesPermitidas)) {
            resultado.put("error", "Sin permisos");
            resultado.put("message", "Este rol no tiene permisos para realizar operaciones en el estado: " + estadoCurso);
            return resultado;
        }
        
        // Verificar si la operacion especifica esta permitida
        if (operacionesPermitidas.contains(operacion) || operacionesPermitidas.contains("todas")) {
            resultado.put("permitido", true);
            resultado.put("message", "Operacion permitida");
        } else {
            resultado.put("error", "Operacion no permitida");
            resultado.put("message", "El rol " + rolUsuario + " no puede realizar la operacion '" + operacion + "' en el estado " + estadoCurso);
        }
        
        return resultado;
    }
    
    /**
     * Endpoint para obtener informacion de permisos por estado
     * GET /api/cursos-intersemestrales/permisos-estado/{estado}/{rol}
     */
    @GetMapping("/permisos-estado/{estado}/{rol}")
    public ResponseEntity<Map<String, Object>> obtenerPermisosPorEstado(
            @PathVariable String estado,
            @PathVariable String rol) {
        try {
            Map<String, Object> permisos = validarPermisosPorEstado(estado, rol, "consultar_permisos");
            return ResponseEntity.ok(permisos);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno");
            error.put("message", "Error obteniendo permisos: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint temporal para debug - verificar datos en la base de datos
     * GET /api/cursos-intersemestrales/debug-solicitudes/{idUsuario}
     */
    @GetMapping("/debug-solicitudes/{idUsuario}")
    public ResponseEntity<Map<String, Object>> debugSolicitudes(@PathVariable Integer idUsuario) {
        try {
            log.debug("Buscando solicitudes para el usuario con ID {}", idUsuario);
            
            // Buscar todas las solicitudes del usuario
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("usuarioId", idUsuario);
            debug.put("preinscripcionesEncontradas", preinscripciones.size());
            debug.put("preinscripciones", preinscripciones);
            
            log.debug("Se encontraron {} preinscripciones asociadas", preinscripciones.size());
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en debug: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint de debug especifico para seguimiento
     * GET /api/cursos-intersemestrales/debug-seguimiento/{idUsuario}
     */
    @GetMapping("/debug-seguimiento/{idUsuario}")
    public ResponseEntity<Map<String, Object>> debugSeguimiento(@PathVariable Integer idUsuario) {
        try {
            log.debug("Iniciando seguimiento de solicitudes para el usuario {}", idUsuario);
            
            List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("totalSolicitudes", preinscripcionesReales.size());
            
            List<Map<String, Object>> solicitudesDebug = new ArrayList<>();
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", preinscripcion.getId_solicitud());
                info.put("observacion", preinscripcion.getObservacion());
                info.put("nombre_solicitud", preinscripcion.getNombre_solicitud());
                info.put("clase", preinscripcion.getClass().getSimpleName());
                
                // Probar deteccion de tipo
                boolean esSolicitudCursoNuevo = false;
                if (preinscripcion.getObservacion() != null) {
                    String obs = preinscripcion.getObservacion().toLowerCase();
                    if (obs.contains("solicitud de apertura") || obs.contains("curso:")) {
                        esSolicitudCursoNuevo = true;
                    }
                }
                if (!esSolicitudCursoNuevo && preinscripcion.getNombre_solicitud() != null) {
                    String nombre = preinscripcion.getNombre_solicitud().toLowerCase();
                    if (nombre.contains("solicitud de apertura") || nombre.contains("curso:")) {
                        esSolicitudCursoNuevo = true;
                    }
                }
                
                info.put("esSolicitudCursoNuevo", esSolicitudCursoNuevo);
                info.put("tipoDetectado", esSolicitudCursoNuevo ? "Solicitud de Curso Nuevo" : "Preinscripcion");
                
                solicitudesDebug.add(info);
            }
            
            debug.put("solicitudes", solicitudesDebug);
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en debug: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener todas las solicitudes de cursos intersemestrales para funcionarios
     * GET /api/cursos-intersemestrales/solicitudes
     */
    @GetMapping("/solicitudes")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodasLasSolicitudes() {
        try {
            log.info("Obteniendo todas las solicitudes de cursos intersemestrales para funcionarios");
            
            // Buscar todas las solicitudes usando el repositorio directamente
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            
            List<Map<String, Object>> solicitudesFormateadas = new ArrayList<>();
            
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                // Solo procesar solicitudes de cursos intersemestrales
                if (solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity || 
                    solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                    
                    Map<String, Object> solicitudInfo = new HashMap<>();
                    
                    // Informacion basica
                    solicitudInfo.put("id", solicitud.getId_solicitud());
                    solicitudInfo.put("fecha", solicitud.getFecha_registro_solicitud());
                    solicitudInfo.put("tipo", solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity ? 
                        "Preinscripcion" : "Inscripcion");
                    
                    // Motivo de la solicitud
                    String motivoSolicitud = "No especificado";
                    
                    if (solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        SolicitudCursoVeranoPreinscripcionEntity preinscripcion = (SolicitudCursoVeranoPreinscripcionEntity) solicitud;
                        if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().trim().isEmpty()) {
                            motivoSolicitud = preinscripcion.getObservacion();
                        } else if (preinscripcion.getNombre_solicitud() != null && !preinscripcion.getNombre_solicitud().trim().isEmpty()) {
                            motivoSolicitud = preinscripcion.getNombre_solicitud();
                        }
                    } else if (solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                        SolicitudCursoVeranoInscripcionEntity inscripcion = (SolicitudCursoVeranoInscripcionEntity) solicitud;
                        if (inscripcion.getObservacion() != null && !inscripcion.getObservacion().trim().isEmpty()) {
                            motivoSolicitud = inscripcion.getObservacion();
                        } else if (inscripcion.getNombre_solicitud() != null && !inscripcion.getNombre_solicitud().trim().isEmpty()) {
                            motivoSolicitud = inscripcion.getNombre_solicitud();
                        }
                    }
                    
                    solicitudInfo.put("motivoSolicitud", motivoSolicitud);
                    
                    // Informacion del usuario
                    if (solicitud.getObjUsuario() != null) {
                        solicitudInfo.put("nombreCompleto", solicitud.getObjUsuario().getNombre_completo());
                        solicitudInfo.put("codigo", solicitud.getObjUsuario().getCodigo());
                    } else {
                        solicitudInfo.put("nombreCompleto", "Usuario no disponible");
                        solicitudInfo.put("codigo", "N/A");
                    }
                    
                    // Informacion del curso
                    String nombreCurso = "Curso no disponible";
                    String condicion = "N/A";
                    
                    if (solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        SolicitudCursoVeranoPreinscripcionEntity preinscripcion = (SolicitudCursoVeranoPreinscripcionEntity) solicitud;
                        
                        if (preinscripcion.getObjCursoOfertadoVerano() != null && 
                            preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                        } else if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().isEmpty()) {
                            // Para solicitudes de curso nuevo
                            String observacion = preinscripcion.getObservacion();
                            if (observacion.contains(": ")) {
                                nombreCurso = observacion.split(": ")[1].trim();
                            } else {
                                nombreCurso = observacion;
                            }
                        }
                        
                        // Estado de la preinscripcion
                        String estadoPreinscripcion = "Enviada";
                        if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                            estadoPreinscripcion = preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                        }
                        solicitudInfo.put("estado", estadoPreinscripcion);
                        
                        // Condicion academica del estudiante (Primera Vez, Repitencia, Habilitacion, etc.)
                        String condicionAcademica = "PRIMERA_VEZ";
                        if (preinscripcion.getCodicion_solicitud() != null) {
                            condicionAcademica = preinscripcion.getCodicion_solicitud().toString();
                        }
                        solicitudInfo.put("condicion", condicionAcademica);
                        
                    } else if (solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                        SolicitudCursoVeranoInscripcionEntity inscripcion = (SolicitudCursoVeranoInscripcionEntity) solicitud;
                        
                        if (inscripcion.getObjCursoOfertadoVerano() != null && 
                            inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                        }
                        
                        // Condicion de la inscripcion
                        if (inscripcion.getCodicion_solicitud() != null) {
                            condicion = inscripcion.getCodicion_solicitud().toString();
                        }
                        
                        // Estado de la inscripcion
                        String estadoInscripcion = "Enviada";
                        if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                            estadoInscripcion = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                        }
                        solicitudInfo.put("estado", estadoInscripcion);
                        solicitudInfo.put("condicion", condicion);
                    }
                    
                    solicitudInfo.put("curso", nombreCurso);
                    
                    solicitudesFormateadas.add(solicitudInfo);
                }
            }
            
            log.debug("Procesadas {} solicitudes de cursos intersemestrales", solicitudesFormateadas.size());
            
            return ResponseEntity.ok(solicitudesFormateadas);
            
        } catch (Exception e) {
            log.error("Error obteniendo solicitudes: {}", e.getMessage(), e);
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exportar todas las solicitudes de cursos intersemestrales a Excel
     * GET /api/cursos-intersemestrales/solicitudes/export/excel
     */
    @GetMapping("/solicitudes/export/excel")
    public ResponseEntity<byte[]> exportarSolicitudesExcel() {
        try {
            log.info("Exportando solicitudes de cursos intersemestrales a Excel");
            
            // Obtener todas las solicitudes usando la misma logica del endpoint anterior
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            
            List<Map<String, Object>> solicitudesFormateadas = new ArrayList<>();
            
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                // Solo procesar solicitudes de cursos intersemestrales
                if (solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity || 
                    solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                    
                    Map<String, Object> solicitudInfo = new HashMap<>();
                    
                    // Informacion basica
                    solicitudInfo.put("id", solicitud.getId_solicitud());
                    solicitudInfo.put("fecha", solicitud.getFecha_registro_solicitud());
                    solicitudInfo.put("tipo", solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity ? 
                        "Preinscripcion" : "Inscripcion");
                    
                    // Motivo de la solicitud
                    String motivoSolicitud = "No especificado";
                    
                    if (solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        SolicitudCursoVeranoPreinscripcionEntity preinscripcion = (SolicitudCursoVeranoPreinscripcionEntity) solicitud;
                        if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().trim().isEmpty()) {
                            motivoSolicitud = preinscripcion.getObservacion();
                        } else if (preinscripcion.getNombre_solicitud() != null && !preinscripcion.getNombre_solicitud().trim().isEmpty()) {
                            motivoSolicitud = preinscripcion.getNombre_solicitud();
                        }
                    } else if (solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                        SolicitudCursoVeranoInscripcionEntity inscripcion = (SolicitudCursoVeranoInscripcionEntity) solicitud;
                        if (inscripcion.getObservacion() != null && !inscripcion.getObservacion().trim().isEmpty()) {
                            motivoSolicitud = inscripcion.getObservacion();
                        } else if (inscripcion.getNombre_solicitud() != null && !inscripcion.getNombre_solicitud().trim().isEmpty()) {
                            motivoSolicitud = inscripcion.getNombre_solicitud();
                        }
                    }
                    
                    solicitudInfo.put("motivoSolicitud", motivoSolicitud);
                    
                    // Informacion del usuario
                    if (solicitud.getObjUsuario() != null) {
                        solicitudInfo.put("nombreCompleto", solicitud.getObjUsuario().getNombre_completo());
                        solicitudInfo.put("codigo", solicitud.getObjUsuario().getCodigo());
                    } else {
                        solicitudInfo.put("nombreCompleto", "Usuario no disponible");
                        solicitudInfo.put("codigo", "N/A");
                    }
                    
                    // Informacion del curso
                    String nombreCurso = "Curso no disponible";
                    String condicion = "N/A";
                    
                    if (solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        SolicitudCursoVeranoPreinscripcionEntity preinscripcion = (SolicitudCursoVeranoPreinscripcionEntity) solicitud;
                        
                        if (preinscripcion.getObjCursoOfertadoVerano() != null && 
                            preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                        } else if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().isEmpty()) {
                            // Para solicitudes de curso nuevo
                            String observacion = preinscripcion.getObservacion();
                            if (observacion.contains(": ")) {
                                nombreCurso = observacion.split(": ")[1].trim();
                            } else {
                                nombreCurso = observacion;
                            }
                        }
                        
                        // Estado de la preinscripcion
                        String estadoPreinscripcion = "Enviada";
                        if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                            estadoPreinscripcion = preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                        }
                        solicitudInfo.put("estado", estadoPreinscripcion);
                        
                        // Condicion academica del estudiante (Primera Vez, Repitencia, Habilitacion, etc.)
                        String condicionAcademica = "PRIMERA_VEZ";
                        if (preinscripcion.getCodicion_solicitud() != null) {
                            condicionAcademica = preinscripcion.getCodicion_solicitud().toString();
                        }
                        solicitudInfo.put("condicion", condicionAcademica);
                        
                    } else if (solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                        SolicitudCursoVeranoInscripcionEntity inscripcion = (SolicitudCursoVeranoInscripcionEntity) solicitud;
                        
                        if (inscripcion.getObjCursoOfertadoVerano() != null && 
                            inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                        }
                        
                        // Condicion de la inscripcion
                        if (inscripcion.getCodicion_solicitud() != null) {
                            condicion = inscripcion.getCodicion_solicitud().toString();
                        }
                        
                        // Estado de la inscripcion
                        String estadoInscripcion = "Enviada";
                        if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                            estadoInscripcion = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                        }
                        solicitudInfo.put("estado", estadoInscripcion);
                        solicitudInfo.put("condicion", condicion);
                    }
                    
                    solicitudInfo.put("curso", nombreCurso);
                    
                    solicitudesFormateadas.add(solicitudInfo);
                }
            }
            
            // Generar Excel
            byte[] excelBytes = generarExcelSolicitudes(solicitudesFormateadas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String nombreArchivo = "solicitudes_cursos_intersemestrales_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            
            log.debug("Excel generado exitosamente con {} solicitudes", solicitudesFormateadas.size());
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error exportando solicitudes a Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Genera un archivo Excel con las solicitudes de cursos intersemestrales
     */
    private byte[] generarExcelSolicitudes(List<Map<String, Object>> solicitudes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Crear hoja de solicitudes
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Solicitudes Cursos Intersemestrales");
            
            // Estilos
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            int rowNum = 0;
            
            // Titulo
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SOLICITUDES DE CURSOS INTERSEMESTRALES DE VERANO");
            titleCell.setCellStyle(titleStyle);
            
            rowNum++; // Espacio
            
            // Fecha de generacion
            org.apache.poi.ss.usermodel.Row fechaRow = sheet.createRow(rowNum++);
            fechaRow.createCell(0).setCellValue("Fecha de generacion: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            
            rowNum++; // Espacio
            
            // Encabezados
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Nombre Completo", "Codigo", "Curso", "Condicion", "Motivo de la Solicitud", "Estado", "Fecha", "Tipo"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Map<String, Object> solicitud : solicitudes) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                int colNum = 0;
                row.createCell(colNum++).setCellValue(solicitud.get("nombreCompleto") != null ? solicitud.get("nombreCompleto").toString() : "");
                row.createCell(colNum++).setCellValue(solicitud.get("codigo") != null ? solicitud.get("codigo").toString() : "");
                row.createCell(colNum++).setCellValue(solicitud.get("curso") != null ? solicitud.get("curso").toString() : "");
                
                // Condicion - formatear para que sea mas legible
                String condicion = solicitud.get("condicion") != null ? solicitud.get("condicion").toString() : "";
                if (condicion.equals("PRIMERA_VEZ")) {
                    condicion = "PRIMERA VEZ";
                }
                row.createCell(colNum++).setCellValue(condicion);
                
                row.createCell(colNum++).setCellValue(solicitud.get("motivoSolicitud") != null ? solicitud.get("motivoSolicitud").toString() : "");
                row.createCell(colNum++).setCellValue(solicitud.get("estado") != null ? solicitud.get("estado").toString() : "");
                
                // Fecha formateada
                if (solicitud.get("fecha") != null) {
                    Date fecha = (Date) solicitud.get("fecha");
                    row.createCell(colNum++).setCellValue(dateFormat.format(fecha));
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                row.createCell(colNum++).setCellValue(solicitud.get("tipo") != null ? solicitud.get("tipo").toString() : "");
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Aumentar un poco mas el ancho para mejor legibilidad
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generando Excel: {}", e.getMessage(), e);
            log.error("Error: {}", e.getMessage(), e);
            
            // Generar Excel de error
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                org.apache.poi.xssf.usermodel.XSSFWorkbook errorWorkbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.ss.usermodel.Sheet errorSheet = errorWorkbook.createSheet("Error");
                
                org.apache.poi.ss.usermodel.Row errorRow = errorSheet.createRow(0);
                errorRow.createCell(0).setCellValue("Error al generar el reporte: " + e.getMessage());
                
                errorWorkbook.write(errorBaos);
                errorWorkbook.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                log.error("Error generando Excel de error: {}", ex.getMessage(), ex);
                return new byte[0];
            }
        }
    }

    /**
     * Obtener estadisticas del dashboard de cursos intersemestrales
     * GET /api/cursos-intersemestrales/dashboard/estadisticas
     */
    @GetMapping("/dashboard/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDashboard() {
        try {
            log.info("Obteniendo estadisticas del dashboard");
            
            Map<String, Object> estadisticas = new HashMap<>();
            
            // 1. Contar cursos activos (estados: Publicado, Preinscripcion, Inscripcion)
            List<CursoOfertadoVerano> todosLosCursos = cursoCU.listarTodos();
            int cursosActivos = 0;
            int cursosGestionados = 0;
            
            for (CursoOfertadoVerano curso : todosLosCursos) {
                if (curso.getEstadosCursoOfertados() != null && !curso.getEstadosCursoOfertados().isEmpty()) {
                    String estadoActual = curso.getEstadosCursoOfertados()
                        .get(curso.getEstadosCursoOfertados().size() - 1)
                        .getEstado_actual();
                    
                    // Cursos activos: Publicado, Preinscripcion, Inscripcion
                    if ("Publicado".equals(estadoActual) || 
                        "Preinscripcion".equals(estadoActual) || 
                        "Inscripcion".equals(estadoActual)) {
                        cursosActivos++;
                    }
                    
                    // Cursos gestionados: cualquier curso que tenga al menos un estado
                    cursosGestionados++;
                }
            }
            
            // 2. Contar total de preinscripciones
            int totalPreinscripciones = 0;
            try {
                List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
                for (SolicitudEntity solicitud : todasLasSolicitudes) {
                    if (solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        totalPreinscripciones++;
                    }
                }
            } catch (Exception e) {
                log.error("Error contando preinscripciones: {}", e.getMessage(), e);
            }
            
            // 3. Contar total de inscripciones
            int totalInscripciones = 0;
            try {
                List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
                for (SolicitudEntity solicitud : todasLasSolicitudes) {
                    if (solicitud instanceof SolicitudCursoVeranoInscripcionEntity) {
                        totalInscripciones++;
                    }
                }
            } catch (Exception e) {
                log.error("Error contando inscripciones: {}", e.getMessage(), e);
            }
            
            // 4. Calcular progreso de gestion
            int totalCursos = todosLosCursos.size();
            
            // Validar valores para evitar datos inconsistentes
            int cursosGestionadosValido = Math.max(0, cursosGestionados); // No permitir negativos
            int totalCursosValido = Math.max(0, totalCursos); // No permitir negativos
            
            // Calcular porcentaje asegurando que esté entre 0 y 100
            double porcentajeProgreso = 0;
            if (totalCursosValido > 0) {
                porcentajeProgreso = (cursosGestionadosValido * 100.0 / totalCursosValido);
                porcentajeProgreso = Math.max(0, Math.min(100, porcentajeProgreso)); // Asegurar entre 0 y 100
            }
            
            // Construir respuesta con valores validados
            estadisticas.put("cursosActivos", Math.max(0, cursosActivos));
            estadisticas.put("totalPreinscripciones", Math.max(0, totalPreinscripciones));
            estadisticas.put("totalInscripciones", Math.max(0, totalInscripciones));
            estadisticas.put("cursosGestionados", cursosGestionadosValido);
            estadisticas.put("totalCursos", totalCursosValido);
            estadisticas.put("porcentajeProgreso", Math.round(porcentajeProgreso));
            
            log.info("Estadisticas del dashboard generadas exitosamente");
            log.debug("  - Cursos Activos: {}, Preinscripciones: {}, Inscripciones: {}, Progreso: {} de {} ({}%)", 
                Math.max(0, cursosActivos), Math.max(0, totalPreinscripciones), Math.max(0, totalInscripciones),
                cursosGestionadosValido, totalCursosValido, Math.round(porcentajeProgreso));
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadisticas del dashboard: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint temporal para debug - verificar TODAS las preinscripciones
     * GET /api/cursos-intersemestrales/debug-todas-solicitudes
     */
    @GetMapping("/debug-todas-solicitudes")
    public ResponseEntity<Map<String, Object>> debugTodasSolicitudes() {
        try {
            log.debug("Consultando todas las solicitudes en la base de datos.");
            
            // Buscar todas las solicitudes usando el repositorio directamente
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("totalSolicitudes", todasLasSolicitudes.size());
            
            List<Map<String, Object>> solicitudesInfo = new ArrayList<>();
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", solicitud.getId_solicitud());
                info.put("tipo", solicitud.getClass().getSimpleName());
                info.put("usuarioId", solicitud.getObjUsuario() != null ? solicitud.getObjUsuario().getId_usuario() : "null");
                info.put("fecha", solicitud.getFecha_registro_solicitud());
                solicitudesInfo.add(info);
            }
            
            debug.put("solicitudes", solicitudesInfo);
            
            log.debug("Se encontraron {} solicitudes en total", todasLasSolicitudes.size());
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en debug: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener seguimiento completo de actividades del usuario (preinscripciones + inscripciones)
     * GET /api/cursos-intersemestrales/seguimiento/{idUsuario}
     */
    @GetMapping("/seguimiento/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerSeguimientoActividades(@PathVariable Integer idUsuario) {
        try {
            log.debug("Obteniendo seguimiento de actividades para usuario ID: {}", idUsuario);
            
            // Obtener preinscripciones reales de la base de datos
            List<Map<String, Object>> preinscripciones = new ArrayList<>();
            
            try {
                // Obtener preinscripciones reales de la base de datos
                List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
                
                for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
                    Map<String, Object> preinscripcionMap = new HashMap<>();
                    preinscripcionMap.put("id", preinscripcion.getId_solicitud());
                    preinscripcionMap.put("fecha", preinscripcion.getFecha_registro_solicitud());
                    preinscripcionMap.put("estado", preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                        ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual() : "Enviado");
                    
                    // Determinar el tipo correcto de solicitud
                    String tipoSolicitud = "Preinscripcion";
                    
                    // Deteccion para solicitudes de curso nuevo
                    boolean esSolicitudCursoNuevo = false;
                    
                    if (preinscripcion.getObservacion() != null) {
                        String obs = preinscripcion.getObservacion().toLowerCase();
                        if (obs.contains("solicitud de apertura") || obs.contains("curso:")) {
                            esSolicitudCursoNuevo = true;
                        }
                    }
                    
                    if (!esSolicitudCursoNuevo && preinscripcion.getNombre_solicitud() != null) {
                        String nombre = preinscripcion.getNombre_solicitud().toLowerCase();
                        if (nombre.contains("solicitud de apertura") || nombre.contains("curso:")) {
                            esSolicitudCursoNuevo = true;
                        }
                    }
                    
                    if (esSolicitudCursoNuevo) {
                        tipoSolicitud = "Solicitud de Curso Nuevo";
                    }
                    preinscripcionMap.put("tipo", tipoSolicitud);
                    
                    // Obtener el nombre del curso correctamente
                    String nombreCurso = "Curso no disponible";
                    if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                        // Para cursos ya existentes
                        nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                    } else {
                        // Para solicitudes de curso nuevo, obtener el nombre del campo observacion
                        if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().isEmpty()) {
                            // Extraer solo el nombre del curso de la observacion
                            String observacion = preinscripcion.getObservacion();
                            if (observacion.contains(": ")) {
                                nombreCurso = observacion.split(": ")[1].trim();
                            } else {
                                nombreCurso = observacion;
                            }
                        } else if (preinscripcion.getNombre_solicitud() != null && 
                                  preinscripcion.getNombre_solicitud().contains(":")) {
                            // Fallback: extraer del nombre de la solicitud
                            nombreCurso = preinscripcion.getNombre_solicitud().split(":")[1].trim();
                        }
                    }
                    preinscripcionMap.put("curso", nombreCurso);
                    preinscripcionMap.put("estudianteId", idUsuario);
                    preinscripcionMap.put("cursoId", preinscripcion.getObjCursoOfertadoVerano() != null 
                        ? preinscripcion.getObjCursoOfertadoVerano().getId_curso() : null);
                    
                    // Agregar informacion del estado del curso y acciones disponibles
                    String estadoCurso = "No disponible";
                    List<String> accionesDisponibles = new ArrayList<>();
                    
                    if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                        // Para cursos existentes, obtener el estado del curso
                        CursoOfertadoVerano curso = preinscripcion.getObjCursoOfertadoVerano();
                        if (curso.getEstadosCursoOfertados() != null && !curso.getEstadosCursoOfertados().isEmpty()) {
                            estadoCurso = curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
                        }
                        
                        // Determinar acciones disponibles basado en el estado de la preinscripcion y del curso
                        String estadoPreinscripcion = preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                            ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual() : "Enviado";
                        
                        if ("Aprobado".equals(estadoPreinscripcion)) {
                            if ("Inscripcion".equals(estadoCurso)) {
                                accionesDisponibles.add("proceder_inscripcion");
                            } else {
                                accionesDisponibles.add("esperando_inscripcion");
                            }
                        } else if ("Enviada".equals(estadoPreinscripcion)) {
                            accionesDisponibles.add("esperando_aprobacion");
                        } else if ("Rechazada".equals(estadoPreinscripcion)) {
                            accionesDisponibles.add("revisar_motivo_rechazo");
                        }
                    } else {
                        // Para solicitudes de curso nuevo
                        String estadoPreinscripcion = preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                            ? preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual() : "Enviado";
                        
                        if ("Enviada".equals(estadoPreinscripcion)) {
                            accionesDisponibles.add("esperando_aprobacion_curso");
                        } else if ("Aprobado".equals(estadoPreinscripcion)) {
                            accionesDisponibles.add("curso_aprobado_esperando_apertura");
                        }
                    }
                    
                    preinscripcionMap.put("estadoCurso", estadoCurso);
                    preinscripcionMap.put("accionesDisponibles", accionesDisponibles);
                    
                    preinscripciones.add(preinscripcionMap);
                }
                
                log.debug("Preinscripciones reales encontradas: {}", preinscripciones.size());
                
            } catch (Exception e) {
                log.warn("Error obteniendo preinscripciones reales: {}", e.getMessage());
                // En caso de error, devolver lista vacia en lugar de datos simulados
                preinscripciones = new ArrayList<>();
            }
            
            // Obtener inscripciones reales del usuario
            List<Map<String, Object>> inscripcionesUsuario = new ArrayList<>();
            
            try {
                // Obtener inscripciones reales de la base de datos
                List<SolicitudCursoVeranoIncripcion> inscripcionesReales = solicitudGateway.buscarInscripcionesPorUsuario(idUsuario);
                
                for (SolicitudCursoVeranoIncripcion inscripcion : inscripcionesReales) {
                    Map<String, Object> inscripcionMap = new HashMap<>();
                    inscripcionMap.put("id", inscripcion.getId_solicitud());
                    inscripcionMap.put("fecha", inscripcion.getFecha_registro_solicitud());
                    
                    // Obtener el estado actual de la inscripcion
                    String estadoInscripcion = "Enviada";
                    if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                        estadoInscripcion = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                    }
                    inscripcionMap.put("estado", estadoInscripcion);
                    
                    inscripcionMap.put("tipoSolicitud", "Inscripcion");
                    inscripcionMap.put("nombreSolicitud", inscripcion.getNombre_solicitud());
                    
                    // Informacion del curso - solo el nombre para evitar [object Object]
                    String nombreCurso = "Curso no disponible";
                    Integer cursoId = null;
                    if (inscripcion.getObjCursoOfertadoVerano() != null && 
                        inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                        nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                        cursoId = inscripcion.getObjCursoOfertadoVerano().getId_curso();
                    }
                    inscripcionMap.put("curso", nombreCurso);
                    inscripcionMap.put("cursoId", cursoId);
                    inscripcionMap.put("estudianteId", idUsuario);
                    
                    // Mapear estadoCurso basado en el estado de la inscripcion
                    String estadoCurso = "PENDIENTE";
                    if ("Pago_Validado".equals(estadoInscripcion)) {
                        estadoCurso = "INSCRITO";
                    } else if ("Inscripcion_Completada".equals(estadoInscripcion)) {
                        estadoCurso = "ACTIVO";
                    } else if ("Curso_Aprobado".equals(estadoInscripcion)) {
                        estadoCurso = "APROBADO";
                    } else if ("Curso_Finalizado".equals(estadoInscripcion)) {
                        estadoCurso = "FINALIZADO";
                    } else if ("Enviada".equals(estadoInscripcion)) {
                        estadoCurso = "PENDIENTE";
                    } else if ("Pago_Rechazado".equals(estadoInscripcion)) {
                        estadoCurso = "RECHAZADO";
                    }
                    inscripcionMap.put("estadoCurso", estadoCurso);
                    
                    // Determinar acciones disponibles basadas en el estado
                    List<String> accionesDisponibles = new ArrayList<>();
                    if ("Enviada".equals(estadoInscripcion)) {
                        accionesDisponibles.add("esperando_validacion_pago");
                    } else if ("Pago_Validado".equals(estadoInscripcion)) {
                        accionesDisponibles.add("inscripcion_aceptada");
                    } else if ("Pago_Rechazado".equals(estadoInscripcion)) {
                        accionesDisponibles.add("pago_rechazado");
                    }
                    
                    inscripcionMap.put("accionesDisponibles", accionesDisponibles);
                    
                    inscripcionesUsuario.add(inscripcionMap);
                }
                
                log.debug("Inscripciones reales encontradas: {}", inscripcionesUsuario.size());
                
            } catch (Exception e) {
                log.warn("Error obteniendo inscripciones reales: {}", e.getMessage());
                // En caso de error, devolver lista vacia
                inscripcionesUsuario = new ArrayList<>();
            }
            
            // Crear respuesta con estadisticas
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("preinscripciones", preinscripciones);
            respuesta.put("inscripciones", inscripcionesUsuario);
            respuesta.put("totalPreinscripciones", preinscripciones.size());
            respuesta.put("totalInscripciones", inscripcionesUsuario.size());
            respuesta.put("totalActividades", preinscripciones.size() + inscripcionesUsuario.size());
            
            log.debug("Seguimiento obtenido: {} preinscripciones reales, {} inscripciones", 
                preinscripciones.size(), inscripcionesUsuario.size());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error en seguimiento: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener solicitudes del usuario especifico
     * GET /api/cursos-intersemestrales/cursos-verano/solicitudes/{idUsuario}
     */
    @GetMapping("/cursos-verano/solicitudes/{idUsuario}")
    public ResponseEntity<List<Map<String, Object>>> obtenerSolicitudesUsuario(
            @Min(value = 1) @PathVariable Integer idUsuario) {
        try {
            List<Map<String, Object>> solicitudes = new ArrayList<>();
            
            // Solicitud 1
            Map<String, Object> solicitud1 = new HashMap<>();
            solicitud1.put("id_solicitud", 1);
            solicitud1.put("curso", "Programacion I");
            solicitud1.put("fecha", "2024-01-15T10:30:00");
            solicitud1.put("estado", "Pendiente");
            solicitud1.put("tipo", "Preinscripcion");
            solicitudes.add(solicitud1);
            
            // Solicitud 2
            Map<String, Object> solicitud2 = new HashMap<>();
            solicitud2.put("id_solicitud", 2);
            solicitud2.put("curso", "Matematicas Basicas");
            solicitud2.put("fecha", "2024-01-16T14:20:00");
            solicitud2.put("estado", "Aprobado");
            solicitud2.put("tipo", "Preinscripcion");
            solicitudes.add(solicitud2);
            
            // Solicitud 3
            Map<String, Object> solicitud3 = new HashMap<>();
            solicitud3.put("id_solicitud", 3);
            solicitud3.put("curso", "Bases de Datos");
            solicitud3.put("fecha", "2024-01-17T09:15:00");
            solicitud3.put("estado", "Rechazado");
            solicitud3.put("tipo", "Inscripcion");
            solicitudes.add(solicitud3);
            
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Cancelar inscripcion
     * DELETE /api/cursos-intersemestrales/inscripciones/{id}
     */
    @DeleteMapping("/inscripciones/{id}")
    public ResponseEntity<Map<String, Object>> cancelarInscripcion(@PathVariable Integer id) {
        try {
            log.debug("Cancelando inscripcion con ID: {}", id);
            
            // Se valida y elimina la inscripcion registrada en la base de datos
            // Validar que la inscripcion existe
            if (!solicitudRepository.existsById(id)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Inscripcion no encontrada");
                error.put("message", "No existe una inscripcion con el ID: " + id);
                error.put("status", 404);
                error.put("timestamp", java.time.LocalDateTime.now().toString());
                return ResponseEntity.status(404).body(error);
            }
            
            // Obtener la solicitud para verificar su estado
            SolicitudEntity solicitud = solicitudRepository.findById(id).orElse(null);
            if (solicitud != null) {
                // Verificar que no este en estado "Aprobado" (no se puede cancelar una inscripcion aprobada)
                if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                    String estadoActual = solicitud.getEstadosSolicitud()
                        .get(solicitud.getEstadosSolicitud().size() - 1)
                        .getEstado_actual();
                    
                    if ("Aprobado".equalsIgnoreCase(estadoActual)) {
            Map<String, Object> error = new HashMap<>();
                        error.put("error", "No se puede cancelar");
                        error.put("message", "No se puede cancelar una inscripcion que ya ha sido aprobada");
                        error.put("status", 400);
            error.put("timestamp", java.time.LocalDateTime.now().toString());
                        return ResponseEntity.status(400).body(error);
                    }
                }
            }
            
            // Eliminar la inscripcion
            solicitudRepository.deleteById(id);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Inscripcion cancelada exitosamente");
            respuesta.put("id_inscripcion", id);
            respuesta.put("status", 200);
            respuesta.put("timestamp", java.time.LocalDateTime.now().toString());
            
            log.debug("Inscripcion {} cancelada exitosamente", id);
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error cancelando inscripcion: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ha ocurrido un error inesperado: " + e.getMessage());
            error.put("status", 500);
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== ENDPOINTS FALTANTES PARA EL FRONTEND ====================

    /**
     * Obtener todos los cursos de verano
     * GET /api/cursos-intersemestrales/cursos-verano
     */
    @GetMapping("/cursos-verano-alt")
    public ResponseEntity<List<Map<String, Object>>> getTodosLosCursos() {
        try {
            List<Map<String, Object>> cursos = new ArrayList<>();
            
            // Curso 1: Matematicas Basicas
            Map<String, Object> curso1 = new HashMap<>();
            curso1.put("id_curso", 1);
            curso1.put("nombre_curso", "Matematicas Basicas");
            curso1.put("codigo_curso", "MAT-101");
            curso1.put("descripcion", "Curso de matematicas basicas para verano");
            curso1.put("fecha_inicio", "2024-06-01T08:00:00Z");
            curso1.put("fecha_fin", "2024-07-15T17:00:00Z");
            curso1.put("cupo_maximo", 30);
            curso1.put("cupo_disponible", 25);
            curso1.put("cupo_estimado", 30);
            curso1.put("espacio_asignado", "Aula 201");
            curso1.put("estado", "Abierto");
            
            // Objeto materia
            Map<String, Object> materia1 = new HashMap<>();
            materia1.put("id_materia", 1);
            materia1.put("nombre_materia", "Matematicas");
            materia1.put("codigo_materia", "MAT");
            materia1.put("creditos", 3);
            curso1.put("objMateria", materia1);
            
            // Objeto docente
            Map<String, Object> docente1 = new HashMap<>();
            docente1.put("id_usuario", 2);
            docente1.put("nombre", "Juan");
            docente1.put("apellido", "Perez");
            docente1.put("email", "juan.perez@unicauca.edu.co");
            docente1.put("telefono", "3001234567");
            
            // Objeto rol
            Map<String, Object> rol1 = new HashMap<>();
            rol1.put("id_rol", 2);
            rol1.put("nombre", "Docente"); // SUCCESS CORREGIDO: nombre → nombre
            docente1.put("objRol", rol1);
            
            curso1.put("objDocente", docente1);
            cursos.add(curso1);
            
            // Curso 2: Programacion I
            Map<String, Object> curso2 = new HashMap<>();
            curso2.put("id_curso", 2);
            curso2.put("nombre_curso", "Programacion I");
            curso2.put("codigo_curso", "PROG-201");
            curso2.put("descripcion", "Fundamentos de programacion");
            curso2.put("fecha_inicio", "2024-06-15T08:00:00Z");
            curso2.put("fecha_fin", "2024-07-30T17:00:00Z");
            curso2.put("cupo_maximo", 25);
            curso2.put("cupo_disponible", 20);
            curso2.put("cupo_estimado", 25);
            curso2.put("espacio_asignado", "Lab 301");
            curso2.put("estado", "Preinscripcion");
            
            Map<String, Object> materia2 = new HashMap<>();
            materia2.put("id_materia", 2);
            materia2.put("nombre_materia", "Programacion");
            materia2.put("codigo_materia", "PROG");
            materia2.put("creditos", 4);
            curso2.put("objMateria", materia2);
            
            Map<String, Object> docente2 = new HashMap<>();
            docente2.put("id_usuario", 3);
            docente2.put("nombre", "Maria");
            docente2.put("apellido", "Garcia");
            docente2.put("email", "maria.garcia@unicauca.edu.co");
            docente2.put("telefono", "3007654321");
            
            Map<String, Object> rol2 = new HashMap<>();
            rol2.put("id_rol", 2);
            rol2.put("nombre", "Docente"); // SUCCESS CORREGIDO: nombre → nombre
            docente2.put("objRol", rol2);
            
            curso2.put("objDocente", docente2);
            cursos.add(curso2);
            
            return ResponseEntity.ok(cursos);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Crear nuevo curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano
     */
    @PostMapping("/cursos-verano")
    public ResponseEntity<Map<String, Object>> crearCurso(@RequestBody CreateCursoDTO dto) {
        try {
            log.debug("DEBUG: ===== RECIBIENDO PETICIÓN PARA CREAR CURSO =====");
            log.debug("DEBUG: DTO completo recibido: idMateria={} (tipo={}), idDocente={} (tipo={}), cupo={}, fechaInicio={}, fechaFin={}, periodo={}, espacio={}, estado={}", 
                dto.getId_materia(), dto.getId_materia() != null ? dto.getId_materia().getClass().getSimpleName() : "null",
                dto.getId_docente(), dto.getId_docente() != null ? dto.getId_docente().getClass().getSimpleName() : "null",
                dto.getCupo_estimado(), dto.getFecha_inicio(), dto.getFecha_fin(), 
                dto.getPeriodoAcademico(), dto.getEspacio_asignado(), 
                dto.getEstado() != null ? dto.getEstado() : "Abierto (por defecto)");
            
            // Validar que el ID del docente sea válido
            if (dto.getId_docente() != null && dto.getId_docente() <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "ID de docente inválido");
                error.put("message", "El ID del docente debe ser mayor a 0. ID recibido: " + dto.getId_docente());
                log.warn("DEBUG: ERROR - ID de docente inválido: {}", dto.getId_docente());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validar que el ID de la materia sea válido
            if (dto.getId_materia() != null && dto.getId_materia() <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "ID de materia inválido");
                error.put("message", "El ID de la materia debe ser mayor a 0. ID recibido: " + dto.getId_materia());
                log.warn("DEBUG: ERROR - ID de materia inválido: {}", dto.getId_materia());
                return ResponseEntity.badRequest().body(error);
            }
            
            if (dto.getId_materia() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Materia requerida");
                error.put("message", "Debe seleccionar una materia");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (dto.getId_docente() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Docente requerido");
                error.put("message", "Debe seleccionar un docente");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (dto.getCupo_estimado() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Cupo estimado requerido");
                error.put("message", "Debe proporcionar un cupo estimado");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validar fechas
            if (dto.getFecha_inicio() == null || dto.getFecha_inicio().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Fecha de inicio requerida");
                error.put("message", "Debe proporcionar una fecha de inicio");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (dto.getFecha_fin() == null || dto.getFecha_fin().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Fecha de fin requerida");
                error.put("message", "Debe proporcionar una fecha de fin");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validar período académico
            if (dto.getPeriodoAcademico() == null || dto.getPeriodoAcademico().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Período académico requerido");
                error.put("message", "Debe seleccionar un período académico");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validar que el período académico sea válido usando el enum (consistencia con ECAES)
            String periodoAcademicoTrimmed = dto.getPeriodoAcademico().trim();
            if (!PeriodoAcademicoEnum.esValido(periodoAcademicoTrimmed)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Período académico inválido");
                error.put("message", "El período académico '" + periodoAcademicoTrimmed + "' no es válido. Debe ser un período válido como '2025-1' o '2025-2'");
                error.put("periodosValidos", PeriodoAcademicoEnum.getPeriodosRecientes());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validar que la fecha de fin sea posterior a la fecha de inicio
            try {
                java.util.Date fechaInicio;
                java.util.Date fechaFin;
                
                try {
                    fechaInicio = parsearFecha(dto.getFecha_inicio());
                } catch (java.text.ParseException e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Formato de fecha de inicio inválido");
                    error.put("message", "La fecha de inicio debe estar en formato ISO 8601 (ej: '2025-01-15T08:00:00Z' o '2025-01-15')");
                    return ResponseEntity.badRequest().body(error);
                }
                
                try {
                    fechaFin = parsearFecha(dto.getFecha_fin());
                } catch (java.text.ParseException e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Formato de fecha de fin inválido");
                    error.put("message", "La fecha de fin debe estar en formato ISO 8601 (ej: '2025-01-15T08:00:00Z' o '2025-01-15')");
                    return ResponseEntity.badRequest().body(error);
                }
                
                if (fechaFin.before(fechaInicio) || fechaFin.equals(fechaInicio)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Fechas inválidas");
                    error.put("message", "La fecha de fin debe ser posterior a la fecha de inicio");
                    return ResponseEntity.badRequest().body(error);
                }
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error validando fechas");
                error.put("message", "No se pudieron validar las fechas: " + e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Crear el curso usando el caso de uso existente
            try {
                log.debug("DEBUG: Creando curso usando caso de uso");
                
                // Crear objeto de dominio del curso
                CursoOfertadoVerano cursoDominio = new CursoOfertadoVerano();
                cursoDominio.setCupo_estimado(dto.getCupo_estimado());
                // Espacio asignado: usar el proporcionado o "Aula 101" por defecto
                cursoDominio.setSalon(dto.getEspacio_asignado() != null && !dto.getEspacio_asignado().trim().isEmpty() 
                    ? dto.getEspacio_asignado().trim() 
                    : "Aula 101");
                // Grupo: siempre se asigna como "A" por defecto
                cursoDominio.setGrupo(co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano.A);
                
                // Parsear fecha de inicio (ANTES de usarla)
                java.util.Date fechaInicioDate;
                try {
                    fechaInicioDate = parsearFecha(dto.getFecha_inicio());
                } catch (java.text.ParseException e) {
                    // Si falla el parsing, usar la fecha actual
                    log.debug("DEBUG: WARNING - No se pudo parsear la fecha de inicio, usando fecha actual");
                    fechaInicioDate = new java.util.Date();
                }
                
                // Parsear fecha de fin (ANTES de usarla)
                java.util.Date fechaFinDate;
                try {
                    fechaFinDate = parsearFecha(dto.getFecha_fin());
                } catch (java.text.ParseException e) {
                    // Si falla el parsing, calcular como fecha_inicio + 6 semanas
                    log.debug("DEBUG: WARNING - No se pudo parsear la fecha de fin, calculando como fecha_inicio + 6 semanas");
                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                    cal.setTime(fechaInicioDate);
                    cal.add(java.util.Calendar.WEEK_OF_YEAR, 6);
                    fechaFinDate = cal.getTime();
                }
                
                // Guardar fechas y período directamente en el curso (no en estados)
                cursoDominio.setFecha_inicio(fechaInicioDate);
                cursoDominio.setFecha_fin(fechaFinDate);
                cursoDominio.setPeriodo_academico(periodoAcademicoTrimmed);
                
                // Obtener materia real de la base de datos
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materiaDominio = 
                    materiaCU.obtenerMateriaPorId(dto.getId_materia().intValue());
                if (materiaDominio == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Materia no encontrada");
                    error.put("message", "La materia con ID " + dto.getId_materia() + " no existe");
                    return ResponseEntity.badRequest().body(error);
                }
                cursoDominio.setObjMateria(materiaDominio);
                
                // Obtener docente real de la base de datos
                Integer idDocenteParaCurso = dto.getId_docente().intValue();
                log.debug("DEBUG: Asignando docente al curso - ID recibido en DTO: {}, ID convertido a Integer: {}", 
                    dto.getId_docente(), idDocenteParaCurso);
                
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente docenteDominio = 
                    docenteCU.obtenerDocentePorId(idDocenteParaCurso);
                if (docenteDominio == null) {
                    log.warn("DEBUG: ERROR - Docente no encontrado con ID: {}", idDocenteParaCurso);
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Docente no encontrado");
                    error.put("message", "El docente con ID " + dto.getId_docente() + " no existe");
                    error.put("id_docente_recibido", dto.getId_docente());
                    return ResponseEntity.badRequest().body(error);
                }
                
                log.debug("DEBUG: Docente asignado al cursoDominio - ID: {}, Nombre: {}", 
                    docenteDominio.getId_docente(), docenteDominio.getNombre_docente());
                cursoDominio.setObjDocente(docenteDominio);
                
                // Verificar que el docente fue asignado correctamente
                if (cursoDominio.getObjDocente() == null) {
                    log.debug("DEBUG: ERROR - El docente no se asignó correctamente al cursoDominio");
                } else {
                    log.debug("DEBUG: Verificación - CursoDominio tiene docente ID: {}", 
                        cursoDominio.getObjDocente().getId_docente());
                }
                
                // Crear estado inicial del curso
                // Validar estado: debe ser uno de los estados válidos
                String estadoInicial = (dto.getEstado() != null && !dto.getEstado().trim().isEmpty()) 
                    ? dto.getEstado().trim() 
                    : "Abierto";
                
                // Validar que el estado sea válido
                String[] estadosValidos = {"Borrador", "Abierto", "Publicado", "Preinscripcion", "Inscripcion", "Cerrado"};
                boolean estadoValido = false;
                for (String estadoVal : estadosValidos) {
                    if (estadoVal.equals(estadoInicial)) {
                        estadoValido = true;
                        break;
                    }
                }
                if (!estadoValido) {
                    estadoInicial = "Abierto"; // Por defecto si el estado no es válido
                    log.debug("DEBUG: WARNING - Estado inválido proporcionado, usando 'Abierto' por defecto");
                }
                
                // Crear estado inicial (solo con fecha de registro del estado, las fechas del curso van en el curso)
                EstadoCursoOfertado estadoCurso = new EstadoCursoOfertado();
                estadoCurso.setEstado_actual(estadoInicial);
                estadoCurso.setFecha_registro_estado(fechaInicioDate); // Fecha en que se registra este estado (coincide con fecha_inicio del curso al crearlo)
                // fecha_fin y periodo_academico ahora están solo en el curso, no en estados
                List<EstadoCursoOfertado> estados = new ArrayList<>();
                estados.add(estadoCurso);
                cursoDominio.setEstadosCursoOfertados(estados);
                
                log.debug("DEBUG: Estado inicial del curso: {}, Fecha inicio: {}, Fecha fin: {}, Período: {}", 
                    estadoInicial, fechaInicioDate, fechaFinDate, periodoAcademicoTrimmed);
                
                // Usar el caso de uso para crear el curso
                CursoOfertadoVerano cursoCreado = cursoCU.crearCurso(cursoDominio);
                log.debug("DEBUG: Curso creado exitosamente con ID: {}", cursoCreado.getId_curso());
                
                // Verificar que el curso creado tiene el docente correcto
                if (cursoCreado.getObjDocente() != null) {
                    log.debug("DEBUG: Docente del curso creado - ID: {}, Nombre: {}", 
                        cursoCreado.getObjDocente().getId_docente(), cursoCreado.getObjDocente().getNombre_docente());
                } else {
                    log.debug("DEBUG: WARNING - El curso creado no tiene docente asignado");
                }
                
                // Obtener información completa del curso creado desde la BD para la respuesta
                // Usar el mapper para obtener la información estructurada
                CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mappearDeCursoOfertadoARespuesta(cursoCreado);
                
                // Crear respuesta con datos reales del curso creado
                // Usar las fechas y período académico proporcionados por el usuario en lugar de los calculados
                Map<String, Object> nuevoCurso = new HashMap<>();
                nuevoCurso.put("id_curso", cursoCreado.getId_curso());
                nuevoCurso.put("nombre_curso", cursoDTO.getNombre_curso()); // De la materia
                nuevoCurso.put("codigo_curso", cursoDTO.getCodigo_curso()); // De la materia
                nuevoCurso.put("descripcion", cursoDTO.getDescripcion()); // Generada automáticamente
                nuevoCurso.put("fecha_inicio", dto.getFecha_inicio()); // Usar la fecha proporcionada por el usuario
                nuevoCurso.put("fecha_fin", dto.getFecha_fin()); // Usar la fecha proporcionada por el usuario
                nuevoCurso.put("periodo", periodoAcademicoTrimmed); // Usar el período académico validado (enum)
                nuevoCurso.put("cupo_maximo", cursoDTO.getCupo_maximo()); // Igual a cupo_estimado
                nuevoCurso.put("cupo_disponible", cursoDTO.getCupo_disponible()); // Igual a cupo_estimado inicialmente
                nuevoCurso.put("cupo_estimado", cursoDTO.getCupo_estimado());
                nuevoCurso.put("espacio_asignado", cursoDTO.getEspacio_asignado());
                nuevoCurso.put("estado", cursoDTO.getEstado()); // Estado actual
                nuevoCurso.put("objMateria", cursoDTO.getObjMateria());
                nuevoCurso.put("objDocente", cursoDTO.getObjDocente());
                
                nuevoCurso.put("message", "Curso creado exitosamente en la base de datos");
                
                log.debug("DEBUG: Curso creado exitosamente en BD - ID: {}, Nombre: {}, Docente: {}, Período: {}, Fecha Inicio: {}, Fecha Fin: {}", 
                    cursoCreado.getId_curso(), cursoDTO.getNombre_curso(), 
                    cursoDTO.getObjDocente() != null ? cursoDTO.getObjDocente().getNombre_docente() : "null",
                    periodoAcademicoTrimmed, dto.getFecha_inicio(), dto.getFecha_fin());
                return ResponseEntity.ok(nuevoCurso);
                
            } catch (Exception e) {
                log.error("DEBUG: Error guardando en BD: {}", e.getMessage(), e);
                
                // Si falla el guardado, devolver error
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error al crear el curso");
                error.put("message", "No se pudo crear el curso en la base de datos: " + e.getMessage());
                return ResponseEntity.status(500).body(error);
            }
            
        } catch (Exception e) {
            log.error("ERROR: Error creando curso: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "No se pudo crear el curso: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Actualizar curso de verano
     * PUT /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @PutMapping("/cursos-verano/{id}")
    public ResponseEntity<Map<String, Object>> actualizarCurso(
            @PathVariable Long id, 
            @RequestBody UpdateCursoDTO dto) {
        try {
            log.debug("DEBUG: Actualizando curso ID: {}, Datos recibidos: {}", id, dto);
            
            // Validaciones basicas
            if (dto.getCupo_estimado() != null && (dto.getCupo_estimado() < 1 || dto.getCupo_estimado() > 100)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Cupo invalido");
                error.put("message", "El cupo estimado debe estar entre 1 y 100");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (dto.getEspacio_asignado() != null && dto.getEspacio_asignado().trim().length() < 3) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Espacio invalido");
                error.put("message", "El espacio asignado debe tener al menos 3 caracteres");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validar estado
            if (dto.getEstado() != null) {
                String[] estadosValidos = {"Borrador", "Abierto", "Publicado", "Preinscripcion", "Inscripcion", "Cerrado"};
                boolean estadoValido = false;
                for (String estado : estadosValidos) {
                    if (estado.equals(dto.getEstado())) {
                        estadoValido = true;
                        break;
                    }
                }
                if (!estadoValido) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Estado invalido");
                    error.put("message", "El estado debe ser uno de: Borrador, Abierto, Publicado, Preinscripcion, Inscripcion, Cerrado");
                    return ResponseEntity.badRequest().body(error);
                }
            }
            
            // Obtener el curso existente directamente del repositorio
            CursoOfertadoVeranoEntity cursoEntity = cursoRepository.findById(id.intValue()).orElse(null);
            if (cursoEntity == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Curso no encontrado");
                error.put("message", "No se encontro el curso con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            log.debug("DEBUG: Curso encontrado: {}", cursoEntity.getObjMateria() != null ? cursoEntity.getObjMateria().getNombre() : "Sin materia");
            
            // Aplicar cambios al objeto
            boolean cursoModificado = false;
            
            if (dto.getCupo_estimado() != null) {
                cursoEntity.setCupo_estimado(dto.getCupo_estimado());
                cursoModificado = true;
                log.debug("DEBUG: Cupo actualizado a: {}", dto.getCupo_estimado());
            }
            
            if (dto.getEspacio_asignado() != null) {
                cursoEntity.setSalon(dto.getEspacio_asignado());
                cursoModificado = true;
                log.debug("DEBUG: Espacio actualizado a: {}", dto.getEspacio_asignado());
            }
            
            // Crear nuevo estado si se proporciona
            EstadoCursoOfertadoEntity nuevoEstadoEntity = null;
            if (dto.getEstado() != null) {
                // Validar transicion de estado
                String estadoActual = obtenerEstadoActual(cursoEntity);
                String nuevoEstado = dto.getEstado();
                
                // Validar si la transicion es valida
                Map<String, Object> validacionTransicion = validarTransicionEstado(estadoActual, nuevoEstado, cursoEntity);
                if (!(Boolean) validacionTransicion.get("valido")) {
                    return ResponseEntity.badRequest().body(validacionTransicion);
                }
                
                nuevoEstadoEntity = new EstadoCursoOfertadoEntity();
                nuevoEstadoEntity.setEstado_actual(dto.getEstado());
                nuevoEstadoEntity.setFecha_registro_estado(new java.util.Date());
                nuevoEstadoEntity.setObjCursoOfertadoVerano(cursoEntity);
                cursoModificado = true;
                log.debug("DEBUG: Estado actualizado a: {}", dto.getEstado());
            }
            
            if (!cursoModificado) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Sin cambios");
                error.put("message", "No se proporcionaron datos para actualizar");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Guardar cambios en la base de datos
            try {
                log.debug("DEBUG: Guardando cambios en la base de datos");
                CursoOfertadoVeranoEntity cursoActualizado = cursoRepository.save(cursoEntity);
                
                // Si hay nuevo estado, guardarlo tambien
                if (nuevoEstadoEntity != null) {
                    log.debug("DEBUG: Guardando nuevo estado: {}", nuevoEstadoEntity.getEstado_actual());
                    estadoRepository.save(nuevoEstadoEntity);
                    log.debug("DEBUG: Nuevo estado guardado exitosamente en BD");
                }
                
                log.debug("DEBUG: Cambios guardados exitosamente en BD");
                
                // Crear respuesta con los datos actualizados
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("id_curso", cursoActualizado.getId_curso());
                resultado.put("nombre_curso", cursoActualizado.getObjMateria() != null ? cursoActualizado.getObjMateria().getNombre() : "Curso");
                resultado.put("codigo_curso", cursoActualizado.getObjMateria() != null ? cursoActualizado.getObjMateria().getCodigo() : "N/A");
                resultado.put("cupo_estimado", cursoActualizado.getCupo_estimado());
                resultado.put("espacio_asignado", cursoActualizado.getSalon());
                resultado.put("estado", dto.getEstado() != null ? dto.getEstado() : "Actualizado");
                resultado.put("message", "Curso actualizado exitosamente");
                resultado.put("debug_info", "Cambios aplicados y guardados en BD");
                
                return ResponseEntity.ok(resultado);
                
            } catch (Exception e) {
                log.error("DEBUG: Error guardando en BD: {}", e.getMessage(), e);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error guardando en base de datos");
                error.put("message", "No se pudo guardar los cambios: " + e.getMessage());
                return ResponseEntity.status(500).body(error);
            }
            
        } catch (Exception e) {
            log.error("ERROR: Error actualizando curso: {}", e.getMessage(), e);
            log.error("Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "No se pudo actualizar el curso: " + e.getMessage());
            error.put("details", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }


    /**
     * Eliminar curso de verano
     * DELETE /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @DeleteMapping("/cursos-verano/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCurso(@PathVariable Long id) {
        try {
            log.debug("DEBUG: Eliminando curso ID: {}", id);
            
            // Verificar que el curso existe
            CursoOfertadoVerano cursoExistente = cursoCU.obtenerCursoPorId(id.intValue());
            if (cursoExistente == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Curso no encontrado");
                error.put("message", "No se encontro el curso con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            // Verificar si hay estudiantes inscritos
            if (cursoExistente.getEstudiantesInscritos() != null && !cursoExistente.getEstudiantesInscritos().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se puede eliminar el curso");
                error.put("message", "El curso tiene estudiantes inscritos. No se puede eliminar.");
                error.put("estudiantes_inscritos", cursoExistente.getEstudiantesInscritos().size());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Llamar al caso de uso para eliminar
            boolean eliminado = cursoCU.eliminarCurso(id.intValue());
            
            if (eliminado) {
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("message", "Curso eliminado exitosamente");
                resultado.put("id_curso_eliminado", id);
                resultado.put("nombre_curso", cursoExistente.getObjMateria() != null ? cursoExistente.getObjMateria().getNombre() : "Curso");
                
                log.debug("DEBUG: Curso eliminado exitosamente");
                return ResponseEntity.ok(resultado);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error al eliminar el curso");
                error.put("message", "No se pudo eliminar el curso por razones desconocidas");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            log.error("ERROR: Error eliminando curso: {}", e.getMessage(), e);
            log.error("Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "No se pudo eliminar el curso: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Obtener curso de verano por ID (datos reales de la base de datos)
     * GET /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @GetMapping("/cursos-verano/{id}")
    public ResponseEntity<Map<String, Object>> getCursoPorId(@PathVariable Long id) {
        log.debug("DEBUG: Obteniendo informacion del curso ID: {}", id);
        
        // Obtener curso real de la base de datos - lanza EntidadNoExisteException si no existe
        CursoOfertadoVerano cursoReal = cursoCU.obtenerCursoPorId(id.intValue());
        
        // Verificar docente antes del mapeo
        if (cursoReal.getObjDocente() == null) {
            log.warn("ERROR: El curso ID {} NO tiene docente asignado en el dominio", id);
        } else {
            log.debug("DEBUG: Curso ID {} tiene docente en dominio - ID: {}, Nombre: {}, Código: {}", 
                id, cursoReal.getObjDocente().getId_docente(), 
                cursoReal.getObjDocente().getNombre_docente(),
                cursoReal.getObjDocente().getCodigo_docente());
        }
        
        // Usar el mapper existente para obtener datos estructurados
        CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mappearDeCursoOfertadoARespuesta(cursoReal);
        
        // Verificar docente después del mapeo
        if (cursoDTO.getObjDocente() == null) {
            log.warn("ERROR: El DTO del curso ID {} NO tiene docente después del mapeo", id);
        } else {
            log.debug("DEBUG: DTO del curso ID {} tiene docente - ID: {}, Nombre: {}, Código: {}", 
                id, cursoDTO.getObjDocente().getId_docente(),
                cursoDTO.getObjDocente().getNombre_docente(),
                cursoDTO.getObjDocente().getCodigo_docente());
        }
        
        // Mapear a estructura esperada por el frontend
        Map<String, Object> curso = new HashMap<>();
        curso.put("id_curso", cursoDTO.getId_curso());
        curso.put("idCurso", cursoDTO.getId_curso()); // Compatibilidad con pruebas
        curso.put("nombre_curso", cursoDTO.getNombre_curso());
        curso.put("codigo_curso", cursoDTO.getCodigo_curso());
        curso.put("descripcion", cursoDTO.getDescripcion());
        curso.put("fecha_inicio", cursoDTO.getFecha_inicio());
        curso.put("fecha_fin", cursoDTO.getFecha_fin());
        curso.put("cupo_maximo", cursoDTO.getCupo_maximo());
        curso.put("cupo_disponible", cursoDTO.getCupo_disponible());
        curso.put("cupo_estimado", cursoDTO.getCupo_estimado());
        curso.put("espacio_asignado", cursoDTO.getEspacio_asignado());
        curso.put("estado", cursoDTO.getEstado());
        
        // Obtener conteo real de preinscripciones para este curso
        try {
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(id.intValue());
            curso.put("solicitudes", preinscripciones.size());
            log.debug("Preinscripciones encontradas para el curso: {}", preinscripciones.size());
        } catch (Exception e) {
            log.warn("Error obteniendo conteo de preinscripciones: {}", e.getMessage());
            curso.put("solicitudes", 0);
        }
        
        // Usar la informacion del DTO que ya esta mapeada correctamente
        curso.put("objMateria", cursoDTO.getObjMateria());
        curso.put("objDocente", cursoDTO.getObjDocente());
        
        // Log final para verificar que el docente está en la respuesta
        if (curso.get("objDocente") == null) {
            log.warn("objDocente es NULL en la respuesta final para el curso ID {}", id);
        } else {
            log.debug("objDocente incluido en la respuesta para el curso ID {}", id);
        }
        
        log.debug("SUCCESS: Informacion del curso obtenida correctamente");
        
        return ResponseEntity.ok(curso);
    }

    /**
     * Filtrar cursos por periodo academico
     * GET /api/cursos-intersemestrales/cursos-verano/periodo/{periodo}
     */
    @GetMapping("/cursos-verano/periodo/{periodo}")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> filtrarCursosPorPeriodo(@PathVariable String periodo) {
        try {
            // Por ahora, retornar todos los cursos (implementacion futura)
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                    .map(cursoMapper::mappearDeCursoOfertadoARespuestaDisponible)
                    .map(cursoMapper::postMapCurso) // Asignar idCurso
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Filtrar cursos por materia
     * GET /api/cursos-intersemestrales/cursos-verano/materia/{idMateria}
     */
    @GetMapping("/cursos-verano/materia/{idMateria}")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> filtrarCursosPorMateria(@PathVariable Integer idMateria) {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar cursos por materia
            List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                    .filter(curso -> curso.getObjMateria() != null && 
                                   curso.getObjMateria().getId_materia().equals(idMateria))
                    .map(cursoMapper::mappearDeCursoOfertadoARespuestaDisponible)
                    .map(cursoMapper::postMapCurso) // Asignar idCurso
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ENDPOINTS PARA MATERIAS Y DOCENTES ====================

    /**
     * Obtener todas las materias (endpoint actualizado para usar datos reales)
     * GET /api/cursos-intersemestrales/materias
     */
    @GetMapping("/materias")
    public ResponseEntity<List<Map<String, Object>>> getTodasLasMaterias() {
        try {
            // Obtener todas las materias de la base de datos
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materiasReales = 
                materiaCU.listarMaterias();
            
            List<Map<String, Object>> materias = new ArrayList<>();
            
            for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materiasReales) {
                Map<String, Object> materiaMap = new HashMap<>();
                materiaMap.put("id_materia", materia.getId_materia());
                materiaMap.put("nombre_materia", materia.getNombre());
                materiaMap.put("codigo_materia", materia.getCodigo());
                materiaMap.put("creditos", materia.getCreditos());
                materias.add(materiaMap);
            }
            
            return ResponseEntity.ok(materias);
        } catch (Exception e) {
            log.error("Error obteniendo materias: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtener materias para el filtro de solicitudes (formato simplificado)
     * GET /api/cursos-intersemestrales/materias-filtro
     */
    @GetMapping("/materias-filtro")
    public ResponseEntity<List<Map<String, Object>>> obtenerMateriasParaFiltro() {
        try {
            log.info("Obteniendo materias para el filtro de solicitudes");
            
            // Obtener todas las materias de la base de datos
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materiasReales = 
                materiaCU.listarMaterias();
            
            List<Map<String, Object>> materiasFiltro = new ArrayList<>();
            
            // Agregar opcion "Todas las materias"
            Map<String, Object> todasLasMaterias = new HashMap<>();
            todasLasMaterias.put("id", 0);
            todasLasMaterias.put("nombre", "Todas las materias");
            todasLasMaterias.put("codigo", "TODAS");
            materiasFiltro.add(todasLasMaterias);
            
            // Agregar materias reales
            for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materiasReales) {
                Map<String, Object> materiaMap = new HashMap<>();
                materiaMap.put("id", materia.getId_materia());
                materiaMap.put("nombre", materia.getNombre());
                materiaMap.put("codigo", materia.getCodigo());
                materiasFiltro.add(materiaMap);
            }
            
            log.debug("Materias para filtro obtenidas: {}", materiasFiltro.size());
            
            return ResponseEntity.ok(materiasFiltro);
        } catch (Exception e) {
            log.error("Error obteniendo materias para filtro: {}", e.getMessage(), e);
            
            // En caso de error, devolver al menos la opcion "Todas las materias"
            List<Map<String, Object>> fallback = new ArrayList<>();
            Map<String, Object> todasLasMaterias = new HashMap<>();
            todasLasMaterias.put("id", 0);
            todasLasMaterias.put("nombre", "Todas las materias");
            todasLasMaterias.put("codigo", "TODAS");
            fallback.add(todasLasMaterias);
            
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * Obtener todos los docentes
     * GET /api/cursos-intersemestrales/docentes
     */
    @GetMapping("/docentes")
    public ResponseEntity<List<Map<String, Object>>> getTodosLosDocentes() {
        try {
            log.debug("DEBUG: Obteniendo todos los docentes desde la base de datos");
            
            // Obtener docentes reales de la base de datos
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente> docentesDominio = 
                docenteCU.listarDocentes();
            
            if (docentesDominio == null || docentesDominio.isEmpty()) {
                log.debug("DEBUG: No se encontraron docentes en la base de datos");
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            log.debug("Se encontraron {} docentes en la base de datos", docentesDominio.size());
            
            // Mapear docentes de dominio a formato de respuesta para el frontend
            List<Map<String, Object>> docentes = docentesDominio.stream()
                .map(docente -> {
                    Map<String, Object> docenteMap = new HashMap<>();
                    // Campo crítico: id_docente (NO id_usuario)
                    docenteMap.put("id_docente", docente.getId_docente());
                    docenteMap.put("codigo_docente", docente.getCodigo_docente());
                    docenteMap.put("nombre_docente", docente.getNombre_docente());
                    // Mantener compatibilidad con frontend (si lo necesita)
                    docenteMap.put("id_usuario", docente.getId_docente()); // Usar id_docente como id_usuario para compatibilidad
                    docenteMap.put("codigo_usuario", docente.getCodigo_docente());
                    docenteMap.put("nombre_usuario", docente.getNombre_docente());
                    
                    log.debug("Docente mapeado - ID: {}, Nombre: {}, Código: {}", 
                        docente.getId_docente(), docente.getNombre_docente(), docente.getCodigo_docente());
                    
                    return docenteMap;
                })
                .collect(Collectors.toList());

            log.debug("Total de docentes devueltos: {}", docentes.size());
            return ResponseEntity.ok(docentes);
        } catch (Exception e) {
            log.error("Error obteniendo docentes: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== ENDPOINTS PARA GESTION DE PREINSCRIPCIONES ====================

    /**
     * Obtener preinscripciones por curso (datos reales de la base de datos)
     * GET /api/cursos-intersemestrales/preinscripciones/curso/{idCurso}
     */
    @GetMapping("/preinscripciones/curso/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> getPreinscripcionesPorCurso(
            @PathVariable Long idCurso) {
        try {
            log.debug("Obteniendo preinscripciones reales para curso ID: {}", idCurso);
            
            List<Map<String, Object>> preinscripciones = new ArrayList<>();
            
            // Obtener preinscripciones reales de la base de datos
            List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarPreinscripcionesPorCurso(idCurso.intValue());
            
            log.debug("Preinscripciones encontradas: {}", preinscripcionesReales.size());
            
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
                Map<String, Object> preinscripcionMap = new HashMap<>();
                
                // Informacion basica de la preinscripcion
                preinscripcionMap.put("id_preinscripcion", preinscripcion.getId_solicitud());
                preinscripcionMap.put("id_solicitud", preinscripcion.getId_solicitud()); // Para compatibilidad con frontend
                preinscripcionMap.put("fecha_preinscripcion", preinscripcion.getFecha_registro_solicitud());
                
                // Estado de la solicitud
                String estado = "Pendiente"; // Valor por defecto
                if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                    estado = preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                }
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
            
            log.debug("Preinscripciones procesadas: {}", preinscripciones.size());
            
            return ResponseEntity.ok(preinscripciones);
            
        } catch (Exception e) {
            log.error("Error obteniendo preinscripciones por curso: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtener estudiantes inscritos en un curso (estudiantes que ya completaron la inscripcion)
     * GET /api/cursos-intersemestrales/inscripciones/estudiantes-elegibles/{idCurso}
     */
    @GetMapping("/inscripciones/estudiantes-elegibles/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> getEstudiantesElegiblesParaInscripcion(
            @PathVariable Long idCurso) {
        try {
            log.debug("[ESTUDIANTES_INSCRITOS] Obteniendo estudiantes inscritos en curso ID: {}", idCurso);
            
            List<Map<String, Object>> estudiantesInscritos = new ArrayList<>();
            
            // 1. Obtener todas las inscripciones del curso (no preinscripciones)
            log.debug("[ESTUDIANTES_INSCRITOS] Buscando inscripciones para curso ID: {}", idCurso);
            List<SolicitudCursoVeranoIncripcion> inscripciones = solicitudCU.buscarInscripcionesPorCurso(idCurso.intValue());
            log.debug("[ESTUDIANTES_INSCRITOS] Total inscripciones encontradas: {}", inscripciones.size());
            
            // Debug: Mostrar detalles de cada inscripcion encontrada
            for (SolicitudCursoVeranoIncripcion inscripcion : inscripciones) {
                log.debug("[ESTUDIANTES_INSCRITOS] Inscripcion encontrada - ID: {}, Usuario: {}, Curso: {}", 
                    inscripcion.getId_solicitud(),
                    inscripcion.getObjUsuario() != null ? inscripcion.getObjUsuario().getId_usuario() : "NULL",
                    inscripcion.getObjCursoOfertadoVerano() != null ? inscripcion.getObjCursoOfertadoVerano().getId_curso() : "NULL");
            }
            
            // 2. Procesar cada inscripcion encontrada
            log.debug("[ESTUDIANTES_INSCRITOS] Procesando inscripciones encontradas...");
            
            for (SolicitudCursoVeranoIncripcion inscripcion : inscripciones) {
                try {
                    log.debug("[ESTUDIANTES_INSCRITOS] Procesando inscripcion ID: {} para usuario ID: {} en curso ID: {}", 
                        inscripcion.getId_solicitud(), inscripcion.getObjUsuario().getId_usuario(), idCurso);
                    
                    // 2.1. Verificar el estado de la inscripcion - SOLO mostrar las que NO estan aceptadas
                    String estadoActual = "Inscrito"; // Estado por defecto
                    if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                        estadoActual = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                    }
                    
                    // Si la inscripcion ya fue aceptada (Pago_Validado), no la mostrar en la lista
                    if ("Pago_Validado".equals(estadoActual)) {
                        log.debug("[ESTUDIANTES_INSCRITOS] Inscripcion ID {} ya fue aceptada (Pago_Validado), omitiendo de la lista", 
                            inscripcion.getId_solicitud());
                        continue; // Saltar esta inscripcion
                    }
                    
                    log.debug("[ESTUDIANTES_INSCRITOS] Inscripcion ID {} en estado '{}', incluyendo en la lista", 
                        inscripcion.getId_solicitud(), estadoActual);
                    
                    // Crear informacion del estudiante inscrito
                    Map<String, Object> estudianteInscrito = new HashMap<>();
                    
                    // Informacion basica del estudiante
                    estudianteInscrito.put("id_usuario", inscripcion.getObjUsuario().getId_usuario());
                    estudianteInscrito.put("nombre_completo", inscripcion.getObjUsuario().getNombre_completo());
                    estudianteInscrito.put("codigo", inscripcion.getObjUsuario().getCodigo());
                    estudianteInscrito.put("correo", inscripcion.getObjUsuario().getCorreo());
                    
                    // Informacion de la inscripcion
                    estudianteInscrito.put("id_solicitud", inscripcion.getId_solicitud());
                    estudianteInscrito.put("nombre_solicitud", inscripcion.getNombre_solicitud());
                    estudianteInscrito.put("fecha_solicitud", inscripcion.getFecha_registro_solicitud());
                    estudianteInscrito.put("condicion_solicitud", inscripcion.getCodicion_solicitud());
                    
                    // Informacion del curso
                    if (inscripcion.getObjCursoOfertadoVerano() != null) {
                        estudianteInscrito.put("id_curso", inscripcion.getObjCursoOfertadoVerano().getId_curso());
                        estudianteInscrito.put("nombre_curso", inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
                        // El codigo del curso no esta disponible en esta entidad
                    }
                    
                    // Estado de la inscripcion (ya calculado arriba)
                    estudianteInscrito.put("estado_actual", estadoActual);
                    
                    // Informacion adicional
                    estudianteInscrito.put("tipo_solicitud", "Inscripcion");
                    estudianteInscrito.put("motivo_inclusion", "Estudiante inscrito en el curso");
                    estudianteInscrito.put("tiene_inscripcion_formal", true);
                    
                    // Informacion del archivo de pago (asumir que esta validado)
                    Map<String, Object> archivoPago = new HashMap<>();
                    archivoPago.put("id_documento", "validado");
                    archivoPago.put("nombre", "Comprobante de pago validado");
                    archivoPago.put("url", "/uploads/comprobantes/");
                    archivoPago.put("fecha", inscripcion.getFecha_registro_solicitud());
                    estudianteInscrito.put("archivoPago", archivoPago);
                    
                    estudiantesInscritos.add(estudianteInscrito);
                    
                    log.debug("Estudiante inscrito encontrado: {} (ID: {})", 
                        inscripcion.getObjUsuario().getNombre_completo(), 
                        inscripcion.getObjUsuario().getId_usuario());
                } catch (Exception e) {
                    log.warn("[ESTUDIANTES_INSCRITOS] Error procesando inscripcion ID {}: {}", 
                        inscripcion.getId_solicitud(), e.getMessage());
                    log.error("Error: {}", e.getMessage(), e);
                }
            }
            
            log.debug("[ESTUDIANTES_INSCRITOS] Total estudiantes inscritos encontrados: {}", estudiantesInscritos.size());
            
            return ResponseEntity.ok(estudiantesInscritos);
            
        } catch (Exception e) {
            log.error("[ESTUDIANTES_INSCRITOS] Error obteniendo estudiantes inscritos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint temporal para debug - mostrar TODAS las preinscripciones sin filtros
     * GET /api/cursos-intersemestrales/debug/preinscripciones/curso/{idCurso}
     */
    @GetMapping("/debug/preinscripciones/curso/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> debugPreinscripcionesPorCurso(
            @PathVariable Long idCurso) {
        try {
            log.debug("Obteniendo TODAS las preinscripciones para curso ID: {}", idCurso);
            
            List<Map<String, Object>> debugInfo = new ArrayList<>();
            
            // Obtener todas las preinscripciones del curso
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarPreinscripcionesPorCurso(idCurso.intValue());
            log.debug("Total preinscripciones encontradas: {}", preinscripciones.size());
            
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripciones) {
                Map<String, Object> debugMap = new HashMap<>();
                
                debugMap.put("id_preinscripcion", preinscripcion.getId_solicitud());
                debugMap.put("usuario_id", preinscripcion.getObjUsuario() != null ? preinscripcion.getObjUsuario().getId_usuario() : "NULL");
                debugMap.put("usuario_nombre", preinscripcion.getObjUsuario() != null ? preinscripcion.getObjUsuario().getNombre_completo() : "NULL");
                debugMap.put("curso_id", preinscripcion.getObjCursoOfertadoVerano() != null ? preinscripcion.getObjCursoOfertadoVerano().getId_curso() : "NULL");
                
                // Estados
                if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                    List<String> estados = new ArrayList<>();
                    for (int i = 0; i < preinscripcion.getEstadosSolicitud().size(); i++) {
                        String estado = preinscripcion.getEstadosSolicitud().get(i).getEstado_actual();
                        String fecha = preinscripcion.getEstadosSolicitud().get(i).getFecha_registro_estado().toString();
                        estados.add(estado + " (" + fecha + ")");
                    }
                    debugMap.put("estados", estados);
                    debugMap.put("estado_actual", preinscripcion.getEstadosSolicitud().get(preinscripcion.getEstadosSolicitud().size() - 1).getEstado_actual());
                } else {
                    debugMap.put("estados", "SIN ESTADOS");
                    debugMap.put("estado_actual", "SIN ESTADOS");
                }
                
                // Buscar inscripcion correspondiente
                try {
                    SolicitudCursoVeranoIncripcion inscripcion = solicitudGateway.buscarSolicitudInscripcionPorUsuarioYCurso(
                        preinscripcion.getObjUsuario().getId_usuario(), 
                        idCurso.intValue()
                    );
                    
                    if (inscripcion != null) {
                        debugMap.put("tiene_inscripcion", true);
                        debugMap.put("inscripcion_id", inscripcion.getId_solicitud());
                        
                        if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                            String estadoInscripcion = inscripcion.getEstadosSolicitud()
                                .get(inscripcion.getEstadosSolicitud().size() - 1).getEstado_actual();
                            debugMap.put("estado_inscripcion", estadoInscripcion);
                        } else {
                            debugMap.put("estado_inscripcion", "SIN ESTADOS");
                        }
                    } else {
                        debugMap.put("tiene_inscripcion", false);
                        debugMap.put("estado_inscripcion", "NO EXISTE");
                    }
                } catch (Exception e) {
                    debugMap.put("tiene_inscripcion", "ERROR: " + e.getMessage());
                }
                
                debugInfo.add(debugMap);
            }
            
            log.debug("Informacion de debug generada para {} preinscripciones", debugInfo.size());
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            log.error("Error en debug: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Aceptar inscripcion de estudiante (funcionario acepta inscripcion final)
     * PUT /api/cursos-intersemestrales/inscripciones/{idInscripcion}/aceptar
     */
    @PutMapping("/inscripciones/{idInscripcion}/aceptar")
    public ResponseEntity<Map<String, Object>> aceptarInscripcionEstudiante(
            @PathVariable Long idInscripcion,
            @RequestBody Map<String, String> request) {
        try {
            log.debug("Aceptando inscripcion para inscripcion ID: {}, Request body recibido: {}", idInscripcion, request);
            
            String observaciones = request.get("observaciones");
            if (observaciones == null || observaciones.trim().isEmpty()) {
                observaciones = "Inscripcion aceptada por funcionario";
            }
            
            // 1. Buscar la inscripcion directamente por ID
            log.debug("Buscando inscripcion con ID: {}", idInscripcion.intValue());
            SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
            
            log.debug("Resultado busqueda inscripcion: {}", inscripcion != null ? "ENCONTRADA" : "NO ENCONTRADA");
            if (inscripcion == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se encontro la inscripcion con ID: " + idInscripcion);
                return ResponseEntity.badRequest().body(error);
            }
            
            // 2. Validar pago de la inscripcion (marcar como aceptada)
            log.debug("Validando pago para inscripcion ID: {}", inscripcion.getId_solicitud());
            SolicitudCursoVeranoIncripcion inscripcionAceptada = solicitudGateway.validarPago(
                inscripcion.getId_solicitud(), 
                true, 
                observaciones
            );
            
            if (inscripcionAceptada == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se pudo aceptar la inscripcion");
                return ResponseEntity.internalServerError().body(error);
            }
            
            // 3. Insertar en tabla cursosestudiantes (relacion Many-to-Many)
            try {
                log.debug("Insertando en tabla cursosestudiantes - Usuario: {}, Curso: {}", 
                    inscripcion.getObjUsuario().getId_usuario(), 
                    inscripcion.getObjCursoOfertadoVerano().getId_curso());
                
                // Usar el repositorio directamente para asociar usuario-curso
                int resultado = cursoRepository.insertarCursoEstudiante(
                    inscripcion.getObjCursoOfertadoVerano().getId_curso(),
                    inscripcion.getObjUsuario().getId_usuario()
                );
                Boolean asociacionExitosa = (resultado == 1);
                
                if (asociacionExitosa) {
                    log.debug("Estudiante asociado exitosamente al curso en tabla cursosestudiantes");
                } else {
                    log.debug("El estudiante ya estaba asociado al curso o hubo un problema");
                }
            } catch (Exception e) {
                log.error("Error asociando estudiante al curso: {}", e.getMessage(), e);
                // No fallar la operacion por esto, pero logear el error
            }
            
            // 4. Preparar respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Inscripcion aceptada exitosamente");
            respuesta.put("id_inscripcion", inscripcionAceptada.getId_solicitud());
            respuesta.put("estudiante_nombre", inscripcion.getObjUsuario().getNombre_completo());
            respuesta.put("curso_nombre", inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
            respuesta.put("fecha_aceptacion", new java.util.Date());
            respuesta.put("observaciones", observaciones);
            
            log.debug("Inscripcion aceptada exitosamente para: {} en curso ID: {}", 
                inscripcion.getObjUsuario().getNombre_completo(), 
                inscripcion.getObjCursoOfertadoVerano().getId_curso());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error aceptando inscripcion: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Rechazar inscripcion de estudiante (funcionario rechaza inscripcion)
     * PUT /api/cursos-intersemestrales/inscripciones/{idInscripcion}/rechazar
     */
    @PutMapping("/inscripciones/{idInscripcion}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarInscripcionEstudiante(
            @PathVariable Long idInscripcion,
            @RequestBody Map<String, String> request) {
        try {
            log.debug("Rechazando inscripcion para inscripcion ID: {}", idInscripcion);
            
            String motivo = request.get("motivo");
            if (motivo == null || motivo.trim().isEmpty()) {
                motivo = "Inscripcion rechazada por funcionario";
            } else {
                motivo = motivo.trim();
            }
            
            // 1. Buscar la inscripcion directamente por ID
            SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
            
            if (inscripcion == null) {
                log.warn("No se encontro la inscripcion con ID: {}", idInscripcion);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se encontro la inscripcion con ID: " + idInscripcion);
                return ResponseEntity.badRequest().body(error);
            }
            
            log.debug("Inscripcion encontrada: {}", inscripcion.getNombre_solicitud());
            String estadoActual = null;
            if (inscripcion.getEstadosSolicitud() != null && !inscripcion.getEstadosSolicitud().isEmpty()) {
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimo = inscripcion.getEstadosSolicitud().get(inscripcion.getEstadosSolicitud().size() - 1);
                estadoActual = ultimo.getEstado_actual();
            }
            log.debug("Estado actual: {}, Estudiante: {}, Curso: {}", 
                estadoActual, 
                inscripcion.getObjUsuario().getNombre_completo(),
                inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
            
            // 2. Verificar que la inscripcion este en estado valido para rechazar
            if (!"Enviada".equals(estadoActual) && !"Pago_Validado".equals(estadoActual)) {
                log.warn("Estado invalido para rechazar: {}", estadoActual);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "La inscripcion no puede ser rechazada en su estado actual: " + estadoActual);
                return ResponseEntity.badRequest().body(error);
            }
            
            // 3. Marcar como rechazada usando el caso de uso
            log.debug(" Procesando rechazo...");
            SolicitudCursoVeranoIncripcion inscripcionRechazada;
            try {
                inscripcionRechazada = solicitudCU.validarPago(
                    inscripcion.getId_solicitud(), 
                    false, 
                    "Inscripcion rechazada: " + motivo
                );
            } catch (Exception e) {
                log.error("Error en validarPago: {}", e.getMessage(), e);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error al rechazar la inscripcion: " + e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
            
            if (inscripcionRechazada == null) {
                log.debug("Error al actualizar el estado de la inscripcion - resultado es null");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se pudo actualizar el estado de la inscripcion");
                return ResponseEntity.internalServerError().body(error);
            }
            
            log.debug("Estado actualizado exitosamente");
            String nuevoEstado = null;
            if (inscripcionRechazada.getEstadosSolicitud() != null && !inscripcionRechazada.getEstadosSolicitud().isEmpty()) {
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud ultimoNuevo = inscripcionRechazada.getEstadosSolicitud().get(inscripcionRechazada.getEstadosSolicitud().size() - 1);
                nuevoEstado = ultimoNuevo.getEstado_actual();
            }
            log.debug("Nuevo estado: {}", nuevoEstado);
            
            // 4. Preparar respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Inscripcion rechazada exitosamente");
            respuesta.put("id_inscripcion", inscripcionRechazada.getId_solicitud());
            respuesta.put("estudiante_nombre", inscripcion.getObjUsuario().getNombre_completo());
            respuesta.put("curso_nombre", inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre());
            respuesta.put("fecha_rechazo", new java.util.Date());
            respuesta.put("motivo", motivo);
            respuesta.put("nuevo_estado", nuevoEstado);
            
            log.debug("Inscripcion rechazada para: {} en curso ID: {}", 
                inscripcion.getObjUsuario().getNombre_completo(), 
                inscripcion.getObjCursoOfertadoVerano().getId_curso());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error rechazando inscripcion: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Actualizar observaciones de preinscripcion
     * PUT /api/cursos-intersemestrales/preinscripciones/{idPreinscripcion}/observaciones
     */
    @PutMapping("/preinscripciones/{idPreinscripcion}/observaciones")
    public ResponseEntity<Map<String, Object>> actualizarObservacionesPreinscripcion(
            @PathVariable Long idPreinscripcion,
            @RequestBody Map<String, String> request) {
        try {
            String observaciones = request.get("observaciones");
            
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
            SolicitudCursoVeranoPreinscripcion solicitudActualizada = solicitudGateway.actualizarSolicitudCursoVerano(solicitud);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("message", "Observaciones actualizadas exitosamente");
            respuesta.put("success", true);
            
            Map<String, Object> preinscripcion = new HashMap<>();
            preinscripcion.put("id_preinscripcion", solicitudActualizada.getId_solicitud());
            preinscripcion.put("observaciones", solicitudActualizada.getObservacion());
            preinscripcion.put("estado", solicitudActualizada.getEstadosSolicitud() != null && !solicitudActualizada.getEstadosSolicitud().isEmpty() 
                ? solicitudActualizada.getEstadosSolicitud().get(solicitudActualizada.getEstadosSolicitud().size() - 1).getEstado_actual() : "Pendiente");
            
            respuesta.put("preinscripcion", preinscripcion);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== ENDPOINT DE PRUEBA SIMPLE ====================
    
    /**
     * Endpoint de prueba simple
     * GET /api/cursos-intersemestrales/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Backend funcionando correctamente");
        response.put("timestamp", new java.util.Date());
        response.put("port", "5000");
        return ResponseEntity.ok(response);
    }


    // ==================== ENDPOINTS PARA APROBAR Y RECHAZAR PREINSCRIPCIONES ====================

    /**
     * Aprobar preinscripcion (para funcionarios)
     * PUT /api/cursos-intersemestrales/preinscripciones/{idSolicitud}/aprobar
     */
    @PutMapping("/preinscripciones/{idSolicitud}/aprobar")
    public ResponseEntity<Map<String, Object>> aprobarPreinscripcion(
            @PathVariable Integer idSolicitud,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String comentarios = requestBody != null ? requestBody.get("comentarios") : null;
            SolicitudCursoVeranoPreinscripcion solicitudAprobada = solicitudCU.aprobarPreinscripcion(idSolicitud, comentarios);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Preinscripcion aprobada exitosamente");
            respuesta.put("id_solicitud", solicitudAprobada.getId_solicitud());
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al aprobar preinscripcion: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Rechazar preinscripcion (para funcionarios)
     * PUT /api/cursos-intersemestrales/preinscripciones/{idSolicitud}/rechazar
     */
    @PutMapping("/preinscripciones/{idSolicitud}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarPreinscripcion(
            @PathVariable Integer idSolicitud,
            @RequestBody Map<String, String> requestBody) {
        try {
            String motivo = requestBody.get("motivo");
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
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al rechazar preinscripcion: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ==================== ENDPOINTS PARA APROBAR Y RECHAZAR SOLICITUDES ====================

    /**
     * Aprobar solicitud de curso nuevo
     * PUT /api/cursos-intersemestrales/{id}/aprobar
     */
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Map<String, Object>> aprobarSolicitud(@PathVariable Long id) {
        try {
            // Simular aprobacion de solicitud
            Map<String, Object> solicitud = new HashMap<>();
            solicitud.put("id_solicitud", id);
            solicitud.put("nombre_solicitud", "Solicitud de Curso Nuevo");
            solicitud.put("fecha_solicitud", "2024-01-10T10:30:00Z");
            solicitud.put("estado", "Aprobado");
            solicitud.put("observaciones", "Estudiante con buen rendimiento academico");
            solicitud.put("condicion", "Primera_Vez");
            solicitud.put("tipoSolicitud", "PREINSCRIPCION");
            
            // Usuario estudiante
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id_usuario", 4);
            usuario.put("nombre", "Pepa");
            usuario.put("apellido", "Gonzalez");
            usuario.put("email", "pepa.gonzalez@unicauca.edu.co");
            usuario.put("telefono", "3001111111");
            usuario.put("codigo_estudiante", "104612345660");
            
            Map<String, Object> rolEstudiante = new HashMap<>();
            rolEstudiante.put("id_rol", 1);
            rolEstudiante.put("nombre", "Estudiante");
            usuario.put("objRol", rolEstudiante);
            
            solicitud.put("objUsuario", usuario);
            
            // Curso ofertado
            Map<String, Object> curso = new HashMap<>();
            curso.put("id_curso", 1);
            curso.put("nombre_curso", "Programacion I");
            curso.put("codigo_curso", "PROG-201");
            curso.put("descripcion", "Fundamentos de programacion");
            curso.put("fecha_inicio", "2024-01-15T00:00:00Z");
            curso.put("fecha_fin", "2024-03-15T00:00:00Z");
            curso.put("cupo_maximo", 25);
            curso.put("cupo_estimado", 20);
            curso.put("cupo_disponible", 15);
            curso.put("espacio_asignado", "Lab 301");
            curso.put("estado", "Preinscripcion");
            
            // Materia del curso
            Map<String, Object> materia = new HashMap<>();
            materia.put("id_materia", 3);
            materia.put("nombre_materia", "Programacion I");
            materia.put("codigo_materia", "PROG");
            materia.put("creditos", 4);
            curso.put("objMateria", materia);
            
            // Docente del curso
            Map<String, Object> docente = new HashMap<>();
            docente.put("id_usuario", 3);
            docente.put("nombre", "Ana");
            docente.put("apellido", "Martinez");
            docente.put("email", "ana@unicauca.edu.co");
            docente.put("telefono", "3009876543");
            
            Map<String, Object> rolDocente = new HashMap<>();
            rolDocente.put("id_rol", 2);
            rolDocente.put("nombre", "Docente");
            docente.put("objRol", rolDocente);
            
            curso.put("objDocente", docente);
            solicitud.put("objCursoOfertadoVerano", curso);
            
            return ResponseEntity.ok(solicitud);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Rechazar solicitud de curso nuevo
     * PUT /api/cursos-intersemestrales/{id}/rechazar
     */
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarSolicitud(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String motivo = request != null ? request.get("motivo") : "Solicitud rechazada por el funcionario";
            
            // Simular rechazo de solicitud
            Map<String, Object> solicitud = new HashMap<>();
            solicitud.put("id_solicitud", id);
            solicitud.put("nombre_solicitud", "Solicitud de Curso Nuevo");
            solicitud.put("fecha_solicitud", "2024-01-10T10:30:00Z");
            solicitud.put("estado", "Rechazado");
            solicitud.put("observaciones", motivo);
            solicitud.put("condicion", "Primera_Vez");
            solicitud.put("tipoSolicitud", "PREINSCRIPCION");
            
            // Usuario estudiante
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id_usuario", 4);
            usuario.put("nombre", "Pepa");
            usuario.put("apellido", "Gonzalez");
            usuario.put("email", "pepa.gonzalez@unicauca.edu.co");
            usuario.put("telefono", "3001111111");
            usuario.put("codigo_estudiante", "104612345660");
            
            Map<String, Object> rolEstudiante = new HashMap<>();
            rolEstudiante.put("id_rol", 1);
            rolEstudiante.put("nombre", "Estudiante");
            usuario.put("objRol", rolEstudiante);
            
            solicitud.put("objUsuario", usuario);
            
            // Curso ofertado
            Map<String, Object> curso = new HashMap<>();
            curso.put("id_curso", 1);
            curso.put("nombre_curso", "Programacion I");
            curso.put("codigo_curso", "PROG-201");
            curso.put("descripcion", "Fundamentos de programacion");
            curso.put("fecha_inicio", "2024-01-15T00:00:00Z");
            curso.put("fecha_fin", "2024-03-15T00:00:00Z");
            curso.put("cupo_maximo", 25);
            curso.put("cupo_estimado", 20);
            curso.put("cupo_disponible", 15);
            curso.put("espacio_asignado", "Lab 301");
            curso.put("estado", "Preinscripcion");
            
            // Materia del curso
            Map<String, Object> materia = new HashMap<>();
            materia.put("id_materia", 3);
            materia.put("nombre_materia", "Programacion I");
            materia.put("codigo_materia", "PROG");
            materia.put("creditos", 4);
            curso.put("objMateria", materia);
            
            // Docente del curso
            Map<String, Object> docente = new HashMap<>();
            docente.put("id_usuario", 3);
            docente.put("nombre", "Ana");
            docente.put("apellido", "Martinez");
            docente.put("email", "ana@unicauca.edu.co");
            docente.put("telefono", "3009876543");
            
            Map<String, Object> rolDocente = new HashMap<>();
            rolDocente.put("id_rol", 2);
            rolDocente.put("nombre", "Docente");
            docente.put("objRol", rolDocente);
            
            curso.put("objDocente", docente);
            solicitud.put("objCursoOfertadoVerano", curso);
            
            return ResponseEntity.ok(solicitud);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // ==================== ENDPOINT PARA CONFIRMAR INSCRIPCION ====================

    /**
     * Confirmar inscripcion de estudiante
     * PUT /api/cursos-intersemestrales/inscripciones/{id}/confirmar
     */
    @PutMapping("/inscripciones/{id}/confirmar")
    public ResponseEntity<Map<String, Object>> confirmarInscripcion(@PathVariable Long id) {
        try {
            // Simular confirmacion de inscripcion
            Map<String, Object> inscripcion = new HashMap<>();
            inscripcion.put("id", id);
            inscripcion.put("fecha", "2024-01-15T10:30:00");
            inscripcion.put("estado", "inscrito"); // Cambiado de "pendiente" a "inscrito"
            inscripcion.put("estudianteId", 1);
            inscripcion.put("cursoId", 1);
            
            // Informacion completa del estudiante
            Map<String, Object> estudiante = new HashMap<>();
            estudiante.put("id_usuario", 1);
            estudiante.put("nombre", "Ana");
            estudiante.put("apellido", "Gonzalez");
            estudiante.put("email", "ana.gonzalez@unicauca.edu.co");
            estudiante.put("codigo_estudiante", "104612345660");
            inscripcion.put("estudiante", estudiante);
            
            // Informacion del archivo de pago (ya existia)
            Map<String, Object> archivoPago = new HashMap<>();
            archivoPago.put("id_documento", 1);
            archivoPago.put("nombre", "comprobante_pago_ana.pdf");
            archivoPago.put("url", "/uploads/comprobante_pago_ana.pdf");
            archivoPago.put("fecha", "2024-01-15T10:30:00");
            inscripcion.put("archivoPago", archivoPago);
            
            // Mensaje de confirmacion
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("message", "Inscripcion confirmada exitosamente");
            respuesta.put("inscripcion", inscripcion);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== ENDPOINT PARA DESCARGAR COMPROBANTE DE PAGO ====================

    

    @GetMapping("/inscripciones/{idInscripcion}/info")
    public ResponseEntity<Map<String, Object>> infoInscripcion(@PathVariable Long idInscripcion) {
        try {
            log.debug(" Obteniendo informacion de inscripcion: " + " {}", idInscripcion);
            
            // 1. Buscar la inscripcion directamente por ID
            SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
            
            Map<String, Object> resultado = new HashMap<>();
            
            if (inscripcion == null) {
                resultado.put("encontrada", false);
                resultado.put("mensaje", "Inscripcion no encontrada");
                return ResponseEntity.ok(resultado);
            }
            
            resultado.put("encontrada", true);
            resultado.put("id_solicitud", inscripcion.getId_solicitud());
            resultado.put("nombre_solicitud", inscripcion.getNombre_solicitud());
            resultado.put("fecha_registro", inscripcion.getFecha_registro_solicitud());
            
            // Verificar documentos
            List<Documento> documentos = inscripcion.getDocumentos();
            resultado.put("documentos_count", documentos != null ? documentos.size() : "NULL");
            resultado.put("documentos_null", documentos == null);
            resultado.put("documentos_empty", documentos != null && documentos.isEmpty());
            
            if (documentos != null && !documentos.isEmpty()) {
                List<Map<String, Object>> documentosInfo = new ArrayList<>();
                for (Documento doc : documentos) {
                    Map<String, Object> docInfo = new HashMap<>();
                    docInfo.put("id", doc.getId_documento());
                    docInfo.put("nombre", doc.getNombre());
                    docInfo.put("ruta", doc.getRuta_documento());
                    docInfo.put("es_valido", doc.isEsValido());
                    documentosInfo.add(docInfo);
                }
                resultado.put("documentos", documentosInfo);
                
                // Agregar informacion especifica del archivo para descarga
                Documento primerDocumento = documentos.get(0);
                resultado.put("archivo_pago", Map.of(
                    "nombre_archivo", primerDocumento.getNombre(),
                    "ruta_archivo", primerDocumento.getRuta_documento(),
                    "id_documento", primerDocumento.getId_documento()
                ));
            }
            
            log.debug("Informacion de inscripcion obtenida exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo informacion de inscripcion: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo informacion: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }


    // ==================== ENDPOINT PARA SOLICITUDES DE CURSO NUEVO ====================

    /**
     * Obtener solicitudes de curso nuevo
     * GET /api/cursos-intersemestrales/solicitudes-curso-nuevo
     */
    @GetMapping("/solicitudes-curso-nuevo")
    public ResponseEntity<List<Map<String, Object>>> getSolicitudesCursoNuevo() {
        try {
            List<Map<String, Object>> solicitudes = new ArrayList<>();
            
            // Solicitud 1: Pepa Gonzalez
            Map<String, Object> solicitud1 = new HashMap<>();
            solicitud1.put("id_solicitud", 1);
            solicitud1.put("nombre_solicitud", "Solicitud de Curso Nuevo - Programacion Avanzada");
            solicitud1.put("fecha_solicitud", "2024-01-15T10:30:00Z");
            solicitud1.put("estado", "Pendiente");
            solicitud1.put("condicion", "Primera_Vez");
            solicitud1.put("observaciones", "Estudiante solicita curso de programacion avanzada para el verano");
            solicitud1.put("tipoSolicitud", "PREINSCRIPCION");
            
            // Usuario estudiante con estructura corregida
            Map<String, Object> usuario1 = new HashMap<>();
            usuario1.put("id_usuario", 1);
            usuario1.put("nombre_completo", "Pepa Gonzalez");
            usuario1.put("codigo", "104612345660");
            usuario1.put("correo", "pepa.gonzalez@unicauca.edu.co");
            usuario1.put("password", "ContrasenaSegura123");
            usuario1.put("estado_usuario", true);
            
            // Rol corregido
            Map<String, Object> rol1 = new HashMap<>();
            rol1.put("id_rol", 1);
            rol1.put("nombre", "Estudiante"); // SUCCESS CORREGIDO: ya no es null
            usuario1.put("rol", rol1);
            
            // Programa
            Map<String, Object> programa1 = new HashMap<>();
            programa1.put("id_programa", 1);
            programa1.put("codigo", "INF01");
            programa1.put("nombre_programa", "Ingenieria Informatica");
            usuario1.put("objPrograma", programa1);
            
            solicitud1.put("objUsuario", usuario1);
            
            // Curso ofertado
            Map<String, Object> curso1 = new HashMap<>();
            curso1.put("id_curso", 1);
            curso1.put("nombre_curso", "Programacion Avanzada");
            curso1.put("codigo_curso", "PROG-301");
            curso1.put("descripcion", "Curso de programacion avanzada");
            curso1.put("fecha_inicio", "2024-01-15T00:00:00Z");
            curso1.put("fecha_fin", "2024-03-15T00:00:00Z");
            curso1.put("cupo_maximo", 25);
            curso1.put("cupo_estimado", 25);
            curso1.put("cupo_disponible", 20);
            curso1.put("espacio_asignado", "Lab 301");
            curso1.put("estado", "Abierto");
            
            // Materia del curso
            Map<String, Object> materia1 = new HashMap<>();
            materia1.put("id_materia", 1);
            materia1.put("nombre_materia", "Programacion");
            materia1.put("codigo_materia", "PROG");
            materia1.put("creditos", 4);
            curso1.put("objMateria", materia1);
            
            // Docente del curso con estructura corregida
            Map<String, Object> docente1 = new HashMap<>();
            docente1.put("id_usuario", 2);
            docente1.put("nombre_completo", "Maria Garcia");
            docente1.put("codigo", "DOC001");
            docente1.put("correo", "maria.garcia@unicauca.edu.co");
            docente1.put("estado_usuario", true);
            
            // Rol del docente corregido
            Map<String, Object> rolDocente1 = new HashMap<>();
            rolDocente1.put("id_rol", 2);
            rolDocente1.put("nombre", "Docente"); // SUCCESS CORREGIDO: ya no es null
            docente1.put("rol", rolDocente1);
            
            // Programa del docente
            Map<String, Object> programaDocente1 = new HashMap<>();
            programaDocente1.put("id_programa", 1);
            programaDocente1.put("codigo", "INF01");
            programaDocente1.put("nombre_programa", "Ingenieria Informatica");
            docente1.put("objPrograma", programaDocente1);
            
            curso1.put("objDocente", docente1);
            solicitud1.put("objCursoOfertadoVerano", curso1);
            
            solicitudes.add(solicitud1);
            
            // Solicitud 2: Carlos Lopez
            Map<String, Object> solicitud2 = new HashMap<>();
            solicitud2.put("id_solicitud", 2);
            solicitud2.put("nombre_solicitud", "Solicitud de Curso Nuevo - Inteligencia Artificial");
            solicitud2.put("fecha_solicitud", "2024-01-16T14:20:00Z");
            solicitud2.put("estado", "Aprobado");
            solicitud2.put("condicion", "Repitencia");
            solicitud2.put("observaciones", "Estudiante con buen rendimiento academico");
            solicitud2.put("tipoSolicitud", "PREINSCRIPCION");
            
            // Usuario estudiante 2
            Map<String, Object> usuario2 = new HashMap<>();
            usuario2.put("id_usuario", 3);
            usuario2.put("nombre_completo", "Carlos Lopez");
            usuario2.put("codigo", "104612345661");
            usuario2.put("correo", "carlos.lopez@unicauca.edu.co");
            usuario2.put("password", "ContrasenaSegura123");
            usuario2.put("estado_usuario", true);
            
            // Rol corregido
            Map<String, Object> rol2 = new HashMap<>();
            rol2.put("id_rol", 1);
            rol2.put("nombre", "Estudiante"); // SUCCESS CORREGIDO
            usuario2.put("rol", rol2);
            
            // Programa
            Map<String, Object> programa2 = new HashMap<>();
            programa2.put("id_programa", 1);
            programa2.put("codigo", "INF01");
            programa2.put("nombre_programa", "Ingenieria Informatica");
            usuario2.put("objPrograma", programa2);
            
            solicitud2.put("objUsuario", usuario2);
            solicitud2.put("objCursoOfertadoVerano", curso1); // Mismo curso
            
            solicitudes.add(solicitud2);
            
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/inscripciones/curso/{idCurso}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasInscripciones(@PathVariable Long idCurso) {
        try {
            log.debug("[ESTADISTICAS] Obteniendo estadisticas para curso ID: " + " {}", idCurso);
            
            Map<String, Object> estadisticas = new HashMap<>();
            
            // Contar inscripciones por estado con logging detallado
            Integer pendientes = solicitudGateway.contarInscripcionesPorEstado(idCurso.intValue(), "Enviada");
            Integer aceptadas = solicitudGateway.contarInscripcionesPorEstado(idCurso.intValue(), "Pago_Validado");
            Integer rechazadas = solicitudGateway.contarInscripcionesPorEstado(idCurso.intValue(), "Pago_Rechazado");
            
            log.debug("[ESTADISTICAS] Conteos individuales:");
            log.debug("[ESTADISTICAS] - Pendientes (Enviada): {}", pendientes);
            log.debug("[ESTADISTICAS] - Aceptadas (Pago_Validado): {}", aceptadas);
            log.debug("[ESTADISTICAS] - Rechazadas (Pago_Rechazado): {}", rechazadas);
            
            Integer totalInscripciones = pendientes + aceptadas + rechazadas;
            log.debug("[ESTADISTICAS] - Total: {}", totalInscripciones);
            
            estadisticas.put("total_inscripciones", totalInscripciones);
            estadisticas.put("pendientes_revision", pendientes);
            estadisticas.put("aceptadas", aceptadas);
            estadisticas.put("rechazadas", rechazadas);
            estadisticas.put("curso_id", idCurso);
            estadisticas.put("fecha_consulta", new java.util.Date());
            
            log.debug("[ESTADISTICAS] Estadisticas generadas: " + " {}", estadisticas);
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            log.error("[ESTADISTICAS] Error obteniendo estadisticas: " + e.getMessage());
            log.error("Error: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo estadisticas: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Descargar comprobante de pago por ID de inscripcion (generico para cualquier estudiante)
     * GET /api/cursos-intersemestrales/inscripciones/{idInscripcion}/comprobante
     */
    @GetMapping("/inscripciones/{idInscripcion}/comprobante")
    public ResponseEntity<?> descargarComprobantePago(@PathVariable Long idInscripcion) {
        try {
            log.debug("[DESCARGAR_COMPROBANTE] Iniciando descarga para inscripcion ID: {}", idInscripcion);
            
            // Validar ID
            if (idInscripcion == null || idInscripcion <= 0) {
                log.warn("[DESCARGAR_COMPROBANTE] ID de inscripcion invalido: {}", idInscripcion);
                return ResponseEntity.badRequest().body("ID de inscripcion invalido");
            }
            
            // 1. Buscar la inscripcion
            log.debug("[DESCARGAR_COMPROBANTE] Buscando inscripcion con ID: {}", idInscripcion.intValue());
            SolicitudCursoVeranoIncripcion inscripcion = solicitudCU.buscarPorIdInscripcion(idInscripcion.intValue());
            
            if (inscripcion == null) {
                log.warn("[DESCARGAR_COMPROBANTE] Inscripcion no encontrada: {}", idInscripcion);
                return ResponseEntity.status(404)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Inscripcion no encontrada\",\"idInscripcion\":" + idInscripcion + "}");
            }
            
            log.debug("[DESCARGAR_COMPROBANTE] Inscripcion encontrada: {} (ID: {})", 
                inscripcion.getNombre_solicitud(), inscripcion.getId_solicitud());
            
            // 2. Buscar documentos asociados
            List<Documento> documentos = inscripcion.getDocumentos();
            if (documentos == null || documentos.isEmpty()) {
                log.warn("[DESCARGAR_COMPROBANTE] No hay documentos asociados a la inscripcion: {}", idInscripcion);
                return ResponseEntity.status(404)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"No hay documentos asociados a esta inscripcion\",\"idInscripcion\":" + idInscripcion + "}");
            }
            
            log.debug("[DESCARGAR_COMPROBANTE] Documentos asociados: {}", documentos.size());
            
            // 3. Buscar el primer documento PDF (comprobante de pago)
            for (Documento documento : documentos) {
                log.debug("[DESCARGAR_COMPROBANTE] Revisando documento: {}, ID: {}", 
                    documento.getNombre() != null ? documento.getNombre() : "NOMBRE_NULL",
                    documento.getId_documento());
                
                if (documento.getNombre() != null && documento.getNombre().toLowerCase().endsWith(".pdf")) {
                    try {
                        log.debug("[DESCARGAR_COMPROBANTE] Documento PDF encontrado: {}, Ruta: {}", 
                            documento.getNombre(), documento.getRuta_documento());
                        
                        // Obtener el archivo usando la ruta completa si está disponible, sino usar nombre
                        String rutaArchivo = documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre();
                        byte[] archivo = objGestionarArchivos.getFile(rutaArchivo);
                        
                        if (archivo == null || archivo.length == 0) {
                            log.warn("[DESCARGAR_COMPROBANTE] Archivo no encontrado en disco: {}", rutaArchivo);
                            // Intentar con el nombre si la ruta no funcionó
                            if (documento.getRuta_documento() != null && !documento.getRuta_documento().equals(documento.getNombre())) {
                                log.debug("[DESCARGAR_COMPROBANTE] Intentando con nombre alternativo: {}", documento.getNombre());
                                archivo = objGestionarArchivos.getFile(documento.getNombre());
                            }
                            
                            if (archivo == null || archivo.length == 0) {
                                log.warn("[DESCARGAR_COMPROBANTE] Archivo no disponible, probando siguiente documento");
                                continue; // Probar el siguiente documento
                            }
                        }
                        
                        log.debug("[DESCARGAR_COMPROBANTE] Archivo obtenido exitosamente: {} ({} bytes)", 
                            documento.getNombre(), archivo.length);
                        
                        // Configurar headers para descarga
                        String contentDisposition = "attachment; filename=\"" + documento.getNombre() + "\"";
                        
                        return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(archivo);
                            
                    } catch (Exception e) {
                        log.error("[DESCARGAR_COMPROBANTE] Error procesando documento: {} - {}", 
                            documento.getNombre(), e.getMessage(), e);
                        continue; // Probar el siguiente documento
                    }
                } else {
                    log.debug("[DESCARGAR_COMPROBANTE] Documento ignorado (no es PDF): {}", 
                        documento.getNombre() != null ? documento.getNombre() : "NOMBRE_NULL");
                }
            }
            
            log.warn("[DESCARGAR_COMPROBANTE] No se encontro ningun documento PDF valido para la inscripcion: {}", idInscripcion);
            return ResponseEntity.status(404)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"No se encontro ningun comprobante PDF valido\",\"idInscripcion\":" + idInscripcion + "}");
                
        } catch (Exception e) {
            log.error("[DESCARGAR_COMPROBANTE] Error descargando comprobante: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Error interno del servidor al descargar comprobante\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/debug/inscripcion/{idInscripcion}")
    public ResponseEntity<Map<String, Object>> debugInscripcion(@PathVariable Long idInscripcion) {
        try {
            log.debug("Verificando la inscripcion con ID {}", idInscripcion);
            
            // Buscar la inscripcion
            SolicitudCursoVeranoIncripcion inscripcion = solicitudGateway.buscarSolicitudInscripcionPorId(idInscripcion.intValue());
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("status", "OK");
            resultado.put("inscripcion_encontrada", inscripcion != null);
            
            if (inscripcion != null) {
                resultado.put("id_solicitud", inscripcion.getId_solicitud());
                resultado.put("nombre_solicitud", inscripcion.getNombre_solicitud());
                resultado.put("fecha_registro", inscripcion.getFecha_registro_solicitud());
                
                // Verificar documentos
                List<Documento> documentos = inscripcion.getDocumentos();
                resultado.put("documentos_count", documentos != null ? documentos.size() : "NULL");
                resultado.put("documentos_null", documentos == null);
                resultado.put("documentos_empty", documentos != null && documentos.isEmpty());
                
                if (documentos != null && !documentos.isEmpty()) {
                    List<Map<String, Object>> documentosInfo = new ArrayList<>();
                    for (Documento doc : documentos) {
                        Map<String, Object> docInfo = new HashMap<>();
                        docInfo.put("id", doc.getId_documento());
                        docInfo.put("nombre", doc.getNombre());
                        docInfo.put("ruta", doc.getRuta_documento());
                        docInfo.put("es_valido", doc.isEsValido());
                        documentosInfo.add(docInfo);
                    }
                    resultado.put("documentos", documentosInfo);
                }
            }
            
            log.debug("La verificacion de la inscripcion finalizo correctamente.");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Se presento un error al verificar la inscripcion: " + e.getMessage());
            log.error("Error: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Error verificando inscripcion: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Método auxiliar para parsear fechas correctamente, evitando problemas de zona horaria.
     * Si la fecha viene sin hora (formato yyyy-MM-dd), se establece al final del día (23:59:59) en UTC.
     * 
     * @param fechaStr String con la fecha a parsear
     * @return Date parseado correctamente
     * @throws java.text.ParseException si no se puede parsear la fecha
     */
    private java.util.Date parsearFecha(String fechaStr) throws java.text.ParseException {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            throw new java.text.ParseException("Fecha vacía", 0);
        }
        
        // Intentar primero con formato ISO completo (con hora y zona horaria)
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            sdf.setLenient(false);
            return sdf.parse(fechaStr);
        } catch (java.text.ParseException e) {
            // Si falla, intentar con formato ISO sin la Z
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                sdf.setLenient(false);
                return sdf.parse(fechaStr);
            } catch (java.text.ParseException e2) {
                // Si falla, intentar con formato simple (solo fecha)
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    sdf.setLenient(false);
                    java.util.Date fecha = sdf.parse(fechaStr);
                    
                    // Si es formato simple, establecer la hora al final del día (23:59:59) en UTC
                    // para evitar problemas de zona horaria que muestren un día anterior
                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                    cal.setTime(fecha);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    cal.set(java.util.Calendar.MINUTE, 59);
                    cal.set(java.util.Calendar.SECOND, 59);
                    cal.set(java.util.Calendar.MILLISECOND, 999);
                    return cal.getTime();
                } catch (java.text.ParseException e3) {
                    throw new java.text.ParseException("Formato de fecha inválido: " + fechaStr, 0);
                }
            }
        }
    }

}


