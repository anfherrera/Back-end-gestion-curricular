package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudPazYSalvoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
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
 * PRUEBAS UNITARIAS - MÓDULO PAZ Y SALVO (GEPA4)
 * ============================================================
 * 
 * Objetivo: Validar la lógica de negocio del módulo de Paz y Salvo
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
@DisplayName("Pruebas Unitarias - Lógica de Negocio Paz y Salvo")
class PazYSalvoUnidadTest {

    @Mock
    private GestionarSolicitudPazYSalvoGatewayIntPort solicitudGateway;

    @Mock
    private GestionarUsuarioGatewayIntPort usuarioGateway;

    @Mock
    private GestionarDocumentosGatewayIntPort documentosGateway;

    @Mock
    private GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway;

    @Mock
    private FormateadorResultadosIntPort formateadorResultados;

    @Mock
    private GestionarArchivosCUIntPort gestionarArchivos;

    private GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU;

    private SolicitudPazYSalvo solicitudEjemplo;

    @BeforeEach
    void setUp() {
        // Crear el CU Adapter con TODAS las dependencias mockeadas
        solicitudPazYSalvoCU = new GestionarSolicitudPazYSalvoCUAdapter(
                solicitudGateway,
                usuarioGateway,
                documentosGateway,
                estadoSolicitudGateway,
                formateadorResultados,
                gestionarArchivos
        );

        // Crear solicitud de ejemplo
        solicitudEjemplo = new SolicitudPazYSalvo();
    }

    // ==================== PRUEBAS DE GUARDADO ====================

    @Test
    @DisplayName("Test 1: Guardar solicitud - Requiere usuario válido")
    void testGuardarSolicitudRequiereUsuarioValido() {
        // Arrange - Solicitud sin usuario
        SolicitudPazYSalvo solicitudSinUsuario = new SolicitudPazYSalvo();

        // Act & Assert - Debe lanzar una excepción porque la solicitud no tiene usuario
        assertThatThrownBy(() -> solicitudPazYSalvoCU.guardar(solicitudSinUsuario))
                .hasMessageContaining("usuario"); 
    }

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("Test 2: Listar solicitudes - Retorna lista del gateway")
    void testListarSolicitudesRetornaListaGateway() {
        // Arrange
        List<SolicitudPazYSalvo> listaEsperada = Arrays.asList(
                new SolicitudPazYSalvo(),
                new SolicitudPazYSalvo()
        );
        when(solicitudGateway.listarSolicitudes()).thenReturn(listaEsperada);

        // Act
        List<SolicitudPazYSalvo> resultado = solicitudPazYSalvoCU.listarSolicitudes();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(solicitudGateway, times(1)).listarSolicitudes();
    }

    @Test
    @DisplayName("Test 3: Listar solicitudes vacías - Retorna lista vacía")
    void testListarSolicitudesVaciasRetornaListaVacia() {
        // Arrange
        when(solicitudGateway.listarSolicitudes()).thenReturn(Collections.emptyList());

        // Act
        List<SolicitudPazYSalvo> resultado = solicitudPazYSalvoCU.listarSolicitudes();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Test 4: Buscar por ID - Delega correctamente")
    void testBuscarPorIdDelegaCorrectamente() {
        // Arrange
        int idBuscado = 1;
        when(solicitudGateway.buscarPorId(idBuscado))
                .thenReturn(Optional.of(solicitudEjemplo));

        // Act
        SolicitudPazYSalvo resultado = solicitudPazYSalvoCU.buscarPorId(idBuscado);

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).buscarPorId(idBuscado);
    }

    @Test
    @DisplayName("Test 5: Buscar por ID inexistente - Lanza excepción")
    void testBuscarPorIdInexistenteLanzaExcepcion() {
        // Arrange
        int idInexistente = 999;
        when(solicitudGateway.buscarPorId(idInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert - El método lanza RuntimeException si no encuentra
        assertThatThrownBy(() -> solicitudPazYSalvoCU.buscarPorId(idInexistente))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    // ==================== PRUEBAS DE FILTRADO POR ROL ====================

    @Test
    @DisplayName("Test 6: Listar solicitudes para Funcionario - Delega correctamente")
    void testListarSolicitudesParaFuncionarioDelegaCorrectamente() {
        // Arrange
        List<SolicitudPazYSalvo> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesToFuncionario())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudPazYSalvo> resultado = solicitudPazYSalvoCU.listarSolicitudesToFuncionario();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(solicitudGateway, times(1)).listarSolicitudesToFuncionario();
    }

    @Test
    @DisplayName("Test 7: Listar solicitudes para Coordinador - Delega correctamente")
    void testListarSolicitudesParaCoordinadorDelegaCorrectamente() {
        // Arrange
        List<SolicitudPazYSalvo> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesToCoordinador())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudPazYSalvo> resultado = solicitudPazYSalvoCU.listarSolicitudesToCoordinador();

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).listarSolicitudesToCoordinador();
    }

    @Test
    @DisplayName("Test 8: Listar solicitudes para Secretaria - Delega correctamente")
    void testListarSolicitudesParaSecretariaDelegaCorrectamente() {
        // Arrange
        List<SolicitudPazYSalvo> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesToSecretaria())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudPazYSalvo> resultado = solicitudPazYSalvoCU.listarSolicitudesToSecretaria();

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).listarSolicitudesToSecretaria();
    }

    // ==================== PRUEBAS DE MANEJO DE EXCEPCIONES ====================

    @Test
    @DisplayName("Test 9: Gateway lanza excepción - La propaga correctamente")
    void testGatewayLanzaExcepcionLaPropagaCorrectamente() {
        // Arrange
        when(solicitudGateway.listarSolicitudes())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        assertThatThrownBy(() -> solicitudPazYSalvoCU.listarSolicitudes())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de base de datos");
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE COMPORTAMIENTO ====================

    @Test
    @DisplayName("Test 10: Llamadas múltiples al mismo método")
    void testLlamadasMultiplesAlMismoMetodo() {
        // Arrange
        when(solicitudGateway.listarSolicitudes())
                .thenReturn(Arrays.asList(solicitudEjemplo));

        // Act
        for (int i = 0; i < 3; i++) {
            solicitudPazYSalvoCU.listarSolicitudes();
        }

        // Assert
        verify(solicitudGateway, times(3)).listarSolicitudes();
    }

    @Test
    @DisplayName("Test 11: Verificar que el adaptador implementa la interfaz correcta")
    void testAdaptadorImplementaInterfazCorrecta() {
        // Assert
        assertThat(solicitudPazYSalvoCU).isInstanceOf(GestionarSolicitudPazYSalvoCUIntPort.class);
    }

    @Test
    @DisplayName("Test 12: Lista solicitudes retorna la misma referencia del gateway")
    void testListaSolicitudesRetornaLaMismaReferencia() {
        // Arrange
        List<SolicitudPazYSalvo> listaOriginal = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudes()).thenReturn(listaOriginal);

        // Act
        List<SolicitudPazYSalvo> resultado = solicitudPazYSalvoCU.listarSolicitudes();

        // Assert - Verificar que es la misma instancia (no se hace copia)
        assertThat(resultado).isSameAs(listaOriginal);
    }
}

