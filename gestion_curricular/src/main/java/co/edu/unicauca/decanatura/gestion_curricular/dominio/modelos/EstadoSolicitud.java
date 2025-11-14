package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadoSolicitud {
    private Integer id_estado;
    private String estado_actual;
    private Date fecha_registro_estado;
    private String comentario;

    private Solicitud objSolicitud;

    public EstadoSolicitud(){
        this.estado_actual = "Enviado";
    }
    
}
