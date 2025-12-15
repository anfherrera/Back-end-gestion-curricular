package co.edu.unicauca.decanatura.gestion_curricular.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final SecurityAuditService securityAuditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Verificar si el header Authorization existe y comienza con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token JWT
        jwt = authHeader.substring(7);
        
        try {
            // Extraer el email del token
            userEmail = jwtUtil.extraerCorreoDesdeToken(jwt);
            
            if (userEmail == null) {
                log.warn("No se pudo extraer el correo del token JWT");
                filterChain.doFilter(request, response);
                return;
            }
            
            // Si el email no es null y no hay autenticación en el contexto
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Validar el token primero
                if (!jwtUtil.validarToken(jwt)) {
                    log.warn("Token inválido o expirado para usuario: {}", userEmail);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Cargar los detalles del usuario
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                if (userDetails == null) {
                    log.warn("Usuario no encontrado: {}", userEmail);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Crear el token de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                
                // Establecer los detalles de la autenticación
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("Usuario autenticado: {} con roles: {}", userEmail, 
                    userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(java.util.stream.Collectors.joining(", ")));
            }
        } catch (Exception e) {
            log.error("Error al procesar el token JWT: {}", e.getMessage());
            
            // Registrar evento de seguridad para token inválido/expirado
            if (jwt != null) {
                String tokenType = jwtUtil.estaExpirado(jwt) ? "Token expirado" : "Token inválido";
                SecurityAuditService.SecurityEventType eventType = jwtUtil.estaExpirado(jwt) 
                    ? SecurityAuditService.SecurityEventType.TOKEN_EXPIRED 
                    : SecurityAuditService.SecurityEventType.TOKEN_INVALID;
                
                securityAuditService.logSecurityEvent(
                    eventType,
                    tokenType + " en URI: " + request.getRequestURI(),
                    null,
                    request
                );
            }
            
            // No establecer autenticación si hay error
        }

        filterChain.doFilter(request, response);
    }
}
