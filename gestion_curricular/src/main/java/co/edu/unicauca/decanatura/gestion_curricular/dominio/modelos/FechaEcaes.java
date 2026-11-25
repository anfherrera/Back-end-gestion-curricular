package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FechaEcaes {
    private Integer idFechaEcaes;
    private String periodoAcademico; // Ejemplo: "2025-2"
    private LocalDate inscripcionEstByFacultad;
    private LocalDate registroRecaudoOrdinario;
    private LocalDate registroRecaudoExtraordinario;
    private LocalDate citacion;
    private LocalDate aplicacion;
    private LocalDate resultadosIndividuales;

}
