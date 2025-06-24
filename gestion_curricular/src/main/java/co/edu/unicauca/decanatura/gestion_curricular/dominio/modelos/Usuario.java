package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Integer id_usuario;
    private String nombre_completo;
    private Rol objRol;
    private String codigo;
    private String correo;
    private String password;
    private boolean estado_usuario;
    private Programa objPrograma;
    
}
