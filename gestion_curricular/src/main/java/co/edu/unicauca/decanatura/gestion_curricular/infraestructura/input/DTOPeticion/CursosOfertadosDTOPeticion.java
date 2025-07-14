package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursosOfertadosDTOPeticion {

    private Integer id_curso;

    @NotNull(message = "{Curso.objMateria.empty}")
    @Valid
    private MateriaDTOPeticion objMateria;

    @NotNull(message = "{Curso.objDocente.empty}")
    @Valid
    private DocenteDTOPeticion objDocente;

    @NotBlank(message = "{Curso.grupo.empty}")
    @Size(min = 1, max = 5, message = "{Curso.grupo.length}")
    private String grupo;

    @NotNull(message = "{Curso.cupoEstimado.empty}")
    @Min(value = 1, message = "{Curso.cupoEstimado.min}")
    private Integer cupo_estimado;

    @NotBlank(message = "{Curso.salon.empty}")
    @Size(min = 2, max = 20, message = "{Curso.salon.length}")
    private String salon;

    
    private EstadoCursoOfertadoDTOPeticion EstadoCursoOfertado;

    @Valid
    private Set<UsuarioDTOPeticion> estudiantesInscritos;
}
