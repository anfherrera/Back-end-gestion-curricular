package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;

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
}
