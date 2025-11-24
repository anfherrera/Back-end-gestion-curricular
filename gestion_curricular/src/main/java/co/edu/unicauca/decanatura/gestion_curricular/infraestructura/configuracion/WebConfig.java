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
