package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoNuevoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.PreinscripcionCursoVeranoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
public class CursosIntersemestralesRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final CursosOfertadosMapperDominio cursoMapper;
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudMapper;

    /**
     * Obtener cursos de verano disponibles para estudiantes
     * GET /api/cursos-intersemestrales/cursos-verano/disponibles
     */
    @GetMapping("/cursos-verano/disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerCursosVeranoDisponibles() {
        try {
            List<Map<String, Object>> cursosDisponibles = new ArrayList<>();
            
            // Curso 1: Matemáticas Básicas
            Map<String, Object> curso1 = new HashMap<>();
            curso1.put("id_curso", 1);
            curso1.put("nombre_curso", "Matemáticas Básicas");
            curso1.put("codigo_curso", "MAT-101");
            curso1.put("descripcion", "Curso de matemáticas básicas para verano");
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
            materia1.put("nombre", "Matemáticas");
            materia1.put("creditos", 3);
            curso1.put("objMateria", materia1);
            
            // Objeto docente
            Map<String, Object> docente1 = new HashMap<>();
            docente1.put("id_usuario", 2);
            docente1.put("nombre", "Juan");
            docente1.put("apellido", "Pérez");
            curso1.put("objDocente", docente1);
            
            cursosDisponibles.add(curso1);
            
            // Curso 2: Programación I
            Map<String, Object> curso2 = new HashMap<>();
            curso2.put("id_curso", 2);
            curso2.put("nombre_curso", "Programación I");
            curso2.put("codigo_curso", "PROG-201");
            curso2.put("descripcion", "Fundamentos de programación");
            curso2.put("fecha_inicio", "2024-06-15T08:00:00Z");
            curso2.put("fecha_fin", "2024-07-30T17:00:00Z");
            curso2.put("cupo_maximo", 25);
            curso2.put("cupo_disponible", 20);
            curso2.put("cupo_estimado", 25);
            curso2.put("espacio_asignado", "Lab 301");
            curso2.put("estado", "Preinscripcion");
            
            Map<String, Object> materia2 = new HashMap<>();
            materia2.put("id_materia", 2);
            materia2.put("nombre", "Programación");
            materia2.put("creditos", 4);
            curso2.put("objMateria", materia2);
            
            Map<String, Object> docente2 = new HashMap<>();
            docente2.put("id_usuario", 3);
            docente2.put("nombre", "María");
            docente2.put("apellido", "García");
            curso2.put("objDocente", docente2);
            
            cursosDisponibles.add(curso2);
            
            // Curso 3: Bases de Datos
            Map<String, Object> curso3 = new HashMap<>();
            curso3.put("id_curso", 3);
            curso3.put("nombre_curso", "Bases de Datos");
            curso3.put("codigo_curso", "BD-301");
            curso3.put("descripcion", "Diseño y gestión de bases de datos");
            curso3.put("fecha_inicio", "2024-07-01T08:00:00Z");
            curso3.put("fecha_fin", "2024-08-15T17:00:00Z");
            curso3.put("cupo_maximo", 20);
            curso3.put("cupo_disponible", 15);
            curso3.put("cupo_estimado", 20);
            curso3.put("espacio_asignado", "Aula 102");
            curso3.put("estado", "Inscripcion");
            
            Map<String, Object> materia3 = new HashMap<>();
            materia3.put("id_materia", 3);
            materia3.put("nombre", "Bases de Datos");
            materia3.put("creditos", 3);
            curso3.put("objMateria", materia3);
            
            Map<String, Object> docente3 = new HashMap<>();
            docente3.put("id_usuario", 4);
            docente3.put("nombre", "Carlos");
            docente3.put("apellido", "López");
            curso3.put("objDocente", docente3);
            
            cursosDisponibles.add(curso3);
            
            return ResponseEntity.ok(cursosDisponibles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener cursos disponibles para preinscripción
     * GET /api/cursos-intersemestrales/cursos/preinscripcion
     */
    @GetMapping("/cursos/preinscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosPreinscripcion() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar solo cursos que estén en estado "Preinscripcion"
            List<CursoOfertadoVerano> cursosPreinscripcion = cursos.stream()
                    .filter(curso -> {
                        if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
                            return false;
                        }
                        String estadoActual = curso.getEstadosCursoOfertados().get(0).getEstado_actual();
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
     * Obtener solicitudes de un estudiante específico
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
     * Obtener cursos ofertados (método legacy para compatibilidad)
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
            List<Map<String, Object>> cursosDisponibles = new ArrayList<>();
            
            // Agregar cursos de prueba específicos
            Map<String, Object> curso1 = new HashMap<>();
            curso1.put("id_curso", 1);
            curso1.put("nombre_curso", "Inteligencia Artificial");
            curso1.put("codigo_curso", "IA-301");
            curso1.put("creditos", 4);
            curso1.put("descripcion", "Curso avanzado de inteligencia artificial");
            cursosDisponibles.add(curso1);
            
            Map<String, Object> curso2 = new HashMap<>();
            curso2.put("id_curso", 2);
            curso2.put("nombre_curso", "Desarrollo Web");
            curso2.put("codigo_curso", "WEB-302");
            curso2.put("creditos", 3);
            curso2.put("descripcion", "Desarrollo web moderno con React y Node.js");
            cursosDisponibles.add(curso2);
            
            Map<String, Object> curso3 = new HashMap<>();
            curso3.put("id_curso", 3);
            curso3.put("nombre_curso", "Machine Learning");
            curso3.put("codigo_curso", "ML-401");
            curso3.put("creditos", 4);
            curso3.put("descripcion", "Fundamentos de machine learning y deep learning");
            cursosDisponibles.add(curso3);
            
            Map<String, Object> curso4 = new HashMap<>();
            curso4.put("id_curso", 4);
            curso4.put("nombre_curso", "Ciberseguridad");
            curso4.put("codigo_curso", "CS-501");
            curso4.put("creditos", 3);
            curso4.put("descripcion", "Seguridad informática y protección de datos");
            cursosDisponibles.add(curso4);
            
            return ResponseEntity.ok(cursosDisponibles);
        } catch (Exception e) {
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
            // Crear respuesta JSON con la estructura esperada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_solicitud", 1);
            respuesta.put("nombreCompleto", peticion.getNombreCompleto());
            respuesta.put("codigo", peticion.getCodigo());
            respuesta.put("curso", peticion.getCurso());
            respuesta.put("condicion", peticion.getCondicion());
            respuesta.put("fecha", java.time.LocalDateTime.now().toString());
            respuesta.put("estado", "Pendiente");
            
            // Crear objeto usuario
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id_usuario", peticion.getIdUsuario());
            usuario.put("nombre_completo", peticion.getNombreCompleto());
            respuesta.put("objUsuario", usuario);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Crear preinscripción a curso de verano
     * POST /api/cursos-intersemestrales/cursos-verano/preinscripciones
     */
    @PostMapping("/cursos-verano/preinscripciones")
    public ResponseEntity<Map<String, Object>> crearPreinscripcion(@RequestBody PreinscripcionCursoVeranoDTOPeticion peticion) {
        try {
            // Crear respuesta JSON con la estructura esperada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_preinscripcion", 1);
            respuesta.put("idUsuario", peticion.getIdUsuario());
            respuesta.put("idCurso", peticion.getIdCurso());
            respuesta.put("nombreSolicitud", peticion.getNombreSolicitud());
            respuesta.put("fecha", java.time.LocalDateTime.now().toString());
            respuesta.put("estado", "Pendiente");
            respuesta.put("mensaje", "Preinscripción creada exitosamente");
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener inscripciones
     * GET /api/cursos-intersemestrales/inscripciones
     */
    @GetMapping("/inscripciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerInscripciones() {
        try {
            List<Map<String, Object>> inscripciones = new ArrayList<>();
            
            // Inscripción 1
            Map<String, Object> inscripcion1 = new HashMap<>();
            inscripcion1.put("id", 1);
            inscripcion1.put("cursoId", 1);
            inscripcion1.put("estudianteId", 1);
            inscripcion1.put("fecha", "2024-01-15T10:30:00");
            inscripcion1.put("estado", "inscrito");
            inscripciones.add(inscripcion1);
            
            // Inscripción 2
            Map<String, Object> inscripcion2 = new HashMap<>();
            inscripcion2.put("id", 2);
            inscripcion2.put("cursoId", 2);
            inscripcion2.put("estudianteId", 1);
            inscripcion2.put("fecha", "2024-01-16T14:20:00");
            inscripcion2.put("estado", "inscrito");
            inscripciones.add(inscripcion2);
            
            // Inscripción 3
            Map<String, Object> inscripcion3 = new HashMap<>();
            inscripcion3.put("id", 3);
            inscripcion3.put("cursoId", 3);
            inscripcion3.put("estudianteId", 2);
            inscripcion3.put("fecha", "2024-01-17T09:15:00");
            inscripcion3.put("estado", "pendiente");
            inscripciones.add(inscripcion3);
            
            return ResponseEntity.ok(inscripciones);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Obtener solicitudes del usuario específico
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
            solicitud1.put("curso", "Programación I");
            solicitud1.put("fecha", "2024-01-15T10:30:00");
            solicitud1.put("estado", "Pendiente");
            solicitud1.put("tipo", "Preinscripción");
            solicitudes.add(solicitud1);
            
            // Solicitud 2
            Map<String, Object> solicitud2 = new HashMap<>();
            solicitud2.put("id_solicitud", 2);
            solicitud2.put("curso", "Matemáticas Básicas");
            solicitud2.put("fecha", "2024-01-16T14:20:00");
            solicitud2.put("estado", "Aprobado");
            solicitud2.put("tipo", "Preinscripción");
            solicitudes.add(solicitud2);
            
            // Solicitud 3
            Map<String, Object> solicitud3 = new HashMap<>();
            solicitud3.put("id_solicitud", 3);
            solicitud3.put("curso", "Bases de Datos");
            solicitud3.put("fecha", "2024-01-17T09:15:00");
            solicitud3.put("estado", "Rechazado");
            solicitud3.put("tipo", "Inscripción");
            solicitudes.add(solicitud3);
            
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Cancelar inscripción
     * DELETE /api/cursos-intersemestrales/inscripciones/{id}
     */
    @DeleteMapping("/inscripciones/{id}")
    public ResponseEntity<Map<String, Object>> cancelarInscripcion(@PathVariable Integer id) {
        try {
            // Simular búsqueda de inscripción (en implementación real usarías un servicio)
            Map<String, Object> inscripcion = new HashMap<>();
            inscripcion.put("id", id);
            inscripcion.put("estado", "inscrito"); // Estado actual
            
            // Verificar si la inscripción existe (simulado)
            if (id < 1 || id > 10) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Inscripción no encontrada");
                error.put("message", "La inscripción con ID " + id + " no existe");
                error.put("status", 404);
                error.put("timestamp", java.time.LocalDateTime.now().toString());
                return ResponseEntity.status(404).body(error);
            }
            
            // Verificar si ya está cancelada (simulado)
            if (id == 5) { // Simular una inscripción ya cancelada
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se puede cancelar la inscripción");
                error.put("message", "La inscripción ya está cancelada o no se puede cancelar en este momento");
                error.put("status", 400);
                error.put("timestamp", java.time.LocalDateTime.now().toString());
                return ResponseEntity.status(400).body(error);
            }
            
            // Cancelar la inscripción exitosamente
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("message", "Inscripción cancelada exitosamente");
            respuesta.put("inscripcionId", id);
            respuesta.put("estadoAnterior", "inscrito");
            respuesta.put("estadoNuevo", "cancelada");
            respuesta.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ha ocurrido un error inesperado. Por favor, contacta al administrador.");
            error.put("status", 500);
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(error);
        }
    }
}
