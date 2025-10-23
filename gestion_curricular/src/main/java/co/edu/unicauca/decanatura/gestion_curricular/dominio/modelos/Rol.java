package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rol {
    private Integer id_rol;
    private String nombre;
    private List<Usuario> usuarios;
}
