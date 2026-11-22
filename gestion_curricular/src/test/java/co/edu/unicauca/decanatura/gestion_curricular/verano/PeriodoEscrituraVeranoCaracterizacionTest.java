package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarSolicitudCursoVeranoGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafosSolicitud;

/**
 * El período académico que guarda {@link GestionarSolicitudCursoVeranoGatewayImplAdapter} al crear una
 * preinscripción o una inscripción, con el período informado con espacios, en blanco o ausente (BUG-17).
 * Para cada caso fija el período de la solicitud y del curso que se envían a {@code save}. El período actual,
 * que depende de la fecha, se sustituye por {@code <actual>} solo si coincide con el de hoy.
 */
class PeriodoEscrituraVeranoCaracterizacionTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/persistencia/periodo-escritura-verano.json");
    private static final String[] PERIODOS = { " 2025-2 ", "   ", null };

    private final List<SolicitudEntity> guardadas = new ArrayList<>();

    @Test
    void periodoGuardadoAlCrear() throws Exception {
        SolicitudRepositoryInt repositorio = mock(SolicitudRepositoryInt.class);
        CursoOfertadoVeranoRepositoryInt cursos = mock(CursoOfertadoVeranoRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> {
            guardadas.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        CursoOfertadoVeranoEntity sinPeriodo = GrafosSolicitud.cursoEntity(GrafosSolicitud.usuarioEntity());
        sinPeriodo.setId_curso(5);
        sinPeriodo.setPeriodo_academico(null);
        when(cursos.existsById(5)).thenReturn(true);
        when(cursos.findById(5)).thenReturn(Optional.of(sinPeriodo));
        GestionarSolicitudCursoVeranoGatewayImplAdapter gateway = new GestionarSolicitudCursoVeranoGatewayImplAdapter(
                repositorio, cursos, mock(EstadoSolicitudRepositoryInt.class));

        ObjectNode casos = JSON.createObjectNode();
        for (String periodo : PERIODOS) {
            String clave = periodo == null ? "null" : "[" + periodo + "]";
            // El período va en la solicitud: apertura (id 0) y curso existente sin período (id 5)
            for (int idCurso : new int[] { 0, 5 }) {
                SolicitudCursoVeranoPreinscripcion pre = preinscripcion(curso(idCurso, "2025-1"));
                pre.setPeriodoAcademico(periodo);
                gateway.crearSolicitudCursoVeranoPreinscripcion(pre);
                registrar(casos, "preinscripcion curso " + idCurso + " solicitud " + clave);
            }
            SolicitudCursoVeranoInscripcion enCurso = inscripcion(curso(5, "2025-1"));
            enCurso.setPeriodoAcademico(periodo);
            gateway.crearSolicitudCursoVeranoInscripcion(enCurso);
            registrar(casos, "inscripcion curso 5 solicitud " + clave);
            // El período va en un curso que todavía no existe
            gateway.crearSolicitudCursoVeranoPreinscripcion(preinscripcion(curso(99, periodo)));
            registrar(casos, "preinscripcion curso nuevo " + clave);
            gateway.crearSolicitudCursoVeranoInscripcion(inscripcion(curso(97, periodo)));
            registrar(casos, "inscripcion curso nuevo " + clave);
        }

        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(REFERENCIA)) {
            Files.writeString(REFERENCIA, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(casos));
        }
        assertThat(casos).isEqualTo(Correcciones.aplicar(JSON.readTree(Files.readString(REFERENCIA)),
                "persistencia/correcciones/periodo-escritura-verano-bug17.json"));
    }

    /** El período de la última solicitud guardada y el de su curso. */
    private void registrar(ObjectNode casos, String nombre) {
        SolicitudEntity guardada = guardadas.get(guardadas.size() - 1);
        ObjectNode caso = casos.putObject(nombre);
        caso.put("solicitud", marcar(guardada.getPeriodo_academico()));
        CursoOfertadoVeranoEntity curso = guardada.getObjCursoOfertadoVerano();
        caso.put("curso", curso == null ? "<sin curso>" : marcar(curso.getPeriodo_academico()));
    }

    private static String marcar(String periodo) {
        String actual = PeriodoAcademicoEnum.oActual(null);
        return periodo != null && periodo.equals(actual) ? "<actual>" : periodo;
    }

    private static CursoOfertadoVerano curso(int id, String periodo) {
        CursoOfertadoVerano curso = GrafosSolicitud.curso(null);
        curso.setId_curso(id);
        curso.setPeriodo_academico(periodo);
        return curso;
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcion(CursoOfertadoVerano curso) {
        SolicitudCursoVeranoPreinscripcion solicitud = GrafosSolicitud.completar(new SolicitudCursoVeranoPreinscripcion());
        solicitud.setIdSolicitud(null);
        solicitud.setNombre_estudiante("Ana");
        solicitud.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
        solicitud.setObjCursoOfertadoVerano(curso);
        return solicitud;
    }

    private static SolicitudCursoVeranoInscripcion inscripcion(CursoOfertadoVerano curso) {
        SolicitudCursoVeranoInscripcion solicitud = GrafosSolicitud.completar(new SolicitudCursoVeranoInscripcion());
        solicitud.setIdSolicitud(null);
        solicitud.setNombre_estudiante("Ana");
        solicitud.setCodicion_solicitud(CondicionSolicitudVerano.Habilitación);
        solicitud.setObjCursoOfertadoVerano(curso);
        return solicitud;
    }
}
