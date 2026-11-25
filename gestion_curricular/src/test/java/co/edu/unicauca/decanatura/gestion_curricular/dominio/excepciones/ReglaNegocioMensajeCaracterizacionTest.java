package co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarArchivosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización del mensaje de {@link ReglaNegocioExcepcion}: lo que devuelven {@code getMessage()},
 * {@code formatException()} y {@code toString()}, y lo que queda cuando un caso de uso la envuelve con
 * {@code getMessage()} (los siete métodos de {@link GestionarArchivosCUAdapter} con un gateway que la lanza).
 */
@DisplayName("ReglaNegocioExcepcion: mensaje directo y envuelto")
class ReglaNegocioMensajeCaracterizacionTest {

    private static final ArchivoSubido ARCHIVO = mock(ArchivoSubido.class);

    @Test
    void mensajeDirectoYEnvuelto() throws Exception {
        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("directa con texto", describir(new ReglaNegocioExcepcion("regla rota")));
        casos.set("directa sin texto", describir(new ReglaNegocioExcepcion(null)));

        Function<GestionarArchivosCUAdapter, Object>[] operaciones = operaciones();
        String[] nombres = { "saveFile", "saveFile organizado", "getFile", "getFileByPath", "moverArchivoAOrganizado",
                "getFileAsPath", "getFileByPathAsPath" };
        RuntimeException regla = new ReglaNegocioExcepcion("disco lleno");
        IOException entradaSalida = new IOException("disco lleno");
        for (int i = 0; i < operaciones.length; i++) {
            casos.set("archivos " + nombres[i] + " gateway regla", envuelta(operaciones[i], regla));
            casos.set("archivos " + nombres[i] + " gateway io", envuelta(operaciones[i], entradaSalida));
        }
        GrafoObjetos.compararConReferencia(casos, "excepciones/regla-negocio-mensaje.json",
                "excepciones/correcciones/regla-negocio-mensaje-bug41.json");
    }

    @SuppressWarnings("unchecked")
    private static Function<GestionarArchivosCUAdapter, Object>[] operaciones() {
        return new Function[] {
                (Function<GestionarArchivosCUAdapter, Object>) cu -> cu.saveFile(ARCHIVO, "a.pdf", "pdf"),
                (Function<GestionarArchivosCUAdapter, Object>) cu -> cu.saveFile(ARCHIVO, "a.pdf", "pdf", "reingreso", 1),
                (Function<GestionarArchivosCUAdapter, Object>) cu -> cu.getFile("a.pdf"),
                (Function<GestionarArchivosCUAdapter, Object>) cu -> cu.getFileByPath("reingreso/a.pdf"),
                (Function<GestionarArchivosCUAdapter, Object>) cu -> cu.moverArchivoAOrganizado("a.pdf", "a.pdf",
                        "reingreso", 1),
                (Function<GestionarArchivosCUAdapter, Object>) cu -> cu.getFileAsPath("a.pdf"),
                (Function<GestionarArchivosCUAdapter, Object>) cu -> cu.getFileByPathAsPath("reingreso/a.pdf") };
    }

    /** Llama a la operación con un gateway cuyos siete métodos lanzan {@code error} y describe lo que sale. */
    private static ObjectNode envuelta(Function<GestionarArchivosCUAdapter, Object> operacion, Exception error)
            throws IOException {
        GestionarArchivosGatewayIntPort gateway = mock(GestionarArchivosGatewayIntPort.class);
        when(gateway.saveFile(any(), anyString(), anyString())).thenThrow(error);
        when(gateway.saveFile(any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(error);
        when(gateway.getFile(anyString())).thenThrow(error);
        when(gateway.getFileByPath(anyString())).thenThrow(error);
        when(gateway.moverArchivoAOrganizado(anyString(), anyString(), anyString(), anyInt())).thenThrow(error);
        when(gateway.getFileAsPath(anyString())).thenThrow(error);
        when(gateway.getFileByPathAsPath(anyString())).thenThrow(error);
        try {
            operacion.apply(new GestionarArchivosCUAdapter(gateway));
            return JsonNodeFactory.instance.objectNode().put("excepcion", "ninguna");
        } catch (RuntimeException e) {
            return describir(e);
        }
    }

    private static ObjectNode describir(RuntimeException e) {
        ObjectNode nodo = JsonNodeFactory.instance.objectNode();
        nodo.put("excepcion", e.getClass().getSimpleName());
        nodo.put("getMessage", e.getMessage());
        nodo.put("toString", e.toString());
        if (e instanceof GestionCurricularException gestion) {
            nodo.put("formatException", gestion.formatException());
            nodo.put("codigoError", gestion.getCodigoError().getCodigo());
        }
        return nodo;
    }
}
