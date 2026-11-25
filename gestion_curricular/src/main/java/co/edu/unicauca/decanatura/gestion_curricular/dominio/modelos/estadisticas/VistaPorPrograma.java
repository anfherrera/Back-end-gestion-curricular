package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Vista del dashboard por programa académico: solicitudes y estudiantes de cada programa y el análisis de
 * rendimiento.
 *
 * @param porPrograma            detalle de cada programa, por nombre del programa
 * @param estudiantesPorPrograma estudiantes de cada programa
 * @param solicitudesPorPrograma solicitudes de cada programa
 * @param analisis               análisis de rendimiento
 * @param fechaConsulta          momento de la consulta
 * @param descripcion            descripción de la vista
 */
public record VistaPorPrograma(Map<String, Detalle> porPrograma, Map<String, Integer> estudiantesPorPrograma,
        Map<String, Integer> solicitudesPorPrograma, Analisis analisis, Date fechaConsulta, String descripcion) {

    /**
     * Solicitudes, estudiantes, eficiencia (porcentaje de aprobadas), tiempo promedio de resolución en días y
     * tendencia de un programa.
     */
    public record Detalle(String nombre, String codigo, int totalSolicitudes, int estudiantes, int aprobadas,
            int rechazadas, int enviadas, double eficiencia, double tiempoPromedio, String tendencia,
            double porcentaje, Map<String, Integer> procesos, String color, String icono, String descripcion) {
    }

    /**
     * Análisis de rendimiento. Los programas son null si ninguno destaca; sin tiempos, tiempoMinimo queda en
     * Double.MAX_VALUE redondeado.
     */
    public record Analisis(String programaMasActivo, String programaConMasEstudiantes, String programaMasEficiente,
            String programaMasRapido, int maxSolicitudes, int maxEstudiantes, double eficienciaMaxima,
            double tiempoMinimo, int totalProgramas, int totalSolicitudes, int totalEstudiantes) {
    }
}
