# PROMPT: Agregar Sección "Información de la Solicitud" en Homologación - Frontend

## CONTEXTO DEL CAMBIO EN BACKEND

Se han agregado dos nuevos campos a la entidad `SolicitudHomologacion` en el backend para almacenar información sobre los programas de origen y destino de la homologación:

### Nuevos Campos en Backend

**En `SolicitudHomologacionDTOPeticion` (Request):**
- `programa_origen` (String, opcional, máximo 200 caracteres)
- `programa_destino` (String, opcional, máximo 200 caracteres)

**En `SolicitudHomologacionDTORespuesta` (Response):**
- `programa_origen` (String, opcional)
- `programa_destino` (String, opcional)

**Validaciones Backend:**
- Ambos campos son **opcionales** (`nullable = true`)
- Longitud máxima: **200 caracteres**
- Validación: `@Size(max = 200)`

**Propósito:**
Estos campos se utilizan para reemplazar los placeholders `[PROGRAMA_ORIGEN]` y `[PROGRAMA_DESTINO]` en la plantilla de generación de documentos (oficios/resoluciones de homologación).

---

## OBJETIVO

Agregar una nueva sección en la interfaz de **Estudiante - Homologación** que permita al usuario ingresar la información de los programas de origen y destino. Esta sección debe ser similar a la sección "Información de la Solicitud" que existe en la funcionalidad de **Paz y Salvo** (usar como referencia, **NO modificar** la funcionalidad de Paz y Salvo).

---

## REQUERIMIENTOS

### 1. Ubicación

**Sección:** Estudiante → Homologación → Formulario de creación/edición de solicitud

**Posición:** Agregar una nueva sección llamada **"Información de la Solicitud"** (o similar) en el formulario de homologación.

**Nota:** Esta sección debe ser similar en estructura y diseño a la sección equivalente que existe en Paz y Salvo, pero adaptada para los campos específicos de homologación.

---

### 2. Campos a Agregar

La nueva sección debe contener **dos campos de texto**:

#### Campo 1: Programa de Origen
- **Label:** "Programa de Origen" o "Programa Académico de Origen"
- **Nombre del campo en el formulario:** `programa_origen`
- **Tipo:** Campo de texto (TextField/Input)
- **Validación Frontend:**
  - Máximo 200 caracteres
  - Opcional (no requerido)
  - Mostrar contador de caracteres si es posible
- **Placeholder:** "Ej: Ingeniería de Sistemas"
- **Helper Text:** "Programa académico donde el estudiante cursó las asignaturas que desea homologar"

#### Campo 2: Programa de Destino
- **Label:** "Programa de Destino" o "Programa Académico de Destino"
- **Nombre del campo en el formulario:** `programa_destino`
- **Tipo:** Campo de texto (TextField/Input)
- **Validación Frontend:**
  - Máximo 200 caracteres
  - Opcional (no requerido)
  - Mostrar contador de caracteres si es posible
- **Placeholder:** "Ej: Ingeniería en Automática Industrial"
- **Helper Text:** "Programa académico donde se homologarán las asignaturas"

---

### 3. Estructura de la Sección

La sección debe tener:

1. **Título/Encabezado:** "Información de la Solicitud" o "Datos de la Solicitud"
2. **Diseño:** Similar a la sección de Paz y Salvo (usar como referencia visual)
3. **Layout:** Los dos campos pueden estar:
   - En una sola fila (lado a lado) si el espacio lo permite
   - O en dos filas (uno debajo del otro) si es más apropiado para el diseño responsive
4. **Espaciado:** Mantener consistencia con el resto del formulario

---

### 4. Integración con el Formulario

#### Al Crear Solicitud:
- Los campos deben incluirse en el objeto que se envía al endpoint `POST /api/solicitudes-homologacion/crearSolicitud-Homologacion`
- El objeto `SolicitudHomologacionDTOPeticion` debe incluir:
  ```typescript
  {
    // ... otros campos existentes
    programa_origen: string | null,
    programa_destino: string | null
  }
  ```

#### Al Editar/Visualizar Solicitud:
- Si la solicitud ya tiene valores en `programa_origen` y `programa_destino`, deben mostrarse en los campos
- Los campos deben ser editables si el estado de la solicitud lo permite

#### Al Cargar Solicitud Existente:
- Al obtener una solicitud desde `GET /api/solicitudes-homologacion/listarSolicitud-Homologacion/{id}`, los campos `programa_origen` y `programa_destino` vendrán en la respuesta
- Estos valores deben mostrarse en los campos correspondientes

---

### 5. Validaciones Frontend

```typescript
// Ejemplo de validación (adaptar según el framework usado)
const validaciones = {
  programa_origen: {
    maxLength: 200,
    required: false,
    message: "El programa de origen no puede exceder 200 caracteres"
  },
  programa_destino: {
    maxLength: 200,
    required: false,
    message: "El programa de destino no puede exceder 200 caracteres"
  }
};
```

---

