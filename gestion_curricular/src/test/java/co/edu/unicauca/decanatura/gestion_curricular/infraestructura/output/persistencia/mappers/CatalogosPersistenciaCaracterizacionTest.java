package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SalonEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarDocenteGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarMateriasGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarProgramaGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarSalonesGatewayImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SalonRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a DUP-10: lo que los gateways de Programa, Docente, Materia y Salón
 * envían al repositorio y lo que devuelven, con valores completos, nulos y relaciones que el
 * dominio no tiene (los usuarios de un programa).
 */
class CatalogosPersistenciaCaracterizacionTest {

    private final List<Object> guardados = new ArrayList<>();

    @Test
    void programa() throws Exception {
        ProgramaRepositoryInt repositorio = mock(ProgramaRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        ProgramaEntity completo = programaEntity(1, "SIS", "Ingeniería de Sistemas");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId_usuario(5);
        usuario.setObjPrograma(completo);
        completo.getUsuarios().add(usuario);
        ProgramaEntity vacio = new ProgramaEntity();
        vacio.setUsuarios(null);
        when(repositorio.findById(1)).thenReturn(Optional.of(completo));
        when(repositorio.findById(2)).thenReturn(Optional.of(vacio));
        when(repositorio.findById(3)).thenReturn(Optional.empty());
        when(repositorio.buscarPorCodigo("SIS")).thenReturn(Optional.of(completo));
        when(repositorio.buscarPorNombreParcial("Ing")).thenReturn(List.of(completo, vacio));
        when(repositorio.findAll()).thenReturn(List.of(vacio, completo));
        GestionarProgramaGatewayImplAdapter gateway = new GestionarProgramaGatewayImplAdapter(
                repositorio, mock(UsuarioRepositoryInt.class));

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("buscarPorIdPrograma 1", GrafoObjetos.resultado(() -> gateway.buscarPorIdPrograma(1)));
        casos.set("buscarPorIdPrograma 2", GrafoObjetos.resultado(() -> gateway.buscarPorIdPrograma(2)));
        casos.set("buscarPorIdPrograma 3", GrafoObjetos.resultado(() -> gateway.buscarPorIdPrograma(3)));
        casos.set("obtenerProgramaPorId 1", GrafoObjetos.resultado(() -> gateway.obtenerProgramaPorId(1)));
        casos.set("obtenerProgramaPorId 3", GrafoObjetos.resultado(() -> gateway.obtenerProgramaPorId(3)));
        casos.set("obtenerProgramaPorCodigo", GrafoObjetos.resultado(() -> gateway.obtenerProgramaPorCodigo("SIS")));
        casos.set("buscarPorNombreParcial", GrafoObjetos.resultado(() -> gateway.buscarPorNombreParcial("Ing")));
        casos.set("listarProgramas", GrafoObjetos.resultado(gateway::listarProgramas));
        casos.set("guardarPrograma completo", guardado(() -> gateway.guardarPrograma(new Programa(7, "ELE", "Electrónica"))));
        casos.set("guardarPrograma nulos", guardado(() -> gateway.guardarPrograma(new Programa())));
        casos.set("actualizarPrograma", guardado(() -> gateway.actualizarPrograma(new Programa(1, null, "Sistemas"))));
        casos.set("actualizarPrograma inexistente", guardado(() -> gateway.actualizarPrograma(new Programa(3, "X", "Y"))));
        GrafoObjetos.compararConReferencia(casos, "persistencia/programa.json");
    }

    @Test
    void docente() throws Exception {
        DocenteRepositoryInt repositorio = mock(DocenteRepositoryInt.class);
        when(repositorio.save(any())).thenAnswer(inv -> guardar(inv.getArgument(0)));
        DocenteEntity completo = new DocenteEntity();
        completo.setId_docente(1);
        completo.setCodigo_docente("D01");
        completo.setNombre_docente("Ana Pérez");
        DocenteEntity vacio = new DocenteEntity();
        when(repositorio.findById(1)).thenReturn(Optional.of(completo));
        when(repositorio.findById(2)).thenReturn(Optional.of(vacio));
        when(repositorio.findById(3)).thenReturn(Optional.empty());
        when(repositorio.buscarPorCodigo("D01")).thenReturn(Optional.of(completo));
        when(repositorio.buscarPorNombreParcial("An")).thenReturn(List.of(completo, vacio));
        when(repositorio.findAll()).thenReturn(List.of(vacio, completo));
        GestionarDocenteGatewayImplAdapter gateway = new GestionarDocenteGatewayImplAdapter(
                repositorio, mock(CursoOfertadoVeranoRepositoryInt.class));

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("obtenerDocentePorId 1", GrafoObjetos.resultado(() -> gateway.obtenerDocentePorId(1)));
        casos.set("obtenerDocentePorId 2", GrafoObjetos.resultado(() -> gateway.obtenerDocentePorId(2)));
        casos.set("obtenerDocentePorId 3", GrafoObjetos.resultado(() -> gateway.obtenerDocentePorId(3)));
        casos.set("obtenerDocentePorCodigo", GrafoObjetos.resultado(() -> gateway.obtenerDocentePorCodigo("D01")));
        casos.set("buscarPorNombreParcial", GrafoObjetos.resultado(() -> gateway.buscarPorNombreParcial("An")));
        casos.set("listarDocentes", GrafoObjetos.resultado(gateway::listarDocentes));
        casos.set("guardarDocente completo", guardado(() -> gateway.guardarDocente(new Docente(9, "D09", "Luis"))));
        casos.set("guardarDocente nulos", guardado(() -> gateway.guardarDocente(new Docente())));
        casos.set("actualizarDocente", guardado(() -> gateway.actualizarDocente(new Docente(1, null, "Ana"))));
        casos.set("actualizarDocente inexistente", guardado(() -> gateway.actualizarDocente(new Docente(3, "X", "Y"))));
        GrafoObjetos.compararConReferencia(casos, "persistencia/docente.json");
    }

    @Test
    void materia() throws Exception {
        MateriaRepositoryInt repositorio = mock(MateriaRepositoryInt.class);
        MateriaEntity completa = new MateriaEntity();
        completa.setId_materia(1);
        completa.setCodigo("MAT1");
        completa.setNombre("Cálculo");
        completa.setCreditos(4);
        MateriaEntity vacia = new MateriaEntity();
        when(repositorio.findById(1)).thenReturn(Optional.of(completa));
        when(repositorio.findById(2)).thenReturn(Optional.of(vacia));
        when(repositorio.findById(3)).thenReturn(Optional.empty());
        when(repositorio.findAll()).thenReturn(List.of(completa, vacia));
        GestionarMateriasGatewayImplAdapter gateway = new GestionarMateriasGatewayImplAdapter(repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("obtenerMateriaPorId 1", GrafoObjetos.resultado(() -> gateway.obtenerMateriaPorId(1)));
        casos.set("obtenerMateriaPorId 2", GrafoObjetos.resultado(() -> gateway.obtenerMateriaPorId(2)));
        casos.set("obtenerMateriaPorId 3", GrafoObjetos.resultado(() -> gateway.obtenerMateriaPorId(3)));
        casos.set("listarMaterias", GrafoObjetos.resultado(gateway::listarMaterias));
        GrafoObjetos.compararConReferencia(casos, "persistencia/materia.json");
    }

    @Test
    void salon() throws Exception {
        SalonRepositoryInt repositorio = mock(SalonRepositoryInt.class);
        SalonEntity completo = new SalonEntity();
        completo.setId_salon(1);
        completo.setNumero_salon("221");
        completo.setEdificio("FIET");
        completo.setActivo(true);
        SalonEntity inactivo = new SalonEntity();
        inactivo.setId_salon(2);
        inactivo.setActivo(false);
        SalonEntity vacio = new SalonEntity();
        when(repositorio.findById(1)).thenReturn(Optional.of(completo));
        when(repositorio.findById(2)).thenReturn(Optional.of(inactivo));
        when(repositorio.findById(3)).thenReturn(Optional.empty());
        when(repositorio.buscarPorNumero("221")).thenReturn(Optional.of(completo));
        when(repositorio.buscarPorNumero("000")).thenReturn(Optional.of(vacio));
        when(repositorio.listarSalonesActivos()).thenReturn(List.of(completo, vacio));
        GestionarSalonesGatewayImplAdapter gateway = new GestionarSalonesGatewayImplAdapter(
                repositorio);

        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        casos.set("obtenerSalonPorId 1", GrafoObjetos.resultado(() -> gateway.obtenerSalonPorId(1)));
        casos.set("obtenerSalonPorId 2", GrafoObjetos.resultado(() -> gateway.obtenerSalonPorId(2)));
        casos.set("obtenerSalonPorId 3", GrafoObjetos.resultado(() -> gateway.obtenerSalonPorId(3)));
        casos.set("obtenerSalonPorNumero 221", GrafoObjetos.resultado(() -> gateway.obtenerSalonPorNumero("221")));
        casos.set("obtenerSalonPorNumero 000", GrafoObjetos.resultado(() -> gateway.obtenerSalonPorNumero("000")));
        casos.set("listarSalonesActivos", GrafoObjetos.resultado(gateway::listarSalonesActivos));
        GrafoObjetos.compararConReferencia(casos, "persistencia/salon.json");
    }

    private static ProgramaEntity programaEntity(int id, String codigo, String nombre) {
        ProgramaEntity entidad = new ProgramaEntity();
        entidad.setId_programa(id);
        entidad.setCodigo(codigo);
        entidad.setNombre_programa(nombre);
        return entidad;
    }

    private Object guardar(Object entidad) {
        guardados.add(entidad);
        return entidad;
    }

    /** Lo que se envió a {@code save} y lo que devolvió el gateway. */
    private ObjectNode guardado(Callable<?> llamada) {
        guardados.clear();
        ObjectNode caso = JsonNodeFactory.instance.objectNode();
        caso.set("resultado", GrafoObjetos.resultado(llamada));
        caso.set("guardados", GrafoObjetos.volcar(new ArrayList<>(guardados)));
        return caso;
    }
}
