package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;

@DisplayName("Solicitud: estado actual")
class SolicitudEstadoActualTest {

    private static EstadoSolicitud estado(String texto) {
        EstadoSolicitud e = new EstadoSolicitud();
        e.setEstado_actual(texto);
        e.setFecha_registro_estado(new Date());
        return e;
    }

    @Test
    @DisplayName("Sin historial: null, o el valor por defecto que se pida")
    void sinHistorial() {
        Solicitud solicitud = new Solicitud();
        assertNull(solicitud.ultimoRegistroEstado());
        assertNull(solicitud.estadoActual());
        assertEquals("Pendiente", solicitud.estadoActual("Pendiente"));

        solicitud.setEstadosSolicitud(null);
        assertNull(solicitud.estadoActual());
        assertEquals("Pendiente", solicitud.estadoActual("Pendiente"));
    }

    @Test
    @DisplayName("Devuelve el último registro de la lista")
    void ultimoRegistro() {
        Solicitud solicitud = new Solicitud();
        EstadoSolicitud ultimo = estado("APROBADA_FUNCIONARIO");
        solicitud.getEstadosSolicitud().add(estado("Enviada"));
        solicitud.getEstadosSolicitud().add(ultimo);

        assertSame(ultimo, solicitud.ultimoRegistroEstado());
        assertEquals("APROBADA_FUNCIONARIO", solicitud.estadoActual());
        assertEquals("APROBADA_FUNCIONARIO", solicitud.estadoActual("Pendiente"));
    }

    @Test
    @DisplayName("No salta registros vacíos (a diferencia de FlujoEstadosSolicitud.ultimoEstado)")
    void noSaltaVacios() {
        Solicitud solicitud = new Solicitud();
        solicitud.getEstadosSolicitud().add(estado("Enviada"));
        solicitud.getEstadosSolicitud().add(estado(""));

        assertEquals("", solicitud.estadoActual("Pendiente"));
        assertEquals("Enviada", FlujoEstadosSolicitud.ultimoEstado(solicitud.getEstadosSolicitud()));
    }

    @Test
    @DisplayName("EstadosSolicitud: solo ENVIADA se guarda con un texto distinto de su nombre")
    void textoGuardado() {
        for (EstadosSolicitud e : EstadosSolicitud.values()) {
            String esperado = e == EstadosSolicitud.ENVIADA ? "Enviada" : e.name();
            assertEquals(esperado, e.textoGuardado());
        }
    }
}
