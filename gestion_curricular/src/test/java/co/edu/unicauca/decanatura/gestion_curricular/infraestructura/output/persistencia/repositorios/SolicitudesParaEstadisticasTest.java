package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.CondicionSolicitudVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity;
import jakarta.persistence.EntityManager;

/**
 * Las estadísticas leen las solicitudes con dos consultas de columnas sueltas. Esta prueba las ejecuta contra
 * H2 y revisa que el gateway arme bien los modelos de dominio: subtipo, usuario, programa, curso, materia y estados.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Gateway de estadísticas - lectura de solicitudes desde la base de datos")
class SolicitudesParaEstadisticasTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private GestionarEstadisticasGatewayIntPort gateway;

    @Test
    @DisplayName("Arma cada solicitud con su subtipo, usuario, programa, curso, materia y estados")
    void armaLasSolicitudes() {
        ProgramaEntity programa = new ProgramaEntity();
        programa.setCodigo("PRUEBA-EST");
        programa.setNombre_programa("Programa prueba estadisticas");
        em.persist(programa);
        RolEntity rol = new RolEntity();
        rol.setNombre("Estudiante prueba");
        em.persist(rol);
        UsuarioEntity ana = new UsuarioEntity();
        ana.setNombre_completo("Ana Prueba");
        ana.setCodigo("9100000001");
        ana.setCedula("9100000001");
        ana.setCorreo("ana.estadisticas@unicauca.edu.co");
        ana.setPassword("x");
        ana.setEstado_usuario(true);
        ana.setObjRol(rol);
        ana.setObjPrograma(programa);
        em.persist(ana);

        MateriaEntity materia = new MateriaEntity();
        materia.setCodigo("MAT-EST-1");
        materia.setNombre("Cálculo prueba");
        materia.setCreditos(4);
        em.persist(materia);
        DocenteEntity docente = new DocenteEntity();
        docente.setCodigo_docente("DOC-EST-1");
        docente.setNombre_docente("Docente prueba");
        em.persist(docente);
        CursoOfertadoVeranoEntity curso = new CursoOfertadoVeranoEntity();
        curso.setGrupo(GrupoCursoVeranoEntity.A);
        curso.setCupo_estimado(10);
        curso.setSalon("101");
        curso.setPeriodo_academico("2025-2");
        curso.setObjMateria(materia);
        curso.setObjDocente(docente);
        em.persist(curso);

        SolicitudHomologacionEntity homologacion = new SolicitudHomologacionEntity();
        Integer idHomologacion = guardar(homologacion, ana, null, "2025-1",
                estado("Enviada", 2025, 2, 1), estado("APROBADA", 2025, 2, 10));
        SolicitudCursoVeranoPreinscripcionEntity preinscripcion = new SolicitudCursoVeranoPreinscripcionEntity();
        preinscripcion.setNombre_estudiante("Ana Prueba");
        preinscripcion.setCodicion_solicitud(CondicionSolicitudVeranoEntity.values()[0]);
        preinscripcion.setObservacion("Prueba");
        Integer idCurso = guardar(preinscripcion, ana, curso, null,
                estado("Enviada", 2025, 6, 1));
        em.clear();

        Map<Integer, Solicitud> porId = gateway.listarSolicitudesConEstadosYUsuario().stream()
                .collect(Collectors.toMap(Solicitud::getId_solicitud, Function.identity()));

        Solicitud h = porId.get(idHomologacion);
        assertThat(h).isInstanceOf(SolicitudHomologacion.class);
        assertThat(h.getPeriodo_academico()).isEqualTo("2025-1");
        assertThat(h.getObjUsuario().getNombre_completo()).isEqualTo("Ana Prueba");
        assertThat(h.getObjUsuario().getObjPrograma().getNombre_programa()).isEqualTo("Programa prueba estadisticas");
        assertThat(h.getObjCursoOfertadoVerano()).isNull();
        assertThat(h.getEstadosSolicitud()).extracting(EstadoSolicitud::getEstado_actual).containsExactly("Enviada", "APROBADA");

        Solicitud c = porId.get(idCurso);
        assertThat(c).isInstanceOf(SolicitudCursoVeranoPreinscripcion.class);
        assertThat(c.getObjCursoOfertadoVerano().getPeriodo_academico()).isEqualTo("2025-2");
        assertThat(c.getObjCursoOfertadoVerano().getObjMateria().getNombre()).isEqualTo("Cálculo prueba");
        assertThat(c.getEstadosSolicitud()).hasSize(1);
    }

    private Integer guardar(SolicitudEntity s, UsuarioEntity usuario, CursoOfertadoVeranoEntity curso, String periodo,
            EstadoSolicitudEntity... estados) {
        s.setNombre_solicitud("Solicitud de prueba");
        s.setPeriodo_academico(periodo != null ? periodo : "N/A");
        s.setFecha_registro_solicitud(estados[0].getFecha_registro_estado());
        s.setObjUsuario(usuario);
        s.setObjCursoOfertadoVerano(curso);
        s.setEstadosSolicitud(new java.util.ArrayList<>(List.of(estados)));
        for (EstadoSolicitudEntity e : estados) {
            e.setObjSolicitud(s);
        }
        em.persist(s);
        em.flush();
        return s.getId_solicitud();
    }

    private static EstadoSolicitudEntity estado(String nombre, int anio, int mes, int dia) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(anio, mes - 1, dia, 10, 0);
        EstadoSolicitudEntity e = new EstadoSolicitudEntity();
        e.setEstado_actual(nombre);
        e.setFecha_registro_estado(new Date(c.getTimeInMillis()));
        return e;
    }
}
