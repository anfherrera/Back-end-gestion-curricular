package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarEstadisticasGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadisticaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a DUP-10: lo que el gateway de Estadísticas guarda y devuelve al crear,
 * actualizar y consultar una {@code Estadistica}, con listas llenas, vacías y nulas.
 */
class EstadisticaPersistenciaCaracterizacionTest {

    private final List<Object> guardados = new ArrayList<>();

    @Test
    void estadistica() throws Exception {
        EstadisticaRepositoryInt repositorio = mock(EstadisticaRepositoryInt.class);
        SolicitudRepositoryInt solicitudes = mock(SolicitudRepositoryInt.class);
        ProgramaRepositoryInt programas = mock(ProgramaRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> {
            guardados.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        when(solicitudes.count()).thenReturn(4L);
        when(programas.count()).thenReturn(2L);
        when(solicitudes.totalSolicitudes()).thenReturn(4);
        when(solicitudes.contarEstado(anyString())).thenReturn(1);
        when(solicitudes.buscarNombresSolicitudes()).thenReturn(Set.of("Reingreso"));
        when(programas.buscarNombresProgramas()).thenReturn(List.of("Sistemas", "Electrónica"));
        when(solicitudes.contarNombreFechaEstadoYPrograma(any(), any(), any(), any(), any())).thenReturn(3);
        EstadisticaEntity completa = new EstadisticaEntity();
        completa.setId_estadistica(1);
        completa.setNombre("Resumen");
        completa.setTotal_solicitudes(10);
        completa.setTotal_aprobadas(6);
        completa.setTotal_rechazadas(2);
        completa.getPeriodos_academico().add(new Date(1_700_000_000_000L));
        completa.getNombres_procesos().add("Homologación");
        completa.getNombres_programas().add("Sistemas");
        EstadisticaEntity nula = new EstadisticaEntity();
        nula.setId_estadistica(2);
        nula.setPeriodos_academico(null);
        nula.setNombres_procesos(null);
        nula.setNombres_programas(null);
        when(repositorio.findById(1)).thenReturn(Optional.of(completa));
        when(repositorio.findById(2)).thenReturn(Optional.of(nula));
        when(repositorio.findById(9)).thenReturn(Optional.empty());
        GestionarEstadisticasGatewayImplAdapter gateway = gateway(repositorio, programas, solicitudes);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("obtenerEstadisticaPorId 1", GrafoObjetos.resultado(() -> gateway.obtenerEstadisticaPorId(1)));
        casos.set("obtenerEstadisticaPorId 2", GrafoObjetos.resultado(() -> gateway.obtenerEstadisticaPorId(2)));
        casos.set("obtenerEstadisticaPorId 9", GrafoObjetos.resultado(() -> gateway.obtenerEstadisticaPorId(9)));
        casos.set("obtenerEstadisticasSolicitudPeriodoEstadoYPrograma 2", GrafoObjetos.resultado(
                () -> gateway.obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(2, "Reingreso", null, null, null, 1)));
        Estadistica conListas = new Estadistica(5, "Nueva", 1, 1, 0,
                new ArrayList<>(List.of(new Date(1_600_000_000_000L))), new ArrayList<>(List.of("X")),
                new ArrayList<>(List.of("Y")));
        casos.set("crearEstadistica con listas", guardado(() -> gateway.crearEstadistica(conListas)));
        Estadistica sinListas = new Estadistica(null, null, null, null, null, null, null, null);
        casos.set("crearEstadistica nulos", guardado(() -> gateway.crearEstadistica(sinListas)));
        casos.set("actualizarEstadistica", guardado(() -> gateway.actualizarEstadistica(
                new Estadistica(1, "Otra", null, 2, null, null, new ArrayList<>(), null))));
        casos.set("actualizarEstadistica inexistente", guardado(() -> gateway.actualizarEstadistica(
                new Estadistica(9, "X", null, null, null, null, null, null))));
        GrafoObjetos.compararConReferencia(casos, "persistencia/estadistica.json");
    }

    private static GestionarEstadisticasGatewayImplAdapter gateway(EstadisticaRepositoryInt repositorio,
            ProgramaRepositoryInt programas, SolicitudRepositoryInt solicitudes) {
        return new GestionarEstadisticasGatewayImplAdapter(repositorio, programas, solicitudes,
                mock(UsuarioRepositoryInt.class));
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
