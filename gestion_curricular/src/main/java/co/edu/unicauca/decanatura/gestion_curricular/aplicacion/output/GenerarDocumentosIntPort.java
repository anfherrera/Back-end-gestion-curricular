package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.io.IOException;
import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;

public interface GenerarDocumentosIntPort {
    byte[] generarDocumento(DatosGeneracionDocumento datos) throws IOException;
    List<PlantillaDocumento> getTemplates(String proceso);
    byte[] generarPazYSalvo(String numero, String fecha, String observaciones, DatosDocumento datos, boolean pdf) throws IOException;
}
