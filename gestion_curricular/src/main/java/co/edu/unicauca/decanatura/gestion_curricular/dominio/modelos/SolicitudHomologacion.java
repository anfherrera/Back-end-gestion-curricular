package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacion extends Solicitud {
    private String rutaPmFo4For27;
    private String rutaContenidoProgramatico;
    private String programaOrigen;
    private String programaDestino;
}
