package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PermisosCursoVerano;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Catálogos para los formularios de cursos de verano: materias, docentes, salones, condiciones y permisos por estado.
 * Ruta base compartida con los demás controladores de cursos de verano.
 */
@RestController
@RequestMapping("/api/cursos-intersemestrales")
@RequiredArgsConstructor
@Slf4j
public class CatalogosCursosVeranoRestController {

    private final GestionarMateriasCUIntPort materiaCU;
    private final GestionarDocentesCUIntPort docenteCU;
    private final GestionarSalonesCUIntPort salonCU;

    /**
     * Obtener todas las materias disponibles para solicitud de cursos intersemestrales
     * GET /api/cursos-intersemestrales/materias-disponibles
     */
    @GetMapping("/materias-disponibles")
    public ResponseEntity<List<Map<String, Object>>> obtenerMateriasDisponibles() {
        // Obtener todas las materias de la base de datos
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materias = 
            materiaCU.listarMaterias();
        
        List<Map<String, Object>> materiasDisponibles = new ArrayList<>();
        
        for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materias) {
            Map<String, Object> materiaMap = new HashMap<>();
            materiaMap.put("id_materia", materia.getId_materia());
            materiaMap.put("codigo", materia.getCodigo());
            materiaMap.put("nombre", materia.getNombre());
            materiaMap.put("creditos", materia.getCreditos());
            materiaMap.put("descripcion", materia.getNombre() + " (" + materia.getCodigo() + ") - " + materia.getCreditos() + " creditos");
            materiasDisponibles.add(materiaMap);
        }
        
