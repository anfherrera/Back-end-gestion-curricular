package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.ArrayList;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AlertaCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AlertaPorMateria;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AlertaPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DemandaProyectada;

/**
 * Alertas críticas de la predicción de demanda de cursos de verano: situaciones que requieren atención inmediata.
 */
final class AlertasDemandaCursosVerano {

    private AlertasDemandaCursosVerano() {
    }

    /** Primero las materias con crecimiento muy alto (más del 50%), luego los programas con más de 10 solicitudes. */
    static List<AlertaCursosVerano> generar(List<DemandaProyectada> materiasConTendenciaCreciente,
            List<DemandaProyectada> programasConTendenciaCreciente) {
        List<AlertaCursosVerano> alertasCriticas = new ArrayList<>();

        // Alerta: Materias con crecimiento muy alto (>50%)
        for (DemandaProyectada materia : materiasConTendenciaCreciente) {
            int porcentaje = materia.porcentajeVariacion();
            if (porcentaje > 50) {
                List<String> accionesUrgentes = new ArrayList<>();
                accionesUrgentes.add(" ACCION INMEDIATA REQUERIDA");
                accionesUrgentes.add("Convocar reunion urgente con coordinacion academica");
                accionesUrgentes.add("Evaluar contratar docentes adicionales con caracter urgente");
                accionesUrgentes.add("Verificar disponibilidad de espacios fisicos alternativos");
                accionesUrgentes.add("Considerar modalidad virtual/hibrida si no hay capacidad presencial");

                alertasCriticas.add(new AlertaPorMateria(
                        "ALERTA_CRITICA_" + materia.nombre().replaceAll("\\s+", "_").toUpperCase(),
                        "ALERTA_CAPACIDAD", "URGENTE", "CRITICA",
                        " ALERTA: Crecimiento excepcional en " + materia.nombre(),
                        materia.nombre(), porcentaje + "%",
                        String.format(
                            "ATENCION: La materia %s presenta un crecimiento proyectado del %d%%, " +
                            "significativamente superior al promedio. Se requiere accion inmediata para garantizar cobertura.",
                            materia.nombre(), porcentaje),
                        accionesUrgentes));
            }
        }

        // Alerta: Programas con demanda muy alta
        for (DemandaProyectada programa : programasConTendenciaCreciente) {
            int demanda = programa.demandaEstimada();
            if (demanda > 10) { // Mas de 10 solicitudes para un programa
                List<String> estrategias = new ArrayList<>();
                estrategias.add("Coordinar con director(a) de programa");
                estrategias.add("Identificar materias criticas especificas del programa");
                estrategias.add("Evaluar docentes especializados del programa");
                estrategias.add("Planificar oferta diversificada para atender necesidades del programa");

                alertasCriticas.add(new AlertaPorPrograma(
                        "ALERTA_PROGRAMA_" + programa.nombre().replaceAll("\\s+", "_").toUpperCase(),
                        "ALERTA_PROGRAMA", "IMPORTANTE", "ALTA",
                        " Demanda concentrada en " + programa.nombre(),
                        programa.nombre(), demanda,
                        String.format(
                            "%s concentra una demanda proyectada de %d solicitudes, requiere estrategia especifica.",
                            programa.nombre(), demanda),
                        estrategias));
            }
        }
        return alertasCriticas;
    }
}
