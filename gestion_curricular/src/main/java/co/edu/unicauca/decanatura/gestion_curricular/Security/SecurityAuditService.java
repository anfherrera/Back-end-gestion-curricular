package co.edu.unicauca.decanatura.gestion_curricular.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de auditoría de seguridad para registrar eventos de seguridad
 */
@Service
@Slf4j
public class SecurityAuditService {

    /**
     * Registra un evento de seguridad
     */
    public void logSecurityEvent(SecurityEventType eventType, String details, HttpServletRequest request) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("eventType", eventType);
        auditData.put("details", details);
        
        if (request != null) {
            auditData.put("ipAddress", getClientIpAddress(request));
            auditData.put("userAgent", request.getHeader("User-Agent"));
            auditData.put("method", request.getMethod());
            auditData.put("uri", request.getRequestURI());
        }
        
        // Log estructurado para fácil análisis
        log.info("[SECURITY_AUDIT] {} - {} - IP: {} - URI: {}", 
            eventType, 
            details,
            auditData.get("ipAddress"),
            auditData.get("uri"));
    }

    /**
     * Registra un evento de seguridad con usuario
     */
    public void logSecurityEvent(SecurityEventType eventType, String details, String usuario, HttpServletRequest request) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("eventType", eventType);
        auditData.put("details", details);
        auditData.put("usuario", usuario);
        
        if (request != null) {
            auditData.put("ipAddress", getClientIpAddress(request));
            auditData.put("userAgent", request.getHeader("User-Agent"));
            auditData.put("method", request.getMethod());
            auditData.put("uri", request.getRequestURI());
        }
        
        // Log de LOGIN_SUCCESS en nivel DEBUG para evitar verbosidad
        // Los demás eventos críticos se mantienen en INFO/WARN
        if (eventType == SecurityEventType.LOGIN_SUCCESS) {
            log.debug("[SECURITY_AUDIT] {} - Usuario: {} - {} - IP: {} - URI: {}", 
                eventType,
                usuario,
                details,
                auditData.get("ipAddress"),
                auditData.get("uri"));
        } else {
            log.info("[SECURITY_AUDIT] {} - Usuario: {} - {} - IP: {} - URI: {}", 
                eventType,
                usuario,
                details,
                auditData.get("ipAddress"),
                auditData.get("uri"));
        }
    }

    /**
     * Obtiene la IP real del cliente (considera proxies)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Si hay múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * Tipos de eventos de seguridad
     */
    public enum SecurityEventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        ACCESS_DENIED,
        TOKEN_INVALID,
        TOKEN_EXPIRED,
        RATE_LIMIT_EXCEEDED,
        UNAUTHORIZED_ACCESS,
        SUSPICIOUS_ACTIVITY,
        PASSWORD_CHANGE,
        ROLE_CHANGE,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED
    }
}

