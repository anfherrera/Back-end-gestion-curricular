package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.PresentacionEstadisticas.PROCESOS_DASHBOARD;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.redondear2;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorProceso;

/**
 * Vista del dashboard por proceso (filtrable por periodo académico y programa): conteos, eficiencia, tiempos y
 * tendencia de cada proceso, y cuál es el más eficiente, el más rápido y el más demandado.
 */
public class DashboardPorProceso {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardPorProceso(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public VistaPorProceso detalladasPorProceso(String periodoAcademico, Integer idPrograma) {
        Date corte = ContadorSolicitudes.corteTendencia(periodoAcademico);
        Map<String, ContadorSolicitudes> porProceso = new HashMap<>();
        for (String proceso : PROCESOS_DASHBOARD) {
            porProceso.put(proceso, new ContadorSolicitudes());
        }
        for (Solicitud solicitud : solicitudes(datos, periodoAcademico, idPrograma)) {
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                ContadorSolicitudes c = porProceso.get(proceso);
                if (solicitud.getFecha_registro_solicitud() != null) {
                    c.fechas.add(solicitud.getFecha_registro_solicitud());
                }
                c.contar(solicitud, true);
            }
        }

        Map<String, VistaPorProceso.Detalle> detalle = new HashMap<>();
        String procesoMasEficiente = null;
        String procesoMasRapido = null;
        String procesoMasDemandado = null;
        double maxEficiencia = 0;
        double minTiempo = Double.MAX_VALUE;
        int maxDemanda = 0;

        for (String proceso : PROCESOS_DASHBOARD) {
            ContadorSolicitudes c = porProceso.get(proceso);
            double eficiencia = c.eficiencia();
            double tiempoPromedio = c.tiempoPromedio();

            detalle.put(proceso, new VistaPorProceso.Detalle(proceso, c.total, c.aprobadas, c.rechazadas,
                    c.enviadas, c.enProceso, redondear2(eficiencia), redondear2(tiempoPromedio),
                    redondear2(c.tiempos.stream().mapToDouble(Double::doubleValue).min().orElse(0)),
                    redondear2(c.tiempos.stream().mapToDouble(Double::doubleValue).max().orElse(0)),
                    c.tendenciaReciente(corte), PresentacionEstadisticas.descripcionProcesoDetallada(proceso),
                    PresentacionEstadisticas.colorProceso(proceso), PresentacionEstadisticas.SIN_ICONO));

            if (eficiencia > maxEficiencia) {
                maxEficiencia = eficiencia;
                procesoMasEficiente = proceso;
            }
            if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                minTiempo = tiempoPromedio;
                procesoMasRapido = proceso;
            }
            if (c.total > maxDemanda) {
                maxDemanda = c.total;
                procesoMasDemandado = proceso;
            }
        }

        VistaPorProceso.ResumenComparativo resumenComparativo = new VistaPorProceso.ResumenComparativo(
                procesoMasEficiente, procesoMasRapido, procesoMasDemandado, redondear2(maxEficiencia),
                redondear2(minTiempo), maxDemanda, PROCESOS_DASHBOARD.length);

        return new VistaPorProceso(detalle, resumenComparativo, new Date(),
                "Estadisticas detalladas por proceso con analisis de eficiencia y tiempo - DATOS REALES");
    }
}
