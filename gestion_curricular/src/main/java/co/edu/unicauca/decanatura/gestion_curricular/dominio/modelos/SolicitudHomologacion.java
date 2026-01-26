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
    private String ruta_PM_FO_4_FOR_27;
    private String ruta_contenido_programatico;
    private String programa_origen;
    private String programa_destino;
}
