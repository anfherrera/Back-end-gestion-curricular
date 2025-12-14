package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums;

/**
 * Enum para los períodos académicos disponibles
 * Genera períodos desde 2020 en adelante con formato YYYY-P
 * donde P es 1 o 2 (primer o segundo período del año)
 * 
 * Fechas del calendario académico (según Acuerdo Académico 030 de 2024):
 * - Período 1: 7 de enero - 23 de junio
 * - Período 2: 14 de julio - 24 de diciembre
 * 
 * @author Sistema de Gestión Curricular
 * @version 2.0 - Actualizado con fechas específicas del calendario académico
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
     * Usa fechas específicas del calendario académico según el Acuerdo Académico 030 de 2024:
     * - Período 1: 7 de enero - 23 de junio
     * - Período 2: 14 de julio - 24 de diciembre
     * 
     * @return El período académico actual o null si no se puede determinar
     */
    public static PeriodoAcademicoEnum getPeriodoActual() {
        java.time.LocalDate ahora = java.time.LocalDate.now();
        int año = ahora.getYear();
        
        // Fechas específicas del calendario académico
        java.time.LocalDate inicioPeriodo1 = java.time.LocalDate.of(año, 1, 7);
        java.time.LocalDate finPeriodo1 = java.time.LocalDate.of(año, 6, 23);
        java.time.LocalDate inicioPeriodo2 = java.time.LocalDate.of(año, 7, 14);
        java.time.LocalDate finPeriodo2 = java.time.LocalDate.of(año, 12, 24);
        
        int numeroPeriodo;
        
        // Verificar si estamos en el período 1
        if (!ahora.isBefore(inicioPeriodo1) && !ahora.isAfter(finPeriodo1)) {
            numeroPeriodo = 1;
        }
        // Verificar si estamos en el período 2
        else if (!ahora.isBefore(inicioPeriodo2) && !ahora.isAfter(finPeriodo2)) {
            numeroPeriodo = 2;
        }
        // Si estamos entre períodos o fuera del año académico
        else {
            // Si estamos entre fin del período 1 y inicio del período 2 (24 junio - 13 julio)
            if (ahora.isAfter(finPeriodo1) && ahora.isBefore(inicioPeriodo2)) {
                // Retornar el período más cercano (período 2 que está por comenzar)
                numeroPeriodo = 2;
            }
            // Si estamos antes del inicio del período 1 (1-6 enero)
            else if (ahora.isBefore(inicioPeriodo1)) {
                // Retornar el período 1 del año actual
                numeroPeriodo = 1;
            }
            // Si estamos después del fin del período 2 (25-31 diciembre)
            else if (ahora.isAfter(finPeriodo2)) {
                // Retornar el período 1 del año siguiente
                año = año + 1;
                numeroPeriodo = 1;
            }
            // Caso por defecto (no debería llegar aquí)
            else {
                numeroPeriodo = (ahora.getMonthValue() <= 6) ? 1 : 2;
            }
        }
        
        String valor = año + "-" + numeroPeriodo;
        try {
            return fromValor(valor);
        } catch (IllegalArgumentException e) {
            // Si el período no existe en el enum, intentar con el año anterior o siguiente
            // Esto puede pasar si estamos en un año muy futuro o pasado
            if (año > 2030) {
                // Si estamos después de 2030, usar el último período disponible
                return PERIODO_2030_2;
            } else if (año < 2020) {
                // Si estamos antes de 2020, usar el primer período disponible
                return PERIODO_2020_1;
            }
            return null;
        }
    }
    
    /**
     * Obtiene el período académico para una fecha específica
     * @param fecha La fecha para la cual se quiere obtener el período
     * @return El período académico correspondiente a la fecha o null si no se puede determinar
     */
    public static PeriodoAcademicoEnum getPeriodoParaFecha(java.time.LocalDate fecha) {
        if (fecha == null) {
            return null;
        }
        
        int año = fecha.getYear();
        
        // Fechas específicas del calendario académico
        java.time.LocalDate inicioPeriodo1 = java.time.LocalDate.of(año, 1, 7);
        java.time.LocalDate finPeriodo1 = java.time.LocalDate.of(año, 6, 23);
        java.time.LocalDate inicioPeriodo2 = java.time.LocalDate.of(año, 7, 14);
        java.time.LocalDate finPeriodo2 = java.time.LocalDate.of(año, 12, 24);
        
        int numeroPeriodo;
        
        // Verificar si estamos en el período 1
        if (!fecha.isBefore(inicioPeriodo1) && !fecha.isAfter(finPeriodo1)) {
            numeroPeriodo = 1;
        }
        // Verificar si estamos en el período 2
        else if (!fecha.isBefore(inicioPeriodo2) && !fecha.isAfter(finPeriodo2)) {
            numeroPeriodo = 2;
        }
        // Si estamos entre períodos o fuera del año académico
        else {
            // Si estamos entre fin del período 1 y inicio del período 2 (24 junio - 13 julio)
            if (fecha.isAfter(finPeriodo1) && fecha.isBefore(inicioPeriodo2)) {
                // Retornar el período más cercano (período 2 que está por comenzar)
                numeroPeriodo = 2;
            }
            // Si estamos antes del inicio del período 1 (1-6 enero)
            else if (fecha.isBefore(inicioPeriodo1)) {
                // Retornar el período 1 del año actual
                numeroPeriodo = 1;
            }
            // Si estamos después del fin del período 2 (25-31 diciembre)
            else if (fecha.isAfter(finPeriodo2)) {
                // Retornar el período 1 del año siguiente
                año = año + 1;
                numeroPeriodo = 1;
            }
            // Caso por defecto
            else {
                numeroPeriodo = (fecha.getMonthValue() <= 6) ? 1 : 2;
            }
        }
        
        String valor = año + "-" + numeroPeriodo;
        try {
            return fromValor(valor);
        } catch (IllegalArgumentException e) {
            if (año > 2030) {
                return PERIODO_2030_2;
            } else if (año < 2020) {
                return PERIODO_2020_1;
            }
            return null;
        }
    }
    
    /**
     * Verifica si una fecha está dentro de este período académico
     * @param fecha La fecha a verificar
     * @return true si la fecha está dentro del período, false en caso contrario
     */
    public boolean contieneFecha(java.time.LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        
        int año = getAño();
        int numeroPeriodo = getNumeroPeriodo();
        
        // Fechas específicas del calendario académico
        java.time.LocalDate inicioPeriodo1 = java.time.LocalDate.of(año, 1, 7);
        java.time.LocalDate finPeriodo1 = java.time.LocalDate.of(año, 6, 23);
        java.time.LocalDate inicioPeriodo2 = java.time.LocalDate.of(año, 7, 14);
        java.time.LocalDate finPeriodo2 = java.time.LocalDate.of(año, 12, 24);
        
        if (numeroPeriodo == 1) {
            return !fecha.isBefore(inicioPeriodo1) && !fecha.isAfter(finPeriodo1);
        } else {
            return !fecha.isBefore(inicioPeriodo2) && !fecha.isAfter(finPeriodo2);
        }
    }
    
    /**
     * Obtiene la fecha de inicio del período académico
     * @return La fecha de inicio del período
     */
    public java.time.LocalDate getFechaInicio() {
        int año = getAño();
        int numeroPeriodo = getNumeroPeriodo();
        
        if (numeroPeriodo == 1) {
            return java.time.LocalDate.of(año, 1, 7);
        } else {
            return java.time.LocalDate.of(año, 7, 14);
        }
    }
    
    /**
     * Obtiene la fecha de fin del período académico
     * @return La fecha de fin del período
     */
    public java.time.LocalDate getFechaFin() {
        int año = getAño();
        int numeroPeriodo = getNumeroPeriodo();
        
        if (numeroPeriodo == 1) {
            return java.time.LocalDate.of(año, 6, 23);
        } else {
            return java.time.LocalDate.of(año, 12, 24);
        }
    }
    
    @Override
    public String toString() {
        return valor;
    }
}

