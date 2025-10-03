package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.servicios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InscripcionService {
    
    @Autowired
    private SolicitudRepositoryInt solicitudRepository;
    
    public InscripcionService() {
        // Ya no necesitamos inicializar datos de prueba
    }
    
    public List<Map<String, Object>> findAll() {
        // Buscar todas las inscripciones usando el repositorio existente
        List<SolicitudCursoVeranoInscripcionEntity> entities = solicitudRepository.findAll()
                .stream()
                .filter(s -> s instanceof SolicitudCursoVeranoInscripcionEntity)
                .map(s -> (SolicitudCursoVeranoInscripcionEntity) s)
                .collect(Collectors.toList());
        
        return entities.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }
    
    public Map<String, Object> findById(Integer id) {
        Optional<SolicitudCursoVeranoInscripcionEntity> entity = solicitudRepository.findById(id)
                .filter(s -> s instanceof SolicitudCursoVeranoInscripcionEntity)
                .map(s -> (SolicitudCursoVeranoInscripcionEntity) s);
        return entity.map(this::convertToMap).orElse(null);
    }
    
    public boolean existsById(Integer id) {
        return solicitudRepository.existsById(id);
    }
    
    public void save(Map<String, Object> inscripcion) {
        // Este método necesitaría implementación más compleja
        // Por ahora lo dejamos como placeholder
    }
    
    public void updateEstado(Integer id, String nuevoEstado) {
        System.out.println("🔄 Actualizando estado de inscripción ID: " + id + " a: " + nuevoEstado);
        
        Optional<SolicitudCursoVeranoInscripcionEntity> entityOpt = solicitudRepository.findById(id)
                .filter(s -> s instanceof SolicitudCursoVeranoInscripcionEntity)
                .map(s -> (SolicitudCursoVeranoInscripcionEntity) s);
        
        if (entityOpt.isPresent()) {
            SolicitudCursoVeranoInscripcionEntity entity = entityOpt.get();
            
            // Actualizar el estado en la primera entrada de estados
            if (entity.getEstadosSolicitud() != null && !entity.getEstadosSolicitud().isEmpty()) {
                entity.getEstadosSolicitud().get(0).setEstado_actual(nuevoEstado);
                solicitudRepository.save(entity);
                System.out.println("✅ Estado actualizado exitosamente");
            } else {
                System.out.println("⚠️ No se encontraron estados para la inscripción ID: " + id);
            }
        } else {
            System.out.println("❌ No se encontró la inscripción con ID: " + id);
        }
    }
    
    public String getEstado(Integer id) {
        Optional<SolicitudCursoVeranoInscripcionEntity> entity = solicitudRepository.findById(id)
                .filter(s -> s instanceof SolicitudCursoVeranoInscripcionEntity)
                .map(s -> (SolicitudCursoVeranoInscripcionEntity) s);
        return entity.map(e -> {
            if (e.getEstadosSolicitud() != null && !e.getEstadosSolicitud().isEmpty()) {
                return e.getEstadosSolicitud().get(0).getEstado_actual();
            }
            return null;
        }).orElse(null);
    }
    
    public void delete(Integer id) {
        solicitudRepository.deleteById(id);
    }
    
    public int count() {
        return (int) solicitudRepository.findAll()
                .stream()
                .filter(s -> s instanceof SolicitudCursoVeranoInscripcionEntity)
                .count();
    }
    
    private Map<String, Object> convertToMap(SolicitudCursoVeranoInscripcionEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId_solicitud());
        map.put("cursoId", entity.getObjCursoOfertadoVerano() != null ? entity.getObjCursoOfertadoVerano().getId_curso() : null);
        map.put("estudianteId", entity.getObjUsuario() != null ? entity.getObjUsuario().getId_usuario() : null);
        map.put("fecha", entity.getFecha_registro_solicitud());
        map.put("estado", entity.getEstadosSolicitud() != null && !entity.getEstadosSolicitud().isEmpty() 
                ? entity.getEstadosSolicitud().get(0).getEstado_actual() : null);
        map.put("archivoPagoId", null); // No hay archivo de pago en esta entidad
        return map;
    }
}
