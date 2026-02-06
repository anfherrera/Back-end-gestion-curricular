# Prompt para FRONTEND (Angular) – Error NG0100 en Reingreso y Homologación



---

## Objetivo

Corregir el error **NG0100 (ExpressionChangedAfterItHasBeenCheckedError)** en los componentes de Reingreso y Homologación sin afectar el funcionamiento del sistema.

---

## Contexto

Al crear una solicitud de **reingreso** o de **homologación**, la solicitud se crea correctamente, pero en la consola del navegador (al inspeccionar) aparece:

- `ExpressionChangedAfterItHasBeenCheckedError` en **ReingresoEstudianteComponent** (reingreso-estudiante.component.html, línea 5).
- El mismo error en **HomologacionAsignaturasComponent** (homologacion-asignaturas, línea 5, posición 43).

El valor de la expresión pasa de un **array con documentos** (con `file`, `nombre`, `fecha`, `estado`, etc.) a **`[]`** (array vacío). Eso indica que la lista de documentos cambia después de que Angular ya comprobó la vista en el mismo ciclo de detección de cambios.

---

## Requisitos

1. **Identificar la expresión en la plantilla**  
   En ambos componentes, revisar la línea indicada (aprox. línea 5 del HTML). Ver qué propiedad o expresión se está mostrando (por ejemplo `documentos`, `solicitud.documentos`, etc.) y qué la alimenta en el TypeScript.

2. **Evitar que esa lista cambie después del chequeo**  
   - No reasignar la lista (por ejemplo a `[]`) en el mismo ciclo en que ya se pintó con datos.  
   - Si se vacía o se reasigna tras una petición HTTP o tras cargar la solicitud, hacerlo de forma que no ocurra “después” del render (por ejemplo usando `setTimeout(..., 0)`, `Promise.resolve().then(() => ...)`, o `ChangeDetectorRef.detectChanges()` después de actualizar, según convenga).  
   - Preferir no usar getters que devuelvan listas que puedan cambiar durante el ciclo de detección; mejor usar propiedades que se actualicen en un momento claro (p. ej. al recibir la respuesta del backend).

3. **Mantener el comportamiento actual**  
   La creación de solicitudes y la visualización de documentos deben seguir funcionando igual; solo se debe eliminar el cambio de valor en el mismo ciclo que provoca el NG0100.

4. **Probar**  
   Tras el cambio, crear una solicitud de reingreso y otra de homologación, inspeccionar la consola y confirmar que el error NG0100 ya no aparece y que la lista de documentos se muestra estable.
