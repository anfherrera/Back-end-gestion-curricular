package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;


import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarCursoOfertadoVeranoCUAdapter implements GestionarCursoOfertadoVeranoCUIntPort {

    private final GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway;
    private final GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarCursoOfertadoVeranoCUAdapter(GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway, 
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            FormateadorResultadosIntPort objFormateadorResultados) {
        this.objGestionarCursoOfertadoVeranoGateway = objGestionarCursoOfertadoVeranoGateway;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objFormateadorResultados = objFormateadorResultados;
    }

    @Override
    public CursoOfertadoVerano actualizarCurso(CursoOfertadoVerano curso, EstadoCursoOfertado estadoCurso, List<Solicitud> solicitudes) {
        CursoOfertadoVerano cursoABuscar = curso;
        Integer idCurso = null;
        Usuario usuario = null;
        EstadoSolicitud estadoSolicitud = null;
        if(curso == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el curso");
        }
        if(estadoCurso == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay datos en el estado");
        }
        if(estadoCurso.getEstado_actual().equals("Preinscripcion")){
            idCurso = curso.getId_curso();
            cursoABuscar = objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
            
            if(cursoABuscar == null){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encuentra el curso");
            }else{
                if(solicitudes.size() >= cursoABuscar.getCupo_estimado()){
                    for (Solicitud solicitud : solicitudes) {
                        estadoSolicitud = solicitud.getObjEstadoSolicitud();
                        estadoSolicitud.setEstado_actual("Aprobado");
                        solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                        usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getId_solicitud());
                        this.objGestionarCursoOfertadoVeranoGateway.asociarUsuarioCurso(usuario.getId_usuario(), idCurso);
                    }
                    cursoABuscar.setObjEstadoCursoOfertado(estadoCurso);
                }else{
                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se puede publicar el curso, porque no se alcanzo el cupo estimado");
                }
                
            }
            
        }

        if(estadoCurso.getEstado_actual().equals("Inscripcion")){
            idCurso = curso.getId_curso();
            cursoABuscar = objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
            
            if(cursoABuscar == null){
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encuentra el curso");
            }else{
                if(solicitudes.size() >= cursoABuscar.getCupo_estimado()){   
                        for (Solicitud solicitud : solicitudes) {
                            estadoSolicitud = solicitud.getObjEstadoSolicitud();
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
                    
                    cursoABuscar.setObjEstadoCursoOfertado(estadoCurso);
                }else{
                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se puede publicar el curso, porque no se alcanzo el cupo estimado");
                }
                
            }
            
        }

        return this.objGestionarCursoOfertadoVeranoGateway.actualizarCurso(cursoABuscar, estadoCurso);
    }

    @Override
    public CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso) {
        List<CursoOfertadoVerano> cursos = null;
        CursoOfertadoVerano cursoGuardado = null;
        Boolean bandera = false;
        if(curso != null){
            cursos = this.objGestionarCursoOfertadoVeranoGateway.listarTodos();
            for (CursoOfertadoVerano cursoOfertadoVerano : cursos) {
                if(curso.getObjMateria().getId_materia() == cursoOfertadoVerano.getObjMateria().getId_materia()){
                    if(curso.getGrupo().name() == cursoOfertadoVerano.getGrupo().name()){
                        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El grupo asignado ya esta en uso"+ curso.getGrupo().name());
                        bandera = true;
                        break;
                    }
                }
            }
            if(!bandera){
            cursoGuardado = this.objGestionarCursoOfertadoVeranoGateway.crearCurso(curso);
            }
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
