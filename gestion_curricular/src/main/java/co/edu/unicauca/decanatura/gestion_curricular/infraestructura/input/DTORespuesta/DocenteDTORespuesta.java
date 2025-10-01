package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocenteDTORespuesta {
    private Integer id_docente;
    private String codigo_docente;
    private String nombre_docente;
    
    // Campos que espera el frontend
    private Integer id_usuario;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private RolDTORespuesta objRol;
}
