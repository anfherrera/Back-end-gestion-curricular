package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;

@DisplayName("Flujos de estado de homologación, reingreso y ECAES")
class FlujoEstadosSolicitudTest {

    private static final FlujoEstadosSolicitud TRES = FlujoEstadosSolicitud.TRES_APROBACIONES;
    private static final FlujoEstadosSolicitud ECAES = FlujoEstadosSolicitud.ECAES;

    @Test
    @DisplayName("Tres aprobaciones: cada paso tiene su rol y no se puede saltar")
    void tresAprobaciones() {
        assertEquals("Funcionario", TRES.validarYObtenerRolResponsable("Enviada", "APROBADA_FUNCIONARIO"));
        assertEquals("Coordinador", TRES.validarYObtenerRolResponsable("APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR"));
        assertEquals("Secretario", TRES.validarYObtenerRolResponsable("APROBADA_COORDINADOR", "APROBADA"));
        assertThrows(IllegalArgumentException.class, () -> TRES.validarYObtenerRolResponsable("ENVIADA", "APROBADA"));
        assertThrows(IllegalArgumentException.class, () -> TRES.validarYObtenerRolResponsable("ENVIADA", "PRE_REGISTRADO"));
    }

    @Test
    @DisplayName("ECAES: solo el Funcionario pre-registra o rechaza, y ambos son finales")
    void ecaes() {
        assertEquals("Funcionario", ECAES.validarYObtenerRolResponsable("Enviada", "PRE_REGISTRADO"));
        assertEquals("Funcionario", ECAES.validarYObtenerRolResponsable("Enviada", "Rechazada"));
        assertThrows(IllegalArgumentException.class, () -> ECAES.validarYObtenerRolResponsable("ENVIADA", "APROBADA"));
        assertThrows(IllegalArgumentException.class, () -> ECAES.validarYObtenerRolResponsable("PRE_REGISTRADO", "RECHAZADA"));
        // Reenviar ENVIADA (lo hacía la pantalla del estudiante) no es un paso válido
        assertThrows(IllegalArgumentException.class, () -> ECAES.validarYObtenerRolResponsable("ENVIADA", "ENVIADA"));
        assertTrue(ECAES.esFinal("PRE_REGISTRADO"));
        assertFalse(ECAES.esFinal("Enviada"));
        assertEquals("RECHAZADA", ECAES.normalizar("Rechazada"));
    }
}
