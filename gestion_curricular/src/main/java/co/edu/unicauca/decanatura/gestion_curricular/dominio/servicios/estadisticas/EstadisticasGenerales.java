package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.ESTADOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.PROCESOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.enRango;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.filtrarPorPeriodoYPrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.idPrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.normalizarEstadistica;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.normalizarEstado;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/**
 * Conteos de solicitudes por proceso, estado, programa y periodo, el resumen completo, el historial
 * por semestre y el número de estudiantes.
 */
public class EstadisticasGenerales {

    private final GestionarEstadisticasGatewayIntPort datos;

    public EstadisticasGenerales(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    List<Solicitud> solicitudes() {
        List<Solicitud> solicitudes = datos.listarSolicitudesConEstadosYUsuario();
        return solicitudes != null ? solicitudes : new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------
    // Globales: los totales por estado los cuenta la base de datos
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> globales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        int totalSolicitudes;
        int totalAprobadas;
        int totalRechazadas;
        int enProcesoFuncionario;
        int enProcesoCoordinador;
        int totalEnviadas;

        if (proceso == null && idPrograma == null && fechaInicio == null && fechaFin == null) {
            totalAprobadas = datos.contarSolicitudesPorEstado(EstadosSolicitud.APROBADA.name());
            totalRechazadas = datos.contarSolicitudesPorEstado(EstadosSolicitud.RECHAZADA.name());
            enProcesoFuncionario = datos.contarSolicitudesPorEstado(EstadosSolicitud.APROBADA_FUNCIONARIO.name());
            enProcesoCoordinador = datos.contarSolicitudesPorEstado(EstadosSolicitud.APROBADA_COORDINADOR.name());
            // PRE_REGISTRADO se trata como ENVIADA para estadísticas
            totalEnviadas = datos.contarSolicitudesPorEstado(EstadosSolicitud.ENVIADA.name()) + datos.contarSolicitudesPorEstado(EstadosSolicitud.PRE_REGISTRADO.name());

            totalSolicitudes = totalAprobadas + totalRechazadas + enProcesoFuncionario + enProcesoCoordinador + totalEnviadas;
            if (totalSolicitudes == 0) {
                totalSolicitudes = datos.contarTotalSolicitudes();
            }
        } else {
            totalAprobadas = datos.contarSolicitudesPorEstadoConFiltros(EstadosSolicitud.APROBADA.name(), proceso, idPrograma, fechaInicio, fechaFin);
            totalRechazadas = datos.contarSolicitudesPorEstadoConFiltros(EstadosSolicitud.RECHAZADA.name(), proceso, idPrograma, fechaInicio, fechaFin);
            enProcesoFuncionario = datos.contarSolicitudesPorEstadoConFiltros(EstadosSolicitud.APROBADA_FUNCIONARIO.name(), proceso, idPrograma, fechaInicio, fechaFin);
            enProcesoCoordinador = datos.contarSolicitudesPorEstadoConFiltros(EstadosSolicitud.APROBADA_COORDINADOR.name(), proceso, idPrograma, fechaInicio, fechaFin);
            totalEnviadas = datos.contarSolicitudesPorEstadoConFiltros(EstadosSolicitud.ENVIADA.name(), proceso, idPrograma, fechaInicio, fechaFin);

            totalSolicitudes = datos.contarSolicitudesConFiltros(proceso, idPrograma, fechaInicio, fechaFin);
            if (totalSolicitudes == 0) {
                totalSolicitudes = totalAprobadas + totalRechazadas + enProcesoFuncionario + enProcesoCoordinador + totalEnviadas;
            }
        }

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalSolicitudes", totalSolicitudes);
        estadisticas.put("totalAprobadas", totalAprobadas);
        estadisticas.put("totalRechazadas", totalRechazadas);
        estadisticas.put("totalEnviadas", totalEnviadas);
        // En Proceso = solo las aprobadas por funcionario y coordinador (no incluye enviadas)
        estadisticas.put("totalEnProceso", enProcesoFuncionario + enProcesoCoordinador);
        double porcentajeAprobacion = totalSolicitudes > 0 ? (double) totalAprobadas / totalSolicitudes * 100 : 0.0;
        estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 10.0) / 10.0);

