package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoCursoOfertadoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway.GestionarMateriasGatewayImplAdapter;

public class GestionarCursoOfertadoVeranoCUAdapter implements GestionarCursoOfertadoVeranoCUIntPort {

    private final GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway;
    private final GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarMateriasIntPort objGestionarMateriasGateway;
    private final GestionarDocenteGatewayIntPort objGestionarDocenteGateway;
    private final GestionarEstadoCursoOfertadoGatewayIntPort objGestionarEstadoCursoOfertadoGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarCursoOfertadoVeranoCUAdapter(GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway, 
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarMateriasIntPort objGestionarMateriasGateway,
            GestionarDocenteGatewayIntPort objGestionarDocenteGateway,
            GestionarEstadoCursoOfertadoGatewayIntPort objGestionarEstadoCursoOfertadoGateway,
            FormateadorResultadosIntPort objFormateadorResultados) {
        this.objGestionarCursoOfertadoVeranoGateway = objGestionarCursoOfertadoVeranoGateway;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarMateriasGateway = objGestionarMateriasGateway;
        this.objGestionarDocenteGateway = objGestionarDocenteGateway;
        this.objGestionarEstadoCursoOfertadoGateway = objGestionarEstadoCursoOfertadoGateway;
        this.objFormateadorResultados = objFormateadorResultados;
    }

    @Override
    public CursoOfertadoVerano actualizarCurso(CursoOfertadoVerano curso, EstadoCursoOfertado estadoCurso) {
        CursoOfertadoVerano cursoABuscar = curso;
        Integer idCurso = null;
        Integer idEstado = null;
        Usuario usuario = null;
        EstadoSolicitud estadoSolicitud = null;
        EstadoCursoOfertado estadoCursoOfertado = null;
        EstadoCursoOfertado nuevoEstado = null;
        Integer sizeEstados = null;
        if(curso == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el curso");
        }
        if(curso.getId_curso() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el curso");
        }
        idCurso = curso.getId_curso();
        cursoABuscar = objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if(cursoABuscar == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encuentra el curso");
        }
        if(estadoCurso == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el estado");
        }
        if(estadoCurso.getId_estado() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el estado");
        }
        idEstado = estadoCurso.getId_estado();
        estadoCursoOfertado = this.objGestionarEstadoCursoOfertadoGateway.buscarPorIEstadoCursoOfertado(idEstado);
        if(estadoCursoOfertado == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No existe el estado con el ID: " + idEstado);
        }
        if(estadoCurso.getEstado_actual() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el estado actual");
        }

        if(estadoCurso.getEstado_actual().equals("Preinscripcion")){
            if(cursoABuscar.getEstadosCursoOfertados().isEmpty()){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede actualizar el curso, porque no tiene estados");
            }
            sizeEstados = cursoABuscar.getEstadosCursoOfertados().size();
            if(cursoABuscar.getEstadosCursoOfertados().get(sizeEstados-1).getEstado_actual().equals("Abierto")){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede actualizar el curso, porque no esta publicado");
            }

            List<Solicitud> solicitudes = this.objGestionarSolicitudGateway.buscarPorNombreCursoYSeleccionadoPre(idCurso, true);
            if(!solicitudes.isEmpty()){
                if(solicitudes.size()< 20){
                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se puede publicar el curso, porque no se alcanzo el cupo estimado");
                }
                    for (Solicitud solicitud : solicitudes) {
                        estadoSolicitud = new EstadoSolicitud();
                        estadoSolicitud.setFecha_registro_estado(new Date());
                        estadoSolicitud.setEstado_actual("Aprobado");
                        solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                        usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getId_solicitud());
                        this.objGestionarCursoOfertadoVeranoGateway.asociarUsuarioCurso(usuario.getId_usuario(), idCurso);
                    }
                    

            }
        }

