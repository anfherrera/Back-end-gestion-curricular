package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Rol {
    private Integer id_rol;
    private String nombre;
    private List<Usuario> usuarios;
    public Rol(){
        this.usuarios = new ArrayList<Usuario>();
    }
}
