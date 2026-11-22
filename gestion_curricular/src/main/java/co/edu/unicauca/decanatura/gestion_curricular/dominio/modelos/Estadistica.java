package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Estadistica {
    private Integer idEstadistica;
    private String nombre;
    private Integer totalSolicitudes;
    private Integer totalAprobadas;
    private Integer totalRechazadas;

    private List<Date> periodosAcademico; //Solo usar año con uno 1 y 2 correspondientes a los 6 meses
    private List<String> nombresProcesos;
    private List<String> nombresProgramas;


        public Estadistica() {
        this.periodosAcademico = new ArrayList<Date>();
        this.nombresProcesos = new ArrayList<String>();
        this.nombresProgramas = new ArrayList<String>();   
    }
}


