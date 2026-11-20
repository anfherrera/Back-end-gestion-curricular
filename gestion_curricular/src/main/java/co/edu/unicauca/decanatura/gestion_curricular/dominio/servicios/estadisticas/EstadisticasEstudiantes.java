package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/** Número de estudiantes registrados y su distribución por programa académico. */
public class EstadisticasEstudiantes {

    private final GestionarEstadisticasGatewayIntPort datos;

    public EstadisticasEstudiantes(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    public Map<String, Object> numeroTotalEstudiantes() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalEstudiantes", (long) datos.listarEstudiantes().size());
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Total de estudiantes registrados en el sistema");
        return resultado;
    }

    public Map<String, Object> estudiantesPorPrograma(Integer idPrograma) {
        List<Usuario> estudiantes = datos.listarEstudiantes();
        if (idPrograma != null) {
            estudiantes = estudiantes.stream()
                    .filter(e -> e.getObjPrograma() != null && e.getObjPrograma().getId_programa().equals(idPrograma))
                    .collect(Collectors.toList());
        }

        Map<String, Integer> porPrograma = new HashMap<>();
        for (Usuario estudiante : estudiantes) {
            if (estudiante.getObjPrograma() != null) {
                porPrograma.merge(estudiante.getObjPrograma().getNombre_programa(), 1, Integer::sum);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("estudiantesPorPrograma", porPrograma);
        resultado.put("totalEstudiantes", estudiantes.size());
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Distribucion de estudiantes por programa academico");
        return resultado;
    }
}
