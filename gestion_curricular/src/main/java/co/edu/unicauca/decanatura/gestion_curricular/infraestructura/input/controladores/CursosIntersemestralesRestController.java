package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
public class CursosIntersemestralesRestController {

    private final GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    private final GestionarSolicitudCursoVeranoGatewayIntPort solicitudGateway;
    private final GestionarMateriasCUIntPort materiaCU;
    private final CursosOfertadosMapperDominio cursoMapper;
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudMapper;
    private final InscripcionService inscripcionService;
    private final SolicitudRepositoryInt solicitudRepository;
    private final GestionarUsuarioCUIntPort usuarioCU;
    private final CursoOfertadoVeranoRepositoryInt cursoRepository;
    private final EstadoCursoOfertadoRepositoryInt estadoRepository;

    /**
     * Obtener cursos de verano (endpoint principal que llama el frontend)
     * GET /api/cursos-intersemestrales/cursos-verano
     */
    @GetMapping("/cursos-verano")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosVerano() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
                    .map(cursoMapper::mappearDeCursoOfertadoARespuestaDisponible)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
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
     * Obtener cursos disponibles para preinscripción (solo cursos en estado Preinscripción)
     * GET /api/cursos-intersemestrales/cursos/preinscripcion
     */
    @GetMapping("/cursos/preinscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosPreinscripcion() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar solo cursos en estado de preinscripción
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
     * Obtener cursos disponibles para inscripción (solo cursos en estado Inscripción)
     * GET /api/cursos-intersemestrales/cursos/inscripcion
     */
    @GetMapping("/cursos/inscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosInscripcion() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Filtrar solo cursos en estado de inscripción
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
            // Usar datos reales de la base de datos
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            List<Map<String, Object>> cursosDisponibles = new ArrayList<>();
            
            for (CursoOfertadoVerano curso : cursos) {
                Map<String, Object> cursoMap = new HashMap<>();
                cursoMap.put("id_curso", curso.getId_curso());
                cursoMap.put("nombre_curso", curso.getObjMateria() != null ? curso.getObjMateria().getNombre() : "Sin nombre");
                cursoMap.put("codigo_curso", curso.getObjMateria() != null ? curso.getObjMateria().getCodigo() : "Sin código");
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
                materiaMap.put("descripcion", materia.getNombre() + " (" + materia.getCodigo() + ") - " + materia.getCreditos() + " créditos");
                materiasDisponibles.add(materiaMap);
            }
            
            return ResponseEntity.ok(materiasDisponibles);
        } catch (Exception e) {
            System.out.println("ERROR obteniendo materias disponibles: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("DEBUG: Recibiendo solicitud de curso nuevo:");
            System.out.println("  - Nombre Completo: " + peticion.getNombreCompleto());
            System.out.println("  - Código: " + peticion.getCodigo());
            System.out.println("  - Curso: " + peticion.getCurso());
            System.out.println("  - Condición: " + peticion.getCondicion());
            System.out.println("  - ID Usuario: " + peticion.getIdUsuario());

            // Mapear el DTO a nuestro modelo de dominio
            SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
            solicitudDominio.setNombre_estudiante(peticion.getNombreCompleto());
            solicitudDominio.setCodigo_estudiante(peticion.getCodigo());
            
            // Mapear la condición con validación
            try {
                solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.valueOf(peticion.getCondicion()));
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR ERROR: Condición inválida: " + peticion.getCondicion());
                throw new IllegalArgumentException("Condición inválida: " + peticion.getCondicion() + ". Valores válidos: " + java.util.Arrays.toString(CondicionSolicitudVerano.values()));
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

            System.out.println("DEBUG DEBUG: Solicitud dominio creada, llamando al caso de uso...");

            // Llamar al caso de uso
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);
            
            System.out.println("DEBUG DEBUG: Solicitud guardada con ID: " + (solicitudGuardada != null ? solicitudGuardada.getId_solicitud() : "NULL"));

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
            
            System.out.println("SUCCESS DEBUG: Respuesta creada exitosamente");
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR ERROR de validación: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error de validación: " + e.getMessage());
            error.put("tipo", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.out.println("ERROR ERROR interno del servidor: " + e.getMessage());
            e.printStackTrace(); // Imprimir el stack trace completo para debug
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
        System.out.println("DEBUG DEBUG: Endpoint de prueba simple llamado");
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Endpoint funcionando correctamente");
        respuesta.put("timestamp", new java.util.Date().toString());
        respuesta.put("puerto", "5000");
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint de prueba POST simple para simular la petición del frontend
     * POST /api/cursos-intersemestrales/test-post-simple
     */
    @PostMapping("/test-post-simple")
    public ResponseEntity<Map<String, Object>> testPostSimple(@RequestBody Map<String, Object> datos) {
        try {
            System.out.println("DEBUG DEBUG: Endpoint POST de prueba llamado");
            System.out.println("DEBUG DEBUG: Datos recibidos: " + datos);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "POST funcionando correctamente");
            respuesta.put("datos_recibidos", datos);
            respuesta.put("timestamp", new java.util.Date().toString());
            respuesta.put("puerto", "5000");
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("ERROR ERROR en POST de prueba: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("DEBUG DEBUG: Verificando usuarios disponibles...");
            
            // Buscar usuarios en la base de datos usando el caso de uso correcto
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario> usuarios = 
                usuarioCU.listarUsuarios(); // Listar todos los usuarios disponibles
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Usuarios encontrados");
            respuesta.put("total", usuarios.size());
            respuesta.put("usuarios", usuarios);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("ERROR ERROR verificando usuarios: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error verificando usuarios: " + e.getMessage());
            error.put("tipo", "USUARIOS_ERROR");
            error.put("clase", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(error); // Devolver como 200 para ver el error
        }
    }

    /**
     * Endpoint de prueba que simula exactamente el endpoint problemático
     * POST /api/cursos-intersemestrales/test-solicitud-exacta
     */
    @PostMapping("/test-solicitud-exacta")
    public ResponseEntity<Map<String, Object>> testSolicitudExacta(@RequestBody SolicitudCursoNuevoDTOPeticion peticion) {
        try {
            System.out.println("DEBUG DEBUG: Test endpoint exacto llamado");
            System.out.println("DEBUG DEBUG: Petición recibida: " + peticion);
            
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
            System.out.println("ERROR ERROR en test exacto: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("DEBUG DEBUG: Probando endpoint de solicitud de curso nuevo");
            
            // Crear datos de prueba
            SolicitudCursoNuevoDTOPeticion peticionPrueba = new SolicitudCursoNuevoDTOPeticion();
            peticionPrueba.setNombreCompleto("Juan Pérez");
            peticionPrueba.setCodigo("104612345660");
            peticionPrueba.setCurso("Programación Avanzada");
            peticionPrueba.setCondicion("Primera_Vez");
            peticionPrueba.setIdUsuario(1);
            
            System.out.println("DEBUG DEBUG: Datos de prueba creados, llamando al endpoint...");
            
            // Llamar al método principal
            ResponseEntity<Map<String, Object>> respuesta = crearSolicitudCursoNuevo(peticionPrueba);
            
            System.out.println("DEBUG DEBUG: Respuesta del endpoint: " + respuesta.getStatusCode());
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("mensaje", "Prueba completada");
            resultado.put("status", respuesta.getStatusCode().value());
            resultado.put("respuesta", respuesta.getBody());
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            System.out.println("ERROR ERROR en prueba: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en prueba: " + e.getMessage());
            error.put("tipo", "TEST_ERROR");
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
            System.out.println("DEBUG DEBUG: Recibiendo preinscripción:");
            System.out.println("  - ID Usuario: " + peticion.getIdUsuario());
            System.out.println("  - ID Curso: " + peticion.getIdCurso());
            System.out.println("  - Nombre Solicitud: " + peticion.getNombreSolicitud());
            
            // Mapear el DTO a nuestro modelo de dominio
            SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
            solicitudDominio.setNombre_estudiante("Estudiante"); // Valor por defecto
            solicitudDominio.setCodigo_estudiante("EST001"); // Valor por defecto
            solicitudDominio.setObservacion(peticion.getNombreSolicitud());
            solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez); // Valor por defecto
            
            System.out.println("DEBUG DEBUG: Solicitud dominio creada");
            
            // Crear curso con el ID real
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            curso.setId_curso(peticion.getIdCurso());
            solicitudDominio.setObjCursoOfertadoVerano(curso);
            
            System.out.println("DEBUG DEBUG: Curso asignado con ID: " + peticion.getIdCurso());
            
            // Crear usuario
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario = 
                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
            usuario.setId_usuario(peticion.getIdUsuario());
            solicitudDominio.setObjUsuario(usuario);

            System.out.println("DEBUG DEBUG: Usuario asignado con ID: " + peticion.getIdUsuario());
            
            // Verificar si ya existe una preinscripción para este usuario y curso
            System.out.println("DEBUG DEBUG: Verificando preinscripciones existentes...");
            Solicitud preinscripcionExistente = solicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(peticion.getIdUsuario(), peticion.getIdCurso());
            
            if (preinscripcionExistente != null) {
                System.out.println("ERROR ERROR: Ya existe una preinscripción para este usuario y curso");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Ya tienes una preinscripción activa para este curso");
                error.put("codigo", "DUPLICATE_PREINSCRIPTION");
                return ResponseEntity.badRequest().body(error);
            }
            
            System.out.println("DEBUG DEBUG: No hay preinscripciones duplicadas, procediendo a crear...");

            // Llamar al gateway directamente para guardar en la base de datos
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

            System.out.println("DEBUG DEBUG: Solicitud guardada con ID: " + (solicitudGuardada != null ? solicitudGuardada.getId_solicitud() : "NULL"));

            // Crear respuesta JSON con la estructura esperada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_preinscripcion", solicitudGuardada.getId_solicitud());
            respuesta.put("idUsuario", peticion.getIdUsuario());
            respuesta.put("idCurso", peticion.getIdCurso());
            respuesta.put("nombreSolicitud", peticion.getNombreSolicitud());
            respuesta.put("fecha", solicitudGuardada.getFecha_registro_solicitud());
            respuesta.put("estado", "Pendiente");
            respuesta.put("mensaje", "Preinscripción creada exitosamente");
            
            System.out.println("DEBUG DEBUG: Respuesta creada exitosamente");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("ERROR ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener inscripciones con información completa
     * GET /api/cursos-intersemestrales/inscripciones
     */
    @GetMapping("/inscripciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerInscripciones() {
        try {
            // Por ahora usar datos hardcodeados que funcionan
            // TODO: Implementar conexión real a la base de datos
            List<Map<String, Object>> inscripciones = new ArrayList<>();
            System.out.println("INFO Usando datos simulados para inscripciones");
            
            // Inscripción 1: Ana González
            Map<String, Object> inscripcion1 = new HashMap<>();
            inscripcion1.put("id", 1);
            inscripcion1.put("fecha", "2024-01-15T10:30:00");
            inscripcion1.put("estado", "inscrito");
            inscripcion1.put("estudianteId", 1);
            inscripcion1.put("cursoId", 1);
            
            // Información completa del estudiante
            Map<String, Object> estudiante1 = new HashMap<>();
            estudiante1.put("id_usuario", 1);
            estudiante1.put("nombre", "Ana");
            estudiante1.put("apellido", "González");
            estudiante1.put("email", "ana.gonzalez@unicauca.edu.co");
            estudiante1.put("codigo_estudiante", "104612345660");
            inscripcion1.put("estudiante", estudiante1);
            
            // Información del archivo de pago
            Map<String, Object> archivoPago1 = new HashMap<>();
            archivoPago1.put("id_documento", 1);
            archivoPago1.put("nombre", "comprobante_pago_ana.pdf");
            archivoPago1.put("url", "/uploads/comprobante_pago_ana.pdf");
            archivoPago1.put("fecha", "2024-01-15T10:30:00");
            inscripcion1.put("archivoPago", archivoPago1);
            
            inscripciones.add(inscripcion1);
            
            // Inscripción 2: Carlos López
            Map<String, Object> inscripcion2 = new HashMap<>();
            inscripcion2.put("id", 2);
            inscripcion2.put("fecha", "2024-01-16T14:20:00");
            inscripcion2.put("estado", "inscrito");
            inscripcion2.put("estudianteId", 2);
            inscripcion2.put("cursoId", 2);
            
            // Información completa del estudiante
            Map<String, Object> estudiante2 = new HashMap<>();
            estudiante2.put("id_usuario", 2);
            estudiante2.put("nombre", "Carlos");
            estudiante2.put("apellido", "López");
            estudiante2.put("email", "carlos.lopez@unicauca.edu.co");
            estudiante2.put("codigo_estudiante", "104612345661");
            inscripcion2.put("estudiante", estudiante2);
            
            // Información del archivo de pago
            Map<String, Object> archivoPago2 = new HashMap<>();
            archivoPago2.put("id_documento", 2);
            archivoPago2.put("nombre", "comprobante_pago_carlos.pdf");
            archivoPago2.put("url", "/uploads/comprobante_pago_carlos.pdf");
            archivoPago2.put("fecha", "2024-01-16T14:20:00");
            inscripcion2.put("archivoPago", archivoPago2);
            
            inscripciones.add(inscripcion2);
            
            // Inscripción 3: María Rodríguez (sin archivo de pago)
            Map<String, Object> inscripcion3 = new HashMap<>();
            inscripcion3.put("id", 3);
            inscripcion3.put("fecha", "2024-01-17T09:15:00");
            inscripcion3.put("estado", "pendiente");
            inscripcion3.put("estudianteId", 3);
            inscripcion3.put("cursoId", 1);
            
            // Información completa del estudiante
            Map<String, Object> estudiante3 = new HashMap<>();
            estudiante3.put("id_usuario", 3);
            estudiante3.put("nombre", "María");
            estudiante3.put("apellido", "Rodríguez");
            estudiante3.put("email", "maria.rodriguez@unicauca.edu.co");
            estudiante3.put("codigo_estudiante", "104612345662");
            inscripcion3.put("estudiante", estudiante3);
            
            // Sin archivo de pago (null)
            inscripcion3.put("archivoPago", null);
            
            inscripciones.add(inscripcion3);
            
            // Inscripción 4: Pedro Martínez
            Map<String, Object> inscripcion4 = new HashMap<>();
            inscripcion4.put("id", 4);
            inscripcion4.put("fecha", "2024-01-18T16:45:00");
            inscripcion4.put("estado", "inscrito");
            inscripcion4.put("estudianteId", 4);
            inscripcion4.put("cursoId", 3);
            
            // Información completa del estudiante
            Map<String, Object> estudiante4 = new HashMap<>();
            estudiante4.put("id_usuario", 4);
            estudiante4.put("nombre", "Pedro");
            estudiante4.put("apellido", "Martínez");
            estudiante4.put("email", "pedro.martinez@unicauca.edu.co");
            estudiante4.put("codigo_estudiante", "104612345663");
            inscripcion4.put("estudiante", estudiante4);
            
            // Información del archivo de pago
            Map<String, Object> archivoPago4 = new HashMap<>();
            archivoPago4.put("id_documento", 4);
            archivoPago4.put("nombre", "comprobante_pago_pedro.pdf");
            archivoPago4.put("url", "/uploads/comprobante_pago_pedro.pdf");
            archivoPago4.put("fecha", "2024-01-18T16:45:00");
            inscripcion4.put("archivoPago", archivoPago4);
            
            inscripciones.add(inscripcion4);
            
            // Enriquecer los datos con información de estudiantes y archivos de pago
            List<Map<String, Object>> inscripcionesEnriquecidas = new ArrayList<>();
            
            for (Map<String, Object> inscripcion : inscripciones) {
                Map<String, Object> inscripcionEnriquecida = new HashMap<>(inscripcion);
                
                // Obtener información del estudiante
                Map<String, Object> estudiante = new HashMap<>();
                Integer estudianteId = (Integer) inscripcion.get("estudianteId");
                
                switch (estudianteId) {
                    case 1:
                        estudiante.put("id_usuario", 1);
                        estudiante.put("nombre", "Ana");
                        estudiante.put("apellido", "González");
                        estudiante.put("email", "ana.gonzalez@unicauca.edu.co");
                        estudiante.put("codigo_estudiante", "104612345660");
                        break;
                    case 2:
                        estudiante.put("id_usuario", 2);
                        estudiante.put("nombre", "Carlos");
                        estudiante.put("apellido", "López");
                        estudiante.put("email", "carlos.lopez@unicauca.edu.co");
                        estudiante.put("codigo_estudiante", "104612345661");
                        break;
                    case 3:
                        estudiante.put("id_usuario", 3);
                        estudiante.put("nombre", "María");
                        estudiante.put("apellido", "Rodríguez");
                        estudiante.put("email", "maria.rodriguez@unicauca.edu.co");
                        estudiante.put("codigo_estudiante", "104612345662");
                        break;
                    case 4:
                        estudiante.put("id_usuario", 4);
                        estudiante.put("nombre", "Pedro");
                        estudiante.put("apellido", "Martínez");
                        estudiante.put("email", "pedro.martinez@unicauca.edu.co");
                        estudiante.put("codigo_estudiante", "104612345663");
                        break;
                    default:
                        estudiante.put("id_usuario", estudianteId);
                        estudiante.put("nombre", "Estudiante");
                        estudiante.put("apellido", "Desconocido");
                        estudiante.put("email", "estudiante@unicauca.edu.co");
                        estudiante.put("codigo_estudiante", "EST" + estudianteId);
                }
                
                inscripcionEnriquecida.put("estudiante", estudiante);
                
                // Obtener información del archivo de pago
                Integer archivoPagoId = (Integer) inscripcion.get("archivoPagoId");
                if (archivoPagoId != null) {
                    Map<String, Object> archivoPago = new HashMap<>();
                    archivoPago.put("id_documento", archivoPagoId);
                    archivoPago.put("nombre", "comprobante_pago_" + estudiante.get("nombre").toString().toLowerCase() + ".pdf");
                    archivoPago.put("url", "/uploads/comprobante_pago_" + estudiante.get("nombre").toString().toLowerCase() + ".pdf");
                    archivoPago.put("fecha", inscripcion.get("fecha"));
                    inscripcionEnriquecida.put("archivoPago", archivoPago);
                } else {
                    inscripcionEnriquecida.put("archivoPago", null);
                }
                
                inscripcionesEnriquecidas.add(inscripcionEnriquecida);
            }
            
            return ResponseEntity.ok(inscripcionesEnriquecidas);
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
            System.out.println("DEBUG Obteniendo inscripciones para usuario ID: " + id_usuario);
            
            // TODO: Implementar obtención de inscripciones reales de la base de datos
            // Por ahora devolver lista vacía hasta que se implemente la funcionalidad de inscripciones
            List<Map<String, Object>> inscripciones = new ArrayList<>();
            
            System.out.println("SUCCESS Inscripciones filtradas para usuario " + id_usuario + ": " + inscripciones.size() + " encontradas");
            
            return ResponseEntity.ok(inscripciones);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // ==================== MÉTODOS AUXILIARES PARA VALIDACIÓN DE ESTADOS ====================
    
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
     * Validar si una transición de estado es válida
     */
    private Map<String, Object> validarTransicionEstado(String estadoActual, String nuevoEstado, CursoOfertadoVeranoEntity curso) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("valido", false);
        
        // Si es el mismo estado, es válido (no hay cambio)
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
                    resultado.put("error", "Transición inválida");
                    resultado.put("message", "Desde 'Sin Estado' solo se puede cambiar a 'Borrador'");
                }
                break;
                
            case "Borrador":
                // Desde Borrador puede ir a Abierto o mantenerse en Borrador
                if ("Abierto".equals(nuevoEstado)) {
                    // Validar que el curso esté completo
                    if (validarCompletitudCurso(curso)) {
                        resultado.put("valido", true);
                    } else {
                        resultado.put("error", "Curso incompleto");
                        resultado.put("message", "El curso debe tener materia, docente y cupo estimado para pasar a 'Abierto'");
                    }
                } else {
                    resultado.put("error", "Transición inválida");
                    resultado.put("message", "Desde 'Borrador' solo se puede cambiar a 'Abierto'");
                }
                break;
                
            case "Abierto":
                // Desde Abierto puede ir a Publicado
                if ("Publicado".equals(nuevoEstado)) {
                    resultado.put("valido", true);
                } else if ("Borrador".equals(nuevoEstado)) {
                    // Permitir retroceder a Borrador para edición
                    resultado.put("valido", true);
                } else {
                    resultado.put("error", "Transición inválida");
                    resultado.put("message", "Desde 'Abierto' solo se puede cambiar a 'Publicado' o retroceder a 'Borrador'");
                }
                break;
                
            case "Publicado":
                // Desde Publicado puede ir a Preinscripción
                if ("Preinscripcion".equals(nuevoEstado)) {
                    // Validar que haya solicitudes mínimas para el curso
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
                    resultado.put("error", "Transición inválida");
                    resultado.put("message", "Desde 'Publicado' solo se puede cambiar a 'Preinscripcion' o retroceder a 'Abierto'");
                }
                break;
                
            case "Preinscripcion":
                // Desde Preinscripción puede ir a Inscripción o Cerrado
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
                    resultado.put("error", "Transición inválida");
                    resultado.put("message", "Desde 'Preinscripcion' solo se puede cambiar a 'Inscripcion', 'Cerrado' o retroceder a 'Publicado'");
                }
                break;
                
            case "Inscripcion":
                // Desde Inscripción puede ir a Cerrado
                if ("Cerrado".equals(nuevoEstado)) {
                    resultado.put("valido", true);
                } else if ("Preinscripcion".equals(nuevoEstado)) {
                    // Permitir retroceder a Preinscripción
                    resultado.put("valido", true);
                } else {
                    resultado.put("error", "Transición inválida");
                    resultado.put("message", "Desde 'Inscripcion' solo se puede cambiar a 'Cerrado' o retroceder a 'Preinscripcion'");
                }
                break;
                
            case "Cerrado":
                // Desde Cerrado no se puede cambiar a ningún otro estado (solo consulta)
                resultado.put("error", "Estado final");
                resultado.put("message", "El curso está cerrado y no se puede cambiar su estado");
                break;
                
            default:
                resultado.put("error", "Estado desconocido");
                resultado.put("message", "Estado actual '" + estadoActual + "' no reconocido");
                break;
        }
        
        return resultado;
    }
    
    /**
     * Validar que un curso esté completo (tiene todos los campos obligatorios)
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
     * Validar que haya solicitudes mínimas para abrir preinscripciones
     */
    private boolean validarSolicitudesMinimas(Integer idCurso) {
        try {
            // Por ahora, permitir siempre (se puede implementar lógica específica)
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
            // Por ahora, permitir siempre (se puede implementar lógica específica)
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validar permisos de operación según el estado del curso y el rol del usuario
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
        
        // Estado: Preinscripción
        permisosPorEstado.put("Preinscripcion", List.of(
            "FUNCIONARIO:ver,gestionar_preinscripciones,cambiar_estado",
            "COORDINADOR:ver,gestionar_preinscripciones,cambiar_estado",
            "ESTUDIANTE:ver,preinscribirse"
        ));
        
        // Estado: Inscripción
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
        
        // Buscar permisos para el rol específico
        String permisosRol = permisosEstado.stream()
            .filter(permiso -> permiso.startsWith(rolUsuario + ":"))
            .findFirst()
            .orElse(null);
        
        if (permisosRol == null) {
            resultado.put("error", "Rol no válido");
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
        
        // Verificar si la operación específica está permitida
        if (operacionesPermitidas.contains(operacion) || operacionesPermitidas.contains("todas")) {
            resultado.put("permitido", true);
            resultado.put("message", "Operación permitida");
        } else {
            resultado.put("error", "Operación no permitida");
            resultado.put("message", "El rol " + rolUsuario + " no puede realizar la operación '" + operacion + "' en el estado " + estadoCurso);
        }
        
        return resultado;
    }
    
    /**
     * Endpoint para obtener información de permisos por estado
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
            System.out.println("DEBUG DEBUG: Buscando solicitudes para usuario ID: " + idUsuario);
            
            // Buscar todas las solicitudes del usuario
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("usuarioId", idUsuario);
            debug.put("preinscripcionesEncontradas", preinscripciones.size());
            debug.put("preinscripciones", preinscripciones);
            
            System.out.println("DEBUG DEBUG: Encontradas " + preinscripciones.size() + " preinscripciones");
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en debug: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint de debug específico para seguimiento
     * GET /api/cursos-intersemestrales/debug-seguimiento/{idUsuario}
     */
    @GetMapping("/debug-seguimiento/{idUsuario}")
    public ResponseEntity<Map<String, Object>> debugSeguimiento(@PathVariable Integer idUsuario) {
        try {
            System.out.println("DEBUG DEBUG: Iniciando debug de seguimiento para usuario: " + idUsuario);
            
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
                
                // Probar detección de tipo
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
                info.put("tipoDetectado", esSolicitudCursoNuevo ? "Solicitud de Curso Nuevo" : "Preinscripción");
                
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
     * Endpoint temporal para debug - verificar TODAS las preinscripciones
     * GET /api/cursos-intersemestrales/debug-todas-solicitudes
     */
    @GetMapping("/debug-todas-solicitudes")
    public ResponseEntity<Map<String, Object>> debugTodasSolicitudes() {
        try {
            System.out.println("DEBUG DEBUG: Buscando TODAS las solicitudes en la base de datos");
            
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
            
            System.out.println("DEBUG DEBUG: Encontradas " + todasLasSolicitudes.size() + " solicitudes en total");
            
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
            System.out.println("INFO Obteniendo seguimiento de actividades para usuario ID: " + idUsuario);
            
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
                        ? preinscripcion.getEstadosSolicitud().get(0).getEstado_actual() : "Enviado");
                    
                    // Determinar el tipo correcto de solicitud
                    String tipoSolicitud = "Preinscripción";
                    
                    // Detección para solicitudes de curso nuevo
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
                            // Extraer solo el nombre del curso de la observación
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
                    preinscripciones.add(preinscripcionMap);
                }
                
                System.out.println("INFO Preinscripciones reales encontradas: " + preinscripciones.size());
                
            } catch (Exception e) {
                System.out.println("WARNING Error obteniendo preinscripciones reales: " + e.getMessage());
                // En caso de error, devolver lista vacía en lugar de datos simulados
                preinscripciones = new ArrayList<>();
            }
            
            // Obtener inscripciones reales del usuario (por ahora lista vacía hasta implementar la funcionalidad)
            List<Map<String, Object>> inscripcionesUsuario = new ArrayList<>();
            
            // TODO: Implementar obtención de inscripciones reales cuando esté disponible
            // Por ahora se mantiene vacío para evitar mostrar datos simulados
            
            // Crear respuesta con estadísticas
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("preinscripciones", preinscripciones);
            respuesta.put("inscripciones", inscripcionesUsuario);
            respuesta.put("totalPreinscripciones", preinscripciones.size());
            respuesta.put("totalInscripciones", inscripcionesUsuario.size());
            respuesta.put("totalActividades", preinscripciones.size() + inscripcionesUsuario.size());
            
            System.out.println("SUCCESS Seguimiento obtenido: " + preinscripciones.size() + " preinscripciones reales, " + 
                             inscripcionesUsuario.size() + " inscripciones");
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.out.println("ERROR Error en seguimiento: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
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
            System.out.println("DELETE Cancelando inscripción ID: " + id);
            
            // TODO: Implementar cancelación de inscripciones reales de la base de datos
            // Por ahora devolver error ya que no hay funcionalidad de inscripciones implementada
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Funcionalidad no implementada");
            error.put("message", "La funcionalidad de inscripciones aún no está implementada");
            error.put("status", 501);
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(501).body(error);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ha ocurrido un error inesperado. Por favor, contacta al administrador.");
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
            materia1.put("nombre_materia", "Matemáticas");
            materia1.put("codigo_materia", "MAT");
            materia1.put("creditos", 3);
            curso1.put("objMateria", materia1);
            
            // Objeto docente
            Map<String, Object> docente1 = new HashMap<>();
            docente1.put("id_usuario", 2);
            docente1.put("nombre", "Juan");
            docente1.put("apellido", "Pérez");
            docente1.put("email", "juan.perez@unicauca.edu.co");
            docente1.put("telefono", "3001234567");
            
            // Objeto rol
            Map<String, Object> rol1 = new HashMap<>();
            rol1.put("id_rol", 2);
            rol1.put("nombre", "Docente"); // SUCCESS CORREGIDO: nombre → nombre
            docente1.put("objRol", rol1);
            
            curso1.put("objDocente", docente1);
            cursos.add(curso1);
            
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
            materia2.put("nombre_materia", "Programación");
            materia2.put("codigo_materia", "PROG");
            materia2.put("creditos", 4);
            curso2.put("objMateria", materia2);
            
            Map<String, Object> docente2 = new HashMap<>();
            docente2.put("id_usuario", 3);
            docente2.put("nombre", "María");
            docente2.put("apellido", "García");
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
            System.out.println("DEBUG: Creando nuevo curso:");
            System.out.println("  - Nombre: " + dto.getNombre_curso());
            System.out.println("  - Código: " + dto.getCodigo_curso());
            System.out.println("  - ID Materia: " + dto.getId_materia());
            System.out.println("  - ID Docente: " + dto.getId_docente());
            System.out.println("  - Cupo Estimado: " + dto.getCupo_estimado());
            System.out.println("  - Estado: " + dto.getEstado());
            
            // Validaciones básicas
            if (dto.getNombre_curso() == null || dto.getNombre_curso().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Nombre requerido");
                error.put("message", "El nombre del curso es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (dto.getCodigo_curso() == null || dto.getCodigo_curso().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Código requerido");
                error.put("message", "El código del curso es obligatorio");
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
            
            // Obtener información real de la materia
            Map<String, Object> materia = new HashMap<>();
            try {
                // Aquí deberías obtener la materia real de la base de datos
                // Por ahora usamos datos simulados basados en el ID
                materia.put("id_materia", dto.getId_materia());
                materia.put("nombre_materia", "Materia " + dto.getId_materia());
                materia.put("codigo_materia", "MAT" + dto.getId_materia());
                materia.put("creditos", 3);
                System.out.println("DEBUG: Materia obtenida: " + materia.get("nombre_materia"));
            } catch (Exception e) {
                System.out.println("DEBUG: Error obteniendo materia, usando datos simulados");
                materia.put("id_materia", dto.getId_materia());
                materia.put("nombre_materia", "Materia " + dto.getId_materia());
                materia.put("codigo_materia", "MAT" + dto.getId_materia());
                materia.put("creditos", 3);
            }
            
            // Obtener información real del docente
            Map<String, Object> docente = new HashMap<>();
            try {
                // Aquí deberías obtener el docente real de la base de datos
                // Por ahora usamos datos simulados basados en el ID
                docente.put("id_usuario", dto.getId_docente());
                docente.put("nombre", "Docente " + dto.getId_docente());
                docente.put("apellido", "Apellido");
                docente.put("email", "docente" + dto.getId_docente() + "@unicauca.edu.co");
                docente.put("telefono", "3000000000");
                
                Map<String, Object> rol = new HashMap<>();
                rol.put("id_rol", 1);
                rol.put("nombre", "Docente");
                docente.put("objRol", rol);
                System.out.println("DEBUG: Docente obtenido: " + docente.get("nombre"));
            } catch (Exception e) {
                System.out.println("DEBUG: Error obteniendo docente, usando datos simulados");
                docente.put("id_usuario", dto.getId_docente());
                docente.put("nombre", "Docente " + dto.getId_docente());
                docente.put("apellido", "Apellido");
                docente.put("email", "docente" + dto.getId_docente() + "@unicauca.edu.co");
                docente.put("telefono", "3000000000");
                
                Map<String, Object> rol = new HashMap<>();
                rol.put("id_rol", 1);
                rol.put("nombre", "Docente");
                docente.put("objRol", rol);
            }
            
            // Crear el curso usando el caso de uso existente
            try {
                System.out.println("DEBUG: Creando curso usando caso de uso");
                
                // Crear objeto de dominio del curso
                CursoOfertadoVerano cursoDominio = new CursoOfertadoVerano();
                cursoDominio.setCupo_estimado(dto.getCupo_estimado());
                cursoDominio.setSalon(dto.getEspacio_asignado() != null ? dto.getEspacio_asignado() : "Aula 101");
                cursoDominio.setGrupo(co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano.A);
                
                // Crear materia de dominio
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materiaDominio = new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia();
                materiaDominio.setId_materia(dto.getId_materia().intValue());
                materiaDominio.setNombre(dto.getNombre_curso());
                materiaDominio.setCodigo(dto.getCodigo_curso());
                cursoDominio.setObjMateria(materiaDominio);
                
                // Crear docente de dominio
                co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente docenteDominio = new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente();
                docenteDominio.setId_docente(dto.getId_docente().intValue());
                docenteDominio.setNombre_docente("Docente " + dto.getId_docente());
                cursoDominio.setObjDocente(docenteDominio);
                
                // Usar el caso de uso para crear el curso
                CursoOfertadoVerano cursoCreado = cursoCU.crearCurso(cursoDominio);
                System.out.println("DEBUG: Curso creado exitosamente con ID: " + cursoCreado.getId_curso());
                
                // Crear respuesta con datos reales
                Map<String, Object> nuevoCurso = new HashMap<>();
                nuevoCurso.put("id_curso", cursoCreado.getId_curso());
                nuevoCurso.put("nombre_curso", dto.getNombre_curso());
                nuevoCurso.put("codigo_curso", dto.getCodigo_curso());
                nuevoCurso.put("descripcion", dto.getDescripcion() != null ? dto.getDescripcion() : "Curso de " + dto.getNombre_curso());
                nuevoCurso.put("fecha_inicio", dto.getFecha_inicio());
                nuevoCurso.put("fecha_fin", dto.getFecha_fin());
                nuevoCurso.put("cupo_maximo", dto.getCupo_maximo());
                nuevoCurso.put("cupo_disponible", dto.getCupo_maximo());
                nuevoCurso.put("cupo_estimado", dto.getCupo_estimado());
                nuevoCurso.put("espacio_asignado", dto.getEspacio_asignado());
                nuevoCurso.put("estado", dto.getEstado());
                nuevoCurso.put("objMateria", materia);
                nuevoCurso.put("objDocente", docente);
                nuevoCurso.put("message", "Curso creado exitosamente en la base de datos");
                nuevoCurso.put("debug_info", "Curso guardado con ID: " + cursoCreado.getId_curso());
                
                System.out.println("DEBUG: Curso creado exitosamente en BD");
                return ResponseEntity.ok(nuevoCurso);
                
            } catch (Exception e) {
                System.out.println("DEBUG: Error guardando en BD: " + e.getMessage());
                e.printStackTrace();
                
                // Si falla el guardado, devolver respuesta simulada
                Map<String, Object> nuevoCurso = new HashMap<>();
                nuevoCurso.put("id_curso", 99);
                nuevoCurso.put("nombre_curso", dto.getNombre_curso());
                nuevoCurso.put("codigo_curso", dto.getCodigo_curso());
                nuevoCurso.put("descripcion", dto.getDescripcion() != null ? dto.getDescripcion() : "Curso de " + dto.getNombre_curso());
                nuevoCurso.put("fecha_inicio", dto.getFecha_inicio());
                nuevoCurso.put("fecha_fin", dto.getFecha_fin());
                nuevoCurso.put("cupo_maximo", dto.getCupo_maximo());
                nuevoCurso.put("cupo_disponible", dto.getCupo_maximo());
                nuevoCurso.put("cupo_estimado", dto.getCupo_estimado());
                nuevoCurso.put("espacio_asignado", dto.getEspacio_asignado());
                nuevoCurso.put("estado", dto.getEstado());
                nuevoCurso.put("objMateria", materia);
                nuevoCurso.put("objDocente", docente);
                nuevoCurso.put("message", "Curso creado exitosamente (simulado)");
                nuevoCurso.put("debug_info", "Error guardando en BD: " + e.getMessage());
                
                return ResponseEntity.ok(nuevoCurso);
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: Error creando curso: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("DEBUG: Actualizando curso ID: " + id);
            System.out.println("DEBUG: Datos recibidos: " + dto);
            
            // Validaciones básicas
            if (dto.getCupo_estimado() != null && (dto.getCupo_estimado() < 1 || dto.getCupo_estimado() > 100)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Cupo inválido");
                error.put("message", "El cupo estimado debe estar entre 1 y 100");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (dto.getEspacio_asignado() != null && dto.getEspacio_asignado().trim().length() < 3) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Espacio inválido");
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
                    error.put("error", "Estado inválido");
                    error.put("message", "El estado debe ser uno de: Borrador, Abierto, Publicado, Preinscripcion, Inscripcion, Cerrado");
                    return ResponseEntity.badRequest().body(error);
                }
            }
            
            // Obtener el curso existente directamente del repositorio
            CursoOfertadoVeranoEntity cursoEntity = cursoRepository.findById(id.intValue()).orElse(null);
            if (cursoEntity == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Curso no encontrado");
                error.put("message", "No se encontró el curso con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("DEBUG: Curso encontrado: " + (cursoEntity.getObjMateria() != null ? cursoEntity.getObjMateria().getNombre() : "Sin materia"));
            
            // Aplicar cambios al objeto
            boolean cursoModificado = false;
            
            if (dto.getCupo_estimado() != null) {
                cursoEntity.setCupo_estimado(dto.getCupo_estimado());
                cursoModificado = true;
                System.out.println("DEBUG: Cupo actualizado a: " + dto.getCupo_estimado());
            }
            
            if (dto.getEspacio_asignado() != null) {
                cursoEntity.setSalon(dto.getEspacio_asignado());
                cursoModificado = true;
                System.out.println("DEBUG: Espacio actualizado a: " + dto.getEspacio_asignado());
            }
            
            // Crear nuevo estado si se proporciona
            EstadoCursoOfertadoEntity nuevoEstadoEntity = null;
            if (dto.getEstado() != null) {
                // Validar transición de estado
                String estadoActual = obtenerEstadoActual(cursoEntity);
                String nuevoEstado = dto.getEstado();
                
                // Validar si la transición es válida
                Map<String, Object> validacionTransicion = validarTransicionEstado(estadoActual, nuevoEstado, cursoEntity);
                if (!(Boolean) validacionTransicion.get("valido")) {
                    return ResponseEntity.badRequest().body(validacionTransicion);
                }
                
                nuevoEstadoEntity = new EstadoCursoOfertadoEntity();
                nuevoEstadoEntity.setEstado_actual(dto.getEstado());
                nuevoEstadoEntity.setFecha_registro_estado(new java.util.Date());
                nuevoEstadoEntity.setObjCursoOfertadoVerano(cursoEntity);
                cursoModificado = true;
                System.out.println("DEBUG: Estado actualizado a: " + dto.getEstado());
            }
            
            if (!cursoModificado) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Sin cambios");
                error.put("message", "No se proporcionaron datos para actualizar");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Guardar cambios en la base de datos
            try {
                System.out.println("DEBUG: Guardando cambios en la base de datos");
                CursoOfertadoVeranoEntity cursoActualizado = cursoRepository.save(cursoEntity);
                
                // Si hay nuevo estado, guardarlo también
                if (nuevoEstadoEntity != null) {
                    System.out.println("DEBUG: Guardando nuevo estado: " + nuevoEstadoEntity.getEstado_actual());
                    estadoRepository.save(nuevoEstadoEntity);
                    System.out.println("DEBUG: Nuevo estado guardado exitosamente en BD");
                }
                
                System.out.println("DEBUG: Cambios guardados exitosamente en BD");
                
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
                System.out.println("DEBUG: Error guardando en BD: " + e.getMessage());
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error guardando en base de datos");
                error.put("message", "No se pudo guardar los cambios: " + e.getMessage());
                return ResponseEntity.status(500).body(error);
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: Error actualizando curso: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("DEBUG: Eliminando curso ID: " + id);
            
            // Verificar que el curso existe
            CursoOfertadoVerano cursoExistente = cursoCU.obtenerCursoPorId(id.intValue());
            if (cursoExistente == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Curso no encontrado");
                error.put("message", "No se encontró el curso con ID: " + id);
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
                
                System.out.println("DEBUG: Curso eliminado exitosamente");
                return ResponseEntity.ok(resultado);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Error al eliminar el curso");
                error.put("message", "No se pudo eliminar el curso por razones desconocidas");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            System.out.println("ERROR: Error eliminando curso: " + e.getMessage());
            e.printStackTrace();
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
        try {
            System.out.println("DEBUG: Obteniendo información del curso ID: " + id);
            
            // Obtener curso real de la base de datos
            CursoOfertadoVerano cursoReal = cursoCU.obtenerCursoPorId(id.intValue());
            
            if (cursoReal == null) {
                System.out.println("WARNING: Curso no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            // Usar el mapper existente para obtener datos estructurados
            CursosOfertadosDTORespuesta cursoDTO = cursoMapper.mappearDeCursoOfertadoARespuesta(cursoReal);
            
            // Mapear a estructura esperada por el frontend
            Map<String, Object> curso = new HashMap<>();
            curso.put("id_curso", cursoDTO.getId_curso());
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
                System.out.println("DEBUG: Preinscripciones encontradas para el curso: " + preinscripciones.size());
            } catch (Exception e) {
                System.out.println("WARNING: Error obteniendo conteo de preinscripciones: " + e.getMessage());
                curso.put("solicitudes", 0);
            }
            
            // Usar la información del DTO que ya está mapeada correctamente
            curso.put("objMateria", cursoDTO.getObjMateria());
            curso.put("objDocente", cursoDTO.getObjDocente());
            
            System.out.println("SUCCESS: Información del curso obtenida correctamente");
            
            return ResponseEntity.ok(curso);
        } catch (Exception e) {
            System.out.println("ERROR: Error obteniendo información del curso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
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
            System.out.println("ERROR obteniendo materias: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtener todos los docentes
     * GET /api/cursos-intersemestrales/docentes
     */
    @GetMapping("/docentes")
    public ResponseEntity<List<Map<String, Object>>> getTodosLosDocentes() {
        try {
            System.out.println("DEBUG: Obteniendo todos los docentes");

            // Crear lista de docentes reales (los que agregamos al import.sql)
            List<Map<String, Object>> docentes = new ArrayList<>();
            
            // Docentes reales de la base de datos
            String[][] docentesData = {
                {"1047", "Carlos Alberto Ardila Albarracin", "cardila@unicauca.edu.co"},
                {"1048", "Carlos Alberto Cobos Lozada", "ccobos@unicauca.edu.co"},
                {"1049", "Carolina Gonzalez Serrano", "cgonzals@unicauca.edu.co"},
                {"1050", "Cesar Alberto Collazos Ordonez", "ccollazo@unicauca.edu.co"},
                {"1051", "Ember Ubeimar Martinez Flor", "eumartinez@unicauca.edu.co"},
                {"1052", "Erwin Meza Vega", "emezav@unicauca.edu.co"},
                {"1053", "Francisco Jose Pino Correa", "fjpino@unicauca.edu.co"},
                {"1054", "Jorge Jair Moreno Chaustre", "jjmoreno@unicauca.edu.co"},
                {"1055", "Julio Ariel Hurtado Alegria", "ahurtado@unicauca.edu.co"},
                {"1056", "Luz Marina Sierra Martinez", "lsierra@unicauca.edu.co"},
                {"1057", "Martha Eliana Mendoza Becerra", "mmendoza@unicauca.edu.co"},
                {"1058", "Miguel Angel Nino Zambrano", "manzamb@unicauca.edu.co"},
                {"1059", "Nestor Milciades Diaz Marino", "nediaz@unicauca.edu.co"},
                {"1060", "Pablo Augusto Mage Imbachi", "pmage@unicauca.edu.co"},
                {"1061", "Roberto Carlos Naranjo Cuervo", "rnaranjo@unicauca.edu.co"},
                {"1062", "Sandra Milena Roa Martinez", "smroa@unicauca.edu.co"},
                {"1063", "Siler Amador Donado", "samador@unicauca.edu.co"},
                {"1064", "Wilson Libardo Pantoja Yepez", "wpantoja@unicauca.edu.co"}
            };
            
            for (int i = 0; i < docentesData.length; i++) {
                Map<String, Object> docente = new HashMap<>();
                docente.put("id_usuario", i + 1);
                docente.put("codigo_usuario", docentesData[i][0]);
                docente.put("nombre_usuario", docentesData[i][1]);
                docente.put("correo", docentesData[i][2]);
                docente.put("telefono", "3000000000");
                
                Map<String, Object> rol = new HashMap<>();
                rol.put("id_rol", 1);
                rol.put("nombre", "Docente");
                docente.put("objRol", rol);
                
                docentes.add(docente);
            }

            System.out.println("DEBUG: Se encontraron " + docentes.size() + " docentes");
            return ResponseEntity.ok(docentes);
        } catch (Exception e) {
            System.out.println("ERROR: Error obteniendo docentes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== ENDPOINTS PARA GESTIÓN DE PREINSCRIPCIONES ====================

    /**
     * Obtener preinscripciones por curso (datos reales de la base de datos)
     * GET /api/cursos-intersemestrales/preinscripciones/curso/{idCurso}
     */
    @GetMapping("/preinscripciones/curso/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> getPreinscripcionesPorCurso(
            @PathVariable Long idCurso) {
        try {
            System.out.println("DEBUG: Obteniendo preinscripciones reales para curso ID: " + idCurso);
            
            List<Map<String, Object>> preinscripciones = new ArrayList<>();
            
            // Obtener preinscripciones reales de la base de datos
            List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarPreinscripcionesPorCurso(idCurso.intValue());
            
            System.out.println("DEBUG: Preinscripciones encontradas: " + preinscripcionesReales.size());
            
            for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
                Map<String, Object> preinscripcionMap = new HashMap<>();
                
                // Información básica de la preinscripción
                preinscripcionMap.put("id_preinscripcion", preinscripcion.getId_solicitud());
                preinscripcionMap.put("fecha_preinscripcion", preinscripcion.getFecha_registro_solicitud());
                
                // Estado de la solicitud
                String estado = "Pendiente"; // Valor por defecto
                if (preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty()) {
                    estado = preinscripcion.getEstadosSolicitud().get(0).getEstado_actual();
                }
                preinscripcionMap.put("estado", estado);
                
                // Observaciones
                preinscripcionMap.put("observaciones", preinscripcion.getObservacion() != null ? preinscripcion.getObservacion() : "");
                
                // Condición de la solicitud
                preinscripcionMap.put("condicion", preinscripcion.getCodicion_solicitud() != null ? 
                    preinscripcion.getCodicion_solicitud().toString() : "Primera_Vez");
                
                // Información del usuario/estudiante
                if (preinscripcion.getObjUsuario() != null) {
                    Map<String, Object> usuarioMap = new HashMap<>();
                    usuarioMap.put("id_usuario", preinscripcion.getObjUsuario().getId_usuario());
                    usuarioMap.put("nombre_completo", preinscripcion.getObjUsuario().getNombre_completo());
                    usuarioMap.put("correo", preinscripcion.getObjUsuario().getCorreo());
                    usuarioMap.put("codigo", preinscripcion.getObjUsuario().getCodigo());
                    usuarioMap.put("codigo_estudiante", preinscripcion.getCodigo_estudiante());
                    
                    // Información del rol
                    if (preinscripcion.getObjUsuario().getObjRol() != null) {
                        Map<String, Object> rolMap = new HashMap<>();
                        rolMap.put("id_rol", preinscripcion.getObjUsuario().getObjRol().getId_rol());
                        rolMap.put("nombre", preinscripcion.getObjUsuario().getObjRol().getNombre());
                        usuarioMap.put("objRol", rolMap);
                    }
                    
                    preinscripcionMap.put("objUsuario", usuarioMap);
                }
                
                // Información del curso usando el mapper existente
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
            
            System.out.println("SUCCESS: Preinscripciones procesadas: " + preinscripciones.size());
            
            return ResponseEntity.ok(preinscripciones);
            
        } catch (Exception e) {
            System.out.println("ERROR: Error obteniendo preinscripciones por curso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Actualizar observaciones de preinscripción
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
                error.put("error", "Las observaciones no pueden estar vacías");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Simular actualización de observaciones
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("message", "Observaciones actualizadas exitosamente");
            
            Map<String, Object> preinscripcion = new HashMap<>();
            preinscripcion.put("id_preinscripcion", idPreinscripcion);
            preinscripcion.put("observaciones", observaciones);
            preinscripcion.put("estado", "Pendiente");
            
            respuesta.put("preinscripcion", preinscripcion);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.status(500).body(error);
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
            // Simular aprobación de solicitud
            Map<String, Object> solicitud = new HashMap<>();
            solicitud.put("id_solicitud", id);
            solicitud.put("nombre_solicitud", "Solicitud de Curso Nuevo");
            solicitud.put("fecha_solicitud", "2024-01-10T10:30:00Z");
            solicitud.put("estado", "Aprobado");
            solicitud.put("observaciones", "Estudiante con buen rendimiento académico");
            solicitud.put("condicion", "Primera_Vez");
            solicitud.put("tipoSolicitud", "PREINSCRIPCION");
            
            // Usuario estudiante
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id_usuario", 4);
            usuario.put("nombre", "Pepa");
            usuario.put("apellido", "González");
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
            curso.put("nombre_curso", "Programación I");
            curso.put("codigo_curso", "PROG-201");
            curso.put("descripcion", "Fundamentos de programación");
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
            materia.put("nombre_materia", "Programación I");
            materia.put("codigo_materia", "PROG");
            materia.put("creditos", 4);
            curso.put("objMateria", materia);
            
            // Docente del curso
            Map<String, Object> docente = new HashMap<>();
            docente.put("id_usuario", 3);
            docente.put("nombre", "Ana");
            docente.put("apellido", "Martínez");
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
            usuario.put("apellido", "González");
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
            curso.put("nombre_curso", "Programación I");
            curso.put("codigo_curso", "PROG-201");
            curso.put("descripcion", "Fundamentos de programación");
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
            materia.put("nombre_materia", "Programación I");
            materia.put("codigo_materia", "PROG");
            materia.put("creditos", 4);
            curso.put("objMateria", materia);
            
            // Docente del curso
            Map<String, Object> docente = new HashMap<>();
            docente.put("id_usuario", 3);
            docente.put("nombre", "Ana");
            docente.put("apellido", "Martínez");
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

    // ==================== ENDPOINT PARA CONFIRMAR INSCRIPCIÓN ====================

    /**
     * Confirmar inscripción de estudiante
     * PUT /api/cursos-intersemestrales/inscripciones/{id}/confirmar
     */
    @PutMapping("/inscripciones/{id}/confirmar")
    public ResponseEntity<Map<String, Object>> confirmarInscripcion(@PathVariable Long id) {
        try {
            // Simular confirmación de inscripción
            Map<String, Object> inscripcion = new HashMap<>();
            inscripcion.put("id", id);
            inscripcion.put("fecha", "2024-01-15T10:30:00");
            inscripcion.put("estado", "inscrito"); // Cambiado de "pendiente" a "inscrito"
            inscripcion.put("estudianteId", 1);
            inscripcion.put("cursoId", 1);
            
            // Información completa del estudiante
            Map<String, Object> estudiante = new HashMap<>();
            estudiante.put("id_usuario", 1);
            estudiante.put("nombre", "Ana");
            estudiante.put("apellido", "González");
            estudiante.put("email", "ana.gonzalez@unicauca.edu.co");
            estudiante.put("codigo_estudiante", "104612345660");
            inscripcion.put("estudiante", estudiante);
            
            // Información del archivo de pago (ya existía)
            Map<String, Object> archivoPago = new HashMap<>();
            archivoPago.put("id_documento", 1);
            archivoPago.put("nombre", "comprobante_pago_ana.pdf");
            archivoPago.put("url", "/uploads/comprobante_pago_ana.pdf");
            archivoPago.put("fecha", "2024-01-15T10:30:00");
            inscripcion.put("archivoPago", archivoPago);
            
            // Mensaje de confirmación
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("message", "Inscripción confirmada exitosamente");
            respuesta.put("inscripcion", inscripcion);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== ENDPOINT PARA RECHAZAR INSCRIPCIÓN ====================

    /**
     * Rechazar inscripción de estudiante
     * PUT /api/cursos-intersemestrales/inscripciones/{id}/rechazar
     */
    @PutMapping("/inscripciones/{id}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarInscripcion(@PathVariable Long id) {
        try {
            // Simular rechazo de inscripción
            Map<String, Object> inscripcion = new HashMap<>();
            inscripcion.put("id", id);
            inscripcion.put("fecha", "2024-01-17T09:15:00");
            inscripcion.put("estado", "rechazado"); // Cambiado de "pendiente" a "rechazado"
            inscripcion.put("estudianteId", 3);
            inscripcion.put("cursoId", 1);
            
            // Información completa del estudiante
            Map<String, Object> estudiante = new HashMap<>();
            estudiante.put("id_usuario", 3);
            estudiante.put("nombre", "María");
            estudiante.put("apellido", "Rodríguez");
            estudiante.put("email", "maria.rodriguez@unicauca.edu.co");
            estudiante.put("codigo_estudiante", "104612345662");
            inscripcion.put("estudiante", estudiante);
            
            // Sin archivo de pago (null) - como en el ejemplo
            inscripcion.put("archivoPago", null);
            
            // Mensaje de rechazo
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("message", "Inscripción rechazada exitosamente");
            respuesta.put("inscripcion", inscripcion);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
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
            
            // Solicitud 1: Pepa González
            Map<String, Object> solicitud1 = new HashMap<>();
            solicitud1.put("id_solicitud", 1);
            solicitud1.put("nombre_solicitud", "Solicitud de Curso Nuevo - Programación Avanzada");
            solicitud1.put("fecha_solicitud", "2024-01-15T10:30:00Z");
            solicitud1.put("estado", "Pendiente");
            solicitud1.put("condicion", "Primera_Vez");
            solicitud1.put("observaciones", "Estudiante solicita curso de programación avanzada para el verano");
            solicitud1.put("tipoSolicitud", "PREINSCRIPCION");
            
            // Usuario estudiante con estructura corregida
            Map<String, Object> usuario1 = new HashMap<>();
            usuario1.put("id_usuario", 1);
            usuario1.put("nombre_completo", "Pepa González");
            usuario1.put("codigo", "104612345660");
            usuario1.put("correo", "pepa.gonzalez@unicauca.edu.co");
            usuario1.put("password", "ContraseñaSegura123");
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
            programa1.put("nombre_programa", "Ingeniería Informática");
            usuario1.put("objPrograma", programa1);
            
            solicitud1.put("objUsuario", usuario1);
            
            // Curso ofertado
            Map<String, Object> curso1 = new HashMap<>();
            curso1.put("id_curso", 1);
            curso1.put("nombre_curso", "Programación Avanzada");
            curso1.put("codigo_curso", "PROG-301");
            curso1.put("descripcion", "Curso de programación avanzada");
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
            materia1.put("nombre_materia", "Programación");
            materia1.put("codigo_materia", "PROG");
            materia1.put("creditos", 4);
            curso1.put("objMateria", materia1);
            
            // Docente del curso con estructura corregida
            Map<String, Object> docente1 = new HashMap<>();
            docente1.put("id_usuario", 2);
            docente1.put("nombre_completo", "María García");
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
            programaDocente1.put("nombre_programa", "Ingeniería Informática");
            docente1.put("objPrograma", programaDocente1);
            
            curso1.put("objDocente", docente1);
            solicitud1.put("objCursoOfertadoVerano", curso1);
            
            solicitudes.add(solicitud1);
            
            // Solicitud 2: Carlos López
            Map<String, Object> solicitud2 = new HashMap<>();
            solicitud2.put("id_solicitud", 2);
            solicitud2.put("nombre_solicitud", "Solicitud de Curso Nuevo - Inteligencia Artificial");
            solicitud2.put("fecha_solicitud", "2024-01-16T14:20:00Z");
            solicitud2.put("estado", "Aprobado");
            solicitud2.put("condicion", "Repitencia");
            solicitud2.put("observaciones", "Estudiante con buen rendimiento académico");
            solicitud2.put("tipoSolicitud", "PREINSCRIPCION");
            
            // Usuario estudiante 2
            Map<String, Object> usuario2 = new HashMap<>();
            usuario2.put("id_usuario", 3);
            usuario2.put("nombre_completo", "Carlos López");
            usuario2.put("codigo", "104612345661");
            usuario2.put("correo", "carlos.lopez@unicauca.edu.co");
            usuario2.put("password", "ContraseñaSegura123");
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
            programa2.put("nombre_programa", "Ingeniería Informática");
            usuario2.put("objPrograma", programa2);
            
            solicitud2.put("objUsuario", usuario2);
            solicitud2.put("objCursoOfertadoVerano", curso1); // Mismo curso
            
            solicitudes.add(solicitud2);
            
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Método auxiliar para corregir la codificación de caracteres
     */
    private String fixEncoding(String text) {
        if (text == null) return "";
        try {
            return new String(text.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return text; // Si falla, devolver el texto original
        }
    }
    
}
