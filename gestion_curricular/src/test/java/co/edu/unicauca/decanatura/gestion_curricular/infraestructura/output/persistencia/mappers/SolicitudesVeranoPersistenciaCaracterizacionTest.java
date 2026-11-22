package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.TipoDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.enums.CondicionSolicitudVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarSolicitudCursoVeranoGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarSolicitudGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.ProyeccionValores;

/**
 * Caracterización previa a DUP-10 de los gateways de solicitudes genéricas y de cursos de verano,
 * que eligen la subclase de dominio según la entidad (o al revés) y mapean con el
 * {@code ModelMapper} principal. Cubre las seis subclases, la entidad base, los mapeos cruzados
 * (una solicitud de otro tipo listada como inscripción) y la creación con curso existente, nuevo
 * o de apertura.
 */
class SolicitudesVeranoPersistenciaCaracterizacionTest {

    private final List<Object> guardados = new ArrayList<>();

    @Test
    void solicitud() throws Exception {
        SolicitudRepositoryInt repositorio = mock(SolicitudRepositoryInt.class);
        EstadoSolicitudRepositoryInt estados = mock(EstadoSolicitudRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        when(estados.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        for (int id = 1; id <= 7; id++) {
            int n = id;
            when(repositorio.findById(id)).thenAnswer(inv -> Optional.of(entidad(n)));
        }
        when(repositorio.findById(9)).thenReturn(Optional.empty());
        when(repositorio.findAllWithJoins()).thenAnswer(inv -> todas());
        when(repositorio.buscarHistorialConFiltros("2025-1", 1, null)).thenAnswer(inv -> todas());
        when(repositorio.buscarPorNombre("Solicitud de prueba")).thenAnswer(inv -> todas());
        when(repositorio.buscarPorNombreYCursoIns(4)).thenAnswer(inv -> List.of(entidad(2), entidad(4)));
        when(repositorio.buscarPorNombreYCursoPre(4)).thenAnswer(inv -> List.of(entidad(1), entidad(7)));
        GestionarSolicitudGatewayImplAdapter gateway = new GestionarSolicitudGatewayImplAdapter(repositorio,
                mock(UsuarioRepositoryInt.class), estados);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        for (int id : new int[] {1, 2, 3, 4, 5, 6, 7, 9}) {
            casos.set("obtenerSolicitudPorId " + id, GrafoObjetos.resultado(() -> gateway.obtenerSolicitudPorId(id)));
        }
        casos.set("listarSolicitudes", GrafoObjetos.resultado(gateway::listarSolicitudes));
        casos.set("buscarHistorial", GrafoObjetos.resultado(() -> gateway.buscarHistorial("2025-1", 1, null)));
        casos.set("obtenerSolicitudesPorNombre", GrafoObjetos.resultado(
                () -> gateway.obtenerSolicitudesPorNombre("Solicitud de prueba")));
        casos.set("buscarSolicitudPorNombreCursoIns", GrafoObjetos.resultado(() -> gateway.buscarSolicitudPorNombreCursoIns(4)));
        casos.set("buscarSolicitudPorNombreCursoPre", GrafoObjetos.resultado(() -> gateway.buscarSolicitudPorNombreCursoPre(4)));
        for (int id = 1; id <= 6; id++) {
            int n = id;
            casos.set("actualizarSolicitud " + id, guardado(() -> gateway.actualizarSolicitud(dominio(n),
                    new EstadoSolicitud(null, "APROBADA", GrafosSolicitud.FECHA_3, "ok", null))));
        }
        casos.set("actualizarSolicitud sin estado", guardado(() -> gateway.actualizarSolicitud(dominio(4), null)));
        casos.set("actualizarSolicitud base", guardado(() -> gateway.actualizarSolicitud(
                GrafosSolicitud.completar(new Solicitud()), null)));
        // BUG-36: una solicitud de tipo no reconocido se rechaza antes de guardar ningún estado
        ProyeccionValores.compararConReferencia(casos, "persistencia/solicitud.json",
                "persistencia/correcciones/solicitud-bug36.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/solicitud.json",
                "persistencia/correcciones/solicitud-bug36.json");
    }

    @Test
    void solicitudCursoVerano() throws Exception {
        SolicitudRepositoryInt repositorio = mock(SolicitudRepositoryInt.class);
        EstadoSolicitudRepositoryInt estados = mock(EstadoSolicitudRepositoryInt.class);
        CursoOfertadoVeranoRepositoryInt cursos = mock(CursoOfertadoVeranoRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        when(estados.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        when(cursos.existsById(4)).thenReturn(true);
        when(cursos.findById(4)).thenAnswer(inv -> Optional.of(GrafosSolicitud.cursoEntity(GrafosSolicitud.usuarioEntity())));
        for (int id = 1; id <= 4; id++) {
            int n = id;
            when(repositorio.findById(id)).thenAnswer(inv -> Optional.of(entidad(n)));
        }
        when(repositorio.listarSolicitudesDeVerano(4)).thenAnswer(inv -> List.of(entidad(1), entidad(2), entidad(4)));
        when(repositorio.buscarSolicitudesPorUsuarioyCursoPre(1, 4)).thenAnswer(inv -> entidad(1));
        when(repositorio.buscarSolicitudesPorUsuarioyCursoPre(3, 4)).thenAnswer(inv -> entidad(3));
        when(repositorio.buscarSolicitudesPorUsuario(1)).thenAnswer(inv -> List.of(entidad(1), entidad(2)));
        when(repositorio.buscarSolicitudesPorCurso(4)).thenAnswer(inv -> List.of(entidad(2), entidad(1)));
        when(repositorio.buscarInscripcionesPorCurso(4)).thenAnswer(inv -> List.of(entidad(2), entidad(1)));
        when(repositorio.buscarInscripcionesPorUsuarioYCurso(1, 4)).thenAnswer(inv -> List.of(entidad(2)));
        when(repositorio.buscarInscripcionesPorUsuario(1)).thenAnswer(inv -> List.of(entidad(1), entidad(2)));
        GestionarSolicitudCursoVeranoGatewayImplAdapter gateway = new GestionarSolicitudCursoVeranoGatewayImplAdapter(
                repositorio, cursos, estados);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("listarSolicitudesDeVerano", GrafoObjetos.resultado(() -> gateway.listarSolicitudesDeVerano(4)));
        casos.set("buscarSolicitudesPorUsuarioYCursoPre pre", GrafoObjetos.resultado(
                () -> gateway.buscarSolicitudesPorUsuarioYCursoPre(1, 4)));
        casos.set("buscarSolicitudesPorUsuarioYCursoPre ecaes", GrafoObjetos.resultado(
                () -> gateway.buscarSolicitudesPorUsuarioYCursoPre(3, 4)));
        casos.set("buscarSolicitudesPorUsuarioYCursoPre ninguna", GrafoObjetos.resultado(
                () -> gateway.buscarSolicitudesPorUsuarioYCursoPre(9, 4)));
        casos.set("buscarSolicitudesPorUsuario", GrafoObjetos.resultado(() -> gateway.buscarSolicitudesPorUsuario(1)));
        casos.set("buscarSolicitudPorId 1", GrafoObjetos.resultado(() -> gateway.buscarSolicitudPorId(1)));
        casos.set("buscarSolicitudPorId 2", GrafoObjetos.resultado(() -> gateway.buscarSolicitudPorId(2)));
        casos.set("buscarPreinscripcionesPorCurso", GrafoObjetos.resultado(() -> gateway.buscarPreinscripcionesPorCurso(4)));
        casos.set("buscarInscripcionesPorCurso", GrafoObjetos.resultado(() -> gateway.buscarInscripcionesPorCurso(4)));
        casos.set("buscarSolicitudInscripcionPorId 2", GrafoObjetos.resultado(() -> gateway.buscarSolicitudInscripcionPorId(2)));
        casos.set("buscarInscripcionesPorUsuarioYCurso", GrafoObjetos.resultado(
                () -> gateway.buscarInscripcionesPorUsuarioYCurso(1, 4)));
        casos.set("buscarInscripcionesPorUsuario", GrafoObjetos.resultado(() -> gateway.buscarInscripcionesPorUsuario(1)));
        casos.set("aprobarPreinscripcion", guardado(() -> gateway.aprobarPreinscripcion(1, " visto ")));
        casos.set("rechazarPreinscripcion", guardado(() -> gateway.rechazarPreinscripcion(1, " cupo ")));
        casos.set("validarPago rechazado", guardado(() -> gateway.validarPago(2, false, " ilegible ")));
        casos.set("completarInscripcion", guardado(() -> gateway.completarInscripcion(2)));
        SolicitudCursoVeranoPreinscripcion cambio = new SolicitudCursoVeranoPreinscripcion();
        cambio.setIdSolicitud(1);
        cambio.setObservacion("nueva observación");
        casos.set("actualizarSolicitudCursoVerano", guardado(() -> gateway.actualizarSolicitudCursoVerano(cambio)));

        casos.set("crearPreinscripcion curso existente", guardado(
                () -> gateway.crearSolicitudCursoVeranoPreinscripcion(preinscripcion(cursoConId(4)))));
        CursoOfertadoVerano apertura = GrafosSolicitud.curso(null);
        apertura.setIdCurso(0);
        casos.set("crearPreinscripcion apertura", guardado(
                () -> gateway.crearSolicitudCursoVeranoPreinscripcion(preinscripcion(apertura))));
        CursoOfertadoVerano nuevo = GrafosSolicitud.curso(null);
        nuevo.setIdCurso(99);
        casos.set("crearPreinscripcion curso nuevo", guardado(
                () -> gateway.crearSolicitudCursoVeranoPreinscripcion(preinscripcion(nuevo))));
        CursoOfertadoVerano sinGrupo = GrafosSolicitud.curso(null);
        sinGrupo.setIdCurso(98);
        sinGrupo.setGrupo(null);
        casos.set("crearPreinscripcion curso nuevo sin grupo", guardado(
                () -> gateway.crearSolicitudCursoVeranoPreinscripcion(preinscripcion(sinGrupo))));
        casos.set("crearPreinscripcion sin curso", guardado(
                () -> gateway.crearSolicitudCursoVeranoPreinscripcion(preinscripcion(null))));
        casos.set("crearInscripcion curso existente", guardado(
                () -> gateway.crearSolicitudCursoVeranoInscripcion(inscripcion(cursoConId(4)))));
        CursoOfertadoVerano nuevoInscripcion = GrafosSolicitud.curso(null);
        nuevoInscripcion.setIdCurso(97);
        casos.set("crearInscripcion curso nuevo", guardado(
                () -> gateway.crearSolicitudCursoVeranoInscripcion(inscripcion(nuevoInscripcion))));
        casos.set("crearInscripcion sin curso", guardado(
                () -> gateway.crearSolicitudCursoVeranoInscripcion(inscripcion(null))));
        ProyeccionValores.compararConReferencia(casos, "persistencia/solicitud-curso-verano.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/solicitud-curso-verano.json");
    }

    /** 1 preinscripción, 2 inscripción, 3 ECAES, 4 reingreso, 5 homologación, 6 paz y salvo, 7 base. */
    private static SolicitudEntity entidad(int tipo) {
        SolicitudEntity entidad = switch (tipo) {
            case 1 -> {
                SolicitudCursoVeranoPreinscripcionEntity e = new SolicitudCursoVeranoPreinscripcionEntity();
                e.setNombre_estudiante("Ana");
                e.setCodicion_solicitud(CondicionSolicitudVeranoEntity.Habilitación);
                e.setObservacion("pre");
                yield e;
            }
            case 2 -> {
                SolicitudCursoVeranoInscripcionEntity e = new SolicitudCursoVeranoInscripcionEntity();
                e.setNombre_estudiante("Ana");
                e.setCodicion_solicitud(null);
                e.setObservacion("ins");
                yield e;
            }
            case 3 -> {
                SolicitudEcaesEntity e = new SolicitudEcaesEntity();
                e.setTipoDocumento(TipoDocumento.CC);
                e.setNumero_documento("123");
                e.setFecha_expedicion(GrafosSolicitud.FECHA_1);
                yield e;
            }
            case 4 -> new SolicitudReingresoEntity();
            case 5 -> {
                SolicitudHomologacionEntity e = new SolicitudHomologacionEntity();
                e.setPrograma_origen("Electrónica");
                yield e;
            }
            case 6 -> {
                SolicitudPazYSalvoEntity e = new SolicitudPazYSalvoEntity();
                e.setTitulo_trabajo_grado("Tesis");
                yield e;
            }
            default -> new SolicitudEntity();
        };
        GrafosSolicitud.completar(entidad);
        entidad.setId_solicitud(tipo);
        return entidad;
    }

    private static List<SolicitudEntity> todas() {
        List<SolicitudEntity> todas = new ArrayList<>();
        for (int tipo = 1; tipo <= 7; tipo++) {
            todas.add(entidad(tipo));
        }
        return todas;
    }

    private static Solicitud dominio(int tipo) {
        Solicitud solicitud = switch (tipo) {
            case 1 -> {
                SolicitudCursoVeranoPreinscripcion s = new SolicitudCursoVeranoPreinscripcion();
                s.setNombreEstudiante("Ana");
                s.setCodigoEstudiante("104");
                s.setCondicionSolicitud(CondicionSolicitudVerano.Repeteción);
                yield s;
            }
            case 2 -> {
                SolicitudCursoVeranoInscripcion s = new SolicitudCursoVeranoInscripcion();
                s.setObservacion("ins");
                yield s;
            }
            case 3 -> {
                SolicitudEcaes s = new SolicitudEcaes();
                s.setTipoDocumento(TipoDocumento.CE);
                s.setFechaNacimiento(GrafosSolicitud.FECHA_2);
                yield s;
            }
            case 4 -> new SolicitudReingreso();
            case 5 -> {
                SolicitudHomologacion s = new SolicitudHomologacion();
                s.setProgramaDestino("Sistemas");
                yield s;
            }
            default -> {
                SolicitudPazYSalvo s = new SolicitudPazYSalvo();
                s.setDirectorTrabajoGrado("Director");
                yield s;
            }
        };
        GrafosSolicitud.completar(solicitud);
        solicitud.setIdSolicitud(tipo);
        return solicitud;
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcion(CursoOfertadoVerano curso) {
        SolicitudCursoVeranoPreinscripcion solicitud = GrafosSolicitud.completar(new SolicitudCursoVeranoPreinscripcion());
        solicitud.setIdSolicitud(null);
        solicitud.setNombreEstudiante("Ana");
        solicitud.setCodigoEstudiante("104");
        solicitud.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
        solicitud.setObservacion("quiero el curso");
        solicitud.setObjCursoOfertadoVerano(curso);
        return solicitud;
    }

    private static SolicitudCursoVeranoInscripcion inscripcion(CursoOfertadoVerano curso) {
        SolicitudCursoVeranoInscripcion solicitud = GrafosSolicitud.completar(new SolicitudCursoVeranoInscripcion());
        solicitud.setIdSolicitud(null);
        solicitud.setNombreEstudiante("Ana");
        solicitud.setCondicionSolicitud(CondicionSolicitudVerano.Habilitación);
        solicitud.setObjCursoOfertadoVerano(curso);
        return solicitud;
    }

    private static CursoOfertadoVerano cursoConId(int id) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(id);
        return curso;
    }

    private Object guardar(Object entidad) {
        guardados.add(entidad);
        return entidad;
    }

    /** Lo que se envió a {@code save} y lo que devolvió el gateway. */
    private ObjectNode guardado(Callable<?> llamada) {
        guardados.clear();
        ObjectNode caso = JsonNodeFactory.instance.objectNode();
        caso.set("resultado", GrafoObjetos.resultado(llamada));
        caso.set("guardados", GrafoObjetos.volcar(new ArrayList<>(guardados)));
        return caso;
    }
}
