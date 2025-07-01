package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "Estadisticas")
@Data
@AllArgsConstructor
public class EstadisticaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadistica")
    private Integer id_estadistica;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false)
    private Integer total_solicitudes;
    private Integer total_aprobadas;
    private Integer total_rechazadas;

    private List<Date> periodos_academico; //Solo usar año con uno 1 y 2 correspondientes a los 6 meses
    private List<String> nombres_procesos;
    private List<String> nombres_programas;


    public EstadisticaEntity() {
        this.periodos_academico = new ArrayList<Date>();
        this.nombres_procesos = new ArrayList<String>();
        this.nombres_programas = new ArrayList<String>();
    }
    
}
