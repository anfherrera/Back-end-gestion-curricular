package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;

/**
 * Contratos históricos distintos del error global (BUG-16). La traducción rodea
 * únicamente la ejecución de la operación; no intercepta binding ni autorización.
 * La respuesta la escribe RestApiExceptionHandler.
 */
public final class ErroresPazYSalvo {
    private ErroresPazYSalvo() {}

    public static <T> T alCrear(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (ReglaNegocioExcepcion e) {
            throw new RespuestaHttpExcepcion(HttpStatus.CONFLICT,
                    Map.of("error", e.formatException(), "codigo", "REGLA_NEGOCIO"), e);
        } catch (IllegalArgumentException e) {
            throw new RespuestaHttpExcepcion(HttpStatus.BAD_REQUEST,
                    Map.of("error", "Formato de solicitud inválido: " + e.getMessage()), e);
        } catch (RuntimeException e) {
            throw new RespuestaHttpExcepcion(HttpStatus.BAD_REQUEST, Map.of("error", e.getMessage()), e);
        }
    }

    public static <T> T alCambiarEstado(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (EntidadNoExisteException e) {
            throw new RespuestaHttpExcepcion(HttpStatus.NOT_FOUND, null, e);
        } catch (IllegalArgumentException e) {
            throw new RespuestaHttpExcepcion(HttpStatus.BAD_REQUEST, Map.of("error", e.getMessage()), e);
        }
    }
}
