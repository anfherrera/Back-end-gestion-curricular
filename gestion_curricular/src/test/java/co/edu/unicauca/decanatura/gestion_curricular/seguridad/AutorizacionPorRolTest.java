package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * Reglas de autorización por rol y por dueño, probadas con tokens reales (filtro JWT incluido).
 *
 * Un estudiante no puede: modificar catálogos, administrar usuarios, gestionar cursos o pagos,
 * cambiar estados de solicitudes, actuar a nombre de otro estudiante ni ver lo que es de otro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Seguridad - Autorización por rol y por dueño")
class AutorizacionPorRolTest {

    private static final String PASSWORD = "Clave-Prueba-123";
    private static final String CORREO_A = "seg.estudiante.a@unicauca.edu.co";
    private static final String CORREO_B = "seg.estudiante.b@unicauca.edu.co";
    private static final String CORREO_COORDINADOR = "seg.coordinador@unicauca.edu.co";
    private static final String CORREO_FUNCIONARIO = "seg.funcionario@unicauca.edu.co";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepositoryInt usuarioRepository;
    @Autowired private RolRepositoryInt rolRepository;
    @Autowired private ProgramaRepositoryInt programaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private int idA;
    private int idB;
    private String tokenA;
    private String tokenB;
    private ProgramaEntity programa;

    @BeforeEach
    void prepararEstudiantes() throws Exception {
        RolEntity rolEstudiante = rolRepository.buscarPorNombre("Estudiante").orElseThrow();
        programa = programaRepository.findAll().get(0);
        idA = crearEstudianteSiNoExiste(CORREO_A, "SEG-A", rolEstudiante);
        idB = crearEstudianteSiNoExiste(CORREO_B, "SEG-B", rolEstudiante);
        tokenA = "Bearer " + login(CORREO_A);
        tokenB = "Bearer " + login(CORREO_B);
    }

    // ==================== ACCIONES ADMINISTRATIVAS ====================

