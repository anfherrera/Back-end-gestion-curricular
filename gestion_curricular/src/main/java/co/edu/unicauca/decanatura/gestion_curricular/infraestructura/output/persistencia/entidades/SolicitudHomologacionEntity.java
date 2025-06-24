package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Solicitudes_Homogolacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacionEntity extends SolicitudEntity {
    @Column(nullable = false, length = 100)
    private String ruta_PM_FO_4_FOR_27;
    @Column(length = 100)
    private String ruta_contenido_programatico; //Es nulleable
}
