package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;

/** Mismo mapeo de presentación para el listado HTTP y el Excel. */
public final class ReporteSolicitudesVeranoMapper {
    private ReporteSolicitudesVeranoMapper() {}
    public static List<Map<String, Object>> formatear(List<Solicitud> todasLasSolicitudes, Integer idMateria) {
        List<Map<String, Object>> solicitudesFormateadas = new ArrayList<>();
        
        for (Solicitud solicitud : todasLasSolicitudes) {
            // Solo procesar solicitudes de cursos intersemestrales
            if (solicitud instanceof SolicitudCursoVeranoPreinscripcion || 
                solicitud instanceof SolicitudCursoVeranoIncripcion) {
                
                Map<String, Object> solicitudInfo = new HashMap<>();
                
                // Informacion basica
                solicitudInfo.put("id", solicitud.getId_solicitud());
                solicitudInfo.put("fecha", solicitud.getFecha_registro_solicitud());
                solicitudInfo.put("tipo", solicitud instanceof SolicitudCursoVeranoPreinscripcion ? 
                    "Preinscripcion" : "Inscripcion");
                
                // Motivo de la solicitud
                String motivoSolicitud = "No especificado";
                
                if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                    SolicitudCursoVeranoPreinscripcion preinscripcion = (SolicitudCursoVeranoPreinscripcion) solicitud;
                    if (preinscripcion.getObservacion() != null && !preinscripcion.getObservacion().trim().isEmpty()) {
                        motivoSolicitud = preinscripcion.getObservacion();
                    } else if (preinscripcion.getNombre_solicitud() != null && !preinscripcion.getNombre_solicitud().trim().isEmpty()) {
                        motivoSolicitud = preinscripcion.getNombre_solicitud();
                    }
                } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
                    SolicitudCursoVeranoIncripcion inscripcion = (SolicitudCursoVeranoIncripcion) solicitud;
                    if (inscripcion.getObservacion() != null && !inscripcion.getObservacion().trim().isEmpty()) {
                        motivoSolicitud = inscripcion.getObservacion();
                    } else if (inscripcion.getNombre_solicitud() != null && !inscripcion.getNombre_solicitud().trim().isEmpty()) {
                        motivoSolicitud = inscripcion.getNombre_solicitud();
                    }
                }
                
                solicitudInfo.put("motivoSolicitud", motivoSolicitud);
                
                // Informacion del usuario
                if (solicitud.getObjUsuario() != null) {
                    solicitudInfo.put("nombreCompleto", solicitud.getObjUsuario().getNombre_completo());
                    solicitudInfo.put("codigo", solicitud.getObjUsuario().getCodigo());
                } else {
                    solicitudInfo.put("nombreCompleto", "Usuario no disponible");
                    solicitudInfo.put("codigo", "N/A");
                }
                
                // Informacion del curso
                String nombreCurso = "Curso no disponible";
                String condicion = "N/A";
                Integer idMateriaSolicitud = null;
                Integer idCursoSolicitud = null;
                String codigoMateria = null;
                String grupoCurso = null;
                
                if (solicitud instanceof SolicitudCursoVeranoPreinscripcion) {
                    SolicitudCursoVeranoPreinscripcion preinscripcion = (SolicitudCursoVeranoPreinscripcion) solicitud;
                    
                    if (preinscripcion.getObjCursoOfertadoVerano() != null) {
                        idCursoSolicitud = preinscripcion.getObjCursoOfertadoVerano().getId_curso();
                        grupoCurso = preinscripcion.getObjCursoOfertadoVerano().getGrupo() != null ? 
                            preinscripcion.getObjCursoOfertadoVerano().getGrupo().toString() : null;
                        
                        if (preinscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                            idMateriaSolicitud = preinscripcion.getObjCursoOfertadoVerano().getObjMateria().getId_materia();
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
                    String estadoPreinscripcion = preinscripcion.estadoActual(EstadoSolicitudCursoVerano.ENVIADA.texto());
                    solicitudInfo.put("estado", estadoPreinscripcion);
                    
                    // Condicion academica del estudiante (Primera Vez, Repitencia, Habilitacion, etc.)
                    String condicionAcademica = "PRIMERA_VEZ";
                    if (preinscripcion.getCodicion_solicitud() != null) {
                        condicionAcademica = preinscripcion.getCodicion_solicitud().toString();
                    }
                    solicitudInfo.put("condicion", condicionAcademica);
                    
                } else if (solicitud instanceof SolicitudCursoVeranoIncripcion) {
                    SolicitudCursoVeranoIncripcion inscripcion = (SolicitudCursoVeranoIncripcion) solicitud;
                    
                    if (inscripcion.getObjCursoOfertadoVerano() != null) {
                        idCursoSolicitud = inscripcion.getObjCursoOfertadoVerano().getId_curso();
                        grupoCurso = inscripcion.getObjCursoOfertadoVerano().getGrupo() != null ? 
                            inscripcion.getObjCursoOfertadoVerano().getGrupo().toString() : null;
                        
                        if (inscripcion.getObjCursoOfertadoVerano().getObjMateria() != null) {
                            nombreCurso = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getNombre();
                            idMateriaSolicitud = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getId_materia();
                            codigoMateria = inscripcion.getObjCursoOfertadoVerano().getObjMateria().getCodigo();
                        }
                    }
                    
                    // Condicion de la inscripcion
                    if (inscripcion.getCodicion_solicitud() != null) {
                        condicion = inscripcion.getCodicion_solicitud().toString();
                    }
                    
                    // Estado de la inscripcion
                    String estadoInscripcion = inscripcion.estadoActual(EstadoSolicitudCursoVerano.ENVIADA.texto());
                    solicitudInfo.put("estado", estadoInscripcion);
                    solicitudInfo.put("condicion", condicion);
                }
                
                solicitudInfo.put("curso", nombreCurso);
                solicitudInfo.put("idMateria", idMateriaSolicitud);
                solicitudInfo.put("idCurso", idCursoSolicitud);
                solicitudInfo.put("codigoMateria", codigoMateria);
                solicitudInfo.put("grupoCurso", grupoCurso);
                
                // Filtrar por materia si se proporciona
                if (idMateria != null && idMateria != 0) {
                    if (idMateriaSolicitud == null || !idMateriaSolicitud.equals(idMateria)) {
                        continue; // Saltar esta solicitud si no coincide con el filtro de materia
                    }
                }
                
                solicitudesFormateadas.add(solicitudInfo);
            }
        }
        return solicitudesFormateadas;
    }
}
