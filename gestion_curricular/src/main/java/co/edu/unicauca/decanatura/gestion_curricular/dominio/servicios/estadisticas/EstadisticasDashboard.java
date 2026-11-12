package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.MESES;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.diasDeProcesamiento;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.estadoActual;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.filtrarPorPeriodoYPrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.mesDeRegistro;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.nombrePrograma;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.redondear2;
import static co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.ReglasEstadisticas.tipoProceso;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/**
 * Vistas del dashboard (filtrables por periodo académico y programa): detalle por proceso, por estado,
 * por mes y por programa, y tendencias con recomendación estratégica.
 */
public class EstadisticasDashboard {

    /** Orden en que el dashboard muestra los procesos. */
    private static final String[] PROCESOS = {
            TipoProcesoSolicitud.HOMOLOGACION, TipoProcesoSolicitud.PAZ_Y_SALVO, TipoProcesoSolicitud.REINGRESO,
            TipoProcesoSolicitud.CURSOS_VERANO, TipoProcesoSolicitud.ECAES };

    private static final ZoneId ZONA_ACADEMICA = ZoneId.of("America/Bogota");

    private final GestionarEstadisticasGatewayIntPort datos;

    public EstadisticasDashboard(GestionarEstadisticasGatewayIntPort datos) {
        this.datos = datos;
    }

    private List<Solicitud> solicitudes(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> todas = datos.listarSolicitudesConEstadosYUsuario();
        return filtrarPorPeriodoYPrograma(todas != null ? todas : new ArrayList<>(), periodoAcademico, idPrograma);
    }

    private static Date corteTendencia(String periodo) {
        LocalDate referencia = LocalDate.now(ZONA_ACADEMICA);
        if (periodo != null && periodo.matches("\\d{4}-[12]")) {
            int anio = Integer.parseInt(periodo.substring(0, 4));
            LocalDate fin = periodo.endsWith("-1") ? LocalDate.of(anio, 6, 30) : LocalDate.of(anio, 12, 31);
            if (fin.isBefore(referencia)) {
                referencia = fin;
            }
        }
        return Date.from(referencia.minusDays(89).atStartOfDay(ZONA_ACADEMICA).toInstant());
    }

    /** Contadores de una agrupación (un proceso, un mes o un programa). */
    private static final class Contador {
        int total;
        int aprobadas;
        int rechazadas;
        int enviadas;
        int enProceso;
        final List<Double> tiempos = new ArrayList<>();
        final List<Date> fechas = new ArrayList<>();
        final Map<String, Integer> procesos = new HashMap<>();

        double eficiencia() {
            return total > 0 ? (aprobadas * 100.0) / total : 0;
        }

        double tiempoPromedio() {
            return tiempos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        }

        String tendenciaReciente(Date corte) {
            if (fechas.size() < 2) {
                return "Estable";
            }
            long recientes = fechas.stream().filter(f -> !f.before(corte)).count();
            long anteriores = fechas.size() - recientes;
            return recientes > anteriores ? "Creciente" : recientes < anteriores ? "Decreciente" : "Estable";
        }
    }

