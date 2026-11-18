package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.RechazoCambioEstadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

/**
 * Verifica que un cambio de estado sea un paso válido del flujo y que lo haga el rol responsable
 * de ese paso (el Administrador puede hacer cualquier paso válido).
 */
@Component
@RequiredArgsConstructor
public class ValidadorCambioEstado {

    private static final String ADMINISTRADOR = "Administrador";

    private final Autorizacion autorizacion;

    /**
     * @return null si el cambio está permitido; si no, 400 (sin estado nuevo o paso no permitido) o 403 (rol
     *         incorrecto)
     */
    public ResponseEntity<RechazoCambioEstadoDTORespuesta> validar(FlujoEstadosSolicitud flujo, String estadoActual,
            String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "El nuevo estado es requerido", "ESTADO_REQUERIDO", flujo,
                    estadoActual);
        }
        String rolResponsable;
        try {
            rolResponsable = flujo.validarYObtenerRolResponsable(estadoActual, nuevoEstado);
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage(), "TRANSICION_NO_PERMITIDA", flujo, estadoActual);
        }

        if (!autorizacion.tieneRol(rolResponsable) && !autorizacion.tieneRol(ADMINISTRADOR)) {
            return error(HttpStatus.FORBIDDEN, "Este paso le corresponde al rol " + rolResponsable,
                    "ROL_NO_AUTORIZADO", flujo, estadoActual);
        }
        return null;
    }

    private static ResponseEntity<RechazoCambioEstadoDTORespuesta> error(HttpStatus estado, String mensaje,
            String codigo, FlujoEstadosSolicitud flujo, String estadoActual) {
        return ResponseEntity.status(estado)
                .body(new RechazoCambioEstadoDTORespuesta(mensaje, codigo, flujo.normalizar(estadoActual)));
    }
}
