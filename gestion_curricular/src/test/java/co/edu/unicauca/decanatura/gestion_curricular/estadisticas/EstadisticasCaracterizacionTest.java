package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarEstadisticasCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarEstadisticasGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadisticaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * Fija la respuesta de cada consulta de estadísticas para un conjunto de datos fijo. Pasa por el caso de uso
 * y el gateway real (con repositorios simulados), así que cubre también la conversión a modelos de dominio.
 * Cada caso se compara con su archivo en
 * {@code src/test/resources/referencias/estadisticas/}.
 *
 * Los datos son de 2024 y 2025, así que la tendencia "últimos 90 días" siempre sale igual.
 * Se quitan las fechas que dependen del momento de la consulta (fechaConsulta, fechaGeneracion, fechaPrediccion).
 *
 * Para regrabar las referencias tras un cambio intencional:
 * {@code mvnw test -Dtest=EstadisticasCaracterizacionTest -DgrabarReferencia=true}
 */
@DisplayName("Estadísticas - respuestas fijadas")
class EstadisticasCaracterizacionTest {

    private static final Path CARPETA = Path.of("src/test/resources/referencias/estadisticas");
    private static final Set<String> FECHAS_DE_CONSULTA = Set.of("fechaConsulta", "fechaGeneracion", "fechaPrediccion");
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    private static TimeZone zonaOriginal;
    private static GestionarEstadisticasCUIntPort estadisticas;

    @BeforeAll
    static void preparar() {
        // Los meses y periodos se calculan con la zona del sistema; se fija para que la referencia no dependa del equipo
        zonaOriginal = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        estadisticas = crearCasoDeUso();
    }

    @AfterAll
    static void restaurar() {
        TimeZone.setDefault(zonaOriginal);
    }

