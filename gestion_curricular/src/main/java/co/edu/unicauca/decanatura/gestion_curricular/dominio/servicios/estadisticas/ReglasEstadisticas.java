package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import lombok.extern.slf4j.Slf4j;

/**
 * Reglas que comparten todas las estadísticas: a qué proceso y a qué estado pertenece una solicitud,
 * de qué programa es, y cómo se filtra por periodo académico y programa.
 */
@Slf4j
final class ReglasEstadisticas {

    static final String[] PROCESOS = {
            TipoProcesoSolicitud.REINGRESO, TipoProcesoSolicitud.HOMOLOGACION, TipoProcesoSolicitud.PAZ_Y_SALVO,
            TipoProcesoSolicitud.CURSOS_VERANO, TipoProcesoSolicitud.ECAES };

    static final String[] ESTADOS = {
            EstadosSolicitud.APROBADA.name(), EstadosSolicitud.RECHAZADA.name(), EstadosSolicitud.ENVIADA.name(), EstadosSolicitud.APROBADA_FUNCIONARIO.name(), EstadosSolicitud.APROBADA_COORDINADOR.name() };

    /** Calendar.MONTH devuelve 0-11: 0=Enero, 11=Diciembre. */
    static final String[] MESES = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

    private static final double MS_POR_DIA = 1000.0 * 60 * 60 * 24;

    /** Estados desconocidos ya reportados en el log (para no repetir la advertencia). */
    private static final Set<String> estadosDesconocidosReportados = ConcurrentHashMap.newKeySet();

    private ReglasEstadisticas() {
    }

    static String tipoProceso(Solicitud solicitud) {
        return TipoProcesoSolicitud.de(solicitud);
    }

