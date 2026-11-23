package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CatalogosVeranoDTORespuesta.DocenteCatalogo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CatalogosVeranoDTORespuesta.MateriaCatalogo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CatalogosVeranoDTORespuesta.MateriaDisponible;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CatalogosVeranoDTORespuesta.MateriaFiltro;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosVeranoDTORespuesta.SalonInfo;

/**
 * Catálogos para los formularios de cursos de verano: materias, docentes, salones y condiciones.
 * Rutas /api/cursos-verano/catalogos/… (EST-7, paso 8c).
 */
@RestController
@RequestMapping("/api/cursos-verano/catalogos")
@RequiredArgsConstructor
@Slf4j
public class CatalogosCursosVeranoRestController {

    private final GestionarMateriasCUIntPort materiaCU;
    private final GestionarDocentesCUIntPort docenteCU;
    private final GestionarSalonesCUIntPort salonCU;

    /**
     * Obtener todas las materias disponibles para solicitud de cursos intersemestrales
     * GET /api/cursos-verano/catalogos/materias-disponibles
     */
    @GetMapping("/materias-disponibles")
    public ResponseEntity<List<MateriaDisponible>> obtenerMateriasDisponibles() {
        // Obtener todas las materias de la base de datos
        List<Materia> materias =
            materiaCU.listarMaterias();

        List<MateriaDisponible> materiasDisponibles = new ArrayList<>();

        for (Materia materia : materias) {
            materiasDisponibles.add(new MateriaDisponible(materia.getIdMateria(), materia.getCodigo(),
                materia.getNombre(), materia.getCreditos(),
                materia.getNombre() + " (" + materia.getCodigo() + ") - " + materia.getCreditos() + " creditos"));
        }
        
        return ResponseEntity.ok(materiasDisponibles);
    }

    /**
     * Obtener condiciones de solicitud disponibles
     * GET /api/cursos-verano/catalogos/condiciones (antes /condiciones-solicitud)
     */
    @GetMapping("/condiciones")
    public ResponseEntity<String[]> obtenerCondicionesSolicitud() {
        CondicionSolicitudVerano[] condiciones = CondicionSolicitudVerano.values();
        String[] condicionesString = new String[condiciones.length];
        for (int i = 0; i < condiciones.length; i++) {
            condicionesString[i] = condiciones[i].name();
        }
        return ResponseEntity.ok(condicionesString);
    }

    // ==================== ENDPOINTS PARA MATERIAS Y DOCENTES ====================

    /**
     * Obtener todas las materias (endpoint actualizado para usar datos reales)
     * GET /api/cursos-verano/catalogos/materias
     */
    @GetMapping("/materias")
    public ResponseEntity<List<MateriaCatalogo>> getTodasLasMaterias() {
        // Obtener todas las materias de la base de datos
        List<Materia> materiasReales =
            materiaCU.listarMaterias();

        List<MateriaCatalogo> materias = new ArrayList<>();

        for (Materia materia : materiasReales) {
            materias.add(new MateriaCatalogo(materia.getIdMateria(), materia.getNombre(), materia.getCodigo(),
                materia.getCreditos()));
        }
        
        return ResponseEntity.ok(materias);
    }

    /**
     * Obtener materias para el filtro de solicitudes (formato simplificado)
     * GET /api/cursos-verano/catalogos/materias-filtro
     */
    @GetMapping("/materias-filtro")
    public ResponseEntity<List<MateriaFiltro>> obtenerMateriasParaFiltro() {
        try {

            // Obtener todas las materias de la base de datos
            List<Materia> materiasReales =
                materiaCU.listarMaterias();

            List<MateriaFiltro> materiasFiltro = new ArrayList<>();

            // Agregar opcion "Todas las materias"
            materiasFiltro.add(new MateriaFiltro(0, "Todas las materias", "TODAS"));

            // Agregar materias reales
            for (Materia materia : materiasReales) {
                materiasFiltro.add(new MateriaFiltro(materia.getIdMateria(), materia.getNombre(),
                    materia.getCodigo()));
            }


            return ResponseEntity.ok(materiasFiltro);
        } catch (Exception e) {
            log.error("Error obteniendo materias para filtro: {}", e.getMessage(), e);

            // En caso de error, devolver al menos la opcion "Todas las materias"
            List<MateriaFiltro> fallback = new ArrayList<>();
            fallback.add(new MateriaFiltro(0, "Todas las materias", "TODAS"));

            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * Obtener todos los salones disponibles
     * GET /api/cursos-verano/catalogos/salones
     * 
     * @return Lista de salones activos disponibles para asignar a cursos
     */
    @GetMapping("/salones")
    @PreAuthorize("hasAnyRole('Funcionario', 'Coordinador', 'Administrador')")
    public ResponseEntity<List<SalonInfo>> obtenerSalones() {
        List<Salon> salonesReales =
            salonCU.listarSalonesActivos();

        List<SalonInfo> salones = new ArrayList<>();

        for (Salon salon : salonesReales) {
            // Agregar descripción completa para el frontend
            String descripcion = salon.getNumeroSalon();
            if (salon.getEdificio() != null && !salon.getEdificio().trim().isEmpty()) {
                descripcion += " - " + salon.getEdificio();
            }
            salones.add(new SalonInfo(salon.getIdSalon(), salon.getNumeroSalon(),
                salon.getEdificio() != null ? salon.getEdificio() : "Sin especificar",
                salon.getActivo() != null ? salon.getActivo() : true, descripcion));
        }

        // Ordenar por número de salón
        salones.sort((a, b) -> {
            String numA = a.numeroSalon();
            String numB = b.numeroSalon();
            if (numA == null) numA = "";
            if (numB == null) numB = "";
            return numA.compareToIgnoreCase(numB);
        });
        
        return ResponseEntity.ok(salones);
    }

    /**
     * Obtener todos los docentes
     * GET /api/cursos-verano/catalogos/docentes
     */
    @GetMapping("/docentes")
    public ResponseEntity<List<DocenteCatalogo>> getTodosLosDocentes() {
        
        // Obtener docentes reales de la base de datos
        List<Docente> docentesDominio =
            docenteCU.listarDocentes();
        
        if (docentesDominio == null || docentesDominio.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        
        // Mapear docentes de dominio a formato de respuesta para el frontend
        // Campo crítico: id_docente (NO id_usuario); las claves de usuario se mantienen por compatibilidad
        // con el frontend y usan id_docente como id_usuario
        List<DocenteCatalogo> docentes = docentesDominio.stream()
            .map(docente -> new DocenteCatalogo(docente.getIdDocente(), docente.getCodigoDocente(),
                docente.getNombreDocente(), docente.getIdDocente(), docente.getCodigoDocente(),
                docente.getNombreDocente()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(docentes);
    }
}
