package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.MESES;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.mesDeRegistro;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.redondear2;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatoDemandaCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.PrediccionDemandaCursosVerano;

/** Demanda de cursos de verano por materia, programa y mes, con predicciones y recomendaciones. */
public class EstadisticasCursosVerano {

    private final GestionarEstadisticasGatewayIntPort datos;
    private final PrediccionDemandaCursosVerano prediccionDemanda = new PrediccionDemandaCursosVerano();

    public EstadisticasCursosVerano(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    /**
     * Solicitudes de cursos de verano del periodo (el de la solicitud o el de su curso) y del programa.
     * A diferencia de las demás estadísticas, aquí el periodo no se deduce de la fecha de registro.
     */
    private List<Solicitud> solicitudesCursosVerano(String periodoAcademico, Integer idPrograma) {
        String periodo = periodoAcademico != null && !periodoAcademico.trim().isEmpty() ? periodoAcademico.trim() : null;
        List<Solicitud> todas = datos.listarSolicitudesConEstadosYUsuario();
        return (todas != null ? todas : new ArrayList<Solicitud>()).stream()
                .filter(s -> TipoProcesoSolicitud.CURSOS_VERANO.equals(tipoProceso(s)))
                .filter(s -> periodo == null || periodo.equals(s.getPeriodo_academico())
                        || (s.getObjCursoOfertadoVerano() != null
                                && periodo.equals(s.getObjCursoOfertadoVerano().getPeriodo_academico())))
                .filter(s -> idPrograma == null || (s.getObjUsuario() != null && s.getObjUsuario().getObjPrograma() != null
                        && s.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma)))
                .collect(Collectors.toList());
    }

    private static Map<String, Integer> demandaPorMes(List<Solicitud> solicitudes) {
        Map<String, Integer> demanda = new HashMap<>();
        for (String mes : MESES) {
            demanda.put(mes, 0);
        }
        for (Solicitud solicitud : solicitudes) {
            String mes = mesDeRegistro(solicitud);
            if (mes != null) {
                demanda.merge(mes, 1, Integer::sum);
            }
        }
        return demanda;
    }

    /** Los 12 meses, aunque tengan 0 solicitudes, para que el frontend muestre la tendencia completa. */
    private static List<Map<String, Object>> tendenciasTemporales(Map<String, Integer> demandaPorMes, int total) {
        List<Map<String, Object>> tendencias = new ArrayList<>();
        for (String mes : MESES) {
            int solicitudes = demandaPorMes.get(mes);
            Map<String, Object> tendencia = new HashMap<>();
            tendencia.put("mes", mes);
            tendencia.put("solicitudes", solicitudes);
            tendencia.put("porcentaje", total > 0 ? redondear2((solicitudes * 100.0) / total) : 0.0);
            tendencias.add(tendencia);
        }
        return tendencias;
    }

