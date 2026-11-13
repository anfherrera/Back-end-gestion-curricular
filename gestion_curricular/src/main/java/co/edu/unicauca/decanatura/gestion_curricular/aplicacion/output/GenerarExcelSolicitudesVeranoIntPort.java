package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;
import java.util.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
public interface GenerarExcelSolicitudesVeranoIntPort {
    byte[] generarExcelSolicitudes(List<Solicitud> solicitudes, Integer curso, String estado, String periodo, Date inicio, Date fin);
}
