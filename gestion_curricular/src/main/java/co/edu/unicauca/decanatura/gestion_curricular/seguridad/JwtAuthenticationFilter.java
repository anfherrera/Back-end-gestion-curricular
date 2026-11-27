package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import java.io.IOException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.unicauca.decanatura.gestion_curricular.seguridad.SecurityAuditService.SecurityEventType;

/**
 * Autentica la petición con el token del encabezado {@code Authorization: Bearer ...}. Si el token no es
 * válido, la petición sigue sin autenticar y la regla de la ruta decide (401/403).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final SecurityAuditService securityAuditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(encabezado.substring(PREFIJO.length()), request);
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletRequest request) {
        String correo;
        try {
            correo = jwtUtil.correoDelToken(token);
        } catch (ExpiredJwtException e) {
            securityAuditService.logSecurityEvent(SecurityEventType.TOKEN_EXPIRED, "Token expirado",
                    e.getClaims().getSubject(), request);
            return;
        } catch (JwtException e) {
            securityAuditService.logSecurityEvent(SecurityEventType.TOKEN_INVALID, "Token inválido", null, request);
            return;
        }

        UserDetails usuario;
        try {
            usuario = userDetailsService.loadUserByUsername(correo);
        } catch (UsernameNotFoundException e) {
            securityAuditService.logSecurityEvent(SecurityEventType.TOKEN_INVALID,
                    "Token de un usuario que ya no existe", correo, request);
            return;
        }
        if (!usuario.isEnabled()) {
            securityAuditService.logSecurityEvent(SecurityEventType.ACCOUNT_DISABLED,
                    "Token de una cuenta desactivada", correo, request);
            return;
        }

        UsernamePasswordAuthenticationToken autenticacion =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }
}
