package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.porcentaje;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.redondear2;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;

/**
 * Vista del dashboard por estado (filtrable por periodo académico y programa): cuántas solicitudes hay en cada
 * estado, de qué procesos y programas, y el análisis de su distribución.
 */
public class DashboardPorEstado {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardPorEstado(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public Map<String, Object> porEstado(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudes(datos, periodoAcademico, idPrograma);

        Map<String, Integer> totalPorEstado = new HashMap<>();
        Map<String, Map<String, Integer>> procesosPorEstado = new HashMap<>();
        Map<String, Map<String, Integer>> programasPorEstado = new HashMap<>();
        for (String estado : ReglasEstadisticas.ESTADOS) {
            totalPorEstado.put(estado, 0);
            procesosPorEstado.put(estado, new HashMap<>());
            programasPorEstado.put(estado, new HashMap<>());
        }

        for (Solicitud solicitud : solicitudes) {
            String estado = estadoActual(solicitud);
            totalPorEstado.merge(estado, 1, Integer::sum);
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                procesosPorEstado.get(estado).merge(proceso, 1, Integer::sum);
            }
            String programa = nombrePrograma(solicitud);
            if (programa != null) {
                programasPorEstado.get(estado).merge(programa, 1, Integer::sum);
            }
        }

        int totalSolicitudes = solicitudes.size();
        Map<String, Object> resumenPorEstado = new HashMap<>();
        String estadoMasComun = null;
        String estadoMenosComun = null;
        String estadoMasEficiente = null;
        int maxSolicitudes = 0;
        int minSolicitudes = Integer.MAX_VALUE;

        // Siempre se incluyen los estados principales, aunque tengan 0 solicitudes
        for (String estado : new String[] { EstadosSolicitud.APROBADA.name(), EstadosSolicitud.ENVIADA.name(), EstadosSolicitud.RECHAZADA.name() }) {
            int total = totalPorEstado.get(estado);
            Map<String, Object> detalleEstado = new HashMap<>();
            detalleEstado.put("estado", estado);
            detalleEstado.put("cantidad", total);
            detalleEstado.put("porcentaje", redondear2(porcentaje(total, totalSolicitudes)));
            detalleEstado.put("procesos", procesosPorEstado.get(estado));
            detalleEstado.put("programas", programasPorEstado.get(estado));
            detalleEstado.put("color", PresentacionEstadisticas.colorEstado(estado));
            detalleEstado.put("icono", PresentacionEstadisticas.iconoEstado(estado));
            detalleEstado.put("descripcion", PresentacionEstadisticas.descripcionEstado(estado));
            resumenPorEstado.put(estado, detalleEstado);

            if (total > 0) {
                if (total > maxSolicitudes) {
                    maxSolicitudes = total;
                    estadoMasComun = estado;
                }
                if (total < minSolicitudes) {
                    minSolicitudes = total;
                    estadoMenosComun = estado;
                }
                if (EstadosSolicitud.APROBADA.name().equals(estado)) {
                    estadoMasEficiente = estado;
                }
            }
        }

        // "EN_PROCESO" junta las aprobadas por funcionario y por coordinador
        int totalEnProceso = totalPorEstado.get(EstadosSolicitud.APROBADA_FUNCIONARIO.name()) + totalPorEstado.get(EstadosSolicitud.APROBADA_COORDINADOR.name());
        Map<String, Integer> procesosEnProceso = new HashMap<>(procesosPorEstado.get(EstadosSolicitud.APROBADA_FUNCIONARIO.name()));
        procesosPorEstado.get(EstadosSolicitud.APROBADA_COORDINADOR.name()).forEach((k, v) -> procesosEnProceso.merge(k, v, Integer::sum));
        Map<String, Integer> programasEnProceso = new HashMap<>(programasPorEstado.get(EstadosSolicitud.APROBADA_FUNCIONARIO.name()));
        programasPorEstado.get(EstadosSolicitud.APROBADA_COORDINADOR.name()).forEach((k, v) -> programasEnProceso.merge(k, v, Integer::sum));

        Map<String, Object> detalleEnProceso = new HashMap<>();
        detalleEnProceso.put("estado", "EN_PROCESO");
        detalleEnProceso.put("cantidad", totalEnProceso);
        detalleEnProceso.put("porcentaje", redondear2(porcentaje(totalEnProceso, totalSolicitudes)));
        detalleEnProceso.put("procesos", procesosEnProceso);
        detalleEnProceso.put("programas", programasEnProceso);
        detalleEnProceso.put("color", PresentacionEstadisticas.colorEstado("EN_PROCESO"));
        detalleEnProceso.put("icono", PresentacionEstadisticas.iconoEstado("EN_PROCESO"));
        detalleEnProceso.put("descripcion", "Solicitudes en proceso de revisión");
        resumenPorEstado.put("EN_PROCESO", detalleEnProceso);

        if (totalEnProceso > 0 && totalEnProceso > maxSolicitudes) {
            maxSolicitudes = totalEnProceso;
            estadoMasComun = "EN_PROCESO";
        }

        int completadas = totalPorEstado.get(EstadosSolicitud.APROBADA.name()) + totalPorEstado.get(EstadosSolicitud.RECHAZADA.name());
        Map<String, Object> analisis = new HashMap<>();
        analisis.put("estadoMasComun", estadoMasComun);
        analisis.put("estadoMenosComun", estadoMenosComun);
        analisis.put("estadoMasEficiente", estadoMasEficiente);
        analisis.put("maxSolicitudes", maxSolicitudes);
        analisis.put("minSolicitudes", minSolicitudes);
        analisis.put("totalSolicitudes", totalSolicitudes);
        analisis.put("estadosActivos", resumenPorEstado.size());
        analisis.put("solicitudesPendientes", totalPorEstado.get(EstadosSolicitud.ENVIADA.name()) + totalEnProceso);
        analisis.put("solicitudesCompletadas", completadas);
        analisis.put("tasaResolucion", redondear2(porcentaje(completadas, totalSolicitudes)));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("estados", resumenPorEstado);
        resultado.put("analisis", analisis);
        resultado.put("totalSolicitudes", totalSolicitudes);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas por estado de solicitudes con analisis de distribucion - DATOS REALES");
        return resultado;
    }
}
