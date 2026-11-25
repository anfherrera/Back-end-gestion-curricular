package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.UsuarioRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.RolMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.JwtUtil;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.LoginRateLimiter;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.SecurityAuditService;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 1): usuarios. Como
 * {@link RutasCatalogosCaracterizacionTest}: cada operación por su ruta antigua y por la nueva, con el caso de
 * uso simulado y el mapper real. La autorización ({@code @PreAuthorize}) no se evalúa aquí; la cubre
 * {@code AutorizacionPorRolTest}. {@code /login} no cambia de ruta.
 */
class RutasUsuarioCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void usuarios() throws Exception {
        GestionarUsuarioCUIntPort cu = mock(GestionarUsuarioCUIntPort.class);
        when(cu.crearUsuario(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(21);
            u.setObjRol(new Rol(u.getObjRol().getIdRol(), "Estudiante", null));
            u.setObjPrograma(new Programa(u.getObjPrograma().getIdPrograma(), "SIS", "Ingeniería de Sistemas"));
            return u;
        });
        when(cu.actualizarUsuario(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cu.obtenerUsuarioPorId(1)).thenAnswer(inv -> usuario(1));
        when(cu.eliminarUsuario(1)).thenReturn(true);
        when(cu.listarUsuarios()).thenAnswer(inv -> List.of(usuario(1), usuario(2)));
        AuthenticationManager autenticacion = mock(AuthenticationManager.class);
        when(autenticacion.authenticate(any())).thenThrow(new BadCredentialsException("x"));
        UsuarioMapperDominioImpl mapper = new UsuarioMapperDominioImpl();
        ReflectionTestUtils.setField(mapper, "programaMapperDominio", new ProgramaMapperDominioImpl());
        ReflectionTestUtils.setField(mapper, "rolMapperDominio", new RolMapperDominioImpl());
        MockMvc mvc = SoporteMvc.standaloneSetup(new UsuarioRestController(cu, mapper, autenticacion,
                        // Clases reales: Mockito no simula clases concretas en JDK 25 aquí; el login fallido no usa el JWT
                        new JwtUtil(), mock(UserDetailsService.class), new SecurityAuditService(),
                        new LoginRateLimiter(5, 900, 900)))
                .setControllerAdvice(new RestApiExceptionHandler()).build();

        String b = "/api/usuarios";
        String nuevo = "{\"nombre_completo\":\"Ana Ruiz\",\"id_rol\":2,\"codigo\":\"1045\",\"cedula\":\"123456\","
                + "\"correo\":\"ana@unicauca.edu.co\",\"password\":\"clave\",\"estado_usuario\":true,\"id_programa\":1}";
        String invalido = "{\"nombre_completo\":\"Ana Ruiz\",\"id_rol\":2,\"codigo\":\"1045\","
                + "\"correo\":\"ana@gmail.com\",\"id_programa\":1}";
        ObjectNode casos = json.createObjectNode();
        // Antiguas
        caso(casos, mvc, HttpMethod.POST, b + "/crearUsuario", nuevo);
        caso(casos, mvc, HttpMethod.POST, b + "/crearUsuario", invalido);
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarUsuario", conId(3, nuevo));
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarUsuario", nuevo);
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarUsuario", conId(3, invalido));
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/buscarUsuarioPorId/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarUsuario/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarUsuario/9", null);
        caso(casos, mvc, HttpMethod.GET, b + "/listarUsuarios", null);
        caso(casos, mvc, HttpMethod.PUT, b + "/cambiarEstado/1", null);
        caso(casos, mvc, HttpMethod.PUT, b + "/cambiarEstado/9", null);
        caso(casos, mvc, HttpMethod.POST, b + "/login", "{\"correo\":\"ana@unicauca.edu.co\",\"password\":\"mala\"}");
        // Convención de EST-7
        caso(casos, mvc, HttpMethod.POST, b, nuevo);
        caso(casos, mvc, HttpMethod.POST, b, invalido);
        caso(casos, mvc, HttpMethod.PUT, b + "/3", conId(3, nuevo));
        caso(casos, mvc, HttpMethod.PUT, b + "/3", nuevo);
        caso(casos, mvc, HttpMethod.PUT, b + "/3", conId(5, nuevo));
        caso(casos, mvc, HttpMethod.PUT, b + "/3", conId(3, invalido));
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/9", null);
        caso(casos, mvc, HttpMethod.GET, b, null);
        caso(casos, mvc, HttpMethod.PUT, b + "/1/estado", null);
        caso(casos, mvc, HttpMethod.PUT, b + "/9/estado", null);
        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404; el id de PUT sale de la ruta
        GrafoObjetos.compararConReferencia(casos, "rutas/usuarios.json", "rutas/correcciones/usuarios-est7.json");
    }

    private static Usuario usuario(int id) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombreCompleto("Usuario " + id);
        u.setCodigo("C" + id);
        u.setCedula("10000" + id);
        u.setCorreo("u" + id + "@unicauca.edu.co");
        u.setEstadoUsuario(true);
        u.setObjRol(new Rol(2, "Estudiante", null));
        u.setObjPrograma(new Programa(1, "SIS", "Ingeniería de Sistemas"));
        return u;
    }

    private static String conId(int id, String cuerpo) {
        return "{\"id_usuario\":" + id + "," + cuerpo.substring(1);
    }

    /** Guarda el estado, el tipo de contenido y el cuerpo, con la clave "MÉTODO ruta cuerpo". */
    private void caso(ObjectNode casos, MockMvc mvc, HttpMethod metodo, String ruta, String cuerpo) throws Exception {
        MockHttpServletRequestBuilder peticion = request(metodo, ruta);
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        ObjectNode caso = json.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        if (texto.isEmpty()) {
            caso.putNull("cuerpo");
        } else if (texto.startsWith("{") || texto.startsWith("[")) {
            caso.set("cuerpo", json.readTree(texto));
        } else {
            caso.put("cuerpo", texto);
        }
        casos.set(metodo + " " + ruta + (cuerpo == null ? "" : " " + cuerpo), caso);
    }
}
