package co.edu.unicauca.decanatura.gestion_curricular.Security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SeguridadConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final org.springframework.core.env.Environment environment;
    
    public SeguridadConfig(JwtAuthenticationFilter jwtAuthenticationFilter, 
                          org.springframework.core.env.Environment environment) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitado porque usamos JWT stateless
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Usar nuestra configuración de CORS
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
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()")
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso a Actuator health checks (requerido por Render/Railway)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Permitir acceso a Swagger UI y OpenAPI docs sin autenticación
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Permitir acceso a login sin autenticación (incluye OPTIONS para preflight)
                .requestMatchers("/api/usuarios/login").permitAll()
                // Permitir acceso a estadísticas sin autenticación (compatible con despliegue actual)
                .requestMatchers("/api/estadisticas/**").permitAll()
                // Permitir todas las peticiones OPTIONS (preflight CORS)
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            // Agregar el filtro JWT antes del filtro de autenticación por defecto
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Usar la misma configuración que WebConfig (variables de entorno)
        String allowedOrigins = getAllowedOrigins();
        List<String> origins;
        if (allowedOrigins.equals("*")) {
            origins = Arrays.asList("*");
        } else {
            // Limpiar espacios en blanco de los orígenes
            origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Permitir todos los headers para mayor compatibilidad
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // Usar false para consistencia con WebConfig (JWT en headers, no cookies)
        configuration.setAllowCredentials(false);
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
