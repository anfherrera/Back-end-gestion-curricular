package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones;

import static java.util.stream.Collectors.joining;

import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonMappingException;
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
                    Map.of("error", "Formato de solicitud inválido: " + mensajeConversion(e)), e);
        } catch (RuntimeException e) {
            throw new RespuestaHttpExcepcion(HttpStatus.BAD_REQUEST, Map.of("error", e.getMessage()), e);
        }
    }

    private static String mensajeConversion(IllegalArgumentException error) {
        if (!(error.getCause() instanceof JsonMappingException jackson)) {
            return String.valueOf(error.getMessage());
        }

        // Solo la ruta de campos: el mensaje de Jackson trae clases Java e internos del parser (BUG-26).
        String campo = jackson.getPath().stream()
                .map(referencia -> referencia.getFieldName() != null
                        ? referencia.getFieldName() : String.valueOf(referencia.getIndex()))
                .collect(joining("."));
        if (campo.isEmpty()) {
            return "el cuerpo tiene un valor no válido";
        }
        return "el campo '" + campo + "' tiene un valor no válido";
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
