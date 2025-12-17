package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.Mapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudPazYSalvoRepositoryInt;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GestionarSolicitudPazYSalvoGatewayImplAdapter implements GestionarSolicitudPazYSalvoGatewayIntPort {

    private final SolicitudPazYSalvoRepositoryInt solicitudRepository;
    private final Mapper mapper;

    public GestionarSolicitudPazYSalvoGatewayImplAdapter(SolicitudPazYSalvoRepositoryInt solicitudRepository, Mapper mapper) {
        this.solicitudRepository = solicitudRepository;
        this.mapper = mapper;
    }

    @Override
    public SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitudPazYSalvo) {
        SolicitudPazYSalvoEntity entity = mapper.map(solicitudPazYSalvo, SolicitudPazYSalvoEntity.class);
        
        // Generar nombre descriptivo con el nombre del estudiante
        String nombreSolicitud = "Solicitud Paz y Salvo";
        if (solicitudPazYSalvo.getObjUsuario() != null && solicitudPazYSalvo.getObjUsuario().getNombre_completo() != null) {
            nombreSolicitud = "Solicitud Paz y Salvo - " + solicitudPazYSalvo.getObjUsuario().getNombre_completo();
        }
        entity.setNombre_solicitud(nombreSolicitud);
        entity.setFecha_registro_solicitud(new Date());

        // Crear estado inicial
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(entity);

        List<EstadoSolicitudEntity> estadosSolicitud = entity.getEstadosSolicitud();
        estadosSolicitud.add(estadoSolicitudEntity);
        entity.setEstadosSolicitud(estadosSolicitud);

        SolicitudPazYSalvoEntity savedEntity = solicitudRepository.save(entity);
        return mapper.map(savedEntity, SolicitudPazYSalvo.class);
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudes() {
        return solicitudRepository.findAll().stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToFuncionario() {
        // Buscar solo las solicitudes cuyo último estado sea "Enviada"
        return solicitudRepository.findByUltimoEstado("Enviada").stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToCoordinador() {
        return solicitudRepository.findByUltimoEstado("APROBADA_FUNCIONARIO").stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToCoordinadorPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            return listarSolicitudesToCoordinador();
        }
        return solicitudRepository.findByUltimoEstadoAndPrograma("APROBADA_FUNCIONARIO", idPrograma).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToSecretaria() {
        return solicitudRepository.findByUltimoEstado("APROBADA_COORDINADOR").stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToSecretaria() {
        // Buscar solicitudes que hayan tenido estado APROBADA en su historial (no solo como último estado)
        // Esto permite ver el historial incluso después de que cambien de estado
        return solicitudRepository.findByEstadoEnHistorialAndPeriodoAcademico("APROBADA", null).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToFuncionario() {
        // Solicitudes que el funcionario ya procesó (aprobó) - buscar en historial completo, no solo último estado
        return solicitudRepository.findByEstadoEnHistorialAndPeriodoAcademico("APROBADA_FUNCIONARIO", null).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToCoordinador() {
        // Solicitudes que el coordinador ya procesó (aprobó) - buscar en historial completo, no solo último estado
        return solicitudRepository.findByEstadoEnHistorialAndPeriodoAcademico("APROBADA_COORDINADOR", null).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToCoordinadorPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            return listarSolicitudesAprobadasToCoordinador();
        }
        // Buscar en historial completo, no solo último estado
        return solicitudRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico("APROBADA_COORDINADOR", idPrograma, null).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public Optional<SolicitudPazYSalvo> buscarPorId(Integer idSolicitud) {
        // Usar el método con JOIN FETCH para cargar todas las relaciones, incluyendo el usuario completo con su cédula
        return solicitudRepository.findByIdWithRelations(idSolicitud)
                .map(entity -> {
                    // Verificar que el usuario esté cargado con todas sus propiedades (incluyendo cédula)
                    if (entity.getObjUsuario() != null) {
                        // Forzar acceso a la cédula para asegurar que esté cargada desde la BD
                        entity.getObjUsuario().getCedula();
                        // Verificar que el programa esté cargado
                        if (entity.getObjUsuario().getObjPrograma() != null) {
                            entity.getObjUsuario().getObjPrograma().getNombre_programa();
                        }
                    }
                    // Forzar la carga de documentos si no se cargaron con JOIN FETCH
                    if (entity.getDocumentos() != null) {
                        entity.getDocumentos().size(); // Esto fuerza la carga lazy
                    }
                    // Forzar la carga de estados con sus comentarios si no se cargaron con JOIN FETCH
                    if (entity.getEstadosSolicitud() != null) {
                        entity.getEstadosSolicitud().size(); // Esto fuerza la carga lazy
                        // Asegurar que los comentarios se carguen
                        entity.getEstadosSolicitud().forEach(estado -> {
                            if (estado.getComentario() != null) {
                                estado.getComentario(); // Forzar carga del comentario
                            }
                        });
                    }
                    return mapper.map(entity, SolicitudPazYSalvo.class);
                });
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToFuncionarioPorPeriodo(String periodoAcademico) {
        return solicitudRepository.findByUltimoEstadoAndPeriodoAcademico("Enviada", periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToFuncionarioPorPeriodo(String periodoAcademico) {
        // Buscar en historial completo, no solo último estado
        return solicitudRepository.findByEstadoEnHistorialAndPeriodoAcademico("APROBADA_FUNCIONARIO", periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        if (idPrograma == null) {
            return listarSolicitudesToCoordinador();
        }
        return solicitudRepository.findByUltimoEstadoAndProgramaAndPeriodoAcademico("APROBADA_FUNCIONARIO", idPrograma, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        if (idPrograma == null) {
            return listarSolicitudesAprobadasToCoordinador();
        }
        // Buscar en historial completo, no solo último estado
        return solicitudRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico("APROBADA_COORDINADOR", idPrograma, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToSecretariaPorPeriodo(String periodoAcademico) {
        return solicitudRepository.findByUltimoEstadoAndPeriodoAcademico("APROBADA_COORDINADOR", periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico) {
        // Buscar en historial completo, no solo último estado
        return solicitudRepository.findByEstadoEnHistorialAndPeriodoAcademico("APROBADA", periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico) {
        // Para estudiantes, buscamos todas las solicitudes del usuario (sin filtro de estado)
        // pero filtramos por período académico
        return solicitudRepository.findAll().stream()
                .filter(entity -> entity.getObjUsuario() != null && 
                                 entity.getObjUsuario().getId_usuario().equals(idUsuario) &&
                                 (periodoAcademico == null || periodoAcademico.equals(entity.getPeriodo_academico())))
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    @Transactional
    public void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado) {
        // Usar findById normal (más seguro y confiable)
        SolicitudPazYSalvoEntity solicitudEntity = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new EntidadNoExisteException("Solicitud de Paz y Salvo no encontrada con ID: " + idSolicitud));

        // Asegurar que la lista de estados esté inicializada
        // Si es null, inicializarla; si no, forzar la carga lazy dentro de la transacción
        if (solicitudEntity.getEstadosSolicitud() == null) {
            solicitudEntity.setEstadosSolicitud(new java.util.ArrayList<>());
        } else {
            // Forzar la carga de la colección lazy dentro de la transacción
            solicitudEntity.getEstadosSolicitud().size();
        }

        // Crear el nuevo estado
        EstadoSolicitudEntity nuevo = new EstadoSolicitudEntity();
        nuevo.setEstado_actual(nuevoEstado.getEstado_actual());
        nuevo.setFecha_registro_estado(nuevoEstado.getFecha_registro_estado() != null ? nuevoEstado.getFecha_registro_estado() : new java.util.Date());
        nuevo.setObjSolicitud(solicitudEntity);
        
        // Establecer el comentario si se proporciona
        if (nuevoEstado.getComentario() != null && !nuevoEstado.getComentario().trim().isEmpty()) {
            nuevo.setComentario(nuevoEstado.getComentario().trim());
        }

        // Agregar el nuevo estado a la lista
        solicitudEntity.getEstadosSolicitud().add(nuevo);
        
        // Guardar la solicitud (esto también guardará el nuevo estado por cascade)
        solicitudRepository.save(solicitudEntity);
    }

    
}
