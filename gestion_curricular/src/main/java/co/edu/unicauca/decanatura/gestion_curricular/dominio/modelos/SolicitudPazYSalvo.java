package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvo extends Solicitud {

    private String ruta_PM_FO_4_FOR_27;
    private String ruta_autorizacion_publicar;
    private String ruta_resultados_pruebas;
    private String ruta_formato_TI_G;
    private String ruta_formato_PP_H;
    private String ruta_comprobante_pago;
    private String rute_trabajo_grado;
}
