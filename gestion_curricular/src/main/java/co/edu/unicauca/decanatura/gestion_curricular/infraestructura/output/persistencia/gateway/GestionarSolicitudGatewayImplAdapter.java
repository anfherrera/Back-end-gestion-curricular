package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;


@Service
@Transactional
public class GestionarSolicitudGatewayImplAdapter implements GestionarSolicitudGatewayIntPort {

    private final SolicitudRepositoryInt solicitudRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final ModelMapper solicitudMapper;

    public GestionarSolicitudGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository,
                                                UsuarioRepositoryInt usuarioRepository,
                                                ModelMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.cursoOfertadoVeranoRepository = cursoOfertadoVeranoRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudMapper = solicitudMapper;
    }

    @Override
    public SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(
        SolicitudCursoVeranoPreinscripcion solicitudCursoVerano) {
        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoEntity = solicitudMapper.map(solicitudCursoVerano, SolicitudCursoVeranoPreinscripcionEntity.class);
        solicitudCursoVeranoEntity.setNombre_solicitud(SolicitudCursoVeranoPreinscripcion.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudCursoVeranoEntity);
        solicitudCursoVeranoEntity.setObjEstadoSolicitud(estadoSolicitudEntity);

        CursoOfertadoVerano cursoOfertado = solicitudCursoVerano.getObjCursoOfertado();
        CursoOfertadoVeranoEntity cursoOfertadoVeranoEntity = null;

        if(cursoOfertado != null && cursoOfertado.getId_curso() != null) {
            Integer idCurso = cursoOfertado.getId_curso();
            if(cursoOfertadoVeranoRepository.existsById(idCurso)){
                cursoOfertadoVeranoEntity = cursoOfertadoVeranoRepository.findById(idCurso)
                        .orElseThrow(() -> new IllegalArgumentException("Curso ofertado no encontrado con ID: " + idCurso));
            } else {
                cursoOfertadoVeranoEntity = new CursoOfertadoVeranoEntity();
                GrupoCursoVeranoEntity  grupoEntity = solicitudMapper.map(cursoOfertado.getGrupo(), GrupoCursoVeranoEntity.class);
                cursoOfertadoVeranoEntity.setGrupo(grupoEntity);
                cursoOfertadoVeranoEntity.setCupo_estimado(cursoOfertado.getCupo_estimado());
                cursoOfertadoVeranoEntity.setSalon(cursoOfertado.getSalon());
                MateriaEntity materiaEntity = solicitudMapper.map(cursoOfertado.getObjMateria(), MateriaEntity.class);
                DocenteEntity docenteEntity = solicitudMapper.map(cursoOfertado.getObjDocente(), DocenteEntity.class);
                cursoOfertadoVeranoEntity.setObjMateria(materiaEntity);
                cursoOfertadoVeranoEntity.setObjDocente(docenteEntity);
                
            }
            solicitudCursoVeranoEntity.setObjCursoOfertado(cursoOfertadoVeranoEntity);
        }else{
            throw new IllegalArgumentException("El curso ofertado no puede ser nulo o debe tener un ID válido.");
        }

        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoGuardado = solicitudRepository.save(solicitudCursoVeranoEntity);

        return solicitudMapper.map(solicitudCursoVeranoGuardado,SolicitudCursoVeranoPreinscripcion.class); // Implementación pendiente
    }


    @Override
    public SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(
            SolicitudCursoVeranoIncripcion solicitudCursoVerano) {
        SolicitudCursoVeranoInscripcionEntity solicitudCursoVeranoEntity = solicitudMapper.map(solicitudCursoVerano, SolicitudCursoVeranoInscripcionEntity.class);
        solicitudCursoVeranoEntity.setNombre_solicitud(SolicitudCursoVeranoIncripcion.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudCursoVeranoEntity);
        solicitudCursoVeranoEntity.setObjEstadoSolicitud(estadoSolicitudEntity);

        CursoOfertadoVerano cursoOfertado = solicitudCursoVerano.getObjCursoOfertado();
        CursoOfertadoVeranoEntity cursoOfertadoVeranoEntity = null;
        if(cursoOfertado != null && cursoOfertado.getId_curso() != null) {
            Integer idCurso = cursoOfertado.getId_curso();
            if(cursoOfertadoVeranoRepository.existsById(idCurso)){
                cursoOfertadoVeranoEntity = cursoOfertadoVeranoRepository.findById(idCurso)
                        .orElseThrow(() -> new IllegalArgumentException("Curso ofertado no encontrado con ID: " + idCurso));
            } else {
                cursoOfertadoVeranoEntity = new CursoOfertadoVeranoEntity();
                GrupoCursoVeranoEntity  grupoEntity = solicitudMapper.map(cursoOfertado.getGrupo(), GrupoCursoVeranoEntity.class);
                cursoOfertadoVeranoEntity.setGrupo(grupoEntity);
                cursoOfertadoVeranoEntity.setCupo_estimado(cursoOfertado.getCupo_estimado());
                cursoOfertadoVeranoEntity.setSalon(cursoOfertado.getSalon());
                MateriaEntity materiaEntity = solicitudMapper.map(cursoOfertado.getObjMateria(), MateriaEntity.class);
                DocenteEntity docenteEntity = solicitudMapper.map(cursoOfertado.getObjDocente(), DocenteEntity.class);
                cursoOfertadoVeranoEntity.setObjMateria(materiaEntity);
                cursoOfertadoVeranoEntity.setObjDocente(docenteEntity);
                
            }
            solicitudCursoVeranoEntity.setObjCursoOfertado(cursoOfertadoVeranoEntity);
        } else {
            throw new IllegalArgumentException("El curso ofertado no puede ser nulo o debe tener un ID válido.");
        }
        SolicitudCursoVeranoInscripcionEntity solicitudCursoVeranoGuardado = solicitudRepository.save(solicitudCursoVeranoEntity);

        return solicitudMapper.map(solicitudCursoVeranoGuardado, SolicitudCursoVeranoIncripcion.class); // Implementación pendiente
    }

    @Override
    @Transactional
    public SolicitudEcaes crearSolicitudEcaes(SolicitudEcaes solicitudEcaes) {
        SolicitudEcaesEntity solicitudEcaesEntity = solicitudMapper.map(solicitudEcaes, SolicitudEcaesEntity.class);
        solicitudEcaesEntity.setNombre_solicitud(SolicitudEcaes.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudEcaesEntity);
        solicitudEcaesEntity.setObjEstadoSolicitud(estadoSolicitudEntity);

        SolicitudEcaesEntity solicitudEcaesGuardado = solicitudRepository.save(solicitudEcaesEntity);

        return solicitudMapper.map(solicitudEcaesGuardado, SolicitudEcaes.class);
    }

    @Override
    @Transactional
    public SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitudReingreso) {
        SolicitudReingresoEntity solicitudReingresoEntity = solicitudMapper.map(solicitudReingreso, SolicitudReingresoEntity.class);
        solicitudReingresoEntity.setNombre_solicitud(SolicitudReingreso.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudReingresoEntity);
        solicitudReingresoEntity.setObjEstadoSolicitud(estadoSolicitudEntity);

        SolicitudReingresoEntity solicitudReingresoGuardado = solicitudRepository.save(solicitudReingresoEntity);

        return solicitudMapper.map(solicitudReingresoGuardado, SolicitudReingreso.class);        

    }

    @Override
    @Transactional
    public SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion) {
        SolicitudHomologacionEntity solicitudHomologacionEntity = solicitudMapper.map(solicitudHomologacion, SolicitudHomologacionEntity.class);
        solicitudHomologacionEntity.setNombre_solicitud(SolicitudHomologacion.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudHomologacionEntity);
        solicitudHomologacionEntity.setObjEstadoSolicitud(estadoSolicitudEntity);

        SolicitudHomologacionEntity solicitudHomologacionGuardado = solicitudRepository.save(solicitudHomologacionEntity);

        return solicitudMapper.map(solicitudHomologacionGuardado, SolicitudHomologacion.class); // Implementación pendiente
    }

    @Override
    @Transactional
    public SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo) {
        SolicitudPazYSalvoEntity solicitudPazYSalvoEntity = solicitudMapper.map(solicitudPazYSalvo, SolicitudPazYSalvoEntity.class);
        solicitudPazYSalvoEntity.setNombre_solicitud(SolicitudPazYSalvo.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudPazYSalvoEntity);
        solicitudPazYSalvoEntity.setObjEstadoSolicitud(estadoSolicitudEntity);

        SolicitudPazYSalvoEntity solicitudPazYSalvoGuardado = solicitudRepository.save(solicitudPazYSalvoEntity);
        
        return solicitudMapper.map(solicitudPazYSalvoGuardado, SolicitudPazYSalvo.class); // Implementación pendiente

    }

    @Override
    @Transactional(readOnly = true)
    public Solicitud obtenerSolicitudPorId(Integer idSolicitud) {
        Optional<SolicitudEntity> solicitudEntityOptional = solicitudRepository.findById(idSolicitud);
        if(solicitudEntityOptional != null){
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
        if (solicitudEntity != null) {
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
        List<SolicitudEntity> solicitudEntities = solicitudRepository.findAll();
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
    public Usuario asociarUsuarioASolicitud(Integer idUsuario, Integer idSolicitud) {
        Set<SolicitudEntity> solicitudesEntitySet = null;
        UsuarioEntity usuarioEntityGuardado = null;
        Optional<UsuarioEntity> usuarioEntityOptional = usuarioRepository.findById(idUsuario);
        Optional<SolicitudEntity> solicitudEntityOptional = solicitudRepository.findById(idSolicitud);

        if(usuarioEntityOptional != null && solicitudEntityOptional != null) {
            UsuarioEntity usuarioEntity = usuarioEntityOptional.get();
            SolicitudEntity solicitudEntity = solicitudEntityOptional.get();

            solicitudesEntitySet = usuarioEntity.getSolicitudes();
            solicitudesEntitySet.add(solicitudEntity);
            usuarioEntity.setSolicitudes(solicitudesEntitySet);
            
            usuarioEntityGuardado = usuarioRepository.save(usuarioEntity);
        }
        return solicitudMapper.map(usuarioEntityGuardado, Usuario.class);
    }


    @Override
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

        EstadoSolicitudEntity estadoSolicitudEntity = null;
        if (estadoSolicitud != null) {
            estadoSolicitudEntity = solicitudMapper.map(estadoSolicitud, EstadoSolicitudEntity.class);
        } else {
            estadoSolicitudEntity = new EstadoSolicitudEntity();
        }
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudEntity);

        solicitudEntity.setObjEstadoSolicitud(estadoSolicitudEntity);

        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);

        return solicitudMapper.map(solicitudActualizada, Solicitud.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudPorNombreYCurso(String nombreSolicitud, Integer idCurso) {
        return solicitudRepository.contarPorNombreYCurso(nombreSolicitud, idCurso);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudPorNombreCursoEstado(String nombreSolicitud, Integer idCurso, String estadoActual) {
        return solicitudRepository.contarPorNombreCursoEstado(nombreSolicitud, idCurso, estadoActual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarSolicitudPorNombreCursoEstado(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreCursoEstado(nombreSolicitud, idCurso, estadoActual);
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
    public List<Solicitud> buscarSolicitudPorNombreCurso(String nombreSolicitud, Integer idCurso) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreYCurso(nombreSolicitud, idCurso);
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
    
}
