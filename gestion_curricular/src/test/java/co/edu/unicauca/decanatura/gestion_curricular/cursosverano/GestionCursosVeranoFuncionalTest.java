package co.edu.unicauca.decanatura.gestion_curricular.cursosverano;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * PRUEBAS FUNCIONALES - GESTIÓN DE CURSOS DE VERANO (GCV5)
 * ============================================================
 *
 * Objetivo: Validar FLUJOS COMPLETOS DE NEGOCIO del módulo de Cursos de Verano.
 * Estas pruebas se enfocan en casos de uso end-to-end desde la perspectiva
 * del proceso de oferta e inscripción de cursos intersemestrales.
 *
 * Escenarios de negocio cubiertos:
 * 1. Flujo completo de oferta de cursos de verano
 * 2. Flujo de preinscripción de estudiantes
 * 3. Flujo de selección y confirmación de inscritos
 * 4. Flujo de consulta de cursos disponibles
 *
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Funcionales - Gestión de Cursos de Verano")
class GestionCursosVeranoFuncionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UsuarioRepositoryInt usuarioRepository;

    @Autowired
    private RolRepositoryInt rolRepository;

    @Autowired
    private ProgramaRepositoryInt programaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "password123";
    private static final String ESTUDIANTE_EMAIL = "estudiante.test@unicauca.edu.co";
    private static final String COORDINADOR_EMAIL = "coordinador.test@unicauca.edu.co";
    private static final String SECRETARIA_EMAIL = "secretaria.test@unicauca.edu.co";
    private static final String TEST_PROGRAMA_CODIGO = "TEST-IET";
    private static final String TEST_PROGRAMA_NOMBRE = "Ingeniería de Pruebas";

    private String tokenEstudiante;
    private String tokenCoordinador;
    private String tokenSecretaria;

    @BeforeEach
    void setUpTokens() throws Exception {
        ensureUsuariosDePrueba();
        if (tokenEstudiante == null) {
            tokenEstudiante = "Bearer " + obtenerToken(ESTUDIANTE_EMAIL);
        }
        if (tokenCoordinador == null) {
            tokenCoordinador = "Bearer " + obtenerToken(COORDINADOR_EMAIL);
        }
        if (tokenSecretaria == null) {
            tokenSecretaria = "Bearer " + obtenerToken(SECRETARIA_EMAIL);
        }
    }

    private void ensureUsuariosDePrueba() {
        ProgramaEntity programa = programaRepository.buscarPorCodigo(TEST_PROGRAMA_CODIGO)
                .orElseGet(() -> {
                    ProgramaEntity nuevoPrograma = new ProgramaEntity();
                    nuevoPrograma.setCodigo(TEST_PROGRAMA_CODIGO);
                    nuevoPrograma.setNombre_programa(TEST_PROGRAMA_NOMBRE);
                    return programaRepository.save(nuevoPrograma);
                });

        RolEntity rolEstudiante = obtenerORCrearRol("Estudiante");
        RolEntity rolCoordinador = obtenerORCrearRol("Coordinador");
        RolEntity rolSecretaria = obtenerORCrearRol("Secretario");

        crearUsuarioSiNoExiste(ESTUDIANTE_EMAIL, "EST-TEST-001", "Juan Pérez Estudiante", rolEstudiante, programa);
        crearUsuarioSiNoExiste(COORDINADOR_EMAIL, "COO-TEST-001", "Carlos Ramírez Coordinador", rolCoordinador, programa);
        crearUsuarioSiNoExiste(SECRETARIA_EMAIL, "SEC-TEST-001", "Ana López Secretaria", rolSecretaria, programa);
    }

    private RolEntity obtenerORCrearRol(String nombreRol) {
        return rolRepository.buscarPorNombre(nombreRol)
                .orElseGet(() -> {
                    RolEntity rol = new RolEntity();
                    rol.setNombre(nombreRol);
                    return rolRepository.save(rol);
                });
    }

    private void crearUsuarioSiNoExiste(String correo, String codigo, String nombreCompleto,
                                        RolEntity rol, ProgramaEntity programa) {
        usuarioRepository.buscarPorCorreo(correo).orElseGet(() -> {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setNombre_completo(nombreCompleto);
            usuario.setCodigo(codigo);
            usuario.setCorreo(correo);
            usuario.setPassword(passwordEncoder.encode(PASSWORD));
            usuario.setEstado_usuario(true);
            usuario.setCedula("T" + Math.abs(correo.hashCode()));
            usuario.setObjRol(rol);
            usuario.setObjPrograma(programa);
            return usuarioRepository.save(usuario);
        });
    }

    /** Estudiante propio de una prueba: un estudiante solo puede preinscribirse a sí mismo y una vez por curso. */
    private record EstudiantePrueba(int id, String token) {}

    private EstudiantePrueba estudiantePropio(String sufijo) throws Exception {
        String correo = "estudiante." + sufijo + "@unicauca.edu.co";
        ProgramaEntity programa = programaRepository.buscarPorCodigo(TEST_PROGRAMA_CODIGO).orElseThrow();
        RolEntity rol = rolRepository.buscarPorNombre("Estudiante").orElseThrow();
        crearUsuarioSiNoExiste(correo, "EP" + Math.abs(sufijo.hashCode() % 1_000_000), "Estudiante " + sufijo, rol, programa);
        int id = usuarioRepository.buscarPorCorreo(correo).orElseThrow().getId_usuario();
        return new EstudiantePrueba(id, "Bearer " + obtenerToken(correo));
    }
    private String obtenerToken(String correo) throws Exception {
        String loginRequest = """
            {
                "correo": "%s",
                "password": "%s"
            }
            """.formatted(correo, PASSWORD);

        String clientIp = "test-suite-" + correo + "-" + System.nanoTime();
        MvcResult result = mockMvc.perform(post("/api/usuarios/login")
                        .header("X-Forwarded-For", clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    // ==================== FLUJO 1: OFERTA DE CURSOS ====================

    @Test
    @DisplayName("Funcional 1: Flujo completo - Coordinador oferta curso de verano")
    void testFlujoOfertaCursoVerano() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador consulta cursos ofertados actuales
         * 2. Coordinador verifica que no haya duplicados
         * 3. Sistema lista todos los cursos disponibles
         * 
         * ACTORES: Coordinador del programa
         * RESULTADO ESPERADO: Lista de cursos ofertados correctamente
         */
        
        // Paso 1: Consultar cursos ofertados
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Funcional 2: Flujo de búsqueda - Coordinador busca curso específico")
    void testFlujoBusquedaCursoEspecifico() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador necesita consultar información de un curso específico
         * 2. Sistema recupera toda la información del curso
         * 3. Coordinador verifica datos del curso (docente, horario, cupos)
         * 
         * ACTORES: Coordinador, Secretaria
         * RESULTADO ESPERADO: Información detallada del curso
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/1")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCurso").value(1));
    }

    // ==================== FLUJO 2: PREINSCRIPCIÓN ====================

    @Test
    @DisplayName("Funcional 3: Flujo completo - Estudiante realiza preinscripción")
    void testFlujoPreinscripcionEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante consulta cursos disponibles para verano
         * 2. Estudiante selecciona un curso de su interés
         * 3. Estudiante se preinscribe al curso
         * 4. Sistema registra la preinscripción
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Preinscripción registrada exitosamente
         */
        
        // Paso 1: Consultar cursos disponibles
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk());

        // Paso 2: Realizar preinscripción (usando usuario 6 para evitar duplicados)
        EstudiantePrueba estudiante = estudiantePropio("flujo-preinscripcion");
        String jsonPreinscripcion = """
            {
                "idUsuario": %d,
                "idCurso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """.formatted(estudiante.id());

        mockMvc.perform(post("/api/cursos-intersemestrales/solicitudes/preinscripcion")
                        .header("Authorization", estudiante.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreinscripcion))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Funcional 4: Flujo de validación - Preinscripción con datos inválidos")
    void testFlujoValidacionPreinscripcionInvalida() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante intenta preinscribirse sin datos completos
         * 2. Sistema valida la información
         * 3. Sistema rechaza la preinscripción y muestra error claro
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Rechazo con mensaje de error
         */
        
        String jsonInvalido = """
            {
                "idUsuario": null,
                "idCurso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        mockMvc.perform(post("/api/cursos-intersemestrales/solicitudes/preinscripcion")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    // ==================== FLUJO 3: CONSULTA DE PREINSCRIPCIONES ====================

    @Test
    @DisplayName("Funcional 5: Flujo de consulta - Estudiante consulta sus preinscripciones")
    void testFlujoConsultaPreinscripcionesEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante inicia sesión
         * 2. Estudiante consulta el estado de sus preinscripciones
         * 3. Sistema muestra únicamente las preinscripciones del estudiante
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Lista de preinscripciones del estudiante
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/" + usuarioRepository.buscarPorCorreo(ESTUDIANTE_EMAIL).orElseThrow().getId_usuario())
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 4: FILTRADO DE CURSOS ====================

    @Test
    @DisplayName("Funcional 6: Flujo de filtrado - Buscar cursos por periodo")
    void testFlujoFiltroCursosPorPeriodo() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario (estudiante o coordinador) necesita ver cursos de un periodo específico
         * 2. Usuario filtra por periodo académico (ej: 2025-1)
         * 3. Sistema muestra únicamente cursos del periodo solicitado
         * 
         * ACTORES: Estudiante, Coordinador
         * RESULTADO ESPERADO: Lista filtrada de cursos
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/periodo/2025-1")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Funcional 7: Flujo de filtrado - Buscar cursos por materia")
    void testFlujoFiltroCursosPorMateria() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante busca curso de una materia específica que debe aprobar
         * 2. Sistema filtra cursos por código de materia
         * 3. Estudiante ve todas las ofertas disponibles de esa materia
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Cursos de la materia solicitada
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/materia/1")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 5: MANEJO DE CASOS EXCEPCIONALES ====================

    @Test
    @DisplayName("Funcional 8: Flujo de error - Curso inexistente")
    void testFlujoErrorCursoInexistente() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario intenta buscar un curso que no existe
         * 2. Sistema valida la existencia del curso
         * 3. Sistema retorna error 404 con mensaje descriptivo
         * 
         * ACTORES: Estudiante, Coordinador
         * RESULTADO ESPERADO: Error 404 Not Found
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/999")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isNotFound());
    }

    // ==================== FLUJO 6: FLUJO COMPLETO EXITOSO ====================

    @Test
    @DisplayName("Funcional 9: Flujo completo exitoso - De oferta a preinscripción")
    void testFlujoCompletoOfertaAPreinscripcion() throws Exception {
        /*
         * FLUJO DE NEGOCIO COMPLETO:
         * 1. Coordinador verifica cursos ofertados
         * 2. Estudiante consulta cursos disponibles
         * 3. Estudiante encuentra curso de su interés
         * 4. Estudiante se preinscribe exitosamente
         * 5. Estudiante consulta su preinscripción
         * 
         * ACTORES: Coordinador, Estudiante
         * RESULTADO ESPERADO: Preinscripción registrada y confirmada
         */
        
        // Paso 1: Consultar cursos ofertados (como coordinador)
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk());

        // Paso 2: Buscar curso específico (como estudiante)
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/1")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCurso").value(1));

        // Paso 3: Realizar preinscripción
        EstudiantePrueba estudiante = estudiantePropio("oferta-a-preinscripcion");
        String jsonPreinscripcion = """
            {
                "idUsuario": %d,
                "idCurso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """.formatted(estudiante.id());

        mockMvc.perform(post("/api/cursos-intersemestrales/solicitudes/preinscripcion")
                        .header("Authorization", estudiante.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreinscripcion))
                .andExpect(status().isCreated());

        // Paso 4: Consultar preinscripciones del estudiante
        mockMvc.perform(get("/api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/" + usuarioRepository.buscarPorCorreo(ESTUDIANTE_EMAIL).orElseThrow().getId_usuario())
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Funcional 10: Flujo de exploración - Estudiante explora opciones de cursos")
    void testFlujoExploracionCursos() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante consulta todos los cursos disponibles
         * 2. Estudiante filtra por periodo académico
         * 3. Estudiante busca una materia específica
         * 4. Estudiante toma decisión informada
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Información completa para toma de decisión
         */
        
        // Paso 1: Ver todos los cursos
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk());

        // Paso 2: Filtrar por periodo
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/periodo/2025-1")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk());

        // Paso 3: Buscar materia específica
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/materia/1")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk());
    }
    @Test
    void crearCursoRechazaFechasInvertidas() throws Exception {
        mockMvc.perform(post("/api/cursos-intersemestrales/cursos-verano")
                        .header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionCrearCurso("2025-07-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Fechas inválidas"))
                .andExpect(jsonPath("$.message").value("La fecha de fin debe ser posterior a la fecha de inicio"));
    }

    @Test
    @Transactional
    void crearCursoConservaRespuestaYResuelveElSalonPorId() throws Exception {
        registrarMateriaYDocenteDePrueba();
        jdbc.update("INSERT INTO Salones(idSalon, numero_salon, activo) VALUES (901, 'REF-901', true)");

        ObjectNode peticion = (ObjectNode) objectMapper.readTree(peticionCrearCurso("2025-08-01"));
        peticion.put("id_materia", 901);
        peticion.put("id_docente", 901);
        peticion.put("id_salon", 901);

        mockMvc.perform(post("/api/cursos-intersemestrales/cursos-verano")
                        .header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_curso").isNumber())
                .andExpect(jsonPath("$.nombre_curso").value("Materia de prueba"))
                .andExpect(jsonPath("$.espacio_asignado").value("REF-901"))
                .andExpect(jsonPath("$.fecha_inicio").value("2025-07-02"))
                .andExpect(jsonPath("$.fecha_fin").value("2025-08-01"))
                .andExpect(jsonPath("$.periodo").value("2025-2"))
                .andExpect(jsonPath("$.estado").value("Abierto"));
    }

    @Test
    void crearCursoConservaCodigoDeCursoDuplicado() throws Exception {
        mockMvc.perform(post("/api/cursos-intersemestrales/cursos-verano")
                        .header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionCrearCurso("2025-08-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Curso duplicado"))
                .andExpect(jsonPath("$.codigo").value("CURSO_DUPLICADO"));
    }

    private String peticionCrearCurso(String fechaFin) {
        return """
                {
                  "id_materia": 1,
                  "id_docente": 1,
                  "cupo_estimado": 30,
                  "fecha_inicio": "2025-07-02",
                  "fecha_fin": "%s",
                  "periodoAcademico": " 2025-2 ",
                  "espacio_asignado": "221",
                  "grupo": " a "
                }
                """.formatted(fechaFin);
    }

    @Test
    void listarEstudiantesDeCursoVacio() throws Exception {
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/2/estudiantes")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_estudiantes").value(0))
                .andExpect(jsonPath("$.total_preinscritos").value(0))
                .andExpect(jsonPath("$.total_inscritos").value(0))
                .andExpect(jsonPath("$.estudiantes").isEmpty());
    }

    @Test
    @Transactional
    void listarEstudiantesConPreinscripcionEInscripcion() throws Exception {
        registrarMateriaYDocenteDePrueba();
        jdbc.update("""
                INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon)
                VALUES (901, 901, 901, 'A', 30, 'REF-901')
                """);
        registrarSolicitudVerano(901, 1, false);
        registrarSolicitudVerano(902, 1, true);
        registrarSolicitudVerano(903, 5, false);
        registrarSolicitudVerano(904, 26, true);
        jdbc.update("""
                INSERT INTO EstadosSolicitudes(idfkSolicitud, fecha_registro_estado, estado_actual)
                VALUES (902, '2025-07-03 08:00:00', 'Pago_Validado')
                """);

        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/901/estudiantes")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_estudiantes").value(3))
                .andExpect(jsonPath("$.total_preinscritos").value(2))
                .andExpect(jsonPath("$.total_inscritos").value(1))
                .andExpect(jsonPath("$.estudiantes[0].id_usuario").value(26))
                .andExpect(jsonPath("$.estudiantes[0].tiene_preinscripcion").value(false))
                .andExpect(jsonPath("$.estudiantes[0].estado_inscripcion").value("Enviada"))
                .andExpect(jsonPath("$.estudiantes[0].estado_preinscripcion").hasJsonPath())
                .andExpect(jsonPath("$.estudiantes[0].estado_preinscripcion").doesNotExist())
                .andExpect(jsonPath("$.estudiantes[0].fecha_inscripcion").exists())
                .andExpect(jsonPath("$.estudiantes[0].fecha_preinscripcion").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.estudiantes[0].tiene_inscripcion").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.estudiantes[1].id_usuario").value(1))
                .andExpect(jsonPath("$.estudiantes[1].id_solicitud_inscripcion").value(902))
                .andExpect(jsonPath("$.estudiantes[1].tipo").value("Inscrito"))
                .andExpect(jsonPath("$.estudiantes[1].id_programa").value(1))
                .andExpect(jsonPath("$.estudiantes[1].tiene_inscripcion").value(true))
                .andExpect(jsonPath("$.estudiantes[1].fecha_inscripcion").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.estudiantes[1].tiene_preinscripcion").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.estudiantes[2].id_usuario").value(5))
                .andExpect(jsonPath("$.estudiantes[2].estado_inscripcion").value("Sin inscripción"))
                .andExpect(jsonPath("$.estudiantes[2].tipo").value("Preinscrito"))
                .andExpect(jsonPath("$.estudiantes[2].id_solicitud_inscripcion").hasJsonPath())
                .andExpect(jsonPath("$.estudiantes[2].id_solicitud_inscripcion").doesNotExist());

        byte[] pdf = mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/901/estudiantes/export/pdf")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        PdfReader lector = new PdfReader(pdf);
        try {
            assertThat(PdfTextExtractor.getTextFromPage(lector, 1))
                    .contains("REF901", "Total Estudiantes: 3", "Juan Pérez", "Pago_Validado");
        } finally {
            lector.close();
        }
    }

    private void registrarMateriaYDocenteDePrueba() {
        jdbc.update("INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (901, 'REF901', 'Materia de prueba', 3)");
        jdbc.update("INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (901, 'REF901', 'Docente de prueba')");
    }

    private void registrarSolicitudVerano(int id, int idUsuario, boolean inscripcion) {
        jdbc.update("""
                INSERT INTO Solicitudes(idSolicitud, idUsuario, idCurso, fecha_registro_solicitud,
                                        periodo_academico, nombre_solicitud)
                VALUES (?, ?, 901, '2025-07-02 08:00:00', '2025-2', 'Curso de verano')
                """, id, idUsuario);
        String tabla = inscripcion ? "Solicitudes_Cursos_Verano_Inscripcion" : "Solicitudes_Cursos_Verano_Preinscripcion";
        jdbc.update("INSERT INTO " + tabla
                + "(idSolicitud, nombre_estudiante, observacion, codicion_solicitud) VALUES (?, 'Prueba', '', 'Primera_Vez')", id);
    }

}

