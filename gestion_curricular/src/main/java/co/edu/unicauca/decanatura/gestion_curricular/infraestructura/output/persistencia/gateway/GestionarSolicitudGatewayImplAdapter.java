package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;


@Service
@Transactional
public class GestionarSolicitudGatewayImplAdapter implements GestionarSolicitudGatewayIntPort {

    private final SolicitudRepositoryInt solicitudRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final EstadoSolicitudRepositoryInt estadoSolicitudRepository;
    private final ModelMapper solicitudMapper;

    public GestionarSolicitudGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository,
                                                UsuarioRepositoryInt usuarioRepository,
                                                EstadoSolicitudRepositoryInt estadoSolicitudRepository,
                                                ModelMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.cursoOfertadoVeranoRepository = cursoOfertadoVeranoRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
        this.solicitudMapper = solicitudMapper;
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(
        SolicitudCursoVeranoPreinscripcion solicitudCursoVerano) {
        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoEntity = solicitudMapper.map(solicitudCursoVerano, SolicitudCursoVeranoPreinscripcionEntity.class);
        solicitudCursoVeranoEntity.setNombre_solicitud(SolicitudCursoVeranoPreinscripcion.class.getSimpleName());
        
        EstadoSolicitudEntity estadoSolicitudEntity = null;

        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudCursoVeranoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudCursoVeranoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudCursoVeranoEntity.setEstadosSolicitud(estadosSolcitud);

        CursoOfertadoVerano cursoOfertado = solicitudCursoVerano.getObjCursoOfertadoVerano();
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
            solicitudCursoVeranoEntity.setObjCursoOfertadoVerano(cursoOfertadoVeranoEntity);
        }else{
            throw new IllegalArgumentException("El curso ofertado no puede ser nulo o debe tener un ID válido.");
        }

        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoGuardado = solicitudRepository.save(solicitudCursoVeranoEntity);

        return solicitudMapper.map(solicitudCursoVeranoGuardado,SolicitudCursoVeranoPreinscripcion.class); // Implementación pendiente
    }


    @Override
    @Transactional
    public SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(
            SolicitudCursoVeranoIncripcion solicitudCursoVerano) {
        SolicitudCursoVeranoInscripcionEntity solicitudCursoVeranoEntity = solicitudMapper.map(solicitudCursoVerano, SolicitudCursoVeranoInscripcionEntity.class);
        solicitudCursoVeranoEntity.setNombre_solicitud(SolicitudCursoVeranoIncripcion.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudCursoVeranoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudCursoVeranoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudCursoVeranoEntity.setEstadosSolicitud(estadosSolcitud);
        CursoOfertadoVerano cursoOfertado = solicitudCursoVerano.getObjCursoOfertadoVerano();
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
            solicitudCursoVeranoEntity.setObjCursoOfertadoVerano(cursoOfertadoVeranoEntity);
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
        solicitudReingresoEntity.setNombre_solicitud(SolicitudReingreso.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudReingresoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudReingresoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudReingresoEntity.setEstadosSolicitud(estadosSolcitud);
        SolicitudReingresoEntity solicitudReingresoGuardado = solicitudRepository.save(solicitudReingresoEntity);

        return solicitudMapper.map(solicitudReingresoGuardado, SolicitudReingreso.class);        

    }

    @Override
    @Transactional
    public SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion) {
        SolicitudHomologacionEntity solicitudHomologacionEntity = solicitudMapper.map(solicitudHomologacion, SolicitudHomologacionEntity.class);
        solicitudHomologacionEntity.setNombre_solicitud(SolicitudHomologacion.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudHomologacionEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudHomologacionEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudHomologacionEntity.setEstadosSolicitud(estadosSolcitud);

        SolicitudHomologacionEntity solicitudHomologacionGuardado = solicitudRepository.save(solicitudHomologacionEntity);

        return solicitudMapper.map(solicitudHomologacionGuardado, SolicitudHomologacion.class); // Implementación pendiente
    }

    @Override
    @Transactional
    public SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo) {
        SolicitudPazYSalvoEntity solicitudPazYSalvoEntity = solicitudMapper.map(solicitudPazYSalvo, SolicitudPazYSalvoEntity.class);
        solicitudPazYSalvoEntity.setNombre_solicitud(SolicitudPazYSalvo.class.getSimpleName());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudPazYSalvoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudPazYSalvoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudPazYSalvoEntity.setEstadosSolicitud(estadosSolcitud);
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
    public List<Solicitud> buscarSolicitudPorNombreCursoIns(String nombreSolicitud, Integer idCurso) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreYCursoIns(nombreSolicitud, idCurso);
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
    public List<Solicitud> buscarSolicitudPorNombreCursoPre(String nombreSolicitud, Integer idCurso) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreYCursoPre(nombreSolicitud, idCurso);
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
    public List<Solicitud> buscarPorNombreCursoYSeleccionadoIns(Integer idCurso, boolean seleccionado) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreCursoYSeleccionadoIns(idCurso, seleccionado);
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
    public List<Solicitud> buscarPorNombreCursoYSeleccionadoPre(Integer idCurso, boolean seleccionado) {
        List<SolicitudEntity> solicitudEntities = solicitudRepository.buscarPorNombreCursoYSeleccionadoPre(idCurso, seleccionado);
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
