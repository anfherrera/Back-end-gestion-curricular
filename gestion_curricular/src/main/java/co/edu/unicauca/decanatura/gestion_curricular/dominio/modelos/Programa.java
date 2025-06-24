package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Programa {

    private Integer id_programa;
    private String codigo;
    private String nombre_programa;
    private List<Usuario> usuarios;
    public Programa (){
        this.usuarios = new ArrayList<Usuario>();
    }
}
