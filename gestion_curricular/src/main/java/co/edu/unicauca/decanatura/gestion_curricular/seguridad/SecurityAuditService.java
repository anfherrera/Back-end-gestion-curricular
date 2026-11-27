package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Deja en el log los eventos de seguridad (logger {@code ...seguridad.SecurityAuditService}), con la IP
 * y la ruta de la petición. Los ingresos exitosos van en DEBUG para no llenar el log; el resto, en WARN.
 * Nunca registra contraseñas ni tokens.
 */
@Service
@Slf4j
public class SecurityAuditService {

    public void logSecurityEvent(SecurityEventType tipo, String detalle, String usuario, HttpServletRequest request) {
        String ip = request == null ? "-" : request.getRemoteAddr();
        String ruta = request == null ? "-" : request.getMethod() + " " + request.getRequestURI();
        if (tipo == SecurityEventType.LOGIN_SUCCESS) {
            log.debug("[{}] {} usuario={} ip={} ruta={}", tipo, detalle, usuario, ip, ruta);
        } else {
            log.warn("[{}] {} usuario={} ip={} ruta={}", tipo, detalle, usuario, ip, ruta);
        }
    }

    /** Tipos de eventos de seguridad */
    public enum SecurityEventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        ACCOUNT_DISABLED,
        RATE_LIMIT_EXCEEDED,
        TOKEN_INVALID,
        TOKEN_EXPIRED
    }
}
