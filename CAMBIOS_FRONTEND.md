# 📋 CAMBIOS EN BACKEND - ACTUALIZACIÓN REQUERIDA EN FRONTEND

## 🎯 CONTEXTO
Se realizaron cambios en las entidades base compartidas que afectan los procesos de **Reingreso**, **Homologación** y **ECAES**. Estos cambios requieren actualizaciones en el frontend.

---

## ❌ CAMBIOS: CAMPOS ELIMINADOS

### 1. Campo `esSeleccionado` (Boolean) - **ELIMINADO**

**Afecta a:**
- Todas las solicitudes (Reingreso, Homologación, ECAES)

**Acciones en Frontend:**
- ✅ **Eliminar** cualquier referencia a `esSeleccionado` en:
  - Interfaces/Modelos TypeScript/JavaScript
  - Formularios de creación/edición de solicitudes
  - Tablas/listados que muestren este campo
  - Filtros o búsquedas que usen este campo
  - Lógica de negocio que dependa de `esSeleccionado`
  - Validaciones que incluyan este campo

**Ejemplo de código a eliminar:**
```typescript
// ❌ ELIMINAR
interface Solicitud {
  id_solicitud: number;
  nombre_solicitud: string;
  esSeleccionado: boolean; // ← ELIMINAR ESTA LÍNEA
  // ...
}
```

---

## ✅ CAMBIOS: CAMPOS AGREGADOS

### 1. Campo `fecha_ceremonia` (Date) - **AGREGADO**

**Afecta a:**
- Todas las solicitudes (Reingreso, Homologación, ECAES)

**Ubicación en DTOs:**
- `SolicitudDTOPeticion` (Request)
- `SolicitudDTORespuesta` (Response)
- `SolicitudReingresoDTOPeticion` / `SolicitudReingresoDTORespuesta`
- `SolicitudHomologacionDTOPeticion` / `SolicitudHomologacionDTORespuesta`
- `SolicitudEcaesDTOPeticion` / `SolicitudEcaesDTORespuesta`

**Especificaciones:**
- **Tipo:** `Date` (ISO 8601 string en JSON)
- **Obligatorio:** NO (opcional/nullable)
- **Validación Backend:** `@PastOrPresent` (debe ser fecha pasada o presente)
- **Descripción:** Fecha de la ceremonia de graduación (para filtrar por ceremonias: marzo, junio, diciembre, etc.)

**Acciones en Frontend:**
- ✅ **Agregar** el campo `fecha_ceremonia` en:
  - Interfaces/Modelos TypeScript/JavaScript
  - Formularios de creación/edición (campo opcional)
  - Tablas/listados (si se desea mostrar)
  - Filtros (opcional, para filtrar por ceremonia)

**Ejemplo de código a agregar:**
```typescript
// ✅ AGREGAR
interface Solicitud {
  id_solicitud: number;
  nombre_solicitud: string;
  periodo_academico: string;
  fecha_ceremonia?: string | null; // ← AGREGAR (opcional)
  fecha_registro_solicitud: string;
  // ...
}
```

**Ejemplo de formulario:**
```typescript
// En formularios de solicitud
<DatePicker
  label="Fecha de Ceremonia (Opcional)"
  value={formData.fecha_ceremonia}
  onChange={(date) => setFormData({ ...formData, fecha_ceremonia: date })}
  maxDate={new Date()} // Solo fechas pasadas o presente
/>
```

---

### 2. Campo `periodo_academico` (String) - **ACTUALIZADO**

**Afecta a:**
- Todas las solicitudes (Reingreso, Homologación, ECAES)

**Cambios:**
- **Antes:** Opcional (nullable)
- **Ahora:** **OBLIGATORIO** (required, `nullable = false`)
- **Longitud máxima:** 50 caracteres (antes era 10)
- **Formato:** `YYYY-P` donde P es 1 o 2 (ej: "2024-1", "2025-2")
- **Validación Backend:** `@Pattern(regexp = "^\\d{4}-[12]$")`

**Acciones en Frontend:**
- ✅ **Actualizar** validaciones:
  - Hacer el campo **obligatorio** en formularios
  - Validar formato `YYYY-P` (ej: "2024-1", "2025-2")
  - Asegurar que el campo siempre tenga un valor antes de enviar

**Ejemplo de validación:**
```typescript
// Validación en formulario
const validatePeriodoAcademico = (periodo: string): boolean => {
  const pattern = /^\d{4}-[12]$/;
  return pattern.test(periodo);
};

// En el formulario
<TextField
  label="Período Académico *"
  value={formData.periodo_academico}
  onChange={(e) => setFormData({ ...formData, periodo_academico: e.target.value })}
  required
  error={!validatePeriodoAcademico(formData.periodo_academico)}
  helperText="Formato: YYYY-P (ej: 2024-1, 2025-2)"
/>
```

