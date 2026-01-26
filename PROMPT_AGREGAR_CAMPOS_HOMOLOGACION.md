# PROMPT: Agregar Campos `programa_origen` y `programa_destino` a SolicitudHomologacion

## CONTEXTO DEL PROYECTO

Este es un proyecto Spring Boot con arquitectura hexagonal (puertos y adaptadores) para la gestión curricular de una universidad. El proyecto incluye el proceso de **Homologación** que permite a los estudiantes solicitar la homologación de asignaturas cursadas en otros programas.

Actualmente, el sistema genera oficios/resoluciones de homologación usando plantillas Word (.docx) que contienen placeholders que se reemplazan con datos de la solicitud.

---

## OBJETIVO

Agregar dos nuevos atributos a la entidad `SolicitudHomologacion` para almacenar información sobre los programas de origen y destino de la homologación, y actualizar la lógica de generación de documentos para usar estos datos en la plantilla.

---

## CAMBIOS REQUERIDOS

### 1. Nuevos Atributos a Agregar

**En la entidad `SolicitudHomologacionEntity`:**
- `programa_origen` (String) - Programa académico de origen (donde el estudiante cursó las asignaturas)
- `programa_destino` (String) - Programa académico de destino (donde se homologarán las asignaturas)

**Especificaciones:**
- Ambos campos deben ser `nullable = true` (opcionales, para compatibilidad con datos existentes)
- Longitud recomendada: `length = 200` (para nombres completos de programas)
- Tipo: `String`

---

## ESTRUCTURA DEL PROYECTO A ACTUALIZAR

### 1. Entidad de Persistencia

**Archivo:** `infraestructura/output/persistencia/entidades/SolicitudHomologacionEntity.java`

**Estado actual:**
```java
@Entity
@Table(name = "Solicitudes_Homogolacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacionEntity extends SolicitudEntity {
    @Column(nullable = true)
    private String ruta_PM_FO_4_FOR_27;

    @Column(nullable = true)
    private String ruta_contenido_programatico;
}
```

**Cambio requerido:**
- Agregar los dos nuevos campos con anotaciones `@Column(nullable = true, length = 200)`

---

### 2. Modelo de Dominio

**Archivo:** `dominio/modelos/SolicitudHomologacion.java`

**Estado actual:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacion extends Solicitud {
    private String ruta_PM_FO_4_FOR_27;
    private String ruta_contenido_programatico;
}
```

**Cambio requerido:**
- Agregar los dos nuevos campos `programa_origen` y `programa_destino`

---

### 3. DTOs de Petición

**Archivo:** `infraestructura/input/DTOPeticion/SolicitudHomologacionDTOPeticion.java`

**Cambio requerido:**
- Agregar los campos `programa_origen` y `programa_destino` como opcionales
- Agregar validaciones opcionales:
  - `@Size(max = 200)` para ambos campos
  - Considerar `@NotBlank` si se requiere que sean obligatorios en el futuro

**Ejemplo:**
```java
@Size(max = 200, message = "{SolicitudHomologacion.programa_origen.length}")
private String programa_origen;

@Size(max = 200, message = "{SolicitudHomologacion.programa_destino.length}")
private String programa_destino;
```

---

### 4. DTOs de Respuesta

**Archivo:** `infraestructura/input/DTORespuesta/SolicitudHomologacionDTORespuesta.java`

**Cambio requerido:**
- Agregar los campos `programa_origen` y `programa_destino` para que se retornen en las respuestas

---

### 5. Mappers MapStruct

**Archivo:** `infraestructura/input/mappers/SolicitudHomologacioneMapperDominio.java`

**Cambio requerido:**
- Verificar que los mapeos incluyan los nuevos campos automáticamente (MapStruct los mapea por nombre si coinciden)
- Si hay mapeos explícitos con `@Mapping`, agregar los nuevos campos o verificar que no se ignoren

**Estado actual relevante:**
```java
@Mapping(target = "ruta_PM_FO_4_FOR_27", ignore = true) // Propiedad no mapeada
@Mapping(target = "ruta_contenido_programatico", ignore = true) // Propiedad no mapeada
```

**Nota:** Los nuevos campos NO deben ignorarse, deben mapearse automáticamente.

---

### 6. Servicio de Generación de Documentos

**Archivo:** `infraestructura/output/formateador/DocumentGeneratorService.java`

**Método a actualizar:** `crearMapaReemplazos(DocumentRequest request)`

**Ubicación actual del código:**
Líneas 478-480:
```java
// Programas (no disponibles en los datos, usar placeholders)
replacements.put("PROGRAMA_DESTINO", "------");
replacements.put("PROGRAMA_ORIGEN", "------");
```

**Cambio requerido:**
- Reemplazar los valores por defecto "------" con los datos reales de la solicitud
- Obtener los valores de `datosSolicitud.get("programaOrigen")` y `datosSolicitud.get("programaDestino")`
- Si no están disponibles, usar valores por defecto o lanzar excepción según la lógica de negocio

**Código sugerido:**
```java
// Obtener programas de origen y destino desde los datos de la solicitud
String programaOrigen = datosSolicitud.getOrDefault("programaOrigen", "").toString();
String programaDestino = datosSolicitud.getOrDefault("programaDestino", "").toString();

