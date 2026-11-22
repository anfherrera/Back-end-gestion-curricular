package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Docente {
    private Integer idDocente;
    private String codigoDocente;
    private String nombreDocente;
}
