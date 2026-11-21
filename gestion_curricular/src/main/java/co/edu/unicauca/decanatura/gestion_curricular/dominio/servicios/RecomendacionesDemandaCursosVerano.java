package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DemandaProyectada;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.RecomendacionCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.RecomendacionPorMateria;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.RecomendacionPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.RecomendacionTemporal;

/**
 * Recomendaciones accionables que se derivan de la predicción de demanda de cursos de verano: ampliar la oferta de
 * cada materia que crece, priorizar cada programa que crece y anticipar la oferta del mes pico.
 */
final class RecomendacionesDemandaCursosVerano {

    private RecomendacionesDemandaCursosVerano() {
    }

    /** Primero las de materia, luego las de programa y por último la del mes pico, si tuvo solicitudes. */
    static List<RecomendacionCursosVerano> generar(List<DemandaProyectada> materiasConTendenciaCreciente,
            List<DemandaProyectada> programasConTendenciaCreciente, Map<String, Integer> demandaPorMes,
            String mesPico, int demandaEstimadaMesPico) {
        List<RecomendacionCursosVerano> recomendacionesFuturas = new ArrayList<>();

        // Recomendaciones especificas por materia
        for (DemandaProyectada materia : materiasConTendenciaCreciente) {
            recomendacionesFuturas.add(porMateria(materia));
        }

        // Recomendaciones con enfoque estrategico por programa
        for (DemandaProyectada programa : programasConTendenciaCreciente) {
            recomendacionesFuturas.add(porPrograma(programa));
        }

        // Recomendacion para la planificacion temporal
        if (demandaPorMes.get(mesPico) > 0) {
            recomendacionesFuturas.add(temporal(demandaPorMes.get(mesPico), mesPico, demandaEstimadaMesPico));
        }
        return recomendacionesFuturas;
    }

    private static RecomendacionPorMateria porMateria(DemandaProyectada materia) {
        String nombreMateria = materia.nombre();
        int demandaActualMateria = materia.demandaActual();
        int demandaEstimadaMateria = materia.demandaEstimada();
        int variacion = materia.variacion();
        int porcentajeVariacion = materia.porcentajeVariacion();

        // Calcular recursos necesarios
        int gruposActuales = Math.max(1, (int) Math.ceil(demandaActualMateria / 20.0));
        int gruposRecomendados = Math.max(1, (int) Math.ceil(demandaEstimadaMateria / 20.0));
        int docentesNecesarios = gruposRecomendados;
        int aulasNecesarias = gruposRecomendados;

        // Determinar prioridad y urgencia
        String prioridad = porcentajeVariacion > 30 ? "ALTA" :
                          porcentajeVariacion > 15 ? "MEDIA" : "BAJA";
        String id = "MAT_" + nombreMateria.replaceAll("\\s+", "_").toUpperCase();

        // Recursos necesarios
        RecomendacionPorMateria.Recursos recursos = new RecomendacionPorMateria.Recursos(
                docentesNecesarios, aulasNecesarias,
                nombreMateria.toLowerCase().contains("programacion") ||
                        nombreMateria.toLowerCase().contains("base") ? docentesNecesarios : 0,
                gruposActuales, gruposRecomendados, 20);

        // Descripcion detallada
        String descripcion = String.format(
            "La materia %s presenta una tendencia de crecimiento del %d%%, pasando de %d a %d estudiantes proyectados. " +
            "Se recomienda ampliar la oferta para garantizar cobertura completa.",
            nombreMateria, porcentajeVariacion, demandaActualMateria, demandaEstimadaMateria);

        // Acciones especificas
        List<String> acciones = new ArrayList<>();
        if (gruposRecomendados > gruposActuales) {
            int gruposAdicionales = gruposRecomendados - gruposActuales;
            acciones.add(String.format("Abrir %d %s %s de 20 estudiantes",
                                      gruposAdicionales,
                                      gruposAdicionales == 1 ? "grupo" : "grupos",
                                      gruposAdicionales == 1 ? "adicional" : "adicionales"));
        }
        acciones.add(String.format("Contratar %d %s %s",
                                  docentesNecesarios,
                                  docentesNecesarios == 1 ? "docente" : "docentes",
                                  docentesNecesarios == 1 ? "especializado" : "especializados"));
        acciones.add(String.format("Reservar %d %s",
                                  aulasNecesarias,
                                  aulasNecesarias == 1 ? "aula o laboratorio" : "aulas o laboratorios"));
        if (porcentajeVariacion > 30) {
            acciones.add("URGENTE: Iniciar gestion con 2 meses de anticipacion");
        }
        acciones.add("Publicar oferta academica con suficiente antelacion");

        // Justificacion simplificada
        String justificacion = String.format(
            "Con base en las tendencias observadas en periodos anteriores, " +
            "se estima un incremento de %d %s (%d%% de crecimiento).",
            variacion,
            variacion == 1 ? "estudiante" : "estudiantes",
            porcentajeVariacion);

        // Impacto esperado
        RecomendacionPorMateria.Impacto impacto =
                new RecomendacionPorMateria.Impacto(demandaEstimadaMateria, variacion, "100%");

        return new RecomendacionPorMateria(id, "OFERTA_MATERIA", "CURSOS_VERANO", prioridad,
                "Ampliar oferta de " + nombreMateria, nombreMateria,
                demandaActualMateria, demandaEstimadaMateria, variacion, porcentajeVariacion,
                recursos, descripcion, acciones, justificacion, impacto);
    }

