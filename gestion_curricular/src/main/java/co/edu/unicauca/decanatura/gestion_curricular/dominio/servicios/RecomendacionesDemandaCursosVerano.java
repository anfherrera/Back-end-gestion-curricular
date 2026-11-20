package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recomendaciones accionables que se derivan de la predicción de demanda de cursos de verano: ampliar la oferta de
 * cada materia que crece, priorizar cada programa que crece y anticipar la oferta del mes pico.
 */
final class RecomendacionesDemandaCursosVerano {

    private RecomendacionesDemandaCursosVerano() {
    }

    /** Primero las de materia, luego las de programa y por último la del mes pico, si tuvo solicitudes. */
    static List<Map<String, Object>> generar(List<Map<String, Object>> materiasConTendenciaCreciente,
            List<Map<String, Object>> programasConTendenciaCreciente, Map<String, Integer> demandaPorMes,
            String mesPico, int demandaEstimadaMesPico) {
        List<Map<String, Object>> recomendacionesFuturas = new ArrayList<>();

        // Recomendaciones especificas por materia
        for (Map<String, Object> materia : materiasConTendenciaCreciente) {
            recomendacionesFuturas.add(porMateria(materia));
        }

        // Recomendaciones con enfoque estrategico por programa
        for (Map<String, Object> programa : programasConTendenciaCreciente) {
            recomendacionesFuturas.add(porPrograma(programa));
        }

        // Recomendacion para la planificacion temporal
        if (demandaPorMes.get(mesPico) > 0) {
            recomendacionesFuturas.add(temporal(demandaPorMes.get(mesPico), mesPico, demandaEstimadaMesPico));
        }
        return recomendacionesFuturas;
    }

    private static Map<String, Object> porMateria(Map<String, Object> materia) {
        String nombreMateria = (String) materia.get("nombre");
        int demandaActualMateria = ((Number) materia.get("demandaActual")).intValue();
        int demandaEstimadaMateria = ((Number) materia.get("demandaEstimada")).intValue();
        int variacion = ((Number) materia.get("variacion")).intValue();
        int porcentajeVariacion = ((Number) materia.get("porcentajeVariacion")).intValue();

        // Calcular recursos necesarios
        int gruposActuales = Math.max(1, (int) Math.ceil(demandaActualMateria / 20.0));
        int gruposRecomendados = Math.max(1, (int) Math.ceil(demandaEstimadaMateria / 20.0));
        int docentesNecesarios = gruposRecomendados;
        int aulasNecesarias = gruposRecomendados;

        // Determinar prioridad y urgencia
        String prioridad = porcentajeVariacion > 30 ? "ALTA" :
                          porcentajeVariacion > 15 ? "MEDIA" : "BAJA";

        Map<String, Object> recomendacion = new HashMap<>();
        recomendacion.put("id", "MAT_" + nombreMateria.replaceAll("\\s+", "_").toUpperCase());
        recomendacion.put("tipo", "OFERTA_MATERIA");
        recomendacion.put("categoria", "CURSOS_VERANO");
        recomendacion.put("prioridad", prioridad);
        recomendacion.put("titulo", "Ampliar oferta de " + nombreMateria);
        recomendacion.put("materia", nombreMateria);

        // Datos actuales vs proyectados
        recomendacion.put("demandaActual", demandaActualMateria);
        recomendacion.put("demandaProyectada", demandaEstimadaMateria);
        recomendacion.put("crecimiento", variacion);
        recomendacion.put("porcentajeCrecimiento", porcentajeVariacion);

        // Recursos necesarios
        Map<String, Object> recursos = new HashMap<>();
        recursos.put("docentes", docentesNecesarios);
        recursos.put("aulas", aulasNecesarias);
        recursos.put("laboratorios", nombreMateria.toLowerCase().contains("programacion") ||
                                   nombreMateria.toLowerCase().contains("base") ? docentesNecesarios : 0);
        recursos.put("gruposActuales", gruposActuales);
        recursos.put("gruposRecomendados", gruposRecomendados);
        recursos.put("capacidadPorGrupo", 20);
        recomendacion.put("recursos", recursos);

        // Descripcion detallada
        recomendacion.put("descripcion", String.format(
            "La materia %s presenta una tendencia de crecimiento del %d%%, pasando de %d a %d estudiantes proyectados. " +
            "Se recomienda ampliar la oferta para garantizar cobertura completa.",
            nombreMateria, porcentajeVariacion, demandaActualMateria, demandaEstimadaMateria));

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
        recomendacion.put("acciones", acciones);

        // Justificacion simplificada
        recomendacion.put("justificacion", String.format(
            "Con base en las tendencias observadas en periodos anteriores, " +
            "se estima un incremento de %d %s (%d%% de crecimiento).",
            variacion,
            variacion == 1 ? "estudiante" : "estudiantes",
            porcentajeVariacion));

        // Impacto esperado
        Map<String, Object> impacto = new HashMap<>();
        impacto.put("estudiantesAtendidos", demandaEstimadaMateria);
        impacto.put("estudiantesBeneficiados", variacion);
        impacto.put("tasaCoberturaObjetivo", "100%");
        recomendacion.put("impacto", impacto);
        return recomendacion;
    }

