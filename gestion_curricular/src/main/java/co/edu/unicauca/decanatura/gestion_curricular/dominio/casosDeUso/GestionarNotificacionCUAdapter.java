package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarNotificacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;

public class GestionarNotificacionCUAdapter implements GestionarNotificacionCUIntPort {

    private final GestionarNotificacionGatewayIntPort objGestionarNotificacionGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarNotificacionCUAdapter(GestionarNotificacionGatewayIntPort objGestionarNotificacionGateway,
                                         FormateadorResultadosIntPort objFormateadorResultados) {
        this.objGestionarNotificacionGateway = objGestionarNotificacionGateway;
        this.objFormateadorResultados = objFormateadorResultados;
    }

    @Override
    public Notificacion crearNotificacion(Notificacion notificacion) {
        if (notificacion == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La notificación no puede ser nula");
        }
        if (notificacion.getObjUsuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la notificación no puede ser nulo");
        }
        if (notificacion.getTipoSolicitud() == null || notificacion.getTipoSolicitud().trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de solicitud no puede estar vacío");
        }
        if (notificacion.getTipoNotificacion() == null || notificacion.getTipoNotificacion().trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de notificación no puede estar vacío");
        }
        if (notificacion.getMensaje() == null || notificacion.getMensaje().trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El mensaje no puede estar vacío");
        }
        
        return this.objGestionarNotificacionGateway.crearNotificacion(notificacion);
    }

    @Override
    public Notificacion actualizarNotificacion(Notificacion notificacion) {
        if (notificacion == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La notificación no puede ser nula");
        }
        if (notificacion.getId_notificacion() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la notificación no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.actualizarNotificacion(notificacion);
    }

    @Override
    public boolean eliminarNotificacion(Integer idNotificacion) {
        if (idNotificacion == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la notificación no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.eliminarNotificacion(idNotificacion);
    }

    @Override
    public Notificacion obtenerNotificacionPorId(Integer idNotificacion) {
        if (idNotificacion == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la notificación no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.obtenerNotificacionPorId(idNotificacion);
    }

    @Override
    public List<Notificacion> buscarPorUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorUsuario(idUsuario);
    }

    @Override
    public Page<Notificacion> buscarPorUsuarioPaginado(Integer idUsuario, Pageable pageable) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        if (pageable == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La paginación no puede ser nula");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorUsuarioPaginado(idUsuario, pageable);
    }

    @Override
    public List<Notificacion> buscarNoLeidasPorUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.buscarNoLeidasPorUsuario(idUsuario);
    }

    @Override
    public Integer contarNoLeidasPorUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.contarNoLeidasPorUsuario(idUsuario);
    }

    @Override
    public List<Notificacion> buscarPorTipoSolicitud(String tipoSolicitud) {
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de solicitud no puede estar vacío");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorTipoSolicitud(tipoSolicitud);
    }

    @Override
    public List<Notificacion> buscarPorTipoNotificacion(String tipoNotificacion) {
        if (tipoNotificacion == null || tipoNotificacion.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de notificación no puede estar vacío");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorTipoNotificacion(tipoNotificacion);
    }

    @Override
    public List<Notificacion> buscarPorUsuarioYTipoSolicitud(Integer idUsuario, String tipoSolicitud) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de solicitud no puede estar vacío");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorUsuarioYTipoSolicitud(idUsuario, tipoSolicitud);
    }

