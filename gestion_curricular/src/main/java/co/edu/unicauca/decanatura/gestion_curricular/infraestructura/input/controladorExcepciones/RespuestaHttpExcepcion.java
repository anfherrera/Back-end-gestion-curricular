package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones;

import org.springframework.http.HttpStatus;

/** Respuesta de un contrato HTTP existente, traducida antes de llegar al manejador global. */
public final class RespuestaHttpExcepcion extends RuntimeException {
    private final HttpStatus estado;
    private final Object cuerpo;

    public RespuestaHttpExcepcion(HttpStatus estado, Object cuerpo, RuntimeException causa) {
        super(causa);
        this.estado = estado;
        this.cuerpo = cuerpo;
    }

    public HttpStatus estado() { return estado; }
    public Object cuerpo() { return cuerpo; }
}
