package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Map;

public class DocumentRequest {
    private int idSolicitud;
    private String tipoDocumento;
    private Map<String, Object> datosDocumento;
    private Map<String, Object> datosSolicitud;

    // Constructores
    public DocumentRequest() {}

    public DocumentRequest(int idSolicitud, String tipoDocumento, 
                          Map<String, Object> datosDocumento, 
                          Map<String, Object> datosSolicitud) {
        this.idSolicitud = idSolicitud;
        this.tipoDocumento = tipoDocumento;
        this.datosDocumento = datosDocumento;
        this.datosSolicitud = datosSolicitud;
    }

    // Getters y Setters
    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Map<String, Object> getDatosDocumento() {
        return datosDocumento;
    }

    public void setDatosDocumento(Map<String, Object> datosDocumento) {
        this.datosDocumento = datosDocumento;
    }

    public Map<String, Object> getDatosSolicitud() {
        return datosSolicitud;
    }

    public void setDatosSolicitud(Map<String, Object> datosSolicitud) {
        this.datosSolicitud = datosSolicitud;
    }

    @Override
    public String toString() {
        return "DocumentRequest{" +
                "idSolicitud=" + idSolicitud +
                ", tipoDocumento='" + tipoDocumento + '\'' +
                ", datosDocumento=" + datosDocumento +
                ", datosSolicitud=" + datosSolicitud +
                '}';
    }
}
