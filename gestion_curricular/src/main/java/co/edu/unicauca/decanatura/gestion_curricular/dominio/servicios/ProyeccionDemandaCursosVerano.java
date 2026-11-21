package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatoDemandaCursoVerano;

/**
 * Modelo de la predicción de demanda de cursos de verano: regresión lineal simple sobre el histórico mensual,
 * acotada a un rango realista, y la tendencia que resulta de comparar la estimación con la demanda actual.
 */
final class ProyeccionDemandaCursosVerano {

    private static final List<String> MESES = List.of("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

    /** Variacion porcentual (redondeada) a partir de la cual una tendencia deja de ser ESTABLE. */
    private static final int UMBRAL_TENDENCIA = 5;

    private ProyeccionDemandaCursosVerano() {
    }

    /** Meses (1 = Enero ... 12 = Diciembre) que abarca el periodo observado, del primero al ultimo con solicitudes. */
    record VentanaPeriodo(int primerMes, int ultimoMes) {
        int cantidadMeses() {
            return ultimoMes - primerMes + 1;
        }
    }

    /** Demanda estimada para el proximo periodo y su tendencia respecto al periodo actual. */
    record Estimacion(int demandaEstimada, String tendencia) {
    }

    /**
     * Ventana del periodo observado: del primer al ultimo mes con solicitudes. Es null si no hay datos.
     */
    static VentanaPeriodo calcularVentana(Map<String, Integer> demandaPorMes) {
        int primero = 0;
        int ultimo = 0;
        for (int i = 0; i < MESES.size(); i++) {
            if (demandaPorMes.getOrDefault(MESES.get(i), 0) > 0) {
                if (primero == 0) {
                    primero = i + 1;
                }
                ultimo = i + 1;
            }
        }
        return primero == 0 ? null : new VentanaPeriodo(primero, ultimo);
    }

    /**
     * Proyecta el proximo periodo completo con REGRESION LINEAL SIMPLE.
     * x = numero de mes, y = solicitudes de ese mes; los meses de la ventana sin solicitudes cuentan como 0.
     * El proximo periodo tiene la misma cantidad de meses que el observado y continua la tendencia:
     * se predice cada uno de esos meses y se suman, para que el resultado sea comparable con el total
     * del periodo actual (la suma de todos sus meses).
     * Devuelve null si la ventana tiene menos de 2 meses (no hay recta que ajustar).
     */
    private static Double proyectarPeriodo(Map<Integer, Integer> solicitudesPorMes, VentanaPeriodo ventana) {
        if (ventana == null || ventana.cantidadMeses() < 2) {
            return null;
        }
        SimpleRegression regression = new SimpleRegression();
        for (int mes = ventana.primerMes(); mes <= ventana.ultimoMes(); mes++) {
            regression.addData(mes, solicitudesPorMes.getOrDefault(mes, 0));
        }
        double total = 0;
        for (int i = 1; i <= ventana.cantidadMeses(); i++) {
            total += Math.max(0, regression.predict(ventana.ultimoMes() + i));
        }
        return total;
    }

    /** Mantiene la prediccion en un rango realista: entre el 80% y el 200% de la demanda actual. */
    private static int acotar(double prediccion, int demandaActual) {
        int demandaEstimada = (int) Math.round(Math.max(prediccion, demandaActual * 0.8));
        return Math.min(demandaEstimada, demandaActual * 2);
    }

    /**
     * Calcula la demanda total estimada para el proximo periodo de verano.
     */
    static int calcularDemandaEstimada(int demandaActual, Map<String, Integer> demandaPorMes, VentanaPeriodo ventana) {
        Map<Integer, Integer> porNumeroDeMes = new HashMap<>();
        for (int i = 0; i < MESES.size(); i++) {
            porNumeroDeMes.put(i + 1, demandaPorMes.getOrDefault(MESES.get(i), 0));
        }
        Double proyeccion = proyectarPeriodo(porNumeroDeMes, ventana);
        if (proyeccion == null) {
            // No hay suficientes datos historicos, usar estimacion conservadora
            return (int) Math.round(demandaActual * 1.15);
        }
        return acotar(proyeccion, demandaActual);
    }

    /**
     * Estima la demanda de una materia o un programa para el proximo periodo completo.
     * La tendencia sale de comparar esa estimacion con la demanda actual, asi que siempre concuerda
     * con el porcentaje de variacion que se muestra.
     * Con menos de 2 meses en la ventana se usa una estimacion conservadora (+factor).
     */
    static Estimacion estimar(List<DatoDemandaCursoVerano> solicitudes, Predicate<DatoDemandaCursoVerano> filtro,
            int demandaActual, VentanaPeriodo ventana, double factorConservador) {
        Map<Integer, Integer> solicitudesPorMes = new HashMap<>();
        for (DatoDemandaCursoVerano solicitud : solicitudes) {
            Date fecha = solicitud.fechaRegistro();
            if (fecha != null && filtro.test(solicitud)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fecha);
                solicitudesPorMes.merge(cal.get(Calendar.MONTH) + 1, 1, Integer::sum);
            }
        }

        Double proyeccion = proyectarPeriodo(solicitudesPorMes, ventana);
        if (proyeccion == null) {
            // Datos insuficientes: estimacion conservadora; ceil asegura un crecimiento de al menos 1
            return new Estimacion((int) Math.ceil(demandaActual * (1 + factorConservador)), "CRECIENTE");
        }

        int demandaEstimada = acotar(proyeccion, demandaActual);
        int porcentaje = porcentajeVariacion(demandaActual, demandaEstimada);
        String tendencia = porcentaje >= UMBRAL_TENDENCIA ? "CRECIENTE"
                : porcentaje <= -UMBRAL_TENDENCIA ? "DECRECIENTE" : "ESTABLE";
        return new Estimacion(demandaEstimada, tendencia);
    }

    static int porcentajeVariacion(int demandaActual, int demandaEstimada) {
        return demandaActual > 0
                ? (int) Math.round(((double) (demandaEstimada - demandaActual) / demandaActual) * 100.0)
                : 0;
    }

    /**
     * Encuentra el mes con mayor demanda
     */
    static String encontrarMesPico(Map<String, Integer> demandaPorMes) {
        return demandaPorMes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Agosto");
    }

    /**
     * Calcula la demanda estimada para un mes especifico
     */
    static int calcularDemandaEstimadaMes(int demandaActual) {
        return (int) (demandaActual * 1.2); // 20% de crecimiento para el mes pico
    }
}
