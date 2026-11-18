package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocumentoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.NotificacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.enums.CondicionSolicitudVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.enums.GrupoCursoVeranoEntity;

/**
 * Mapeo explícito del grafo de una solicitud entre el dominio y sus entidades (DUP-10).
 *
 * Sustituye al {@code ModelMapper} principal con estas reglas:
 * <ul>
 * <li>La subclase del resultado la elige quien llama, solo para el objeto raíz; las solicitudes
 * anidadas (la de un estado, un documento, una notificación o la lista de un curso) usan la clase
 * base, como el tipo declarado de esos campos.</li>
 * <li>Dentro de una llamada, cada objeto de origen da un único objeto de destino, así que las
 * referencias de vuelta (el estado y el documento a su solicitud, el estado del curso a su curso)
 * apuntan al objeto ya mapeado. {@code ModelMapper} podía crear varias copias del mismo curso;
 * ninguna parte del código dependía de ello.</li>
 * <li>Un valor nulo en el origen deja el valor del constructor del destino ({@code skipNull}):
 * listas vacías, "Enviado" en los estados de solicitud, "Abierto" en los de curso y
 * {@code false} en las marcas de una notificación.</li>
 * <li>El usuario se mapea con {@link UsuarioMapperPersistencia}: sin solicitudes ni cursos y con
 * rol y programa como copias sin relaciones.</li>
 * </ul>
 * Los métodos {@code ...Ecaes} reproducen el {@code ModelMapper} sin configurar que usaba el
 * gateway de ECAES: un nulo del origen sustituye al valor del constructor, y el usuario lleva
 * también sus solicitudes y sus cursos.
 */
public final class SolicitudMapperPersistencia {

    private static final Map<Class<?>, Supplier<? extends Solicitud>> DOMINIO = Map.of(
            Solicitud.class, Solicitud::new,
            SolicitudReingreso.class, SolicitudReingreso::new,
            SolicitudHomologacion.class, SolicitudHomologacion::new,
            SolicitudPazYSalvo.class, SolicitudPazYSalvo::new,
            SolicitudEcaes.class, SolicitudEcaes::new,
            SolicitudCursoVeranoPreinscripcion.class, SolicitudCursoVeranoPreinscripcion::new,
            SolicitudCursoVeranoInscripcion.class, SolicitudCursoVeranoInscripcion::new);

    private static final Map<Class<?>, Supplier<? extends SolicitudEntity>> ENTIDAD = Map.of(
            SolicitudEntity.class, SolicitudEntity::new,
            SolicitudReingresoEntity.class, SolicitudReingresoEntity::new,
            SolicitudHomologacionEntity.class, SolicitudHomologacionEntity::new,
            SolicitudPazYSalvoEntity.class, SolicitudPazYSalvoEntity::new,
            SolicitudEcaesEntity.class, SolicitudEcaesEntity::new,
            SolicitudCursoVeranoPreinscripcionEntity.class, SolicitudCursoVeranoPreinscripcionEntity::new,
            SolicitudCursoVeranoInscripcionEntity.class, SolicitudCursoVeranoInscripcionEntity::new);

    private SolicitudMapperPersistencia() {
    }

    public static <D extends Solicitud> D aDominio(SolicitudEntity entidad, Class<D> tipo) {
        Origen.exigir(entidad);
        return new AlDominio(false).solicitud(entidad, tipo);
    }

    public static <E extends SolicitudEntity> E aEntidad(Solicitud solicitud, Class<E> tipo) {
        Origen.exigir(solicitud);
        return new AlaEntidad(false).solicitud(solicitud, tipo);
    }

    public static EstadoSolicitud aDominio(EstadoSolicitudEntity entidad) {
        Origen.exigir(entidad);
        return new AlDominio(false).estado(entidad);
    }

    public static EstadoSolicitudEntity aEntidad(EstadoSolicitud estado) {
        Origen.exigir(estado);
        return new AlaEntidad(false).estado(estado);
    }

    public static Documento aDominio(DocumentoEntity entidad) {
        Origen.exigir(entidad);
        return new AlDominio(false).documento(entidad);
    }

