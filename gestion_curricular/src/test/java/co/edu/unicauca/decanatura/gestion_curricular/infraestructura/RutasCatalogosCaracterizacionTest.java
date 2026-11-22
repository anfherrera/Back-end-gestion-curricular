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
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarProgramasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarRolesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.DocenteRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ProgramaRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.RolRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocenteMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.RolMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 1): roles, programas y docentes.
 *
 * Cada operación se pide por su ruta antigua (con verbo, {@code /crearRol}, {@code /buscarRolPorId/{id}}…) y por
 * la de la convención ({@code POST /}, {@code GET /{id}}, {@code GET /?nombre=}…), con los casos de uso simulados
 * y los mappers reales. Grabada con el código anterior, las rutas nuevas responden 404; tras migrar, una corrección
 * les da la respuesta que tenía la ruta antigua y deja la antigua en 404.
 */
class RutasCatalogosCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void roles() throws Exception {
        GestionarRolesCUIntPort cu = mock(GestionarRolesCUIntPort.class);
        when(cu.guardarRol(any())).thenAnswer(inv -> new Rol(7, ((Rol) inv.getArgument(0)).getNombre(), null));
        when(cu.actualizarRol(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cu.obtenerRolPorId(1)).thenReturn(new Rol(1, "ADMIN", null));
        when(cu.eliminarRol(1)).thenReturn(true);
        when(cu.obtenerRolPorNombre("ADMIN")).thenReturn(new Rol(1, "ADMIN", null));
        when(cu.listarRoles()).thenReturn(List.of(new Rol(1, "ADMIN", null), new Rol(2, "ESTUDIANTE", null)));
        MockMvc mvc = mvc(new RolRestController(cu, Mappers.getMapper(RolMapperDominio.class)));

        String b = "/api/roles";
        ObjectNode casos = json.createObjectNode();
        // Antiguas
        caso(casos, mvc, HttpMethod.POST, b + "/crearRol", "{\"nombre\":\"NUEVO\"}");
        caso(casos, mvc, HttpMethod.POST, b + "/crearRol", "{\"nombre\":\"AB\"}");
        caso(casos, mvc, HttpMethod.POST, b + "/crearRol", "{}");
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarRol", "{\"id_rol\":3,\"nombre\":\"EDITADO\"}");
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarRol", "{\"nombre\":\"EDITADO\"}");
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/buscarRolPorId/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarRol/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarRol/9", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre?nombre=ADMIN", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre?nombre=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "/listarRoles", null);
        // Convención de EST-7
        caso(casos, mvc, HttpMethod.POST, b, "{\"nombre\":\"NUEVO\"}");
        caso(casos, mvc, HttpMethod.POST, b, "{\"nombre\":\"AB\"}");
        caso(casos, mvc, HttpMethod.POST, b, "{}");
        caso(casos, mvc, HttpMethod.PUT, b + "/3", "{\"id_rol\":3,\"nombre\":\"EDITADO\"}");
        caso(casos, mvc, HttpMethod.PUT, b + "/3", "{\"nombre\":\"EDITADO\"}");
        caso(casos, mvc, HttpMethod.PUT, b + "/3", "{\"id_rol\":5,\"nombre\":\"EDITADO\"}");
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/9", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=ADMIN", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=X", null);
        caso(casos, mvc, HttpMethod.GET, b, null);
        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404; el id de PUT sale de la ruta
        GrafoObjetos.compararConReferencia(casos, "rutas/roles.json", "rutas/correcciones/roles-est7.json");
    }

    @Test
    void programas() throws Exception {
        GestionarProgramasCUIntPort cu = mock(GestionarProgramasCUIntPort.class);
        Programa sis = new Programa(1, "SIS", "Ingeniería de Sistemas");
        when(cu.guardarPrograma(any())).thenAnswer(inv -> {
            Programa p = inv.getArgument(0);
            return new Programa(7, p.getCodigo(), p.getNombrePrograma());
        });
        when(cu.actualizarPrograma(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cu.obtenerProgramaPorId(1)).thenReturn(sis);
        when(cu.eliminarPrograma(1)).thenReturn(true);
        when(cu.buscarPorNombreParcial("Sis")).thenReturn(List.of(sis));
        when(cu.obtenerProgramaPorCodigo("SIS")).thenReturn(sis);
        when(cu.listarProgramas()).thenReturn(List.of(sis, new Programa(2, "ELE", "Ingeniería Electrónica")));
        MockMvc mvc = mvc(new ProgramaRestController(cu, Mappers.getMapper(ProgramaMapperDominio.class)));

        String b = "/api/programas";
        String nuevo = "{\"codigo\":\"NUE\",\"nombre_programa\":\"Programa nuevo\"}";
        ObjectNode casos = json.createObjectNode();
        caso(casos, mvc, HttpMethod.POST, b + "/crearPrograma", nuevo);
        caso(casos, mvc, HttpMethod.POST, b + "/crearPrograma", "{\"codigo\":\"AB\",\"nombre_programa\":\"Programa nuevo\"}");
        caso(casos, mvc, HttpMethod.POST, b + "/crearPrograma", "{\"codigo\":\"NUE\"}");
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarPrograma", conId("id_programa", 3, nuevo));
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarPrograma", nuevo);
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/buscarProgramaPorId/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarPrograma/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarPrograma/9", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre?nombre=Sis", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre?nombre=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorCodigo?codigo=SIS", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorCodigo?codigo=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "/listarProgramas", null);
        caso(casos, mvc, HttpMethod.POST, b, nuevo);
        caso(casos, mvc, HttpMethod.POST, b, "{\"codigo\":\"AB\",\"nombre_programa\":\"Programa nuevo\"}");
        caso(casos, mvc, HttpMethod.POST, b, "{\"codigo\":\"NUE\"}");
        caso(casos, mvc, HttpMethod.PUT, b + "/3", conId("id_programa", 3, nuevo));
        caso(casos, mvc, HttpMethod.PUT, b + "/3", nuevo);
        caso(casos, mvc, HttpMethod.PUT, b + "/3", conId("id_programa", 5, nuevo));
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/9", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=Sis", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "?codigo=SIS", null);
        caso(casos, mvc, HttpMethod.GET, b + "?codigo=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=Sis&codigo=SIS", null);
        caso(casos, mvc, HttpMethod.GET, b, null);
        GrafoObjetos.compararConReferencia(casos, "rutas/programas.json", "rutas/correcciones/programas-est7.json");
    }

    @Test
    void docentes() throws Exception {
        GestionarDocentesCUIntPort cu = mock(GestionarDocentesCUIntPort.class);
        Docente ana = new Docente(1, "D001", "Ana Docente");
        when(cu.guardarDocente(any())).thenAnswer(inv -> {
            Docente d = inv.getArgument(0);
            return new Docente(7, d.getCodigoDocente(), d.getNombreDocente());
        });
        when(cu.actualizarDocente(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cu.obtenerDocentePorId(1)).thenReturn(ana);
        when(cu.eliminarDocente(1)).thenReturn(true);
        when(cu.buscarPorNombreParcial("Ana")).thenReturn(List.of(ana));
        when(cu.obtenerDocentePorCodigo("D001")).thenReturn(ana);
        when(cu.listarDocentes()).thenReturn(List.of(ana, new Docente(2, "D002", "Luis Docente")));
        MockMvc mvc = mvc(new DocenteRestController(cu, Mappers.getMapper(DocenteMapperDominio.class)));

        String b = "/api/docentes";
        String nuevo = "{\"codigo_docente\":\"D009\",\"nombre_docente\":\"Docente nuevo\"}";
        ObjectNode casos = json.createObjectNode();
        caso(casos, mvc, HttpMethod.POST, b + "/crearDocente", nuevo);
        caso(casos, mvc, HttpMethod.POST, b + "/crearDocente", "{\"codigo_docente\":\"AB\",\"nombre_docente\":\"Docente nuevo\"}");
        caso(casos, mvc, HttpMethod.POST, b + "/crearDocente", "{\"codigo_docente\":\"D009\"}");
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarDocente", conId("id_docente", 3, nuevo));
        caso(casos, mvc, HttpMethod.PUT, b + "/actualizarDocente", nuevo);
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/buscarDocentePorId/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarDocente/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/eliminarDocente/9", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre?nombre=Ana", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre?nombre=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorCodigo?codigo=D001", null);
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorCodigo?codigo=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "/listarDocentes", null);
        caso(casos, mvc, HttpMethod.POST, b, nuevo);
        caso(casos, mvc, HttpMethod.POST, b, "{\"codigo_docente\":\"AB\",\"nombre_docente\":\"Docente nuevo\"}");
        caso(casos, mvc, HttpMethod.POST, b, "{\"codigo_docente\":\"D009\"}");
        caso(casos, mvc, HttpMethod.PUT, b + "/3", conId("id_docente", 3, nuevo));
        caso(casos, mvc, HttpMethod.PUT, b + "/3", nuevo);
        caso(casos, mvc, HttpMethod.PUT, b + "/3", conId("id_docente", 5, nuevo));
        for (String id : List.of("1", "9", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/" + id, null);
        }
        caso(casos, mvc, HttpMethod.DELETE, b + "/1", null);
        caso(casos, mvc, HttpMethod.DELETE, b + "/9", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=Ana", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "?codigo=D001", null);
        caso(casos, mvc, HttpMethod.GET, b + "?codigo=X", null);
        caso(casos, mvc, HttpMethod.GET, b + "?nombre=Ana&codigo=D001", null);
        caso(casos, mvc, HttpMethod.GET, b, null);
        GrafoObjetos.compararConReferencia(casos, "rutas/docentes.json");
    }

    private static MockMvc mvc(Object controlador) {
        return SoporteMvc.standaloneSetup(controlador).setControllerAdvice(new RestApiExceptionHandler()).build();
    }

    private static String conId(String campo, int id, String cuerpo) {
        return "{\"" + campo + "\":" + id + "," + cuerpo.substring(1);
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
        } else {
            caso.set("cuerpo", json.readTree(texto));
        }
        casos.set(metodo + " " + ruta + (cuerpo == null ? "" : " " + cuerpo), caso);
    }
}
