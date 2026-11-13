package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.ConsultarSolicitudesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;

/**
 * BUG-7: el funcionario debe ver en {@code listarSolicitudesPorRol} las solicitudes recién
 * creadas, que se guardan con {@code "Enviada"} ({@link EstadosSolicitud#textoGuardado()}).
 */
@DisplayName("listarSolicitudesPorRol: el funcionario ve las solicitudes enviadas (BUG-7)")
class ListarSolicitudesPorRolTest {

    private static <T extends Solicitud> T conEstados(T solicitud, int id, String... estados) {
        solicitud.setId_solicitud(id);
        for (String texto : estados) {
            EstadoSolicitud e = new EstadoSolicitud();
            e.setEstado_actual(texto);
            e.setFecha_registro_estado(new Date());
            solicitud.getEstadosSolicitud().add(e);
        }
        return solicitud;
    }

    private static final String ENVIADA = EstadosSolicitud.ENVIADA.textoGuardado();
    private static final String APROBADA_FUNCIONARIO = EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado();

    /**
     * Desde DUP-1 el filtro por último estado lo hace la consulta del gateway. El simulado responde como
     * ella: compara el texto exacto del último estado con el que recibe.
     */
    private static <T extends Solicitud> void simularUltimoEstado(ConsultarSolicitudesGatewayIntPort<T> gateway,
            List<T> solicitudes) {
        when(gateway.listarPorUltimoEstado(anyString(), isNull(), isNull())).thenAnswer(llamada -> solicitudes.stream()
                .filter(s -> s.estadoActual().equals(llamada.getArgument(0)))
                .toList());
    }

    @Test
    @DisplayName("Homologación")
    void homologacion() {
        GestionarSolicitudHomologacionGatewayIntPort gateway = mock(GestionarSolicitudHomologacionGatewayIntPort.class);
        simularUltimoEstado(gateway, List.of(
                conEstados(new SolicitudHomologacion(), 1, ENVIADA),
                conEstados(new SolicitudHomologacion(), 2, ENVIADA, APROBADA_FUNCIONARIO)));
        GestionarSolicitudHomologacionCUAdapter cu = new GestionarSolicitudHomologacionCUAdapter(
                mock(FormateadorResultadosIntPort.class), gateway, mock(GestionarUsuarioGatewayIntPort.class),
                mock(GestionarDocumentosGatewayIntPort.class), mock(GestionarEstadoSolicitudGatewayIntPort.class),
                mock(GestionarNotificacionCUIntPort.class), mock(GestionarArchivosCUIntPort.class));

        assertThat(cu.listarSolicitudesPorRol("FUNCIONARIO", null))
                .extracting(Solicitud::getId_solicitud).containsExactly(1);
        assertThat(cu.listarSolicitudesPorRol("COORDINADOR", null))
                .extracting(Solicitud::getId_solicitud).containsExactly(2);
    }

    @Test
    @DisplayName("Reingreso")
    void reingreso() {
        GestionarSolicitudReingresoGatewayIntPort gateway = mock(GestionarSolicitudReingresoGatewayIntPort.class);
        simularUltimoEstado(gateway, List.of(
                conEstados(new SolicitudReingreso(), 1, ENVIADA),
                conEstados(new SolicitudReingreso(), 2, ENVIADA, APROBADA_FUNCIONARIO)));
        GestionarSolicitudReingresoCUAdapter cu = new GestionarSolicitudReingresoCUAdapter(
                mock(GestionarUsuarioGatewayIntPort.class), mock(GestionarDocumentosGatewayIntPort.class),
                mock(GestionarEstadoSolicitudGatewayIntPort.class), mock(FormateadorResultadosIntPort.class),
                gateway, mock(GestionarNotificacionCUIntPort.class), mock(GestionarArchivosCUIntPort.class));

        assertThat(cu.listarSolicitudesReingresoPorRol("FUNCIONARIO", null))
                .extracting(Solicitud::getId_solicitud).containsExactly(1);
        assertThat(cu.listarSolicitudesReingresoPorRol("COORDINADOR", null))
                .extracting(Solicitud::getId_solicitud).containsExactly(2);
    }

    @Test
    @DisplayName("ECAES")
    void ecaes() {
        GestionarPreRegistroEcaesGatewayIntPort gateway = mock(GestionarPreRegistroEcaesGatewayIntPort.class);
        simularUltimoEstado(gateway, List.of(
                conEstados(new SolicitudEcaes(), 1, ENVIADA),
                conEstados(new SolicitudEcaes(), 2, ENVIADA, EstadosSolicitud.PRE_REGISTRADO.textoGuardado())));
        GestionarSolicitudEcaesCUAdapter cu = new GestionarSolicitudEcaesCUAdapter(
                gateway, mock(FormateadorResultadosIntPort.class), mock(GestionarDocumentosGatewayIntPort.class),
                mock(GestionarEstadoSolicitudGatewayIntPort.class), mock(GestionarUsuarioGatewayIntPort.class),
                mock(GestionarNotificacionCUIntPort.class), mock(GestionarArchivosCUIntPort.class));

        assertThat(cu.listarSolicitudesPorRol("FUNCIONARIO", null))
                .extracting(Solicitud::getId_solicitud).containsExactly(1);
    }
}
