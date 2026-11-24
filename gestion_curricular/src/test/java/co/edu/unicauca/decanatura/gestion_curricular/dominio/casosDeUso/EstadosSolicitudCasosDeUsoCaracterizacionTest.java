package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/**
 * Fija lo que los casos de uso de ECAES, Homologación, Reingreso y Paz y Salvo escriben en el historial de estados
 * (OPC-1): el estado inicial al crear la solicitud y el registro que entregan al gateway al cambiar de estado. Para
 * cada caso guarda la excepción o el resultado, y las llamadas a los gateways y a las notificaciones en orden. Las
 * fechas solo se registran como "reciente" (generada durante la llamada).
 */
class EstadosSolicitudCasosDeUsoCaracterizacionTest {

    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/estados/solicitudes-casos-de-uso.json");

    /** Historiales de partida para los cambios de estado. */
    private static final String[][] HISTORIALES = {
        {},
        {"Enviada"},
        {"ENVIADA", "APROBADA_FUNCIONARIO"},
        {"Enviada", "  "},
        {"Enviada", null},
        {"APROBADA"},
        {"PRE_REGISTRADO"},
        {"Desconocido"},
    };

    private static final String[] NUEVOS_ESTADOS = {
        "APROBADA_FUNCIONARIO", "aprobada funcionario", "Aprobada Coordinador", "RECHAZADA", "PRE_REGISTRADO",
        "Inventado", null, "  ",
    };

    private final ObjectMapper json = new ObjectMapper();
    private Date inicioLlamada;
    private Date finLlamada;

