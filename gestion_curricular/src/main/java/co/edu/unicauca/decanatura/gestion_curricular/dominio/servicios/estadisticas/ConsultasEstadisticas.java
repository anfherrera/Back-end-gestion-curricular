package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.ESTADOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.PROCESOS;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.enRango;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.idPrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.normalizarEstadistica;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.normalizarEstado;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;

/**
 * Conteos de solicitudes por proceso, estado, programa y periodo de fechas, y la comparación entre dos periodos.
 */
public class ConsultasEstadisticas {

    private final GestionarEstadisticasGatewayIntPort datos;

    public ConsultasEstadisticas(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

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
        List<Solicitud> delProceso = solicitudes(datos).stream()
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
        List<Solicitud> conEstado = solicitudes(datos).stream()
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
        List<Solicitud> delPrograma = solicitudes(datos).stream()
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
        List<Solicitud> enPeriodo = solicitudes(datos).stream()
                .filter(s -> enRango(s, fechaInicio, fechaFin))
                .collect(Collectors.toList());

        Map<String, Object> estadisticas = agregar(enPeriodo);
        estadisticas.put("fechaInicio", fechaInicio);
        estadisticas.put("fechaFin", fechaFin);
        return normalizarEstadistica(estadisticas);
    }

    public Map<String, Object> porProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin) {
        List<Solicitud> filtradas = solicitudes(datos).stream()
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
        List<Solicitud> filtradas = solicitudes(datos).stream()
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
}
