package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.TipoProcesoSolicitud;

@DisplayName("Estadísticas - Clasificación de solicitudes por tipo de proceso")
class TipoProcesoSolicitudTest {

    @Test
    @DisplayName("Clasifica por la clase de la solicitud, sin importar el nombre")
    void clasificaPorTipoDeSolicitud() {
        SolicitudHomologacion homologacion = new SolicitudHomologacion();
        homologacion.setNombre_solicitud("Solicitud de Homologación - Juan Pérez"); // con tilde
        assertEquals(TipoProcesoSolicitud.HOMOLOGACION, TipoProcesoSolicitud.de(homologacion));

        SolicitudCursoVeranoIncripcion inscripcion = new SolicitudCursoVeranoIncripcion();
        inscripcion.setNombre_solicitud("Solicitud Inscripción - María"); // no dice "curso" ni "verano"
        assertEquals(TipoProcesoSolicitud.CURSOS_VERANO, TipoProcesoSolicitud.de(inscripcion));

        assertEquals(TipoProcesoSolicitud.CURSOS_VERANO, TipoProcesoSolicitud.de(new SolicitudCursoVeranoPreinscripcion()));
        assertEquals(TipoProcesoSolicitud.PAZ_Y_SALVO, TipoProcesoSolicitud.de(new SolicitudPazYSalvo()));
        assertEquals(TipoProcesoSolicitud.REINGRESO, TipoProcesoSolicitud.de(new SolicitudReingreso()));
        assertEquals(TipoProcesoSolicitud.ECAES, TipoProcesoSolicitud.de(new SolicitudEcaes()));
    }

    @Test
    @DisplayName("Solicitud genérica: usa el nombre ignorando tildes")
    void solicitudGenericaUsaNombreSinTildes() {
        Solicitud generica = new Solicitud();
        generica.setNombre_solicitud("Solicitud de Homologación - Ana");
        assertEquals(TipoProcesoSolicitud.HOMOLOGACION, TipoProcesoSolicitud.de(generica));

        generica.setNombre_solicitud("Algo sin tipo");
        assertNull(TipoProcesoSolicitud.de(generica));

        assertNull(TipoProcesoSolicitud.de(null));
    }
}
