package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarCursoOfertadoVeranoCUAdapter implements GestionarCursoOfertadoVeranoCUIntPort {

    private final GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway;
    private final GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarMateriasIntPort objGestionarMateriasGateway;
    private final GestionarDocenteGatewayIntPort objGestionarDocenteGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarCursoOfertadoVeranoCUAdapter(GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway, 
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarMateriasIntPort objGestionarMateriasGateway,
            GestionarDocenteGatewayIntPort objGestionarDocenteGateway,
            FormateadorResultadosIntPort objFormateadorResultados) {
        this.objGestionarCursoOfertadoVeranoGateway = objGestionarCursoOfertadoVeranoGateway;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarMateriasGateway = objGestionarMateriasGateway;
        this.objGestionarDocenteGateway = objGestionarDocenteGateway;
        this.objFormateadorResultados = objFormateadorResultados;
    }

    @Override
    public CursoOfertadoVerano actualizarCurso(CursoOfertadoVerano curso, EstadoCursoOfertado estadoCurso) {
        Integer puntoEquilibrioMinimo = 2;
        CursoOfertadoVerano cursoABuscar = curso;
        Integer idCurso = null;
        Usuario usuario = null;
        EstadoSolicitud estadoSolicitud = null;
        EstadoCursoOfertado nuevoEstado = null;
        CursoOfertadoVerano cursoActualizado = null;
        List<Usuario> estudiantesInscritos = new ArrayList<Usuario>();
        List<Usuario> usuariosRemovidos = new ArrayList<Usuario>();
        Boolean bandera = false;
        if(curso == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el curso");
        }
        if(curso.getId_curso() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay id en el curso");
        }
        idCurso = curso.getId_curso();
        cursoABuscar = objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if(cursoABuscar == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encuentra el curso");
        }
        if(cursoABuscar.getEstadosCursoOfertados().isEmpty()){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede actualizar el curso, porque no tiene estados");
        }
        if(estadoCurso == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el estado");
        }
        if(estadoCurso.getId_estado() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay id en el estado");
        }
        // estadoCursoOfertado = this.objGestionarEstadoCursoOfertadoGateway.buscarPorIEstadoCursoOfertado(idEstado);
        // if(estadoCursoOfertado == null){
        //    this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No existe el estado con el ID: " + idEstado);
        // }
        if(estadoCurso.getEstado_actual() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el estado actual");
        }

        if(estadoCurso.getEstado_actual().equals("Preinscripcion")){
            if(cursoABuscar.getEstadosCursoOfertados().get(0).getEstado_actual().equals("Publicado")){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede actualizar el curso, porque no esta publicado");
            }

            List<Solicitud> solicitudes = this.objGestionarSolicitudGateway.buscarSolicitudPorNombreCursoPre(idCurso);
            if(solicitudes.isEmpty()){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede publicar el curso, porque no hay solicitudes");
            }
                if(solicitudes.size()< puntoEquilibrioMinimo){
                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se puede publicar el curso, porque no se alcanzo el cupo estimado");
                }
                    for (Solicitud solicitud : solicitudes) {
                        estadoSolicitud = new EstadoSolicitud();
                        estadoSolicitud.setFecha_registro_estado(new Date());
                            if(solicitud.getEsSeleccionado() == true){
                                estadoSolicitud.setEstado_actual("Aprobado");
                                solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                                usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getId_solicitud());
                                bandera = this.objGestionarCursoOfertadoVeranoGateway.asociarUsuarioCurso(usuario.getId_usuario(), idCurso);
                                if(bandera == false){
                                    this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se pudo asociar el usuario al curso");
                                }
                            }else{
                                estadoSolicitud.setEstado_actual("Rechazado");
                                solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);

                            }
                    }
                    

        }

        if(estadoCurso.getEstado_actual().equals("Inscripcion")){
            if(cursoABuscar.getEstadosCursoOfertados().get(0).getEstado_actual().equals("Preinscripcion")){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede actualizar el curso, porque no esta publicado");
            }

            List<Solicitud> solicitudes = this.objGestionarSolicitudGateway.buscarSolicitudPorNombreCursoIns(idCurso);
            if(solicitudes.isEmpty()){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se puede publicar el curso, porque no hay solicitudes");
            }
                if(solicitudes.size()< cursoABuscar.getCupo_estimado()){
                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se puede abrir el curso, porque no se alcanzo el punto de equilibrio");
                }
                estudiantesInscritos = new ArrayList<Usuario>(cursoABuscar.getEstudiantesInscritos());
                for (Solicitud solicitud : solicitudes) {
                    estadoSolicitud = new EstadoSolicitud();
                    estadoSolicitud.setFecha_registro_estado(new Date());
                        if(solicitud.getEsSeleccionado() == true){
                            estadoSolicitud.setEstado_actual("Aprobado");
                            solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                            usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getId_solicitud());
                                if(solicitudes.size() < cursoABuscar.getEstudiantesInscritos().size()){
                                    for (Usuario usuarioViejo : estudiantesInscritos) {
                                        if (usuarioViejo.getId_usuario().equals(usuario.getId_usuario())) {
                                            usuariosRemovidos.add(usuarioViejo);
                                        }
                                    }
                                }
                        }else{
                            estadoSolicitud.setEstado_actual("Rechazado");
                            solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud); 
                        }
                }
                if(estudiantesInscritos != null){
                    estudiantesInscritos.removeAll(usuariosRemovidos);
                    for (Usuario usuarioRemover : estudiantesInscritos) {
                        // bandera = this.objGestionarCursoOfertadoVeranoGateway.desasociarUsuarioCurso(usuarioRemover.getId_usuario(), idCurso);
                        // if(bandera == false){
                        //    this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se pudo desasociar el usuario del curso");
                        // }
                        cursoABuscar.getEstudiantesInscritos().remove(usuarioRemover);
                    }                
                }
        }

        nuevoEstado = new EstadoCursoOfertado();
        nuevoEstado.setFecha_registro_estado(new Date());
        nuevoEstado.setEstado_actual(estadoCurso.getEstado_actual());
        //nuevoEstado.setObjCursoOfertadoVerano(cursoABuscar);

        cursoActualizado = this.objGestionarCursoOfertadoVeranoGateway.actualizarCurso(cursoABuscar, nuevoEstado);

        if(cursoActualizado == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se pudo actualizar el curso");
        }

        return cursoActualizado;
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
            this.objFormateadorResultados.retornarRespuestaErrorEntidadNoExiste("No se encuentra el curso");
        }
        return cursosABuscar;
    }

    @Override
    public List<CursoOfertadoVerano> listarTodos() {
        return this.objGestionarCursoOfertadoVeranoGateway.listarTodos();
    }

    @Override
    public List<CursoOfertadoVerano> publicarCursosMasivamente(List<Integer> idsCursos) {
        List<CursoOfertadoVerano> cursosPublicados = new ArrayList<>();
        
        if (idsCursos == null || idsCursos.isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La lista de cursos no puede estar vacía");
        }
        
        for (Integer idCurso : idsCursos) {
            if (idCurso == null) {
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del curso no puede ser nulo");
            }
            
            // Obtener el curso
            CursoOfertadoVerano curso = this.objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
            if (curso == null) {
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el curso con ID: " + idCurso);
            }
            
            // Verificar que el curso esté en estado "Abierto" para poder publicarlo
            if (curso.getEstadosCursoOfertados().isEmpty() || 
                !curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual().equals("Abierto")) {
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso con ID " + idCurso + " no está en estado 'Abierto' para ser publicado");
            }
            
            // Crear nuevo estado "Publicado"
            EstadoCursoOfertado estadoPublicado = new EstadoCursoOfertado();
            estadoPublicado.setFecha_registro_estado(new Date());
            estadoPublicado.setEstado_actual("Publicado");
            
            // Actualizar el curso con el nuevo estado
            CursoOfertadoVerano cursoActualizado = this.objGestionarCursoOfertadoVeranoGateway.actualizarCurso(curso, estadoPublicado);
            if (cursoActualizado != null) {
                cursosPublicados.add(cursoActualizado);
            }
        }
        
        return cursosPublicados;
    }

    
}
