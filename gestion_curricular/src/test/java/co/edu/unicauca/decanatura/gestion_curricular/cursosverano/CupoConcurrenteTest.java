package co.edu.unicauca.decanatura.gestion_curricular.cursosverano;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.CondicionSolicitudVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;

/**
 * Dos funcionarios aceptan al mismo tiempo inscripciones del mismo curso con un solo cupo:
 * el curso se bloquea durante la aceptación, así que solo una puede pasar.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Cursos de verano - Cupo con aceptaciones simultáneas")
class CupoConcurrenteTest {

    @Autowired private GestionarSolicitudCursoVeranoGatewayIntPort gateway;
    @Autowired private CursoOfertadoVeranoRepositoryInt cursoRepository;
    @Autowired private MateriaRepositoryInt materiaRepository;
    @Autowired private DocenteRepositoryInt docenteRepository;
    @Autowired private SolicitudRepositoryInt solicitudRepository;

    @Test
    @DisplayName("Curso con cupo 1 y dos aceptaciones a la vez: una se acepta y la otra recibe cupo agotado")
    void soloUnaAceptacionPasa() throws Exception {
        CursoOfertadoVeranoEntity curso = crearCurso(1);
        int idA = crearInscripcionEnviada(curso, "A");
        int idB = crearInscripcionEnviada(curso, "B");

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (int id : new int[] { idA, idB }) {
                Callable<Boolean> aceptar = () -> {
                    salida.await();
                    try {
                        gateway.validarPago(id, true, "Pago correcto");
                        return true;
                    } catch (CupoAgotadoException e) {
                        return false;
                    }
                };
                resultados.add(hilos.submit(aceptar));
            }
            salida.countDown();

            int aceptadas = 0;
            for (Future<Boolean> r : resultados) {
                if (obtener(r)) {
                    aceptadas++;
                }
            }
            assertThat(aceptadas).isEqualTo(1);
            assertThat(gateway.contarInscripcionesAceptadasPorCurso(curso.getId_curso())).isEqualTo(1);
        } finally {
            hilos.shutdownNow();
        }
    }

    private static boolean obtener(Future<Boolean> resultado) throws Exception {
        try {
            return resultado.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new AssertionError("La aceptación falló con un error inesperado", e.getCause());
        }
    }

    private CursoOfertadoVeranoEntity crearCurso(int cupo) {
        String sufijo = Long.toString(System.nanoTime() % 1_000_000_000L, 36);

        MateriaEntity materia = new MateriaEntity();
        materia.setCodigo("CC" + sufijo);
        materia.setNombre("Materia cupo " + sufijo);
        materia.setCreditos(3);
        materia = materiaRepository.save(materia);

        DocenteEntity docente = new DocenteEntity();
        docente.setCodigo_docente("DC" + sufijo);
        docente.setNombre_docente("Docente cupo " + sufijo);
        docente = docenteRepository.save(docente);

        CursoOfertadoVeranoEntity curso = new CursoOfertadoVeranoEntity();
        curso.setGrupo(GrupoCursoVeranoEntity.A);
        curso.setCupo_estimado(cupo);
        curso.setSalon("Salón 101");
        curso.setPeriodo_academico("2026-2");
        curso.setObjMateria(materia);
        curso.setObjDocente(docente);
        return cursoRepository.save(curso);
    }

    private int crearInscripcionEnviada(CursoOfertadoVeranoEntity curso, String nombre) {
        SolicitudCursoVeranoInscripcionEntity inscripcion = new SolicitudCursoVeranoInscripcionEntity();
        inscripcion.setNombre_solicitud("Inscripción " + nombre);
        inscripcion.setPeriodo_academico("2026-2");
        inscripcion.setFecha_registro_solicitud(new Date());
        inscripcion.setNombre_estudiante("Estudiante " + nombre);
        inscripcion.setCodicion_solicitud(CondicionSolicitudVeranoEntity.Primera_Vez);
        inscripcion.setObservacion("Prueba de cupo");
        inscripcion.setObjCursoOfertadoVerano(curso);

        EstadoSolicitudEntity enviada = new EstadoSolicitudEntity();
        enviada.setEstado_actual("Enviada");
        // Fecha anterior para que "Pago_Validado" quede como último estado
        enviada.setFecha_registro_estado(new Date(System.currentTimeMillis() - 60_000));
        enviada.setObjSolicitud(inscripcion);
        List<EstadoSolicitudEntity> estados = new ArrayList<>();
        estados.add(enviada);
        inscripcion.setEstadosSolicitud(estados);

        return solicitudRepository.save(inscripcion).getId_solicitud();
    }
}
