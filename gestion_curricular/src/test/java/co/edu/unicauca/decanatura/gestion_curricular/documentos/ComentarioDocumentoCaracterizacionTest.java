package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * HTTP de la ruta que comenta un documento, antes de quitar la ñ de {@code /añadirComentario} (EST-7).
 * Caso de uso real, gateway simulado y seguridad de método activa. {@link #ruta} da la ruta vigente; el caso
 * {@code ruta-con-enie} usa siempre la ruta original, para fijar qué responde después del cambio.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ComentarioDocumentoCaracterizacionTest {
    private static final String RUTA_CON_ENIE = "/api/documentos/añadirComentario";
    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/documentos/comentario-documento.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired MockMvc mvc;
    @MockitoBean GestionarDocumentosGatewayIntPort documentos;

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void comentarDocumento() throws Exception {
        ObjectNode r = JSON.createObjectNode();
        caso(r, "comenta", ruta(9), "{\"idDocumento\":9,\"comentario\":\"  hola  \"}", "Funcionario");
        caso(r, "sin-comentario-previo", ruta(10), "{\"idDocumento\":10,\"comentario\":\"hola\"}", "Coordinador");
        // Desde el paso 3 de EST-7 el id va en la ruta, y el cuerpo ya no necesita idDocumento
        caso(r, "sin-id", ruta(9), "{\"comentario\":\"hola\"}", "Funcionario");
        caso(r, "comentario-blanco", ruta(9), "{\"idDocumento\":9,\"comentario\":\"   \"}", "Funcionario");
        caso(r, "inexistente", ruta(8), "{\"idDocumento\":8,\"comentario\":\"hola\"}", "Funcionario");
        caso(r, "estudiante", ruta(9), "{\"idDocumento\":9,\"comentario\":\"hola\"}", "Estudiante");
        caso(r, "ruta-con-enie", RUTA_CON_ENIE, "{\"idDocumento\":9,\"comentario\":\"hola\"}", "Funcionario");
        // EST-7: la ruta pierde la ñ y la original responde 404; en el paso 3 pasa a PUT /{id}/comentarios
        comparar(r, "documentos/correcciones/comentario-documento-est7.json",
                "documentos/correcciones/comentario-documento-est7-paso3.json");
    }

    /** Ruta vigente para comentar el documento {@code id}. */
    private static String ruta(int id) {
        return "/api/documentos/" + id + "/comentarios";
    }

    private void caso(ObjectNode r, String clave, String ruta, String cuerpo, String... roles) throws Exception {
        reset(documentos);
        Documento conComentario = new Documento();
        conComentario.setIdDocumento(9);
        conComentario.setComentario("previo");
        when(documentos.buscarDocumentoId(9)).thenReturn(conComentario);
        Documento sinComentario = new Documento();
        sinComentario.setIdDocumento(10);
        when(documentos.buscarDocumentoId(10)).thenReturn(sinComentario);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "sin.usuario@example.test", "x",
                Arrays.stream(roles).map(rol -> new SimpleGrantedAuthority("ROLE_" + rol)).toList()));

        var respuesta = mvc.perform(put(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse();
        ObjectNode valor = r.putObject(clave);
        valor.put("http", respuesta.getStatus());
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        valor.set("json", texto.isEmpty() ? null : JSON.readTree(texto));
        ArgumentCaptor<Documento> guardado = ArgumentCaptor.forClass(Documento.class);
        verify(documentos, atMost(1)).actualizarDocumento(guardado.capture());
        valor.put("guardado", guardado.getAllValues().isEmpty() ? null
                : guardado.getValue().getComentario().replaceAll("\\[[^\\]]*\\]", "[<fecha>]"));
    }

    /** Compara con la referencia, superponiendo en orden las correcciones indicadas. */
    private void comparar(JsonNode actual, String... rutasCorreccion) throws Exception {
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(REFERENCIA.getParent());
            Files.writeString(REFERENCIA, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + "\n");
        }
        JsonNode esperado = JSON.readTree(Files.readString(REFERENCIA));
        for (String rutaCorreccion : rutasCorreccion) {
            esperado = Correcciones.aplicar(esperado, rutaCorreccion);
        }
        assertThat(actual).isEqualTo(esperado);
    }
}
