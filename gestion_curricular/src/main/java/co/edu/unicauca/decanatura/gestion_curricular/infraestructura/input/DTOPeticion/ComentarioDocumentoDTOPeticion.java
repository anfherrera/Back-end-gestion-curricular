package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioDocumentoDTOPeticion {
    private Integer idDocumento;
    private String comentario;
}