### 6. Referencia Visual

**IMPORTANTE:** Revisar la sección "Información de la Solicitud" en la funcionalidad de **Paz y Salvo** para:
- Ver el diseño y estructura visual
- Mantener consistencia en el estilo
- Usar componentes similares (cards, secciones, etc.)
- Mantener el mismo espaciado y tipografía

**NO modificar** la funcionalidad de Paz y Salvo, solo usarla como referencia visual y estructural.

---

## ESTRUCTURA DEL DTO ACTUALIZADO

### SolicitudHomologacionDTOPeticion (Request)
```typescript
interface SolicitudHomologacionDTOPeticion {
  id_solicitud?: number;
  nombre_solicitud: string;
  fecha_registro_solicitud: Date;
  periodo_academico: string;
  fecha_ceremonia?: Date;
  estado_actual?: EstadoSolicitudDTOPeticion;
  objUsuario: UsuarioDTOPeticion;
  documentos?: DocumentosDTOPeticion[];
  
  // ✅ NUEVOS CAMPOS
  programa_origen?: string | null;      // Máximo 200 caracteres, opcional
  programa_destino?: string | null;     // Máximo 200 caracteres, opcional
}
```

### SolicitudHomologacionDTORespuesta (Response)
```typescript
interface SolicitudHomologacionDTORespuesta {
  id_solicitud: number;
  nombre_solicitud: string;
  periodo_academico: string;
  fecha_ceremonia?: Date;
  fecha_registro_solicitud: Date;
  estadosSolicitud: EstadoSolicitudDTORespuesta[];
  objUsuario: UsuarioDTORespuesta;
  documentos: DocumentosDTORespuesta[];
  categoria: string;
  tipo_solicitud: string;
  
  // ✅ NUEVOS CAMPOS
  programa_origen?: string | null;
  programa_destino?: string | null;
}
```

---

## ENDPOINTS AFECTADOS

### 1. Crear Solicitud
**POST** `/api/solicitudes-homologacion/crearSolicitud-Homologacion`

**Request Body:** Incluir `programa_origen` y `programa_destino` en el objeto enviado.

**Ejemplo:**
```json
{
  "nombre_solicitud": "Solicitud Homologación - Juan Pérez",
  "fecha_registro_solicitud": "2025-01-23",
  "periodo_academico": "2025-1",
  "objUsuario": { ... },
  "documentos": [ ... ],
  "programa_origen": "Ingeniería de Sistemas",
  "programa_destino": "Ingeniería en Automática Industrial"
}
```

### 2. Obtener Solicitud
**GET** `/api/solicitudes-homologacion/listarSolicitud-Homologacion/{id}`

**Response:** Incluirá `programa_origen` y `programa_destino` si están disponibles.

**Ejemplo:**
```json
{
  "id_solicitud": 1,
  "nombre_solicitud": "Solicitud Homologación - Juan Pérez",
  "programa_origen": "Ingeniería de Sistemas",
  "programa_destino": "Ingeniería en Automática Industrial",
  ...
}
```

---

## CONSIDERACIONES IMPORTANTES

1. **Compatibilidad:** Los campos son opcionales, por lo que las solicitudes existentes sin estos valores seguirán funcionando correctamente.

2. **Validación:** Aunque son opcionales, es recomendable validar la longitud máxima (200 caracteres) en el frontend para mejorar la experiencia del usuario.

3. **Diseño Responsive:** Asegurar que la sección se vea bien en dispositivos móviles y tablets.

4. **Accesibilidad:** Mantener las etiquetas (labels) asociadas correctamente a los campos y asegurar que sean accesibles para lectores de pantalla.

5. **Mensajes de Error:** Si el backend retorna un error de validación (por ejemplo, si se excede el límite de caracteres), mostrar el mensaje de error de manera clara al usuario.

---

## RESULTADO ESPERADO

Al finalizar, el formulario de Homologación debe:

- ✅ Tener una nueva sección "Información de la Solicitud" similar a la de Paz y Salvo
- ✅ Incluir dos campos de texto para `programa_origen` y `programa_destino`
- ✅ Validar la longitud máxima de 200 caracteres
- ✅ Enviar estos campos al backend al crear/editar una solicitud
- ✅ Mostrar estos campos al cargar una solicitud existente
- ✅ Mantener consistencia visual con el resto de la aplicación
- ✅ Ser responsive y accesible

---

## NOTAS ADICIONALES

- **No modificar** la funcionalidad de Paz y Salvo, solo usarla como referencia
- Los campos son **opcionales**, por lo que el usuario puede dejar los campos vacíos si lo desea
- Estos valores se usarán automáticamente en la generación de documentos (oficios/resoluciones)
- Si los campos están vacíos, el sistema usará valores por defecto en la generación de documentos

---

**IMPORTANTE:** Si hay dudas sobre la estructura o diseño, revisar la sección equivalente en Paz y Salvo como referencia, pero **NO realizar cambios** en esa funcionalidad.