    public static DocumentoEntity aEntidad(Documento documento) {
        Origen.exigir(documento);
        return new AlaEntidad(false).documento(documento);
    }

    public static CursoOfertadoVerano aDominio(CursoOfertadoVeranoEntity entidad) {
        Origen.exigir(entidad);
        return new AlDominio(false).curso(entidad);
    }

    public static GrupoCursoVeranoEntity aEntidad(GrupoCursoVerano grupo) {
        Origen.exigir(grupo);
        return GrupoCursoVeranoEntity.valueOf(grupo.name());
    }

    /** Variante de ECAES: copia los nulos y el usuario lleva sus solicitudes y cursos. */
    public static SolicitudEcaes aDominioEcaes(SolicitudEcaesEntity entidad) {
        Origen.exigir(entidad);
        return new AlDominio(true).solicitud(entidad, SolicitudEcaes.class);
    }

    /** Variante de ECAES: copia los nulos y el usuario lleva sus solicitudes y cursos. */
    public static SolicitudEcaesEntity aEntidadEcaes(SolicitudEcaes solicitud) {
        Origen.exigir(solicitud);
        return new AlaEntidad(true).solicitud(solicitud, SolicitudEcaesEntity.class);
    }

    /** Variante de ECAES: copia los nulos ({@code estado_actual} nulo no pasa a "Enviado"). */
    public static EstadoSolicitudEntity aEntidadEcaes(EstadoSolicitud estado) {
        Origen.exigir(estado);
        return new AlaEntidad(true).estado(estado);
    }

    public static Notificacion aDominio(NotificacionEntity entidad) {
        Origen.exigir(entidad);
        return new AlDominio(false).notificacion(entidad);
    }

    /** Un recorrido de entidad a dominio; {@code mapeados} da un destino por origen. */
    private static final class AlDominio {

        private final Map<Object, Object> mapeados = new IdentityHashMap<>();
        private final boolean copiaNulos;

        AlDominio(boolean copiaNulos) {
            this.copiaNulos = copiaNulos;
        }

        <D extends Solicitud> D solicitud(SolicitudEntity entidad, Class<D> tipo) {
            Object previo = mapeados.get(entidad);
            if (tipo.isInstance(previo)) {
                return tipo.cast(previo);
            }
            D solicitud = tipo.cast(crear(DOMINIO, tipo).get());
            mapeados.put(entidad, solicitud);
            solicitud.setId_solicitud(entidad.getId_solicitud());
            solicitud.setNombre_solicitud(entidad.getNombre_solicitud());
            solicitud.setPeriodo_academico(entidad.getPeriodo_academico());
            solicitud.setFecha_ceremonia(entidad.getFecha_ceremonia());
            solicitud.setFecha_registro_solicitud(entidad.getFecha_registro_solicitud());
            camposPropios(entidad, solicitud);
            if (copiaNulos || entidad.getEstadosSolicitud() != null) {
                solicitud.setEstadosSolicitud(entidad.getEstadosSolicitud() == null ? null : lista(entidad.getEstadosSolicitud(), this::estado));
            }
            if (copiaNulos || entidad.getObjUsuario() != null) {
                solicitud.setObjUsuario(entidad.getObjUsuario() == null ? null : usuario(entidad.getObjUsuario()));
            }
            if (copiaNulos || entidad.getDocumentos() != null) {
                solicitud.setDocumentos(entidad.getDocumentos() == null ? null : lista(entidad.getDocumentos(), this::documento));
            }
            if (copiaNulos || entidad.getObjCursoOfertadoVerano() != null) {
                solicitud.setObjCursoOfertadoVerano(entidad.getObjCursoOfertadoVerano() == null ? null : curso(entidad.getObjCursoOfertadoVerano()));
            }
            return solicitud;
        }

