package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Emite y valida los tokens de sesión (HS512). El sujeto del token es el correo del usuario. */
@Component
public class JwtUtil {

    /** Mínimo de 256 bits para HMAC; una clave más corta se puede adivinar por fuerza bruta. */
    private static final int LONGITUD_MINIMA_CLAVE = 32;
    /** HS512 exige 512 bits. */
    private static final int LONGITUD_CLAVE_HS512 = 64;

    private final SecretKey clave;
    private final JwtParser lector;
    private final long duracionMs;

    /** Si la clave falta o es corta, la aplicación no arranca: nunca se firma con una clave conocida. */
    public JwtUtil(@Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration:3600000}") long duracionMs) {
        if (secreto == null || secreto.isBlank()
                || secreto.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_CLAVE) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) debe estar definida y tener al menos "
                            + LONGITUD_MINIMA_CLAVE + " bytes");
        }
        this.clave = claveHs512(secreto);
        this.lector = Jwts.parserBuilder().setSigningKey(clave).build();
        this.duracionMs = duracionMs;
    }

    /**
     * Completa la clave hasta 64 bytes con un relleno fijo. Es la misma derivación de siempre, para que los
     * tokens emitidos antes de un despliegue sigan siendo válidos; la entropía la aporta el secreto (≥ 256 bits).
     */
    private static SecretKey claveHs512(String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LONGITUD_CLAVE_HS512) {
            byte[] extendida = new byte[LONGITUD_CLAVE_HS512];
            System.arraycopy(bytes, 0, extendida, 0, bytes.length);
            for (int i = bytes.length; i < LONGITUD_CLAVE_HS512; i++) {
                extendida[i] = (byte) i;
            }
            bytes = extendida;
        }
        return new SecretKeySpec(bytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generarToken(String correo) {
        Date ahora = new Date();
        return Jwts.builder()
                .setSubject(correo)
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Verifica la firma y la vigencia del token y devuelve su correo.
     *
     * @throws ExpiredJwtException si el token venció
     * @throws JwtException si el token está mal formado o su firma no corresponde
     */
    public String correoDelToken(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtException("Token vacío");
        }
        return lector.parseClaimsJws(token).getBody().getSubject();
    }
}
