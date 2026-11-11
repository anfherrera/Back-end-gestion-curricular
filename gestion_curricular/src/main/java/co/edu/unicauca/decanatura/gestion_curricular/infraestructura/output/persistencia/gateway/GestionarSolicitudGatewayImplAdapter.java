package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;


@Service
@Transactional
public class GestionarSolicitudGatewayImplAdapter implements GestionarSolicitudGatewayIntPort {

    private final SolicitudRepositoryInt solicitudRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final EstadoSolicitudRepositoryInt estadoSolicitudRepository;
    private final ModelMapper solicitudMapper;

    public GestionarSolicitudGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                UsuarioRepositoryInt usuarioRepository,
                                                EstadoSolicitudRepositoryInt estadoSolicitudRepository,
                                                ModelMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
        this.solicitudMapper = solicitudMapper;
    }

    @Override
    @Transactional
    public SolicitudEcaes crearSolicitudEcaes(SolicitudEcaes solicitudEcaes) {
        SolicitudEcaesEntity solicitudEcaesEntity = solicitudMapper.map(solicitudEcaes, SolicitudEcaesEntity.class);
        
        // Generar nombre descriptivo con el nombre del estudiante
        String nombreSolicitud = "Solicitud ECAES";
        if (solicitudEcaes.getObjUsuario() != null && solicitudEcaes.getObjUsuario().getId_usuario() != null) {
            UsuarioEntity usuarioEntity = usuarioRepository.findById(solicitudEcaes.getObjUsuario().getId_usuario()).orElse(null);
            if (usuarioEntity != null && usuarioEntity.getNombre_completo() != null && !usuarioEntity.getNombre_completo().trim().isEmpty()) {
                nombreSolicitud = "Solicitud ECAES - " + usuarioEntity.getNombre_completo();
            }
        }
        solicitudEcaesEntity.setNombre_solicitud(nombreSolicitud);
        solicitudEcaes.setFecha_registro_solicitud(new Date());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudEcaesEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudEcaesEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudEcaesEntity.setEstadosSolicitud(estadosSolcitud);

        SolicitudEcaesEntity solicitudEcaesGuardado = solicitudRepository.save(solicitudEcaesEntity);

        return solicitudMapper.map(solicitudEcaesGuardado, SolicitudEcaes.class);
    }

    @Override
    @Transactional
    public SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitudReingreso) {
        SolicitudReingresoEntity solicitudReingresoEntity = solicitudMapper.map(solicitudReingreso, SolicitudReingresoEntity.class);
        
        // Generar nombre descriptivo con el nombre del estudiante
        String nombreSolicitud = "Solicitud de Reingreso";
        if (solicitudReingreso.getObjUsuario() != null && solicitudReingreso.getObjUsuario().getId_usuario() != null) {
            UsuarioEntity usuarioEntity = usuarioRepository.findById(solicitudReingreso.getObjUsuario().getId_usuario()).orElse(null);
            if (usuarioEntity != null && usuarioEntity.getNombre_completo() != null && !usuarioEntity.getNombre_completo().trim().isEmpty()) {
                nombreSolicitud = "Solicitud de Reingreso - " + usuarioEntity.getNombre_completo();
            }
        }
        solicitudReingresoEntity.setNombre_solicitud(nombreSolicitud);
        solicitudReingresoEntity.setFecha_registro_solicitud(new Date());
        
        // NO tocar los estados aquí, el caso de uso (GestionarSolicitudReingresoCUAdapter) los maneja completamente
        // ModelMapper ya maneja la lista de estados si viene del dominio
        
        SolicitudReingresoEntity solicitudReingresoGuardado = solicitudRepository.save(solicitudReingresoEntity);

        return solicitudMapper.map(solicitudReingresoGuardado, SolicitudReingreso.class);        

    }

    @Override
    @Transactional
    public SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion) {
        SolicitudHomologacionEntity solicitudHomologacionEntity = solicitudMapper.map(solicitudHomologacion, SolicitudHomologacionEntity.class);
        
        // Generar nombre descriptivo con el nombre del estudiante
        String nombreSolicitud = "Solicitud de Homologación";
        if (solicitudHomologacion.getObjUsuario() != null && solicitudHomologacion.getObjUsuario().getId_usuario() != null) {
            UsuarioEntity usuarioEntity = usuarioRepository.findById(solicitudHomologacion.getObjUsuario().getId_usuario()).orElse(null);
            if (usuarioEntity != null && usuarioEntity.getNombre_completo() != null && !usuarioEntity.getNombre_completo().trim().isEmpty()) {
                nombreSolicitud = "Solicitud de Homologación - " + usuarioEntity.getNombre_completo();
            }
        }
        solicitudHomologacionEntity.setNombre_solicitud(nombreSolicitud);
        solicitudHomologacionEntity.setFecha_registro_solicitud(new Date());
        
        // NO tocar los estados aquí, el caso de uso (GestionarSolicitudHomologacionCUAdapter) los maneja completamente
        // ModelMapper ya maneja la lista de estados si viene del dominio

        SolicitudHomologacionEntity solicitudHomologacionGuardado = solicitudRepository.save(solicitudHomologacionEntity);

        return solicitudMapper.map(solicitudHomologacionGuardado, SolicitudHomologacion.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Solicitud obtenerSolicitudPorId(Integer idSolicitud) {
        Optional<SolicitudEntity> solicitudEntityOptional = solicitudRepository.findById(idSolicitud);
        if (solicitudEntityOptional.isPresent()) {
            SolicitudEntity solicitudEntity = solicitudEntityOptional.get();
            if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
            } 
            if(solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
            }
            if (solicitudEntity instanceof SolicitudEcaesEntity) {
                return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
            } 
            if (solicitudEntity instanceof SolicitudReingresoEntity) {
                return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
            } 
            if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
            } 
            if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
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
    public boolean existeSolicitudPorNombre(String nombreSolicitud) {
        return solicitudRepository.contarPorNombre(nombreSolicitud) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudes() {
        return solicitudRepository.totalSolicitudes();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudesPorFecha(Date fechaInicio, Date fechaFin) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorRangoFechas(fechaInicio, fechaFin);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities!=null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                    if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                    } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                    } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                    } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                    } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                    } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                    }
                    return null;
                })
                .toList();
        }
        return solicitudes;

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
            return solicitudMapper.map(entidad, SolicitudCursoVeranoPreinscripcion.class);
        } else if (entidad instanceof SolicitudEcaesEntity) {
            return solicitudMapper.map(entidad, SolicitudEcaes.class);
        } else if (entidad instanceof SolicitudReingresoEntity) {
            return solicitudMapper.map(entidad, SolicitudReingreso.class);
        } else if (entidad instanceof SolicitudHomologacionEntity) {
            return solicitudMapper.map(entidad, SolicitudHomologacion.class);
        } else if (entidad instanceof SolicitudPazYSalvoEntity) {
            return solicitudMapper.map(entidad, SolicitudPazYSalvo.class);
        } else if (entidad instanceof SolicitudCursoVeranoInscripcionEntity) {
            return solicitudMapper.map(entidad, SolicitudCursoVeranoIncripcion.class);
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
                        return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                    } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                    } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                    } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                    } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                    } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                    }
                    return null;
                })
                .toList();
        }
        return solicitudes;
        
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> obtenerSolicitudesPorUsuario(Integer idUsuario) {
        Optional<UsuarioEntity> usuarioEntityCollection = usuarioRepository.findById(idUsuario);
        UsuarioEntity usuarioEntity = null;
        List<SolicitudEntity> solicitudesEntity = null;
        List<Solicitud> solicitudes = null;
        if (usuarioEntityCollection!= null) {
            usuarioEntity = usuarioEntityCollection.get();
            solicitudesEntity = solicitudRepository.buscarSolicitudesPorUsuario(usuarioEntity.getId_usuario());
            if(solicitudesEntity != null){           
                solicitudes = solicitudesEntity.stream().map(solicitudEntity -> {
                    if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                    } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                    } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                    } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                    } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                    } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                        return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                    }
                    return null;
                }).toList();

            } else {
                solicitudesEntity = List.of();
            }
            
        }else{
            throw new IllegalArgumentException("El usuario no existe");
        }

        return solicitudes;
    }

    @Override
    @Transactional
    public Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud) {
        SolicitudEntity solicitudEntity = null;
        if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
            solicitudEntity = solicitudMapper.map(solicitud, SolicitudCursoVeranoPreinscripcionEntity.class);
        } else if (solicitud instanceof SolicitudEcaes) {
            solicitudEntity = solicitudMapper.map(solicitud, SolicitudEcaesEntity.class);
        } else if (solicitud instanceof SolicitudReingreso) {
            solicitudEntity = solicitudMapper.map(solicitud, SolicitudReingresoEntity.class);
        } else if (solicitud instanceof SolicitudHomologacion) {
            solicitudEntity = solicitudMapper.map(solicitud, SolicitudHomologacionEntity.class);
        } else if (solicitud instanceof SolicitudPazYSalvo) {
            solicitudEntity = solicitudMapper.map(solicitud, SolicitudPazYSalvoEntity.class);
        } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
            solicitudEntity = solicitudMapper.map(solicitud, SolicitudCursoVeranoInscripcionEntity.class);
        }

        List<EstadoSolicitudEntity> estadosSolicitud = null;
        EstadoSolicitudEntity estadoSolicitudEntity = null;

        if (estadoSolicitud != null) {
            estadoSolicitudEntity = solicitudMapper.map(estadoSolicitud, EstadoSolicitudEntity.class);

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

        return solicitudMapper.map(solicitudActualizada, Solicitud.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudPorNombreYCursoIns(String nombreSolicitud, Integer idCurso) {
        return solicitudRepository.contarPorNombreYCursoIns(nombreSolicitud, idCurso);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso, String estadoActual) {
        return solicitudRepository.contarPorNombreCursoEstadoIns(nombreSolicitud, idCurso, estadoActual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreCursoEstadoIns(nombreSolicitud, idCurso, estadoActual);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities != null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                }
                return null;
            }).toList();
        }
        return solicitudes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudPorNombreCursoIns(Integer idCurso) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreYCursoIns(idCurso);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities != null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                }
                return null;
            }).toList();
        }
        return solicitudes;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudPorNombreYCursoPre(String nombreSolicitud, Integer idCurso) {
        return solicitudRepository.contarPorNombreYCursoPre(nombreSolicitud, idCurso);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        return solicitudRepository.contarPorNombreCursoEstadoPre(nombreSolicitud, idCurso, estadoActual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudPorNombreCursoPre(Integer idCurso) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreYCursoPre(idCurso);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities != null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                }
                return null;
            }).toList();
        }
        return solicitudes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreCursoEstadoPre(nombreSolicitud, idCurso, estadoActual);
        List<Solicitud> solicitudes = null;
        if(solicitudEntities != null){
            solicitudes = solicitudEntities.stream().map(solicitudEntity -> {
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                }
                return null;
            }).toList();
        }
        return solicitudes;
    }

    @Override
    public Solicitud buscarSolicitudesPorUsuarioEstadoCursoPre(Integer idUsuario,String estado_actual, Integer idCurso) {
        SolicitudEntity solicitudEntity = solicitudRepository.buscarSolicitudesPorUsuarioCursoEstadoPre(idUsuario, idCurso, estado_actual);
        Solicitud solicitud = null;
        if(solicitudEntity!= null){
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                }
        }
        return solicitud;
    }


    @Override
    public Solicitud buscarSolicitudesPorUsuarioYCursoIns(Integer idUsuario, Integer idCurso) {
        SolicitudEntity solicitudEntity = solicitudRepository.buscarSolicitudesPorUsuarioyCursoIns(idUsuario, idCurso);
        Solicitud solicitud = null;
        if(solicitudEntity!= null){
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                }
        }
        return solicitud;
    }

    @Override
    public Solicitud buscarSolicitudesPorUsuarioYCursoPre(Integer idUsuario, Integer idCurso) {
SolicitudEntity solicitudEntity = solicitudRepository.buscarSolicitudesPorUsuarioyCursoPre(idUsuario, idCurso);
        Solicitud solicitud = null;
        if(solicitudEntity!= null){
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    solicitud = solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
                }
        }
        return solicitud;
    }
}
