# PROMPT: Organizar Archivos en Subcarpetas por Tipo de Solicitud - Análisis Completo y Implementación

## CONTEXTO

Se requiere implementar la organización de archivos en subcarpetas para los módulos de **Homologación**, **Reingreso** y **ECAES**, siguiendo el mismo patrón que ya está implementado en **Paz y Salvo**. 

**IMPORTANTE:** 
- Respetar la arquitectura hexagonal del proyecto (puertos y adaptadores)
- Analizar el impacto completo del cambio de rutas en todos los procesos del sistema
- Asegurar que todas las funcionalidades existentes sigan funcionando correctamente
- Reutilizar código genérico cuando sea posible

---

## OBJETIVO

Organizar los archivos subidos para las solicitudes de **Homologación**, **Reingreso** y **ECAES** en una estructura de carpetas jerárquica dentro de la carpeta `Archivos`, similar a como funciona **Paz y Salvo**:

```
Archivos/
├── pazysalvo/
│   └── solicitud_123/
│       ├── documento1.pdf
│       └── documento2.pdf
├── reingreso/
│   └── solicitud_456/
│       └── documento_reingreso.pdf
├── homologacion/
│   └── solicitud_789/
│       ├── documento1.pdf
│       └── documento2.pdf
└── ecaes/
    └── solicitud_101/
        └── documento_ecaes.pdf
```

**Formato de ruta relativa guardada en BD:**
- `pazysalvo/solicitud_123/documento.pdf`
- `reingreso/solicitud_456/documento.pdf`
- `homologacion/solicitud_789/documento.pdf`
- `ecaes/solicitud_101/documento.pdf`

---

## ANÁLISIS REQUERIDO

### 1. Análisis de Arquitectura Hexagonal

**Tarea:** Analizar la estructura actual del proyecto para entender:
- Cómo está implementada la organización de archivos en Paz y Salvo
- Qué puertos (interfaces) y adaptadores se utilizan
- Cómo se inyectan las dependencias
- Qué servicios/gateways son reutilizables

**Archivos clave a revisar:**
- `aplicacion/output/GestionarArchivosGatewayIntPort.java` - Interfaz del puerto
- `infraestructura/output/persistencia/gateway/GestionarArchivosGatewayImplAdapter.java` - Implementación
- `aplicacion/input/GestionarArchivosCUIntPort.java` - Caso de uso de archivos
- `dominio/casosDeUso/GestionarArchivosCUIAdapter.java` - Adaptador del caso de uso
- `dominio/casosDeUso/GestionarSolicitudPazYSalvoCUAdapter.java` - Referencia de implementación

### 2. Análisis de Impacto en Procesos Existentes

**Tarea crítica:** Identificar todos los procesos que pueden verse afectados por el cambio de rutas de archivos:

#### 2.1. Visualización/Descarga de Archivos
- **Buscar:** Todos los endpoints que obtienen/descargan archivos
- **Verificar:** Cómo se obtienen los archivos actualmente (¿usando `getFile()` o `getFileByPath()`?)
- **Identificar:** Controladores que muestran/descargan documentos asociados a solicitudes
- **Archivos a revisar:**
  - `infraestructura/input/controladores/ArchivosRestController.java`
  - `infraestructura/input/controladores/SolicitudHomologacionRestController.java`
  - `infraestructura/input/controladores/SolicitudReingresoRestController.java`
  - `infraestructura/input/controladores/SolicitudEcaesRestController.java`
  - Cualquier otro controlador que maneje archivos

#### 2.2. Generación de Documentos
- **Verificar:** Si la generación de documentos (oficios/resoluciones) accede a archivos por ruta
- **Archivos a revisar:**
  - `infraestructura/output/formateador/DocumentGeneratorService.java`

#### 2.3. Asociación de Documentos
- **Verificar:** Cómo se asocian documentos a solicitudes cuando se guardan
- **Archivos a revisar:**
  - Casos de uso de guardado de solicitudes