    @Test
    @DisplayName("Estudiante no puede crear programas ni eliminar roles (solo Administrador)")
    void estudianteNoModificaCatalogos() throws Exception {
        mockMvc.perform(post("/api/programas/crearPrograma").header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"X1\",\"nombre_programa\":\"Programa falso\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/roles/eliminarRol/1").header("Authorization", tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Estudiante sí puede consultar catálogos")
    void estudianteConsultaCatalogos() throws Exception {
        mockMvc.perform(get("/api/roles/listarRoles").header("Authorization", tokenA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Estudiante no puede listar usuarios ni activar/desactivar cuentas")
    void estudianteNoAdministraUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios/listarUsuarios").header("Authorization", tokenA))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/usuarios/cambiarEstado/" + idB).header("Authorization", tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Estudiante ve sus datos de usuario pero no los de otro")
    void estudianteSoloVeSuUsuario() throws Exception {
        mockMvc.perform(get("/api/usuarios/buscarUsuarioPorId/" + idA).header("Authorization", tokenA))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios/buscarUsuarioPorId/" + idB).header("Authorization", tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Estudiante no puede validar pagos ni aprobar preinscripciones, pero sí ver cursos disponibles")
    void estudianteNoGestionaCursosDeVerano() throws Exception {
        mockMvc.perform(put("/api/cursos-verano/inscripciones/1/validar-pago").header("Authorization", tokenA))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/cursos-verano/preinscripciones/1/aprobar").header("Authorization", tokenA))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/cursos-verano/disponibles").header("Authorization", tokenA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Estudiante no puede cambiar el estado de una solicitud (p. ej. aprobar la suya)")
    void estudianteNoCambiaEstados() throws Exception {
        String cambio = "{\"idSolicitud\":1,\"nuevoEstado\":\"APROBADA\"}";
        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud").header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON).content(cambio))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/solicitudes-ecaes/actualizarEstadoSolicitud").header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON).content(cambio))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Estudiante no puede eliminar solicitudes ni ver el historial general")
    void estudianteNoUsaSolicitudesGenerales() throws Exception {
        mockMvc.perform(delete("/api/solicitudes/eliminar/1").header("Authorization", tokenA))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/solicitudes/historial").header("Authorization", tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Coordinador crea estudiantes pero no administradores; tampoco puede modificar a un administrador")
    void coordinadorNoCreaAdministradores() throws Exception {
        RolEntity rolCoordinador = rolRepository.buscarPorNombre("Coordinador").orElseThrow();
        crearUsuarioSiNoExiste(CORREO_COORDINADOR, "SEG-C", rolCoordinador);
        String tokenCoordinador = "Bearer " + login(CORREO_COORDINADOR);
        int idRolAdmin = rolRepository.buscarPorNombre("Administrador").orElseThrow().getId_rol();
        int idRolEstudiante = rolRepository.buscarPorNombre("Estudiante").orElseThrow().getId_rol();

        mockMvc.perform(post("/api/usuarios/crearUsuario").header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionUsuario(null, "seg.nuevo.admin@unicauca.edu.co", "SEG-NA", idRolAdmin)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/usuarios/crearUsuario").header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionUsuario(null, "seg.nuevo.est@unicauca.edu.co", "SEG-NE", idRolEstudiante)))
                .andExpect(status().isCreated());

        // Modificar al administrador existente (aunque sea para dejarlo como estudiante) tampoco
        int idAdmin = usuarioRepository.buscarPorCorreo("admin@unicauca.edu.co").orElseThrow().getId_usuario();
        mockMvc.perform(put("/api/usuarios/actualizarUsuario").header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionUsuario(idAdmin, "admin@unicauca.edu.co", "ADM001", idRolEstudiante)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cada paso de homologación lo hace su rol: sin saltos y sin que otro rol apruebe por él")
    void cadaPasoLoHaceSuRol() throws Exception {
        String tokenFuncionario = "Bearer " + login(usuarioConRol(CORREO_FUNCIONARIO, "SEG-F", "Funcionario"));
        String tokenCoordinador = "Bearer " + login(usuarioConRol(CORREO_COORDINADOR, "SEG-C", "Coordinador"));

        String respuesta = mockMvc.perform(post("/api/solicitudes-homologacion/crearSolicitud-Homologacion")
                        .header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionHomologacion(idA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(respuesta).get("id_solicitud").asInt();

        // El Funcionario no puede saltar directo a APROBADA
        cambiarEstadoHomologacion(tokenFuncionario, id, "APROBADA").andExpect(status().isBadRequest());
        // El Coordinador no puede hacer el paso del Funcionario
        cambiarEstadoHomologacion(tokenCoordinador, id, "APROBADA_FUNCIONARIO").andExpect(status().isForbidden());
        // El Funcionario sí
        cambiarEstadoHomologacion(tokenFuncionario, id, "APROBADA_FUNCIONARIO").andExpect(status().isNoContent());
        // Y el siguiente paso ya no es suyo
        cambiarEstadoHomologacion(tokenFuncionario, id, "APROBADA_COORDINADOR").andExpect(status().isForbidden());
        cambiarEstadoHomologacion(tokenCoordinador, id, "APROBADA_COORDINADOR").andExpect(status().isNoContent());
    }

    // ==================== DUEÑO DE LA SOLICITUD ====================

    @Test
    @DisplayName("La solicitud queda a nombre de quien inicia sesión aunque el cliente envíe otro id")
    void solicitudQuedaANombreDelAutenticado() throws Exception {
        String respuesta = mockMvc.perform(post("/api/solicitudes-homologacion/crearSolicitud-Homologacion")
                        .header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionHomologacion(idB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode solicitud = objectMapper.readTree(respuesta);
        assertThat(solicitud.at("/objUsuario/id_usuario").asInt()).isEqualTo(idA);

        // Y el otro estudiante no puede verla
        int idSolicitud = solicitud.get("id_solicitud").asInt();
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/" + idSolicitud)
                        .header("Authorization", tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/" + idSolicitud)
                        .header("Authorization", tokenA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Al listar por rol, un estudiante solo recibe sus propias solicitudes")
    void listarPorRolSoloDevuelveLasPropias() throws Exception {
        String respuesta = mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/porRol")
                        .param("rol", "FUNCIONARIO")
                        .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        for (JsonNode s : objectMapper.readTree(respuesta)) {
            assertThat(s.at("/objUsuario/id_usuario").asInt()).isEqualTo(idB);
        }
    }

    @Test
    @DisplayName("Estudiante no puede preinscribir a otro estudiante en un curso de verano")
    void estudianteNoPreinscribeAOtro() throws Exception {
        mockMvc.perform(post("/api/cursos-intersemestrales/cursos-verano/preinscripciones")
                        .header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuario\":" + idB + ",\"idCurso\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Estudiante no puede descargar archivos que no son suyos")
    void estudianteNoDescargaArchivosAjenos() throws Exception {
        mockMvc.perform(get("/api/archivos/descargar/pdf").param("filename", "archivo-de-otro.pdf")
                        .header("Authorization", tokenA))
                .andExpect(status().isForbidden());
    }

    // ==================== APOYO ====================

    private org.springframework.test.web.servlet.ResultActions cambiarEstadoHomologacion(String token, int id,
            String nuevoEstado) throws Exception {
        return mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idSolicitud\":%d,\"nuevoEstado\":\"%s\"}".formatted(id, nuevoEstado)));
    }

    private String usuarioConRol(String correo, String codigo, String nombreRol) {
        crearUsuarioSiNoExiste(correo, codigo, rolRepository.buscarPorNombre(nombreRol).orElseThrow());
        return correo;
    }

    private String peticionHomologacion(int idUsuarioEnviado) {
        return """
            {
              "nombre_solicitud": "Homologación de asignaturas",
              "fecha_registro_solicitud": "2025-01-15",
              "periodo_academico": "2025-1",
              "programa_origen": "Ingeniería Electrónica",
              "programa_destino": "Ingeniería de Sistemas",
              "objUsuario": {
                "id_usuario": %d,
                "nombre_completo": "Estudiante B",
                "id_rol": 2,
                "codigo": "SEG-B",
                "correo": "%s",
                "id_programa": %d
              }
            }
            """.formatted(idUsuarioEnviado, CORREO_B, programa.getId_programa());
    }

    private String peticionUsuario(Integer idUsuario, String correo, String codigo, int idRol) {
        return """
            {
              "id_usuario": %s,
              "nombre_completo": "Usuario %s",
              "id_rol": %d,
              "codigo": "%s",
              "cedula": "%d",
              "correo": "%s",
              "password": "%s",
              "estado_usuario": true,
              "id_programa": %d
            }
            """.formatted(idUsuario, codigo, idRol, codigo, 10000000L + Math.abs(correo.hashCode() % 1_000_000),
                correo, PASSWORD, programa.getId_programa());
    }

    private int crearEstudianteSiNoExiste(String correo, String codigo, RolEntity rol) {
        return crearUsuarioSiNoExiste(correo, codigo, rol);
    }

    private int crearUsuarioSiNoExiste(String correo, String codigo, RolEntity rol) {
        return usuarioRepository.buscarPorCorreo(correo).orElseGet(() -> {
            UsuarioEntity u = new UsuarioEntity();
            u.setNombre_completo("Estudiante " + codigo);
            u.setCodigo(codigo);
            u.setCorreo(correo);
            u.setPassword(passwordEncoder.encode(PASSWORD));
            u.setEstado_usuario(true);
            u.setCedula("9" + Math.abs(correo.hashCode()));
            u.setObjRol(rol);
            u.setObjPrograma(programa);
            return usuarioRepository.save(u);
        }).getId_usuario();
    }

    private String login(String correo) throws Exception {
        String cuerpo = "{\"correo\":\"%s\",\"password\":\"%s\"}".formatted(correo, PASSWORD);
        String respuesta = mockMvc.perform(post("/api/usuarios/login")
                        .header("X-Forwarded-For", "seg-" + correo + "-" + System.nanoTime())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("token").asText();
    }
}
