package co.edu.unicauca.decanatura.gestion_curricular.ecaes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudEcaesRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.TipoDocumentoController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadoSolicitudMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudEcaesMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;

class TiposDocumentoCaracterizacionTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaCatalogoYTiposAceptadosPorEcaes() throws Exception {
        var casoDeUso = mock(GestionarSolicitudEcaesCUIntPort.class);
        when(casoDeUso.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        var autorizacion = new Autorizacion(null, null, null, null) {
            @Override
            public Integer idSolicitante(Integer idEnviado) {
                return idEnviado;
            }
        };
        var mapper = new SolicitudEcaesMapperDominioImpl();
        ReflectionTestUtils.setField(mapper, "usuarioMapperDominio", mock(UsuarioMapperDominio.class));
        var mvc = MockMvcBuilders.standaloneSetup(new TipoDocumentoController(),
            new SolicitudEcaesRestController(casoDeUso, mapper, null, autorizacion, null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        var resultados = json.createObjectNode();
        resultados.set("catalogo", respuesta(mvc, get("/api/tipos-documento/todos")));
        for (String codigo : new String[]{"CC", "TI", "CE", "PA", "RC", "NIT", "NUIP", "cc", "XX"}) {
            var caso = resultados.putObject(codigo);
            caso.set("validar", respuesta(mvc, get("/api/tipos-documento/validar/{codigo}", codigo)));
            caso.set("info", respuesta(mvc, get("/api/tipos-documento/info/{codigo}", codigo)));
            caso.set("crearEcaes", respuesta(mvc, crearEcaes(codigo)));
        }

        comparar("tipos-documento.json", resultados);

        // Completa el mapper para fijar también las respuestas de creación exitosas.
        ReflectionTestUtils.setField(mapper, "estadoSolicitudMapperDominio", mock(EstadoSolicitudMapperDominio.class));
        var solicitudesValidas = json.createObjectNode();
        for (String codigo : new String[]{"CC", "CE"}) {
            var resultado = respuesta(mvc, crearEcaes(codigo));
            assertThat(resultado.path("status").asInt()).isEqualTo(201);
            solicitudesValidas.set(codigo, resultado);
        }
        comparar("tipos-documento-ecaes-validos.json", solicitudesValidas);
    }

    private void comparar(String archivo, ObjectNode resultados) throws Exception {
        var referencia = Path.of("src/test/resources/referencias/ecaes", archivo);
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.createDirectories(referencia.getParent());
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(referencia)));
    }


    private RequestBuilder crearEcaes(String codigo) {
        return post("/api/solicitudes-ecaes/crearSolicitud-Ecaes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "nombre_solicitud": "Solicitud ECAES",
              "fecha_registro_solicitud": "2020-01-01T00:00:00Z",
              "periodo_academico": "2020-1",
              "tipoDocumento": "%s",
              "numero_documento": "12345",
              "fecha_expedicion": "2019-01-01T00:00:00Z",
              "fecha_nacimiento": "2000-01-01T00:00:00Z",
              "objUsuario": {
                "id_usuario": 1,
                "nombre_completo": "Ana Ejemplo",
                "codigo": "12345",
                "correo": "ana@unicauca.edu.co",
                "id_rol": 1,
                "id_programa": 1
              }
            }
            """.formatted(codigo));
    }

    private ObjectNode respuesta(MockMvc mvc, RequestBuilder peticion) throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        var cuerpo = json.readTree(respuesta.getContentAsByteArray());
        if (cuerpo instanceof ObjectNode objeto) {
            for (String campo : new String[]{"fecha_registro_solicitud", "fecha_expedicion", "fecha_nacimiento", "timestamp"}) {
                if (objeto.hasNonNull(campo)) {
                    objeto.put(campo, "<fecha>");
                }
            }
        }
        return json.valueToTree(Map.of("status", respuesta.getStatus(), "body", cuerpo));
    }
}
