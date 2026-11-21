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
        estadistica.setId_estadistica(entidad.getId_estadistica());
        estadistica.setNombre(entidad.getNombre());
        estadistica.setTotal_solicitudes(entidad.getTotal_solicitudes());
        estadistica.setTotal_aprobadas(entidad.getTotal_aprobadas());
        estadistica.setTotal_rechazadas(entidad.getTotal_rechazadas());
        if (entidad.getPeriodos_academico() != null) {
            estadistica.setPeriodos_academico(new ArrayList<>(entidad.getPeriodos_academico()));
        }
        if (entidad.getNombres_procesos() != null) {
            estadistica.setNombres_procesos(new ArrayList<>(entidad.getNombres_procesos()));
        }
        if (entidad.getNombres_programas() != null) {
            estadistica.setNombres_programas(new ArrayList<>(entidad.getNombres_programas()));
        }
        return estadistica;
    }

    public static EstadisticaEntity aEntidad(Estadistica estadistica) {
        Origen.exigir(estadistica);
        EstadisticaEntity entidad = new EstadisticaEntity();
        entidad.setId_estadistica(estadistica.getId_estadistica());
        entidad.setNombre(estadistica.getNombre());
        entidad.setTotal_solicitudes(estadistica.getTotal_solicitudes());
        entidad.setTotal_aprobadas(estadistica.getTotal_aprobadas());
        entidad.setTotal_rechazadas(estadistica.getTotal_rechazadas());
        if (estadistica.getPeriodos_academico() != null) {
            entidad.setPeriodos_academico(new ArrayList<>(estadistica.getPeriodos_academico()));
        }
        if (estadistica.getNombres_procesos() != null) {
            entidad.setNombres_procesos(new ArrayList<>(estadistica.getNombres_procesos()));
        }
        if (estadistica.getNombres_programas() != null) {
            entidad.setNombres_programas(new ArrayList<>(estadistica.getNombres_programas()));
        }
        return entidad;
    }
}
