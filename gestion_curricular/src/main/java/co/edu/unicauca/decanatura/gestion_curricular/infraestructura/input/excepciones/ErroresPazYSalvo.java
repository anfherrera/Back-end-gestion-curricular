package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones;

import static java.util.stream.Collectors.joining;

import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
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
        String mensaje = String.valueOf(error.getMessage());
        if (!(error.getCause() instanceof JsonMappingException jackson)) {
            return mensaje;
        }

        // El contrato anterior incluía los nombres Java emitidos por Jackson (BUG-26).
        mensaje = nombreDtoHistorico(mensaje, "Cannot construct instance of `");
        mensaje = nombreDtoHistorico(mensaje, "Cannot deserialize value of type `");
        if (jackson instanceof PropertyBindingException campo) {
            String nombre = "\"" + campo.getPropertyName() + "\" (class ";
            mensaje = nombreDtoHistorico(mensaje, "Unrecognized field " + nombre);
            mensaje = nombreDtoHistorico(mensaje, "Ignored field " + nombre);
        }
        String ruta = jackson.getPathReference();
        if (ruta.isEmpty() || !mensaje.endsWith(ruta + ")")) {
            return mensaje;
        }

        String rutaHistorica = jackson.getPath().stream()
                .map(referencia -> nombreDtoHistorico(referencia.getDescription(), ""))
                .collect(joining("->"));
        return mensaje.substring(0, mensaje.length() - ruta.length() - 1) + rutaHistorica + ")";
    }

    private static String nombreDtoHistorico(String texto, String prefijo) {
        String paqueteActual = "co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.";
        String paqueteHistorico = "co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.";
        if (texto.startsWith(prefijo + paqueteActual)) {
            return prefijo + paqueteHistorico + texto.substring(prefijo.length() + paqueteActual.length());
        }
        return texto;
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