    @Override
    public List<Notificacion> buscarPorSolicitud(Integer idSolicitud) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorSolicitud(idSolicitud);
    }

    @Override
    public List<Notificacion> buscarPorCurso(Integer idCurso) {
        if (idCurso == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del curso no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorCurso(idCurso);
    }

    @Override
    public List<Notificacion> buscarUrgentes() {
        return this.objGestionarNotificacionGateway.buscarUrgentes();
    }

    @Override
    public List<Notificacion> buscarPorRangoFechas(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Las fechas no pueden ser nulas");
        }
        if (fechaInicio.after(fechaFin)) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La fecha de inicio no puede ser posterior a la fecha fin");
        }
        
        return this.objGestionarNotificacionGateway.buscarPorRangoFechas(fechaInicio, fechaFin);
    }

    @Override
    public boolean marcarComoLeida(Integer idNotificacion) {
        if (idNotificacion == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la notificación no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.marcarComoLeida(idNotificacion);
    }

    @Override
    public boolean marcarComoNoLeida(Integer idNotificacion) {
        if (idNotificacion == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la notificación no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.marcarComoNoLeida(idNotificacion);
    }

    @Override
    public boolean marcarTodasComoLeidas(Integer idUsuario) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.marcarTodasComoLeidas(idUsuario);
    }

    @Override
    public Map<String, Long> estadisticasPorTipoSolicitud() {
        return this.objGestionarNotificacionGateway.estadisticasPorTipoSolicitud();
    }

    @Override
    public Map<String, Long> estadisticasPorTipoNotificacion() {
        return this.objGestionarNotificacionGateway.estadisticasPorTipoNotificacion();
    }

    @Override
    public Map<Integer, Long> estadisticasPorUsuario() {
        return this.objGestionarNotificacionGateway.estadisticasPorUsuario();
    }

    @Override
    public Map<String, Object> obtenerDashboardUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("notificacionesNoLeidas", this.contarNoLeidasPorUsuario(idUsuario));
        dashboard.put("ultimasNotificaciones", this.buscarPorUsuario(idUsuario).stream().limit(5).toList());
        dashboard.put("notificacionesUrgentes", this.buscarUrgentes().stream()
            .filter(n -> n.getObjUsuario().getId_usuario().equals(idUsuario))
            .toList());
        
        return dashboard;
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("porTipoSolicitud", this.estadisticasPorTipoSolicitud());
        estadisticas.put("porTipoNotificacion", this.estadisticasPorTipoNotificacion());
        estadisticas.put("porUsuario", this.estadisticasPorUsuario());
        estadisticas.put("totalNotificaciones", this.estadisticasPorTipoSolicitud().values().stream().mapToLong(Long::longValue).sum());
        
        return estadisticas;
    }

    @Override
    public void eliminarNotificacionesAntiguas(Date fechaLimite) {
        if (fechaLimite == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La fecha límite no puede ser nula");
        }
        
        this.objGestionarNotificacionGateway.eliminarNotificacionesAntiguas(fechaLimite);
    }

    // Métodos de utilidad para crear notificaciones comunes
    @Override
    public Notificacion crearNotificacionNuevaSolicitud(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, String mensaje) {
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de solicitud no puede estar vacío");
        }
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        if (mensaje == null || mensaje.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El mensaje no puede estar vacío");
        }
        
        return this.objGestionarNotificacionGateway.crearNotificacionNuevaSolicitud(tipoSolicitud, idUsuario, idSolicitud, mensaje);
    }

    @Override
    public Notificacion crearNotificacionCambioEstado(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, String estadoAnterior, String estadoNuevo) {
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de solicitud no puede estar vacío");
        }
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        if (estadoAnterior == null || estadoAnterior.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El estado anterior no puede estar vacío");
        }
        if (estadoNuevo == null || estadoNuevo.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El estado nuevo no puede estar vacío");
        }
        
        return this.objGestionarNotificacionGateway.crearNotificacionCambioEstado(tipoSolicitud, idUsuario, idSolicitud, estadoAnterior, estadoNuevo);
    }

    @Override
    public Notificacion crearNotificacionAprobacion(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, boolean aprobado, String motivo) {
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de solicitud no puede estar vacío");
        }
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        
        return this.objGestionarNotificacionGateway.crearNotificacionAprobacion(tipoSolicitud, idUsuario, idSolicitud, aprobado, motivo);
    }

    @Override
    public Notificacion crearNotificacionAlerta(String tipoSolicitud, Integer idUsuario, String titulo, String mensaje, boolean esUrgente) {
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El tipo de solicitud no puede estar vacío");
        }
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El título no puede estar vacío");
        }
        if (mensaje == null || mensaje.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El mensaje no puede estar vacío");
        }
        
        return this.objGestionarNotificacionGateway.crearNotificacionAlerta(tipoSolicitud, idUsuario, titulo, mensaje, esUrgente);
    }

    // Métodos específicos para cursos de verano
    @Override
    public Notificacion notificarNuevaPreinscripcion(Integer idUsuario, Integer idSolicitud) {
        return this.crearNotificacionNuevaSolicitud("CURSO_VERANO", idUsuario, idSolicitud, 
            "✅ Preinscripción enviada exitosamente. Tu solicitud #" + idSolicitud + " ha sido recibida y está en proceso de revisión. Te notificaremos cuando sea evaluada por la coordinación académica.");
    }

    @Override
    public Notificacion notificarNuevaInscripcion(Integer idUsuario, Integer idSolicitud) {
        return this.crearNotificacionNuevaSolicitud("CURSO_VERANO", idUsuario, idSolicitud, 
            "🎓 Inscripción enviada exitosamente. Tu solicitud #" + idSolicitud + " ha sido recibida y está en proceso de validación. Te notificaremos cuando tu comprobante de pago sea revisado.");
    }

    @Override
    public Notificacion notificarPreinscripcionAprobada(Integer idUsuario, Integer idSolicitud) {
        return this.crearNotificacionAprobacion("CURSO_VERANO", idUsuario, idSolicitud, true, 
            "🎉 ¡Excelente! Tu preinscripción #" + idSolicitud + " ha sido APROBADA por la coordinación académica. Ahora puedes proceder con la inscripción formal y el pago correspondiente.");
    }

    @Override
    public Notificacion notificarPreinscripcionRechazada(Integer idUsuario, Integer idSolicitud, String motivo) {
        return this.crearNotificacionAprobacion("CURSO_VERANO", idUsuario, idSolicitud, false, 
            "❌ Tu preinscripción #" + idSolicitud + " no pudo ser aprobada. Motivo: " + (motivo != null ? motivo : "No cumple con los requisitos establecidos") + ". Puedes contactar a la coordinación académica para más información.");
    }

    @Override
    public Notificacion notificarPagoValidado(Integer idUsuario, Integer idSolicitud, boolean esValido) {
        if (esValido) {
            return this.crearNotificacionAlerta("CURSO_VERANO", idUsuario, "Pago Validado", 
                "💰 ¡Excelente! Tu comprobante de pago para la solicitud #" + idSolicitud + " ha sido VALIDADO correctamente. Tu inscripción está oficialmente confirmada.", false);
        } else {
            return this.crearNotificacionAlerta("CURSO_VERANO", idUsuario, "Pago Rechazado", 
                "⚠️ Tu comprobante de pago para la solicitud #" + idSolicitud + " fue RECHAZADO. Por favor, verifica que el documento sea legible y corresponda al pago correcto, luego súbelo nuevamente.", true);
        }
    }

    @Override
    public Notificacion notificarInscripcionCompletada(Integer idUsuario, Integer idSolicitud) {
        return this.crearNotificacionAlerta("CURSO_VERANO", idUsuario, "Inscripción Completada", 
            "🎓 ¡Felicidades! Tu inscripción #" + idSolicitud + " al curso intersemestral ha sido COMPLETADA exitosamente. Ya estás oficialmente inscrito y recibirás más información sobre el inicio de clases.", false);
    }

    // Métodos para alertas automáticas
    @Override
    public Notificacion alertarCursoAltaDemanda(Integer idCurso, Integer cantidadSolicitudes) {
        // Notificar a funcionarios sobre alta demanda
        return this.crearNotificacionAlerta("CURSO_VERANO", 1, "Alta Demanda", 
            "El curso tiene " + cantidadSolicitudes + " preinscripciones. Considera abrir más grupos.", true);
    }

    @Override
    public Notificacion alertarCursoPuntoEquilibrio(Integer idCurso, Integer cantidadSolicitudes, Integer cupoEstimado) {
        return this.crearNotificacionAlerta("CURSO_VERANO", 1, "Punto de Equilibrio Alcanzado", 
            "El curso ha alcanzado el punto de equilibrio con " + cantidadSolicitudes + " solicitudes para un cupo de " + cupoEstimado + ".", false);
    }

    @Override
    public Notificacion alertarFechaLimiteProxima(String tipoSolicitud, Date fechaLimite) {
        return this.crearNotificacionAlerta(tipoSolicitud, 1, "Fecha Límite Próxima", 
            "La fecha límite para " + tipoSolicitud + " es el " + fechaLimite + ". Revisa las solicitudes pendientes.", true);
    }

    // Métodos para notificar a funcionarios
    @Override
    public void notificarFuncionariosNuevaSolicitud(String tipoSolicitud, Integer idSolicitud, String nombreEstudiante) {
        try {
            // Buscar funcionarios por rol
            List<Notificacion> funcionarios = this.objGestionarNotificacionGateway.buscarFuncionariosPorRol("Funcionario");
            List<Notificacion> coordinadores = this.objGestionarNotificacionGateway.buscarFuncionariosPorRol("Coordinador");
            List<Notificacion> secretarios = this.objGestionarNotificacionGateway.buscarFuncionariosPorRol("Secretario");
            
            // Combinar todas las listas
            List<Notificacion> todosLosFuncionarios = new ArrayList<>();
            todosLosFuncionarios.addAll(funcionarios);
            todosLosFuncionarios.addAll(coordinadores);
            todosLosFuncionarios.addAll(secretarios);
            
            // Crear notificación para cada funcionario
            for (Notificacion funcionario : todosLosFuncionarios) {
                String mensaje = "📋 Nueva solicitud de " + tipoSolicitud + " del estudiante " + nombreEstudiante + 
                    " (Solicitud #" + idSolicitud + "). Requiere revisión y aprobación.";
                
                this.objGestionarNotificacionGateway.crearNotificacionAlerta(
                    tipoSolicitud, 
                    funcionario.getObjUsuario().getId_usuario(), 
                    "Nueva Solicitud Pendiente", 
                    mensaje, 
                    true // Es urgente para funcionarios
                );
            }
            
            System.out.println("✅ [NOTIFICACIONES_FUNCIONARIOS] Notificaciones enviadas a " + todosLosFuncionarios.size() + " funcionarios para solicitud #" + idSolicitud);
            
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICACIONES_FUNCIONARIOS] Error notificando a funcionarios: " + e.getMessage());
        }
    }

    @Override
    public void notificarFuncionariosCambioEstado(String tipoSolicitud, Integer idSolicitud, String nombreEstudiante, String estadoAnterior, String estadoNuevo) {
        try {
            // Buscar funcionarios por rol
            List<Notificacion> funcionarios = this.objGestionarNotificacionGateway.buscarFuncionariosPorRol("Funcionario");
            List<Notificacion> coordinadores = this.objGestionarNotificacionGateway.buscarFuncionariosPorRol("Coordinador");
            
            // Combinar listas
            List<Notificacion> todosLosFuncionarios = new ArrayList<>();
            todosLosFuncionarios.addAll(funcionarios);
            todosLosFuncionarios.addAll(coordinadores);
            
            // Crear notificación para cada funcionario
            for (Notificacion funcionario : todosLosFuncionarios) {
                String mensaje = "🔄 Cambio de estado en solicitud #" + idSolicitud + " del estudiante " + nombreEstudiante + 
                    ". Estado: " + estadoAnterior + " → " + estadoNuevo;
                
                this.objGestionarNotificacionGateway.crearNotificacionAlerta(
                    tipoSolicitud, 
                    funcionario.getObjUsuario().getId_usuario(), 
                    "Estado de Solicitud Actualizado", 
                    mensaje, 
                    false // No es urgente
                );
            }
            
            System.out.println("✅ [NOTIFICACIONES_FUNCIONARIOS] Notificaciones de cambio de estado enviadas a " + todosLosFuncionarios.size() + " funcionarios");
            
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICACIONES_FUNCIONARIOS] Error notificando cambio de estado: " + e.getMessage());
        }
    }

    @Override
    public void notificarFuncionariosDocumentoSubido(String tipoSolicitud, Integer idSolicitud, String nombreEstudiante, String tipoDocumento) {
        try {
            // Buscar funcionarios por rol
            List<Notificacion> funcionarios = this.objGestionarNotificacionGateway.buscarFuncionariosPorRol("Funcionario");
            List<Notificacion> secretarios = this.objGestionarNotificacionGateway.buscarFuncionariosPorRol("Secretario");
            
            // Combinar listas
            List<Notificacion> todosLosFuncionarios = new ArrayList<>();
            todosLosFuncionarios.addAll(funcionarios);
            todosLosFuncionarios.addAll(secretarios);
            
            // Crear notificación para cada funcionario
            for (Notificacion funcionario : todosLosFuncionarios) {
                String mensaje = "📄 Nuevo documento subido: " + tipoDocumento + " para la solicitud #" + idSolicitud + 
                    " del estudiante " + nombreEstudiante + ". Requiere validación.";
                
                this.objGestionarNotificacionGateway.crearNotificacionAlerta(
                    tipoSolicitud, 
                    funcionario.getObjUsuario().getId_usuario(), 
                    "Documento Subido para Validación", 
                    mensaje, 
                    true // Es urgente para validación
                );
            }
            
            System.out.println("✅ [NOTIFICACIONES_FUNCIONARIOS] Notificaciones de documento subido enviadas a " + todosLosFuncionarios.size() + " funcionarios");
            
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICACIONES_FUNCIONARIOS] Error notificando documento subido: " + e.getMessage());
        }
    }

    @Override
    public List<Notificacion> buscarFuncionariosPorRol(String nombreRol) {
        return this.objGestionarNotificacionGateway.buscarFuncionariosPorRol(nombreRol);
    }
}
