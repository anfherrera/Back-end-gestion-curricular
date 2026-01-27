# PROMPT: Corregir Endpoint de Descarga de Archivos - Error 404 con Archivos Organizados

## PROBLEMA IDENTIFICADO

El endpoint `GET /api/archivos/descargar/pdf?filename={nombreArchivo}` está fallando con error **404 (Not Found)** después de implementar la organización de archivos en subcarpetas.

**Error específico:**
```
GET /api/archivos/descargar/pdf?filename=Planeacion_Primero_09-23%20de%20Enero%202026.docx.pdf
Response: 404 (Not Found)
```

**Causa raíz:**
- El endpoint solo recibe el `filename` (nombre del archivo)
- Los archivos ahora están organizados en subcarpetas: `tipoSolicitud/solicitud_{id}/archivo.pdf`
- El método `getFile(filename)` busca primero en la raíz, pero si el archivo está en una subcarpeta organizada, no lo encuentra
- El endpoint NO tiene acceso al objeto `Documento` completo para obtener su `ruta_documento`

---

## CONTEXTO

Después de implementar la organización de archivos en subcarpetas para Homologación, Reingreso y ECAES, los archivos se guardan con rutas como:
- `homologacion/solicitud_123/documento.pdf`
- `reingreso/solicitud_456/documento.pdf`
- `ecaes/solicitud_789/documento.pdf`

El endpoint `/descargar/pdf` recibe solo el nombre del archivo, pero necesita la ruta completa para encontrarlo en las subcarpetas organizadas.

---

## SOLUCIÓN REQUERIDA

Actualizar el endpoint `GET /api/archivos/descargar/pdf` siguiendo **exactamente el mismo patrón** que usa Paz y Salvo en su endpoint `/descargar-documento` (líneas 896-936 de `SolicitudPazYSalvoRestController.java`).

**Patrón a seguir:**
1. **Buscar el documento en la base de datos** por nombre en todas las solicitudes
2. **Obtener la `ruta_documento` completa** si está disponible
3. **Usar `getFile(rutaArchivo)`** que ya maneja tanto rutas simples como rutas con subcarpetas (el método busca primero en raíz, luego como ruta relativa)

**Referencia exacta:** `SolicitudPazYSalvoRestController.descargarDocumento()` (líneas 896-936)

---

## ARCHIVO A MODIFICAR

**Archivo:** `infraestructura/input/controladores/ArchivosRestController.java`

**Método:** `bajarPDF(@RequestParam(name = "filename", required = true) String filename)`

**Ubicación actual:** Líneas 273-292

**Estado actual:**
```java
@GetMapping("/descargar/pdf")
public ResponseEntity<byte[]> bajarPDF(@RequestParam(name = "filename", required = true) String filename) {
    try {
        byte[] archivos = this.objGestionarArchivos.getFile(filename);
        
        if (archivos == null || archivos.length == 0) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(archivos);
    } catch (Exception e) {
        log.error("Error al descargar archivo: {}", filename, e);
        return ResponseEntity.notFound().build();
    }
}
```

---

## CAMBIOS REQUERIDOS

### 1. Agregar Dependencias Necesarias

**En la clase `ArchivosRestController`:**

Ya tiene:
- `GestionarArchivosCUIntPort objGestionarArchivos`
- `GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway`
- `GestionarSolicitudPazYSalvoCUIntPort solicitudPazYSalvoCU`
- `GestionarSolicitudCursoVeranoCUIntPort solicitudCursoVeranoCU`

**Agregar dependencias para buscar en todos los tipos de solicitudes:**
- `GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU` (para buscar en homologación)
- `GestionarSolicitudReingresoCUIntPort solicitudReingresoCU` (para buscar en reingreso)
- `GestionarSolicitudEcaesCUIntPort solicitudEcaesCU` (para buscar en ECAES)

**Imports necesarios:**
```java
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
```

**Agregar al constructor (usando `@RequiredArgsConstructor` de Lombok, se inyectan automáticamente):**
```java
private final GestionarSolicitudHomologacionCUIntPort solicitudHomologacionCU;
private final GestionarSolicitudReingresoCUIntPort solicitudReingresoCU;
private final GestionarSolicitudEcaesCUIntPort solicitudEcaesCU;
```

