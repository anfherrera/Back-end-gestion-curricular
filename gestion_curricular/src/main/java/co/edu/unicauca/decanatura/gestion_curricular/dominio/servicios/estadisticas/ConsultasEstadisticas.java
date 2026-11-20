package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.ESTADOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.PROCESOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.enRango;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.idPrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.normalizarEstado;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ConteoSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ProcesosPorProgramaYPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ProgramasPorProcesoYEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TendenciasPorPeriodo;

/**
 * Conteos de solicitudes por proceso, estado, programa y periodo de fechas, y la comparación entre dos periodos.
 * Los argumentos llegan ya validados por el caso de uso (no nulos ni vacíos).
 */
public class ConsultasEstadisticas {

    private final GestionarEstadisticasGatewayIntPort datos;

    public ConsultasEstadisticas(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    /** Total, por estado, por tipo de proceso y por programa, aprobadas, rechazadas, en proceso y porcentaje. */
    private ConteoSolicitudes agregar(List<Solicitud> solicitudes) {
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
        return new ConteoSolicitudes(total, aprobadas,
                porEstado.getOrDefault(EstadosSolicitud.RECHAZADA.name(), 0),
                porEstado.getOrDefault(EstadosSolicitud.APROBADA_FUNCIONARIO.name(), 0)
                        + porEstado.getOrDefault(EstadosSolicitud.APROBADA_COORDINADOR.name(), 0),
                porEstado.getOrDefault(EstadosSolicitud.ENVIADA.name(), 0),
                total > 0 ? Math.round(aprobadas * 1000.0 / total) / 10.0 : 0.0,
                porEstado, porTipoProceso, porPrograma);
    }

    public EstadisticasPorProceso porProceso(String tipoProceso) {
        String proceso = TipoProcesoSolicitud.porNombre(tipoProceso);
        List<Solicitud> delProceso = solicitudes(datos).stream()
                .filter(s -> proceso != null && proceso.equals(tipoProceso(s)))
                .collect(Collectors.toList());

        return new EstadisticasPorProceso(agregar(delProceso).sinPorTipoProcesoNiPorPrograma(), new Date(),
                tipoProceso, PresentacionEstadisticas.descripcionProceso(tipoProceso));
    }

    public EstadisticasPorEstado porEstado(String estado) {
        String estadoNormalizado = normalizarEstado(estado);
        List<Solicitud> conEstado = solicitudes(datos).stream()
                .filter(s -> estadoNormalizado.equals(estadoActual(s)))
                .collect(Collectors.toList());

        double porcentaje = EstadosSolicitud.APROBADA.name().equals(estadoNormalizado) && !conEstado.isEmpty()
                ? 100.0 : 0.0;
        return new EstadisticasPorEstado(agregar(conEstado).sinPorEstado().conPorcentajeAprobacion(porcentaje),
                new Date(), estado, PresentacionEstadisticas.descripcionEstado(estado));
    }

    public EstadisticasPorPrograma porPrograma(Integer idPrograma) {
        List<Solicitud> delPrograma = solicitudes(datos).stream()
                .filter(s -> idPrograma.equals(idPrograma(s)))
                .collect(Collectors.toList());

        return new EstadisticasPorPrograma(agregar(delPrograma).sinPorPrograma(), new Date(), idPrograma);
    }

    public EstadisticasPorPeriodo porPeriodo(Date fechaInicio, Date fechaFin) {
        List<Solicitud> enPeriodo = solicitudes(datos).stream()
                .filter(s -> enRango(s, fechaInicio, fechaFin))
                .collect(Collectors.toList());

        return new EstadisticasPorPeriodo(agregar(enPeriodo), new Date(), fechaInicio, fechaFin);
    }

    public ProcesosPorProgramaYPeriodo porProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin) {
        List<Solicitud> filtradas = solicitudes(datos).stream()
                .filter(s -> idPrograma == null || idPrograma.equals(idPrograma(s)))
                .filter(s -> enRango(s, fechaInicio, fechaFin))
                .collect(Collectors.toList());

        return new ProcesosPorProgramaYPeriodo(idPrograma, fechaInicio, fechaFin, agregar(filtradas).porTipoProceso(),
                new Date());
    }

    public ProgramasPorProcesoYEstado porProcesoYEstado(String tipoProceso, String estado) {
        String proceso = TipoProcesoSolicitud.porNombre(tipoProceso);
        String estadoNormalizado = estado != null ? normalizarEstado(estado) : null;
        List<Solicitud> filtradas = solicitudes(datos).stream()
                .filter(s -> proceso == null || proceso.equals(tipoProceso(s)))
                .filter(s -> estadoNormalizado == null || estadoNormalizado.equals(estadoActual(s)))
                .collect(Collectors.toList());

        return new ProgramasPorProcesoYEstado(tipoProceso, estado, agregar(filtradas).porPrograma(), new Date());
    }

    public TendenciasPorPeriodo tendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2,
            Date fechaFin2) {
        EstadisticasPorPeriodo periodo1 = porPeriodo(fechaInicio1, fechaFin1);
        EstadisticasPorPeriodo periodo2 = porPeriodo(fechaInicio2, fechaFin2);

        int total1 = periodo1.totalSolicitudes();
        int total2 = periodo2.totalSolicitudes();
        double variacion = total1 > 0 ? ((double) (total2 - total1) / total1) * 100 : 0.0;

        return new TendenciasPorPeriodo(periodo1, periodo2, variacion,
                variacion > 0 ? "CRECIENTE" : variacion < 0 ? "DECRECIENTE" : "ESTABLE", new Date());
    }
}
