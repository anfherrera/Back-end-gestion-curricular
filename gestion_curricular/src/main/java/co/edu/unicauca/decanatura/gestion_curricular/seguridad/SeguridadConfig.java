package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SeguridadConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Catálogos que solo el Administrador puede modificar. */
    private static final String[] CATALOGOS = {
        "/api/roles/**", "/api/programas/**", "/api/docentes/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitado porque usamos JWT stateless
            .cors(cors -> {}) // Usa el bean corsConfigurationSource
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless porque usamos JWT
            )
            // Configurar headers de seguridad HTTP
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "script-src 'self'; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self' http://localhost:* https://*")
                )
                .frameOptions(frame -> frame.deny()) // X-Frame-Options: DENY (protección clickjacking)
                .contentTypeOptions(contentType -> {}) // X-Content-Type-Options: nosniff
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000) // 1 año
                    .includeSubDomains(true)
                    .preload(true)
                )
                .xssProtection(xss -> {}) // X-XSS-Protection: 1; mode=block
                .referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .permissionsPolicyHeader(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()")
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso a Swagger UI y OpenAPI docs sin autenticación
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Chequeo de salud del hosting: solo {"status":"UP"}; los detalles exigen sesión
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                // Inicio de sesión con contraseña o con la cuenta institucional de Google
                .requestMatchers(HttpMethod.POST, "/api/usuarios/login", "/api/usuarios/login/google").permitAll()
                // Estadísticas: solo consultas y reportes (EST-7, paso 9), para los roles administrativos
                .requestMatchers("/api/estadisticas/**")
                    .hasAnyRole("Administrador", "Funcionario", "Coordinador", "Secretario", "Decano")
                // Catálogos: cualquiera autenticado consulta; solo el Administrador crea, modifica o elimina
                .requestMatchers(HttpMethod.POST, CATALOGOS).hasRole("Administrador")
                .requestMatchers(HttpMethod.PUT, CATALOGOS).hasRole("Administrador")
                .requestMatchers(HttpMethod.DELETE, CATALOGOS).hasRole("Administrador")
                // Cursos de verano (/api/cursos-verano): cada ruta lleva su @PreAuthorize; los estudiantes
                // consultan la oferta y gestionan sus preinscripciones e inscripciones (EST-7, paso 8)
                // Generación de documentos desde plantillas: solo staff
                .requestMatchers("/api/documentos-generados", "/api/documentos-generados/**")
                    .hasAnyRole("Administrador", "Funcionario", "Coordinador", "Secretario", "Decano")
                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            // Agregar el filtro JWT antes del filtro de autenticación por defecto
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Orígenes del frontend, separados por comas ({@code app.cors.allowed-origins}, variable
     * {@code CORS_ALLOWED_ORIGINS} en producción). Admite patrones como {@code https://*.vercel.app}.
     * Sin credenciales: la sesión viaja en el encabezado {@code Authorization}, no en cookies.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String origenesPermitidos) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
