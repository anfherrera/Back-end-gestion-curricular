package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;


@Entity
@Table(name = "Usuarios")
@Data
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
    
    @ManyToOne
    @JoinColumn(name = "idfkRol", referencedColumnName = "idRol", nullable = false)
    private RolEntity objRol;

    @ManyToOne
    @JoinColumn(name = "idfkPrograma", referencedColumnName = "idPrograma", nullable = false)
    private ProgramaEntity objPrograma;


    @OneToMany(mappedBy = "objUsuario", orphanRemoval = true)
	private List<SolicitudEntity> solicitudes;

    @ManyToMany(mappedBy = "estudiantesInscritos")
    private List<CursoOfertadoVeranoEntity> cursosOfertadosInscritos;

    public UsuarioEntity(){
        this.solicitudes = new ArrayList<SolicitudEntity>();
        this.cursosOfertadosInscritos = new ArrayList<CursoOfertadoVeranoEntity>();
    }
    
}