// Si están vacíos, intentar obtenerlos de la solicitud de homologación directamente
if (programaOrigen.isEmpty() || programaDestino.isEmpty()) {
    // Aquí se podría obtener la solicitud completa desde el request si está disponible
    // o desde la base de datos usando el ID de la solicitud
}

replacements.put("PROGRAMA_DESTINO", programaDestino.isEmpty() ? "------" : programaDestino);
replacements.put("PROGRAMA_ORIGEN", programaOrigen.isEmpty() ? "------" : programaOrigen);
```

**IMPORTANTE:** También actualizar el placeholder `[CEDULA_ESTUDIANTE]` para que use el atributo `cedula` del usuario asociado a la solicitud. Actualmente está en la línea 444:
```java
replacements.put("CEDULA_ESTUDIANTE", datosSolicitud.getOrDefault("cedulaEstudiante", "No especificada").toString());
```

**Cambio requerido para CEDULA_ESTUDIANTE:**
- Asegurar que se obtenga la cédula del usuario (`objUsuario.cedula`) de la solicitud
- Si la solicitud tiene un objeto Usuario asociado, obtener la cédula desde ahí
- Si no está disponible en `datosSolicitud`, buscar en la solicitud completa

---

### 7. Controlador REST (si aplica)

**Archivo:** `infraestructura/input/controladores/SolicitudHomologacionRestController.java`

**Cambio requerido:**
- Verificar que los endpoints que generan documentos pasen los nuevos campos en el `DocumentRequest`
- Asegurar que cuando se construye el `DocumentRequest`, se incluyan `programa_origen` y `programa_destino` en el mapa `datosSolicitud`

**Búsqueda necesaria:**
- Buscar métodos que llamen a `DocumentGeneratorService` o que construyan `DocumentRequest`
- Asegurar que se pasen los nuevos campos desde la solicitud de homologación

---

### 8. Scripts SQL (Opcional)

**Archivo:** `src/main/resources/import.sql` y `src/test/resources/test-data.sql`

**Cambio requerido:**
- Si hay INSERTs directos en la tabla `Solicitudes_Homogolacion`, agregar los nuevos campos (pueden ser NULL para datos existentes)
- Ejemplo:
```sql
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico, programa_origen, programa_destino) 
VALUES (10, '/docs/homologacion/roberto_silva_pm_fo4.pdf', '/docs/homologacion/roberto_silva_contenido.pdf', 'Ingeniería de Sistemas', 'Ingeniería de Sistemas');
```

**Nota:** Si se usa `ddl-auto=create` o `update`, Hibernate creará las columnas automáticamente.

---

### 9. Tests (Opcional pero recomendado)

**Archivos a actualizar:**
- Tests de integración de Homologación
- Tests funcionales de generación de documentos
- Tests unitarios del `DocumentGeneratorService`

**Cambio requerido:**
- Agregar valores de prueba para `programa_origen` y `programa_destino`
- Verificar que los placeholders se reemplacen correctamente en los tests de generación de documentos

---

## PLACEHOLDERS EN PLANTILLA

La plantilla `templates/oficio-homologacion-template.txt` (y el .docx correspondiente) contiene los siguientes placeholders que deben ser reemplazados:

- `[PROGRAMA_ORIGEN]` - Debe usar el valor de `programa_origen` de la solicitud
- `[PROGRAMA_DESTINO]` - Debe usar el valor de `programa_destino` de la solicitud
- `[CEDULA_ESTUDIANTE]` - Debe usar el valor de `cedula` del usuario asociado a la solicitud

**Ubicaciones en la plantilla:**
- Línea 15: `cursó y aprobó en el Programa de [PROGRAMA_ORIGEN]`
- Línea 15: `solicita le sea(n) homologada(s) las asignaturas al Programa [PROGRAMA_DESTINO]`
- Línea 17: `El Comité del Programa de [PROGRAMA_DESTINO]`
- Línea 23: `cursó y aprobó en el Programa de [PROGRAMA_ORIGEN]`
- Línea 23: `homologada(s) al Programa de [PROGRAMA_DESTINO]`
- Líneas 15 y 23: `cédula de ciudadanía # [CEDULA_ESTUDIANTE]`

