package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarEstadisticasGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadisticaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * Fija la respuesta completa de las estadísticas de cursos de verano (incluidas las predicciones)
 * para un conjunto de datos fijo. Sirve para reorganizar el código sin cambiar lo que ve el usuario.
 *
 * Para regrabar la referencia tras un cambio intencional:
 * {@code mvnw test -Dtest=EstadisticasCursosVeranoCaracterizacionTest -DgrabarReferencia=true}
 */
@DisplayName("Estadísticas de cursos de verano - respuesta fijada")
class EstadisticasCursosVeranoCaracterizacionTest {

    private static final Path REFERENCIA =
            Path.of("src/test/resources/referencias/estadisticas-cursos-verano.json");

    @Test
    @DisplayName("La respuesta (resumen, tendencias, predicciones y recomendaciones) no cambia")
    void respuestaFijada() throws Exception {
        SolicitudRepositoryInt solicitudRepository = mock(SolicitudRepositoryInt.class);
        when(solicitudRepository.buscarTodasConEstadosYUsuario()).thenReturn(datos());
        GestionarEstadisticasGatewayImplAdapter gateway = new GestionarEstadisticasGatewayImplAdapter(
                mock(EstadisticaRepositoryInt.class), mock(ProgramaRepositoryInt.class), solicitudRepository,
                mock(UsuarioRepositoryInt.class), new ModelMapper());

        Map<String, Object> resultado = gateway.obtenerEstadisticasCursosVerano(null, null);
        resultado.remove("fechaConsulta");
        ((Map<?, ?>) resultado.get("predicciones")).remove("fechaPrediccion");

        ObjectMapper json = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        String obtenido = json.writeValueAsString(resultado).replace("\r\n", "\n");

        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(REFERENCIA.getParent());
            Files.writeString(REFERENCIA, obtenido, StandardCharsets.UTF_8);
        }
        String esperado = Files.readString(REFERENCIA, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertThat(obtenido).isEqualTo(esperado);
    }

    /**
     * Dos programas y tres materias, repartidos en varios meses. La materia de cada solicitud sale del
     * curso solicitado, no del nombre de la solicitud.
     */
    private static List<SolicitudEntity> datos() {
        ProgramaEntity sistemas = programa(1, "Ingeniería de Sistemas");
        ProgramaEntity electronica = programa(2, "Ingeniería Electrónica");
        CursoOfertadoVeranoEntity calculo = curso(10, "Cálculo I");
        CursoOfertadoVeranoEntity programacion = curso(11, "Programación Orientada a Objetos");
        CursoOfertadoVeranoEntity fisica = curso(12, "Física I");

        List<SolicitudEntity> res = new ArrayList<>();
        int id = 100;
        // {programa, curso, mes (1-12), estado}
        Object[][] filas = {
            {sistemas, calculo, 1, "Enviada"},
            {sistemas, calculo, 2, "APROBADA"},
            {sistemas, calculo, 3, "APROBADA"},
            {sistemas, calculo, 5, "Pago_Validado"},
            {sistemas, programacion, 6, "Enviada"},
            {sistemas, programacion, 6, "Rechazada"},
            {sistemas, programacion, 7, "APROBADA_FUNCIONARIO"},
            {electronica, fisica, 7, "Enviada"},
            {electronica, fisica, 8, "APROBADA"},
            {electronica, calculo, 8, "Enviada"},
            {electronica, calculo, 8, "APROBADA_COORDINADOR"},
            {sistemas, fisica, 9, "Enviada"},
        };
        for (Object[] f : filas) {
            String nombre = "Curso de verano - Estudiante " + id;
            res.add(solicitud(id++, (ProgramaEntity) f[0], (CursoOfertadoVeranoEntity) f[1],
                    (Integer) f[2], (String) f[3], nombre));
        }
        return res;
    }

    private static ProgramaEntity programa(int id, String nombre) {
        ProgramaEntity p = new ProgramaEntity();
        p.setId_programa(id);
        p.setNombre_programa(nombre);
        return p;
    }

    private static CursoOfertadoVeranoEntity curso(int id, String materia) {
        MateriaEntity m = new MateriaEntity();
        m.setId_materia(id);
        m.setNombre(materia);
        CursoOfertadoVeranoEntity c = new CursoOfertadoVeranoEntity();
        c.setId_curso(id);
        c.setObjMateria(m);
        c.setPeriodo_academico("2025-2");
        return c;
    }

    private static SolicitudEntity solicitud(int id, ProgramaEntity programa, CursoOfertadoVeranoEntity curso,
            int mes, String estado, String nombre) {
        UsuarioEntity u = new UsuarioEntity();
        u.setId_usuario(id);
        u.setNombre_completo("Estudiante " + id);
        u.setObjPrograma(programa);

        SolicitudCursoVeranoPreinscripcionEntity s = new SolicitudCursoVeranoPreinscripcionEntity();
        s.setId_solicitud(id);
        s.setNombre_solicitud(nombre);
        s.setPeriodo_academico("2025-2");
        s.setFecha_registro_solicitud(fecha(mes));
        s.setObjUsuario(u);
        s.setObjCursoOfertadoVerano(curso);

        EstadoSolicitudEntity e = new EstadoSolicitudEntity();
        e.setEstado_actual(estado);
        e.setFecha_registro_estado(fecha(mes));
        e.setObjSolicitud(s);
        List<EstadoSolicitudEntity> estados = new ArrayList<>();
        estados.add(e);
        s.setEstadosSolicitud(estados);
        return s;
    }

    private static Date fecha(int mes) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2025, mes - 1, 15, 10, 0);
        return c.getTime();
    }
}