        return ResponseEntity.ok(materiasDisponibles);
    }

    /**
     * Obtener condiciones de solicitud disponibles
     * GET /api/cursos-intersemestrales/condiciones-solicitud
     */
    @GetMapping("/condiciones-solicitud")
    public ResponseEntity<String[]> obtenerCondicionesSolicitud() {
        CondicionSolicitudVerano[] condiciones = CondicionSolicitudVerano.values();
        String[] condicionesString = new String[condiciones.length];
        for (int i = 0; i < condiciones.length; i++) {
            condicionesString[i] = condiciones[i].name();
        }
        return ResponseEntity.ok(condicionesString);
    }

    
    
    /**
     * Endpoint para obtener informacion de permisos por estado
     * GET /api/cursos-intersemestrales/permisos-estado/{estado}/{rol}
     */
    @GetMapping("/permisos-estado/{estado}/{rol}")
    public ResponseEntity<Map<String, Object>> obtenerPermisosPorEstado(
            @PathVariable String estado,
            @PathVariable String rol) {
        Map<String, Object> permisos = PermisosCursoVerano.validar(estado, rol, "consultar_permisos");
        return ResponseEntity.ok(permisos);
    }

    // ==================== ENDPOINTS PARA MATERIAS Y DOCENTES ====================

    /**
     * Obtener todas las materias (endpoint actualizado para usar datos reales)
     * GET /api/cursos-intersemestrales/materias
     */
    @GetMapping("/materias")
    public ResponseEntity<List<Map<String, Object>>> getTodasLasMaterias() {
        // Obtener todas las materias de la base de datos
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materiasReales = 
            materiaCU.listarMaterias();
        
        List<Map<String, Object>> materias = new ArrayList<>();
        
        for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materiasReales) {
            Map<String, Object> materiaMap = new HashMap<>();
            materiaMap.put("id_materia", materia.getId_materia());
            materiaMap.put("nombre_materia", materia.getNombre());
            materiaMap.put("codigo_materia", materia.getCodigo());
            materiaMap.put("creditos", materia.getCreditos());
            materias.add(materiaMap);
        }
        
        return ResponseEntity.ok(materias);
    }

    /**
     * Obtener materias para el filtro de solicitudes (formato simplificado)
     * GET /api/cursos-intersemestrales/materias-filtro
     */
    @GetMapping("/materias-filtro")
    public ResponseEntity<List<Map<String, Object>>> obtenerMateriasParaFiltro() {
        try {
            
            // Obtener todas las materias de la base de datos
            List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia> materiasReales = 
                materiaCU.listarMaterias();
            
            List<Map<String, Object>> materiasFiltro = new ArrayList<>();
            
            // Agregar opcion "Todas las materias"
            Map<String, Object> todasLasMaterias = new HashMap<>();
            todasLasMaterias.put("id", 0);
            todasLasMaterias.put("nombre", "Todas las materias");
            todasLasMaterias.put("codigo", "TODAS");
            materiasFiltro.add(todasLasMaterias);
            
            // Agregar materias reales
            for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia materia : materiasReales) {
                Map<String, Object> materiaMap = new HashMap<>();
                materiaMap.put("id", materia.getId_materia());
                materiaMap.put("nombre", materia.getNombre());
                materiaMap.put("codigo", materia.getCodigo());
                materiasFiltro.add(materiaMap);
            }
            
            
            return ResponseEntity.ok(materiasFiltro);
        } catch (Exception e) {
            log.error("Error obteniendo materias para filtro: {}", e.getMessage(), e);
            
            // En caso de error, devolver al menos la opcion "Todas las materias"
            List<Map<String, Object>> fallback = new ArrayList<>();
            Map<String, Object> todasLasMaterias = new HashMap<>();
            todasLasMaterias.put("id", 0);
            todasLasMaterias.put("nombre", "Todas las materias");
            todasLasMaterias.put("codigo", "TODAS");
            fallback.add(todasLasMaterias);
            
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * Obtener todos los salones disponibles
     * GET /api/cursos-intersemestrales/salones
     * 
     * @return Lista de salones activos disponibles para asignar a cursos
     */
    @GetMapping("/salones")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<List<Map<String, Object>>> obtenerSalones() {
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon> salonesReales = 
            salonCU.listarSalonesActivos();
        
        List<Map<String, Object>> salones = new ArrayList<>();
        
        for (co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon salon : salonesReales) {
            Map<String, Object> salonMap = new HashMap<>();
            salonMap.put("id_salon", salon.getId_salon());
            salonMap.put("numero_salon", salon.getNumero_salon());
            salonMap.put("edificio", salon.getEdificio() != null ? salon.getEdificio() : "Sin especificar");
            salonMap.put("activo", salon.getActivo() != null ? salon.getActivo() : true);
            // Agregar descripción completa para el frontend
            String descripcion = salon.getNumero_salon();
            if (salon.getEdificio() != null && !salon.getEdificio().trim().isEmpty()) {
                descripcion += " - " + salon.getEdificio();
            }
            salonMap.put("descripcion", descripcion);
            salones.add(salonMap);
        }
        
        // Ordenar por número de salón
        salones.sort((a, b) -> {
            String numA = (String) a.get("numero_salon");
            String numB = (String) b.get("numero_salon");
            if (numA == null) numA = "";
            if (numB == null) numB = "";
            return numA.compareToIgnoreCase(numB);
        });
        
        return ResponseEntity.ok(salones);
    }

    /**
     * Obtener todos los docentes
     * GET /api/cursos-intersemestrales/docentes
     */
    @GetMapping("/docentes")
    public ResponseEntity<List<Map<String, Object>>> getTodosLosDocentes() {
        
        // Obtener docentes reales de la base de datos
        List<co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente> docentesDominio = 
            docenteCU.listarDocentes();
        
        if (docentesDominio == null || docentesDominio.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        
        // Mapear docentes de dominio a formato de respuesta para el frontend
        List<Map<String, Object>> docentes = docentesDominio.stream()
            .map(docente -> {
                Map<String, Object> docenteMap = new HashMap<>();
                // Campo crítico: id_docente (NO id_usuario)
                docenteMap.put("id_docente", docente.getId_docente());
                docenteMap.put("codigo_docente", docente.getCodigo_docente());
                docenteMap.put("nombre_docente", docente.getNombre_docente());
                // Mantener compatibilidad con frontend (si lo necesita)
                docenteMap.put("id_usuario", docente.getId_docente()); // Usar id_docente como id_usuario para compatibilidad
                docenteMap.put("codigo_usuario", docente.getCodigo_docente());
                docenteMap.put("nombre_usuario", docente.getNombre_docente());
                
                return docenteMap;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(docentes);
    }
}
