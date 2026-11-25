package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Vista del dashboard por proceso: el detalle de cada proceso y la comparación entre ellos.
 *
 * @param estadisticasPorProceso detalle de cada proceso del dashboard, por nombre del proceso
 * @param resumenComparativo     el proceso más eficiente, el más rápido y el más demandado
 * @param fechaConsulta          momento de la consulta
 * @param descripcion            descripción de la vista
 */
public record VistaPorProceso(Map<String, Detalle> estadisticasPorProceso, ResumenComparativo resumenComparativo,
        Date fechaConsulta, String descripcion) {

    /**
     * Conteos, eficiencia (porcentaje de aprobadas), tiempos de resolución en días y tendencia de un proceso.
     */
    public record Detalle(String proceso, int totalSolicitudes, int aprobadas, int rechazadas, int enviadas,
            int enProceso, double eficiencia, double tiempoPromedio, double tiempoMinimo, double tiempoMaximo,
            String tendencia, String descripcion, String color, String icono) {
    }

    /**
     * Comparación entre procesos. Los nombres son null si ningún proceso destaca; sin tiempos, tiempoMinimo
     * queda en Double.MAX_VALUE redondeado.
     */
    public record ResumenComparativo(String procesoMasEficiente, String procesoMasRapido,
            String procesoMasDemandado, double eficienciaMaxima, double tiempoMinimo, int demandaMaxima,
            int totalProcesos) {
    }
}
