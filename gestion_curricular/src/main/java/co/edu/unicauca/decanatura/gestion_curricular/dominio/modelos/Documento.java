package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumentoSolicitudPazYSalvo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    private Integer id_documento;
    private String nombre;
    private String ruta_documento;
    private Date fecha_documento;
    private boolean esValido;
    private String comentario; // observacion

    private TipoDocumentoSolicitudPazYSalvo tipoDocumentoSolicitudPazYSalvo;

    private Solicitud objSolicitud;
    
}
