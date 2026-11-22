package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadoSolicitud {
    private Integer idEstado;
    private String estadoActual;
    private Date fechaRegistroEstado;
    private String comentario;

    private Solicitud objSolicitud;

    public EstadoSolicitud(){
        this.estadoActual = "Enviado";
    }
    
}
