package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

public interface FormateadorResultadosIntPort {
    
    void retornarRespuestaErrorEntidadExiste(String mensaje);

    void retornarRespuestaErrorReglaDeNegocio(String mensaje);

}
