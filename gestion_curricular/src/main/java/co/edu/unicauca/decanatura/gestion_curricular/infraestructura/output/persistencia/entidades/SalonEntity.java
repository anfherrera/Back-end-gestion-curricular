package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

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
@Table(name = "Salones")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSalon")
    private Integer id_salon;
    
    @Column(nullable = false, unique = true, length = 10)
    private String numero_salon; // Número del salón (ej: "221", "222", etc.)
    
    @Column(nullable = true, length = 100)
    private String edificio; // Edificio donde está el salón (ej: "FIET")
    
    @Column(nullable = true)
    private Boolean activo; // Indica si el salón está disponible para uso
    
}