        EstadoSolicitud estado(EstadoSolicitudEntity entidad) {
            Object previo = mapeados.get(entidad);
            if (previo instanceof EstadoSolicitud estado) {
                return estado;
            }
            EstadoSolicitud estado = new EstadoSolicitud();
            mapeados.put(entidad, estado);
            estado.setId_estado(entidad.getId_estado());
            if (copiaNulos || entidad.getEstado_actual() != null) {
                estado.setEstado_actual(entidad.getEstado_actual() == null ? null : entidad.getEstado_actual());
            }
            estado.setFecha_registro_estado(entidad.getFecha_registro_estado());
            estado.setComentario(entidad.getComentario());
            if (copiaNulos || entidad.getObjSolicitud() != null) {
                estado.setObjSolicitud(entidad.getObjSolicitud() == null ? null : solicitud(entidad.getObjSolicitud(), Solicitud.class));
            }
            return estado;
        }

        Documento documento(DocumentoEntity entidad) {
            Object previo = mapeados.get(entidad);
            if (previo instanceof Documento documento) {
                return documento;
            }
            Documento documento = new Documento();
            mapeados.put(entidad, documento);
            documento.setId_documento(entidad.getId_documento());
            documento.setNombre(entidad.getNombre());
            documento.setRuta_documento(entidad.getRuta_documento());
            documento.setFecha_documento(entidad.getFecha_documento());
            documento.setEsValido(entidad.isEsValido());
            documento.setComentario(entidad.getComentario());
            documento.setIdUsuarioSubio(entidad.getIdUsuarioSubio());
            if (copiaNulos || entidad.getObjSolicitud() != null) {
                documento.setObjSolicitud(entidad.getObjSolicitud() == null ? null : solicitud(entidad.getObjSolicitud(), Solicitud.class));
            }
            return documento;
        }

        Usuario usuario(UsuarioEntity entidad) {
            Object previo = mapeados.get(entidad);
            if (previo instanceof Usuario usuario) {
                return usuario;
            }
            Usuario usuario = UsuarioMapperPersistencia.aDominio(entidad);
            mapeados.put(entidad, usuario);
            if (copiaNulos) {
                usuario.setSolicitudes(entidad.getSolicitudes() == null ? null
                        : lista(entidad.getSolicitudes(), s -> solicitud(s, Solicitud.class)));
                usuario.setCursosOfertadosInscritos(entidad.getCursosOfertadosInscritos() == null ? null
                        : lista(entidad.getCursosOfertadosInscritos(), this::curso));
            }
            return usuario;
        }

        CursoOfertadoVerano curso(CursoOfertadoVeranoEntity entidad) {
            Object previo = mapeados.get(entidad);
            if (previo instanceof CursoOfertadoVerano curso) {
                return curso;
            }
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            mapeados.put(entidad, curso);
            curso.setId_curso(entidad.getId_curso());
            if (copiaNulos || entidad.getObjMateria() != null) {
                curso.setObjMateria(entidad.getObjMateria() == null ? null : CatalogosMapperPersistencia.aDominio(entidad.getObjMateria()));
            }
            if (copiaNulos || entidad.getObjDocente() != null) {
                curso.setObjDocente(entidad.getObjDocente() == null ? null : CatalogosMapperPersistencia.aDominio(entidad.getObjDocente()));
            }
            if (copiaNulos || entidad.getGrupo() != null) {
                curso.setGrupo(entidad.getGrupo() == null ? null : GrupoCursoVerano.valueOf(entidad.getGrupo().name()));
            }
            curso.setCupo_estimado(entidad.getCupo_estimado());
            curso.setSalon(entidad.getSalon());
            curso.setFecha_inicio(entidad.getFecha_inicio());
            curso.setFecha_fin(entidad.getFecha_fin());
            curso.setPeriodo_academico(entidad.getPeriodo_academico());
            if (copiaNulos || entidad.getEstadosCursoOfertados() != null) {
                curso.setEstadosCursoOfertados(entidad.getEstadosCursoOfertados() == null ? null : lista(entidad.getEstadosCursoOfertados(), this::estadoCurso));
            }
            if (copiaNulos || entidad.getEstudiantesInscritos() != null) {
                curso.setEstudiantesInscritos(entidad.getEstudiantesInscritos() == null ? null : lista(entidad.getEstudiantesInscritos(), this::usuario));
            }
            if (copiaNulos || entidad.getSolicitudes() != null) {
                curso.setSolicitudes(entidad.getSolicitudes() == null ? null : lista(entidad.getSolicitudes(), s -> solicitud(s, Solicitud.class)));
            }
            return curso;
        }

