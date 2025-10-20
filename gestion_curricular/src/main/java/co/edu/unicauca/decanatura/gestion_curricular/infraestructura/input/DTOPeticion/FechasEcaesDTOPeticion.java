package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.sql.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FechasEcaesDTOPeticion {
    private Integer idFechaEcaes;
    private String periodoAcademico; // Ejemplo: "2025-2"
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date inscripcion_est_by_facultad;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registro_recaudo_ordinario;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registro_recaudo_extraordinario;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date citacion;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date aplicacion;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date resultados_individuales;
}
