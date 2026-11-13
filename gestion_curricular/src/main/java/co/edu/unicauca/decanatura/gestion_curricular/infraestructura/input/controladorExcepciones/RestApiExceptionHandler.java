package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CodigoError;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.estructuraExcepciones.Error;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.estructuraExcepciones.ErrorUtils;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import lombok.extern.slf4j.Slf4j;

/**
 * Único manejador global de excepciones. Todas las respuestas de error tienen la forma
 * {@link Error}: {@code codigoError}, {@code mensaje}, {@code codigoHttp}, {@code url}, {@code metodo}
 * (el frontend lee {@code mensaje}).
 *
 * Spring elige, para cada excepción, el manejador cuyo tipo sea el más cercano; el de
 * {@link Exception} solo actúa cuando ningún otro aplica.
 */
@RestControllerAdvice
@Slf4j
public class RestApiExceptionHandler {

        @ExceptionHandler(RespuestaHttpExcepcion.class)
        public ResponseEntity<Object> handleRespuestaCompatible(RespuestaHttpExcepcion ex) {
                return ResponseEntity.status(ex.estado()).body(ex.cuerpo());
        }

        // ==================== 4xx: errores del cliente o de reglas de negocio ====================

        @ExceptionHandler(EntidadYaExisteException.class)
        public ResponseEntity<Error> handleEntidadYaExiste(HttpServletRequest req, EntidadYaExisteException ex) {
                return responder(req, HttpStatus.CONFLICT, CodigoError.ENTIDAD_YA_EXISTE.getCodigo(),
                                CodigoError.ENTIDAD_YA_EXISTE.getLlaveMensaje() + ", " + ex.getMessage());
        }

        @ExceptionHandler(ReglaNegocioExcepcion.class)
        public ResponseEntity<Error> handleReglaNegocio(HttpServletRequest req, ReglaNegocioExcepcion ex) {
                return responder(req, HttpStatus.BAD_REQUEST, CodigoError.VIOLACION_REGLA_DE_NEGOCIO.getCodigo(),
                                ex.formatException());
        }

        @ExceptionHandler(EntidadNoExisteException.class)
        public ResponseEntity<Error> handleEntidadNoExiste(HttpServletRequest req, EntidadNoExisteException ex) {
                return responder(req, HttpStatus.NOT_FOUND, CodigoError.ENTIDAD_NO_ENCONTRADA.getCodigo(),
                                CodigoError.ENTIDAD_NO_ENCONTRADA.getLlaveMensaje() + ", " + ex.getMessage());
        }

        @ExceptionHandler(CupoAgotadoException.class)
        public ResponseEntity<Error> handleCupoAgotado(HttpServletRequest req, CupoAgotadoException ex) {
                return responder(req, HttpStatus.CONFLICT, "CUPO_AGOTADO", ex.getMessage());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Error> handleDataIntegrityViolation(HttpServletRequest req,
                        DataIntegrityViolationException ex) {
                String mensajeUsuario = "No se puede eliminar el registro porque tiene elementos asociados.";
                if (ex.getMessage() != null) {
                        String mensaje = ex.getMessage().toLowerCase();
                        if (mensaje.contains("usuarios") || mensaje.contains("idfkprograma") || mensaje.contains("idfkrol")) {
                                mensajeUsuario = "No se puede eliminar porque tiene usuarios asociados. Por favor, reasigne o elimine los usuarios primero.";
                        } else if (mensaje.contains("cursos") || mensaje.contains("docente") || mensaje.contains("materia")) {
                                mensajeUsuario = "No se puede eliminar porque tiene cursos asociados. Por favor, reasigne o elimine los cursos primero.";
                        } else if (mensaje.contains("solicitudes")) {
                                mensajeUsuario = "No se puede eliminar porque tiene solicitudes asociadas.";
                        }
                }
                return responder(req, HttpStatus.CONFLICT, CodigoError.VIOLACION_REGLA_DE_NEGOCIO.getCodigo(),
                                mensajeUsuario);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Error> handleArgumentoInvalido(HttpServletRequest req, IllegalArgumentException ex) {
                return responder(req, HttpStatus.BAD_REQUEST, CodigoError.VIOLACION_REGLA_DE_NEGOCIO.getCodigo(),
                                ex.getMessage());
        }

        @ExceptionHandler({ HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
                        MethodArgumentTypeMismatchException.class })
        public ResponseEntity<Error> handlePeticionMalFormada(HttpServletRequest req, Exception ex) {
                return responder(req, HttpStatus.BAD_REQUEST, "GC-0400",
                                "La petición no tiene el formato esperado: revise los parámetros y el cuerpo enviados");
        }

        /** Errores de validación de {@code @Valid}: un mapa campo → mensaje (lo usan los formularios). */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidacion(MethodArgumentNotValidException ex) {
                Map<String, String> errores = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> errores.put(
                                error instanceof FieldError campo ? campo.getField() : error.getObjectName(),
                                error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errores);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Error> handleConstraintViolation(HttpServletRequest req, ConstraintViolationException ex) {
                return responder(req, HttpStatus.BAD_REQUEST, CodigoError.VIOLACION_REGLA_DE_NEGOCIO.getCodigo(),
                                ex.getMessage());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Error> handleCredencialesInvalidas(HttpServletRequest req, BadCredentialsException ex) {
                return responder(req, HttpStatus.UNAUTHORIZED, CodigoError.CREDENCIALES_INVALIDAS.getCodigo(),
                                CodigoError.CREDENCIALES_INVALIDAS.getLlaveMensaje());
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Error> handleAutenticacion(HttpServletRequest req, AuthenticationException ex) {
                return responder(req, HttpStatus.UNAUTHORIZED, "GC-0401",
                                "Debe iniciar sesión para acceder a este recurso");
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Error> handleAccesoDenegado(HttpServletRequest req, AccessDeniedException ex) {
                return responder(req, HttpStatus.FORBIDDEN, "GC-0403",
                                "Acceso denegado: no tiene permisos para este recurso");
        }

        @ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
        public ResponseEntity<Error> handleRutaNoExiste(HttpServletRequest req, Exception ex) {
                return responder(req, HttpStatus.NOT_FOUND, "GC-0404", "El recurso solicitado no existe");
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<Error> handleMetodoNoSoportado(HttpServletRequest req,
                        HttpRequestMethodNotSupportedException ex) {
                return responder(req, HttpStatus.METHOD_NOT_ALLOWED, "GC-0405",
                                "El método " + ex.getMethod() + " no está permitido en esta ruta");
        }

        // ==================== 5xx: errores inesperados ====================

        /** Cualquier otra excepción. No se envía el detalle al cliente, pero sí queda en el log. */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Error> handleErrorInesperado(HttpServletRequest req, Exception ex) {
                log.error("Error no manejado en {} {}", req.getMethod(), req.getRequestURI(), ex);
                return responder(req, HttpStatus.INTERNAL_SERVER_ERROR, CodigoError.ERROR_GENERICO.getCodigo(),
                                "Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo.");
        }

        private static ResponseEntity<Error> responder(HttpServletRequest req, HttpStatus estado, String codigo,
                        String mensaje) {
                Error error = ErrorUtils.crearError(codigo, mensaje, estado.value())
                                .setUrl(req.getRequestURL().toString())
                                .setMetodo(req.getMethod());
                return ResponseEntity.status(estado).body(error);
        }
}
