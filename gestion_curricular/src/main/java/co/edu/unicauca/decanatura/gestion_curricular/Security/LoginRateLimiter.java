package co.edu.unicauca.decanatura.gestion_curricular.Security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Limitador simple de intentos de login por correo e IP.
 * - Si se superan los intentos permitidos en la ventana configurada, se bloquea temporalmente.
 * - Resetea el contador cuando hay un login exitoso.
 */
@Component
public class LoginRateLimiter {

    private static final class Attempt {
        int failedCount;
        Instant firstAttemptAt;
        Instant lockedUntil;
    }

    private final Map<String, Attempt> keyToAttempt = new ConcurrentHashMap<>();

    // Configuración
    private final int maxAttempts;
    private final Duration window;
    private final Duration lockDuration;

    public LoginRateLimiter(
            @Value("${security.login.maxAttempts:5}") int maxAttempts,
            @Value("${security.login.windowSeconds:900}") long windowSeconds,        // 15 min
            @Value("${security.login.lockSeconds:900}") long lockSeconds            // 15 min
    ) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.window = Duration.ofSeconds(Math.max(60, windowSeconds));
        this.lockDuration = Duration.ofSeconds(Math.max(60, lockSeconds));
    }

    /**
     * Verifica si una combinación correo-ip está permitida para intentar login.
     * Devuelve null si está permitido; de lo contrario devuelve el instante hasta el cual está bloqueado.
     */
    public Instant checkAllowed(String correo, String ip) {
        String key = buildKey(correo, ip);
        Attempt attempt = keyToAttempt.get(key);
        if (attempt == null) {
            return null;
        }

        Instant now = Instant.now();
        // Si está bloqueado y el bloqueo no ha expirado
        if (attempt.lockedUntil != null && now.isBefore(attempt.lockedUntil)) {
            return attempt.lockedUntil;
        }

        // Si la ventana ya expiró, reiniciar intentos
        if (attempt.firstAttemptAt == null || now.isAfter(attempt.firstAttemptAt.plus(window))) {
            reset(key);
            return null;
        }

        // Si aún no supera el máximo, permitir
        if (attempt.failedCount < maxAttempts) {
            return null;
        }

        // Aplicar bloqueo
        attempt.lockedUntil = now.plus(lockDuration);
        return attempt.lockedUntil;
    }

    /**
     * Registrar un fallo de autenticación.
     */
    public void onFailure(String correo, String ip) {
        String key = buildKey(correo, ip);
        Attempt attempt = keyToAttempt.computeIfAbsent(key, k -> {
            Attempt a = new Attempt();
            a.firstAttemptAt = Instant.now();
            return a;
        });
        Instant now = Instant.now();

        // Si pasó la ventana, reiniciar
        if (attempt.firstAttemptAt == null || now.isAfter(attempt.firstAttemptAt.plus(window))) {
            attempt.failedCount = 0;
            attempt.firstAttemptAt = now;
            attempt.lockedUntil = null;
        }

        attempt.failedCount++;
        // Si ya superó el máximo, fijar bloqueo
        if (attempt.failedCount >= maxAttempts) {
            attempt.lockedUntil = now.plus(lockDuration);
        }
        keyToAttempt.put(key, attempt);
    }

    /**
     * Limpiar contadores tras autenticación exitosa.
     */
    public void onSuccess(String correo, String ip) {
        reset(buildKey(correo, ip));
    }

    private void reset(String key) {
        keyToAttempt.remove(key);
    }

    private String buildKey(String correo, String ip) {
        String c = correo == null ? "null" : correo.trim().toLowerCase();
        String i = ip == null ? "unknown" : ip.trim();
        return c + "|" + i;
    }
}