#### 2.4. Validación de Documentos
- **Verificar:** Si hay procesos que validan la existencia de archivos por ruta
- **Archivos a revisar:**
  - Métodos de validación en controladores o casos de uso

### 3. Análisis de Estado Actual por Módulo

#### 3.1. Paz y Salvo (✅ Ya implementado - usar como referencia)
- **Caso de uso:** `GestionarSolicitudPazYSalvoCUAdapter.guardar()` mueve archivos a carpetas organizadas
- **Controlador:** Usa `saveFile()` con parámetros de tipo e ID cuando sube archivos directamente
- **Estructura:** `pazysalvo/solicitud_{id}/archivo.pdf`

#### 3.2. Reingreso (⚠️ Parcialmente implementado)
- **Controlador:** `SolicitudReingresoRestController.subirArchivo()` ya usa `saveFile()` con organización (línea 455)
- **Caso de uso:** `GestionarSolicitudReingresoCUAdapter.guardar()` NO mueve archivos sin solicitud
- **Estado:** Los archivos subidos directamente con ID se organizan, pero los subidos sin asociar NO se mueven

#### 3.3. Homologación (❌ No implementado)
- **Caso de uso:** `GestionarSolicitudHomologacionCUAdapter.guardar()` NO mueve archivos a carpetas organizadas
- **Controlador:** Verificar si hay endpoints que suban archivos directamente con ID
- **Estado:** Los archivos se guardan en la raíz cuando se suben sin asociar

#### 3.4. ECAES (❌ No implementado)
- **Caso de uso:** `GestionarSolicitudEcaesCUAdapter.guardar()` NO mueve archivos a carpetas organizadas
- **Controlador:** Verificar si hay endpoints que suban archivos directamente con ID
- **Estado:** Los archivos se guardan en la raíz cuando se suben sin asociar

---

## CAMBIOS REQUERIDOS

### 1. Actualizar Casos de Uso

#### 1.1. GestionarSolicitudHomologacionCUAdapter

**Archivo:** `dominio/casosDeUso/GestionarSolicitudHomologacionCUAdapter.java`

**Cambios:**
1. Agregar dependencia `GestionarArchivosCUIntPort gestionarArchivos` al constructor
2. Actualizar método `guardar()` para mover archivos sin solicitud a carpeta organizada

**Código a agregar en `guardar()`:**
```java
//Asociar documentos con solicitud = null y moverlos a carpeta organizada
List<Documento> documentosSinSolicitud = this.objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
for (Documento doc : documentosSinSolicitud) {
    // Mover archivo a carpeta organizada si está en la raíz
    String rutaActual = doc.getRuta_documento() != null ? doc.getRuta_documento() : doc.getNombre();
    if (rutaActual != null && !rutaActual.contains("/")) {
        // El archivo está en la raíz, moverlo a carpeta organizada
        String nuevaRuta = gestionarArchivos.moverArchivoAOrganizado(
            rutaActual, 
            doc.getNombre(), 
            "homologacion", 
            solicitudGuardada.getId_solicitud()
        );
        if (nuevaRuta != null) {
            doc.setRuta_documento(nuevaRuta);
        }
    }
    
    // Asociar documento a la solicitud
    doc.setObjSolicitud(solicitudGuardada);
    this.objGestionarDocumentosGateway.actualizarDocumento(doc);
}
```

#### 1.2. GestionarSolicitudEcaesCUAdapter

**Archivo:** `dominio/casosDeUso/GestionarSolicitudEcaesCUAdapter.java`

**Cambios:**
1. Agregar dependencia `GestionarArchivosCUIntPort gestionarArchivos` al constructor
2. Actualizar método `guardar()` para mover archivos sin solicitud a carpeta organizada

