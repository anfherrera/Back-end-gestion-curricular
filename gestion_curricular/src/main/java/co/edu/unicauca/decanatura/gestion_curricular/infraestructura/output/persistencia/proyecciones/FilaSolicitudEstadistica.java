package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.proyecciones;

import java.util.Date;

/**
 * Una solicitud con lo que necesitan las estadísticas, leída como columnas sueltas (sin cargar entidades).
 * Los campos del usuario, programa, curso y materia son null si la solicitud no los tiene.
 *
 * @param tipo clase de la entidad (SolicitudHomologacionEntity, SolicitudEcaesEntity...), que define el proceso
 */
public record FilaSolicitudEstadistica(
        Integer idSolicitud,
        Class<?> tipo,
        String nombre,
        String periodoAcademico,
        Date fechaRegistro,
        Integer idUsuario,
        String nombreUsuario,
        Integer idPrograma,
        String codigoPrograma,
        String nombrePrograma,
        Integer idCurso,
        String periodoCurso,
        Integer idMateria,
        String codigoMateria,
        String nombreMateria) {
}
