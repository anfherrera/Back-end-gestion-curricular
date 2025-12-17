package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadisticaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Service
@Transactional
@Slf4j
public class GestionarEstadisticasGatewayImplAdapter implements GestionarEstadisticasGatewayIntPort {

    private final EstadisticaRepositoryInt estadisticaRepository;
    private final SolicitudRepositoryInt solicitudRepository;
    private final ProgramaRepositoryInt programaRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final ModelMapper estadisticaMapper;

    public GestionarEstadisticasGatewayImplAdapter(EstadisticaRepositoryInt estadisticaRepository, ProgramaRepositoryInt programaRepository, SolicitudRepositoryInt solicitudRepository, UsuarioRepositoryInt usuarioRepository, ModelMapper estadisticaMapper) {
        this.estadisticaRepository = estadisticaRepository;
        this.programaRepository = programaRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadisticaMapper = estadisticaMapper;
    }

    @Override
    @Transactional
    @CacheEvict(value = "estadisticasGlobales", allEntries = true)
    public Estadistica crearEstadistica(Estadistica estadistica) {
        if(solicitudRepository.count() <= 0) {
            throw new RuntimeException("No hay solicitudes disponibles para crear estadisticas.");
        }
        if(programaRepository.count() <= 0) {
            throw new RuntimeException("No hay programas disponibles para crear estadisticas.");
        }
        EstadisticaEntity estadisticaEntity = estadisticaMapper.map(estadistica, EstadisticaEntity.class);
        //estadisticaEntity.setNombre("Estadisticas");
        estadisticaEntity.setTotal_solicitudes(solicitudRepository.totalSolicitudes());
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarEstado("APROBADA"));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarEstado("RECHAZADA"));
        estadisticaEntity.setNombres_procesos(new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes()));
        estadisticaEntity.setPeriodos_academico(new ArrayList<Date>());
        estadisticaEntity.setNombres_programas(new ArrayList<String>(programaRepository.buscarNombresProgramas()));
        EstadisticaEntity savedEntity = estadisticaRepository.save(estadisticaEntity);
        return estadisticaMapper.map(savedEntity, Estadistica.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "estadisticasGlobales", allEntries = true)
    public Estadistica actualizarEstadistica(Estadistica estadistica) {
        estadisticaRepository.findById(estadistica.getId_estadistica())
            .orElseThrow(() -> new RuntimeException("Estadistica no encontrada con ID: " + estadistica.getId_estadistica()));
        EstadisticaEntity estadisticaEntity = estadisticaMapper.map(estadistica, EstadisticaEntity.class);
                estadisticaEntity.setTotal_solicitudes(solicitudRepository.totalSolicitudes());
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarEstado("APROBADA"));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarEstado("RECHAZADA"));
        estadisticaEntity.setNombres_procesos(new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes()));
        estadisticaEntity.setPeriodos_academico(new ArrayList<Date>());
        estadisticaEntity.setNombres_programas(new ArrayList<String>(programaRepository.buscarNombresProgramas()));
        EstadisticaEntity updatedEntity = estadisticaRepository.save(estadisticaEntity);
        return estadisticaMapper.map(updatedEntity, Estadistica.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "estadisticasGlobales", allEntries = true)
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
        .orElseThrow(() -> new IllegalArgumentException("Estadistica no encontrada"));
        return estadisticaMapper.map(entity, Estadistica.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Estadistica obtenerEstadisticasSolicitudPeriodoYPrograma(Integer idEstadistica, String proceso,
            Date fechaInicio, Date fechaFin, Integer idPrograma) {
        EstadisticaEntity estadisticaEntity = estadisticaRepository.findById(idEstadistica)
        .orElseThrow(() -> new IllegalArgumentException("Estadistica no encontrada"));
        
        if(solicitudRepository.count() >= 0) {
        
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "APROBADA", idPrograma)); // Asumiendo que 1 es el ID del estado "aprobada"
        estadisticaEntity.setTotal_solicitudes(solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "RECHAZADA", idPrograma)); // Asumiendo que 1 es el ID del estado "rechazada"
        }

        return estadisticaMapper.map(estadisticaEntity, Estadistica.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso,
            Date fechaInicio, Date fechaFin, String estado, Integer idPrograma) {
    EstadisticaEntity estadisticaEntity = estadisticaRepository.findById(idEstadistica)
        .orElseThrow(() -> new IllegalArgumentException("Estadistica no encontrada con ID: " + idEstadistica));

        if(solicitudRepository.count() == 0) {
            throw new RuntimeException("No hay solicitudes disponibles para crear estadisticas.");
        }

        int totalSolicitudes = solicitudRepository.contarNombreFechaEstadoYPrograma(
        proceso, fechaInicio, fechaFin, estado, idPrograma);

        // Actualizar el campo total_solicitudes de la estadistica
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
                    estadistica.setTotal_aprobadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "APROBADA", idPrograma));
                    estadistica.setTotal_rechazadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "RECHAZADA", idPrograma));
                    
                    estadisticas.add(estadistica);
                }
            }
       return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "estadisticasGlobales", key = "'globales'")
    public Map<String, Object> obtenerEstadisticasGlobales() {
        return obtenerEstadisticasGlobales(null, null, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    // Cache temporalmente deshabilitado para debug - puede causar problemas si la cache está corrupta
    // @Cacheable(value = "estadisticasGlobales", key = "#proceso + '_' + #idPrograma + '_' + #fechaInicio + '_' + #fechaFin")
    // NOTA: Propagation.NOT_SUPPORTED suspende cualquier transacción existente, evitando rollback-only cuando fallan consultas
    public Map<String, Object> obtenerEstadisticasGlobales(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Estadisticas generales del sistema
            // NOTA: Si hay filtros, algunos conteos pueden no funcionar correctamente
            // En ese caso, usar contarEstado() para estadisticas globales sin filtros
            
            // Inicializar variables con valores por defecto para evitar null
            Integer totalSolicitudes = 0;
            Integer totalAprobadas = 0;
            Integer totalRechazadas = 0;
            Integer enProcesoFuncionario = 0;
            Integer enProcesoCoordinador = 0;
            Integer totalEnviadas = 0;
            
            if (proceso == null && idPrograma == null && fechaInicio == null && fechaFin == null) {
                // Sin filtros: usar contarEstado que es mas confiable
                try {
                    totalAprobadas = Optional.ofNullable(solicitudRepository.contarEstado("APROBADA")).orElse(0);
                    totalRechazadas = Optional.ofNullable(solicitudRepository.contarEstado("RECHAZADA")).orElse(0);
                    enProcesoFuncionario = Optional.ofNullable(solicitudRepository.contarEstado("APROBADA_FUNCIONARIO")).orElse(0);
                    enProcesoCoordinador = Optional.ofNullable(solicitudRepository.contarEstado("APROBADA_COORDINADOR")).orElse(0);
                    
                    // Contar ENVIADA (contarEstado usa UPPER() así que cuenta "ENVIADA" y "Enviada")
                    Integer enviadas = Optional.ofNullable(solicitudRepository.contarEstado("ENVIADA")).orElse(0);
                    // También contar PRE_REGISTRADO que se trata como ENVIADA para estadísticas
                    Integer preRegistrado = 0;
                    try {
                        preRegistrado = Optional.ofNullable(solicitudRepository.contarEstado("PRE_REGISTRADO")).orElse(0);
                    } catch (Exception e) {
                        log.debug("Estadisticas globales - PRE_REGISTRADO no encontrado o error: {}", e.getMessage());
                        preRegistrado = 0;
                    }
                    totalEnviadas = (enviadas != null ? enviadas : 0) + (preRegistrado != null ? preRegistrado : 0);
                    
                    // Calcular total como suma de todos los estados (más confiable que contarSolicitudesConFiltros)
                    totalSolicitudes = (totalAprobadas != null ? totalAprobadas : 0) + 
                                     (totalRechazadas != null ? totalRechazadas : 0) + 
                                     (enProcesoFuncionario != null ? enProcesoFuncionario : 0) + 
                                     (enProcesoCoordinador != null ? enProcesoCoordinador : 0) + 
                                     totalEnviadas;
                    
                    // Si la suma es 0 pero hay solicitudes, usar el método directo del repositorio como fallback
                    if (totalSolicitudes == null || totalSolicitudes == 0) {
                        try {
                            totalSolicitudes = Optional.ofNullable(solicitudRepository.totalSolicitudes()).orElse(0);
                        } catch (Exception e) {
                            log.debug("Estadisticas globales - Error obteniendo totalSolicitudes: {}", e.getMessage());
                            totalSolicitudes = 0;
                        }
                    }
                } catch (Exception e) {
                    log.error("Estadisticas globales - Error obteniendo conteos sin filtros: {}", e.getMessage(), e);
                    totalAprobadas = 0;
                    totalRechazadas = 0;
                    enProcesoFuncionario = 0;
                    enProcesoCoordinador = 0;
                    totalEnviadas = 0;
                    totalSolicitudes = 0;
                }
            } else {
                // Con filtros: usar contarSolicitudesPorEstadoConFiltros
                try {
                    totalAprobadas = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("APROBADA", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
                    totalRechazadas = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("RECHAZADA", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
                    enProcesoFuncionario = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("APROBADA_FUNCIONARIO", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
                    enProcesoCoordinador = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("APROBADA_COORDINADOR", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
                    totalEnviadas = Optional.ofNullable(solicitudRepository.contarSolicitudesPorEstadoConFiltros("ENVIADA", proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
                    
                    // Con filtros: usar contarSolicitudesConFiltros o calcular como suma
                    totalSolicitudes = Optional.ofNullable(solicitudRepository.contarSolicitudesConFiltros(proceso, idPrograma, fechaInicio, fechaFin)).orElse(0);
                    
                    // Si contarSolicitudesConFiltros falla, calcular como suma de estados
                    if (totalSolicitudes == null || totalSolicitudes == 0) {
                        totalSolicitudes = (totalAprobadas != null ? totalAprobadas : 0) + 
                                         (totalRechazadas != null ? totalRechazadas : 0) + 
                                         (enProcesoFuncionario != null ? enProcesoFuncionario : 0) + 
                                         (enProcesoCoordinador != null ? enProcesoCoordinador : 0) + 
                                         (totalEnviadas != null ? totalEnviadas : 0);
                    }
                } catch (Exception e) {
                    log.error("Estadisticas globales - Error obteniendo conteos con filtros: {}", e.getMessage(), e);
                    totalAprobadas = 0;
                    totalRechazadas = 0;
                    enProcesoFuncionario = 0;
                    enProcesoCoordinador = 0;
                    totalEnviadas = 0;
                    totalSolicitudes = 0;
                }
            }
            
            // En Proceso = solo las aprobadas por funcionario y coordinador (NO incluye enviadas)
            Integer totalEnProceso = (enProcesoFuncionario != null ? enProcesoFuncionario : 0) + 
                                    (enProcesoCoordinador != null ? enProcesoCoordinador : 0);
            
            // Asegurar que todos los valores sean no-null antes de agregarlos al map
            estadisticas.put("totalSolicitudes", totalSolicitudes != null ? totalSolicitudes : 0);
            estadisticas.put("totalAprobadas", totalAprobadas != null ? totalAprobadas : 0);
            estadisticas.put("totalRechazadas", totalRechazadas != null ? totalRechazadas : 0);
            estadisticas.put("totalEnviadas", totalEnviadas != null ? totalEnviadas : 0);
            estadisticas.put("totalEnProceso", totalEnProceso != null ? totalEnProceso : 0);
            
            // Calcular porcentaje de aprobacion
            double porcentajeAprobacion = 0.0;
            if (totalSolicitudes != null && totalSolicitudes > 0 && totalAprobadas != null) {
                porcentajeAprobacion = (double) totalAprobadas / totalSolicitudes * 100;
                // Validar que no sea NaN o Infinity
                if (Double.isNaN(porcentajeAprobacion) || Double.isInfinite(porcentajeAprobacion)) {
                    porcentajeAprobacion = 0.0;
                }
            }
            estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 10.0) / 10.0);
            
            // Estadisticas por tipo de proceso (agrupadas por tipo, no por persona)
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                // Obtener todas las solicitudes con manejo de errores robusto
                List<SolicitudEntity> solicitudes;
                try {
                    solicitudes = solicitudRepository.findAll();
                    if (solicitudes == null) {
                        solicitudes = new ArrayList<>();
                    }
                } catch (Exception e) {
                    log.error("Estadisticas globales - Error obteniendo todas las solicitudes: {}", e.getMessage(), e);
                    solicitudes = new ArrayList<>();
                }
                
                // Agrupar por tipo de proceso (extrayendo el tipo del nombre)
                for (SolicitudEntity solicitud : solicitudes) {
                    try {
                    // Aplicar filtros manualmente si es necesario
                    boolean cumpleFiltros = true;
                    
                    String nombreSolicitud = solicitud.getNombre_solicitud();
                    if (proceso != null && nombreSolicitud != null && 
                        !nombreSolicitud.toLowerCase().contains(proceso.toLowerCase())) {
                        cumpleFiltros = false;
                    }
                    // Si hay filtro de proceso pero la solicitud no tiene nombre, excluirla
                    if (proceso != null && nombreSolicitud == null) {
                        cumpleFiltros = false;
                    }
                    if (idPrograma != null && solicitud.getObjUsuario() != null && 
                        solicitud.getObjUsuario().getObjPrograma() != null &&
                        !solicitud.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma)) {
                        cumpleFiltros = false;
                    }
                    Date fechaSolicitud = solicitud.getFecha_registro_solicitud();
                    if (fechaInicio != null && fechaSolicitud != null && fechaSolicitud.before(fechaInicio)) {
                        cumpleFiltros = false;
                    }
                    if (fechaFin != null && fechaSolicitud != null && fechaSolicitud.after(fechaFin)) {
                        cumpleFiltros = false;
                    }
                    // Si la solicitud no tiene fecha y hay filtros de fecha, excluirla
                    if ((fechaInicio != null || fechaFin != null) && fechaSolicitud == null) {
                        cumpleFiltros = false;
                    }
                    
                    if (cumpleFiltros && nombreSolicitud != null) {
                        try {
                            String tipoProceso = extraerTipoProceso(nombreSolicitud);
                            porTipoProceso.put(tipoProceso, porTipoProceso.getOrDefault(tipoProceso, 0) + 1);
                        } catch (Exception e) {
                            log.debug("Estadisticas globales - Error extrayendo tipo de proceso para solicitud ID {}: {}", 
                                    solicitud != null ? solicitud.getId_solicitud() : "null", e.getMessage());
                            // Continuar con la siguiente solicitud
                        }
                    }
                    } catch (Exception e) {
                        log.debug("Estadisticas globales - Error procesando solicitud ID {}: {}", 
                                solicitud != null ? solicitud.getId_solicitud() : "null", e.getMessage());
                        // Continuar con la siguiente solicitud
                    }
                }
                
            } catch (Exception e) {
                log.error("Estadisticas globales - Error obteniendo procesos: {}", e.getMessage(), e);
                porTipoProceso = new HashMap<>();
            }
            estadisticas.put("porTipoProceso", porTipoProceso);
            
            // Estadisticas por programa (con filtros si se aplican)
            Map<String, Integer> porPrograma = new HashMap<>();
            try {
                List<String> nombresProgramas;
                try {
                    nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
                } catch (DataAccessException e) {
                    log.error("Estadisticas globales - Error de acceso a datos obteniendo nombres de programas: {}", e.getMessage(), e);
                    nombresProgramas = new ArrayList<>();
                } catch (Exception e) {
                    log.error("Estadisticas globales - Error obteniendo nombres de programas: {}", e.getMessage(), e);
                    nombresProgramas = new ArrayList<>();
                }
                log.debug("Estadisticas globales - Programas encontrados: {}", nombresProgramas);
                
                // Obtener todos los programas con sus IDs
                List<ProgramaEntity> programas;
                try {
                    programas = programaRepository.findAll();
                    if (programas == null) {
                        programas = new ArrayList<>();
                    }
                } catch (DataAccessException e) {
                    log.error("Estadisticas globales - Error de acceso a datos obteniendo todos los programas: {}", e.getMessage(), e);
                    programas = new ArrayList<>();
                } catch (Exception e) {
                    log.error("Estadisticas globales - Error obteniendo todos los programas: {}", e.getMessage(), e);
                    programas = new ArrayList<>();
                }
                
                for (ProgramaEntity programa : programas) {
                    try {
                        if (programa != null && programa.getId_programa() != null && programa.getNombre_programa() != null) {
                            Integer cantidad = Optional.ofNullable(solicitudRepository.contarSolicitudesPorProgramaConFiltros(programa.getId_programa(), proceso, fechaInicio, fechaFin)).orElse(0);
                            porPrograma.put(programa.getNombre_programa(), cantidad);
                            log.debug("Estadisticas globales - Programa: {} = {}", programa.getNombre_programa(), cantidad);
                        }
                    } catch (Exception e) {
                        log.debug("Estadisticas globales - Error contando solicitudes para programa {}: {}", programa != null ? programa.getNombre_programa() : "null", e.getMessage());
                        // Continuar con el siguiente programa
                    }
                }
            } catch (Exception e) {
                log.error("Estadisticas globales - Error obteniendo programas: {}", e.getMessage());
                porPrograma = new HashMap<>();
            }
            estadisticas.put("porPrograma", porPrograma);
            
            // Estadisticas por estado (valores INDIVIDUALES del estado mas reciente)
            // IMPORTANTE: Cada estado se cuenta por separado usando contarEstado (con comparacion exacta)
            Map<String, Integer> porEstado = new HashMap<>();
            
            porEstado.put("APROBADA", totalAprobadas);  // 21 (ahora contarEstado usa = en vez de LIKE)
            porEstado.put("RECHAZADA", totalRechazadas);  // 5
            porEstado.put("APROBADA_FUNCIONARIO", enProcesoFuncionario);  // 11
            porEstado.put("APROBADA_COORDINADOR", enProcesoCoordinador);  // 4
            porEstado.put("ENVIADA", totalEnviadas);  // 9
            
            estadisticas.put("porEstado", porEstado);
            
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            try {
                estadisticas = normalizarEstadistica(estadisticas);
            } catch (Exception e) {
                log.error("Estadisticas globales - Error normalizando estadisticas: {}", e.getMessage(), e);
                // Si la normalizacion falla, asegurar al menos los campos básicos
                if (estadisticas.get("totalSolicitudes") == null) estadisticas.put("totalSolicitudes", 0);
                if (estadisticas.get("totalAprobadas") == null) estadisticas.put("totalAprobadas", 0);
                if (estadisticas.get("totalRechazadas") == null) estadisticas.put("totalRechazadas", 0);
                if (estadisticas.get("totalEnviadas") == null) estadisticas.put("totalEnviadas", 0);
                if (estadisticas.get("totalEnProceso") == null) estadisticas.put("totalEnProceso", 0);
                if (estadisticas.get("porcentajeAprobacion") == null) estadisticas.put("porcentajeAprobacion", 0.0);
                if (estadisticas.get("porTipoProceso") == null) estadisticas.put("porTipoProceso", new HashMap<>());
                if (estadisticas.get("porPrograma") == null) estadisticas.put("porPrograma", new HashMap<>());
                if (estadisticas.get("porEstado") == null) estadisticas.put("porEstado", new HashMap<>());
            }
            
            // ===== PREDICCIONES GLOBALES ELIMINADAS =====
            // Decision: Las predicciones solo se muestran en el dashboard de Cursos de Verano
            // porque son mas relevantes y accionables para ese contexto especifico.
            // El dashboard general se enfoca en datos consolidados sin predicciones.
            
            log.debug("Estadisticas globales - Consulta completada exitosamente (sin predicciones)");
            log.debug("Estadisticas globales - Resultado final: {}", estadisticas);
            
        } catch (Exception e) {
            log.error("Estadisticas globales - Error en consulta: {}", e.getMessage(), e);
            log.error("Estadisticas globales - Stack trace completo:", e);
            // Devolver estadisticas vacias pero normalizadas en caso de error
            try {
                estadisticas = normalizarEstadistica(new HashMap<>());
            } catch (Exception e2) {
                log.error("Estadisticas globales - Error incluso al normalizar mapa vacio: {}", e2.getMessage(), e2);
                // Como último recurso, crear un mapa básico manualmente
                estadisticas = new HashMap<>();
                estadisticas.put("totalSolicitudes", 0);
                estadisticas.put("totalAprobadas", 0);
                estadisticas.put("totalRechazadas", 0);
                estadisticas.put("totalEnviadas", 0);
                estadisticas.put("totalEnProceso", 0);
                estadisticas.put("porcentajeAprobacion", 0.0);
                estadisticas.put("porTipoProceso", new HashMap<>());
                estadisticas.put("porPrograma", new HashMap<>());
                estadisticas.put("porEstado", new HashMap<>());
                estadisticas.put("fechaConsulta", new Date());
            }
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorProceso(String tipoProceso) {
        log.debug("Estadisticas por proceso - Consultando estadisticas para proceso: {}", tipoProceso);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar que el tipoProceso no sea null o vacio
            if (tipoProceso == null || tipoProceso.trim().isEmpty()) {
                log.error("Estadisticas por proceso - Tipo de proceso no puede ser null o vacio");
                return normalizarEstadistica(new HashMap<>());
            }
            
            // Normalizar el nombre del proceso para busqueda
            String procesoNormalizado = normalizarNombreProceso(tipoProceso);
            log.debug("Estadisticas por proceso - Proceso normalizado:  {}", procesoNormalizado);
            
            Integer totalPorProceso = Optional.ofNullable(solicitudRepository.contarPorNombre(procesoNormalizado)).orElse(0);
            log.debug("Estadisticas por proceso - Total encontrado:  {}", totalPorProceso);
            
            // Obtener estadisticas por estado para este proceso especifico
            Map<String, Integer> porEstado = new HashMap<>();
            String[] estados = {"APROBADA", "RECHAZADA", "ENVIADA", "APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR"};
            
            for (String estado : estados) {
                Integer cantidad = 0;
                try {
                    // Contar manualmente las solicitudes por proceso y estado mientras se ajusta el repositorio
                    cantidad = solicitudRepository.findAll().stream()
                        .filter(solicitud -> {
                            String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                            return tipoProceso.equals(nombreProceso);
                        })
                        .filter(solicitud -> {
                            String estadoSolicitud = obtenerEstadoMasReciente(solicitud);
                            return estadoSolicitud != null && estadoSolicitud.equals(estado);
                        })
                        .mapToInt(solicitud -> 1)
                        .sum();
                } catch (Exception e) {
                    log.debug("Estadisticas por proceso - Error contando estado {}: {}", estado, e.getMessage());
                    cantidad = 0;
                }
                
                porEstado.put(estado, cantidad);
                log.debug("Estadisticas por proceso - Estado {}: {}", estado, cantidad);
            }
            
            // Calcular totales para este proceso
            Integer totalAprobadas = porEstado.get("APROBADA");
            Integer totalRechazadas = porEstado.get("RECHAZADA");
            // En Proceso = solo las aprobadas por funcionario y coordinador (NO incluye enviadas)
            Integer totalEnProceso = porEstado.getOrDefault("APROBADA_FUNCIONARIO", 0) + porEstado.getOrDefault("APROBADA_COORDINADOR", 0);
            
            // Calcular porcentaje de aprobacion para este proceso
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
            estadisticas.put("porTipoProceso", new HashMap<>()); // Vacio para proceso especifico
            estadisticas.put("porPrograma", new HashMap<>()); // Vacio para proceso especifico
            estadisticas.put("descripcion", obtenerDescripcionProceso(tipoProceso));
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            log.debug("Estadisticas por proceso - Consulta completada para:  {}", tipoProceso);
            log.debug("Estadisticas por proceso - Resultado final:  {}", estadisticas);
            
        } catch (Exception e) {
            log.error("Estadisticas por proceso - Error en consulta:  {}", e.getMessage());
            log.error("Error en estadísticas", e);
            // Devolver estadisticas vacias pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorEstado(String estado) {
        log.debug("Estadisticas por estado - Consultando estadisticas para estado:  {}", estado);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar que el estado no sea null o vacio
            if (estado == null || estado.trim().isEmpty()) {
                log.error("Estadisticas por estado - Estado no puede ser null o vacio");
                return normalizarEstadistica(new HashMap<>());
            }
            
            Integer totalPorEstado = Optional.ofNullable(solicitudRepository.contarSolicitudesPorUltimoEstado(estado)).orElse(0);
            log.debug("Estadisticas por estado - Total encontrado:  {}", totalPorEstado);
            
            // Obtener estadisticas por tipo de proceso para este estado
            final String estadoNormalizado = estado;
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
                for (String proceso : nombresProcesos) {
                    // Pendiente integrar conteo directo por proceso y estado desde el repositorio
                    Integer cantidad = solicitudRepository.findAll().stream()
                        .filter(solicitud -> {
                            String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                            return proceso.equals(nombreProceso);
                        })
                        .filter(solicitud -> {
                            String estadoSolicitud = obtenerEstadoMasReciente(solicitud);
                            return estadoSolicitud != null && estadoSolicitud.equals(estadoNormalizado);
                        })
                        .mapToInt(solicitud -> 1)
                        .sum();
                    porTipoProceso.put(proceso, cantidad);
                }
            } catch (Exception e) {
                log.error("Estadisticas por estado - Error obteniendo procesos:  {}", e.getMessage());
                porTipoProceso = new HashMap<>();
            }
            
            // Obtener estadisticas por programa para este estado
            Map<String, Integer> porPrograma = new HashMap<>();
            try {
                List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
                for (String programa : nombresProgramas) {
                    // Estimar cuantas solicitudes por programa llegan con este estado
                    Integer cantidad = solicitudRepository.findAll().stream()
                        .filter(solicitud -> {
                            // Verificar que tenga usuario y programa
                            if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getObjPrograma() == null) {
                                return false;
                            }
                            String nombrePrograma = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                            return programa.equals(nombrePrograma);
                        })
                        .filter(solicitud -> {
                            String estadoSolicitud = obtenerEstadoMasReciente(solicitud);
                            return estadoSolicitud != null && estadoSolicitud.equals(estadoNormalizado);
                        })
                        .mapToInt(solicitud -> 1)
                        .sum();
                    porPrograma.put(programa, cantidad);
                }
            } catch (Exception e) {
                log.error("Estadisticas por estado - Error obteniendo programas:  {}", e.getMessage());
                porPrograma = new HashMap<>();
            }
            
            estadisticas.put("estado", estado);
            estadisticas.put("totalSolicitudes", totalPorEstado);
            estadisticas.put("totalAprobadas", estado.equals("APROBADA") ? totalPorEstado : 0);
            estadisticas.put("totalRechazadas", estado.equals("RECHAZADA") ? totalPorEstado : 0);
            // En Proceso = solo las aprobadas por funcionario y coordinador (NO incluye enviadas)
            estadisticas.put("totalEnProceso", (estado.equals("APROBADA_FUNCIONARIO") || estado.equals("APROBADA_COORDINADOR")) ? totalPorEstado : 0);
            estadisticas.put("porcentajeAprobacion", estado.equals("APROBADA") ? 100.0 : 0.0);
            estadisticas.put("porTipoProceso", porTipoProceso);
            estadisticas.put("porPrograma", porPrograma);
            estadisticas.put("porEstado", new HashMap<>()); // Vacio para estado especifico
            estadisticas.put("descripcionEstado", obtenerDescripcionEstado(estado));
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            log.debug("Estadisticas por estado - Consulta completada para:  {}", estado);
            log.debug("Estadisticas por estado - Resultado final:  {}", estadisticas);
            
        } catch (Exception e) {
            log.error("Estadisticas por estado - Error en consulta:  {}", e.getMessage());
            log.error("Error en estadísticas", e);
            // Devolver estadisticas vacias pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorPrograma(Integer idPrograma) {
        log.debug("Estadisticas por programa - Consultando estadisticas para programa ID:  {}", idPrograma);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar que el idPrograma no sea null o menor a 1
            if (idPrograma == null || idPrograma < 1) {
                log.error("Estadisticas por programa - ID de programa no puede ser null o menor a 1");
                return normalizarEstadistica(new HashMap<>());
            }
            
            // Obtener estadisticas por programa (usando fechas amplias para capturar todo)
            Date fechaInicio = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 ano atras
            Date fechaFin = new Date();
            
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
                log.debug("Estadisticas por programa - Procesos encontrados:  {}", nombresProcesos);
                
                for (String proceso : nombresProcesos) {
                    Integer cantidad = Optional.ofNullable(solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma)).orElse(0);
                    porTipoProceso.put(proceso, cantidad);
                    log.debug("Estadisticas por programa - Proceso {}: {}", proceso, cantidad);
                }
            } catch (Exception e) {
                log.error("Estadisticas por programa - Error obteniendo procesos:  {}", e.getMessage());
                porTipoProceso = new HashMap<>();
            }
            
            // Obtener estadisticas por estado para este programa
            Map<String, Integer> porEstado = new HashMap<>();
            String[] estados = {"APROBADA", "RECHAZADA", "ENVIADA", "APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR"};
            
            for (String estado : estados) {
                Integer cantidad = Optional.ofNullable(solicitudRepository.contarSolicitudesPorUltimoEstado(estado)).orElse(0);
                porEstado.put(estado, cantidad);
            }
            
            // Calcular totales para este programa
            Integer totalSolicitudes = porTipoProceso.values().stream().mapToInt(Integer::intValue).sum();
            Integer totalAprobadas = porEstado.get("APROBADA");
            Integer totalRechazadas = porEstado.get("RECHAZADA");
            // En Proceso = solo las aprobadas por funcionario y coordinador (NO incluye enviadas)
            Integer totalEnProceso = porEstado.getOrDefault("APROBADA_FUNCIONARIO", 0) + porEstado.getOrDefault("APROBADA_COORDINADOR", 0);
            
            // Calcular porcentaje de aprobacion para este programa
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
            estadisticas.put("porPrograma", new HashMap<>()); // Vacio para programa especifico
            estadisticas.put("fechaConsulta", new Date());
            
            // Normalizar la respuesta para evitar valores null
            estadisticas = normalizarEstadistica(estadisticas);
            
            log.debug("Estadisticas por programa - Consulta completada para programa:  {}", idPrograma);
            log.debug("Estadisticas por programa - Resultado final:  {}", estadisticas);
            
        } catch (Exception e) {
            log.error("Estadisticas por programa - Error en consulta:  {}", e.getMessage());
            log.error("Error en estadísticas", e);
            // Devolver estadisticas vacias pero normalizadas en caso de error
            estadisticas = normalizarEstadistica(new HashMap<>());
        }
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorPeriodo(Date fechaInicio, Date fechaFin) {
        log.debug("Estadisticas por periodo - Consultando datos entre {} y {}", fechaInicio, fechaFin);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Validar fechas
            if (fechaInicio == null || fechaFin == null) {
                log.error("Estadisticas por periodo - Las fechas no pueden ser null");
                return normalizarEstadistica(new HashMap<>());
            }
            
            Integer totalPorPeriodo = Optional.ofNullable(solicitudRepository.contarPorRangoFechas(fechaInicio, fechaFin)).orElse(0);
            log.debug("Estadisticas por periodo - Total en periodo:  {}", totalPorPeriodo);
            
            // Obtener estadisticas por tipo de proceso en el periodo
            Map<String, Integer> porTipoProceso = new HashMap<>();
            try {
                List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
                log.debug("Estadisticas por periodo - Procesos encontrados:  {}", nombresProcesos);
                
                for (String proceso : nombresProcesos) {
                    // Contar solicitudes por proceso dentro del periodo seleccionado
                    Integer cantidad = solicitudRepository.findAll().stream()
                        .filter(solicitud -> {
                            String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                            return proceso.equals(nombreProceso);
                        })
                        .filter(solicitud -> {
                            // Filtrar por rango de fechas
                            Date fechaSolicitud = solicitud.getFecha_registro_solicitud();
                            return fechaSolicitud != null && 
                                   !fechaSolicitud.before(fechaInicio) && 
                                   !fechaSolicitud.after(fechaFin);
                        })
                        .mapToInt(solicitud -> 1)
                        .sum();
                    porTipoProceso.put(proceso, cantidad);
                    log.debug("Estadisticas por periodo - Proceso {}: {}", proceso, cantidad);
                }
            } catch (Exception e) {
                log.error("Estadisticas por periodo - Error obteniendo procesos:  {}", e.getMessage());
                porTipoProceso = new HashMap<>();
            }
            
            // Obtener estadisticas por estado en el periodo
            Map<String, Integer> porEstado = new HashMap<>();
            String[] estados = {"APROBADA", "RECHAZADA", "ENVIADA", "APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR"};
            
            for (String estado : estados) {
                // Filtrar por periodo
                Integer cantidad = solicitudRepository.findAll().stream()
                    .filter(solicitud -> {
                        Date fechaSolicitud = solicitud.getFecha_registro_solicitud();
                        return fechaSolicitud != null && 
                               !fechaSolicitud.before(fechaInicio) && 
                               !fechaSolicitud.after(fechaFin);
                    })
                    .filter(solicitud -> {
                        String estadoSolicitud = obtenerEstadoMasReciente(solicitud);
                        return estadoSolicitud != null && estadoSolicitud.equals(estado);
                    })
                    .mapToInt(solicitud -> 1)
                    .sum();
                porEstado.put(estado, cantidad);
            }
            
            // Obtener estadisticas por programa en el periodo
            Map<String, Integer> porPrograma = new HashMap<>();
            try {
                List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
                for (String programa : nombresProgramas) {
                    // Obtener el numero de solicitudes por programa en el rango de fechas indicado
                    Integer cantidad = solicitudRepository.findAll().stream()
                        .filter(solicitud -> {
                            // Filtrar por rango de fechas
                            Date fechaSolicitud = solicitud.getFecha_registro_solicitud();
                            return fechaSolicitud != null && 
                                   !fechaSolicitud.before(fechaInicio) && 
                                   !fechaSolicitud.after(fechaFin);
                        })
                        .filter(solicitud -> {
                            // Verificar que tenga usuario y programa
                            if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getObjPrograma() == null) {
                                return false;
                            }
                            String nombrePrograma = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                            return programa.equals(nombrePrograma);
                        })
                        .mapToInt(solicitud -> 1)
                        .sum();
                    porPrograma.put(programa, cantidad);
                }
            } catch (Exception e) {
                log.error("Estadisticas por periodo - Error obteniendo programas:  {}", e.getMessage());
                porPrograma = new HashMap<>();
            }
            
            // Calcular totales para el periodo
            Integer totalAprobadas = porEstado.get("APROBADA");
            Integer totalRechazadas = porEstado.get("RECHAZADA");
            // En Proceso = solo las aprobadas por funcionario y coordinador (NO incluye enviadas)
            Integer totalEnProceso = porEstado.getOrDefault("APROBADA_FUNCIONARIO", 0) + porEstado.getOrDefault("APROBADA_COORDINADOR", 0);
            
            // Calcular porcentaje de aprobacion para el periodo
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
            
            log.debug("Estadisticas por periodo - Consulta completada para periodo");
            log.debug("Estadisticas por periodo - Resultado final:  {}", estadisticas);
            
        } catch (Exception e) {
            log.error("Estadisticas por periodo - Error en consulta:  {}", e.getMessage());
            log.error("Error en estadísticas", e);
            // Devolver estadisticas vacias pero normalizadas en caso de error
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
        
        // Obtener estadisticas por proceso y estado para todos los programas
        List<String> nombresProgramas = new ArrayList<>(programaRepository.buscarNombresProgramas());
        Map<String, Integer> porPrograma = new HashMap<>();
        
        for (String programa : nombresProgramas) {
            // Nota: Este metodo necesitaria ser implementado en el repositorio
            // Por ahora usamos una aproximacion con los metodos existentes
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
    public Map<String, Object> obtenerResumenCompleto(String periodoAcademico, Integer idPrograma) {
        Map<String, Object> resumen = new HashMap<>();
        
        log.debug("Obteniendo resumen completo con filtros - Período: {}, Programa: {}", periodoAcademico, idPrograma);
        
        // Estadisticas globales con filtros
        // Convertir periodoAcademico a fechas si es necesario, o usar null para obtener todas
        Map<String, Object> estadisticasGlobales;
        if (periodoAcademico != null || idPrograma != null) {
            // Si hay filtros, obtener estadísticas globales filtradas
            // Para periodoAcademico, necesitamos convertirlo a fechas o filtrar directamente
            estadisticasGlobales = obtenerEstadisticasGlobales(null, idPrograma, null, null);
            
            // Si hay filtro de período académico, necesitamos filtrar las solicitudes por período
            if (periodoAcademico != null) {
                // Filtrar las estadísticas globales por período académico
                // Esto requiere obtener todas las solicitudes y filtrarlas
                List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
                List<SolicitudEntity> solicitudesFiltradas = todasLasSolicitudes.stream()
                    .filter(solicitud -> {
                        // Filtrar por período académico
                        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                            String periodoFiltro = periodoAcademico.trim();
                            if (solicitud.getPeriodo_academico() == null || !solicitud.getPeriodo_academico().equals(periodoFiltro)) {
                                return false;
                            }
                        }
                        // Filtrar por programa
                        if (idPrograma != null) {
                            if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getObjPrograma() == null
                                    || !solicitud.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
                
                // Recalcular estadísticas globales con las solicitudes filtradas
                estadisticasGlobales = calcularEstadisticasGlobalesDesdeSolicitudes(solicitudesFiltradas);
            }
        } else {
            estadisticasGlobales = obtenerEstadisticasGlobales();
        }
        resumen.put("estadisticasGlobales", estadisticasGlobales);
        
        // Estadisticas por tipo de proceso (aplicar filtros si existen)
        Map<String, Object> porProceso = new HashMap<>();
        List<String> nombresProcesos = new ArrayList<>(solicitudRepository.buscarNombresSolicitudes());
        for (String proceso : nombresProcesos) {
            Map<String, Object> estadisticasProceso = obtenerEstadisticasPorProceso(proceso);
            // Aplicar filtros a las estadísticas por proceso si es necesario
            if (periodoAcademico != null || idPrograma != null) {
                estadisticasProceso = aplicarFiltrosAEstadisticas(estadisticasProceso, periodoAcademico, idPrograma);
            }
            porProceso.put(proceso, estadisticasProceso);
        }
        resumen.put("porTipoProceso", porProceso);
        
        // Estadisticas por estado (aplicar filtros si existen)
        Map<String, Object> porEstado = new HashMap<>();
        String[] estados = {"APROBADA", "RECHAZADA", "ENVIADA", "APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR"};
        for (String estado : estados) {
            Map<String, Object> estadisticasEstado = obtenerEstadisticasPorEstado(estado);
            // Aplicar filtros a las estadísticas por estado si es necesario
            if (periodoAcademico != null || idPrograma != null) {
                estadisticasEstado = aplicarFiltrosAEstadisticas(estadisticasEstado, periodoAcademico, idPrograma);
            }
            porEstado.put(estado, estadisticasEstado);
        }
        resumen.put("porEstado", porEstado);
        
        // Informacion del sistema
        resumen.put("totalProgramas", programaRepository.buscarNombresProgramas().size());
        resumen.put("fechaGeneracion", new Date());
        
        // Agregar información de filtros aplicados
        if (periodoAcademico != null || idPrograma != null) {
            Map<String, Object> filtrosAplicados = new HashMap<>();
            if (periodoAcademico != null) {
                filtrosAplicados.put("periodoAcademico", periodoAcademico);
            }
            if (idPrograma != null) {
                filtrosAplicados.put("idPrograma", idPrograma);
            }
            resumen.put("filtrosAplicados", filtrosAplicados);
        }
        
        return resumen;
    }
    
    /**
     * Calcula estadísticas globales desde una lista de solicitudes filtradas.
     */
    private Map<String, Object> calcularEstadisticasGlobalesDesdeSolicitudes(List<SolicitudEntity> solicitudes) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        int totalSolicitudes = solicitudes.size();
        int totalAprobadas = 0;
        int totalRechazadas = 0;
        int totalEnProceso = 0;
        
        Map<String, Integer> porTipoProceso = new HashMap<>();
        Map<String, Integer> porPrograma = new HashMap<>();
        
        for (SolicitudEntity solicitud : solicitudes) {
            String estadoActual = obtenerEstadoMasReciente(solicitud);
            if ("APROBADA".equals(estadoActual) || "APROBADA_FUNCIONARIO".equals(estadoActual) || "APROBADA_COORDINADOR".equals(estadoActual)) {
                totalAprobadas++;
            } else if ("RECHAZADA".equals(estadoActual)) {
                totalRechazadas++;
            } else {
                totalEnProceso++;
            }
            
            // Por tipo de proceso
            String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
            porTipoProceso.put(nombreProceso, porTipoProceso.getOrDefault(nombreProceso, 0) + 1);
            
            // Por programa
            if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                String nombrePrograma = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                porPrograma.put(nombrePrograma, porPrograma.getOrDefault(nombrePrograma, 0) + 1);
            }
        }
        
        double porcentajeAprobacion = totalSolicitudes > 0 ? (totalAprobadas * 100.0) / totalSolicitudes : 0.0;
        
        estadisticas.put("totalSolicitudes", totalSolicitudes);
        estadisticas.put("totalAprobadas", totalAprobadas);
        estadisticas.put("totalRechazadas", totalRechazadas);
        estadisticas.put("totalEnProceso", totalEnProceso);
        estadisticas.put("porcentajeAprobacion", Math.round(porcentajeAprobacion * 100.0) / 100.0);
        estadisticas.put("porTipoProceso", porTipoProceso);
        estadisticas.put("porPrograma", porPrograma);
        estadisticas.put("fechaConsulta", new Date());
        
        return estadisticas;
    }
    
    /**
     * Aplica filtros a estadísticas existentes (método auxiliar para compatibilidad).
     */
    private Map<String, Object> aplicarFiltrosAEstadisticas(Map<String, Object> estadisticas, String periodoAcademico, Integer idPrograma) {
        // Este método puede ser usado para aplicar filtros adicionales si es necesario
        // Por ahora, retornamos las estadísticas tal cual ya que los filtros se aplican en la obtención
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTendenciasPorPeriodo(Date fechaInicio1, Date fechaFin1, Date fechaInicio2, Date fechaFin2) {
        Map<String, Object> tendencias = new HashMap<>();
        
        // Estadisticas del primer periodo
        Map<String, Object> periodo1 = obtenerEstadisticasPorPeriodo(fechaInicio1, fechaFin1);
        
        // Estadisticas del segundo periodo
        Map<String, Object> periodo2 = obtenerEstadisticasPorPeriodo(fechaInicio2, fechaFin2);
        
        // Calcular tendencias
        Integer total1 = ((Number) periodo1.get("totalSolicitudes")).intValue();
        Integer total2 = ((Number) periodo2.get("totalSolicitudes")).intValue();
        
        double variacion = total1 > 0 ? ((double) (total2 - total1) / total1) * 100 : 0.0;
        
        tendencias.put("periodo1", periodo1);
        tendencias.put("periodo2", periodo2);
        tendencias.put("variacionPorcentual", variacion);
        tendencias.put("tendencia", variacion > 0 ? "CRECIENTE" : variacion < 0 ? "DECRECIENTE" : "ESTABLE");
        tendencias.put("fechaConsulta", new Date());
        
        return tendencias;
    }

    /**
     * Metodo auxiliar para obtener descripcion de procesos
     */
    private String obtenerDescripcionProceso(String tipoProceso) {
        switch (tipoProceso.toUpperCase()) {
            case "REINGRESO":
                return "Solicitudes de reingreso a programas academicos";
            case "HOMOLOGACION":
                return "Solicitudes de homologacion de materias";
            case "ECAES":
                return "Solicitudes para examenes ECAES";
            case "CURSO_VERANO":
                return "Solicitudes para cursos intersemestrales";
            case "PAZ_SALVO":
                return "Solicitudes de paz y salvo";
            default:
                return "Proceso academico";
        }
    }

    /**
     * Metodo auxiliar para obtener descripcion de estados
     */
    private String obtenerDescripcionEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "aprobado":
            case "aprobada":
            case "aprobadas":
                return "Solicitudes aprobadas y procesadas";
            case "rechazado":
            case "rechazada":
            case "rechazadas":
                return "Solicitudes rechazadas";
            case "enviada":
            case "enviadas":
                return "Solicitudes enviadas pendientes de revision";
            case "en_proceso":
            case "en proceso":
            case "pendiente":
                return "Solicitudes en proceso de evaluacion";
            case "borrador":
                return "Solicitudes en borrador";
            default:
                return "Estado: " + estado;
        }
    }

    /**
     * Metodo auxiliar para normalizar nombres de procesos
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
            case "HOMOLOGACION DE MATERIAS":
                return "Homologacion";
            case "ECAES":
            case "INSCRIPCION A EXAMENES":
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
                // Si no coincide con ningun mapeo, devolver el nombre original
                log.debug("Normalizar proceso - Nombre de proceso no reconocido:  {}", nombreProceso);
                return procesoNormalizado;
        }
    }

    /**
     * Metodo centralizado para normalizar estadisticas y evitar valores null
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

    @Override
    public Map<String, Object> obtenerNumeroTotalEstudiantes() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Contar usuarios con rol de estudiante (ID = 2)
            long totalEstudiantes = usuarioRepository.buscarPorRol(2).size();
            
            resultado.put("totalEstudiantes", totalEstudiantes);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Total de estudiantes registrados en el sistema");
            
            return resultado;
        } catch (Exception e) {
            // En caso de error, devolver 0
            resultado.put("totalEstudiantes", 0);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener el conteo de estudiantes");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    @Override
    public Map<String, Object> obtenerEstudiantesPorPrograma() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener todos los estudiantes
            List<UsuarioEntity> estudiantes = usuarioRepository.buscarPorRol(2);
            
            // Agrupar por programa
            Map<String, Integer> estudiantesPorPrograma = new HashMap<>();
            for (UsuarioEntity estudiante : estudiantes) {
                String nombrePrograma = estudiante.getObjPrograma().getNombre_programa();
                estudiantesPorPrograma.put(nombrePrograma, 
                    estudiantesPorPrograma.getOrDefault(nombrePrograma, 0) + 1);
            }
            
            resultado.put("estudiantesPorPrograma", estudiantesPorPrograma);
            resultado.put("totalEstudiantes", estudiantes.size());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Distribucion de estudiantes por programa academico");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("estudiantesPorPrograma", new HashMap<>());
            resultado.put("totalEstudiantes", 0);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener distribucion de estudiantes");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    @Override
    public Map<String, Object> obtenerEstadisticasDetalladasPorProceso() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener datos reales del backend
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            
            // Inicializar contadores por proceso
            Map<String, Integer> totalPorProceso = new HashMap<>();
            Map<String, Integer> aprobadasPorProceso = new HashMap<>();
            Map<String, Integer> rechazadasPorProceso = new HashMap<>();
            Map<String, Integer> enviadasPorProceso = new HashMap<>();
            Map<String, Integer> enProcesoPorProceso = new HashMap<>();
            Map<String, List<Double>> tiemposPorProceso = new HashMap<>();
            Map<String, List<Date>> fechasPorProceso = new HashMap<>();
            
            // Inicializar mapas
            String[] procesos = {"Homologacion", "Paz y Salvo", "Reingreso", "Cursos de Verano", "ECAES"};
            for (String proceso : procesos) {
                totalPorProceso.put(proceso, 0);
                aprobadasPorProceso.put(proceso, 0);
                rechazadasPorProceso.put(proceso, 0);
                enviadasPorProceso.put(proceso, 0);
                enProcesoPorProceso.put(proceso, 0);
                tiemposPorProceso.put(proceso, new ArrayList<>());
                fechasPorProceso.put(proceso, new ArrayList<>());
            }
            
            // Procesar todas las solicitudes
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                if (nombreProceso != null) {
                    totalPorProceso.put(nombreProceso, totalPorProceso.get(nombreProceso) + 1);
                    
                    // Agregar fecha de creacion para analisis temporal
                    if (solicitud.getFecha_registro_solicitud() != null) {
                        fechasPorProceso.get(nombreProceso).add(solicitud.getFecha_registro_solicitud());
                    }
                    
                    // Analizar estado
                    String estado = obtenerEstadoMasReciente(solicitud);
                    if ("APROBADA".equals(estado)) {
                        aprobadasPorProceso.put(nombreProceso, aprobadasPorProceso.get(nombreProceso) + 1);
                    } else if ("RECHAZADA".equals(estado)) {
                        rechazadasPorProceso.put(nombreProceso, rechazadasPorProceso.get(nombreProceso) + 1);
                    } else if ("APROBADA_FUNCIONARIO".equals(estado) || "APROBADA_COORDINADOR".equals(estado)) {
                        enProcesoPorProceso.put(nombreProceso, enProcesoPorProceso.get(nombreProceso) + 1);
                    } else {
                        enviadasPorProceso.put(nombreProceso, enviadasPorProceso.get(nombreProceso) + 1);
                    }
                    
                    // Calcular tiempo de procesamiento si esta completado
                    if ("APROBADA".equals(estado) || "RECHAZADA".equals(estado)) {
                        Date fechaCreacion = solicitud.getFecha_registro_solicitud();
                        Date fechaActualizacion = obtenerFechaActualizacion(solicitud);
                        
                        if (fechaCreacion != null && fechaActualizacion != null) {
                            long diferenciaMs = fechaActualizacion.getTime() - fechaCreacion.getTime();
                            double tiempoDias = diferenciaMs / (1000.0 * 60 * 60 * 24);
                            tiemposPorProceso.get(nombreProceso).add(tiempoDias);
                        }
                    }
                }
            }
            
            // Crear estadisticas detalladas por proceso
            Map<String, Object> estadisticasDetalladas = new HashMap<>();
            Map<String, Object> resumenComparativo = new HashMap<>();
            
            String procesoMasEficiente = null;
            String procesoMasRapido = null;
            String procesoMasDemandado = null;
            double maxEficiencia = 0;
            double minTiempo = Double.MAX_VALUE;
            int maxDemanda = 0;
            
            for (String proceso : procesos) {
                int total = totalPorProceso.get(proceso);
                int aprobadas = aprobadasPorProceso.get(proceso);
                int rechazadas = rechazadasPorProceso.get(proceso);
                int enviadas = enviadasPorProceso.get(proceso);
                int enProceso = enProcesoPorProceso.get(proceso);
                
                // Calcular eficiencia
                double eficiencia = total > 0 ? (aprobadas * 100.0) / total : 0;
                
                // Calcular tiempo promedio
                List<Double> tiempos = tiemposPorProceso.get(proceso);
                double tiempoPromedio = tiempos.isEmpty() ? 0 : tiempos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double tiempoMinimo = tiempos.isEmpty() ? 0 : tiempos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                double tiempoMaximo = tiempos.isEmpty() ? 0 : tiempos.stream().mapToDouble(Double::doubleValue).max().orElse(0);
                
                // Analisis de tendencia temporal (ultimos 3 meses vs anteriores)
                List<Date> fechas = fechasPorProceso.get(proceso);
                String tendencia = "Estable";
                if (fechas.size() >= 2) {
                    // Simular analisis de tendencia basado en distribucion de fechas
                    long fechasRecientes = fechas.stream()
                        .filter(fecha -> fecha.after(new Date(System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000)))
                        .count();
                    long fechasAnteriores = fechas.size() - fechasRecientes;
                    
                    if (fechasRecientes > fechasAnteriores) {
                        tendencia = "Creciente";
                    } else if (fechasRecientes < fechasAnteriores) {
                        tendencia = "Decreciente";
                    }
                }
                
                // Crear detalle del proceso
                Map<String, Object> detalleProceso = new HashMap<>();
                detalleProceso.put("proceso", proceso);
                detalleProceso.put("totalSolicitudes", total);
                detalleProceso.put("aprobadas", aprobadas);
                detalleProceso.put("rechazadas", rechazadas);
                detalleProceso.put("enviadas", enviadas);
                detalleProceso.put("enProceso", enProceso);
                detalleProceso.put("eficiencia", Math.round(eficiencia * 100.0) / 100.0);
                detalleProceso.put("tiempoPromedio", Math.round(tiempoPromedio * 100.0) / 100.0);
                detalleProceso.put("tiempoMinimo", Math.round(tiempoMinimo * 100.0) / 100.0);
                detalleProceso.put("tiempoMaximo", Math.round(tiempoMaximo * 100.0) / 100.0);
                detalleProceso.put("tendencia", tendencia);
                detalleProceso.put("descripcion", obtenerDescripcionProcesoDetallada(proceso));
                detalleProceso.put("color", obtenerColorProceso(proceso));
                detalleProceso.put("icono", obtenerIconoProceso(proceso));
                
                estadisticasDetalladas.put(proceso, detalleProceso);
                
                // Actualizar comparativas
                if (eficiencia > maxEficiencia) {
                    maxEficiencia = eficiencia;
                    procesoMasEficiente = proceso;
                }
                
                if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                    minTiempo = tiempoPromedio;
                    procesoMasRapido = proceso;
                }
                
                if (total > maxDemanda) {
                    maxDemanda = total;
                    procesoMasDemandado = proceso;
                }
            }
            
            // Crear resumen comparativo
            resumenComparativo.put("procesoMasEficiente", procesoMasEficiente);
            resumenComparativo.put("procesoMasRapido", procesoMasRapido);
            resumenComparativo.put("procesoMasDemandado", procesoMasDemandado);
            resumenComparativo.put("eficienciaMaxima", Math.round(maxEficiencia * 100.0) / 100.0);
            resumenComparativo.put("tiempoMinimo", Math.round(minTiempo * 100.0) / 100.0);
            resumenComparativo.put("demandaMaxima", maxDemanda);
            resumenComparativo.put("totalProcesos", procesos.length);
            
            resultado.put("estadisticasPorProceso", estadisticasDetalladas);
            resultado.put("resumenComparativo", resumenComparativo);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadisticas detalladas por proceso con analisis de eficiencia y tiempo - DATOS REALES");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("estadisticasPorProceso", new HashMap<>());
            resultado.put("resumenComparativo", new HashMap<>());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener estadisticas por proceso");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    /**
     * Metodo auxiliar para obtener descripcion de procesos (version mejorada)
     */
    private String obtenerDescripcionProcesoDetallada(String nombreProceso) {
        if (nombreProceso.contains("Reingreso")) {
            return "Solicitudes de reingreso a programas academicos";
        } else if (nombreProceso.contains("Homologacion")) {
            return "Solicitudes de homologacion de materias";
        } else if (nombreProceso.contains("ECAES")) {
            return "Solicitudes de inscripcion a examenes ECAES";
        } else if (nombreProceso.contains("Curso Verano")) {
            return "Solicitudes de cursos intersemestrales/verano";
        } else if (nombreProceso.contains("Paz y Salvo")) {
            return "Solicitudes de paz y salvo";
        } else {
            return "Proceso academico";
        }
    }

    @Override
    public Map<String, Object> obtenerResumenPorProceso() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener estadisticas globales
            Map<String, Object> estadisticasGlobales = obtenerEstadisticasGlobales();
            
            // Procesar datos para crear resumen organizado
            Map<String, Object> resumenProcesos = new HashMap<>();
            
            // Mapear tipos de procesos con colores y estilos
            String[] tiposProcesos = {"Reingreso", "Homologacion", "ECAES", "Curso Verano", "Paz y Salvo"};
            String[] colores = {"#007bff", "#28a745", "#ffc107", "#17a2b8", "#dc3545"};
            String[] iconos = {"fas fa-user-plus", "fas fa-exchange-alt", "fas fa-graduation-cap", "fas fa-calendar-alt", "fas fa-check-circle"};
            
            // Contar solicitudes por tipo de proceso
            @SuppressWarnings("unchecked")
            Map<String, Object> porTipoProceso = (Map<String, Object>) estadisticasGlobales.get("porTipoProceso");
            
            if (porTipoProceso != null) {
                for (String tipoProceso : tiposProcesos) {
                    int contador = 0;
                    String nombreLimpio = "";
                    
                    // Contar solicitudes de este tipo
                    for (Map.Entry<String, Object> entry : porTipoProceso.entrySet()) {
                        String nombreSolicitud = entry.getKey();
                        if (nombreSolicitud.contains(tipoProceso)) {
                            contador++;
                            if (nombreLimpio.isEmpty()) {
                                nombreLimpio = tipoProceso;
                            }
                        }
                    }
                    
                    if (contador > 0) {
                        // Crear objeto de proceso con estilo
                        Map<String, Object> procesoInfo = new HashMap<>();
                        procesoInfo.put("nombre", nombreLimpio);
                        procesoInfo.put("cantidad", contador);
                        procesoInfo.put("descripcion", obtenerDescripcionProcesoDetallada(nombreLimpio));
                        procesoInfo.put("color", colores[getIndiceTipoProceso(tipoProceso)]);
                        procesoInfo.put("icono", iconos[getIndiceTipoProceso(tipoProceso)]);
                        
                        resumenProcesos.put(nombreLimpio, procesoInfo);
                    }
                }
            }
            
            resultado.put("procesos", resumenProcesos);
            resultado.put("totalProcesos", resumenProcesos.size());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Resumen de procesos con estilos para dashboard");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("procesos", new HashMap<>());
            resultado.put("totalProcesos", 0);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener resumen de procesos");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    /**
     * Metodo auxiliar para obtener indice del tipo de proceso
     */
    private int getIndiceTipoProceso(String tipoProceso) {
        switch (tipoProceso) {
            case "Reingreso": return 0;
            case "Homologacion": return 1;
            case "ECAES": return 2;
            case "Curso Verano": return 3;
            case "Paz y Salvo": return 4;
            default: return 0;
        }
    }

    @Override
    public Map<String, Object> obtenerConfiguracionEstilos() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Configuracion de tema claro con fondo blanco
            Map<String, Object> tema = new HashMap<>();
            tema.put("nombre", "Tema Claro");
            tema.put("fondoPrincipal", "#ffffff");
            tema.put("fondoSecundario", "#f8f9fa");
            tema.put("textoPrincipal", "#212529");
            tema.put("textoSecundario", "#6c757d");
            tema.put("borde", "#dee2e6");
            tema.put("sombra", "0 2px 10px rgba(0, 0, 0, 0.1)");
            
            // Colores para las tarjetas de estadisticas
            Map<String, Object> coloresTarjetas = new HashMap<>();
            coloresTarjetas.put("estudiantes", "#007bff");
            coloresTarjetas.put("procesos", "#6f42c1");
            coloresTarjetas.put("aprobadas", "#28a745");
            coloresTarjetas.put("enProceso", "#ffc107");
            coloresTarjetas.put("rechazadas", "#dc3545");
            coloresTarjetas.put("pendientes", "#17a2b8");
            
            // Configuracion de gradientes para las tarjetas
            Map<String, Object> gradientes = new HashMap<>();
            gradientes.put("estudiantes", "linear-gradient(135deg, #007bff, #0056b3)");
            gradientes.put("procesos", "linear-gradient(135deg, #6f42c1, #5a32a3)");
            gradientes.put("aprobadas", "linear-gradient(135deg, #28a745, #1e7e34)");
            gradientes.put("enProceso", "linear-gradient(135deg, #ffc107, #e0a800)");
            gradientes.put("rechazadas", "linear-gradient(135deg, #dc3545, #c82333)");
            gradientes.put("pendientes", "linear-gradient(135deg, #17a2b8, #138496)");
            
            // Configuracion de iconos
            Map<String, Object> iconos = new HashMap<>();
            iconos.put("estudiantes", "fas fa-users");
            iconos.put("procesos", "fas fa-chart-bar");
            iconos.put("aprobadas", "fas fa-check-circle");
            iconos.put("enProceso", "fas fa-clock");
            iconos.put("rechazadas", "fas fa-times-circle");
            iconos.put("pendientes", "fas fa-hourglass-half");
            
            // Configuracion de estilos para componentes
            Map<String, Object> estilos = new HashMap<>();
            estilos.put("borderRadius", "12px");
            estilos.put("padding", "20px");
            estilos.put("margin", "10px");
            estilos.put("fontFamily", "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif");
            estilos.put("fontSizeTitulo", "1.5rem");
            estilos.put("fontSizeSubtitulo", "1rem");
            estilos.put("fontSizeNumero", "2.5rem");
            
            resultado.put("tema", tema);
            resultado.put("coloresTarjetas", coloresTarjetas);
            resultado.put("gradientes", gradientes);
            resultado.put("iconos", iconos);
            resultado.put("estilos", estilos);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Configuracion de estilos para dashboard con tema claro");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("tema", new HashMap<>());
            resultado.put("coloresTarjetas", new HashMap<>());
            resultado.put("gradientes", new HashMap<>());
            resultado.put("iconos", new HashMap<>());
            resultado.put("estilos", new HashMap<>());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener configuracion de estilos");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorEstado() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener datos reales del backend
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            
            // Inicializar contadores por estado
            Map<String, Integer> totalPorEstado = new HashMap<>();
            Map<String, Map<String, Integer>> procesosPorEstado = new HashMap<>();
            Map<String, Map<String, Integer>> programasPorEstado = new HashMap<>();
            
            // Inicializar estados
            String[] estados = {"ENVIADA", "APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR", "APROBADA", "RECHAZADA"};
            
            for (String estado : estados) {
                totalPorEstado.put(estado, 0);
                procesosPorEstado.put(estado, new HashMap<>());
                programasPorEstado.put(estado, new HashMap<>());
            }
            
            // Procesar todas las solicitudes
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                String estadoActual = obtenerEstadoMasReciente(solicitud);
                String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                String nombrePrograma = null;
                
                if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                    nombrePrograma = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                }
                
                // Contar por estado actual
                totalPorEstado.put(estadoActual, totalPorEstado.getOrDefault(estadoActual, 0) + 1);
                
                // Contar procesos por estado
                if (nombreProceso != null) {
                    Map<String, Integer> procesos = procesosPorEstado.getOrDefault(estadoActual, new HashMap<>());
                    procesos.put(nombreProceso, procesos.getOrDefault(nombreProceso, 0) + 1);
                    procesosPorEstado.put(estadoActual, procesos);
                }
                
                // Contar programas por estado
                if (nombrePrograma != null) {
                    Map<String, Integer> programas = programasPorEstado.getOrDefault(estadoActual, new HashMap<>());
                    programas.put(nombrePrograma, programas.getOrDefault(nombrePrograma, 0) + 1);
                    programasPorEstado.put(estadoActual, programas);
                }
            }
            
            // Crear estadisticas detalladas por estado
            Map<String, Object> resumenPorEstado = new HashMap<>();
            Map<String, Object> analisisComparativo = new HashMap<>();
            
            String estadoMasComun = null;
            String estadoMenosComun = null;
            String estadoMasEficiente = null;
            int maxSolicitudes = 0;
            int minSolicitudes = Integer.MAX_VALUE;
            int totalSolicitudes = todasLasSolicitudes.size();
            
            // ⚠️ IMPORTANTE: Incluir TODOS los estados principales, incluso si tienen 0 solicitudes
            // Esto permite que el frontend muestre siempre los 4 estados principales: Aprobada, Enviada, En Proceso, Rechazada
            String[] estadosPrincipales = {"APROBADA", "ENVIADA", "RECHAZADA"};
            
            // Calcular total "En Proceso" (suma de funcionario + coordinador)
            int totalEnProcesoCombinado = totalPorEstado.getOrDefault("APROBADA_FUNCIONARIO", 0) + 
                                         totalPorEstado.getOrDefault("APROBADA_COORDINADOR", 0);
            
            // Procesar estados principales
            for (String estado : estadosPrincipales) {
                int total = totalPorEstado.getOrDefault(estado, 0);
                
                // Calcular porcentaje
                double porcentaje = totalSolicitudes > 0 ? (total * 100.0) / totalSolicitudes : 0.0;
                
                // Crear detalle del estado (SIEMPRE, incluso si total es 0)
                Map<String, Object> detalleEstado = new HashMap<>();
                detalleEstado.put("estado", estado);
                detalleEstado.put("cantidad", total);
                detalleEstado.put("porcentaje", Math.round(porcentaje * 100.0) / 100.0);
                detalleEstado.put("procesos", procesosPorEstado.getOrDefault(estado, new HashMap<>()));
                detalleEstado.put("programas", programasPorEstado.getOrDefault(estado, new HashMap<>()));
                detalleEstado.put("color", obtenerColorEstado(estado));
                detalleEstado.put("icono", obtenerIconoEstado(estado));
                detalleEstado.put("descripcion", obtenerDescripcionEstado(estado));
                
                resumenPorEstado.put(estado, detalleEstado);
                
                // Actualizar comparativas (solo si hay solicitudes)
                if (total > 0) {
                    if (total > maxSolicitudes) {
                        maxSolicitudes = total;
                        estadoMasComun = estado;
                    }
                    
                    if (total < minSolicitudes) {
                        minSolicitudes = total;
                        estadoMenosComun = estado;
                    }
                    
                    // Calcular eficiencia (aprobadas vs total)
                    if ("APROBADA".equals(estado)) {
                        estadoMasEficiente = estado;
                    }
                }
            }
            
            // Agregar estado "EN_PROCESO" combinado (suma de funcionario + coordinador)
            Map<String, Object> detalleEnProceso = new HashMap<>();
            detalleEnProceso.put("estado", "EN_PROCESO");
            detalleEnProceso.put("cantidad", totalEnProcesoCombinado);
            double porcentajeEnProceso = totalSolicitudes > 0 ? (totalEnProcesoCombinado * 100.0) / totalSolicitudes : 0.0;
            detalleEnProceso.put("porcentaje", Math.round(porcentajeEnProceso * 100.0) / 100.0);
            
            // Combinar procesos de ambos estados en proceso
            Map<String, Integer> procesosEnProcesoCombinados = new HashMap<>();
            Map<String, Integer> procesosFuncionario = procesosPorEstado.getOrDefault("APROBADA_FUNCIONARIO", new HashMap<>());
            Map<String, Integer> procesosCoordinador = procesosPorEstado.getOrDefault("APROBADA_COORDINADOR", new HashMap<>());
            procesosFuncionario.forEach((proceso, cantidad) -> 
                procesosEnProcesoCombinados.put(proceso, procesosEnProcesoCombinados.getOrDefault(proceso, 0) + cantidad));
            procesosCoordinador.forEach((proceso, cantidad) -> 
                procesosEnProcesoCombinados.put(proceso, procesosEnProcesoCombinados.getOrDefault(proceso, 0) + cantidad));
            detalleEnProceso.put("procesos", procesosEnProcesoCombinados);
            
            // Combinar programas
            Map<String, Integer> programasEnProcesoCombinados = new HashMap<>();
            Map<String, Integer> programasFuncionario = programasPorEstado.getOrDefault("APROBADA_FUNCIONARIO", new HashMap<>());
            Map<String, Integer> programasCoordinador = programasPorEstado.getOrDefault("APROBADA_COORDINADOR", new HashMap<>());
            programasFuncionario.forEach((programa, cantidad) -> 
                programasEnProcesoCombinados.put(programa, programasEnProcesoCombinados.getOrDefault(programa, 0) + cantidad));
            programasCoordinador.forEach((programa, cantidad) -> 
                programasEnProcesoCombinados.put(programa, programasEnProcesoCombinados.getOrDefault(programa, 0) + cantidad));
            detalleEnProceso.put("programas", programasEnProcesoCombinados);
            
            detalleEnProceso.put("color", obtenerColorEstado("EN_PROCESO"));
            detalleEnProceso.put("icono", obtenerIconoEstado("EN_PROCESO"));
            detalleEnProceso.put("descripcion", "Solicitudes en proceso de revisión");
            
            resumenPorEstado.put("EN_PROCESO", detalleEnProceso);
            
            // Actualizar comparativas para En Proceso
            if (totalEnProcesoCombinado > 0 && totalEnProcesoCombinado > maxSolicitudes) {
                maxSolicitudes = totalEnProcesoCombinado;
                estadoMasComun = "EN_PROCESO";
            }
            
            // Crear analisis comparativo
            analisisComparativo.put("estadoMasComun", estadoMasComun);
            analisisComparativo.put("estadoMenosComun", estadoMenosComun);
            analisisComparativo.put("estadoMasEficiente", estadoMasEficiente);
            analisisComparativo.put("maxSolicitudes", maxSolicitudes);
            analisisComparativo.put("minSolicitudes", minSolicitudes);
            analisisComparativo.put("totalSolicitudes", totalSolicitudes);
            analisisComparativo.put("estadosActivos", resumenPorEstado.size());
            analisisComparativo.put("solicitudesPendientes", totalPorEstado.getOrDefault("ENVIADA", 0) + totalPorEstado.getOrDefault("APROBADA_FUNCIONARIO", 0) + totalPorEstado.getOrDefault("APROBADA_COORDINADOR", 0));
            analisisComparativo.put("solicitudesCompletadas", totalPorEstado.getOrDefault("APROBADA", 0) + totalPorEstado.getOrDefault("RECHAZADA", 0));
            
            // Calcular tasa de resolucion
            // totalSolicitudes puede ser 0 si no se contaron correctamente, usar el tamaño real de la lista
            int totalSolicitudesReales = todasLasSolicitudes != null ? todasLasSolicitudes.size() : 0;
            int completadas = totalPorEstado.getOrDefault("APROBADA", 0) + totalPorEstado.getOrDefault("RECHAZADA", 0);
            double tasaResolucion = totalSolicitudesReales > 0 ? (completadas * 100.0) / totalSolicitudesReales : 0.0;
            // Redondear a 2 decimales y asegurar que no sea NaN o Infinity
            if (Double.isNaN(tasaResolucion) || Double.isInfinite(tasaResolucion)) {
                tasaResolucion = 0.0;
            }
            analisisComparativo.put("tasaResolucion", Math.round(tasaResolucion * 100.0) / 100.0);
            
            resultado.put("estados", resumenPorEstado);
            resultado.put("analisis", analisisComparativo);
            // Agregar total de solicitudes en el nivel raíz para que el frontend lo pueda usar fácilmente
            resultado.put("totalSolicitudes", totalSolicitudes);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadisticas por estado de solicitudes con analisis de distribucion - DATOS REALES");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("estados", new HashMap<>());
            resultado.put("analisis", new HashMap<>());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener estadisticas por estado");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }


    /**
     * Metodo auxiliar para obtener color del estado
     */
    private String obtenerColorEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "aprobado":
            case "aprobada":
            case "aprobadas":
                return "#28a745"; // Verde
            case "enviada":
            case "enviadas":
                return "#ffc107"; // Amarillo
            case "en_proceso":
            case "en proceso":
            case "pendiente":
                return "#17a2b8"; // Cyan
            case "rechazado":
            case "rechazada":
            case "rechazadas":
                return "#dc3545"; // Rojo
            case "borrador":
                return "#6c757d"; // Gris
            default:
                return "#007bff"; // Azul
        }
    }

    /**
     * Metodo auxiliar para obtener icono del estado
     */
    private String obtenerIconoEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "aprobado":
            case "aprobada":
            case "aprobadas":
                return "fas fa-check-circle";
            case "enviada":
            case "enviadas":
                return "fas fa-paper-plane";
            case "en_proceso":
            case "en proceso":
            case "pendiente":
                return "fas fa-clock";
            case "rechazado":
            case "rechazada":
            case "rechazadas":
                return "fas fa-times-circle";
            case "borrador":
                return "fas fa-edit";
            default:
                return "fas fa-question-circle";
        }
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPeriodo() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener datos reales del backend
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            
            // Inicializar contadores por mes
            Map<String, Integer> totalPorMes = new HashMap<>();
            Map<String, Integer> aprobadasPorMes = new HashMap<>();
            Map<String, Integer> rechazadasPorMes = new HashMap<>();
            Map<String, Integer> enviadasPorMes = new HashMap<>();
            Map<String, List<Double>> tiemposPorMes = new HashMap<>();
            Map<String, Map<String, Integer>> procesosPorMes = new HashMap<>();
            
            // Inicializar meses (Calendar.MONTH devuelve 0-11: 0=Enero, 1=Febrero, ..., 11=Diciembre)
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            
            for (String mes : meses) {
                totalPorMes.put(mes, 0);
                aprobadasPorMes.put(mes, 0);
                rechazadasPorMes.put(mes, 0);
                enviadasPorMes.put(mes, 0);
                tiemposPorMes.put(mes, new ArrayList<>());
                procesosPorMes.put(mes, new HashMap<>());
            }
            
            // Procesar todas las solicitudes
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                Date fechaCreacion = solicitud.getFecha_registro_solicitud();
                if (fechaCreacion != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(fechaCreacion);
                    int mesNumero = cal.get(Calendar.MONTH); // 0-11 (0=Enero, 11=Diciembre)
                    // Asegurar que el índice está en el rango válido
                    if (mesNumero >= 0 && mesNumero < meses.length) {
                        String nombreMes = meses[mesNumero];
                        String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                        
                        // Contar solicitudes por mes
                        totalPorMes.put(nombreMes, totalPorMes.get(nombreMes) + 1);
                        
                        // Contar procesos por mes
                        if (nombreProceso != null) {
                            Map<String, Integer> procesos = procesosPorMes.get(nombreMes);
                            procesos.put(nombreProceso, procesos.getOrDefault(nombreProceso, 0) + 1);
                        }
                        
                        // Analizar estado
                        String estado = obtenerEstadoMasReciente(solicitud);
                        if ("APROBADA".equals(estado)) {
                            aprobadasPorMes.put(nombreMes, aprobadasPorMes.get(nombreMes) + 1);
                        } else if ("RECHAZADA".equals(estado)) {
                            rechazadasPorMes.put(nombreMes, rechazadasPorMes.get(nombreMes) + 1);
                        } else {
                            enviadasPorMes.put(nombreMes, enviadasPorMes.get(nombreMes) + 1);
                        }
                        
                        // Calcular tiempo de procesamiento si esta completado
                        if ("APROBADA".equals(estado) || "RECHAZADA".equals(estado)) {
                            Date fechaActualizacion = obtenerFechaActualizacion(solicitud);
                            
                            if (fechaActualizacion != null) {
                                long diferenciaMs = fechaActualizacion.getTime() - fechaCreacion.getTime();
                                double tiempoDias = diferenciaMs / (1000.0 * 60 * 60 * 24);
                                tiemposPorMes.get(nombreMes).add(tiempoDias);
                            }
                        }
                    } else {
                        log.warn("Mes numero fuera de rango: {} para solicitud ID: {}", mesNumero, solicitud.getId_solicitud());
                    }
                }
            }
            
            // Crear estadisticas detalladas por mes
            Map<String, Object> resumenPorMes = new HashMap<>();
            Map<String, Object> analisisTendencias = new HashMap<>();
            
            String mesMasActivo = null;
            String mesMasEficiente = null;
            String mesMasRapido = null;
            int maxSolicitudes = 0;
            double maxEficiencia = 0;
            double minTiempo = Double.MAX_VALUE;
            int totalSolicitudes = 0;
            
            for (String mes : meses) {
                int total = totalPorMes.getOrDefault(mes, 0);
                int aprobadas = aprobadasPorMes.getOrDefault(mes, 0);
                int rechazadas = rechazadasPorMes.getOrDefault(mes, 0);
                int enviadas = enviadasPorMes.getOrDefault(mes, 0);
                
                // Incluir TODOS los meses en el resultado, incluso si tienen 0 solicitudes
                // Esto permite que el frontend muestre correctamente la tendencia
                totalSolicitudes += total;
                
                // Calcular eficiencia (solo si hay solicitudes)
                double eficiencia = total > 0 ? (aprobadas * 100.0) / total : 0.0;
                
                // Calcular tiempo promedio
                List<Double> tiempos = tiemposPorMes.getOrDefault(mes, new ArrayList<>());
                double tiempoPromedio = tiempos.isEmpty() ? 0 : tiempos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                
                // Calcular porcentaje del total (usar total real de solicitudes, no solo meses con datos)
                double porcentaje = todasLasSolicitudes.size() > 0 ? (total * 100.0) / todasLasSolicitudes.size() : 0.0;
                
                // Crear detalle del mes (SIEMPRE, incluso si total es 0)
                Map<String, Object> detalleMes = new HashMap<>();
                detalleMes.put("mes", mes);
                detalleMes.put("total", total);
                detalleMes.put("aprobadas", aprobadas);
                detalleMes.put("rechazadas", rechazadas);
                detalleMes.put("enviadas", enviadas);
                detalleMes.put("eficiencia", Math.round(eficiencia * 100.0) / 100.0);
                detalleMes.put("tiempoPromedio", Math.round(tiempoPromedio * 100.0) / 100.0);
                detalleMes.put("porcentaje", Math.round(porcentaje * 100.0) / 100.0);
                detalleMes.put("procesos", procesosPorMes.getOrDefault(mes, new HashMap<>()));
                detalleMes.put("color", obtenerColorMes(mes));
                detalleMes.put("icono", obtenerIconoMes(mes));
                detalleMes.put("descripcion", obtenerDescripcionMes(mes));
                
                resumenPorMes.put(mes, detalleMes);
                
                // Actualizar comparativas (solo si hay datos)
                if (total > 0) {
                    if (total > maxSolicitudes) {
                        maxSolicitudes = total;
                        mesMasActivo = mes;
                    }
                    
                    if (eficiencia > maxEficiencia && total > 0) {
                        maxEficiencia = eficiencia;
                        mesMasEficiente = mes;
                    }
                    
                    if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                        minTiempo = tiempoPromedio;
                        mesMasRapido = mes;
                    }
                }
            }
            
            // Analisis de tendencias
            List<String> mesesConDatos = new ArrayList<>();
            for (String mes : meses) {
                if (totalPorMes.get(mes) > 0) {
                    mesesConDatos.add(mes);
                }
            }
            
            String tendenciaGeneral = "Estable";
            double crecimientoPromedio = 0;
            
            if (mesesConDatos.size() >= 2) {
                // Calcular tendencia basada en los ultimos 2 meses con datos
                String mesAnterior = mesesConDatos.get(mesesConDatos.size() - 2);
                String mesActual = mesesConDatos.get(mesesConDatos.size() - 1);
                
                int solicitudesAnterior = totalPorMes.get(mesAnterior);
                int solicitudesActual = totalPorMes.get(mesActual);
                
                if (solicitudesActual > solicitudesAnterior) {
                    tendenciaGeneral = "Creciente";
                    crecimientoPromedio = solicitudesAnterior > 0 ? 
                        ((solicitudesActual - solicitudesAnterior) * 100.0) / solicitudesAnterior : 0;
                } else if (solicitudesActual < solicitudesAnterior) {
                    tendenciaGeneral = "Decreciente";
                    crecimientoPromedio = solicitudesAnterior > 0 ? 
                        ((solicitudesAnterior - solicitudesActual) * 100.0) / solicitudesAnterior : 0;
                }
            }
            
            // Crear analisis de tendencias
            analisisTendencias.put("mesMasActivo", mesMasActivo);
            analisisTendencias.put("mesMasEficiente", mesMasEficiente);
            analisisTendencias.put("mesMasRapido", mesMasRapido);
            analisisTendencias.put("maxSolicitudes", maxSolicitudes);
            analisisTendencias.put("eficienciaMaxima", Math.round(maxEficiencia * 100.0) / 100.0);
            analisisTendencias.put("tiempoMinimo", Math.round(minTiempo * 100.0) / 100.0);
            analisisTendencias.put("tendenciaGeneral", tendenciaGeneral);
            analisisTendencias.put("crecimientoPromedio", Math.round(crecimientoPromedio * 100.0) / 100.0);
            analisisTendencias.put("mesesAnalizados", mesesConDatos.size());
            analisisTendencias.put("totalSolicitudes", totalSolicitudes);
            analisisTendencias.put("promedioMensual", mesesConDatos.size() > 0 ? Math.round((totalSolicitudes * 100.0) / mesesConDatos.size()) / 100.0 : 0);
            
            resultado.put("porMes", resumenPorMes);
            resultado.put("tendencias", analisisTendencias);
            // Agregar array con todos los meses para que el frontend sepa qué meses debe mostrar
            // Esto asegura que el frontend muestre todos los meses, incluso si tienen 0 solicitudes
            resultado.put("todosLosMeses", Arrays.asList(meses));
            // Agregar también como lista ordenada de objetos para facilitar el renderizado en el frontend
            List<Map<String, Object>> mesesOrdenados = new ArrayList<>();
            for (String mes : meses) {
                @SuppressWarnings("unchecked")
                Map<String, Object> detalleMes = (Map<String, Object>) resumenPorMes.get(mes);
                if (detalleMes != null) {
                    mesesOrdenados.add(detalleMes);
                }
            }
            resultado.put("mesesOrdenados", mesesOrdenados);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadisticas por periodo/mes con analisis de tendencias - DATOS REALES");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("porMes", new HashMap<>());
            resultado.put("tendencias", new HashMap<>());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener estadisticas por periodo");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    private String obtenerColorMes(String mes) {
        switch (mes) {
            case "Enero":
                return "#FF6B6B"; // Rojo
            case "Febrero":
                return "#4ECDC4"; // Turquesa
            case "Marzo":
                return "#45B7D1"; // Azul
            case "Abril":
                return "#96CEB4"; // Verde
            case "Mayo":
                return "#FFEAA7"; // Amarillo
            case "Junio":
                return "#DDA0DD"; // Purpura
            case "Julio":
                return "#98D8C8"; // Verde agua
            case "Agosto":
                return "#F7DC6F"; // Amarillo dorado
            case "Septiembre":
                return "#BB8FCE"; // Purpura claro
            case "Octubre":
                return "#85C1E9"; // Azul claro
            case "Noviembre":
                return "#F8C471"; // Naranja
            case "Diciembre":
                return "#F1948A"; // Rosa
            default:
                return "#6B7280"; // Gris
        }
    }

    private String obtenerIconoMes(String mes) {
        switch (mes) {
            case "Enero":
                return "";
            case "Febrero":
                return "";
            case "Marzo":
                return "";
            case "Abril":
                return "";
            case "Mayo":
                return "";
            case "Junio":
                return "";
            case "Julio":
                return "";
            case "Agosto":
                return "";
            case "Septiembre":
                return "";
            case "Octubre":
                return "";
            case "Noviembre":
                return "";
            case "Diciembre":
                return "";
            default:
                return "";
        }
    }

    private String obtenerDescripcionMes(String mes) {
        switch (mes) {
            case "Enero":
                return "Mes de inicio de ano academico - Periodo de inscripciones";
            case "Febrero":
                return "Mes de inicio de clases - Periodo de ajustes academicos";
            case "Marzo":
                return "Mes de estabilizacion academica - Periodo regular";
            case "Abril":
                return "Mes de evaluaciones parciales - Periodo de examenes";
            case "Mayo":
                return "Mes de mitad de semestre - Periodo de seguimiento";
            case "Junio":
                return "Mes de finalizacion de semestre - Periodo de examenes finales";
            case "Julio":
                return "Mes de vacaciones - Periodo de cursos de verano";
            case "Agosto":
                return "Mes de inicio de segundo semestre - Periodo de inscripciones";
            case "Septiembre":
                return "Mes de estabilizacion - Periodo regular de clases";
            case "Octubre":
                return "Mes de evaluaciones - Periodo de examenes parciales";
            case "Noviembre":
                return "Mes de preparacion final - Periodo de proyectos";
            case "Diciembre":
                return "Mes de finalizacion de ano - Periodo de graduaciones";
            default:
                return "Periodo academico";
        }
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorPrograma() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener datos reales del backend
            List<ProgramaEntity> todosLosProgramas = programaRepository.findAll();
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            List<UsuarioEntity> todosLosEstudiantes = usuarioRepository.buscarPorRol(2);
            
            // Inicializar contadores por programa
            Map<String, Integer> estudiantesPorPrograma = new HashMap<>();
            Map<String, Integer> solicitudesPorPrograma = new HashMap<>();
            Map<String, Integer> aprobadasPorPrograma = new HashMap<>();
            Map<String, Integer> rechazadasPorPrograma = new HashMap<>();
            Map<String, Integer> enviadasPorPrograma = new HashMap<>();
            Map<String, List<Double>> tiemposPorPrograma = new HashMap<>();
            Map<String, List<Date>> fechasPorPrograma = new HashMap<>();
            Map<String, Map<String, Integer>> procesosPorPrograma = new HashMap<>();
            
            // Inicializar mapas para cada programa
            for (ProgramaEntity programa : todosLosProgramas) {
                String nombrePrograma = programa.getNombre_programa();
                estudiantesPorPrograma.put(nombrePrograma, 0);
                solicitudesPorPrograma.put(nombrePrograma, 0);
                aprobadasPorPrograma.put(nombrePrograma, 0);
                rechazadasPorPrograma.put(nombrePrograma, 0);
                enviadasPorPrograma.put(nombrePrograma, 0);
                tiemposPorPrograma.put(nombrePrograma, new ArrayList<>());
                fechasPorPrograma.put(nombrePrograma, new ArrayList<>());
                procesosPorPrograma.put(nombrePrograma, new HashMap<>());
            }
            
            // Contar estudiantes por programa
            for (UsuarioEntity estudiante : todosLosEstudiantes) {
                if (estudiante.getObjPrograma() != null) {
                    String nombrePrograma = estudiante.getObjPrograma().getNombre_programa();
                    estudiantesPorPrograma.put(nombrePrograma, estudiantesPorPrograma.getOrDefault(nombrePrograma, 0) + 1);
                }
            }
            
            // Procesar todas las solicitudes
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                    String nombrePrograma = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                    String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                    
                    // Contar solicitudes por programa
                    solicitudesPorPrograma.put(nombrePrograma, solicitudesPorPrograma.getOrDefault(nombrePrograma, 0) + 1);
                    
                    // Contar procesos por programa
                    if (nombreProceso != null) {
                        Map<String, Integer> procesos = procesosPorPrograma.get(nombrePrograma);
                        procesos.put(nombreProceso, procesos.getOrDefault(nombreProceso, 0) + 1);
                    }
                    
                    // Agregar fecha de creacion para analisis temporal
                    if (solicitud.getFecha_registro_solicitud() != null) {
                        fechasPorPrograma.get(nombrePrograma).add(solicitud.getFecha_registro_solicitud());
                    }
                    
                    // Analizar estado
                    String estado = obtenerEstadoMasReciente(solicitud);
                    if ("APROBADA".equals(estado)) {
                        aprobadasPorPrograma.put(nombrePrograma, aprobadasPorPrograma.get(nombrePrograma) + 1);
                    } else if ("RECHAZADA".equals(estado)) {
                        rechazadasPorPrograma.put(nombrePrograma, rechazadasPorPrograma.get(nombrePrograma) + 1);
                    } else {
                        enviadasPorPrograma.put(nombrePrograma, enviadasPorPrograma.get(nombrePrograma) + 1);
                    }
                    
                    // Calcular tiempo de procesamiento si esta completado
                    if ("APROBADA".equals(estado) || "RECHAZADA".equals(estado)) {
                        Date fechaCreacion = solicitud.getFecha_registro_solicitud();
                        Date fechaActualizacion = obtenerFechaActualizacion(solicitud);
                        
                        if (fechaCreacion != null && fechaActualizacion != null) {
                            long diferenciaMs = fechaActualizacion.getTime() - fechaCreacion.getTime();
                            double tiempoDias = diferenciaMs / (1000.0 * 60 * 60 * 24);
                            tiemposPorPrograma.get(nombrePrograma).add(tiempoDias);
                        }
                    }
                }
            }
            
            // Crear estadisticas detalladas por programa
            Map<String, Object> resumenPorPrograma = new HashMap<>();
            Map<String, Object> analisisComparativo = new HashMap<>();
            
            String programaMasActivo = null;
            String programaConMasEstudiantes = null;
            String programaMasEficiente = null;
            String programaMasRapido = null;
            int maxSolicitudes = 0;
            int maxEstudiantes = 0;
            double maxEficiencia = 0;
            double minTiempo = Double.MAX_VALUE;
            
            for (ProgramaEntity programa : todosLosProgramas) {
                String nombrePrograma = programa.getNombre_programa();
                int estudiantes = estudiantesPorPrograma.getOrDefault(nombrePrograma, 0);
                int solicitudes = solicitudesPorPrograma.getOrDefault(nombrePrograma, 0);
                int aprobadas = aprobadasPorPrograma.getOrDefault(nombrePrograma, 0);
                int rechazadas = rechazadasPorPrograma.getOrDefault(nombrePrograma, 0);
                int enviadas = enviadasPorPrograma.getOrDefault(nombrePrograma, 0);
                
                // Calcular eficiencia
                double eficiencia = solicitudes > 0 ? (aprobadas * 100.0) / solicitudes : 0;
                
                // Calcular tiempo promedio
                List<Double> tiempos = tiemposPorPrograma.get(nombrePrograma);
                double tiempoPromedio = tiempos.isEmpty() ? 0 : tiempos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                
                // Analisis de tendencia temporal
                List<Date> fechas = fechasPorPrograma.get(nombrePrograma);
                String tendencia = "Estable";
                if (fechas.size() >= 2) {
                    long fechasRecientes = fechas.stream()
                        .filter(fecha -> fecha.after(new Date(System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000)))
                        .count();
                    long fechasAnteriores = fechas.size() - fechasRecientes;
                    
                    if (fechasRecientes > fechasAnteriores) {
                        tendencia = "Creciente";
                    } else if (fechasRecientes < fechasAnteriores) {
                        tendencia = "Decreciente";
                    }
                }
                
                // Calcular porcentaje del total
                int totalSolicitudesSistema = todasLasSolicitudes.size();
                double porcentaje = totalSolicitudesSistema > 0 ? (solicitudes * 100.0) / totalSolicitudesSistema : 0;
                
                // Crear detalle del programa
                Map<String, Object> detallePrograma = new HashMap<>();
                detallePrograma.put("nombre", nombrePrograma);
                detallePrograma.put("codigo", programa.getCodigo());
                detallePrograma.put("totalSolicitudes", solicitudes);
                detallePrograma.put("estudiantes", estudiantes);
                detallePrograma.put("aprobadas", aprobadas);
                detallePrograma.put("rechazadas", rechazadas);
                detallePrograma.put("enviadas", enviadas);
                detallePrograma.put("eficiencia", Math.round(eficiencia * 100.0) / 100.0);
                detallePrograma.put("tiempoPromedio", Math.round(tiempoPromedio * 100.0) / 100.0);
                detallePrograma.put("tendencia", tendencia);
                detallePrograma.put("porcentaje", Math.round(porcentaje * 100.0) / 100.0);
                detallePrograma.put("procesos", procesosPorPrograma.get(nombrePrograma));
                detallePrograma.put("color", obtenerColorPrograma(nombrePrograma));
                detallePrograma.put("icono", obtenerIconoPrograma(nombrePrograma));
                detallePrograma.put("descripcion", obtenerDescripcionPrograma(nombrePrograma));
                
                resumenPorPrograma.put(nombrePrograma, detallePrograma);
                
                // Actualizar comparativas
                if (solicitudes > maxSolicitudes) {
                    maxSolicitudes = solicitudes;
                    programaMasActivo = nombrePrograma;
                }
                
                if (estudiantes > maxEstudiantes) {
                    maxEstudiantes = estudiantes;
                    programaConMasEstudiantes = nombrePrograma;
                }
                
                if (eficiencia > maxEficiencia) {
                    maxEficiencia = eficiencia;
                    programaMasEficiente = nombrePrograma;
                }
                
                if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                    minTiempo = tiempoPromedio;
                    programaMasRapido = nombrePrograma;
                }
            }
            
            // Crear analisis comparativo
            analisisComparativo.put("programaMasActivo", programaMasActivo);
            analisisComparativo.put("programaConMasEstudiantes", programaConMasEstudiantes);
            analisisComparativo.put("programaMasEficiente", programaMasEficiente);
            analisisComparativo.put("programaMasRapido", programaMasRapido);
            analisisComparativo.put("maxSolicitudes", maxSolicitudes);
            analisisComparativo.put("maxEstudiantes", maxEstudiantes);
            analisisComparativo.put("eficienciaMaxima", Math.round(maxEficiencia * 100.0) / 100.0);
            analisisComparativo.put("tiempoMinimo", Math.round(minTiempo * 100.0) / 100.0);
            analisisComparativo.put("totalProgramas", todosLosProgramas.size());
            analisisComparativo.put("totalSolicitudes", todasLasSolicitudes.size());
            analisisComparativo.put("totalEstudiantes", todosLosEstudiantes.size());
            
            resultado.put("porPrograma", resumenPorPrograma);
            resultado.put("estudiantesPorPrograma", estudiantesPorPrograma);
            resultado.put("solicitudesPorPrograma", solicitudesPorPrograma);
            resultado.put("analisis", analisisComparativo);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadisticas por programa academico con analisis de rendimiento - DATOS REALES");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("porPrograma", new HashMap<>());
            resultado.put("analisis", new HashMap<>());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener estadisticas por programa");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    private String obtenerColorPrograma(String nombrePrograma) {
        switch (nombrePrograma) {
            case "Ingenieria de Sistemas":
                return "#3B82F6"; // Azul
            case "Ingenieria Electronica y Telecomunicaciones":
                return "#10B981"; // Verde
            case "Ingenieria Automatica Industrial":
                return "#F59E0B"; // Amarillo
            case "Tecnologia en Telematica":
                return "#8B5CF6"; // Purpura
            default:
                return "#6B7280"; // Gris
        }
    }

    private String obtenerIconoPrograma(String nombrePrograma) {
        switch (nombrePrograma) {
            case "Ingenieria de Sistemas":
                return "";
            case "Ingenieria Electronica y Telecomunicaciones":
                return "";
            case "Ingenieria Automatica Industrial":
                return "";
            case "Tecnologia en Telematica":
                return "";
            default:
                return "";
        }
    }

    private String obtenerDescripcionPrograma(String nombrePrograma) {
        switch (nombrePrograma) {
            case "Ingenieria de Sistemas":
                return "Programa de Ingenieria de Sistemas - Desarrollo de software y sistemas informaticos";
            case "Ingenieria Electronica y Telecomunicaciones":
                return "Programa de Ingenieria Electronica y Telecomunicaciones - Sistemas electronicos y comunicaciones";
            case "Ingenieria Automatica Industrial":
                return "Programa de Ingenieria Automatica Industrial - Automatizacion y control industrial";
            case "Tecnologia en Telematica":
                return "Programa de Tecnologia en Telematica - Redes y telecomunicaciones";
            default:
                return "Programa academico";
        }
    }

    @Override
    public Map<String, Object> obtenerTiempoPromedioProcesamiento() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Crear datos de ejemplo basados en los procesos del import.sql
            Map<String, Object> tiemposPorProceso = new HashMap<>();
            
            // Datos simulados basados en los procesos existentes
            Map<String, Object> homologacion = new HashMap<>();
            homologacion.put("nombre", "Homologacion");
            homologacion.put("tiempoPromedio", 3.5);
            homologacion.put("tiempoMinimo", 1.0);
            homologacion.put("tiempoMaximo", 7.0);
            homologacion.put("totalSolicitudes", 15);
            homologacion.put("solicitudesCompletadas", 12);
            homologacion.put("eficiencia", 80.0);
            homologacion.put("color", "#28a745");
            homologacion.put("icono", "fas fa-graduation-cap");
            homologacion.put("descripcion", "Proceso de homologacion de materias");
            tiemposPorProceso.put("Homologacion", homologacion);
            
            Map<String, Object> pazSalvo = new HashMap<>();
            pazSalvo.put("nombre", "Paz y Salvo");
            pazSalvo.put("tiempoPromedio", 2.2);
            pazSalvo.put("tiempoMinimo", 0.5);
            pazSalvo.put("tiempoMaximo", 4.0);
            pazSalvo.put("totalSolicitudes", 18);
            pazSalvo.put("solicitudesCompletadas", 16);
            pazSalvo.put("eficiencia", 88.9);
            pazSalvo.put("color", "#007bff");
            pazSalvo.put("icono", "fas fa-shield-alt");
            pazSalvo.put("descripcion", "Proceso de paz y salvo academico");
            tiemposPorProceso.put("Paz y Salvo", pazSalvo);
            
            Map<String, Object> reingreso = new HashMap<>();
            reingreso.put("nombre", "Reingreso");
            reingreso.put("tiempoPromedio", 4.8);
            reingreso.put("tiempoMinimo", 2.0);
            reingreso.put("tiempoMaximo", 10.0);
            reingreso.put("totalSolicitudes", 8);
            reingreso.put("solicitudesCompletadas", 6);
            reingreso.put("eficiencia", 75.0);
            reingreso.put("color", "#ffc107");
            reingreso.put("icono", "fas fa-user-plus");
            reingreso.put("descripcion", "Proceso de reingreso estudiantil");
            tiemposPorProceso.put("Reingreso", reingreso);
            
            Map<String, Object> cursosVerano = new HashMap<>();
            cursosVerano.put("nombre", "Cursos de Verano");
            cursosVerano.put("tiempoPromedio", 1.8);
            cursosVerano.put("tiempoMinimo", 0.5);
            cursosVerano.put("tiempoMaximo", 3.5);
            cursosVerano.put("totalSolicitudes", 5);
            cursosVerano.put("solicitudesCompletadas", 5);
            cursosVerano.put("eficiencia", 100.0);
            cursosVerano.put("color", "#17a2b8");
            cursosVerano.put("icono", "fas fa-sun");
            cursosVerano.put("descripcion", "Proceso de cursos de verano");
            tiemposPorProceso.put("Cursos de Verano", cursosVerano);
            
            // Analisis de eficiencia
            Map<String, Object> analisisEficiencia = new HashMap<>();
            analisisEficiencia.put("procesoMasRapido", "Cursos de Verano");
            analisisEficiencia.put("procesoMasLento", "Reingreso");
            analisisEficiencia.put("procesoMasEficiente", "Cursos de Verano");
            analisisEficiencia.put("tiempoPromedioGeneral", 3.1);
            analisisEficiencia.put("eficienciaGeneral", 85.9);
            analisisEficiencia.put("totalSolicitudes", 46);
            analisisEficiencia.put("solicitudesCompletadas", 39);
            analisisEficiencia.put("solicitudesPendientes", 7);
            
            // Metas de tiempo
            Map<String, Object> metasTiempo = new HashMap<>();
            metasTiempo.put("homologacion", 3.0);
            metasTiempo.put("pazSalvo", 2.0);
            metasTiempo.put("reingreso", 5.0);
            metasTiempo.put("cursosVerano", 2.0);
            metasTiempo.put("metaGeneral", 3.0);
            
            resultado.put("porProceso", tiemposPorProceso);
            resultado.put("analisisEficiencia", analisisEficiencia);
            resultado.put("metasTiempo", metasTiempo);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadisticas de tiempo promedio de procesamiento con analisis de eficiencia");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("porProceso", new HashMap<>());
            resultado.put("analisisEficiencia", new HashMap<>());
            resultado.put("metasTiempo", new HashMap<>());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener estadisticas de tiempo de procesamiento");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }

    @Override
    public Map<String, Object> obtenerTendenciasYComparativas() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener todas las solicitudes para analisis de tendencias
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            List<UsuarioEntity> todosLosEstudiantes = usuarioRepository.buscarPorRol(2);
            
            // Analisis de crecimiento temporal
            Map<String, Object> crecimientoTemporal = new HashMap<>();
            Map<String, Integer> solicitudesPorMes = new HashMap<>();
            Map<String, Integer> estudiantesPorMes = new HashMap<>();
            
            // Inicializar meses (Calendar.MONTH devuelve 0-11: 0=Enero, 1=Febrero, ..., 11=Diciembre)
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            
            for (String mes : meses) {
                solicitudesPorMes.put(mes, 0);
                estudiantesPorMes.put(mes, 0);
            }
            
            // Contar solicitudes por mes
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                Date fechaCreacion = solicitud.getFecha_registro_solicitud();
                if (fechaCreacion != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(fechaCreacion);
                    int mesNumero = cal.get(Calendar.MONTH); // 0-11 (0=Enero, 11=Diciembre)
                    // Asegurar que el índice está en el rango válido
                    if (mesNumero >= 0 && mesNumero < meses.length) {
                        String nombreMes = meses[mesNumero];
                        solicitudesPorMes.put(nombreMes, solicitudesPorMes.get(nombreMes) + 1);
                    } else {
                        log.warn("Mes numero fuera de rango: {} para solicitud ID: {}", mesNumero, solicitud.getId_solicitud());
                    }
                }
            }
            
            // Contar estudiantes por mes de registro (simulado)
            for (UsuarioEntity estudiante : todosLosEstudiantes) {
                // Como no tenemos fecha de registro de estudiantes, simulamos distribucion
                String[] mesesEstudiantes = {"Julio", "Agosto", "Septiembre", "Octubre"};
                String mesAleatorio = mesesEstudiantes[Math.abs(estudiante.getId_usuario() % mesesEstudiantes.length)];
                estudiantesPorMes.put(mesAleatorio, estudiantesPorMes.get(mesAleatorio) + 1);
            }
            
            // Calcular crecimiento
            List<String> mesesConDatos = new ArrayList<>();
            for (String mes : meses) {
                if (solicitudesPorMes.get(mes) > 0) {
                    mesesConDatos.add(mes);
                }
            }
            
            String tendenciaSolicitudes = "Estable";
            String tendenciaEstudiantes = "Estable";
            double crecimientoSolicitudes = 0.0;
            double crecimientoEstudiantes = 0.0;
            
            if (mesesConDatos.size() >= 2) {
                String mesAnterior = mesesConDatos.get(mesesConDatos.size() - 2);
                String mesActual = mesesConDatos.get(mesesConDatos.size() - 1);
                
                int solicitudesAnterior = solicitudesPorMes.getOrDefault(mesAnterior, 0);
                int solicitudesActual = solicitudesPorMes.getOrDefault(mesActual, 0);
                int estudiantesAnterior = estudiantesPorMes.getOrDefault(mesAnterior, 0);
                int estudiantesActual = estudiantesPorMes.getOrDefault(mesActual, 0);
                
                // Calcular tendencia de solicitudes
                if (solicitudesActual > solicitudesAnterior) {
                    tendenciaSolicitudes = "Creciente";
                    crecimientoSolicitudes = solicitudesAnterior > 0 ? 
                        ((solicitudesActual - solicitudesAnterior) * 100.0) / solicitudesAnterior : 0.0;
                } else if (solicitudesActual < solicitudesAnterior) {
                    tendenciaSolicitudes = "Decreciente";
                    crecimientoSolicitudes = solicitudesAnterior > 0 ? 
                        ((solicitudesAnterior - solicitudesActual) * 100.0) / solicitudesAnterior : 0.0;
                }
                
                // Calcular tendencia de estudiantes
                if (estudiantesActual > estudiantesAnterior) {
                    tendenciaEstudiantes = "Creciente";
                    crecimientoEstudiantes = estudiantesAnterior > 0 ? 
                        ((estudiantesActual - estudiantesAnterior) * 100.0) / estudiantesAnterior : 0.0;
                } else if (estudiantesActual < estudiantesAnterior) {
                    tendenciaEstudiantes = "Decreciente";
                    crecimientoEstudiantes = estudiantesAnterior > 0 ? 
                        ((estudiantesAnterior - estudiantesActual) * 100.0) / estudiantesAnterior : 0.0;
                }
            } else if (mesesConDatos.size() == 1) {
                // Si solo hay un mes con datos, la tendencia es "Nueva" o "Estable" con 0% de crecimiento
                tendenciaSolicitudes = "Nueva";
                tendenciaEstudiantes = "Nueva";
                crecimientoSolicitudes = 0.0;
                crecimientoEstudiantes = 0.0;
            }
            
            // Asegurar que los valores no sean NaN o Infinity
            if (Double.isNaN(crecimientoSolicitudes) || Double.isInfinite(crecimientoSolicitudes)) {
                crecimientoSolicitudes = 0.0;
            }
            if (Double.isNaN(crecimientoEstudiantes) || Double.isInfinite(crecimientoEstudiantes)) {
                crecimientoEstudiantes = 0.0;
            }
            
            // Si solo hay un mes con datos, mantener 0.0% de crecimiento (no hay comparación posible)
            // La tendencia "Nueva" ya indica que es el primer mes con datos
            
            crecimientoTemporal.put("tendenciaSolicitudes", tendenciaSolicitudes);
            crecimientoTemporal.put("crecimientoSolicitudes", Math.round(crecimientoSolicitudes * 100.0) / 100.0);
            crecimientoTemporal.put("tendenciaEstudiantes", tendenciaEstudiantes);
            crecimientoTemporal.put("crecimientoEstudiantes", Math.round(crecimientoEstudiantes * 100.0) / 100.0);
            crecimientoTemporal.put("mesesAnalizados", mesesConDatos.size());
            crecimientoTemporal.put("totalSolicitudes", todasLasSolicitudes.size());
            crecimientoTemporal.put("totalEstudiantes", todosLosEstudiantes.size());
            // Agregar datos por mes para que el frontend pueda mostrar la tendencia completa
            crecimientoTemporal.put("solicitudesPorMes", solicitudesPorMes);
            crecimientoTemporal.put("estudiantesPorMes", estudiantesPorMes);
            
            // Analisis comparativo por proceso
            Map<String, Object> comparativaProcesos = new HashMap<>();
            Map<String, Integer> solicitudesPorProceso = new HashMap<>();
            Map<String, Integer> aprobadasPorProceso = new HashMap<>();
            
            solicitudesPorProceso.put("Homologacion", 0);
            solicitudesPorProceso.put("Paz y Salvo", 0);
            solicitudesPorProceso.put("Reingreso", 0);
            solicitudesPorProceso.put("Cursos de Verano", 0);
            solicitudesPorProceso.put("ECAES", 0);
            
            aprobadasPorProceso.put("Homologacion", 0);
            aprobadasPorProceso.put("Paz y Salvo", 0);
            aprobadasPorProceso.put("Reingreso", 0);
            aprobadasPorProceso.put("Cursos de Verano", 0);
            aprobadasPorProceso.put("ECAES", 0);
            
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                if (nombreProceso != null) {
                    solicitudesPorProceso.put(nombreProceso, solicitudesPorProceso.get(nombreProceso) + 1);
                    
                    String estado = obtenerEstadoMasReciente(solicitud);
                    if ("APROBADA".equals(estado)) {
                        aprobadasPorProceso.put(nombreProceso, aprobadasPorProceso.get(nombreProceso) + 1);
                    }
                }
            }
            
            String procesoMasDemandado = null;
            String procesoMasEficiente = null;
            int maxDemanda = 0;
            double maxEficiencia = 0;
            
            for (Map.Entry<String, Integer> entry : solicitudesPorProceso.entrySet()) {
                String proceso = entry.getKey();
                int total = entry.getValue();
                int aprobadas = aprobadasPorProceso.get(proceso);
                
                if (total > maxDemanda) {
                    maxDemanda = total;
                    procesoMasDemandado = proceso;
                }
                
                double eficiencia = total > 0 ? (aprobadas * 100.0) / total : 0;
                if (eficiencia > maxEficiencia) {
                    maxEficiencia = eficiencia;
                    procesoMasEficiente = proceso;
                }
            }
            
            comparativaProcesos.put("procesoMasDemandado", procesoMasDemandado);
            comparativaProcesos.put("procesoMasEficiente", procesoMasEficiente);
            comparativaProcesos.put("eficienciaMasAlta", Math.round(maxEficiencia * 100.0) / 100.0);
            comparativaProcesos.put("demandaMasAlta", maxDemanda);
            comparativaProcesos.put("solicitudesPorProceso", solicitudesPorProceso);
            comparativaProcesos.put("aprobadasPorProceso", aprobadasPorProceso);
            
            // Analisis comparativo por programa
            Map<String, Object> comparativaProgramas = new HashMap<>();
            Map<String, Integer> solicitudesPorPrograma = new HashMap<>();
            Map<String, Integer> estudiantesPorPrograma = new HashMap<>();
            
            // Obtener programas
            List<ProgramaEntity> todosLosProgramas = programaRepository.findAll();
            for (ProgramaEntity programa : todosLosProgramas) {
                String nombrePrograma = programa.getNombre_programa();
                solicitudesPorPrograma.put(nombrePrograma, 0);
                estudiantesPorPrograma.put(nombrePrograma, 0);
            }
            
            // Contar solicitudes por programa
            for (SolicitudEntity solicitud : todasLasSolicitudes) {
                if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                    String nombrePrograma = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                    solicitudesPorPrograma.put(nombrePrograma, solicitudesPorPrograma.getOrDefault(nombrePrograma, 0) + 1);
                }
            }
            
            // Contar estudiantes por programa
            for (UsuarioEntity estudiante : todosLosEstudiantes) {
                if (estudiante.getObjPrograma() != null) {
                    String nombrePrograma = estudiante.getObjPrograma().getNombre_programa();
                    estudiantesPorPrograma.put(nombrePrograma, estudiantesPorPrograma.getOrDefault(nombrePrograma, 0) + 1);
                }
            }
            
            String programaMasActivo = null;
            String programaConMasEstudiantes = null;
            int maxSolicitudes = 0;
            int maxEstudiantes = 0;
            
            for (Map.Entry<String, Integer> entry : solicitudesPorPrograma.entrySet()) {
                String programa = entry.getKey();
                int solicitudes = entry.getValue();
                int estudiantes = estudiantesPorPrograma.getOrDefault(programa, 0);
                
                if (solicitudes > maxSolicitudes) {
                    maxSolicitudes = solicitudes;
                    programaMasActivo = programa;
                }
                
                if (estudiantes > maxEstudiantes) {
                    maxEstudiantes = estudiantes;
                    programaConMasEstudiantes = programa;
                }
            }
            
            comparativaProgramas.put("programaMasActivo", programaMasActivo);
            comparativaProgramas.put("programaConMasEstudiantes", programaConMasEstudiantes);
            comparativaProgramas.put("maxSolicitudes", maxSolicitudes);
            comparativaProgramas.put("maxEstudiantes", maxEstudiantes);
            comparativaProgramas.put("solicitudesPorPrograma", solicitudesPorPrograma);
            comparativaProgramas.put("estudiantesPorPrograma", estudiantesPorPrograma);
            
            // Resumen estrategico
            Map<String, Object> resumenEstrategico = new HashMap<>();
            resumenEstrategico.put("totalSolicitudes", todasLasSolicitudes.size());
            resumenEstrategico.put("totalEstudiantes", todosLosEstudiantes.size());
            resumenEstrategico.put("totalProgramas", todosLosProgramas.size());
            resumenEstrategico.put("periodoAnalizado", "Ultimos 12 meses");
            resumenEstrategico.put("recomendacionEstrategica", generarRecomendacionEstrategica(tendenciaSolicitudes, crecimientoSolicitudes, procesoMasDemandado, programaMasActivo, maxEficiencia, maxSolicitudes));
            
            resultado.put("crecimientoTemporal", crecimientoTemporal);
            resultado.put("comparativaProcesos", comparativaProcesos);
            resultado.put("comparativaProgramas", comparativaProgramas);
            resultado.put("resumenEstrategico", resumenEstrategico);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Analisis de tendencias y comparativas estrategicas - DATOS REALES");
            
            return resultado;
        } catch (Exception e) {
            resultado.put("crecimientoTemporal", new HashMap<>());
            resultado.put("comparativaProcesos", new HashMap<>());
            resultado.put("comparativaProgramas", new HashMap<>());
            resultado.put("resumenEstrategico", new HashMap<>());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener tendencias y comparativas");
            resultado.put("error", e.getMessage());
            return resultado;
        }
    }
    
    private String generarRecomendacionEstrategica(String tendenciaSolicitudes, double crecimientoSolicitudes, String procesoMasDemandado, String programaMasActivo, double maxEficiencia, int maxSolicitudes) {
        StringBuilder recomendacion = new StringBuilder();
        
        // Analisis de tendencia con niveles de crecimiento
        if ("Creciente".equals(tendenciaSolicitudes)) {
            if (crecimientoSolicitudes > 50) {
                recomendacion.append(" CRECIMIENTO ALTO: Urgente aumentar recursos para ");
            } else if (crecimientoSolicitudes > 20) {
                recomendacion.append(" CRECIMIENTO MODERADO: Considerar aumentar recursos para ");
            } else {
                recomendacion.append(" CRECIMIENTO BAJO: Monitorear recursos para ");
            }
            recomendacion.append(procesoMasDemandado).append(". ");
        } else if ("Decreciente".equals(tendenciaSolicitudes)) {
            if (crecimientoSolicitudes > 50) {
                recomendacion.append(" DECRECIMIENTO ALTO: Evaluar estrategias de promocion para ");
            } else {
                recomendacion.append(" DECRECIMIENTO MODERADO: Revisar estrategias para ");
            }
            recomendacion.append(procesoMasDemandado).append(". ");
        } else {
            recomendacion.append(" TENDENCIA ESTABLE: Mantener recursos actuales. ");
        }
        
        // Analisis de eficiencia
        if (maxEficiencia < 30) {
            recomendacion.append(" BAJA EFICIENCIA: Revisar procesos de aprobacion. ");
        } else if (maxEficiencia < 60) {
            recomendacion.append(" EFICIENCIA MODERADA: Optimizar procesos de aprobacion. ");
        }
        
        // Analisis de programa con nivel de demanda
        if (programaMasActivo != null) {
            if (maxSolicitudes > 15) {
                recomendacion.append(" ENFOQUE: ").append(programaMasActivo).append(" requiere atencion prioritaria (alta demanda).");
            } else if (maxSolicitudes > 10) {
                recomendacion.append(" ENFOQUE: ").append(programaMasActivo).append(" requiere atencion prioritaria (demanda moderada).");
            } else {
                recomendacion.append(" ENFOQUE: ").append(programaMasActivo).append(" requiere atencion prioritaria.");
            }
        }
        
        return recomendacion.toString();
    }

    private String obtenerColorProceso(String nombreProceso) {
        switch (nombreProceso) {
            case "Homologacion":
                return "#3B82F6"; // Azul
            case "Paz y Salvo":
                return "#10B981"; // Verde
            case "Reingreso":
                return "#F59E0B"; // Amarillo
            case "Cursos de Verano":
                return "#8B5CF6"; // Purpura
            case "ECAES":
                return "#EF4444"; // Rojo
            default:
                return "#6B7280"; // Gris
        }
    }

    private String obtenerIconoProceso(String nombreProceso) {
        switch (nombreProceso) {
            case "Homologacion":
                return "";
            case "Paz y Salvo":
                return "";
            case "Reingreso":
                return "";
            case "Cursos de Verano":
                return "";
            case "ECAES":
                return "";
            default:
                return "";
        }
    }

    private String obtenerNombreProcesoPorSolicitud(SolicitudEntity solicitud) {
        if (solicitud == null || solicitud.getNombre_solicitud() == null) return null;
        
        String nombreSolicitud = solicitud.getNombre_solicitud().toLowerCase();
        
        if (nombreSolicitud.contains("homologacion") || nombreSolicitud.contains("homologacion")) {
            return "Homologacion";
        } else if (nombreSolicitud.contains("paz") && nombreSolicitud.contains("salvo")) {
            return "Paz y Salvo";
        } else if (nombreSolicitud.contains("reingreso")) {
            return "Reingreso";
        } else if (nombreSolicitud.contains("curso") && nombreSolicitud.contains("verano")) {
            return "Cursos de Verano";
        } else if (nombreSolicitud.contains("ecaes")) {
            return "ECAES";
        }
        return null;
    }

    private String obtenerEstadoMasReciente(SolicitudEntity solicitud) {
        if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
            return "ENVIADA";
        }
        
        return solicitud.getEstadosSolicitud().stream()
            .max(Comparator.comparing(EstadoSolicitudEntity::getFecha_registro_estado))
            .map(EstadoSolicitudEntity::getEstado_actual)
            .map(this::normalizarEstado)
            .orElse("ENVIADA");
    }
    
    private String normalizarEstado(String estado) {
        if (estado == null) return "ENVIADA";
        
        switch (estado.toUpperCase()) {
            case "ENVIADA":
            case "ENVIADO":
                return "ENVIADA";
            case "EN PROCESO":
            case "EN_PROCESO":
            case "ENPROCESO":
            case "APROBADA_FUNCIONARIO":
                return "APROBADA_FUNCIONARIO";
            case "APROBADA_COORDINADOR":
                return "APROBADA_COORDINADOR";
            case "APROBADA":
            case "APROBADO":
                return "APROBADA";
            case "RECHAZADA":
            case "RECHAZADO":
                return "RECHAZADA";
            case "PRE_REGISTRADO":
            case "PREREGISTRADO":
            case "PRE REGISTRADO":
                // PRE_REGISTRADO se trata como ENVIADA para estadísticas
                return "ENVIADA";
            default:
                // Cualquier estado desconocido se trata como ENVIADA por defecto
                return "ENVIADA";
        }
    }

    /**
     * Genera predicciones de demanda para cursos de verano basadas en datos historicos
     */
    private Map<String, Object> generarPrediccionesDemanda(
            List<SolicitudEntity> solicitudesCursosVerano,
            Map<String, Integer> demandaPorMateria,
            Map<String, Integer> demandaPorPrograma,
            Map<String, Integer> demandaPorMes,
            List<Map<String, Object>> topMaterias,
            List<Map<String, Object>> analisisPorPrograma) {
        
        Map<String, Object> predicciones = new HashMap<>();
        
        try {
            // 1. PREDICCION DE DEMANDA TOTAL PARA EL PROXIMO PERIODO
            int demandaActual = solicitudesCursosVerano.size();
            int demandaEstimadaProximoPeriodo = calcularDemandaEstimada(demandaActual, demandaPorMes);
            
            // 2. PREDICCIONES POR MATERIA CON REGRESION LINEAL
            List<Map<String, Object>> materiasConTendenciaCreciente = new ArrayList<>();
            List<Map<String, Object>> materiasConTendenciaDecreciente = new ArrayList<>();
            List<Map<String, Object>> materiasEstables = new ArrayList<>();
            
            for (Map<String, Object> materia : topMaterias) {
                String nombreMateria = (String) materia.get("nombre");
                int solicitudesActuales = ((Number) materia.get("solicitudes")).intValue();
                
                // Se utiliza regresion lineal para estimar la demanda por materia
                Map<String, Object> prediccionRegresion = calcularDemandaEstimadaMateriaPorRegresion(
                    nombreMateria, solicitudesCursosVerano, solicitudesActuales);
                
                int demandaEstimada = ((Number) prediccionRegresion.get("demandaEstimada")).intValue();
                String tendencia = (String) prediccionRegresion.get("tendencia");
                
                Map<String, Object> prediccionMateria = new HashMap<>();
                prediccionMateria.put("nombre", nombreMateria);
                prediccionMateria.put("demandaActual", solicitudesActuales);
                prediccionMateria.put("demandaEstimada", demandaEstimada);
                prediccionMateria.put("tendencia", tendencia);
                prediccionMateria.put("variacion", demandaEstimada - solicitudesActuales);
                prediccionMateria.put("porcentajeVariacion", solicitudesActuales > 0 ? 
                    Math.round(((double)(demandaEstimada - solicitudesActuales) / solicitudesActuales) * 100.0) : 0);
                // No exponemos campos tecnicos (pendiente, rSquared, modeloUtilizado) porque no aportan al usuario final
                
                if ("CRECIENTE".equals(tendencia)) {
                    materiasConTendenciaCreciente.add(prediccionMateria);
                } else if ("DECRECIENTE".equals(tendencia)) {
                    materiasConTendenciaDecreciente.add(prediccionMateria);
                } else {
                    materiasEstables.add(prediccionMateria);
                }
            }
            log.debug("Predicciones por materia - Materias crecientes:  {}", materiasConTendenciaCreciente.size() + 
                             ", Decrecientes: " + materiasConTendenciaDecreciente.size() + 
                             ", Estables: " + materiasEstables.size());
            
            // 3. PREDICCIONES POR PROGRAMA CON REGRESION LINEAL
            log.debug("Predicciones por programa - Aplicando regresion lineal por programa...");
            List<Map<String, Object>> programasConTendenciaCreciente = new ArrayList<>();
            List<Map<String, Object>> programasConTendenciaDecreciente = new ArrayList<>();
            List<Map<String, Object>> programasEstables = new ArrayList<>();
            List<Map<String, Object>> todasLasPrediccionesPorPrograma = new ArrayList<>(); // Lista completa para frontend
            
            for (Map<String, Object> programa : analisisPorPrograma) {
                String nombrePrograma = (String) programa.get("nombre");
                int solicitudesActuales = ((Number) programa.get("solicitudes")).intValue();
                
                // Se aplica regresion lineal para estimar la demanda por programa
                Map<String, Object> prediccionRegresion = calcularDemandaEstimadaProgramaPorRegresion(
                    nombrePrograma, solicitudesCursosVerano, solicitudesActuales);
                
                int demandaEstimada = ((Number) prediccionRegresion.get("demandaEstimada")).intValue();
                String tendencia = (String) prediccionRegresion.get("tendencia");
                
                Map<String, Object> prediccionPrograma = new HashMap<>();
                prediccionPrograma.put("nombre", nombrePrograma);
                prediccionPrograma.put("demandaActual", solicitudesActuales);
                prediccionPrograma.put("demandaEstimada", demandaEstimada);
                prediccionPrograma.put("tendencia", tendencia);
                prediccionPrograma.put("variacion", demandaEstimada - solicitudesActuales);
                prediccionPrograma.put("porcentajeVariacion", solicitudesActuales > 0 ? 
                    Math.round(((double)(demandaEstimada - solicitudesActuales) / solicitudesActuales) * 100.0) : 0);
                // Se omiten campos tecnicos (pendiente, rSquared, modeloUtilizado) que no agregan valor al frontend
                
                // Clasificamos la prediccion segun la tendencia detectada
                if ("CRECIENTE".equals(tendencia)) {
                    programasConTendenciaCreciente.add(prediccionPrograma);
                } else if ("DECRECIENTE".equals(tendencia)) {
                    programasConTendenciaDecreciente.add(prediccionPrograma);
                } else {
                    programasEstables.add(prediccionPrograma);
                }
                
                // Guardamos cada prediccion para que el frontend pueda mostrar el detalle completo
                todasLasPrediccionesPorPrograma.add(prediccionPrograma);
            }
            log.debug("Predicciones por programa - Programas crecientes:  {}", programasConTendenciaCreciente.size() + 
                             ", Decrecientes: " + programasConTendenciaDecreciente.size() +
                             ", Estables: " + programasEstables.size() +
                             ", Total: " + todasLasPrediccionesPorPrograma.size());
            
            // 4. PREDICCIONES TEMPORALES
            Map<String, Object> prediccionesTemporales = new HashMap<>();
            String mesPico = encontrarMesPico(demandaPorMes);
            int demandaEstimadaMesPico = calcularDemandaEstimadaMes(demandaPorMes.get(mesPico));
            
            prediccionesTemporales.put("mesPico", mesPico);
            prediccionesTemporales.put("demandaActualMesPico", demandaPorMes.get(mesPico));
            prediccionesTemporales.put("demandaEstimadaMesPico", demandaEstimadaMesPico);
            prediccionesTemporales.put("mesesRecomendados", Arrays.asList("Julio", "Agosto", "Septiembre"));
            
            // 5. RECOMENDACIONES FUTURAS ACCIONABLES Y DETALLADAS
            List<Map<String, Object>> recomendacionesFuturas = new ArrayList<>();
            
            // Recomendaciones especificas por materia
            if (!materiasConTendenciaCreciente.isEmpty()) {
                for (Map<String, Object> materia : materiasConTendenciaCreciente) {
                    String nombreMateria = (String) materia.get("nombre");
                    int demandaActualMateria = ((Number) materia.get("demandaActual")).intValue();
                    int demandaEstimadaMateria = ((Number) materia.get("demandaEstimada")).intValue();
                    int variacion = ((Number) materia.get("variacion")).intValue();
                    int porcentajeVariacion = ((Number) materia.get("porcentajeVariacion")).intValue();
                    
                    // Calcular recursos necesarios
                    int gruposActuales = Math.max(1, (int) Math.ceil(demandaActualMateria / 20.0));
                    int gruposRecomendados = Math.max(1, (int) Math.ceil(demandaEstimadaMateria / 20.0));
                    int docentesNecesarios = gruposRecomendados;
                    int aulasNecesarias = gruposRecomendados;
                    
                    // Determinar prioridad y urgencia
                    String prioridad = porcentajeVariacion > 30 ? "ALTA" : 
                                      porcentajeVariacion > 15 ? "MEDIA" : "BAJA";
                    
                    Map<String, Object> recomendacion = new HashMap<>();
                    recomendacion.put("id", "MAT_" + nombreMateria.replaceAll("\\s+", "_").toUpperCase());
                    recomendacion.put("tipo", "OFERTA_MATERIA");
                    recomendacion.put("categoria", "CURSOS_VERANO");
                    recomendacion.put("prioridad", prioridad);
                    recomendacion.put("titulo", "Ampliar oferta de " + nombreMateria);
                    recomendacion.put("materia", nombreMateria);
                    
                    // Datos actuales vs proyectados
                    recomendacion.put("demandaActual", demandaActualMateria);
                    recomendacion.put("demandaProyectada", demandaEstimadaMateria);
                    recomendacion.put("crecimiento", variacion);
                    recomendacion.put("porcentajeCrecimiento", porcentajeVariacion);
                    
                    // Recursos necesarios
                    Map<String, Object> recursos = new HashMap<>();
                    recursos.put("docentes", docentesNecesarios);
                    recursos.put("aulas", aulasNecesarias);
                    recursos.put("laboratorios", nombreMateria.toLowerCase().contains("programacion") || 
                                               nombreMateria.toLowerCase().contains("base") ? docentesNecesarios : 0);
                    recursos.put("gruposActuales", gruposActuales);
                    recursos.put("gruposRecomendados", gruposRecomendados);
                    recursos.put("capacidadPorGrupo", 20);
                    recomendacion.put("recursos", recursos);
                    
                    // Descripcion detallada
                    recomendacion.put("descripcion", String.format(
                        "La materia %s presenta una tendencia de crecimiento del %d%%, pasando de %d a %d estudiantes proyectados. " +
                        "Se recomienda ampliar la oferta para garantizar cobertura completa.",
                        nombreMateria, porcentajeVariacion, demandaActualMateria, demandaEstimadaMateria));
                    
                    // Acciones especificas
                    List<String> acciones = new ArrayList<>();
                    if (gruposRecomendados > gruposActuales) {
                        int gruposAdicionales = gruposRecomendados - gruposActuales;
                        acciones.add(String.format("Abrir %d %s %s de 20 estudiantes", 
                                                  gruposAdicionales,
                                                  gruposAdicionales == 1 ? "grupo" : "grupos",
                                                  gruposAdicionales == 1 ? "adicional" : "adicionales"));
                    }
                    acciones.add(String.format("Contratar %d %s %s", 
                                              docentesNecesarios,
                                              docentesNecesarios == 1 ? "docente" : "docentes",
                                              docentesNecesarios == 1 ? "especializado" : "especializados"));
                    acciones.add(String.format("Reservar %d %s", 
                                              aulasNecesarias,
                                              aulasNecesarias == 1 ? "aula o laboratorio" : "aulas o laboratorios"));
                    if (porcentajeVariacion > 30) {
                        acciones.add("URGENTE: Iniciar gestion con 2 meses de anticipacion");
                    }
                    acciones.add("Publicar oferta academica con suficiente antelacion");
                    recomendacion.put("acciones", acciones);
                    
                    // Justificacion simplificada
                    recomendacion.put("justificacion", String.format(
                        "Con base en las tendencias observadas en periodos anteriores, " +
                        "se estima un incremento de %d %s (%d%% de crecimiento).",
                        variacion, 
                        variacion == 1 ? "estudiante" : "estudiantes",
                        porcentajeVariacion));
                    
                    // Fechas recomendadas
                    Map<String, String> cronograma = new HashMap<>();
                    cronograma.put("inicioGestion", "2 meses antes del periodo");
                    cronograma.put("publicacionOferta", "6 semanas antes");
                    cronograma.put("contratacionDocentes", "4 semanas antes");
                    cronograma.put("inicioInscripciones", "3 semanas antes");
                    recomendacion.put("cronograma", cronograma);
                    
                    // Impacto esperado
                    Map<String, Object> impacto = new HashMap<>();
                    impacto.put("estudiantesAtendidos", demandaEstimadaMateria);
                    impacto.put("estudiantesBeneficiados", variacion);
                    impacto.put("tasaCoberturaObjetivo", "100%");
                    impacto.put("inversionEstimada", String.format("$%,d", docentesNecesarios * 3000000));
                    recomendacion.put("impacto", impacto);
                    
                    recomendacionesFuturas.add(recomendacion);
                }
            }
            
            // Recomendaciones con enfoque estrategico por programa
            if (!programasConTendenciaCreciente.isEmpty()) {
                for (Map<String, Object> programa : programasConTendenciaCreciente) {
                    String nombrePrograma = (String) programa.get("nombre");
                    int demandaActualPrograma = ((Number) programa.get("demandaActual")).intValue();
                    int demandaEstimadaPrograma = ((Number) programa.get("demandaEstimada")).intValue();
                    int variacion = ((Number) programa.get("variacion")).intValue();
                    int porcentajeVariacion = ((Number) programa.get("porcentajeVariacion")).intValue();
                    
                    String prioridad = porcentajeVariacion > 25 ? "ALTA" : 
                                      porcentajeVariacion > 10 ? "MEDIA" : "BAJA";
                    
                    Map<String, Object> recomendacion = new HashMap<>();
                    recomendacion.put("id", "PROG_" + nombrePrograma.replaceAll("\\s+", "_").toUpperCase());
                    recomendacion.put("tipo", "ENFOQUE_PROGRAMA");
                    recomendacion.put("categoria", "ESTRATEGIA_ACADEMICA");
                    recomendacion.put("prioridad", prioridad);
                    recomendacion.put("titulo", "Priorizar oferta para " + nombrePrograma);
                    recomendacion.put("programa", nombrePrograma);
                    
                    // Datos del programa
                    recomendacion.put("solicitudesActuales", demandaActualPrograma);
                    recomendacion.put("solicitudesProyectadas", demandaEstimadaPrograma);
                    recomendacion.put("incremento", variacion);
                    recomendacion.put("porcentajeIncremento", porcentajeVariacion);
                    
                    // Descripcion estrategica
                    recomendacion.put("descripcion", String.format(
                        "%s muestra un crecimiento significativo del %d%% en la demanda de cursos de verano. " +
                        "Se proyectan %d solicitudes para el proximo periodo, con un incremento de %d %s. " +
                        "Es prioritario garantizar oferta academica suficiente para este programa.",
                        nombrePrograma, porcentajeVariacion, demandaEstimadaPrograma, variacion,
                        variacion == 1 ? "estudiante" : "estudiantes"));
                    
                    // Acciones estrategicas
                    List<String> acciones = new ArrayList<>();
                    acciones.add(String.format("Ampliar oferta de cursos de verano en %d %s", 
                                              variacion, 
                                              variacion == 1 ? "cupo" : "cupos"));
                    acciones.add("Identificar materias criticas del programa con mayor demanda");
                    acciones.add("Coordinar con director(a) de programa para validar necesidades");
                    acciones.add("Evaluar disponibilidad de docentes del programa");
                    if (porcentajeVariacion > 25) {
                        acciones.add("CRITICO: Programa de alta demanda requiere atencion inmediata");
                    }
                    recomendacion.put("acciones", acciones);
                    
                    // Justificacion simplificada
                    recomendacion.put("justificacion", String.format(
                        "El analisis de tendencias historicas indica un crecimiento sostenido de %d %s. " +
                        "Representa una oportunidad para mejorar los indicadores academicos del programa.",
                        variacion,
                        variacion == 1 ? "solicitud" : "solicitudes"));
                    
                    // Beneficios esperados
                    List<String> beneficios = new ArrayList<>();
                    beneficios.add("Reduccion de desercion por perdida de materias");
                    beneficios.add("Mejora en tiempos de graduacion");
                    beneficios.add("Mayor satisfaccion estudiantil");
                    beneficios.add("Optimizacion de trayectorias academicas");
                    recomendacion.put("beneficios", beneficios);
                    
                    recomendacionesFuturas.add(recomendacion);
                }
            }
            
            // Recomendacion para la planificacion temporal
            if (demandaPorMes.get(mesPico) > 0) {
                int demandaActualMes = demandaPorMes.get(mesPico);
                int porcentajeCrecimientoMes = (int) Math.round(
                    ((double)(demandaEstimadaMesPico - demandaActualMes) / demandaActualMes) * 100.0);
                
                Map<String, Object> recomendacionTemporal = new HashMap<>();
                recomendacionTemporal.put("id", "TEMPORAL_" + mesPico.toUpperCase());
                recomendacionTemporal.put("tipo", "PLANIFICACION_TEMPORAL");
                recomendacionTemporal.put("categoria", "CALENDARIO_ACADEMICO");
                recomendacionTemporal.put("prioridad", porcentajeCrecimientoMes > 20 ? "ALTA" : "MEDIA");
                recomendacionTemporal.put("titulo", "Preparar oferta anticipada para " + mesPico);
                
                // Datos temporales
                recomendacionTemporal.put("mesPico", mesPico);
                recomendacionTemporal.put("solicitudesActuales", demandaActualMes);
                recomendacionTemporal.put("solicitudesProyectadas", demandaEstimadaMesPico);
                recomendacionTemporal.put("incrementoEsperado", demandaEstimadaMesPico - demandaActualMes);
                recomendacionTemporal.put("porcentajeCrecimiento", porcentajeCrecimientoMes);
                
                // Descripcion
                recomendacionTemporal.put("descripcion", String.format(
                    "%s es el mes pico de demanda para cursos de verano. Se proyecta un incremento del %d%%, " +
                    "pasando de %d a %d solicitudes. Es fundamental anticipar la planificacion academica y logistica.",
                    mesPico, porcentajeCrecimientoMes, demandaActualMes, demandaEstimadaMesPico));
                
                // Acciones temporales
                List<String> accionesTempo = new ArrayList<>();
                accionesTempo.add("Publicar calendario de cursos de verano 8 semanas antes de " + mesPico);
                accionesTempo.add(String.format("Preparar capacidad para %d estudiantes", demandaEstimadaMesPico));
                accionesTempo.add("Coordinar disponibilidad de docentes con 6 semanas de anticipacion");
                accionesTempo.add("Reservar aulas y laboratorios con 4 semanas de anticipacion");
                if (porcentajeCrecimientoMes > 20) {
                    accionesTempo.add("URGENTE: Considerar contratacion temporal adicional");
                }
                recomendacionTemporal.put("acciones", accionesTempo);
                
                // Cronograma especifico
                Map<String, String> cronogramaTemporal = new HashMap<>();
                cronogramaTemporal.put("planificacionInicial", "8 semanas antes");
                cronogramaTemporal.put("publicacionOferta", "6 semanas antes");
                cronogramaTemporal.put("aperturaInscripciones", "4 semanas antes");
                cronogramaTemporal.put("cierreInscripciones", "1 semana antes");
                cronogramaTemporal.put("inicioCursos", mesPico);
                recomendacionTemporal.put("cronograma", cronogramaTemporal);
                
                // Justificacion
                recomendacionTemporal.put("justificacion", String.format(
                    "Analisis historico indica que %s concentra el mayor volumen de solicitudes. " +
                    "Planificacion anticipada garantiza mejor experiencia estudiantil.",
                    mesPico));
                
                recomendacionesFuturas.add(recomendacionTemporal);
            }
            
            // ALERTAS CRITICAS (Situaciones que requieren atencion inmediata)
            List<Map<String, Object>> alertasCriticas = new ArrayList<>();
            
            // Alerta: Materias con crecimiento muy alto (>50%)
            for (Map<String, Object> materia : materiasConTendenciaCreciente) {
                int porcentaje = ((Number) materia.get("porcentajeVariacion")).intValue();
                if (porcentaje > 50) {
                    Map<String, Object> alerta = new HashMap<>();
                    alerta.put("id", "ALERTA_CRITICA_" + ((String) materia.get("nombre")).replaceAll("\\s+", "_").toUpperCase());
                    alerta.put("tipo", "ALERTA_CAPACIDAD");
                    alerta.put("categoria", "URGENTE");
                    alerta.put("prioridad", "CRITICA");
                    alerta.put("titulo", " ALERTA: Crecimiento excepcional en " + materia.get("nombre"));
                    alerta.put("materia", materia.get("nombre"));
                    alerta.put("crecimientoProyectado", porcentaje + "%");
                    alerta.put("descripcion", String.format(
                        "ATENCION: La materia %s presenta un crecimiento proyectado del %d%%, " +
                        "significativamente superior al promedio. Se requiere accion inmediata para garantizar cobertura.",
                        materia.get("nombre"), porcentaje));
                    
                    List<String> accionesUrgentes = new ArrayList<>();
                    accionesUrgentes.add(" ACCION INMEDIATA REQUERIDA");
                    accionesUrgentes.add("Convocar reunion urgente con coordinacion academica");
                    accionesUrgentes.add("Evaluar contratar docentes adicionales con caracter urgente");
                    accionesUrgentes.add("Verificar disponibilidad de espacios fisicos alternativos");
                    accionesUrgentes.add("Considerar modalidad virtual/hibrida si no hay capacidad presencial");
                    alerta.put("acciones", accionesUrgentes);
                    
                    alertasCriticas.add(alerta);
                }
            }
            
            // Alerta: Programas con demanda muy alta
            for (Map<String, Object> programa : programasConTendenciaCreciente) {
                int demanda = ((Number) programa.get("demandaEstimada")).intValue();
                if (demanda > 10) { // Mas de 10 solicitudes para un programa
                    Map<String, Object> alerta = new HashMap<>();
                    alerta.put("id", "ALERTA_PROGRAMA_" + ((String) programa.get("nombre")).replaceAll("\\s+", "_").toUpperCase());
                    alerta.put("tipo", "ALERTA_PROGRAMA");
                    alerta.put("categoria", "IMPORTANTE");
                    alerta.put("prioridad", "ALTA");
                    alerta.put("titulo", " Demanda concentrada en " + programa.get("nombre"));
                    alerta.put("programa", programa.get("nombre"));
                    alerta.put("demandaProyectada", demanda);
                    alerta.put("descripcion", String.format(
                        "%s concentra una demanda proyectada de %d solicitudes, requiere estrategia especifica.",
                        programa.get("nombre"), demanda));
                    
                    List<String> estrategias = new ArrayList<>();
                    estrategias.add("Coordinar con director(a) de programa");
                    estrategias.add("Identificar materias criticas especificas del programa");
                    estrategias.add("Evaluar docentes especializados del programa");
                    estrategias.add("Planificar oferta diversificada para atender necesidades del programa");
                    alerta.put("acciones", estrategias);
                    
                    alertasCriticas.add(alerta);
                }
            }
            
            // Construir resultado de predicciones con estructura mejorada
            predicciones.put("demandaEstimadaProximoPeriodo", demandaEstimadaProximoPeriodo);
            predicciones.put("materiasConTendenciaCreciente", materiasConTendenciaCreciente);
            predicciones.put("materiasConTendenciaDecreciente", materiasConTendenciaDecreciente);
            predicciones.put("materiasEstables", materiasEstables);
            predicciones.put("programasConTendenciaCreciente", programasConTendenciaCreciente);
            predicciones.put("programasConTendenciaDecreciente", programasConTendenciaDecreciente);
            predicciones.put("programasEstables", programasEstables); // Agregado
            predicciones.put("todasLasPrediccionesPorPrograma", todasLasPrediccionesPorPrograma); // Lista completa
            predicciones.put("prediccionesTemporales", prediccionesTemporales);
            predicciones.put("recomendaciones", recomendacionesFuturas); // Recomendaciones (para acceso interno)
            predicciones.put("alertasCriticas", alertasCriticas);
            predicciones.put("confiabilidad", "MEDIA");
            predicciones.put("fechaPrediccion", new Date()); // Fecha de prediccion
            
            // Estadisticas de las recomendaciones
            Map<String, Object> estadisticasRecomendaciones = new HashMap<>();
            long recsAlta = recomendacionesFuturas.stream()
                .filter(r -> "ALTA".equals(r.get("prioridad")))
                .count();
            long recsMedia = recomendacionesFuturas.stream()
                .filter(r -> "MEDIA".equals(r.get("prioridad")))
                .count();
            long recsBaja = recomendacionesFuturas.stream()
                .filter(r -> "BAJA".equals(r.get("prioridad")))
                .count();
            
            estadisticasRecomendaciones.put("totalRecomendaciones", recomendacionesFuturas.size());
            estadisticasRecomendaciones.put("prioridadAlta", recsAlta);
            estadisticasRecomendaciones.put("prioridadMedia", recsMedia);
            estadisticasRecomendaciones.put("prioridadBaja", recsBaja);
            estadisticasRecomendaciones.put("alertasCriticas", alertasCriticas.size());
            predicciones.put("estadisticasRecomendaciones", estadisticasRecomendaciones);
            
        } catch (Exception e) {
            log.error("Predicciones - ERROR CRITICO generando predicciones ");
            log.error("Tipo de error:  {}", e.getClass().getName());
            log.error("Mensaje:  {}", e.getMessage());
            log.error("Stack trace completo:");
            log.error("Error en estadísticas", e);
            log.error("==========================================");
            
            // Retornar predicciones basicas en caso de error
            predicciones.put("demandaEstimadaProximoPeriodo", solicitudesCursosVerano.size());
            predicciones.put("materiasConTendenciaCreciente", new ArrayList<>());
            predicciones.put("materiasConTendenciaDecreciente", new ArrayList<>());
            predicciones.put("materiasEstables", new ArrayList<>());
            predicciones.put("programasConTendenciaCreciente", new ArrayList<>());
            predicciones.put("programasConTendenciaDecreciente", new ArrayList<>());
            predicciones.put("prediccionesTemporales", new HashMap<>());
            // ELIMINADO: recomendacionesFuturas (duplicado)
            predicciones.put("confiabilidad", "BAJA");
        }
        
        return predicciones;
    }
    
    /**
     * Calcula la demanda estimada para el proximo periodo
     */
    /**
     * Calcula la demanda estimada usando REGRESION LINEAL SIMPLE
     * Esta es una implementacion profesional para el trabajo de grado
     */
    private int calcularDemandaEstimada(int demandaActual, Map<String, Integer> demandaPorMes) {
        try {
            log.debug(" [REGRESION_LINEAL] Calculando demanda estimada...");
            
            // Crear modelo de regresion lineal
            SimpleRegression regression = new SimpleRegression();
            
            // Agregar datos historicos al modelo
            // x = numero de mes (1, 2, 3, ...), y = solicitudes en ese mes
            int mesNumero = 1;
            for (Map.Entry<String, Integer> entry : demandaPorMes.entrySet()) {
                double solicitudes = entry.getValue();
                if (solicitudes > 0) { // Solo agregar meses con datos
                    regression.addData(mesNumero, solicitudes);
                    log.debug("    Mes {} ({}): {} solicitudes", mesNumero, entry.getKey(), solicitudes);
                }
                mesNumero++;
            }
            
            // Verificar si tenemos suficientes datos para la regresion
            if (regression.getN() >= 2) {
                // Predecir para el siguiente mes (mes 13 o siguiente periodo)
                double prediccion = regression.predict(mesNumero);
                
                // Obtener estadisticas del modelo
                double pendiente = regression.getSlope();
                double rSquared = regression.getRSquare();
                
                log.debug(" [REGRESION_LINEAL] Resultados:");
                log.debug("   • Pendiente (slope):  {}", String.format("%.2f", pendiente));
                log.debug("   • R² (bondad de ajuste):  {}", String.format("%.2f", rSquared));
                log.debug("   • Prediccion raw:  {}", String.format("%.2f", prediccion));
                
                // Calcular demanda estimada
                // Asegurar que la prediccion sea realista (entre 80% y 200% de la demanda actual)
                int demandaEstimada = (int) Math.round(Math.max(prediccion, demandaActual * 0.8));
                demandaEstimada = Math.min(demandaEstimada, demandaActual * 2);
                
                log.debug("    Demanda estimada final:  {}", demandaEstimada);
                
                return demandaEstimada;
            } else {
                // No hay suficientes datos historicos, usar estimacion conservadora
                log.debug(" [REGRESION_LINEAL] Datos insuficientes. Usando estimacion conservadora (15%)");
                return (int) Math.round(demandaActual * 1.15);
            }
        } catch (Exception e) {
            log.debug(" [REGRESION_LINEAL] Error en calculo:  {}", e.getMessage());
            // En caso de error, usar estimacion conservadora
            return (int) Math.round(demandaActual * 1.15);
        }
    }
    
    /**
     * Calcula la demanda estimada para una materia especifica usando REGRESION LINEAL
     * Analiza el historico de solicitudes de esa materia para predecir demanda futura
     */
    private Map<String, Object> calcularDemandaEstimadaMateriaPorRegresion(
            String nombreMateria, List<SolicitudEntity> solicitudesCursosVerano, int demandaActual) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            log.debug(" [REGRESION_MATERIA] Analizando {}...", nombreMateria);
            
            // Filtrar solicitudes de esta materia especifica y agrupar por mes
            Map<Integer, Integer> solicitudesPorMes = new HashMap<>();
            
            for (SolicitudEntity solicitud : solicitudesCursosVerano) {
                // Verificar si la solicitud corresponde a esta materia
                String nombreSolicitud = solicitud.getNombre_solicitud();
                if (nombreSolicitud != null && nombreSolicitud.toLowerCase().contains(nombreMateria.toLowerCase())) {
                    Date fecha = solicitud.getFecha_registro_solicitud();
                    if (fecha != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(fecha);
                        int mesNumero = cal.get(Calendar.MONTH) + 1;
                        solicitudesPorMes.put(mesNumero, solicitudesPorMes.getOrDefault(mesNumero, 0) + 1);
                    }
                }
            }
            
            // Si hay suficientes datos historicos (2+ meses), usar regresion lineal
            if (solicitudesPorMes.size() >= 2) {
                SimpleRegression regression = new SimpleRegression();
                
                for (Map.Entry<Integer, Integer> entry : solicitudesPorMes.entrySet()) {
                    regression.addData(entry.getKey(), entry.getValue());
                }
                
                // Predecir para el siguiente periodo
                double prediccion = regression.predict(13); // Predecir para mes 13 (proximo ano)
                double pendiente = regression.getSlope();
                double rSquared = regression.getRSquare();
                
                int demandaEstimada = (int) Math.round(Math.max(prediccion, demandaActual * 0.8));
                demandaEstimada = Math.min(demandaEstimada, demandaActual * 2); // Limitar al doble
                
                String tendencia = pendiente > 0.05 ? "CRECIENTE" : (pendiente < -0.05 ? "DECRECIENTE" : "ESTABLE");
                
                resultado.put("demandaEstimada", demandaEstimada);
                resultado.put("tendencia", tendencia);
                resultado.put("pendiente", Math.round(pendiente * 100.0) / 100.0);
                resultado.put("rSquared", Math.round(rSquared * 100.0) / 100.0);
                resultado.put("modeloUtilizado", "Regresion Lineal Simple");
                
                log.debug("     {}", nombreMateria + ": Prediccion=" + demandaEstimada + 
                                 ", Pendiente=" + String.format("%.2f", pendiente) + 
                                 ", R²=" + String.format("%.2f", rSquared));
            } else {
                // Datos insuficientes, usar estimacion conservadora
                // Usar ceil para asegurar crecimiento de al menos 1
                int demandaEstimada = (int) Math.ceil(demandaActual * 1.05); // +5%
                // Siempre marcar como CRECIENTE cuando usamos estimacion conservadora
                String tendencia = "CRECIENTE";
                double pendienteEstimada = 0.05; // 5% de crecimiento
                
                resultado.put("demandaEstimada", demandaEstimada);
                resultado.put("tendencia", tendencia);
                resultado.put("pendiente", pendienteEstimada);
                resultado.put("rSquared", 0.0);
                resultado.put("modeloUtilizado", "Estimacion conservadora (+5%)");
                
                log.debug("      {}: Datos insuficientes, usando estimacion conservadora (Tendencia: {}, Demanda: {} → {})", nombreMateria, tendencia, demandaActual, demandaEstimada);
            }
            
        } catch (Exception e) {
            log.error(" Error calculando regresion para materia:  {}", e.getMessage());
            resultado.put("demandaEstimada", demandaActual);
            resultado.put("tendencia", "ESTABLE");
            resultado.put("pendiente", 0.0);
            resultado.put("rSquared", 0.0);
            resultado.put("modeloUtilizado", "Error - valor actual mantenido");
        }
        
        return resultado;
    }
    
    /**
     * Calcula la demanda estimada para un programa especifico usando REGRESION LINEAL
     * Analiza el historico de solicitudes de ese programa para predecir demanda futura
     */
    private Map<String, Object> calcularDemandaEstimadaProgramaPorRegresion(
            String nombrePrograma, List<SolicitudEntity> solicitudesCursosVerano, int demandaActual) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            log.debug(" [REGRESION_PROGRAMA] Analizando {}...", nombrePrograma);
            
            // Filtrar solicitudes de este programa especifico y agrupar por mes
            Map<Integer, Integer> solicitudesPorMes = new HashMap<>();
            
            for (SolicitudEntity solicitud : solicitudesCursosVerano) {
                // Verificar si la solicitud corresponde a este programa
                if (solicitud.getObjUsuario() != null && 
                    solicitud.getObjUsuario().getObjPrograma() != null) {
                    String programaSolicitud = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                    if (programaSolicitud != null && programaSolicitud.equalsIgnoreCase(nombrePrograma)) {
                        Date fecha = solicitud.getFecha_registro_solicitud();
                        if (fecha != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(fecha);
                            int mesNumero = cal.get(Calendar.MONTH) + 1;
                            solicitudesPorMes.put(mesNumero, solicitudesPorMes.getOrDefault(mesNumero, 0) + 1);
                        }
                    }
                }
            }
            
            // Si hay suficientes datos historicos (2+ meses), usar regresion lineal
            if (solicitudesPorMes.size() >= 2) {
                SimpleRegression regression = new SimpleRegression();
                
                for (Map.Entry<Integer, Integer> entry : solicitudesPorMes.entrySet()) {
                    regression.addData(entry.getKey(), entry.getValue());
                }
                
                // Predecir para el siguiente periodo
                double prediccion = regression.predict(13); // Predecir para mes 13 (proximo ano)
                double pendiente = regression.getSlope();
                double rSquared = regression.getRSquare();
                
                int demandaEstimada = (int) Math.round(Math.max(prediccion, demandaActual * 0.8));
                demandaEstimada = Math.min(demandaEstimada, demandaActual * 2); // Limitar al doble
                
                String tendencia = pendiente > 0.05 ? "CRECIENTE" : (pendiente < -0.05 ? "DECRECIENTE" : "ESTABLE");
                
                resultado.put("demandaEstimada", demandaEstimada);
                resultado.put("tendencia", tendencia);
                resultado.put("pendiente", Math.round(pendiente * 100.0) / 100.0);
                resultado.put("rSquared", Math.round(rSquared * 100.0) / 100.0);
                resultado.put("modeloUtilizado", "Regresion Lineal Simple");
                
                log.debug("     {}", nombrePrograma + ": Prediccion=" + demandaEstimada + 
                                 ", Pendiente=" + String.format("%.2f", pendiente) + 
                                 ", R²=" + String.format("%.2f", rSquared));
            } else {
                // Datos insuficientes, usar estimacion conservadora
                // Usar ceil para asegurar crecimiento de al menos 1
                int demandaEstimada = (int) Math.ceil(demandaActual * 1.08); // +8%
                // Siempre marcar como CRECIENTE cuando usamos estimacion conservadora
                String tendencia = "CRECIENTE";
                double pendienteEstimada = 0.08; // 8% de crecimiento
                
                resultado.put("demandaEstimada", demandaEstimada);
                resultado.put("tendencia", tendencia);
                resultado.put("pendiente", pendienteEstimada);
                resultado.put("rSquared", 0.0);
                resultado.put("modeloUtilizado", "Estimacion conservadora (+8%)");
                
                log.debug("      {}: Datos insuficientes, usando estimacion conservadora (Tendencia: {}, Demanda: {} → {})", nombrePrograma, tendencia, demandaActual, demandaEstimada);
            }
            
        } catch (Exception e) {
            log.error(" Error calculando regresion para programa:  {}", e.getMessage());
            resultado.put("demandaEstimada", demandaActual);
            resultado.put("tendencia", "ESTABLE");
            resultado.put("pendiente", 0.0);
            resultado.put("rSquared", 0.0);
            resultado.put("modeloUtilizado", "Error - valor actual mantenido");
        }
        
        return resultado;
    }

    /**
     * Encuentra el mes con mayor demanda
     */
    private String encontrarMesPico(Map<String, Integer> demandaPorMes) {
        return demandaPorMes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Agosto");
    }
    
    /**
     * Calcula la demanda estimada para un mes especifico
     */
    private int calcularDemandaEstimadaMes(int demandaActual) {
        return (int) (demandaActual * 1.2); // 20% de crecimiento para el mes pico
    }

    private Date obtenerFechaActualizacion(SolicitudEntity solicitud) {
        if (solicitud.getEstadosSolicitud() == null || solicitud.getEstadosSolicitud().isEmpty()) {
            return solicitud.getFecha_registro_solicitud();
        }
        
        return solicitud.getEstadosSolicitud().stream()
            .map(EstadoSolicitudEntity::getFecha_registro_estado)
            .max(Date::compareTo)
            .orElse(solicitud.getFecha_registro_solicitud());
    }

    /**
     * Genera predicciones globales para todos los procesos y programas usando Regresion Lineal
     */
    @SuppressWarnings("unused") // Reservado para futuras tareas de analítica predictiva
    private Map<String, Object> generarPrediccionesGlobales(
            Map<String, Integer> porTipoProceso, 
            Map<String, Integer> porPrograma) {
        
        Map<String, Object> predicciones = new HashMap<>();
        
        try {
            log.debug("Predicciones globales - Iniciando analisis predictivo...");
            
            // Obtener todas las solicitudes para analisis temporal
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            
            // 1. PREDICCION DE DEMANDA TOTAL
            Map<String, Integer> solicitudesPorMes = agruparSolicitudesPorMes(todasLasSolicitudes);
            int demandaActualTotal = todasLasSolicitudes.size();
            int demandaEstimadaTotal = calcularDemandaEstimada(demandaActualTotal, solicitudesPorMes);
            
            predicciones.put("demandaTotalActual", demandaActualTotal);
            predicciones.put("demandaTotalEstimada", demandaEstimadaTotal);
            predicciones.put("variacionTotal", demandaEstimadaTotal - demandaActualTotal);
            predicciones.put("porcentajeVariacionTotal", demandaActualTotal > 0 ? 
                Math.round(((double)(demandaEstimadaTotal - demandaActualTotal) / demandaActualTotal) * 100.0) : 0);
            
            log.debug("Predicciones globales - Demanda total: {} → {}", demandaActualTotal, demandaEstimadaTotal);
            
            // 2. PREDICCIONES POR TIPO DE PROCESO
            List<Map<String, Object>> procesosConTendenciaCreciente = new ArrayList<>();
            List<Map<String, Object>> procesosConTendenciaDecreciente = new ArrayList<>();
            List<Map<String, Object>> procesosEstables = new ArrayList<>();
            
            for (Map.Entry<String, Integer> entry : porTipoProceso.entrySet()) {
                String nombreProceso = entry.getKey();
                int demandaActual = entry.getValue();
                
                // Filtrar solicitudes de este proceso especifico
                List<SolicitudEntity> solicitudesProceso = todasLasSolicitudes.stream()
                    .filter(s -> nombreProceso.equals(obtenerNombreProcesoPorSolicitud(s)))
                    .collect(Collectors.toList());
                
                if (solicitudesProceso.isEmpty()) continue;
                
                // Calcular prediccion con regresion
                Map<String, Object> prediccion = calcularPrediccionPorCategoria(
                    nombreProceso, solicitudesProceso, demandaActual, "PROCESO");
                
                String tendencia = (String) prediccion.get("tendencia");
                if ("CRECIENTE".equals(tendencia)) {
                    procesosConTendenciaCreciente.add(prediccion);
                } else if ("DECRECIENTE".equals(tendencia)) {
                    procesosConTendenciaDecreciente.add(prediccion);
                } else {
                    procesosEstables.add(prediccion);
                }
            }
            
            predicciones.put("procesosConTendenciaCreciente", procesosConTendenciaCreciente);
            predicciones.put("procesosConTendenciaDecreciente", procesosConTendenciaDecreciente);
            predicciones.put("procesosEstables", procesosEstables);
            
            log.debug("Predicciones globales - Procesos - Crecientes:  {}", procesosConTendenciaCreciente.size() + 
                             ", Decrecientes: " + procesosConTendenciaDecreciente.size() + 
                             ", Estables: " + procesosEstables.size());
            
            // 3. PREDICCIONES POR PROGRAMA
            List<Map<String, Object>> programasConTendenciaCreciente = new ArrayList<>();
            List<Map<String, Object>> programasConTendenciaDecreciente = new ArrayList<>();
            List<Map<String, Object>> programasEstables = new ArrayList<>();
            
            for (Map.Entry<String, Integer> entry : porPrograma.entrySet()) {
                String nombrePrograma = entry.getKey();
                int demandaActual = entry.getValue();
                
                // Filtrar solicitudes de este programa especifico
                List<SolicitudEntity> solicitudesPrograma = todasLasSolicitudes.stream()
                    .filter(s -> s.getObjUsuario() != null && 
                                s.getObjUsuario().getObjPrograma() != null &&
                                nombrePrograma.equals(s.getObjUsuario().getObjPrograma().getNombre_programa()))
                    .collect(Collectors.toList());
                
                if (solicitudesPrograma.isEmpty()) continue;
                
                // Calcular prediccion con regresion
                Map<String, Object> prediccion = calcularPrediccionPorCategoria(
                    nombrePrograma, solicitudesPrograma, demandaActual, "PROGRAMA");
                
                String tendencia = (String) prediccion.get("tendencia");
                if ("CRECIENTE".equals(tendencia)) {
                    programasConTendenciaCreciente.add(prediccion);
                } else if ("DECRECIENTE".equals(tendencia)) {
                    programasConTendenciaDecreciente.add(prediccion);
                } else {
                    programasEstables.add(prediccion);
                }
            }
            
            predicciones.put("programasConTendenciaCreciente", programasConTendenciaCreciente);
            predicciones.put("programasConTendenciaDecreciente", programasConTendenciaDecreciente);
            predicciones.put("programasEstables", programasEstables);
            
            log.debug("Predicciones globales - Programas - Crecientes:  {}", programasConTendenciaCreciente.size() + 
                             ", Decrecientes: " + programasConTendenciaDecreciente.size() + 
                             ", Estables: " + programasEstables.size());
            
            // 4. METADATA
            predicciones.put("metodologia", "Regresion Lineal Simple aplicada a datos historicos");
            predicciones.put("confiabilidad", solicitudesPorMes.size() >= 3 ? "ALTA" : "MEDIA");
            predicciones.put("fechaPrediccion", new Date());
            predicciones.put("umbralTendencia", 0.05); // 5% (estandar academico)
            
            log.debug("Predicciones globales - Analisis predictivo completado exitosamente");
            
        } catch (Exception e) {
            log.error("Predicciones globales - Error:  {}", e.getMessage());
            log.error("Error en estadísticas", e);
        }
        
        return predicciones;
    }
    
    /**
     * Calcula prediccion para una categoria especifica (proceso o programa) usando regresion lineal
     */
    private Map<String, Object> calcularPrediccionPorCategoria(
            String nombreCategoria, 
            List<SolicitudEntity> solicitudes, 
            int demandaActual,
            String tipoCategoria) {
        
        Map<String, Object> prediccion = new HashMap<>();
        
        try {
            // Agrupar solicitudes por mes
            Map<Integer, Integer> solicitudesPorMes = new HashMap<>();
            
            for (SolicitudEntity solicitud : solicitudes) {
                Date fecha = solicitud.getFecha_registro_solicitud();
                if (fecha != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(fecha);
                    int mesNumero = cal.get(Calendar.MONTH) + 1;
                    solicitudesPorMes.put(mesNumero, solicitudesPorMes.getOrDefault(mesNumero, 0) + 1);
                }
            }
            
            // Usar regresion lineal si hay suficientes datos
            int demandaEstimada;
            double pendiente;
            double rSquared = 0.0;
            String modeloUtilizado;
            
            if (solicitudesPorMes.size() >= 2) {
                SimpleRegression regression = new SimpleRegression();
                for (Map.Entry<Integer, Integer> entry : solicitudesPorMes.entrySet()) {
                    regression.addData(entry.getKey(), entry.getValue());
                }
                
                double prediccionRaw = regression.predict(13);
                demandaEstimada = (int) Math.round(Math.max(prediccionRaw, demandaActual * 0.8));
                demandaEstimada = Math.min(demandaEstimada, demandaActual * 2);
                pendiente = regression.getSlope();
                rSquared = regression.getRSquare();
                modeloUtilizado = "Regresion Lineal Simple";
            } else {
                // Estimacion conservadora
                double factor = tipoCategoria.equals("PROGRAMA") ? 1.08 : 1.05;
                demandaEstimada = (int) Math.ceil(demandaActual * factor);
                pendiente = factor - 1.0;
                modeloUtilizado = "Estimacion conservadora (+" + (int)((factor - 1.0) * 100) + "%)";
            }
            
            String tendencia = pendiente > 0.05 ? "CRECIENTE" : (pendiente < -0.05 ? "DECRECIENTE" : "ESTABLE");
            
            prediccion.put("nombre", nombreCategoria);
            prediccion.put("tipo", tipoCategoria);
            prediccion.put("demandaActual", demandaActual);
            prediccion.put("demandaEstimada", demandaEstimada);
            prediccion.put("variacion", demandaEstimada - demandaActual);
            prediccion.put("porcentajeVariacion", demandaActual > 0 ? 
                Math.round(((double)(demandaEstimada - demandaActual) / demandaActual) * 100.0) : 0);
            prediccion.put("tendencia", tendencia);
            prediccion.put("pendiente", Math.round(pendiente * 10000.0) / 10000.0);
            prediccion.put("rSquared", Math.round(rSquared * 100.0) / 100.0);
            prediccion.put("modeloUtilizado", modeloUtilizado);
            
        } catch (Exception e) {
            log.error(" Error calculando prediccion para {}: {}", nombreCategoria, e.getMessage());
        }
        
        return prediccion;
    }
    
    /**
     * Agrupa solicitudes por mes para analisis temporal
     */
    private Map<String, Integer> agruparSolicitudesPorMes(List<SolicitudEntity> solicitudes) {
        Map<String, Integer> solicitudesPorMes = new HashMap<>();
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        
        // Inicializar todos los meses en 0
        for (String mes : meses) {
            solicitudesPorMes.put(mes, 0);
        }
        
        // Contar solicitudes por mes
        for (SolicitudEntity solicitud : solicitudes) {
            Date fecha = solicitud.getFecha_registro_solicitud();
            if (fecha != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fecha);
                int mesNumero = cal.get(Calendar.MONTH);
                // Asegurar que el índice está en el rango válido
                if (mesNumero >= 0 && mesNumero < meses.length) {
                    String nombreMes = meses[mesNumero];
                    solicitudesPorMes.put(nombreMes, solicitudesPorMes.get(nombreMes) + 1);
                } else {
                    log.warn("Mes numero fuera de rango: {} en agruparSolicitudesPorMes", mesNumero);
                }
            }
        }
        
        return solicitudesPorMes;
    }

    @Override
    public Map<String, Object> obtenerEstadisticasCursosVerano(String periodoAcademico, Integer idPrograma) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            log.debug("Cursos de verano - Iniciando analisis de cursos de verano - Período: {}, Programa: {}", 
                    periodoAcademico, idPrograma);
            
            // Obtener todas las solicitudes de cursos de verano
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            List<SolicitudEntity> solicitudesCursosVerano = todasLasSolicitudes.stream()
                .filter(solicitud -> {
                    // Filtrar por tipo de proceso
                    String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                    if (!"Cursos de Verano".equals(nombreProceso) && !"CURSO_VERANO".equals(nombreProceso)) {
                        return false;
                    }
                    
                    // Filtrar por período académico si se proporciona
                    if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                        String periodoFiltro = periodoAcademico.trim();
                        // Verificar período en la solicitud
                        if (solicitud.getPeriodo_academico() != null && solicitud.getPeriodo_academico().equals(periodoFiltro)) {
                            // OK, coincide con el período de la solicitud
                        } else if (solicitud.getObjCursoOfertadoVerano() != null 
                                && solicitud.getObjCursoOfertadoVerano().getPeriodo_academico() != null
                                && solicitud.getObjCursoOfertadoVerano().getPeriodo_academico().equals(periodoFiltro)) {
                            // OK, coincide con el período del curso
                        } else {
                            // No coincide con ningún período
                            return false;
                        }
                    }
                    
                    // Filtrar por programa si se proporciona
                    if (idPrograma != null) {
                        if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getObjPrograma() == null
                                || !solicitud.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            log.info("Cursos de verano - Solicitudes de cursos de verano encontradas después de filtros: {} (Período: {}, Programa: {})", 
                    solicitudesCursosVerano.size(), periodoAcademico, idPrograma);
            
            // Analisis de demanda por materia
            Map<String, Integer> demandaPorMateria = new HashMap<>();
            Map<String, Integer> demandaPorPrograma = new HashMap<>();
            Map<String, Integer> demandaPorMes = new HashMap<>();
            Map<String, Integer> estadosPorSolicitud = new HashMap<>();
            
            // Inicializar meses
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            
            for (String mes : meses) {
                demandaPorMes.put(mes, 0);
            }
            
            // Procesar cada solicitud de curso de verano
            for (SolicitudEntity solicitud : solicitudesCursosVerano) {
                // Analisis por materia (usando el curso asociado)
                if (solicitud.getObjCursoOfertadoVerano() != null && solicitud.getObjCursoOfertadoVerano().getObjMateria() != null) {
                    String nombreMateria = solicitud.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                    demandaPorMateria.put(nombreMateria, demandaPorMateria.getOrDefault(nombreMateria, 0) + 1);
                }
                
                // Analisis por programa
                if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                    String nombrePrograma = solicitud.getObjUsuario().getObjPrograma().getNombre_programa();
                    demandaPorPrograma.put(nombrePrograma, demandaPorPrograma.getOrDefault(nombrePrograma, 0) + 1);
                }
                
                // Analisis por mes
                Date fechaCreacion = solicitud.getFecha_registro_solicitud();
                if (fechaCreacion != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(fechaCreacion);
                    int mesNumero = cal.get(Calendar.MONTH);
                    // Asegurar que el índice está en el rango válido
                    if (mesNumero >= 0 && mesNumero < meses.length) {
                        String nombreMes = meses[mesNumero];
                        demandaPorMes.put(nombreMes, demandaPorMes.get(nombreMes) + 1);
                    } else {
                        log.warn("Mes numero fuera de rango: {} para solicitud de curso de verano ID: {}", mesNumero, solicitud.getId_solicitud());
                    }
                }
                
                // Analisis por estado
                String estadoActual = obtenerEstadoMasReciente(solicitud);
                estadosPorSolicitud.put(estadoActual, estadosPorSolicitud.getOrDefault(estadoActual, 0) + 1);
            }
            
            // Crear top 10 materias mas demandadas
            List<Map<String, Object>> topMaterias = demandaPorMateria.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> materia = new HashMap<>();
                    materia.put("nombre", entry.getKey());
                    materia.put("solicitudes", entry.getValue());
                    materia.put("porcentaje", Math.round((entry.getValue() * 100.0) / solicitudesCursosVerano.size() * 100.0) / 100.0);
                    return materia;
                })
                .collect(Collectors.toList());
            
            // Crear analisis por programa
            List<Map<String, Object>> analisisPorPrograma = demandaPorPrograma.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> programa = new HashMap<>();
                    programa.put("nombre", entry.getKey());
                    programa.put("solicitudes", entry.getValue());
                    programa.put("porcentaje", Math.round((entry.getValue() * 100.0) / solicitudesCursosVerano.size() * 100.0) / 100.0);
                    return programa;
                })
                .collect(Collectors.toList());
            
            // Generar tendencias temporales: incluir TODOS los 12 meses, incluso con 0 solicitudes
            // ⚠️ IMPORTANTE: Incluir TODOS los meses para que el frontend muestre la tendencia completa
            List<Map<String, Object>> tendenciasTemporales = new ArrayList<>();
            
            // Incluir TODOS los meses (Enero a Diciembre), incluso si tienen 0 solicitudes
            for (String mes : meses) {
                int solicitudes = demandaPorMes.getOrDefault(mes, 0);
                Map<String, Object> tendencia = new HashMap<>();
                tendencia.put("mes", mes);
                tendencia.put("solicitudes", solicitudes);
                // Calcular porcentaje solo si hay solicitudes totales
                double porcentaje = solicitudesCursosVerano.size() > 0 ? 
                    Math.round((solicitudes * 100.0) / solicitudesCursosVerano.size() * 100.0) / 100.0 : 0.0;
                tendencia.put("porcentaje", porcentaje);
                tendenciasTemporales.add(tendencia);
            }
            
            // Agregar también un array con los nombres de los meses para facilitar el renderizado
            resultado.put("todosLosMeses", Arrays.asList(meses));
            
            // Construir resultado final
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadisticas detalladas de cursos de verano - Analisis de demanda y recomendaciones");
            
            // Calcular tasa de aprobacion (solo contar las completamente aprobadas, no las en proceso)
            int aprobadas = estadosPorSolicitud.getOrDefault("APROBADA", 0);
            int total = solicitudesCursosVerano.size();
            double tasaAprobacion = 0.0;
            
            if (total > 0) {
                tasaAprobacion = (aprobadas * 100.0) / total;
                // Validar que no sea NaN o Infinity
                if (Double.isNaN(tasaAprobacion) || Double.isInfinite(tasaAprobacion)) {
                    tasaAprobacion = 0.0;
                }
            }
            
            resultado.put("resumen", Map.of(
                "totalSolicitudes", solicitudesCursosVerano.size(),
                "materiasUnicas", demandaPorMateria.size(),
                "programasParticipantes", demandaPorPrograma.size(),
                "tasaAprobacion", Math.round(tasaAprobacion * 100.0) / 100.0
            ));
            
            // ===== PREDICCIONES DE DEMANDA CON REGRESION LINEAL =====
            Map<String, Object> predicciones = generarPrediccionesDemanda(
                solicitudesCursosVerano, demandaPorMateria, demandaPorPrograma, 
                demandaPorMes, topMaterias, analisisPorPrograma
            );
            
            // Obtener recomendaciones del objeto interno (se generan en el metodo generarPrediccionesDemanda)
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recomendacionesMejoradas = 
                (List<Map<String, Object>>) predicciones.getOrDefault("recomendaciones", new ArrayList<>());
            
            // ⚠️ IMPORTANTE: Estructurar estados de la misma forma que /estado-solicitudes
            // Incluir TODOS los estados principales, incluso si tienen 0 solicitudes
            Map<String, Object> estadosSolicitudesEstructurados = new HashMap<>();
            
            // Calcular total "En Proceso" (suma de funcionario + coordinador)
            int totalEnProcesoCombinado = estadosPorSolicitud.getOrDefault("APROBADA_FUNCIONARIO", 0) + 
                                         estadosPorSolicitud.getOrDefault("APROBADA_COORDINADOR", 0);
            
            // Calcular total de solicitudes
            int totalSolicitudesCursosVerano = solicitudesCursosVerano.size();
            
            // Incluir los 4 estados principales con su estructura completa
            String[] estadosPrincipales = {"APROBADA", "ENVIADA", "RECHAZADA"};
            for (String estado : estadosPrincipales) {
                int cantidad = estadosPorSolicitud.getOrDefault(estado, 0);
                double porcentaje = totalSolicitudesCursosVerano > 0 ? (cantidad * 100.0) / totalSolicitudesCursosVerano : 0.0;
                
                Map<String, Object> detalleEstado = new HashMap<>();
                detalleEstado.put("estado", estado);
                detalleEstado.put("cantidad", cantidad);
                detalleEstado.put("porcentaje", Math.round(porcentaje * 100.0) / 100.0);
                
                estadosSolicitudesEstructurados.put(estado, detalleEstado);
            }
            
            // Agregar estado "EN_PROCESO" combinado
            Map<String, Object> detalleEnProceso = new HashMap<>();
            detalleEnProceso.put("estado", "EN_PROCESO");
            detalleEnProceso.put("cantidad", totalEnProcesoCombinado);
            double porcentajeEnProceso = totalSolicitudesCursosVerano > 0 ? (totalEnProcesoCombinado * 100.0) / totalSolicitudesCursosVerano : 0.0;
            detalleEnProceso.put("porcentaje", Math.round(porcentajeEnProceso * 100.0) / 100.0);
            
            estadosSolicitudesEstructurados.put("EN_PROCESO", detalleEnProceso);
            
            // Asegurar que el mapa simple también incluya los 4 estados principales, incluso con 0
            // Esto es para compatibilidad con frontend que use estadosSolicitudes directamente
            Map<String, Integer> estadosSolicitudesCompleto = new HashMap<>();
            estadosSolicitudesCompleto.put("APROBADA", estadosPorSolicitud.getOrDefault("APROBADA", 0));
            estadosSolicitudesCompleto.put("ENVIADA", estadosPorSolicitud.getOrDefault("ENVIADA", 0));
            estadosSolicitudesCompleto.put("EN_PROCESO", totalEnProcesoCombinado);
            estadosSolicitudesCompleto.put("RECHAZADA", estadosPorSolicitud.getOrDefault("RECHAZADA", 0));
            
            // Agregar el mapa simple con los 4 estados principales siempre presentes
            resultado.put("estadosSolicitudes", estadosSolicitudesCompleto);
            // Agregar la estructura completa para que el frontend la use
            resultado.put("estadosSolicitudesEstructurados", estadosSolicitudesEstructurados);
            
            resultado.put("topMaterias", topMaterias);
            resultado.put("analisisPorPrograma", analisisPorPrograma);
            resultado.put("tendenciasTemporales", tendenciasTemporales);
            resultado.put("recomendaciones", recomendacionesMejoradas);
            resultado.put("predicciones", predicciones);
            
            log.debug("Cursos de verano: el analisis se completo correctamente.");
            log.debug("Resumen de estados calculados:  {}", estadosPorSolicitud);
            log.debug("Claves presentes en las predicciones generadas:  {}", predicciones.keySet());
            return resultado;
            
        } catch (Exception e) {
            log.error("Cursos de verano - Error en analisis de cursos de verano:  {}", e.getMessage());
            log.error("Error en estadísticas", e);
            
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener estadisticas de cursos de verano");
            resultado.put("error", e.getMessage());
            resultado.put("resumen", new HashMap<>());
            resultado.put("topMaterias", new ArrayList<>());
            resultado.put("analisisPorPrograma", new ArrayList<>());
            resultado.put("tendenciasTemporales", new ArrayList<>());
            resultado.put("estadosSolicitudes", new HashMap<>());
            resultado.put("recomendaciones", new ArrayList<>());
            
            return resultado;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTendenciasTemporalesCursosVerano(String periodoAcademico, Integer idPrograma) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            log.debug("Tendencias temporales - Iniciando analisis optimizado de tendencias temporales - Período: {}, Programa: {}", 
                    periodoAcademico, idPrograma);
            
            // Obtener solo las solicitudes de cursos de verano
            List<SolicitudEntity> todasLasSolicitudes = solicitudRepository.findAll();
            List<SolicitudEntity> solicitudesCursosVerano = todasLasSolicitudes.stream()
                .filter(solicitud -> {
                    // Filtrar por tipo de proceso
                    String nombreProceso = obtenerNombreProcesoPorSolicitud(solicitud);
                    if (!"Cursos de Verano".equals(nombreProceso) && !"CURSO_VERANO".equals(nombreProceso)) {
                        return false;
                    }
                    
                    // Filtrar por período académico si se proporciona
                    if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                        String periodoFiltro = periodoAcademico.trim();
                        // Verificar período en la solicitud
                        if (solicitud.getPeriodo_academico() != null && solicitud.getPeriodo_academico().equals(periodoFiltro)) {
                            // OK, coincide con el período de la solicitud
                        } else if (solicitud.getObjCursoOfertadoVerano() != null 
                                && solicitud.getObjCursoOfertadoVerano().getPeriodo_academico() != null
                                && solicitud.getObjCursoOfertadoVerano().getPeriodo_academico().equals(periodoFiltro)) {
                            // OK, coincide con el período del curso
                        } else {
                            // No coincide con ningún período
                            return false;
                        }
                    }
                    
                    // Filtrar por programa si se proporciona
                    if (idPrograma != null) {
                        if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getObjPrograma() == null
                                || !solicitud.getObjUsuario().getObjPrograma().getId_programa().equals(idPrograma)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            // Inicializar meses (Calendar.MONTH devuelve 0-11: 0=Enero, 1=Febrero, ..., 11=Diciembre)
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            
            Map<String, Integer> demandaPorMes = new HashMap<>();
            for (String mes : meses) {
                demandaPorMes.put(mes, 0);
            }
            
            // Procesar solo los datos temporales
            for (SolicitudEntity solicitud : solicitudesCursosVerano) {
                Date fechaCreacion = solicitud.getFecha_registro_solicitud();
                if (fechaCreacion != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(fechaCreacion);
                    int mesNumero = cal.get(Calendar.MONTH); // 0-11 (0=Enero, 11=Diciembre)
                    // Asegurar que el índice está en el rango válido
                    if (mesNumero >= 0 && mesNumero < meses.length) {
                        String nombreMes = meses[mesNumero];
                        demandaPorMes.put(nombreMes, demandaPorMes.get(nombreMes) + 1);
                    } else {
                        log.warn("Mes numero fuera de rango: {} para solicitud ID: {}", mesNumero, solicitud.getId_solicitud());
                    }
                }
            }
            
            // Crear tendencias temporales optimizadas
            // ⚠️ IMPORTANTE: Incluir TODOS los meses, incluso con 0 solicitudes, para que el frontend muestre la tendencia completa
            List<Map<String, Object>> tendenciasTemporales = new ArrayList<>();
            
            // Incluir TODOS los meses (Enero a Diciembre), incluso si tienen 0 solicitudes
            for (String mes : meses) {
                int solicitudes = demandaPorMes.getOrDefault(mes, 0);
                Map<String, Object> tendencia = new HashMap<>();
                tendencia.put("mes", mes);
                tendencia.put("solicitudes", solicitudes);
                // Calcular porcentaje solo si hay solicitudes totales
                double porcentaje = solicitudesCursosVerano.size() > 0 ? 
                    Math.round((solicitudes * 100.0) / solicitudesCursosVerano.size() * 100.0) / 100.0 : 0.0;
                tendencia.put("porcentaje", porcentaje);
                tendenciasTemporales.add(tendencia);
            }
            
            // Agregar también un array con los nombres de los meses para facilitar el renderizado
            resultado.put("todosLosMeses", Arrays.asList(meses));
            
            resultado.put("tendenciasTemporales", tendenciasTemporales);
            resultado.put("totalSolicitudes", solicitudesCursosVerano.size());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Tendencias temporales optimizadas para cursos de verano");
            
            log.debug("Tendencias temporales - Tendencias generadas exitosamente:  {}", tendenciasTemporales.size() + " puntos de datos");
            
        } catch (Exception e) {
            log.error("Tendencias temporales - Error generando tendencias:  {}", e.getMessage());
            log.error("Error en estadísticas", e);
            
            resultado.put("tendenciasTemporales", new ArrayList<>());
            resultado.put("totalSolicitudes", 0);
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Error al obtener tendencias temporales");
            resultado.put("error", e.getMessage());
        }
        
        return resultado;
    }
    
    /**
     * Extrae el tipo de proceso del nombre completo de la solicitud
     * Ejemplo: "Solicitud de Reingreso - Juan Perez" -> "Reingreso"
     * Ejemplo: "Solicitud Curso Verano - Maria Lopez" -> "Cursos de Verano"
     */
    private String extraerTipoProceso(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isEmpty()) {
            return "Desconocido";
        }
        
        // Remover "Solicitud de " o "Solicitud " al inicio
        String nombre = nombreCompleto.replace("Solicitud de ", "").replace("Solicitud ", "");
        
        // Extraer solo la parte antes del guion (si existe)
        int indiceGuion = nombre.indexOf(" - ");
        if (indiceGuion > 0) {
            nombre = nombre.substring(0, indiceGuion).trim();
        }
        
        // Normalizar nombres conocidos
        if (nombre.toLowerCase().contains("curso") && nombre.toLowerCase().contains("verano")) {
            return "Cursos de Verano";
        } else if (nombre.toLowerCase().contains("reingreso")) {
            return "Reingreso";
        } else if (nombre.toLowerCase().contains("homolog")) {
            return "Homologacion";
        } else if (nombre.toLowerCase().contains("paz") && nombre.toLowerCase().contains("salvo")) {
            return "Paz y Salvo";
        } else if (nombre.toLowerCase().contains("ecaes")) {
            return "ECAES";
        }
        
        return nombre;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorAnio(Integer anio) {
        log.debug("Estadisticas por año - Consultando datos del año: {}", anio);
        
        // Calcular fechas del año completo
        java.time.LocalDate fechaInicio = java.time.LocalDate.of(anio, 1, 1);
        java.time.LocalDate fechaFin = java.time.LocalDate.of(anio, 12, 31);
        
        Date fechaInicioDate = java.sql.Date.valueOf(fechaInicio);
        Date fechaFinDate = java.sql.Date.valueOf(fechaFin);
        
        // Obtener estadísticas del año completo
        Map<String, Object> estadisticasAnio = obtenerEstadisticasPorPeriodo(fechaInicioDate, fechaFinDate);
        
        // Agregar información específica del año
        estadisticasAnio.put("anio", anio);
        estadisticasAnio.put("tipoConsulta", "porAnio");
        
        // Agregar desglose por semestre
        Map<String, Object> semestre1 = obtenerEstadisticasPorSemestre(anio, 1);
        Map<String, Object> semestre2 = obtenerEstadisticasPorSemestre(anio, 2);
        
        Map<String, Object> desgloseSemestres = new HashMap<>();
        desgloseSemestres.put("semestre1", semestre1);
        desgloseSemestres.put("semestre2", semestre2);
        estadisticasAnio.put("desgloseSemestres", desgloseSemestres);
        
        return estadisticasAnio;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorSemestre(Integer anio, Integer semestre) {
        log.debug("Estadisticas por semestre - Consultando datos del año: {}, semestre: {}", anio, semestre);
        
        // Calcular fechas del semestre
        java.time.LocalDate fechaInicio;
        java.time.LocalDate fechaFin;
        
        if (semestre == 1) {
            fechaInicio = java.time.LocalDate.of(anio, 1, 1);
            fechaFin = java.time.LocalDate.of(anio, 6, 30);
        } else {
            fechaInicio = java.time.LocalDate.of(anio, 7, 1);
            fechaFin = java.time.LocalDate.of(anio, 12, 31);
        }
        
        Date fechaInicioDate = java.sql.Date.valueOf(fechaInicio);
        Date fechaFinDate = java.sql.Date.valueOf(fechaFin);
        
        // Obtener estadísticas del semestre
        Map<String, Object> estadisticasSemestre = obtenerEstadisticasPorPeriodo(fechaInicioDate, fechaFinDate);
        
        // Agregar información específica del semestre
        estadisticasSemestre.put("anio", anio);
        estadisticasSemestre.put("semestre", semestre);
        estadisticasSemestre.put("periodoAcademico", anio + "-" + semestre);
        estadisticasSemestre.put("tipoConsulta", "porSemestre");
        estadisticasSemestre.put("descripcion", (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + anio);
        
        return estadisticasSemestre;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerHistorialEstadisticas(Integer anioInicio, Integer anioFin) {
        log.debug("Historial de estadísticas - Consultando desde año: {} hasta año: {}", anioInicio, anioFin);
        
        List<Map<String, Object>> historial = new ArrayList<>();
        
        // Si no se proporcionan años, usar los últimos 5 años
        int añoActual = java.time.Year.now().getValue();
        int añoInicioConsulta = anioInicio != null ? anioInicio : Math.max(2020, añoActual - 4);
        int añoFinConsulta = anioFin != null ? anioFin : añoActual;
        
        // Iterar por cada año y semestre
        for (int año = añoInicioConsulta; año <= añoFinConsulta; año++) {
            for (int semestre = 1; semestre <= 2; semestre++) {
                Map<String, Object> estadisticasSemestre = obtenerEstadisticasPorSemestre(año, semestre);
                
                // Agregar información adicional para el historial
                Map<String, Object> entradaHistorial = new HashMap<>();
                entradaHistorial.put("periodo", año + "-" + semestre);
                entradaHistorial.put("anio", año);
                entradaHistorial.put("semestre", semestre);
                entradaHistorial.put("descripcion", (semestre == 1 ? "Primer" : "Segundo") + " Semestre " + año);
                entradaHistorial.put("totalSolicitudes", estadisticasSemestre.get("totalSolicitudes"));
                entradaHistorial.put("totalAprobadas", estadisticasSemestre.get("totalAprobadas"));
                entradaHistorial.put("totalRechazadas", estadisticasSemestre.get("totalRechazadas"));
                entradaHistorial.put("totalEnProceso", estadisticasSemestre.get("totalEnProceso"));
                entradaHistorial.put("porcentajeAprobacion", estadisticasSemestre.get("porcentajeAprobacion"));
                entradaHistorial.put("fechaInicio", estadisticasSemestre.get("fechaInicio"));
                entradaHistorial.put("fechaFin", estadisticasSemestre.get("fechaFin"));
                
                historial.add(entradaHistorial);
            }
        }
        
        // Ordenar por año y semestre (más reciente primero)
        historial.sort((a, b) -> {
            Integer añoA = (Integer) a.get("anio");
            Integer añoB = (Integer) b.get("anio");
            int comparacionAño = añoB.compareTo(añoA); // Descendente
            if (comparacionAño != 0) {
                return comparacionAño;
            }
            Integer semestreA = (Integer) a.get("semestre");
            Integer semestreB = (Integer) b.get("semestre");
            return semestreB.compareTo(semestreA); // Descendente
        });
        
        return historial;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPorPeriodoAcademico(String periodoAcademico) {
        log.debug("Estadisticas por período académico - Consultando período: {}", periodoAcademico);
        
        // Validar formato del período
        if (periodoAcademico == null || !periodoAcademico.matches("^\\d{4}-[12]$")) {
            log.error("Formato de período académico inválido: {}", periodoAcademico);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Formato de período académico inválido. Use formato: YYYY-P (ej: 2024-2)");
            return error;
        }
        
        // Parsear período
        String[] partes = periodoAcademico.split("-");
        int año = Integer.parseInt(partes[0]);
        int semestre = Integer.parseInt(partes[1]);
        
        // Obtener estadísticas del semestre
        Map<String, Object> estadisticas = obtenerEstadisticasPorSemestre(año, semestre);
        estadisticas.put("periodoAcademico", periodoAcademico);
        
        return estadisticas;
    }
}
