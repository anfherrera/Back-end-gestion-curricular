package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Servicio para el envío de correos electrónicos
 * Integrado con el sistema de notificaciones
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.email.from-name:Gestión Curricular - FIET}")
    private String fromName;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Verifica si el correo está configurado correctamente
     */
    public boolean estaConfigurado() {
        return fromEmail != null && !fromEmail.isBlank();
    }

    /**
     * Envía un correo electrónico cuando se crea una notificación
     * @param notificacion La notificación a enviar por correo
     * @param destinatarioEmail El correo del destinatario
     * @param nombreDestinatario El nombre del destinatario
     */
    public void enviarNotificacionPorCorreo(Notificacion notificacion, String destinatarioEmail, String nombreDestinatario) {
        if (!emailEnabled) {
            log.debug("Envío de correos deshabilitado. Notificación ID: {}", notificacion.getId_notificacion());
            return;
        }

        if (destinatarioEmail == null || destinatarioEmail.isBlank()) {
            log.warn("No se puede enviar correo: el destinatario no tiene correo electrónico. Notificación ID: {}", 
                    notificacion.getId_notificacion());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar remitente
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatarioEmail);
            helper.setSubject(obtenerAsunto(notificacion));

            // Preparar contexto para la plantilla
            Context context = new Context(new Locale("es", "CO"));
            context.setVariable("nombreDestinatario", nombreDestinatario != null ? nombreDestinatario : "Usuario");
            context.setVariable("titulo", notificacion.getTitulo());
            context.setVariable("mensaje", notificacion.getMensaje());
            context.setVariable("tipoSolicitud", notificacion.getTipoSolicitud());
            context.setVariable("tipoNotificacion", notificacion.getTipoNotificacion());
            context.setVariable("esUrgente", notificacion.getEsUrgente() != null && notificacion.getEsUrgente());
            context.setVariable("fechaCreacion", formatearFecha(notificacion.getFechaCreacion()));
            context.setVariable("urlAccion", notificacion.getUrlAccion() != null ? notificacion.getUrlAccion() : "");
            context.setVariable("accion", notificacion.getAccion() != null ? notificacion.getAccion() : "Ver detalles");

            // Generar contenido HTML desde la plantilla
            String htmlContent = templateEngine.process("email/notificacion", context);
            helper.setText(htmlContent, true);

            // Enviar correo
            mailSender.send(message);
            log.info("Correo enviado exitosamente a: {} para notificación ID: {}", destinatarioEmail, notificacion.getId_notificacion());

        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", destinatarioEmail, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al enviar correo a {}: {}", destinatarioEmail, e.getMessage(), e);
        }
    }

    /**
     * Envía un correo simple (texto plano) - método alternativo
     */
    public void enviarCorreoSimple(String destinatario, String asunto, String mensaje) {
        if (!emailEnabled) {
            log.debug("Envío de correos deshabilitado");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinatario);
            message.setSubject(asunto);
            message.setText(mensaje);

            mailSender.send(message);
            log.info("Correo simple enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo simple a {}: {}", destinatario, e.getMessage(), e);
        }
    }

    /**
     * Obtiene el asunto del correo basado en el tipo de notificación
     */
    private String obtenerAsunto(Notificacion notificacion) {
        String tipoNotificacion = notificacion.getTipoNotificacion();
        String tipoSolicitud = notificacion.getTipoSolicitud();

        String prefijo = "[Gestión Curricular - FIET] ";

        switch (tipoNotificacion) {
            case "NUEVA_SOLICITUD":
                return prefijo + "Nueva solicitud de " + tipoSolicitud;
            case "APROBADA":
            case "APROBADA_FUNCIONARIO":
            case "APROBADA_COORDINADOR":
                return prefijo + "Solicitud aprobada: " + tipoSolicitud;
            case "RECHAZADA":
                return prefijo + "Solicitud rechazada: " + tipoSolicitud;
            case "CAMBIO_ESTADO":
                return prefijo + "Actualización de solicitud: " + tipoSolicitud;
            default:
                return prefijo + notificacion.getTitulo();
        }
    }

    /**
     * Formatea la fecha para mostrar en el correo
     */
    private String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "Fecha no disponible";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es", "CO"));
        return formatter.format(fecha);
    }
}

