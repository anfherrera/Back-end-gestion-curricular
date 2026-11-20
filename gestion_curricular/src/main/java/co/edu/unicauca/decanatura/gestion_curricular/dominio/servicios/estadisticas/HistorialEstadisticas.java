package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EntradaHistorial;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPeriodoAcademico;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorSemestre;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.PeriodoAcademicoInvalido;

/**
 * Estadísticas por semestre (1: enero-junio, 2: julio-diciembre): el historial de varios años y un periodo
 * académico concreto. Cada semestre se cuenta como un periodo de fechas de {@link ConsultasEstadisticas}.
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

    /** @param periodoAcademico con formato YYYY-1 o YYYY-2 */
    public EstadisticasPeriodoAcademico porPeriodoAcademico(String periodoAcademico) {
        if (periodoAcademico == null || !periodoAcademico.matches("^\\d{4}-[12]$")) {
            return new PeriodoAcademicoInvalido("Formato de período académico inválido. Use formato: YYYY-P (ej: 2024-2)");
        }
        String[] partes = periodoAcademico.split("-");
        return porSemestre(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]), periodoAcademico);
    }

    private EstadisticasPorSemestre porSemestre(int anio, int semestre) {
        return porSemestre(anio, semestre, anio + "-" + semestre);
    }

    /** @param periodoAcademico cómo se muestra el periodo (el texto pedido, que puede llevar ceros a la izquierda) */
    private EstadisticasPorSemestre porSemestre(int anio, int semestre, String periodoAcademico) {
        LocalDate inicio = semestre == 1 ? LocalDate.of(anio, 1, 1) : LocalDate.of(anio, 7, 1);
        LocalDate fin = semestre == 1 ? LocalDate.of(anio, 6, 30) : LocalDate.of(anio, 12, 31);

        // java.sql.Date: la API devuelve estas fechas como "yyyy-MM-dd", sin hora
        return new EstadisticasPorSemestre(consultas.porPeriodo(Date.valueOf(inicio), Date.valueOf(fin)), anio,
                semestre, periodoAcademico, "porSemestre",
                (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + anio);
    }
}
