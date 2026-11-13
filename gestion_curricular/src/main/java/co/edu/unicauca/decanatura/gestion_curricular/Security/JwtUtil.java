package co.edu.unicauca.decanatura.gestion_curricular.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {

    /** Mínimo de 256 bits para HMAC; una clave más corta se puede adivinar por fuerza bruta. */
    private static final int LONGITUD_MINIMA_CLAVE = 32;

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    @Value("${app.jwt.expiration:3600000}") // 1 hora por defecto
    private Long EXPIRATION_TIME;

    /** Si la clave falta o es corta, la aplicación no arranca: nunca se firma con una clave conocida. */
    @PostConstruct
    void validarClave() {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()
                || SECRET_KEY.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_CLAVE) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) debe estar definida y tener al menos "
                            + LONGITUD_MINIMA_CLAVE + " bytes");
        }
    }

    /**
     * Genera una clave secreta segura a partir de la cadena de configuración
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        // HS512 requiere al menos 512 bits (64 bytes)
        if (keyBytes.length < 64) {
            // Extender la clave si es muy corta
            byte[] extendedKey = new byte[64];
            System.arraycopy(keyBytes, 0, extendedKey, 0, Math.min(keyBytes.length, 64));
            for (int i = keyBytes.length; i < 64; i++) {
                extendedKey[i] = (byte) (i % 256);
            }
            keyBytes = extendedKey;
        }
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generarToken(String correo) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
            .setSubject(correo)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, getSigningKey())
            .compact();
    }

    public String extraerCorreoDesdeToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error al extraer correo del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Valida si el token está expirado
     */
    public boolean estaExpirado(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Error al verificar expiración del token: {}", e.getMessage());
            return true; // Si hay error, consideramos el token como expirado
        }
    }
    
    /**
     * Valida el token JWT: verifica la firma y la expiración
     */
    public boolean validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
            
            // Verificación explícita de expiración
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            log.error("Token inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

}
