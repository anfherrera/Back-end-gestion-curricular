package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.List;
import java.util.Map;

/** Matriz de operaciones por estado y rol de los cursos de verano. */
public final class PermisosCursoVerano {

    /** Operaciones de cada rol en cada estado; una lista vacía significa que el rol no tiene permisos. */
    private static final Map<String, Map<String, List<String>>> PERMISOS = Map.of(
        "Borrador", Map.of(
            "FUNCIONARIO", List.of("ver", "editar_completo", "eliminar", "cambiar_estado"),
            "COORDINADOR", List.of("ver", "editar_completo", "eliminar", "cambiar_estado"),
            "ESTUDIANTE", List.of()),
        "Abierto", Map.of(
            "FUNCIONARIO", List.of("ver", "editar_parcial", "cambiar_estado"),
            "COORDINADOR", List.of("ver", "editar_parcial", "cambiar_estado"),
            "ESTUDIANTE", List.of()),
        "Publicado", Map.of(
            "FUNCIONARIO", List.of("ver", "gestionar_solicitudes", "cambiar_estado"),
            "COORDINADOR", List.of("ver", "gestionar_solicitudes", "cambiar_estado"),
            "ESTUDIANTE", List.of("ver", "solicitar_curso_nuevo")),
        "Preinscripcion", Map.of(
            "FUNCIONARIO", List.of("ver", "gestionar_preinscripciones", "cambiar_estado"),
            "COORDINADOR", List.of("ver", "gestionar_preinscripciones", "cambiar_estado"),
            "ESTUDIANTE", List.of("ver", "preinscribirse")),
        "Inscripcion", Map.of(
            "FUNCIONARIO", List.of("ver", "gestionar_inscripciones", "cambiar_estado"),
            "COORDINADOR", List.of("ver", "gestionar_inscripciones", "cambiar_estado"),
            "ESTUDIANTE", List.of("ver", "inscribirse")),
        "Cerrado", Map.of(
            "FUNCIONARIO", List.of("ver", "consultar"),
            "COORDINADOR", List.of("ver", "consultar"),
            "ESTUDIANTE", List.of("ver", "consultar")));

    private PermisosCursoVerano() {
    }

    /**
     * Resultado de la consulta: {@code operaciones} son las que el rol puede hacer en el estado (vacía si
     * ninguna). {@code permitido} es true si hay alguna, y entonces {@code error} es null.
     */
    public record Consulta(boolean permitido, String error, String message, List<String> operaciones) {}

    /** Operaciones disponibles para {@code rolUsuario} en un curso en {@code estadoCurso} (BUG-15). */
    public static Consulta consultar(String estadoCurso, String rolUsuario) {
        Map<String, List<String>> porRol = PERMISOS.get(estadoCurso);
        if (porRol == null) {
            return new Consulta(false, "Estado desconocido",
                "No se pueden determinar permisos para el estado: " + estadoCurso, List.of());
        }
        List<String> operaciones = porRol.get(rolUsuario);
        if (operaciones == null) {
            return new Consulta(false, "Rol no valido", "No se encontraron permisos para el rol: " + rolUsuario,
                List.of());
        }
        if (operaciones.isEmpty()) {
            return new Consulta(false, "Sin permisos",
                "Este rol no tiene permisos para realizar operaciones en el estado: " + estadoCurso, List.of());
        }
        return new Consulta(true, null,
            "Operaciones permitidas para el rol " + rolUsuario + " en el estado " + estadoCurso, operaciones);
    }
}
