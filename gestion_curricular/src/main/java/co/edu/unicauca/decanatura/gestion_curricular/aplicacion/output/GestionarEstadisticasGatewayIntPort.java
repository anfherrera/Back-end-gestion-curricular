package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;

public interface GestionarEstadisticasGatewayIntPort {
    Estadistica crearEstadistica(Estadistica estadistica);
    Estadistica actualizarEstadistica(Estadistica estadistica);
    Estadistica eliminarEstadistica(Integer idEstadistica);
    Estadistica obtenerEstadisticaPorId(Integer idEstadistica);
    Estadistica obtenerEstadisticaPorNomre(String nombreEstadistica);
    List<Estadistica> obtenerEstadisticasPorPrograma(Integer idPrograma);
    List<Estadistica> obtenerEstadisticasPorSolicitud(Integer idSolicitud);
    List<Estadistica> obeterEstadisiticasPorPeriodo(Date fechaInicio, Date fechaFin);


}
