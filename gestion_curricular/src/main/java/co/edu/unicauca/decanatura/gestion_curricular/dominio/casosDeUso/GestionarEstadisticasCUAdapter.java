package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;

public class GestionarEstadisticasCUAdapter implements GestionarEstadisticasCUIntPort {

    private final GestionarEstadisticasGatewayIntPort estadisticasGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarEstadisticasCUAdapter(
            GestionarEstadisticasGatewayIntPort estadisticasGateway,
            FormateadorResultadosIntPort formateadorResultados) {
        this.estadisticasGateway = estadisticasGateway;
        this.formateadorResultados = formateadorResultados;
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
    public Estadistica obtenerEstadisticasSolicitudPeriodoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, Integer idPrograma) {
        if (proceso == null || proceso.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El proceso no puede estar vacío.");
        }

        if (fechaInicio == null || fechaFin == null || idPrograma == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Parámetros incompletos para la consulta.");
        }

        return estadisticasGateway.obtenerEstadisticasSolicitudPeriodoYPrograma(idEstadistica, proceso, fechaInicio, fechaFin, idPrograma);
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
        return estadisticasGateway.obtenerEstadisticasGlobales();
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGlobales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        return estadisticasGateway.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorProceso(String tipoProceso) {
        if (tipoProceso == null || tipoProceso.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El tipo de proceso no puede estar vacío.");
        }

        return estadisticasGateway.obtenerEstadisticasPorProceso(tipoProceso);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El estado no puede estar vacío.");
        }

        return estadisticasGateway.obtenerEstadisticasPorEstado(estado);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPrograma(Integer idPrograma) {
        if (idPrograma == null || idPrograma <= 0) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del programa debe ser válido.");
        }

        return estadisticasGateway.obtenerEstadisticasPorPrograma(idPrograma);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Las fechas de inicio y fin son requeridas.");
        }

        if (fechaInicio.after(fechaFin)) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return estadisticasGateway.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);
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

        return estadisticasGateway.obtenerEstadisticasPorProgramaYPeriodo(idPrograma, fechaInicio, fechaFin);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorProcesoYEstado(String tipoProceso, String estado) {
        if (tipoProceso == null || tipoProceso.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El tipo de proceso no puede estar vacío.");
        }

        if (estado == null || estado.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El estado no puede estar vacío.");
        }

        return estadisticasGateway.obtenerEstadisticasPorProcesoYEstado(tipoProceso, estado);
    }

    @Override
    public Map<String, Object> obtenerResumenCompleto() {
        return estadisticasGateway.obtenerResumenCompleto();
    }

    @Override
    public Map<String, Object> obtenerTendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2) {
        if (fechaInicio1 == null || fechaFin1 == null || fechaInicio2 == null || fechaFin2 == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Todas las fechas son requeridas para la comparación.");
        }

        if (fechaInicio1.after(fechaFin1) || fechaInicio2.after(fechaFin2)) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Las fechas de inicio no pueden ser posteriores a las fechas de fin.");
        }

        return estadisticasGateway.obtenerTendenciasPorPeriodo(fechaInicio1, fechaFin1, fechaInicio2, fechaFin2);
    }

    @Override
    public Map<String, Object> obtenerNumeroTotalEstudiantes() {
        return estadisticasGateway.obtenerNumeroTotalEstudiantes();
    }

    @Override
    public Map<String, Object> obtenerEstudiantesPorPrograma() {
        return estadisticasGateway.obtenerEstudiantesPorPrograma();
    }

    @Override
    public Map<String, Object> obtenerEstadisticasDetalladasPorProceso() {
        return estadisticasGateway.obtenerEstadisticasDetalladasPorProceso();
    }

    @Override
    public Map<String, Object> obtenerResumenPorProceso() {
        return estadisticasGateway.obtenerResumenPorProceso();
    }

    @Override
    public Map<String, Object> obtenerConfiguracionEstilos() {
        return estadisticasGateway.obtenerConfiguracionEstilos();
    }
}
