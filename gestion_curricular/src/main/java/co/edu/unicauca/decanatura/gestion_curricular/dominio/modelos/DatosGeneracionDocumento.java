package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Map;

/** Solicitud de formato y datos consultados por el caso de uso; el generador no hace consultas. */
public record DatosGeneracionDocumento(int idSolicitud, String tipoDocumento, Map<String, Object> datosDocumento,
        Map<String, Object> datosSolicitud, SolicitudHomologacion homologacion, SolicitudReingreso reingreso) {
    public int getIdSolicitud() { return idSolicitud; }
    public String getTipoDocumento() { return tipoDocumento; }
    public Map<String, Object> getDatosDocumento() { return datosDocumento; }
    public Map<String, Object> getDatosSolicitud() { return datosSolicitud; }
}