### 2. Actualizar el Método `bajarPDF`

**Reemplazar el método completo con:**

```java
@GetMapping("/descargar/pdf")
public ResponseEntity<byte[]> bajarPDF(@RequestParam(name = "filename", required = true) String filename) {
    try {
        // Buscar el documento en la BD por nombre para obtener su ruta completa
        String rutaArchivo = filename; // Por defecto usar el nombre recibido
        
        // Buscar en todas las solicitudes de todos los tipos (igual que Paz y Salvo)
        
        // 1. Buscar en solicitudes de Paz y Salvo
        List<SolicitudPazYSalvo> solicitudesPazSalvo = solicitudPazYSalvoCU.listarSolicitudes();
        for (SolicitudPazYSalvo solicitud : solicitudesPazSalvo) {
            if (solicitud.getDocumentos() != null) {
                for (Documento doc : solicitud.getDocumentos()) {
                    // Comparar por nombre (sin ruta)
                    String nombreDoc = doc.getNombre();
                    if (nombreDoc != null && nombreDoc.equals(filename)) {
                        // Usar la ruta completa si está disponible
                        if (doc.getRuta_documento() != null && !doc.getRuta_documento().isEmpty()) {
                            rutaArchivo = doc.getRuta_documento();
                        }
                        break;
                    }
                }
            }
        }
        
        // 2. Buscar en solicitudes de Homologación (mismo patrón que Paz y Salvo)
        List<SolicitudHomologacion> solicitudesHomologacion = solicitudHomologacionCU.listarSolicitudes();
        for (SolicitudHomologacion solicitud : solicitudesHomologacion) {
            if (solicitud.getDocumentos() != null) {
                for (Documento doc : solicitud.getDocumentos()) {
                    // Comparar por nombre (sin ruta) - igual que Paz y Salvo
                    String nombreDoc = doc.getNombre();
                    if (nombreDoc != null && nombreDoc.equals(filename)) {
                        // Usar la ruta completa si está disponible
                        if (doc.getRuta_documento() != null && !doc.getRuta_documento().isEmpty()) {
                            rutaArchivo = doc.getRuta_documento();
                        }
                        break;
                    }
                }
            }
        }
        
        // 3. Buscar en solicitudes de Reingreso (mismo patrón que Paz y Salvo)
        List<SolicitudReingreso> solicitudesReingreso = solicitudReingresoCU.listarSolicitudesReingreso();
        for (SolicitudReingreso solicitud : solicitudesReingreso) {
            if (solicitud.getDocumentos() != null) {
                for (Documento doc : solicitud.getDocumentos()) {
                    // Comparar por nombre (sin ruta) - igual que Paz y Salvo
                    String nombreDoc = doc.getNombre();
                    if (nombreDoc != null && nombreDoc.equals(filename)) {
                        // Usar la ruta completa si está disponible
                        if (doc.getRuta_documento() != null && !doc.getRuta_documento().isEmpty()) {
                            rutaArchivo = doc.getRuta_documento();
                        }
                        break;
                    }
                }
            }
        }
        
        // 4. Buscar en solicitudes de ECAES (mismo patrón que Paz y Salvo)
        List<SolicitudEcaes> solicitudesEcaes = solicitudEcaesCU.listarSolicitudes();
        for (SolicitudEcaes solicitud : solicitudesEcaes) {
            if (solicitud.getDocumentos() != null) {
                for (Documento doc : solicitud.getDocumentos()) {
                    // Comparar por nombre (sin ruta) - igual que Paz y Salvo
                    String nombreDoc = doc.getNombre();
                    if (nombreDoc != null && nombreDoc.equals(filename)) {
                        // Usar la ruta completa si está disponible
                        if (doc.getRuta_documento() != null && !doc.getRuta_documento().isEmpty()) {
                            rutaArchivo = doc.getRuta_documento();
                        }
                        break;
                    }
                }
            }
        }
        
        // Nota: Cursos de Verano ya está disponible como solicitudCursoVeranoCU si se necesita en el futuro
        
        // Obtener el archivo usando la ruta (puede ser nombre simple o ruta completa)
        // El método getFile() ya maneja ambos casos: busca primero en raíz, luego como ruta relativa
        byte[] archivo = objGestionarArchivos.getFile(rutaArchivo);
        
        if (archivo == null || archivo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        
        // Extraer solo el nombre del archivo para el header (sin la ruta)
        String nombreArchivo = filename;
        if (rutaArchivo.contains("/")) {
            nombreArchivo = rutaArchivo.substring(rutaArchivo.lastIndexOf("/") + 1);
        }
        
        // Configurar Content-Disposition con filename y filename* (UTF-8) - igual que Paz y Salvo
        String encoded = java.net.URLEncoder.encode(nombreArchivo, java.nio.charset.StandardCharsets.UTF_8)
            .replace("+", "%20");
        String contentDisposition = "attachment; filename=\"" + nombreArchivo + "\"; filename*=UTF-8''" + encoded;

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .contentType(MediaType.APPLICATION_PDF)
            .body(archivo);
            
    } catch (Exception e) {
        log.error("Error al descargar archivo: {}", filename, e);
        return ResponseEntity.notFound().build();
    }
}
```

