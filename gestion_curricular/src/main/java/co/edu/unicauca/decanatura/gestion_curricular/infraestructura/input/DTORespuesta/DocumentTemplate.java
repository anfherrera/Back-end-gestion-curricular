package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.List;

public class DocumentTemplate {
    private String id;
    private String nombre;
    private String descripcion;
    private List<String> camposRequeridos;
    private List<String> camposOpcionales;

    // Constructores
    public DocumentTemplate() {}

    public DocumentTemplate(String id, String nombre, String descripcion, 
                           List<String> camposRequeridos, List<String> camposOpcionales) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.camposRequeridos = camposRequeridos;
        this.camposOpcionales = camposOpcionales;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getCamposRequeridos() {
        return camposRequeridos;
    }

    public void setCamposRequeridos(List<String> camposRequeridos) {
        this.camposRequeridos = camposRequeridos;
    }

    public List<String> getCamposOpcionales() {
        return camposOpcionales;
    }

    public void setCamposOpcionales(List<String> camposOpcionales) {
        this.camposOpcionales = camposOpcionales;
    }

    @Override
    public String toString() {
        return "DocumentTemplate{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", camposRequeridos=" + camposRequeridos +
                ", camposOpcionales=" + camposOpcionales +
                '}';
    }
}

