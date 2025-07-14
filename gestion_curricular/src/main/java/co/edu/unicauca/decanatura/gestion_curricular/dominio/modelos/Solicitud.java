package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Solicitud {

    
    private Integer id_solicitud;
    private String nombre_solicitud;
    private Date fecha_registro_solicitud;

    private List<EstadoSolicitud> estadosSolicitud;

    private Usuario objUsuario;

    private List<Documento> documentos;

    public Solicitud () {
        this.documentos = new ArrayList<Documento>();
        this.estadosSolicitud = new ArrayList<EstadoSolicitud>();
    }

    
}
