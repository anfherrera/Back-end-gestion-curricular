package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Estadistica {
    private Integer id_estadistica;
    private String nombre;
    private Integer total_solicitudes;
    private Integer total_aprobadas;
    private Integer total_rechazadas;

    private List<Date> periodos_academico; //Solo usar año con uno 1 y 2 correspondientes a los 6 meses
    private List<String> nombres_procesos;
    private List<String> nombres_programas;


        public Estadistica() {
        this.periodos_academico = new ArrayList<Date>();
        this.nombres_procesos = new ArrayList<String>();
        this.nombres_programas = new ArrayList<String>();   
    }
}


