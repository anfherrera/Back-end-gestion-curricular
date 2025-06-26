package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Solicitudes_PazYSalvo")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvoEntity extends SolicitudEntity {

    @Column(nullable = false, length = 100)
    private String ruta_PM_FO_4_FOR_27;
    @Column(nullable = false, length = 100)
    private String ruta_autorizacion_publicar;
    @Column(nullable = false, length = 100)
    private String ruta_resultados_pruebas;
    @Column(nullable = false, length = 100)
    private String ruta_formato_TI_G;
    @Column(nullable = false, length = 100)
    private String ruta_formato_PP_H;
    @Column(nullable = false, length = 100)
    private String ruta_comprobante_pago;
    @Column(nullable = false, length = 100)
    private String rute_trabajo_grado;
    @Column(columnDefinition = "TINYINT", length = 1)
    private boolean esValido;
    
}
