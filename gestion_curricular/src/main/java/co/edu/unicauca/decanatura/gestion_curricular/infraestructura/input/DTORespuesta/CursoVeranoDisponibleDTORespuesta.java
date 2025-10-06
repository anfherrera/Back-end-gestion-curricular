package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursoVeranoDisponibleDTORespuesta {
    
    private Integer id_curso;
    private String nombre_curso;
    private String codigo_curso;
    private String descripcion;
    private LocalDateTime fecha_inicio;
    private LocalDateTime fecha_fin;
    private Integer cupo_maximo;
    private Integer cupo_disponible;
    private Integer cupo_estimado;
    private String espacio_asignado;
    private String estado;
    private MateriaDTORespuesta objMateria;
    private DocenteDTORespuesta objDocente;
}
