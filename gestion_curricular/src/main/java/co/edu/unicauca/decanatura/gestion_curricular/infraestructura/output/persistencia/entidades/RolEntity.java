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
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "Roles")
@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"usuarios"})
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private Integer id_rol;
    @Column(nullable = false, length = 100)
    private String nombre;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST }, mappedBy = "objRol")
    private List<UsuarioEntity> usuarios;

    public RolEntity(){
        this.usuarios = new ArrayList<UsuarioEntity>();
    }
}
