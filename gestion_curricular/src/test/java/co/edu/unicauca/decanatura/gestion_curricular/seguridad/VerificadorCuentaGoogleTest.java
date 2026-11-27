package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

@DisplayName("Inicio de sesión con Google - verificación del ID token")
class VerificadorCuentaGoogleTest {

    private static final String CORREO = "jperez@unicauca.edu.co";

    private final TokensGoogleDePrueba google = new TokensGoogleDePrueba();
    private final VerificadorCuentaGoogle verificador = google.verificador();

    @Test
    @DisplayName("Una cuenta institucional válida devuelve su correo en minúsculas")
    void cuentaValida() {
        assertThat(verificador.correoVerificado(google.token("JPerez@Unicauca.edu.co"))).isEqualTo(CORREO);
    }

    @Test
    @DisplayName("Acepta el emisor sin https, que Google también usa")
    void emisorSinEsquema() {
        String token = google.token(CORREO, c -> c.issuer("accounts.google.com"));
        assertThat(verificador.correoVerificado(token)).isEqualTo(CORREO);
    }

    @Test
    @DisplayName("Rechaza con 403 una cuenta de Gmail (sin dominio de Workspace)")
    void cuentaGmail() {
        rechazado("alguien@gmail.com", c -> c.claims(claims -> claims.remove("hd")), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Rechaza con 403 una cuenta de otro Google Workspace")
    void otroDominio() {
        rechazado("alguien@otra.edu.co", c -> c.claim("hd", "otra.edu.co"), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Rechaza con 403 un correo que no es del dominio aunque el hd sí lo sea")
    void correoDeOtroDominioConHdInstitucional() {
        rechazado("alguien@gmail.com", c -> { }, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Rechaza con 401 un correo que Google no ha verificado")
    void correoSinVerificar() {
        rechazado(CORREO, c -> c.claim("email_verified", false), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Rechaza con 401 un token emitido para otra aplicación")
    void otraAudiencia() {
        rechazado(CORREO, c -> c.audience(List.of("otra-app.apps.googleusercontent.com")), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Rechaza con 401 un token que no emitió Google")
    void otroEmisor() {
        rechazado(CORREO, c -> c.issuer("https://falso.example.com"), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Rechaza con 401 un token vencido")
    void vencido() {
        Instant hace2Horas = Instant.now().minusSeconds(7200);
        rechazado(CORREO, c -> c.issuedAt(hace2Horas).expiresAt(hace2Horas.plusSeconds(3600)), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Rechaza con 401 un token firmado con otra llave")
    void firmaAjena() {
        String tokenAjeno = new TokensGoogleDePrueba().token(CORREO);
        assertThatThrownBy(() -> verificador.correoVerificado(tokenAjeno))
                .isInstanceOfSatisfying(InicioSesionRechazadoException.class,
                        e -> assertThat(e.getEstado()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Rechaza con 401 un texto que no es un token")
    void noEsToken() {
        assertThatThrownBy(() -> verificador.correoVerificado("esto-no-es-un-jwt"))
                .isInstanceOfSatisfying(InicioSesionRechazadoException.class,
                        e -> assertThat(e.getEstado()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Sin Client ID configurado responde 503")
    void sinConfigurar() {
        VerificadorCuentaGoogle sinClientId = new VerificadorCuentaGoogle("", TokensGoogleDePrueba.DOMINIO);
        assertThatThrownBy(() -> sinClientId.correoVerificado(google.token(CORREO)))
                .isInstanceOfSatisfying(InicioSesionRechazadoException.class,
                        e -> assertThat(e.getEstado()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private void rechazado(String correo, Consumer<JwtClaimsSet.Builder> ajuste, HttpStatus esperado) {
        String token = google.token(correo, ajuste);
        assertThatThrownBy(() -> verificador.correoVerificado(token))
                .isInstanceOfSatisfying(InicioSesionRechazadoException.class,
                        e -> assertThat(e.getEstado()).isEqualTo(esperado));
    }
}
