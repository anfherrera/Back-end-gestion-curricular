package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorReportesEstadisticas;

@DisplayName("Reportes de estadísticas: cada variante produce un documento válido")
class GeneradorReportesEstadisticasTest {

    private final GeneradorReportesEstadisticas generador = new GeneradorReportesEstadisticas();

    private final Map<String, Object> estadisticas = Map.of(
            "totalSolicitudes", 10, "totalAprobadas", 6, "totalRechazadas", 2, "totalEnProceso", 2,
            "porcentajeAprobacion", 60.0,
            "porTipoProceso", Map.of("Paz y Salvo", 4, "Reingreso", 6),
            "porEstado", Map.of("APROBADA", 6, "RECHAZADA", 2, "ENVIADA", 2),
            "porPrograma", Map.of("Ingeniería de Sistemas", 10));

    private final Map<String, Object> cursosVerano = Map.of(
            "resumen", Map.of("totalSolicitudes", 5, "materiasUnicas", 2, "programasParticipantes", 1, "tasaAprobacion", 40.0),
            "topMaterias", List.of(Map.of("nombre", "Cálculo I", "solicitudes", 3, "porcentaje", 60.0)),
            "analisisPorPrograma", List.of(Map.of("nombre", "Ingeniería de Sistemas", "solicitudes", 5, "porcentaje", 100.0)),
            "recomendaciones", List.of());

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
