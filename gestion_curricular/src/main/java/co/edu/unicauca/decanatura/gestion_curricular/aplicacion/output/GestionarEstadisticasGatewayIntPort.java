package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/**
 * Datos que necesitan las estadísticas. Los cálculos están en el dominio
 * ({@code dominio.servicios.estadisticas}); aquí solo se lee la base de datos.
 */
public interface GestionarEstadisticasGatewayIntPort {
    Estadistica crearEstadistica(Estadistica estadistica);
    Estadistica actualizarEstadistica(Estadistica estadistica);
    Boolean eliminarEstadistica(Integer idEstadistica);
    Estadistica obtenerEstadisticaPorId(Integer idEstadistica);
    Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso, Date fechaInicio, Date fechaFin, String estado, Integer idPrograma);
    List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma( Date fechaInicio, Date fechaFin, Integer idPrograma);

    /**
     * Todas las solicitudes, cada una con el subtipo de su proceso (homologación, ECAES...), sus estados,
     * el usuario con su programa y, si es de cursos de verano, el curso con su materia.
     */
    List<Solicitud> listarSolicitudesConEstadosYUsuario();

    List<Programa> listarProgramas();

    List<String> listarNombresProgramas();

    /** Usuarios con rol de estudiante, con su programa. */
    List<Usuario> listarEstudiantes();

    // Conteos hechos por la base de datos (0 si no hay datos). El estado se compara sin distinguir mayúsculas.

    int contarSolicitudesPorEstado(String estado);

    int contarTotalSolicitudes();

    int contarSolicitudesPorEstadoConFiltros(String estado, String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin);

    int contarSolicitudesConFiltros(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin);

    int contarSolicitudesPorProgramaConFiltros(Integer idPrograma, String proceso, Date fechaInicio, Date fechaFin);
}