    /**
     * Cuenta la solicitud en su estado. Con {@code separarEnProceso} las aprobadas por funcionario o coordinador
     * van a enProceso; si no, cuentan como enviadas. Las aprobadas y rechazadas suman su tiempo de procesamiento.
     */
    private static void contar(Contador c, Solicitud solicitud, boolean separarEnProceso) {
        c.total++;
        String estado = estadoActual(solicitud);
        switch (estado) {
            case "APROBADA" -> c.aprobadas++;
            case "RECHAZADA" -> c.rechazadas++;
            case "APROBADA_FUNCIONARIO", "APROBADA_COORDINADOR" -> {
                if (separarEnProceso) c.enProceso++; else c.enviadas++;
            }
            default -> c.enviadas++;
        }
        if ("APROBADA".equals(estado) || "RECHAZADA".equals(estado)) {
            Double dias = diasDeProcesamiento(solicitud);
            if (dias != null) {
                c.tiempos.add(dias);
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Por proceso
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> detalladasPorProceso(String periodoAcademico, Integer idPrograma) {
        Date corte = corteTendencia(periodoAcademico);
        Map<String, Contador> porProceso = new HashMap<>();
        for (String proceso : PROCESOS) {
            porProceso.put(proceso, new Contador());
        }
        for (Solicitud solicitud : solicitudes(periodoAcademico, idPrograma)) {
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                Contador c = porProceso.get(proceso);
                if (solicitud.getFecha_registro_solicitud() != null) {
                    c.fechas.add(solicitud.getFecha_registro_solicitud());
                }
                contar(c, solicitud, true);
            }
        }

        Map<String, Object> detalle = new HashMap<>();
        String procesoMasEficiente = null;
        String procesoMasRapido = null;
        String procesoMasDemandado = null;
        double maxEficiencia = 0;
        double minTiempo = Double.MAX_VALUE;
        int maxDemanda = 0;

        for (String proceso : PROCESOS) {
            Contador c = porProceso.get(proceso);
            double eficiencia = c.eficiencia();
            double tiempoPromedio = c.tiempoPromedio();

            Map<String, Object> detalleProceso = new HashMap<>();
            detalleProceso.put("proceso", proceso);
            detalleProceso.put("totalSolicitudes", c.total);
            detalleProceso.put("aprobadas", c.aprobadas);
            detalleProceso.put("rechazadas", c.rechazadas);
            detalleProceso.put("enviadas", c.enviadas);
            detalleProceso.put("enProceso", c.enProceso);
            detalleProceso.put("eficiencia", redondear2(eficiencia));
            detalleProceso.put("tiempoPromedio", redondear2(tiempoPromedio));
            detalleProceso.put("tiempoMinimo", redondear2(c.tiempos.stream().mapToDouble(Double::doubleValue).min().orElse(0)));
            detalleProceso.put("tiempoMaximo", redondear2(c.tiempos.stream().mapToDouble(Double::doubleValue).max().orElse(0)));
            detalleProceso.put("tendencia", c.tendenciaReciente(corte));
            detalleProceso.put("descripcion", PresentacionEstadisticas.descripcionProcesoDetallada(proceso));
            detalleProceso.put("color", PresentacionEstadisticas.colorProceso(proceso));
            detalleProceso.put("icono", PresentacionEstadisticas.SIN_ICONO);
            detalle.put(proceso, detalleProceso);

            if (eficiencia > maxEficiencia) {
                maxEficiencia = eficiencia;
                procesoMasEficiente = proceso;
            }
            if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                minTiempo = tiempoPromedio;
                procesoMasRapido = proceso;
            }
            if (c.total > maxDemanda) {
                maxDemanda = c.total;
                procesoMasDemandado = proceso;
            }
        }

        Map<String, Object> resumenComparativo = new HashMap<>();
        resumenComparativo.put("procesoMasEficiente", procesoMasEficiente);
        resumenComparativo.put("procesoMasRapido", procesoMasRapido);
        resumenComparativo.put("procesoMasDemandado", procesoMasDemandado);
        resumenComparativo.put("eficienciaMaxima", redondear2(maxEficiencia));
        resumenComparativo.put("tiempoMinimo", redondear2(minTiempo));
        resumenComparativo.put("demandaMaxima", maxDemanda);
        resumenComparativo.put("totalProcesos", PROCESOS.length);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("estadisticasPorProceso", detalle);
        resultado.put("resumenComparativo", resumenComparativo);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas detalladas por proceso con analisis de eficiencia y tiempo - DATOS REALES");
        return resultado;
    }

    // ------------------------------------------------------------------------------------------
    // Por estado
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> porEstado(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudes(periodoAcademico, idPrograma);

        Map<String, Integer> totalPorEstado = new HashMap<>();
        Map<String, Map<String, Integer>> procesosPorEstado = new HashMap<>();
        Map<String, Map<String, Integer>> programasPorEstado = new HashMap<>();
        for (String estado : ReglasEstadisticas.ESTADOS) {
            totalPorEstado.put(estado, 0);
            procesosPorEstado.put(estado, new HashMap<>());
            programasPorEstado.put(estado, new HashMap<>());
        }

        for (Solicitud solicitud : solicitudes) {
            String estado = estadoActual(solicitud);
            totalPorEstado.merge(estado, 1, Integer::sum);
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                procesosPorEstado.get(estado).merge(proceso, 1, Integer::sum);
            }
            String programa = nombrePrograma(solicitud);
            if (programa != null) {
                programasPorEstado.get(estado).merge(programa, 1, Integer::sum);
            }
        }

        int totalSolicitudes = solicitudes.size();
        Map<String, Object> resumenPorEstado = new HashMap<>();
        String estadoMasComun = null;
        String estadoMenosComun = null;
        String estadoMasEficiente = null;
        int maxSolicitudes = 0;
        int minSolicitudes = Integer.MAX_VALUE;

        // Siempre se incluyen los estados principales, aunque tengan 0 solicitudes
        for (String estado : new String[] { "APROBADA", "ENVIADA", "RECHAZADA" }) {
            int total = totalPorEstado.get(estado);
            Map<String, Object> detalleEstado = new HashMap<>();
            detalleEstado.put("estado", estado);
            detalleEstado.put("cantidad", total);
            detalleEstado.put("porcentaje", redondear2(porcentaje(total, totalSolicitudes)));
            detalleEstado.put("procesos", procesosPorEstado.get(estado));
            detalleEstado.put("programas", programasPorEstado.get(estado));
            detalleEstado.put("color", PresentacionEstadisticas.colorEstado(estado));
            detalleEstado.put("icono", PresentacionEstadisticas.iconoEstado(estado));
            detalleEstado.put("descripcion", PresentacionEstadisticas.descripcionEstado(estado));
            resumenPorEstado.put(estado, detalleEstado);

            if (total > 0) {
                if (total > maxSolicitudes) {
                    maxSolicitudes = total;
                    estadoMasComun = estado;
                }
                if (total < minSolicitudes) {
                    minSolicitudes = total;
                    estadoMenosComun = estado;
                }
                if ("APROBADA".equals(estado)) {
                    estadoMasEficiente = estado;
                }
            }
        }

        // "EN_PROCESO" junta las aprobadas por funcionario y por coordinador
        int totalEnProceso = totalPorEstado.get("APROBADA_FUNCIONARIO") + totalPorEstado.get("APROBADA_COORDINADOR");
        Map<String, Integer> procesosEnProceso = new HashMap<>(procesosPorEstado.get("APROBADA_FUNCIONARIO"));
        procesosPorEstado.get("APROBADA_COORDINADOR").forEach((k, v) -> procesosEnProceso.merge(k, v, Integer::sum));
        Map<String, Integer> programasEnProceso = new HashMap<>(programasPorEstado.get("APROBADA_FUNCIONARIO"));
        programasPorEstado.get("APROBADA_COORDINADOR").forEach((k, v) -> programasEnProceso.merge(k, v, Integer::sum));

        Map<String, Object> detalleEnProceso = new HashMap<>();
        detalleEnProceso.put("estado", "EN_PROCESO");
        detalleEnProceso.put("cantidad", totalEnProceso);
        detalleEnProceso.put("porcentaje", redondear2(porcentaje(totalEnProceso, totalSolicitudes)));
        detalleEnProceso.put("procesos", procesosEnProceso);
        detalleEnProceso.put("programas", programasEnProceso);
        detalleEnProceso.put("color", PresentacionEstadisticas.colorEstado("EN_PROCESO"));
        detalleEnProceso.put("icono", PresentacionEstadisticas.iconoEstado("EN_PROCESO"));
        detalleEnProceso.put("descripcion", "Solicitudes en proceso de revisión");
        resumenPorEstado.put("EN_PROCESO", detalleEnProceso);

        if (totalEnProceso > 0 && totalEnProceso > maxSolicitudes) {
            maxSolicitudes = totalEnProceso;
            estadoMasComun = "EN_PROCESO";
        }

        int completadas = totalPorEstado.get("APROBADA") + totalPorEstado.get("RECHAZADA");
        Map<String, Object> analisis = new HashMap<>();
        analisis.put("estadoMasComun", estadoMasComun);
        analisis.put("estadoMenosComun", estadoMenosComun);
        analisis.put("estadoMasEficiente", estadoMasEficiente);
        analisis.put("maxSolicitudes", maxSolicitudes);
        analisis.put("minSolicitudes", minSolicitudes);
        analisis.put("totalSolicitudes", totalSolicitudes);
        analisis.put("estadosActivos", resumenPorEstado.size());
        analisis.put("solicitudesPendientes", totalPorEstado.get("ENVIADA") + totalEnProceso);
        analisis.put("solicitudesCompletadas", completadas);
        analisis.put("tasaResolucion", redondear2(porcentaje(completadas, totalSolicitudes)));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("estados", resumenPorEstado);
        resultado.put("analisis", analisis);
        resultado.put("totalSolicitudes", totalSolicitudes);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas por estado de solicitudes con analisis de distribucion - DATOS REALES");
        return resultado;
    }

