package co.edu.unicauca.decanatura.gestion_curricular.soporte;

import java.util.ArrayList;
import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.enums.GrupoCursoVeranoEntity;

/**
 * Grafos de solicitud para las caracterizaciones de los mapeos de persistencia (DUP-10).
 *
 * {@code completar} llena una solicitud ya creada (de la subclase que la prueba necesite) con dos
 * estados (uno apunta a la solicitud y otro no, con campos nulos), dos documentos (igual), un
 * usuario con rol, programa y la solicitud entre las suyas, y un curso de verano con materia,
 * docente, estados, el mismo usuario inscrito y la solicitud en su lista.
 */
public final class GrafosSolicitud {

    public static final Date FECHA_1 = new Date(1_700_000_000_000L);
    public static final Date FECHA_2 = new Date(1_710_000_000_000L);
    public static final Date FECHA_3 = new Date(1_720_000_000_000L);

    private GrafosSolicitud() {
    }

    public static <T extends SolicitudEntity> T completar(T solicitud) {
        solicitud.setId_solicitud(7);
        solicitud.setNombre_solicitud("Solicitud de prueba");
        solicitud.setPeriodo_academico("2025-1");
        solicitud.setFecha_ceremonia(FECHA_1);
        solicitud.setFecha_registro_solicitud(FECHA_2);

        EstadoSolicitudEntity enviada = new EstadoSolicitudEntity();
        enviada.setId_estado(1);
        enviada.setEstado_actual("Enviada");
        enviada.setFecha_registro_estado(FECHA_2);
        enviada.setObjSolicitud(solicitud);
        EstadoSolicitudEntity sinEstado = new EstadoSolicitudEntity();
        sinEstado.setId_estado(2);
        sinEstado.setEstado_actual(null);
        sinEstado.setComentario("revisar");
        solicitud.setEstadosSolicitud(new ArrayList<>());
        solicitud.getEstadosSolicitud().add(enviada);
        solicitud.getEstadosSolicitud().add(sinEstado);

        UsuarioEntity usuario = usuarioEntity();
        usuario.getSolicitudes().add(solicitud);
        solicitud.setObjUsuario(usuario);

        DocumentoEntity documento = new DocumentoEntity();
        documento.setId_documento(3);
        documento.setNombre("carta.pdf");
        documento.setRuta_documento("reingreso/carta.pdf");
        documento.setFecha_documento(FECHA_3);
        documento.setEsValido(true);
        documento.setComentario("legible");
        documento.setIdUsuarioSubio(1);
        documento.setObjSolicitud(solicitud);
        DocumentoEntity suelto = new DocumentoEntity();
        suelto.setId_documento(4);
        solicitud.setDocumentos(new ArrayList<>());
        solicitud.getDocumentos().add(documento);
        solicitud.getDocumentos().add(suelto);

        CursoOfertadoVeranoEntity curso = cursoEntity(usuario);
        curso.getSolicitudes().add(solicitud);
        solicitud.setObjCursoOfertadoVerano(curso);
        return solicitud;
    }

    public static UsuarioEntity usuarioEntity() {
        RolEntity rol = new RolEntity();
        rol.setId_rol(1);
        rol.setNombre("ESTUDIANTE");
        ProgramaEntity programa = new ProgramaEntity();
        programa.setId_programa(1);
        programa.setCodigo("SIS");
        programa.setNombre_programa("Ingeniería de Sistemas");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId_usuario(1);
        usuario.setNombre_completo("Ana Pérez");
        usuario.setCodigo("104");
        usuario.setCedula("123");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setPassword("$2a$hash");
        usuario.setEstado_usuario(true);
        usuario.setObjRol(rol);
        usuario.setObjPrograma(programa);
        rol.getUsuarios().add(usuario);
        programa.getUsuarios().add(usuario);
        return usuario;
    }