    /** Nombre, solicitudes y porcentaje del total, de mayor a menor demanda. */
    private static List<Map<String, Object>> ranking(Map<String, Integer> demanda, int total, long limite) {
        return demanda.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limite)
                .map(entry -> {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("nombre", entry.getKey());
                    fila.put("solicitudes", entry.getValue());
                    fila.put("porcentaje", redondear2((entry.getValue() * 100.0) / total));
                    return fila;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> estadisticas(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudesCursosVerano(periodoAcademico, idPrograma);
        int total = solicitudes.size();

        Map<String, Integer> demandaPorMateria = new HashMap<>();
        Map<String, Integer> demandaPorPrograma = new HashMap<>();
        Map<String, Integer> porEstado = new HashMap<>();
        for (Solicitud solicitud : solicitudes) {
            // La materia sale del curso solicitado, no del nombre de la solicitud
            if (solicitud.getObjCursoOfertadoVerano() != null && solicitud.getObjCursoOfertadoVerano().getObjMateria() != null) {
                demandaPorMateria.merge(solicitud.getObjCursoOfertadoVerano().getObjMateria().getNombre(), 1, Integer::sum);
            }
            if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                demandaPorPrograma.merge(nombrePrograma(solicitud), 1, Integer::sum);
            }
            porEstado.merge(estadoActual(solicitud), 1, Integer::sum);
        }
        Map<String, Integer> demandaPorMes = demandaPorMes(solicitudes);

        List<Map<String, Object>> topMaterias = ranking(demandaPorMateria, total, 10);
        List<Map<String, Object>> analisisPorPrograma = ranking(demandaPorPrograma, total, Long.MAX_VALUE);

        // Tasa de aprobación: solo las completamente aprobadas, no las en proceso
        int aprobadas = porEstado.getOrDefault(EstadosSolicitud.APROBADA.name(), 0);
        double tasaAprobacion = total > 0 ? (aprobadas * 100.0) / total : 0.0;

        Map<String, Object> predicciones = prediccionDemanda.generar(
                solicitudes.stream().map(EstadisticasCursosVerano::datoDemanda).toList(),
                demandaPorMes, topMaterias, analisisPorPrograma);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recomendaciones =
                (List<Map<String, Object>>) predicciones.getOrDefault("recomendaciones", new ArrayList<>());

        // Estados con la misma forma que /estado-solicitudes; "EN_PROCESO" junta funcionario y coordinador
        int enProceso = porEstado.getOrDefault(EstadosSolicitud.APROBADA_FUNCIONARIO.name(), 0) + porEstado.getOrDefault(EstadosSolicitud.APROBADA_COORDINADOR.name(), 0);
        Map<String, Integer> estadosSolicitudes = new HashMap<>();
        estadosSolicitudes.put(EstadosSolicitud.APROBADA.name(), aprobadas);
        estadosSolicitudes.put(EstadosSolicitud.ENVIADA.name(), porEstado.getOrDefault(EstadosSolicitud.ENVIADA.name(), 0));
        estadosSolicitudes.put("EN_PROCESO", enProceso);
        estadosSolicitudes.put(EstadosSolicitud.RECHAZADA.name(), porEstado.getOrDefault(EstadosSolicitud.RECHAZADA.name(), 0));

        Map<String, Object> estadosEstructurados = new HashMap<>();
        estadosSolicitudes.forEach((estado, cantidad) -> {
            Map<String, Object> detalle = new HashMap<>();
            detalle.put("estado", estado);
            detalle.put("cantidad", cantidad);
            detalle.put("porcentaje", redondear2(total > 0 ? (cantidad * 100.0) / total : 0.0));
            estadosEstructurados.put(estado, detalle);
        });

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("todosLosMeses", Arrays.asList(MESES));
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas detalladas de cursos de verano - Analisis de demanda y recomendaciones");
        resultado.put("resumen", Map.of(
                "totalSolicitudes", total,
                "materiasUnicas", demandaPorMateria.size(),
                "programasParticipantes", demandaPorPrograma.size(),
                "tasaAprobacion", redondear2(tasaAprobacion)));
        resultado.put("estadosSolicitudes", estadosSolicitudes);
        resultado.put("estadosSolicitudesEstructurados", estadosEstructurados);
        resultado.put("topMaterias", topMaterias);
        resultado.put("analisisPorPrograma", analisisPorPrograma);
        resultado.put("tendenciasTemporales", tendenciasTemporales(demandaPorMes, total));
        resultado.put("recomendaciones", recomendaciones);
        resultado.put("predicciones", predicciones);
        return resultado;
    }

    public Map<String, Object> tendenciasTemporales(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudesCursosVerano(periodoAcademico, idPrograma);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("todosLosMeses", Arrays.asList(MESES));
        resultado.put("tendenciasTemporales", tendenciasTemporales(demandaPorMes(solicitudes), solicitudes.size()));
        resultado.put("totalSolicitudes", solicitudes.size());
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Tendencias temporales optimizadas para cursos de verano");
        return resultado;
    }

    /** Datos de la solicitud que usa la predicción de demanda. */
    private static DatoDemandaCursoVerano datoDemanda(Solicitud solicitud) {
        String materia = solicitud.getObjCursoOfertadoVerano() != null
                && solicitud.getObjCursoOfertadoVerano().getObjMateria() != null
                ? solicitud.getObjCursoOfertadoVerano().getObjMateria().getNombre()
                : null;
        return new DatoDemandaCursoVerano(materia, nombrePrograma(solicitud), solicitud.getFecha_registro_solicitud());
    }
}
