package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para mejorar el rendimiento del sistema
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configuración del CacheManager con expiración automática
     * El caché de estadísticas expirará después de 5 minutos
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @Override
            protected org.springframework.cache.Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(
                    name,
                    CacheBuilder.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES) // Caché expira en 5 minutos
                        .maximumSize(1000) // Máximo 1000 entradas
                        .build()
                        .asMap(),
                    false
                );
            }
        };
        
        // Definir nombres de cachés
        cacheManager.setCacheNames(Arrays.asList("estadisticasGlobales"));
        
        return cacheManager;
    }
}

