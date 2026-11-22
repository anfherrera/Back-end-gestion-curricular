package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.*;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadoSolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ReportesVeranoDTORespuesta.SolicitudReporte;

/** Mismo mapeo de presentación para el listado HTTP y el Excel. */
public final class ReporteSolicitudesVeranoMapper {
    private ReporteSolicitudesVeranoMapper() {}
    public static List<SolicitudReporte> formatear(List<Solicitud> todasLasSolicitudes, Integer idMateria) {
        List<SolicitudReporte> solicitudesFormateadas = new ArrayList<>();

        for (Solicitud solicitud : todasLasSolicitudes) {
            // Solo procesar solicitudes de cursos intersemestrales
            if (solicitud instanceof SolicitudCursoVeranoPreinscripcion ||
                solicitud instanceof SolicitudCursoVeranoInscripcion) {

                // Informacion basica
                String tipo = solicitud instanceof SolicitudCursoVeranoPreinscripcion ?
                    "Preinscripcion" : "Inscripcion";

                // Motivo de la solicitud
                String motivoSolicitud = "No especificado";

                if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                    SolicitudCursoVeranoPreinscripcion preinscripcion = (SolicitudCursoVeranoPreinscripcion) solicitud;
                    if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().trim().isEmpty()) {
                        motivoSolicitud = preinscripcion.getObservacion();
                    } else if (preinscripcion.getNombre_solicitud() != null && !preinscripcion.getNombre_solicitud().trim().isEmpty()) {
                        motivoSolicitud = preinscripcion.getNombre_solicitud();
                    }
                } else if (solicitud instanceof SolicitudCursoVeranoInscripcion) {
                    SolicitudCursoVeranoInscripcion inscripcion = (SolicitudCursoVeranoInscripcion) solicitud;
                    if (inscripcion.getObservacion() != null && !inscripcion.getObservacion().trim().isEmpty()) {
                        motivoSolicitud = inscripcion.getObservacion();
                    } else if (inscripcion.getNombre_solicitud() != null && !inscripcion.getNombre_solicitud().trim().isEmpty()) {
                        motivoSolicitud = inscripcion.getNombre_solicitud();
                    }
                }

                // Informacion del usuario
                String nombreCompleto;
                String codigo;
                if (solicitud.getObjUsuario() != null) {
                    nombreCompleto = solicitud.getObjUsuario().getNombreCompleto();
                    codigo = solicitud.getObjUsuario().getCodigo();
                } else {
                    nombreCompleto = "Usuario no disponible";
                    codigo = "N/A";
                }

                // Informacion del curso
                String nombreCurso = "Curso no disponible";
                String condicion = "N/A";
                Integer idMateriaSolicitud = null;
                Integer idCursoSolicitud = null;
                String codigoMateria = null;
                String grupoCurso = null;
                String estado;

                if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                    SolicitudCursoVeranoPreinscripcion preinscripcion = (SolicitudCursoVeranoPreinscripcion) solicitud;

                    if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                        idCursoSolicitud = preinscripcion.getObjCursoOfertadoVerano().getId_curso();
                        grupoCurso = preinscripcion.getObjCursoOfertadoVerano().getGrupo() != null ?
                            preinscripcion.getObjCursoOfertadoVerano().getGrupo().toString() : null;

                        if (preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                            idMateriaSolicitud = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getIdMateria();
                            codigoMateria = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getCodigo();
                        }
                    } else if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().isEmpty()) {
                        // Para solicitudes de curso nuevo
                        String observacion = preinscripcion.getObservacion();
                        if (observacion.contains(": ")) {
                            nombreCurso = observacion.split(": ")[1].trim();
                        } else {
                            nombreCurso = observacion;
                        }
                    }

                    // Estado de la preinscripcion
                    estado = preinscripcion.estadoActual(EstadoSolicitudCursoVerano.ENVIADA.texto());

                    // Condicion academica del estudiante (Primera Vez, Repitencia, Habilitacion, etc.)
                    condicion = "PRIMERA_VEZ";
                    if (preinscripcion.getCodicion_solicitud() != null) {
                        condicion = preinscripcion.getCodicion_solicitud().toString();
                    }

                } else {
                    SolicitudCursoVeranoInscripcion inscripcion = (SolicitudCursoVeranoInscripcion) solicitud;

                    if (inscripcion.getObjCursoOfertadoVerano() != null) {
                        idCursoSolicitud = inscripcion.getObjCursoOfertadoVerano().getId_curso();
                        grupoCurso = inscripcion.getObjCursoOfertadoVerano().getGrupo() != null ?
                            inscripcion.getObjCursoOfertadoVerano().getGrupo().toString() : null;

                        if (inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                            idMateriaSolicitud = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getIdMateria();
                            codigoMateria = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getCodigo();
                        }
                    }

                    // Condicion de la inscripcion
                    if (inscripcion.getCodicion_solicitud() != null) {
                        condicion = inscripcion.getCodicion_solicitud().toString();
                    }

                    // Estado de la inscripcion
                    estado = inscripcion.estadoActual(EstadoSolicitudCursoVerano.ENVIADA.texto());
                }

                // Filtrar por materia si se proporciona
                if (idMateria != null && idMateria != 0) {
                    if (idMateriaSolicitud == null || !idMateriaSolicitud.equals(idMateria)) {
                        continue; // Saltar esta solicitud si no coincide con el filtro de materia
                    }
                }

                solicitudesFormateadas.add(new SolicitudReporte(solicitud.getId_solicitud(),
                    solicitud.getFecha_registro_solicitud(), tipo, motivoSolicitud, nombreCompleto, codigo, estado,
                    condicion, nombreCurso, idMateriaSolicitud, idCursoSolicitud, codigoMateria, grupoCurso));
            }
        }
        return solicitudesFormateadas;
    }
}
