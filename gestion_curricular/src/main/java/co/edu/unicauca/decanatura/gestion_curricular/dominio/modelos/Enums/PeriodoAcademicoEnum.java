package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums;

/**
 * Enum para los períodos académicos disponibles
 * Genera períodos desde 2020 en adelante con formato YYYY-P
 * donde P es 1 o 2 (primer o segundo período del año)
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
public enum PeriodoAcademicoEnum {
    
    // 2020
    PERIODO_2020_1("2020-1"),
    PERIODO_2020_2("2020-2"),
    
    // 2021
    PERIODO_2021_1("2021-1"),
    PERIODO_2021_2("2021-2"),
    
    // 2022
    PERIODO_2022_1("2022-1"),
    PERIODO_2022_2("2022-2"),
    
    // 2023
    PERIODO_2023_1("2023-1"),
    PERIODO_2023_2("2023-2"),
    
    // 2024
    PERIODO_2024_1("2024-1"),
    PERIODO_2024_2("2024-2"),
    
    // 2025
    PERIODO_2025_1("2025-1"),
    PERIODO_2025_2("2025-2"),
    
    // 2026
    PERIODO_2026_1("2026-1"),
    PERIODO_2026_2("2026-2"),
    
    // 2027
    PERIODO_2027_1("2027-1"),
    PERIODO_2027_2("2027-2"),
    
    // 2028
    PERIODO_2028_1("2028-1"),
    PERIODO_2028_2("2028-2"),
    
    // 2029
    PERIODO_2029_1("2029-1"),
    PERIODO_2029_2("2029-2"),
    
    // 2030
    PERIODO_2030_1("2030-1"),
    PERIODO_2030_2("2030-2");
    
    private final String valor;
    
    /**
     * Constructor del enum
     * @param valor El valor del período académico (ej: "2024-2")
     */
    PeriodoAcademicoEnum(String valor) {
        this.valor = valor;
    }
    
    /**
     * Obtiene el valor del período académico
     * @return El valor del período (ej: "2024-2")
     */
    public String getValor() {
        return valor;
    }
    
    /**
     * Obtiene el año del período académico
     * @return El año como entero (ej: 2024)
     */
    public int getAño() {
        return Integer.parseInt(valor.split("-")[0]);
    }
    
    /**
     * Obtiene el número del período (1 o 2)
     * @return El número del período (1 o 2)
     */
    public int getNumeroPeriodo() {
        return Integer.parseInt(valor.split("-")[1]);
    }
    
    /**
     * Obtiene el nombre completo del período
     * @return Descripción completa (ej: "Primer Período 2024")
     */
    public String getDescripcion() {
        int año = getAño();
        int numero = getNumeroPeriodo();
        String periodo = (numero == 1) ? "Primer Período" : "Segundo Período";
        return periodo + " " + año;
    }
    
    /**
     * Busca un período académico por su valor
     * @param valor El valor a buscar (ej: "2024-2")
     * @return El enum correspondiente
     * @throws IllegalArgumentException Si el valor no es válido
     */
    public static PeriodoAcademicoEnum fromValor(String valor) {
        for (PeriodoAcademicoEnum periodo : values()) {
            if (periodo.valor.equals(valor)) {
                return periodo;
            }
        }
        throw new IllegalArgumentException("Período académico no válido: " + valor);
    }
    
    /**
     * Verifica si un valor es un período académico válido
     * @param valor El valor a verificar
     * @return true si es válido, false en caso contrario
     */
    public static boolean esValido(String valor) {
        try {
            fromValor(valor);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Obtiene todos los períodos académicos como array de strings
     * @return Array con todos los valores de períodos
     */
    public static String[] getTodosLosValores() {
        PeriodoAcademicoEnum[] valores = values();
        String[] resultado = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = valores[i].getValor();
        }
        return resultado;
    }
    
    /**
     * Obtiene los períodos académicos futuros (año actual en adelante)
     * @return Array con los períodos futuros
     */
    public static String[] getPeriodosFuturos() {
        int añoActual = java.time.Year.now().getValue();
        return java.util.Arrays.stream(values())
                .filter(periodo -> periodo.getAño() >= añoActual)
                .map(PeriodoAcademicoEnum::getValor)
                .toArray(String[]::new);
    }
    
    /**
     * Obtiene los períodos académicos recientes (últimos 5 años)
     * @return Array con los períodos recientes
     */
    public static String[] getPeriodosRecientes() {
        int añoActual = java.time.Year.now().getValue();
        int añoInicio = Math.max(2020, añoActual - 4);
        return java.util.Arrays.stream(values())
                .filter(periodo -> periodo.getAño() >= añoInicio)
                .map(PeriodoAcademicoEnum::getValor)
                .toArray(String[]::new);
    }
    
    /**
     * Obtiene el período académico actual basado en la fecha
     * @return El período académico actual o null si no se puede determinar
     */
    public static PeriodoAcademicoEnum getPeriodoActual() {
        java.time.LocalDate ahora = java.time.LocalDate.now();
        int año = ahora.getYear();
        int mes = ahora.getMonthValue();
        
        // Lógica para determinar el período basado en el mes
        // Asumiendo que el primer período va de enero a junio
        // y el segundo período va de julio a diciembre
        int numeroPeriodo = (mes <= 6) ? 1 : 2;
        
        String valor = año + "-" + numeroPeriodo;
        try {
            return fromValor(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return valor;
    }
}