        EstadoCursoOfertado estadoCurso(EstadoCursoOfertadoEntity entidad) {
            Object previo = mapeados.get(entidad);
            if (previo instanceof EstadoCursoOfertado estado) {
                return estado;
            }
            EstadoCursoOfertado estado = new EstadoCursoOfertado();
            mapeados.put(entidad, estado);
            estado.setId_estado(entidad.getId_estado());
            if (copiaNulos || entidad.getEstado_actual() != null) {
                estado.setEstado_actual(entidad.getEstado_actual() == null ? null : entidad.getEstado_actual());
            }
            estado.setFecha_registro_estado(entidad.getFecha_registro_estado());
            if (copiaNulos || entidad.getObjCursoOfertadoVerano() != null) {
                estado.setObjCursoOfertadoVerano(entidad.getObjCursoOfertadoVerano() == null ? null : curso(entidad.getObjCursoOfertadoVerano()));
            }
            return estado;
        }

        Notificacion notificacion(NotificacionEntity entidad) {
            Notificacion notificacion = new Notificacion();
            notificacion.setId_notificacion(entidad.getId_notificacion());
            notificacion.setTipoSolicitud(entidad.getTipoSolicitud());
            notificacion.setTipoNotificacion(entidad.getTipoNotificacion());
            notificacion.setTitulo(entidad.getTitulo());
            notificacion.setMensaje(entidad.getMensaje());
            notificacion.setFechaCreacion(entidad.getFechaCreacion());
            if (copiaNulos || entidad.getLeida() != null) {
                notificacion.setLeida(entidad.getLeida() == null ? null : entidad.getLeida());
            }
            if (copiaNulos || entidad.getEsUrgente() != null) {
                notificacion.setEsUrgente(entidad.getEsUrgente() == null ? null : entidad.getEsUrgente());
            }
            notificacion.setAccion(entidad.getAccion());
            notificacion.setUrlAccion(entidad.getUrlAccion());
            if (copiaNulos || entidad.getObjUsuario() != null) {
                notificacion.setObjUsuario(entidad.getObjUsuario() == null ? null : usuario(entidad.getObjUsuario()));
            }
            if (copiaNulos || entidad.getObjSolicitud() != null) {
                notificacion.setObjSolicitud(entidad.getObjSolicitud() == null ? null : solicitud(entidad.getObjSolicitud(), Solicitud.class));
            }
            if (copiaNulos || entidad.getObjCurso() != null) {
                notificacion.setObjCurso(entidad.getObjCurso() == null ? null : curso(entidad.getObjCurso()));
            }
            return notificacion;
        }

        /** Copia los campos de la subclase cuando origen y destino los tienen. */
        private static void camposPropios(SolicitudEntity origen, Solicitud destino) {
            if (origen instanceof SolicitudHomologacionEntity o && destino instanceof SolicitudHomologacion d) {
                d.setRuta_PM_FO_4_FOR_27(o.getRuta_PM_FO_4_FOR_27());
                d.setRuta_contenido_programatico(o.getRuta_contenido_programatico());
                d.setPrograma_origen(o.getPrograma_origen());
                d.setPrograma_destino(o.getPrograma_destino());
            }
            if (origen instanceof SolicitudPazYSalvoEntity o && destino instanceof SolicitudPazYSalvo d) {
                d.setTitulo_trabajo_grado(o.getTitulo_trabajo_grado());
                d.setDirector_trabajo_grado(o.getDirector_trabajo_grado());
            }
            if (origen instanceof SolicitudEcaesEntity o && destino instanceof SolicitudEcaes d) {
                d.setTipoDocumento(o.getTipoDocumento());
                d.setNumero_documento(o.getNumero_documento());
                d.setFecha_expedicion(o.getFecha_expedicion());
                d.setFecha_nacimiento(o.getFecha_nacimiento());
            }
            if (origen instanceof SolicitudCursoVeranoPreinscripcionEntity o) {
                verano(o.getNombre_estudiante(), o.getCodicion_solicitud(), o.getObservacion(), destino);
            }
            if (origen instanceof SolicitudCursoVeranoInscripcionEntity o) {
                verano(o.getNombre_estudiante(), o.getCodicion_solicitud(), o.getObservacion(), destino);
            }
        }

