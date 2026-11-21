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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AlertaCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DemandaCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DemandaProyectada;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.PrediccionesCalculadas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.PrediccionesCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.PrediccionesDeRespaldo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.RecomendacionCursosVerano;
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
     * Genera predicciones de demanda para cursos de verano basadas en datos historicos. Si el cálculo falla,
     * devuelve la predicción de respaldo.
     */
    public PrediccionesCursosVerano generar(
            List<DatoDemandaCursoVerano> solicitudesCursosVerano,
            Map<String, Integer> demandaPorMes,
            List<DemandaCursoVerano> topMaterias,
            List<DemandaCursoVerano> analisisPorPrograma) {

        try {
            // 1. PREDICCION DE DEMANDA TOTAL PARA EL PROXIMO PERIODO
            // "Proximo periodo" = el periodo de verano completo: misma cantidad de meses que el observado,
            // para que la estimacion sea comparable con la demanda actual (el total de todos los meses).
            VentanaPeriodo ventana = calcularVentana(demandaPorMes);
            int demandaActual = solicitudesCursosVerano.size();
            int demandaEstimadaProximoPeriodo = calcularDemandaEstimada(demandaActual, demandaPorMes, ventana);

            // 2. PREDICCIONES POR MATERIA CON REGRESION LINEAL
            List<DemandaProyectada> materiasConTendenciaCreciente = new ArrayList<>();
            List<DemandaProyectada> materiasConTendenciaDecreciente = new ArrayList<>();
            List<DemandaProyectada> materiasEstables = new ArrayList<>();

            for (DemandaCursoVerano materia : topMaterias) {
                String nombreMateria = materia.nombre();
                int solicitudesActuales = materia.solicitudes();

                // Se utiliza regresion lineal para estimar la demanda por materia
                // (la materia de la solicitud es la del curso solicitado)
                Estimacion estimacion = estimar(solicitudesCursosVerano,
                    s -> s.materia() != null && s.materia().equalsIgnoreCase(nombreMateria),
                    solicitudesActuales, ventana, 0.05);
                DemandaProyectada prediccionMateria = prediccion(nombreMateria, solicitudesActuales, estimacion);

                if ("CRECIENTE".equals(estimacion.tendencia())) {
                    materiasConTendenciaCreciente.add(prediccionMateria);
                } else if ("DECRECIENTE".equals(estimacion.tendencia())) {
                    materiasConTendenciaDecreciente.add(prediccionMateria);
                } else {
                    materiasEstables.add(prediccionMateria);
                }
            }

            // 3. PREDICCIONES POR PROGRAMA CON REGRESION LINEAL
            List<DemandaProyectada> programasConTendenciaCreciente = new ArrayList<>();
            List<DemandaProyectada> programasConTendenciaDecreciente = new ArrayList<>();
            List<DemandaProyectada> programasEstables = new ArrayList<>();
            List<DemandaProyectada> todasLasPrediccionesPorPrograma = new ArrayList<>(); // Lista completa para frontend

            for (DemandaCursoVerano programa : analisisPorPrograma) {
                String nombrePrograma = programa.nombre();
                int solicitudesActuales = programa.solicitudes();

                // Se aplica regresion lineal para estimar la demanda por programa
                Estimacion estimacion = estimar(solicitudesCursosVerano,
                    s -> s.programa() != null && s.programa().equalsIgnoreCase(nombrePrograma),
                    solicitudesActuales, ventana, 0.08);
                DemandaProyectada prediccionPrograma = prediccion(nombrePrograma, solicitudesActuales, estimacion);

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
            String mesPico = encontrarMesPico(demandaPorMes);
            int demandaEstimadaMesPico = calcularDemandaEstimadaMes(demandaPorMes.get(mesPico));
            PrediccionesCalculadas.PrediccionTemporal prediccionesTemporales =
                    new PrediccionesCalculadas.PrediccionTemporal(mesPico, demandaPorMes.get(mesPico),
                            demandaEstimadaMesPico, Arrays.asList("Julio", "Agosto", "Septiembre"));

            // 5. RECOMENDACIONES FUTURAS ACCIONABLES Y DETALLADAS
            List<RecomendacionCursosVerano> recomendacionesFuturas = RecomendacionesDemandaCursosVerano.generar(
                    materiasConTendenciaCreciente, programasConTendenciaCreciente, demandaPorMes, mesPico,
                    demandaEstimadaMesPico);

            // ALERTAS CRITICAS (Situaciones que requieren atencion inmediata)
            List<AlertaCursosVerano> alertasCriticas = AlertasDemandaCursosVerano.generar(
                    materiasConTendenciaCreciente, programasConTendenciaCreciente);

            // Estadisticas de las recomendaciones
            long recsAlta = recomendacionesFuturas.stream()
                .filter(r -> "ALTA".equals(r.prioridad()))
                .count();
            long recsMedia = recomendacionesFuturas.stream()
                .filter(r -> "MEDIA".equals(r.prioridad()))
                .count();
            long recsBaja = recomendacionesFuturas.stream()
                .filter(r -> "BAJA".equals(r.prioridad()))
                .count();
            PrediccionesCalculadas.EstadisticasRecomendaciones estadisticasRecomendaciones =
                    new PrediccionesCalculadas.EstadisticasRecomendaciones(recomendacionesFuturas.size(),
                            recsAlta, recsMedia, recsBaja, alertasCriticas.size());

            return new PrediccionesCalculadas(demandaEstimadaProximoPeriodo,
                    materiasConTendenciaCreciente, materiasConTendenciaDecreciente, materiasEstables,
                    programasConTendenciaCreciente, programasConTendenciaDecreciente, programasEstables,
                    todasLasPrediccionesPorPrograma, prediccionesTemporales, recomendacionesFuturas, alertasCriticas,
                    "MEDIA", new Date(), estadisticasRecomendaciones);

        } catch (Exception e) {
            log.error("Predicciones - ERROR CRITICO generando predicciones ");
            log.error("Tipo de error:  {}", e.getClass().getName());
            log.error("Mensaje:  {}", e.getMessage());
            log.error("Stack trace completo:");
            log.error("Error en estadísticas", e);
            log.error("==========================================");

            // Retornar predicciones basicas en caso de error
            return new PrediccionesDeRespaldo(solicitudesCursosVerano.size(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>(), "BAJA");
        }
    }

    /**
     * Predicción de una materia o un programa. No expone campos técnicos (pendiente, rSquared, modeloUtilizado)
     * porque no aportan al usuario final.
     */
    private static DemandaProyectada prediccion(String nombre, int demandaActual, Estimacion estimacion) {
        int demandaEstimada = estimacion.demandaEstimada();
        return new DemandaProyectada(nombre, demandaActual, demandaEstimada, estimacion.tendencia(),
                demandaEstimada - demandaActual, porcentajeVariacion(demandaActual, demandaEstimada));
    }
}
