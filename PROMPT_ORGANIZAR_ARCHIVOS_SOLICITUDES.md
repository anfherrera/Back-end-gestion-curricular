# PROMPT: Organizar Archivos en Carpetas por Tipo de Solicitud - Backend

## CONTEXTO

Actualmente, el sistema tiene una infraestructura para organizar archivos en carpetas por tipo de solicitud e ID de solicitud. Esta funcionalidad ya está implementada para **Paz y Salvo**, pero necesita ser adaptada y aplicada consistentemente para los módulos de **Reingreso**, **Homologación** y **ECAES**.

**IMPORTANTE:** Respetar la arquitectura hexagonal del proyecto (puertos y adaptadores).

---

## OBJETIVO

Asegurar que todos los archivos subidos para las solicitudes de **Reingreso**, **Homologación** y **ECAES** se guarden en una estructura de carpetas organizada, similar a como lo hace **Paz y Salvo**:

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

---

## ESTADO ACTUAL

### ✅ Ya Implementado Correctamente

1. **Infraestructura de Archivos:**
   - `GestionarArchivosGatewayImplAdapter` ya tiene el método `saveFile(MultipartFile, String, String, String, Integer)` que crea carpetas organizadas
   - Método `moverArchivoAOrganizado()` para mover archivos desde la raíz a carpetas organizadas
   - Método `getFileByPath(String)` para obtener archivos por ruta relativa completa

2. **Paz y Salvo:**
   - ✅ `GestionarSolicitudPazYSalvoCUAdapter.guardar()` mueve archivos sin solicitud a carpetas organizadas
   - ✅ Controlador `SolicitudPazYSalvoRestController` usa el método organizado para subir oficios

3. **Reingreso:**
   - ✅ Controlador `SolicitudReingresoRestController.subirArchivo()` ya usa el método organizado (línea 455)

### ⚠️ Necesita Actualización

1. **Homologación:**
   - ❌ `GestionarSolicitudHomologacionCUAdapter.guardar()` NO mueve archivos sin solicitud a carpetas organizadas
   - ❌ Los archivos se guardan en la raíz cuando se suben sin asociar a solicitud

2. **ECAES:**
   - ❌ `GestionarSolicitudEcaesCUAdapter.guardar()` NO mueve archivos sin solicitud a carpetas organizadas
   - ❌ Los archivos se guardan en la raíz cuando se suben sin asociar a solicitud

---

## CAMBIOS REQUERIDOS

### 1. Actualizar Caso de Uso de Homologación

**Archivo:** `dominio/casosDeUso/GestionarSolicitudHomologacionCUAdapter.java`

**Método:** `guardar(SolicitudHomologacion solicitud)`

**Ubicación actual:** Líneas 64-71

**Estado actual:**
```java
//Asociar documentos con solicitud = null
List<Documento> documentosSinSolicitud = this.objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
for (Documento doc : documentosSinSolicitud) {
    doc.setObjSolicitud(solicitudGuardada);
    this.objGestionarDocumentosGateway.actualizarDocumento(doc);
}
```

**Cambio requerido:**
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

**Dependencia a agregar:**
- Inyectar `GestionarArchivosGatewayIntPort gestionarArchivos` en el constructor
- Agregar al constructor: `this.gestionarArchivos = gestionarArchivos;`

**Import necesario:**
```java
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
```

---

### 2. Actualizar Caso de Uso de ECAES

**Archivo:** `dominio/casosDeUso/GestionarSolicitudEcaesCUAdapter.java`

**Método:** `guardar(SolicitudEcaes solicitud)`

**Ubicación actual:** Líneas 78-85

**Estado actual:**
```java
List<Documento> documentosSinSolicitud = this.objDocumentosGateway.buscarDocumentosSinSolicitud();

for (Documento doc : documentosSinSolicitud) {
    doc.setObjSolicitud(solicitudGuardada);
    this.objDocumentosGateway.actualizarDocumento(doc);
}
```

