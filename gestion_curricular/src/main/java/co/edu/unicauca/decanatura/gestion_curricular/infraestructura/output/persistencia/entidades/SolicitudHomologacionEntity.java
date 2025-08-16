package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;
import java.util.List;

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
    //validar si estos atributos son necesarios al crear la solicitud
    @Column(nullable = true)
    private String ruta_PM_FO_4_FOR_27;

    @Column(nullable = true)
    private String ruta_contenido_programatico;
}
