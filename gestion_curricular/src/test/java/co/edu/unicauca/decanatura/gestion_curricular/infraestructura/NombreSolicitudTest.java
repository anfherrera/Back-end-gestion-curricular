package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.NombreSolicitud;

@DisplayName("Nombre descriptivo de las solicitudes")
class NombreSolicitudTest {

    @Test
    @DisplayName("Usa el primer nombre no vacío; sin nombre queda solo el tipo")
    void componeNombre() {
        assertEquals("Solicitud de Reingreso - Ana Pérez", NombreSolicitud.de(NombreSolicitud.REINGRESO, " Ana Pérez "));
        assertEquals("Solicitud Curso Verano - Luis", NombreSolicitud.de(NombreSolicitud.CURSO_VERANO, null, "  ", "Luis"));
        assertEquals("Solicitud ECAES", NombreSolicitud.de(NombreSolicitud.ECAES, (String) null));
        assertEquals("Solicitud Paz y Salvo", NombreSolicitud.de(NombreSolicitud.PAZ_Y_SALVO));
    }
}
