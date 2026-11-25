package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.List;

public record PlantillaDocumento(String id, String nombre, String descripcion,
        List<String> camposRequeridos, List<String> camposOpcionales) {}