        if(estadoCurso.getEstado_actual().equals("Inscripcion")){
           if(cursoABuscar.getEstadosCursoOfertados().isEmpty()){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede actualizar el curso, porque no tiene estados");
            }
            sizeEstados = cursoABuscar.getEstadosCursoOfertados().size();
            if(cursoABuscar.getEstadosCursoOfertados().get(sizeEstados-1).getEstado_actual().equals("Preinscripcion")){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede actualizar el curso, porque no esta publicado");
            }

            List<Solicitud> solicitudes = this.objGestionarSolicitudGateway.buscarPorNombreCursoYSeleccionadoIns(idCurso, true);
            if(!solicitudes.isEmpty()){
                if(solicitudes.size()< cursoABuscar.getCupo_estimado()){
                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se puede publicar el curso, porque no se alcanzo el cupo estimado");
                }
                for (Solicitud solicitud : solicitudes) {
                    estadoSolicitud = new EstadoSolicitud();
                    estadoSolicitud.setFecha_registro_estado(new Date());
                    estadoSolicitud.setEstado_actual("Aprobado");
                    solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                    usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getId_solicitud());
                    if(solicitudes.size() < cursoABuscar.getEstudiantesInscritos().size()){
                        for (Usuario usuarioViejo : cursoABuscar.getEstudiantesInscritos()) {
                            if(usuarioViejo.getId_usuario() != usuario.getId_usuario()){
                                this.objGestionarCursoOfertadoVeranoGateway.desasociarUsuarioCurso(usuarioViejo.getId_usuario(), idCurso);
                            }
                        }
                    }
                }

            }
        }

        nuevoEstado = new EstadoCursoOfertado();
        nuevoEstado.setFecha_registro_estado(new Date());
        nuevoEstado.setEstado_actual(estadoCurso.getEstado_actual());
        //nuevoEstado.setObjCursoOfertadoVerano(cursoABuscar);

        return this.objGestionarCursoOfertadoVeranoGateway.actualizarCurso(cursoABuscar, nuevoEstado);
    }

    @Override
    public CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso) {
        List<CursoOfertadoVerano> cursos = null;
        CursoOfertadoVerano cursoGuardado = null;
        Materia materia = null;
        Docente docente = null;
        Boolean bandera = false;
        if(curso == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el curso");
        }
        if(curso.getObjMateria() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en la materia");
        }
        if(curso.getObjMateria().getId_materia() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en la   materia");
        }
        materia = this.objGestionarMateriasGateway.obtenerMateriaPorId(curso.getObjMateria().getId_materia());
        if(materia == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No existe la materia con el ID: " + curso.getObjMateria().getId_materia());
        }
        if(curso.getObjDocente() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el docente");
        }
        if(curso.getObjDocente().getId_docente() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el docente");
        }
        docente = this.objGestionarDocenteGateway.buscarDocentePorId(curso.getObjDocente().getId_docente());
        if(docente == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No existe el docente con el ID: " + curso.getObjDocente().getId_docente());
        }            
        cursos = this.objGestionarCursoOfertadoVeranoGateway.listarTodos();
        if(!cursos.isEmpty()){
            for (CursoOfertadoVerano cursoOfertadoVerano : cursos) {
                if(curso.getObjMateria().getId_materia() == cursoOfertadoVerano.getObjMateria().getId_materia()){
                    if(curso.getGrupo().name() == cursoOfertadoVerano.getGrupo().name()){
                        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El grupo asignado ya esta en uso"+ curso.getGrupo().name());
                        bandera = true;
                        break;
                    }
                }
            }
        }
        if(!bandera){
            cursoGuardado = this.objGestionarCursoOfertadoVeranoGateway.crearCurso(curso);
        }

        return cursoGuardado;
    }

    @Override
    public boolean eliminarCurso(Integer idCurso) {
        CursoOfertadoVerano cursosABuscar=null;
        cursosABuscar = this.objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if(cursosABuscar == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encuentra el curso");
        }
        return this.objGestionarCursoOfertadoVeranoGateway.eliminarCurso(idCurso);
    }

    @Override
    public CursoOfertadoVerano obtenerCursoPorId(Integer idCurso) {
        CursoOfertadoVerano cursosABuscar=null;
        cursosABuscar = this.objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if(cursosABuscar == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encuentra el curso");
        }
        return this.objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
    }

    @Override
    public List<CursoOfertadoVerano> listarTodos() {
        return this.objGestionarCursoOfertadoVeranoGateway.listarTodos();
    }

    
}
