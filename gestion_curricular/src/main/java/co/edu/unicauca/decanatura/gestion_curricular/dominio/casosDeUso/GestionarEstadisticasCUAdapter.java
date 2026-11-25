package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DashboardEjecutivo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EntradaHistorial;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorSemestre;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstudiantesPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesContadas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenCompleto;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Rendimiento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TendenciasCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TotalEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorMes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaTendencias;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ConsultasEstadisticas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.DashboardPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.DashboardPorMes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.DashboardPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.DashboardPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.DashboardTendencias;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasGlobales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.HistorialEstadisticas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ResumenEstadisticas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.TipoProcesoSolicitud;

public class GestionarEstadisticasCUAdapter implements GestionarEstadisticasCUIntPort {

    private final GestionarEstadisticasGatewayIntPort estadisticasGateway;
    private final EstadisticasGlobales globales;
    private final ConsultasEstadisticas consultas;
    private final HistorialEstadisticas historial;
    private final ResumenEstadisticas resumen;
    private final EstadisticasEstudiantes estudiantes;
    private final DashboardPorProceso dashboardPorProceso;
    private final DashboardPorEstado dashboardPorEstado;
    private final DashboardPorMes dashboardPorMes;
    private final DashboardPorPrograma dashboardPorPrograma;
    private final DashboardTendencias dashboardTendencias;
    private final EstadisticasCursosVerano cursosVerano;

    public GestionarEstadisticasCUAdapter(
            GestionarEstadisticasGatewayIntPort estadisticasGateway) {
        this.estadisticasGateway = estadisticasGateway;
        this.globales = new EstadisticasGlobales(estadisticasGateway);
        this.consultas = new ConsultasEstadisticas(estadisticasGateway);
        this.historial = new HistorialEstadisticas(estadisticasGateway);
        this.resumen = new ResumenEstadisticas(estadisticasGateway);
        this.estudiantes = new EstadisticasEstudiantes(estadisticasGateway);
        this.dashboardPorProceso = new DashboardPorProceso(estadisticasGateway);
        this.dashboardPorEstado = new DashboardPorEstado(estadisticasGateway);
        this.dashboardPorMes = new DashboardPorMes(estadisticasGateway);
        this.dashboardPorPrograma = new DashboardPorPrograma(estadisticasGateway);
        this.dashboardTendencias = new DashboardTendencias(estadisticasGateway);
        this.cursosVerano = new EstadisticasCursosVerano(estadisticasGateway);
    }

    @Override
    public GlobalesContadas obtenerEstadisticasGlobales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        return globales.globales(proceso, idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public EstadisticasPorEstado obtenerEstadisticasPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ReglaNegocioExcepcion("El estado no puede estar vacío.");
        }

