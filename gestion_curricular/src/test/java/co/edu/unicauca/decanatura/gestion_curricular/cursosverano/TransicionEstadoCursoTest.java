package co.edu.unicauca.decanatura.gestion_curricular.cursosverano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso.TransicionInvalidaException;

class TransicionEstadoCursoTest {

    private static CursoOfertadoVerano curso(String... estados) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        List<EstadoCursoOfertado> lista = new ArrayList<>();
        for (String e : estados) {
            EstadoCursoOfertado estado = new EstadoCursoOfertado();
            estado.setEstado_actual(e);
            lista.add(estado);
        }
        curso.setEstadosCursoOfertados(lista);
        return curso;
    }

    private static CursoOfertadoVerano completo(String... estados) {
        CursoOfertadoVerano curso = curso(estados);
        curso.setObjMateria(new Materia());
        curso.setObjDocente(new Docente());
        curso.setCupo_estimado(20);
        curso.setSalon("101");
        return curso;
    }

    @Test
    @DisplayName("El estado actual es el último registrado, o Sin_Estado si no hay")
    void estadoActual() {
        assertThat(TransicionEstadoCurso.estadoActual(curso("Borrador", "Abierto"))).isEqualTo("Abierto");
        assertThat(TransicionEstadoCurso.estadoActual(curso())).isEqualTo(TransicionEstadoCurso.SIN_ESTADO);
    }

    @Test
    @DisplayName("Permite avanzar y retroceder un paso en el flujo")
    void transicionesPermitidas() {
        assertThatCode(() -> TransicionEstadoCurso.validar(completo("Borrador"), "Abierto")).doesNotThrowAnyException();
        assertThatCode(() -> TransicionEstadoCurso.validar(curso("Abierto"), "Publicado")).doesNotThrowAnyException();
        assertThatCode(() -> TransicionEstadoCurso.validar(curso("Publicado"), "Abierto")).doesNotThrowAnyException();
        assertThatCode(() -> TransicionEstadoCurso.validar(curso("Preinscripcion"), "Cerrado")).doesNotThrowAnyException();
        assertThatCode(() -> TransicionEstadoCurso.validar(curso("Inscripcion"), "Cerrado")).doesNotThrowAnyException();
        assertThatCode(() -> TransicionEstadoCurso.validar(curso("Inscripcion"), "Inscripcion")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No permite saltarse pasos")
    void noSaltaPasos() {
        assertThatThrownBy(() -> TransicionEstadoCurso.validar(curso("Abierto"), "Inscripcion"))
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessageContaining("Desde 'Abierto'");
    }

    @Test
    @DisplayName("Para pasar a Abierto el curso debe estar completo")
    void abiertoExigeCursoCompleto() {
        assertThatThrownBy(() -> TransicionEstadoCurso.validar(curso("Borrador"), "Abierto"))
                .isInstanceOfSatisfying(TransicionInvalidaException.class,
                        e -> assertThat(e.getError()).isEqualTo("Curso incompleto"));
    }

    @Test
    @DisplayName("Un curso cerrado no cambia de estado")
    void cerradoEsFinal() {
        assertThatThrownBy(() -> TransicionEstadoCurso.validar(curso("Cerrado"), "Inscripcion"))
                .isInstanceOfSatisfying(TransicionInvalidaException.class,
                        e -> assertThat(e.getError()).isEqualTo("Estado final"));
    }
}
