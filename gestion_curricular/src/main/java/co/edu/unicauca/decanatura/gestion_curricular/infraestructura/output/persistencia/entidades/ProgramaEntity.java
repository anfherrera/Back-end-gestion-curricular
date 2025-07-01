package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "Programas")
@Data
@AllArgsConstructor
public class ProgramaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPrograma")
    private Integer id_programa;
    @Column(nullable = false, unique = true, length = 12)
    private String codigo;
    @Column(nullable = false, unique = true, length = 100)
    private String nombre_programa;

    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST }, mappedBy = "objPrograma")
    private List<UsuarioEntity> usuarios;

    public ProgramaEntity (){
        this.usuarios = new ArrayList<UsuarioEntity>();
    }
}
