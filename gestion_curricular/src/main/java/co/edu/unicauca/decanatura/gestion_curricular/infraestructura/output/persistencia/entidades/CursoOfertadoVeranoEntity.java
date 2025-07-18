package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "Cursos_ofertados")
@Data
@AllArgsConstructor
public class CursoOfertadoVeranoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCurso")
    private Integer id_curso;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrupoCursoVeranoEntity grupo;
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

    @OneToMany( mappedBy = "objCursoOfertadoVerano", cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<EstadoCursoOfertadoEntity> estadosCursoOfertados; // Estado del curso ofertado
    
    @ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "cursosEstudiantes", joinColumns = @JoinColumn(name = "idCurso"), inverseJoinColumns = @JoinColumn(name = "idUsuario"))
    private Set<UsuarioEntity> estudiantesInscritos; // Lista de estudiantes inscritos en el curso


    @OneToMany(mappedBy = "objCursoOfertadoVerano", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private List<SolicitudEntity> solicitudes; // Lista de solicitudes de estudiantes para el curso

    public CursoOfertadoVeranoEntity() {
        this.estudiantesInscritos = new HashSet<UsuarioEntity>();
        this.estadosCursoOfertados = new ArrayList<EstadoCursoOfertadoEntity>();
        this.solicitudes = new ArrayList<SolicitudEntity>();
    }
}
