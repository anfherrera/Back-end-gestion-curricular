package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EstudiantesCursoVeranoDTORespuesta(
        Integer idCurso, String nombreCurso, String codigoCurso, Integer cupoEstimado,
        int totalEstudiantes, int totalPreinscritos, int totalInscritos, List<Estudiante> estudiantes) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Estudiante(Integer idUsuario, String nombreCompleto, String codigo, String correo,
                             String estadoPreinscripcion, String estadoInscripcion, Integer idSolicitudInscripcion,
                             String tipo, @JsonInclude(JsonInclude.Include.NON_NULL) String condicion,
                             @JsonUnwrapped Programa programa, @JsonUnwrapped Preinscripcion preinscripcion,
                             @JsonUnwrapped InscripcionSinPreinscripcion inscripcionSinPreinscripcion) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Programa(Integer idPrograma, String programa) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Preinscripcion(Date fechaPreinscripcion, Integer idSolicitudPreinscripcion, boolean tieneInscripcion) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record InscripcionSinPreinscripcion(Date fechaInscripcion, boolean tienePreinscripcion) {
    }
}
