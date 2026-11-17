package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolDTORespuesta {
    private Integer id_rol;
    private String nombre;
}