    @Test
    void registraLosEstadosDeLasSolicitudes() throws Exception {
        ObjectNode resultados = json.createObjectNode();
        resultados.set("ecaes", ecaes());
        resultados.set("homologacion", homologacion());
        resultados.set("reingreso", reingreso());
        resultados.set("pazysalvo", pazYSalvo());

        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(REFERENCIA)) {
            Files.createDirectories(REFERENCIA.getParent());
            Files.writeString(REFERENCIA, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(REFERENCIA)));
    }

    // ==================== Por tipo de solicitud ====================

    private ObjectNode ecaes() throws Exception {
        ObjectNode porTipo = json.createObjectNode();
        ObjectNode crear = porTipo.putObject("crear");
        for (String[] historial : new String[][] {null, {}}) {
            Entorno e = new Entorno();
            var gateway = mock(GestionarPreRegistroEcaesGatewayIntPort.class);
            when(gateway.guardar(any())).thenAnswer(i -> guardada(SolicitudEcaes::new, historial));
            var cu = new GestionarSolicitudEcaesCUAdapter(gateway, e.documentos, e.estados, e.usuarios, e.notificaciones,
                    e.archivos);
            crear.set(nombreHistorial(historial), llamar(() -> cu.guardar(nueva(SolicitudEcaes::new)), gateway, e));
        }
        ObjectNode cambiar = porTipo.putObject("cambiarEstado");
        for (String[] historial : HISTORIALES) {
            ObjectNode porHistorial = cambiar.putObject(nombreHistorial(historial));
            for (String nuevo : NUEVOS_ESTADOS) {
                Entorno e = new Entorno();
                var gateway = mock(GestionarPreRegistroEcaesGatewayIntPort.class);
                when(gateway.buscarPorId(anyInt())).thenAnswer(i -> Optional.of(conHistorial(SolicitudEcaes::new, historial)));
                var cu = new GestionarSolicitudEcaesCUAdapter(gateway, e.documentos, e.estados, e.usuarios,
                        e.notificaciones, e.archivos);
                porHistorial.set(String.valueOf(nuevo), llamar(() -> {
                    cu.cambiarEstadoSolicitud(3, nuevo);
                    return null;
                }, gateway, e));
            }
        }
        return porTipo;
    }

    private ObjectNode homologacion() throws Exception {
        ObjectNode porTipo = json.createObjectNode();
        ObjectNode crear = porTipo.putObject("crear");
        for (String[] historial : new String[][] {null, {}}) {
            Entorno e = new Entorno();
            var gateway = mock(GestionarSolicitudHomologacionGatewayIntPort.class);
            when(gateway.guardar(any())).thenAnswer(i -> guardada(SolicitudHomologacion::new, historial));
            var cu = new GestionarSolicitudHomologacionCUAdapter(gateway, e.usuarios, e.documentos, e.estados,
                    e.notificaciones, e.archivos);
            crear.set(nombreHistorial(historial), llamar(() -> cu.guardar(nueva(SolicitudHomologacion::new)), gateway, e));
        }
        ObjectNode cambiar = porTipo.putObject("cambiarEstado");
        for (String[] historial : HISTORIALES) {
            ObjectNode porHistorial = cambiar.putObject(nombreHistorial(historial));
            for (String nuevo : NUEVOS_ESTADOS) {
                Entorno e = new Entorno();
                var gateway = mock(GestionarSolicitudHomologacionGatewayIntPort.class);
                when(gateway.buscarPorId(anyInt()))
                        .thenAnswer(i -> Optional.of(conHistorial(SolicitudHomologacion::new, historial)));
                var cu = new GestionarSolicitudHomologacionCUAdapter(gateway, e.usuarios, e.documentos, e.estados,
                        e.notificaciones, e.archivos);
                porHistorial.set(String.valueOf(nuevo), llamar(() -> {
                    cu.cambiarEstadoSolicitud(3, nuevo);
                    return null;
                }, gateway, e));
            }
        }
        return porTipo;
    }

    private ObjectNode reingreso() throws Exception {
        ObjectNode porTipo = json.createObjectNode();
        ObjectNode crear = porTipo.putObject("crear");
        for (String[] historial : new String[][] {null, {}}) {
            Entorno e = new Entorno();
            var gateway = mock(GestionarSolicitudReingresoGatewayIntPort.class);
            when(gateway.crearSolicitudReingreso(any())).thenAnswer(i -> guardada(SolicitudReingreso::new, historial));
            var cu = new GestionarSolicitudReingresoCUAdapter(e.usuarios, e.documentos, e.estados, gateway,
                    e.notificaciones, e.archivos);
            crear.set(nombreHistorial(historial),
                    llamar(() -> cu.crearSolicitudReingreso(nueva(SolicitudReingreso::new)), gateway, e));
        }
        ObjectNode cambiar = porTipo.putObject("cambiarEstado");
        for (String[] historial : HISTORIALES) {
            ObjectNode porHistorial = cambiar.putObject(nombreHistorial(historial));
            for (String nuevo : NUEVOS_ESTADOS) {
                Entorno e = new Entorno();
                var gateway = mock(GestionarSolicitudReingresoGatewayIntPort.class);
                when(gateway.buscarPorId(anyInt()))
                        .thenAnswer(i -> Optional.of(conHistorial(SolicitudReingreso::new, historial)));
                var cu = new GestionarSolicitudReingresoCUAdapter(e.usuarios, e.documentos, e.estados, gateway,
                        e.notificaciones, e.archivos);
                porHistorial.set(String.valueOf(nuevo), llamar(() -> {
                    cu.cambiarEstadoSolicitudReingreso(3, nuevo);
                    return null;
                }, gateway, e));
            }
        }
        return porTipo;
    }

    private ObjectNode pazYSalvo() throws Exception {
        ObjectNode porTipo = json.createObjectNode();
        ObjectNode crear = porTipo.putObject("crear");
        // El gateway de Paz y Salvo crea el estado inicial al guardar; el caso de uso solo lo agrega si falta
        for (String[] historial : new String[][] {null, {}, {"Enviada"}, {"  "}, {null}, {"", "ENVIADA"}}) {
            Entorno e = new Entorno();
            var gateway = mock(GestionarSolicitudPazYSalvoGatewayIntPort.class);
            when(gateway.guardar(any())).thenAnswer(i -> guardada(SolicitudPazYSalvo::new, historial));
            var cu = new GestionarSolicitudPazYSalvoCUAdapter(gateway, e.usuarios, e.documentos, e.estados, e.archivos,
                    e.notificaciones);
            crear.set(nombreHistorial(historial), llamar(() -> {
                SolicitudPazYSalvo solicitud = nueva(SolicitudPazYSalvo::new);
                solicitud.setPeriodoAcademico("2025-1");
                return cu.guardar(solicitud);
            }, gateway, e));
        }
        String[] comentarios = {null, "", "   ", "  Falta el recibo  "};
        ObjectNode cambiar = porTipo.putObject("cambiarEstado");
        for (String[] historial : HISTORIALES) {
            ObjectNode porHistorial = cambiar.putObject(nombreHistorial(historial));
            for (String nuevo : NUEVOS_ESTADOS) {
                ObjectNode porEstado = porHistorial.putObject(String.valueOf(nuevo));
                for (String comentario : comentarios) {
                    Entorno e = new Entorno();
                    var gateway = gatewayPazYSalvo(historial);
                    var cu = new GestionarSolicitudPazYSalvoCUAdapter(gateway, e.usuarios, e.documentos, e.estados,
                            e.archivos, e.notificaciones);
                    porEstado.set(String.valueOf(comentario), llamar(() -> {
                        cu.cambiarEstadoSolicitud(3, nuevo, comentario);
                        return null;
                    }, gateway, e));
                }
                // Variante con rol: el Funcionario puede actuar, ningún otro rol
                Entorno e = new Entorno();
                var gateway = gatewayPazYSalvo(historial);
                var cu = new GestionarSolicitudPazYSalvoCUAdapter(gateway, e.usuarios, e.documentos, e.estados,
                        e.archivos, e.notificaciones);
                porEstado.set("solo Funcionario", llamar(
                        () -> cu.cambiarEstadoSolicitud(3, nuevo, " visto ", "Funcionario"::equals), gateway, e));
            }
        }
        return porTipo;
    }

    private GestionarSolicitudPazYSalvoGatewayIntPort gatewayPazYSalvo(String[] historial) {
        var gateway = mock(GestionarSolicitudPazYSalvoGatewayIntPort.class);
        when(gateway.buscarPorId(anyInt())).thenAnswer(i -> Optional.of(conHistorial(SolicitudPazYSalvo::new, historial)));
        return gateway;
    }

    // ==================== Entorno y llamadas ====================

    /** Gateways comunes simulados: el usuario 7 existe, no hay documentos pendientes y nada se notifica con error. */
    private static final class Entorno {
        final GestionarDocumentosGatewayIntPort documentos = mock(GestionarDocumentosGatewayIntPort.class);
        final GestionarEstadoSolicitudGatewayIntPort estados = mock(GestionarEstadoSolicitudGatewayIntPort.class);
        final GestionarUsuarioGatewayIntPort usuarios = mock(GestionarUsuarioGatewayIntPort.class);
        final GestionarNotificacionCUIntPort notificaciones = mock(GestionarNotificacionCUIntPort.class);
        final GestionarArchivosCUIntPort archivos = mock(GestionarArchivosCUIntPort.class);

        Entorno() {
            when(usuarios.buscarUsuarioPorId(anyInt())).thenAnswer(i -> Optional.of(usuario()));
            when(usuarios.obtenerUsuarioPorId(anyInt())).thenAnswer(i -> usuario());
        }
    }

    private ObjectNode llamar(Callable<Object> llamada, Object gatewaySolicitudes, Entorno e) {
        ObjectNode resultado = json.createObjectNode();
        inicioLlamada = new Date();
        Object devuelto = null;
        try {
            devuelto = llamada.call();
        } catch (Exception ex) {
            resultado.put("excepcion", ex.getClass().getSimpleName());
            resultado.put("mensaje", ex.getMessage());
        }
        finLlamada = new Date();
        resultado.set("devuelve", describir(devuelto, devuelto));
        resultado.set("gatewaySolicitudes", invocaciones(gatewaySolicitudes, devuelto));
        resultado.set("gatewayEstados", invocaciones(e.estados, devuelto));
        resultado.set("notificaciones", invocaciones(e.notificaciones, devuelto));
        return resultado;
    }

    private ArrayNode invocaciones(Object simulado, Object devuelto) {
        ArrayNode lista = json.createArrayNode();
        for (Invocation invocacion : mockingDetails(simulado).getInvocations()) {
            ObjectNode llamada = lista.addObject();
            llamada.put("metodo", invocacion.getMethod().getName());
            ArrayNode argumentos = llamada.putArray("argumentos");
            for (Object argumento : invocacion.getArguments()) {
                argumentos.add(describir(argumento, devuelto));
            }
        }
        return lista;
    }

    /** Describe sin recorrer ciclos: una solicitud por su clase, id e historial; un estado por sus campos. */
    private JsonNode describir(Object valor, Object devuelto) {
        if (valor == null) {
            return json.nullNode();
        }
        if (valor instanceof Solicitud solicitud) {
            ObjectNode nodo = json.createObjectNode();
            nodo.put("clase", solicitud.getClass().getSimpleName());
            nodo.put("idSolicitud", solicitud.getIdSolicitud());
            nodo.put("esLaDevuelta", solicitud == devuelto);
            if (solicitud.getEstadosSolicitud() == null) {
                nodo.putNull("estadosSolicitud");
            } else {
                ArrayNode estados = nodo.putArray("estadosSolicitud");
                solicitud.getEstadosSolicitud().forEach(estado -> estados.add(describirEstado(estado, solicitud, devuelto)));
            }
            return nodo;
        }
        if (valor instanceof EstadoSolicitud estado) {
            return describirEstado(estado, null, devuelto);
        }
        if (valor instanceof Optional<?> opcional) {
            ObjectNode nodo = json.createObjectNode();
            nodo.put("clase", "Optional");
            nodo.set("valor", describir(opcional.orElse(null), devuelto));
            return nodo;
        }
        if (valor instanceof Record registro) {
            ObjectNode nodo = json.createObjectNode();
            nodo.put("clase", registro.getClass().getSimpleName());
            nodo.put("texto", registro.toString());
            return nodo;
        }
        if (valor instanceof Integer || valor instanceof String) {
            return json.valueToTree(valor);
        }
        return json.getNodeFactory().textNode(valor.getClass().getSimpleName());
    }

    private ObjectNode describirEstado(EstadoSolicitud estado, Solicitud contenedora, Object devuelto) {
        ObjectNode nodo = json.createObjectNode();
        nodo.put("idEstado", estado.getIdEstado());
        nodo.put("estadoActual", estado.getEstadoActual());
        nodo.put("comentario", estado.getComentario());
        Date fecha = estado.getFechaRegistroEstado();
        nodo.put("fecha", fecha == null ? "null"
                : !fecha.before(inicioLlamada) && (finLlamada == null || !fecha.after(finLlamada)) ? "reciente" : "anterior");
        Solicitud vinculo = estado.getObjSolicitud();
        nodo.put("objSolicitud", vinculo == null ? "null"
                : vinculo == devuelto ? "la devuelta"
                : vinculo == contenedora ? "la que lo contiene"
                : vinculo.getClass().getSimpleName() + " " + vinculo.getIdSolicitud());
        return nodo;
    }

    // ==================== Datos ====================

    private static Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNombreCompleto("Ana Pérez");
        return usuario;
    }

    private static <T extends Solicitud> T nueva(Supplier<T> constructor) {
        T solicitud = constructor.get();
        solicitud.setObjUsuario(usuario());
        return solicitud;
    }

    /** La solicitud que devuelve el gateway al guardar: id 10 y el historial dado (null = sin lista). */
    private static <T extends Solicitud> T guardada(Supplier<T> constructor, String[] historial) {
        T solicitud = conHistorial(constructor, historial);
        solicitud.setIdSolicitud(10);
        solicitud.setObjUsuario(usuario());
        return solicitud;
    }

    private static <T extends Solicitud> T conHistorial(Supplier<T> constructor, String[] historial) {
        T solicitud = constructor.get();
        solicitud.setIdSolicitud(3);
        if (historial == null) {
            solicitud.setEstadosSolicitud(null);
            return solicitud;
        }
        List<EstadoSolicitud> estados = new ArrayList<>();
        for (int i = 0; i < historial.length; i++) {
            EstadoSolicitud estado = new EstadoSolicitud();
            estado.setIdEstado(100 + i);
            estado.setEstadoActual(historial[i]);
            estado.setFechaRegistroEstado(new Date(0));
            estados.add(estado);
        }
        solicitud.setEstadosSolicitud(estados);
        return solicitud;
    }

    private static String nombreHistorial(String[] historial) {
        return historial == null ? "sin lista" : Arrays.toString(historial);
    }
}
