package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesContadas;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenCompleto;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaTendencias;

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
 * - Validan las reglas de cálculo con datos de prueba (el gateway es un mock)
 * 
 * @author Andrés Felipe Herrera Artunduaga
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Lógica de Negocio Estadísticas")
class EstadisticasUnidadTest {

    @Mock
    private GestionarEstadisticasGatewayIntPort estadisticasGateway;

    private GestionarEstadisticasCUIntPort estadisticasCU;

    private Estadistica estadisticaEjemplo;

    @BeforeEach
    void setUp() {
        // Crear el CU Adapter con las dependencias mockeadas
        estadisticasCU = new GestionarEstadisticasCUAdapter(estadisticasGateway);

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
        estadisticaEjemplo.setId_estadistica(1);
        when(estadisticasGateway.obtenerEstadisticaPorId(1)).thenReturn(estadisticaEjemplo);
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
        when(estadisticasGateway.obtenerEstadisticaPorId(id)).thenReturn(estadisticaEjemplo);
        when(estadisticasGateway.eliminarEstadistica(id)).thenReturn(true);

        // Act
        Boolean resultado = estadisticasCU.eliminarEstadistica(id);

        // Assert
        assertThat(resultado).isTrue();
        verify(estadisticasGateway, times(1)).eliminarEstadistica(id);
    }

    // ==================== REGLAS DE LAS ESTADÍSTICAS (dominio) ====================

    @Test
    @DisplayName("Test 5: Globales sin filtros - PRE_REGISTRADO cuenta como enviada")
    void testGlobalesSumanPreRegistradasAEnviadas() {
        when(estadisticasGateway.contarSolicitudesPorEstado("APROBADA")).thenReturn(3);
        when(estadisticasGateway.contarSolicitudesPorEstado("RECHAZADA")).thenReturn(1);
        when(estadisticasGateway.contarSolicitudesPorEstado("APROBADA_FUNCIONARIO")).thenReturn(0);
        when(estadisticasGateway.contarSolicitudesPorEstado("APROBADA_COORDINADOR")).thenReturn(0);
        when(estadisticasGateway.contarSolicitudesPorEstado("ENVIADA")).thenReturn(4);
        when(estadisticasGateway.contarSolicitudesPorEstado("PRE_REGISTRADO")).thenReturn(2);

        GlobalesContadas resultado = estadisticasCU.obtenerEstadisticasGlobales();

        assertThat(resultado.totalSolicitudes()).isEqualTo(10);
        assertThat(resultado.totalEnviadas()).isEqualTo(6);
        assertThat(resultado.porcentajeAprobacion()).isEqualTo(30.0);
        verify(estadisticasGateway, never()).contarTotalSolicitudes();
    }

