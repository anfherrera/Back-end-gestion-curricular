package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public class GestionarDocumentosCUAdapter implements GestionarDocumentosCUIntPort {

    private static final DateTimeFormatter FECHA_COMENTARIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final GestionarDocumentosGatewayIntPort gestionarDocumentosGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarDocumentosCUAdapter(GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
                                        FormateadorResultadosIntPort formateadorResultados) {
        this.gestionarDocumentosGateway = gestionarDocumentosGateway;
        this.formateadorResultados = formateadorResultados;
    }

    @Override
    public Documento buscarDocumentoId(Integer idDocumento) {
        if(idDocumento == null) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del documento debe ser un número positivo.");

        }

        Documento documento = gestionarDocumentosGateway.buscarDocumentoId(idDocumento);
        if (documento == null) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el documento con ID: " + idDocumento);
        }
        return documento;
    }

    @Override
    public void añadirComentario(Integer idDocumento, String comentario, String nombreUsuario, String rolUsuario) {
        if (idDocumento == null || comentario == null) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del documento y el comentario son obligatorios.");
        }
        String textoNuevo = comentario.trim();
        if (textoNuevo.isEmpty()) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("El comentario no puede estar vacío.");
        }
        Documento documento = gestionarDocumentosGateway.buscarDocumentoId(idDocumento);
        if (documento == null) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el documento con ID: " + idDocumento);
        }
        String actual = documento.getComentario();
        if (actual == null) {
            actual = "";
        }
        String fechaStr = LocalDateTime.now().format(FECHA_COMENTARIO);
        String nuevoBloque;
        if (rolUsuario != null && !rolUsuario.isBlank() || nombreUsuario != null && !nombreUsuario.isBlank()) {
            String rol = rolUsuario != null ? rolUsuario.trim() : "";
            String nombre = nombreUsuario != null ? nombreUsuario.trim() : "";
            nuevoBloque = String.format("[%s] %s - %s: %s", fechaStr, rol, nombre, textoNuevo);
        } else {
            nuevoBloque = String.format("[%s] %s", fechaStr, textoNuevo);
        }
        String resultado = actual.isEmpty() ? nuevoBloque : actual + "\n" + nuevoBloque;
        documento.setComentario(resultado);
        gestionarDocumentosGateway.actualizarDocumento(documento);
    }
}
