package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.NotificacionRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * Fija el código HTTP y el JSON de los endpoints de Paz y Salvo que se vacían en ARQ-6:
 * {@code validarDocumentosRequeridos}, {@code obtenerDocumentos}, {@code obtenerComentarios},
 * {@code obtenerOficios} y {@code crearSolicitud-PazYSalvo}.
 *
 * <p>La prueba crea sus propias solicitudes, con documentos y estados elegidos para recorrer cada rama
 * de la clasificación por nombre (incluida la diferencia de BUG-1) y de la búsqueda del comentario de rechazo.
 *
 * <p>Antes de comparar se reemplazan los valores que cambian entre ejecuciones: los ids generados
 * ({@code "<id>"}, o el nombre del estudiante del conjunto), las fechas de hoy ({@code "<hoy>"}) y el
 * período actual ({@code "<periodoActual>"}). El orden de los campos de un objeto no se compara; el de las
 * listas sí.
 *
 * <p>Referencias en {@code src/test/resources/referencias/pazysalvo/}. Para regrabarlas tras un cambio
 * intencional: {@code mvnw test -Dtest=PazYSalvoCaracterizacionTest -DgrabarReferencia=true}
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Paz y Salvo - respuestas fijadas (ARQ-6, BUG-1)")
class PazYSalvoCaracterizacionTest {

    private static final Path CARPETA = Path.of("src/test/resources/referencias/pazysalvo");
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final String BASE = "/api/solicitudes-pazysalvo";
    private static final int SOLICITUD_INEXISTENTE = 2_000_000_000;
    private static final int USUARIO_INEXISTENTE = 2_000_000_001;

    /** Staff que no existe en la BD. */
    private static final String STAFF = "staff.pys.caract@example.test";
    private static final String[] ROLES_STAFF = { "Funcionario", "Coordinador", "Secretario" };

    @Autowired private MockMvc mockMvc;
    @Autowired private SolicitudRepositoryInt solicitudRepository;
    @Autowired private UsuarioRepositoryInt usuarioRepository;
    @Autowired private ProgramaRepositoryInt programaRepository;
    @Autowired private RolRepositoryInt rolRepository;
    @Autowired private NotificacionRepositoryInt notificacionRepository;

    private final List<Integer> programas = new ArrayList<>();
    private final List<Integer> usuarios = new ArrayList<>();
    private final List<Integer> solicitudes = new ArrayList<>();
    /** id de usuario → nombre con el que aparece en las referencias. */
    private final Map<Integer, String> nombres = new LinkedHashMap<>();
    /** nombre de la solicitud del conjunto → id. */
    private final Map<String, Integer> conjunto = new LinkedHashMap<>();
    private final Map<String, UsuarioEntity> estudiantes = new LinkedHashMap<>();
    private ProgramaEntity programa;
    private String periodoActual;

