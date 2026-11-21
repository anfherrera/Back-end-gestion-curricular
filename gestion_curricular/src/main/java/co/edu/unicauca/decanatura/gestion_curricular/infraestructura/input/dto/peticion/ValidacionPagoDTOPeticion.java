package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Cuerpo de {@code validar-pago}: {@code esValido} (obligatorio) y {@code observaciones}.
 * Los valores no se convierten (BUG-19): si {@code esValido} no es un booleano JSON u {@code observaciones} no es
 * texto, el cuerpo no se puede leer y la respuesta es el 400 {@code GC-0400} de cualquier cuerpo mal formado.
 * Un {@code esValido} ausente o null sigue llegando como null al controlador.
 */
public final class ValidacionPagoDTOPeticion {
    private final Boolean esValido;
    private final String observaciones;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ValidacionPagoDTOPeticion(Map<String, Object> campos) {
        this.esValido = leer(campos, "esValido", Boolean.class);
        this.observaciones = leer(campos, "observaciones", String.class);
    }

    private static <T> T leer(Map<String, Object> campos, String nombre, Class<T> tipo) {
        Object valor = campos.get(nombre);
        if (valor != null && !tipo.isInstance(valor)) {
            throw new IllegalArgumentException("'" + nombre + "' debe ser de tipo " + tipo.getSimpleName());
        }
        return tipo.cast(valor);
    }

    public Boolean getEsValido() { return esValido; }

    public String getObservaciones() { return observaciones; }
}