    static Stream<Arguments> consultas() {
        return Stream.of(
                caso("globales-sin-filtros", g -> g.obtenerEstadisticasGlobales()),
                caso("globales-con-filtros", g -> g.obtenerEstadisticasGlobales("Homologacion", 1, fecha(2025, 1, 1), fecha(2025, 12, 31))),
                caso("por-proceso-homologacion", g -> g.obtenerEstadisticasPorProceso("Homologacion")),
                caso("por-proceso-paz-y-salvo", g -> g.obtenerEstadisticasPorProceso("Paz y Salvo")),
                caso("por-estado-aprobada", g -> g.obtenerEstadisticasPorEstado("APROBADA")),
                caso("por-estado-rechazada", g -> g.obtenerEstadisticasPorEstado("Rechazada")),
                caso("por-programa-id", g -> g.obtenerEstadisticasPorPrograma(1)),
                caso("por-periodo-fechas", g -> g.obtenerEstadisticasPorPeriodo(fecha(2025, 1, 1), fecha(2025, 6, 30))),
                caso("por-programa-y-periodo", g -> g.obtenerEstadisticasPorProgramaYPeriodo(2, fecha(2024, 7, 1), fecha(2025, 12, 31))),
                caso("por-proceso-y-estado", g -> g.obtenerEstadisticasPorProcesoYEstado("Cursos de Verano", "ENVIADA")),
                caso("resumen-completo", g -> g.obtenerResumenCompleto(null, null)),
                caso("resumen-completo-filtrado", g -> g.obtenerResumenCompleto("2025-1", 1)),
                caso("tendencias-por-periodo", g -> g.obtenerTendenciasPorPeriodo(
                        fecha(2024, 7, 1), fecha(2024, 12, 31), fecha(2025, 1, 1), fecha(2025, 6, 30))),
                caso("total-estudiantes", g -> g.obtenerNumeroTotalEstudiantes()),
                caso("estudiantes-por-programa", g -> g.obtenerEstudiantesPorPrograma(null, null)),
                caso("estudiantes-por-programa-filtrado", g -> g.obtenerEstudiantesPorPrograma(null, 2)),
                caso("detalladas-por-proceso", g -> g.obtenerEstadisticasDetalladasPorProceso(null, null)),
                caso("detalladas-por-proceso-filtrado", g -> g.obtenerEstadisticasDetalladasPorProceso("2025-2", null)),
                caso("por-estado-dashboard", g -> g.obtenerEstadisticasPorEstado(null, null)),
                caso("por-estado-dashboard-filtrado", g -> g.obtenerEstadisticasPorEstado("2025-1", 2)),
                caso("por-periodo-dashboard", g -> g.obtenerEstadisticasPorPeriodo((String) null, null)),
                caso("por-periodo-dashboard-filtrado", g -> g.obtenerEstadisticasPorPeriodo("2024-2", null)),
                caso("por-programa-dashboard", g -> g.obtenerEstadisticasPorPrograma((String) null, null)),
                caso("por-programa-dashboard-filtrado", g -> g.obtenerEstadisticasPorPrograma("2025-1", 1)),
                caso("tendencias-y-comparativas", g -> g.obtenerTendenciasYComparativas(null, null)),
                caso("tendencias-y-comparativas-filtrado", g -> g.obtenerTendenciasYComparativas("2025-2", 2)),
                caso("cursos-verano-filtrado", g -> g.obtenerEstadisticasCursosVerano("2025-2", 1)),
                caso("tendencias-temporales-cursos-verano", g -> g.obtenerTendenciasTemporalesCursosVerano(null, null)),
                caso("historial", g -> g.obtenerHistorialEstadisticas(2024, 2025)),
                caso("por-periodo-academico", g -> g.obtenerEstadisticasPorPeriodoAcademico("2025-1")),
                caso("por-periodo-academico-invalido", g -> g.obtenerEstadisticasPorPeriodoAcademico("2025-3")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("consultas")
    @DisplayName("La respuesta no cambia")
    void respuestaFijada(String nombre, Function<GestionarEstadisticasCUIntPort, Object> consulta) throws Exception {
        Object resultado = sinFechasDeConsulta(consulta.apply(estadisticas));
        String obtenido = JSON.writeValueAsString(resultado).replace("\r\n", "\n");

        Path referencia = CARPETA.resolve(nombre + ".json");
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(CARPETA);
            Files.writeString(referencia, obtenido, StandardCharsets.UTF_8);
        }
        String esperado = Files.readString(referencia, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertThat(obtenido).isEqualTo(esperado);
    }

    private static Arguments caso(String nombre, Function<GestionarEstadisticasCUIntPort, Object> consulta) {
        return Arguments.of(nombre, consulta);
    }

    /** Quita, a cualquier profundidad, las fechas que dependen de cuándo se corre la prueba. */
    private static Object sinFechasDeConsulta(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            mapa.keySet().removeIf(FECHAS_DE_CONSULTA::contains);
            mapa.values().forEach(EstadisticasCaracterizacionTest::sinFechasDeConsulta);
        } else if (valor instanceof Iterable<?> lista) {
            lista.forEach(EstadisticasCaracterizacionTest::sinFechasDeConsulta);
        }
        return valor;
    }

    // ------------------------------------------------------------------------------------------
    // Datos
    // ------------------------------------------------------------------------------------------

    private static GestionarEstadisticasCUIntPort crearCasoDeUso() {
        ProgramaEntity sistemas = programa(1, "Ingeniería de Sistemas");
        ProgramaEntity electronica = programa(2, "Ingeniería Electrónica");
        ProgramaEntity civil = programa(3, "Ingeniería Civil"); // sin solicitudes

        UsuarioEntity ana = estudiante(1, "Ana", sistemas);
        UsuarioEntity beto = estudiante(2, "Beto", sistemas);
        UsuarioEntity carla = estudiante(3, "Carla", electronica);
        UsuarioEntity dario = estudiante(4, "Darío", electronica);
        UsuarioEntity eva = estudiante(5, "Eva", civil);
        UsuarioEntity sinPrograma = estudiante(6, "Sin programa", null);

        CursoOfertadoVeranoEntity calculo = curso(10, "Cálculo I", "2025-2");
        CursoOfertadoVeranoEntity fisica = curso(11, "Física I", "2025-2");

        List<SolicitudEntity> solicitudes = new ArrayList<>();
        int id = 100;
        // Homologación: varios estados, con historial (sirve para tiempos de procesamiento)
        solicitudes.add(solicitud(new SolicitudHomologacionEntity(), id++, "Solicitud Homologacion - Ana", "2024-2", ana, null,
                estado("Enviada", 2024, 8, 5), estado("APROBADA_FUNCIONARIO", 2024, 8, 12), estado("APROBADA", 2024, 9, 1)));
        solicitudes.add(solicitud(new SolicitudHomologacionEntity(), id++, "Solicitud Homologacion - Beto", "2025-1", beto, null,
                estado("Enviada", 2025, 2, 3), estado("RECHAZADA", 2025, 2, 20)));
        solicitudes.add(solicitud(new SolicitudHomologacionEntity(), id++, "Solicitud Homologacion - Carla", "2025-1", carla, null,
                estado("Enviada", 2025, 3, 10), estado("APROBADA_COORDINADOR", 2025, 3, 25)));
        // Paz y salvo
        solicitudes.add(solicitud(new SolicitudPazYSalvoEntity(), id++, "Solicitud Paz y Salvo - Ana", "2025-1", ana, null,
                estado("Enviada", 2025, 4, 2), estado("Aprobado", 2025, 4, 30)));
        solicitudes.add(solicitud(new SolicitudPazYSalvoEntity(), id++, "Solicitud Paz y Salvo - Darío", "2025-2", dario, null,
                estado("ENVIADA", 2025, 8, 18)));
        // Reingreso: uno sin estados y otro con un estado desconocido (se cuenta como ENVIADA)
        solicitudes.add(solicitud(new SolicitudReingresoEntity(), id++, "Solicitud de Reingreso - Beto", "2024-2", beto, null));
        solicitudes.add(solicitud(new SolicitudReingresoEntity(), id++, "Solicitud de Reingreso - Eva", null, eva, null,
                estado("Enviada", 2025, 1, 20), estado("EN_REVISION", 2025, 1, 28)));
        // ECAES
        solicitudes.add(solicitud(new SolicitudEcaesEntity(), id++, "Solicitud ECAES - Carla", "2025-1", carla, null,
                estado("Enviada", 2025, 5, 6), estado("APROBADA", 2025, 5, 9)));
        solicitudes.add(solicitud(new SolicitudEcaesEntity(), id++, "Solicitud ECAES - Sin programa", "2025-2", sinPrograma, null,
                estado("Rechazada", 2025, 9, 1)));
        // Cursos de verano: preinscripciones e inscripciones; el periodo puede venir solo del curso
        solicitudes.add(solicitud(new SolicitudCursoVeranoPreinscripcionEntity(), id++, "Curso de verano - Ana", null, ana, calculo,
                estado("Enviada", 2025, 6, 10), estado("APROBADA", 2025, 6, 20)));
        solicitudes.add(solicitud(new SolicitudCursoVeranoPreinscripcionEntity(), id++, "Curso de verano - Beto", "2025-2", beto, calculo,
                estado("Enviada", 2025, 6, 12)));
        solicitudes.add(solicitud(new SolicitudCursoVeranoPreinscripcionEntity(), id++, "Curso de verano - Carla", "2025-2", carla, fisica,
                estado("Enviada", 2025, 7, 1), estado("Rechazada", 2025, 7, 3)));
        solicitudes.add(solicitud(new SolicitudCursoVeranoInscripcionEntity(), id++, "Inscripción curso - Ana", "2025-2", ana, calculo,
                estado("Pago_Validado", 2025, 7, 5), estado("Inscripcion_Completada", 2025, 7, 15)));
        solicitudes.add(solicitud(new SolicitudCursoVeranoInscripcionEntity(), id++, "Inscripción curso - Darío", "2025-2", dario, fisica,
                estado("Pago_Rechazado", 2025, 7, 8)));

        SolicitudRepositoryInt solicitudRepository = mock(SolicitudRepositoryInt.class);
        FilasEstadisticas.simular(solicitudRepository, solicitudes);
        // Conteos de la base de datos (estadísticas globales)
        when(solicitudRepository.contarEstado("APROBADA")).thenReturn(4);
        when(solicitudRepository.contarEstado("RECHAZADA")).thenReturn(4);
        when(solicitudRepository.contarEstado("APROBADA_FUNCIONARIO")).thenReturn(0);
        when(solicitudRepository.contarEstado("APROBADA_COORDINADOR")).thenReturn(1);
        when(solicitudRepository.contarEstado("ENVIADA")).thenReturn(4);
        when(solicitudRepository.contarEstado("PRE_REGISTRADO")).thenReturn(1);
        when(solicitudRepository.contarSolicitudesPorEstadoConFiltros(anyString(), eq("Homologacion"), eq(1), any(), any()))
                .thenReturn(1);
        when(solicitudRepository.contarSolicitudesConFiltros(eq("Homologacion"), eq(1), any(), any())).thenReturn(0);
        when(solicitudRepository.contarSolicitudesPorProgramaConFiltros(eq(1), isNull(), isNull(), isNull())).thenReturn(8);
        when(solicitudRepository.contarSolicitudesPorProgramaConFiltros(eq(2), isNull(), isNull(), isNull())).thenReturn(5);
        when(solicitudRepository.contarSolicitudesPorProgramaConFiltros(eq(1), eq("Homologacion"), any(), any())).thenReturn(1);

        ProgramaRepositoryInt programaRepository = mock(ProgramaRepositoryInt.class);
        when(programaRepository.findAll()).thenReturn(List.of(sistemas, electronica, civil));
        when(programaRepository.buscarNombresProgramas()).thenReturn(List.of(
                sistemas.getNombre_programa(), electronica.getNombre_programa(), civil.getNombre_programa()));

        UsuarioRepositoryInt usuarioRepository = mock(UsuarioRepositoryInt.class);
        when(usuarioRepository.buscarPorRol(2)).thenReturn(List.of(ana, beto, carla, dario, eva, sinPrograma));

        GestionarEstadisticasGatewayImplAdapter gateway = new GestionarEstadisticasGatewayImplAdapter(mock(EstadisticaRepositoryInt.class), programaRepository,
                solicitudRepository, usuarioRepository, new ModelMapper());
        return new GestionarEstadisticasCUAdapter(gateway, mock(FormateadorResultadosIntPort.class));
    }

    private static ProgramaEntity programa(int id, String nombre) {
        ProgramaEntity p = new ProgramaEntity();
        p.setId_programa(id);
        p.setNombre_programa(nombre);
        return p;
    }

    private static UsuarioEntity estudiante(int id, String nombre, ProgramaEntity programa) {
        UsuarioEntity u = new UsuarioEntity();
        u.setId_usuario(id);
        u.setNombre_completo(nombre);
        u.setObjPrograma(programa);
        return u;
    }

    private static CursoOfertadoVeranoEntity curso(int id, String materia, String periodo) {
        MateriaEntity m = new MateriaEntity();
        m.setId_materia(id);
        m.setNombre(materia);
        CursoOfertadoVeranoEntity c = new CursoOfertadoVeranoEntity();
        c.setId_curso(id);
        c.setObjMateria(m);
        c.setPeriodo_academico(periodo);
        return c;
    }

    /** La fecha de registro de la solicitud es la de su primer estado (o 2024-11-15 si no tiene estados). */
    private static SolicitudEntity solicitud(SolicitudEntity s, int id, String nombre, String periodo, UsuarioEntity usuario,
            CursoOfertadoVeranoEntity curso, EstadoSolicitudEntity... estados) {
        s.setId_solicitud(id);
        s.setNombre_solicitud(nombre);
        s.setPeriodo_academico(periodo);
        s.setObjUsuario(usuario);
        s.setObjCursoOfertadoVerano(curso);
        s.setFecha_registro_solicitud(estados.length > 0 ? estados[0].getFecha_registro_estado() : fecha(2024, 11, 15));
        List<EstadoSolicitudEntity> lista = new ArrayList<>();
        for (EstadoSolicitudEntity e : estados) {
            e.setObjSolicitud(s);
            lista.add(e);
        }
        s.setEstadosSolicitud(lista);
        return s;
    }

    private static EstadoSolicitudEntity estado(String nombre, int anio, int mes, int dia) {
        EstadoSolicitudEntity e = new EstadoSolicitudEntity();
        e.setEstado_actual(nombre);
        e.setFecha_registro_estado(fecha(anio, mes, dia));
        return e;
    }

    private static Date fecha(int anio, int mes, int dia) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
        c.clear();
        c.set(anio, mes - 1, dia, 10, 0);
        return c.getTime();
    }
}
