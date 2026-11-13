package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException.Motivo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;

public class GestionarCursoOfertadoVeranoCUAdapter implements GestionarCursoOfertadoVeranoCUIntPort {

    private final GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway;
    private final GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarMateriasIntPort objGestionarMateriasGateway;
    private final GestionarDocenteGatewayIntPort objGestionarDocenteGateway;
    private final GestionarSalonesIntPort salonesGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;

    public GestionarCursoOfertadoVeranoCUAdapter(GestionarCursoOfertadoVeranoGatewayIntPort objGestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway, 
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarMateriasIntPort objGestionarMateriasGateway,
            GestionarDocenteGatewayIntPort objGestionarDocenteGateway,
            GestionarSalonesIntPort salonesGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU) {
        this.objGestionarCursoOfertadoVeranoGateway = objGestionarCursoOfertadoVeranoGateway;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarMateriasGateway = objGestionarMateriasGateway;
        this.objGestionarDocenteGateway = objGestionarDocenteGateway;
        this.salonesGateway = salonesGateway;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
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
                        String estadoAnterior = solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()
                                ? solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual()
                                : "Pendiente";
                        
                        estadoSolicitud = new EstadoSolicitud();
                        estadoSolicitud.setFecha_registro_estado(new Date());
                        // Nota: La lógica de aprobación/rechazo basada en esSeleccionado fue eliminada
                        // Todas las solicitudes se aprueban por defecto
                        estadoSolicitud.setEstado_actual(EstadoSolicitudCursoVerano.APROBADO.texto());
                        solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                        usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getId_solicitud());
                        bandera = this.objGestionarCursoOfertadoVeranoGateway.asociarUsuarioCurso(usuario.getId_usuario(), idCurso);
                        if(bandera == false){
                            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se pudo asociar el usuario al curso");
                        }
                        
                        // Notificar cambio de estado - Preinscripción Aprobada
                        try {
                            this.objGestionarNotificacionCU.notificarCambioEstadoSolicitud(
                                solicitud, estadoAnterior, EstadoSolicitudCursoVerano.APROBADO.texto(), "CURSO_VERANO_PREINSCRIPCION"
                            );
                        } catch (Exception e) {
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
                    String estadoAnterior = solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()
                            ? solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual()
                            : "Pendiente";
                    
                    estadoSolicitud = new EstadoSolicitud();
                    estadoSolicitud.setFecha_registro_estado(new Date());
                    // Nota: La lógica de aprobación/rechazo basada en esSeleccionado fue eliminada
                    // Todas las solicitudes se aprueban por defecto
                    estadoSolicitud.setEstado_actual(EstadoSolicitudCursoVerano.APROBADO.texto());
                    solicitud = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                    usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getId_solicitud());
                    if(solicitudes.size() < cursoABuscar.getEstudiantesInscritos().size()){
                        for (Usuario usuarioViejo : estudiantesInscritos) {
                            if (usuarioViejo.getId_usuario().equals(usuario.getId_usuario())) {
                                usuariosRemovidos.add(usuarioViejo);
                            }
                        }
                    }
                    
