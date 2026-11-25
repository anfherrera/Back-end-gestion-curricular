package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.proyecciones.FilaEstadoEstadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.proyecciones.FilaSolicitudEstadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;

/**
 * Hace que un repositorio simulado devuelva, para las estadísticas, las mismas filas que devolvería la base de
 * datos con estas solicitudes. Así las pruebas siguen escribiendo los datos como entidades.
 */
final class FilasEstadisticas {

    private FilasEstadisticas() {
    }

    static void simular(SolicitudRepositoryInt repositorio, List<SolicitudEntity> solicitudes) {
        List<FilaSolicitudEstadistica> filas = new ArrayList<>();
        List<FilaEstadoEstadistica> estados = new ArrayList<>();
        for (SolicitudEntity s : solicitudes) {
            var usuario = s.getObjUsuario();
            var programa = usuario != null ? usuario.getObjPrograma() : null;
            var curso = s.getObjCursoOfertadoVerano();
            var materia = curso != null ? curso.getObjMateria() : null;
            filas.add(new FilaSolicitudEstadistica(
                    s.getId_solicitud(), s.getClass(), s.getNombre_solicitud(), s.getPeriodo_academico(),
                    s.getFecha_registro_solicitud(),
                    usuario != null ? usuario.getId_usuario() : null,
                    usuario != null ? usuario.getNombre_completo() : null,
                    programa != null ? programa.getId_programa() : null,
                    programa != null ? programa.getCodigo() : null,
                    programa != null ? programa.getNombre_programa() : null,
                    curso != null ? curso.getId_curso() : null,
                    curso != null ? curso.getPeriodo_academico() : null,
                    materia != null ? materia.getId_materia() : null,
                    materia != null ? materia.getCodigo() : null,
                    materia != null ? materia.getNombre() : null));
            if (s.getEstadosSolicitud() != null) {
                for (EstadoSolicitudEntity e : s.getEstadosSolicitud()) {
                    estados.add(new FilaEstadoEstadistica(s.getId_solicitud(), e.getEstado_actual(), e.getFecha_registro_estado()));
                }
            }
        }
        when(repositorio.buscarSolicitudesParaEstadisticas()).thenReturn(filas);
        when(repositorio.buscarEstadosParaEstadisticas()).thenReturn(estados);
    }
}
