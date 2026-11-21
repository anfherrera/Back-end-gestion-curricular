package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DemandaCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesContadas;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorReportesEstadisticas;

@DisplayName("Reportes de estadísticas: cada variante produce un documento válido")
class GeneradorReportesEstadisticasTest {

    private final GeneradorReportesEstadisticas generador = new GeneradorReportesEstadisticas();

    private final GlobalesContadas estadisticas = new GlobalesContadas(10, 6, 2, 2, 0, 60.0,
            Map.of("APROBADA", 6, "RECHAZADA", 2, "ENVIADA", 2),
            Map.of("Paz y Salvo", 4, "Reingreso", 6),
            Map.of("Ingeniería de Sistemas", 10), new Date());

    /** Resumen, una materia, un programa, sin recomendaciones y sin predicciones. */
    private final AnalisisCursosVerano cursosVerano = new AnalisisCursosVerano(null, null, null,
            new AnalisisCursosVerano.Resumen(5, 2, 1, 40.0), null, null,
            List.of(new DemandaCursoVerano("Cálculo I", 3, 60.0)),
            List.of(new DemandaCursoVerano("Ingeniería de Sistemas", 5, 100.0)),
            null, List.of(), null);

    @Test
    void pdfs() {
        assertThat(cabecera(generador.pdfGeneral(estadisticas))).isEqualTo("%PDF");
        assertThat(cabecera(generador.pdfCursosVerano(cursosVerano, "2025-2", 1))).isEqualTo("%PDF");
        assertThat(cabecera(generador.pdfCompleto(estadisticas, cursosVerano, "2025-2", 1))).isEqualTo("%PDF");
        assertThat(cabecera(generador.pdfCompleto(estadisticas, null, null, null))).isEqualTo("%PDF");
    }

    @Test
    void excels() {
        // un .xlsx es un zip: empieza por "PK"
        assertThat(cabecera(generador.excel(estadisticas, null, null, null))).startsWith("PK");
        assertThat(cabecera(generador.excel(null, cursosVerano, "2025-2", 1))).startsWith("PK");
        assertThat(cabecera(generador.excel(estadisticas, cursosVerano, "2025-2", 1))).startsWith("PK");
    }

    private static String cabecera(byte[] contenido) {
        return new String(contenido, 0, 4, StandardCharsets.ISO_8859_1);
    }
}
