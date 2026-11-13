package co.edu.unicauca.decanatura.gestion_curricular.cursosverano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCursoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;

/**
 * Reglas del caso de uso de cursos de verano (los controladores ya no van directo al gateway):
 * fase del curso, preinscripción aprobada, inscripción duplicada, cupo y aceptación de pagos.
 */
@DisplayName("Cursos de verano - Reglas de preinscripción e inscripción")
class ReglasSolicitudesCursoVeranoTest {

    private static final int ID_USUARIO = 7;
    private static final int ID_CURSO = 30;

    private GestionarSolicitudCursoVeranoGatewayIntPort solicitudes;
    private GestionarCursoOfertadoVeranoGatewayIntPort cursos;
    private GestionarUsuarioGatewayIntPort usuarios;
    private GestionarSolicitudCursoVeranoCUAdapter casoDeUso;

    @BeforeEach
    void setUp() {
        solicitudes = mock(GestionarSolicitudCursoVeranoGatewayIntPort.class);
        cursos = mock(GestionarCursoOfertadoVeranoGatewayIntPort.class);
        usuarios = mock(GestionarUsuarioGatewayIntPort.class);
        FormateadorResultadosIntPort formateador = mock(FormateadorResultadosIntPort.class);
        doThrow(new IllegalStateException("regla")).when(formateador).retornarRespuestaErrorReglaDeNegocio(anyString());
        doThrow(new IllegalStateException("no existe")).when(formateador).retornarRespuestaErrorEntidadExiste(anyString());
        casoDeUso = new GestionarSolicitudCursoVeranoCUAdapter(solicitudes, cursos, usuarios,
                mock(GestionarDocumentosGatewayIntPort.class), formateador,
                mock(GestionarArchivosCUIntPort.class), mock(GestionarNotificacionCUIntPort.class));

        Usuario usuario = new Usuario();
        usuario.setId_usuario(ID_USUARIO);
        usuario.setSolicitudes(new ArrayList<>());
        when(usuarios.obtenerUsuarioPorId(ID_USUARIO)).thenReturn(usuario);
        when(solicitudes.buscarInscripcionesPorUsuarioYCurso(ID_USUARIO, ID_CURSO)).thenReturn(List.of());
        when(solicitudes.contarInscripcionesAceptadasPorCurso(ID_CURSO)).thenReturn(0);
    }

    // ==================== Preinscripción ====================

