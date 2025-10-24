package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudPazYSalvoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS - MÓDULO GEPA4 (Paz y Salvo)
 * Objetivo: Probar la lógica de negocio del caso de uso de manera aislada
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Gestión de Paz y Salvo")
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

    private GestionarSolicitudPazYSalvoCUAdapter casoDeUso;

    private Usuario usuarioMock;
    private SolicitudPazYSalvo solicitudMock;

    @BeforeEach
    void setUp() {
        casoDeUso = new GestionarSolicitudPazYSalvoCUAdapter(
            solicitudGateway,
            usuarioGateway,
            documentosGateway,
            estadoSolicitudGateway,
            formateadorResultados
        );

        // Crear usuario mock
        usuarioMock = new Usuario();
        usuarioMock.setId_usuario(1);
        usuarioMock.setNombre_completo("Juan Pérez Test");
        usuarioMock.setCorreo("juan.test@unicauca.edu.co");
        usuarioMock.setCodigo("102010");
        usuarioMock.setSolicitudes(new ArrayList<>());

        // Crear solicitud mock
        solicitudMock = new SolicitudPazYSalvo();
        solicitudMock.setId_solicitud(1);
        solicitudMock.setNombre_solicitud("Solicitud Paz y Salvo Test");
        solicitudMock.setFecha_registro_solicitud(new Date());
        solicitudMock.setObjUsuario(usuarioMock);
        solicitudMock.setDocumentos(new ArrayList<>());
        solicitudMock.setEstadosSolicitud(new ArrayList<>());
    }

    @Test
    @DisplayName("Test 1: Crear solicitud de paz y salvo exitosamente")
    void testCrearSolicitudPazYSalvoExitoso() {
        // Arrange
        when(usuarioGateway.obtenerUsuarioPorId(1)).thenReturn(usuarioMock);
        when(solicitudGateway.guardar(any(SolicitudPazYSalvo.class))).thenReturn(solicitudMock);
        when(documentosGateway.buscarDocumentosSinSolicitud()).thenReturn(new ArrayList<>());
        when(estadoSolicitudGateway.guarEstadoSolicitud(any(EstadoSolicitud.class))).thenReturn(new EstadoSolicitud());

        // Act
        SolicitudPazYSalvo resultado = casoDeUso.guardar(solicitudMock);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId_solicitud()).isEqualTo(1);
        assertThat(resultado.getObjUsuario()).isNotNull();
        
        // Verificar que se llamaron los métodos correctos
        verify(usuarioGateway, times(1)).obtenerUsuarioPorId(1);
        verify(solicitudGateway, times(1)).guardar(any(SolicitudPazYSalvo.class));
        verify(documentosGateway, times(1)).buscarDocumentosSinSolicitud();
        verify(estadoSolicitudGateway, times(1)).guarEstadoSolicitud(any(EstadoSolicitud.class));
    }

    @Test
    @DisplayName("Test 2: Crear solicitud con documentos huérfanos los asocia correctamente")
    void testCrearSolicitudAsociaDocumentosHuerfanos() {
        // Arrange
        Documento doc1 = new Documento();
        doc1.setId_documento(1);
        doc1.setNombre("documento1.pdf");
        
        Documento doc2 = new Documento();
        doc2.setId_documento(2);
        doc2.setNombre("documento2.pdf");
        
        List<Documento> documentosHuerfanos = List.of(doc1, doc2);

        when(usuarioGateway.obtenerUsuarioPorId(1)).thenReturn(usuarioMock);
        when(solicitudGateway.guardar(any(SolicitudPazYSalvo.class))).thenReturn(solicitudMock);
        when(documentosGateway.buscarDocumentosSinSolicitud()).thenReturn(documentosHuerfanos);
        when(estadoSolicitudGateway.guarEstadoSolicitud(any(EstadoSolicitud.class))).thenReturn(new EstadoSolicitud());

        // Act
        SolicitudPazYSalvo resultado = casoDeUso.guardar(solicitudMock);

        // Assert
        assertThat(resultado).isNotNull();
        
        // Verificar que se intentó actualizar cada documento
        verify(documentosGateway, times(2)).actualizarDocumento(any(Documento.class));
    }

    @Test
    @DisplayName("Test 3: Crear solicitud sin usuario lanza excepción")
    void testCrearSolicitudSinUsuario() {
        // Arrange
        SolicitudPazYSalvo solicitudSinUsuario = new SolicitudPazYSalvo();
        solicitudSinUsuario.setNombre_solicitud("Test");
        
        doThrow(new RuntimeException("El usuario no puede ser nulo o sin ID"))
            .when(formateadorResultados)
            .retornarRespuestaErrorReglaDeNegocio(anyString());

        // Act & Assert
        assertThatThrownBy(() -> casoDeUso.guardar(solicitudSinUsuario))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("El usuario no puede ser nulo o sin ID");
    }

    @Test
    @DisplayName("Test 4: Listar todas las solicitudes")
    void testListarTodasLasSolicitudes() {
        // Arrange
        List<SolicitudPazYSalvo> solicitudesMock = List.of(
            solicitudMock,
            crearSolicitudMock(2, "Solicitud 2"),
            crearSolicitudMock(3, "Solicitud 3")
        );
        
        when(solicitudGateway.listarSolicitudes()).thenReturn(solicitudesMock);

        // Act
        List<SolicitudPazYSalvo> resultado = casoDeUso.listarSolicitudes();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(3);
        verify(solicitudGateway, times(1)).listarSolicitudes();
    }

    @Test
    @DisplayName("Test 5: Listar solicitudes para funcionario")
    void testListarSolicitudesParaFuncionario() {
        // Arrange
        List<SolicitudPazYSalvo> solicitudesMock = List.of(solicitudMock);
        when(solicitudGateway.listarSolicitudesToFuncionario()).thenReturn(solicitudesMock);

        // Act
        List<SolicitudPazYSalvo> resultado = casoDeUso.listarSolicitudesToFuncionario();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(solicitudGateway, times(1)).listarSolicitudesToFuncionario();
    }

    @Test
    @DisplayName("Test 6: Listar solicitudes para coordinador")
    void testListarSolicitudesParaCoordinador() {
        // Arrange
        List<SolicitudPazYSalvo> solicitudesMock = List.of(solicitudMock);
        when(solicitudGateway.listarSolicitudesToCoordinador()).thenReturn(solicitudesMock);

        // Act
        List<SolicitudPazYSalvo> resultado = casoDeUso.listarSolicitudesToCoordinador();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(solicitudGateway, times(1)).listarSolicitudesToCoordinador();
    }

    @Test
    @DisplayName("Test 7: Listar solicitudes para secretaria")
    void testListarSolicitudesParaSecretaria() {
        // Arrange
        List<SolicitudPazYSalvo> solicitudesMock = List.of(solicitudMock);
        when(solicitudGateway.listarSolicitudesToSecretaria()).thenReturn(solicitudesMock);

        // Act
        List<SolicitudPazYSalvo> resultado = casoDeUso.listarSolicitudesToSecretaria();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(solicitudGateway, times(1)).listarSolicitudesToSecretaria();
    }

    @Test
    @DisplayName("Test 8: Filtrar solicitudes por rol ESTUDIANTE")
    void testFiltrarSolicitudesPorRolEstudiante() {
        // Arrange
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual("Enviada");
        solicitudMock.setEstadosSolicitud(List.of(estado));
        
        when(solicitudGateway.listarSolicitudes()).thenReturn(List.of(solicitudMock));

        // Act
        List<SolicitudPazYSalvo> resultado = casoDeUso.listarSolicitudesPorRol("ESTUDIANTE", 1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getObjUsuario().getId_usuario()).isEqualTo(1);
    }

    @Test
    @DisplayName("Test 9: Filtrar solicitudes por rol FUNCIONARIO")
    void testFiltrarSolicitudesPorRolFuncionario() {
        // Arrange
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual("Enviada");
        solicitudMock.setEstadosSolicitud(List.of(estado));
        
        when(solicitudGateway.listarSolicitudes()).thenReturn(List.of(solicitudMock));

        // Act
        List<SolicitudPazYSalvo> resultado = casoDeUso.listarSolicitudesPorRol("FUNCIONARIO", null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Test 10: Solicitud nula no debe guardarse")
    void testSolicitudNulaNoDebeGuardarse() {
        // Arrange
        doThrow(new RuntimeException("La solicitud no puede ser nula"))
            .when(formateadorResultados)
            .retornarRespuestaErrorReglaDeNegocio("La solicitud no puede ser nula");

        // Act & Assert
        assertThatThrownBy(() -> casoDeUso.guardar(null))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Test 11: Usuario inexistente no permite crear solicitud")
    void testUsuarioInexistenteNoCreaScolicitud() {
        // Arrange
        when(usuarioGateway.obtenerUsuarioPorId(999)).thenReturn(null);
        
        Usuario usuarioInexistente = new Usuario();
        usuarioInexistente.setId_usuario(999);
        
        SolicitudPazYSalvo solicitud = new SolicitudPazYSalvo();
        solicitud.setObjUsuario(usuarioInexistente);
        
        doThrow(new RuntimeException("Usuario no encontrado"))
            .when(formateadorResultados)
            .retornarRespuestaErrorEntidadExiste("Usuario no encontrado");

        // Act & Assert
        assertThatThrownBy(() -> casoDeUso.guardar(solicitud))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("Test 12: Estado inicial se crea correctamente al guardar solicitud")
    void testEstadoInicialSeCreaCorrectamente() {
        // Arrange
        when(usuarioGateway.obtenerUsuarioPorId(1)).thenReturn(usuarioMock);
        when(solicitudGateway.guardar(any(SolicitudPazYSalvo.class))).thenReturn(solicitudMock);
        when(documentosGateway.buscarDocumentosSinSolicitud()).thenReturn(new ArrayList<>());
        when(estadoSolicitudGateway.guarEstadoSolicitud(any(EstadoSolicitud.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        casoDeUso.guardar(solicitudMock);

        // Assert
        verify(estadoSolicitudGateway, times(1)).guarEstadoSolicitud(argThat(estado ->
            "Enviada".equals(estado.getEstado_actual()) &&
            estado.getFecha_registro_estado() != null
        ));
    }

    // Método auxiliar para crear solicitudes mock
    private SolicitudPazYSalvo crearSolicitudMock(Integer id, String nombre) {
        SolicitudPazYSalvo solicitud = new SolicitudPazYSalvo();
        solicitud.setId_solicitud(id);
        solicitud.setNombre_solicitud(nombre);
        solicitud.setFecha_registro_solicitud(new Date());
        solicitud.setObjUsuario(usuarioMock);
        solicitud.setDocumentos(new ArrayList<>());
        solicitud.setEstadosSolicitud(new ArrayList<>());
        return solicitud;
    }
}

