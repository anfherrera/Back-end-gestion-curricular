package co.edu.unicauca.decanatura.gestion_curricular.cursosverano;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;

@DisplayName("Cursos de verano - Control de cupo al aceptar inscripciones")
class CupoCursoVeranoTest {

    private GestionarSolicitudCursoVeranoGatewayIntPort gateway;
    private FormateadorResultadosIntPort formateador;
    private GestionarSolicitudCursoVeranoCUAdapter casoDeUso;

    @BeforeEach
    void setUp() {
        gateway = mock(GestionarSolicitudCursoVeranoGatewayIntPort.class);
        formateador = mock(FormateadorResultadosIntPort.class);
        doThrow(new IllegalStateException("regla de negocio"))
                .when(formateador).retornarRespuestaErrorReglaDeNegocio(anyString());
        casoDeUso = new GestionarSolicitudCursoVeranoCUAdapter(gateway,
                mock(GestionarCursoOfertadoVeranoGatewayIntPort.class),
                mock(GestionarUsuarioGatewayIntPort.class),
                mock(GestionarDocumentosGatewayIntPort.class),
                formateador,
                mock(GestionarArchivosCUIntPort.class),
                mock(GestionarNotificacionCUIntPort.class));
    }

    private SolicitudCursoVeranoIncripcion inscripcionEnviada(int idCurso, int cupo) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(idCurso);
        curso.setCupo_estimado(cupo);

        EstadoSolicitud enviada = new EstadoSolicitud();
        enviada.setEstado_actual("Enviada");
        enviada.setFecha_registro_estado(new Date());
        List<EstadoSolicitud> estados = new ArrayList<>();
        estados.add(enviada);

        SolicitudCursoVeranoIncripcion inscripcion = new SolicitudCursoVeranoIncripcion();
        inscripcion.setId_solicitud(50);
        inscripcion.setObjCursoOfertadoVerano(curso);
        inscripcion.setEstadosSolicitud(estados);
        return inscripcion;
    }

    @Test
    @DisplayName("Curso lleno: no se puede aceptar otra inscripción")
    void cursoLlenoRechaza() {
        when(gateway.buscarSolicitudInscripcionPorId(50)).thenReturn(inscripcionEnviada(7, 2));
        when(gateway.contarInscripcionesAceptadasPorCurso(7)).thenReturn(2);

        CupoAgotadoException e = assertThrows(CupoAgotadoException.class,
                () -> casoDeUso.validarPago(50, true, "ok"));
        assertTrue(e.getMessage().contains("cupos"));
        assertEquals(2, e.getCupo());
        assertEquals(2, e.getOcupados());
        verify(gateway, never()).validarPago(eq(50), eq(true), anyString());
    }

    @Test
    @DisplayName("Con cupo disponible: se acepta")
    void conCupoAcepta() {
        when(gateway.buscarSolicitudInscripcionPorId(50)).thenReturn(inscripcionEnviada(7, 2));
        when(gateway.contarInscripcionesAceptadasPorCurso(7)).thenReturn(1);

        casoDeUso.validarPago(50, true, "ok");

        verify(gateway).validarPago(50, true, "ok");
    }

    @Test
    @DisplayName("Rechazar un pago no depende del cupo")
    void rechazarNoValidaCupo() {
        when(gateway.buscarSolicitudInscripcionPorId(50)).thenReturn(inscripcionEnviada(7, 2));
        when(gateway.contarInscripcionesAceptadasPorCurso(7)).thenReturn(5);

        casoDeUso.validarPago(50, false, "pago inválido");

        verify(gateway).validarPago(50, false, "pago inválido");
    }

    @Test
    @DisplayName("verificarCupoDisponible: curso lleno lanza; curso null o sin id no se limita")
    void verificarCupoDisponible() {
        when(gateway.contarInscripcionesAceptadasPorCurso(7)).thenReturn(2);
        CursoOfertadoVerano curso = inscripcionEnviada(7, 2).getObjCursoOfertadoVerano();

        assertThrows(CupoAgotadoException.class, () -> casoDeUso.verificarCupoDisponible(curso));
        casoDeUso.verificarCupoDisponible(null);
        curso.setId_curso(null);
        casoDeUso.verificarCupoDisponible(curso);
    }

    @Test
    @DisplayName("verificarCupoParaAceptar: una inscripción que ya ocupa cupo no vuelve a contar")
    void verificarCupoParaAceptar() {
        when(gateway.contarInscripcionesAceptadasPorCurso(7)).thenReturn(2);
        SolicitudCursoVeranoIncripcion inscripcion = inscripcionEnviada(7, 2);

        assertThrows(CupoAgotadoException.class, () -> casoDeUso.verificarCupoParaAceptar(inscripcion));

        EstadoSolicitud validado = new EstadoSolicitud();
        validado.setEstado_actual("Pago_Validado");
        validado.setFecha_registro_estado(new Date());
        inscripcion.getEstadosSolicitud().add(validado);
        casoDeUso.verificarCupoParaAceptar(inscripcion);
    }
}
