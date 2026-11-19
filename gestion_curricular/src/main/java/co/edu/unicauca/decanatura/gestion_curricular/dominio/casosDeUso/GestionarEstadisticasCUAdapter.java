package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasDashboard;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasGenerales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.TipoProcesoSolicitud;

public class GestionarEstadisticasCUAdapter implements GestionarEstadisticasCUIntPort {

    private final GestionarEstadisticasGatewayIntPort estadisticasGateway;
    private final EstadisticasGenerales generales;
    private final EstadisticasDashboard dashboard;
    private final EstadisticasCursosVerano cursosVerano;

    public GestionarEstadisticasCUAdapter(
            GestionarEstadisticasGatewayIntPort estadisticasGateway) {
        this.estadisticasGateway = estadisticasGateway;
        this.generales = new EstadisticasGenerales(estadisticasGateway);
        this.dashboard = new EstadisticasDashboard(estadisticasGateway);
        this.cursosVerano = new EstadisticasCursosVerano(estadisticasGateway);
    }

    @Override
    public Estadistica crearEstadistica(Estadistica estadistica) {
        if (estadistica == null) {
            throw new ReglaNegocioExcepcion("La estadística no puede ser nula.");
        }

        return estadisticasGateway.crearEstadistica(estadistica);
    }

    @Override
    public Estadistica actualizarEstadistica(Estadistica estadistica) {
        if (estadistica == null || estadistica.getId_estadistica() == null) {
            throw new ReglaNegocioExcepcion("Estadística inválida para actualización.");
        }

        Estadistica existente = estadisticasGateway.obtenerEstadisticaPorId(estadistica.getId_estadistica());

        if (existente == null) {
            throw new EntidadNoExisteException("No se encontró la estadística a actualizar.");
        }

        return estadisticasGateway.actualizarEstadistica(estadistica);
    }

