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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
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
        return obtenerEstadisticasGlobales(null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasGlobales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        System.out.println("📊 [ESTADISTICAS_GLOBALES] Iniciando consulta de estadísticas globales");
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Estadísticas generales del sistema (con filtros si se aplican)
            Integer totalSolicitudes = Optional.ofNullable(solicitudRepository.contarSolicitudesConFiltros(proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
            Integer totalAprobadas = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("Aprobado", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
            Integer totalRechazadas = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("Rechazado", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
            Integer totalEnProceso = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("En_Proceso", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
            Integer totalEnviadas = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("Enviada", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
            
            System.out.println("📊 [ESTADISTICAS_GLOBALES] Totales obtenidos - Total: " + totalSolicitudes + 
                             ", Aprobadas: " + totalAprobadas + ", Rechazadas: " + totalRechazadas + 
                             ", En Proceso: " + totalEnProceso + ", Enviadas: " + totalEnviadas);
            
            estadisticas.put("totalSolicitudes", totalSolicitudes);
            estadisticas.put("totalAprobadas", totalAprobadas);
            estadisticas.put("totalRechazadas", totalRechazadas);
            estadisticas.put("totalEnProceso", totalEnProceso + totalEnviadas); // Combinar ambos tipos
            
            // Calcular porcentaje de aprobación
            double porcentajeAprobacion = 0.0;
            if (totalSolicitudes > 0) {
                porcentajeAprobacion = (double) totalAprobadas / totalSolicitudes * 100;
            }
            estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 10.0) / 10.0);
            
            // Estadísticas por tipo de proceso (con filtros si se aplican)
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudesConFiltros(proceso, idPrograma, fechaInicio, fechaFin));
                System.out.println("📊 [ESTADISTICAS_GLOBALES] Procesos encontrados: " + nombresProcesos);
                
                for (String nombreProceso : nombresProcesos) {
                    Integer cantidad = Optional.ofNullable(solicitudRepository.contarPorNombreConFiltros(nombreProceso, proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
                    porTipoProceso.put(nombreProceso, cantidad);
                    System.out.println("📊 [ESTADISTICAS_GLOBALES] Proceso: " + nombreProceso + " = " + cantidad);
                }
            } catch (Exception e) {
                System.err.println("⚠️ [ESTADISTICAS_GLOBALES] Error obteniendo procesos: " + e.getMessage());
                porTipoProceso = new HashMap<>();
            }
            estadisticas.put("porTipoProceso", porTipoProceso);
            
            // Estadísticas por programa (con filtros si se aplican)
            Map<String, Integer> porPrograma = new HashMap<>();
            try {
                List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
                System.out.println("📊 [ESTADISTICAS_GLOBALES] Programas encontrados: " + nombresProgramas);
                
                // Obtener todos los programas con sus IDs
                List<ProgramaEntity> programas = programaRepository.findAll();
                
                for (ProgramaEntity programa : programas) {
                    Integer cantidad = Optional.ofNullable(solicitudRepository.contarSolicitudesPorProgramaConFiltros(programa.getId_programa(), proceso, fechaInicio, fechaFin)).orElse(0);
                    porPrograma.put(programa.getNombre_programa(), cantidad);
                    System.out.println("📊 [ESTADISTICAS_GLOBALES] Programa: " + programa.getNombre_programa() + " = " + cantidad);
                }
            } catch (Exception e) {
                System.err.println("⚠️ [ESTADISTICAS_GLOBALES] Error obteniendo programas: " + e.getMessage());
                porPrograma = new HashMap<>();
            }
            estadisticas.put("porPrograma", porPrograma);
            
            // Estadísticas por estado (consistente con la BD)
            Map<String, Integer> porEstado = new HashMap<>();
            porEstado.put("Aprobado", totalAprobadas);
            porEstado.put("Rechazado", totalRechazadas);
            porEstado.put("En_Proceso", totalEnProceso);
            porEstado.put("Enviada", totalEnviadas);
            estadisticas.put("porEstado", porEstado);
            
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            System.out.println("✅ [ESTADISTICAS_GLOBALES] Consulta completada exitosamente");
            System.out.println("📊 [ESTADISTICAS_GLOBALES] Resultado final: " + estadisticas);
            
        } catch (Exception e) {
            System.err.println("❌ [ESTADISTICAS_GLOBALES] Error en consulta: " + e.getMessage());
            e.printStackTrace();
            // Devolver estadísticas vacías pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorProceso(String tipoProceso) {
        System.out.println("📊 [ESTADISTICAS_POR_PROCESO] Consultando estadísticas para proceso: " + tipoProceso);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar que el tipoProceso no sea null o vacío
            if (tipoProceso == null || tipoProceso.trim().isEmpty()) {
                System.err.println("❌ [ESTADISTICAS_POR_PROCESO] Tipo de proceso no puede ser null o vacío");
                return normalizarEstadistica(new HashMap<>());
            }
            
            // Normalizar el nombre del proceso para búsqueda
            String procesoNormalizado = normalizarNombreProceso(tipoProceso);
            System.out.println("📊 [ESTADISTICAS_POR_PROCESO] Proceso normalizado: " + procesoNormalizado);
            
            Integer totalPorProceso = Optional.ofNullable(solicitudRepository.contarPorNombre(procesoNormalizado)).orElse(0);
            System.out.println("📊 [ESTADISTICAS_POR_PROCESO] Total encontrado: " + totalPorProceso);
            
            // Obtener estadísticas por estado para este proceso específico
            Map<String, Integer> porEstado = new HashMap<>();
            String[] estados = {"Aprobado", "Rechazado", "Enviada", "En_Proceso"};
            
            for (String estado : estados) {
                Integer cantidad = 0;
                try {
                    // Por ahora usamos el conteo general por estado
                    // TODO: Implementar conteo específico por proceso y estado
                    cantidad = Optional.ofNullable(solicitudRepository.contarSolicitudesPorUltimoEstado(estado)).orElse(0);
                } catch (Exception e) {
                    System.out.println("⚠️ [ESTADISTICAS_POR_PROCESO] Error contando estado " + estado + ": " + e.getMessage());
                    cantidad = 0;
                }
                
                porEstado.put(estado, cantidad);
                System.out.println("📊 [ESTADISTICAS_POR_PROCESO] Estado " + estado + ": " + cantidad);
            }
            
            // Calcular totales para este proceso
            Integer totalAprobadas = porEstado.get("Aprobado");
            Integer totalRechazadas = porEstado.get("Rechazado");
            Integer totalEnProceso = porEstado.get("Enviada") + porEstado.get("En_Proceso");
            
            // Calcular porcentaje de aprobación para este proceso
            double porcentajeAprobacion = 0.0;
            if (totalPorProceso > 0) {
                porcentajeAprobacion = (double) totalAprobadas / totalPorProceso * 100;
            }
            
            estadisticas.put("tipoProceso", tipoProceso);
            estadisticas.put("totalSolicitudes", totalPorProceso);
            estadisticas.put("totalAprobadas", totalAprobadas);
            estadisticas.put("totalRechazadas", totalRechazadas);
            estadisticas.put("totalEnProceso", totalEnProceso);
            estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 10.0) / 10.0);
            estadisticas.put("porEstado", porEstado);
            estadisticas.put("porTipoProceso", new HashMap<>()); // Vacío para proceso específico
            estadisticas.put("porPrograma", new HashMap<>()); // Vacío para proceso específico
            estadisticas.put("descripcion", obtenerDescripcionProceso(tipoProceso));
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            System.out.println("✅ [ESTADISTICAS_POR_PROCESO] Consulta completada para: " + tipoProceso);
            System.out.println("📊 [ESTADISTICAS_POR_PROCESO] Resultado final: " + estadisticas);
            
        } catch (Exception e) {
            System.err.println("❌ [ESTADISTICAS_POR_PROCESO] Error en consulta: " + e.getMessage());
            e.printStackTrace();
            // Devolver estadísticas vacías pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorEstado(String estado) {
        System.out.println("📊 [ESTADISTICAS_POR_ESTADO] Consultando estadísticas para estado: " + estado);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar que el estado no sea null o vacío
            if (estado == null || estado.trim().isEmpty()) {
                System.err.println("❌ [ESTADISTICAS_POR_ESTADO] Estado no puede ser null o vacío");
                return normalizarEstadistica(new HashMap<>());
            }
            
            Integer totalPorEstado = Optional.ofNullable(solicitudRepository.contarSolicitudesPorUltimoEstado(estado)).orElse(0);
            System.out.println("📊 [ESTADISTICAS_POR_ESTADO] Total encontrado: " + totalPorEstado);
            
            // Obtener estadísticas por tipo de proceso para este estado
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
                for (String proceso : nombresProcesos) {
                    // TODO: Implementar conteo específico por proceso y estado
                    porTipoProceso.put(proceso, 0);
                }
            } catch (Exception e) {
                System.err.println("⚠️ [ESTADISTICAS_POR_ESTADO] Error obteniendo procesos: " + e.getMessage());
                porTipoProceso = new HashMap<>();
            }
            
            // Obtener estadísticas por programa para este estado
            Map<String, Integer> porPrograma = new HashMap<>();
            try {
                List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
                for (String programa : nombresProgramas) {
                    // TODO: Implementar conteo específico por programa y estado
                    porPrograma.put(programa, 0);
                }
            } catch (Exception e) {
                System.err.println("⚠️ [ESTADISTICAS_POR_ESTADO] Error obteniendo programas: " + e.getMessage());
                porPrograma = new HashMap<>();
            }
            
            estadisticas.put("estado", estado);
            estadisticas.put("totalSolicitudes", totalPorEstado);
            estadisticas.put("totalAprobadas", estado.equals("Aprobado") ? totalPorEstado : 0);
            estadisticas.put("totalRechazadas", estado.equals("Rechazado") ? totalPorEstado : 0);
            estadisticas.put("totalEnProceso", (estado.equals("Enviada") || estado.equals("En_Proceso")) ? totalPorEstado : 0);
            estadisticas.put("porcentajeAprobacion", estado.equals("Aprobado") ? 100.0 : 0.0);
            estadisticas.put("porTipoProceso", porTipoProceso);
            estadisticas.put("porPrograma", porPrograma);
            estadisticas.put("porEstado", new HashMap<>()); // Vacío para estado específico
            estadisticas.put("descripcionEstado", obtenerDescripcionEstado(estado));
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            System.out.println("✅ [ESTADISTICAS_POR_ESTADO] Consulta completada para: " + estado);
            System.out.println("📊 [ESTADISTICAS_POR_ESTADO] Resultado final: " + estadisticas);
            
        } catch (Exception e) {
            System.err.println("❌ [ESTADISTICAS_POR_ESTADO] Error en consulta: " + e.getMessage());
            e.printStackTrace();
            // Devolver estadísticas vacías pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorPrograma(Integer idPrograma) {
        System.out.println("📊 [ESTADISTICAS_POR_PROGRAMA] Consultando estadísticas para programa ID: " + idPrograma);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar que el idPrograma no sea null o menor a 1
            if (idPrograma == null || idPrograma < 1) {
                System.err.println("❌ [ESTADISTICAS_POR_PROGRAMA] ID de programa no puede ser null o menor a 1");
                return normalizarEstadistica(new HashMap<>());
            }
            
            // Obtener estadísticas por programa (usando fechas amplias para capturar todo)
            Date fechaInicio = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 año atrás
            Date fechaFin = new Date();
            
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
                System.out.println("📊 [ESTADISTICAS_POR_PROGRAMA] Procesos encontrados: " + nombresProcesos);
                
                for (String proceso : nombresProcesos) {
                    Integer cantidad = Optional.ofNullable(solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma)).orElse(0);
                    porTipoProceso.put(proceso, cantidad);
                    System.out.println("📊 [ESTADISTICAS_POR_PROGRAMA] Proceso " + proceso + ": " + cantidad);
                }
            } catch (Exception e) {
                System.err.println("⚠️ [ESTADISTICAS_POR_PROGRAMA] Error obteniendo procesos: " + e.getMessage());
                porTipoProceso = new HashMap<>();
            }
            
            // Obtener estadísticas por estado para este programa
            Map<String, Integer> porEstado = new HashMap<>();
            String[] estados = {"Aprobado", "Rechazado", "Enviada", "En_Proceso"};
            
            for (String estado : estados) {
                Integer cantidad = Optional.ofNullable(solicitudRepository.contarSolicitudesPorUltimoEstado(estado)).orElse(0);
                porEstado.put(estado, cantidad);
            }
            
            // Calcular totales para este programa
            Integer totalSolicitudes = porTipoProceso.values().stream().mapToInt(Integer::intValue).sum();
            Integer totalAprobadas = porEstado.get("Aprobado");
            Integer totalRechazadas = porEstado.get("Rechazado");
            Integer totalEnProceso = porEstado.get("Enviada") + porEstado.get("En_Proceso");
            
            // Calcular porcentaje de aprobación para este programa
            double porcentajeAprobacion = 0.0;
            if (totalSolicitudes > 0) {
                porcentajeAprobacion = (double) totalAprobadas / totalSolicitudes * 100;
            }
            
            estadisticas.put("idPrograma", idPrograma);
            estadisticas.put("totalSolicitudes", totalSolicitudes);
            estadisticas.put("totalAprobadas", totalAprobadas);
            estadisticas.put("totalRechazadas", totalRechazadas);
            estadisticas.put("totalEnProceso", totalEnProceso);
            estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 10.0) / 10.0);
            estadisticas.put("porTipoProceso", porTipoProceso);
            estadisticas.put("porEstado", porEstado);
            estadisticas.put("porPrograma", new HashMap<>()); // Vacío para programa específico
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            System.out.println("✅ [ESTADISTICAS_POR_PROGRAMA] Consulta completada para programa: " + idPrograma);
            System.out.println("📊 [ESTADISTICAS_POR_PROGRAMA] Resultado final: " + estadisticas);
            
        } catch (Exception e) {
            System.err.println("❌ [ESTADISTICAS_POR_PROGRAMA] Error en consulta: " + e.getMessage());
            e.printStackTrace();
            // Devolver estadísticas vacías pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin) {
        System.out.println("📊 [ESTADISTICAS_POR_PERIODO] Consultando estadísticas para período: " + fechaInicio + " - " + fechaFin);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar fechas
            if (fechaInicio == null || fechaFin == null) {
                System.err.println("❌ [ESTADISTICAS_POR_PERIODO] Las fechas no pueden ser null");
                return normalizarEstadistica(new HashMap<>());
            }
            
            Integer totalPorPeriodo = Optional.ofNullable(solicitudRepository.contarPorRangoFechas(fechaInicio, fechaFin)).orElse(0);
            System.out.println("📊 [ESTADISTICAS_POR_PERIODO] Total en período: " + totalPorPeriodo);
            
            // Obtener estadísticas por tipo de proceso en el período
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
                System.out.println("📊 [ESTADISTICAS_POR_PERIODO] Procesos encontrados: " + nombresProcesos);
                
                for (String proceso : nombresProcesos) {
                    // TODO: Implementar conteo específico por proceso y período
                    // Por ahora usamos el conteo general del proceso
                    Integer cantidad = Optional.ofNullable(solicitudRepository.contarPorNombre(proceso)).orElse(0);
                    porTipoProceso.put(proceso, cantidad);
                    System.out.println("📊 [ESTADISTICAS_POR_PERIODO] Proceso " + proceso + ": " + cantidad);
                }
            } catch (Exception e) {
                System.err.println("⚠️ [ESTADISTICAS_POR_PERIODO] Error obteniendo procesos: " + e.getMessage());
                porTipoProceso = new HashMap<>();
            }
            
            // Obtener estadísticas por estado en el período
            Map<String, Integer> porEstado = new HashMap<>();
            String[] estados = {"Aprobado", "Rechazado", "Enviada", "En_Proceso"};
            
            for (String estado : estados) {
                Integer cantidad = Optional.ofNullable(solicitudRepository.contarSolicitudesPorUltimoEstado(estado)).orElse(0);
                porEstado.put(estado, cantidad);
            }
            
            // Obtener estadísticas por programa en el período
            Map<String, Integer> porPrograma = new HashMap<>();
            try {
                List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
                for (String programa : nombresProgramas) {
                    // TODO: Implementar conteo real por programa y período
                    porPrograma.put(programa, 0);
                }
            } catch (Exception e) {
                System.err.println("⚠️ [ESTADISTICAS_POR_PERIODO] Error obteniendo programas: " + e.getMessage());
                porPrograma = new HashMap<>();
            }
            
            // Calcular totales para el período
            Integer totalAprobadas = porEstado.get("Aprobado");
            Integer totalRechazadas = porEstado.get("Rechazado");
            Integer totalEnProceso = porEstado.get("Enviada") + porEstado.get("En_Proceso");
            
            // Calcular porcentaje de aprobación para el período
            double porcentajeAprobacion = 0.0;
            if (totalPorPeriodo > 0) {
                porcentajeAprobacion = (double) totalAprobadas / totalPorPeriodo * 100;
            }
            
            estadisticas.put("fechaInicio", fechaInicio);
            estadisticas.put("fechaFin", fechaFin);
            estadisticas.put("totalSolicitudes", totalPorPeriodo);
            estadisticas.put("totalAprobadas", totalAprobadas);
            estadisticas.put("totalRechazadas", totalRechazadas);
            estadisticas.put("totalEnProceso", totalEnProceso);
            estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 10.0) / 10.0);
            estadisticas.put("porTipoProceso", porTipoProceso);
            estadisticas.put("porEstado", porEstado);
            estadisticas.put("porPrograma", porPrograma);
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            System.out.println("✅ [ESTADISTICAS_POR_PERIODO] Consulta completada para período");
            System.out.println("📊 [ESTADISTICAS_POR_PERIODO] Resultado final: " + estadisticas);
            
        } catch (Exception e) {
            System.err.println("❌ [ESTADISTICAS_POR_PERIODO] Error en consulta: " + e.getMessage());
            e.printStackTrace();
            // Devolver estadísticas vacías pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
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

    /**
     * Método auxiliar para normalizar nombres de procesos
     * Convierte nombres del frontend a nombres que existen en la base de datos
     */
    private String normalizarNombreProceso(String nombreProceso) {
        if (nombreProceso == null || nombreProceso.trim().isEmpty()) {
            return nombreProceso;
        }
        
        String procesoNormalizado = nombreProceso.trim();
        
        // Mapear nombres del frontend a nombres de la base de datos
        switch (procesoNormalizado.toUpperCase()) {
            case "REINGRESO":
            case "REINGRESO A PROGRAMAS":
                return "Reingreso";
            case "HOMOLOGACION":
            case "HOMOLOGACIÓN":
            case "HOMOLOGACION DE MATERIAS":
                return "Homologación";
            case "ECAES":
            case "INSCRIPCION A EXAMENES":
            case "INSCRIPCIÓN A EXÁMENES":
                return "ECAES";
            case "CURSO_VERANO":
            case "CURSOS DE VERANO":
            case "CURSOS INTERSEMESTRALES":
                return "CURSO_VERANO";
            case "PAZ_SALVO":
            case "PAZ Y SALVO":
            case "TRAMITES ADMINISTRATIVOS":
                return "PAZ_SALVO";
            default:
                // Si no coincide con ningún mapeo, devolver el nombre original
                System.out.println("⚠️ [NORMALIZAR_PROCESO] Nombre de proceso no reconocido: " + nombreProceso);
                return procesoNormalizado;
        }
    }

    /**
     * Método centralizado para normalizar estadísticas y evitar valores null
     * Asegura que todos los campos tengan valores por defecto apropiados
     */
    private Map<String, Object> normalizarEstadistica(Map<String, Object> estadistica) {
        if (estadistica == null) {
            estadistica = new HashMap<>();
        }
        
        // Normalizar totales
        estadistica.put("totalSolicitudes", estadistica.get("totalSolicitudes") != null ? estadistica.get("totalSolicitudes") : 0);
        estadistica.put("totalAprobadas", estadistica.get("totalAprobadas") != null ? estadistica.get("totalAprobadas") : 0);
        estadistica.put("totalRechazadas", estadistica.get("totalRechazadas") != null ? estadistica.get("totalRechazadas") : 0);
        estadistica.put("totalEnProceso", estadistica.get("totalEnProceso") != null ? estadistica.get("totalEnProceso") : 0);
        
        // Normalizar porcentaje
        estadistica.put("porcentajeAprobacion", estadistica.get("porcentajeAprobacion") != null ? estadistica.get("porcentajeAprobacion") : 0.0);
        
        // Normalizar mapas
        estadistica.put("porTipoProceso", estadistica.get("porTipoProceso") != null ? estadistica.get("porTipoProceso") : new HashMap<>());
        estadistica.put("porPrograma", estadistica.get("porPrograma") != null ? estadistica.get("porPrograma") : new HashMap<>());
        estadistica.put("porEstado", estadistica.get("porEstado") != null ? estadistica.get("porEstado") : new HashMap<>());
        
        // Asegurar fecha de consulta
        if (estadistica.get("fechaConsulta") == null) {
            estadistica.put("fechaConsulta", new Date());
        }
        
        return estadistica;
    }
   
}
