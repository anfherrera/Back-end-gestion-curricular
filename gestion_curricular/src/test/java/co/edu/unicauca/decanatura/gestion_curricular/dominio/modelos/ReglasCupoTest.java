package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ReglasCupo: qué inscripciones ocupan cupo")
class ReglasCupoTest {

    private static SolicitudCursoVeranoIncripcion inscripcion(String... estados) {
        SolicitudCursoVeranoIncripcion s = new SolicitudCursoVeranoIncripcion();
        for (String texto : estados) {
            EstadoSolicitud e = new EstadoSolicitud();
            e.setEstado_actual(texto);
            e.setFecha_registro_estado(new Date());
            s.getEstadosSolicitud().add(e);
        }
        return s;
    }

    @ParameterizedTest
    @ValueSource(strings = {"Pago_Validado", "Inscripcion_Completada", "Aprobado", "APROBADA", "Aprobada"})
    @DisplayName("Ocupan cupo los estados aceptados, incluidas las variantes antiguas")
    void ocupan(String estado) {
        assertTrue(ReglasCupo.estadoOcupaCupo(estado));
        assertTrue(ReglasCupo.ocupaCupo(inscripcion("Enviada", estado)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Enviada", "Pago_Rechazado", "Rechazado", "pago_validado", ""})
    @DisplayName("No ocupan cupo los pendientes, rechazados ni otras grafías")
    void noOcupan(String estado) {
        assertFalse(ReglasCupo.estadoOcupaCupo(estado));
    }

    @Test
    @DisplayName("Solo cuenta el último estado; sin estados o con estado null no ocupa")
    void ultimoEstado() {
        assertFalse(ReglasCupo.ocupaCupo(inscripcion("Pago_Validado", "Pago_Rechazado")));
        assertFalse(ReglasCupo.ocupaCupo(inscripcion()));
        assertFalse(ReglasCupo.ocupaCupo(inscripcion((String) null)));
        assertFalse(ReglasCupo.estadoOcupaCupo(null));
    }
}