    @Test
    @DisplayName("Preinscripción: solo si el curso está en Preinscripcion")
    void preinscripcionSoloEnSuFase() {
        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Inscripcion", 20));
        assertThatThrownBy(() -> casoDeUso.crearSolicitudCursoVeranoPreinscripcion(preinscripcionNueva()))
                .isInstanceOf(IllegalStateException.class);
        verify(solicitudes, never()).crearSolicitudCursoVeranoPreinscripcion(any());

        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Preinscripcion", 20));
        when(solicitudes.crearSolicitudCursoVeranoPreinscripcion(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(casoDeUso.crearSolicitudCursoVeranoPreinscripcion(preinscripcionNueva())).isNotNull();
    }

    @Test
    @DisplayName("Preinscripción sin lista de documentos (null) no falla")
    void preinscripcionSinDocumentos() {
        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Preinscripcion", 20));
        when(solicitudes.crearSolicitudCursoVeranoPreinscripcion(any())).thenAnswer(i -> i.getArgument(0));
        SolicitudCursoVeranoPreinscripcion p = preinscripcionNueva();
        p.setDocumentos(null);
        assertThat(casoDeUso.crearSolicitudCursoVeranoPreinscripcion(p)).isNotNull();
    }

    // ==================== Inscripción ====================

    @Test
    @DisplayName("Inscripción: en fase Inscripcion, con preinscripción aprobada y sin comprobante aún")
    void inscripcionValida() {
        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Inscripcion", 20));
        when(solicitudes.buscarSolicitudesPorUsuarioYCursoPre(ID_USUARIO, ID_CURSO)).thenReturn(preinscripcion("Aprobado"));
        when(solicitudes.crearSolicitudCursoVeranoInscripcion(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(casoDeUso.crearSolicitudCursoVeranoInscripcion(inscripcionNueva())).isNotNull();
    }

    @Test
    @DisplayName("Inscripción: rechazada si el curso sigue en Preinscripcion")
    void inscripcionFueraDeFase() {
        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Preinscripcion", 20));
        when(solicitudes.buscarSolicitudesPorUsuarioYCursoPre(ID_USUARIO, ID_CURSO)).thenReturn(preinscripcion("Aprobado"));

        assertThatThrownBy(() -> casoDeUso.crearSolicitudCursoVeranoInscripcion(inscripcionNueva()))
                .isInstanceOf(IllegalStateException.class);
        verify(solicitudes, never()).crearSolicitudCursoVeranoInscripcion(any());
    }

    @Test
    @DisplayName("Inscripción: rechazada si la preinscripción no está aprobada")
    void inscripcionSinAprobacion() {
        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Inscripcion", 20));
        when(solicitudes.buscarSolicitudesPorUsuarioYCursoPre(ID_USUARIO, ID_CURSO)).thenReturn(preinscripcion("Enviada"));

        assertThatThrownBy(() -> casoDeUso.crearSolicitudCursoVeranoInscripcion(inscripcionNueva()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Inscripción: rechazada si ya hay otra activa; permitida si la anterior tuvo el pago rechazado")
    void inscripcionDuplicada() {
        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Inscripcion", 20));
        when(solicitudes.buscarSolicitudesPorUsuarioYCursoPre(ID_USUARIO, ID_CURSO)).thenReturn(preinscripcion("Aprobado"));
        when(solicitudes.crearSolicitudCursoVeranoInscripcion(any())).thenAnswer(i -> i.getArgument(0));

        when(solicitudes.buscarInscripcionesPorUsuarioYCurso(ID_USUARIO, ID_CURSO)).thenReturn(List.of(inscripcion("Enviada")));
        assertThatThrownBy(() -> casoDeUso.crearSolicitudCursoVeranoInscripcion(inscripcionNueva()))
                .isInstanceOf(IllegalStateException.class);

        when(solicitudes.buscarInscripcionesPorUsuarioYCurso(ID_USUARIO, ID_CURSO)).thenReturn(List.of(inscripcion("Pago_Rechazado")));
        assertThat(casoDeUso.crearSolicitudCursoVeranoInscripcion(inscripcionNueva())).isNotNull();
    }

    @Test
    @DisplayName("Inscripción: curso sin cupo responde cupo agotado")
    void inscripcionSinCupo() {
        when(cursos.obtenerCursoPorId(ID_CURSO)).thenReturn(curso("Inscripcion", 2));
        when(solicitudes.buscarSolicitudesPorUsuarioYCursoPre(ID_USUARIO, ID_CURSO)).thenReturn(preinscripcion("Aprobado"));
        when(solicitudes.contarInscripcionesAceptadasPorCurso(ID_CURSO)).thenReturn(2);

        assertThatThrownBy(() -> casoDeUso.crearSolicitudCursoVeranoInscripcion(inscripcionNueva()))
                .isInstanceOf(CupoAgotadoException.class);
    }

    // ==================== Aceptar inscripción ====================

    @Test
    @DisplayName("Aceptar: no se puede aceptar una inscripción con el pago rechazado")
    void noAceptarRechazada() {
        SolicitudCursoVeranoIncripcion rechazada = inscripcion("Pago_Rechazado");
        rechazada.setId_solicitud(90);
        when(solicitudes.buscarSolicitudInscripcionPorId(90)).thenReturn(rechazada);

        assertThatThrownBy(() -> casoDeUso.validarPago(90, true, "ok")).isInstanceOf(IllegalStateException.class);
        verify(solicitudes, never()).validarPago(any(), org.mockito.ArgumentMatchers.anyBoolean(), any());
    }

    // ==================== Apoyo ====================

    private static CursoOfertadoVerano curso(String estado, int cupo) {
        EstadoCursoOfertado e = new EstadoCursoOfertado();
        e.setEstado_actual(estado);
        CursoOfertadoVerano c = new CursoOfertadoVerano();
        c.setId_curso(ID_CURSO);
        c.setCupo_estimado(cupo);
        c.setEstadosCursoOfertados(new ArrayList<>(List.of(e)));
        return c;
    }

    private static Usuario usuarioRef() {
        Usuario u = new Usuario();
        u.setId_usuario(ID_USUARIO);
        return u;
    }

    private static CursoOfertadoVerano cursoRef() {
        CursoOfertadoVerano c = new CursoOfertadoVerano();
        c.setId_curso(ID_CURSO);
        return c;
    }

    private static List<EstadoSolicitud> estados(String estado) {
        EstadoSolicitud e = new EstadoSolicitud();
        e.setEstado_actual(estado);
        e.setFecha_registro_estado(new Date());
        return new ArrayList<>(List.of(e));
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcionNueva() {
        SolicitudCursoVeranoPreinscripcion p = new SolicitudCursoVeranoPreinscripcion();
        p.setObjUsuario(usuarioRef());
        p.setObjCursoOfertadoVerano(cursoRef());
        p.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
        p.setDocumentos(new ArrayList<>());
        return p;
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcion(String estado) {
        SolicitudCursoVeranoPreinscripcion p = preinscripcionNueva();
        p.setEstadosSolicitud(estados(estado));
        return p;
    }

    private static SolicitudCursoVeranoIncripcion inscripcionNueva() {
        SolicitudCursoVeranoIncripcion i = new SolicitudCursoVeranoIncripcion();
        i.setObjUsuario(usuarioRef());
        i.setObjCursoOfertadoVerano(cursoRef());
        i.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
        i.setDocumentos(new ArrayList<>());
        return i;
    }

    private static SolicitudCursoVeranoIncripcion inscripcion(String estado) {
        SolicitudCursoVeranoIncripcion i = inscripcionNueva();
        i.setEstadosSolicitud(estados(estado));
        return i;
    }
}