---

### 3. Campo `cedula` (String) - **AGREGADO en Usuario**

**Afecta a:**
- Entidad `Usuario` (usado en todas las solicitudes)

**Ubicación en DTOs:**
- `UsuarioDTOPeticion` (Request)
- `UsuarioDTORespuesta` (Response)

**Especificaciones:**
- **Tipo:** `String`
- **Obligatorio:** **SÍ** (`nullable = false`)
- **Longitud:** 5-20 caracteres
- **Validación Backend:** 
  - `@Size(min = 5, max = 20)`
  - `@Pattern(regexp = "^[0-9]{5,20}$")` (solo números)
- **Unicidad:** Único en la base de datos (`unique = true`)

**Acciones en Frontend:**
- ✅ **Agregar** el campo `cedula` en:
  - Interfaces/Modelos de Usuario
  - Formularios de creación/edición de usuarios
  - Validar que sea obligatorio
  - Validar formato (solo números, 5-20 caracteres)
  - Mostrar mensaje de error si ya existe (cuando el backend retorne error de unicidad)

**Ejemplo de código:**
```typescript
// ✅ AGREGAR
interface Usuario {
  id_usuario?: number;
  nombre_completo: string;
  codigo: string;
  cedula: string; // ← AGREGAR (obligatorio)
  correo: string;
  // ...
}
```

**Ejemplo de formulario:**
```typescript
<TextField
  label="Cédula *"
  value={formData.cedula}
  onChange={(e) => {
    // Solo permitir números
    const value = e.target.value.replace(/\D/g, '');
    if (value.length <= 20) {
      setFormData({ ...formData, cedula: value });
    }
  }}
  required
  error={!formData.cedula || formData.cedula.length < 5}
  helperText="Solo números, entre 5 y 20 caracteres"
/>
```

---

## 📊 ESTRUCTURA ACTUALIZADA DE DTOs

### SolicitudDTOPeticion (Request)
```typescript
interface SolicitudDTOPeticion {
  id_solicitud?: number;
  nombre_solicitud: string; // Required, 3-100 caracteres
  fecha_registro_solicitud: string; // Required, Date ISO 8601
  periodo_academico: string; // Required, formato YYYY-P (ej: "2024-2")
  fecha_ceremonia?: string | null; // Opcional, Date ISO 8601, @PastOrPresent
  estado_actual?: EstadoSolicitudDTOPeticion;
  objUsuario: UsuarioDTOPeticion; // Required
  documentos?: DocumentosDTOPeticion[];
}
```

### SolicitudDTORespuesta (Response)
```typescript
interface SolicitudDTORespuesta {
  id_solicitud: number;
  nombre_solicitud: string;
  periodo_academico: string; // Required
  fecha_ceremonia?: string | null; // Opcional
  fecha_registro_solicitud: string;
  estadosSolicitud?: EstadoSolicitudDTORespuesta[];
  objUsuario?: UsuarioDTORespuesta;
  documentos?: DocumentosDTORespuesta[];
  categoria?: string;
  tipo_solicitud?: string;
}
```

### UsuarioDTOPeticion (Request)
```typescript
interface UsuarioDTOPeticion {
  id_usuario?: number;
  nombre_completo: string; // Required, 3-100 caracteres
  id_rol: number; // Required, mínimo 1
  codigo: string; // Required, 3-50 caracteres
  cedula: string; // Required, 5-20 caracteres, solo números
  correo: string; // Required, formato @unicauca.edu.co
  password?: string;
  estado_usuario?: boolean;
  id_programa: number; // Required, mínimo 1
}
```

### UsuarioDTORespuesta (Response)
```typescript
interface UsuarioDTORespuesta {
  id_usuario: number;
  nombre_completo: string;
  rol?: RolDTORespuesta;
  codigo: string;
  cedula: string; // ← NUEVO
  correo: string;
  estado_usuario: boolean;
  objPrograma?: ProgramaDTORespuesta;
}
```

---

## 🔄 ENDPOINTS AFECTADOS

Los siguientes endpoints ahora retornan/aceptan los nuevos campos:

### Reingreso
- `POST /api/solicitudes-reingreso/crearSolicitud-Reingreso`
- `GET /api/solicitudes-reingreso/listarSolicitudes-Reingreso`
- `GET /api/solicitudes-reingreso/listarSolicitud-Reingreso/id/{id}`
- Todos los endpoints de Reingreso

