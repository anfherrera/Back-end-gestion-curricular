package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones;

import java.util.function.Supplier;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/**
 * Traducción histórica de errores al crear usuarios (BUG-22): solo {@link IllegalArgumentException} llega
 * como tal al manejador global (400); cualquier otro error, incluidos los de dominio, se envuelve en una
 * {@link RuntimeException} genérica y el manejador global responde 500.
 */
public final class ErroresUsuario {
    private ErroresUsuario() {}

    public static <T> T alCrear(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage(), e);
        }
    }

    /** Errores del mapeo de la respuesta, incluido {@link IllegalArgumentException}, como error genérico. */
    public static <T> T alMapearCreado(Supplier<T> mapeo, Usuario usuarioCreado) {
        try {
            return mapeo.get();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("source cannot be null")) {
                throw new RuntimeException("Error al mapear usuario a DTO: "
                        + "Rol: " + (usuarioCreado.getObjRol() != null ? "presente" : "null")
                        + ", Programa: " + (usuarioCreado.getObjPrograma() != null ? "presente" : "null"), e);
            }
            throw new RuntimeException("Error al mapear el usuario creado a DTO de respuesta: " + e.getMessage(), e);
        }
    }
}