    @Test
    @DisplayName("Test 6: Globales sin estados conocidos - usa el total de solicitudes")
    void testGlobalesSinEstadosUsanElTotal() {
        when(estadisticasGateway.contarTotalSolicitudes()).thenReturn(7);

        GlobalesContadas resultado = estadisticasCU.obtenerEstadisticasGlobales();

        assertThat(resultado.totalSolicitudes()).isEqualTo(7);
        assertThat(resultado.porcentajeAprobacion()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Test 7: Por proceso - solo ese proceso y con el estado más reciente")
    void testPorProcesoUsaElEstadoMasReciente() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario()).thenReturn(List.of(
                solicitud(new SolicitudHomologacion(), 1, estado("Enviada", 1), estado("APROBADA", 2)),
                solicitud(new SolicitudHomologacion(), 1, estado("RECHAZADA", 3)),
                solicitud(new SolicitudReingreso(), 1, estado("APROBADA", 3))));

        EstadisticasPorProceso resultado = estadisticasCU.obtenerEstadisticasPorProceso("Homologacion");

        assertThat(resultado.totalSolicitudes()).isEqualTo(2);
        assertThat(resultado.totalAprobadas()).isEqualTo(1);
        assertThat(resultado.totalRechazadas()).isEqualTo(1);
        assertThat(resultado.porcentajeAprobacion()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Test 8: Por estado - unifica las formas de escribir el estado")
    void testPorEstadoNormalizaElEstado() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario()).thenReturn(List.of(
                solicitud(new SolicitudPazYSalvo(), 1, estado("Aprobado", 1)),
                solicitud(new SolicitudCursoVeranoInscripcion(), 1, estado("Inscripcion_Completada", 1)),
                solicitud(new SolicitudEcaes(), 1, estado("Enviada", 1))));

        EstadisticasPorEstado resultado = estadisticasCU.obtenerEstadisticasPorEstado("aprobada");

        assertThat(resultado.totalSolicitudes()).isEqualTo(2);
        assertThat(resultado.porcentajeAprobacion()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Test 9: Por programa - cuenta solo las solicitudes de ese programa")
    void testPorProgramaFiltraPorPrograma() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario()).thenReturn(List.of(
                solicitud(new SolicitudHomologacion(), 1, estado("APROBADA", 1)),
                solicitud(new SolicitudHomologacion(), 2, estado("APROBADA", 1)),
                solicitud(new SolicitudReingreso(), 2, estado("Enviada", 1))));

        EstadisticasPorPrograma resultado = estadisticasCU.obtenerEstadisticasPorPrograma(2);

        assertThat(resultado.totalSolicitudes()).isEqualTo(2);
        assertThat(resultado.totalEnviadas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Test 10: Si falla la lectura de datos, el error se propaga (no se devuelven ceros)")
    void testFalloDelGatewaySePropaga() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario())
                .thenThrow(new RuntimeException("Error de base de datos"));

        assertThatThrownBy(() -> estadisticasCU.obtenerEstadisticasPorProceso("Homologacion"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error de base de datos");
    }

    @Test
    @DisplayName("Test 11: Verificar que el adaptador implementa la interfaz correcta")
    void testAdaptadorImplementaInterfazCorrecta() {
        assertThat(estadisticasCU).isInstanceOf(GestionarEstadisticasCUIntPort.class);
    }

    @Test
    @DisplayName("Test 12: Dashboard por estado - EN_PROCESO junta funcionario y coordinador")
    void testDashboardPorEstadoJuntaEnProceso() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario()).thenReturn(List.of(
                solicitud(new SolicitudHomologacion(), 1, estado("APROBADA_FUNCIONARIO", 1)),
                solicitud(new SolicitudReingreso(), 1, estado("APROBADA_COORDINADOR", 1)),
                solicitud(new SolicitudEcaes(), 1, estado("RECHAZADA", 1)),
                solicitud(new SolicitudEcaes(), 1, estado("APROBADA", 1))));

        VistaPorEstado resultado = estadisticasCU.obtenerEstadisticasPorEstado(null, null);

        VistaPorEstado.Detalle enProceso = resultado.estados().get("EN_PROCESO");
        assertThat(enProceso.cantidad()).isEqualTo(2);
        assertThat(enProceso.porcentaje()).isEqualTo(50.0);
        assertThat(resultado.analisis().tasaResolucion()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Test 13: Tendencias - con un solo mes de datos la tendencia es Nueva")
    void testTendenciasConUnSoloMes() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario()).thenReturn(List.of(
                solicitud(new SolicitudHomologacion(), 1, estado("Enviada", 3))));

        VistaTendencias resultado = estadisticasCU.obtenerTendenciasYComparativas(null, null);

        VistaTendencias.CrecimientoTemporal crecimiento = resultado.crecimientoTemporal();
        assertThat(crecimiento.tendenciaSolicitudes()).isEqualTo("Nueva");
        assertThat(crecimiento.crecimientoSolicitudes()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Test 14: Cursos de verano - pago validado está en proceso y no cuenta como aprobada")
    void testCursosVeranoPagoValidadoEnProceso() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario()).thenReturn(List.of(
                solicitud(new SolicitudCursoVeranoInscripcion(), 1, estado("Pago_Validado", 7)),
                solicitud(new SolicitudCursoVeranoInscripcion(), 1, estado("Inscripcion_Completada", 7)),
                solicitud(new SolicitudHomologacion(), 1, estado("APROBADA", 7))));

        AnalisisCursosVerano resultado = estadisticasCU.obtenerEstadisticasCursosVerano(null, null);

        Map<String, Integer> estados = resultado.estadosSolicitudes();
        assertThat(estados.get("EN_PROCESO")).isEqualTo(1);
        assertThat(estados.get("APROBADA")).isEqualTo(1);
        assertThat(resultado.resumen().totalSolicitudes()).isEqualTo(2);
        assertThat(resultado.resumen().tasaAprobacion()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Test 15: Resumen por periodo - sin periodo guardado, se deduce de la fecha de registro")
    void testResumenDeducePeriodoDeLaFecha() {
        when(estadisticasGateway.listarSolicitudesConEstadosYUsuario()).thenReturn(List.of(
                solicitud(new SolicitudHomologacion(), 1, estado("Enviada", 3)),   // marzo: 2025-1
                solicitud(new SolicitudHomologacion(), 1, estado("Enviada", 9)))); // septiembre: 2025-2

        ResumenCompleto resultado = estadisticasCU.obtenerResumenCompleto("2025-1", null);

        assertThat(resultado.estadisticasGlobales().totalSolicitudes()).isEqualTo(1);
    }

    // ==================== DATOS ====================

    /** Solicitud de un estudiante del programa indicado; su fecha de registro es la del primer estado. */
    private static Solicitud solicitud(Solicitud solicitud, int idPrograma, EstadoSolicitud... estados) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(idPrograma * 100);
        usuario.setObjPrograma(new Programa(idPrograma, "P" + idPrograma, "Programa " + idPrograma));
        solicitud.setObjUsuario(usuario);
        solicitud.setEstadosSolicitud(new ArrayList<>(Arrays.asList(estados)));
        solicitud.setFecha_registro_solicitud(estados[0].getFecha_registro_estado());
        return solicitud;
    }

    /** Estado registrado el día 10 del mes indicado de 2025. */
    private static EstadoSolicitud estado(String nombre, int mes) {
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(nombre);
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2025, mes - 1, 10, 10, 0);
        estado.setFecha_registro_estado(c.getTime());
        return estado;
    }
}
