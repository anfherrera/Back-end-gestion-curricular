package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.diasDeProcesamiento;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;

/** Contadores de una agrupación del dashboard (un proceso, un mes o un programa). */
final class ContadorSolicitudes {

    private static final ZoneId ZONA_ACADEMICA = ZoneId.of("America/Bogota");

    int total;
    int aprobadas;
    int rechazadas;
    int enviadas;
    int enProceso;
    final List<Double> tiempos = new ArrayList<>();
    final List<Date> fechas = new ArrayList<>();
    final Map<String, Integer> procesos = new HashMap<>();

    /**
     * Inicio de los últimos 90 días con los que {@link #tendenciaReciente} compara: hasta hoy o, si el periodo
     * ({@code YYYY-1} o {@code YYYY-2}) ya terminó, hasta su último día.
     */
    static Date corteTendencia(String periodo) {
        LocalDate referencia = LocalDate.now(ZONA_ACADEMICA);
        if (periodo != null && periodo.matches("\\d{4}-[12]")) {
            int anio = Integer.parseInt(periodo.substring(0, 4));
            LocalDate fin = periodo.endsWith("-1") ? LocalDate.of(anio, 6, 30) : LocalDate.of(anio, 12, 31);
            if (fin.isBefore(referencia)) {
                referencia = fin;
            }
        }
        return Date.from(referencia.minusDays(89).atStartOfDay(ZONA_ACADEMICA).toInstant());
    }

    double eficiencia() {
        return total > 0 ? (aprobadas * 100.0) / total : 0;
    }

    double tiempoPromedio() {
        return tiempos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    String tendenciaReciente(Date corte) {
        if (fechas.size() < 2) {
            return "Estable";
        }
        long recientes = fechas.stream().filter(f -> !f.before(corte)).count();
        long anteriores = fechas.size() - recientes;
        return recientes > anteriores ? "Creciente" : recientes < anteriores ? "Decreciente" : "Estable";
    }

    /**
     * Cuenta la solicitud en su estado. Con {@code separarEnProceso} las aprobadas por funcionario o coordinador
     * van a enProceso; si no, cuentan como enviadas. Las aprobadas y rechazadas suman su tiempo de procesamiento.
     */
    void contar(Solicitud solicitud, boolean separarEnProceso) {
        total++;
        String estado = estadoActual(solicitud);
        switch (EstadosSolicitud.valueOf(estado)) { // estadoActual siempre devuelve la forma canónica
            case APROBADA -> aprobadas++;
            case RECHAZADA -> rechazadas++;
            case APROBADA_FUNCIONARIO, APROBADA_COORDINADOR -> {
                if (separarEnProceso) enProceso++; else enviadas++;
            }
            default -> enviadas++;
        }
        if (EstadosSolicitud.APROBADA.name().equals(estado) || EstadosSolicitud.RECHAZADA.name().equals(estado)) {
            Double dias = diasDeProcesamiento(solicitud);
            if (dias != null) {
                tiempos.add(dias);
            }
        }
    }
}
