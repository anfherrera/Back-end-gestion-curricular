package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.equals("*") 
                ? new String[]{"*"} 
                : allowedOrigins.split(",");
        
        // Configurar CORS para todas las rutas (incluyendo /api/** y /actuator/**)
        registry.addMapping("/**")
                // Si CORS_ALLOWED_ORIGINS no está definida, permite todos los orígenes (*)
                // Para restringir orígenes, definir: CORS_ALLOWED_ORIGINS=https://tu-dominio.com,https://otro-dominio.com
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
                .allowedHeaders("*") // Permitir todos los headers
                .exposedHeaders("Authorization", "Content-Type") // Exponer headers importantes
                .allowCredentials(false) // Deshabilitado porque usamos JWT en headers, no cookies
                .maxAge(3600); // Cachear preflight requests por 1 hora
    }
}
