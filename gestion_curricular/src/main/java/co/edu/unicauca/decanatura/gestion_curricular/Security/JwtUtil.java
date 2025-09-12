package co.edu.unicauca.decanatura.gestion_curricular.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:mi_clave_super_secreta_de_256_bits_minimo_para_produccion_segura}")
    private String SECRET_KEY;
    
    @Value("${jwt.expiration:3600000}") // 1 hora por defecto
    private Long EXPIRATION_TIME;

    public String generarToken(String correo) {
        log.debug("Generando token para usuario: {}", correo);
        return Jwts.builder()
            .setSubject(correo)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes())
            .compact();
    }

    // public String extraerCorreo(String token) {
    //     return Jwts.parser()
    //             .setSigningKey(SECRET_KEY)
    //             .parseClaimsJws(token)
    //             .getBody()
    //             .getSubject();
    // }

    public String extraerCorreoDesdeToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            log.error("Error al extraer correo del token: {}", e.getMessage());
            return null;
        }
    }

    // public boolean validarToken(String token, UserDetails userDetails) {
    //     final String correo = extraerCorreo(token);
    //     return (correo.equals(userDetails.getUsername()) && !estaExpirado(token));
    // }

    // public boolean validarToken(String token) {
    //     try {
    //         Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    //         Jwts.parserBuilder()
    //             .setSigningKey(key)
    //             .build()
    //             .parseClaimsJws(token); // lanza excepción si es inválido
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace(); // o log.warn("Token inválido", e);
    //         return false;
    //     }
    // }
    
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    private boolean estaExpirado(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            log.error("Error al verificar expiración del token: {}", e.getMessage());
            return true; // Si hay error, considerar como expirado
        }
    }
}
