package co.edu.unicauca.decanatura.gestion_curricular.homologacion;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudHomologacionCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
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
 * PRUEBAS UNITARIAS - MÓDULO HOMOLOGACIÓN
 * ============================================================
 * 
 * Objetivo: Validar la lógica de negocio del módulo de Homologación
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
@DisplayName("Pruebas Unitarias - Lógica de Negocio Homologación")
class HomologacionUnidadTest {

    @Mock
    private GestionarSolicitudHomologacionGatewayIntPort solicitudGateway;

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

    private GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;

    private SolicitudHomologacion solicitudEjemplo;

    @BeforeEach
    void setUp() {
        // Crear el CU Adapter con TODAS las dependencias mockeadas
        solicitudHomologacionCU = new GestionarSolicitudHomologacionCUAdapter(
                formateadorResultados,
                solicitudGateway,
                usuarioGateway,
                documentosGateway,
                estadoSolicitudGateway,
                notificacionCU,
                gestionarArchivos
        );

        // Crear solicitud de ejemplo
        solicitudEjemplo = new SolicitudHomologacion();
    }

    // ==================== PRUEBAS DE GUARDADO ====================

    @Test
    @DisplayName("Test 1: Guardar solicitud - Requiere usuario válido")
    void testGuardarSolicitudRequiereUsuarioValido() {
        // Arrange - Solicitud sin usuario
        SolicitudHomologacion solicitudSinUsuario = new SolicitudHomologacion();

        // Act & Assert - Debe lanzar una excepción porque la solicitud no tiene usuario
        assertThatThrownBy(() -> solicitudHomologacionCU.guardar(solicitudSinUsuario))
                .hasMessageContaining("usuario"); 
    }

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("Test 2: Listar solicitudes - Retorna lista del gateway")
    void testListarSolicitudesRetornaListaGateway() {
        // Arrange
        List<SolicitudHomologacion> listaEsperada = Arrays.asList(
                new SolicitudHomologacion(),
                new SolicitudHomologacion()
        );
        when(solicitudGateway.listarSolicitudes()).thenReturn(listaEsperada);

        // Act
        List<SolicitudHomologacion> resultado = solicitudHomologacionCU.listarSolicitudes();

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
        List<SolicitudHomologacion> resultado = solicitudHomologacionCU.listarSolicitudes();

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
        SolicitudHomologacion resultado = solicitudHomologacionCU.buscarPorId(idBuscado);

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
        assertThatThrownBy(() -> solicitudHomologacionCU.buscarPorId(idInexistente))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    // ==================== PRUEBAS DE FILTRADO POR ROL ====================

    @Test
    @DisplayName("Test 6: Listar solicitudes para Funcionario - Delega correctamente")
    void testListarSolicitudesParaFuncionarioDelegaCorrectamente() {
        // Arrange
        List<SolicitudHomologacion> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesToFuncionario())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudHomologacion> resultado = solicitudHomologacionCU.listarSolicitudesToFuncionario();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(solicitudGateway, times(1)).listarSolicitudesToFuncionario();
    }

    @Test
    @DisplayName("Test 7: Listar solicitudes para Coordinador - Delega correctamente")
    void testListarSolicitudesParaCoordinadorDelegaCorrectamente() {
        // Arrange
        List<SolicitudHomologacion> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesToCoordinador())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudHomologacion> resultado = solicitudHomologacionCU.listarSolicitudesToCoordinador();

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).listarSolicitudesToCoordinador();
    }

    @Test
    @DisplayName("Test 8: Listar solicitudes para Secretaria - Delega correctamente")
    void testListarSolicitudesParaSecretariaDelegaCorrectamente() {
        // Arrange
        List<SolicitudHomologacion> listaEsperada = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudesToSecretaria())
                .thenReturn(listaEsperada);

        // Act
        List<SolicitudHomologacion> resultado = solicitudHomologacionCU.listarSolicitudesToSecretaria();

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudGateway, times(1)).listarSolicitudesToSecretaria();
    }

    // ==================== PRUEBAS DE FILTRADO POR ROL Y USUARIO ====================

    @Test
    @DisplayName("Test 9: Listar solicitudes por rol ESTUDIANTE - Filtra correctamente")
    void testListarSolicitudesPorRolEstudiante() {
        // Arrange
        SolicitudHomologacion solicitud1 = new SolicitudHomologacion();
        SolicitudHomologacion solicitud2 = new SolicitudHomologacion();
        
        // Necesitamos mockear usuarios para las solicitudes
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario1 = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
        usuario1.setId_usuario(1);
        
        co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario usuario2 = 
            new co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario();
        usuario2.setId_usuario(2);
        
        solicitud1.setObjUsuario(usuario1);
        solicitud2.setObjUsuario(usuario2);
        
        List<SolicitudHomologacion> todasLasSolicitudes = Arrays.asList(solicitud1, solicitud2);
        when(solicitudGateway.listarSolicitudes()).thenReturn(todasLasSolicitudes);

        // Act
        List<SolicitudHomologacion> resultado = solicitudHomologacionCU.listarSolicitudesPorRol("ESTUDIANTE", 1);

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
        when(solicitudGateway.listarSolicitudes())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        assertThatThrownBy(() -> solicitudHomologacionCU.listarSolicitudes())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de base de datos");
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE COMPORTAMIENTO ====================

    @Test
    @DisplayName("Test 11: Llamadas múltiples al mismo método")
    void testLlamadasMultiplesAlMismoMetodo() {
        // Arrange
        when(solicitudGateway.listarSolicitudes())
                .thenReturn(Arrays.asList(solicitudEjemplo));

        // Act
        for (int i = 0; i < 3; i++) {
            solicitudHomologacionCU.listarSolicitudes();
        }

        // Assert
        verify(solicitudGateway, times(3)).listarSolicitudes();
    }

    @Test
    @DisplayName("Test 12: Verificar que el adaptador implementa la interfaz correcta")
    void testAdaptadorImplementaInterfazCorrecta() {
        // Assert
        assertThat(solicitudHomologacionCU).isInstanceOf(GestionarSolicitudHomologacionCUIntPort.class);
    }

    @Test
    @DisplayName("Test 13: Lista solicitudes retorna la misma referencia del gateway")
    void testListaSolicitudesRetornaLaMismaReferencia() {
        // Arrange
        List<SolicitudHomologacion> listaOriginal = Arrays.asList(solicitudEjemplo);
        when(solicitudGateway.listarSolicitudes()).thenReturn(listaOriginal);

        // Act
        List<SolicitudHomologacion> resultado = solicitudHomologacionCU.listarSolicitudes();

        // Assert - Verificar que es la misma instancia (no se hace copia)
        assertThat(resultado).isSameAs(listaOriginal);
    }

    // ==================== PRUEBAS DE CAMBIO DE ESTADO ====================

    @Test
    @DisplayName("Test 14: Cambiar estado solicitud - Delega correctamente")
    void testCambiarEstadoSolicitudDelegaCorrectamente() {
        // Arrange - el CU puede llamar a buscarPorId antes de cambiar estado
        int idSolicitud = 1;
        String nuevoEstado = "APROBADA_FUNCIONARIO";
        when(solicitudGateway.buscarPorId(idSolicitud)).thenReturn(Optional.of(solicitudEjemplo));

        // Act
        solicitudHomologacionCU.cambiarEstadoSolicitud(idSolicitud, nuevoEstado);

        // Assert
        verify(solicitudGateway, times(1)).cambiarEstadoSolicitud(eq(idSolicitud), any());
    }
}

