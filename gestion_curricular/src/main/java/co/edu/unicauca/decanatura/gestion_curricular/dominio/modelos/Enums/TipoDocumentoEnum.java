package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums;

/**
 * Enum para los tipos de documento de identidad
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
public enum TipoDocumentoEnum {
    
    // Documentos de identidad colombianos
    CEDULA_CIUDADANIA("CC", "Cédula de Ciudadanía"),
    TARJETA_IDENTIDAD("TI", "Tarjeta de Identidad"),
    CEDULA_EXTRANJERIA("CE", "Cédula de Extranjería"),
    PASAPORTE("PA", "Pasaporte"),
    REGISTRO_CIVIL("RC", "Registro Civil"),
    
    // Documentos adicionales
    NIT("NIT", "Número de Identificación Tributaria"),
    NUIP("NUIP", "Número Único de Identificación Personal");
    
    private final String codigo;
    private final String descripcion;
    
    /**
     * Constructor del enum
     * @param codigo El código del tipo de documento (ej: "CC", "TI")
     * @param descripcion La descripción completa del documento
     */
    TipoDocumentoEnum(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene el código del tipo de documento
     * @return El código (ej: "CC", "TI")
     */
    public String getCodigo() {
        return codigo;
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
    public static TipoDocumentoEnum fromCodigo(String codigo) {
        for (TipoDocumentoEnum tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
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
     * Obtiene todos los códigos de tipos de documento como array de strings
     * @return Array con todos los códigos
     */
    public static String[] getTodosLosCodigos() {
        TipoDocumentoEnum[] valores = values();
        String[] resultado = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = valores[i].getCodigo();
        }
        return resultado;
    }
    
    /**
     * Obtiene todos los tipos de documento con su información completa
     * @return Array con objetos que contienen código y descripción
     */
    public static TipoDocumentoInfo[] getTodosLosTipos() {
        TipoDocumentoEnum[] valores = values();
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
        return codigo;
    }
}

