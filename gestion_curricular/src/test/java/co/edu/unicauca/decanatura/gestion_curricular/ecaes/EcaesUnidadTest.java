package co.edu.unicauca.decanatura.gestion_curricular.ecaes;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudEcaesCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 * PRUEBAS UNITARIAS - MÓDULO ECAES
 * ============================================================
 * 
 * Objetivo: Validar la lógica de negocio del módulo de ECAES
 * de forma aislada, usando mocks para las dependencias.
 * 
 * Tipo de pruebas: UNITARIAS
 * - Prueban componentes individuales (CU Adapter)
 * - Usan mocks (Mockito) para dependencias (Gateway)
 * - No requieren Spring Context
 * - Son muy rápidas (~50ms)
 * - Validan delegación correcta al gateway
 * 
 * @author Daniel
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Lógica de Negocio ECAES")
class EcaesUnidadTest {

    @Mock
    private GestionarPreRegistroEcaesGatewayIntPort solicitudGateway;

    @Mock
    private GestionarUsuarioGatewayIntPort usuarioGateway;

    @Mock
    private GestionarDocumentosGatewayIntPort documentosGateway;

    @Mock
    private GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway;

    @Mock
    private FormateadorResultadosIntPort formateadorResultados;

    @Mock
    private GestionarNotificacionCUIntPort notificacionCU;

    @Mock
    private GestionarArchivosCUIntPort gestionarArchivos;

    private GestionarSolicitudEcaesCUIntPort solicitudEcaesCU;

    private SolicitudEcaes solicitudEjemplo;

    @BeforeEach
    void setUp() {
        // Crear el CU Adapter con TODAS las dependencias mockeadas
        solicitudEcaesCU = new GestionarSolicitudEcaesCUAdapter(
                solicitudGateway,
                formateadorResultados,
                documentosGateway,
                estadoSolicitudGateway,
                usuarioGateway,
                notificacionCU,
                gestionarArchivos
        );

        // Crear solicitud de ejemplo
        solicitudEjemplo = new SolicitudEcaes();
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("Test 1: Crear solicitud - Requiere usuario válido")
    void testCrearSolicitudRequiereUsuarioValido() {
        // Arrange - Solicitud sin usuario
        SolicitudEcaes solicitudSinUsuario = new SolicitudEcaes();

        // Act & Assert - Debe lanzar una excepción porque la solicitud no tiene usuario
        assertThatThrownBy(() -> solicitudEcaesCU.guardar(solicitudSinUsuario))
                .hasMessageContaining("usuario"); 
    }

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("Test 2: Listar solicitudes - Retorna lista del gateway")
    void testListarSolicitudesRetornaListaGateway() {
        // Arrange
        List<SolicitudEcaes> listaEsperada = Arrays.asList(
                new SolicitudEcaes(),
                new SolicitudEcaes()
        );
        when(solicitudGateway.listar()).thenReturn(listaEsperada);

        // Act
        List<SolicitudEcaes> resultado = solicitudEcaesCU.listarSolicitudes();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(solicitudGateway, times(1)).listar();
    }

    @Test
    @DisplayName("Test 3: Listar solicitudes vacías - Retorna lista vacía")
    void testListarSolicitudesVaciasRetornaListaVacia() {
        // Arrange
        when(solicitudGateway.listar()).thenReturn(Collections.emptyList());

        // Act
        List<SolicitudEcaes> resultado = solicitudEcaesCU.listarSolicitudes();

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
        SolicitudEcaes resultado = solicitudEcaesCU.buscarPorId(idBuscado);

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
        assertThatThrownBy(() -> solicitudEcaesCU.buscarPorId(idInexistente))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    // ==================== PRUEBAS DE FECHAS ECAES ====================

    @Test
    @DisplayName("Test 6: Publicar fechas ECAES - Delega correctamente")
    void testPublicarFechasEcaesDelegaCorrectamente() {
        // Arrange
        FechaEcaes fechaEcaes = new FechaEcaes();
        fechaEcaes.setPeriodoAcademico("2025-1");
        fechaEcaes.setInscripcion_est_by_facultad(LocalDate.of(2025, 2, 1));
        fechaEcaes.setAplicacion(LocalDate.of(2025, 3, 10));
        
        when(solicitudGateway.publicarFechasEcaes(fechaEcaes)).thenReturn(fechaEcaes);

        // Act
        FechaEcaes resultado = solicitudEcaesCU.publicarFechasEcaes(fechaEcaes);

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).publicarFechasEcaes(fechaEcaes);
    }

    @Test
    @DisplayName("Test 7: Listar fechas ECAES - Retorna lista del gateway")
    void testListarFechasEcaesRetornaListaGateway() {
        // Arrange
        List<FechaEcaes> listaEsperada = Arrays.asList(
                new FechaEcaes(),
                new FechaEcaes()
        );
        when(solicitudGateway.listarFechasEcaes()).thenReturn(listaEsperada);

        // Act
        List<FechaEcaes> resultado = solicitudEcaesCU.listarFechasEcaes();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(solicitudGateway, times(1)).listarFechasEcaes();
    }

    @Test
    @DisplayName("Test 8: Buscar fechas por período - Delega correctamente")
    void testBuscarFechasPorPeriodoDelegaCorrectamente() {
        // Arrange
        String periodo = "2025-1";
        FechaEcaes fechaEcaes = new FechaEcaes();
        fechaEcaes.setPeriodoAcademico(periodo);
        
        when(solicitudGateway.buscarFechasPorPeriodo(periodo))
                .thenReturn(Optional.of(fechaEcaes));

        // Act
        Optional<FechaEcaes> resultado = solicitudEcaesCU.buscarFechasPorPeriodo(periodo);

        // Assert
        assertThat(resultado).isPresent();
        verify(solicitudGateway, times(1)).buscarFechasPorPeriodo(periodo);
    }

    @Test
    @DisplayName("Test 9: Actualizar fechas ECAES - Delega correctamente")
    void testActualizarFechasEcaesDelegaCorrectamente() {
        // Arrange
        FechaEcaes fechaEcaes = new FechaEcaes();
        fechaEcaes.setIdFechaEcaes(1);
        fechaEcaes.setPeriodoAcademico("2025-1");
        
        when(solicitudGateway.actualizarFechasEcaes(fechaEcaes)).thenReturn(fechaEcaes);

        // Act
        FechaEcaes resultado = solicitudEcaesCU.actualizarFechasEcaes(fechaEcaes);

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).actualizarFechasEcaes(fechaEcaes);
    }