    // ------------------------------------------------------------------------------------------
    // Por mes
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> porPeriodo(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudes(periodoAcademico, idPrograma);

        Map<String, Contador> porMes = new HashMap<>();
        for (String mes : MESES) {
            porMes.put(mes, new Contador());
        }
        for (Solicitud solicitud : solicitudes) {
            String mes = mesDeRegistro(solicitud);
            if (mes != null) {
                Contador c = porMes.get(mes);
                String proceso = tipoProceso(solicitud);
                if (proceso != null) {
                    c.procesos.merge(proceso, 1, Integer::sum);
                }
                contar(c, solicitud, false);
            }
        }

        Map<String, Object> resumenPorMes = new HashMap<>();
        List<Map<String, Object>> mesesOrdenados = new ArrayList<>();
        List<String> mesesConDatos = new ArrayList<>();
        String mesMasActivo = null;
        String mesMasEficiente = null;
        String mesMasRapido = null;
        int maxSolicitudes = 0;
        double maxEficiencia = 0;
        double minTiempo = Double.MAX_VALUE;
        int totalSolicitudes = 0;

        // Todos los meses van en el resultado, aunque tengan 0 solicitudes, para que el frontend muestre la tendencia
        for (String mes : MESES) {
            Contador c = porMes.get(mes);
            totalSolicitudes += c.total;
            double eficiencia = c.eficiencia();
            double tiempoPromedio = c.tiempoPromedio();

            Map<String, Object> detalleMes = new HashMap<>();
            detalleMes.put("mes", mes);
            detalleMes.put("total", c.total);
            detalleMes.put("aprobadas", c.aprobadas);
            detalleMes.put("rechazadas", c.rechazadas);
            detalleMes.put("enviadas", c.enviadas);
            detalleMes.put("eficiencia", redondear2(eficiencia));
            detalleMes.put("tiempoPromedio", redondear2(tiempoPromedio));
            detalleMes.put("porcentaje", redondear2(porcentaje(c.total, solicitudes.size())));
            detalleMes.put("procesos", c.procesos);
            detalleMes.put("color", PresentacionEstadisticas.colorMes(mes));
            detalleMes.put("icono", PresentacionEstadisticas.SIN_ICONO);
            detalleMes.put("descripcion", PresentacionEstadisticas.descripcionMes(mes));
            resumenPorMes.put(mes, detalleMes);
            mesesOrdenados.add(detalleMes);

            if (c.total > 0) {
                mesesConDatos.add(mes);
                if (c.total > maxSolicitudes) {
                    maxSolicitudes = c.total;
                    mesMasActivo = mes;
                }
                if (eficiencia > maxEficiencia) {
                    maxEficiencia = eficiencia;
                    mesMasEficiente = mes;
                }
                if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                    minTiempo = tiempoPromedio;
                    mesMasRapido = mes;
                }
            }
        }

