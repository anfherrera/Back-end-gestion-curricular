package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.ESTADOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.PROCESOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesContadas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesDelPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesDelProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenCompleto;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenDelEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenDelProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenFiltrado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenSinFiltros;

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

    public ResumenCompleto resumenCompleto(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> filtradas = solicitudes(datos, periodoAcademico, idPrograma);

        GlobalesDelPeriodo estadisticasGlobales = globalesDe(filtradas);

        Map<String, ResumenDelProceso> porProceso = new HashMap<>();
        for (String proceso : PROCESOS) {
            porProceso.put(proceso, resumenDelProceso(filtradas, proceso));
        }

        Map<String, ResumenDelEstado> porEstado = new HashMap<>();
        for (String estado : ESTADOS) {
            porEstado.put(estado, resumenDelEstado(filtradas, estado));
        }

        int totalProgramas = datos.listarNombresProgramas().size();
        Date fechaGeneracion = new Date();

        if (periodoAcademico == null && idPrograma == null) {
            return new ResumenSinFiltros(estadisticasGlobales, porProceso, porEstado, totalProgramas, fechaGeneracion);
        }
        Map<String, Object> filtrosAplicados = new HashMap<>();
        if (periodoAcademico != null) {
            filtrosAplicados.put("periodoAcademico", periodoAcademico);
        }
        if (idPrograma != null) {
            filtrosAplicados.put("idPrograma", idPrograma);
        }
        return new ResumenFiltrado(estadisticasGlobales, porProceso, porEstado, totalProgramas, fechaGeneracion,
                filtrosAplicados);
    }

    /**
     * Las estadísticas globales del resumen del periodo (ya normalizado) y programa. Si se pide un proceso, los
     * KPI son los de ese proceso (ver {@link #conKpisDelProceso}).
     */
    public Globales globalesDelPeriodo(String periodoAcademico, Integer idPrograma, String proceso) {
        ResumenCompleto resumen = resumenCompleto(periodoAcademico, idPrograma);
        if (proceso != null && !proceso.trim().isEmpty()) {
            return conKpisDelProceso(resumen.estadisticasGlobales(), resumen, TipoProcesoSolicitud.porNombre(proceso));
        }
        return resumen.estadisticasGlobales();
    }

    /**
     * El resumen completo del periodo (ya normalizado) y programa. Si se pide un proceso, los KPI globales son
     * los de ese proceso, porTipoProceso se reduce a él y se anota en filtrosAplicados.
     */
    public ResumenCompleto resumenCompleto(String periodoAcademico, Integer idPrograma, String proceso) {
        ResumenCompleto resumen = resumenCompleto(periodoAcademico, idPrograma);
        String procesoNormalizado = TipoProcesoSolicitud.porNombre(proceso);
        if (procesoNormalizado == null) {
            return resumen;
        }
        Globales estadisticasGlobales = conKpisDelProceso(resumen.estadisticasGlobales(), resumen, procesoNormalizado);
        Map<String, ResumenDelProceso> porTipoProceso = resumen.porTipoProceso().containsKey(procesoNormalizado)
                ? Map.of(procesoNormalizado, resumen.porTipoProceso().get(procesoNormalizado))
                : resumen.porTipoProceso();
        Map<String, Object> filtrosAplicados = resumen instanceof ResumenFiltrado filtrado
                ? new HashMap<>(filtrado.filtrosAplicados())
                : new HashMap<>();
        filtrosAplicados.put("proceso", procesoNormalizado);
        return new ResumenFiltrado(estadisticasGlobales, porTipoProceso, resumen.porEstado(), resumen.totalProgramas(),
                resumen.fechaGeneracion(), filtrosAplicados);
    }

    /**
     * Dashboard ejecutivo. Con periodo (ya normalizado) o programa sale del resumen completo y, si además se pide
     * un proceso, con los KPI de ese proceso; si no, de las estadísticas globales con el proceso tal cual.
     */
    public Map<String, Object> dashboardEjecutivo(String periodoAcademico, Integer idPrograma, String proceso) {
        boolean hayProceso = proceso != null && !proceso.trim().isEmpty();

        Globales estadisticasGlobales;
        if (periodoAcademico != null || idPrograma != null) {
            ResumenCompleto resumen = resumenCompleto(periodoAcademico, idPrograma);
            estadisticasGlobales = hayProceso
                    ? conKpisDelProceso(resumen.estadisticasGlobales(), resumen, TipoProcesoSolicitud.porNombre(proceso))
                    : resumen.estadisticasGlobales();
        } else {
            estadisticasGlobales = globales.globales(proceso, idPrograma, null, null);
        }

        Map<String, Object> dashboardEjecutivo = new HashMap<>();
        dashboardEjecutivo.put("resumenGlobal", estadisticasGlobales);
        dashboardEjecutivo.put("topProcesos", estadisticasGlobales.porTipoProceso());
        dashboardEjecutivo.put("resumenEstados", porEstado(estadisticasGlobales));
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

    /** El desglose por estado de las globales; null en las del periodo, que no lo traen. */
    private static Map<String, Integer> porEstado(Globales estadisticasGlobales) {
        if (estadisticasGlobales instanceof GlobalesContadas contadas) {
            return contadas.porEstado();
        }
        if (estadisticasGlobales instanceof GlobalesDelProceso delProceso) {
            return delProceso.porEstado();
        }
        return null;
    }

    /**
     * Las globales con los KPI del proceso (clave de {@link TipoProcesoSolicitud}), tomados de
     * resumen.porTipoProceso, que tiene las estadísticas completas de cada proceso (las globales solo tienen
     * conteos). porPrograma y fechaConsulta siguen siendo los de las globales. Si el proceso es null o no está en el
     * resumen, devuelve las globales sin cambios.
     */
    static Globales conKpisDelProceso(Globales estadisticasGlobales, ResumenCompleto resumen, String proceso) {
        if (proceso == null) {
            return estadisticasGlobales;
        }
        ResumenDelProceso delProceso = resumen.porTipoProceso().get(proceso);
        if (delProceso == null) {
            return estadisticasGlobales;
        }
        return new GlobalesDelProceso(delProceso.totalSolicitudes(), delProceso.totalAprobadas(),
                delProceso.totalRechazadas(), delProceso.totalEnProceso(),
                delProceso.porEstado().get(EstadosSolicitud.ENVIADA.name()), delProceso.porcentajeAprobacion(),
                delProceso.porEstado(), Map.of(proceso, delProceso), estadisticasGlobales.porPrograma(),
                estadisticasGlobales.fechaConsulta());
    }

    private static GlobalesDelPeriodo globalesDe(List<Solicitud> solicitudes) {
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
        return new GlobalesDelPeriodo(total, totalAprobadas, totalRechazadas, totalEnProceso, totalEnviadas,
                Math.round(porcentajeAprobacion * 100.0) / 100.0, porTipoProceso, porPrograma, new Date());
    }

    private static ResumenDelProceso resumenDelProceso(List<Solicitud> solicitudes, String tipoProceso) {
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
        return new ResumenDelProceso(tipoProceso, total, totalAprobadas,
                porEstado.get(EstadosSolicitud.RECHAZADA.name()),
                porEstado.get(EstadosSolicitud.APROBADA_FUNCIONARIO.name()) + porEstado.get(EstadosSolicitud.APROBADA_COORDINADOR.name()),
                Math.round(porcentajeAprobacion * 10.0) / 10.0, porEstado, new HashMap<>(), new HashMap<>(),
                PresentacionEstadisticas.descripcionProceso(tipoProceso), new Date());
    }

    private static ResumenDelEstado resumenDelEstado(List<Solicitud> solicitudes, String estado) {
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
        return new ResumenDelEstado(estado, total,
                estado.equals(EstadosSolicitud.APROBADA.name()) ? total : 0,
                estado.equals(EstadosSolicitud.RECHAZADA.name()) ? total : 0,
                enProceso ? total : 0,
                estado.equals(EstadosSolicitud.APROBADA.name()) ? 100.0 : 0.0,
                porTipoProceso, porPrograma, new HashMap<>(), PresentacionEstadisticas.descripcionEstado(estado),
                new Date());
    }
}
