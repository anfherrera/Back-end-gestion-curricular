package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Materia {
    private Integer idMateria;
    private String codigo;
    private String nombre; 
    private Integer creditos;
    
}