        return consultas.porEstado(estado);
    }

    @Override
    public EstadisticasPorPrograma obtenerEstadisticasPorPrograma(Integer idPrograma) {
        if (idPrograma == null || idPrograma <= 0) {
            throw new ReglaNegocioExcepcion("El ID del programa debe ser válido.");
        }

        return consultas.porPrograma(idPrograma);
    }

    @Override
    public EstadisticasPorPeriodo obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaNegocioExcepcion("Las fechas de inicio y fin son requeridas.");
        }

        if (fechaInicio.after(fechaFin)) {
            throw new ReglaNegocioExcepcion("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return consultas.porPeriodo(fechaInicio, fechaFin);
    }

    @Override
    public ResumenCompleto obtenerResumenCompleto(String periodoAcademico, Integer idPrograma) {
        return resumen.resumenCompleto(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public TotalEstudiantes obtenerNumeroTotalEstudiantes() {
        return estudiantes.numeroTotalEstudiantes();
    }

    @Override
    public EstudiantesPorPrograma obtenerEstudiantesPorPrograma(String periodoAcademico, Integer idPrograma) {
        return estudiantes.estudiantesPorPrograma(idPrograma);
    }

    @Override
    public VistaPorProceso obtenerEstadisticasDetalladasPorProceso(String periodoAcademico, Integer idPrograma) {
        return dashboardPorProceso.detalladasPorProceso(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public VistaPorEstado obtenerEstadisticasPorEstado(String periodoAcademico, Integer idPrograma) {
        return dashboardPorEstado.porEstado(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public VistaPorMes obtenerEstadisticasPorPeriodo(String periodoAcademico, Integer idPrograma) {
        return dashboardPorMes.porPeriodo(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public VistaPorPrograma obtenerEstadisticasPorPrograma(String periodoAcademico, Integer idPrograma) {
        return dashboardPorPrograma.porPrograma(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public VistaTendencias obtenerTendenciasYComparativas(String periodoAcademico, Integer idPrograma) {
        return dashboardTendencias.tendenciasYComparativas(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public AnalisisCursosVerano obtenerEstadisticasCursosVerano(String periodoAcademico, Integer idPrograma) {
        return cursosVerano.estadisticas(periodoAcademico, idPrograma);
    }

    @Override
    public TendenciasCursosVerano obtenerTendenciasTemporalesCursosVerano(String periodoAcademico, Integer idPrograma) {
        return cursosVerano.tendenciasTemporales(periodoAcademico, idPrograma);
    }

    @Override
    public List<EntradaHistorial> obtenerHistorialEstadisticas(Integer anioInicio, Integer anioFin) {
        // Validar años si se proporcionan
        if (anioInicio != null && (anioInicio < 2020 || anioInicio > 2030)) {
            throw new ReglaNegocioExcepcion("Año de inicio inválido. Debe estar entre 2020 y 2030.");
        }
        if (anioFin != null && (anioFin < 2020 || anioFin > 2030)) {
            throw new ReglaNegocioExcepcion("Año de fin inválido. Debe estar entre 2020 y 2030.");
        }
        if (anioInicio != null && anioFin != null && anioInicio > anioFin) {
            throw new ReglaNegocioExcepcion("El año de inicio no puede ser mayor al año de fin.");
        }
        return historial.historial(anioInicio, anioFin);
    }

    @Override
    public EstadisticasPorSemestre obtenerEstadisticasPorPeriodoAcademico(String periodoAcademico) {
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("El período académico no puede estar vacío.");
        }
        return historial.porPeriodoAcademico(periodoAcademico.trim());
    }

    @Override
    public Globales obtenerEstadisticasGlobales(String proceso, Integer idPrograma, String periodoAcademico,
            Date fechaInicio, Date fechaFin) {
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            return resumen.globalesDelPeriodo(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma, proceso);
        }
        return globales.globales(proceso, idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public ResumenCompleto obtenerResumenCompleto(String periodoAcademico, Integer idPrograma, String proceso) {
        return resumen.resumenCompleto(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma, proceso);
    }

    @Override
    public DashboardEjecutivo obtenerDashboardEjecutivo(String periodoAcademico, Integer idPrograma, String proceso) {
        return resumen.dashboardEjecutivo(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma, proceso);
    }

    @Override
    public Rendimiento obtenerEstadisticasRendimiento() {
        return globales.rendimiento();
    }

    @Override
    public DatosReporte prepararReporte(String proceso, Integer idPrograma, String periodoAcademico, Date fechaInicio, Date fechaFin) {
        String periodo = PeriodoAcademicoEnum.normalizar(periodoAcademico);
        Globales estadisticas = periodo == null
                ? obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin)
                : obtenerResumenCompleto(periodo, idPrograma).estadisticasGlobales();

        boolean sinProceso = proceso == null || proceso.isBlank();
        AnalisisCursosVerano cursosVerano = sinProceso
                || TipoProcesoSolicitud.CURSOS_VERANO.equals(TipoProcesoSolicitud.porNombre(proceso))
                ? obtenerEstadisticasCursosVerano(periodo, idPrograma) : null;
        return new DatosReporte(estadisticas, cursosVerano);
    }
}
