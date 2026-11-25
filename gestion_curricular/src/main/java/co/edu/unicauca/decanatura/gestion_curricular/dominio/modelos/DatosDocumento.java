package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

/** Datos ya reunidos para rellenar el documento de Paz y Salvo. */
public record DatosDocumento(String nombreArchivo, String nombreEstudiante, String codigoEstudiante, String programa,
        Object fechaSolicitud, String cedula, String titulo, String director) {}
