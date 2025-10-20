package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FechaEcaesDTORespuesta {
    private Integer idFechaEcaes;
    private String periodoAcademico; // Ejemplo: "2025-2"
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date inscripcion_est_by_facultad;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date registro_recaudo_ordinario;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date registro_recaudo_extraordinario;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date citacion;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date aplicacion;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date resultados_individuales;
}
