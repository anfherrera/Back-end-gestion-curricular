package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Cuerpo de {@code validar-pago}: {@code esValido} (obligatorio) y {@code observaciones}.
 * Los valores se leen sin coerción, como antes: un tipo distinto de booleano o de texto lanza
 * {@link ClassCastException} al leer el campo (respuesta 500, BUG-19), no al deserializar el cuerpo.
 */
public final class ValidacionPagoDTOPeticion {
    private final Map<String, Object> campos;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ValidacionPagoDTOPeticion(Map<String, Object> campos) {
        this.campos = campos;
    }

    public Boolean getEsValido() { return (Boolean) campos.get("esValido"); }

    public String getObservaciones() { return (String) campos.get("observaciones"); }
}
