package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;


@Entity
@Table(name = "EstadosCursos")
@Data
@AllArgsConstructor
public class EstadoCursoOfertadoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstado")
    private Integer id_estado;
    @Column(nullable = false, length = 100)
    private String estado_actual;
    //@Column(nullable = false)
    private Date fecha_registro_estado; // Fecha de inicio del curso
    @Column(nullable = true)
    private Date fecha_fin; // Fecha de fin del curso (opcional)
    @Column(nullable = true, length = 10)
    private String periodo_academico; // Período académico proporcionado por el usuario (ej: "2025-1", "2025-2")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfkCurso", referencedColumnName = "idCurso", nullable = true)
    private CursoOfertadoVeranoEntity objCursoOfertadoVerano;

    public EstadoCursoOfertadoEntity(){
        this.estado_actual = "Abierto";
    }
}
