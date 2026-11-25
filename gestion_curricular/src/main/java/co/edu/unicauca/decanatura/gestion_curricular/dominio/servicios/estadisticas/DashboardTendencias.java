package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.PresentacionEstadisticas.PROCESOS_DASHBOARD;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.MESES;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.mesDeRegistro;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.redondear2;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.solicitudes;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaTendencias;

/**
 * Vista del dashboard de tendencias y comparativas (filtrable por periodo académico y programa): crecimiento entre
 * los dos últimos meses con datos, comparativas por proceso y por programa, y la recomendación estratégica.
 */
public class DashboardTendencias {

    private final GestionarEstadisticasGatewayIntPort datos;

    public DashboardTendencias(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public VistaTendencias tendenciasYComparativas(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudes(datos, periodoAcademico, idPrograma);
        List<Usuario> estudiantes = datos.listarEstudiantes();

        // Solicitudes por mes y estudiantes distintos que radicaron solicitudes en cada mes
        Map<String, Integer> solicitudesPorMes = new HashMap<>();
        Map<String, Integer> estudiantesPorMes = new HashMap<>();
        for (String mes : MESES) {
            solicitudesPorMes.put(mes, 0);
            estudiantesPorMes.put(mes, 0);
        }
        Map<String, Set<Integer>> estudiantesActivosPorMes = new HashMap<>();
        for (Solicitud solicitud : solicitudes) {
            String mes = mesDeRegistro(solicitud);
            if (mes != null) {
                solicitudesPorMes.merge(mes, 1, Integer::sum);
                if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getIdUsuario() != null) {
                    estudiantesActivosPorMes.computeIfAbsent(mes, k -> new HashSet<>())
                            .add(solicitud.getObjUsuario().getIdUsuario());
                }
            }
        }
        estudiantesActivosPorMes.forEach((mes, ids) -> estudiantesPorMes.put(mes, ids.size()));

        List<String> mesesConDatos = new ArrayList<>();
        for (String mes : MESES) {
            if (solicitudesPorMes.get(mes) > 0) {
                mesesConDatos.add(mes);
            }
        }

        String tendenciaSolicitudes = "Estable";
        String tendenciaEstudiantes = "Estable";
        double crecimientoSolicitudes = 0.0;
        double crecimientoEstudiantes = 0.0;
        if (mesesConDatos.size() >= 2) {
            // Los dos últimos meses con datos
            String mesAnterior = mesesConDatos.get(mesesConDatos.size() - 2);
            String mesActual = mesesConDatos.get(mesesConDatos.size() - 1);
            int solicitudesAnterior = solicitudesPorMes.get(mesAnterior);
            int solicitudesActual = solicitudesPorMes.get(mesActual);
            int estudiantesAnterior = estudiantesPorMes.get(mesAnterior);
            int estudiantesActual = estudiantesPorMes.get(mesActual);

            tendenciaSolicitudes = tendencia(solicitudesAnterior, solicitudesActual);
            crecimientoSolicitudes = variacion(solicitudesAnterior, solicitudesActual);
            tendenciaEstudiantes = tendencia(estudiantesAnterior, estudiantesActual);
            crecimientoEstudiantes = variacion(estudiantesAnterior, estudiantesActual);
        } else if (mesesConDatos.size() == 1) {
            // Un solo mes con datos: no hay con qué comparar
            tendenciaSolicitudes = "Nueva";
            tendenciaEstudiantes = "Nueva";
        }

        VistaTendencias.CrecimientoTemporal crecimientoTemporal = new VistaTendencias.CrecimientoTemporal(
                tendenciaSolicitudes, redondear2(crecimientoSolicitudes), tendenciaEstudiantes,
                redondear2(crecimientoEstudiantes), mesesConDatos.size(), solicitudes.size(), estudiantes.size(),
                solicitudesPorMes, estudiantesPorMes);

        // Comparativa por proceso
        Map<String, Integer> solicitudesPorProceso = new HashMap<>();
        Map<String, Integer> aprobadasPorProceso = new HashMap<>();
        for (String proceso : PROCESOS_DASHBOARD) {
            solicitudesPorProceso.put(proceso, 0);
            aprobadasPorProceso.put(proceso, 0);
        }
        for (Solicitud solicitud : solicitudes) {
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                solicitudesPorProceso.merge(proceso, 1, Integer::sum);
                if (EstadosSolicitud.APROBADA.name().equals(estadoActual(solicitud))) {
                    aprobadasPorProceso.merge(proceso, 1, Integer::sum);
                }
            }
        }

        String procesoMasDemandado = null;
        String procesoMasEficiente = null;
        int maxDemanda = 0;
        double maxEficiencia = 0;
        for (Map.Entry<String, Integer> entry : solicitudesPorProceso.entrySet()) {
            int total = entry.getValue();
            if (total > maxDemanda) {
                maxDemanda = total;
                procesoMasDemandado = entry.getKey();
            }
            double eficiencia = total > 0 ? (aprobadasPorProceso.get(entry.getKey()) * 100.0) / total : 0;
            if (eficiencia > maxEficiencia) {
                maxEficiencia = eficiencia;
                procesoMasEficiente = entry.getKey();
            }
        }

