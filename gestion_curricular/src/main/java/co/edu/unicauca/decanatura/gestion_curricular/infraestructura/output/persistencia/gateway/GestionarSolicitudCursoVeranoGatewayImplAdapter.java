package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;

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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;


@Service
@Transactional
public class GestionarSolicitudCursoVeranoGatewayImplAdapter implements GestionarSolicitudCursoVeranoGatewayIntPort {

    private final SolicitudRepositoryInt solicitudRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository;
    private final ModelMapper solicitudMapper;

    public GestionarSolicitudCursoVeranoGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                           CursoOfertadoVeranoRepositoryInt cursoOfertadoVeranoRepository,
                                                           ModelMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.cursoOfertadoVeranoRepository = cursoOfertadoVeranoRepository;
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
        solicitudCursoVeranoEntity.setFecha_registro_solicitud(new Date());
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
