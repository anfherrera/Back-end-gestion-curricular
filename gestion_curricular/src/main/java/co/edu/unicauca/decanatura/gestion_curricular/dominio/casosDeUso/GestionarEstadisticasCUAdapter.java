package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasDashboard;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.EstadisticasGenerales;

public class GestionarEstadisticasCUAdapter implements GestionarEstadisticasCUIntPort {

    private final GestionarEstadisticasGatewayIntPort estadisticasGateway;
    private final FormateadorResultadosIntPort formateadorResultados;
    private final EstadisticasGenerales generales;
    private final EstadisticasDashboard dashboard;
    private final EstadisticasCursosVerano cursosVerano;

    public GestionarEstadisticasCUAdapter(
            GestionarEstadisticasGatewayIntPort estadisticasGateway,
            FormateadorResultadosIntPort formateadorResultados) {
        this.estadisticasGateway = estadisticasGateway;
        this.formateadorResultados = formateadorResultados;
        this.generales = new EstadisticasGenerales(estadisticasGateway);
        this.dashboard = new EstadisticasDashboard(estadisticasGateway);
        this.cursosVerano = new EstadisticasCursosVerano(estadisticasGateway);
    }

    @Override
    public Estadistica crearEstadistica(Estadistica estadistica) {
        if (estadistica == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("La estadística no puede ser nula.");
        }

        return estadisticasGateway.crearEstadistica(estadistica);
    }

    @Override
    public Estadistica actualizarEstadistica(Estadistica estadistica) {
        if (estadistica == null || estadistica.getId_estadistica() == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Estadística inválida para actualización.");
        }

        Estadistica existente = estadisticasGateway.obtenerEstadisticaPorId(estadistica.getId_estadistica());

        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró la estadística a actualizar.");
        }

        return estadisticasGateway.actualizarEstadistica(estadistica);
    }

    @Override
    public Boolean eliminarEstadistica(Integer idEstadistica) {
        if (idEstadistica == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Estadistica estadistica = estadisticasGateway.obtenerEstadisticaPorId(idEstadistica);

        if (estadistica == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Estadística no encontrada para eliminar.");
        }

        return estadisticasGateway.eliminarEstadistica(idEstadistica);
    }

    @Override
    public Estadistica obtenerEstadisticaPorId(Integer idEstadistica) {
        if (idEstadistica == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Estadistica estadistica = estadisticasGateway.obtenerEstadisticaPorId(idEstadistica);

        if (estadistica == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Estadística no encontrada.");
        }

        return estadistica;
    }

    @Override
    public Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, String estado, Integer idPrograma) {
        if (proceso == null || proceso.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El proceso no puede estar vacío.");
        }

        if (fechaInicio == null || fechaFin == null || idPrograma == null || estado == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Parámetros incompletos para la consulta.");
        }

        return estadisticasGateway.obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(idEstadistica, proceso, fechaInicio, fechaFin, estado, idPrograma);
    }

    @Override
    public List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma(Date fechaInicio, Date fechaFin, Integer idPrograma) {
        if (fechaInicio == null || fechaFin == null || idPrograma == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Parámetros incompletos para la consulta.");
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
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El tipo de proceso no puede estar vacío.");
        }

        return generales.porProceso(tipoProceso);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El estado no puede estar vacío.");
        }

        return generales.porEstado(estado);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPrograma(Integer idPrograma) {
        if (idPrograma == null || idPrograma <= 0) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del programa debe ser válido.");
        }

        return generales.porPrograma(idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Las fechas de inicio y fin son requeridas.");
        }

        if (fechaInicio.after(fechaFin)) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return generales.porPeriodo(fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin) {
        if (idPrograma == null || idPrograma <= 0) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del programa debe ser válido.");
        }

        if (fechaInicio == null || fechaFin == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Las fechas de inicio y fin son requeridas.");
        }

        if (fechaInicio.after(fechaFin)) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return generales.porProgramaYPeriodo(idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorProcesoYEstado(String tipoProceso, String estado) {
        if (tipoProceso == null || tipoProceso.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El tipo de proceso no puede estar vacío.");
        }

        if (estado == null || estado.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El estado no puede estar vacío.");
        }

        return generales.porProcesoYEstado(tipoProceso, estado);
    }

    @Override
    public Map<String, Object> obtenerResumenCompleto(String periodoAcademico, Integer idPrograma) {
        return generales.resumenCompleto(periodoAcademico, idPrograma);
    }

    @Override
    public Map<String, Object> obtenerTendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2) {
        if (fechaInicio1 == null || fechaFin1 == null || fechaInicio2 == null || fechaFin2 == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Todas las fechas son requeridas para la comparación.");
        }

        if (fechaInicio1.after(fechaFin1) || fechaInicio2.after(fechaFin2)) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Las fechas de inicio no pueden ser posteriores a las fechas de fin.");
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
        return dashboard.detalladasPorProceso(periodoAcademico, idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorEstado(String periodoAcademico, Integer idPrograma) {
        return dashboard.porEstado(periodoAcademico, idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodo(String periodoAcademico, Integer idPrograma) {
        return dashboard.porPeriodo(periodoAcademico, idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPrograma(String periodoAcademico, Integer idPrograma) {
        return dashboard.porPrograma(periodoAcademico, idPrograma);
    }

    @Override
    public Map<String, Object> obtenerTendenciasYComparativas(String periodoAcademico, Integer idPrograma) {
        return dashboard.tendenciasYComparativas(periodoAcademico, idPrograma);
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
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Año de inicio inválido. Debe estar entre 2020 y 2030.");
        }
        if (anioFin != null && (anioFin < 2020 || anioFin > 2030)) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Año de fin inválido. Debe estar entre 2020 y 2030.");
        }
        if (anioInicio != null && anioFin != null && anioInicio > anioFin) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El año de inicio no puede ser mayor al año de fin.");
        }
        return generales.historial(anioInicio, anioFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodoAcademico(String periodoAcademico) {
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El período académico no puede estar vacío.");
        }
        return generales.porPeriodoAcademico(periodoAcademico.trim());
    }
}
