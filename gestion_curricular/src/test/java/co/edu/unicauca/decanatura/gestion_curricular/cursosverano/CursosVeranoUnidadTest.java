package co.edu.unicauca.decanatura.gestion_curricular.cursosverano;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
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
import static org.mockito.Mockito.doThrow;

/**
 * ============================================================
 * PRUEBAS UNITARIAS - MÓDULO CURSOS DE VERANO (GCV5)
 * ============================================================
 *
 * Objetivo: Validar la lógica de negocio del módulo de Cursos de Verano
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
@DisplayName("Pruebas Unitarias - Lógica de Negocio Cursos de Verano")
class CursosVeranoUnidadTest {

    @Mock
    private GestionarCursoOfertadoVeranoGatewayIntPort cursoVeranoGateway;

    @Mock
    private GestionarSolicitudGatewayIntPort solicitudGateway;

    @Mock
    private GestionarUsuarioGatewayIntPort usuarioGateway;

    @Mock
    private GestionarMateriasIntPort materiasGateway;

    @Mock
    private GestionarDocenteGatewayIntPort docenteGateway;

    @Mock
    private FormateadorResultadosIntPort formateadorResultados;

    @Mock
    private GestionarNotificacionCUIntPort notificacionCU;

    private GestionarCursoOfertadoVeranoCUIntPort cursosVeranoCU;

    private CursoOfertadoVerano cursoEjemplo;

    @BeforeEach
    void setUp() {
        // Crear el CU Adapter con TODAS las dependencias mockeadas
        cursosVeranoCU = new GestionarCursoOfertadoVeranoCUAdapter(
                cursoVeranoGateway,
                solicitudGateway,
                usuarioGateway,
                materiasGateway,
                docenteGateway,
                formateadorResultados,
                notificacionCU
        );

        // Crear curso de ejemplo
        cursoEjemplo = new CursoOfertadoVerano();
        cursoEjemplo.setId_curso(1);
        cursoEjemplo.setCupo_estimado(30);
        cursoEjemplo.setSalon("A-101");
        
        Materia materia = new Materia();
        materia.setId_materia(1);
        materia.setNombre("Cálculo I");
        cursoEjemplo.setObjMateria(materia);
        
        Docente docente = new Docente();
        docente.setId_docente(1);
        docente.setNombre_docente("Juan Pérez");
        cursoEjemplo.setObjDocente(docente);
    }

    // ==================== PRUEBAS DE LISTADO ====================

    @Test
    @DisplayName("Test 1: Listar todos los cursos - Retorna lista del gateway")
    void testListarTodosCursosRetornaListaGateway() {
        // Arrange
        List<CursoOfertadoVerano> listaEsperada = Arrays.asList(
                cursoEjemplo,
                new CursoOfertadoVerano()
        );
        when(cursoVeranoGateway.listarTodos()).thenReturn(listaEsperada);

        // Act
        List<CursoOfertadoVerano> resultado = cursosVeranoCU.listarTodos();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(cursoVeranoGateway, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Test 2: Listar cursos vacíos - Retorna lista vacía")
    void testListarCursosVaciosRetornaListaVacia() {
        // Arrange
        when(cursoVeranoGateway.listarTodos()).thenReturn(Collections.emptyList());

        // Act
        List<CursoOfertadoVerano> resultado = cursosVeranoCU.listarTodos();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(cursoVeranoGateway, times(1)).listarTodos();
    }

    // ==================== PRUEBAS DE CONSULTA POR ID ====================

    @Test
    @DisplayName("Test 3: Obtener curso por ID - Delega correctamente")
    void testObtenerCursoPorIdDelegaCorrectamente() {
        // Arrange
        int idBuscado = 1;
        when(cursoVeranoGateway.obtenerCursoPorId(idBuscado))
                .thenReturn(cursoEjemplo);

        // Act
        CursoOfertadoVerano resultado = cursosVeranoCU.obtenerCursoPorId(idBuscado);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId_curso()).isEqualTo(1);
        // El adapter llama al gateway 1 vez
        verify(cursoVeranoGateway, times(1)).obtenerCursoPorId(idBuscado);
    }

    @Test
    @DisplayName("Test 4: Obtener curso por ID inexistente - Gateway lanza excepción")
    void testObtenerCursoPorIdInexistenteLanzaExcepcion() {
        // Arrange
        int idInexistente = 999;
        when(cursoVeranoGateway.obtenerCursoPorId(idInexistente))
                .thenReturn(null);
        doThrow(new RuntimeException("No se encuentra el curso"))
                .when(formateadorResultados).retornarRespuestaErrorEntidadNoExiste(anyString());

        // Act & Assert
        assertThatThrownBy(() -> cursosVeranoCU.obtenerCursoPorId(idInexistente))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encuentra el curso");
        verify(cursoVeranoGateway, atLeastOnce()).obtenerCursoPorId(idInexistente);
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("Test 5: Crear curso - Delega correctamente al gateway")
    void testCrearCursoDelegaCorrectamente() {
        // Arrange
        when(cursoVeranoGateway.crearCurso(any(CursoOfertadoVerano.class)))
                .thenReturn(cursoEjemplo);

        // Act
        CursoOfertadoVerano resultado = cursosVeranoCU.crearCurso(cursoEjemplo);

        // Assert
        assertThat(resultado).isNotNull();
        verify(cursoVeranoGateway, times(1)).crearCurso(cursoEjemplo);
    }

    // ==================== PRUEBAS DE ELIMINACIÓN ====================

    @Test
    @DisplayName("Test 6: Eliminar curso existente - Retorna true")
    void testEliminarCursoExistenteRetornaTrue() {
        // Arrange
        int idCurso = 1;
        when(cursoVeranoGateway.eliminarCurso(idCurso)).thenReturn(true);

        // Act
        boolean resultado = cursosVeranoCU.eliminarCurso(idCurso);

        // Assert
        assertThat(resultado).isTrue();
        verify(cursoVeranoGateway, times(1)).eliminarCurso(idCurso);
    }

    @Test
    @DisplayName("Test 7: Eliminar curso inexistente - Retorna false")
    void testEliminarCursoInexistenteRetornaFalse() {
        // Arrange
        int idInexistente = 999;
        when(cursoVeranoGateway.eliminarCurso(idInexistente)).thenReturn(false);

        // Act
        boolean resultado = cursosVeranoCU.eliminarCurso(idInexistente);

        // Assert
        assertThat(resultado).isFalse();
        verify(cursoVeranoGateway, times(1)).eliminarCurso(idInexistente);
    }

    // ==================== PRUEBAS DE ACTUALIZACIÓN ====================

    @Test
    @DisplayName("Test 8: Actualizar curso - Requiere curso válido con ID")
    void testActualizarCursoRequiereCursoValidoConId() {
        // Arrange - Curso sin ID
        CursoOfertadoVerano cursoSinId = new CursoOfertadoVerano();
        EstadoCursoOfertado estado = new EstadoCursoOfertado();
        estado.setId_estado(1);
        estado.setEstado_actual("Abierto");
        
        doThrow(new RuntimeException("No hay id en el curso"))
                .when(formateadorResultados).retornarRespuestaErrorEntidadExiste("No hay id en el curso");
        
        // Act & Assert
        assertThatThrownBy(() -> cursosVeranoCU.actualizarCurso(cursoSinId, estado))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay id en el curso");
        
        // Verificar que NO se llamó al gateway porque el curso no tiene ID
        verify(cursoVeranoGateway, never()).obtenerCursoPorId(anyInt());
    }

    // ==================== PRUEBAS DE MANEJO DE EXCEPCIONES ====================

    @Test
    @DisplayName("Test 9: Gateway lanza excepción - La propaga correctamente")
    void testGatewayLanzaExcepcionLaPropagaCorrectamente() {
        // Arrange
        when(cursoVeranoGateway.listarTodos())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        assertThatThrownBy(() -> cursosVeranoCU.listarTodos())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de base de datos");
        verify(cursoVeranoGateway, times(1)).listarTodos();
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE COMPORTAMIENTO ====================

    @Test
    @DisplayName("Test 10: Llamadas múltiples al mismo método")
    void testLlamadasMultiplesAlMismoMetodo() {
        // Arrange
        when(cursoVeranoGateway.listarTodos())
                .thenReturn(Arrays.asList(cursoEjemplo));

        // Act
        for (int i = 0; i < 3; i++) {
            cursosVeranoCU.listarTodos();
        }

        // Assert
        verify(cursoVeranoGateway, times(3)).listarTodos();
    }

    @Test
    @DisplayName("Test 11: Verificar que el adaptador implementa la interfaz correcta")
    void testAdaptadorImplementaInterfazCorrecta() {
        // Assert
        assertThat(cursosVeranoCU).isInstanceOf(GestionarCursoOfertadoVeranoCUIntPort.class);
    }

    @Test
    @DisplayName("Test 12: Lista cursos retorna la misma referencia del gateway")
    void testListaCursosRetornaLaMismaReferencia() {
        // Arrange
        List<CursoOfertadoVerano> listaOriginal = Arrays.asList(cursoEjemplo);
        when(cursoVeranoGateway.listarTodos()).thenReturn(listaOriginal);

        // Act
        List<CursoOfertadoVerano> resultado = cursosVeranoCU.listarTodos();

        // Assert - Verificar que es la misma instancia (no se hace copia)
        assertThat(resultado).isSameAs(listaOriginal);
    }

    @Test
    @DisplayName("Test 13: Verificar interacción con múltiples gateways en operación compleja")
    void testVerificarInteraccionMultiplesGateways() {
        // Arrange
        when(cursoVeranoGateway.crearCurso(any(CursoOfertadoVerano.class)))
                .thenReturn(cursoEjemplo);

        // Act
        CursoOfertadoVerano resultado = cursosVeranoCU.crearCurso(cursoEjemplo);

        // Assert
        assertThat(resultado).isNotNull();
        verify(cursoVeranoGateway, times(1)).crearCurso(any(CursoOfertadoVerano.class));
        // Verificar que NO se llamaron otros gateways innecesariamente
        verify(solicitudGateway, never()).listarSolicitudes();
        verify(usuarioGateway, never()).listarUsuarios();
    }
}

