package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatoDemandaCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DemandaCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.PrediccionesCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización de {@link PrediccionDemandaCursosVerano#generar} antes de dividirla (OPC-3): sin datos (el mes
 * pico no existe y responde el respaldo del {@code catch}), con un solo mes (estimaciones conservadoras y textos en
 * singular) y con tres meses (materias creciente, decreciente y estable, recomendaciones por materia, programa y
 * mes, alerta crítica por materia y alerta por programa).
 */
@DisplayName("PrediccionDemandaCursosVerano: predicciones, recomendaciones y alertas")
class PrediccionDemandaCaracterizacionTest {

    private static final String[] MESES = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
            "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final long MARGEN_AHORA_MS = 120_000L;

    private final PrediccionDemandaCursosVerano prediccion = new PrediccionDemandaCursosVerano();

    @Test
    void prediccionesRecomendacionesYAlertas() throws Exception {
        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("vacio", arbol(prediccion.generar(List.of(), new HashMap<>(), List.of(), List.of())));
        casos.set("un mes", arbol(generar(unMes())));
        casos.set("tres meses", arbol(generar(tresMeses())));
        GrafoObjetos.compararConReferencia(casos, "estadisticas/prediccion-demanda.json");
    }

    /**
     * Árbol JSON de la predicción, como lo dejaba GrafoObjetos sobre mapas: los contadores de prioridad (long) se
     * releen como int y la fecha de la predicción, si es de ahora, se escribe "&lt;ahora&gt;". Así la comparación no
     * depende de si la predicción es un mapa o un record.
     */
    private static JsonNode arbol(Object resultado) throws Exception {
        ObjectNode arbol = (ObjectNode) JSON.readTree(JSON.writeValueAsString(resultado));
        JsonNode fecha = arbol.get("fechaPrediccion");
        if (fecha != null && fecha.isNumber() && Math.abs(fecha.asLong() - System.currentTimeMillis()) < MARGEN_AHORA_MS) {
            arbol.put("fechaPrediccion", "<ahora>");
        }
        return arbol;
    }

    /** Una materia con una solicitud y un programa con doce, todas en julio. */
    private static List<DatoDemandaCursoVerano> unMes() {
        List<DatoDemandaCursoVerano> datos = new ArrayList<>();
        agregar(datos, "Algebra", "Ingenieria Civil", Calendar.JULY, 1);
        agregar(datos, "Dibujo", "Ingenieria Electronica", Calendar.JULY, 11);
        return datos;
    }

    /** Junio, julio y agosto: una materia que crece, una que baja, una estable y una con una sola solicitud. */
    private static List<DatoDemandaCursoVerano> tresMeses() {
        List<DatoDemandaCursoVerano> datos = new ArrayList<>();
        agregar(datos, "Programacion I", "Ingenieria de Sistemas", Calendar.JUNE, 2);
        agregar(datos, "Programacion I", "Ingenieria de Sistemas", Calendar.JULY, 4);
        agregar(datos, "Programacion I", "Ingenieria de Sistemas", Calendar.AUGUST, 9);
        agregar(datos, "Base de Datos", "Ingenieria Electronica", Calendar.JUNE, 6);
        agregar(datos, "Base de Datos", "Ingenieria Electronica", Calendar.JULY, 3);
        agregar(datos, "Base de Datos", "Ingenieria Electronica", Calendar.AUGUST, 1);
        agregar(datos, "Calculo", "Ingenieria Electronica", Calendar.JUNE, 3);
        agregar(datos, "Calculo", "Ingenieria Electronica", Calendar.JULY, 3);
        agregar(datos, "Calculo", "Ingenieria Electronica", Calendar.AUGUST, 3);
        agregar(datos, "Fisica", "Ingenieria Civil", Calendar.JULY, 1);
        datos.add(new DatoDemandaCursoVerano(null, null, null));
        return datos;
    }

    private static void agregar(List<DatoDemandaCursoVerano> datos, String materia, String programa, int mes,
            int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            datos.add(new DatoDemandaCursoVerano(materia, programa,
                    new GregorianCalendar(2025, mes, 15, 12, 0).getTime()));
        }
    }

    /** Como lo arma EstadisticasCursosVerano: demanda de los doce meses y conteos por materia y por programa. */
    private PrediccionesCursosVerano generar(List<DatoDemandaCursoVerano> datos) {
        Map<String, Integer> demandaPorMes = new LinkedHashMap<>();
        for (String mes : MESES) {
            demandaPorMes.put(mes, 0);
        }
        Map<String, Integer> porMateria = new LinkedHashMap<>();
        Map<String, Integer> porPrograma = new LinkedHashMap<>();
        for (DatoDemandaCursoVerano dato : datos) {
            if (dato.fechaRegistro() != null) {
                Calendar calendario = Calendar.getInstance();
                calendario.setTime(dato.fechaRegistro());
                demandaPorMes.merge(MESES[calendario.get(Calendar.MONTH)], 1, Integer::sum);
            }
            if (dato.materia() != null) {
                porMateria.merge(dato.materia(), 1, Integer::sum);
            }
            if (dato.programa() != null) {
                porPrograma.merge(dato.programa(), 1, Integer::sum);
            }
        }
        return prediccion.generar(datos, demandaPorMes, conteos(porMateria), conteos(porPrograma));
    }

    /** La predicción no usa el porcentaje: va en 0. */
    private static List<DemandaCursoVerano> conteos(Map<String, Integer> porNombre) {
        List<DemandaCursoVerano> lista = new ArrayList<>();
        porNombre.forEach((nombre, solicitudes) -> lista.add(new DemandaCursoVerano(nombre, solicitudes, 0.0)));
        return lista;
    }
}
