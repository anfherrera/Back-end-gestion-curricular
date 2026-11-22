package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

/**
 * Nombres JSON históricos de los campos del dominio renombrados a camelCase (OPC-2).
 *
 * Algunas respuestas serializan modelos del dominio tal cual, y el dominio no puede importar Jackson
 * ({@code ArquitecturaHexagonalTest}). Este módulo, que Spring Boot registra en su {@code ObjectMapper},
 * devuelve a cada campo renombrado su nombre anterior al serializar y al leer. También fija el orden de
 * las propiedades: Jackson pasaría al final las que tienen un nombre explícito, y algunas
 * caracterizaciones comparan el texto. El orden es el de los campos, de la superclase a la subclase,
 * que es el que Jackson usa sin nombres explícitos.
 */
@Configuration
public class NombresJsonDominio {

    /** Clase que declara el campo → nombre nuevo del campo → nombre histórico. */
    private static final Map<Class<?>, Map<String, String>> HISTORICOS = new HashMap<>();

    static {
        historico(Rol.class, "idRol", "id_rol");
        historico(Programa.class, "idPrograma", "id_programa");
        historico(Programa.class, "nombrePrograma", "nombre_programa");
        historico(Materia.class, "idMateria", "id_materia");
        historico(Docente.class, "idDocente", "id_docente");
        historico(Docente.class, "codigoDocente", "codigo_docente");
        historico(Docente.class, "nombreDocente", "nombre_docente");
        historico(Salon.class, "idSalon", "id_salon");
        historico(Salon.class, "numeroSalon", "numero_salon");
        historico(Notificacion.class, "idNotificacion", "id_notificacion");
        historico(Usuario.class, "idUsuario", "id_usuario");
        historico(Usuario.class, "nombreCompleto", "nombre_completo");
        historico(Usuario.class, "estadoUsuario", "estado_usuario");
        historico(Solicitud.class, "idSolicitud", "id_solicitud");
        historico(Solicitud.class, "nombreSolicitud", "nombre_solicitud");
        historico(Solicitud.class, "periodoAcademico", "periodo_academico");
        historico(Solicitud.class, "fechaCeremonia", "fecha_ceremonia");
        historico(Solicitud.class, "fechaRegistroSolicitud", "fecha_registro_solicitud");
        historico(EstadoSolicitud.class, "idEstado", "id_estado");
        historico(EstadoSolicitud.class, "estadoActual", "estado_actual");
        historico(EstadoSolicitud.class, "fechaRegistroEstado", "fecha_registro_estado");
        historico(Documento.class, "idDocumento", "id_documento");
        historico(Documento.class, "rutaDocumento", "ruta_documento");
        historico(Documento.class, "fechaDocumento", "fecha_documento");
        historico(SolicitudEcaes.class, "numeroDocumento", "numero_documento");
        historico(SolicitudEcaes.class, "fechaExpedicion", "fecha_expedicion");
        historico(SolicitudEcaes.class, "fechaNacimiento", "fecha_nacimiento");
        historico(FechaEcaes.class, "inscripcionEstByFacultad", "inscripcion_est_by_facultad");
        historico(FechaEcaes.class, "registroRecaudoOrdinario", "registro_recaudo_ordinario");
        historico(FechaEcaes.class, "registroRecaudoExtraordinario", "registro_recaudo_extraordinario");
        historico(FechaEcaes.class, "resultadosIndividuales", "resultados_individuales");
        historico(SolicitudHomologacion.class, "rutaPmFo4For27", "ruta_PM_FO_4_FOR_27");
        historico(SolicitudHomologacion.class, "rutaContenidoProgramatico", "ruta_contenido_programatico");
        historico(SolicitudHomologacion.class, "programaOrigen", "programa_origen");
        historico(SolicitudHomologacion.class, "programaDestino", "programa_destino");
        historico(SolicitudPazYSalvo.class, "tituloTrabajoGrado", "titulo_trabajo_grado");
        historico(SolicitudPazYSalvo.class, "directorTrabajoGrado", "director_trabajo_grado");
    }

    private static void historico(Class<?> clase, String campo, String nombre) {
        HISTORICOS.computeIfAbsent(clase, c -> new HashMap<>()).put(campo, nombre);
    }

    /** El nombre histórico del campo, o su nombre si no se renombró. */
    public static String nombre(Class<?> declarante, String campo) {
        Map<String, String> propios = HISTORICOS.get(declarante);
        String historico = propios == null ? null : propios.get(campo);
        return historico != null ? historico : campo;
    }

    @Bean
    public Module nombresJsonDominioModulo() {
        return modulo();
    }

    /** El módulo, también para los {@code ObjectMapper} que no crea Spring Boot. */
    public static Module modulo() {
        return new SimpleModule("NombresJsonDominio") {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.insertAnnotationIntrospector(new Introspector());
            }
        };
    }

    private static boolean renombrado(Class<?> clase) {
        for (Class<?> c = clase; c != null && c != Object.class; c = c.getSuperclass()) {
            if (HISTORICOS.containsKey(c)) {
                return true;
            }
        }
        return false;
    }

    private static final class Introspector extends NopAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public PropertyName findNameForSerialization(Annotated a) {
            return nombreExplicito(a);
        }

        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            return nombreExplicito(a);
        }

        private static PropertyName nombreExplicito(Annotated a) {
            if (a instanceof AnnotatedField campo) {
                String nombre = nombre(campo.getDeclaringClass(), campo.getName());
                if (!nombre.equals(campo.getName())) {
                    return PropertyName.construct(nombre);
                }
            }
            return null;
        }

        @Override
        public String[] findSerializationPropertyOrder(AnnotatedClass ac) {
            if (!renombrado(ac.getRawType())) {
                return null;
            }
            Deque<Class<?>> jerarquia = new ArrayDeque<>();
            for (Class<?> c = ac.getRawType(); c != null && c != Object.class; c = c.getSuperclass()) {
                jerarquia.push(c);
            }
            List<String> orden = new ArrayList<>();
            for (Class<?> c : jerarquia) {
                for (Field campo : c.getDeclaredFields()) {
                    if (!Modifier.isStatic(campo.getModifiers()) && !campo.isSynthetic()) {
                        orden.add(nombre(c, campo.getName()));
                    }
                }
            }
            return orden.toArray(String[]::new);
        }
    }
}
