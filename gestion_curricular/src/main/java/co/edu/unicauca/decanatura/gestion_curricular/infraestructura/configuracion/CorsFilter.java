package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro CORS de alta prioridad para asegurar que las peticiones OPTIONS
 * siempre respondan con los headers CORS correctos
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter implements Filter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String origin = httpRequest.getHeader("Origin");
        
        // CRÍTICO: Siempre establecer headers CORS antes de procesar la petición
        // Esto asegura que los headers estén presentes incluso si hay errores
        
        // Permitir el origen específico del frontend de Vercel y localhost
        if (origin != null && (origin.contains("vercel.app") || 
            origin.startsWith("http://localhost") ||
            origin.startsWith("http://127.0.0.1"))) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        } else if (origin != null) {
            // Permitir cualquier origen válido (útil para desarrollo)
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            // Si no hay Origin header, permitir el origen específico de Vercel por defecto
            httpResponse.setHeader("Access-Control-Allow-Origin", 
                "https://front-end-gestion-git-6132f1-andres-herreras-projects-2e8b8ec1.vercel.app");
        }

        // Headers CORS esenciales
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept, X-Requested-With, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        httpResponse.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Si es una petición OPTIONS (preflight), responder inmediatamente con 204 No Content
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
            httpResponse.setContentLength(0);
            return;
        }

        chain.doFilter(request, response);
    }
}

