package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;


import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVerano;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Cursos_ofertados")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursoOfertadoVeranoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCurso")
    private Integer id_curso;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrupoCursoVerano grupo;
    @Column(nullable = false, length = 2)
    private Integer cupo_estimado; // Valor mínimo para abrir un curso
    @Column(nullable = false, length = 100)
    private String salon;
    
    @OneToOne
    @JoinColumn(name = "idfkMateria", referencedColumnName = "idMateria", nullable = false)
    private MateriaEntity objMateria;
    @OneToOne
    @JoinColumn(name = "idfkDocente", referencedColumnName = "idDocente", nullable = false)
    private DocenteEntity objDocente;
}
