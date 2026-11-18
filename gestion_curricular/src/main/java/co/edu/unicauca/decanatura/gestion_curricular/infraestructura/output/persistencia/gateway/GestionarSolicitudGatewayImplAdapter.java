package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.SolicitudMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;


@Service
@Transactional
public class GestionarSolicitudGatewayImplAdapter implements GestionarSolicitudGatewayIntPort {
    @Override
    @Transactional(readOnly = true)
    public Integer idDuenoDeSolicitud(Integer idSolicitud) {
        return solicitudRepository.findById(idSolicitud)
                .map(SolicitudEntity::getObjUsuario)
                .map(usuario -> usuario.getId_usuario()).orElse(null);
    }

    private final SolicitudRepositoryInt solicitudRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final EstadoSolicitudRepositoryInt estadoSolicitudRepository;

    public GestionarSolicitudGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                UsuarioRepositoryInt usuarioRepository,
                                                EstadoSolicitudRepositoryInt estadoSolicitudRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Solicitud obtenerSolicitudPorId(Integer idSolicitud) {
        Optional<SolicitudEntity> solicitudEntityOptional = solicitudRepository.findById(idSolicitud);
        if (solicitudEntityOptional.isPresent()) {
            SolicitudEntity solicitudEntity = solicitudEntityOptional.get();
            if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
            } 
            if(solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoInscripcion.class);
            }
            if (solicitudEntity instanceof SolicitudEcaesEntity) {
                return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudEcaes.class);
            } 
            if (solicitudEntity instanceof SolicitudReingresoEntity) {
                return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudReingreso.class);
            } 
            if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudHomologacion.class);
            } 
            if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudPazYSalvo.class);
            } 
        }
        return null;
        
    }

    @Override
    @Transactional
    public boolean eliminarSolicitud(Integer idSolicitud) {
        boolean existe = false;
        Optional<SolicitudEntity> solicitudEntity = solicitudRepository.findById(idSolicitud);
        if (solicitudEntity.isPresent()) {
            solicitudRepository.deleteById(idSolicitud);
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> listarSolicitudes() {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.findAllWithJoins();
        if (solicitudEntities == null) {
            return null;
        }
        return solicitudEntities.stream().map(this::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarHistorial(String periodoAcademico, Integer idUsuario, String estadoActual) {
        List<SolicitudEntity> entidades = solicitudRepository.buscarHistorialConFiltros(periodoAcademico, idUsuario, estadoActual);
        if (entidades == null) {
            return List.of();
        }
        return entidades.stream().map(this::aDominio).filter(java.util.Objects::nonNull).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarHistorial() {
        Long total = solicitudRepository.contarHistorialConFiltros(null, null, null);
        return total != null ? total : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarSolicitudesProcesadas() {
        Long total = solicitudRepository.contarSolicitudesProcesadas(null, null);
        return total != null ? total : 0L;
    }

    /** Convierte la entidad al modelo de dominio de su tipo concreto; null si el tipo no se reconoce. */
    private Solicitud aDominio(SolicitudEntity entidad) {
        if (entidad instanceof SolicitudCursoVeranoPreinscripcionEntity) {
            return SolicitudMapperPersistencia.aDominio(entidad, SolicitudCursoVeranoPreinscripcion.class);
        } else if (entidad instanceof SolicitudEcaesEntity) {
            return SolicitudMapperPersistencia.aDominio(entidad, SolicitudEcaes.class);
        } else if (entidad instanceof SolicitudReingresoEntity) {
            return SolicitudMapperPersistencia.aDominio(entidad, SolicitudReingreso.class);
        } else if (entidad instanceof SolicitudHomologacionEntity) {
            return SolicitudMapperPersistencia.aDominio(entidad, SolicitudHomologacion.class);
        } else if (entidad instanceof SolicitudPazYSalvoEntity) {
            return SolicitudMapperPersistencia.aDominio(entidad, SolicitudPazYSalvo.class);
        } else if (entidad instanceof SolicitudCursoVeranoInscripcionEntity) {
            return SolicitudMapperPersistencia.aDominio(entidad, SolicitudCursoVeranoInscripcion.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> obtenerSolicitudesPorNombre(String nombreSolicitud) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombre(nombreSolicitud);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities!=null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                    if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                    } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                        return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudEcaes.class);
                    } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                        return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudReingreso.class);
                    } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                        return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudHomologacion.class);
                    } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                        return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudPazYSalvo.class);
                    } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                        return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoInscripcion.class);
                    }
                    return null;
                })
                .toList();
        }
        return solicitudes;
        
    }

    @Override
    @Transactional
    public Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud) {
        SolicitudEntity solicitudEntity = null;
        if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
            solicitudEntity = SolicitudMapperPersistencia.aEntidad(solicitud, SolicitudCursoVeranoPreinscripcionEntity.class);
        } else if (solicitud instanceof SolicitudEcaes) {
            solicitudEntity = SolicitudMapperPersistencia.aEntidad(solicitud, SolicitudEcaesEntity.class);
        } else if (solicitud instanceof SolicitudReingreso) {
            solicitudEntity = SolicitudMapperPersistencia.aEntidad(solicitud, SolicitudReingresoEntity.class);
        } else if (solicitud instanceof SolicitudHomologacion) {
            solicitudEntity = SolicitudMapperPersistencia.aEntidad(solicitud, SolicitudHomologacionEntity.class);
        } else if (solicitud instanceof SolicitudPazYSalvo) {
            solicitudEntity = SolicitudMapperPersistencia.aEntidad(solicitud, SolicitudPazYSalvoEntity.class);
        } else if (solicitud instanceof SolicitudCursoVeranoInscripcion) {
            solicitudEntity = SolicitudMapperPersistencia.aEntidad(solicitud, SolicitudCursoVeranoInscripcionEntity.class);
        }

        List<EstadoSolicitudEntity> estadosSolicitud = null;
        EstadoSolicitudEntity estadoSolicitudEntity = null;

        if (estadoSolicitud != null) {
            estadoSolicitudEntity = SolicitudMapperPersistencia.aEntidad(estadoSolicitud);

        } else {
            estadoSolicitudEntity = new EstadoSolicitudEntity();
        }

        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudEntity);

        EstadoSolicitudEntity estadoNuevo = estadoSolicitudRepository.save(estadoSolicitudEntity);


        estadosSolicitud = solicitudEntity.getEstadosSolicitud();

        estadosSolicitud.add(estadoNuevo);

        solicitudEntity.setEstadosSolicitud(estadosSolicitud);

        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);

        return SolicitudMapperPersistencia.aDominio(solicitudActualizada, Solicitud.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudPorNombreCursoIns(Integer idCurso) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreYCursoIns(idCurso);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities != null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoInscripcion.class);
                }
                return null;
            }).toList();
        }
        return solicitudes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudPorNombreCursoPre(Integer idCurso) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreYCursoPre(idCurso);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities != null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    return SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoInscripcion.class);
                }
                return null;
            }).toList();
        }
        return solicitudes;
    }


}
