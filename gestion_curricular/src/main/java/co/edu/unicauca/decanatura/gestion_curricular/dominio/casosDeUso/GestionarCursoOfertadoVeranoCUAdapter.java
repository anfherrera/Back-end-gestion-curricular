package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException.Motivo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;

public class GestionarCursoOfertadoVeranoCUAdapter implements GestionarCursoOfertadoVeranoCUIntPort {

    private final GestionarCursoOfertadoVeranoGatewayIntPort gestionarCursoOfertadoVeranoGateway;
    private final GestionarMateriasIntPort gestionarMateriasGateway;
    private final GestionarDocenteGatewayIntPort gestionarDocenteGateway;
    private final GestionarSalonesIntPort salonesGateway;

    public GestionarCursoOfertadoVeranoCUAdapter(GestionarCursoOfertadoVeranoGatewayIntPort gestionarCursoOfertadoVeranoGateway,
            GestionarMateriasIntPort gestionarMateriasGateway,
            GestionarDocenteGatewayIntPort gestionarDocenteGateway,
            GestionarSalonesIntPort salonesGateway) {
        this.gestionarCursoOfertadoVeranoGateway = gestionarCursoOfertadoVeranoGateway;
        this.gestionarMateriasGateway = gestionarMateriasGateway;
        this.gestionarDocenteGateway = gestionarDocenteGateway;
        this.salonesGateway = salonesGateway;
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
                .filter(curso -> !soloVisibles || curso.esVisibleParaEstudiantes())
                .filter(curso -> idPrograma == null || disponibleParaPrograma(curso, idPrograma))
                .toList();
    }

    @Override
    public List<String> listarPeriodosConCursosVisibles() {
        return listarPorFiltros(null, null, true).stream()
                .map(curso -> PeriodoAcademicoEnum.recortado(curso.getPeriodoAcademico()))
                .filter(Objects::nonNull)
                .distinct()
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    private static final Pattern ANIO_Y_NUMERO = Pattern.compile("(\\d{4})-(\\d+)");

    /** AAAA-N del más reciente al más antiguo; los demás, al final y por orden alfabético. */
    private static final Comparator<String> MAS_RECIENTE_PRIMERO = (a, b) -> {
        Matcher ma = ANIO_Y_NUMERO.matcher(a);
        Matcher mb = ANIO_Y_NUMERO.matcher(b);
        boolean va = ma.matches();
        boolean vb = mb.matches();
        if (va && vb) {
            int porAnio = Integer.compare(Integer.parseInt(mb.group(1)), Integer.parseInt(ma.group(1)));
            return porAnio != 0 ? porAnio : Integer.compare(Integer.parseInt(mb.group(2)), Integer.parseInt(ma.group(2)));
        }
        if (va != vb) {
            return va ? -1 : 1;
        }
        return a.compareTo(b);
    };

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

    
}