---

## REFERENCIA: IMPLEMENTACIÓN EXACTA DE PAZ Y SALVO

**Archivo:** `infraestructura/input/controladores/SolicitudPazYSalvoRestController.java`

**Método:** `descargarDocumento(@RequestParam("filename") String filename)` (líneas 896-936)

**Código exacto de referencia (FUNCIONANDO CORRECTAMENTE):**
```java
@GetMapping("/descargar-documento")
public ResponseEntity<byte[]> descargarDocumento(@RequestParam("filename") String filename) {
    try {
        // Buscar el documento en la BD por nombre para obtener su ruta completa
        String rutaArchivo = filename; // Por defecto usar el nombre recibido
        
        // Buscar en todas las solicitudes de paz y salvo
        List<SolicitudPazYSalvo> solicitudes = solicitudPazYSalvoCU.listarSolicitudes();
        for (SolicitudPazYSalvo solicitud : solicitudes) {
            if (solicitud.getDocumentos() != null) {
                for (Documento doc : solicitud.getDocumentos()) {
                    // Comparar por nombre (sin ruta)
                    String nombreDoc = doc.getNombre();
                    if (nombreDoc != null && nombreDoc.equals(filename)) {
                        // Usar la ruta completa si está disponible
                        if (doc.getRuta_documento() != null && !doc.getRuta_documento().isEmpty()) {
                            rutaArchivo = doc.getRuta_documento();
                        }
                        break;
                    }
                }
            }
        }
        
        // Obtener el archivo usando la ruta (puede ser nombre simple o ruta completa)
        byte[] archivo = objGestionarArchivos.getFile(rutaArchivo);
        
        if (archivo == null || archivo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        
        // Extraer solo el nombre del archivo para el header (sin la ruta)
        String nombreArchivo = filename;
        if (rutaArchivo.contains("/")) {
            nombreArchivo = rutaArchivo.substring(rutaArchivo.lastIndexOf("/") + 1);
        }
        
        // Configurar Content-Disposition con filename y filename* (UTF-8)
        String encoded = java.net.URLEncoder.encode(nombreArchivo, java.nio.charset.StandardCharsets.UTF_8)
            .replace("+", "%20");
        String contentDisposition = "attachment; filename=\"" + nombreArchivo + "\"; filename*=UTF-8''" + encoded;

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .contentType(MediaType.APPLICATION_PDF)
            .body(archivo);
            
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
```

**IMPORTANTE:** Usar este código EXACTO como plantilla, solo extender la búsqueda a Homologación, Reingreso y ECAES. El método `getFile()` ya maneja rutas relativas automáticamente (busca primero en raíz, luego como ruta relativa completa).

---

## CONSIDERACIONES IMPORTANTES

### 1. Patrón Exacto de Paz y Salvo
- **Seguir exactamente** el mismo patrón que usa Paz y Salvo
- **NO agregar lógica adicional** de `getFileByPath()` - usar solo `getFile()` como en Paz y Salvo
- El método `getFile()` ya maneja automáticamente rutas relativas (busca primero en raíz, luego como ruta completa)