**Cambio requerido:**
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

**Dependencia a agregar:**
- Inyectar `GestionarArchivosGatewayIntPort gestionarArchivos` en el constructor
- Agregar al constructor: `this.gestionarArchivos = gestionarArchivos;`

**Import necesario:**
```java
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
```

---

### 3. Verificar Controladores (Opcional pero Recomendado)

**Verificar que los controladores usen el método organizado cuando suben archivos directamente asociados a una solicitud:**

#### 3.1. Homologación

**Archivo:** `infraestructura/input/controladores/SolicitudHomologacionRestController.java`

**Búsqueda:** Buscar métodos que suban archivos y verificar que usen:
```java
objGestionarArchivos.saveFile(archivo, nombreArchivo, "pdf", "homologacion", idSolicitud);
```

**Nota:** Si no hay endpoints específicos para subir archivos con ID de solicitud, esto es opcional ya que el caso de uso se encargará de mover los archivos.

#### 3.2. ECAES

**Archivo:** `infraestructura/input/controladores/SolicitudEcaesRestController.java` o controladores relacionados

**Búsqueda:** Buscar métodos que suban archivos y verificar que usen:
```java
objGestionarArchivos.saveFile(archivo, nombreArchivo, "pdf", "ecaes", idSolicitud);
```

**Nota:** Si no hay endpoints específicos para subir archivos con ID de solicitud, esto es opcional ya que el caso de uso se encargará de mover los archivos.

---

## REFERENCIA: IMPLEMENTACIÓN DE PAZ Y SALVO

Para referencia, ver cómo está implementado en `GestionarSolicitudPazYSalvoCUAdapter.guardar()` (líneas 66-87):

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

---

## ESTRUCTURA DE CARPETAS ESPERADA

Después de los cambios, la estructura de carpetas debe ser:

```
Archivos/
├── pazysalvo/
│   └── solicitud_{id}/
│       └── archivos...
├── reingreso/
│   └── solicitud_{id}/
│       └── archivos...
├── homologacion/
│   └── solicitud_{id}/
│       └── archivos...
└── ecaes/
    └── solicitud_{id}/
        └── archivos...
```

**Formato de ruta relativa guardada en BD:**
- `pazysalvo/solicitud_123/documento.pdf`
- `reingreso/solicitud_456/documento.pdf`
- `homologacion/solicitud_789/documento.pdf`
- `ecaes/solicitud_101/documento.pdf`

---

## LÓGICA DE MOVIMIENTO DE ARCHIVOS

### Condición para Mover Archivo

Un archivo se mueve a carpeta organizada si:
1. El archivo está en la raíz (ruta no contiene "/")
2. El archivo está asociado a una solicitud (cuando se guarda la solicitud)

### Método `moverArchivoAOrganizado`

**Ubicación:** `GestionarArchivosGatewayImplAdapter.moverArchivoAOrganizado()`

**Parámetros:**
- `rutaActual`: Ruta actual del archivo (puede ser solo el nombre si está en raíz)
- `nombreArchivo`: Nombre del archivo
- `tipoSolicitud`: "homologacion" o "ecaes" (en minúsculas)
- `idSolicitud`: ID de la solicitud

**Retorna:** Nueva ruta relativa completa (ej: "homologacion/solicitud_789/documento.pdf")

**Nota:** El método ya existe y está implementado. Solo necesita ser llamado desde los casos de uso.

---

## CONSIDERACIONES IMPORTANTES

1. **Arquitectura Hexagonal:**
   - Usar el puerto `GestionarArchivosGatewayIntPort` (capa de aplicación)
   - NO acceder directamente al adaptador de implementación

2. **Compatibilidad hacia atrás:**
   - Los archivos existentes en la raíz se moverán automáticamente cuando se asocien a una solicitud
   - Los archivos que ya están organizados (tienen "/" en la ruta) NO se moverán

