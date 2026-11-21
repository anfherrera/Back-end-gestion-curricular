package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Vista del dashboard por mes de registro: los doce meses, aunque no tengan solicitudes, y la tendencia entre los
 * dos últimos meses con datos.
 *
 * @param porMes         detalle de cada mes, por nombre del mes
 * @param tendencias     análisis temporal
 * @param todosLosMeses  los doce meses en orden
 * @param mesesOrdenados el detalle de los doce meses en orden
 * @param fechaConsulta  momento de la consulta
 * @param descripcion    descripción de la vista
 */
public record VistaPorMes(Map<String, Detalle> porMes, Tendencias tendencias, List<String> todosLosMeses,
        List<Detalle> mesesOrdenados, Date fechaConsulta, String descripcion) {

    /**
     * Conteos, eficiencia (porcentaje de aprobadas) y tiempo promedio de resolución en días de un mes.
     */
    public record Detalle(String mes, int total, int aprobadas, int rechazadas, int enviadas, double eficiencia,
            double tiempoPromedio, double porcentaje, Map<String, Integer> procesos, String color, String icono,
            String descripcion) {
    }

    /**
     * Análisis temporal. Los meses son null si ninguno destaca; sin tiempos, tiempoMinimo queda en
     * Double.MAX_VALUE redondeado.
     */
    public record Tendencias(String mesMasActivo, String mesMasEficiente, String mesMasRapido, int maxSolicitudes,
            double eficienciaMaxima, double tiempoMinimo, String tendenciaGeneral, double crecimientoPromedio,
            int mesesAnalizados, int totalSolicitudes, double promedioMensual) {
    }
}
