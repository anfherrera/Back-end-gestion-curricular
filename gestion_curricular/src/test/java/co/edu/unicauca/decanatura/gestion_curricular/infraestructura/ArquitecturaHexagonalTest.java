package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * El dominio y los puertos (aplicacion) no deben depender de infraestructura, de la configuración
 * de seguridad ni de Spring. Revisa los imports y los nombres calificados en el código fuente.
 */
@DisplayName("Arquitectura hexagonal - dependencias del núcleo")
class ArquitecturaHexagonalTest {

    private static final Path RAIZ = Path.of("src/main/java/co/edu/unicauca/decanatura/gestion_curricular");
    private static final Pattern PROHIBIDO = Pattern.compile(
            "gestion_curricular\\.infraestructura\\.|gestion_curricular\\.Security\\.|org\\.springframework\\.");

    @Test
    @DisplayName("dominio/ y aplicacion/ no importan infraestructura, Security ni Spring")
    void nucleoSinDependenciasHaciaAfuera() throws IOException {
        List<String> violaciones = new ArrayList<>();
        for (String capa : List.of("dominio", "aplicacion")) {
            try (Stream<Path> archivos = Files.walk(RAIZ.resolve(capa))) {
                for (Path archivo : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                    List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
                    for (int i = 0; i < lineas.size(); i++) {
                        String linea = lineas.get(i).trim();
                        if (!linea.startsWith("//") && !linea.startsWith("*") && PROHIBIDO.matcher(linea).find()) {
                            violaciones.add(RAIZ.relativize(archivo) + ":" + (i + 1) + "  " + linea);
                        }
                    }
                }
            }
        }
        assertThat(violaciones).isEmpty();
    }
}
