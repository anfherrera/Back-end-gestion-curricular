package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.EstadisticaDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadisticaDTORespuesta;

@Mapper(componentModel = "spring")
public interface EstadisticaMapperDominio {

    @Mapping(target = "total_solicitudes", ignore = true)
    @Mapping(target = "total_aprobadas", ignore =  true)
    @Mapping(target =  "total_rechazadas", ignore =  true)
    @Mapping(target =  "periodos_academico", ignore =  true)
    @Mapping(target = "nombres_procesos", ignore =  true)
    @Mapping(target =  "nombres_programas", ignore = true)
    Estadistica mappearEstadisticaDTOADominio(EstadisticaDTOPeticion peticion);

    // Dominio -> DTO de respuesta
    EstadisticaDTORespuesta mappearDeEstadisticaAEstadisticaDTORespuesta(Estadistica estadistica);

    List<EstadisticaDTORespuesta> mappearDeEstadisticasAEstadisticasDTORespuesta(List<Estadistica> lista);
}
