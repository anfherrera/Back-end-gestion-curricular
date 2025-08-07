package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class documentoDto {
   private Integer id_documento;
    private String nombre;
    private String ruta_documento;
    private Date fecha_documento;
    private boolean esValido;
    private String comentario;

    private String tipoDocumentoSolicitudPazYSalvo;
    private Integer idSolicitud; // opcional, depende del flujo

}
