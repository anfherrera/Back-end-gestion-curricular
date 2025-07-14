package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTORespuesta {

    private Integer id_usuario;
    private String nombre_completo;
    private RolDTORespuesta rol;
    private String codigo;
    private String correo;
    private boolean estado_usuario;
    private ProgramaDTORespuesta objPrograma;
}
