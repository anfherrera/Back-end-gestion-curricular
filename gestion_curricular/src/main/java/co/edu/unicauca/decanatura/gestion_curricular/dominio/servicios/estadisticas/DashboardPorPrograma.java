package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/**
 * Vista del dashboard por programa académico (filtrable por periodo académico y programa): solicitudes y
 * estudiantes de cada programa, su eficiencia, tiempos y tendencia, y el análisis de rendimiento.
 */
public class DashboardPorPrograma {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardPorPrograma(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public Map<String, Object> porPrograma(String periodoAcademico, Integer idPrograma) {
        Date corte = ContadorSolicitudes.corteTendencia(periodoAcademico);
        List<Programa> programas = datos.listarProgramas();
        List<Solicitud> solicitudes = solicitudes(datos, periodoAcademico, idPrograma);
        List<Usuario> estudiantes = datos.listarEstudiantes();

        Map<String, Integer> estudiantesPorPrograma = new HashMap<>();
        Map<String, ContadorSolicitudes> porPrograma = new HashMap<>();
        for (Programa programa : programas) {
            estudiantesPorPrograma.put(programa.getNombre_programa(), 0);
            porPrograma.put(programa.getNombre_programa(), new ContadorSolicitudes());
        }
        for (Usuario estudiante : estudiantes) {
            if (estudiante.getObjPrograma() != null) {
                estudiantesPorPrograma.merge(estudiante.getObjPrograma().getNombre_programa(), 1, Integer::sum);
            }
        }
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                ContadorSolicitudes c = porPrograma.get(nombrePrograma(solicitud));
                String proceso = tipoProceso(solicitud);
                if (proceso != null) {
                    c.procesos.merge(proceso, 1, Integer::sum);
                }
                if (solicitud.getFecha_registro_solicitud() != null) {
                    c.fechas.add(solicitud.getFecha_registro_solicitud());
                }
                c.contar(solicitud, false);
            }
        }

        Map<String, Integer> solicitudesPorPrograma = new HashMap<>();
        porPrograma.forEach((nombre, c) -> solicitudesPorPrograma.put(nombre, c.total));

        Map<String, Object> resumenPorPrograma = new HashMap<>();
        String programaMasActivo = null;
        String programaConMasEstudiantes = null;
        String programaMasEficiente = null;
        String programaMasRapido = null;
        int maxSolicitudes = 0;
        int maxEstudiantes = 0;
        double maxEficiencia = 0;
        double minTiempo = Double.MAX_VALUE;

        for (Programa programa : programas) {
            String nombre = programa.getNombre_programa();
            ContadorSolicitudes c = porPrograma.get(nombre);
            int numEstudiantes = estudiantesPorPrograma.getOrDefault(nombre, 0);
            double eficiencia = c.eficiencia();
            double tiempoPromedio = c.tiempoPromedio();

            Map<String, Object> detallePrograma = new HashMap<>();
            detallePrograma.put("nombre", nombre);
            detallePrograma.put("codigo", programa.getCodigo());
            detallePrograma.put("totalSolicitudes", c.total);
            detallePrograma.put("estudiantes", numEstudiantes);
            detallePrograma.put("aprobadas", c.aprobadas);
            detallePrograma.put("rechazadas", c.rechazadas);
            detallePrograma.put("enviadas", c.enviadas);
            detallePrograma.put("eficiencia", redondear2(eficiencia));
            detallePrograma.put("tiempoPromedio", redondear2(tiempoPromedio));
            detallePrograma.put("tendencia", c.tendenciaReciente(corte));
            detallePrograma.put("porcentaje", redondear2(porcentaje(c.total, solicitudes.size())));
            detallePrograma.put("procesos", c.procesos);
            detallePrograma.put("color", PresentacionEstadisticas.colorPrograma(nombre));
            detallePrograma.put("icono", PresentacionEstadisticas.SIN_ICONO);
            detallePrograma.put("descripcion", PresentacionEstadisticas.descripcionPrograma(nombre));
            resumenPorPrograma.put(nombre, detallePrograma);

            if (c.total > maxSolicitudes) {
                maxSolicitudes = c.total;
                programaMasActivo = nombre;
            }
            if (numEstudiantes > maxEstudiantes) {
                maxEstudiantes = numEstudiantes;
                programaConMasEstudiantes = nombre;
            }
            if (eficiencia > maxEficiencia) {
                maxEficiencia = eficiencia;
                programaMasEficiente = nombre;
            }
            if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                minTiempo = tiempoPromedio;
                programaMasRapido = nombre;
            }
        }

        Map<String, Object> analisis = new HashMap<>();
        analisis.put("programaMasActivo", programaMasActivo);
        analisis.put("programaConMasEstudiantes", programaConMasEstudiantes);
        analisis.put("programaMasEficiente", programaMasEficiente);
        analisis.put("programaMasRapido", programaMasRapido);
        analisis.put("maxSolicitudes", maxSolicitudes);
        analisis.put("maxEstudiantes", maxEstudiantes);
        analisis.put("eficienciaMaxima", redondear2(maxEficiencia));
        analisis.put("tiempoMinimo", redondear2(minTiempo));
        analisis.put("totalProgramas", programas.size());
        analisis.put("totalSolicitudes", solicitudes.size());
        analisis.put("totalEstudiantes", estudiantes.size());

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("porPrograma", resumenPorPrograma);
        resultado.put("estudiantesPorPrograma", estudiantesPorPrograma);
        resultado.put("solicitudesPorPrograma", solicitudesPorPrograma);
        resultado.put("analisis", analisis);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas por programa academico con analisis de rendimiento - DATOS REALES");
        return resultado;
    }
}