    @BeforeAll
    void crearConjunto() {
        periodoActual = PeriodoAcademicoEnum.getPeriodoActual().getValor();
        programa = new ProgramaEntity();
        programa.setCodigo("PYS-CAR");
        programa.setNombre_programa("Programa de caracterización paz y salvo");
        programa = programaRepository.save(programa);
        programas.add(programa.getId_programa());

        for (String nombre : List.of("dueno", "crea1", "crea2", "crea3", "crea4", "crea5", "crea6")) {
            estudiantes.put(nombre, estudiante(nombre));
        }
        UsuarioEntity dueno = estudiantes.get("dueno");
        long t = System.currentTimeMillis() - 3_600_000L;

        solicitud("completa-con-guion", dueno, List.of(
                estado("Enviada", t)),
                doc("Formato PM-FO-4-FOR-27.pdf", null),
                doc("autorizacion_publicar.pdf", null),
                doc("hoja_vida_academica.pdf", null),
                doc("comprobante_pago.pdf", null),
                doc("documento_trabajo_grado.pdf", null),
                doc("oficio_123.pdf", null),
                doc("resolucion_final.docx", null));

        solicitud("variantes-de-nombre", dueno, List.of(
                estado("Enviada", t)),
                doc("formato_pm_fo_4_for_27.pdf", null),
                doc("pm_fo_4_for_27.pdf", null),
                doc("FORMATO-PM-FO-4-FOR-27.PDF", null),
                doc("Resultado_Saber_Pro.pdf", null),
                doc("saber pro.pdf", null),
                doc("hoja de vida.pdf", null),
                doc("trabajo de grado final.pdf", null),
                doc("comprobante.pdf", null),
                doc("aprobacion_documento_trabajo_grado.pdf", null),
                doc("paz_y_salvo_firmado.PDF", null),
                doc("Autorizacion para publicar.pdf", null),
                doc("carta.pdf", null));

        // Los cinco obligatorios, con el formato escrito solo con guion bajo (BUG-1)
        solicitud("formato-con-guion-bajo", dueno, List.of(
                estado("Enviada", t)),
                doc("formato_pm_fo_4_for_27.pdf", null),
                doc("autorizacion_publicar.pdf", null),
                doc("hoja_vida_academica.pdf", null),
                doc("comprobante_pago.pdf", null),
                doc("documento_trabajo_grado.pdf", null));

        solicitud("sin-documentos", dueno, List.of(estado("Enviada", t)));

        solicitud("nombre-nulo", dueno, List.of(estado("Enviada", t)),
                doc(null, "comentario de un documento sin nombre"),
                doc("salvoconducto.txt", null));

        solicitud("rechazos-ultimo-sin-comentario", dueno, List.of(
                estado("Enviada", t),
                estado("RECHAZADA", t + 60_000, "primer rechazo"),
                estado("APROBADA_FUNCIONARIO", t + 120_000),
                estado("RECHAZADO", t + 180_000, "  segundo rechazo  "),
                estado("Rechazada", t + 240_000, "   ")),
                doc("hoja_vida_academica.pdf", "  revisar firma  "),
                doc("comprobante_pago.pdf", "   "),
                doc("oficio.pdf", "sin sello"));

        solicitud("rechazos-con-comentario", dueno, List.of(
                estado("Enviada", t),
                estado("rechazado", t + 60_000, "primero"),
                // Fecha anterior aunque esté después en la lista: cuenta la fecha
                estado("RECHAZADA", t + 30_000, "por fecha es anterior"),
                estado("Rechazada", t + 90_000, " el más reciente ")));

        solicitud("sin-comentarios", dueno, List.of(
                estado("Enviada", t),
                estado("APROBADA_FUNCIONARIO", t + 60_000, "aprobada con nota")),
                doc("formato.pdf", null));
    }

    @AfterAll
    void borrarConjunto() {
        for (Integer id : usuarios) {
            solicitudRepository.findAll().stream()
                    .filter(s -> s.getObjUsuario() != null && id.equals(s.getObjUsuario().getId_usuario()))
                    .map(SolicitudEntity::getId_solicitud)
                    .filter(s -> !solicitudes.contains(s))
                    .forEach(solicitudes::add);
        }
        // Crear una solicitud genera notificaciones que apuntan a ella o al estudiante
        notificacionRepository.deleteAll(notificacionRepository.findAll().stream()
                .filter(n -> (n.getObjSolicitud() != null && solicitudes.contains(n.getObjSolicitud().getId_solicitud()))
                        || (n.getObjUsuario() != null && usuarios.contains(n.getObjUsuario().getId_usuario())))
                .toList());
        solicitudRepository.deleteAllById(solicitudes);
        usuarioRepository.deleteAllById(usuarios);
        programaRepository.deleteAllById(programas);
    }

    @AfterEach
    void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    // ==================== CONSULTAS DE DOCUMENTOS Y COMENTARIOS ====================

    @Test
    @DisplayName("validarDocumentosRequeridos")
    void validarDocumentosRequeridos() throws Exception {
        compararConReferencia("validar-documentos-requeridos", consultarConjunto("/validarDocumentosRequeridos/"));
    }

