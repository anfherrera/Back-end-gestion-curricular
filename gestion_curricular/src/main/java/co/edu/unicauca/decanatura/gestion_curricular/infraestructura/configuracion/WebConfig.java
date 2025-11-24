package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import org.springframework.context.annotation.Configuration;
// CORS se maneja completamente en SeguridadConfig.java

/**
 * Configuración de CORS para permitir peticiones desde el frontend Angular y Postman
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@Configuration
public class WebConfig {
    // CORS se maneja completamente en SeguridadConfig.java para evitar conflictos
    // Spring Security tiene prioridad sobre WebMvcConfigurer para CORS
    // No necesitamos implementar WebMvcConfigurer aquí
}
