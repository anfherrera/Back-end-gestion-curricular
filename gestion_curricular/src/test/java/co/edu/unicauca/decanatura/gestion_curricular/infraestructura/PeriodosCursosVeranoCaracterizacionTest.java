package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a BUG-51: {@code GET /api/cursos-verano/cursos/periodos}, que pide el selector de períodos
 * de "Cursos ofertados". El caso de uso es el real; el gateway de cursos, simulado, devuelve cursos en estados
 * visibles y no visibles, con períodos repetidos, con espacios, vacíos y desordenados. Pide como estudiante y como
 * funcionario. Guarda el estado y el cuerpo.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PeriodosCursosVeranoCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();

    @Autowired MockMvc mvc;
    @MockBean GestionarCursoOfertadoVeranoGatewayIntPort cursoGateway;

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void periodosConCursos() throws Exception {
        when(cursoGateway.listarTodos()).thenReturn(List.of(
                curso(1, "2024-1", "Inscripcion"),
                curso(2, "2025-2", "Publicado"),
                curso(3, " 2024-2 ", "Preinscripcion"),
                curso(4, "2025-2", "Preinscripcion"),
                curso(5, "2026-1", "Borrador"),
                curso(6, "2023-2", "Cerrado"),
                curso(7, "  ", "Publicado"),
                curso(8, null, "Publicado"),
                curso(9, "2022-1", null)));

        ObjectNode casos = json.createObjectNode();
        for (String quien : List.of("estudiante", "funcionario")) {
            String rol = quien.equals("estudiante") ? "Estudiante" : "Funcionario";
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    quien + "@prueba.test", "x", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
            var respuesta = mvc.perform(get("/api/cursos-verano/cursos/periodos")).andReturn().getResponse();
            SecurityContextHolder.clearContext();
            ObjectNode caso = casos.putObject("GET /api/cursos-verano/cursos/periodos [" + quien + "]");
            caso.put("status", respuesta.getStatus());
            caso.set("cuerpo", json.readTree(respuesta.getContentAsString(StandardCharsets.UTF_8)));
        }

        // BUG-51: la ruta existe y da los períodos con cursos visibles, sin repetir y del más reciente al más antiguo
        GrafoObjetos.compararConReferencia(casos, "verano-endpoints/periodos-cursos.json",
                "verano-endpoints/correcciones/periodos-cursos-bug51.json");
    }

    private static CursoOfertadoVerano curso(int id, String periodo, String estado) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(id);
        curso.setPeriodoAcademico(periodo);
        if (estado != null) {
            EstadoCursoOfertado e = new EstadoCursoOfertado();
            e.setIdEstado(id * 10);
            e.setEstadoActual(estado);
            curso.getEstadosCursoOfertados().add(e);
        }
        return curso;
    }
}
