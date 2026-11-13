package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Datos de una decisión sobre una solicitud. La conversión del mapa se limita a
 * la entrada HTTP para conservar las coerciones escalares de los clientes antiguos,
 * incluida la validación de los valores de claves desconocidas.
 * Las reglas de obligatoriedad y los textos por defecto dependen de cada operación.
 */
public final class DecisionSolicitudDTOPeticion {
    private final String comentarios;
    private final String motivo;
    private final String observaciones;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public DecisionSolicitudDTOPeticion(Map<String, String> campos) {
        comentarios = campos.get("comentarios");
        motivo = campos.get("motivo");
        observaciones = campos.get("observaciones");
    }

    public String getComentarios() { return comentarios; }
    public String getMotivo() { return motivo; }
    public String getObservaciones() { return observaciones; }
}
