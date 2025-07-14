package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaDTORespuesta {
    private Integer id_estadistica;
    private String nombre;
    private Integer total_solicitudes;
    private Integer total_aprobadas;
    private Integer total_rechazadas;

    private List<Date> periodos_academico; // Ejemplo: 2023-01, 2023-02
    private List<String> nombres_procesos;
    private List<String> nombres_programas;
}
