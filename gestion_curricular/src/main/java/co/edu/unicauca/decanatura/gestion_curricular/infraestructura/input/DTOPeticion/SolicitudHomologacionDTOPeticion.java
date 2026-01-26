package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacionDTOPeticion extends SolicitudDTOPeticion {
        public SolicitudHomologacionDTOPeticion() {
        super(); // Llama al constructor de la clase base (SolicitudDTOPeticion)
    }

    // Constructor con parámetros para todos los campos de la clase base y de esta clase
    public SolicitudHomologacionDTOPeticion(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, String periodo_academico, Date fecha_ceremonia,
                                             EstadoSolicitudDTOPeticion estado_actual, UsuarioDTOPeticion objUsuario, List<DocumentosDTOPeticion> documentos) {
        // Llamamos al constructor de la clase base (orden: id, nombre, fecha, periodo, fecha_ceremonia, estado, usuario, documentos)
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, fecha_ceremonia, estado_actual, objUsuario, documentos);
    }

    @Size(max = 200, message = "{SolicitudHomologacion.programa_origen.length}")
    private String programa_origen;

    @Size(max = 200, message = "{SolicitudHomologacion.programa_destino.length}")
    private String programa_destino;
}