        private static void verano(String nombre, CondicionSolicitudVeranoEntity condicion, String observacion,
                Solicitud destino) {
            CondicionSolicitudVerano condicionDominio = condicion == null ? null
                    : CondicionSolicitudVerano.valueOf(condicion.name());
            if (destino instanceof SolicitudCursoVeranoPreinscripcion d) {
                d.setNombre_estudiante(nombre);
                d.setCodicion_solicitud(condicionDominio);
                d.setObservacion(observacion);
            }
            if (destino instanceof SolicitudCursoVeranoInscripcion d) {
                d.setNombre_estudiante(nombre);
                d.setCodicion_solicitud(condicionDominio);
                d.setObservacion(observacion);
            }
        }
    }

    /** Un recorrido de dominio a entidad; {@code mapeados} da un destino por origen. */
    private static final class AlaEntidad {

        private final Map<Object, Object> mapeados = new IdentityHashMap<>();
        private final boolean copiaNulos;

        AlaEntidad(boolean copiaNulos) {
            this.copiaNulos = copiaNulos;
        }

        <E extends SolicitudEntity> E solicitud(Solicitud solicitud, Class<E> tipo) {
            Object previo = mapeados.get(solicitud);
            if (tipo.isInstance(previo)) {
                return tipo.cast(previo);
            }
            E entidad = tipo.cast(crear(ENTIDAD, tipo).get());
            mapeados.put(solicitud, entidad);
            entidad.setId_solicitud(solicitud.getId_solicitud());
            entidad.setNombre_solicitud(solicitud.getNombre_solicitud());
            entidad.setPeriodo_academico(solicitud.getPeriodo_academico());
            entidad.setFecha_ceremonia(solicitud.getFecha_ceremonia());
            entidad.setFecha_registro_solicitud(solicitud.getFecha_registro_solicitud());
            camposPropios(solicitud, entidad);
            if (copiaNulos || solicitud.getEstadosSolicitud() != null) {
                entidad.setEstadosSolicitud(solicitud.getEstadosSolicitud() == null ? null : lista(solicitud.getEstadosSolicitud(), this::estado));
            }
            if (copiaNulos || solicitud.getObjUsuario() != null) {
                entidad.setObjUsuario(solicitud.getObjUsuario() == null ? null : usuario(solicitud.getObjUsuario()));
            }
            if (copiaNulos || solicitud.getDocumentos() != null) {
                entidad.setDocumentos(solicitud.getDocumentos() == null ? null : lista(solicitud.getDocumentos(), this::documento));
            }
            if (copiaNulos || solicitud.getObjCursoOfertadoVerano() != null) {
                entidad.setObjCursoOfertadoVerano(solicitud.getObjCursoOfertadoVerano() == null ? null : curso(solicitud.getObjCursoOfertadoVerano()));
            }
            return entidad;
        }

        EstadoSolicitudEntity estado(EstadoSolicitud estado) {
            Object previo = mapeados.get(estado);
            if (previo instanceof EstadoSolicitudEntity entidad) {
                return entidad;
            }
            EstadoSolicitudEntity entidad = new EstadoSolicitudEntity();
            mapeados.put(estado, entidad);
            entidad.setId_estado(estado.getId_estado());
            if (copiaNulos || estado.getEstado_actual() != null) {
                entidad.setEstado_actual(estado.getEstado_actual() == null ? null : estado.getEstado_actual());
            }
            entidad.setFecha_registro_estado(estado.getFecha_registro_estado());
            entidad.setComentario(estado.getComentario());
            if (copiaNulos || estado.getObjSolicitud() != null) {
                entidad.setObjSolicitud(estado.getObjSolicitud() == null ? null : solicitud(estado.getObjSolicitud(), SolicitudEntity.class));
            }
            return entidad;
        }

