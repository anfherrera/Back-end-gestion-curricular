package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS para permitir peticiones desde el frontend Angular y Postman
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Environment environment;

    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    private String getAllowedOrigins() {
        // Buscar primero CORS_ALLOWED_ORIGIN (sin S) para compatibilidad con Render
        String origin = environment.getProperty("CORS_ALLOWED_ORIGIN");
        if (origin != null && !origin.isEmpty()) {
            return origin;
        }
        // Si no existe, buscar CORS_ALLOWED_ORIGINS (con S)
        origin = environment.getProperty("CORS_ALLOWED_ORIGINS");
        if (origin != null && !origin.isEmpty()) {
            return origin;
        }
        // Si no existe ninguna, usar el valor de application.properties o "*"
        return environment.getProperty("app.cors.allowed-origins", "*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowedOrigins = getAllowedOrigins();
        String[] origins;
        if (allowedOrigins.equals("*")) {
            origins = new String[]{"*"};
        } else {
            // Limpiar espacios en blanco de los orígenes para mayor robustez
            origins = java.util.Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
        
        registry.addMapping("/api/**")
                // Usar variables de entorno para producción: 
                // app.cors.allowed-origins=http://localhost:4200,https://tu-dominio.com
                // Para desarrollo, permite cualquier origen (solo en desarrollo)
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")
                .exposedHeaders("Authorization") // Exponer el header Authorization para que el frontend pueda leerlo
                .allowCredentials(false) // Deshabilitado porque usamos JWT en headers, no cookies
                .maxAge(3600); // Cachear preflight requests por 1 hora
    }
}