    @Override
    public Boolean eliminarEstadistica(Integer idEstadistica) {
        if (idEstadistica == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Estadistica estadistica = estadisticasGateway.obtenerEstadisticaPorId(idEstadistica);

        if (estadistica == null) {
            throw new EntidadNoExisteException("Estadística no encontrada para eliminar.");
        }

        return estadisticasGateway.eliminarEstadistica(idEstadistica);
    }

    @Override
    public Estadistica obtenerEstadisticaPorId(Integer idEstadistica) {
        if (idEstadistica == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Estadistica estadistica = estadisticasGateway.obtenerEstadisticaPorId(idEstadistica);

        if (estadistica == null) {
            throw new EntidadNoExisteException("Estadística no encontrada.");
        }

        return estadistica;
    }

    @Override
    public Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, String estado, Integer idPrograma) {
        if (proceso == null || proceso.isBlank()) {
            throw new ReglaNegocioExcepcion("El proceso no puede estar vacío.");
        }

        if (fechaInicio == null || fechaFin == null || idPrograma == null || estado == null) {
            throw new ReglaNegocioExcepcion("Parámetros incompletos para la consulta.");
        }

        return estadisticasGateway.obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(idEstadistica, proceso, fechaInicio, fechaFin, estado, idPrograma);
    }

    @Override
    public List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma(Date fechaInicio, Date fechaFin, Integer idPrograma) {
        if (fechaInicio == null || fechaFin == null || idPrograma == null) {
            throw new ReglaNegocioExcepcion("Parámetros incompletos para la consulta.");
        }

        return estadisticasGateway.obtenerEstadisticasPeriodoEstadoYPrograma(fechaInicio, fechaFin, idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGlobales() {
        return generales.globales(null, null, null, null);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGlobales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        return generales.globales(proceso, idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorProceso(String tipoProceso) {
        if (tipoProceso == null || tipoProceso.isBlank()) {
            throw new ReglaNegocioExcepcion("El tipo de proceso no puede estar vacío.");
        }

        return generales.porProceso(tipoProceso);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ReglaNegocioExcepcion("El estado no puede estar vacío.");
        }

        return generales.porEstado(estado);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPrograma(Integer idPrograma) {
        if (idPrograma == null || idPrograma <= 0) {
            throw new ReglaNegocioExcepcion("El ID del programa debe ser válido.");
        }

        return generales.porPrograma(idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaNegocioExcepcion("Las fechas de inicio y fin son requeridas.");
        }

        if (fechaInicio.after(fechaFin)) {
            throw new ReglaNegocioExcepcion("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return generales.porPeriodo(fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin) {
        if (idPrograma == null || idPrograma <= 0) {
            throw new ReglaNegocioExcepcion("El ID del programa debe ser válido.");
        }

        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaNegocioExcepcion("Las fechas de inicio y fin son requeridas.");
        }

        if (fechaInicio.after(fechaFin)) {
            throw new ReglaNegocioExcepcion("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return generales.porProgramaYPeriodo(idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorProcesoYEstado(String tipoProceso, String estado) {
        if (tipoProceso == null || tipoProceso.isBlank()) {
            throw new ReglaNegocioExcepcion("El tipo de proceso no puede estar vacío.");
        }

        if (estado == null || estado.isBlank()) {
            throw new ReglaNegocioExcepcion("El estado no puede estar vacío.");
        }

        return generales.porProcesoYEstado(tipoProceso, estado);
    }

    @Override
    public Map<String, Object> obtenerResumenCompleto(String periodoAcademico, Integer idPrograma) {
        return generales.resumenCompleto(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public Map<String, Object> obtenerTendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2) {
        if (fechaInicio1 == null || fechaFin1 == null || fechaInicio2 == null || fechaFin2 == null) {
            throw new ReglaNegocioExcepcion("Todas las fechas son requeridas para la comparación.");
        }

        if (fechaInicio1.after(fechaFin1) || fechaInicio2.after(fechaFin2)) {
            throw new ReglaNegocioExcepcion("Las fechas de inicio no pueden ser posteriores a las fechas de fin.");
        }

        return generales.tendenciasPorPeriodo(fechaInicio1, fechaFin1, fechaInicio2, fechaFin2);
    }

    @Override
    public Map<String, Object> obtenerNumeroTotalEstudiantes() {
        return generales.numeroTotalEstudiantes();
    }

    @Override
    public Map<String, Object> obtenerEstudiantesPorPrograma(String periodoAcademico, Integer idPrograma) {
        return generales.estudiantesPorPrograma(idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasDetalladasPorProceso(String periodoAcademico, Integer idPrograma) {
        return dashboard.detalladasPorProceso(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorEstado(String periodoAcademico, Integer idPrograma) {
        return dashboard.porEstado(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodo(String periodoAcademico, Integer idPrograma) {
        return dashboard.porPeriodo(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPrograma(String periodoAcademico, Integer idPrograma) {
        return dashboard.porPrograma(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public Map<String, Object> obtenerTendenciasYComparativas(String periodoAcademico, Integer idPrograma) {
        return dashboard.tendenciasYComparativas(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasCursosVerano(String periodoAcademico, Integer idPrograma) {
        return cursosVerano.estadisticas(periodoAcademico, idPrograma);
    }

    @Override
    public Map<String, Object> obtenerTendenciasTemporalesCursosVerano(String periodoAcademico, Integer idPrograma) {
        return cursosVerano.tendenciasTemporales(periodoAcademico, idPrograma);
    }

    @Override
    public List<Map<String, Object>> obtenerHistorialEstadisticas(Integer anioInicio, Integer anioFin) {
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
        return generales.historial(anioInicio, anioFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodoAcademico(String periodoAcademico) {
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("El período académico no puede estar vacío.");
        }
        return generales.porPeriodoAcademico(periodoAcademico.trim());
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGlobales(String proceso, Integer idPrograma, String periodoAcademico,
            Date fechaInicio, Date fechaFin) {
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            return generales.globalesDelPeriodo(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma, proceso);
        }
        return generales.globales(proceso, idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerResumenCompleto(String periodoAcademico, Integer idPrograma, String proceso) {
        return generales.resumenCompleto(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma, proceso);
    }

    @Override
    public Map<String, Object> obtenerDashboardEjecutivo(String periodoAcademico, Integer idPrograma, String proceso) {
        return generales.dashboardEjecutivo(PeriodoAcademicoEnum.normalizar(periodoAcademico), idPrograma, proceso);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasRendimiento() {
        return generales.rendimiento();
    }

    @Override
    public DatosReporte prepararReporte(String proceso, Integer idPrograma, String periodoAcademico, Date fechaInicio, Date fechaFin) {
        String periodo = PeriodoAcademicoEnum.normalizar(periodoAcademico);
        @SuppressWarnings("unchecked")
        Map<String, Object> estadisticas = periodo == null ? null
                : (Map<String, Object>) obtenerResumenCompleto(periodo, idPrograma).get("estadisticasGlobales");
        if (estadisticas == null) {
            estadisticas = obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
        }

        boolean sinProceso = proceso == null || proceso.isBlank();
        Map<String, Object> cursosVerano = sinProceso
                || TipoProcesoSolicitud.CURSOS_VERANO.equals(TipoProcesoSolicitud.porNombre(proceso))
                ? obtenerEstadisticasCursosVerano(periodo, idPrograma) : null;
        return new DatosReporte(estadisticas, cursosVerano);
    }
}
