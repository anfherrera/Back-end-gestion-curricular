package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import lombok.RequiredArgsConstructor;

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
     * @return null si el cambio está permitido; si no, 400 (paso no permitido) o 403 (rol incorrecto)
     */
    public ResponseEntity<Map<String, Object>> validar(FlujoEstadosSolicitud flujo, String estadoActual,
            String nuevoEstado) {
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

    private static ResponseEntity<Map<String, Object>> error(HttpStatus estado, String mensaje, String codigo,
            FlujoEstadosSolicitud flujo, String estadoActual) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("error", mensaje);
        cuerpo.put("codigo", codigo);
        cuerpo.put("estadoActual", flujo.normalizar(estadoActual));
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
