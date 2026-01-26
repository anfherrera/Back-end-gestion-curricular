# PROMPT: Actualizar Placeholders en Generación de Documentos de Reingreso - Backend

## CONTEXTO

Se requiere actualizar la lógica de generación de documentos (resoluciones/oficios) para el módulo de **Reingreso** en el método `crearMapaReemplazos` del servicio `DocumentGeneratorService`. Los cambios deben obtener datos reales de la solicitud de reingreso en lugar de usar valores por defecto o del mapa de datos.

**IMPORTANTE:** Respetar la arquitectura hexagonal del proyecto (puertos y adaptadores).

---

## OBJETIVO

Actualizar el método `crearMapaReemplazos` en `DocumentGeneratorService` para el tipo de documento `RESOLUCION_REINGRESO`, de manera que los siguientes placeholders se reemplacen con datos reales obtenidos de la solicitud de reingreso:

1. **`CEDULA_ESTUDIANTE`**: Usar el atributo `cedula` del usuario asociado a la solicitud
2. **`FECHA_SOLICITUD`**: Usar el atributo `fecha_registro_solicitud` de la solicitud
3. **`DIA_FIRMA`**: Extraer el día del atributo `fecha_registro_solicitud`
4. **`MES_FIRMA`**: Extraer el mes del atributo `fecha_registro_solicitud` (formateado en español)
5. **`AÑO_FIRMA`**: Extraer el año del atributo `fecha_registro_solicitud`

---

## ARCHIVO A MODIFICAR

**Archivo:** `infraestructura/output/formateador/DocumentGeneratorService.java`

**Método:** `crearMapaReemplazos(DocumentRequest request)`

**Ubicación actual:** Líneas 617-620 (aproximadamente)

**Estado actual del código:**
```java
} else if ("RESOLUCION_REINGRESO".equals(request.getTipoDocumento())) {
    replacements.put("TIPO_PROCESO", "reingreso al programa");
    replacements.put("TITULO_DOCUMENTO", "RESOLUCIÓN DE REINGRESO");
}
```

---

## CAMBIOS REQUERIDOS

### 1. Inyectar Dependencia Necesaria

**En la clase `DocumentGeneratorService`:**

Ya existe la inyección de `GestionarSolicitudHomologacionCUIntPort` para homologación. Se debe agregar la inyección de `GestionarSolicitudReingresoCUIntPort` para reingreso.

**Código a agregar:**
```java
@Service
@RequiredArgsConstructor
public class DocumentGeneratorService {
    private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
    private final GestionarSolicitudReingresoCUIntPort solicitudReingresoCU; // ✅ AGREGAR
    // ... resto del código
}
```

**Import necesario:**
```java
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
```

---

### 2. Actualizar el Bloque de RESOLUCION_REINGRESO

**Reemplazar el bloque actual (líneas 617-620) con:**

```java
} else if ("RESOLUCION_REINGRESO".equals(request.getTipoDocumento())) {
    replacements.put("TIPO_PROCESO", "reingreso al programa");
    replacements.put("TITULO_DOCUMENTO", "RESOLUCIÓN DE REINGRESO");
    
    // Obtener la solicitud completa desde la base de datos para acceder a los datos reales
    SolicitudReingreso solicitudReingreso = null;
    try {
        if (request.getIdSolicitud() > 0) {
            solicitudReingreso = solicitudReingresoCU.obtenerSolicitudReingresoPorId(request.getIdSolicitud());
        }
    } catch (Exception e) {
        // Si no se puede obtener la solicitud, continuar con valores del mapa como fallback
        log.warn("No se pudo obtener la solicitud de reingreso con ID: " + request.getIdSolicitud(), e);
    }
    
    // Cédula del estudiante: obtener desde el usuario asociado a la solicitud
    String cedulaEstudiante = "No especificada";
    if (solicitudReingreso != null && solicitudReingreso.getObjUsuario() != null 
        && solicitudReingreso.getObjUsuario().getCedula() != null) {
        cedulaEstudiante = solicitudReingreso.getObjUsuario().getCedula();
    } else {
        // Fallback: intentar obtener del mapa de datos
        Object cedulaObj = datosSolicitud.get("cedulaEstudiante");
        if (cedulaObj != null) {
            cedulaEstudiante = cedulaObj.toString();
        }
    }
    replacements.put("CEDULA_ESTUDIANTE", cedulaEstudiante);
    
    // Fecha de solicitud: obtener desde fecha_registro_solicitud de la solicitud
    String fechaSolicitud = "";
    LocalDate fechaRegistroLocal = null;
    
    if (solicitudReingreso != null && solicitudReingreso.getFecha_registro_solicitud() != null) {
        // Convertir java.util.Date a LocalDate
        java.util.Date fechaRegistro = solicitudReingreso.getFecha_registro_solicitud();
        fechaRegistroLocal = fechaRegistro.toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();
        fechaSolicitud = formatearFecha(fechaRegistroLocal);
    } else {
        // Fallback: intentar obtener del mapa de datos
        Object fechaSolicitudObj = datosSolicitud.get("fechaSolicitud");
        if (fechaSolicitudObj != null) {
            fechaSolicitud = formatearFecha(fechaSolicitudObj);
        }
    }
    replacements.put("FECHA_SOLICITUD", fechaSolicitud);
    
    // Fechas de firma: extraer día, mes y año de fecha_registro_solicitud
    if (fechaRegistroLocal != null) {
        // Día de firma (formato completo: "23 de enero de 2025")
        replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaRegistroLocal));
        
        // Día numérico (solo el número del día)
        replacements.put("DIA_NUMERO", String.valueOf(fechaRegistroLocal.getDayOfMonth()));
        
        // Mes de firma (nombre del mes en español)
        replacements.put("MES_FIRMA", formatearMes(fechaRegistroLocal.getMonthValue()));
        
        // Año de firma (año en formato texto)
        replacements.put("AÑO_FIRMA", formatearAño(fechaRegistroLocal.getYear()));
    } else {
        // Fallback: usar fecha actual si no se puede obtener de la solicitud
        LocalDate fechaActual = LocalDate.now();
        replacements.put("DIA_FIRMA", formatearFechaCompleta(fechaActual));
        replacements.put("DIA_NUMERO", String.valueOf(fechaActual.getDayOfMonth()));
        replacements.put("MES_FIRMA", formatearMes(fechaActual.getMonthValue()));
        replacements.put("AÑO_FIRMA", formatearAño(fechaActual.getYear()));
    }
}
```

