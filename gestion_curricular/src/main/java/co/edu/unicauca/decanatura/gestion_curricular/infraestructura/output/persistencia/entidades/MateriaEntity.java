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
@Table(name = "Materias")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MateriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMateria")
    private Integer id_materia;
    @Column(nullable = false, unique =  true, length = 12)
    private String codigo;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false,length = 2) 
    private Integer creditos;
    
}
