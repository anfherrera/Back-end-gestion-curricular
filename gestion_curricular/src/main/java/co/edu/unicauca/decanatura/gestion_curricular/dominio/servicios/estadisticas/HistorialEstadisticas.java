package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EntradaHistorial;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorSemestre;

/**
 * Estadísticas por semestre (1: enero-junio, 2: julio-diciembre) para el historial de varios años. Cada semestre se cuenta como un periodo de fechas de {@link ConsultasEstadisticas}.
 */
public class HistorialEstadisticas {

    private final ConsultasEstadisticas consultas;

    public HistorialEstadisticas(GestionarEstadisticasGatewayIntPort datos) {
        this.consultas = new ConsultasEstadisticas(datos);
    }

    public List<EntradaHistorial> historial(Integer anioInicio, Integer anioFin) {
        // Sin años: los últimos 5 años, desde 2020 como mínimo
        int anioActual = Year.now().getValue();
        int desde = anioInicio != null ? anioInicio : Math.max(2020, anioActual - 4);
        int hasta = anioFin != null ? anioFin : anioActual;

        List<EntradaHistorial> historial = new ArrayList<>();
        for (int anio = desde; anio <= hasta; anio++) {
            for (int semestre = 1; semestre <= 2; semestre++) {
                historial.add(EntradaHistorial.de(porSemestre(anio, semestre)));
            }
        }

        // Más reciente primero
        historial.sort(Comparator.comparingInt(EntradaHistorial::anio)
                .thenComparingInt(EntradaHistorial::semestre)
                .reversed());
        return historial;
    }

    private EstadisticasPorSemestre porSemestre(int anio, int semestre) {
        String periodoAcademico = anio + "-" + semestre;
        LocalDate inicio = semestre == 1 ? LocalDate.of(anio, 1, 1) : LocalDate.of(anio, 7, 1);
        LocalDate fin = semestre == 1 ? LocalDate.of(anio, 6, 30) : LocalDate.of(anio, 12, 31);

        // java.sql.Date: la API devuelve estas fechas como "yyyy-MM-dd", sin hora
        return new EstadisticasPorSemestre(consultas.porPeriodo(Date.valueOf(inicio), Date.valueOf(fin)), anio,
                semestre, periodoAcademico, "porSemestre",
                (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + anio);
    }
}
