package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto;
import lombok.Data;
import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumento;


@Data
public class solicitudEcaesDto {
    private TipoDocumento tipoDocumento;
    private String numero_documento;
    private Date fecha_expedicion;
}