**Código a agregar en `guardar()`:**
```java
List<Documento> documentosSinSolicitud = this.objDocumentosGateway.buscarDocumentosSinSolicitud();

for (Documento doc : documentosSinSolicitud) {
    // Mover archivo a carpeta organizada si está en la raíz
    String rutaActual = doc.getRuta_documento() != null ? doc.getRuta_documento() : doc.getNombre();
    if (rutaActual != null && !rutaActual.contains("/")) {
        // El archivo está en la raíz, moverlo a carpeta organizada
        String nuevaRuta = gestionarArchivos.moverArchivoAOrganizado(
            rutaActual, 
            doc.getNombre(), 
            "ecaes", 
            solicitudGuardada.getId_solicitud()
        );
        if (nuevaRuta != null) {
            doc.setRuta_documento(nuevaRuta);
        }
    }
    
    // Asociar documento a la solicitud
    doc.setObjSolicitud(solicitudGuardada);
    this.objDocumentosGateway.actualizarDocumento(doc);
}
```

#### 1.3. GestionarSolicitudReingresoCUAdapter

**Archivo:** `dominio/casosDeUso/GestionarSolicitudReingresoCUAdapter.java`

**Cambios:**
1. Verificar si ya tiene la dependencia `GestionarArchivosCUIntPort`
2. Si no la tiene, agregarla al constructor
3. Actualizar método `guardar()` para mover archivos sin solicitud a carpeta organizada (similar a los anteriores)

**Nota:** El controlador ya organiza archivos cuando se suben con ID, pero falta mover los archivos sin asociar cuando se guarda la solicitud.

### 2. Actualizar Configuración de Spring

**Archivo:** `infraestructura/configuracion/BeanConfiguration.java`

**Cambios:**
1. Agregar parámetro `GestionarArchivosCUIntPort gestionarArchivos` a:
   - `crearGestionarSolicitudHomologacionCUInt()`
   - `crearGestionarSolicitudEcaesCUInt()`
   - `crearGestionarSolicitudReingresoCUInt()` (si no lo tiene)

**Ejemplo:**
```java
@Bean
public GestionarSolicitudHomologacionCUAdapter crearGestionarSolicitudHomologacionCUInt(
        FormateadorResultadosIntPort formateadorResultados, 
        GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway,
        GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, 
        GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
        GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
        GestionarNotificacionCUIntPort objGestionarNotificacionCU,
        GestionarArchivosCUIntPort gestionarArchivos  // ✅ AGREGAR
) {
    return new GestionarSolicitudHomologacionCUAdapter(
        formateadorResultados, 
        gestionarSolicitudHomologacionGateway, 
        gestionarUsuarioGateway, 
        gestionarDocumentosGateway, 
        gestionarEstadoSolicitudGateway, 
        objGestionarNotificacionCU,
        gestionarArchivos  // ✅ AGREGAR
    );
}
```

### 3. Verificar y Actualizar Controladores

#### 3.1. Verificar Uso de Métodos de Archivos

**Tarea:** Revisar todos los controladores que suben archivos y asegurar que usen el método organizado cuando tengan el ID de solicitud:

**Método a usar:**
```java
objGestionarArchivos.saveFile(archivo, nombreArchivo, "pdf", "tipoSolicitud", idSolicitud);
```

**Tipos de solicitud a usar:**
- `"homologacion"` (minúsculas)
- `"reingreso"` (minúsculas)
- `"ecaes"` (minúsculas)

**Archivos a revisar:**
- `infraestructura/input/controladores/SolicitudHomologacionRestController.java`
- `infraestructura/input/controladores/SolicitudReingresoRestController.java` (ya lo usa, verificar)
- `infraestructura/input/controladores/SolicitudEcaesRestController.java`

#### 3.2. Actualizar Obtención de Archivos

**Tarea crítica:** Verificar que todos los métodos que obtienen archivos usen el método correcto según el formato de ruta:

**Problema potencial:** Si la ruta guardada en BD es `"homologacion/solicitud_123/documento.pdf"`, el método `getFile("documento.pdf")` NO funcionará.