        DocumentoEntity documento(Documento documento) {
            Object previo = mapeados.get(documento);
            if (previo instanceof DocumentoEntity entidad) {
                return entidad;
            }
            DocumentoEntity entidad = new DocumentoEntity();
            mapeados.put(documento, entidad);
            entidad.setId_documento(documento.getId_documento());
            entidad.setNombre(documento.getNombre());
            entidad.setRuta_documento(documento.getRuta_documento());
            entidad.setFecha_documento(documento.getFecha_documento());
            entidad.setEsValido(documento.isEsValido());
            entidad.setComentario(documento.getComentario());
            entidad.setIdUsuarioSubio(documento.getIdUsuarioSubio());
            if (copiaNulos || documento.getObjSolicitud() != null) {
                entidad.setObjSolicitud(documento.getObjSolicitud() == null ? null : solicitud(documento.getObjSolicitud(), SolicitudEntity.class));
            }
            return entidad;
        }

        UsuarioEntity usuario(Usuario usuario) {
            Object previo = mapeados.get(usuario);
            if (previo instanceof UsuarioEntity entidad) {
                return entidad;
            }
            UsuarioEntity entidad = UsuarioMapperPersistencia.aEntidad(usuario);
            mapeados.put(usuario, entidad);
            if (copiaNulos) {
                entidad.setSolicitudes(usuario.getSolicitudes() == null ? null
                        : lista(usuario.getSolicitudes(), s -> solicitud(s, SolicitudEntity.class)));
                entidad.setCursosOfertadosInscritos(usuario.getCursosOfertadosInscritos() == null ? null
                        : lista(usuario.getCursosOfertadosInscritos(), this::curso));
            }
            return entidad;
        }

        CursoOfertadoVeranoEntity curso(CursoOfertadoVerano curso) {
            Object previo = mapeados.get(curso);
            if (previo instanceof CursoOfertadoVeranoEntity entidad) {
                return entidad;
            }
            CursoOfertadoVeranoEntity entidad = new CursoOfertadoVeranoEntity();
            mapeados.put(curso, entidad);
            entidad.setId_curso(curso.getId_curso());
            if (copiaNulos || curso.getObjMateria() != null) {
                entidad.setObjMateria(curso.getObjMateria() == null ? null : CatalogosMapperPersistencia.aEntidad(curso.getObjMateria()));
            }
            if (copiaNulos || curso.getObjDocente() != null) {
                entidad.setObjDocente(curso.getObjDocente() == null ? null : CatalogosMapperPersistencia.aEntidad(curso.getObjDocente()));
            }
            if (copiaNulos || curso.getGrupo() != null) {
                entidad.setGrupo(curso.getGrupo() == null ? null : GrupoCursoVeranoEntity.valueOf(curso.getGrupo().name()));
            }
            entidad.setCupo_estimado(curso.getCupo_estimado());
            entidad.setSalon(curso.getSalon());
            entidad.setFecha_inicio(curso.getFecha_inicio());
            entidad.setFecha_fin(curso.getFecha_fin());
            entidad.setPeriodo_academico(curso.getPeriodo_academico());
            if (copiaNulos || curso.getEstadosCursoOfertados() != null) {
                entidad.setEstadosCursoOfertados(curso.getEstadosCursoOfertados() == null ? null : lista(curso.getEstadosCursoOfertados(), this::estadoCurso));
            }
            if (copiaNulos || curso.getEstudiantesInscritos() != null) {
                entidad.setEstudiantesInscritos(curso.getEstudiantesInscritos() == null ? null : lista(curso.getEstudiantesInscritos(), this::usuario));
            }
            if (copiaNulos || curso.getSolicitudes() != null) {
                entidad.setSolicitudes(curso.getSolicitudes() == null ? null : lista(curso.getSolicitudes(), s -> solicitud(s, SolicitudEntity.class)));
            }
            return entidad;
        }