        // Tendencia: los dos últimos meses con datos
        String tendenciaGeneral = "Estable";
        double crecimientoPromedio = 0;
        if (mesesConDatos.size() >= 2) {
            int anterior = porMes.get(mesesConDatos.get(mesesConDatos.size() - 2)).total;
            int actual = porMes.get(mesesConDatos.get(mesesConDatos.size() - 1)).total;
            if (actual != anterior) {
                tendenciaGeneral = actual > anterior ? "Creciente" : "Decreciente";
                crecimientoPromedio = Math.abs(actual - anterior) * 100.0 / anterior;
            }
        }

        Map<String, Object> tendencias = new HashMap<>();
        tendencias.put("mesMasActivo", mesMasActivo);
        tendencias.put("mesMasEficiente", mesMasEficiente);
        tendencias.put("mesMasRapido", mesMasRapido);
        tendencias.put("maxSolicitudes", maxSolicitudes);
        tendencias.put("eficienciaMaxima", redondear2(maxEficiencia));
        tendencias.put("tiempoMinimo", redondear2(minTiempo));
        tendencias.put("tendenciaGeneral", tendenciaGeneral);
        tendencias.put("crecimientoPromedio", redondear2(crecimientoPromedio));
        tendencias.put("mesesAnalizados", mesesConDatos.size());
        tendencias.put("totalSolicitudes", totalSolicitudes);
        tendencias.put("promedioMensual", mesesConDatos.size() > 0
                ? Math.round((totalSolicitudes * 100.0) / mesesConDatos.size()) / 100.0
                : 0);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("porMes", resumenPorMes);
        resultado.put("tendencias", tendencias);
        resultado.put("todosLosMeses", Arrays.asList(MESES));
        resultado.put("mesesOrdenados", mesesOrdenados);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas por periodo/mes con analisis de tendencias - DATOS REALES");
        return resultado;
    }

    // ------------------------------------------------------------------------------------------
    // Por programa
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> porPrograma(String periodoAcademico, Integer idPrograma) {
        Date corte = corteTendencia(periodoAcademico);
        List<Programa> programas = datos.listarProgramas();
        List<Solicitud> solicitudes = solicitudes(periodoAcademico, idPrograma);
        List<Usuario> estudiantes = datos.listarEstudiantes();

        Map<String, Integer> estudiantesPorPrograma = new HashMap<>();
        Map<String, Contador> porPrograma = new HashMap<>();
        for (Programa programa : programas) {
            estudiantesPorPrograma.put(programa.getNombre_programa(), 0);
            porPrograma.put(programa.getNombre_programa(), new Contador());
        }
        for (Usuario estudiante : estudiantes) {
            if (estudiante.getObjPrograma() != null) {
                estudiantesPorPrograma.merge(estudiante.getObjPrograma().getNombre_programa(), 1, Integer::sum);
            }
        }
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                Contador c = porPrograma.get(nombrePrograma(solicitud));
                String proceso = tipoProceso(solicitud);
                if (proceso != null) {
                    c.procesos.merge(proceso, 1, Integer::sum);
                }
                if (solicitud.getFecha_registro_solicitud() != null) {
                    c.fechas.add(solicitud.getFecha_registro_solicitud());
                }
                contar(c, solicitud, false);
            }
        }

        Map<String, Integer> solicitudesPorPrograma = new HashMap<>();
        porPrograma.forEach((nombre, c) -> solicitudesPorPrograma.put(nombre, c.total));

        Map<String, Object> resumenPorPrograma = new HashMap<>();
        String programaMasActivo = null;
        String programaConMasEstudiantes = null;
        String programaMasEficiente = null;
        String programaMasRapido = null;
        int maxSolicitudes = 0;
        int maxEstudiantes = 0;
        double maxEficiencia = 0;
        double minTiempo = Double.MAX_VALUE;

        for (Programa programa : programas) {
            String nombre = programa.getNombre_programa();
            Contador c = porPrograma.get(nombre);
            int numEstudiantes = estudiantesPorPrograma.getOrDefault(nombre, 0);
            double eficiencia = c.eficiencia();
            double tiempoPromedio = c.tiempoPromedio();

            Map<String, Object> detallePrograma = new HashMap<>();
            detallePrograma.put("nombre", nombre);
            detallePrograma.put("codigo", programa.getCodigo());
            detallePrograma.put("totalSolicitudes", c.total);
            detallePrograma.put("estudiantes", numEstudiantes);
            detallePrograma.put("aprobadas", c.aprobadas);
            detallePrograma.put("rechazadas", c.rechazadas);
            detallePrograma.put("enviadas", c.enviadas);
            detallePrograma.put("eficiencia", redondear2(eficiencia));
            detallePrograma.put("tiempoPromedio", redondear2(tiempoPromedio));
            detallePrograma.put("tendencia", c.tendenciaReciente(corte));
            detallePrograma.put("porcentaje", redondear2(porcentaje(c.total, solicitudes.size())));
            detallePrograma.put("procesos", c.procesos);
            detallePrograma.put("color", PresentacionEstadisticas.colorPrograma(nombre));
            detallePrograma.put("icono", PresentacionEstadisticas.SIN_ICONO);
            detallePrograma.put("descripcion", PresentacionEstadisticas.descripcionPrograma(nombre));
            resumenPorPrograma.put(nombre, detallePrograma);

            if (c.total > maxSolicitudes) {
                maxSolicitudes = c.total;
                programaMasActivo = nombre;
            }
            if (numEstudiantes > maxEstudiantes) {
                maxEstudiantes = numEstudiantes;
                programaConMasEstudiantes = nombre;
            }
            if (eficiencia > maxEficiencia) {
                maxEficiencia = eficiencia;
                programaMasEficiente = nombre;
            }
            if (tiempoPromedio > 0 && tiempoPromedio < minTiempo) {
                minTiempo = tiempoPromedio;
                programaMasRapido = nombre;
            }
        }

        Map<String, Object> analisis = new HashMap<>();
        analisis.put("programaMasActivo", programaMasActivo);
        analisis.put("programaConMasEstudiantes", programaConMasEstudiantes);
        analisis.put("programaMasEficiente", programaMasEficiente);
        analisis.put("programaMasRapido", programaMasRapido);
        analisis.put("maxSolicitudes", maxSolicitudes);
        analisis.put("maxEstudiantes", maxEstudiantes);
        analisis.put("eficienciaMaxima", redondear2(maxEficiencia));
        analisis.put("tiempoMinimo", redondear2(minTiempo));
        analisis.put("totalProgramas", programas.size());
        analisis.put("totalSolicitudes", solicitudes.size());
        analisis.put("totalEstudiantes", estudiantes.size());

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("porPrograma", resumenPorPrograma);
        resultado.put("estudiantesPorPrograma", estudiantesPorPrograma);
        resultado.put("solicitudesPorPrograma", solicitudesPorPrograma);
        resultado.put("analisis", analisis);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Estadisticas por programa academico con analisis de rendimiento - DATOS REALES");
        return resultado;
    }

    // ------------------------------------------------------------------------------------------
    // Tendencias y comparativas
    // ------------------------------------------------------------------------------------------

    public Map<String, Object> tendenciasYComparativas(String periodoAcademico, Integer idPrograma) {
        List<Solicitud> solicitudes = solicitudes(periodoAcademico, idPrograma);
        List<Usuario> estudiantes = datos.listarEstudiantes();

        // Solicitudes por mes y estudiantes distintos que radicaron solicitudes en cada mes
        Map<String, Integer> solicitudesPorMes = new HashMap<>();
        Map<String, Integer> estudiantesPorMes = new HashMap<>();
        for (String mes : MESES) {
            solicitudesPorMes.put(mes, 0);
            estudiantesPorMes.put(mes, 0);
        }
        Map<String, Set<Integer>> estudiantesActivosPorMes = new HashMap<>();
        for (Solicitud solicitud : solicitudes) {
            String mes = mesDeRegistro(solicitud);
            if (mes != null) {
                solicitudesPorMes.merge(mes, 1, Integer::sum);
                if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getId_usuario() != null) {
                    estudiantesActivosPorMes.computeIfAbsent(mes, k -> new HashSet<>())
                            .add(solicitud.getObjUsuario().getId_usuario());
                }
            }
        }
        estudiantesActivosPorMes.forEach((mes, ids) -> estudiantesPorMes.put(mes, ids.size()));

        List<String> mesesConDatos = new ArrayList<>();
        for (String mes : MESES) {
            if (solicitudesPorMes.get(mes) > 0) {
                mesesConDatos.add(mes);
            }
        }

        String tendenciaSolicitudes = "Estable";
        String tendenciaEstudiantes = "Estable";
        double crecimientoSolicitudes = 0.0;
        double crecimientoEstudiantes = 0.0;
        if (mesesConDatos.size() >= 2) {
            // Los dos últimos meses con datos
            String mesAnterior = mesesConDatos.get(mesesConDatos.size() - 2);
            String mesActual = mesesConDatos.get(mesesConDatos.size() - 1);
            int solicitudesAnterior = solicitudesPorMes.get(mesAnterior);
            int solicitudesActual = solicitudesPorMes.get(mesActual);
            int estudiantesAnterior = estudiantesPorMes.get(mesAnterior);
            int estudiantesActual = estudiantesPorMes.get(mesActual);

            tendenciaSolicitudes = tendencia(solicitudesAnterior, solicitudesActual);
            crecimientoSolicitudes = variacion(solicitudesAnterior, solicitudesActual);
            tendenciaEstudiantes = tendencia(estudiantesAnterior, estudiantesActual);
            crecimientoEstudiantes = variacion(estudiantesAnterior, estudiantesActual);
        } else if (mesesConDatos.size() == 1) {
            // Un solo mes con datos: no hay con qué comparar
            tendenciaSolicitudes = "Nueva";
            tendenciaEstudiantes = "Nueva";
        }

        Map<String, Object> crecimientoTemporal = new HashMap<>();
        crecimientoTemporal.put("tendenciaSolicitudes", tendenciaSolicitudes);
        crecimientoTemporal.put("crecimientoSolicitudes", redondear2(crecimientoSolicitudes));
        crecimientoTemporal.put("tendenciaEstudiantes", tendenciaEstudiantes);
        crecimientoTemporal.put("crecimientoEstudiantes", redondear2(crecimientoEstudiantes));
        crecimientoTemporal.put("mesesAnalizados", mesesConDatos.size());
        crecimientoTemporal.put("totalSolicitudes", solicitudes.size());
        crecimientoTemporal.put("totalEstudiantes", estudiantes.size());
        crecimientoTemporal.put("solicitudesPorMes", solicitudesPorMes);
        crecimientoTemporal.put("estudiantesPorMes", estudiantesPorMes);

        // Comparativa por proceso
        Map<String, Integer> solicitudesPorProceso = new HashMap<>();
        Map<String, Integer> aprobadasPorProceso = new HashMap<>();
        for (String proceso : PROCESOS) {
            solicitudesPorProceso.put(proceso, 0);
            aprobadasPorProceso.put(proceso, 0);
        }
        for (Solicitud solicitud : solicitudes) {
            String proceso = tipoProceso(solicitud);
            if (proceso != null) {
                solicitudesPorProceso.merge(proceso, 1, Integer::sum);
                if ("APROBADA".equals(estadoActual(solicitud))) {
                    aprobadasPorProceso.merge(proceso, 1, Integer::sum);
                }
            }
        }

        String procesoMasDemandado = null;
        String procesoMasEficiente = null;
        int maxDemanda = 0;
        double maxEficiencia = 0;
        for (Map.Entry<String, Integer> entry : solicitudesPorProceso.entrySet()) {
            int total = entry.getValue();
            if (total > maxDemanda) {
                maxDemanda = total;
                procesoMasDemandado = entry.getKey();
            }
            double eficiencia = total > 0 ? (aprobadasPorProceso.get(entry.getKey()) * 100.0) / total : 0;
            if (eficiencia > maxEficiencia) {
                maxEficiencia = eficiencia;
                procesoMasEficiente = entry.getKey();
            }
        }

        Map<String, Object> comparativaProcesos = new HashMap<>();
        comparativaProcesos.put("procesoMasDemandado", procesoMasDemandado);
        comparativaProcesos.put("procesoMasEficiente", procesoMasEficiente);
        comparativaProcesos.put("eficienciaMasAlta", redondear2(maxEficiencia));
        comparativaProcesos.put("demandaMasAlta", maxDemanda);
        comparativaProcesos.put("solicitudesPorProceso", solicitudesPorProceso);
        comparativaProcesos.put("aprobadasPorProceso", aprobadasPorProceso);

        // Comparativa por programa
        List<Programa> programas = datos.listarProgramas();
        Map<String, Integer> solicitudesPorPrograma = new HashMap<>();
        Map<String, Integer> estudiantesPorPrograma = new HashMap<>();
        for (Programa programa : programas) {
            solicitudesPorPrograma.put(programa.getNombre_programa(), 0);
            estudiantesPorPrograma.put(programa.getNombre_programa(), 0);
        }
        for (Solicitud solicitud : solicitudes) {
            String programa = nombrePrograma(solicitud);
            if (solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getObjPrograma() != null) {
                solicitudesPorPrograma.merge(programa, 1, Integer::sum);
            }
        }
        for (Usuario estudiante : estudiantes) {
            if (estudiante.getObjPrograma() != null) {
                estudiantesPorPrograma.merge(estudiante.getObjPrograma().getNombre_programa(), 1, Integer::sum);
            }
        }

        String programaMasActivo = null;
        String programaConMasEstudiantes = null;
        int maxSolicitudes = 0;
        int maxEstudiantes = 0;
        for (Map.Entry<String, Integer> entry : solicitudesPorPrograma.entrySet()) {
            int numSolicitudes = entry.getValue();
            int numEstudiantes = estudiantesPorPrograma.getOrDefault(entry.getKey(), 0);
            if (numSolicitudes > maxSolicitudes) {
                maxSolicitudes = numSolicitudes;
                programaMasActivo = entry.getKey();
            }
            if (numEstudiantes > maxEstudiantes) {
                maxEstudiantes = numEstudiantes;
                programaConMasEstudiantes = entry.getKey();
            }
        }

        Map<String, Object> comparativaProgramas = new HashMap<>();
        comparativaProgramas.put("programaMasActivo", programaMasActivo);
        comparativaProgramas.put("programaConMasEstudiantes", programaConMasEstudiantes);
        comparativaProgramas.put("maxSolicitudes", maxSolicitudes);
        comparativaProgramas.put("maxEstudiantes", maxEstudiantes);
        comparativaProgramas.put("solicitudesPorPrograma", solicitudesPorPrograma);
        comparativaProgramas.put("estudiantesPorPrograma", estudiantesPorPrograma);

        Map<String, Object> resumenEstrategico = new HashMap<>();
        resumenEstrategico.put("totalSolicitudes", solicitudes.size());
        resumenEstrategico.put("totalEstudiantes", estudiantes.size());
        resumenEstrategico.put("totalProgramas", programas.size());
        resumenEstrategico.put("periodoAnalizado", "Ultimos 12 meses");
        resumenEstrategico.put("recomendacionEstrategica", recomendacionEstrategica(tendenciaSolicitudes,
                crecimientoSolicitudes, procesoMasDemandado, programaMasActivo, maxEficiencia, maxSolicitudes));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("crecimientoTemporal", crecimientoTemporal);
        resultado.put("comparativaProcesos", comparativaProcesos);
        resultado.put("comparativaProgramas", comparativaProgramas);
        resultado.put("resumenEstrategico", resumenEstrategico);
        resultado.put("fechaConsulta", new Date());
        resultado.put("descripcion", "Analisis de tendencias y comparativas estrategicas - DATOS REALES");
        return resultado;
    }

    private static String tendencia(int anterior, int actual) {
        return actual > anterior ? "Creciente" : actual < anterior ? "Decreciente" : "Estable";
    }

    /** Variación porcentual (siempre positiva) respecto al valor anterior; 0 si el anterior es 0. */
    private static double variacion(int anterior, int actual) {
        return anterior > 0 ? Math.abs(actual - anterior) * 100.0 / anterior : 0.0;
    }

    private static String recomendacionEstrategica(String tendenciaSolicitudes, double crecimientoSolicitudes,
            String procesoMasDemandado, String programaMasActivo, double maxEficiencia, int maxSolicitudes) {
        StringBuilder recomendacion = new StringBuilder();

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

        if (maxEficiencia < 30) {
            recomendacion.append(" BAJA EFICIENCIA: Revisar procesos de aprobacion. ");
        } else if (maxEficiencia < 60) {
            recomendacion.append(" EFICIENCIA MODERADA: Optimizar procesos de aprobacion. ");
        }

        if (programaMasActivo != null) {
            recomendacion.append(" ENFOQUE: ").append(programaMasActivo).append(" requiere atencion prioritaria");
            if (maxSolicitudes > 15) {
                recomendacion.append(" (alta demanda)");
            } else if (maxSolicitudes > 10) {
                recomendacion.append(" (demanda moderada)");
            }
            recomendacion.append(".");
        }
        return recomendacion.toString();
    }

    private static double porcentaje(int parte, int total) {
        return total > 0 ? (parte * 100.0) / total : 0.0;
    }
}
