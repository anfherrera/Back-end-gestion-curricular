package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Caracterización previa a OPC-4: qué beans de las configuraciones anidadas de las pruebas entran en el contexto de
 * una {@code @SpringBootTest} que no las declara, y qué bean resuelve el puerto de estadísticas. Un bean es "de
 * prueba" si su clase, o la de la configuración que lo crea, está anidada en una clase de prueba ({@code Test$}).
 */
@SpringBootTest
@ActiveProfiles("test")
class ConfiguracionesDePruebaCaracterizacionTest {

    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/configuracion/beans-de-prueba.json");

    private final ObjectMapper json = new ObjectMapper();

    @Autowired ConfigurableApplicationContext contexto;

    @Test
    void beansDeConfiguracionesDePrueba() throws Exception {
        ConfigurableListableBeanFactory fabrica = contexto.getBeanFactory();
        ObjectNode resultado = json.createObjectNode();
        ArrayNode dePrueba = resultado.putArray("beansDePrueba");
        String[] nombres = fabrica.getBeanDefinitionNames();
        Arrays.sort(nombres);
        for (String nombre : nombres) {
            String origen = origen(fabrica, nombre);
            if (origen != null && origen.contains("Test$")) {
                // Sin el sufijo de la subclase CGLIB, que no es parte de lo que se caracteriza
                String clase = origen.substring(origen.lastIndexOf('.') + 1);
                int cglib = clase.indexOf("$$");
                dePrueba.addObject().put("bean", nombre).put("origen", cglib < 0 ? clase : clase.substring(0, cglib));
            }
        }
        ArrayNode candidatos = resultado.putArray("estadisticasCU");
        String[] puertos = contexto.getBeanNamesForType(GestionarEstadisticasCUIntPort.class);
        Arrays.sort(puertos);
        for (String nombre : puertos) {
            candidatos.addObject().put("bean", nombre).put("primario", fabrica.getBeanDefinition(nombre).isPrimary());
        }
        Object inyectado = contexto.getBean(GestionarEstadisticasCUIntPort.class);
        resultado.put("estadisticasCUInyectado",
                Arrays.stream(puertos).filter(n -> contexto.getBean(n) == inyectado).findFirst().orElse(null));

        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(REFERENCIA)) {
            Files.createDirectories(REFERENCIA.getParent());
            Files.writeString(REFERENCIA, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultado));
        }
        // OPC-4: con los filtros de @SpringBootApplication no entra ninguna configuración anidada de las pruebas
        assertThat(resultado).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(REFERENCIA)),
                "configuracion/correcciones/beans-de-prueba-opc4.json"));
    }

    /** La clase del bean o, si sale de un método {@code @Bean}, la de la configuración que lo declara. */
    private static String origen(ConfigurableListableBeanFactory fabrica, String nombre) {
        BeanDefinition definicion = fabrica.getBeanDefinition(nombre);
        if (definicion.getFactoryBeanName() != null && fabrica.containsBeanDefinition(definicion.getFactoryBeanName())) {
            return fabrica.getBeanDefinition(definicion.getFactoryBeanName()).getBeanClassName();
        }
        return definicion.getBeanClassName();
    }
}
