package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Catálogos de los formularios de cursos de verano (CatalogosCursosVeranoRestController). El orden de las claves
 * es el que producía el {@code HashMap} anterior. Los salones usan {@link CursosVeranoDTORespuesta.SalonInfo},
 * que escribe las claves en el mismo orden.
 */
public final class CatalogosVeranoDTORespuesta {
    private CatalogosVeranoDTORespuesta() {}

    /** Elemento de {@code /materias-disponibles}. */
    @JsonPropertyOrder({"descripcion", "codigo", "id_materia", "creditos", "nombre"})
    public record MateriaDisponible(
            @JsonProperty("id_materia") Integer idMateria,
            String codigo,
            String nombre,
            Integer creditos,
            String descripcion) {}

    /** Elemento de {@code /materias}. */
    @JsonPropertyOrder({"nombre_materia", "codigo_materia", "id_materia", "creditos"})
    public record MateriaCatalogo(
            @JsonProperty("id_materia") Integer idMateria,
            @JsonProperty("nombre_materia") String nombreMateria,
            @JsonProperty("codigo_materia") String codigoMateria,
            Integer creditos) {}

    /** Elemento de {@code /materias-filtro}; la opción "Todas las materias" lleva id 0. */
    @JsonPropertyOrder({"codigo", "id", "nombre"})
    public record MateriaFiltro(Integer id, String nombre, String codigo) {}

    /** Elemento de {@code /docentes}; repite id, código y nombre con las claves de usuario. */
    @JsonPropertyOrder({"id_usuario", "codigo_docente", "nombre_docente", "id_docente", "codigo_usuario",
        "nombre_usuario"})
    public record DocenteCatalogo(
            @JsonProperty("id_docente") Integer idDocente,
            @JsonProperty("codigo_docente") String codigoDocente,
            @JsonProperty("nombre_docente") String nombreDocente,
            @JsonProperty("id_usuario") Integer idUsuario,
            @JsonProperty("codigo_usuario") String codigoUsuario,
            @JsonProperty("nombre_usuario") String nombreUsuario) {}
}
