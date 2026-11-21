package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.TipoDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.FechaEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarPreRegistroEcaesGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.FechaEcaesRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudEcaesRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.ProyeccionValores;

/**
 * Caracterización previa a DUP-10 del gateway de ECAES, que mapea con un {@code ModelMapper}
 * propio sin configurar (estrategia estándar, sin {@code skipNull} ni mapas de tipos): solicitudes,
 * estados y fechas.
 */
class EcaesPersistenciaCaracterizacionTest {

    private final List<Object> guardados = new ArrayList<>();

    @Test
    void ecaes() throws Exception {
        SolicitudEcaesRepositoryInt solicitudes = mock(SolicitudEcaesRepositoryInt.class);
        FechaEcaesRepositoryInt fechas = mock(FechaEcaesRepositoryInt.class);
        when(solicitudes.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        when(fechas.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        SolicitudEcaesEntity vacia = new SolicitudEcaesEntity();
        vacia.setId_solicitud(8);
        vacia.setEstadosSolicitud(null);
        vacia.setDocumentos(null);
        when(solicitudes.findById(7)).thenAnswer(inv -> Optional.of(ecaesEntity()));
        when(solicitudes.findById(8)).thenReturn(Optional.of(vacia));
        when(solicitudes.listarSolicitudesConUsuarios()).thenAnswer(inv -> List.of(ecaesEntity(), vacia));
        when(solicitudes.findByUsuarioYPeriodo(1, "2025-1")).thenAnswer(inv -> List.of(ecaesEntity()));
        FechaEcaesEntity fecha = new FechaEcaesEntity();
        fecha.setId_fecha_ecaes(3);
        fecha.setPeriodoAcademico("2025-1");
        fecha.setInscripcion_est_by_facultad(LocalDate.of(2025, 2, 1));
        fecha.setCitacion(LocalDate.of(2025, 3, 1));
        FechaEcaesEntity fechaVacia = new FechaEcaesEntity();
        when(fechas.findAll()).thenReturn(List.of(fecha, fechaVacia));
        when(fechas.findByPeriodoAcademico("2025-1")).thenReturn(Optional.of(fecha));
        when(fechas.findByPeriodoAcademico("2026-1")).thenReturn(Optional.empty());
        GestionarPreRegistroEcaesGatewayImplAdapter gateway = new GestionarPreRegistroEcaesGatewayImplAdapter(
                solicitudes, fechas);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("listar", GrafoObjetos.resultado(gateway::listar));
        casos.set("listarPorUsuario", GrafoObjetos.resultado(() -> gateway.listarPorUsuario(1, "2025-1")));
        casos.set("buscarPorId 7", GrafoObjetos.resultado(() -> gateway.buscarPorId(7)));
        casos.set("buscarOpcionalPorId 8", GrafoObjetos.resultado(() -> gateway.buscarOpcionalPorId(8)));
        casos.set("buscarOpcionalPorId 9", GrafoObjetos.resultado(() -> gateway.buscarOpcionalPorId(9)));
        SolicitudEcaes nueva = GrafosSolicitud.completar(new SolicitudEcaes());
        nueva.setTipoDocumento(TipoDocumento.CE);
        nueva.setNumero_documento("987");
        nueva.setFecha_nacimiento(GrafosSolicitud.FECHA_1);
        casos.set("guardar completa", guardado(() -> gateway.guardar(nueva)));
        SolicitudEcaes sinListas = new SolicitudEcaes();
        sinListas.setEstadosSolicitud(null);
        sinListas.setDocumentos(null);
        casos.set("guardar sin listas", guardado(() -> gateway.guardar(sinListas)));
        casos.set("cambiarEstado", guardado(() -> {
            gateway.cambiarEstadoSolicitudEcaes(7, new EstadoSolicitud(null, "PRE_REGISTRADO", GrafosSolicitud.FECHA_3, "ok", null));
            return null;
        }));
        casos.set("cambiarEstado sin estado", guardado(() -> {
            gateway.cambiarEstadoSolicitudEcaes(7, new EstadoSolicitud(null, null, null, null, null));
            return null;
        }));
        casos.set("listarFechasEcaes", GrafoObjetos.resultado(gateway::listarFechasEcaes));
        casos.set("buscarFechasPorPeriodo", GrafoObjetos.resultado(() -> gateway.buscarFechasPorPeriodo("2025-1")));
        FechaEcaes nuevasFechas = new FechaEcaes(null, "2026-1", LocalDate.of(2026, 2, 1), null,
                LocalDate.of(2026, 2, 20), null, LocalDate.of(2026, 4, 1), null);
        casos.set("publicarFechasEcaes", guardado(() -> gateway.publicarFechasEcaes(nuevasFechas)));
        casos.set("publicarFechasEcaes vacias", guardado(() -> gateway.publicarFechasEcaes(new FechaEcaes())));
        casos.set("actualizarFechasEcaes existente", guardado(() -> gateway.actualizarFechasEcaes(
                new FechaEcaes(5, "2025-1", null, LocalDate.of(2025, 2, 5), null, null, null, null))));
        casos.set("actualizarFechasEcaes nueva", guardado(() -> gateway.actualizarFechasEcaes(nuevasFechas)));
        ProyeccionValores.compararConReferencia(casos, "persistencia/ecaes.json");
        GrafoObjetos.compararConReferencia(casos, "persistencia/proyeccion/ecaes.json");
    }

    private static SolicitudEcaesEntity ecaesEntity() {
        SolicitudEcaesEntity entidad = GrafosSolicitud.completar(new SolicitudEcaesEntity());
        entidad.setTipoDocumento(TipoDocumento.CC);
        entidad.setNumero_documento("123");
        entidad.setFecha_expedicion(GrafosSolicitud.FECHA_2);
        return entidad;
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
