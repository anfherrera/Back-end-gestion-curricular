package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.MateriaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.MateriaDTOPeticion;

@Mapper(componentModel = "spring")
public interface MateriaMapperDominio {

    // Dominio -> DTO de respuesta
    MateriaDTORespuesta mappearDeMateriaAMateriaDTORespuesta(Materia materia);

    List<MateriaDTORespuesta> mappearDeMateriasAMateriasDTORespuesta(List<Materia> materias);

    // DTO de petición -> Dominio
    Materia mappearDeMateriaDTOPeticionAMateria(MateriaDTOPeticion peticion);

    List<Materia> mappearDeMateriaDTOPeticionAMaterias(List<MateriaDTOPeticion> peticiones);
}
