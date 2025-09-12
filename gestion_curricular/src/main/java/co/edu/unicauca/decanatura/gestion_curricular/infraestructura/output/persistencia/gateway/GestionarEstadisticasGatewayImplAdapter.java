package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadisticaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;

@Service
@Transactional
public class GestionarEstadisticasGatewayImplAdapter implements GestionarEstadisticasGatewayIntPort {

    private final EstadisticaRepositoryInt estadisticaRepository;
    private final SolicitudRepositoryInt solicitudRepository;
    private final ProgramaRepositoryInt programaRepository;
    private final ModelMapper estadisticaMapper;

    public GestionarEstadisticasGatewayImplAdapter(EstadisticaRepositoryInt estadisticaRepository, ProgramaRepositoryInt programaRepository, SolicitudRepositoryInt solicitudRepository, ModelMapper estadisticaMapper) {
        this.estadisticaRepository = estadisticaRepository;
        this.programaRepository = programaRepository;
        this.solicitudRepository = solicitudRepository;
        this.estadisticaMapper = estadisticaMapper;
    }

    @Override
    @Transactional
    public Estadistica crearEstadistica(Estadistica estadistica) {
        if(solicitudRepository.count() <= 0) {
            throw new RuntimeException("No hay solicitudes disponibles para crear estadísticas.");
        }
        if(programaRepository.count() <= 0) {
            throw new RuntimeException("No hay programas disponibles para crear estadísticas.");
        }
        EstadisticaEntity estadisticaEntity = estadisticaMapper.map(estadistica, EstadisticaEntity.class);
        //estadisticaEntity.setNombre("Estadisticas");
        estadisticaEntity.setTotal_solicitudes(solicitudRepository.totalSolicitudes());
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarEstado("Aprobado"));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarEstado("Rechazado"));
        estadisticaEntity.setNombres_procesos(new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes()));
        estadisticaEntity.setPeriodos_academico(new ArrayList<Date>());
        estadisticaEntity.setNombres_programas(new ArrayList<String>(programaRepository.buscarNombresProgramas()));
        EstadisticaEntity savedEntity = estadisticaRepository.save(estadisticaEntity);
        return estadisticaMapper.map(savedEntity, Estadistica.class);
    }

    @Override
    @Transactional
    public Estadistica actualizarEstadistica(Estadistica estadistica) {
        estadisticaRepository.findById(estadistica.getId_estadistica())
            .orElseThrow(() -> new RuntimeException("Estadistica no encontrada con ID: " + estadistica.getId_estadistica()));
        EstadisticaEntity estadisticaEntity = estadisticaMapper.map(estadistica, EstadisticaEntity.class);
                estadisticaEntity.setTotal_solicitudes(solicitudRepository.totalSolicitudes());
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarEstado("Aprobado"));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarEstado("Rechazado"));
        estadisticaEntity.setNombres_procesos(new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes()));
        estadisticaEntity.setPeriodos_academico(new ArrayList<Date>());
        estadisticaEntity.setNombres_programas(new ArrayList<String>(programaRepository.buscarNombresProgramas()));
        EstadisticaEntity updatedEntity = estadisticaRepository.save(estadisticaEntity);
        return estadisticaMapper.map(updatedEntity, Estadistica.class);
    }

    @Override
    @Transactional
    public Boolean eliminarEstadistica(Integer idEstadistica) {
        boolean eliminado = false;
        Optional<EstadisticaEntity> estadisticaEntity = estadisticaRepository.findById(idEstadistica);
        if (estadisticaEntity != null) {
            estadisticaRepository.deleteById(idEstadistica);
            eliminado = true;
        }
        return eliminado;
    }

    @Override
    @Transactional(readOnly = true)
    public Estadistica obtenerEstadisticaPorId(Integer idEstadistica) {
       EstadisticaEntity entity = estadisticaRepository.findById(idEstadistica)
        .orElseThrow(() -> new IllegalArgumentException("Estadística no encontrada"));
        return estadisticaMapper.map(entity, Estadistica.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Estadistica obtenerEstadisticasSolicitudPeriodoYPrograma(Integer idEstadistica, String proceso,
            Date fechaInicio, Date fechaFin, Integer idPrograma) {
        EstadisticaEntity estadisticaEntity = estadisticaRepository.findById(idEstadistica)
        .orElseThrow(() -> new IllegalArgumentException("Estadística no encontrada"));
        
        if(solicitudRepository.count() >= 0) {
        
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "Aprobado", idPrograma)); // Asumiendo que 1 es el ID del estado "aprobada"
        estadisticaEntity.setTotal_solicitudes(solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "Rechazado", idPrograma)); // Asumiendo que 1 es el ID del estado "rechazada"
        }

        return estadisticaMapper.map(estadisticaEntity, Estadistica.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso,
            Date fechaInicio, Date fechaFin, String estado, Integer idPrograma) {
    EstadisticaEntity estadisticaEntity = estadisticaRepository.findById(idEstadistica)
        .orElseThrow(() -> new IllegalArgumentException("Estadística no encontrada con ID: " + idEstadistica));

        if(solicitudRepository.count() == 0) {
            throw new RuntimeException("No hay solicitudes disponibles para crear estadísticas.");
        }

        int totalSolicitudes = solicitudRepository.contarNombreFechaEstadoYPrograma(
        proceso, fechaInicio, fechaFin, estado, idPrograma);

        // Actualizar el campo total_solicitudes de la estadística
        estadisticaEntity.setTotal_solicitudes(totalSolicitudes);
        
        return estadisticaMapper.map(estadisticaEntity, Estadistica.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma(Date fechaInicio, Date fechaFin,
            Integer idPrograma) {
            List<EstadisticaEntity> estadisticaEntityOp = estadisticaRepository.findAll();
            List<Estadistica> estadisticas = new ArrayList<>();
            if(estadisticaEntityOp!=null){
                List<String> nombres_procesos = new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes());
                for (String proceso : nombres_procesos) {
                    Estadistica estadistica = new Estadistica();
                    estadistica.setNombre(proceso);
                    
                    estadistica.setTotal_solicitudes(solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma));
                    estadistica.setTotal_aprobadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "Aprobado", idPrograma));
                    estadistica.setTotal_rechazadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "Rechazado", idPrograma));
                    
                    estadisticas.add(estadistica);
                }
            }
       return estadisticas;
    }


    

    
   
}
