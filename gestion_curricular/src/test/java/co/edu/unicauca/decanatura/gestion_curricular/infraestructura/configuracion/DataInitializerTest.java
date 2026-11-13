package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

@DisplayName("DataInitializer - cuenta de administrador inicial")
class DataInitializerTest {

    private GestionarUsuarioGatewayIntPort usuarioGateway;
    private GestionarRolGatewayIntPort rolGateway;
    private GestionarProgramaGatewayIntPort programaGateway;
    private GestionarUsuarioCUIntPort usuarioCU;
    private DataInitializer inicializador;

    @BeforeEach
    void preparar() {
        usuarioGateway = mock(GestionarUsuarioGatewayIntPort.class);
        rolGateway = mock(GestionarRolGatewayIntPort.class);
        programaGateway = mock(GestionarProgramaGatewayIntPort.class);
        usuarioCU = mock(GestionarUsuarioCUIntPort.class);
        inicializador = new DataInitializer(usuarioGateway, rolGateway, programaGateway, usuarioCU);
        configurar("000009000", "clave-inicial");
    }

    @Test
    @DisplayName("no modifica una cuenta de administrador que ya existe")
    void noTocaAdministradorExistente() {
        Usuario existente = new Usuario();
        existente.setPassword("$2a$10$hashDeUnaContrasenaCambiada");
        when(usuarioGateway.buscarUsuarioPorCorreo(DataInitializer.CORREO_ADMINISTRADOR)).thenReturn(existente);

        inicializador.run();

        verify(usuarioGateway, never()).actualizarUsuario(any());
        verifyNoInteractions(usuarioCU);
        assertThat(existente.getPassword()).isEqualTo("$2a$10$hashDeUnaContrasenaCambiada");
    }

    @Test
    @DisplayName("crea la cuenta con la contraseña configurada si no existe")
    void creaAdministradorSiNoExiste() {
        when(rolGateway.bucarRolPorId(1)).thenReturn(new Rol());
        when(programaGateway.buscarPorIdPrograma(1)).thenReturn(new Programa());

        inicializador.run();

        ArgumentCaptor<Usuario> creado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioCU).crearUsuario(creado.capture());
        assertThat(creado.getValue().getCorreo()).isEqualTo(DataInitializer.CORREO_ADMINISTRADOR);
        assertThat(creado.getValue().getPassword()).isEqualTo("clave-inicial");
        assertThat(creado.getValue().getCedula()).isEqualTo("000009000");
    }

    @Test
    @DisplayName("no hace nada si falta la contraseña")
    void sinContrasenaNoHaceNada() {
        configurar("000009000", "");

        inicializador.run();

        verifyNoInteractions(usuarioGateway, usuarioCU);
    }

    private void configurar(String cedula, String password) {
        ReflectionTestUtils.setField(inicializador, "cedulaAdministrador", cedula);
        ReflectionTestUtils.setField(inicializador, "passwordAdministrador", password);
    }
}
