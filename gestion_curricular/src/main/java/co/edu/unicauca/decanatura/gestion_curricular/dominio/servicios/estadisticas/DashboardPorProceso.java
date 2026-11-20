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

/**
 * Vista del dashboard por proceso (filtrable por periodo académico y programa): conteos, eficiencia, tiempos y
 * tendencia de cada proceso, y cuál es el más eficiente, el más rápido y el más demandado.
 */
public class DashboardPorProceso {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardPorProceso(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public Map<String, Object> detalladasPorProceso(String periodoAcademico, Integer idPrograma) {
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

        Map<String, Object> detalle = new HashMap<>();
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

            Map<String, Object> detalleProceso = new HashMap<>();
            detalleProceso.put("proceso", proceso);
            detalleProceso.put("totalSolicitudes", c.total);
            detalleProceso.put("aprobadas", c.aprobadas);
            detalleProceso.put("rechazadas", c.rechazadas);
            detalleProceso.put("enviadas", c.enviadas);
            detalleProceso.put("enProceso", c.enProceso);
            detalleProceso.put("eficiencia", redondear2(eficiencia));
            detalleProceso.put("tiempoPromedio", redondear2(tiempoPromedio));
            detalleProceso.put("tiempoMinimo", redondear2(c.tiempos.stream().mapToDouble(Double::doubleValue).min().orElse(0)));
            detalleProceso.put("tiempoMaximo", redondear2(c.tiempos.stream().mapToDouble(Double::doubleValue).max().orElse(0)));
            detalleProceso.put("tendencia", c.tendenciaReciente(corte));
            detalleProceso.put("descripcion", PresentacionEstadisticas.descripcionProcesoDetallada(proceso));
            detalleProceso.put("color", PresentacionEstadisticas.colorProceso(proceso));
            detalleProceso.put("icono", PresentacionEstadisticas.SIN_ICONO);
            detalle.put(proceso, detalleProceso);

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

        Map<String, Object> resumenComparativo = new HashMap<>();
        resumenComparativo.put("procesoMasEficiente", procesoMasEficiente);
        resumenComparativo.put("procesoMasRapido", procesoMasRapido);
        resumenComparativo.put("procesoMasDemandado", procesoMasDemandado);
        resumenComparativo.put("eficienciaMaxima", redondear2(maxEficiencia));
        resumenComparativo.put("tiempoMinimo", redondear2(minTiempo));
        resumenComparativo.put("demandaMaxima", maxDemanda);
        resumenComparativo.put("totalProcesos", PROCESOS_DASHBOARD.length);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("estadisticasPorProceso", detalle);
        resultado.put("resumenComparativo", resumenComparativo);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas detalladas por proceso con analisis de eficiencia y tiempo - DATOS REALES");
        return resultado;
    }
}