    private static Map<String, Object> porPrograma(Map<String, Object> programa) {
        String nombrePrograma = (String) programa.get("nombre");
        int demandaActualPrograma = ((Number) programa.get("demandaActual")).intValue();
        int demandaEstimadaPrograma = ((Number) programa.get("demandaEstimada")).intValue();
        int variacion = ((Number) programa.get("variacion")).intValue();
        int porcentajeVariacion = ((Number) programa.get("porcentajeVariacion")).intValue();

        String prioridad = porcentajeVariacion > 25 ? "ALTA" :
                          porcentajeVariacion > 10 ? "MEDIA" : "BAJA";

        Map<String, Object> recomendacion = new HashMap<>();
        recomendacion.put("id", "PROG_" + nombrePrograma.replaceAll("\\s+", "_").toUpperCase());
        recomendacion.put("tipo", "ENFOQUE_PROGRAMA");
        recomendacion.put("categoria", "ESTRATEGIA_ACADEMICA");
        recomendacion.put("prioridad", prioridad);
        recomendacion.put("titulo", "Priorizar oferta para " + nombrePrograma);
        recomendacion.put("programa", nombrePrograma);

        // Datos del programa
        recomendacion.put("solicitudesActuales", demandaActualPrograma);
        recomendacion.put("solicitudesProyectadas", demandaEstimadaPrograma);
        recomendacion.put("incremento", variacion);
        recomendacion.put("porcentajeIncremento", porcentajeVariacion);

        // Descripcion estrategica
        recomendacion.put("descripcion", String.format(
            "%s muestra un crecimiento significativo del %d%% en la demanda de cursos de verano. " +
            "Se proyectan %d solicitudes para el proximo periodo, con un incremento de %d %s. " +
            "Es prioritario garantizar oferta academica suficiente para este programa.",
            nombrePrograma, porcentajeVariacion, demandaEstimadaPrograma, variacion,
            variacion == 1 ? "estudiante" : "estudiantes"));

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
        recomendacion.put("acciones", acciones);

        // Justificacion simplificada
        recomendacion.put("justificacion", String.format(
            "El analisis de tendencias historicas indica un crecimiento sostenido de %d %s. " +
            "Representa una oportunidad para mejorar los indicadores academicos del programa.",
            variacion,
            variacion == 1 ? "solicitud" : "solicitudes"));

        // Beneficios esperados
        List<String> beneficios = new ArrayList<>();
        beneficios.add("Reduccion de desercion por perdida de materias");
        beneficios.add("Mejora en tiempos de graduacion");
        beneficios.add("Mayor satisfaccion estudiantil");
        beneficios.add("Optimizacion de trayectorias academicas");
        recomendacion.put("beneficios", beneficios);
        return recomendacion;
    }

    private static Map<String, Object> temporal(int demandaActualMes, String mesPico, int demandaEstimadaMesPico) {
        int porcentajeCrecimientoMes = (int) Math.round(
            ((double)(demandaEstimadaMesPico - demandaActualMes) / demandaActualMes) * 100.0);

        Map<String, Object> recomendacionTemporal = new HashMap<>();
        recomendacionTemporal.put("id", "TEMPORAL_" + mesPico.toUpperCase());
        recomendacionTemporal.put("tipo", "PLANIFICACION_TEMPORAL");
        recomendacionTemporal.put("categoria", "CALENDARIO_ACADEMICO");
        recomendacionTemporal.put("prioridad", porcentajeCrecimientoMes > 20 ? "ALTA" : "MEDIA");
        recomendacionTemporal.put("titulo", "Preparar oferta anticipada para " + mesPico);

        // Datos temporales
        recomendacionTemporal.put("mesPico", mesPico);
        recomendacionTemporal.put("solicitudesActuales", demandaActualMes);
        recomendacionTemporal.put("solicitudesProyectadas", demandaEstimadaMesPico);
        recomendacionTemporal.put("incrementoEsperado", demandaEstimadaMesPico - demandaActualMes);
        recomendacionTemporal.put("porcentajeCrecimiento", porcentajeCrecimientoMes);

        // Descripcion
        recomendacionTemporal.put("descripcion", String.format(
            "%s es el mes pico de demanda para cursos de verano. Se proyecta un incremento del %d%%, " +
            "pasando de %d a %d solicitudes. Es fundamental anticipar la planificacion academica y logistica.",
            mesPico, porcentajeCrecimientoMes, demandaActualMes, demandaEstimadaMesPico));

        // Acciones temporales
        List<String> accionesTempo = new ArrayList<>();
        accionesTempo.add("Publicar calendario de cursos de verano 8 semanas antes de " + mesPico);
        accionesTempo.add(String.format("Preparar capacidad para %d estudiantes", demandaEstimadaMesPico));
        accionesTempo.add("Coordinar disponibilidad de docentes con 6 semanas de anticipacion");
        accionesTempo.add("Reservar aulas y laboratorios con 4 semanas de anticipacion");
        if (porcentajeCrecimientoMes > 20) {
            accionesTempo.add("URGENTE: Considerar contratacion temporal adicional");
        }
        recomendacionTemporal.put("acciones", accionesTempo);

        // Justificacion
        recomendacionTemporal.put("justificacion", String.format(
            "Analisis historico indica que %s concentra el mayor volumen de solicitudes. " +
            "Planificacion anticipada garantiza mejor experiencia estudiantil.",
            mesPico));
        return recomendacionTemporal;
    }
}
