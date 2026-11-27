package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Emite ID tokens con la forma de los de Google, firmados con una llave RSA propia, y arma un
 * {@link VerificadorCuentaGoogle} que confía en esa llave en lugar de en las de Google.
 */
final class TokensGoogleDePrueba {

    static final String CLIENT_ID = "cliente-de-prueba.apps.googleusercontent.com";
    static final String DOMINIO = "unicauca.edu.co";

    private final RSAKey llave;
    private final NimbusJwtEncoder firmador;

    TokensGoogleDePrueba() {
        try {
            llave = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
        firmador = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(llave)));
    }

    VerificadorCuentaGoogle verificador() {
        try {
            return new VerificadorCuentaGoogle(CLIENT_ID, DOMINIO,
                    NimbusJwtDecoder.withPublicKey(llave.toRSAPublicKey()).build());
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Un token válido de la cuenta; {@code ajuste} cambia los claims para probar cada rechazo. */
    String token(String correo, Consumer<JwtClaimsSet.Builder> ajuste) {
        Instant ahora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("https://accounts.google.com")
                .audience(List.of(CLIENT_ID))
                .subject("1234567890")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600))
                .claim("email", correo)
                .claim("email_verified", true)
                .claim("hd", DOMINIO);
        ajuste.accept(claims);
        JwsHeader cabecera = JwsHeader.with(SignatureAlgorithm.RS256).keyId(llave.getKeyID()).build();
        return firmador.encode(JwtEncoderParameters.from(cabecera, claims.build())).getTokenValue();
    }

    String token(String correo) {
        return token(correo, claims -> { });
    }
}
