package co.edu.unicauca.decanatura.gestion_curricular.soporte;

import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion.NombresJsonDominio;

/**
 * {@link MockMvcBuilders#standaloneSetup} con los mismos convertidores por defecto de Spring MVC, más el
 * módulo {@link NombresJsonDominio} que Spring Boot registra en la aplicación (OPC-2). Sin él, una
 * respuesta que serializa un modelo del dominio saldría con los nombres nuevos de sus campos.
 */
public final class SoporteMvc {

    private SoporteMvc() {
    }

    public static StandaloneMockMvcBuilder standaloneSetup(Object... controladores) {
        return MockMvcBuilders.standaloneSetup(controladores).setMessageConverters(convertidores());
    }

    /** Los convertidores por defecto de Spring MVC, con el módulo en el de JSON. */
    public static HttpMessageConverter<?>[] convertidores() {
        var configuracion = new WebMvcConfigurationSupport() {
            List<HttpMessageConverter<?>> porDefecto() {
                return getMessageConverters();
            }
        };
        List<HttpMessageConverter<?>> lista = configuracion.porDefecto();
        for (HttpMessageConverter<?> convertidor : lista) {
            if (convertidor instanceof MappingJackson2HttpMessageConverter json) {
                json.getObjectMapper().registerModule(NombresJsonDominio.modulo());
            }
        }
        return lista.toArray(HttpMessageConverter<?>[]::new);
    }
}
