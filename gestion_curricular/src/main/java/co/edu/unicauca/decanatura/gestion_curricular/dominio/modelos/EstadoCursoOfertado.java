package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor

public class EstadoCursoOfertado {
    private Integer id_estado;
    private String estado_actual;
    private Date fecha_registro_estado;

    private CursoOfertadoVerano objCursoOfertadoVerano;

    public EstadoCursoOfertado(){
        this.estado_actual = "Abierto";
    }
}
