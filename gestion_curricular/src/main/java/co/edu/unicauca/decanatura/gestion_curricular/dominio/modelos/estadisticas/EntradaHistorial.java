package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;

/**
 * Un semestre del historial de estadísticas.
 *
 * @param periodo              el semestre como YYYY-S
 * @param anio                 año del semestre
 * @param semestre             1 o 2
 * @param descripcion          "Primer Semestre YYYY" o "Segundo Semestre YYYY"
 * @param totalSolicitudes     solicitudes del semestre
 * @param totalAprobadas       en estado APROBADA
 * @param totalRechazadas      en estado RECHAZADA
 * @param totalEnProceso       aprobadas por funcionario o por coordinador
 * @param porcentajeAprobacion aprobadas sobre el total, con un decimal
 * @param fechaInicio          primer día del semestre
 * @param fechaFin             último día del semestre
 */
public record EntradaHistorial(String periodo, int anio, int semestre, String descripcion, int totalSolicitudes,
        int totalAprobadas, int totalRechazadas, int totalEnProceso, double porcentajeAprobacion, Date fechaInicio,
        Date fechaFin) {

    public static EntradaHistorial de(EstadisticasPorSemestre semestre) {
        return new EntradaHistorial(semestre.anio() + "-" + semestre.semestre(), semestre.anio(), semestre.semestre(),
                semestre.descripcion(), semestre.totalSolicitudes(), semestre.totalAprobadas(),
                semestre.totalRechazadas(), semestre.totalEnProceso(), semestre.porcentajeAprobacion(),
                semestre.fechaInicio(), semestre.fechaFin());
    }
}