    private static RecomendacionPorPrograma porPrograma(DemandaProyectada programa) {
        String nombrePrograma = programa.nombre();
        int demandaActualPrograma = programa.demandaActual();
        int demandaEstimadaPrograma = programa.demandaEstimada();
        int variacion = programa.variacion();
        int porcentajeVariacion = programa.porcentajeVariacion();

        String prioridad = porcentajeVariacion > 25 ? "ALTA" :
                          porcentajeVariacion > 10 ? "MEDIA" : "BAJA";
        String id = "PROG_" + nombrePrograma.replaceAll("\\s+", "_").toUpperCase();

        // Descripcion estrategica
        String descripcion = String.format(
            "%s muestra un crecimiento significativo del %d%% en la demanda de cursos de verano. " +
            "Se proyectan %d solicitudes para el proximo periodo, con un incremento de %d %s. " +
            "Es prioritario garantizar oferta academica suficiente para este programa.",
            nombrePrograma, porcentajeVariacion, demandaEstimadaPrograma, variacion,
            variacion == 1 ? "estudiante" : "estudiantes");

        // Acciones estrategicas
        List<String> acciones = new ArrayList<>();
        acciones.add(String.format("Ampliar oferta de cursos de verano en %d %s",
                                  variacion,
                                  variacion == 1 ? "cupo" : "cupos"));
        acciones.add("Identificar materias criticas del programa con mayor demanda");
        acciones.add("Coordinar con director(a) de programa para validar necesidades");
        acciones.add("Evaluar disponibilidad de docentes del programa");
        if (porcentajeVariacion > 25) {
            acciones.add("CRITICO: Programa de alta demanda requiere atencion inmediata");
        }

        // Justificacion simplificada
        String justificacion = String.format(
            "El analisis de tendencias historicas indica un crecimiento sostenido de %d %s. " +
            "Representa una oportunidad para mejorar los indicadores academicos del programa.",
            variacion,
            variacion == 1 ? "solicitud" : "solicitudes");

        // Beneficios esperados
        List<String> beneficios = new ArrayList<>();
        beneficios.add("Reduccion de desercion por perdida de materias");
        beneficios.add("Mejora en tiempos de graduacion");
        beneficios.add("Mayor satisfaccion estudiantil");
        beneficios.add("Optimizacion de trayectorias academicas");

        return new RecomendacionPorPrograma(id, "ENFOQUE_PROGRAMA", "ESTRATEGIA_ACADEMICA", prioridad,
                "Priorizar oferta para " + nombrePrograma, nombrePrograma,
                demandaActualPrograma, demandaEstimadaPrograma, variacion, porcentajeVariacion,
                descripcion, acciones, justificacion, beneficios);
    }

    private static RecomendacionTemporal temporal(int demandaActualMes, String mesPico, int demandaEstimadaMesPico) {
        int porcentajeCrecimientoMes = (int) Math.round(
            ((double)(demandaEstimadaMesPico - demandaActualMes) / demandaActualMes) * 100.0);

        // Descripcion
        String descripcion = String.format(
            "%s es el mes pico de demanda para cursos de verano. Se proyecta un incremento del %d%%, " +
            "pasando de %d a %d solicitudes. Es fundamental anticipar la planificacion academica y logistica.",
            mesPico, porcentajeCrecimientoMes, demandaActualMes, demandaEstimadaMesPico);

        // Acciones temporales
        List<String> accionesTempo = new ArrayList<>();
        accionesTempo.add("Publicar calendario de cursos de verano 8 semanas antes de " + mesPico);
        accionesTempo.add(String.format("Preparar capacidad para %d estudiantes", demandaEstimadaMesPico));
        accionesTempo.add("Coordinar disponibilidad de docentes con 6 semanas de anticipacion");
        accionesTempo.add("Reservar aulas y laboratorios con 4 semanas de anticipacion");
        if (porcentajeCrecimientoMes > 20) {
            accionesTempo.add("URGENTE: Considerar contratacion temporal adicional");
        }

        // Justificacion
        String justificacion = String.format(
            "Analisis historico indica que %s concentra el mayor volumen de solicitudes. " +
            "Planificacion anticipada garantiza mejor experiencia estudiantil.",
            mesPico);

        return new RecomendacionTemporal("TEMPORAL_" + mesPico.toUpperCase(), "PLANIFICACION_TEMPORAL",
                "CALENDARIO_ACADEMICO", porcentajeCrecimientoMes > 20 ? "ALTA" : "MEDIA",
                "Preparar oferta anticipada para " + mesPico, mesPico,
                demandaActualMes, demandaEstimadaMesPico, demandaEstimadaMesPico - demandaActualMes,
                porcentajeCrecimientoMes, descripcion, accionesTempo, justificacion);
    }
}
