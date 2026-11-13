package co.edu.unicauca.decanatura.gestion_curricular.soporte;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.TipoDocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * Crea solicitudes propias de cada prueba, ya en el estado que la prueba necesita.
 *
 * Evita depender de ids fijos ("la solicitud 1"), cuyo estado cambia según qué pruebas
 * corrieron antes y que, con el flujo de estados validado, dejan de admitir el paso probado.
 */
public final class SolicitudesDePrueba {

    private final SolicitudRepositoryInt solicitudRepository;
    private final UsuarioRepositoryInt usuarioRepository;

    public SolicitudesDePrueba(SolicitudRepositoryInt solicitudRepository, UsuarioRepositoryInt usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Homologación cuyo último estado es {@code estado} ("Enviada", "APROBADA_FUNCIONARIO"...). */
    public int homologacion(String estado) {
        SolicitudHomologacionEntity s = new SolicitudHomologacionEntity();
        s.setPrograma_origen("Ingeniería Electrónica");
        s.setPrograma_destino("Ingeniería de Sistemas");
        return guardar(s, "Homologación de prueba", estado);
    }

    public int reingreso(String estado) {
        return guardar(new SolicitudReingresoEntity(), "Reingreso de prueba", estado);
    }

    public int ecaes(String estado) {
        SolicitudEcaesEntity s = new SolicitudEcaesEntity();
        s.setTipoDocumento(TipoDocumentoEntity.CC);
        s.setNumero_documento("100200300");
        s.setFecha_expedicion(new Date());
        s.setFecha_nacimiento(new Date());
        return guardar(s, "ECAES de prueba", estado);
    }

    private int guardar(SolicitudEntity s, String nombre, String estado) {
        s.setNombre_solicitud(nombre);
        s.setPeriodo_academico("2026-2");
        s.setFecha_registro_solicitud(new Date());
        usuarioRepository.findAll().stream().findFirst().ifPresent(s::setObjUsuario);

        List<EstadoSolicitudEntity> estados = new ArrayList<>();
        // La fecha queda en el pasado para que el estado nuevo de la prueba sea el último
        estados.add(estado(s, "Enviada", 120_000));
        if (!"Enviada".equalsIgnoreCase(estado)) {
            estados.add(estado(s, estado, 60_000));
        }
        s.setEstadosSolicitud(estados);
        return solicitudRepository.save(s).getId_solicitud();
    }

    private static EstadoSolicitudEntity estado(SolicitudEntity s, String valor, long haceMs) {
        EstadoSolicitudEntity e = new EstadoSolicitudEntity();
        e.setEstado_actual(valor);
        e.setFecha_registro_estado(new Date(System.currentTimeMillis() - haceMs));
        e.setObjSolicitud(s);
        return e;
    }
}
