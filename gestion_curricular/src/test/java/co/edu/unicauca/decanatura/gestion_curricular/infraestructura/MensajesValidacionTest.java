package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

/**
 * Cada {@code message = "{clave}"} de los DTO de petición debe existir en ValidationMessages.properties;
 * si no, el usuario ve el texto crudo "{clave}" en lugar del mensaje.
 */
@DisplayName("Mensajes de validación")
class MensajesValidacionTest {

    private static final String PAQUETE_DTO =
            "co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion";
    private static final Pattern CLAVE = Pattern.compile("^\\{([^}]+)\\}$");

    @Test
    @DisplayName("Todos los mensajes que piden los DTO existen")
    void clavesDeLosDtoExisten() throws Exception {
        Properties mensajes = new Properties();
        try (InputStream entrada = getClass().getResourceAsStream("/ValidationMessages.properties")) {
            mensajes.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        }

        Set<String> usadas = clavesUsadas();
        assertThat(usadas).as("se encontraron claves en los DTO").isNotEmpty();
        assertThat(usadas).as("claves pedidas que no están en ValidationMessages.properties")
                .allMatch(mensajes::containsKey);
    }

    private static Set<String> clavesUsadas() throws ClassNotFoundException, IOException {
        var escaner = new ClassPathScanningCandidateComponentProvider(false);
        escaner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        Set<String> claves = new TreeSet<>();
        for (BeanDefinition definicion : escaner.findCandidateComponents(PAQUETE_DTO)) {
            for (Field campo : Class.forName(definicion.getBeanClassName()).getDeclaredFields()) {
                for (Annotation anotacion : campo.getAnnotations()) {
                    clave(anotacion).ifPresent(claves::add);
                }
            }
        }
        return claves;
    }

    private static java.util.Optional<String> clave(Annotation anotacion) {
        try {
            Method message = anotacion.annotationType().getMethod("message");
            Matcher m = CLAVE.matcher((String) message.invoke(anotacion));
            return m.matches() && !m.group(1).startsWith("jakarta.") ? java.util.Optional.of(m.group(1))
                    : java.util.Optional.empty();
        } catch (ReflectiveOperationException | ClassCastException e) {
            return java.util.Optional.empty();
        }
    }
}
