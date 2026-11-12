package co.edu.unicauca.decanatura.gestion_curricular;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "FLYWAY_TEST_MYSQL_URL", matches = "jdbc:mysql:.*")
class MigracionesMySqlTest {

    @Test
    void aplicaMigracionesYConservaDatosAlReiniciar() throws Exception {
        try (var aplicacion = iniciar()) {
            JdbcTemplate jdbc = aplicacion.getBean(JdbcTemplate.class);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM Roles", Integer.class)).isEqualTo(7);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM Programas", Integer.class)).isEqualTo(4);
            assertThat(jdbc.queryForObject("SELECT cedula FROM Usuarios WHERE codigo = 'ADMIN001'", String.class))
                    .isEqualTo("000009000");
            jdbc.update("INSERT INTO Roles(nombre) VALUES ('Rol de prueba de reinicio')");
            try (var conexion = aplicacion.getBean(DataSource.class).getConnection()) {
                ScriptUtils.executeSqlScript(conexion, new EncodedResource(
                        new FileSystemResource("scripts/datos_demo.sql"), StandardCharsets.UTF_8));
            }

            var verano = aplicacion.getBean(GestionarSolicitudCursoVeranoCUIntPort.class);
            assertThat(verano.listarSolicitudesParaReporte(1, null, "2025-2", null, null))
                    .isNotEmpty()
                    .allSatisfy(solicitud -> {
                        assertThat(solicitud).isInstanceOfAny(
                                SolicitudCursoVeranoPreinscripcion.class, SolicitudCursoVeranoIncripcion.class);
                        assertThat(solicitud.getObjCursoOfertadoVerano().getId_curso()).isEqualTo(1);
                    });
        }

        try (var aplicacion = iniciar()) {
            JdbcTemplate jdbc = aplicacion.getBean(JdbcTemplate.class);
            assertThat(jdbc.queryForList("SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank",
                    String.class)).contains("1", "2");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM Roles WHERE nombre = 'Rol de prueba de reinicio'",
                    Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM Usuarios WHERE codigo = 'ADMIN001'", Integer.class))
                    .isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM Usuarios", Integer.class)).isEqualTo(36);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM Solicitudes", Integer.class)).isEqualTo(50);
        }
    }

    private ConfigurableApplicationContext iniciar() {
        return new SpringApplicationBuilder(GestionCurricularApplication.class).profiles("dev").run(
                "--server.port=0", "--spring.devtools.restart.enabled=false", "--spring.devtools.livereload.enabled=false",
                "--spring.datasource.url=" + System.getenv("FLYWAY_TEST_MYSQL_URL"),
                "--spring.datasource.username=curricular", "--spring.datasource.password=curricular");
    }
}