    public static CursoOfertadoVeranoEntity cursoEntity(UsuarioEntity inscrito) {
        MateriaEntity materia = new MateriaEntity();
        materia.setId_materia(5);
        materia.setCodigo("MAT1");
        materia.setNombre("Cálculo");
        materia.setCreditos(4);
        DocenteEntity docente = new DocenteEntity();
        docente.setId_docente(6);
        docente.setCodigo_docente("D06");
        docente.setNombre_docente("Luis");
        CursoOfertadoVeranoEntity curso = new CursoOfertadoVeranoEntity();
        curso.setId_curso(4);
        curso.setObjMateria(materia);
        curso.setObjDocente(docente);
        curso.setGrupo(GrupoCursoVeranoEntity.B);
        curso.setCupo_estimado(20);
        curso.setSalon("221");
        curso.setFecha_inicio(FECHA_1);
        curso.setFecha_fin(FECHA_3);
        curso.setPeriodo_academico("2025-1");
        EstadoCursoOfertadoEntity abierto = new EstadoCursoOfertadoEntity();
        abierto.setId_estado(8);
        abierto.setEstado_actual("Abierto");
        abierto.setFecha_registro_estado(FECHA_1);
        abierto.setObjCursoOfertadoVerano(curso);
        EstadoCursoOfertadoEntity sinEstado = new EstadoCursoOfertadoEntity();
        sinEstado.setId_estado(9);
        sinEstado.setEstado_actual(null);
        curso.getEstadosCursoOfertados().add(abierto);
        curso.getEstadosCursoOfertados().add(sinEstado);
        if (inscrito != null) {
            curso.getEstudiantesInscritos().add(inscrito);
            inscrito.getCursosOfertadosInscritos().add(curso);
        }
        return curso;
    }

    public static <T extends Solicitud> T completar(T solicitud) {
        solicitud.setIdSolicitud(7);
        solicitud.setNombreSolicitud("Solicitud de prueba");
        solicitud.setPeriodoAcademico("2025-1");
        solicitud.setFechaCeremonia(FECHA_1);
        solicitud.setFechaRegistroSolicitud(FECHA_2);

        EstadoSolicitud enviada = new EstadoSolicitud(1, "Enviada", FECHA_2, null, solicitud);
        EstadoSolicitud sinEstado = new EstadoSolicitud(2, null, null, "revisar", null);
        solicitud.setEstadosSolicitud(new ArrayList<>());
        solicitud.getEstadosSolicitud().add(enviada);
        solicitud.getEstadosSolicitud().add(sinEstado);

        Usuario usuario = usuario();
        usuario.getSolicitudes().add(solicitud);
        solicitud.setObjUsuario(usuario);

        Documento documento = new Documento(3, "carta.pdf", "reingreso/carta.pdf", FECHA_3, true, "legible", 1,
                solicitud);
        Documento suelto = new Documento();
        suelto.setIdDocumento(4);
        solicitud.setDocumentos(new ArrayList<>());
        solicitud.getDocumentos().add(documento);
        solicitud.getDocumentos().add(suelto);

        CursoOfertadoVerano curso = curso(usuario);
        curso.getSolicitudes().add(solicitud);
        solicitud.setObjCursoOfertadoVerano(curso);
        return solicitud;
    }

    public static Usuario usuario() {
        Rol rol = new Rol(1, "ESTUDIANTE", new ArrayList<>());
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombreCompleto("Ana Pérez");
        usuario.setCodigo("104");
        usuario.setCedula("123");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setPassword("$2a$hash");
        usuario.setEstadoUsuario(true);
        usuario.setObjRol(rol);
        usuario.setObjPrograma(new Programa(1, "SIS", "Ingeniería de Sistemas"));
        rol.getUsuarios().add(usuario);
        return usuario;
    }

    public static CursoOfertadoVerano curso(Usuario inscrito) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(4);
        curso.setObjMateria(new Materia(5, "MAT1", "Cálculo", 4));
        curso.setObjDocente(new Docente(6, "D06", "Luis"));
        curso.setGrupo(GrupoCursoVerano.B);
        curso.setCupoEstimado(20);
        curso.setSalon("221");
        curso.setFecha_inicio(FECHA_1);
        curso.setFecha_fin(FECHA_3);
        curso.setPeriodoAcademico("2025-1");
        EstadoCursoOfertado abierto = new EstadoCursoOfertado(8, "Abierto", FECHA_1, curso);
        EstadoCursoOfertado sinEstado = new EstadoCursoOfertado(9, null, null, null);
        curso.getEstadosCursoOfertados().add(abierto);
        curso.getEstadosCursoOfertados().add(sinEstado);
        if (inscrito != null) {
            curso.getEstudiantesInscritos().add(inscrito);
            inscrito.getCursosOfertadosInscritos().add(curso);
        }
        return curso;
    }
}
