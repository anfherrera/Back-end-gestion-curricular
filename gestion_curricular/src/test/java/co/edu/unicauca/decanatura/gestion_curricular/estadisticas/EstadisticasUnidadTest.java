package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 * PRUEBAS UNITARIAS - MÓDULO ESTADÍSTICAS (ME6)
 * ============================================================
 * 
 * Objetivo: Validar la lógica de negocio del módulo de Estadísticas
 * de forma aislada, usando mocks para las dependencias.
 * 
 * Tipo de pruebas: UNITARIAS
 * - Prueban componentes individuales (CU Adapter)
 * - Usan mocks (Mockito) para dependencias (Gateway)
 * - No requieren Spring Context
 * - Son muy rápidas (~50ms)
 * - Validan delegación correcta al gateway
 * 
 * @author Andrés Felipe Herrera Artunduaga
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Lógica de Negocio Estadísticas")
class EstadisticasUnidadTest {

    @Mock
    private GestionarEstadisticasGatewayIntPort estadisticasGateway;

    @Mock
    private FormateadorResultadosIntPort formateadorResultados;

    private GestionarEstadisticasCUIntPort estadisticasCU;

    private Estadistica estadisticaEjemplo;

    @BeforeEach
    void setUp() {
        // Crear el CU Adapter con las dependencias mockeadas
        estadisticasCU = new GestionarEstadisticasCUAdapter(
                estadisticasGateway,
                formateadorResultados
        );

        // Crear estadística de ejemplo
        estadisticaEjemplo = new Estadistica();
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("Test 1: Crear estadística - Delega correctamente al gateway")
    void testCrearEstadisticaDelegaCorrectamente() {
        // Arrange
        when(estadisticasGateway.crearEstadistica(any(Estadistica.class)))
                .thenReturn(estadisticaEjemplo);

        // Act
        Estadistica resultado = estadisticasCU.crearEstadistica(estadisticaEjemplo);

        // Assert
        assertThat(resultado).isNotNull();
        verify(estadisticasGateway, times(1)).crearEstadistica(estadisticaEjemplo);
    }

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("Test 2: Obtener estadística por ID - Delega correctamente")
    void testObtenerEstadisticaPorIdDelegaCorrectamente() {
        // Arrange
        int idBuscado = 1;
        when(estadisticasGateway.obtenerEstadisticaPorId(idBuscado))
                .thenReturn(estadisticaEjemplo);

        // Act
        Estadistica resultado = estadisticasCU.obtenerEstadisticaPorId(idBuscado);

        // Assert
        assertThat(resultado).isNotNull();
        verify(estadisticasGateway, times(1)).obtenerEstadisticaPorId(idBuscado);
    }

    @Test
    @DisplayName("Test 3: Actualizar estadística - Delega correctamente")
    void testActualizarEstadisticaDelegaCorrectamente() {
        // Arrange
        when(estadisticasGateway.actualizarEstadistica(any(Estadistica.class)))
                .thenReturn(estadisticaEjemplo);

        // Act
        Estadistica resultado = estadisticasCU.actualizarEstadistica(estadisticaEjemplo);

        // Assert
        assertThat(resultado).isNotNull();
        verify(estadisticasGateway, times(1)).actualizarEstadistica(estadisticaEjemplo);
    }

    @Test
    @DisplayName("Test 4: Eliminar estadística - Delega correctamente")
    void testEliminarEstadisticaDelegaCorrectamente() {
        // Arrange
        int id = 1;
        when(estadisticasGateway.eliminarEstadistica(id)).thenReturn(true);

        // Act
        Boolean resultado = estadisticasCU.eliminarEstadistica(id);

        // Assert
        assertThat(resultado).isTrue();
        verify(estadisticasGateway, times(1)).eliminarEstadistica(id);
    }

    // ==================== PRUEBAS DE ESTADÍSTICAS GLOBALES ====================

    @Test
    @DisplayName("Test 5: Obtener estadísticas globales - Delega correctamente")
    void testObtenerEstadisticasGlobalesDelegaCorrectamente() {
        // Arrange
        Map<String, Object> estadisticasEsperadas = new HashMap<>();
        estadisticasEsperadas.put("totalSolicitudes", 100);
        estadisticasEsperadas.put("aprobadas", 80);
        when(estadisticasGateway.obtenerEstadisticasGlobales()).thenReturn(estadisticasEsperadas);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerEstadisticasGlobales();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).containsKey("totalSolicitudes");
        assertThat(resultado.get("totalSolicitudes")).isEqualTo(100);
        verify(estadisticasGateway, times(1)).obtenerEstadisticasGlobales();
    }

    // ==================== PRUEBAS DE ESTADÍSTICAS POR PROCESO ====================

