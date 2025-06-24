package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Solicitudes_Reingreso")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudReingresoEntity extends SolicitudEntity {

    @Column(nullable = false, length = 100)
    private String ruta_PM_FO_4_FOR_17;
    @Column(nullable = false, length = 100)
    private String ruta_pazysalvo_academico;
    @Column(nullable = false, length = 100)
    private String ruta_pazysalvo_financiero;
    
}