**Solución:** Usar `getFileByPath(rutaCompleta)` cuando la ruta contiene "/", o verificar el método `getFile()` para que maneje ambos casos.

**Métodos a revisar:**
- `GestionarArchivosGatewayImplAdapter.getFile(String filename)` - Verificar si maneja rutas relativas
- `GestionarArchivosGatewayImplAdapter.getFileByPath(String relativePath)` - Usar este para rutas organizadas

**Código a buscar en controladores:**
```java
// Buscar llamadas a:
objGestionarArchivos.getFile(...)
objGestionarArchivos.getFileByPath(...)
```

**Estrategia de actualización:**
1. Si `ruta_documento` contiene "/", usar `getFileByPath(ruta_documento)`
2. Si `ruta_documento` no contiene "/", usar `getFile(ruta_documento)` (compatibilidad hacia atrás)

**Ejemplo de código adaptativo:**
```java
byte[] archivoBytes;
String rutaDocumento = documento.getRuta_documento();

if (rutaDocumento != null && rutaDocumento.contains("/")) {
    // Ruta organizada (nueva estructura)
    archivoBytes = objGestionarArchivos.getFileByPath(rutaDocumento);
} else {
    // Ruta simple (compatibilidad hacia atrás)
    String nombreArchivo = rutaDocumento != null ? rutaDocumento : documento.getNombre();
    archivoBytes = objGestionarArchivos.getFile(nombreArchivo);
}
```

**Archivos a actualizar:**
- Todos los controladores que descargan/muestran archivos asociados a solicitudes
- Especialmente:
  - Endpoints de descarga de documentos de homologación
  - Endpoints de descarga de documentos de reingreso
  - Endpoints de descarga de documentos de ECAES
  - Endpoints de visualización de oficios/resoluciones

### 4. Verificar Generación de Documentos

**Archivo:** `infraestructura/output/formateador/DocumentGeneratorService.java`

**Tarea:** Verificar si este servicio accede a archivos por ruta. Si es así, actualizar para usar `getFileByPath()` cuando la ruta contenga "/".

---

## REFERENCIA: IMPLEMENTACIÓN DE PAZ Y SALVO

**Archivo:** `dominio/casosDeUso/GestionarSolicitudPazYSalvoCUAdapter.java`

**Método `guardar()` (líneas 66-87):**
```java
// Asociar documentos sin solicitud y moverlos a carpeta organizada
List<Documento> documentosSinSolicitud = documentosGateway.buscarDocumentosSinSolicitud();
for (Documento doc : documentosSinSolicitud) {
    // Mover archivo a carpeta organizada si está en la raíz
    String rutaActual = doc.getRuta_documento() != null ? doc.getRuta_documento() : doc.getNombre();
    if (rutaActual != null && !rutaActual.contains("/")) {
        // El archivo está en la raíz, moverlo a carpeta organizada
        String nuevaRuta = gestionarArchivos.moverArchivoAOrganizado(
            rutaActual, 
            doc.getNombre(), 
            "pazysalvo", 
            solicitudGuardada.getId_solicitud()
        );
        if (nuevaRuta != null) {
            doc.setRuta_documento(nuevaRuta);
        }
    }
    
    // Asociar documento a la solicitud
    doc.setObjSolicitud(solicitudGuardada);
    documentosGateway.actualizarDocumento(doc);
}
```

**Usar este código como plantilla para los otros módulos.**

---

## INFRAESTRUCTURA DISPONIBLE

### Métodos del Gateway de Archivos

**Interfaz:** `aplicacion/output/GestionarArchivosGatewayIntPort.java`

**Métodos disponibles:**
1. `saveFile(MultipartFile, String, String)` - Guarda en raíz (retrocompatibilidad)
2. `saveFile(MultipartFile, String, String, String, Integer)` - Guarda organizado en subcarpetas
3. `getFile(String filename)` - Obtiene por nombre (busca en raíz y luego en rutas relativas)
4. `getFileByPath(String relativePath)` - Obtiene por ruta relativa completa
5. `moverArchivoAOrganizado(String, String, String, Integer)` - Mueve archivo a carpeta organizada

