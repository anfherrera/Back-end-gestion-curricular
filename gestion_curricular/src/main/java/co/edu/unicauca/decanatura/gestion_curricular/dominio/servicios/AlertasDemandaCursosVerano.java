package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alertas críticas de la predicción de demanda de cursos de verano: situaciones que requieren atención inmediata.
 */
final class AlertasDemandaCursosVerano {

    private AlertasDemandaCursosVerano() {
    }

    /** Primero las materias con crecimiento muy alto (más del 50%), luego los programas con más de 10 solicitudes. */
    static List<Map<String, Object>> generar(List<Map<String, Object>> materiasConTendenciaCreciente,
            List<Map<String, Object>> programasConTendenciaCreciente) {
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
        return alertasCriticas;
    }
}
