package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException.Motivo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadoSolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;

public class GestionarCursoOfertadoVeranoCUAdapter implements GestionarCursoOfertadoVeranoCUIntPort {

    private final GestionarCursoOfertadoVeranoGatewayIntPort gestionarCursoOfertadoVeranoGateway;
    private final GestionarSolicitudGatewayIntPort gestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort gestionarUsuarioGateway;
    private final GestionarMateriasIntPort gestionarMateriasGateway;
    private final GestionarDocenteGatewayIntPort gestionarDocenteGateway;
    private final GestionarSalonesIntPort salonesGateway;
    private final GestionarNotificacionCUIntPort gestionarNotificacionCU;

    public GestionarCursoOfertadoVeranoCUAdapter(GestionarCursoOfertadoVeranoGatewayIntPort gestionarCursoOfertadoVeranoGateway,
            GestionarSolicitudGatewayIntPort gestionarSolicitudGateway,
            GestionarUsuarioGatewayIntPort gestionarUsuarioGateway,
            GestionarMateriasIntPort gestionarMateriasGateway,
            GestionarDocenteGatewayIntPort gestionarDocenteGateway,
            GestionarSalonesIntPort salonesGateway,
            GestionarNotificacionCUIntPort gestionarNotificacionCU) {
        this.gestionarCursoOfertadoVeranoGateway = gestionarCursoOfertadoVeranoGateway;
        this.gestionarSolicitudGateway = gestionarSolicitudGateway;
        this.gestionarUsuarioGateway = gestionarUsuarioGateway;
        this.gestionarMateriasGateway = gestionarMateriasGateway;
        this.gestionarDocenteGateway = gestionarDocenteGateway;
        this.salonesGateway = salonesGateway;
        this.gestionarNotificacionCU = gestionarNotificacionCU;
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
            throw new ReglaNegocioExcepcion("No hay datos en el curso");
        }
        if(curso.getIdCurso() == null){
            throw new ReglaNegocioExcepcion("No hay id en el curso");
        }
        idCurso = curso.getIdCurso();
        cursoABuscar = gestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if(cursoABuscar == null){
            throw new EntidadNoExisteException("No se encuentra el curso");
        }
        if(cursoABuscar.getEstadosCursoOfertados().isEmpty()){
                throw new ReglaNegocioExcepcion("No se puede actualizar el curso, porque no tiene estados");
        }
        if(estadoCurso == null){
            throw new ReglaNegocioExcepcion("No hay datos en el estado");
        }
        if(estadoCurso.getIdEstado() == null){
            throw new ReglaNegocioExcepcion("No hay id en el estado");
        }
        if(estadoCurso.getEstadoActual() == null){
            throw new ReglaNegocioExcepcion("No hay datos en el estado actual");
        }

