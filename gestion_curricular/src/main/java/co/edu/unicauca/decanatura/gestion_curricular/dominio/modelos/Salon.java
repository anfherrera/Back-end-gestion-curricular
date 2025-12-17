package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Salon {
    private Integer id_salon;
    private String numero_salon; // Número del salón (ej: "221", "222", etc.)
    private String edificio; // Edificio donde está el salón (ej: "FIET")
    private Boolean activo; // Indica si el salón está disponible para uso
}









