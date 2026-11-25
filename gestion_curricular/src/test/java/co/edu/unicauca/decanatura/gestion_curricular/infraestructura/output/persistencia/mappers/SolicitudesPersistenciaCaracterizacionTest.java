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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.NotificacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarDocumentoGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarEstadoSolicitudGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarNotificacionGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarSolicitudHomologacionGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarSolicitudPazYSalvoGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarSolicitudReingresoGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocumentoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.NotificacionRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudHomologacionRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudPazYSalvoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudReingresoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.ProyeccionValores;

/**
 * Caracterización previa a DUP-10 de los gateways que mapean solicitudes con el
 * {@code ModelMapper} principal: Reingreso, Homologación y Paz y Salvo (a través de
 * {@code Mapper}), estados, documentos y notificaciones. Cada uno guarda su referencia.
 */
class SolicitudesPersistenciaCaracterizacionTest {

    private final List<Object> guardados = new ArrayList<>();

    @Test
    void reingreso() throws Exception {
        SolicitudReingresoRepositoryInt repositorio = mock(SolicitudReingresoRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        SolicitudReingresoEntity completa = GrafosSolicitud.completar(new SolicitudReingresoEntity());
        SolicitudReingresoEntity vacia = vacia(new SolicitudReingresoEntity());
        when(repositorio.findById(7)).thenAnswer(inv -> Optional.of(GrafosSolicitud.completar(new SolicitudReingresoEntity())));
        when(repositorio.findById(8)).thenReturn(Optional.of(vacia));
        when(repositorio.findAll()).thenReturn(List.of(completa, vacia));
        when(repositorio.findByUsuarioYPeriodo(1, "2025-1")).thenReturn(List.of(completa));
        GestionarSolicitudReingresoGatewayImplAdapter gateway = new GestionarSolicitudReingresoGatewayImplAdapter(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("listarPorUsuario", GrafoObjetos.resultado(() -> gateway.listarPorUsuario(1, "2025-1")));
        casos.set("buscarPorId 7", GrafoObjetos.resultado(() -> gateway.buscarPorId(7)));
        casos.set("buscarPorId 9", GrafoObjetos.resultado(() -> gateway.buscarPorId(9)));
        casos.set("crearSolicitudReingreso completa", guardado(
                () -> gateway.crearSolicitudReingreso(GrafosSolicitud.completar(new SolicitudReingreso()))));
        casos.set("crearSolicitudReingreso vacia", guardado(() -> gateway.crearSolicitudReingreso(vaciaDominio(new SolicitudReingreso()))));
        casos.set("cambiarEstado simple", guardado(() -> {
            gateway.cambiarEstadoSolicitudReingreso(8, estadoSimple());
            return null;
        }));
        casos.set("cambiarEstado con solicitud", guardado(() -> {
            gateway.cambiarEstadoSolicitudReingreso(7, estadoConSolicitud());
            return null;
        }));
        casos.set("cambiarEstado inexistente", guardado(() -> {
            gateway.cambiarEstadoSolicitudReingreso(9, estadoSimple());
            return null;
        }));
        ProyeccionValores.compararConReferencia(casos, "persistencia/reingreso.json",
                "persistencia/correcciones/reingreso-bug46.json",
                "persistencia/correcciones/reingreso-mue11.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/reingreso.json",
                "persistencia/correcciones/proyeccion-reingreso-bug46.json",
                "persistencia/correcciones/proyeccion-reingreso-mue11.json");
    }

    @Test
    void homologacion() throws Exception {
        SolicitudHomologacionRepositoryInt repositorio = mock(SolicitudHomologacionRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        SolicitudHomologacionEntity completa = homologacionEntity();
        SolicitudHomologacionEntity vacia = vacia(new SolicitudHomologacionEntity());
        when(repositorio.findById(7)).thenAnswer(inv -> Optional.of(homologacionEntity()));
        when(repositorio.findById(8)).thenReturn(Optional.of(vacia));
        when(repositorio.findByIdWithDocumentos(7)).thenReturn(Optional.of(completa));
        when(repositorio.findAll()).thenReturn(List.of(completa, vacia));
        GestionarSolicitudHomologacionGatewayImplAdapter gateway = new GestionarSolicitudHomologacionGatewayImplAdapter(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("buscarPorId 7", GrafoObjetos.resultado(() -> gateway.buscarPorId(7)));
        SolicitudHomologacion nueva = GrafosSolicitud.completar(new SolicitudHomologacion());
        nueva.setProgramaOrigen("Electrónica");
        nueva.setProgramaDestino("Sistemas");
        nueva.setRutaPmFo4For27("h/form.pdf");
        nueva.setRutaContenidoProgramatico(null);
        casos.set("guardar completa", guardado(() -> gateway.guardar(nueva)));
        casos.set("guardar vacia", guardado(() -> gateway.guardar(vaciaDominio(new SolicitudHomologacion()))));
        casos.set("cambiarEstado simple", guardado(() -> {
            gateway.cambiarEstadoSolicitud(8, estadoSimple());
            return null;
        }));
        casos.set("cambiarEstado con solicitud", guardado(() -> {
            gateway.cambiarEstadoSolicitud(7, estadoConSolicitud());
            return null;
        }));
        ProyeccionValores.compararConReferencia(casos, "persistencia/homologacion.json",
                "persistencia/correcciones/homologacion-bug46.json",
                "persistencia/correcciones/homologacion-mue11.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/homologacion.json",
                "persistencia/correcciones/proyeccion-homologacion-bug46.json",
                "persistencia/correcciones/proyeccion-homologacion-mue11.json");
    }

    @Test
    void pazYSalvo() throws Exception {
        SolicitudPazYSalvoRepositoryInt repositorio = mock(SolicitudPazYSalvoRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        SolicitudPazYSalvoEntity completa = GrafosSolicitud.completar(new SolicitudPazYSalvoEntity());
        completa.setTitulo_trabajo_grado("Tesis");
        completa.setDirector_trabajo_grado("Director");
        SolicitudPazYSalvoEntity vacia = vacia(new SolicitudPazYSalvoEntity());
        when(repositorio.findByIdWithRelations(7)).thenReturn(Optional.of(completa));
        when(repositorio.findByIdWithRelations(8)).thenReturn(Optional.of(vacia));
        when(repositorio.findAll()).thenReturn(List.of(completa, vacia));
        GestionarSolicitudPazYSalvoGatewayImplAdapter gateway = new GestionarSolicitudPazYSalvoGatewayImplAdapter(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("listarSolicitudes", GrafoObjetos.resultado(gateway::listarSolicitudes));
        casos.set("buscarPorId 7", GrafoObjetos.resultado(() -> gateway.buscarPorId(7)));
        casos.set("buscarPorId 8", GrafoObjetos.resultado(() -> gateway.buscarPorId(8)));
        SolicitudPazYSalvo nueva = GrafosSolicitud.completar(new SolicitudPazYSalvo());
        nueva.setTituloTrabajoGrado("Tesis nueva");
        casos.set("guardar completa", guardado(() -> gateway.guardar(nueva)));
        casos.set("guardar vacia", guardado(() -> gateway.guardar(vaciaDominio(new SolicitudPazYSalvo()))));
        ProyeccionValores.compararConReferencia(casos, "persistencia/pazysalvo.json",
                "persistencia/correcciones/pazysalvo-bug46.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/pazysalvo.json",
                "persistencia/correcciones/proyeccion-pazysalvo-bug46.json");
    }

    @Test
    void estadoSolicitud() throws Exception {
        EstadoSolicitudRepositoryInt repositorio = mock(EstadoSolicitudRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        GestionarEstadoSolicitudGatewayImplAdapter gateway = new GestionarEstadoSolicitudGatewayImplAdapter(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("guardar simple", guardado(() -> gateway.guarEstadoSolicitud(estadoSimple())));
        casos.set("guardar con solicitud", guardado(() -> gateway.guarEstadoSolicitud(estadoConSolicitud())));
        casos.set("guardar sin estado", guardado(() -> gateway.guarEstadoSolicitud(new EstadoSolicitud(null, null, null, null, null))));
        casos.set("guardar nulo", guardado(() -> gateway.guarEstadoSolicitud(null)));
        ProyeccionValores.compararConReferencia(casos, "persistencia/estado-solicitud.json",
                "persistencia/correcciones/estado-solicitud-bug46.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/estado-solicitud.json",
                "persistencia/correcciones/proyeccion-estado-solicitud-bug46.json");
    }

    @Test
    void documento() throws Exception {
        DocumentoRepositoryInt repositorio = mock(DocumentoRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        DocumentoEntity conSolicitud = GrafosSolicitud.completar(new SolicitudReingresoEntity()).getDocumentos().get(0);
        DocumentoEntity suelto = new DocumentoEntity();
        suelto.setId_documento(4);
        suelto.setIdUsuarioSubio(1);
        when(repositorio.findById(3)).thenAnswer(inv -> Optional.of(
                GrafosSolicitud.completar(new SolicitudReingresoEntity()).getDocumentos().get(0)));
        when(repositorio.findById(4)).thenReturn(Optional.of(suelto));
        when(repositorio.findByObjSolicitudIsNullAndIdUsuarioSubio(1)).thenReturn(List.of(suelto, conSolicitud));
        GestionarDocumentoGatewayImplAdapter gateway = new GestionarDocumentoGatewayImplAdapter(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("buscarDocumentoId 3", GrafoObjetos.resultado(() -> gateway.buscarDocumentoId(3)));
        casos.set("buscarDocumentoId 4", GrafoObjetos.resultado(() -> gateway.buscarDocumentoId(4)));
        casos.set("buscarDocumentosSinSolicitud", GrafoObjetos.resultado(() -> gateway.buscarDocumentosSinSolicitud(1)));
        Documento nuevo = GrafosSolicitud.completar(new SolicitudReingreso()).getDocumentos().get(0);
        casos.set("crearDocumento con solicitud", guardado(() -> gateway.crearDocumento(nuevo)));
        casos.set("crearDocumento vacio", guardado(() -> gateway.crearDocumento(new Documento())));
        Documento cambio = GrafosSolicitud.completar(new SolicitudHomologacion()).getDocumentos().get(0);
        cambio.setNombre("nuevo.pdf");
        cambio.setEsValido(false);
        casos.set("actualizarDocumento con solicitud", guardado(() -> gateway.actualizarDocumento(cambio)));
        Documento sinSolicitud = new Documento(4, "otro.pdf", null, null, true, null, 9, null);
        casos.set("actualizarDocumento sin solicitud", guardado(() -> gateway.actualizarDocumento(sinSolicitud)));
        ProyeccionValores.compararConReferencia(casos, "persistencia/documento.json",
                "persistencia/correcciones/documento-bug46.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/documento.json",
                "persistencia/correcciones/proyeccion-documento-bug46.json");
    }

    @Test
    void notificacion() throws Exception {
        NotificacionRepositoryInt repositorio = mock(NotificacionRepositoryInt.class);
        UsuarioRepositoryInt usuarios = mock(UsuarioRepositoryInt.class);
        SolicitudRepositoryInt solicitudes = mock(SolicitudRepositoryInt.class);
        CursoOfertadoVeranoRepositoryInt cursos = mock(CursoOfertadoVeranoRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        UsuarioEntity usuario = GrafosSolicitud.usuarioEntity();
        when(usuarios.findById(1)).thenReturn(Optional.of(usuario));
        when(solicitudes.findById(7)).thenAnswer(inv -> Optional.of(GrafosSolicitud.completar(new SolicitudReingresoEntity())));
        when(cursos.findById(4)).thenAnswer(inv -> Optional.of(GrafosSolicitud.cursoEntity(GrafosSolicitud.usuarioEntity())));
        NotificacionEntity completa = notificacionEntity();
        NotificacionEntity vacia = new NotificacionEntity();
        vacia.setId_notificacion(2);
        vacia.setLeida(null);
        vacia.setEsUrgente(null);
        when(repositorio.findById(1)).thenReturn(Optional.of(completa));
        when(repositorio.findById(2)).thenReturn(Optional.of(vacia));
        when(repositorio.buscarPorUsuario(1)).thenReturn(List.of(completa, vacia));
        when(repositorio.buscarUrgentesPorUsuario(1)).thenReturn(List.of(vacia, completa));
        GestionarNotificacionGatewayImplAdapter gateway = new GestionarNotificacionGatewayImplAdapter(
                repositorio, usuarios, solicitudes, cursos);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("obtenerNotificacionPorId 1", GrafoObjetos.resultado(() -> gateway.obtenerNotificacionPorId(1)));
        casos.set("obtenerNotificacionPorId 2", GrafoObjetos.resultado(() -> gateway.obtenerNotificacionPorId(2)));
        casos.set("buscarPorUsuario", GrafoObjetos.resultado(() -> gateway.buscarPorUsuario(1)));
        casos.set("buscarUrgentesPorUsuario", GrafoObjetos.resultado(() -> gateway.buscarUrgentesPorUsuario(1)));
        Notificacion nueva = new Notificacion(null, "REINGRESO", "APROBADA", "Título", "Mensaje",
                GrafosSolicitud.FECHA_1, null, true, "ver", "/r/7", usuarioConId(1),
                GrafosSolicitud.completar(new SolicitudReingreso()), cursoConId(4));
        casos.set("crearNotificacion completa", guardado(() -> gateway.crearNotificacion(nueva)));
        ProyeccionValores.compararConReferencia(casos, "persistencia/notificacion.json",
                "persistencia/correcciones/notificacion-bug46.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/notificacion.json",
                "persistencia/correcciones/proyeccion-notificacion-bug46.json");
    }

    private static SolicitudHomologacionEntity homologacionEntity() {
        SolicitudHomologacionEntity entidad = GrafosSolicitud.completar(new SolicitudHomologacionEntity());
        entidad.setPrograma_origen("Electrónica");
        entidad.setPrograma_destino("Sistemas");
        entidad.setRuta_PM_FO_4_FOR_27("h/form.pdf");
        return entidad;
    }

    private static NotificacionEntity notificacionEntity() {
        NotificacionEntity entidad = new NotificacionEntity();
        entidad.setId_notificacion(1);
        entidad.setTipoSolicitud("REINGRESO");
        entidad.setTipoNotificacion("APROBADA");
        entidad.setTitulo("Título");
        entidad.setMensaje("Mensaje");
        entidad.setFechaCreacion(GrafosSolicitud.FECHA_2);
        entidad.setLeida(true);
        entidad.setEsUrgente(false);
        entidad.setAccion("ver");
        entidad.setUrlAccion("/r/7");
        SolicitudReingresoEntity solicitud = GrafosSolicitud.completar(new SolicitudReingresoEntity());
        entidad.setObjUsuario(solicitud.getObjUsuario());
        entidad.setObjSolicitud(solicitud);
        entidad.setObjCurso(solicitud.getObjCursoOfertadoVerano());
        return entidad;
    }

    private static <T extends SolicitudEntity> T vacia(T entidad) {
        entidad.setId_solicitud(8);
        entidad.setEstadosSolicitud(null);
        entidad.setDocumentos(null);
        return entidad;
    }

    private static <T extends Solicitud> T vaciaDominio(T solicitud) {
        solicitud.setEstadosSolicitud(null);
        solicitud.setDocumentos(null);
        return solicitud;
    }

    private static EstadoSolicitud estadoSimple() {
        return new EstadoSolicitud(null, "APROBADA", GrafosSolicitud.FECHA_3, "bien", null);
    }

    private static EstadoSolicitud estadoConSolicitud() {
        SolicitudReingreso solicitud = GrafosSolicitud.completar(new SolicitudReingreso());
        return new EstadoSolicitud(null, "RECHAZADA", GrafosSolicitud.FECHA_3, "falta", solicitud);
    }

    private static Usuario usuarioConId(int id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        return usuario;
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
