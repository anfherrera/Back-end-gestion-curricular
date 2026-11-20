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

    private final PrediccionDemandaCursosVerano prediccion = new PrediccionDemandaCursosVerano();

    @Test
    void prediccionesRecomendacionesYAlertas() throws Exception {
        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("vacio", GrafoObjetos.resultado(
                () -> prediccion.generar(List.of(), new HashMap<>(), List.of(), List.of())));
        casos.set("un mes", GrafoObjetos.resultado(() -> generar(unMes())));
        casos.set("tres meses", GrafoObjetos.resultado(() -> generar(tresMeses())));
        // Los contadores de prioridad son long: se vuelcan como LongNode y al leer la referencia salen como IntNode
        JsonNode comoTexto = JSON.readTree(JSON.writeValueAsString(casos));
        GrafoObjetos.compararConReferencia(comoTexto, "estadisticas/prediccion-demanda.json");
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
    private Map<String, Object> generar(List<DatoDemandaCursoVerano> datos) {
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

    private static List<Map<String, Object>> conteos(Map<String, Integer> porNombre) {
        List<Map<String, Object>> lista = new ArrayList<>();
        porNombre.forEach((nombre, solicitudes) -> lista.add(Map.of("nombre", nombre, "solicitudes", solicitudes)));
        return lista;
    }
}
