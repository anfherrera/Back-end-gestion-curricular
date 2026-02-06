# Prompt para BACKEND – Descarga de documento por ID (500 en GET /api/documentos/{id}/descargar)



---

## Objetivo

Implementar o corregir el endpoint de descarga de documento por ID para que **GET /api/documentos/{id}/descargar** responda **200** con el archivo y no **500**, y manejar bien errores (404/400) sin afectar el resto del sistema.

---

## Contexto

El frontend llama a:

- **GET** `/api/documentos/{id}/descargar`  
  (ejemplo: `GET /api/documentos/6/descargar`).

Actualmente la petición devuelve **500 Internal Server Error**. La aplicación usa:

- Entidad/documento con `id`, `nombre`, `ruta_documento` (ruta relativa del archivo en disco, p. ej. `pazysalvo/solicitud_105/archivo.pdf`).
- Un gateway/servicio de archivos que permite obtener el contenido por **ruta relativa** (por ejemplo `getFileByPath(String relativePath)` o similar) y/o por nombre.

---

## Requisitos

1. **Endpoint**  
   - Ruta: **GET** `/api/documentos/{id}/descargar` (por ejemplo en el controlador que ya expone `/api/documentos`, típicamente `DocumentoRestController` o el que centralice documentos).  
   - Parámetro: `id` (ID del documento, entero).  
   - Comportamiento:  
     - Buscar el documento por ID (repositorio/CU de documentos).  
     - Si no existe → responder **404 Not Found**.  
     - Si existe, obtener la ruta del archivo: usar `ruta_documento` si está informada; si no, usar `nombre` como fallback (según cómo esté implementada la resolución de archivos en el proyecto).  
     - Llamar al gateway de archivos (por ejemplo `getFileByPath(rutaRelativa)` o el método que corresponda) para obtener el contenido en bytes.  
     - Si el archivo no existe en disco → responder **404** o **500** según política del proyecto (recomendable 404 con mensaje claro).  
     - Si todo es correcto → responder **200** con el contenido del archivo y cabeceras adecuadas:  
       - `Content-Disposition: attachment; filename="nombre_del_archivo"` (usar el nombre del documento, escapado si es necesario).  
       - `Content-Type` según tipo de archivo (PDF, etc.) o `application/octet-stream` si no se conoce.

2. **Manejo de excepciones**  
   - No devolver 500 genérico sin log; capturar excepciones (archivo no encontrado, IO, documento no encontrado), registrar el error (log) y devolver códigos HTTP adecuados (404 para “no encontrado”, 400 si el ID es inválido, 500 solo para errores inesperados con mensaje controlado).

3. **No romper el resto**  
   - No cambiar el comportamiento de otros endpoints de documentos (p. ej. descarga por nombre en Paz y Salvo, descarga por solicitud, etc.). Solo añadir o corregir este GET por ID.

4. **Estructura del proyecto**  
   - Si el controlador de documentos no tiene inyectado el gateway de archivos, inyectar el puerto/adaptador que permita obtener el archivo por ruta (o nombre) y usarlo solo en este endpoint.

5. **Pruebas**  
   - Probar GET /api/documentos/6/descargar (y otro ID existente) y comprobar que devuelve 200 y el PDF (o archivo) correcto.  
   - Probar con ID inexistente y comprobar que devuelve 404.  
   - Comprobar que en consola del navegador ya no aparece el error 500 para este endpoint cuando se visualiza/descarga un documento.
