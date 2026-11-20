package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.ESTADOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.PROCESOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.normalizarEstadistica;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;

/**
 * Resumen completo (filtrable por periodo académico, programa y proceso) con las estadísticas globales y las de
 * cada proceso y estado, y el dashboard ejecutivo que se arma a partir de él o de {@link EstadisticasGlobales}.
 */
public class ResumenEstadisticas {

    private final GestionarEstadisticasGatewayIntPort datos;
    private final EstadisticasGlobales globales;

    public ResumenEstadisticas(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
        this.globales = new EstadisticasGlobales(datos);
    }

    public Map<String, Object> resumenCompleto(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> filtradas = solicitudes(datos, periodoAcademico, idPrograma);

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

    /**
     * Las estadísticas globales del resumen del periodo (ya normalizado) y programa. Si se pide un proceso, los
     * KPI son los de ese proceso (ver {@link #aplicarKpisDelProceso}).
     */
    public Map<String, Object> globalesDelPeriodo(String periodoAcademico, Integer idPrograma, String proceso) {
        Map<String, Object> resumen = resumenCompleto(periodoAcademico, idPrograma);
        @SuppressWarnings("unchecked")
        Map<String, Object> estadisticasGlobales = (Map<String, Object>) resumen.get("estadisticasGlobales");
        if (estadisticasGlobales != null && proceso != null && !proceso.trim().isEmpty()) {
            aplicarKpisDelProceso(estadisticasGlobales, resumen, TipoProcesoSolicitud.porNombre(proceso));
        }
        return estadisticasGlobales != null ? estadisticasGlobales : new HashMap<>();
    }

    /**
     * El resumen completo del periodo (ya normalizado) y programa. Si se pide un proceso, los KPI globales son
     * los de ese proceso, porTipoProceso se reduce a él y se anota en filtrosAplicados.
     */
    public Map<String, Object> resumenCompleto(String periodoAcademico, Integer idPrograma, String proceso) {
        Map<String, Object> resumen = resumenCompleto(periodoAcademico, idPrograma);
        String procesoNormalizado = TipoProcesoSolicitud.porNombre(proceso);
        if (procesoNormalizado == null) {
            return resumen;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> estadisticasGlobales = (Map<String, Object>) resumen.get("estadisticasGlobales");
        if (estadisticasGlobales != null) {
            aplicarKpisDelProceso(estadisticasGlobales, resumen, procesoNormalizado);
        }
        if (resumen.get("porTipoProceso") instanceof Map<?, ?> porTipoProceso && porTipoProceso.containsKey(procesoNormalizado)) {
            resumen.put("porTipoProceso", Map.of(procesoNormalizado, porTipoProceso.get(procesoNormalizado)));
        }
        if (resumen.get("filtrosAplicados") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> filtrosAplicados = (Map<String, Object>) resumen.get("filtrosAplicados");
            filtrosAplicados.put("proceso", procesoNormalizado);
        } else {
            Map<String, Object> filtros = new HashMap<>();
            filtros.put("proceso", procesoNormalizado);
            resumen.put("filtrosAplicados", filtros);
        }
        return resumen;
    }

    /**
     * Dashboard ejecutivo. Con periodo (ya normalizado) o programa sale del resumen completo y, si además se pide
     * un proceso, con los KPI de ese proceso; si no, de las estadísticas globales con el proceso tal cual.
     */
    public Map<String, Object> dashboardEjecutivo(String periodoAcademico, Integer idPrograma, String proceso) {
        boolean hayProceso = proceso != null && !proceso.trim().isEmpty();

        Map<String, Object> estadisticasGlobales;
        Map<String, Object> resumen = null;
        if (periodoAcademico != null || idPrograma != null) {
            resumen = resumenCompleto(periodoAcademico, idPrograma);
            @SuppressWarnings("unchecked")
            Map<String, Object> globalesDelResumen = (Map<String, Object>) resumen.get("estadisticasGlobales");
            estadisticasGlobales = globalesDelResumen;
        } else {
            estadisticasGlobales = globales.globales(proceso, idPrograma, null, null);
        }

        if (hayProceso && estadisticasGlobales != null && resumen != null) {
            aplicarKpisDelProceso(estadisticasGlobales, resumen, TipoProcesoSolicitud.porNombre(proceso));
        }

        Map<String, Object> dashboardEjecutivo = new HashMap<>();
        dashboardEjecutivo.put("resumenGlobal", estadisticasGlobales);
        dashboardEjecutivo.put("topProcesos", estadisticasGlobales != null ? estadisticasGlobales.get("porTipoProceso") : new HashMap<>());
        dashboardEjecutivo.put("resumenEstados", estadisticasGlobales != null ? estadisticasGlobales.get("porEstado") : new HashMap<>());
        dashboardEjecutivo.put("fechaGeneracion", new Date());

        if (periodoAcademico != null || idPrograma != null || hayProceso) {
            Map<String, Object> filtrosAplicados = new HashMap<>();
            if (periodoAcademico != null) {
                filtrosAplicados.put("periodoAcademico", periodoAcademico);
            }
            if (idPrograma != null) {
                filtrosAplicados.put("idPrograma", idPrograma);
            }
            if (hayProceso) {
                filtrosAplicados.put("proceso", proceso); // el texto recibido, sin normalizar
            }
            dashboardEjecutivo.put("filtrosAplicados", filtrosAplicados);
        }
        return dashboardEjecutivo;
    }

    /**
     * Reemplaza los KPI globales por los del proceso (clave de {@link TipoProcesoSolicitud}), tomados de
     * resumen.porTipoProceso, que tiene las estadísticas completas de cada proceso (estadisticasGlobales.porTipoProceso
     * solo tiene conteos). No hace nada si el proceso es null o no está en el resumen.
     */
    static void aplicarKpisDelProceso(Map<String, Object> estadisticasGlobales, Map<String, Object> resumen, String proceso) {
        if (proceso == null) {
            return;
        }
        Object porTipoProceso = resumen.get("porTipoProceso");
        if (!(porTipoProceso instanceof Map<?, ?> porTipo) || !(porTipo.get(proceso) instanceof Map<?, ?> datosProceso)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> statsProceso = (Map<String, Object>) datosProceso;
        estadisticasGlobales.put("totalSolicitudes", statsProceso.getOrDefault("totalSolicitudes", 0));
        estadisticasGlobales.put("totalAprobadas", statsProceso.getOrDefault("totalAprobadas", 0));
        estadisticasGlobales.put("totalRechazadas", statsProceso.getOrDefault("totalRechazadas", 0));
        estadisticasGlobales.put("totalEnProceso", statsProceso.getOrDefault("totalEnProceso", 0));
        int totalEnviadas = 0;
        if (statsProceso.get("porEstado") instanceof Map<?, ?> porEstado && porEstado.get(EstadosSolicitud.ENVIADA.name()) instanceof Number env) {
            totalEnviadas = env.intValue();
        }
        estadisticasGlobales.put("totalEnviadas", totalEnviadas);
        estadisticasGlobales.put("porcentajeAprobacion", statsProceso.getOrDefault("porcentajeAprobacion", 0.0));
        estadisticasGlobales.put("porEstado", statsProceso.getOrDefault("porEstado", new HashMap<>()));
        estadisticasGlobales.put("porTipoProceso", Map.of(proceso, statsProceso));
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
}
