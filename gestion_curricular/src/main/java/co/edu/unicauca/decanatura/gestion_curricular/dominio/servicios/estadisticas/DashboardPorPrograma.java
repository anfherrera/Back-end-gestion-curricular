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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorPrograma;

/**
 * Vista del dashboard por programa académico (filtrable por periodo académico y programa): solicitudes y
 * estudiantes de cada programa, su eficiencia, tiempos y tendencia, y el análisis de rendimiento.
 */
public class DashboardPorPrograma {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardPorPrograma(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public VistaPorPrograma porPrograma(String periodoAcademico, Integer idPrograma) {
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

        Map<String, VistaPorPrograma.Detalle> resumenPorPrograma = new HashMap<>();
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

            resumenPorPrograma.put(nombre, new VistaPorPrograma.Detalle(nombre, programa.getCodigo(), c.total,
                    numEstudiantes, c.aprobadas, c.rechazadas, c.enviadas, redondear2(eficiencia),
                    redondear2(tiempoPromedio), c.tendenciaReciente(corte),
                    redondear2(porcentaje(c.total, solicitudes.size())), c.procesos,
                    PresentacionEstadisticas.colorPrograma(nombre), PresentacionEstadisticas.SIN_ICONO,
                    PresentacionEstadisticas.descripcionPrograma(nombre)));

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

        VistaPorPrograma.Analisis analisis = new VistaPorPrograma.Analisis(programaMasActivo,
                programaConMasEstudiantes, programaMasEficiente, programaMasRapido, maxSolicitudes, maxEstudiantes,
                redondear2(maxEficiencia), redondear2(minTiempo), programas.size(), solicitudes.size(),
                estudiantes.size());

        return new VistaPorPrograma(resumenPorPrograma, estudiantesPorPrograma, solicitudesPorPrograma, analisis,
                new Date(), "Estadisticas por programa academico con analisis de rendimiento - DATOS REALES");
    }
}
