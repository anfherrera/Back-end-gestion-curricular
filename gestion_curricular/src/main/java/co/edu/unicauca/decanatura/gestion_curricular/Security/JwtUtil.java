package co.edu.unicauca.decanatura.gestion_curricular.Security;

import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;

import java.security.Key;



public class JwtUtil {

    private final String SECRET_KEY = "clave_secreta"; // Ocultar informacion al hacer commit(Produccion)

    public String generarToken(String correo) {
        return Jwts.builder()
            .setSubject(correo)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora
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
        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
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
            e.printStackTrace();
            return false;
        }
    }

    private boolean estaExpirado(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}
