package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums;

/**
 * Enum para los tipos de documento de identidad
 *
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
public enum TipoDocumento {

    // Documentos de identidad colombianos
    CC("Cédula de Ciudadanía"),
    TI("Tarjeta de Identidad"),
    CE("Cédula de Extranjería"),
    PA("Pasaporte"),
    RC("Registro Civil"),

    // Documentos adicionales
    NIT("Número de Identificación Tributaria"),
    NUIP("Número Único de Identificación Personal");

    private final String descripcion;

    /**
     * Constructor del enum
     * @param descripcion La descripción completa del documento
     */
    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el código del tipo de documento
     * @return El código (ej: "CC", "TI")
     */
    public String getCodigo() {
        return name();
    }

    /**
     * Obtiene la descripción completa del tipo de documento
     * @return La descripción (ej: "Cédula de Ciudadanía")
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Busca un tipo de documento por su código
     * @param codigo El código a buscar (ej: "CC")
     * @return El enum correspondiente
     * @throws IllegalArgumentException Si el código no es válido
     */
    public static TipoDocumento fromCodigo(String codigo) {
        for (TipoDocumento tipo : values()) {
            if (tipo.name().equals(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de documento no válido: " + codigo);
    }

    /**
     * Verifica si un código es un tipo de documento válido
     * @param codigo El código a verificar
     * @return true si es válido, false en caso contrario
     */
    public static boolean esValido(String codigo) {
        try {
            fromCodigo(codigo);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtiene todos los tipos de documento con su información completa
     * @return Array con objetos que contienen código y descripción
     */
    public static TipoDocumentoInfo[] getTodosLosTipos() {
        TipoDocumento[] valores = values();
        TipoDocumentoInfo[] resultado = new TipoDocumentoInfo[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = new TipoDocumentoInfo(valores[i].getCodigo(), valores[i].getDescripcion());
        }
        return resultado;
    }

    /**
     * Clase interna para representar la información completa de un tipo de documento
     */
    public static class TipoDocumentoInfo {
        private final String codigo;
        private final String descripcion;

        public TipoDocumentoInfo(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