3. **Inyección de Dependencias:**
   - Agregar `GestionarArchivosGatewayIntPort` al constructor de los casos de uso
   - Actualizar la configuración de Spring si es necesario (probablemente ya está configurado)

4. **Nombres de Tipo de Solicitud:**
   - Usar minúsculas: "homologacion", "ecaes", "reingreso", "pazysalvo"
   - El método `moverArchivoAOrganizado` normaliza el nombre automáticamente

5. **Manejo de Errores:**
   - Si `moverArchivoAOrganizado` falla, continuar con la asociación del documento
   - No lanzar excepciones que interrumpan el guardado de la solicitud

---

## ORDEN DE EJECUCIÓN RECOMENDADO

1. Actualizar `GestionarSolicitudHomologacionCUAdapter`:
   - Agregar dependencia `GestionarArchivosGatewayIntPort` al constructor
   - Actualizar método `guardar()` para mover archivos

2. Actualizar `GestionarSolicitudEcaesCUAdapter`:
   - Agregar dependencia `GestionarArchivosGatewayIntPort` al constructor
   - Actualizar método `guardar()` para mover archivos

3. Verificar configuración de Spring:
   - Asegurar que `GestionarArchivosGatewayIntPort` esté disponible como bean
   - Verificar que los casos de uso se construyan correctamente con la nueva dependencia

4. Probar flujo completo:
   - Subir archivo sin asociar a solicitud (se guarda en raíz)
   - Crear solicitud (los archivos se mueven a carpeta organizada)
   - Verificar que la estructura de carpetas sea correcta

---

## RESULTADO ESPERADO

Al finalizar, el sistema debe:

- ✅ Los archivos de **Homologación** se organizan en `Archivos/homologacion/solicitud_{id}/`
- ✅ Los archivos de **ECAES** se organizan en `Archivos/ecaes/solicitud_{id}/`
- ✅ Los archivos de **Reingreso** ya están organizados (sin cambios necesarios)
- ✅ Los archivos de **Paz y Salvo** ya están organizados (sin cambios necesarios)
- ✅ Cuando se guarda una solicitud, los archivos sin asociar se mueven automáticamente a su carpeta correspondiente
- ✅ La estructura de carpetas es consistente para todos los tipos de solicitud
- ✅ Se respeta la arquitectura hexagonal del proyecto
- ✅ Los archivos existentes en la raíz se organizan cuando se asocian a una solicitud

---

## NOTAS ADICIONALES

- **No modificar** la funcionalidad de Paz y Salvo (ya está correcta)
- **No modificar** la funcionalidad de Reingreso (ya está correcta)
- **No modificar** la infraestructura de archivos (ya está implementada)
- Solo agregar la lógica de movimiento de archivos en los casos de uso de Homologación y ECAES
- Si hay archivos huérfanos en la raíz, se organizarán automáticamente cuando se asocien a una solicitud

---

## REFERENCIA: ESTRUCTURA DE ARCHIVOS

**Gateway de Archivos:**
- `aplicacion/output/GestionarArchivosGatewayIntPort.java` - Interfaz del puerto
- `infraestructura/output/persistencia/gateway/GestionarArchivosGatewayImplAdapter.java` - Implementación

**Casos de Uso a Actualizar:**
- `dominio/casosDeUso/GestionarSolicitudHomologacionCUAdapter.java`
- `dominio/casosDeUso/GestionarSolicitudEcaesCUAdapter.java`

**Referencia (NO modificar):**
- `dominio/casosDeUso/GestionarSolicitudPazYSalvoCUAdapter.java` - Usar como referencia

---

**IMPORTANTE:** Asegurar que todos los cambios respeten la arquitectura hexagonal y no rompan la funcionalidad existente. Los archivos que ya están organizados (tienen "/" en la ruta) NO deben moverse nuevamente.