        EstadoCursoOfertadoEntity estadoCurso(EstadoCursoOfertado estado) {
            Object previo = mapeados.get(estado);
            if (previo instanceof EstadoCursoOfertadoEntity entidad) {
                return entidad;
            }
            EstadoCursoOfertadoEntity entidad = new EstadoCursoOfertadoEntity();
            mapeados.put(estado, entidad);
            entidad.setId_estado(estado.getId_estado());
            if (copiaNulos || estado.getEstado_actual() != null) {
                entidad.setEstado_actual(estado.getEstado_actual() == null ? null : estado.getEstado_actual());
            }
            entidad.setFecha_registro_estado(estado.getFecha_registro_estado());
            if (copiaNulos || estado.getObjCursoOfertadoVerano() != null) {
                entidad.setObjCursoOfertadoVerano(estado.getObjCursoOfertadoVerano() == null ? null : curso(estado.getObjCursoOfertadoVerano()));
            }
            return entidad;
        }

        /** Copia los campos de la subclase cuando origen y destino los tienen. */
        private static void camposPropios(Solicitud origen, SolicitudEntity destino) {
            if (origen instanceof SolicitudHomologacion o && destino instanceof SolicitudHomologacionEntity d) {
                d.setRuta_PM_FO_4_FOR_27(o.getRuta_PM_FO_4_FOR_27());
                d.setRuta_contenido_programatico(o.getRuta_contenido_programatico());
                d.setPrograma_origen(o.getPrograma_origen());
                d.setPrograma_destino(o.getPrograma_destino());
            }
            if (origen instanceof SolicitudPazYSalvo o && destino instanceof SolicitudPazYSalvoEntity d) {
                d.setTitulo_trabajo_grado(o.getTitulo_trabajo_grado());
                d.setDirector_trabajo_grado(o.getDirector_trabajo_grado());
            }
            if (origen instanceof SolicitudEcaes o && destino instanceof SolicitudEcaesEntity d) {
                d.setTipoDocumento(o.getTipoDocumento());
                d.setNumero_documento(o.getNumero_documento());
                d.setFecha_expedicion(o.getFecha_expedicion());
                d.setFecha_nacimiento(o.getFecha_nacimiento());
            }
            if (origen instanceof SolicitudCursoVeranoPreinscripcion o) {
                verano(o.getNombre_estudiante(), o.getCodicion_solicitud(), o.getObservacion(), destino);
            }
            if (origen instanceof SolicitudCursoVeranoInscripcion o) {
                verano(o.getNombre_estudiante(), o.getCodicion_solicitud(), o.getObservacion(), destino);
            }
        }

        private static void verano(String nombre, CondicionSolicitudVerano condicion, String observacion,
                SolicitudEntity destino) {
            CondicionSolicitudVeranoEntity condicionEntidad = condicion == null ? null
                    : CondicionSolicitudVeranoEntity.valueOf(condicion.name());
            if (destino instanceof SolicitudCursoVeranoPreinscripcionEntity d) {
                d.setNombre_estudiante(nombre);
                d.setCodicion_solicitud(condicionEntidad);
                d.setObservacion(observacion);
            }
            if (destino instanceof SolicitudCursoVeranoInscripcionEntity d) {
                d.setNombre_estudiante(nombre);
                d.setCodicion_solicitud(condicionEntidad);
                d.setObservacion(observacion);
            }
        }
    }

    private static <T> Supplier<? extends T> crear(Map<Class<?>, Supplier<? extends T>> fabricas, Class<?> tipo) {
        Supplier<? extends T> fabrica = fabricas.get(tipo);
        if (fabrica == null) {
            throw new IllegalArgumentException("Tipo de solicitud sin mapeo de persistencia: " + tipo.getName());
        }
        return fabrica;
    }

    private static <O, D> List<D> lista(List<O> origen, Function<O, D> mapear) {
        List<D> destino = new ArrayList<>(origen.size());
        for (O elemento : origen) {
            destino.add(elemento == null ? null : mapear.apply(elemento));
        }
        return destino;
    }
}
