package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import java.util.Map;

/**
 * Textos, colores e íconos que acompañan a las estadísticas en el dashboard.
 * Los íconos de mes, programa y proceso van vacíos: el frontend pone los suyos.
 */
final class PresentacionEstadisticas {

    static final String SIN_ICONO = "";

    private static final Map<String, String> COLOR_MES = Map.ofEntries(
            Map.entry("Enero", "#FF6B6B"), Map.entry("Febrero", "#4ECDC4"), Map.entry("Marzo", "#45B7D1"),
            Map.entry("Abril", "#96CEB4"), Map.entry("Mayo", "#FFEAA7"), Map.entry("Junio", "#DDA0DD"),
            Map.entry("Julio", "#98D8C8"), Map.entry("Agosto", "#F7DC6F"), Map.entry("Septiembre", "#BB8FCE"),
            Map.entry("Octubre", "#85C1E9"), Map.entry("Noviembre", "#F8C471"), Map.entry("Diciembre", "#F1948A"));

    private static final Map<String, String> DESCRIPCION_MES = Map.ofEntries(
            Map.entry("Enero", "Mes de inicio de ano academico - Periodo de inscripciones"),
            Map.entry("Febrero", "Mes de inicio de clases - Periodo de ajustes academicos"),
            Map.entry("Marzo", "Mes de estabilizacion academica - Periodo regular"),
            Map.entry("Abril", "Mes de evaluaciones parciales - Periodo de examenes"),
            Map.entry("Mayo", "Mes de mitad de semestre - Periodo de seguimiento"),
            Map.entry("Junio", "Mes de finalizacion de semestre - Periodo de examenes finales"),
            Map.entry("Julio", "Mes de vacaciones - Periodo de cursos de verano"),
            Map.entry("Agosto", "Mes de inicio de segundo semestre - Periodo de inscripciones"),
            Map.entry("Septiembre", "Mes de estabilizacion - Periodo regular de clases"),
            Map.entry("Octubre", "Mes de evaluaciones - Periodo de examenes parciales"),
            Map.entry("Noviembre", "Mes de preparacion final - Periodo de proyectos"),
            Map.entry("Diciembre", "Mes de finalizacion de ano - Periodo de graduaciones"));

    private static final Map<String, String> COLOR_PROCESO = Map.of(
            TipoProcesoSolicitud.HOMOLOGACION, "#3B82F6",
            TipoProcesoSolicitud.PAZ_Y_SALVO, "#10B981",
            TipoProcesoSolicitud.REINGRESO, "#F59E0B",
            TipoProcesoSolicitud.CURSOS_VERANO, "#8B5CF6",
            TipoProcesoSolicitud.ECAES, "#EF4444");

    private static final Map<String, String> COLOR_PROGRAMA = Map.of(
            "Ingenieria de Sistemas", "#3B82F6",
            "Ingenieria Electronica y Telecomunicaciones", "#10B981",
            "Ingenieria Automatica Industrial", "#F59E0B",
            "Tecnologia en Telematica", "#8B5CF6");

    private static final Map<String, String> DESCRIPCION_PROGRAMA = Map.of(
            "Ingenieria de Sistemas",
            "Programa de Ingenieria de Sistemas - Desarrollo de software y sistemas informaticos",
            "Ingenieria Electronica y Telecomunicaciones",
            "Programa de Ingenieria Electronica y Telecomunicaciones - Sistemas electronicos y comunicaciones",
            "Ingenieria Automatica Industrial",
            "Programa de Ingenieria Automatica Industrial - Automatizacion y control industrial",
            "Tecnologia en Telematica",
            "Programa de Tecnologia en Telematica - Redes y telecomunicaciones");

    private static final String GRIS = "#6B7280";

    private PresentacionEstadisticas() {
    }

    static String colorMes(String mes) {
        return COLOR_MES.getOrDefault(mes, GRIS);
    }

    static String descripcionMes(String mes) {
        return DESCRIPCION_MES.getOrDefault(mes, "Periodo academico");
    }

    static String colorProceso(String proceso) {
        return COLOR_PROCESO.getOrDefault(proceso, GRIS);
    }

    static String colorPrograma(String programa) {
        return COLOR_PROGRAMA.getOrDefault(programa, GRIS);
    }

    static String descripcionPrograma(String programa) {
        return DESCRIPCION_PROGRAMA.getOrDefault(programa, "Programa academico");
    }

    /** Descripción corta del proceso (consultas por proceso). */
    static String descripcionProceso(String tipoProceso) {
        switch (tipoProceso.toUpperCase()) {
            case "REINGRESO":
                return "Solicitudes de reingreso a programas academicos";
            case "HOMOLOGACION":
                return "Solicitudes de homologacion de materias";
            case "ECAES":
                return "Solicitudes para examenes ECAES";
            case "CURSO_VERANO":
                return "Solicitudes para cursos intersemestrales";
            case "PAZ_SALVO":
                return "Solicitudes de paz y salvo";
            default:
                return "Proceso academico";
        }
    }

    /** Descripción del proceso en el dashboard detallado. */
    static String descripcionProcesoDetallada(String proceso) {
        if (proceso.contains("Reingreso")) {
            return "Solicitudes de reingreso a programas academicos";
        } else if (proceso.contains("Homologacion")) {
            return "Solicitudes de homologacion de materias";
        } else if (proceso.contains("ECAES")) {
            return "Solicitudes de inscripcion a examenes ECAES";
        } else if (proceso.contains("Verano")) {
            return "Solicitudes de cursos intersemestrales/verano";
        } else if (proceso.contains("Paz y Salvo")) {
            return "Solicitudes de paz y salvo";
        }
        return "Proceso academico";
    }

    static String descripcionEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "aprobado":
            case "aprobada":
            case "aprobadas":
                return "Solicitudes aprobadas y procesadas";
            case "rechazado":
            case "rechazada":
            case "rechazadas":
                return "Solicitudes rechazadas";
            case "enviada":
            case "enviadas":
                return "Solicitudes enviadas pendientes de revision";
            case "en_proceso":
            case "en proceso":
            case "pendiente":
                return "Solicitudes en proceso de evaluacion";
            case "borrador":
                return "Solicitudes en borrador";
            default:
                return "Estado: " + estado;
        }
    }

    static String colorEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "aprobado":
            case "aprobada":
            case "aprobadas":
                return "#28a745";
            case "enviada":
            case "enviadas":
                return "#ffc107";
            case "en_proceso":
            case "en proceso":
            case "pendiente":
                return "#17a2b8";
            case "rechazado":
            case "rechazada":
            case "rechazadas":
                return "#dc3545";
            case "borrador":
                return "#6c757d";
            default:
                return "#007bff";
        }
    }

    static String iconoEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "aprobado":
            case "aprobada":
            case "aprobadas":
                return "fas fa-check-circle";
            case "enviada":
            case "enviadas":
                return "fas fa-paper-plane";
            case "en_proceso":
            case "en proceso":
            case "pendiente":
                return "fas fa-clock";
            case "rechazado":
            case "rechazada":
            case "rechazadas":
                return "fas fa-times-circle";
            case "borrador":
                return "fas fa-edit";
            default:
                return "fas fa-question-circle";
        }
    }
}
