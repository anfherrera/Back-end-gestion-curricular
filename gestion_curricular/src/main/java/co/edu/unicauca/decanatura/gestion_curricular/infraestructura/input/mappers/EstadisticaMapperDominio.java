package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.EstadisticaDTOPeticion;


import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadisticaDTORespuesta;

@Mapper(componentModel = "spring")
public interface EstadisticaMapperDominio {

    // El constructor del dominio inicializa las listas: asignarlas también cuando vienen null.
    @Mapping(target = "periodos_academico", expression = "java(peticion.getPeriodos_academico())")
    @Mapping(target = "nombres_procesos", expression = "java(peticion.getNombres_procesos())")
    @Mapping(target = "nombres_programas", expression = "java(peticion.getNombres_programas())")
    Estadistica mappearDeEstadisticaDTOPeticionAEstadistica(EstadisticaDTOPeticion peticion);

    // Dominio -> DTO de respuesta
    EstadisticaDTORespuesta mappearDeEstadisticaAEstadisticaDTORespuesta(Estadistica estadistica);

}
