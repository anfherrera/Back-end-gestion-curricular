package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.NombreSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ReglasCupo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadoSolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.enums.GrupoCursoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.CatalogosMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.SolicitudMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;


@Service
@Transactional
public class GestionarSolicitudCursoVeranoGatewayImplAdapter implements GestionarSolicitudCursoVeranoGatewayIntPort {

    private final SolicitudRepositoryInt solicitudRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository;
    private final EstadoSolicitudRepositoryInt estadoSolicitudRepository;

    public GestionarSolicitudCursoVeranoGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                           CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository,
                                                           EstadoSolicitudRepositoryInt estadoSolicitudRepository) {
        this.solicitudRepository = solicitudRepository;
        this.cursoOfertadoVeranoRepository = cursoOfertadoVeranoRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> listarSolicitudesDeVerano(Integer idCurso) {
        return solicitudRepository.listarSolicitudesDeVerano(idCurso).stream()
                .<Solicitud>map(solicitud -> solicitud instanceof SolicitudCursoVeranoPreinscripcionEntity
                        ? SolicitudMapperPersistencia.aDominio(solicitud, SolicitudCursoVeranoPreinscripcion.class)
                        : SolicitudMapperPersistencia.aDominio(solicitud, SolicitudCursoVeranoInscripcion.class))
                .toList();
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(
        SolicitudCursoVeranoPreinscripcion solicitudCursoVerano) {
        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoEntity = SolicitudMapperPersistencia.aEntidad(solicitudCursoVerano, SolicitudCursoVeranoPreinscripcionEntity.class);
        
        // Generar nombre descriptivo con el nombre del estudiante
        solicitudCursoVeranoEntity.setNombre_solicitud(NombreSolicitud.de(NombreSolicitud.CURSO_VERANO,
                NombreSolicitud.nombreDe(solicitudCursoVerano.getObjUsuario()),
                solicitudCursoVerano.getNombreEstudiante()));
        solicitudCursoVeranoEntity.setFecha_registro_solicitud(new Date());
        EstadoSolicitudEntity estadoSolicitudEntity = null;

        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setEstado_actual(EstadoSolicitudCursoVerano.ENVIADA.texto());
        estadoSolicitudEntity.setObjSolicitud(solicitudCursoVeranoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudCursoVeranoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudCursoVeranoEntity.setEstadosSolicitud(estadosSolcitud);

        CursoOfertadoVerano cursoOfertado = solicitudCursoVerano.getObjCursoOfertadoVerano();
        CursoOfertadoVeranoEntity cursoOfertadoVeranoEntity = null;

        if(cursoOfertado != null) {
            Integer idCurso = cursoOfertado.getIdCurso();
            
            // Si el ID es 0 o null, es un curso nuevo (solicitud de apertura)
            if(idCurso == null || idCurso == 0) {
                // Para cursos nuevos, no necesitamos crear la entidad del curso
                // Solo guardamos la solicitud con la información del curso solicitado
                cursoOfertadoVeranoEntity = null;
                solicitudCursoVeranoEntity.setPeriodo_academico(
                        PeriodoAcademicoEnum.oActual(solicitudCursoVerano.getPeriodoAcademico()));
            } else if(cursoOfertadoVeranoRepository.existsById(idCurso)){
                cursoOfertadoVeranoEntity = cursoOfertadoVeranoRepository.findById(idCurso)
                        .orElseThrow(() -> new IllegalArgumentException("Curso ofertado no encontrado con ID: " + idCurso));
                // Asignar el período académico del curso a la solicitud
                if (cursoOfertadoVeranoEntity.getPeriodo_academico() != null && !cursoOfertadoVeranoEntity.getPeriodo_academico().trim().isEmpty()) {
                    solicitudCursoVeranoEntity.setPeriodo_academico(cursoOfertadoVeranoEntity.getPeriodo_academico());
                }
            } else {
                cursoOfertadoVeranoEntity = new CursoOfertadoVeranoEntity();
                GrupoCursoVeranoEntity  grupoEntity = SolicitudMapperPersistencia.aEntidad(cursoOfertado.getGrupo());
                cursoOfertadoVeranoEntity.setGrupo(grupoEntity);
                cursoOfertadoVeranoEntity.setCupo_estimado(cursoOfertado.getCupoEstimado());
                cursoOfertadoVeranoEntity.setSalon(cursoOfertado.getSalon());
                MateriaEntity materiaEntity = CatalogosMapperPersistencia.aEntidad(cursoOfertado.getObjMateria());
                DocenteEntity docenteEntity = CatalogosMapperPersistencia.aEntidad(cursoOfertado.getObjDocente());
                cursoOfertadoVeranoEntity.setObjMateria(materiaEntity);
                cursoOfertadoVeranoEntity.setObjDocente(docenteEntity);
                String periodoValor = PeriodoAcademicoEnum.oActual(cursoOfertado.getPeriodoAcademico());
                if (periodoValor != null) {
                    cursoOfertadoVeranoEntity.setPeriodo_academico(periodoValor);
                    solicitudCursoVeranoEntity.setPeriodo_academico(periodoValor);
                }
            }
            solicitudCursoVeranoEntity.setObjCursoOfertadoVerano(cursoOfertadoVeranoEntity);
        }else{
            throw new IllegalArgumentException("El curso ofertado no puede ser nulo.");
        }

        solicitudCursoVeranoEntity.setPeriodo_academico(
                PeriodoAcademicoEnum.recortado(solicitudCursoVeranoEntity.getPeriodo_academico()));
        SolicitudCursoVeranoPreinscripcionEntity solicitudCursoVeranoGuardado = solicitudRepository.save(solicitudCursoVeranoEntity);

        return SolicitudMapperPersistencia.aDominio(solicitudCursoVeranoGuardado, SolicitudCursoVeranoPreinscripcion.class);
    }


    @Override
    @Transactional
    public SolicitudCursoVeranoInscripcion crearSolicitudCursoVeranoInscripcion(
            SolicitudCursoVeranoInscripcion solicitudCursoVerano) {
        SolicitudCursoVeranoInscripcionEntity solicitudCursoVeranoEntity = SolicitudMapperPersistencia.aEntidad(solicitudCursoVerano, SolicitudCursoVeranoInscripcionEntity.class);
        
        // Generar nombre descriptivo con el nombre del estudiante
        solicitudCursoVeranoEntity.setNombre_solicitud(NombreSolicitud.de(NombreSolicitud.CURSO_VERANO,
                NombreSolicitud.nombreDe(solicitudCursoVerano.getObjUsuario())));
        solicitudCursoVeranoEntity.setFecha_registro_solicitud(new Date());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setEstado_actual(EstadoSolicitudCursoVerano.ENVIADA.texto());
        estadoSolicitudEntity.setObjSolicitud(solicitudCursoVeranoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudCursoVeranoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudCursoVeranoEntity.setEstadosSolicitud(estadosSolcitud);
        CursoOfertadoVerano cursoOfertado = solicitudCursoVerano.getObjCursoOfertadoVerano();
        CursoOfertadoVeranoEntity cursoOfertadoVeranoEntity = null;
        if(cursoOfertado != null && cursoOfertado.getIdCurso() != null) {
            Integer idCurso = cursoOfertado.getIdCurso();
            if(cursoOfertadoVeranoRepository.existsById(idCurso)){
                cursoOfertadoVeranoEntity = cursoOfertadoVeranoRepository.findById(idCurso)
                        .orElseThrow(() -> new IllegalArgumentException("Curso ofertado no encontrado con ID: " + idCurso));
                // Asignar el período académico del curso a la solicitud
                if (cursoOfertadoVeranoEntity.getPeriodo_academico() != null && !cursoOfertadoVeranoEntity.getPeriodo_academico().trim().isEmpty()) {
                    solicitudCursoVeranoEntity.setPeriodo_academico(cursoOfertadoVeranoEntity.getPeriodo_academico());
                }
            } else {
                cursoOfertadoVeranoEntity = new CursoOfertadoVeranoEntity();
                GrupoCursoVeranoEntity  grupoEntity = SolicitudMapperPersistencia.aEntidad(cursoOfertado.getGrupo());
                cursoOfertadoVeranoEntity.setGrupo(grupoEntity);
                cursoOfertadoVeranoEntity.setCupo_estimado(cursoOfertado.getCupoEstimado());
                cursoOfertadoVeranoEntity.setSalon(cursoOfertado.getSalon());
                MateriaEntity materiaEntity = CatalogosMapperPersistencia.aEntidad(cursoOfertado.getObjMateria());
                DocenteEntity docenteEntity = CatalogosMapperPersistencia.aEntidad(cursoOfertado.getObjDocente());
                cursoOfertadoVeranoEntity.setObjMateria(materiaEntity);
                cursoOfertadoVeranoEntity.setObjDocente(docenteEntity);
                // Si el curso tiene período académico en el dominio, asignarlo también a la solicitud
                String periodoCurso = PeriodoAcademicoEnum.recortado(cursoOfertado.getPeriodoAcademico());
                if (periodoCurso != null) {
                    cursoOfertadoVeranoEntity.setPeriodo_academico(periodoCurso);
                    solicitudCursoVeranoEntity.setPeriodo_academico(periodoCurso);
                }
            }
            solicitudCursoVeranoEntity.setObjCursoOfertadoVerano(cursoOfertadoVeranoEntity);
        } else {
            throw new IllegalArgumentException("El curso ofertado no puede ser nulo o debe tener un ID válido.");
        }
        solicitudCursoVeranoEntity.setPeriodo_academico(
                PeriodoAcademicoEnum.recortado(solicitudCursoVeranoEntity.getPeriodo_academico()));
        SolicitudCursoVeranoInscripcionEntity solicitudCursoVeranoGuardado = solicitudRepository.save(solicitudCursoVeranoEntity);

        return SolicitudMapperPersistencia.aDominio(solicitudCursoVeranoGuardado, SolicitudCursoVeranoInscripcion.class);
    }

    @Override
    public Solicitud buscarSolicitudesPorUsuarioYCursoPre(Integer idUsuario, Integer idCurso) {
    SolicitudEntity solicitudEntity = solicitudRepository.buscarSolicitudesPorUsuarioyCursoPre(idUsuario, idCurso);
        Solicitud solicitud = null;
        if(solicitudEntity!= null){
                if (solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                    solicitud = SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoPreinscripcion.class);
                } else if (solicitudEntity instanceof SolicitudEcaesEntity) {
                    solicitud = SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudEcaes.class);
                } else if (solicitudEntity instanceof SolicitudReingresoEntity) {
                    solicitud = SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudReingreso.class);
                } else if (solicitudEntity instanceof SolicitudHomologacionEntity) {
                    solicitud = SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudHomologacion.class);
                } else if (solicitudEntity instanceof SolicitudPazYSalvoEntity) {
                    solicitud = SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudPazYSalvo.class);
                } else if (solicitudEntity instanceof SolicitudCursoVeranoInscripcionEntity) {
                    solicitud = SolicitudMapperPersistencia.aDominio(solicitudEntity, SolicitudCursoVeranoInscripcion.class);
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
                solicitudes.add(SolicitudMapperPersistencia.aDominio(entity, SolicitudCursoVeranoPreinscripcion.class));
            }
        }
        
        return solicitudes;
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCursoVeranoPreinscripcion buscarSolicitudPorId(Integer idSolicitud) {
        return solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoPreinscripcionEntity)
            .map(entity -> SolicitudMapperPersistencia.aDominio(entity, SolicitudCursoVeranoPreinscripcion.class))
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoPreinscripcion> buscarPreinscripcionesPorCurso(Integer idCurso) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarSolicitudesPorCurso(idCurso);
        List<SolicitudCursoVeranoPreinscripcion> preinscripciones = new ArrayList<>();
        
        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoPreinscripcionEntity) {
                preinscripciones.add(SolicitudMapperPersistencia.aDominio(entity, SolicitudCursoVeranoPreinscripcion.class));
            }
        }
        
        return preinscripciones;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoInscripcion> buscarInscripcionesPorCurso(Integer idCurso) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarInscripcionesPorCurso(idCurso);
        List<SolicitudCursoVeranoInscripcion> inscripciones = new ArrayList<>();
        
        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoInscripcionEntity) {
                inscripciones.add(SolicitudMapperPersistencia.aDominio(entity, SolicitudCursoVeranoInscripcion.class));
            }
        }
        
        return inscripciones;
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCursoVeranoInscripcion buscarSolicitudInscripcionPorId(Integer idSolicitud) {
        return solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoInscripcionEntity)
            .map(entity -> {
                entity.getDocumentos().size();
                return SolicitudMapperPersistencia.aDominio(entity, SolicitudCursoVeranoInscripcion.class);
            })
            .orElse(null);
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion aprobarPreinscripcion(Integer idSolicitud, String comentarios) {
        // Buscar la solicitud sin filtrar primero
        Optional<SolicitudEntity> solicitudOpt = solicitudRepository.findById(idSolicitud);
        if (!solicitudOpt.isPresent()) {
            return null;
        }
        
        SolicitudEntity solicitudEntity = solicitudOpt.get();
        
        // Verificar si es del tipo correcto
        if (!(solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity)) {
            return null;
        }
        
        // Crear nuevo estado "Aprobado"
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual(EstadoSolicitudCursoVerano.APROBADO.texto());
        estadoEntity.setObjSolicitud(solicitudEntity);
        if (comentarios != null && !comentarios.trim().isEmpty()) {
            estadoEntity.setComentario(comentarios.trim());
        }
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        
        SolicitudCursoVeranoPreinscripcion resultado = SolicitudMapperPersistencia.aDominio(solicitudActualizada, SolicitudCursoVeranoPreinscripcion.class);
        
        return resultado;
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion rechazarPreinscripcion(Integer idSolicitud, String motivo) {
        // Buscar la solicitud sin filtrar primero
        Optional<SolicitudEntity> solicitudOpt = solicitudRepository.findById(idSolicitud);
        if (!solicitudOpt.isPresent()) {
            return null;
        }
        
        SolicitudEntity solicitudEntity = solicitudOpt.get();
        
        // Verificar si es del tipo correcto
        if (!(solicitudEntity instanceof SolicitudCursoVeranoPreinscripcionEntity)) {
            return null;
        }
        
        // Crear nuevo estado "Rechazado"
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual(EstadoSolicitudCursoVerano.RECHAZADO.texto());
        estadoEntity.setObjSolicitud(solicitudEntity);
        estadoEntity.setComentario(motivo.trim());
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        
        SolicitudCursoVeranoPreinscripcion resultado = SolicitudMapperPersistencia.aDominio(solicitudActualizada, SolicitudCursoVeranoPreinscripcion.class);
        
        return resultado;
    }

    @Override
    // READ_COMMITTED: en MySQL (REPEATABLE READ por defecto) el conteo de cupo, hecho después de
    // bloquear el curso, leería la foto tomada al inicio de la transacción y no vería la
    // inscripción que otro funcionario acaba de aceptar.
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public SolicitudCursoVeranoInscripcion validarPago(Integer idSolicitud, boolean esValido, String observaciones) {
        SolicitudEntity solicitudEntity = solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoInscripcionEntity)
            .orElse(null);
        
        if (solicitudEntity == null) {
            return null;
        }

        if (esValido) {
            verificarCupoConBloqueo(solicitudEntity);
        }

        // Crear nuevo estado según validación
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual(esValido ? EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto() : EstadoSolicitudCursoVerano.PAGO_RECHAZADO.texto());
        estadoEntity.setObjSolicitud(solicitudEntity);
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            estadoEntity.setComentario(observaciones.trim());
        }
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        return SolicitudMapperPersistencia.aDominio(solicitudActualizada, SolicitudCursoVeranoInscripcion.class);
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoInscripcion completarInscripcion(Integer idSolicitud) {
        SolicitudEntity solicitudEntity = solicitudRepository.findById(idSolicitud)
            .filter(entity -> entity instanceof SolicitudCursoVeranoInscripcionEntity)
            .orElse(null);
        
        if (solicitudEntity == null) {
            return null;
        }
        
        // Crear nuevo estado "Inscripcion_Completada"
        EstadoSolicitudEntity estadoEntity = new EstadoSolicitudEntity();
        estadoEntity.setFecha_registro_estado(new Date());
        estadoEntity.setEstado_actual(EstadoSolicitudCursoVerano.INSCRIPCION_COMPLETADA.texto());
        estadoEntity.setObjSolicitud(solicitudEntity);
        
        // Guardar el estado
        estadoSolicitudRepository.save(estadoEntity);
        
        // Actualizar la solicitud
        solicitudEntity.getEstadosSolicitud().add(estadoEntity);
        SolicitudEntity solicitudActualizada = solicitudRepository.save(solicitudEntity);
        
        return SolicitudMapperPersistencia.aDominio(solicitudActualizada, SolicitudCursoVeranoInscripcion.class);
    }

    @Override
    @Transactional
    public SolicitudCursoVeranoPreinscripcion actualizarSolicitudCursoVerano(SolicitudCursoVeranoPreinscripcion solicitud) {
        if (solicitud == null || solicitud.getIdSolicitud() == null) {
            return null;
        }
        
        // Buscar la entidad existente
        SolicitudEntity solicitudEntity = solicitudRepository.findById(solicitud.getIdSolicitud())
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
        
        return SolicitudMapperPersistencia.aDominio(solicitudActualizada, SolicitudCursoVeranoPreinscripcion.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoInscripcion> buscarInscripcionesPorUsuarioYCurso(Integer idUsuario, Integer idCurso) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarInscripcionesPorUsuarioYCurso(idUsuario, idCurso);
        List<SolicitudCursoVeranoInscripcion> inscripciones = new ArrayList<>();

        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoInscripcionEntity) {
                inscripciones.add(SolicitudMapperPersistencia.aDominio(entity, SolicitudCursoVeranoInscripcion.class));
            }
        }

        return inscripciones;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCursoVeranoInscripcion> buscarInscripcionesPorUsuario(Integer idUsuario) {
        List<SolicitudEntity> solicitudesEntity = solicitudRepository.buscarInscripcionesPorUsuario(idUsuario);
        List<SolicitudCursoVeranoInscripcion> inscripciones = new ArrayList<>();

        for (SolicitudEntity entity : solicitudesEntity) {
            if (entity instanceof SolicitudCursoVeranoInscripcionEntity) {
                inscripciones.add(SolicitudMapperPersistencia.aDominio(entity, SolicitudCursoVeranoInscripcion.class));
            }
        }

        return inscripciones;
    }

    /**
     * Si aceptar la inscripción ocuparía un cupo nuevo, bloquea la fila del curso y vuelve a contar
     * dentro de esta transacción. Así, si dos funcionarios aceptan a la vez, el segundo espera al
     * primero y ve el conteo ya actualizado.
     *
     * @throws CupoAgotadoException si el curso ya no tiene cupo
     */
    private void verificarCupoConBloqueo(SolicitudEntity inscripcion) {
        CursoOfertadoVeranoEntity curso = inscripcion.getObjCursoOfertadoVerano();
        if (curso == null || curso.getId_curso() == null || yaOcupaCupo(inscripcion)) {
            return;
        }
        CursoOfertadoVeranoEntity bloqueado = cursoOfertadoVeranoRepository.bloquearPorId(curso.getId_curso())
                .orElse(curso);
        ReglasCupo.exigirCupoLibre(bloqueado.getCupo_estimado(),
                () -> contarInscripcionesAceptadasPorCurso(bloqueado.getId_curso()));
    }

    private static boolean yaOcupaCupo(SolicitudEntity inscripcion) {
        List<EstadoSolicitudEntity> estados = inscripcion.getEstadosSolicitud();
        if (estados == null || estados.isEmpty()) {
            return false;
        }
        return ReglasCupo.estadoOcupaCupo(estados.get(estados.size() - 1).getEstado_actual());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarInscripcionesAceptadasPorCurso(Integer idCurso) {
        // Ocupan cupo las inscripciones aceptadas (ver ReglasCupo)
        int total = 0;
        for (String estado : ReglasCupo.ESTADOS_QUE_OCUPAN_CUPO) {
            Integer n = solicitudRepository.contarInscripcionesPorEstado(idCurso, estado);
            total += n != null ? n : 0;
        }
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarInscripcionesPorEstado(Integer idCurso, String estado) {
        return solicitudRepository.contarInscripcionesPorEstado(idCurso, estado);
    }

}