### Métodos del Caso de Uso de Archivos

**Interfaz:** `aplicacion/input/GestionarArchivosCUIntPort.java`

**Métodos disponibles:**
- Los mismos que el gateway, pero con validaciones de negocio

---

## ESTRATEGIA DE IMPLEMENTACIÓN

### Fase 1: Análisis y Preparación
1. ✅ Analizar arquitectura hexagonal actual
2. ✅ Identificar todos los procesos que usan archivos
3. ✅ Mapear todos los endpoints que obtienen/descargan archivos
4. ✅ Verificar cómo se obtienen archivos actualmente

### Fase 2: Actualización de Casos de Uso
1. ✅ Actualizar `GestionarSolicitudHomologacionCUAdapter`
2. ✅ Actualizar `GestionarSolicitudEcaesCUAdapter`
3. ✅ Actualizar `GestionarSolicitudReingresoCUAdapter` (si es necesario)

### Fase 3: Actualización de Configuración
1. ✅ Actualizar `BeanConfiguration` para inyectar dependencias

### Fase 4: Actualización de Controladores (Crítico)
1. ✅ Verificar/actualizar endpoints de subida de archivos
2. ✅ **CRÍTICO:** Actualizar endpoints de obtención/descarga de archivos para usar método correcto según formato de ruta
3. ✅ Implementar lógica adaptativa que maneje tanto rutas antiguas como nuevas

### Fase 5: Verificación
1. ✅ Verificar que la generación de documentos funcione
2. ✅ Verificar que la visualización de archivos funcione
3. ✅ Verificar que la descarga de archivos funcione
4. ✅ Probar con archivos existentes (rutas antiguas) y nuevos (rutas organizadas)

---

## CONSIDERACIONES IMPORTANTES

### 1. Compatibilidad hacia atrás
- Los archivos existentes en la raíz seguirán funcionando
- La lógica adaptativa debe manejar ambos formatos de ruta
- Los archivos antiguos NO se moverán automáticamente (solo los nuevos)

### 2. Arquitectura Hexagonal
- Usar siempre los puertos (interfaces), nunca los adaptadores directamente
- `GestionarArchivosCUIntPort` es el caso de uso (capa de aplicación)
- `GestionarArchivosGatewayIntPort` es el gateway (capa de aplicación)
- NO acceder directamente a `GestionarArchivosGatewayImplAdapter`

### 3. Manejo de Errores
- Si `moverArchivoAOrganizado()` falla, continuar con la asociación del documento
- Si `getFileByPath()` falla, intentar con `getFile()` como fallback
- No lanzar excepciones que interrumpan el guardado de solicitudes

### 4. Nombres de Tipo de Solicitud
- Usar minúsculas: `"homologacion"`, `"ecaes"`, `"reingreso"`, `"pazysalvo"`
- El método `moverArchivoAOrganizado` normaliza automáticamente

### 5. Validación de Rutas
- Verificar que `ruta_documento` no sea null antes de usar
- Si `ruta_documento` es null, usar `nombre` como fallback
- Verificar si la ruta contiene "/" para determinar el método a usar

---

## RESULTADO ESPERADO

Al finalizar, el sistema debe:

- ✅ Los archivos de **Homologación** se organizan en `Archivos/homologacion/solicitud_{id}/`
- ✅ Los archivos de **ECAES** se organizan en `Archivos/ecaes/solicitud_{id}/`
- ✅ Los archivos de **Reingreso** se organizan en `Archivos/reingreso/solicitud_{id}/`
- ✅ Los archivos de **Paz y Salvo** ya están organizados (sin cambios)
- ✅ Cuando se guarda una solicitud, los archivos sin asociar se mueven automáticamente
- ✅ **CRÍTICO:** Todos los endpoints de visualización/descarga de archivos funcionan correctamente
- ✅ **CRÍTICO:** La lógica adaptativa maneja tanto rutas antiguas como nuevas
- ✅ La generación de documentos funciona correctamente
- ✅ Se respeta la arquitectura hexagonal
- ✅ No se rompen funcionalidades existentes

