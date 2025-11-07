package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI (Swagger) para la documentación de la API
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@Configuration
public class OpenAPIConfig {

    @Value("${app.openapi.dev-url:http://localhost:5000}")
    private String devUrl;

    @Value("${app.openapi.prod-url:https://back-end-gestion-curricular.onrender.com}")
    private String prodUrl;

    @Bean
    public OpenAPI gestionCurricularOpenAPI() {
        // Servidor de desarrollo
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Servidor de Desarrollo");

        // Servidor de producción (ajustar según necesidad)
        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Servidor de Producción");

        // Información de contacto
        Contact contact = new Contact();
        contact.setEmail("decanatura.fiet@unicauca.edu.co");
        contact.setName("Decanatura FIET - Universidad del Cauca");
        contact.setUrl("https://www.unicauca.edu.co/fiet");

        // Licencia
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        // Información general de la API
        Info info = new Info()
                .title("API - Sistema de Gestión Curricular FIET")
                .version("1.0.0")
                .contact(contact)
                .description("API REST para el sistema de atención a estudiantes de pregrado en los procesos relacionados con la gestión curricular de la Facultad de Ingeniería Electrónica y Telecomunicaciones (FIET) de la Universidad del Cauca.")
                .termsOfService("https://www.unicauca.edu.co/terminos")
                .license(mitLicense);

        // Configuración de seguridad JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingrese el token JWT obtenido del endpoint /api/usuarios/login");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}

