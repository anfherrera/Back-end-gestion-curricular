package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor

public class EstadoCursoOfertado {
    private Integer id_estado;
    private String estado_actual;
    private Date fecha_registro_estado; // Fecha de inicio del curso
    private Date fecha_fin; // Fecha de fin del curso (opcional, si no se proporciona se calcula)
    private String periodo_academico; // Período académico proporcionado por el usuario (ej: "2025-1", "2025-2")

    private CursoOfertadoVerano objCursoOfertadoVerano;

    public EstadoCursoOfertado(){
        this.estado_actual = "Abierto";
    }
}
