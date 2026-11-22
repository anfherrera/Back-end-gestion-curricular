package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import java.util.ArrayList;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;

/**
 * Mapeo explícito entre {@link Estadistica} y su entidad (DUP-10). Una lista nula en el origen
 * deja la lista vacía que crea el constructor del destino, como hacía {@code ModelMapper} con
 * {@code skipNull}.
 */
public final class EstadisticaMapperPersistencia {

    private EstadisticaMapperPersistencia() {
    }

    public static Estadistica aDominio(EstadisticaEntity entidad) {
        Origen.exigir(entidad);
        Estadistica estadistica = new Estadistica();
        estadistica.setIdEstadistica(entidad.getId_estadistica());
        estadistica.setNombre(entidad.getNombre());
        estadistica.setTotalSolicitudes(entidad.getTotal_solicitudes());
        estadistica.setTotalAprobadas(entidad.getTotal_aprobadas());
        estadistica.setTotalRechazadas(entidad.getTotal_rechazadas());
        if (entidad.getPeriodos_academico() != null) {
            estadistica.setPeriodosAcademico(new ArrayList<>(entidad.getPeriodos_academico()));
        }
        if (entidad.getNombres_procesos() != null) {
            estadistica.setNombresProcesos(new ArrayList<>(entidad.getNombres_procesos()));
        }
        if (entidad.getNombres_programas() != null) {
            estadistica.setNombresProgramas(new ArrayList<>(entidad.getNombres_programas()));
        }
        return estadistica;
    }

    public static EstadisticaEntity aEntidad(Estadistica estadistica) {
        Origen.exigir(estadistica);
        EstadisticaEntity entidad = new EstadisticaEntity();
        entidad.setId_estadistica(estadistica.getIdEstadistica());
        entidad.setNombre(estadistica.getNombre());
        entidad.setTotal_solicitudes(estadistica.getTotalSolicitudes());
        entidad.setTotal_aprobadas(estadistica.getTotalAprobadas());
        entidad.setTotal_rechazadas(estadistica.getTotalRechazadas());
        if (estadistica.getPeriodosAcademico() != null) {
            entidad.setPeriodos_academico(new ArrayList<>(estadistica.getPeriodosAcademico()));
        }
        if (estadistica.getNombresProcesos() != null) {
            entidad.setNombres_procesos(new ArrayList<>(estadistica.getNombresProcesos()));
        }
        if (estadistica.getNombresProgramas() != null) {
            entidad.setNombres_programas(new ArrayList<>(estadistica.getNombresProgramas()));
        }
        return entidad;
    }
}