        // Por tipo de proceso (agrupadas por tipo, no por persona)
        String procesoBuscado = proceso != null ? TipoProcesoSolicitud.porNombre(proceso) : null;
        Map<String, Integer> porTipoProceso = new HashMap<>();
        for (Solicitud solicitud : solicitudes()) {
            if (solicitud.getNombre_solicitud() == null) {
                continue;
            }
            String tipo = tipoProceso(solicitud);
            if (proceso != null && (procesoBuscado == null || !procesoBuscado.equals(tipo))) {
                continue;
            }
            Integer programaSolicitud = idPrograma(solicitud);
            if (idPrograma != null && programaSolicitud != null && !programaSolicitud.equals(idPrograma)) {
                continue;
            }
            if ((fechaInicio != null || fechaFin != null) && !enRango(solicitud, fechaInicio, fechaFin)) {
                continue;
            }
            if (tipo != null && !tipo.trim().isEmpty()) {
                porTipoProceso.merge(tipo, 1, Integer::sum);
            }
        }
        estadisticas.put("porTipoProceso", porTipoProceso);

        Map<String, Integer> porPrograma = new HashMap<>();
        for (Programa programa : datos.listarProgramas()) {
            if (programa != null && programa.getId_programa() != null && programa.getNombre_programa() != null) {
                porPrograma.put(programa.getNombre_programa(),
                        datos.contarSolicitudesPorProgramaConFiltros(programa.getId_programa(), proceso, fechaInicio, fechaFin));
            }
        }
        estadisticas.put("porPrograma", porPrograma);

