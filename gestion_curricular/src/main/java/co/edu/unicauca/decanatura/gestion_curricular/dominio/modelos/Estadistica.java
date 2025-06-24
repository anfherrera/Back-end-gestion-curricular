package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estadistica {
    private Integer id_estadistica;
    private String nombre;
    private Date periodo_academico; //Solo usar año con uno 1 y 2 correspondientes a los 6 meses
    private Integer total_solicitudes;
    private Integer total_aprobadas;
    private Integer total_rechazadas;

    private Solicitud objSolicitud;
    private Programa objPrograma;

    
}