---

## CHECKLIST DE VERIFICACIÓN

### Antes de finalizar, verificar:

- [ ] Todos los casos de uso actualizados con lógica de movimiento de archivos
- [ ] Configuración de Spring actualizada con nuevas dependencias
- [ ] Todos los controladores que suben archivos usan el método organizado (cuando tienen ID)
- [ ] **CRÍTICO:** Todos los controladores que obtienen archivos usan lógica adaptativa
- [ ] La generación de documentos funciona correctamente
- [ ] Los endpoints de visualización de archivos funcionan con rutas organizadas
- [ ] Los endpoints de descarga de archivos funcionan con rutas organizadas
- [ ] La compatibilidad hacia atrás funciona (archivos en raíz siguen accesibles)
- [ ] No hay errores de compilación
- [ ] La aplicación inicia correctamente
- [ ] Los tests existentes pasan (o se actualizan si es necesario)

---

## NOTAS ADICIONALES

- **No modificar** la funcionalidad de Paz y Salvo (ya está correcta)
- **No modificar** la infraestructura de archivos (ya está implementada)
- Solo agregar la lógica de movimiento en los casos de uso
- **CRÍTICO:** Adaptar la obtención de archivos para manejar ambos formatos de ruta
- Si hay archivos huérfanos en la raíz, se organizarán automáticamente cuando se asocien a una solicitud
- Los archivos que ya están organizados (tienen "/" en la ruta) NO deben moverse nuevamente

---

## REFERENCIA: ESTRUCTURA DE ARCHIVOS

**Gateway de Archivos:**
- `aplicacion/output/GestionarArchivosGatewayIntPort.java` - Interfaz del puerto
- `infraestructura/output/persistencia/gateway/GestionarArchivosGatewayImplAdapter.java` - Implementación

**Caso de Uso de Archivos:**
- `aplicacion/input/GestionarArchivosCUIntPort.java` - Interfaz del caso de uso
- `dominio/casosDeUso/GestionarArchivosCUIAdapter.java` - Adaptador del caso de uso

**Casos de Uso a Actualizar:**
- `dominio/casosDeUso/GestionarSolicitudHomologacionCUAdapter.java`
- `dominio/casosDeUso/GestionarSolicitudEcaesCUAdapter.java`
- `dominio/casosDeUso/GestionarSolicitudReingresoCUAdapter.java` (verificar)

**Referencia (NO modificar):**
- `dominio/casosDeUso/GestionarSolicitudPazYSalvoCUAdapter.java` - Usar como referencia

**Configuración:**
- `infraestructura/configuracion/BeanConfiguration.java`

**Controladores a Revisar/Actualizar:**
- `infraestructura/input/controladores/SolicitudHomologacionRestController.java`
- `infraestructura/input/controladores/SolicitudReingresoRestController.java`
- `infraestructura/input/controladores/SolicitudEcaesRestController.java`
- `infraestructura/input/controladores/ArchivosRestController.java`

---

**IMPORTANTE:** 

1. **Análisis primero:** Antes de hacer cambios, analizar completamente cómo se obtienen archivos en todo el proyecto
2. **Lógica adaptativa:** Implementar lógica que maneje tanto rutas antiguas (sin "/") como nuevas (con "/")
3. **Pruebas exhaustivas:** Probar especialmente la visualización y descarga de archivos después de los cambios
4. **No romper funcionalidades:** Asegurar que todas las funcionalidades existentes sigan funcionando

---

**PRIORIDAD MÁXIMA:** Actualizar la lógica de obtención de archivos en todos los controladores para que funcione con las nuevas rutas organizadas. Este es el cambio más crítico y el que más puede afectar al sistema.