---

## CONSIDERACIONES IMPORTANTES

1. **Compatibilidad hacia atrás:** Los nuevos campos son opcionales (`nullable = true`) para no romper datos existentes.

2. **Validaciones:** Considerar si en el futuro estos campos deben ser obligatorios. Por ahora, dejarlos como opcionales.

3. **Obtención de datos en DocumentGeneratorService:**
   - El método `crearMapaReemplazos` recibe un `DocumentRequest` que contiene un `Map<String, Object> datosSolicitud`
   - Necesitarás asegurar que cuando se construye el `DocumentRequest` (probablemente en el controlador), se incluyan estos campos
   - Alternativamente, si el `DocumentRequest` tiene acceso al ID de la solicitud, se puede obtener la solicitud completa desde la base de datos

4. **Cédula del estudiante:**
   - El usuario ya tiene el atributo `cedula` (agregado en cambios anteriores)
   - Asegurar que cuando se genera el documento, se obtenga la cédula del `objUsuario` de la solicitud
   - Si la solicitud tiene `objUsuario` con `cedula`, usar ese valor
   - Si no está disponible, usar el valor del mapa `datosSolicitud` como fallback

5. **Nombres de campos en el mapa:**
   - Verificar cómo se nombran los campos en el `Map<String, Object> datosSolicitud`
   - Puede ser `"programaOrigen"` (camelCase) o `"programa_origen"` (snake_case)
   - Revisar el código que construye este mapa para usar la convención correcta

---

## ORDEN DE EJECUCIÓN RECOMENDADO

1. Actualizar entidad `SolicitudHomologacionEntity`
2. Actualizar modelo de dominio `SolicitudHomologacion`
3. Actualizar DTOs (Petición y Respuesta)
4. Actualizar mappers MapStruct
5. Actualizar `DocumentGeneratorService` para usar los nuevos campos
6. Actualizar controlador para pasar los nuevos campos al `DocumentRequest`
7. Actualizar scripts SQL (si aplica)
8. Actualizar tests
9. Compilar y ejecutar tests para verificar que todo funciona

---

## RESULTADO ESPERADO

Al finalizar, el sistema debe:
- ✅ Permitir guardar `programa_origen` y `programa_destino` en las solicitudes de homologación
- ✅ Reemplazar correctamente los placeholders `[PROGRAMA_ORIGEN]` y `[PROGRAMA_DESTINO]` en la plantilla
- ✅ Reemplazar correctamente el placeholder `[CEDULA_ESTUDIANTE]` usando el atributo `cedula` del usuario
- ✅ Los nuevos campos deben ser opcionales para mantener compatibilidad
- ✅ Todos los tests deben pasar
- ✅ La aplicación debe compilar sin errores
- ✅ Los endpoints REST deben funcionar correctamente

---

## NOTAS ADICIONALES

- **No modificar:** Procesos de Paz y Salvo, Cursos de Verano, Reingreso, ECAES (solo Homologación)
- **Base de datos:** Si se usa `ddl-auto=update` o `create`, Hibernate agregará las columnas automáticamente
- **Migración:** Si hay datos existentes, los nuevos campos serán NULL, lo cual es aceptable ya que son opcionales

---

**IMPORTANTE:** Asegurar que cuando se genera el documento, se obtengan los valores reales de `programa_origen` y `programa_destino` desde la solicitud de homologación, no valores por defecto. Si no están disponibles, considerar si debe lanzarse una excepción o usar un valor por defecto según la lógica de negocio.
