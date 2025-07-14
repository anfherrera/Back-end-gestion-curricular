package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadisticaDTORespuesta;

@Mapper(componentModel = "spring")
public interface EstadisticaMapperDominio {

    // Dominio -> DTO de respuesta
    EstadisticaDTORespuesta mappearDeEstadisticaAEstadisticaDTORespuesta(Estadistica estadistica);

    List<EstadisticaDTORespuesta> mappearDeEstadisticasAEstadisticasDTORespuesta(List<Estadistica> lista);
}
