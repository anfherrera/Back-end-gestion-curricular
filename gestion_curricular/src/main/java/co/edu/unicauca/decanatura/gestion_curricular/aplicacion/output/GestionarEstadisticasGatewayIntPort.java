package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;

public interface GestionarEstadisticasGatewayIntPort {
    Estadistica crearEstadistica(Estadistica estadistica);
    Estadistica actualizarEstadistica(Estadistica estadistica);
    Boolean eliminarEstadistica(Integer idEstadistica);
    Estadistica obtenerEstadisticaPorId(Integer idEstadistica);
    Estadistica obtenerEstadisticasSolicitudPeriodoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, Integer idPrograma);
    Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, Integer idEstado, Integer idPrograma);
    List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma( Date fechaInicio, Date fechaFin, Integer idPrograma);

}
