package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;


@Service
@Transactional
public class GestionarSolicitudCursoVeranoGatewayImplAdapter implements GestionarSolicitudCursoVeranoGatewayIntPort {

    private final SolicitudRepositoryInt solicitudRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository;
    private final EstadoSolicitudRepositoryInt estadoSolicitudRepository;
    private final ModelMapper solicitudMapper;

    public GestionarSolicitudCursoVeranoGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                           CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository,
                                                           EstadoSolicitudRepositoryInt estadoSolicitudRepository,
                                                           ModelMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.cursoOfertadoVeranoRepository = cursoOfertadoVeranoRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
        this.solicitudMapper = solicitudMapper;
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(
        SolicitudCursoVeranoPreinscripcion solicitudCursoVerano) {
        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoEntity = solicitudMapper.map(solicitudCursoVerano, SolicitudCursoVeranoPreinscripcionEntity.class);
        solicitudCursoVeranoEntity.setNombre_solicitud(SolicitudCursoVeranoPreinscripcion.class.getSimpleName());
        solicitudCursoVeranoEntity.setFecha_registro_solicitud(new Date());
        EstadoSolicitudEntity estadoSolicitudEntity = null;

        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setEstado_actual("Enviada");
        estadoSolicitudEntity.setObjSolicitud(solicitudCursoVeranoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudCursoVeranoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudCursoVeranoEntity.setEstadosSolicitud(estadosSolcitud);

        CursoOfertadoVerano cursoOfertado = solicitudCursoVerano.getObjCursoOfertadoVerano();
        CursoOfertadoVeranoEntity cursoOfertadoVeranoEntity = null;

        if(cursoOfertado != null) {
            Integer idCurso = cursoOfertado.getId_curso();
            
            // Si el ID es 0 o null, es un curso nuevo (solicitud de apertura)
            if(idCurso == null || idCurso == 0) {
                // Para cursos nuevos, no necesitamos crear la entidad del curso
                // Solo guardamos la solicitud con la información del curso solicitado
                cursoOfertadoVeranoEntity = null;
            } else if(cursoOfertadoVeranoRepository.existsById(idCurso)){
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
            throw new IllegalArgumentException("El curso ofertado no puede ser nulo.");
        }

        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoGuardado = solicitudRepository.save(solicitudCursoVeranoEntity);

        return solicitudMapper.map(solicitudCursoVeranoGuardado,SolicitudCursoVeranoPreinscripcion.class);
    }


    @Override
    @Transactional
    public SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(
            SolicitudCursoVeranoIncripcion solicitudCursoVerano) {
        SolicitudCursoVeranoInscripcionEntity solicitudCursoVeranoEntity = solicitudMapper.map(solicitudCursoVerano, SolicitudCursoVeranoInscripcionEntity.class);
        solicitudCursoVeranoEntity.setNombre_solicitud(SolicitudCursoVeranoIncripcion.class.getSimpleName());
        solicitudCursoVeranoEntity.setFecha_registro_solicitud(new Date());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setEstado_actual("Enviada");
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

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoPreinscripcion> buscarSolicitudesPorUsuario(Integer idUsuario) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarSolicitudesPorUsuario(idUsuario);
        List<SolicitudCursoVeranoPreinscripcion> solicitudes = new ArrayList<>();
        
        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                solicitudes.add(solicitudMapper.map(entity, SolicitudCursoVeranoPreinscripcion.class));
            }
        }
        
        return solicitudes;
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCursoVeranoPreinscripcion buscarSolicitudPorId(Integer idSolicitud) {
        return solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoPreinscripcionEntity)
            .map(entity -> solicitudMapper.map(entity, SolicitudCursoVeranoPreinscripcion.class))
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoPreinscripcion> buscarPreinscripcionesPorCurso(Integer idCurso) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarSolicitudesPorCurso(idCurso);
        List<SolicitudCursoVeranoPreinscripcion> preinscripciones = new ArrayList<>();
        
        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                preinscripciones.add(solicitudMapper.map(entity, SolicitudCursoVeranoPreinscripcion.class));
            }
        }
        
        return preinscripciones;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorCurso(Integer idCurso) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarInscripcionesPorCurso(idCurso);
        List<SolicitudCursoVeranoIncripcion> inscripciones = new ArrayList<>();
        
        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoInscripcionEntity) {
                inscripciones.add(solicitudMapper.map(entity, SolicitudCursoVeranoIncripcion.class));
            }
        }
        
        return inscripciones;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarSolicitudesPorCurso(Integer idCurso) {
        return solicitudRepository.contarSolicitudesPorCurso(idCurso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoPreinscripcion> buscarCursosConAltaDemanda(Integer limiteMinimo) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarCursosConAltaDemanda(limiteMinimo);
        List<SolicitudCursoVeranoPreinscripcion> cursosAltaDemanda = new ArrayList<>();
        
        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                cursosAltaDemanda.add(solicitudMapper.map(entity, SolicitudCursoVeranoPreinscripcion.class));
            }
        }
        
        return cursosAltaDemanda;
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCursoVeranoIncripcion buscarSolicitudInscripcionPorId(Integer idSolicitud) {
        return solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoInscripcionEntity)
            .map(entity -> solicitudMapper.map(entity, SolicitudCursoVeranoIncripcion.class))
            .orElse(null);
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion aprobarPreinscripcion(Integer idSolicitud, String comentarios) {
        System.out.println("DEBUG: Intentando aprobar preinscripción con ID: " + idSolicitud);
        
        // Buscar la solicitud sin filtrar primero
        Optional<SolicitudEntity> solicitudOpt = solicitudRepository.findById(idSolicitud);
        if (!solicitudOpt.isPresent()) {
            System.out.println("DEBUG: No se encontró ninguna solicitud con ID: " + idSolicitud);
            return null;
        }
        
        SolicitudEntity solicitudEntity = solicitudOpt.get();
        System.out.println("DEBUG: Solicitud encontrada - Tipo: " + solicitudEntity.getClass().getSimpleName());
        
        // Verificar si es del tipo correcto
        if (!(solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity)) {
            System.out.println("DEBUG: La solicitud no es del tipo SolicitudCursoVeranoPreinscripcionEntity");
            return null;
        }
        
        System.out.println("DEBUG: Solicitud es del tipo correcto, procediendo con la aprobación...");
        
        // Crear nuevo estado "Aprobado"
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual("Aprobado");
        estadoEntity.setObjSolicitud(solicitudEntity);
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        System.out.println("DEBUG: Solicitud actualizada guardada con ID: " + solicitudActualizada.getId_solicitud());
        
        SolicitudCursoVeranoPreinscripcion resultado = solicitudMapper.map(solicitudActualizada, SolicitudCursoVeranoPreinscripcion.class);
        System.out.println("DEBUG: Mapeo completado, resultado: " + (resultado != null ? "OK" : "NULL"));
        
        return resultado;
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion rechazarPreinscripcion(Integer idSolicitud, String motivo) {
        System.out.println("DEBUG: Intentando rechazar preinscripción con ID: " + idSolicitud);
        
        // Buscar la solicitud sin filtrar primero
        Optional<SolicitudEntity> solicitudOpt = solicitudRepository.findById(idSolicitud);
        if (!solicitudOpt.isPresent()) {
            System.out.println("DEBUG: No se encontró ninguna solicitud con ID: " + idSolicitud);
            return null;
        }
        
        SolicitudEntity solicitudEntity = solicitudOpt.get();
        System.out.println("DEBUG: Solicitud encontrada - Tipo: " + solicitudEntity.getClass().getSimpleName());
        
        // Verificar si es del tipo correcto
        if (!(solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity)) {
            System.out.println("DEBUG: La solicitud no es del tipo SolicitudCursoVeranoPreinscripcionEntity");
            return null;
        }
        
        System.out.println("DEBUG: Solicitud es del tipo correcto, procediendo con el rechazo...");
        
        // Crear nuevo estado "Rechazado"
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual("Rechazado");
        estadoEntity.setObjSolicitud(solicitudEntity);
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        System.out.println("DEBUG: Solicitud actualizada guardada con ID: " + solicitudActualizada.getId_solicitud());
        
        SolicitudCursoVeranoPreinscripcion resultado = solicitudMapper.map(solicitudActualizada, SolicitudCursoVeranoPreinscripcion.class);
        System.out.println("DEBUG: Mapeo completado, resultado: " + (resultado != null ? "OK" : "NULL"));
        
        return resultado;
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoIncripcion validarPago(Integer idSolicitud, boolean esValido, String observaciones) {
        SolicitudEntity solicitudEntity = solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoInscripcionEntity)
            .orElse(null);
        
        if (solicitudEntity == null) {
            return null;
        }
        
        // Crear nuevo estado según validación
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual(esValido ? "Pago_Validado" : "Pago_Rechazado");
        estadoEntity.setObjSolicitud(solicitudEntity);
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        return solicitudMapper.map(solicitudActualizada, SolicitudCursoVeranoIncripcion.class);
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoIncripcion completarInscripcion(Integer idSolicitud) {
        SolicitudEntity solicitudEntity = solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoInscripcionEntity)
            .orElse(null);
        
        if (solicitudEntity == null) {
            return null;
        }
        
        // Crear nuevo estado "Inscripcion_Completada"
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual("Inscripcion_Completada");
        estadoEntity.setObjSolicitud(solicitudEntity);
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        return solicitudMapper.map(solicitudActualizada, SolicitudCursoVeranoIncripcion.class);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCursoVeranoIncripcion buscarSolicitudInscripcionPorUsuarioYCurso(Integer idUsuario, Integer idCurso) {
        SolicitudEntity solicitudEntity = solicitudRepository.buscarSolicitudesPorUsuarioyCursoIns(idUsuario, idCurso);
        if (solicitudEntity != null && solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
            return solicitudMapper.map(solicitudEntity, SolicitudCursoVeranoIncripcion.class);
        }
        return null;
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion actualizarSolicitudCursoVerano(SolicitudCursoVeranoPreinscripcion solicitud) {
        if (solicitud == null || solicitud.getId_solicitud() == null) {
            return null;
        }
        
        // Buscar la entidad existente
        SolicitudEntity solicitudEntity = solicitudRepository.findById(solicitud.getId_solicitud())
            .filter(entity -> entity instanceof SolicitudCursoVeranoPreinscripcionEntity)
            .orElse(null);
        
        if (solicitudEntity == null) {
            return null;
        }
        
        // Actualizar solo las observaciones
        if (solicitud.getObservacion() != null && solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
            SolicitudCursoVeranoPreinscripcionEntity preinscripcionEntity = (SolicitudCursoVeranoPreinscripcionEntity) solicitudEntity;
            preinscripcionEntity.setObservacion(solicitud.getObservacion());
        }
        
        // Guardar los cambios
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        return solicitudMapper.map(solicitudActualizada, SolicitudCursoVeranoPreinscripcion.class);
    }

}