### Homologación
- `POST /api/solicitudes-homologacion/crearSolicitud-Homologacion`
- `GET /api/solicitudes-homologacion/listarSolicitudes-Homologacion`
- `GET /api/solicitudes-homologacion/listarSolicitud-Homologacion/id/{id}`
- Todos los endpoints de Homologación

### ECAES
- `POST /api/solicitudes-ecaes/crearSolicitud-Ecaes`
- `GET /api/solicitudes-ecaes/listarSolicitudes-Ecaes`
- `GET /api/solicitudes-ecaes/listarSolicitud-Ecaes/id/{id}`
- Todos los endpoints de ECAES

### Usuario
- `POST /api/usuarios/crearUsuario`
- `PUT /api/usuarios/actualizarUsuario/{id}`
- `GET /api/usuarios/listarUsuarios`
- `GET /api/usuarios/buscarUsuario/{id}`
- Todos los endpoints de Usuario

---

## ✅ CHECKLIST DE ACTUALIZACIÓN FRONTEND

### Solicitudes (Reingreso, Homologación, ECAES)
- [ ] Eliminar todas las referencias a `esSeleccionado`
- [ ] Agregar campo `fecha_ceremonia` (opcional) en interfaces
- [ ] Agregar campo `fecha_ceremonia` en formularios (opcional)
- [ ] Actualizar `periodo_academico` a obligatorio
- [ ] Validar formato de `periodo_academico` (YYYY-P)
- [ ] Actualizar longitud máxima de `periodo_academico` a 50 caracteres
- [ ] Actualizar tablas/listados para mostrar nuevos campos (opcional)
- [ ] Actualizar filtros si se desea filtrar por `fecha_ceremonia`

### Usuario
- [ ] Agregar campo `cedula` (obligatorio) en interfaces
- [ ] Agregar campo `cedula` en formularios de creación/edición
- [ ] Validar formato de `cedula` (solo números, 5-20 caracteres)
- [ ] Manejar error de unicidad cuando la cédula ya existe
- [ ] Actualizar tablas/listados para mostrar `cedula`

### Validaciones
- [ ] Validar formato `YYYY-P` para `periodo_academico`
- [ ] Validar formato numérico para `cedula` (5-20 caracteres)
- [ ] Validar `fecha_ceremonia` como fecha pasada o presente (opcional)
- [ ] Asegurar que `periodo_academico` sea obligatorio

### Pruebas
- [ ] Probar creación de solicitudes con nuevos campos
- [ ] Probar creación de usuarios con `cedula`
- [ ] Probar validaciones de formato
- [ ] Probar que `esSeleccionado` ya no se use
- [ ] Probar que `periodo_academico` sea obligatorio

---

## 📝 NOTAS IMPORTANTES

1. **Compatibilidad:** Los campos nuevos son opcionales en algunos casos (`fecha_ceremonia`), pero `periodo_academico` ahora es obligatorio. Asegúrate de que los formularios existentes incluyan este campo.

2. **Migración de datos:** Si hay datos existentes en el frontend que usen `esSeleccionado`, elimínalos. Los datos antiguos pueden no tener `fecha_ceremonia` o `cedula`, así que maneja valores `null` o `undefined`.

3. **Validaciones:** El backend valida los formatos, pero es buena práctica validar también en el frontend para mejor UX.

4. **Procesos NO afectados:** Los cambios NO afectan a:
   - Paz y Salvo
   - Cursos de Verano
   - Estadísticas

---

## 🚀 EJEMPLO DE MIGRACIÓN

### Antes (❌)
```typescript
interface Solicitud {
  id_solicitud: number;
  nombre_solicitud: string;
  esSeleccionado: boolean; // ← Eliminar
  fecha_registro_solicitud: string;
  periodo_academico?: string; // Opcional
  objUsuario: Usuario;
}

interface Usuario {
  id_usuario: number;
  nombre_completo: string;
  codigo: string;
  // Sin cedula
}
```

### Después (✅)
```typescript
interface Solicitud {
  id_solicitud: number;
  nombre_solicitud: string;
  fecha_registro_solicitud: string;
  periodo_academico: string; // ← Ahora obligatorio
  fecha_ceremonia?: string | null; // ← Nuevo (opcional)
  objUsuario: Usuario;
}

interface Usuario {
  id_usuario: number;
  nombre_completo: string;
  codigo: string;
  cedula: string; // ← Nuevo (obligatorio)
}
```

---

**Fecha de actualización:** 2025-01-XX  
**Versión Backend:** Actualizada  
**Procesos afectados:** Reingreso, Homologación, ECAES, Usuario
