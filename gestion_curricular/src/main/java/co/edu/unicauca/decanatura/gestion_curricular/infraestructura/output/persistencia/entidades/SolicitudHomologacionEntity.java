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
    //validar si estos atributos son necesarios al crear la solicitud
    @Column(nullable = true)
    private String ruta_PM_FO_4_FOR_27;

    @Column(nullable = true)
    private String ruta_contenido_programatico;

    @Column(nullable = true, length = 200)
    private String programa_origen;

    @Column(nullable = true, length = 200)
    private String programa_destino;
}
