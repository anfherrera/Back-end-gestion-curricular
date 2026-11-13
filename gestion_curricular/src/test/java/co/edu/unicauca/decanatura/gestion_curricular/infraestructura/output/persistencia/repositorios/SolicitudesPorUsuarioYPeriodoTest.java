package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.TipoDocumentoEntity;
import jakarta.persistence.EntityManager;

/**
 * Las solicitudes de un estudiante se filtran en la base de datos (antes se traía la tabla completa
 * y se filtraba en Java). Cada prueba crea sus datos y se deshace al terminar.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Repositorios - solicitudes de un estudiante por periodo")
class SolicitudesPorUsuarioYPeriodoTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private SolicitudHomologacionRepositoryInt homologacion;
    @Autowired
    private SolicitudReingresoRepositoryInt reingreso;
    @Autowired
    private SolicitudEcaesRepositoryInt ecaes;
    @Autowired
    private SolicitudPazYSalvoRepositoryInt pazYSalvo;

    private UsuarioEntity ana;
    private UsuarioEntity beto;

    @BeforeEach
    void datos() {
        RolEntity rol = new RolEntity();
        rol.setNombre("Estudiante prueba");
        em.persist(rol);
        ProgramaEntity programa = new ProgramaEntity();
        programa.setCodigo("PRUEBA-UP");
        programa.setNombre_programa("Programa prueba usuario-periodo");
        em.persist(programa);
        ana = usuario("Ana", "9000000001", rol, programa);
        beto = usuario("Beto", "9000000002", rol, programa);
    }

    @Test
    @DisplayName("Devuelve solo las solicitudes del estudiante en ese periodo, en orden")
    void filtraPorUsuarioYPeriodo() {
        Integer h1 = guardar(new SolicitudHomologacionEntity(), ana, "2025-1");
        Integer h2 = guardar(new SolicitudHomologacionEntity(), ana, "2025-1");
        guardar(new SolicitudHomologacionEntity(), ana, "2025-2");
        guardar(new SolicitudHomologacionEntity(), beto, "2025-1");
        Integer r = guardar(new SolicitudReingresoEntity(), beto, "2025-2");
        Integer p = guardar(new SolicitudPazYSalvoEntity(), ana, "2025-1");
        Integer e = guardar(ecaes(), ana, "2025-1");

        assertThat(ids(homologacion.findByUsuarioYPeriodo(ana.getId_usuario(), "2025-1"))).containsExactly(h1, h2);
        assertThat(ids(reingreso.findByUsuarioYPeriodo(beto.getId_usuario(), "2025-2"))).containsExactly(r);
        assertThat(ids(pazYSalvo.findByUsuarioYPeriodo(ana.getId_usuario(), "2025-1"))).containsExactly(p);
        assertThat(ids(ecaes.findByUsuarioYPeriodo(ana.getId_usuario(), "2025-1"))).containsExactly(e);
    }

    @Test
    @DisplayName("Sin periodo devuelve todas las del estudiante; con otro periodo, ninguna")
    void periodoOpcional() {
        Integer h1 = guardar(new SolicitudHomologacionEntity(), ana, "2025-1");
        Integer h2 = guardar(new SolicitudHomologacionEntity(), ana, "2025-2");
        guardar(new SolicitudHomologacionEntity(), beto, "2025-1");

        assertThat(ids(homologacion.findByUsuarioYPeriodo(ana.getId_usuario(), null))).containsExactly(h1, h2);
        assertThat(homologacion.findByUsuarioYPeriodo(ana.getId_usuario(), "2024-2")).isEmpty();
        assertThat(reingreso.findByUsuarioYPeriodo(ana.getId_usuario(), null)).isEmpty();
    }

    private UsuarioEntity usuario(String nombre, String cedula, RolEntity rol, ProgramaEntity programa) {
        UsuarioEntity u = new UsuarioEntity();
        u.setNombre_completo(nombre);
        u.setCodigo(cedula.substring(0, 10));
        u.setCedula(cedula);
        u.setCorreo(nombre.toLowerCase() + ".prueba@unicauca.edu.co");
        u.setPassword("x");
        u.setEstado_usuario(true);
        u.setObjRol(rol);
        u.setObjPrograma(programa);
        em.persist(u);
        return u;
    }

    private Integer guardar(SolicitudEntity s, UsuarioEntity usuario, String periodo) {
        s.setNombre_solicitud("Solicitud de prueba");
        s.setPeriodo_academico(periodo);
        s.setFecha_registro_solicitud(new Date());
        s.setObjUsuario(usuario);
        em.persist(s);
        em.flush();
        return s.getId_solicitud();
    }

    private static SolicitudEcaesEntity ecaes() {
        SolicitudEcaesEntity s = new SolicitudEcaesEntity();
        s.setTipoDocumento(TipoDocumentoEntity.values()[0]);
        s.setNumero_documento("123");
        s.setFecha_expedicion(new Date());
        s.setFecha_nacimiento(new Date());
        return s;
    }

    private static List<Integer> ids(List<? extends SolicitudEntity> solicitudes) {
        return solicitudes.stream().map(SolicitudEntity::getId_solicitud).toList();
    }
}
