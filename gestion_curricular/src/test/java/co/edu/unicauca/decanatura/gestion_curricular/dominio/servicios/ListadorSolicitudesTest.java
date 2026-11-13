package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.ConsultarSolicitudesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;

/** ARQ-7: el flujo decide qué estado ve cada rol; el gateway recibe el texto guardado. */
@DisplayName("Listado de solicitudes por rol (ARQ-7)")
class ListadorSolicitudesTest {

    private static final FlujoEstadosSolicitud TRES = FlujoEstadosSolicitud.TRES_APROBACIONES;
    private static final FlujoEstadosSolicitud ECAES = FlujoEstadosSolicitud.ECAES;

    @Test
    @DisplayName("Estado pendiente de cada rol")
    void estadoPendiente() {
        assertThat(TRES.estadoPendientePara(FlujoEstadosSolicitud.FUNCIONARIO)).contains(EstadosSolicitud.ENVIADA);
        assertThat(TRES.estadoPendientePara(FlujoEstadosSolicitud.COORDINADOR)).contains(EstadosSolicitud.APROBADA_FUNCIONARIO);
        assertThat(TRES.estadoPendientePara(FlujoEstadosSolicitud.SECRETARIO)).contains(EstadosSolicitud.APROBADA_COORDINADOR);
        assertThat(TRES.estadoPendientePara(FiltroSolicitudes.ESTUDIANTE)).isEmpty();
        assertThat(ECAES.estadoPendientePara(FlujoEstadosSolicitud.FUNCIONARIO)).contains(EstadosSolicitud.ENVIADA);
        assertThat(ECAES.estadoPendientePara(FlujoEstadosSolicitud.COORDINADOR)).isEmpty();
        assertThat(ECAES.estadoPendientePara(FlujoEstadosSolicitud.SECRETARIO)).isEmpty();
    }

    @Test
    @DisplayName("Estado al que aprueba cada rol")
    void estadoAprobado() {
        assertThat(TRES.estadoAprobadoPor(FlujoEstadosSolicitud.FUNCIONARIO)).contains(EstadosSolicitud.APROBADA_FUNCIONARIO);
        assertThat(TRES.estadoAprobadoPor(FlujoEstadosSolicitud.COORDINADOR)).contains(EstadosSolicitud.APROBADA_COORDINADOR);
        assertThat(TRES.estadoAprobadoPor(FlujoEstadosSolicitud.SECRETARIO)).contains(EstadosSolicitud.APROBADA);
        assertThat(ECAES.estadoAprobadoPor(FlujoEstadosSolicitud.FUNCIONARIO)).contains(EstadosSolicitud.PRE_REGISTRADO);
        assertThat(ECAES.estadoAprobadoPor(FlujoEstadosSolicitud.COORDINADOR)).isEmpty();
    }

    @Test
    @DisplayName("Rol de los listados 'porRol'")
    void porRol() {
        assertThat(FiltroSolicitudes.porRol("ESTUDIANTE", 7)).contains(FiltroSolicitudes.deEstudiante(7, null));
        assertThat(FiltroSolicitudes.porRol("FUNCIONARIO", 7))
                .contains(FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.FUNCIONARIO, null, null));
        assertThat(FiltroSolicitudes.porRol("SECRETARIA", null))
                .contains(FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.SECRETARIO, null, null));
        assertThat(FiltroSolicitudes.porRol("Funcionario", null)).isEmpty();
        assertThat(FiltroSolicitudes.porRol("otro", null)).isEmpty();
        assertThatThrownBy(() -> FiltroSolicitudes.porRol(null, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("El gateway recibe el texto guardado del estado (\"Enviada\", no \"ENVIADA\")")
    void gatewayRecibeTextoGuardado() {
        @SuppressWarnings("unchecked")
        ConsultarSolicitudesGatewayIntPort<SolicitudHomologacion> gateway = mock(ConsultarSolicitudesGatewayIntPort.class);
        List<SolicitudHomologacion> enviadas = List.of(new SolicitudHomologacion());
        when(gateway.listarPorUltimoEstado("Enviada", null, "2025-2")).thenReturn(enviadas);
        ListadorSolicitudes<SolicitudHomologacion> listador = new ListadorSolicitudes<>(TRES, gateway);

        assertThat(listador.listar(FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.FUNCIONARIO, null, "2025-2")))
                .isSameAs(enviadas);

        listador.listar(FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.COORDINADOR, 3, null));
        verify(gateway).listarPorUltimoEstado("APROBADA_FUNCIONARIO", 3, null);
        listador.listar(FiltroSolicitudes.procesadasPor(FlujoEstadosSolicitud.SECRETARIO, null, "2025-1"));
        verify(gateway).listarPorEstadoEnHistorial("APROBADA", null, "2025-1");
        listador.listar(FiltroSolicitudes.deEstudiante(5, "2025-1"));
        verify(gateway).listarPorUsuario(5, "2025-1");
    }

    @Test
    @DisplayName("Un rol sin paso en el flujo recibe una lista vacía sin consultar")
    void rolSinPaso() {
        @SuppressWarnings("unchecked")
        ConsultarSolicitudesGatewayIntPort<SolicitudHomologacion> gateway = mock(ConsultarSolicitudesGatewayIntPort.class);
        ListadorSolicitudes<SolicitudHomologacion> listador = new ListadorSolicitudes<>(ECAES, gateway);

        assertThat(listador.listar(FiltroSolicitudes.pendientesDe(FlujoEstadosSolicitud.COORDINADOR, 3, "2025-2"))).isEmpty();
        assertThat(listador.listarPorRol("otro", null)).isEmpty();
        verify(gateway, never()).listarPorUltimoEstado(anyString(), any(), any());
    }
}
