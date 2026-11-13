package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaDTOPeticion {

    private Integer id_estadistica; 

    private String nombre; 

    private Integer total_solicitudes;
    private Integer total_aprobadas;
    private Integer total_rechazadas;
    private List<Date> periodos_academico = new ArrayList<>();
    private List<String> nombres_procesos = new ArrayList<>();
    private List<String> nombres_programas = new ArrayList<>();

}
