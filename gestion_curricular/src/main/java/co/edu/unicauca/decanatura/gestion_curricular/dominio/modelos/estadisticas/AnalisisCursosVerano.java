package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Estadísticas de los cursos de verano: demanda por materia, programa y mes, estados de las solicitudes y la
 * predicción de demanda con sus recomendaciones.
 *
 * @param todosLosMeses                   los doce meses, de enero a diciembre
 * @param fechaConsulta                   momento de la consulta
 * @param descripcion                     descripción de la consulta
 * @param resumen                         totales del periodo
 * @param estadosSolicitudes              solicitudes de cada estado principal (APROBADA, ENVIADA, EN_PROCESO, que
 *                                        junta las aprobadas por funcionario y por coordinador, y RECHAZADA)
 * @param estadosSolicitudesEstructurados los mismos estados con su porcentaje, por nombre del estado
 * @param topMaterias                     las diez materias más solicitadas, de mayor a menor
 * @param analisisPorPrograma             todos los programas, de mayor a menor demanda
 * @param tendenciasTemporales            demanda de cada uno de los doce meses
 * @param recomendaciones                 las de la predicción; vacía si la predicción es de respaldo
 * @param predicciones                    predicción de demanda del próximo periodo
 */
public record AnalisisCursosVerano(List<String> todosLosMeses, Date fechaConsulta, String descripcion, Resumen resumen,
        Map<String, Integer> estadosSolicitudes, Map<String, DetalleEstado> estadosSolicitudesEstructurados,
        List<DemandaCursoVerano> topMaterias, List<DemandaCursoVerano> analisisPorPrograma,
        List<DemandaMensualCursosVerano> tendenciasTemporales, List<RecomendacionCursosVerano> recomendaciones,
        PrediccionesCursosVerano predicciones) {

    /**
     * Totales del periodo. La tasa de aprobación cuenta solo las completamente aprobadas, con dos decimales.
     */
    public record Resumen(int totalSolicitudes, int materiasUnicas, int programasParticipantes, double tasaAprobacion) {
    }

    /**
     * Solicitudes de un estado y su porcentaje sobre el total, con dos decimales.
     */
    public record DetalleEstado(String estado, int cantidad, double porcentaje) {
    }
}
