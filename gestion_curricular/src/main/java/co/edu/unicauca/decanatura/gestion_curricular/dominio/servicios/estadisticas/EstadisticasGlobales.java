package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.eficienciaAprobacion;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.enRango;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.idPrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.normalizarEstadistica;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tasaProcesamiento;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;

/**
 * Estadísticas globales: los totales por estado los cuenta la base de datos (con filtros de proceso, programa y
 * fechas, o sin ellos), y el rendimiento que se deriva de ellos.
 */
public class EstadisticasGlobales {

    private final GestionarEstadisticasGatewayIntPort datos;

    public EstadisticasGlobales(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

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
        for (Solicitud solicitud : solicitudes(datos)) {
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

    /** Estadísticas globales sin filtros, con la tasa de procesamiento y la eficiencia de aprobación. */
    public Map<String, Object> rendimiento() {
        Map<String, Object> globales = globales(null, null, null, null);
        Integer total = (Integer) globales.get("totalSolicitudes");
        Integer aprobadas = (Integer) globales.get("totalAprobadas");
        Integer rechazadas = (Integer) globales.get("totalRechazadas");
        return Map.of(
                "estadisticasGlobales", globales,
                "indicadoresRendimiento", Map.of(
                        "tasaProcesamiento", tasaProcesamiento(total, aprobadas, rechazadas),
                        "eficienciaAprobacion", eficienciaAprobacion(aprobadas, rechazadas),
                        "tiempoPromedioProcesamiento", "N/A"),
                "fechaConsulta", new Date());
    }
}
