package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;

@DisplayName("Estadísticas - Clasificación de solicitudes por tipo de proceso")
class TipoProcesoSolicitudTest {

    @Test
    @DisplayName("Clasifica por la clase de la entidad, sin importar el nombre")
    void clasificaPorTipoDeEntidad() {
        SolicitudHomologacionEntity homologacion = new SolicitudHomologacionEntity();
        homologacion.setNombre_solicitud("Solicitud de Homologación - Juan Pérez"); // con tilde
        assertEquals(TipoProcesoSolicitud.HOMOLOGACION, TipoProcesoSolicitud.de(homologacion));

        SolicitudCursoVeranoInscripcionEntity inscripcion = new SolicitudCursoVeranoInscripcionEntity();
        inscripcion.setNombre_solicitud("Solicitud Inscripción - María"); // no dice "curso" ni "verano"
        assertEquals(TipoProcesoSolicitud.CURSOS_VERANO, TipoProcesoSolicitud.de(inscripcion));

        SolicitudCursoVeranoPreinscripcionEntity preinscripcion = new SolicitudCursoVeranoPreinscripcionEntity();
        assertEquals(TipoProcesoSolicitud.CURSOS_VERANO, TipoProcesoSolicitud.de(preinscripcion));

        assertEquals(TipoProcesoSolicitud.PAZ_Y_SALVO, TipoProcesoSolicitud.de(new SolicitudPazYSalvoEntity()));
        assertEquals(TipoProcesoSolicitud.REINGRESO, TipoProcesoSolicitud.de(new SolicitudReingresoEntity()));
        assertEquals(TipoProcesoSolicitud.ECAES, TipoProcesoSolicitud.de(new SolicitudEcaesEntity()));
    }

    @Test
    @DisplayName("Solicitud genérica: usa el nombre ignorando tildes")
    void solicitudGenericaUsaNombreSinTildes() {
        SolicitudEntity generica = new SolicitudEntity();
        generica.setNombre_solicitud("Solicitud de Homologación - Ana");
        assertEquals(TipoProcesoSolicitud.HOMOLOGACION, TipoProcesoSolicitud.de(generica));

        generica.setNombre_solicitud("Algo sin tipo");
        assertNull(TipoProcesoSolicitud.de(generica));

        assertNull(TipoProcesoSolicitud.de(null));
    }
}
