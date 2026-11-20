package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.MESES;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.mesDeRegistro;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.porcentaje;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.redondear2;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorMes;

/**
 * Vista del dashboard por mes de registro (filtrable por periodo académico y programa): conteos, eficiencia y
 * tiempos de los doce meses, y la tendencia entre los dos últimos meses con datos.
 */
public class DashboardPorMes {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardPorMes(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public VistaPorMes porPeriodo(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudes(datos, periodoAcademico, idPrograma);

        Map<String, ContadorSolicitudes> porMes = new HashMap<>();
        for (String mes : MESES) {
            porMes.put(mes, new ContadorSolicitudes());
        }
        for (Solicitud solicitud : solicitudes) {
            String mes = mesDeRegistro(solicitud);
            if (mes != null) {
                ContadorSolicitudes c = porMes.get(mes);
                String proceso = tipoProceso(solicitud);
                if (proceso != null) {
                    c.procesos.merge(proceso, 1, Integer::sum);
                }
                c.contar(solicitud, false);
            }
        }

        Map<String, VistaPorMes.Detalle> resumenPorMes = new HashMap<>();
        List<VistaPorMes.Detalle> mesesOrdenados = new ArrayList<>();
        List<String> mesesConDatos = new ArrayList<>();
        String mesMasActivo = null;
        String mesMasEficiente = null;
        String mesMasRapido = null;
        int maxSolicitudes = 0;
        double maxEficiencia = 0;
        double minTiempo = Double.MAX_VALUE;
        int totalSolicitudes = 0;

        // Todos los meses van en el resultado, aunque tengan 0 solicitudes, para que el frontend muestre la tendencia
        for (String mes : MESES) {
            ContadorSolicitudes c = porMes.get(mes);
            totalSolicitudes += c.total;
            double eficiencia = c.eficiencia();
            double tiempoPromedio = c.tiempoPromedio();

            VistaPorMes.Detalle detalleMes = new VistaPorMes.Detalle(mes, c.total, c.aprobadas, c.rechazadas,
                    c.enviadas, redondear2(eficiencia), redondear2(tiempoPromedio),
                    redondear2(porcentaje(c.total, solicitudes.size())), c.procesos,
                    PresentacionEstadisticas.colorMes(mes), PresentacionEstadisticas.SIN_ICONO,
                    PresentacionEstadisticas.descripcionMes(mes));
            resumenPorMes.put(mes, detalleMes);
            mesesOrdenados.add(detalleMes);

            if (c.total > 0) {
                mesesConDatos.add(mes);
                if (c.total > maxSolicitudes) {
                    maxSolicitudes = c.total;
                    mesMasActivo = mes;
                }
                if (eficiencia > maxEficiencia) {
                    maxEficiencia = eficiencia;
                    mesMasEficiente = mes;
                }
                if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                    minTiempo = tiempoPromedio;
                    mesMasRapido = mes;
                }
            }
        }

        // Tendencia: los dos últimos meses con datos
        String tendenciaGeneral = "Estable";
        double crecimientoPromedio = 0;
        if (mesesConDatos.size() >= 2) {
            int anterior = porMes.get(mesesConDatos.get(mesesConDatos.size() - 2)).total;
            int actual = porMes.get(mesesConDatos.get(mesesConDatos.size() - 1)).total;
            if (actual != anterior) {
                tendenciaGeneral = actual > anterior ? "Creciente" : "Decreciente";
                crecimientoPromedio = Math.abs(actual - anterior) * 100.0 / anterior;
            }
        }

        VistaPorMes.Tendencias tendencias = new VistaPorMes.Tendencias(mesMasActivo, mesMasEficiente, mesMasRapido,
                maxSolicitudes, redondear2(maxEficiencia), redondear2(minTiempo), tendenciaGeneral,
                redondear2(crecimientoPromedio), mesesConDatos.size(), totalSolicitudes,
                mesesConDatos.size() > 0 ? Math.round((totalSolicitudes * 100.0) / mesesConDatos.size()) / 100.0 : 0);

        return new VistaPorMes(resumenPorMes, tendencias, Arrays.asList(MESES), mesesOrdenados, new Date(),
                "Estadisticas por periodo/mes con analisis de tendencias - DATOS REALES");
    }
}
