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

/**
 * Vista del dashboard por mes de registro (filtrable por periodo académico y programa): conteos, eficiencia y
 * tiempos de los doce meses, y la tendencia entre los dos últimos meses con datos.
 */
public class DashboardPorMes {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardPorMes(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public Map<String, Object> porPeriodo(String periodoAcademico, Integer idPrograma) {
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

        Map<String, Object> resumenPorMes = new HashMap<>();
        List<Map<String, Object>> mesesOrdenados = new ArrayList<>();
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

            Map<String, Object> detalleMes = new HashMap<>();
            detalleMes.put("mes", mes);
            detalleMes.put("total", c.total);
            detalleMes.put("aprobadas", c.aprobadas);
            detalleMes.put("rechazadas", c.rechazadas);
            detalleMes.put("enviadas", c.enviadas);
            detalleMes.put("eficiencia", redondear2(eficiencia));
            detalleMes.put("tiempoPromedio", redondear2(tiempoPromedio));
            detalleMes.put("porcentaje", redondear2(porcentaje(c.total, solicitudes.size())));
            detalleMes.put("procesos", c.procesos);
            detalleMes.put("color", PresentacionEstadisticas.colorMes(mes));
            detalleMes.put("icono", PresentacionEstadisticas.SIN_ICONO);
            detalleMes.put("descripcion", PresentacionEstadisticas.descripcionMes(mes));
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

        Map<String, Object> tendencias = new HashMap<>();
        tendencias.put("mesMasActivo", mesMasActivo);
        tendencias.put("mesMasEficiente", mesMasEficiente);
        tendencias.put("mesMasRapido", mesMasRapido);
        tendencias.put("maxSolicitudes", maxSolicitudes);
        tendencias.put("eficienciaMaxima", redondear2(maxEficiencia));
        tendencias.put("tiempoMinimo", redondear2(minTiempo));
        tendencias.put("tendenciaGeneral", tendenciaGeneral);
        tendencias.put("crecimientoPromedio", redondear2(crecimientoPromedio));
        tendencias.put("mesesAnalizados", mesesConDatos.size());
        tendencias.put("totalSolicitudes", totalSolicitudes);
        tendencias.put("promedioMensual", mesesConDatos.size() > 0
                ? Math.round((totalSolicitudes * 100.0) / mesesConDatos.size()) / 100.0
                : 0);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("porMes", resumenPorMes);
        resultado.put("tendencias", tendencias);
        resultado.put("todosLosMeses", Arrays.asList(MESES));
        resultado.put("mesesOrdenados", mesesOrdenados);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas por periodo/mes con analisis de tendencias - DATOS REALES");
        return resultado;
    }
}
