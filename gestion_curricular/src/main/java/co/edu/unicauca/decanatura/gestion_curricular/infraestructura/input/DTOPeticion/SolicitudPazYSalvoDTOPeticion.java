package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvoDTOPeticion extends SolicitudDTOPeticion {
    public SolicitudPazYSalvoDTOPeticion() {
        super(); // Llama al constructor de la clase base (SolicitudDTOPeticion)
    }

    // Constructor con parámetros para todos los campos de la clase base y de esta clase
    public SolicitudPazYSalvoDTOPeticion(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud,
                                             String estado_actual, UsuarioDTOPeticion objUsuario, List<DocumentosDTOPeticion> documentos) {
        // Llamamos al constructor de la clase base
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, estado_actual, objUsuario, documentos);
    }  
}
