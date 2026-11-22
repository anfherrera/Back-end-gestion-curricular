package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarCursoOfertadoVeranoGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafosSolicitud;

/**
 * Caracterización de {@code listarPorFiltros} con el gateway de cursos real: qué cursos ve cada programa según el
 * programa de sus inscritos, y qué rol y programa llegan de cada inscrito.
 */
@DisplayName("Cursos de verano: filtro por programa según los inscritos que entrega el gateway")
class InscritosProgramaCursoVeranoCaracterizacionTest {

    @Test
    void filtroPorProgramaConLosInscritosDelGateway() throws Exception {
        CursoOfertadoVeranoRepositoryInt repositorio = mock(CursoOfertadoVeranoRepositoryInt.class);
        when(repositorio.findAll()).thenAnswer(inv -> List.of(cursoSoloConInscrito()));
        GestionarCursoOfertadoVeranoGatewayImplAdapter gateway = new GestionarCursoOfertadoVeranoGatewayImplAdapter(
                repositorio, mock(UsuarioRepositoryInt.class), mock(EstadoCursoOfertadoRepositoryInt.class),
                mock(DocenteRepositoryInt.class), mock(MateriaRepositoryInt.class));
        GestionarCursoOfertadoVeranoCUAdapter casoDeUso = new GestionarCursoOfertadoVeranoCUAdapter(gateway,
                mock(GestionarSolicitudGatewayIntPort.class), mock(GestionarUsuarioGatewayIntPort.class),
                mock(GestionarMateriasIntPort.class), mock(GestionarDocenteGatewayIntPort.class),
                mock(GestionarSalonesIntPort.class), mock(GestionarNotificacionCUIntPort.class));

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        for (Integer idPrograma : new Integer[] { null, 1, 2 }) {
            ArrayNode ids = casos.putArray("listarPorFiltros idPrograma=" + idPrograma);
            for (CursoOfertadoVerano curso : casoDeUso.listarPorFiltros(null, idPrograma, false)) {
                ids.add(curso.getIdCurso());
            }
        }
        CursoOfertadoVerano curso = gateway.listarTodos().get(0);
        casos.set("inscrito rol", GrafoObjetos.volcar(curso.getEstudiantesInscritos().get(0).getObjRol()));
        casos.set("inscrito programa", GrafoObjetos.volcar(curso.getEstudiantesInscritos().get(0).getObjPrograma()));
        GrafoObjetos.compararConReferencia(casos, "verano-endpoints/inscritos-programa.json",
                "verano-endpoints/correcciones/inscritos-programa-bug37.json");
    }

    /** Curso 4 con una inscrita del programa 1 y sin solicitudes: solo los inscritos deciden el programa. */
    private static CursoOfertadoVeranoEntity cursoSoloConInscrito() {
        CursoOfertadoVeranoEntity curso = GrafosSolicitud.cursoEntity(GrafosSolicitud.usuarioEntity());
        curso.setSolicitudes(new ArrayList<>());
        return curso;
    }
}
