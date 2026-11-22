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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarRolGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarUsuarioGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a DUP-10: lo que los gateways de Usuario y Rol envían al repositorio y
 * lo que devuelven. Los grafos llevan ciclos (el rol con sus usuarios, que apuntan al rol),
 * solicitudes y cursos del usuario, y relaciones nulas.
 */
class UsuarioRolPersistenciaCaracterizacionTest {

    private final List<Object> guardados = new ArrayList<>();

    @Test
    void usuario() throws Exception {
        UsuarioRepositoryInt repositorio = mock(UsuarioRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> {
            UsuarioEntity entidad = inv.getArgument(0);
            guardados.add(entidad);
            if (entidad.getId_usuario() == null) {
                entidad.setId_usuario(50);
            }
            return entidad;
        });
        UsuarioEntity completo = usuarioEntityCompleto();
        UsuarioEntity sinRelaciones = new UsuarioEntity();
        sinRelaciones.setId_usuario(2);
        sinRelaciones.setCorreo("sin@unicauca.edu.co");
        sinRelaciones.setSolicitudes(null);
        sinRelaciones.setCursosOfertadosInscritos(null);
        UsuarioEntity rolSinNombre = new UsuarioEntity();
        rolSinNombre.setId_usuario(3);
        rolSinNombre.setCorreo("rol@unicauca.edu.co");
        rolSinNombre.setObjRol(new RolEntity());
        ProgramaEntity programaParcial = new ProgramaEntity();
        programaParcial.setId_programa(4);
        rolSinNombre.setObjPrograma(programaParcial);
        when(repositorio.findById(1)).thenReturn(Optional.of(completo));
        when(repositorio.findById(2)).thenReturn(Optional.of(sinRelaciones));
        when(repositorio.findById(9)).thenReturn(Optional.empty());
        when(repositorio.findByIdWithRelations(1)).thenReturn(Optional.of(completo));
        when(repositorio.findByIdWithRelations(3)).thenReturn(Optional.of(rolSinNombre));
        when(repositorio.findByIdWithRelations(50)).thenAnswer(inv -> Optional.of(guardadoConRelaciones()));
        when(repositorio.buscarPorCorreoWithRelations("ana@unicauca.edu.co")).thenReturn(Optional.of(completo));
        when(repositorio.buscarPorCedula("123")).thenReturn(Optional.of(completo));
        when(repositorio.buscarPorRol(1)).thenReturn(List.of(completo, sinRelaciones));
        when(repositorio.findAll()).thenReturn(List.of(sinRelaciones, rolSinNombre, completo));
        when(repositorio.buscarUsuariosPorSolicitud(7)).thenReturn(completo);
        when(repositorio.buscarIdUsuarioPorCodigo("104")).thenReturn(rolSinNombre);
        when(repositorio.buscarPorCorreo("ana@unicauca.edu.co")).thenReturn(Optional.of(completo));
        when(repositorio.buscarPorCorreo("sin@unicauca.edu.co")).thenReturn(Optional.of(sinRelaciones));
        GestionarUsuarioGatewayImplAdapter gateway = usuarioGateway(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("obtenerUsuarioPorId 1", GrafoObjetos.resultado(() -> gateway.obtenerUsuarioPorId(1)));
        casos.set("obtenerUsuarioPorId 3", GrafoObjetos.resultado(() -> gateway.obtenerUsuarioPorId(3)));
        casos.set("buscarUsuarioPorId 2", GrafoObjetos.resultado(() -> gateway.buscarUsuarioPorId(2)));
        casos.set("buscarUsuarioPorId 9", GrafoObjetos.resultado(() -> gateway.buscarUsuarioPorId(9)));
        casos.set("buscarUsuarioPorCorreo", GrafoObjetos.resultado(() -> gateway.buscarUsuarioPorCorreo("ana@unicauca.edu.co")));
        casos.set("buscarUsuarioPorCedula", GrafoObjetos.resultado(() -> gateway.buscarUsuarioPorCedula(" 123 ")));
        casos.set("buscarUsuariosPorRol", GrafoObjetos.resultado(() -> gateway.buscarUsuariosPorRol(1)));
        casos.set("listarUsuarios", GrafoObjetos.resultado(gateway::listarUsuarios));
        casos.set("buscarUsuarioPorSolicitud", GrafoObjetos.resultado(() -> gateway.buscarUsuarioPorSolicitud(7)));
        casos.set("buscarUsuarioPorCodigo", GrafoObjetos.resultado(() -> gateway.buscarUsuarioPorCodigo("104")));
        casos.set("loadUserByUsername ana", GrafoObjetos.resultado(
                () -> gateway.loadUserByUsername("ana@unicauca.edu.co").toString()));
        casos.set("loadUserByUsername sin rol", GrafoObjetos.resultado(
                () -> gateway.loadUserByUsername("sin@unicauca.edu.co")));
        casos.set("crearUsuario completo", guardado(() -> gateway.crearUsuario(usuarioCompleto(null))));
        Usuario sinNombreRol = new Usuario();
        sinNombreRol.setObjRol(new Rol(2, null, null));
        sinNombreRol.setObjPrograma(new Programa(3, null, null));
        sinNombreRol.setCedula("  ");
        casos.set("crearUsuario relaciones parciales", guardado(() -> gateway.crearUsuario(sinNombreRol)));
        casos.set("actualizarUsuario completo", guardado(() -> gateway.actualizarUsuario(usuarioCompleto(1))));
        Usuario sinRelacionesDominio = new Usuario();
        sinRelacionesDominio.setIdUsuario(2);
        sinRelacionesDominio.setSolicitudes(null);
        sinRelacionesDominio.setCursosOfertadosInscritos(null);
        sinRelacionesDominio.setObjRol(new Rol(null, "SIN_ID", null));
        casos.set("actualizarUsuario sin relaciones", guardado(() -> gateway.actualizarUsuario(sinRelacionesDominio)));
        GrafoObjetos.compararConReferencia(casos, "persistencia/usuario.json",
                "persistencia/correcciones/usuario-bug46.json");
    }

    @Test
    void rol() throws Exception {
        RolRepositoryInt repositorio = mock(RolRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> {
            guardados.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        UsuarioEntity usuario = usuarioEntityCompleto();
        RolEntity conUsuarios = usuario.getObjRol();
        RolEntity vacio = new RolEntity();
        vacio.setUsuarios(null);
        when(repositorio.findById(1)).thenReturn(Optional.of(conUsuarios));
        when(repositorio.findById(2)).thenReturn(Optional.of(vacio));
        when(repositorio.findById(9)).thenReturn(Optional.empty());
        when(repositorio.buscarPorNombre("ESTUDIANTE")).thenReturn(Optional.of(conUsuarios));
        when(repositorio.findAll()).thenReturn(List.of(vacio, conUsuarios));
        GestionarRolGatewayImplAdapter gateway = rolGateway(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("buscarRolPorId 1", GrafoObjetos.resultado(() -> gateway.buscarRolPorId(1)));
        casos.set("buscarRolPorId 2", GrafoObjetos.resultado(() -> gateway.buscarRolPorId(2)));
        casos.set("buscarRolPorId 9", GrafoObjetos.resultado(() -> gateway.buscarRolPorId(9)));
        casos.set("obtenerRolPorId 1", GrafoObjetos.resultado(() -> gateway.obtenerRolPorId(1)));
        casos.set("obtenerRolPorId 9", GrafoObjetos.resultado(() -> gateway.obtenerRolPorId(9)));
        casos.set("obtenerRolPorNombre", GrafoObjetos.resultado(() -> gateway.obtenerRolPorNombre("ESTUDIANTE")));
        casos.set("listarRoles", GrafoObjetos.resultado(gateway::listarRoles));
        Usuario miembro = usuarioCompleto(1);
        Rol rol = miembro.getObjRol();
        rol.setUsuarios(new ArrayList<>(List.of(miembro)));
        casos.set("guardarRol con usuarios", guardado(() -> gateway.guardarRol(rol)));
        casos.set("guardarRol nulos", guardado(() -> gateway.guardarRol(new Rol())));
        casos.set("actualizarRol", guardado(() -> gateway.actualizarRol(new Rol(1, "ADMIN", new ArrayList<>()))));
        casos.set("actualizarRol inexistente", guardado(() -> gateway.actualizarRol(new Rol(9, "X", null))));
        GrafoObjetos.compararConReferencia(casos, "persistencia/rol.json",
                "persistencia/correcciones/rol-bug46.json");
    }

    private static GestionarUsuarioGatewayImplAdapter usuarioGateway(UsuarioRepositoryInt repositorio) {
        return new GestionarUsuarioGatewayImplAdapter(repositorio, mock(SolicitudRepositoryInt.class));
    }

    private static GestionarRolGatewayImplAdapter rolGateway(RolRepositoryInt repositorio) {
        return new GestionarRolGatewayImplAdapter(repositorio, mock(UsuarioRepositoryInt.class));
    }

    /** Usuario con rol (que lo lista entre sus usuarios), programa, una solicitud y un curso. */
    private static UsuarioEntity usuarioEntityCompleto() {
        RolEntity rol = new RolEntity();
        rol.setId_rol(1);
        rol.setNombre("ESTUDIANTE");
        ProgramaEntity programa = new ProgramaEntity();
        programa.setId_programa(1);
        programa.setCodigo("SIS");
        programa.setNombre_programa("Ingeniería de Sistemas");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId_usuario(1);
        usuario.setNombre_completo("Ana Pérez");
        usuario.setCodigo("104");
        usuario.setCedula("123");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setPassword("$2a$hash");
        usuario.setEstado_usuario(true);
        usuario.setObjRol(rol);
        usuario.setObjPrograma(programa);
        rol.getUsuarios().add(usuario);
        programa.getUsuarios().add(usuario);
        SolicitudReingresoEntity solicitud = new SolicitudReingresoEntity();
        solicitud.setId_solicitud(7);
        solicitud.setObjUsuario(usuario);
        usuario.getSolicitudes().add(solicitud);
        CursoOfertadoVeranoEntity curso = new CursoOfertadoVeranoEntity();
        curso.setId_curso(3);
        curso.getEstudiantesInscritos().add(usuario);
        usuario.getCursosOfertadosInscritos().add(curso);
        return usuario;
    }

    private static UsuarioEntity guardadoConRelaciones() {
        UsuarioEntity usuario = usuarioEntityCompleto();
        usuario.setId_usuario(50);
        return usuario;
    }

    private static Usuario usuarioCompleto(Integer id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombreCompleto("Ana Pérez");
        usuario.setCodigo("104");
        usuario.setCedula(" 123 ");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setPassword("$2a$hash");
        usuario.setEstadoUsuario(true);
        Rol rol = new Rol(1, "ESTUDIANTE", null);
        usuario.setObjRol(rol);
        usuario.setObjPrograma(new Programa(1, "SIS", "Ingeniería de Sistemas"));
        SolicitudReingreso solicitud = new SolicitudReingreso();
        solicitud.setIdSolicitud(7);
        solicitud.setObjUsuario(usuario);
        usuario.getSolicitudes().add(solicitud);
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(3);
        curso.getEstudiantesInscritos().add(usuario);
        usuario.getCursosOfertadosInscritos().add(curso);
        return usuario;
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
