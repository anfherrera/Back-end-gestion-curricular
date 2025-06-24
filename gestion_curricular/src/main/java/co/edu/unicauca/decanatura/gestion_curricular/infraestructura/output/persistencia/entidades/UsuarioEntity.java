package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "Usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer id_usuario;

    @Column(nullable = false, length = 100)
    private String nombre_completo;
    @Column(nullable = false, length = 12)
    private String codigo;
    @Column(nullable = false, unique = true, length = 100)
    private String correo;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, columnDefinition = "TINYINT", length = 1)
    private boolean estado_usuario;
    @ManyToOne(cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "idfkRol", nullable = false)
    private RolEntity objRol;
    @ManyToOne(cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "idfkPrograma", nullable = false)
    private ProgramaEntity objPrograma;
    
}