        VistaTendencias.ComparativaProcesos comparativaProcesos = new VistaTendencias.ComparativaProcesos(
                procesoMasDemandado, procesoMasEficiente, redondear2(maxEficiencia), maxDemanda,
                solicitudesPorProceso, aprobadasPorProceso);

        // Comparativa por programa
        List<Programa> programas = datos.listarProgramas();
        Map<String, Integer> solicitudesPorPrograma = new HashMap<>();
        Map<String, Integer> estudiantesPorPrograma = new HashMap<>();
        for (Programa programa : programas) {
            solicitudesPorPrograma.put(programa.getNombrePrograma(), 0);
            estudiantesPorPrograma.put(programa.getNombrePrograma(), 0);
        }
        for (Solicitud solicitud : solicitudes) {
            String programa = nombrePrograma(solicitud);
            if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                solicitudesPorPrograma.merge(programa, 1, Integer::sum);
            }
        }
        for (Usuario estudiante : estudiantes) {
            if (estudiante.getObjPrograma() != null) {
                estudiantesPorPrograma.merge(estudiante.getObjPrograma().getNombrePrograma(), 1, Integer::sum);
            }
        }

        String programaMasActivo = null;
        String programaConMasEstudiantes = null;
        int maxSolicitudes = 0;
        int maxEstudiantes = 0;
        for (Map.Entry<String, Integer> entry : solicitudesPorPrograma.entrySet()) {
            int numSolicitudes = entry.getValue();
            int numEstudiantes = estudiantesPorPrograma.getOrDefault(entry.getKey(), 0);
            if (numSolicitudes > maxSolicitudes) {
                maxSolicitudes = numSolicitudes;
                programaMasActivo = entry.getKey();
            }
            if (numEstudiantes > maxEstudiantes) {
                maxEstudiantes = numEstudiantes;
                programaConMasEstudiantes = entry.getKey();
            }
        }

        VistaTendencias.ComparativaProgramas comparativaProgramas = new VistaTendencias.ComparativaProgramas(
                programaMasActivo, programaConMasEstudiantes, maxSolicitudes, maxEstudiantes, solicitudesPorPrograma,
                estudiantesPorPrograma);

        VistaTendencias.ResumenEstrategico resumenEstrategico = new VistaTendencias.ResumenEstrategico(
                solicitudes.size(), estudiantes.size(), programas.size(), "Ultimos 12 meses",
                recomendacionEstrategica(tendenciaSolicitudes, crecimientoSolicitudes, procesoMasDemandado,
                        programaMasActivo, maxEficiencia, maxSolicitudes));

        return new VistaTendencias(crecimientoTemporal, comparativaProcesos, comparativaProgramas,
                resumenEstrategico, new Date(), "Analisis de tendencias y comparativas estrategicas - DATOS REALES");
    }

    private static String tendencia(int anterior, int actual) {
        return actual > anterior ? "Creciente" : actual < anterior ? "Decreciente" : "Estable";
    }

    /** Variación porcentual (siempre positiva) respecto al valor anterior; 0 si el anterior es 0. */
    private static double variacion(int anterior, int actual) {
        return anterior > 0 ? Math.abs(actual - anterior) * 100.0 / anterior : 0.0;
    }

    private static String recomendacionEstrategica(String tendenciaSolicitudes, double crecimientoSolicitudes,
            String procesoMasDemandado, String programaMasActivo, double maxEficiencia, int maxSolicitudes) {
        StringBuilder recomendacion = new StringBuilder();

        if ("Creciente".equals(tendenciaSolicitudes)) {
            if (crecimientoSolicitudes > 50) {
                recomendacion.append(" CRECIMIENTO ALTO: Urgente aumentar recursos para ");
            } else if (crecimientoSolicitudes > 20) {
                recomendacion.append(" CRECIMIENTO MODERADO: Considerar aumentar recursos para ");
            } else {
                recomendacion.append(" CRECIMIENTO BAJO: Monitorear recursos para ");
            }
            recomendacion.append(procesoMasDemandado).append(". ");
        } else if ("Decreciente".equals(tendenciaSolicitudes)) {
            if (crecimientoSolicitudes > 50) {
                recomendacion.append(" DECRECIMIENTO ALTO: Evaluar estrategias de promocion para ");
            } else {
                recomendacion.append(" DECRECIMIENTO MODERADO: Revisar estrategias para ");
            }
            recomendacion.append(procesoMasDemandado).append(". ");
        } else {
            recomendacion.append(" TENDENCIA ESTABLE: Mantener recursos actuales. ");
        }

        if (maxEficiencia < 30) {
            recomendacion.append(" BAJA EFICIENCIA: Revisar procesos de aprobacion. ");
        } else if (maxEficiencia < 60) {
            recomendacion.append(" EFICIENCIA MODERADA: Optimizar procesos de aprobacion. ");
        }

        if (programaMasActivo != null) {
            recomendacion.append(" ENFOQUE: ").append(programaMasActivo).append(" requiere atencion prioritaria");
            if (maxSolicitudes > 15) {
                recomendacion.append(" (alta demanda)");
            } else if (maxSolicitudes > 10) {
                recomendacion.append(" (demanda moderada)");
            }
            recomendacion.append(".");
        }
        return recomendacion.toString();
    }
}
