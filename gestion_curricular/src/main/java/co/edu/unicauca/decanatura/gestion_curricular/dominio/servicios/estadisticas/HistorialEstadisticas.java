package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;

/**
 * Estadísticas por semestre (1: enero-junio, 2: julio-diciembre): el historial de varios años y un periodo
 * académico concreto. Cada semestre se cuenta como un periodo de fechas de {@link ConsultasEstadisticas}.
 */
public class HistorialEstadisticas {

    private final ConsultasEstadisticas consultas;

    public HistorialEstadisticas(GestionarEstadisticasGatewayIntPort datos) {
        this.consultas = new ConsultasEstadisticas(datos);
    }

    public List<Map<String, Object>> historial(Integer anioInicio, Integer anioFin) {
        // Sin años: los últimos 5 años, desde 2020 como mínimo
        int anioActual = Year.now().getValue();
        int desde = anioInicio != null ? anioInicio : Math.max(2020, anioActual - 4);
        int hasta = anioFin != null ? anioFin : anioActual;

        List<Map<String, Object>> historial = new ArrayList<>();
        for (int anio = desde; anio <= hasta; anio++) {
            for (int semestre = 1; semestre <= 2; semestre++) {
                Map<String, Object> estadisticas = porSemestre(anio, semestre);

                Map<String, Object> entrada = new HashMap<>();
                entrada.put("periodo", anio + "-" + semestre);
                entrada.put("anio", anio);
                entrada.put("semestre", semestre);
                entrada.put("descripcion", (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + anio);
                for (String clave : List.of("totalSolicitudes", "totalAprobadas", "totalRechazadas", "totalEnProceso",
                        "porcentajeAprobacion", "fechaInicio", "fechaFin")) {
                    entrada.put(clave, estadisticas.get(clave));
                }
                historial.add(entrada);
            }
        }

        // Más reciente primero
        historial.sort(Comparator.<Map<String, Object>, Integer>comparing(e -> (Integer) e.get("anio"))
                .thenComparing(e -> (Integer) e.get("semestre"))
                .reversed());
        return historial;
    }

    /** @param periodoAcademico con formato YYYY-1 o YYYY-2 */
    public Map<String, Object> porPeriodoAcademico(String periodoAcademico) {
        if (periodoAcademico == null || !periodoAcademico.matches("^\\d{4}-[12]$")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Formato de período académico inválido. Use formato: YYYY-P (ej: 2024-2)");
            return error;
        }
        String[] partes = periodoAcademico.split("-");
        Map<String, Object> estadisticas = porSemestre(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
        estadisticas.put("periodoAcademico", periodoAcademico);
        return estadisticas;
    }

    private Map<String, Object> porSemestre(int anio, int semestre) {
        LocalDate inicio = semestre == 1 ? LocalDate.of(anio, 1, 1) : LocalDate.of(anio, 7, 1);
        LocalDate fin = semestre == 1 ? LocalDate.of(anio, 6, 30) : LocalDate.of(anio, 12, 31);

        // java.sql.Date: la API devuelve estas fechas como "yyyy-MM-dd", sin hora
        Map<String, Object> estadisticas = consultas.porPeriodo(java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
        estadisticas.put("anio", anio);
        estadisticas.put("semestre", semestre);
        estadisticas.put("periodoAcademico", anio + "-" + semestre);
        estadisticas.put("tipoConsulta", "porSemestre");
        estadisticas.put("descripcion", (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + anio);
        return estadisticas;
    }
}
