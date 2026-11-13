package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Límites de la arquitectura hexagonal, comprobados sobre los imports y los nombres calificados del
 * código fuente.
 *
 * <p>El núcleo ({@code dominio/} y {@code aplicacion/}) no tiene excepciones. Las reglas entre adaptadores
 * tienen una línea base de archivos que todavía las incumplen (ver REVISION_CODIGO.md, ARQ-2 a ARQ-5): la
 * prueba falla si aparece un archivo nuevo que las incumple y también si un archivo de la línea base ya
 * cumple, para que se quite de la lista y no pueda volver a incumplirla.
 */
@DisplayName("Arquitectura hexagonal - dependencias entre capas")
class ArquitecturaHexagonalTest {

    private static final Path RAIZ = Path.of("src/main/java/co/edu/unicauca/decanatura/gestion_curricular");
    private static final String PAQUETE = "gestion_curricular\\.";

    @Test
    @DisplayName("dominio/ y aplicacion/ no importan infraestructura, Security, Spring ni JPA")
    void nucleoSinDependenciasHaciaAfuera() throws IOException {
        Pattern prohibido = Pattern.compile(
                PAQUETE + "infraestructura\\.|" + PAQUETE + "Security\\.|org\\.springframework\\.|jakarta\\.persistence\\.");
        List<String> violaciones = new ArrayList<>();
        violaciones.addAll(lineasQueIncumplen("dominio", prohibido));
        violaciones.addAll(lineasQueIncumplen("aplicacion", prohibido));
        assertThat(violaciones).isEmpty();
    }

    @Test
    @DisplayName("los adaptadores de entrada no usan adaptadores de salida")
    void entradaNoUsaSalida() throws IOException {
        comprobarConLineaBase("infraestructura/input",
                Pattern.compile(PAQUETE + "infraestructura\\.output\\."),
                Set.of(
                        "infraestructura/input/controladores/CursosVeranoRestController.java",          // ARQ-3
                        "infraestructura/input/controladores/DocumentGeneratorController.java",         // ARQ-3
                        "infraestructura/input/controladores/EstadisticasRestController.java",          // ARQ-3
                        "infraestructura/input/controladores/ReportesCursosVeranoRestController.java",  // ARQ-3
                        "infraestructura/input/controladores/SolicitudPazYSalvoRestController.java"));  // ARQ-3
    }

    @Test
    @DisplayName("los adaptadores de salida no llaman a los puertos de entrada")
    void salidaNoUsaPuertosDeEntrada() throws IOException {
        comprobarConLineaBase("infraestructura/output",
                Pattern.compile(PAQUETE + "aplicacion\\.input\\."),
                Set.of("infraestructura/output/formateador/DocumentGeneratorService.java")); // ARQ-4
    }

    @Test
    @DisplayName("Security no usa repositorios ni entidades JPA")
    void seguridadNoUsaPersistencia() throws IOException {
        comprobarConLineaBase("Security",
                Pattern.compile("persistencia\\.(repositorios|entidades)\\."),
                Set.of());
    }

    private static void comprobarConLineaBase(String capa, Pattern prohibido, Set<String> lineaBase) throws IOException {
        List<String> lineas = lineasQueIncumplen(capa, prohibido);
        Set<String> archivos = new TreeSet<>();
        for (String linea : lineas) {
            archivos.add(linea.substring(0, linea.indexOf(':')));
        }

        List<String> nuevas = lineas.stream()
                .filter(l -> !lineaBase.contains(l.substring(0, l.indexOf(':'))))
                .toList();
        Set<String> yaCumplen = new TreeSet<>(lineaBase);
        yaCumplen.removeAll(archivos);

        assertThat(nuevas).as("Dependencias nuevas que rompen la arquitectura").isEmpty();
        assertThat(yaCumplen).as("Archivos que ya cumplen: quitarlos de la línea base").isEmpty();
    }

    private static List<String> lineasQueIncumplen(String capa, Pattern prohibido) throws IOException {
        List<String> violaciones = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(RAIZ.resolve(capa))) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                String relativo = RAIZ.relativize(archivo).toString().replace('\\', '/');
                List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
                for (int i = 0; i < lineas.size(); i++) {
                    String linea = lineas.get(i).trim();
                    if (!linea.startsWith("//") && !linea.startsWith("*") && prohibido.matcher(linea).find()) {
                        violaciones.add(relativo + ":" + (i + 1) + "  " + linea);
                    }
                }
            }
        }
        return violaciones;
    }
}
