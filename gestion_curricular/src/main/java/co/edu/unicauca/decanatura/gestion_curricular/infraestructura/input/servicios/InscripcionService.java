package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.servicios;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InscripcionService {
    
    // Usar ConcurrentHashMap para simular persistencia en memoria
    private final Map<Integer, Map<String, Object>> inscripciones = new ConcurrentHashMap<>();
    
    public InscripcionService() {
        // Inicializar con datos de prueba
        inicializarDatosPrueba();
    }
    
    private void inicializarDatosPrueba() {
        // Inscripción 1
        Map<String, Object> inscripcion1 = new HashMap<>();
        inscripcion1.put("id", 1);
        inscripcion1.put("cursoId", 1);
        inscripcion1.put("estudianteId", 1);
        inscripcion1.put("fecha", "2024-01-15T10:30:00");
        inscripcion1.put("estado", "inscrito");
        inscripciones.put(1, inscripcion1);
        
        // Inscripción 2
        Map<String, Object> inscripcion2 = new HashMap<>();
        inscripcion2.put("id", 2);
        inscripcion2.put("cursoId", 2);
        inscripcion2.put("estudianteId", 1);
        inscripcion2.put("fecha", "2024-01-16T14:20:00");
        inscripcion2.put("estado", "inscrito");
        inscripciones.put(2, inscripcion2);
        
        // Inscripción 3
        Map<String, Object> inscripcion3 = new HashMap<>();
        inscripcion3.put("id", 3);
        inscripcion3.put("cursoId", 3);
        inscripcion3.put("estudianteId", 2);
        inscripcion3.put("fecha", "2024-01-17T09:15:00");
        inscripcion3.put("estado", "pendiente");
        inscripciones.put(3, inscripcion3);
        
        // Inscripción 4
        Map<String, Object> inscripcion4 = new HashMap<>();
        inscripcion4.put("id", 4);
        inscripcion4.put("cursoId", 1);
        inscripcion4.put("estudianteId", 3);
        inscripcion4.put("fecha", "2024-01-18T11:45:00");
        inscripcion4.put("estado", "inscrito");
        inscripciones.put(4, inscripcion4);
        
        // Inscripción 5 (ya cancelada para pruebas)
        Map<String, Object> inscripcion5 = new HashMap<>();
        inscripcion5.put("id", 5);
        inscripcion5.put("cursoId", 2);
        inscripcion5.put("estudianteId", 3);
        inscripcion5.put("fecha", "2024-01-19T08:30:00");
        inscripcion5.put("estado", "cancelada");
        inscripciones.put(5, inscripcion5);
    }
    
    public List<Map<String, Object>> findAll() {
        return new ArrayList<>(inscripciones.values());
    }
    
    public Map<String, Object> findById(Integer id) {
        return inscripciones.get(id);
    }
    
    public boolean existsById(Integer id) {
        return inscripciones.containsKey(id);
    }
    
    public void save(Map<String, Object> inscripcion) {
        Integer id = (Integer) inscripcion.get("id");
        inscripciones.put(id, inscripcion);
    }
    
    public void updateEstado(Integer id, String nuevoEstado) {
        Map<String, Object> inscripcion = inscripciones.get(id);
        if (inscripcion != null) {
            inscripcion.put("estado", nuevoEstado);
            inscripciones.put(id, inscripcion);
        }
    }
    
    public String getEstado(Integer id) {
        Map<String, Object> inscripcion = inscripciones.get(id);
        return inscripcion != null ? (String) inscripcion.get("estado") : null;
    }
    
    public void delete(Integer id) {
        inscripciones.remove(id);
    }
    
    public int count() {
        return inscripciones.size();
    }
}