                    // Notificar cambio de estado - Inscripción Aprobada
                    try {
                        this.objGestionarNotificacionCU.notificarCambioEstadoSolicitud(
                            solicitud, estadoAnterior, EstadoSolicitudCursoVerano.APROBADO.texto(), "CURSO_VERANO_INSCRIPCION"
                        );
                    } catch (Exception e) {
                    }
                }
                if(estudiantesInscritos != null){
                    estudiantesInscritos.removeAll(usuariosRemovidos);
                    for (Usuario usuarioRemover : estudiantesInscritos) {
                        cursoABuscar.getEstudiantesInscritos().remove(usuarioRemover);
                    }                
                }
        }

        nuevoEstado = new EstadoCursoOfertado();
        nuevoEstado.setFecha_registro_estado(new Date());
        nuevoEstado.setEstado_actual(estadoCurso.getEstado_actual());

        cursoActualizado = this.objGestionarCursoOfertadoVeranoGateway.actualizarCurso(cursoABuscar, nuevoEstado);

        if(cursoActualizado == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se pudo actualizar el curso");
        }

        return cursoActualizado;
    }


    @Override
    public CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso) {
        Materia materia = null;
        Docente docente = null;
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
        return guardarCurso(curso);
    }

    private CursoOfertadoVerano guardarCurso(CursoOfertadoVerano curso) {
        for (CursoOfertadoVerano existente : listarTodos()) {
            if (curso.getObjMateria().getId_materia() == existente.getObjMateria().getId_materia()
                    && curso.getGrupo() == existente.getGrupo()) {
                objFormateadorResultados.retornarRespuestaErrorEntidadExiste(
                        "El grupo asignado ya esta en uso" + curso.getGrupo().name());
                return null;
            }
        }
        return objGestionarCursoOfertadoVeranoGateway.crearCurso(curso);
    }

    @Override
    public CursoOfertadoVerano crearCurso(DatosCreacion datos) {
        if (!datos.fechaFin().after(datos.fechaInicio())) {
            throw new DatosCursoInvalidosException(Motivo.FECHAS_INVALIDAS,
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        String salon = resolverSalon(datos.idSalon(), datos.espacioAsignado());
        if (salon == null && datos.idSalon() == null) {
            throw new DatosCursoInvalidosException(Motivo.SALON_REQUERIDO,
                    "Debe seleccionar un salón de la lista disponible");
        }
        GrupoCursoVerano grupo = grupoInicial(datos.grupo());
        Materia materia = objGestionarMateriasGateway.obtenerMateriaPorId(datos.idMateria());
        if (materia == null) {
            objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Materia no encontrada.");
        }
        Docente docente = objGestionarDocenteGateway.obtenerDocentePorId(datos.idDocente());
        if (docente == null) {
            objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Docente no encontrado.");
        }

        if (existeCurso(datos.idMateria(), datos.idDocente(), datos.periodo(), grupo)) {
            throw new DatosCursoInvalidosException(Motivo.CURSO_DUPLICADO,
                    "Ya existe un curso con la misma materia, docente, período académico y grupo '" + grupo
                            + "'. Puedes crear grupos diferentes (A, B, C, D) para la misma materia y docente.");
        }

        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setCupo_estimado(datos.cupoEstimado());
        curso.setSalon(salon);
        curso.setGrupo(grupo);
        curso.setFecha_inicio(datos.fechaInicio());
        curso.setFecha_fin(datos.fechaFin());
        curso.setPeriodo_academico(datos.periodo());
        curso.setObjMateria(materia);
        curso.setObjDocente(docente);

        String estado = datos.estado() == null ? "" : datos.estado().trim();
        EstadoCursoOfertado estadoInicial = new EstadoCursoOfertado();
        estadoInicial.setEstado_actual(TransicionEstadoCurso.esEstadoValido(estado) ? estado : "Abierto");
        estadoInicial.setFecha_registro_estado(datos.fechaInicio());
        curso.getEstadosCursoOfertados().add(estadoInicial);
        return guardarCurso(curso);
    }

    @Override
    public String resolverSalon(Integer idSalon, String espacioAsignado) {
        if (idSalon == null) {
            return espacioAsignado == null || espacioAsignado.trim().isEmpty() ? null : espacioAsignado.trim();
        }
        Salon salon = salonesGateway.obtenerSalonPorId(idSalon);
        if (salon == null) {
            objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Salón no encontrado.");
            throw new DatosCursoInvalidosException(Motivo.SALON_NO_ENCONTRADO,
                    "El salón con ID " + idSalon + " no existe");
        }
        return salon.getNumero_salon();
    }

    private GrupoCursoVerano grupoInicial(String grupo) {
        if (grupo == null || grupo.trim().isEmpty()) {
            return GrupoCursoVerano.A;
        }
        try {
            return GrupoCursoVerano.valueOf(grupo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GrupoCursoVerano.A;
        }
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
    public boolean asociarEstudiante(Integer idCurso, Integer idUsuario) {
        return Boolean.TRUE.equals(objGestionarCursoOfertadoVeranoGateway.asociarUsuarioCurso(idUsuario, idCurso));
    }

    @Override
    public CursoOfertadoVerano modificarCurso(Integer idCurso, Integer cupoEstimado, String salon, String nuevoEstado) {
        CursoOfertadoVerano curso = objGestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if (curso == null) {
            return null;
        }
        if (nuevoEstado != null) {
            // La completitud se evalúa con los datos nuevos (cupo y salón ya aplicados)
            if (cupoEstimado != null) {
                curso.setCupo_estimado(cupoEstimado);
            }
            if (salon != null) {
                curso.setSalon(salon);
            }
            TransicionEstadoCurso.validar(curso, nuevoEstado);
        }
        return objGestionarCursoOfertadoVeranoGateway.guardarCambiosCurso(idCurso, cupoEstimado, salon, nuevoEstado);
    }

    @Override
    public boolean existeCurso(Integer idMateria, Integer idDocente, String periodoAcademico, GrupoCursoVerano grupo) {
        return objGestionarCursoOfertadoVeranoGateway.existeCurso(idMateria, idDocente, periodoAcademico, grupo);
    }

    @Override
    public List<CursoOfertadoVerano> listarTodos() {
        return this.objGestionarCursoOfertadoVeranoGateway.listarTodos();
    }

    @Override
    public List<CursoOfertadoVerano> listarPorFiltros(String periodo, Integer idPrograma, boolean soloVisibles) {
        List<CursoOfertadoVerano> cursos = periodo == null ? listarTodos()
                : objGestionarCursoOfertadoVeranoGateway.listarPorPeriodo(periodo);
        return cursos.stream()
                .filter(curso -> periodo == null || periodo.equals(curso.getPeriodo_academico()))
                .filter(curso -> !soloVisibles || esVisibleParaEstudiantes(curso))
                .filter(curso -> idPrograma == null || disponibleParaPrograma(curso, idPrograma))
                .toList();
    }

    private boolean disponibleParaPrograma(CursoOfertadoVerano curso, Integer idPrograma) {
        List<Usuario> inscritos = curso.getEstudiantesInscritos();
        List<Solicitud> solicitudes = curso.getSolicitudes();
        boolean sinInscritos = inscritos == null || inscritos.isEmpty();
        boolean sinSolicitudes = solicitudes == null || solicitudes.isEmpty();

        return (sinInscritos && sinSolicitudes)
                || (!sinInscritos && inscritos.stream().anyMatch(usuario -> perteneceAlPrograma(usuario, idPrograma)))
                || (!sinSolicitudes && solicitudes.stream().map(Solicitud::getObjUsuario)
                        .anyMatch(usuario -> perteneceAlPrograma(usuario, idPrograma)));
    }

    private boolean perteneceAlPrograma(Usuario usuario, Integer idPrograma) {
        return usuario != null && usuario.getObjPrograma() != null
                && idPrograma.equals(usuario.getObjPrograma().getId_programa());
    }

    private boolean esVisibleParaEstudiantes(CursoOfertadoVerano curso) {
        List<EstadoCursoOfertado> estados = curso.getEstadosCursoOfertados();
        if (estados == null || estados.isEmpty()) {
            return false;
        }
        String estado = estados.get(estados.size() - 1).getEstado_actual();
        return "Publicado".equals(estado) || "Preinscripcion".equals(estado) || "Inscripcion".equals(estado);
    }

    
}
