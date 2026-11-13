package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.TipoDocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * Fija qué solicitudes devuelve cada endpoint {@code /listar…} de homologación, reingreso, paz y salvo
 * y ECAES según su estado, programa, período y quién consulta (ARQ-7 y DUP-1).
 *
 * <p>La prueba crea su propio conjunto de solicitudes (ver {@link #CONJUNTO}): dos programas, un
 * estudiante en cada uno, un coordinador del programa A y solicitudes en el período actual y en
 * {@value #OTRO_PERIODO}. De cada respuesta se conservan solo las solicitudes de ese conjunto, en el orden
 * en que llegan, así que no le afectan los datos que crean otras pruebas en la misma base.
 *
 * <p>Las referencias están en {@code src/test/resources/referencias/listados/}. Para regrabarlas tras un
 * cambio intencional: {@code mvnw test -Dtest=ListadosPorRolCaracterizacionTest -DgrabarReferencia=true}
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Listados por rol - respuestas fijadas (ARQ-7, DUP-1)")
class ListadosPorRolCaracterizacionTest {

    private static final Path CARPETA = Path.of("src/test/resources/referencias/listados");
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    static final String OTRO_PERIODO = "2090-1";

    private static final String ENVIADA = "Enviada";
    private static final String AF = "APROBADA_FUNCIONARIO";
    private static final String AC = "APROBADA_COORDINADOR";
    private static final String APROBADA = "APROBADA";
    private static final String RECHAZADA = "RECHAZADA";
    private static final String PRE_REGISTRADO = "PRE_REGISTRADO";

    /** Etiqueta "estudiante/período: historial de estados" de cada solicitud del conjunto. */
    private record Caso(String estudiante, boolean periodoActual, String... estados) {
        String etiqueta() {
            return estudiante + "/" + (periodoActual ? "actual" : OTRO_PERIODO) + ": " + String.join(">", estados);
        }
    }

    private static final List<Caso> CONJUNTO = List.of(
            new Caso("A", true, ENVIADA),
            new Caso("A", true, ENVIADA, AF),
            new Caso("A", true, ENVIADA, AF, AC),
            new Caso("A", true, ENVIADA, AF, AC, APROBADA),
            new Caso("A", true, ENVIADA, RECHAZADA),
            new Caso("A", true, ENVIADA, AF, RECHAZADA),
            new Caso("A", true, ENVIADA, PRE_REGISTRADO),
            // Forma canónica en lugar de la guardada ("Enviada"): las consultas comparan el texto exacto
            new Caso("A", true, "ENVIADA"),
            new Caso("B", true, ENVIADA),
            new Caso("B", true, ENVIADA, AF),
            new Caso("B", true, ENVIADA, AF, AC),
            new Caso("A", false, ENVIADA),
            new Caso("A", false, ENVIADA, AF),
            new Caso("A", false, ENVIADA, AF, AC),
            new Caso("A", false, ENVIADA, AF, AC, APROBADA),
            new Caso("B", false, ENVIADA, AF),
            new Caso("B", false, ENVIADA, AF, AC));

    // Quién consulta
    private static final String COORDINADOR_A = "coord.listados@example.test";
    private static final String ESTUDIANTE_A = "est.a.listados@example.test";
    private static final String ESTUDIANTE_B = "est.b.listados@example.test";
    /** Staff que no existe en la BD: el controlador no encuentra su programa. */
    private static final String STAFF_SIN_PROGRAMA = "staff.sin.programa@example.test";

    @Autowired private MockMvc mockMvc;
    @Autowired private SolicitudRepositoryInt solicitudRepository;
    @Autowired private UsuarioRepositoryInt usuarioRepository;
    @Autowired private ProgramaRepositoryInt programaRepository;
    @Autowired private RolRepositoryInt rolRepository;

    private final List<Integer> programas = new ArrayList<>();
    private final List<Integer> usuarios = new ArrayList<>();
    private final List<Integer> solicitudes = new ArrayList<>();
    private UsuarioEntity estudianteA;
    private UsuarioEntity estudianteB;
    private String periodoActual;

    /** id de solicitud → etiqueta, por tipo. */
    private final Map<Integer, String> etiquetas = new LinkedHashMap<>();

    @BeforeAll
    void crearConjunto() {
        periodoActual = PeriodoAcademicoEnum.getPeriodoActual().getValor();
        ProgramaEntity programaA = programa("LST-A", "Programa A de listados");
        ProgramaEntity programaB = programa("LST-B", "Programa B de listados");
        estudianteA = usuario("LSTEST-A", ESTUDIANTE_A, 2, programaA);
        estudianteB = usuario("LSTEST-B", ESTUDIANTE_B, 2, programaB);
        usuario("LSTCOORD", COORDINADOR_A, 3, programaA);

        crear(SolicitudHomologacionEntity::new, "H");
        crear(SolicitudReingresoEntity::new, "R");
        crear(SolicitudPazYSalvoEntity::new, "P");
        crear(() -> {
            SolicitudEcaesEntity s = new SolicitudEcaesEntity();
            s.setTipoDocumento(TipoDocumentoEntity.CC);
            s.setNumero_documento("100200300");
            s.setFecha_expedicion(new Date());
            s.setFecha_nacimiento(new Date());
            return s;
        }, "E");
    }

    @AfterAll
    void borrarConjunto() {
        solicitudRepository.deleteAllById(solicitudes);
        usuarioRepository.deleteAllById(usuarios);
        programaRepository.deleteAllById(programas);
    }

    @AfterEach
    void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    // ==================== HOMOLOGACIÓN ====================

    @Test
    @DisplayName("Homologación")
    void homologacion() throws Exception {
        String base = "/api/solicitudes-homologacion/listarSolicitud-Homologacion";
        Map<String, List<String>> r = new LinkedHashMap<>();
        consultar(r, "H", STAFF_SIN_PROGRAMA, base);
        listadosTresAprobaciones(r, "H", base, base + "/porRol");
        compararConReferencia("homologacion", r);
    }

    // ==================== REINGRESO ====================

    @Test
    @DisplayName("Reingreso")
    void reingreso() throws Exception {
        String base = "/api/solicitudes-reingreso/listarSolicitud-Reingreso";
        Map<String, List<String>> r = new LinkedHashMap<>();
        consultar(r, "R", STAFF_SIN_PROGRAMA, "/api/solicitudes-reingreso/listarSolicitudes-Reingreso");
        listadosTresAprobaciones(r, "R", base, base + "/porUser");
        compararConReferencia("reingreso", r);
    }

    /** Endpoints comunes a homologación y reingreso. */
    private void listadosTresAprobaciones(Map<String, List<String>> r, String tipo, String base, String porRol)
            throws Exception {
        consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/Funcionario");
        consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/Funcionario?periodoAcademico=" + OTRO_PERIODO);
        consultar(r, tipo, COORDINADOR_A, base + "/Coordinador");
        consultar(r, tipo, COORDINADOR_A, base + "/Coordinador?periodoAcademico=" + OTRO_PERIODO);
        consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/Coordinador");
        consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/Secretaria");
        consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/Secretaria?periodoAcademico=" + OTRO_PERIODO);
        porRol(r, tipo, porRol);
        for (String rol : List.of("Funcionario", "Secretaria")) {
            consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/" + rol + "/Aprobadas");
            consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/" + rol + "/Aprobadas?periodoAcademico=todos");
            consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/" + rol + "/Aprobadas?periodoAcademico=" + OTRO_PERIODO);
        }
        consultar(r, tipo, COORDINADOR_A, base + "/Coordinador/Aprobadas");
        consultar(r, tipo, COORDINADOR_A, base + "/Coordinador/Aprobadas?periodoAcademico=" + OTRO_PERIODO);
        consultar(r, tipo, STAFF_SIN_PROGRAMA, base + "/Coordinador/Aprobadas?periodoAcademico=" + OTRO_PERIODO);
    }

    /** Variantes del listado por rol (homologación, reingreso y ECAES aceptan período). */
    private void porRol(Map<String, List<String>> r, String tipo, String ruta) throws Exception {
        for (String rol : List.of("FUNCIONARIO", "COORDINADOR", "SECRETARIA", "otro")) {
            consultar(r, tipo, STAFF_SIN_PROGRAMA, ruta + "?rol=" + rol);
        }
        consultar(r, tipo, COORDINADOR_A, ruta + "?rol=COORDINADOR");
        consultar(r, tipo, STAFF_SIN_PROGRAMA, ruta + "?rol=ESTUDIANTE&idUsuario=" + estudianteA.getId_usuario());
        consultar(r, tipo, STAFF_SIN_PROGRAMA, ruta + "?rol=ESTUDIANTE&idUsuario=" + estudianteA.getId_usuario()
                + "&periodoAcademico=" + OTRO_PERIODO);
        // Quien no es staff solo ve lo suyo, pida el rol que pida
        consultar(r, tipo, ESTUDIANTE_B, ruta + "?rol=FUNCIONARIO", "Estudiante");
        consultar(r, tipo, ESTUDIANTE_B, ruta + "?rol=FUNCIONARIO&periodoAcademico=" + OTRO_PERIODO, "Estudiante");
    }

    // ==================== PAZ Y SALVO ====================

    @Test
    @DisplayName("Paz y salvo")
    void pazYSalvo() throws Exception {
        String base = "/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo";
        Map<String, List<String>> r = new LinkedHashMap<>();
        consultar(r, "P", STAFF_SIN_PROGRAMA, base);
        consultar(r, "P", STAFF_SIN_PROGRAMA, base + "/Funcionario");
        consultar(r, "P", COORDINADOR_A, base + "/Coordinador");
        consultar(r, "P", COORDINADOR_A, base + "/Coordinador?periodoAcademico=" + OTRO_PERIODO);
        consultar(r, "P", STAFF_SIN_PROGRAMA, base + "/Coordinador");
        consultar(r, "P", STAFF_SIN_PROGRAMA, base + "/Secretaria");
        consultar(r, "P", STAFF_SIN_PROGRAMA, base + "/Secretaria?periodoAcademico=" + OTRO_PERIODO);
        for (String rol : List.of("FUNCIONARIO", "COORDINADOR", "SECRETARIA", "otro")) {
            consultar(r, "P", STAFF_SIN_PROGRAMA, base + "/porRol?rol=" + rol);
        }
        consultar(r, "P", STAFF_SIN_PROGRAMA, base + "/porRol?rol=ESTUDIANTE&idUsuario=" + estudianteA.getId_usuario());
        consultar(r, "P", ESTUDIANTE_B, base + "/porRol?rol=FUNCIONARIO", "Estudiante");
        // /{id} con un nombre de rol (BUG-4)
        for (String rol : List.of("coordinador", "Funcionario", "SECRETARIA", "ESTUDIANTE", "otro")) {
            consultar(r, "P", COORDINADOR_A, base + "/" + rol);
        }
        compararConReferencia("pazysalvo", r);
    }

    // ==================== ECAES ====================

    @Test
    @DisplayName("ECAES")
    void ecaes() throws Exception {
        String base = "/api/solicitudes-ecaes";
        Map<String, List<String>> r = new LinkedHashMap<>();
        consultar(r, "E", STAFF_SIN_PROGRAMA, base + "/listarSolicitudes-Ecaes");
        consultar(r, "E", STAFF_SIN_PROGRAMA, base + "/listarSolicitudes-Ecaes/Funcionario");
        consultar(r, "E", STAFF_SIN_PROGRAMA, base + "/listarSolicitudes-Ecaes/Funcionario?periodoAcademico=" + OTRO_PERIODO);
        String porRol = base + "/listarSolicitud-ecaes/porRol";
        porRol(r, "E", porRol);
        consultar(r, "E", COORDINADOR_A, porRol + "?rol=coordinador&periodoAcademico=" + OTRO_PERIODO);
        compararConReferencia("ecaes", r);
    }

    // ==================== SOPORTE ====================

    private void consultar(Map<String, List<String>> resultados, String tipo, String usuario, String ruta)
            throws Exception {
        consultar(resultados, tipo, usuario, ruta, "Funcionario", "Coordinador", "Secretario");
    }

    /** Hace la petición como {@code usuario} y guarda las etiquetas del conjunto que devuelve, en orden. */
    private void consultar(Map<String, List<String>> resultados, String tipo, String usuario, String ruta,
            String... roles) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, "x",
                Arrays.stream(roles).map(rol -> new SimpleGrantedAuthority("ROLE_" + rol)).toList()));
        MvcResult resultado = mockMvc.perform(get(ruta)).andReturn();
        String clave = "GET " + ruta.replace(String.valueOf(estudianteA.getId_usuario()), "{estudianteA}")
                + " [" + usuario.split("@")[0] + "]";
        assertThat(resultado.getResponse().getStatus()).as(clave).isEqualTo(200);

        JsonNode cuerpo = JSON.readTree(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<String> vistas = new ArrayList<>();
        for (JsonNode solicitud : cuerpo) {
            String etiqueta = etiquetas.get(solicitud.path("id_solicitud").asInt());
            if (etiqueta != null && etiqueta.startsWith(tipo + " ")) {
                vistas.add(etiqueta.substring(2));
            }
        }
        resultados.put(clave, vistas);
    }

    private void compararConReferencia(String nombre, Map<String, List<String>> obtenido) throws Exception {
        Path archivo = CARPETA.resolve(nombre + ".json");
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(CARPETA);
            Files.writeString(archivo, JSON.writeValueAsString(obtenido) + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        Map<String, List<String>> esperado = JSON.readValue(Files.readString(archivo, StandardCharsets.UTF_8),
                new TypeReference<LinkedHashMap<String, List<String>>>() { });
        assertThat(obtenido).isEqualTo(esperado);
    }

    private void crear(Supplier<SolicitudEntity> nueva, String tipo) {
        for (Caso caso : CONJUNTO) {
            SolicitudEntity s = nueva.get();
            s.setNombre_solicitud("Listados " + tipo);
            s.setPeriodo_academico(caso.periodoActual() ? periodoActual : OTRO_PERIODO);
            s.setFecha_registro_solicitud(new Date());
            s.setObjUsuario("A".equals(caso.estudiante()) ? estudianteA : estudianteB);
            List<EstadoSolicitudEntity> estados = new ArrayList<>();
            long ahora = System.currentTimeMillis();
            for (int i = 0; i < caso.estados().length; i++) {
                EstadoSolicitudEntity e = new EstadoSolicitudEntity();
                e.setEstado_actual(caso.estados()[i]);
                // En el pasado y en orden: el último de la lista es también el más reciente
                e.setFecha_registro_estado(new Date(ahora - (caso.estados().length - i) * 60_000L));
                e.setObjSolicitud(s);
                estados.add(e);
            }
            s.setEstadosSolicitud(estados);
            Integer id = solicitudRepository.save(s).getId_solicitud();
            solicitudes.add(id);
            etiquetas.put(id, tipo + " " + caso.etiqueta());
        }
    }

    private ProgramaEntity programa(String codigo, String nombre) {
        ProgramaEntity p = new ProgramaEntity();
        p.setCodigo(codigo);
        p.setNombre_programa(nombre);
        p = programaRepository.save(p);
        programas.add(p.getId_programa());
        return p;
    }

    private UsuarioEntity usuario(String codigo, String correo, int idRol, ProgramaEntity programa) {
        UsuarioEntity u = new UsuarioEntity();
        u.setCodigo(codigo);
        u.setNombre_completo("Usuario " + codigo);
        u.setCorreo(correo);
        u.setCedula("LST" + codigo.hashCode());
        u.setPassword("sin-acceso");
        u.setEstado_usuario(true);
        u.setObjRol(rolRepository.findById(idRol).orElseThrow());
        u.setObjPrograma(programa);
        u = usuarioRepository.save(u);
        usuarios.add(u.getId_usuario());
        return u;
    }
}
