package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Vista del dashboard por estado: las solicitudes de cada estado principal (APROBADA, ENVIADA, RECHAZADA y
 * EN_PROCESO, que junta las aprobadas por funcionario y por coordinador) y el análisis de su distribución.
 *
 * @param estados          detalle de cada estado, por nombre del estado
 * @param analisis         análisis de la distribución
 * @param totalSolicitudes solicitudes consideradas
 * @param fechaConsulta    momento de la consulta
 * @param descripcion      descripción de la vista
 */
public record VistaPorEstado(Map<String, Detalle> estados, Analisis analisis, int totalSolicitudes,
        Date fechaConsulta, String descripcion) {

    /**
     * Solicitudes de un estado, con su desglose por proceso y por programa.
     */
    public record Detalle(String estado, int cantidad, double porcentaje, Map<String, Integer> procesos,
            Map<String, Integer> programas, String color, String icono, String descripcion) {
    }

    /**
     * Análisis de la distribución. Sin solicitudes, los estados son null y minSolicitudes queda en
     * Integer.MAX_VALUE.
     */
    public record Analisis(String estadoMasComun, String estadoMenosComun, String estadoMasEficiente,
            int maxSolicitudes, int minSolicitudes, int totalSolicitudes, int estadosActivos,
            int solicitudesPendientes, int solicitudesCompletadas, double tasaResolucion) {
    }
}
