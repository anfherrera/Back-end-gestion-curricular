package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoPazYSalvo;

@DisplayName("Paz y Salvo - Transiciones de estado")
class TransicionEstadoPazYSalvoTest {

    @Test
    @DisplayName("Flujo normal: cada paso lo decide el rol responsable")
    void flujoNormal() {
        assertEquals("Funcionario", TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("Enviada", "APROBADA_FUNCIONARIO"));
        assertEquals("Coordinador", TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR"));
        assertEquals("Secretario", TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("APROBADA_COORDINADOR", "APROBADA"));
    }

    @Test
    @DisplayName("Se puede rechazar en cualquier paso no final")
    void rechazoEnCadaPaso() {
        assertEquals("Funcionario", TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("ENVIADA", "RECHAZADA"));
        assertEquals("Coordinador", TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("APROBADA_FUNCIONARIO", "RECHAZADA"));
        assertEquals("Secretario", TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("APROBADA_COORDINADOR", "RECHAZADA"));
    }

    @Test
    @DisplayName("No se puede saltar pasos, volver de un estado final ni usar estados inventados")
    void transicionesInvalidas() {
        assertThrows(IllegalArgumentException.class,
                () -> TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("ENVIADA", "APROBADA"));
        assertThrows(IllegalArgumentException.class,
                () -> TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("RECHAZADA", "APROBADA"));
        assertThrows(IllegalArgumentException.class,
                () -> TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("APROBADA", "RECHAZADA"));
        assertThrows(IllegalArgumentException.class,
                () -> TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("ENVIADA", "HOLA"));
        assertThrows(IllegalArgumentException.class,
                () -> TransicionEstadoPazYSalvo.validarYObtenerRolResponsable("APROBADA_FUNCIONARIO", "ENVIADA"));
    }

    @Test
    @DisplayName("Normaliza mayúsculas, espacios y estado vacío")
    void normalizacion() {
        assertEquals("ENVIADA", TransicionEstadoPazYSalvo.normalizar("Enviada"));
        assertEquals("ENVIADA", TransicionEstadoPazYSalvo.normalizar(null));
        assertEquals("APROBADA_FUNCIONARIO", TransicionEstadoPazYSalvo.normalizar("aprobada funcionario"));
        assertNull(TransicionEstadoPazYSalvo.normalizar("EN_REVISION_COORDINADOR"));
        assertTrue(TransicionEstadoPazYSalvo.esFinal("Rechazada"));
        assertFalse(TransicionEstadoPazYSalvo.esFinal("Enviada"));
    }
}
