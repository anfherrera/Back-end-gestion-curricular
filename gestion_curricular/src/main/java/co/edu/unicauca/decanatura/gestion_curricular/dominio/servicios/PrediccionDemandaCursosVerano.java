package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.calcularDemandaEstimada;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.calcularDemandaEstimadaMes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.calcularVentana;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.encontrarMesPico;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.estimar;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.porcentajeVariacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatoDemandaCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.Estimacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ProyeccionDemandaCursosVerano.VentanaPeriodo;

/**
 * Predicción de la demanda de cursos de verano (regresión lineal simple sobre el histórico mensual)
 * y las recomendaciones y alertas que se derivan de ella. Es lógica de negocio pura: no conoce la
 * base de datos; recibe los datos ya extraídos de cada solicitud. El modelo está en
 * {@link ProyeccionDemandaCursosVerano}, las recomendaciones en {@link RecomendacionesDemandaCursosVerano} y las
 * alertas en {@link AlertasDemandaCursosVerano}.
 */
@Slf4j
public class PrediccionDemandaCursosVerano {

    /**
     * Genera predicciones de demanda para cursos de verano basadas en datos historicos
     */
    public Map<String, Object> generar(
            List<DatoDemandaCursoVerano> solicitudesCursosVerano,
            Map<String, Integer> demandaPorMes,
            List<Map<String, Object>> topMaterias,
            List<Map<String, Object>> analisisPorPrograma) {

        Map<String, Object> predicciones = new HashMap<>();

        try {
            // 1. PREDICCION DE DEMANDA TOTAL PARA EL PROXIMO PERIODO
            // "Proximo periodo" = el periodo de verano completo: misma cantidad de meses que el observado,
            // para que la estimacion sea comparable con la demanda actual (el total de todos los meses).
            VentanaPeriodo ventana = calcularVentana(demandaPorMes);
            int demandaActual = solicitudesCursosVerano.size();
            int demandaEstimadaProximoPeriodo = calcularDemandaEstimada(demandaActual, demandaPorMes, ventana);

            // 2. PREDICCIONES POR MATERIA CON REGRESION LINEAL
            List<Map<String, Object>> materiasConTendenciaCreciente = new ArrayList<>();
            List<Map<String, Object>> materiasConTendenciaDecreciente = new ArrayList<>();
            List<Map<String, Object>> materiasEstables = new ArrayList<>();

            for (Map<String, Object> materia : topMaterias) {
                String nombreMateria = (String) materia.get("nombre");
                int solicitudesActuales = ((Number) materia.get("solicitudes")).intValue();

                // Se utiliza regresion lineal para estimar la demanda por materia
                // (la materia de la solicitud es la del curso solicitado)
                Estimacion estimacion = estimar(solicitudesCursosVerano,
                    s -> s.materia() != null && s.materia().equalsIgnoreCase(nombreMateria),
                    solicitudesActuales, ventana, 0.05);
                Map<String, Object> prediccionMateria = prediccion(nombreMateria, solicitudesActuales, estimacion);

                if ("CRECIENTE".equals(estimacion.tendencia())) {
                    materiasConTendenciaCreciente.add(prediccionMateria);
                } else if ("DECRECIENTE".equals(estimacion.tendencia())) {
                    materiasConTendenciaDecreciente.add(prediccionMateria);
                } else {
                    materiasEstables.add(prediccionMateria);
                }
            }

            // 3. PREDICCIONES POR PROGRAMA CON REGRESION LINEAL
            List<Map<String, Object>> programasConTendenciaCreciente = new ArrayList<>();
            List<Map<String, Object>> programasConTendenciaDecreciente = new ArrayList<>();
            List<Map<String, Object>> programasEstables = new ArrayList<>();
            List<Map<String, Object>> todasLasPrediccionesPorPrograma = new ArrayList<>(); // Lista completa para frontend

            for (Map<String, Object> programa : analisisPorPrograma) {
                String nombrePrograma = (String) programa.get("nombre");
                int solicitudesActuales = ((Number) programa.get("solicitudes")).intValue();

                // Se aplica regresion lineal para estimar la demanda por programa
                Estimacion estimacion = estimar(solicitudesCursosVerano,
                    s -> s.programa() != null && s.programa().equalsIgnoreCase(nombrePrograma),
                    solicitudesActuales, ventana, 0.08);
                Map<String, Object> prediccionPrograma = prediccion(nombrePrograma, solicitudesActuales, estimacion);

                // Clasificamos la prediccion segun la tendencia detectada
                if ("CRECIENTE".equals(estimacion.tendencia())) {
                    programasConTendenciaCreciente.add(prediccionPrograma);
                } else if ("DECRECIENTE".equals(estimacion.tendencia())) {
                    programasConTendenciaDecreciente.add(prediccionPrograma);
                } else {
                    programasEstables.add(prediccionPrograma);
                }

                // Guardamos cada prediccion para que el frontend pueda mostrar el detalle completo
                todasLasPrediccionesPorPrograma.add(prediccionPrograma);
            }

            // 4. PREDICCIONES TEMPORALES
            Map<String, Object> prediccionesTemporales = new HashMap<>();
            String mesPico = encontrarMesPico(demandaPorMes);
            int demandaEstimadaMesPico = calcularDemandaEstimadaMes(demandaPorMes.get(mesPico));

            prediccionesTemporales.put("mesPico", mesPico);
            prediccionesTemporales.put("demandaActualMesPico", demandaPorMes.get(mesPico));
            prediccionesTemporales.put("demandaEstimadaMesPico", demandaEstimadaMesPico);
            prediccionesTemporales.put("mesesRecomendados", Arrays.asList("Julio", "Agosto", "Septiembre"));

            // 5. RECOMENDACIONES FUTURAS ACCIONABLES Y DETALLADAS
            List<Map<String, Object>> recomendacionesFuturas = RecomendacionesDemandaCursosVerano.generar(
                    materiasConTendenciaCreciente, programasConTendenciaCreciente, demandaPorMes, mesPico,
                    demandaEstimadaMesPico);

            // ALERTAS CRITICAS (Situaciones que requieren atencion inmediata)
            List<Map<String, Object>> alertasCriticas = AlertasDemandaCursosVerano.generar(
                    materiasConTendenciaCreciente, programasConTendenciaCreciente);

            // Construir resultado de predicciones con estructura mejorada
            predicciones.put("demandaEstimadaProximoPeriodo", demandaEstimadaProximoPeriodo);
            predicciones.put("materiasConTendenciaCreciente", materiasConTendenciaCreciente);
            predicciones.put("materiasConTendenciaDecreciente", materiasConTendenciaDecreciente);
            predicciones.put("materiasEstables", materiasEstables);
            predicciones.put("programasConTendenciaCreciente", programasConTendenciaCreciente);
            predicciones.put("programasConTendenciaDecreciente", programasConTendenciaDecreciente);
            predicciones.put("programasEstables", programasEstables); // Agregado
            predicciones.put("todasLasPrediccionesPorPrograma", todasLasPrediccionesPorPrograma); // Lista completa
            predicciones.put("prediccionesTemporales", prediccionesTemporales);
            predicciones.put("recomendaciones", recomendacionesFuturas); // Recomendaciones (para acceso interno)
            predicciones.put("alertasCriticas", alertasCriticas);
            predicciones.put("confiabilidad", "MEDIA");
            predicciones.put("fechaPrediccion", new Date()); // Fecha de prediccion

            // Estadisticas de las recomendaciones
            Map<String, Object> estadisticasRecomendaciones = new HashMap<>();
            long recsAlta = recomendacionesFuturas.stream()
                .filter(r -> "ALTA".equals(r.get("prioridad")))
                .count();
            long recsMedia = recomendacionesFuturas.stream()
                .filter(r -> "MEDIA".equals(r.get("prioridad")))
                .count();
            long recsBaja = recomendacionesFuturas.stream()
                .filter(r -> "BAJA".equals(r.get("prioridad")))
                .count();

            estadisticasRecomendaciones.put("totalRecomendaciones", recomendacionesFuturas.size());
            estadisticasRecomendaciones.put("prioridadAlta", recsAlta);
            estadisticasRecomendaciones.put("prioridadMedia", recsMedia);
            estadisticasRecomendaciones.put("prioridadBaja", recsBaja);
            estadisticasRecomendaciones.put("alertasCriticas", alertasCriticas.size());
            predicciones.put("estadisticasRecomendaciones", estadisticasRecomendaciones);

        } catch (Exception e) {
            log.error("Predicciones - ERROR CRITICO generando predicciones ");
            log.error("Tipo de error:  {}", e.getClass().getName());
            log.error("Mensaje:  {}", e.getMessage());
            log.error("Stack trace completo:");
            log.error("Error en estadísticas", e);
            log.error("==========================================");

            // Retornar predicciones basicas en caso de error
            predicciones.put("demandaEstimadaProximoPeriodo", solicitudesCursosVerano.size());
            predicciones.put("materiasConTendenciaCreciente", new ArrayList<>());
            predicciones.put("materiasConTendenciaDecreciente", new ArrayList<>());
            predicciones.put("materiasEstables", new ArrayList<>());
            predicciones.put("programasConTendenciaCreciente", new ArrayList<>());
            predicciones.put("programasConTendenciaDecreciente", new ArrayList<>());
            predicciones.put("prediccionesTemporales", new HashMap<>());
            // ELIMINADO: recomendacionesFuturas (duplicado)
            predicciones.put("confiabilidad", "BAJA");
        }

        return predicciones;
    }

    /**
     * Predicción de una materia o un programa. No expone campos técnicos (pendiente, rSquared, modeloUtilizado)
     * porque no aportan al usuario final.
     */
    private static Map<String, Object> prediccion(String nombre, int demandaActual, Estimacion estimacion) {
        int demandaEstimada = estimacion.demandaEstimada();
        Map<String, Object> prediccion = new HashMap<>();
        prediccion.put("nombre", nombre);
        prediccion.put("demandaActual", demandaActual);
        prediccion.put("demandaEstimada", demandaEstimada);
        prediccion.put("tendencia", estimacion.tendencia());
        prediccion.put("variacion", demandaEstimada - demandaActual);
        prediccion.put("porcentajeVariacion", porcentajeVariacion(demandaActual, demandaEstimada));
        return prediccion;
    }
}
