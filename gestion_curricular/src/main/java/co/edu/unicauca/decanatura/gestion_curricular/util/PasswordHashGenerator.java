package co.edu.unicauca.decanatura.gestion_curricular.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad temporal para generar hashes BCrypt
 * Usar solo en desarrollo para generar passwords hasheadas
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("Hash BCrypt: " + hashedPassword);
        System.out.println("========================================");
        
        // Verificar que el hash funciona
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("Verificación: " + (matches ? "✅ CORRECTO" : "❌ INCORRECTO"));
        
        // También verificar con el hash del test-data.sql
        String testHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG";
        boolean testMatches = encoder.matches(password, testHash);
        System.out.println("Verificación con hash de test: " + (testMatches ? "✅ CORRECTO" : "❌ INCORRECTO"));
    }
}

