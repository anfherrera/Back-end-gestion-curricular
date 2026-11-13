package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public class GestionarDocumentosCUAdapter implements GestionarDocumentosCUIntPort {

    private static final DateTimeFormatter FECHA_COMENTARIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final GestionarDocumentosGatewayIntPort gestionarDocumentosGateway;
    private final FormateadorResultadosIntPort formateadorResultados;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final GestionarArchivosCUIntPort archivos;

    public GestionarDocumentosCUAdapter(GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
                                        FormateadorResultadosIntPort formateadorResultados,
                                        GestionarArchivosCUIntPort gestionarArchivos) {
        this.gestionarDocumentosGateway = gestionarDocumentosGateway;
        this.formateadorResultados = formateadorResultados;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(gestionarDocumentosGateway, gestionarArchivos);
        this.archivos = gestionarArchivos;
    }

    @Override
    public Documento subirDocumento(ArchivoSubido archivo, Integer idUsuario) {
        String nombre = archivo.nombreOriginal();
        String ruta = archivos.saveFile(archivo, nombre, "pdf");
        return registrarGuardado(nombre, ruta, null, idUsuario, null);
    }

    @Override
    public Documento adjuntarASolicitud(ArchivoSubido archivo, Solicitud solicitud, String carpeta,
            String comentario, boolean usarNombreGuardado) {
        String nombre = archivo.nombreOriginal();
        String ruta = archivos.saveFile(archivo, nombre, "pdf", carpeta, solicitud.getId_solicitud());
        if (usarNombreGuardado) {
            nombre = ruta.contains("/") ? ruta.substring(ruta.lastIndexOf('/') + 1) : ruta;
        }
        return registrarGuardado(nombre, ruta, solicitud, null, comentario);
    }

    private Documento registrarGuardado(String nombre, String ruta, Solicitud solicitud,
            Integer idUsuario, String comentario) {
        Documento documento = new Documento();
        documento.setNombre(nombre);
        documento.setRuta_documento(ruta);
        documento.setFecha_documento(new Date());
        documento.setEsValido(true);
        documento.setObjSolicitud(solicitud);
        documento.setIdUsuarioSubio(idUsuario);
        documento.setComentario(comentario);
        return registrarDocumento(documento);
    }

    @Override
    public Documento registrarDocumento(Documento documento) {
        if (documento == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("El documento no puede ser nulo.");
        }
        return gestionarDocumentosGateway.crearDocumento(documento);
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