---

## PLACEHOLDERS EN LA PLANTILLA

La plantilla `templates/resolucion-reingreso.docx` contiene los siguientes placeholders que deben ser reemplazados:

### Placeholders a Actualizar:

1. **`[CEDULA_ESTUDIANTE]`**
   - **Ubicación en plantilla:** Líneas 16 y 24
   - **Valor:** Cédula del usuario asociado a la solicitud (`objUsuario.cedula`)
   - **Formato:** String (ej: "1234567890")

2. **`[FECHA_SOLICITUD]`**
   - **Ubicación en plantilla:** Línea 16
   - **Valor:** Fecha de registro de la solicitud (`fecha_registro_solicitud`)
   - **Formato:** Fecha formateada en español (ej: "23 de enero de 2025")

3. **`[DIA_FIRMA]`**
   - **Ubicación en plantilla:** Línea 43
   - **Valor:** Fecha completa formateada extraída de `fecha_registro_solicitud`
   - **Formato:** "23 de enero de 2025" (usar método `formatearFechaCompleta`)

4. **`[DIA_NUMERO]`**
   - **Ubicación en plantilla:** Línea 43
   - **Valor:** Día numérico extraído de `fecha_registro_solicitud`
   - **Formato:** String del número del día (ej: "23")

5. **`[MES_FIRMA]`**
   - **Ubicación en plantilla:** Línea 43
   - **Valor:** Mes en español extraído de `fecha_registro_solicitud`
   - **Formato:** Nombre del mes en español (ej: "enero", "febrero", etc.) usando método `formatearMes`

6. **`[AÑO_FIRMA]`**
   - **Ubicación en plantilla:** Línea 43
   - **Valor:** Año extraído de `fecha_registro_solicitud`
   - **Formato:** Año en formato texto (ej: "2025") usando método `formatearAño`

---

## MÉTODOS DE FORMATEO DISPONIBLES

El `DocumentGeneratorService` ya tiene los siguientes métodos de formateo que deben usarse:

1. **`formatearFecha(Object fecha)`**: Formatea una fecha a "dd 'de' MMMM 'de' yyyy" (ej: "23 de enero de 2025")
2. **`formatearFechaCompleta(LocalDate fecha)`**: Formatea una LocalDate a "dd 'de' MMMM 'de' yyyy"
3. **`formatearMes(int mes)`**: Convierte un número de mes (1-12) a su nombre en español
4. **`formatearAño(int año)`**: Convierte un año a String

**Ejemplo de uso:**
```java
LocalDate fecha = LocalDate.of(2025, 1, 23);
String fechaCompleta = formatearFechaCompleta(fecha); // "23 de enero de 2025"
String mes = formatearMes(1); // "enero"
String año = formatearAño(2025); // "2025"
```

---

## ESTRUCTURA DE LA SOLICITUD

La entidad `SolicitudReingreso` tiene la siguiente estructura relevante:

```java
public class SolicitudReingreso extends Solicitud {
    // Hereda de Solicitud:
    // - fecha_registro_solicitud (java.util.Date)
    // - objUsuario (Usuario)
    //   - cedula (String)
    // ... otros campos
}
```

---

## CASO DE USO A USAR

**Interfaz:** `GestionarSolicitudReingresoCUIntPort`

**Método:** `obtenerSolicitudReingresoPorId(Integer id)`