    // ==================== PRUEBAS DE FILTRADO POR ROL ====================

    @Test
    @DisplayName("Test 10: Listar solicitudes para Funcionario - Delega correctamente")
    void testListarSolicitudesParaFuncionarioDelegaCorrectamente() {
        // Arrange
        List<SolicitudEcaes> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesToFuncionario())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudEcaes> resultado = solicitudEcaesCU.listarSolicitudesToFuncionario();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(solicitudGateway, times(1)).listarSolicitudesToFuncionario();
    }

    // ==================== PRUEBAS DE CAMBIO DE ESTADO ====================

    @Test
    @DisplayName("Test 11: Cambiar estado solicitud - Delega correctamente")
    void testCambiarEstadoSolicitudDelegaCorrectamente() {
        // Arrange - el CU llama a buscarPorId antes de cambiar estado
        int idSolicitud = 1;
        String nuevoEstado = "preRegistrado";
        when(solicitudGateway.buscarPorId(idSolicitud)).thenReturn(Optional.of(solicitudEjemplo));

        // Act
        solicitudEcaesCU.cambiarEstadoSolicitud(idSolicitud, nuevoEstado);

        // Assert
        verify(solicitudGateway, times(1)).cambiarEstadoSolicitudEcaes(eq(idSolicitud), any());
    }

    // ==================== PRUEBAS DE MANEJO DE EXCEPCIONES ====================

    @Test
    @DisplayName("Test 12: Gateway lanza excepción - La propaga correctamente")
    void testGatewayLanzaExcepcionLaPropagaCorrectamente() {
        // Arrange
        when(solicitudGateway.listar())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        assertThatThrownBy(() -> solicitudEcaesCU.listarSolicitudes())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de base de datos");
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE COMPORTAMIENTO ====================

    @Test
    @DisplayName("Test 13: Verificar que el adaptador implementa la interfaz correcta")
    void testAdaptadorImplementaInterfazCorrecta() {
        // Assert
        assertThat(solicitudEcaesCU).isInstanceOf(GestionarSolicitudEcaesCUIntPort.class);
    }

    @Test
    @DisplayName("Test 14: Lista solicitudes retorna la misma referencia del gateway")
    void testListaSolicitudesRetornaLaMismaReferencia() {
        // Arrange
        List<SolicitudEcaes> listaOriginal = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listar()).thenReturn(listaOriginal);

        // Act
        List<SolicitudEcaes> resultado = solicitudEcaesCU.listarSolicitudes();

        // Assert - Verificar que es la misma instancia (no se hace copia)
        assertThat(resultado).isSameAs(listaOriginal);
    }
}

