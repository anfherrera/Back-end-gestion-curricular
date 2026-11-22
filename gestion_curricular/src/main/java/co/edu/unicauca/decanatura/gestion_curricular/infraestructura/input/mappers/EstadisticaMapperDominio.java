package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.EstadisticaDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstadisticaDTORespuesta;

@Mapper(componentModel = "spring")
public interface EstadisticaMapperDominio {

    // El constructor del dominio inicializa las listas: asignarlas también cuando vienen null.
    @Mapping(target = "periodosAcademico", expression = "java(peticion.getPeriodos_academico())")
    @Mapping(target = "nombresProcesos", expression = "java(peticion.getNombres_procesos())")
    @Mapping(target = "nombresProgramas", expression = "java(peticion.getNombres_programas())")
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "idEstadistica", source = "id_estadistica")
    @Mapping(target = "totalSolicitudes", source = "total_solicitudes")
    @Mapping(target = "totalAprobadas", source = "total_aprobadas")
    @Mapping(target = "totalRechazadas", source = "total_rechazadas")
    Estadistica mapearDeEstadisticaDTOPeticionAEstadistica(EstadisticaDTOPeticion peticion);

    // Dominio -> DTO de respuesta
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "id_estadistica", source = "idEstadistica")
    @Mapping(target = "total_solicitudes", source = "totalSolicitudes")
    @Mapping(target = "total_aprobadas", source = "totalAprobadas")
    @Mapping(target = "total_rechazadas", source = "totalRechazadas")
    @Mapping(target = "periodos_academico", source = "periodosAcademico")
    @Mapping(target = "nombres_procesos", source = "nombresProcesos")
    @Mapping(target = "nombres_programas", source = "nombresProgramas")
    EstadisticaDTORespuesta mapearDeEstadisticaAEstadisticaDTORespuesta(Estadistica estadistica);

}
