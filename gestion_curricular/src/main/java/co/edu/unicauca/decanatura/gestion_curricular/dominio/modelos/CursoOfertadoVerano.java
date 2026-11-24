package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;


import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;

@Data
@AllArgsConstructor
public class CursoOfertadoVerano {
    private Integer idCurso;
    private Materia objMateria;
    private Docente objDocente;
    private GrupoCursoVerano grupo;
    private Integer cupoEstimado; // Valor mínimo para abrir un curso
    private String salon;
    private java.util.Date fecha_inicio; // Fecha de inicio del curso
    private java.util.Date fecha_fin; // Fecha de fin del curso (opcional)
    private String periodoAcademico; // Período académico (ej: "2025-1", "2025-2")
    
    private List<EstadoCursoOfertado> estadosCursoOfertados; // Estado del curso ofertado
    private List<Usuario> estudiantesInscritos; // Lista de estudiantes inscritos en el curso

    private List<Solicitud> solicitudes;
    
    public CursoOfertadoVerano() {
        this.estudiantesInscritos = new ArrayList<Usuario>();
        this.estadosCursoOfertados = new ArrayList<EstadoCursoOfertado>();
        this.solicitudes = new ArrayList<Solicitud>();
    }

    /** Último registro del historial de estados del curso, o null si no hay lista o está vacía. */
    public EstadoCursoOfertado ultimoRegistroEstado() {
        if (estadosCursoOfertados == null || estadosCursoOfertados.isEmpty()) {
            return null;
        }
        return estadosCursoOfertados.get(estadosCursoOfertados.size() - 1);
    }

    /**
     * Texto del último registro del historial de estados, o null si no hay ninguno. Si el último registro no
     * tiene texto, también es null. A diferencia de {@link TransicionEstadoCurso#estadoActual}, no da
     * {@link TransicionEstadoCurso#SIN_ESTADO}.
     */
    public String estadoActual() {
        EstadoCursoOfertado ultimo = ultimoRegistroEstado();
        return ultimo == null ? null : ultimo.getEstadoActual();
    }

    /** true si el estado actual deja ver el curso a los estudiantes: Publicado, Preinscripcion o Inscripcion. */
    public boolean esVisibleParaEstudiantes() {
        String estado = estadoActual();
        return "Publicado".equals(estado) || "Preinscripcion".equals(estado) || "Inscripcion".equals(estado);
    }

    /** Un curso está completo si tiene materia, docente, cupo positivo y salón. */
    public boolean estaCompleto() {
        return objMateria != null
                && objDocente != null
                && cupoEstimado != null
                && cupoEstimado > 0
                && salon != null
                && !salon.trim().isEmpty();
    }
}
