package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarUsuarioCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion.NombresJsonDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarUsuarioGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a BUG-46: qué hash de contraseña llega a la base al crear y actualizar un usuario,
 * cuál recibe el login y qué JSON produce el {@code Usuario} del dominio que devuelve el gateway, como lo
 * serializan las respuestas que vuelcan modelos del dominio. Usa el caso de uso y el gateway reales sobre un
 * repositorio simulado.
 */
class ContrasenaUsuarioCaracterizacionTest {

    private static final String HASH = "$2a$10$hashGuardado";

    private final List<UsuarioEntity> guardados = new ArrayList<>();

    @Test
    void contrasena() throws Exception {
        UsuarioRepositoryInt repositorio = mock(UsuarioRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> {
            UsuarioEntity entidad = inv.getArgument(0);
            guardados.add(entidad);
            if (entidad.getId_usuario() == null) {
                entidad.setId_usuario(50);
            }
            return entidad;
        });
        when(repositorio.findById(1)).thenAnswer(inv -> Optional.of(usuarioGuardado(1)));
        when(repositorio.findByIdWithRelations(1)).thenAnswer(inv -> Optional.of(usuarioGuardado(1)));
        when(repositorio.findByIdWithRelations(50)).thenAnswer(inv -> Optional.of(usuarioGuardado(50)));
        when(repositorio.buscarPorCorreo("ana@unicauca.edu.co")).thenAnswer(inv -> Optional.of(usuarioGuardado(1)));
        GestionarUsuarioGatewayImplAdapter gateway =
                new GestionarUsuarioGatewayImplAdapter(repositorio);
        GestionarProgramaGatewayIntPort programas = mock(GestionarProgramaGatewayIntPort.class);
        when(programas.buscarPorIdPrograma(1)).thenReturn(new Programa(1, "SIS", "Ingeniería de Sistemas"));
        GestionarRolGatewayIntPort roles = mock(GestionarRolGatewayIntPort.class);
        when(roles.buscarRolPorId(1)).thenAnswer(inv -> new Rol(1, "ESTUDIANTE", new ArrayList<>()));
        GestionarUsuarioCUAdapter casoDeUso =
                new GestionarUsuarioCUAdapter(gateway, programas, roles, clave -> "codificada(" + clave + ")");

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("crearUsuario", guardado(() -> casoDeUso.crearUsuario(peticion(null, "clave"))));
        casos.set("actualizarUsuario sin contrasena", guardado(() -> casoDeUso.actualizarUsuario(peticion(1, null))));
        casos.set("actualizarUsuario contrasena en blanco",
                guardado(() -> casoDeUso.actualizarUsuario(peticion(1, "  "))));
        casos.set("actualizarUsuario con contrasena",
                guardado(() -> casoDeUso.actualizarUsuario(peticion(1, "nueva"))));
        casos.set("loadUserByUsername", GrafoObjetos.resultado(
                () -> gateway.loadUserByUsername("ana@unicauca.edu.co").getPassword()));
        ObjectMapper json = new ObjectMapper().registerModule(NombresJsonDominio.modulo());
        casos.set("json obtenerUsuarioPorId",
                json.readTree(json.writeValueAsString(gateway.obtenerUsuarioPorId(1))));
        GrafoObjetos.compararConReferencia(casos, "persistencia/usuario-contrasena.json",
                "persistencia/correcciones/usuario-contrasena-bug46.json");
    }

    /** La contraseña que se envió a {@code save} y la del usuario que devolvió el caso de uso. */
    private ObjectNode guardado(Callable<Usuario> llamada) throws Exception {
        guardados.clear();
        Usuario resultado = llamada.call();
        ObjectNode caso = JsonNodeFactory.instance.objectNode();
        caso.put("guardada", guardados.size() == 1 ? guardados.get(0).getPassword() : "guardados: " + guardados.size());
        caso.put("devuelta", resultado.getPassword());
        return caso;
    }

    private static Usuario peticion(Integer id, String contrasena) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombreCompleto("Ana Pérez");
        usuario.setCodigo("104");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setPassword(contrasena);
        usuario.setEstadoUsuario(true);
        usuario.setObjRol(new Rol(1, null, null));
        usuario.setObjPrograma(new Programa(1, null, null));
        return usuario;
    }

    private static UsuarioEntity usuarioGuardado(Integer id) {
        RolEntity rol = new RolEntity();
        rol.setId_rol(1);
        rol.setNombre("ESTUDIANTE");
        ProgramaEntity programa = new ProgramaEntity();
        programa.setId_programa(1);
        programa.setCodigo("SIS");
        programa.setNombre_programa("Ingeniería de Sistemas");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId_usuario(id);
        usuario.setNombre_completo("Ana Pérez");
        usuario.setCodigo("104");
        usuario.setCedula("123");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setPassword(HASH);
        usuario.setEstado_usuario(true);
        usuario.setObjRol(rol);
        usuario.setObjPrograma(programa);
        return usuario;
    }
}
