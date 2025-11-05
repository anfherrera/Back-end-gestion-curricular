package co.edu.unicauca.decanatura.gestion_curricular.reingreso;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudReingresoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
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
 * PRUEBAS UNITARIAS - MÓDULO REINGRESO
 * ============================================================
 * 
 * Objetivo: Validar la lógica de negocio del módulo de Reingreso
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
@DisplayName("Pruebas Unitarias - Lógica de Negocio Reingreso")
class ReingresoUnidadTest {

    @Mock
    private GestionarSolicitudReingresoGatewayIntPort solicitudGateway;

    @Mock
    private GestionarUsuarioGatewayIntPort usuarioGateway;

    @Mock
    private GestionarDocumentosGatewayIntPort documentosGateway;

    @Mock
    private GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway;

    @Mock
    private FormateadorResultadosIntPort formateadorResultados;

    private GestionarSolicitudReingresoCUIntPort solicitudReingresoCU;

    private SolicitudReingreso solicitudEjemplo;

    @BeforeEach
    void setUp() {
        // Crear el CU Adapter con TODAS las dependencias mockeadas
        solicitudReingresoCU = new GestionarSolicitudReingresoCUAdapter(
                usuarioGateway,
                documentosGateway,
                estadoSolicitudGateway,
                formateadorResultados,
                solicitudGateway
        );

        // Crear solicitud de ejemplo
        solicitudEjemplo = new SolicitudReingreso();
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("Test 1: Crear solicitud - Requiere usuario válido")
    void testCrearSolicitudRequiereUsuarioValido() {
        // Arrange - Solicitud sin usuario
        SolicitudReingreso solicitudSinUsuario = new SolicitudReingreso();

        // Act & Assert - Debe lanzar una excepción porque la solicitud no tiene usuario
        assertThatThrownBy(() -> solicitudReingresoCU.crearSolicitudReingreso(solicitudSinUsuario))
                .hasMessageContaining("usuario"); 
    }

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("Test 2: Listar solicitudes - Retorna lista del gateway")
    void testListarSolicitudesRetornaListaGateway() {
        // Arrange
        List<SolicitudReingreso> listaEsperada = Arrays.asList(
                new SolicitudReingreso(),
                new SolicitudReingreso()
        );
        when(solicitudGateway.listarSolicitudesReingreso()).thenReturn(listaEsperada);

        // Act
        List<SolicitudReingreso> resultado = solicitudReingresoCU.listarSolicitudesReingreso();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(solicitudGateway, times(1)).listarSolicitudesReingreso();
    }

    @Test
    @DisplayName("Test 3: Listar solicitudes vacías - Retorna lista vacía")
    void testListarSolicitudesVaciasRetornaListaVacia() {
        // Arrange
        when(solicitudGateway.listarSolicitudesReingreso()).thenReturn(Collections.emptyList());

        // Act
        List<SolicitudReingreso> resultado = solicitudReingresoCU.listarSolicitudesReingreso();

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
        SolicitudReingreso resultado = solicitudReingresoCU.obtenerSolicitudReingresoPorId(idBuscado);

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
        assertThatThrownBy(() -> solicitudReingresoCU.obtenerSolicitudReingresoPorId(idInexistente))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    // ==================== PRUEBAS DE FILTRADO POR ROL ====================

    @Test
    @DisplayName("Test 6: Listar solicitudes para Funcionario - Delega correctamente")
    void testListarSolicitudesParaFuncionarioDelegaCorrectamente() {
        // Arrange
        List<SolicitudReingreso> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesReingresoToFuncionario())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudReingreso> resultado = solicitudReingresoCU.listarSolicitudesReingresoToFuncionario();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(solicitudGateway, times(1)).listarSolicitudesReingresoToFuncionario();
    }

    @Test
    @DisplayName("Test 7: Listar solicitudes para Coordinador - Delega correctamente")
    void testListarSolicitudesParaCoordinadorDelegaCorrectamente() {
        // Arrange
        List<SolicitudReingreso> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesReingresoToCoordinador())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudReingreso> resultado = solicitudReingresoCU.listarSolicitudesReingresoToCoordinador();

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).listarSolicitudesReingresoToCoordinador();
    }

    @Test
    @DisplayName("Test 8: Listar solicitudes para Secretaria - Delega correctamente")
    void testListarSolicitudesParaSecretariaDelegaCorrectamente() {
        // Arrange
        List<SolicitudReingreso> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesReingresoToSecretaria())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudReingreso> resultado = solicitudReingresoCU.listarSolicitudesReingresoToSecretaria();

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).listarSolicitudesReingresoToSecretaria();
    }

    // ==================== PRUEBAS DE FILTRADO POR ROL Y USUARIO ====================

    @Test
    @DisplayName("Test 9: Listar solicitudes por rol ESTUDIANTE - Filtra correctamente")
    void testListarSolicitudesPorRolEstudiante() {
        // Arrange
        SolicitudReingreso solicitud1 = new SolicitudReingreso();
        SolicitudReingreso solicitud2 = new SolicitudReingreso();
        
        // Necesitamos mockear usuarios para las solicitudes
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario1 = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
        usuario1.setId_usuario(1);
        
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario2 = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
        usuario2.setId_usuario(2);
        
        solicitud1.setObjUsuario(usuario1);
        solicitud2.setObjUsuario(usuario2);
        
        List<SolicitudReingreso> todasLasSolicitudes = Arrays.asList(solicitud1, solicitud2);
        when(solicitudGateway.listarSolicitudesReingreso()).thenReturn(todasLasSolicitudes);

        // Act
        List<SolicitudReingreso> resultado = solicitudReingresoCU.listarSolicitudesReingresoPorRol("ESTUDIANTE", 1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getObjUsuario().getId_usuario()).isEqualTo(1);
    }

    // ==================== PRUEBAS DE MANEJO DE EXCEPCIONES ====================

    @Test
    @DisplayName("Test 10: Gateway lanza excepción - La propaga correctamente")
    void testGatewayLanzaExcepcionLaPropagaCorrectamente() {
        // Arrange
        when(solicitudGateway.listarSolicitudesReingreso())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        assertThatThrownBy(() -> solicitudReingresoCU.listarSolicitudesReingreso())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de base de datos");
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE COMPORTAMIENTO ====================

    @Test
    @DisplayName("Test 11: Llamadas múltiples al mismo método")
    void testLlamadasMultiplesAlMismoMetodo() {
        // Arrange
        when(solicitudGateway.listarSolicitudesReingreso())
                .thenReturn(Arrays.asList(solicitudEjemplo));

        // Act
        for (int i = 0; i < 3; i++) {
            solicitudReingresoCU.listarSolicitudesReingreso();
        }

        // Assert
        verify(solicitudGateway, times(3)).listarSolicitudesReingreso();
    }

    @Test
    @DisplayName("Test 12: Verificar que el adaptador implementa la interfaz correcta")
    void testAdaptadorImplementaInterfazCorrecta() {
        // Assert
        assertThat(solicitudReingresoCU).isInstanceOf(GestionarSolicitudReingresoCUIntPort.class);
    }

    @Test
    @DisplayName("Test 13: Lista solicitudes retorna la misma referencia del gateway")
    void testListaSolicitudesRetornaLaMismaReferencia() {
        // Arrange
        List<SolicitudReingreso> listaOriginal = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesReingreso()).thenReturn(listaOriginal);

        // Act
        List<SolicitudReingreso> resultado = solicitudReingresoCU.listarSolicitudesReingreso();

        // Assert - Verificar que es la misma instancia (no se hace copia)
        assertThat(resultado).isSameAs(listaOriginal);
    }

    // ==================== PRUEBAS DE CAMBIO DE ESTADO ====================

    @Test
    @DisplayName("Test 14: Cambiar estado solicitud - Delega correctamente")
    void testCambiarEstadoSolicitudDelegaCorrectamente() {
        // Arrange
        int idSolicitud = 1;
        String nuevoEstado = "APROBADA_FUNCIONARIO";

        // Act
        solicitudReingresoCU.cambiarEstadoSolicitudReingreso(idSolicitud, nuevoEstado);

        // Assert
        verify(solicitudGateway, times(1)).cambiarEstadoSolicitudReingreso(eq(idSolicitud), any());
    }
}