**Retorna:** `SolicitudReingreso`

**Ubicación:** `aplicacion/input/GestionarSolicitudReingresoCUIntPort.java`

---

## CONVERSIÓN DE FECHAS

**Importante:** El atributo `fecha_registro_solicitud` es de tipo `java.util.Date`. Para extraer día, mes y año, se debe convertir a `LocalDate`:

```java
java.util.Date fechaRegistro = solicitudReingreso.getFecha_registro_solicitud();
LocalDate fechaRegistroLocal = fechaRegistro.toInstant()
    .atZone(java.time.ZoneId.systemDefault())
    .toLocalDate();

int dia = fechaRegistroLocal.getDayOfMonth();
int mes = fechaRegistroLocal.getMonthValue();
int año = fechaRegistroLocal.getYear();
```

---

## ESTRATEGIA DE FALLBACK

Si no se puede obtener la solicitud completa desde la base de datos, se debe usar una estrategia de fallback:

1. **Para CEDULA_ESTUDIANTE:**
   - Primero: Intentar obtener desde `solicitudReingreso.getObjUsuario().getCedula()`
   - Fallback: Intentar obtener desde `datosSolicitud.get("cedulaEstudiante")`
   - Último recurso: Usar "No especificada"

2. **Para FECHA_SOLICITUD:**
   - Primero: Usar `solicitudReingreso.getFecha_registro_solicitud()`
   - Fallback: Intentar obtener desde `datosSolicitud.get("fechaSolicitud")`
   - Último recurso: String vacío

3. **Para DIA_FIRMA, MES_FIRMA, AÑO_FIRMA:**
   - Primero: Extraer de `fecha_registro_solicitud` de la solicitud
   - Fallback: Usar fecha actual (`LocalDate.now()`)

---

## CONSIDERACIONES IMPORTANTES

1. **Arquitectura Hexagonal:** 
   - Usar el puerto `GestionarSolicitudReingresoCUIntPort` (capa de aplicación)
   - NO acceder directamente a repositorios o gateways desde el servicio

2. **Manejo de Errores:**
   - Si no se puede obtener la solicitud, usar valores de fallback
   - Registrar advertencias en el log si es necesario
   - NO lanzar excepciones que interrumpan la generación del documento

3. **Compatibilidad:**
   - Mantener compatibilidad con el código existente
   - Si el `DocumentRequest` no tiene `idSolicitud` válido, usar valores del mapa de datos

4. **Formateo de Fechas:**
   - Usar los métodos de formateo existentes (`formatearFecha`, `formatearFechaCompleta`, `formatearMes`, `formatearAño`)
   - Asegurar que las fechas se formateen correctamente en español

5. **Logging:**
   - Agregar logging si es necesario para depuración
   - Usar `@Slf4j` si está disponible o `Logger` estándar

---

## RESULTADO ESPERADO

Al finalizar, cuando se genere un documento de tipo `RESOLUCION_REINGRESO`:

- ✅ El placeholder `[CEDULA_ESTUDIANTE]` se reemplazará con la cédula real del usuario asociado a la solicitud
- ✅ El placeholder `[FECHA_SOLICITUD]` se reemplazará con la fecha de registro de la solicitud formateada
- ✅ El placeholder `[DIA_FIRMA]` se reemplazará con la fecha completa formateada extraída de `fecha_registro_solicitud`
- ✅ El placeholder `[DIA_NUMERO]` se reemplazará con el día numérico extraído de `fecha_registro_solicitud`
- ✅ El placeholder `[MES_FIRMA]` se reemplazará con el mes en español extraído de `fecha_registro_solicitud`
- ✅ El placeholder `[AÑO_FIRMA]` se reemplazará con el año extraído de `fecha_registro_solicitud`
- ✅ Se mantendrá la estrategia de fallback si no se puede obtener la solicitud completa
- ✅ Se respetará la arquitectura hexagonal del proyecto

---

## NOTAS ADICIONALES

- **No modificar** otros tipos de documentos (Homologación, Paz y Salvo)
- **No modificar** métodos de formateo existentes a menos que sea estrictamente necesario
- Si se requiere agregar logging, usar `@Slf4j` si está disponible en la clase
- Verificar que el método `obtenerSolicitudReingresoPorId` esté disponible en `GestionarSolicitudReingresoCUIntPort`

---

## REFERENCIA: IMPLEMENTACIÓN SIMILAR

Para referencia, ver cómo se implementó para Homologación (líneas 448-533 aproximadamente), donde se obtiene la solicitud completa y se extraen datos del usuario y de la solicitud.

**Diferencia clave:** En reingreso, se debe extraer día, mes y año de `fecha_registro_solicitud` para los placeholders de firma, mientras que en homologación se usa la fecha actual para la firma.

---

**IMPORTANTE:** Asegurar que todos los cambios respeten la arquitectura hexagonal y no rompan la funcionalidad existente de otros tipos de documentos.
