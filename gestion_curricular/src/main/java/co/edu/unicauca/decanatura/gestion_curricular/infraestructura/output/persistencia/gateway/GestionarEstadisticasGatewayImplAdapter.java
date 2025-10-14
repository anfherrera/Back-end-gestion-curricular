package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasGlobales() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Estadísticas generales del sistema
        Integer totalSolicitudes = solicitudRepository.totalSolicitudes();
        Integer totalAprobadas = solicitudRepository.contarSolicitudesPorUltimoEstado("Aprobado");
        Integer totalRechazadas = solicitudRepository.contarSolicitudesPorUltimoEstado("Rechazado");
        Integer totalEnProceso = solicitudRepository.contarSolicitudesPorUltimoEstado("Enviada");
        
        estadisticas.put("totalSolicitudes", totalSolicitudes);
        estadisticas.put("totalAprobadas", totalAprobadas);
        estadisticas.put("totalRechazadas", totalRechazadas);
        estadisticas.put("totalEnProceso", totalEnProceso);
        estadisticas.put("porcentajeAprobacion", totalSolicitudes > 0 ? (double) totalAprobadas / totalSolicitudes * 100 : 0.0);
        
        // Estadísticas por tipo de proceso
        Map<String, Integer> porTipoProceso = new HashMap<>();
        List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
        for (String proceso : nombresProcesos) {
            Integer cantidad = solicitudRepository.contarPorNombre(proceso);
            porTipoProceso.put(proceso, cantidad);
        }
        estadisticas.put("porTipoProceso", porTipoProceso);
        
        // Estadísticas por programa
        List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
        estadisticas.put("totalProgramas", nombresProgramas.size());
        estadisticas.put("nombresProgramas", nombresProgramas);
        
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorProceso(String tipoProceso) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        Integer totalPorProceso = solicitudRepository.contarPorNombre(tipoProceso);
        
        // Contar por estado para el proceso específico
        // Nota: Estos métodos necesitarían ser implementados en el repositorio
        // Por ahora usamos los métodos existentes como aproximación
        
        estadisticas.put("tipoProceso", tipoProceso);
        estadisticas.put("totalSolicitudes", totalPorProceso);
        estadisticas.put("descripcion", obtenerDescripcionProceso(tipoProceso));
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorEstado(String estado) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        Integer totalPorEstado = solicitudRepository.contarSolicitudesPorUltimoEstado(estado);
        
        estadisticas.put("estado", estado);
        estadisticas.put("totalSolicitudes", totalPorEstado);
        estadisticas.put("descripcionEstado", obtenerDescripcionEstado(estado));
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorPrograma(Integer idPrograma) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Obtener estadísticas por programa (usando fechas amplias para capturar todo)
        Date fechaInicio = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 año atrás
        Date fechaFin = new Date();
        
        Map<String, Integer> porTipoProceso = new HashMap<>();
        List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
        
        for (String proceso : nombresProcesos) {
            Integer cantidad = solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma);
            porTipoProceso.put(proceso, cantidad);
        }
        
        estadisticas.put("idPrograma", idPrograma);
        estadisticas.put("porTipoProceso", porTipoProceso);
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        Integer totalPorPeriodo = solicitudRepository.contarPorRangoFechas(fechaInicio, fechaFin);
        
        Map<String, Integer> porTipoProceso = new HashMap<>();
        List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
        
        for (String proceso : nombresProcesos) {
            Integer cantidad = solicitudRepository.contarPorNombre(proceso);
            porTipoProceso.put(proceso, cantidad);
        }
        
        estadisticas.put("fechaInicio", fechaInicio);
        estadisticas.put("fechaFin", fechaFin);
        estadisticas.put("totalSolicitudes", totalPorPeriodo);
        estadisticas.put("porTipoProceso", porTipoProceso);
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        Map<String, Integer> porTipoProceso = new HashMap<>();
        List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
        
        for (String proceso : nombresProcesos) {
            Integer cantidad = solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma);
            porTipoProceso.put(proceso, cantidad);
        }
        
        estadisticas.put("idPrograma", idPrograma);
        estadisticas.put("fechaInicio", fechaInicio);
        estadisticas.put("fechaFin", fechaFin);
        estadisticas.put("porTipoProceso", porTipoProceso);
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorProcesoYEstado(String tipoProceso, String estado) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Obtener estadísticas por proceso y estado para todos los programas
        List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
        Map<String, Integer> porPrograma = new HashMap<>();
        
        for (String programa : nombresProgramas) {
            // Nota: Este método necesitaría ser implementado en el repositorio
            // Por ahora usamos una aproximación con los métodos existentes
            porPrograma.put(programa, 0); // Placeholder
        }
        
        estadisticas.put("tipoProceso", tipoProceso);
        estadisticas.put("estado", estado);
        estadisticas.put("porPrograma", porPrograma);
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenCompleto() {
        Map<String, Object> resumen = new HashMap<>();
        
        // Estadísticas globales
        Map<String, Object> estadisticasGlobales = obtenerEstadisticasGlobales();
        resumen.put("estadisticasGlobales", estadisticasGlobales);
        
        // Estadísticas por tipo de proceso
        Map<String, Object> porProceso = new HashMap<>();
        List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
        for (String proceso : nombresProcesos) {
            Map<String, Object> estadisticasProceso = obtenerEstadisticasPorProceso(proceso);
            porProceso.put(proceso, estadisticasProceso);
        }
        resumen.put("porTipoProceso", porProceso);
        
        // Estadísticas por estado
        Map<String, Object> porEstado = new HashMap<>();
        String[] estados = {"Aprobado", "Rechazado", "Enviada", "En_Proceso"};
        for (String estado : estados) {
            Map<String, Object> estadisticasEstado = obtenerEstadisticasPorEstado(estado);
            porEstado.put(estado, estadisticasEstado);
        }
        resumen.put("porEstado", porEstado);
        
        // Información del sistema
        resumen.put("totalProgramas", programaRepository.buscarNombresProgramas().size());
        resumen.put("fechaGeneracion", new Date());
        
        return resumen;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2) {
        Map<String, Object> tendencias = new HashMap<>();
        
        // Estadísticas del primer período
        Map<String, Object> periodo1 = obtenerEstadisticasPorPeriodo(fechaInicio1, fechaFin1);
        
        // Estadísticas del segundo período
        Map<String, Object> periodo2 = obtenerEstadisticasPorPeriodo(fechaInicio2, fechaFin2);
        
        // Calcular tendencias
        Integer total1 = (Integer) periodo1.get("totalSolicitudes");
        Integer total2 = (Integer) periodo2.get("totalSolicitudes");
        
        double variacion = total1 > 0 ? ((double) (total2 - total1) / total1) * 100 : 0.0;
        
        tendencias.put("periodo1", periodo1);
        tendencias.put("periodo2", periodo2);
        tendencias.put("variacionPorcentual", variacion);
        tendencias.put("tendencia", variacion > 0 ? "CRECIENTE" : variacion < 0 ? "DECRECIENTE" : "ESTABLE");
        tendencias.put("fechaConsulta", new Date());
        
        return tendencias;
    }

    /**
     * Método auxiliar para obtener descripción de procesos
     */
    private String obtenerDescripcionProceso(String tipoProceso) {
        switch (tipoProceso.toUpperCase()) {
            case "REINGRESO":
                return "Solicitudes de reingreso a programas académicos";
            case "HOMOLOGACION":
                return "Solicitudes de homologación de materias";
            case "ECAES":
                return "Solicitudes para exámenes ECAES";
            case "CURSO_VERANO":
                return "Solicitudes para cursos intersemestrales";
            case "PAZ_SALVO":
                return "Solicitudes de paz y salvo";
            default:
                return "Proceso académico";
        }
    }

    /**
     * Método auxiliar para obtener descripción de estados
     */
    private String obtenerDescripcionEstado(String estado) {
        switch (estado.toUpperCase()) {
            case "APROBADO":
                return "Solicitudes aprobadas";
            case "RECHAZADO":
                return "Solicitudes rechazadas";
            case "ENVIADA":
                return "Solicitudes enviadas pendientes de revisión";
            case "EN_PROCESO":
                return "Solicitudes en proceso de evaluación";
            default:
                return "Estado de solicitud";
        }
    }
   
}
