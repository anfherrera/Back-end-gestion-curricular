package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class usuarioSalidaDto {
    private Integer id_usuario;
    private String nombre_completo;
    private String codigo;
    private String correo;
    private boolean estado_usuario;

    private String nombreRol;
    private String nombrePrograma;
}