    @Test
    @DisplayName("Test 6: Obtener estadísticas por proceso - Delega correctamente")
    void testObtenerEstadisticasPorProcesoDelegaCorrectamente() {
        // Arrange
        String tipoProceso = "PAZ_Y_SALVO";
        Map<String, Object> estadisticasEsperadas = new HashMap<>();
        estadisticasEsperadas.put("total", 50);
        when(estadisticasGateway.obtenerEstadisticasPorProceso(tipoProceso))
                .thenReturn(estadisticasEsperadas);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerEstadisticasPorProceso(tipoProceso);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.get("total")).isEqualTo(50);
        verify(estadisticasGateway, times(1)).obtenerEstadisticasPorProceso(tipoProceso);
    }

    // ==================== PRUEBAS DE ESTADÍSTICAS POR ESTADO ====================

    @Test
    @DisplayName("Test 7: Obtener estadísticas por estado - Delega correctamente")
    void testObtenerEstadisticasPorEstadoDelegaCorrectamente() {
        // Arrange
        String estado = "APROBADO";
        Map<String, Object> estadisticasEsperadas = new HashMap<>();
        estadisticasEsperadas.put("cantidad", 30);
        when(estadisticasGateway.obtenerEstadisticasPorEstado(estado))
                .thenReturn(estadisticasEsperadas);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerEstadisticasPorEstado(estado);

        // Assert
        assertThat(resultado).isNotNull();
        verify(estadisticasGateway, times(1)).obtenerEstadisticasPorEstado(estado);
    }

    // ==================== PRUEBAS DE ESTADÍSTICAS POR PROGRAMA ====================

    @Test
    @DisplayName("Test 8: Obtener estadísticas por programa - Delega correctamente")
    void testObtenerEstadisticasPorProgramaDelegaCorrectamente() {
        // Arrange
        int idPrograma = 1;
        Map<String, Object> estadisticasEsperadas = new HashMap<>();
        estadisticasEsperadas.put("solicitudes", 25);
        when(estadisticasGateway.obtenerEstadisticasPorPrograma(idPrograma))
                .thenReturn(estadisticasEsperadas);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerEstadisticasPorPrograma(idPrograma);

        // Assert
        assertThat(resultado).isNotNull();
        verify(estadisticasGateway, times(1)).obtenerEstadisticasPorPrograma(idPrograma);
    }

    // ==================== PRUEBAS DE MANEJO DE EXCEPCIONES ====================

    @Test
    @DisplayName("Test 9: Gateway lanza excepción - La propaga correctamente")
    void testGatewayLanzaExcepcionLaPropagaCorrectamente() {
        // Arrange
        when(estadisticasGateway.obtenerEstadisticasGlobales())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        assertThatThrownBy(() -> estadisticasCU.obtenerEstadisticasGlobales())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de base de datos");
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE COMPORTAMIENTO ====================

    @Test
    @DisplayName("Test 10: Llamadas múltiples al mismo método")
    void testLlamadasMultiplesAlMismoMetodo() {
        // Arrange
        when(estadisticasGateway.obtenerEstadisticasGlobales())
                .thenReturn(new HashMap<>());

        // Act
        for (int i = 0; i < 3; i++) {
            estadisticasCU.obtenerEstadisticasGlobales();
        }

        // Assert
        verify(estadisticasGateway, times(3)).obtenerEstadisticasGlobales();
    }

    @Test
    @DisplayName("Test 11: Verificar que el adaptador implementa la interfaz correcta")
    void testAdaptadorImplementaInterfazCorrecta() {
        // Assert
        assertThat(estadisticasCU).isInstanceOf(GestionarEstadisticasCUIntPort.class);
    }

    @Test
    @DisplayName("Test 12: Estadísticas globales retorna la misma referencia del gateway")
    void testEstadisticasGlobalesRetornaLaMismaReferencia() {
        // Arrange
        Map<String, Object> mapOriginal = new HashMap<>();
        mapOriginal.put("total", 100);
        when(estadisticasGateway.obtenerEstadisticasGlobales()).thenReturn(mapOriginal);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerEstadisticasGlobales();

        // Assert - Verificar que es la misma instancia
        assertThat(resultado).isSameAs(mapOriginal);
    }

    // ==================== PRUEBAS DE ESTADÍSTICAS ADICIONALES ====================

    @Test
    @DisplayName("Test 13: Obtener tendencias y comparativas - Delega correctamente")
    void testObtenerTendenciasYComparativasDelegaCorrectamente() {
        // Arrange
        Map<String, Object> tendenciasEsperadas = new HashMap<>();
        tendenciasEsperadas.put("tendencia", "ascendente");
        when(estadisticasGateway.obtenerTendenciasYComparativas())
                .thenReturn(tendenciasEsperadas);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerTendenciasYComparativas();

        // Assert
        assertThat(resultado).isNotNull();
        verify(estadisticasGateway, times(1)).obtenerTendenciasYComparativas();
    }

    @Test
    @DisplayName("Test 14: Obtener tiempo promedio procesamiento - Delega correctamente")
    void testObtenerTiempoPromedioProcesamientoDelegaCorrectamente() {
        // Arrange
        Map<String, Object> tiempoEsperado = new HashMap<>();
        tiempoEsperado.put("promedioHoras", 48);
        when(estadisticasGateway.obtenerTiempoPromedioProcesamiento())
                .thenReturn(tiempoEsperado);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerTiempoPromedioProcesamiento();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.get("promedioHoras")).isEqualTo(48);
        verify(estadisticasGateway, times(1)).obtenerTiempoPromedioProcesamiento();
    }

    @Test
    @DisplayName("Test 15: Obtener estadísticas cursos verano - Delega correctamente")
    void testObtenerEstadisticasCursosVeranoDelegaCorrectamente() {
        // Arrange
        Map<String, Object> estadisticasCursos = new HashMap<>();
        estadisticasCursos.put("totalCursos", 15);
        when(estadisticasGateway.obtenerEstadisticasCursosVerano(null, null))
                .thenReturn(estadisticasCursos);

        // Act
        Map<String, Object> resultado = estadisticasCU.obtenerEstadisticasCursosVerano(null, null);

        // Assert
        assertThat(resultado).isNotNull();
        verify(estadisticasGateway, times(1)).obtenerEstadisticasCursosVerano(null, null);
    }
}