    @Test
    @DisplayName("obtenerDocumentos y obtenerDocumentos/coordinador")
    void obtenerDocumentos() throws Exception {
        Map<String, JsonNode> r = consultarConjunto("/obtenerDocumentos/");
        r.putAll(consultarConjunto("/obtenerDocumentos/coordinador/"));
        // El dueño también puede consultarla
        UsuarioEntity dueno = estudiantes.get("dueno");
        r.put("GET /obtenerDocumentos/{completa-con-guion} [dueno]",
                pedir(get(BASE + "/obtenerDocumentos/" + conjunto.get("completa-con-guion")), dueno.getCorreo(), "Estudiante"));
        compararConReferencia("obtener-documentos", r);
    }

    @Test
    @DisplayName("obtenerComentarios")
    void obtenerComentarios() throws Exception {
        compararConReferencia("obtener-comentarios", consultarConjunto("/obtenerComentarios/"));
    }

    @Test
    @DisplayName("obtenerOficios")
    void obtenerOficios() throws Exception {
        compararConReferencia("obtener-oficios", consultarConjunto("/obtenerOficios/"));
    }

    private Map<String, JsonNode> consultarConjunto(String ruta) throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> s : conjunto.entrySet()) {
            r.put("GET " + ruta + "{" + s.getKey() + "}", pedir(get(BASE + ruta + s.getValue()), STAFF, ROLES_STAFF));
        }
        r.put("GET " + ruta + "{inexistente}", pedir(get(BASE + ruta + SOLICITUD_INEXISTENTE), STAFF, ROLES_STAFF));
        return r;
    }

    // ==================== CREAR SOLICITUD ====================

    @Test
    @DisplayName("crearSolicitud-PazYSalvo")
    void crearSolicitud() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        // Estudiante: el usuario sale del token
        crear(r, "estudiante, solo fecha", "crea1", "{\"fecha_solicitud\": \"2025-01-15\"}");
        crear(r, "estudiante, segunda en trámite", "crea1", "{\"fecha_solicitud\": \"2025-01-16\"}");
        crear(r, "estudiante, período, título y director", "crea2", "{\"fecha_solicitud\": \"2025-01-15\","
                + " \"periodo_academico\": \" 2024-2 \", \"titulo_trabajo_grado\": \"  Un título  \","
                + " \"director_trabajo_grado\": \"  \", \"idUsuario\": " + USUARIO_INEXISTENTE
                + ", \"objUsuario\": {\"id_usuario\": " + USUARIO_INEXISTENTE + "}}");
        crear(r, "estudiante, período inválido", "crea3", "{\"periodo_academico\": \"2024-9\"}");
        crear(r, "estudiante, fecha inválida", "crea3", "{\"fecha_solicitud\": \"15/01/2025\"}");
        crear(r, "estudiante, fecha en blanco y sin período", "crea3", "{\"fecha_solicitud\": \" \"}");
        crear(r, "estudiante, cuerpo vacío", "crea4", "{}");
        crear(r, "estudiante, cuerpo que no es objeto", "crea4", "[1]");
        r.put("POST [estudiante sin cuenta] {\"x\":1}", pedir(crearPeticion("{\"x\": 1}"),
                "sin.cuenta.pys@example.test", "Estudiante"));

        // Administrador con idUsuario
        crear(r, "admin, idUsuario inexistente", null, "{\"idUsuario\": " + USUARIO_INEXISTENTE + "}");
        crear(r, "admin, idUsuario en blanco", null, "{\"idUsuario\": \" \"}");
        crear(r, "admin, idUsuario no numérico", null, "{\"idUsuario\": \"abc\"}");
        crear(r, "admin, idUsuario", null, "{\"idUsuario\": " + id("crea4") + ", \"fecha_solicitud\": \"2025-02-01\"}");

        // Administrador con el formato del DTO
        crear(r, "admin dto, sin usuario", null, "{\"nombre_solicitud\": \"X\"}");
        crear(r, "admin dto, usuario inexistente", null, dto(USUARIO_INEXISTENTE, "\"nombre_solicitud\": \"X\","
                + " \"fecha_registro_solicitud\": \"2025-03-01\""));
        crear(r, "admin dto, sin nombre", null, dto(id("crea5"), "\"fecha_registro_solicitud\": \"2025-03-01\""));
        crear(r, "admin dto, sin fecha", null, dto(id("crea5"), "\"nombre_solicitud\": \"X\""));
        crear(r, "admin dto, período inválido", null, dto(id("crea5"), "\"nombre_solicitud\": \"X\","
                + " \"fecha_registro_solicitud\": \"2025-03-01\", \"periodo_academico\": \"2024-9\""));
        crear(r, "admin dto, sin período", null, dto(id("crea5"), "\"nombre_solicitud\": \"X\","
                + " \"fecha_registro_solicitud\": \"2025-03-01\", \"periodo_academico\": \"  \""));
        crear(r, "admin dto, con período y nombre", null, "{\"objUsuario\": {\"id_usuario\": " + id("crea6")
                + ", \"nombre_completo\": \"Nombre enviado\"}, \"nombre_solicitud\": \"X\","
                + " \"fecha_registro_solicitud\": \"2025-03-01\", \"periodo_academico\": \"2024-1\"}");
        compararConReferencia("crear-solicitud", r);
    }

    private void crear(Map<String, JsonNode> r, String caso, String estudiante, String cuerpo) throws Exception {
        JsonNode respuesta = estudiante != null
                ? pedir(crearPeticion(cuerpo), estudiantes.get(estudiante).getCorreo(), "Estudiante")
                : pedir(crearPeticion(cuerpo), STAFF, "Administrador");
        String clave = "POST [" + caso + "] " + cuerpo;
        for (Map.Entry<Integer, String> n : nombres.entrySet()) {
            clave = clave.replace(String.valueOf(n.getKey()), "{" + n.getValue() + "}");
        }
        r.put(clave, respuesta);
    }

    private RequestBuilder crearPeticion(String cuerpo) {
        return post(BASE + "/crearSolicitud-PazYSalvo").contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private static String dto(int idUsuario, String resto) {
        return "{\"objUsuario\": {\"id_usuario\": " + idUsuario + "}, " + resto + "}";
    }

    private int id(String estudiante) {
        return estudiantes.get(estudiante).getId_usuario();
    }

    // ==================== SOPORTE ====================

    /** Hace la petición como {@code usuario} y devuelve {"estado": código, "cuerpo": JSON normalizado}. */
    private JsonNode pedir(RequestBuilder peticion, String usuario, String... roles) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, "x",
                Arrays.stream(roles).map(rol -> new SimpleGrantedAuthority("ROLE_" + rol)).toList()));
        MvcResult resultado = mockMvc.perform(peticion).andReturn();
        SecurityContextHolder.clearContext();
        ObjectNode salida = JSON.createObjectNode();
        salida.put("estado", resultado.getResponse().getStatus());
        String texto = resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
        salida.set("cuerpo", texto.isEmpty() ? null : normalizar(null, JSON.readTree(texto)));
        return salida;
    }

    private JsonNode normalizar(String campo, JsonNode valor) {
        if (valor.isObject()) {
            ObjectNode ordenado = JSON.createObjectNode();
            Map<String, JsonNode> campos = new TreeMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = valor.fields(); it.hasNext();) {
                Map.Entry<String, JsonNode> e = it.next();
                campos.put(e.getKey(), normalizar(e.getKey(), e.getValue()));
            }
            ordenado.setAll(campos);
            return ordenado;
        }
        if (valor.isArray()) {
            ArrayNode lista = JSON.createArrayNode();
            valor.forEach(v -> lista.add(normalizar(campo, v)));
            return lista;
        }
        if (campo == null) {
            return valor;
        }
        String c = campo.toLowerCase();
        if ((c.equals("id") || c.startsWith("id_") || c.startsWith("idusuario") || c.equals("documento_id"))
                && valor.canConvertToInt()) {
            String nombre = c.contains("usuario") ? nombres.get(valor.asInt()) : null;
            return TextNode.valueOf(nombre != null ? "{" + nombre + "}" : "<id>");
        }
        if (c.contains("fecha") && esDeHoy(valor)) {
            return TextNode.valueOf("<hoy>");
        }
        if (valor.isTextual() && valor.asText().equals(periodoActual)) {
            return TextNode.valueOf("<periodoActual>");
        }
        if (valor.isTextual()) {
            String texto = valor.asText();
            for (Map.Entry<Integer, String> n : nombres.entrySet()) {
                texto = texto.replace("ID: " + n.getKey(), "ID: {" + n.getValue() + "}");
            }
            return TextNode.valueOf(texto);
        }
        return valor;
    }

    /** true si el valor es una fecha (texto ISO o milisegundos) de las últimas 48 horas. */
    private static boolean esDeHoy(JsonNode valor) {
        Instant instante = null;
        if (valor.isNumber()) {
            instante = Instant.ofEpochMilli(valor.asLong());
        } else if (valor.isTextual()) {
            String texto = valor.asText();
            for (DateTimeFormatter f : List.of(DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))) {
                try {
                    instante = OffsetDateTime.parse(texto, f).toInstant();
                    break;
                } catch (DateTimeParseException e) {
                    // siguiente formato
                }
            }
        }
        return instante != null && Math.abs(instante.toEpochMilli() - System.currentTimeMillis()) < 48 * 3_600_000L;
    }

    private void compararConReferencia(String nombre, Map<String, JsonNode> obtenido) throws Exception {
        Path archivo = CARPETA.resolve(nombre + ".json");
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(CARPETA);
            Files.writeString(archivo, JSON.writeValueAsString(obtenido) + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        JsonNode esperado = JSON.readTree(Files.readString(archivo, StandardCharsets.UTF_8));
        assertThat(JSON.valueToTree(obtenido).toString()).isEqualTo(esperado.toString());
    }

    // ==================== DATOS ====================

    private UsuarioEntity estudiante(String nombre) {
        UsuarioEntity u = new UsuarioEntity();
        u.setCodigo("PYSCAR-" + nombre);
        u.setNombre_completo("Estudiante " + nombre);
        u.setCorreo(nombre + ".pys.caract@example.test");
        u.setCedula("PYSCAR" + nombre);
        u.setPassword("sin-acceso");
        u.setEstado_usuario(true);
        u.setObjRol(rolRepository.findById(2).orElseThrow());
        u.setObjPrograma(programa);
        u = usuarioRepository.save(u);
        usuarios.add(u.getId_usuario());
        nombres.put(u.getId_usuario(), nombre);
        return u;
    }

    private record Estado(String estado, long fecha, String comentario) {
    }

    private static Estado estado(String estado, long fecha) {
        return new Estado(estado, fecha, null);
    }

    private static Estado estado(String estado, long fecha, String comentario) {
        return new Estado(estado, fecha, comentario);
    }

    private static DocumentoEntity doc(String nombre, String comentario) {
        DocumentoEntity d = new DocumentoEntity();
        d.setNombre(nombre);
        d.setRuta_documento(nombre == null ? null : "pazysalvo/caract/" + nombre);
        d.setFecha_documento(new Date(1_735_689_600_000L)); // 2025-01-01T00:00:00Z
        d.setEsValido(true);
        d.setComentario(comentario);
        return d;
    }

    private void solicitud(String nombre, UsuarioEntity usuario, List<Estado> historial, DocumentoEntity... documentos) {
        SolicitudPazYSalvoEntity s = new SolicitudPazYSalvoEntity();
        s.setNombre_solicitud("Caracterización " + nombre);
        s.setPeriodo_academico("2025-1");
        s.setFecha_registro_solicitud(new Date(1_735_689_600_000L));
        s.setObjUsuario(usuario);
        List<EstadoSolicitudEntity> estados = new ArrayList<>();
        for (Estado e : historial) {
            EstadoSolicitudEntity entidad = new EstadoSolicitudEntity();
            entidad.setEstado_actual(e.estado());
            entidad.setFecha_registro_estado(new Date(e.fecha()));
            entidad.setComentario(e.comentario());
            entidad.setObjSolicitud(s);
            estados.add(entidad);
        }
        s.setEstadosSolicitud(estados);
        List<DocumentoEntity> docs = new ArrayList<>();
        for (DocumentoEntity d : documentos) {
            d.setObjSolicitud(s);
            docs.add(d);
        }
        s.setDocumentos(docs);
        Integer id = solicitudRepository.save(s).getId_solicitud();
        solicitudes.add(id);
        conjunto.put(nombre, id);
    }
}
