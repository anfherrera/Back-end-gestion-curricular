package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FechaEcaes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FechaEcaesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFechaEcaes")
    private Integer id_fecha_ecaes;
    @Column(nullable = false)
    private String periodoAcademico; // Ejemplo: "2025-2"
    @Column(nullable = false)
    private LocalDate inscripcion_est_by_facultad;
    @Column(nullable = false)
    private LocalDate registro_recaudo_ordinario;
    @Column(nullable = false)
    private LocalDate registro_recaudo_extraordinario;
    @Column(nullable = false)
    private LocalDate citacion;
    @Column(nullable = false)
    private LocalDate aplicacion;
    @Column(nullable = false)
    private LocalDate resultados_individuales;

}
