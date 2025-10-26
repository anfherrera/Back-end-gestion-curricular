package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.time.LocalDate;

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
    private LocalDate inscripcion_est_by_facultad;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate registro_recaudo_ordinario;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate registro_recaudo_extraordinario;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate citacion;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate aplicacion;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate resultados_individuales;
}
