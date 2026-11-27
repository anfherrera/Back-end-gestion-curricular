package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import org.springframework.http.HttpStatus;

/** El inicio de sesión no procede. El mensaje se muestra tal cual al usuario. */
public class InicioSesionRechazadoException extends RuntimeException {

    private final HttpStatus estado;

    public InicioSesionRechazadoException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
