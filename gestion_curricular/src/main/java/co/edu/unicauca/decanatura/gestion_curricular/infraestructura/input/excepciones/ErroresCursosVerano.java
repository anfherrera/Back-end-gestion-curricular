package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;

/**
 * Contratos históricos de error de cursos de verano, distintos del error global (BUG-16).
 * Rodean solo la parte de la operación que traducían los controladores; el resto de excepciones
 * sigue hasta RestApiExceptionHandler, que también escribe estas respuestas.
 */
@Slf4j
public final class ErroresCursosVerano {
    private ErroresCursosVerano() {}

    /** Sin cupo: 409 con {@code error}, {@code codigo: CUPO_AGOTADO}, {@code cupo} y {@code ocupados} (DUP-2). */
    public static <T> T conCupo(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (CupoAgotadoException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("codigo", "CUPO_AGOTADO");
            error.put("cupo", e.getCupo());
            error.put("ocupados", e.getOcupados());
            throw new RespuestaHttpExcepcion(HttpStatus.CONFLICT, error, e);
        }
    }

    public static void conCupo(Runnable operacion) {
        conCupo(() -> {
            operacion.run();
            return null;
        });
    }

    /**
     * Argumento inválido al rechazar una inscripción: 400 con su mensaje. Las excepciones de dominio y los
     * errores inesperados siguen hasta RestApiExceptionHandler, que no envía al cliente el detalle de estos.
     */
    public static <T> T alRechazarInscripcion(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (IllegalArgumentException e) {
            log.error("Error en validarPago: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al rechazar la inscripcion: " + e.getMessage());
            throw new RespuestaHttpExcepcion(HttpStatus.BAD_REQUEST, error, e);
        }
    }

    /** Argumento inválido al crear una solicitud de curso nuevo: 400 con {@code tipo: VALIDATION_ERROR} (BUG-24). */
    public static <T> T alCrearCursoNuevo(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error de validacion: " + e.getMessage());
            error.put("tipo", "VALIDATION_ERROR");
            throw new RespuestaHttpExcepcion(HttpStatus.BAD_REQUEST, error, e);
        }
    }
}