### 2. Orden de Búsqueda
- Buscar en todos los tipos de solicitudes (Paz y Salvo, Homologación, Reingreso, ECAES)
- Si el mismo nombre existe en múltiples tipos, se usará la última ruta encontrada
- Esto es aceptable ya que los nombres de archivos deberían ser únicos por solicitud

### 3. Compatibilidad hacia atrás
- Si no se encuentra el documento en la BD, usar el nombre original
- El método `getFile(filename)` intentará buscar en la raíz primero
- Si el archivo está en una subcarpeta organizada, se encontrará usando la ruta completa obtenida de la BD

### 4. Manejo de Errores
- Seguir el mismo patrón de Paz y Salvo: `catch (Exception e) { return ResponseEntity.internalServerError().build(); }`
- No agregar logging adicional a menos que sea necesario para depuración

---

## CASOS DE PRUEBA

Después de implementar, verificar:

1. **Archivo en raíz (compatibilidad hacia atrás):**
   - `GET /api/archivos/descargar/pdf?filename=archivo_viejo.pdf`
   - Debe funcionar si el archivo está en `Archivos/archivo_viejo.pdf`

2. **Archivo organizado en Homologación:**
   - `GET /api/archivos/descargar/pdf?filename=documento_homologacion.pdf`
   - Debe encontrar el archivo en `Archivos/homologacion/solicitud_{id}/documento_homologacion.pdf`

3. **Archivo organizado en Reingreso:**
   - `GET /api/archivos/descargar/pdf?filename=documento_reingreso.pdf`
   - Debe encontrar el archivo en `Archivos/reingreso/solicitud_{id}/documento_reingreso.pdf`

4. **Archivo organizado en ECAES:**
   - `GET /api/archivos/descargar/pdf?filename=documento_ecaes.pdf`
   - Debe encontrar el archivo en `Archivos/ecaes/solicitud_{id}/documento_ecaes.pdf`

5. **Archivo no encontrado:**
   - `GET /api/archivos/descargar/pdf?filename=archivo_inexistente.pdf`
   - Debe retornar 404

---

## ALTERNATIVA: OPTIMIZACIÓN FUTURA (Opcional)

Si el rendimiento se vuelve un problema, se puede optimizar agregando un método al gateway de documentos para buscar por nombre directamente en la BD:

**En `GestionarDocumentosGatewayIntPort`:**
```java
Optional<Documento> buscarDocumentoPorNombre(String nombre);
```

**Implementación en `GestionarDocumentoGatewayImplAdapter`:**
```java
@Override
@Transactional(readOnly = true)
public Optional<Documento> buscarDocumentoPorNombre(String nombre) {
    return documentoRepository.findByNombre(nombre)
        .map(entity -> documentoMapper.map(entity, Documento.class));
}
```

**Pero esto requiere:**
- Agregar método al repositorio `DocumentoRepositoryInt`
- Agregar método al gateway
- Actualizar el endpoint para usar este método

**Por ahora, usar la solución de búsqueda iterativa que es más simple y funcional.**

---

## RESULTADO ESPERADO

Después de implementar:

- ✅ El endpoint `/descargar/pdf?filename={nombre}` encuentra archivos en rutas organizadas
- ✅ Funciona con archivos de Homologación, Reingreso, ECAES, Paz y Salvo
- ✅ Mantiene compatibilidad hacia atrás con archivos en la raíz
- ✅ Retorna 404 solo cuando el archivo realmente no existe
- ✅ El error 404 actual se resuelve

---

## NOTAS ADICIONALES

- **No modificar** otros endpoints que ya funcionan correctamente
- **Mantener** la lógica adaptativa para obtener archivos
- **Agregar** logging suficiente para depuración
- **Probar** con archivos de todos los tipos de solicitudes

---

**IMPORTANTE:** Este cambio es crítico porque el frontend usa este endpoint para descargar archivos. Sin esta corrección, los archivos organizados en subcarpetas no serán accesibles desde el frontend.