        if(estadoCurso.getEstadoActual().equals("Preinscripcion")){
            if(cursoABuscar.getEstadosCursoOfertados().get(0).getEstadoActual().equals("Publicado")){
                throw new ReglaNegocioExcepcion("No se puede actualizar el curso, porque no esta publicado");
            }

            List<Solicitud> solicitudes = this.gestionarSolicitudGateway.buscarSolicitudPorNombreCursoPre(idCurso);
            if(solicitudes.isEmpty()){
                throw new ReglaNegocioExcepcion("No se puede publicar el curso, porque no hay solicitudes");
            }
                if(solicitudes.size()< puntoEquilibrioMinimo){
                    throw new ReglaNegocioExcepcion("No se puede publicar el curso, porque no se alcanzo el cupo estimado");
                }
                    for (Solicitud solicitud : solicitudes) {
                        String estadoAnterior = solicitud.estadoActual("Pendiente");
                        
                        estadoSolicitud = new EstadoSolicitud();
                        estadoSolicitud.setFechaRegistroEstado(new Date());
                        // Nota: La lógica de aprobación/rechazo basada en esSeleccionado fue eliminada
                        // Todas las solicitudes se aprueban por defecto
                        estadoSolicitud.setEstadoActual(EstadoSolicitudCursoVerano.APROBADO.texto());
                        solicitud = this.gestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                        usuario = this.gestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getIdSolicitud());
                        bandera = this.gestionarCursoOfertadoVeranoGateway.asociarUsuarioCurso(usuario.getIdUsuario(), idCurso);
                        if(bandera == false){
                            throw new EntidadYaExisteException("No se pudo asociar el usuario al curso");
                        }
                        
                        // Notificar cambio de estado - Preinscripción Aprobada
                        try {
                            this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(
                                solicitud, estadoAnterior, EstadoSolicitudCursoVerano.APROBADO.texto(), "CURSO_VERANO_PREINSCRIPCION"
                            );
                        } catch (Exception e) {
                        }
                    }
                    

        }

        if(estadoCurso.getEstadoActual().equals("Inscripcion")){
            if(cursoABuscar.getEstadosCursoOfertados().get(0).getEstadoActual().equals("Preinscripcion")){
                throw new ReglaNegocioExcepcion("No se puede actualizar el curso, porque no esta publicado");
            }

            List<Solicitud> solicitudes = this.gestionarSolicitudGateway.buscarSolicitudPorNombreCursoIns(idCurso);
            if(solicitudes.isEmpty()){
                throw new ReglaNegocioExcepcion("No se puede publicar el curso, porque no hay solicitudes");
            }
                if(solicitudes.size()< cursoABuscar.getCupoEstimado()){
                    throw new ReglaNegocioExcepcion("No se puede abrir el curso, porque no se alcanzo el punto de equilibrio");
                }
                estudiantesInscritos = new ArrayList<Usuario>(cursoABuscar.getEstudiantesInscritos());
                for (Solicitud solicitud : solicitudes) {
                    String estadoAnterior = solicitud.estadoActual("Pendiente");
                    
                    estadoSolicitud = new EstadoSolicitud();
                    estadoSolicitud.setFechaRegistroEstado(new Date());
                    // Nota: La lógica de aprobación/rechazo basada en esSeleccionado fue eliminada
                    // Todas las solicitudes se aprueban por defecto
                    estadoSolicitud.setEstadoActual(EstadoSolicitudCursoVerano.APROBADO.texto());
                    solicitud = this.gestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                    usuario = this.gestionarUsuarioGateway.buscarUsuarioPorSolicitud(solicitud.getIdSolicitud());
                    if(solicitudes.size() < cursoABuscar.getEstudiantesInscritos().size()){
                        for (Usuario usuarioViejo : estudiantesInscritos) {
                            if (usuarioViejo.getIdUsuario().equals(usuario.getIdUsuario())) {
                                usuariosRemovidos.add(usuarioViejo);
                            }
                        }
                    }
                    
                    // Notificar cambio de estado - Inscripción Aprobada
                    try {
                        this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(
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
        nuevoEstado.setFechaRegistroEstado(new Date());
        nuevoEstado.setEstadoActual(estadoCurso.getEstadoActual());

        cursoActualizado = this.gestionarCursoOfertadoVeranoGateway.actualizarCurso(cursoABuscar, nuevoEstado);

        if(cursoActualizado == null){
            throw new EntidadYaExisteException("No se pudo actualizar el curso");
        }

        return cursoActualizado;
    }


    @Override
    public CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso) {
        Materia materia = null;
        Docente docente = null;
        if(curso == null){
            throw new ReglaNegocioExcepcion("No hay datos en el curso");
        }
        if(curso.getObjMateria() == null){
            throw new ReglaNegocioExcepcion("No hay datos en la materia");
        }
        if(curso.getObjMateria().getIdMateria() == null){
            throw new ReglaNegocioExcepcion("No hay datos en la   materia");
        }
        materia = this.gestionarMateriasGateway.obtenerMateriaPorId(curso.getObjMateria().getIdMateria());
        if(materia == null){
            throw new EntidadNoExisteException("No existe la materia con el ID: " + curso.getObjMateria().getIdMateria());
        }
        if(curso.getObjDocente() == null){
            throw new ReglaNegocioExcepcion("No hay datos en el docente");
        }
        if(curso.getObjDocente().getIdDocente() == null){
            throw new ReglaNegocioExcepcion("No hay datos en el docente");
        }
        docente = this.gestionarDocenteGateway.buscarDocentePorId(curso.getObjDocente().getIdDocente());
        if(docente == null){
            throw new EntidadNoExisteException("No existe el docente con el ID: " + curso.getObjDocente().getIdDocente());
        }            
        return guardarCurso(curso);
    }

    private CursoOfertadoVerano guardarCurso(CursoOfertadoVerano curso) {
        for (CursoOfertadoVerano existente : listarTodos()) {
            if (curso.getObjMateria().getIdMateria() == existente.getObjMateria().getIdMateria()
                    && curso.getGrupo() == existente.getGrupo()) {
                throw new EntidadYaExisteException(
                        "El grupo asignado ya está en uso: " + curso.getGrupo().name());
            }
        }
        return gestionarCursoOfertadoVeranoGateway.crearCurso(curso);
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
        Materia materia = gestionarMateriasGateway.obtenerMateriaPorId(datos.idMateria());
        if (materia == null) {
            throw new EntidadNoExisteException("Materia no encontrada.");
        }
        Docente docente = gestionarDocenteGateway.obtenerDocentePorId(datos.idDocente());
        if (docente == null) {
            throw new EntidadNoExisteException("Docente no encontrado.");
        }

        if (existeCurso(datos.idMateria(), datos.idDocente(), datos.periodo(), grupo)) {
            throw new DatosCursoInvalidosException(Motivo.CURSO_DUPLICADO,
                    "Ya existe un curso con la misma materia, docente, período académico y grupo '" + grupo
                            + "'. Puedes crear grupos diferentes (A, B, C, D) para la misma materia y docente.");
        }

        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setCupoEstimado(datos.cupoEstimado());
        curso.setSalon(salon);
        curso.setGrupo(grupo);
        curso.setFecha_inicio(datos.fechaInicio());
        curso.setFecha_fin(datos.fechaFin());
        curso.setPeriodoAcademico(datos.periodo());
        curso.setObjMateria(materia);
        curso.setObjDocente(docente);

        String estado = datos.estado() == null ? "" : datos.estado().trim();
        EstadoCursoOfertado estadoInicial = new EstadoCursoOfertado();
        estadoInicial.setEstadoActual(TransicionEstadoCurso.esEstadoValido(estado) ? estado : "Abierto");
        estadoInicial.setFechaRegistroEstado(datos.fechaInicio());
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
            throw new DatosCursoInvalidosException(Motivo.SALON_NO_ENCONTRADO,
                    "El salón con ID " + idSalon + " no existe");
        }
        return salon.getNumeroSalon();
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
        cursosABuscar = this.gestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if(cursosABuscar == null){
            throw new EntidadNoExisteException("No se encuentra el curso");
        }
        return this.gestionarCursoOfertadoVeranoGateway.eliminarCurso(idCurso);
    }

    @Override
    public CursoOfertadoVerano obtenerCursoPorId(Integer idCurso) {
        CursoOfertadoVerano cursosABuscar=null;
        cursosABuscar = this.gestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if(cursosABuscar == null){
            throw new EntidadNoExisteException("No se encuentra el curso");
        }
        return cursosABuscar;
    }

    @Override
    public boolean asociarEstudiante(Integer idCurso, Integer idUsuario) {
        return Boolean.TRUE.equals(gestionarCursoOfertadoVeranoGateway.asociarUsuarioCurso(idUsuario, idCurso));
    }

    @Override
    public CursoOfertadoVerano modificarCurso(Integer idCurso, Integer cupoEstimado, String salon, String nuevoEstado) {
        CursoOfertadoVerano curso = gestionarCursoOfertadoVeranoGateway.obtenerCursoPorId(idCurso);
        if (curso == null) {
            return null;
        }
        if (nuevoEstado != null) {
            // La completitud se evalúa con los datos nuevos (cupo y salón ya aplicados)
            if (cupoEstimado != null) {
                curso.setCupoEstimado(cupoEstimado);
            }
            if (salon != null) {
                curso.setSalon(salon);
            }
            TransicionEstadoCurso.validar(curso, nuevoEstado);
        }
        return gestionarCursoOfertadoVeranoGateway.guardarCambiosCurso(idCurso, cupoEstimado, salon, nuevoEstado);
    }

    @Override
    public boolean existeCurso(Integer idMateria, Integer idDocente, String periodoAcademico, GrupoCursoVerano grupo) {
        return gestionarCursoOfertadoVeranoGateway.existeCurso(idMateria, idDocente, periodoAcademico, grupo);
    }

    @Override
    public List<CursoOfertadoVerano> listarTodos() {
        return this.gestionarCursoOfertadoVeranoGateway.listarTodos();
    }

    @Override
    public List<CursoOfertadoVerano> listarPorFiltros(String periodo, Integer idPrograma, boolean soloVisibles) {
        List<CursoOfertadoVerano> cursos = periodo == null ? listarTodos()
                : gestionarCursoOfertadoVeranoGateway.listarPorPeriodo(periodo);
        return cursos.stream()
                .filter(curso -> periodo == null || periodo.equals(curso.getPeriodoAcademico()))
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
                && idPrograma.equals(usuario.getObjPrograma().getIdPrograma());
    }

    private boolean esVisibleParaEstudiantes(CursoOfertadoVerano curso) {
        List<EstadoCursoOfertado> estados = curso.getEstadosCursoOfertados();
        if (estados == null || estados.isEmpty()) {
            return false;
        }
        String estado = estados.get(estados.size() - 1).getEstadoActual();
        return "Publicado".equals(estado) || "Preinscripcion".equals(estado) || "Inscripcion".equals(estado);
    }

    
}
