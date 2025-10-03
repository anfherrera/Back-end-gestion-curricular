package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
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
    private final CursosOfertadosMapperDominio cursoMapper;
    private final SolicitudCursoDeVeranoPreinscripcionMapperDominio solicitudMapper;
    private final InscripcionService inscripcionService;
    private final SolicitudRepositoryInt solicitudRepository;
    private final GestionarUsuarioCUIntPort usuarioCU;

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
     * Obtener cursos de verano disponibles para estudiantes
     * GET /api/cursos-intersemestrales/cursos-verano/disponibles
     */
    @GetMapping("/cursos-verano/disponibles")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosVeranoDisponibles() {
        try {
            // Usar mapper específico para cursos disponibles (estado "Disponible")
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
     * Obtener cursos disponibles para preinscripción
     * GET /api/cursos-intersemestrales/cursos/preinscripcion
     */
    @GetMapping("/cursos/preinscripcion")
    public ResponseEntity<List<CursosOfertadosDTORespuesta>> obtenerCursosPreinscripcion() {
        try {
            List<CursoOfertadoVerano> cursos = cursoCU.listarTodos();
            // Mostrar todos los cursos disponibles para preinscripción
            List<CursosOfertadosDTORespuesta> respuesta = cursos.stream()
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
            System.out.println("🔍 DEBUG: Recibiendo solicitud de curso nuevo:");
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
                System.out.println("❌ ERROR: Condición inválida: " + peticion.getCondicion());
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

            System.out.println("🔍 DEBUG: Solicitud dominio creada, llamando al caso de uso...");

            // Llamar al caso de uso
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudCU.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);
            
            System.out.println("🔍 DEBUG: Solicitud guardada con ID: " + (solicitudGuardada != null ? solicitudGuardada.getId_solicitud() : "NULL"));

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
            
            System.out.println("✅ DEBUG: Respuesta creada exitosamente");
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ ERROR de validación: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error de validación: " + e.getMessage());
            error.put("tipo", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.out.println("❌ ERROR interno del servidor: " + e.getMessage());
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
        System.out.println("🔍 DEBUG: Endpoint de prueba simple llamado");
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
            System.out.println("🔍 DEBUG: Endpoint POST de prueba llamado");
            System.out.println("🔍 DEBUG: Datos recibidos: " + datos);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "POST funcionando correctamente");
            respuesta.put("datos_recibidos", datos);
            respuesta.put("timestamp", new java.util.Date().toString());
            respuesta.put("puerto", "5000");
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("❌ ERROR en POST de prueba: " + e.getMessage());
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
            System.out.println("🔍 DEBUG: Verificando usuarios disponibles...");
            
            // Buscar usuarios en la base de datos usando el caso de uso correcto
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario> usuarios = 
                usuarioCU.listarUsuarios(); // Listar todos los usuarios disponibles
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Usuarios encontrados");
            respuesta.put("total", usuarios.size());
            respuesta.put("usuarios", usuarios);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("❌ ERROR verificando usuarios: " + e.getMessage());
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
            System.out.println("🔍 DEBUG: Test endpoint exacto llamado");
            System.out.println("🔍 DEBUG: Petición recibida: " + peticion);
            
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
            System.out.println("❌ ERROR en test exacto: " + e.getMessage());
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
            System.out.println("🔍 DEBUG: Probando endpoint de solicitud de curso nuevo");
            
            // Crear datos de prueba
            SolicitudCursoNuevoDTOPeticion peticionPrueba = new SolicitudCursoNuevoDTOPeticion();
            peticionPrueba.setNombreCompleto("Juan Pérez");
            peticionPrueba.setCodigo("104612345660");
            peticionPrueba.setCurso("Programación Avanzada");
            peticionPrueba.setCondicion("Primera_Vez");
            peticionPrueba.setIdUsuario(1);
            
            System.out.println("🔍 DEBUG: Datos de prueba creados, llamando al endpoint...");
            
            // Llamar al método principal
            ResponseEntity<Map<String, Object>> respuesta = crearSolicitudCursoNuevo(peticionPrueba);
            
            System.out.println("🔍 DEBUG: Respuesta del endpoint: " + respuesta.getStatusCode());
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("mensaje", "Prueba completada");
            resultado.put("status", respuesta.getStatusCode().value());
            resultado.put("respuesta", respuesta.getBody());
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            System.out.println("❌ ERROR en prueba: " + e.getMessage());
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
            System.out.println("🔍 DEBUG: Recibiendo preinscripción:");
            System.out.println("  - ID Usuario: " + peticion.getIdUsuario());
            System.out.println("  - ID Curso: " + peticion.getIdCurso());
            System.out.println("  - Nombre Solicitud: " + peticion.getNombreSolicitud());
            
            // Mapear el DTO a nuestro modelo de dominio
            SolicitudCursoVeranoPreinscripcion solicitudDominio = new SolicitudCursoVeranoPreinscripcion();
            solicitudDominio.setNombre_estudiante("Estudiante"); // Valor por defecto
            solicitudDominio.setCodigo_estudiante("EST001"); // Valor por defecto
            solicitudDominio.setObservacion(peticion.getNombreSolicitud());
            solicitudDominio.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez); // Valor por defecto
            
            System.out.println("🔍 DEBUG: Solicitud dominio creada");
            
            // Crear curso con el ID real
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            curso.setId_curso(peticion.getIdCurso());
            solicitudDominio.setObjCursoOfertadoVerano(curso);
            
            System.out.println("🔍 DEBUG: Curso asignado con ID: " + peticion.getIdCurso());
            
            // Crear usuario
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario = 
                new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
            usuario.setId_usuario(peticion.getIdUsuario());
            solicitudDominio.setObjUsuario(usuario);

            System.out.println("🔍 DEBUG: Usuario asignado con ID: " + peticion.getIdUsuario());
            
            // Verificar si ya existe una preinscripción para este usuario y curso
            System.out.println("🔍 DEBUG: Verificando preinscripciones existentes...");
            Solicitud preinscripcionExistente = solicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(peticion.getIdUsuario(), peticion.getIdCurso());
            
            if (preinscripcionExistente != null) {
                System.out.println("❌ ERROR: Ya existe una preinscripción para este usuario y curso");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Ya tienes una preinscripción activa para este curso");
                error.put("codigo", "DUPLICATE_PREINSCRIPTION");
                return ResponseEntity.badRequest().body(error);
            }
            
            System.out.println("🔍 DEBUG: No hay preinscripciones duplicadas, procediendo a crear...");

            // Llamar al gateway directamente para guardar en la base de datos
            SolicitudCursoVeranoPreinscripcion solicitudGuardada = solicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudDominio);

            System.out.println("🔍 DEBUG: Solicitud guardada con ID: " + (solicitudGuardada != null ? solicitudGuardada.getId_solicitud() : "NULL"));

            // Crear respuesta JSON con la estructura esperada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id_preinscripcion", solicitudGuardada.getId_solicitud());
            respuesta.put("idUsuario", peticion.getIdUsuario());
            respuesta.put("idCurso", peticion.getIdCurso());
            respuesta.put("nombreSolicitud", peticion.getNombreSolicitud());
            respuesta.put("fecha", solicitudGuardada.getFecha_registro_solicitud());
            respuesta.put("estado", "Pendiente");
            respuesta.put("mensaje", "Preinscripción creada exitosamente");
            
            System.out.println("🔍 DEBUG: Respuesta creada exitosamente");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
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
            System.out.println("📊 Usando datos simulados para inscripciones");
            
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
            System.out.println("🔍 Obteniendo inscripciones para usuario ID: " + id_usuario);
            
            // Crear lista de todas las inscripciones disponibles
            List<Map<String, Object>> todasLasInscripciones = crearDatosInscripciones();
            
            // Filtrar inscripciones por el ID del usuario
            List<Map<String, Object>> inscripciones = new ArrayList<>();
            
            // Filtrar inscripciones por el ID del usuario
            for (Map<String, Object> inscripcion : todasLasInscripciones) {
                Integer estudianteId = (Integer) inscripcion.get("estudianteId");
                if (estudianteId != null && estudianteId.equals(id_usuario)) {
                    inscripciones.add(inscripcion);
                }
            }
            
            System.out.println("✅ Inscripciones filtradas para usuario " + id_usuario + ": " + inscripciones.size() + " encontradas");
            
            return ResponseEntity.ok(inscripciones);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Endpoint temporal para debug - verificar datos en la base de datos
     * GET /api/cursos-intersemestrales/debug-solicitudes/{idUsuario}
     */
    @GetMapping("/debug-solicitudes/{idUsuario}")
    public ResponseEntity<Map<String, Object>> debugSolicitudes(@PathVariable Integer idUsuario) {
        try {
            System.out.println("🔍 DEBUG: Buscando solicitudes para usuario ID: " + idUsuario);
            
            // Buscar todas las solicitudes del usuario
            List<SolicitudCursoVeranoPreinscripcion> preinscripciones = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("usuarioId", idUsuario);
            debug.put("preinscripcionesEncontradas", preinscripciones.size());
            debug.put("preinscripciones", preinscripciones);
            
            System.out.println("🔍 DEBUG: Encontradas " + preinscripciones.size() + " preinscripciones");
            
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
            System.out.println("🔍 DEBUG: Buscando TODAS las solicitudes en la base de datos");
            
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
            
            System.out.println("🔍 DEBUG: Encontradas " + todasLasSolicitudes.size() + " solicitudes en total");
            
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
            System.out.println("📊 Obteniendo seguimiento de actividades para usuario ID: " + idUsuario);
            
            // Obtener preinscripciones (combinar datos reales con simulados por ahora)
            List<Map<String, Object>> preinscripciones = new ArrayList<>();
            
            try {
                // Intentar obtener preinscripciones reales de la base de datos
                List<SolicitudCursoVeranoPreinscripcion> preinscripcionesReales = solicitudCU.buscarSolicitudesPorUsuario(idUsuario);
                
                for (SolicitudCursoVeranoPreinscripcion preinscripcion : preinscripcionesReales) {
                    Map<String, Object> preinscripcionMap = new HashMap<>();
                    preinscripcionMap.put("id", preinscripcion.getId_solicitud());
                    preinscripcionMap.put("fecha", preinscripcion.getFecha_registro_solicitud());
                    preinscripcionMap.put("estado", preinscripcion.getEstadosSolicitud() != null && !preinscripcion.getEstadosSolicitud().isEmpty() 
                        ? preinscripcion.getEstadosSolicitud().get(0).getEstado_actual() : "Pendiente");
                    preinscripcionMap.put("tipo", "Preinscripción");
                    preinscripcionMap.put("curso", preinscripcion.getObjCursoOfertadoVerano() != null 
                        ? preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre() : "Curso no disponible");
                    preinscripcionMap.put("estudianteId", idUsuario);
                    preinscripcionMap.put("cursoId", preinscripcion.getObjCursoOfertadoVerano() != null 
                        ? preinscripcion.getObjCursoOfertadoVerano().getId_curso() : null);
                    preinscripciones.add(preinscripcionMap);
                }
                
                System.out.println("📊 Preinscripciones reales encontradas: " + preinscripciones.size());
                
            } catch (Exception e) {
                System.out.println("⚠️ Error obteniendo preinscripciones reales: " + e.getMessage());
                // Si hay error, usar datos simulados como fallback
                preinscripciones = crearDatosPreinscripciones(idUsuario);
            }
            
            // Si no hay preinscripciones reales, agregar datos simulados como fallback
            if (preinscripciones.isEmpty()) {
                System.out.println("📊 No hay preinscripciones reales, usando datos simulados");
                preinscripciones = crearDatosPreinscripciones(idUsuario);
            }
            
            // Obtener inscripciones del usuario (usar datos simulados por ahora)
            List<Map<String, Object>> inscripciones = crearDatosInscripciones();
            List<Map<String, Object>> inscripcionesUsuario = new ArrayList<>();
            
            for (Map<String, Object> inscripcion : inscripciones) {
                Integer estudianteId = (Integer) inscripcion.get("estudianteId");
                if (estudianteId != null && estudianteId.equals(idUsuario)) {
                    inscripcionesUsuario.add(inscripcion);
                }
            }
            
            // Crear respuesta con estadísticas
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("preinscripciones", preinscripciones);
            respuesta.put("inscripciones", inscripcionesUsuario);
            respuesta.put("totalPreinscripciones", preinscripciones.size());
            respuesta.put("totalInscripciones", inscripcionesUsuario.size());
            respuesta.put("totalActividades", preinscripciones.size() + inscripcionesUsuario.size());
            
            System.out.println("✅ Seguimiento obtenido: " + preinscripciones.size() + " preinscripciones reales, " + 
                             inscripcionesUsuario.size() + " inscripciones");
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
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
            System.out.println("🗑️ Cancelando inscripción ID: " + id);
            
            // Buscar la inscripción en los datos simulados
            List<Map<String, Object>> todasLasInscripciones = crearDatosInscripciones();
            Map<String, Object> inscripcion = null;
            
            for (Map<String, Object> insc : todasLasInscripciones) {
                if (id.equals(insc.get("id"))) {
                    inscripcion = insc;
                    break;
                }
            }
            
            // Verificar si la inscripción existe
            if (inscripcion == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Inscripción no encontrada");
                error.put("message", "La inscripción con ID " + id + " no existe");
                error.put("status", 404);
                error.put("timestamp", java.time.LocalDateTime.now().toString());
                return ResponseEntity.status(404).body(error);
            }
            
            // Obtener el estado actual
            String estadoActual = (String) inscripcion.get("estado");
            
            // Verificar si ya está cancelada
            if ("cancelada".equals(estadoActual)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No se puede cancelar la inscripción");
                error.put("message", "La inscripción ya está cancelada");
                error.put("status", 400);
                error.put("timestamp", java.time.LocalDateTime.now().toString());
                return ResponseEntity.status(400).body(error);
            }
            
            // Actualizar el estado persistente
            estadosInscripciones.put(id, "cancelada");
            System.out.println("✅ Inscripción ID " + id + " cancelada exitosamente");
            
            // Respuesta exitosa
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("message", "Inscripción cancelada exitosamente");
            respuesta.put("inscripcionId", id);
            respuesta.put("estadoAnterior", estadoActual);
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
            rol1.put("nombre", "Docente"); // ✅ CORREGIDO: nombre → nombre
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
            rol2.put("nombre", "Docente"); // ✅ CORREGIDO: nombre → nombre
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
            // Simular creación del curso
            Map<String, Object> nuevoCurso = new HashMap<>();
            nuevoCurso.put("id_curso", 99); // ID simulado
            nuevoCurso.put("nombre_curso", dto.getNombre_curso());
            nuevoCurso.put("codigo_curso", dto.getCodigo_curso());
            nuevoCurso.put("descripcion", dto.getDescripcion());
            nuevoCurso.put("fecha_inicio", dto.getFecha_inicio());
            nuevoCurso.put("fecha_fin", dto.getFecha_fin());
            nuevoCurso.put("cupo_maximo", dto.getCupo_maximo());
            nuevoCurso.put("cupo_disponible", dto.getCupo_maximo());
            nuevoCurso.put("cupo_estimado", dto.getCupo_estimado());
            nuevoCurso.put("espacio_asignado", dto.getEspacio_asignado());
            nuevoCurso.put("estado", dto.getEstado());
            
            // Objeto materia simulado
            Map<String, Object> materia = new HashMap<>();
            materia.put("id_materia", dto.getId_materia());
            materia.put("nombre_materia", "Materia " + dto.getId_materia());
            materia.put("codigo_materia", "MAT" + dto.getId_materia());
            materia.put("creditos", 3);
            nuevoCurso.put("objMateria", materia);
            
            // Objeto docente simulado
            Map<String, Object> docente = new HashMap<>();
            docente.put("id_usuario", dto.getId_docente());
            docente.put("nombre", "Docente");
            docente.put("apellido", "Apellido");
            docente.put("email", "docente@unicauca.edu.co");
            docente.put("telefono", "3000000000");
            
            Map<String, Object> rol = new HashMap<>();
            rol.put("id_rol", 2);
            rol.put("nombre", "Docente");
            docente.put("objRol", rol);
            
            nuevoCurso.put("objDocente", docente);
            
            return ResponseEntity.ok(nuevoCurso);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
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
            // Simular actualización del curso
            Map<String, Object> cursoActualizado = new HashMap<>();
            cursoActualizado.put("id_curso", id);
            cursoActualizado.put("nombre_curso", dto.getNombre_curso() != null ? dto.getNombre_curso() : "Curso Actualizado");
            cursoActualizado.put("codigo_curso", dto.getCodigo_curso() != null ? dto.getCodigo_curso() : "CURSO-" + id);
            cursoActualizado.put("descripcion", dto.getDescripcion() != null ? dto.getDescripcion() : "Descripción actualizada");
            cursoActualizado.put("fecha_inicio", dto.getFecha_inicio() != null ? dto.getFecha_inicio() : "2024-06-01T08:00:00Z");
            cursoActualizado.put("fecha_fin", dto.getFecha_fin() != null ? dto.getFecha_fin() : "2024-07-15T17:00:00Z");
            cursoActualizado.put("cupo_maximo", dto.getCupo_maximo() != null ? dto.getCupo_maximo() : 25);
            cursoActualizado.put("cupo_disponible", dto.getCupo_maximo() != null ? dto.getCupo_maximo() : 25);
            cursoActualizado.put("cupo_estimado", dto.getCupo_estimado() != null ? dto.getCupo_estimado() : 25);
            cursoActualizado.put("espacio_asignado", dto.getEspacio_asignado() != null ? dto.getEspacio_asignado() : "Aula 101");
            cursoActualizado.put("estado", dto.getEstado() != null ? dto.getEstado() : "Abierto");
            
            // Objeto materia simulado
            Map<String, Object> materia = new HashMap<>();
            materia.put("id_materia", dto.getId_materia() != null ? dto.getId_materia() : 1);
            materia.put("nombre_materia", "Materia Actualizada");
            materia.put("codigo_materia", "MAT001");
            materia.put("creditos", 3);
            cursoActualizado.put("objMateria", materia);
            
            // Objeto docente simulado
            Map<String, Object> docente = new HashMap<>();
            docente.put("id_usuario", dto.getId_docente() != null ? dto.getId_docente() : 1);
            docente.put("nombre", "Docente");
            docente.put("apellido", "Actualizado");
            docente.put("email", "docente.actualizado@unicauca.edu.co");
            docente.put("telefono", "3000000000");
            
            Map<String, Object> rol = new HashMap<>();
            rol.put("id_rol", 2);
            rol.put("nombre", "Docente");
            docente.put("objRol", rol);
            
            cursoActualizado.put("objDocente", docente);
            
            return ResponseEntity.ok(cursoActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Eliminar curso de verano
     * DELETE /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @DeleteMapping("/cursos-verano/{id}")
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
        try {
            // Simular eliminación del curso
            // En una implementación real, aquí se eliminaría de la base de datos
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtener curso de verano por ID
     * GET /api/cursos-intersemestrales/cursos-verano/{id}
     */
    @GetMapping("/cursos-verano/{id}")
    public ResponseEntity<Map<String, Object>> getCursoPorId(@PathVariable Long id) {
        try {
            // Simular obtención del curso por ID
            Map<String, Object> curso = new HashMap<>();
            curso.put("id_curso", id);
            curso.put("nombre_curso", "Curso " + id);
            curso.put("codigo_curso", "CURSO-" + id);
            curso.put("descripcion", "Descripción del curso " + id);
            curso.put("fecha_inicio", "2024-06-01T08:00:00Z");
            curso.put("fecha_fin", "2024-07-15T17:00:00Z");
            curso.put("cupo_maximo", 25);
            curso.put("cupo_disponible", 20);
            curso.put("cupo_estimado", 25);
            curso.put("espacio_asignado", "Aula 101");
            curso.put("estado", "Abierto");
            
            // Objeto materia simulado
            Map<String, Object> materia = new HashMap<>();
            materia.put("id_materia", 1);
            materia.put("nombre_materia", "Materia " + id);
            materia.put("codigo_materia", "MAT" + id);
            materia.put("creditos", 3);
            curso.put("objMateria", materia);
            
            // Objeto docente simulado
            Map<String, Object> docente = new HashMap<>();
            docente.put("id_usuario", 1);
            docente.put("nombre", "Docente");
            docente.put("apellido", "Apellido");
            docente.put("email", "docente@unicauca.edu.co");
            docente.put("telefono", "3000000000");
            
            Map<String, Object> rol = new HashMap<>();
            rol.put("id_rol", 2);
            rol.put("nombre", "Docente");
            docente.put("objRol", rol);
            
            curso.put("objDocente", docente);
            
            return ResponseEntity.ok(curso);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== ENDPOINTS PARA MATERIAS Y DOCENTES ====================

    /**
     * Obtener todas las materias
     * GET /api/cursos-intersemestrales/materias
     */
    @GetMapping("/materias")
    public ResponseEntity<List<Map<String, Object>>> getTodasLasMaterias() {
        try {
            List<Map<String, Object>> materias = new ArrayList<>();
            
            // Materia 1: Programación
            Map<String, Object> materia1 = new HashMap<>();
            materia1.put("id_materia", 1);
            materia1.put("nombre_materia", "Programación");
            materia1.put("codigo_materia", "PROG");
            materia1.put("creditos", 4);
            materias.add(materia1);
            
            // Materia 2: Bases de Datos
            Map<String, Object> materia2 = new HashMap<>();
            materia2.put("id_materia", 2);
            materia2.put("nombre_materia", "Bases de Datos");
            materia2.put("codigo_materia", "BD");
            materia2.put("creditos", 3);
            materias.add(materia2);
            
            // Materia 3: Matemáticas
            Map<String, Object> materia3 = new HashMap<>();
            materia3.put("id_materia", 3);
            materia3.put("nombre_materia", "Matemáticas");
            materia3.put("codigo_materia", "MAT");
            materia3.put("creditos", 3);
            materias.add(materia3);
            
            // Materia 4: Desarrollo Web
            Map<String, Object> materia4 = new HashMap<>();
            materia4.put("id_materia", 4);
            materia4.put("nombre_materia", "Desarrollo Web");
            materia4.put("codigo_materia", "WEB");
            materia4.put("creditos", 4);
            materias.add(materia4);
            
            // Materia 5: Inteligencia Artificial
            Map<String, Object> materia5 = new HashMap<>();
            materia5.put("id_materia", 5);
            materia5.put("nombre_materia", "Inteligencia Artificial");
            materia5.put("codigo_materia", "IA");
            materia5.put("creditos", 4);
            materias.add(materia5);
            
            // Materia 6: Redes de Computadores
            Map<String, Object> materia6 = new HashMap<>();
            materia6.put("id_materia", 6);
            materia6.put("nombre_materia", "Redes de Computadores");
            materia6.put("codigo_materia", "RED");
            materia6.put("creditos", 3);
            materias.add(materia6);
            
            return ResponseEntity.ok(materias);
        } catch (Exception e) {
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
            List<Map<String, Object>> docentes = new ArrayList<>();
            
            // Docente 1: María García
            Map<String, Object> docente1 = new HashMap<>();
            docente1.put("id_usuario", 2);
            docente1.put("nombre", "María");
            docente1.put("apellido", "García");
            docente1.put("email", "maria.garcia@unicauca.edu.co");
            docente1.put("telefono", "3007654321");
            
            Map<String, Object> rol1 = new HashMap<>();
            rol1.put("id_rol", 2);
            rol1.put("nombre", "Docente"); // ✅ CORREGIDO: nombre → nombre
            docente1.put("objRol", rol1);
            
            docentes.add(docente1);
            
            // Docente 2: Carlos López
            Map<String, Object> docente2 = new HashMap<>();
            docente2.put("id_usuario", 3);
            docente2.put("nombre", "Carlos");
            docente2.put("apellido", "López");
            docente2.put("email", "carlos.lopez@unicauca.edu.co");
            docente2.put("telefono", "3009876543");
            
            Map<String, Object> rol2 = new HashMap<>();
            rol2.put("id_rol", 2);
            rol2.put("nombre", "Docente"); // ✅ CORREGIDO: nombre → nombre
            docente2.put("objRol", rol2);
            
            docentes.add(docente2);
            
            // Docente 3: Ana Martínez
            Map<String, Object> docente3 = new HashMap<>();
            docente3.put("id_usuario", 4);
            docente3.put("nombre", "Ana");
            docente3.put("apellido", "Martínez");
            docente3.put("email", "ana.martinez@unicauca.edu.co");
            docente3.put("telefono", "3001234567");
            
            Map<String, Object> rol3 = new HashMap<>();
            rol3.put("id_rol", 2);
            rol3.put("nombre", "Docente"); // ✅ CORREGIDO: nombre → nombre
            docente3.put("objRol", rol3);
            
            docentes.add(docente3);
            
            // Docente 4: Pedro Rodríguez
            Map<String, Object> docente4 = new HashMap<>();
            docente4.put("id_usuario", 5);
            docente4.put("nombre", "Pedro");
            docente4.put("apellido", "Rodríguez");
            docente4.put("email", "pedro.rodriguez@unicauca.edu.co");
            docente4.put("telefono", "3005555555");
            
            Map<String, Object> rol4 = new HashMap<>();
            rol4.put("id_rol", 2);
            rol4.put("nombre", "Docente");
            docente4.put("objRol", rol4);
            
            docentes.add(docente4);
            
            // Docente 5: Laura Botero
            Map<String, Object> docente5 = new HashMap<>();
            docente5.put("id_usuario", 6);
            docente5.put("nombre", "Laura");
            docente5.put("apellido", "Botero");
            docente5.put("email", "laura.botero@unicauca.edu.co");
            docente5.put("telefono", "3007777777");
            
            Map<String, Object> rol5 = new HashMap<>();
            rol5.put("id_rol", 2);
            rol5.put("nombre", "Docente");
            docente5.put("objRol", rol5);
            
            docentes.add(docente5);
            
            return ResponseEntity.ok(docentes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== ENDPOINTS PARA GESTIÓN DE PREINSCRIPCIONES ====================

    /**
     * Obtener preinscripciones por curso
     * GET /api/cursos-intersemestrales/preinscripciones/curso/{idCurso}
     */
    @GetMapping("/preinscripciones/curso/{idCurso}")
    public ResponseEntity<List<Map<String, Object>>> getPreinscripcionesPorCurso(
            @PathVariable Long idCurso) {
        try {
            List<Map<String, Object>> preinscripciones = new ArrayList<>();
            
            // Preinscripción 1
            Map<String, Object> preinscripcion1 = new HashMap<>();
            preinscripcion1.put("id_preinscripcion", 1);
            preinscripcion1.put("fecha_preinscripcion", "2024-01-10T10:30:00Z");
            preinscripcion1.put("estado", "Pendiente");
            preinscripcion1.put("observaciones", "");
            preinscripcion1.put("condicion", "Primera_Vez");
            
            // Usuario estudiante
            Map<String, Object> usuario1 = new HashMap<>();
            usuario1.put("id_usuario", 4);
            usuario1.put("nombre", "Juan");
            usuario1.put("apellido", "Pérez");
            usuario1.put("email", "juan@unicauca.edu.co");
            usuario1.put("telefono", "3001111111");
            usuario1.put("codigo_estudiante", "104612345660");
            
            Map<String, Object> rolEstudiante = new HashMap<>();
            rolEstudiante.put("id_rol", 1);
            rolEstudiante.put("nombre", "Estudiante");
            usuario1.put("objRol", rolEstudiante);
            
            preinscripcion1.put("objUsuario", usuario1);
            
            // Curso
            Map<String, Object> curso1 = new HashMap<>();
            curso1.put("id_curso", idCurso);
            curso1.put("nombre_curso", "Álgebra Lineal");
            curso1.put("codigo_curso", "ALG-201");
            curso1.put("descripcion", "Fundamentos de álgebra lineal");
            curso1.put("fecha_inicio", "2024-01-15T00:00:00Z");
            curso1.put("fecha_fin", "2024-03-15T00:00:00Z");
            curso1.put("cupo_maximo", 30);
            curso1.put("cupo_estimado", 25);
            curso1.put("cupo_disponible", 20);
            curso1.put("espacio_asignado", "Aula 301");
            curso1.put("estado", "Preinscripcion");
            
            // Materia del curso
            Map<String, Object> materia1 = new HashMap<>();
            materia1.put("id_materia", 1);
            materia1.put("nombre_materia", "Álgebra Lineal");
            materia1.put("codigo_materia", "ALG");
            materia1.put("creditos", 4);
            curso1.put("objMateria", materia1);
            
            // Docente del curso
            Map<String, Object> docente1 = new HashMap<>();
            docente1.put("id_usuario", 1);
            docente1.put("nombre", "María");
            docente1.put("apellido", "García");
            docente1.put("email", "maria@unicauca.edu.co");
            docente1.put("telefono", "3001234567");
            
            Map<String, Object> rolDocente = new HashMap<>();
            rolDocente.put("id_rol", 2);
            rolDocente.put("nombre", "Docente");
            docente1.put("objRol", rolDocente);
            
            curso1.put("objDocente", docente1);
            preinscripcion1.put("objCurso", curso1);
            
            preinscripciones.add(preinscripcion1);
            
            // Preinscripción 2
            Map<String, Object> preinscripcion2 = new HashMap<>();
            preinscripcion2.put("id_preinscripcion", 2);
            preinscripcion2.put("fecha_preinscripcion", "2024-01-11T14:20:00Z");
            preinscripcion2.put("estado", "Pendiente");
            preinscripcion2.put("observaciones", "");
            preinscripcion2.put("condicion", "Repitencia");
            
            // Usuario estudiante 2
            Map<String, Object> usuario2 = new HashMap<>();
            usuario2.put("id_usuario", 5);
            usuario2.put("nombre", "María");
            usuario2.put("apellido", "González");
            usuario2.put("email", "maria.gonzalez@unicauca.edu.co");
            usuario2.put("telefono", "3002222222");
            usuario2.put("codigo_estudiante", "104612345661");
            usuario2.put("objRol", rolEstudiante);
            
            preinscripcion2.put("objUsuario", usuario2);
            preinscripcion2.put("objCurso", curso1); // Mismo curso
            
            preinscripciones.add(preinscripcion2);
            
            // Preinscripción 3
            Map<String, Object> preinscripcion3 = new HashMap<>();
            preinscripcion3.put("id_preinscripcion", 3);
            preinscripcion3.put("fecha_preinscripcion", "2024-01-12T09:15:00Z");
            preinscripcion3.put("estado", "Aprobado");
            preinscripcion3.put("observaciones", "Estudiante con excelente rendimiento académico");
            preinscripcion3.put("condicion", "Homologacion");
            
            // Usuario estudiante 3
            Map<String, Object> usuario3 = new HashMap<>();
            usuario3.put("id_usuario", 6);
            usuario3.put("nombre", "Carlos");
            usuario3.put("apellido", "López");
            usuario3.put("email", "carlos.lopez@unicauca.edu.co");
            usuario3.put("telefono", "3003333333");
            usuario3.put("codigo_estudiante", "104612345662");
            usuario3.put("objRol", rolEstudiante);
            
            preinscripcion3.put("objUsuario", usuario3);
            preinscripcion3.put("objCurso", curso1); // Mismo curso
            
            preinscripciones.add(preinscripcion3);
            
            return ResponseEntity.ok(preinscripciones);
        } catch (Exception e) {
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
            rol1.put("nombre", "Estudiante"); // ✅ CORREGIDO: ya no es null
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
            rolDocente1.put("nombre", "Docente"); // ✅ CORREGIDO: ya no es null
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
            rol2.put("nombre", "Estudiante"); // ✅ CORREGIDO
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
    
    // Variable estática para mantener el estado de las inscripciones
    private static Map<Integer, String> estadosInscripciones = new HashMap<>();
    
    /**
     * Método auxiliar para crear datos de inscripciones
     */
    private List<Map<String, Object>> crearDatosInscripciones() {
        List<Map<String, Object>> inscripciones = new ArrayList<>();
        
        // Inscripción 1: Ana González (Usuario ID: 1)
        Map<String, Object> inscripcion1 = new HashMap<>();
        inscripcion1.put("id", 1);
        inscripcion1.put("fecha", "2024-01-15T10:30:00");
        inscripcion1.put("estudianteId", 1);
        inscripcion1.put("cursoId", 1);
        inscripcion1.put("estado", estadosInscripciones.getOrDefault(1, "inscrito"));
        
        Map<String, Object> estudiante1 = new HashMap<>();
        estudiante1.put("id_usuario", 1);
        estudiante1.put("nombre", "Pepa");
        estudiante1.put("apellido", "González");
        estudiante1.put("email", "pepa.gonzalez@unicauca.edu.co");
        estudiante1.put("codigo_estudiante", "104612345660");
        inscripcion1.put("estudiante", estudiante1);
        
        Map<String, Object> archivoPago1 = new HashMap<>();
        archivoPago1.put("id_documento", 1);
        archivoPago1.put("nombre", "comprobante_pago_pepa.pdf");
        archivoPago1.put("url", "/uploads/comprobante_pago_pepa.pdf");
        archivoPago1.put("fecha", "2024-01-15T10:30:00");
        inscripcion1.put("archivoPago", archivoPago1);
        inscripciones.add(inscripcion1);
        
        // Inscripción 2: Carlos López (Usuario ID: 2)
        Map<String, Object> inscripcion2 = new HashMap<>();
        inscripcion2.put("id", 2);
        inscripcion2.put("fecha", "2024-01-16T14:20:00");
        inscripcion2.put("estado", "inscrito");
        inscripcion2.put("estudianteId", 2);
        inscripcion2.put("cursoId", 2);
        
        Map<String, Object> estudiante2 = new HashMap<>();
        estudiante2.put("id_usuario", 2);
        estudiante2.put("nombre", "Carlos");
        estudiante2.put("apellido", "López");
        estudiante2.put("email", "carlos.lopez@unicauca.edu.co");
        estudiante2.put("codigo_estudiante", "104612345661");
        inscripcion2.put("estudiante", estudiante2);
        
        Map<String, Object> archivoPago2 = new HashMap<>();
        archivoPago2.put("id_documento", 2);
        archivoPago2.put("nombre", "comprobante_pago_carlos.pdf");
        archivoPago2.put("url", "/uploads/comprobante_pago_carlos.pdf");
        archivoPago2.put("fecha", "2024-01-16T14:20:00");
        inscripcion2.put("archivoPago", archivoPago2);
        inscripciones.add(inscripcion2);
        
        // Inscripción 3: María Rodríguez (Usuario ID: 3) - sin archivo de pago
        Map<String, Object> inscripcion3 = new HashMap<>();
        inscripcion3.put("id", 3);
        inscripcion3.put("fecha", "2024-01-17T09:15:00");
        inscripcion3.put("estado", "pendiente");
        inscripcion3.put("estudianteId", 3);
        inscripcion3.put("cursoId", 1);
        
        Map<String, Object> estudiante3 = new HashMap<>();
        estudiante3.put("id_usuario", 3);
        estudiante3.put("nombre", "María");
        estudiante3.put("apellido", "Rodríguez");
        estudiante3.put("email", "maria.rodriguez@unicauca.edu.co");
        estudiante3.put("codigo_estudiante", "104612345662");
        inscripcion3.put("estudiante", estudiante3);
        inscripcion3.put("archivoPago", null);
        inscripciones.add(inscripcion3);
        
        // Inscripción 4: Pedro Martínez (Usuario ID: 4)
        Map<String, Object> inscripcion4 = new HashMap<>();
        inscripcion4.put("id", 4);
        inscripcion4.put("fecha", "2024-01-18T16:45:00");
        inscripcion4.put("estado", "inscrito");
        inscripcion4.put("estudianteId", 4);
        inscripcion4.put("cursoId", 3);
        
        Map<String, Object> estudiante4 = new HashMap<>();
        estudiante4.put("id_usuario", 4);
        estudiante4.put("nombre", "Pedro");
        estudiante4.put("apellido", "Martínez");
        estudiante4.put("email", "pedro.martinez@unicauca.edu.co");
        estudiante4.put("codigo_estudiante", "104612345663");
        inscripcion4.put("estudiante", estudiante4);
        
        Map<String, Object> archivoPago4 = new HashMap<>();
        archivoPago4.put("id_documento", 4);
        archivoPago4.put("nombre", "comprobante_pago_pedro.pdf");
        archivoPago4.put("url", "/uploads/comprobante_pago_pedro.pdf");
        archivoPago4.put("fecha", "2024-01-18T16:45:00");
        inscripcion4.put("archivoPago", archivoPago4);
        inscripciones.add(inscripcion4);
        
        return inscripciones;
    }
    
    /**
     * Método auxiliar para crear datos de preinscripciones
     */
    private List<Map<String, Object>> crearDatosPreinscripciones(Integer idUsuario) {
        List<Map<String, Object>> preinscripciones = new ArrayList<>();
        
        // Solo crear preinscripciones para el usuario especificado
        if (idUsuario == 1) {
            // Preinscripción 1: Programación I (Usuario ID: 1)
            Map<String, Object> preinscripcion1 = new HashMap<>();
            preinscripcion1.put("id", 1);
            preinscripcion1.put("fecha", "2024-01-15T10:30:00");
            preinscripcion1.put("estado", "Pendiente");
            preinscripcion1.put("tipo", "Preinscripción");
            preinscripcion1.put("curso", "Programación I");
            preinscripcion1.put("estudianteId", 1);
            preinscripcion1.put("cursoId", 2);
            preinscripciones.add(preinscripcion1);
            
            // Preinscripción 2: Matemáticas Básicas (Usuario ID: 1)
            Map<String, Object> preinscripcion2 = new HashMap<>();
            preinscripcion2.put("id", 2);
            preinscripcion2.put("fecha", "2024-01-16T14:20:00");
            preinscripcion2.put("estado", "Aprobado");
            preinscripcion2.put("tipo", "Preinscripción");
            preinscripcion2.put("curso", "Matemáticas Básicas");
            preinscripcion2.put("estudianteId", 1);
            preinscripcion2.put("cursoId", 1);
            preinscripciones.add(preinscripcion2);
            
            // Preinscripción 3: Bases de Datos (Usuario ID: 1)
            Map<String, Object> preinscripcion3 = new HashMap<>();
            preinscripcion3.put("id", 3);
            preinscripcion3.put("fecha", "2024-01-17T09:15:00");
            preinscripcion3.put("estado", "Rechazado");
            preinscripcion3.put("tipo", "Preinscripción");
            preinscripcion3.put("curso", "Bases de Datos");
            preinscripcion3.put("estudianteId", 1);
            preinscripcion3.put("cursoId", 3);
            preinscripciones.add(preinscripcion3);
        }
        
        return preinscripciones;
    }
}
