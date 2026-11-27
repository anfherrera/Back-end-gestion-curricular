package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Verifica el ID token que entrega el botón "Iniciar sesión con Google" (Google Identity Services) y
 * devuelve el correo institucional de la cuenta. Comprueba la firma con las llaves públicas de Google, el
 * emisor, que el token sea para esta aplicación (Client ID), la vigencia, que Google haya verificado el
 * correo y que la cuenta pertenezca al Google Workspace de la universidad (claim {@code hd}).
 */
@Component
public class VerificadorCuentaGoogle {

    private static final String LLAVES_GOOGLE = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> EMISORES_GOOGLE = Set.of("https://accounts.google.com", "accounts.google.com");
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(5);

    private final String dominio;
    private final NimbusJwtDecoder decodificador;

    @Autowired
    public VerificadorCuentaGoogle(@Value("${app.google.client-id:}") String clientId,
            @Value("${app.google.dominio:unicauca.edu.co}") String dominio) {
        this(clientId, dominio, clientId == null || clientId.isBlank() ? null
                : NimbusJwtDecoder.withJwkSetUri(LLAVES_GOOGLE).restOperations(clienteHttp()).build());
    }

    /** Recibe el decodificador para poder probar con llaves propias en lugar de las de Google. */
    VerificadorCuentaGoogle(String clientId, String dominio, NimbusJwtDecoder decodificador) {
        this.dominio = dominio.trim().toLowerCase(Locale.ROOT);
        this.decodificador = decodificador;
        if (decodificador != null) {
            decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    new JwtTimestampValidator(),
                    new JwtClaimValidator<Object>(JwtClaimNames.ISS,
                            emisor -> emisor != null && EMISORES_GOOGLE.contains(emisor.toString())),
                    new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                            audiencia -> audiencia != null && audiencia.contains(clientId.trim()))));
        }
    }

    private static RestTemplate clienteHttp() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout((int) ESPERA_MAXIMA.toMillis());
        fabrica.setReadTimeout((int) ESPERA_MAXIMA.toMillis());
        return new RestTemplate(fabrica);
    }

    /**
     * @param credencial el ID token ({@code credential}) que Google entregó al frontend
     * @return el correo de la cuenta, en minúsculas
     * @throws InicioSesionRechazadoException si el inicio con Google no está configurado (503), si el token no
     *         es válido (401), si Google no pudo responder (503) o si la cuenta no es del dominio (403)
     */
    public String correoVerificado(String credencial) {
        if (decodificador == null) {
            throw new InicioSesionRechazadoException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El inicio de sesión con Google no está habilitado en este servidor.");
        }
        Jwt token;
        try {
            token = decodificador.decode(credencial);
        } catch (BadJwtException e) {
            throw new InicioSesionRechazadoException(HttpStatus.UNAUTHORIZED,
                    "No se pudo verificar la cuenta de Google o la sesión ya venció. Intente de nuevo.");
        } catch (JwtException e) {
            throw new InicioSesionRechazadoException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible comunicarse con Google. Intente de nuevo en unos minutos.");
        }

        String correo = token.getClaimAsString("email");
        if (correo == null || !"true".equals(String.valueOf(token.getClaims().get("email_verified")))) {
            throw new InicioSesionRechazadoException(HttpStatus.UNAUTHORIZED,
                    "Google no ha verificado el correo de esta cuenta.");
        }
        correo = correo.trim().toLowerCase(Locale.ROOT);
        if (!dominio.equalsIgnoreCase(token.getClaimAsString("hd")) || !correo.endsWith("@" + dominio)) {
            throw new InicioSesionRechazadoException(HttpStatus.FORBIDDEN,
                    "Solo se puede ingresar con una cuenta institucional @" + dominio + ".");
        }
        return correo;
    }
}
