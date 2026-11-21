package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

/**
 * Codifica (hash) una contraseña antes de guardarla. La implementación (BCrypt de Spring Security)
 * vive en infraestructura; el dominio no depende de ella.
 */
@FunctionalInterface
public interface CodificadorContrasenaIntPort {

    String codificar(String contrasenaPlana);
}
