package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor

public class EstadoCursoOfertado {
    private Integer idEstado;
    private String estadoActual;
    private Date fechaRegistroEstado; // Fecha en que se registró este estado (cuando cambió de estado)

    private CursoOfertadoVerano objCursoOfertadoVerano;

    public EstadoCursoOfertado(){
        this.estadoActual = "Abierto";
    }
}
