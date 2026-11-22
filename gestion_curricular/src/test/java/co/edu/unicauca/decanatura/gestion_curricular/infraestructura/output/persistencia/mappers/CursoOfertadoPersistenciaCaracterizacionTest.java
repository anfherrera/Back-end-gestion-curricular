package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarCursoOfertadoVeranoGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.ProyeccionValores;

/**
 * Caracterización previa a DUP-10 del gateway de cursos de verano, que mapea con el bean
 * {@code mapperCurso} (estrategia estándar, sin {@code skipNull}, con estados e inscritos copiados
 * a mano en sus post-convertidores): curso completo con sus solicitudes, curso con nulos, creación,
 * actualización y cambios de cupo.
 */
class CursoOfertadoPersistenciaCaracterizacionTest {

    private final List<Object> guardados = new ArrayList<>();

    @Test
    void cursoOfertado() throws Exception {
        CursoOfertadoVeranoRepositoryInt cursos = mock(CursoOfertadoVeranoRepositoryInt.class);
        EstadoCursoOfertadoRepositoryInt estados = mock(EstadoCursoOfertadoRepositoryInt.class);
        DocenteRepositoryInt docentes = mock(DocenteRepositoryInt.class);
        MateriaRepositoryInt materias = mock(MateriaRepositoryInt.class);
        when(cursos.save(any())).thenAnswer(inv -> {
            CursoOfertadoVeranoEntity curso = inv.getArgument(0);
            guardados.add(curso);
            if (curso.getId_curso() == null) {
                curso.setId_curso(40);
            }
            return curso;
        });
        when(estados.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        when(cursos.findById(4)).thenAnswer(inv -> Optional.of(cursoCompleto()));
        when(cursos.findById(5)).thenAnswer(inv -> Optional.of(cursoNulo()));
        when(cursos.findById(40)).thenAnswer(inv -> Optional.of(cursoCompleto()));
        when(cursos.findAll()).thenAnswer(inv -> List.of(cursoCompleto(), cursoNulo()));
        when(cursos.listarPorPeriodo("2025-1")).thenAnswer(inv -> List.of(cursoCompleto()));
        when(cursos.findByIdConEstados(4)).thenAnswer(inv -> cursoCompleto());
        MateriaEntity materia = new MateriaEntity();
        materia.setId_materia(5);
        materia.setNombre("Cálculo");
        when(materias.findById(5)).thenReturn(Optional.of(materia));
        DocenteEntity docente = new DocenteEntity();
        docente.setId_docente(6);
        docente.setNombre_docente("Luis");
        when(docentes.findById(6)).thenReturn(Optional.of(docente));
        GestionarCursoOfertadoVeranoGatewayImplAdapter gateway = new GestionarCursoOfertadoVeranoGatewayImplAdapter(
                cursos, mock(UsuarioRepositoryInt.class), estados, docentes, materias);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("obtenerCursoPorId 4", GrafoObjetos.resultado(() -> gateway.obtenerCursoPorId(4)));
        casos.set("obtenerCursoPorId 5", GrafoObjetos.resultado(() -> gateway.obtenerCursoPorId(5)));
        casos.set("obtenerCursoPorId 9", GrafoObjetos.resultado(() -> gateway.obtenerCursoPorId(9)));
        casos.set("listarTodos", GrafoObjetos.resultado(gateway::listarTodos));
        casos.set("listarPorPeriodo", GrafoObjetos.resultado(() -> gateway.listarPorPeriodo("2025-1")));
        casos.set("guardarCambiosCurso", guardado(() -> gateway.guardarCambiosCurso(4, 30, "222", "Publicado")));
        casos.set("guardarCambiosCurso inexistente", guardado(() -> gateway.guardarCambiosCurso(9, 30, null, null)));
        CursoOfertadoVerano nuevo = GrafosSolicitud.curso(GrafosSolicitud.usuario());
        nuevo.setIdCurso(null);
        casos.set("crearCurso", guardado(() -> gateway.crearCurso(nuevo)));
        CursoOfertadoVerano sinGrupo = GrafosSolicitud.curso(null);
        sinGrupo.setGrupo(null);
        casos.set("crearCurso sin grupo", guardado(() -> gateway.crearCurso(sinGrupo)));
        CursoOfertadoVerano conSolicitudes = GrafosSolicitud.completar(new SolicitudCursoVeranoInscripcion())
                .getObjCursoOfertadoVerano();
        casos.set("actualizarCurso completo", guardado(() -> gateway.actualizarCurso(conSolicitudes,
                new EstadoCursoOfertado(null, "Preinscripcion", GrafosSolicitud.FECHA_3, conSolicitudes))));
        CursoOfertadoVerano conNulos = new CursoOfertadoVerano();
        conNulos.setIdCurso(5);
        conNulos.setEstadosCursoOfertados(null);
        conNulos.setEstudiantesInscritos(null);
        conNulos.setSolicitudes(null);
        casos.set("actualizarCurso nulos", guardado(() -> gateway.actualizarCurso(conNulos, null)));
        casos.set("actualizarCurso estado sin valor", guardado(() -> gateway.actualizarCurso(
                GrafosSolicitud.curso(null), new EstadoCursoOfertado(null, null, null, null))));
        ProyeccionValores.compararConReferencia(casos, "persistencia/curso-ofertado.json",
                "persistencia/correcciones/curso-ofertado-bug37.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/curso-ofertado.json",
                "persistencia/correcciones/curso-ofertado-bug37.json");
    }

    private static CursoOfertadoVeranoEntity cursoCompleto() {
        return GrafosSolicitud.completar(new SolicitudCursoVeranoInscripcionEntity()).getObjCursoOfertadoVerano();
    }

    private static CursoOfertadoVeranoEntity cursoNulo() {
        CursoOfertadoVeranoEntity curso = new CursoOfertadoVeranoEntity();
        curso.setId_curso(5);
        curso.setEstadosCursoOfertados(null);
        curso.setEstudiantesInscritos(null);
        curso.setSolicitudes(null);
        return curso;
    }

    private Object guardar(Object entidad) {
        guardados.add(entidad);
        return entidad;
    }

    /** Lo que se envió a {@code save} y lo que devolvió el gateway. */
    private ObjectNode guardado(Callable<?> llamada) {
        guardados.clear();
        ObjectNode caso = JsonNodeFactory.instance.objectNode();
        caso.set("resultado", GrafoObjetos.resultado(llamada));
        caso.set("guardados", GrafoObjetos.volcar(new ArrayList<>(guardados)));
        return caso;
    }
}
