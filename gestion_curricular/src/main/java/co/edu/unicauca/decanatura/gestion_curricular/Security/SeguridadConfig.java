package co.edu.unicauca.decanatura.gestion_curricular.Security;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class SeguridadConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitado porque usamos JWT stateless
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilitar CORS con configuración explícita
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
                // CRÍTICO: Permitir todas las peticiones OPTIONS (preflight) sin autenticación
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
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
                // Permitir acceso a login sin autenticación
                .requestMatchers("/api/usuarios/login").permitAll()
                // Permitir acceso a estadísticas sin autenticación (compatible con despliegue actual)
                .requestMatchers("/api/estadisticas/**").permitAll()
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
        
        // IMPORTANTE: Cuando allowCredentials es true, NO se puede usar "*" directamente
        // Usar patrones específicos o orígenes exactos
        
        if ("*".equals(allowedOrigins)) {
            // Si es *, usar patrones que permitan todos los orígenes de Vercel y localhost
            // IMPORTANTE: Incluir el origen específico del frontend
            configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://front-end-gestion-git-6132f1-andres-herreras-projects-2e8b8ec1.vercel.app",
                "https://*.vercel.app",
                "http://localhost:*",
                "http://127.0.0.1:*"
            ));
        } else {
            // Si hay orígenes específicos, procesarlos
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            List<String> exactOrigins = new java.util.ArrayList<>();
            List<String> patterns = new java.util.ArrayList<>();
            
            for (String origin : origins) {
                origin = origin.trim();
                if ("*".equals(origin)) {
                    // Si hay *, agregar patrones de Vercel
                    patterns.add("https://*.vercel.app");
                    patterns.add("http://localhost:*");
                } else if (origin.contains("*")) {
                    // Es un patrón
                    patterns.add(origin);
                } else {
                    // Es un origen exacto
                    exactOrigins.add(origin);
                }
            }
            
            if (!exactOrigins.isEmpty()) {
                configuration.setAllowedOrigins(exactOrigins);
            }
            if (!patterns.isEmpty()) {
                configuration.setAllowedOriginPatterns(patterns);
            }
        }
        
        // Métodos permitidos (incluyendo OPTIONS para preflight)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // Headers permitidos (incluyendo los que el frontend necesita)
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type", 
            "Authorization", 
            "Accept",
            "X-Requested-With",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos para que el frontend pueda leerlos
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // IMPORTANTE: Permitir credenciales (el frontend lo requiere)
        configuration.setAllowCredentials(true);
        
        // Cache de preflight requests (1 hora)
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