    static String nombrePrograma(Solicitud solicitud) {
        if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getObjPrograma() == null) {
            return null;
        }
        return solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
    }

    static Integer idPrograma(Solicitud solicitud) {
        if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getObjPrograma() == null) {
            return null;
        }
        return solicitud.getObjUsuario().getObjPrograma().getId_programa();
    }

    static boolean enRango(Solicitud solicitud, Date inicio, Date fin) {
        Date fecha = solicitud.getFecha_registro_solicitud();
        return fecha != null
                && (inicio == null || !fecha.before(inicio))
                && (fin == null || !fecha.after(fin));
    }

    /** Nombre del mes (Enero..Diciembre) en que se registró la solicitud, o null si no tiene fecha. */
    static String mesDeRegistro(Solicitud solicitud) {
        Date fecha = solicitud.getFecha_registro_solicitud();
        if (fecha == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        return MESES[cal.get(Calendar.MONTH)];
    }

    /** Periodo académico (YYYY-1 o YYYY-2) de una fecha: enero-junio es 1, julio-diciembre es 2. */
    static String inferirPeriodoDesdeFecha(Date fecha) {
        if (fecha == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int anio = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH) + 1;
        return anio + "-" + (mes <= 6 ? 1 : 2);
    }

    /**
     * Filtra por periodo académico e idPrograma (null o vacío = sin filtro). Una solicitud es del periodo si
     * lo dice ella, si lo dice su curso de verano, o si su fecha de registro cae en él.
     */
    static List<Solicitud> filtrarPorPeriodoYPrograma(List<Solicitud> solicitudes, String periodoAcademico, Integer idPrograma) {
        String periodo = periodoAcademico != null && !periodoAcademico.trim().isEmpty() ? periodoAcademico.trim() : null;
        if (periodo == null && idPrograma == null) {
            return solicitudes;
        }
        return solicitudes.stream()
                .filter(s -> periodo == null || esDelPeriodo(s, periodo))
                .filter(s -> idPrograma == null || idPrograma.equals(idPrograma(s)))
                .collect(Collectors.toList());
    }

    private static boolean esDelPeriodo(Solicitud solicitud, String periodo) {
        if (solicitud.getPeriodo_academico() != null && solicitud.getPeriodo_academico().trim().equals(periodo)) {
            return true;
        }
        if (solicitud.getObjCursoOfertadoVerano() != null) {
            String periodoCurso = solicitud.getObjCursoOfertadoVerano().getPeriodo_academico();
            if (periodoCurso != null && periodoCurso.trim().equals(periodo)) {
                return true;
            }
        }
        return periodo.equals(inferirPeriodoDesdeFecha(solicitud.getFecha_registro_solicitud()));
    }

    /** Estado más reciente de la solicitud, normalizado a uno de {@link #ESTADOS}. */
    static String estadoActual(Solicitud solicitud) {
        if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
            return EstadosSolicitud.ENVIADA.name();
        }
        return solicitud.getEstadosSolicitud().stream()
                // Ignora estados sin texto (p. ej. el registro inicial vacío) y tolera fechas nulas
                .filter(e -> e.getEstado_actual() != null && !e.getEstado_actual().isBlank())
                .max(Comparator.comparing(EstadoSolicitud::getFecha_registro_estado,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(EstadoSolicitud::getEstado_actual)
                .map(ReglasEstadisticas::normalizarEstado)
                .orElse(EstadosSolicitud.ENVIADA.name());
    }

    /** Fecha del último cambio de estado (o la de registro si no tiene estados). */
    static Date fechaActualizacion(Solicitud solicitud) {
        if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
            return solicitud.getFecha_registro_solicitud();
        }
        return solicitud.getEstadosSolicitud().stream()
                .map(EstadoSolicitud::getFecha_registro_estado)
                .max(Date::compareTo)
                .orElse(solicitud.getFecha_registro_solicitud());
    }

    /** Días entre el registro y la última actualización, o null si falta alguna de las dos fechas. */
    static Double diasDeProcesamiento(Solicitud solicitud) {
        Date creacion = solicitud.getFecha_registro_solicitud();
        Date actualizacion = fechaActualizacion(solicitud);
        if (creacion == null || actualizacion == null) {
            return null;
        }
        return (actualizacion.getTime() - creacion.getTime()) / MS_POR_DIA;
    }

    /**
     * Unifica las distintas formas en que se guardan los estados ("Enviada", "ENVIADA", "Aprobado",
     * estados de cursos de verano...) en los 5 valores de {@link #ESTADOS}.
     */
    static String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) return EstadosSolicitud.ENVIADA.name();

        String clave = Normalizer.normalize(estado.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replace(' ', '_');

        switch (clave) {
            case "ENVIADA":
            case "ENVIADO":
            case "PRE_REGISTRADO":
            case "PREREGISTRADO":
            case "PREINSCRITO":
            case "PREINSCRITA":
            case "PENDIENTE":
                return EstadosSolicitud.ENVIADA.name();
            case "EN_PROCESO":
            case "ENPROCESO":
            case "APROBADA_FUNCIONARIO":
            case "APROBADO_FUNCIONARIO":
            case "PAGO_VALIDADO":       // Cursos de verano: pago revisado, falta completar la inscripción
                return EstadosSolicitud.APROBADA_FUNCIONARIO.name();
            case "APROBADA_COORDINADOR":
            case "APROBADO_COORDINADOR":
                return EstadosSolicitud.APROBADA_COORDINADOR.name();
            case "APROBADA":
            case "APROBADO":
            case "INSCRIPCION_COMPLETADA": // Cursos de verano: proceso terminado con éxito
            case "INSCRITO":
            case "INSCRITA":
                return EstadosSolicitud.APROBADA.name();
            case "RECHAZADA":
            case "RECHAZADO":
            case "PAGO_RECHAZADO":
                return EstadosSolicitud.RECHAZADA.name();
            default:
                // Se cuenta como ENVIADA para no descuadrar los totales, pero se avisa en el log.
                if (estadosDesconocidosReportados.add(estado)) {
                    log.warn("Estadísticas: estado de solicitud no reconocido '{}', se cuenta como ENVIADA", estado);
                }
                return EstadosSolicitud.ENVIADA.name();
        }
    }

    /** Completa con ceros y mapas vacíos los campos que el frontend espera siempre. */
    static Map<String, Object> normalizarEstadistica(Map<String, Object> estadistica) {
        estadistica.putIfAbsent("totalSolicitudes", 0);
        estadistica.putIfAbsent("totalAprobadas", 0);
        estadistica.putIfAbsent("totalRechazadas", 0);
        estadistica.putIfAbsent("totalEnProceso", 0);
        estadistica.putIfAbsent("porcentajeAprobacion", 0.0);
        estadistica.putIfAbsent("porTipoProceso", new HashMap<>());
        estadistica.putIfAbsent("porPrograma", new HashMap<>());
        estadistica.putIfAbsent("porEstado", new HashMap<>());
        estadistica.putIfAbsent("fechaConsulta", new Date());
        return estadistica;
    }

    /** Porcentaje de solicitudes decididas (aprobadas + rechazadas) sobre el total, sin redondear; 0 si no hay solicitudes. */
    static double tasaProcesamiento(Integer totalSolicitudes, Integer totalAprobadas, Integer totalRechazadas) {
        if (totalSolicitudes == null || totalSolicitudes == 0) {
            return 0.0;
        }
        int procesadas = (totalAprobadas != null ? totalAprobadas : 0) + (totalRechazadas != null ? totalRechazadas : 0);
        return (double) procesadas / totalSolicitudes * 100;
    }

    /** Porcentaje de aprobadas entre las decididas (aprobadas + rechazadas), sin redondear; 0 si no hay decididas. */
    static double eficienciaAprobacion(Integer totalAprobadas, Integer totalRechazadas) {
        int aprobadas = totalAprobadas != null ? totalAprobadas : 0;
        int decididas = aprobadas + (totalRechazadas != null ? totalRechazadas : 0);
        if (decididas == 0) {
            return 0.0;
        }
        return (double) aprobadas / decididas * 100;
    }

    static double redondear2(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
