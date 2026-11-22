package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    private Integer idDocumento;
    private String nombre;
    private String rutaDocumento;
    private Date fechaDocumento;
    private boolean esValido;
    private String comentario; // observacion
    /** Usuario que subió el archivo. Mientras el documento no tenga solicitud, solo se asocia a solicitudes de este usuario. */
    private Integer idUsuarioSubio;

    private Solicitud objSolicitud;
    
}
