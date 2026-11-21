package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DashboardEjecutivo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EntradaHistorial;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorSemestre;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstudiantesPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesContadas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ProcesosPorProgramaYPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ProgramasPorProcesoYEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenCompleto;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Rendimiento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TendenciasPorPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TendenciasCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TotalEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorMes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaTendencias;

public interface GestionarEstadisticasCUIntPort {
    Estadistica crearEstadistica(Estadistica estadistica);
    Estadistica actualizarEstadistica(Estadistica estadistica);
    Boolean eliminarEstadistica(Integer idEstadistica);
    Estadistica obtenerEstadisticaPorId(Integer idEstadistica);
    Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, String estado, Integer idPrograma);
    List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma( Date fechaInicio, Date fechaFin, Integer idPrograma);

    /**
     * Obtiene estadísticas globales del sistema combinando todos los procesos.
     * Utiliza SolicitudRepositoryInt para obtener conteos totales.
     * 
     * @return las estadísticas globales que cuenta la base de datos
     */
    GlobalesContadas obtenerEstadisticasGlobales();

    /**
     * Obtiene estadísticas globales del sistema combinando todos los procesos con filtros.
     * Utiliza SolicitudRepositoryInt para obtener conteos totales.
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return las estadísticas globales que cuenta la base de datos
     */
    GlobalesContadas obtenerEstadisticasGlobales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin);

    /**
     * Obtiene estadísticas específicas por tipo de proceso.
     * Utiliza SolicitudRepositoryInt para filtrar por tipo de solicitud.
     * 
     * @param tipoProceso Tipo de proceso (REINGRESO, HOMOLOGACION, ECAES, CURSO_VERANO, PAZ_SALVO)
     * @return estadísticas del proceso específico
     */
    EstadisticasPorProceso obtenerEstadisticasPorProceso(String tipoProceso);

    /**
     * Obtiene estadísticas por estado de solicitud en todos los procesos.
     * Utiliza SolicitudRepositoryInt para contar por estado.
     * 
     * @param estado Estado de la solicitud (EN_PROCESO, APROBADA, RECHAZADA, etc.)
     * @return estadísticas por estado
     */
    EstadisticasPorEstado obtenerEstadisticasPorEstado(String estado);

    /**
     * Obtiene estadísticas por programa académico.
     * Utiliza SolicitudRepositoryInt y ProgramaRepositoryInt para filtrar por programa.
     * 
     * @param idPrograma ID del programa académico
     * @return estadísticas del programa
     */
    EstadisticasPorPrograma obtenerEstadisticasPorPrograma(Integer idPrograma);

    /**
     * Obtiene estadísticas por período académico.
     * Utiliza SolicitudRepositoryInt para filtrar por rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return estadísticas del período
     */
    EstadisticasPorPeriodo obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin);

    /**
     * Obtiene estadísticas combinadas por programa y período académico.
     * Utiliza SolicitudRepositoryInt para filtrar por programa y fechas.
     * 
     * @param idPrograma ID del programa académico
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return estadísticas combinadas
     */
    ProcesosPorProgramaYPeriodo obtenerEstadisticasPorProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin);

    /**
     * Obtiene estadísticas detalladas por proceso y estado.
     * Utiliza SolicitudRepositoryInt para filtrar por tipo de solicitud y estado.
     * 
     * @param tipoProceso Tipo de proceso
     * @param estado Estado de la solicitud
     * @return estadísticas detalladas
     */
    ProgramasPorProcesoYEstado obtenerEstadisticasPorProcesoYEstado(String tipoProceso, String estado);

    /**
     * Obtiene un resumen completo de todas las estadísticas del sistema.
     * Utiliza múltiples repositorios para generar un dashboard completo.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return el resumen completo
     */
    ResumenCompleto obtenerResumenCompleto(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene estadísticas de tendencias por período (comparación entre períodos).
     * Utiliza SolicitudRepositoryInt para analizar tendencias temporales.
     * 
     * @param fechaInicio1 Fecha de inicio del primer período
     * @param fechaFin1 Fecha de fin del primer período
     * @param fechaInicio2 Fecha de inicio del segundo período
     * @param fechaFin2 Fecha de fin del segundo período
     * @return comparación de tendencias
     */
    TendenciasPorPeriodo obtenerTendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2);

    /**
     * Obtiene el número total de estudiantes en el sistema.
     * Utiliza UsuarioRepositoryInt para contar usuarios con rol de estudiante.
     * 
     * @return el conteo total de estudiantes
     */
    TotalEstudiantes obtenerNumeroTotalEstudiantes();

    /**
     * Obtiene la distribución de estudiantes por programa académico.
     * Utiliza UsuarioRepositoryInt para contar estudiantes por programa.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return la distribución de estudiantes por programa
     */
    EstudiantesPorPrograma obtenerEstudiantesPorPrograma(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene estadísticas detalladas por tipo de proceso.
     * Incluye conteos, porcentajes y análisis por proceso.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return la vista del dashboard por proceso
     */
    VistaPorProceso obtenerEstadisticasDetalladasPorProceso(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene estadísticas por estado de solicitudes.
     * Incluye conteos, porcentajes y análisis por estado.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return la vista del dashboard por estado
     */
    VistaPorEstado obtenerEstadisticasPorEstado(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene estadísticas por período/mes.
     * Incluye tendencias, picos de actividad y análisis temporal.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return la vista del dashboard por mes de registro
     */
    VistaPorMes obtenerEstadisticasPorPeriodo(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene estadísticas por programa académico.
     * Incluye distribución de solicitudes, estudiantes y análisis por programa.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return la vista del dashboard por programa
     */
    VistaPorPrograma obtenerEstadisticasPorPrograma(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene tendencias y comparativas del sistema.
     * Incluye análisis de crecimiento, comparaciones entre períodos y tendencias estratégicas.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return la vista del dashboard de tendencias y comparativas
     */
    VistaTendencias obtenerTendenciasYComparativas(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene estadísticas específicas para cursos de verano.
     * Incluye análisis de demanda por materia, tendencias temporales y recomendaciones.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return el análisis de demanda, la predicción y las recomendaciones
     */
    AnalisisCursosVerano obtenerEstadisticasCursosVerano(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene solo las tendencias temporales de cursos de verano de manera optimizada.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return la demanda de cada uno de los doce meses
     */
    TendenciasCursosVerano obtenerTendenciasTemporalesCursosVerano(String periodoAcademico, Integer idPrograma);

    /**
     * Obtiene historial de estadísticas por períodos académicos.
     * 
     * @param anioInicio Año de inicio (opcional)
     * @param anioFin Año de fin (opcional)
     * @return Lista de estadísticas por período ordenadas cronológicamente
     */
    List<EntradaHistorial> obtenerHistorialEstadisticas(Integer anioInicio, Integer anioFin);

    /**
     * Obtiene estadísticas por período académico usando formato YYYY-P.
     * 
     * @param periodoAcademico Período en formato "YYYY-P" (ej: "2024-2")
     * @return estadísticas del período
     */
    EstadisticasPorSemestre obtenerEstadisticasPorPeriodoAcademico(String periodoAcademico);


    /**
     * Estadísticas globales con filtros. Con período académico (se acepta en forma legible, ver
     * {@code PeriodoAcademicoEnum.normalizar}) salen del resumen completo de ese período y programa, y si además
     * hay proceso, con los KPI de ese proceso; sin período, del conteo por proceso, programa y fechas.
     */
    Globales obtenerEstadisticasGlobales(String proceso, Integer idPrograma, String periodoAcademico,
            Date fechaInicio, Date fechaFin);

    /**
     * Resumen completo del período (en forma legible o "YYYY-P") y programa; con proceso, los KPI globales y
     * porTipoProceso se reducen a ese proceso y se anota en filtrosAplicados.
     */
    ResumenCompleto obtenerResumenCompleto(String periodoAcademico, Integer idPrograma, String proceso);

    /** Dashboard ejecutivo: resumen global, procesos, estados y filtros aplicados. */
    DashboardEjecutivo obtenerDashboardEjecutivo(String periodoAcademico, Integer idPrograma, String proceso);

    /**
     * Estadísticas globales con los indicadores de rendimiento: tasa de procesamiento (decididas sobre el total)
     * y eficiencia de aprobación (aprobadas sobre las decididas).
     */
    Rendimiento obtenerEstadisticasRendimiento();

    record DatosReporte(Globales estadisticas, AnalisisCursosVerano cursosVerano) {
    }

    DatosReporte prepararReporte(String proceso, Integer idPrograma, String periodo, Date fechaInicio, Date fechaFin);
}