        Map<String, Integer> porEstado = new HashMap<>();
        porEstado.put(EstadosSolicitud.APROBADA.name(), totalAprobadas);
        porEstado.put(EstadosSolicitud.RECHAZADA.name(), totalRechazadas);
        porEstado.put(EstadosSolicitud.APROBADA_FUNCIONARIO.name(), enProcesoFuncionario);
        porEstado.put(EstadosSolicitud.APROBADA_COORDINADOR.name(), enProcesoCoordinador);
        porEstado.put(EstadosSolicitud.ENVIADA.name(), totalEnviadas);
        estadisticas.put("porEstado", porEstado);
        estadisticas.put("fechaConsulta", new Date());
        return normalizarEstadistica(estadisticas);
    }

    // ------------------------------------------------------------------------------------------
    // Consultas por proceso, estado, programa y periodo
    // ------------------------------------------------------------------------------------------

    /** Total, por estado, por tipo de proceso y por programa, aprobadas, rechazadas, en proceso y porcentaje. */
    private Map<String, Object> agregar(List<Solicitud> solicitudes) {
        Map<String, Integer> porEstado = new LinkedHashMap<>();
        for (String e : ESTADOS) porEstado.put(e, 0);
        Map<String, Integer> porTipoProceso = new LinkedHashMap<>();
        for (String p : PROCESOS) porTipoProceso.put(p, 0);
        Map<String, Integer> porPrograma = new LinkedHashMap<>();
        for (String programa : datos.listarNombresProgramas()) {
            if (programa != null && !programa.isBlank()) porPrograma.put(programa, 0);
        }

        for (Solicitud s : solicitudes) {
            porEstado.merge(estadoActual(s), 1, Integer::sum);
            String proceso = tipoProceso(s);
            if (proceso != null) porTipoProceso.merge(proceso, 1, Integer::sum);
            String programa = nombrePrograma(s);
            if (programa != null && !programa.isBlank()) porPrograma.merge(programa, 1, Integer::sum);
        }

        int total = solicitudes.size();
        int aprobadas = porEstado.getOrDefault(EstadosSolicitud.APROBADA.name(), 0);

        Map<String, Object> r = new HashMap<>();
        r.put("totalSolicitudes", total);
        r.put("totalAprobadas", aprobadas);
        r.put("totalRechazadas", porEstado.getOrDefault(EstadosSolicitud.RECHAZADA.name(), 0));
        r.put("totalEnProceso", porEstado.getOrDefault(EstadosSolicitud.APROBADA_FUNCIONARIO.name(), 0)
                + porEstado.getOrDefault(EstadosSolicitud.APROBADA_COORDINADOR.name(), 0));
        r.put("totalEnviadas", porEstado.getOrDefault(EstadosSolicitud.ENVIADA.name(), 0));
        r.put("porcentajeAprobacion", total > 0 ? Math.round(aprobadas * 1000.0 / total) / 10.0 : 0.0);
        r.put("porEstado", porEstado);
        r.put("porTipoProceso", porTipoProceso);
        r.put("porPrograma", porPrograma);
        r.put("fechaConsulta", new Date());
        return r;
    }

    public Map<String, Object> porProceso(String tipoProceso) {
        if (tipoProceso == null || tipoProceso.trim().isEmpty()) {
            return normalizarEstadistica(new HashMap<>());
        }
        String proceso = TipoProcesoSolicitud.porNombre(tipoProceso);
        List<Solicitud> delProceso = solicitudes().stream()
                .filter(s -> proceso != null && proceso.equals(tipoProceso(s)))
                .collect(Collectors.toList());

        Map<String, Object> estadisticas = agregar(delProceso);
        estadisticas.put("tipoProceso", tipoProceso);
        estadisticas.put("porTipoProceso", new HashMap<>()); // Vacío para proceso específico
        estadisticas.put("porPrograma", new HashMap<>());    // Vacío para proceso específico
        estadisticas.put("descripcion", PresentacionEstadisticas.descripcionProceso(tipoProceso));
        return normalizarEstadistica(estadisticas);
    }

    public Map<String, Object> porEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return normalizarEstadistica(new HashMap<>());
        }
        String estadoNormalizado = normalizarEstado(estado);
        List<Solicitud> conEstado = solicitudes().stream()
                .filter(s -> estadoNormalizado.equals(estadoActual(s)))
                .collect(Collectors.toList());

        Map<String, Object> estadisticas = agregar(conEstado);
        estadisticas.put("estado", estado);
        estadisticas.put("porEstado", new HashMap<>()); // Vacío para estado específico
        estadisticas.put("porcentajeAprobacion", EstadosSolicitud.APROBADA.name().equals(estadoNormalizado) && !conEstado.isEmpty() ? 100.0 : 0.0);
        estadisticas.put("descripcionEstado", PresentacionEstadisticas.descripcionEstado(estado));
        return normalizarEstadistica(estadisticas);
    }

    public Map<String, Object> porPrograma(Integer idPrograma) {
        if (idPrograma == null || idPrograma < 1) {
            return normalizarEstadistica(new HashMap<>());
        }
        List<Solicitud> delPrograma = solicitudes().stream()
                .filter(s -> idPrograma.equals(idPrograma(s)))
                .collect(Collectors.toList());

        Map<String, Object> estadisticas = agregar(delPrograma);
        estadisticas.put("idPrograma", idPrograma);
        estadisticas.put("porPrograma", new HashMap<>()); // Vacío para programa específico
        return normalizarEstadistica(estadisticas);
    }

    public Map<String, Object> porPeriodo(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return normalizarEstadistica(new HashMap<>());
        }
        List<Solicitud> enPeriodo = solicitudes().stream()
                .filter(s -> enRango(s, fechaInicio, fechaFin))
                .collect(Collectors.toList());

        Map<String, Object> estadisticas = agregar(enPeriodo);
        estadisticas.put("fechaInicio", fechaInicio);
        estadisticas.put("fechaFin", fechaFin);
        return normalizarEstadistica(estadisticas);
    }

    public Map<String, Object> porProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin) {
        List<Solicitud> filtradas = solicitudes().stream()
                .filter(s -> idPrograma == null || idPrograma.equals(idPrograma(s)))
                .filter(s -> enRango(s, fechaInicio, fechaFin))
                .collect(Collectors.toList());

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("idPrograma", idPrograma);
        estadisticas.put("fechaInicio", fechaInicio);
        estadisticas.put("fechaFin", fechaFin);
        estadisticas.put("porTipoProceso", agregar(filtradas).get("porTipoProceso"));
        estadisticas.put("fechaConsulta", new Date());
        return estadisticas;
    }

    public Map<String, Object> porProcesoYEstado(String tipoProceso, String estado) {
        String proceso = TipoProcesoSolicitud.porNombre(tipoProceso);
        String estadoNormalizado = estado != null ? normalizarEstado(estado) : null;
        List<Solicitud> filtradas = solicitudes().stream()
                .filter(s -> proceso == null || proceso.equals(tipoProceso(s)))
                .filter(s -> estadoNormalizado == null || estadoNormalizado.equals(estadoActual(s)))
                .collect(Collectors.toList());

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("tipoProceso", tipoProceso);
        estadisticas.put("estado", estado);
        estadisticas.put("porPrograma", agregar(filtradas).get("porPrograma"));
        estadisticas.put("fechaConsulta", new Date());
        return estadisticas;
    }

    public Map<String, Object> tendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2) {
        Map<String, Object> periodo1 = porPeriodo(fechaInicio1, fechaFin1);
        Map<String, Object> periodo2 = porPeriodo(fechaInicio2, fechaFin2);

        int total1 = ((Number) periodo1.get("totalSolicitudes")).intValue();
        int total2 = ((Number) periodo2.get("totalSolicitudes")).intValue();
        double variacion = total1 > 0 ? ((double) (total2 - total1) / total1) * 100 : 0.0;

        Map<String, Object> tendencias = new HashMap<>();
        tendencias.put("periodo1", periodo1);
        tendencias.put("periodo2", periodo2);
        tendencias.put("variacionPorcentual", variacion);
        tendencias.put("tendencia", variacion > 0 ? "CRECIENTE" : variacion < 0 ? "DECRECIENTE" : "ESTABLE");
        tendencias.put("fechaConsulta", new Date());
        return tendencias;
    }

    // ------------------------------------------------------------------------------------------
    // Resumen completo (filtrable por periodo académico y programa)
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> resumenCompleto(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> filtradas = filtrarPorPeriodoYPrograma(solicitudes(), periodoAcademico, idPrograma);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("estadisticasGlobales", globalesDe(filtradas));

        Map<String, Object> porProceso = new HashMap<>();
        for (String proceso : PROCESOS) {
            porProceso.put(proceso, resumenDelProceso(filtradas, proceso));
        }
        resumen.put("porTipoProceso", porProceso);

        Map<String, Object> porEstado = new HashMap<>();
        for (String estado : ESTADOS) {
            porEstado.put(estado, resumenDelEstado(filtradas, estado));
        }
        resumen.put("porEstado", porEstado);

        resumen.put("totalProgramas", datos.listarNombresProgramas().size());
        resumen.put("fechaGeneracion", new Date());

        if (periodoAcademico != null || idPrograma != null) {
            Map<String, Object> filtrosAplicados = new HashMap<>();
            if (periodoAcademico != null) {
                filtrosAplicados.put("periodoAcademico", periodoAcademico);
            }
            if (idPrograma != null) {
                filtrosAplicados.put("idPrograma", idPrograma);
            }
            resumen.put("filtrosAplicados", filtrosAplicados);
        }
        return resumen;
    }

    private static Map<String, Object> globalesDe(List<Solicitud> solicitudes) {
        int totalAprobadas = 0;
        int totalRechazadas = 0;
        int totalEnProceso = 0;
        int totalEnviadas = 0;
        Map<String, Integer> porTipoProceso = new HashMap<>();
        Map<String, Integer> porPrograma = new HashMap<>();

        for (Solicitud solicitud : solicitudes) {
            switch (EstadosSolicitud.valueOf(estadoActual(solicitud))) {
                case APROBADA -> totalAprobadas++;
                case RECHAZADA -> totalRechazadas++;
                case ENVIADA -> totalEnviadas++;
                default -> totalEnProceso++; // APROBADA_FUNCIONARIO y APROBADA_COORDINADOR
            }
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                porTipoProceso.merge(proceso, 1, Integer::sum);
            }
            String programa = nombrePrograma(solicitud);
            if (programa != null) {
                porPrograma.merge(programa, 1, Integer::sum);
            }
        }

        int total = solicitudes.size();
        double porcentajeAprobacion = total > 0 ? (totalAprobadas * 100.0) / total : 0.0;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalSolicitudes", total);
        estadisticas.put("totalAprobadas", totalAprobadas);
        estadisticas.put("totalRechazadas", totalRechazadas);
        estadisticas.put("totalEnProceso", totalEnProceso);
        estadisticas.put("totalEnviadas", totalEnviadas);
        estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 100.0) / 100.0);
        estadisticas.put("porTipoProceso", porTipoProceso);
        estadisticas.put("porPrograma", porPrograma);
        estadisticas.put("fechaConsulta", new Date());
        return estadisticas;
    }

    private static Map<String, Object> resumenDelProceso(List<Solicitud> solicitudes, String tipoProceso) {
        List<Solicitud> delProceso = solicitudes.stream()
                .filter(s -> tipoProceso.equals(tipoProceso(s)))
                .collect(Collectors.toList());

        Map<String, Integer> porEstado = new HashMap<>();
        for (String estado : ESTADOS) {
            porEstado.put(estado, (int) delProceso.stream().filter(s -> estado.equals(estadoActual(s))).count());
        }

        int total = delProceso.size();
        int totalAprobadas = porEstado.get(EstadosSolicitud.APROBADA.name());
        double porcentajeAprobacion = total > 0 ? (totalAprobadas * 100.0) / total : 0.0;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("tipoProceso", tipoProceso);
        estadisticas.put("totalSolicitudes", total);
        estadisticas.put("totalAprobadas", totalAprobadas);
        estadisticas.put("totalRechazadas", porEstado.get(EstadosSolicitud.RECHAZADA.name()));
        estadisticas.put("totalEnProceso", porEstado.get(EstadosSolicitud.APROBADA_FUNCIONARIO.name()) + porEstado.get(EstadosSolicitud.APROBADA_COORDINADOR.name()));
        estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 10.0) / 10.0);
        estadisticas.put("porEstado", porEstado);
        estadisticas.put("porTipoProceso", new HashMap<>());
        estadisticas.put("porPrograma", new HashMap<>());
        estadisticas.put("descripcion", PresentacionEstadisticas.descripcionProceso(tipoProceso));
        estadisticas.put("fechaConsulta", new Date());
        return normalizarEstadistica(estadisticas);
    }

    private static Map<String, Object> resumenDelEstado(List<Solicitud> solicitudes, String estado) {
        List<Solicitud> conEstado = solicitudes.stream()
                .filter(s -> estado.equals(estadoActual(s)))
                .collect(Collectors.toList());

        Map<String, Integer> porTipoProceso = new HashMap<>();
        Map<String, Integer> porPrograma = new HashMap<>();
        for (Solicitud solicitud : conEstado) {
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                porTipoProceso.merge(proceso, 1, Integer::sum);
            }
            String programa = nombrePrograma(solicitud);
            if (programa != null) {
                porPrograma.merge(programa, 1, Integer::sum);
            }
        }

        int total = conEstado.size();
        boolean enProceso = estado.equals(EstadosSolicitud.APROBADA_FUNCIONARIO.name()) || estado.equals(EstadosSolicitud.APROBADA_COORDINADOR.name());

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("estado", estado);
        estadisticas.put("totalSolicitudes", total);
        estadisticas.put("totalAprobadas", estado.equals(EstadosSolicitud.APROBADA.name()) ? total : 0);
        estadisticas.put("totalRechazadas", estado.equals(EstadosSolicitud.RECHAZADA.name()) ? total : 0);
        estadisticas.put("totalEnProceso", enProceso ? total : 0);
        estadisticas.put("porcentajeAprobacion", estado.equals(EstadosSolicitud.APROBADA.name()) ? 100.0 : 0.0);
        estadisticas.put("porTipoProceso", porTipoProceso);
        estadisticas.put("porPrograma", porPrograma);
        estadisticas.put("porEstado", new HashMap<>());
        estadisticas.put("descripcionEstado", PresentacionEstadisticas.descripcionEstado(estado));
        estadisticas.put("fechaConsulta", new Date());
        return normalizarEstadistica(estadisticas);
    }

    // ------------------------------------------------------------------------------------------
    // Estudiantes
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> numeroTotalEstudiantes() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalEstudiantes", (long) datos.listarEstudiantes().size());
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Total de estudiantes registrados en el sistema");
        return resultado;
    }

    public Map<String, Object> estudiantesPorPrograma(Integer idPrograma) {
        List<Usuario> estudiantes = datos.listarEstudiantes();
        if (idPrograma != null) {
            estudiantes = estudiantes.stream()
                    .filter(e -> e.getObjPrograma() != null && e.getObjPrograma().getId_programa().equals(idPrograma))
                    .collect(Collectors.toList());
        }

        Map<String, Integer> porPrograma = new HashMap<>();
        for (Usuario estudiante : estudiantes) {
            if (estudiante.getObjPrograma() != null) {
                porPrograma.merge(estudiante.getObjPrograma().getNombre_programa(), 1, Integer::sum);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("estudiantesPorPrograma", porPrograma);
        resultado.put("totalEstudiantes", estudiantes.size());
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Distribucion de estudiantes por programa academico");
        return resultado;
    }

    // ------------------------------------------------------------------------------------------
    // Historial por semestre (1: enero-junio, 2: julio-diciembre)
    // ------------------------------------------------------------------------------------------

    public List<Map<String, Object>> historial(Integer anioInicio, Integer anioFin) {
        // Sin años: los últimos 5 años, desde 2020 como mínimo
        int anioActual = Year.now().getValue();
        int desde = anioInicio != null ? anioInicio : Math.max(2020, anioActual - 4);
        int hasta = anioFin != null ? anioFin : anioActual;

        List<Map<String, Object>> historial = new ArrayList<>();
        for (int anio = desde; anio <= hasta; anio++) {
            for (int semestre = 1; semestre <= 2; semestre++) {
                Map<String, Object> estadisticas = porSemestre(anio, semestre);

                Map<String, Object> entrada = new HashMap<>();
                entrada.put("periodo", anio + "-" + semestre);
                entrada.put("anio", anio);
                entrada.put("semestre", semestre);
                entrada.put("descripcion", (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + anio);
                for (String clave : List.of("totalSolicitudes", "totalAprobadas", "totalRechazadas", "totalEnProceso",
                        "porcentajeAprobacion", "fechaInicio", "fechaFin")) {
                    entrada.put(clave, estadisticas.get(clave));
                }
                historial.add(entrada);
            }
        }

        // Más reciente primero
        historial.sort(Comparator.<Map<String, Object>, Integer>comparing(e -> (Integer) e.get("anio"))
                .thenComparing(e -> (Integer) e.get("semestre"))
                .reversed());
        return historial;
    }

    /** @param periodoAcademico con formato YYYY-1 o YYYY-2 */
    public Map<String, Object> porPeriodoAcademico(String periodoAcademico) {
        if (periodoAcademico == null || !periodoAcademico.matches("^\\d{4}-[12]$")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Formato de período académico inválido. Use formato: YYYY-P (ej: 2024-2)");
            return error;
        }
        String[] partes = periodoAcademico.split("-");
        Map<String, Object> estadisticas = porSemestre(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
        estadisticas.put("periodoAcademico", periodoAcademico);
        return estadisticas;
    }

    private Map<String, Object> porSemestre(int anio, int semestre) {
        LocalDate inicio = semestre == 1 ? LocalDate.of(anio, 1, 1) : LocalDate.of(anio, 7, 1);
        LocalDate fin = semestre == 1 ? LocalDate.of(anio, 6, 30) : LocalDate.of(anio, 12, 31);

        // java.sql.Date: la API devuelve estas fechas como "yyyy-MM-dd", sin hora
        Map<String, Object> estadisticas = porPeriodo(java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
        estadisticas.put("anio", anio);
        estadisticas.put("semestre", semestre);
        estadisticas.put("periodoAcademico", anio + "-" + semestre);
        estadisticas.put("tipoConsulta", "porSemestre");
        estadisticas.put("descripcion", (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + anio);
        return estadisticas;
    }
}
