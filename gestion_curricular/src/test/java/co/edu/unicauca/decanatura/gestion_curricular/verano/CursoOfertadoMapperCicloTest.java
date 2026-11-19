package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocenteMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;

/**
 * Un curso cargado con el grafo completo (cada estado apunta de vuelta a su curso, como lo deja
 * {@code SolicitudMapperPersistencia}) se mapeaba curso → estados → curso → ... hasta StackOverflowError: 500 al
 * preinscribir. El estado se responde sin su curso, como siempre salio en la respuesta.
 */
class CursoOfertadoMapperCicloTest {

    @Test
    void cursoConEstadosQueApuntanAlCursoSeMapeaSinRecursion() {
        CursosOfertadosMapperDominioImpl mapper = new CursosOfertadosMapperDominioImpl();
        ReflectionTestUtils.setField(mapper, "docenteMapperDominio", mock(DocenteMapperDominio.class));
        ReflectionTestUtils.setField(mapper, "usuarioMapperDominio", mock(UsuarioMapperDominio.class));

        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(7);
        EstadoCursoOfertado estado = new EstadoCursoOfertado();
        estado.setId_estado(3);
        estado.setEstado_actual("Preinscripcion");
        estado.setObjCursoOfertadoVerano(curso);
        curso.setEstadosCursoOfertados(new ArrayList<>(List.of(estado)));

        CursosOfertadosDTORespuesta dto = mapper.mapearDeCursoOfertadoARespuesta(curso);
        CursosOfertadosDTORespuesta disponible = mapper.mapearDeCursoOfertadoARespuestaDisponible(curso);

        assertEquals(7, dto.getId_curso());
        assertEquals("Preinscripcion", dto.getEstado());
        assertEquals(1, dto.getEstadosCursoOfertados().size());
        assertEquals(3, dto.getEstadosCursoOfertados().get(0).getId_estado());
        assertNull(dto.getEstadosCursoOfertados().get(0).getObjCursoOfertadoVerano());
        assertNull(disponible.getEstadosCursoOfertados().get(0).getObjCursoOfertadoVerano());
    }
}
