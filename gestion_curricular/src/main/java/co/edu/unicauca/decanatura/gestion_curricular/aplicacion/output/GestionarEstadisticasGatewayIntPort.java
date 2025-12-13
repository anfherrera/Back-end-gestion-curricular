package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Date;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;

public interface GestionarEstadisticasGatewayIntPort {
    Estadistica crearEstadistica(Estadistica estadistica);
    Estadistica actualizarEstadistica(Estadistica estadistica);
    Boolean eliminarEstadistica(Integer idEstadistica);
    Estadistica obtenerEstadisticaPorId(Integer idEstadistica);
    Estadistica obtenerEstadisticasSolicitudPeriodoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, Integer idPrograma);
    Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, String estado, Integer idPrograma);
    List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma( Date fechaInicio, Date fechaFin, Integer idPrograma);

    /**
     * Obtiene estadísticas globales del sistema combinando todos los procesos.
     * Utiliza SolicitudRepositoryInt para obtener conteos totales.
     * 
     * @return Map con estadísticas globales del sistema
     */
    Map<String, Object> obtenerEstadisticasGlobales();

    /**
     * Obtiene estadísticas globales del sistema combinando todos los procesos con filtros.
     * Utiliza SolicitudRepositoryInt para obtener conteos totales.
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return Map con estadísticas globales del sistema
     */
    Map<String, Object> obtenerEstadisticasGlobales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin);

    /**
     * Obtiene estadísticas específicas por tipo de proceso.
     * Utiliza SolicitudRepositoryInt para filtrar por tipo de solicitud.
     * 
     * @param tipoProceso Tipo de proceso (REINGRESO, HOMOLOGACION, ECAES, CURSO_VERANO, PAZ_SALVO)
     * @return Map con estadísticas del proceso específico
     */
    Map<String, Object> obtenerEstadisticasPorProceso(String tipoProceso);

    /**
     * Obtiene estadísticas por estado de solicitud en todos los procesos.
     * Utiliza SolicitudRepositoryInt para contar por estado.
     * 
     * @param estado Estado de la solicitud (EN_PROCESO, APROBADA, RECHAZADA, etc.)
     * @return Map con estadísticas por estado
     */
    Map<String, Object> obtenerEstadisticasPorEstado(String estado);

    /**
     * Obtiene estadísticas por programa académico.
     * Utiliza SolicitudRepositoryInt y ProgramaRepositoryInt para filtrar por programa.
     * 
     * @param idPrograma ID del programa académico
     * @return Map con estadísticas del programa
     */
    Map<String, Object> obtenerEstadisticasPorPrograma(Integer idPrograma);

    /**
     * Obtiene estadísticas por período académico.
     * Utiliza SolicitudRepositoryInt para filtrar por rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Map con estadísticas del período
     */
    Map<String, Object> obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin);

    /**
     * Obtiene estadísticas combinadas por programa y período académico.
     * Utiliza SolicitudRepositoryInt para filtrar por programa y fechas.
     * 
     * @param idPrograma ID del programa académico
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Map con estadísticas combinadas
     */
    Map<String, Object> obtenerEstadisticasPorProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin);

    /**
     * Obtiene estadísticas detalladas por proceso y estado.
     * Utiliza SolicitudRepositoryInt para filtrar por tipo de solicitud y estado.
     * 
     * @param tipoProceso Tipo de proceso
     * @param estado Estado de la solicitud
     * @return Map con estadísticas detalladas
     */
    Map<String, Object> obtenerEstadisticasPorProcesoYEstado(String tipoProceso, String estado);

    /**
     * Obtiene un resumen completo de todas las estadísticas del sistema.
     * Utiliza múltiples repositorios para generar un dashboard completo.
     * 
     * @return Map con resumen completo de estadísticas
     */
    Map<String, Object> obtenerResumenCompleto();

    /**
     * Obtiene estadísticas de tendencias por período (comparación entre períodos).
     * Utiliza SolicitudRepositoryInt para analizar tendencias temporales.
     * 
     * @param fechaInicio1 Fecha de inicio del primer período
     * @param fechaFin1 Fecha de fin del primer período
     * @param fechaInicio2 Fecha de inicio del segundo período
     * @param fechaFin2 Fecha de fin del segundo período
     * @return Map con comparación de tendencias
     */
    Map<String, Object> obtenerTendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2);

    /**
     * Obtiene el número total de estudiantes en el sistema.
     * Utiliza UsuarioRepositoryInt para contar usuarios con rol de estudiante.
     * 
     * @return Map con el conteo total de estudiantes
     */
    Map<String, Object> obtenerNumeroTotalEstudiantes();

    /**
     * Obtiene la distribución de estudiantes por programa académico.
     * Utiliza UsuarioRepositoryInt para contar estudiantes por programa.
     * 
     * @return Map con la distribución de estudiantes por programa
     */
    Map<String, Object> obtenerEstudiantesPorPrograma();

    /**
     * Obtiene estadísticas detalladas por tipo de proceso.
     * Incluye conteos, porcentajes y análisis por proceso.
     * 
     * @return Map con estadísticas detalladas por proceso
     */
    Map<String, Object> obtenerEstadisticasDetalladasPorProceso();

    /**
     * Obtiene estadísticas resumidas por tipo de proceso para el dashboard.
     * Formato optimizado para gráficos y KPIs con colores y estilos.
     * 
     * @return Map con resumen por proceso
     */
    Map<String, Object> obtenerResumenPorProceso();

    /**
     * Obtiene configuración de estilos y colores para el dashboard.
     * Incluye tema claro con fondo blanco y colores optimizados.
     * 
     * @return Map con configuración de estilos
     */
    Map<String, Object> obtenerConfiguracionEstilos();

    /**
     * Obtiene estadísticas por estado de solicitudes.
     * Incluye conteos, porcentajes y análisis por estado.
     * 
     * @return Map con estadísticas por estado
     */
    Map<String, Object> obtenerEstadisticasPorEstado();

    /**
     * Obtiene estadísticas por período/mes.
     * Incluye tendencias, picos de actividad y análisis temporal.
     * 
     * @return Map con estadísticas por período
     */
    Map<String, Object> obtenerEstadisticasPorPeriodo();

    /**
     * Obtiene estadísticas por programa académico.
     * Incluye distribución de solicitudes, estudiantes y análisis por programa.
     * 
     * @return Map con estadísticas por programa
     */
    Map<String, Object> obtenerEstadisticasPorPrograma();

    /**
     * Obtiene estadísticas de tiempo promedio de procesamiento.
     * Incluye tiempos por proceso, funcionario y análisis de eficiencia.
     * 
     * @return Map con estadísticas de tiempo de procesamiento
     */
    Map<String, Object> obtenerTiempoPromedioProcesamiento();

    /**
     * Obtiene tendencias y comparativas del sistema.
     * Incluye análisis de crecimiento, comparaciones entre períodos y tendencias estratégicas.
     * 
     * @return Map con tendencias y comparativas
     */
    Map<String, Object> obtenerTendenciasYComparativas();

    /**
     * Obtiene estadísticas específicas para cursos de verano.
     * Incluye análisis de demanda por materia, tendencias temporales y recomendaciones.
     * 
     * @return Map con estadísticas detalladas de cursos de verano
     */
    Map<String, Object> obtenerEstadisticasCursosVerano();

    /**
     * Obtiene solo las tendencias temporales de cursos de verano de manera optimizada.
     * 
     * @return Map con tendencias temporales
     */
    Map<String, Object> obtenerTendenciasTemporalesCursosVerano();

    /**
     * Obtiene estadísticas agrupadas por año.
     * 
     * @param anio Año a consultar
     * @return Map con estadísticas del año
     */
    Map<String, Object> obtenerEstadisticasPorAnio(Integer anio);

    /**
     * Obtiene estadísticas agrupadas por semestre (año y número de semestre).
     * 
     * @param anio Año a consultar
     * @param semestre Número de semestre (1 o 2)
     * @return Map con estadísticas del semestre
     */
    Map<String, Object> obtenerEstadisticasPorSemestre(Integer anio, Integer semestre);

    /**
     * Obtiene historial de estadísticas por períodos académicos.
     * 
     * @param anioInicio Año de inicio (opcional)
     * @param anioFin Año de fin (opcional)
     * @return Lista de estadísticas por período ordenadas cronológicamente
     */
    List<Map<String, Object>> obtenerHistorialEstadisticas(Integer anioInicio, Integer anioFin);

    /**
     * Obtiene estadísticas por período académico usando formato YYYY-P.
     * 
     * @param periodoAcademico Período en formato "YYYY-P" (ej: "2024-2")
     * @return Map con estadísticas del período
     */
    Map<String, Object> obtenerEstadisticasPorPeriodoAcademico(String periodoAcademico);

}
