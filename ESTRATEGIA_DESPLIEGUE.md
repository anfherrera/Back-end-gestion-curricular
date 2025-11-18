# 🚀 Estrategia de Despliegue - Backend Gestión Curricular

## 📊 Situación Actual

### Ramas:
- **`backend`** (desplegada en Render) - Tiene:
  - ✅ Dockerfile para Render
  - ✅ Configuración de CORS para Render
  - ✅ Ajustes de OpenAPI para producción
  - ❌ NO tiene las mejoras de logs
  - ❌ NO tiene auditoría de seguridad completa
  - ❌ NO tiene organización de archivos mejorada

- **`limpia-logs`** (rama actual) - Tiene:
  - ✅ Logs completamente limpiados (SLF4J)
  - ✅ Auditoría de seguridad completa
  - ✅ Rate limiting de login
  - ✅ Organización de archivos por tipo de solicitud
  - ✅ Health checks configurados
  - ✅ Mejoras de seguridad
  - ❌ NO tiene Dockerfile
  - ❌ NO tiene configuraciones específicas de Render

---

## 🎯 Recomendación: **OPCIÓN 1 (Recomendada)**

### **Hacer merge de `backend` → `limpia-logs`, luego actualizar `backend`**

**Ventajas:**
- ✅ Mantienes todas las mejoras de `limpia-logs`
- ✅ Traes las configuraciones de Render
- ✅ `backend` queda actualizada con todo
- ✅ Render sigue desplegando desde `backend` (sin cambios)

**Pasos:**

```bash
# 1. Asegurarte de estar en limpia-logs
git checkout limpia-logs

# 2. Traer los cambios de backend (Dockerfile, configs Render)
git merge backend

# 3. Resolver conflictos si los hay (probablemente solo en OpenAPIConfig)

# 4. Actualizar backend con todas las mejoras
git checkout backend
git merge limpia-logs

# 5. Push a ambas ramas
git push origin limpia-logs
git push origin backend
```

**Render automáticamente detectará el push a `backend` y desplegará las mejoras.**

---

## 🎯 Alternativa: **OPCIÓN 2**

### **Cambiar Render para desplegar desde `limpia-logs`**

**Ventajas:**
- ✅ Más rápido (solo cambiar configuración en Render)
- ✅ No necesitas hacer merge

**Desventajas:**
- ⚠️ Necesitas agregar Dockerfile a `limpia-logs`
- ⚠️ `backend` queda desactualizada

**Pasos:**

1. En Render Dashboard:
   - Ir a tu servicio
   - Settings → Build & Deploy
   - Cambiar "Branch" de `backend` a `limpia-logs`

2. Agregar Dockerfile a `limpia-logs`:
   ```bash
   git checkout backend
   git show backend:Dockerfile > Dockerfile
   git checkout limpia-logs
   git add Dockerfile
   git commit -m "feat: agregar Dockerfile para Render"
   git push origin limpia-logs
   ```

---

## 🎯 Alternativa: **OPCIÓN 3 (Más segura)**

### **Crear rama `backend-v2` desde `limpia-logs` y probar**

**Ventajas:**
- ✅ No afecta `backend` actual (producción sigue funcionando)
- ✅ Puedes probar antes de actualizar producción
- ✅ Rollback fácil si hay problemas

**Pasos:**

```bash
# 1. Crear nueva rama desde limpia-logs
git checkout limpia-logs
git checkout -b backend-v2

# 2. Traer Dockerfile de backend
git checkout backend -- Dockerfile

# 3. Ajustar configuraciones si es necesario
# 4. Push y configurar Render para desplegar desde backend-v2
git push origin backend-v2

# 5. Probar en staging
# 6. Si todo funciona, hacer merge a backend
```

---

## 📋 Checklist Pre-Merge

Antes de hacer merge, verificar:

- [ ] Variables de entorno en Render están configuradas:
  - `SPRING_PROFILES_ACTIVE=prod`
  - `DB_URL` (desde Railway)
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `JWT_SECRET`
  - `CORS_ALLOWED_ORIGINS` (URL de Vercel)

- [ ] Dockerfile existe y está correcto
- [ ] Health checks funcionan (`/actuator/health`)
- [ ] CORS está configurado para el dominio de Vercel

---

## 🔍 Comparación de Cambios

### Cambios en `limpia-logs` que NO están en `backend`:
- ✅ Limpieza completa de logs (SLF4J)
- ✅ Auditoría de seguridad (SecurityAuditService)
- ✅ Rate limiting de login
- ✅ Organización de archivos mejorada
- ✅ Health checks configurados
- ✅ Mejoras en gestión de solicitudes
- ✅ Endpoints de historial para diferentes roles

### Cambios en `backend` que NO están en `limpia-logs`:
- ✅ Dockerfile
- ✅ Configuraciones específicas de Render
- ✅ Ajustes de OpenAPI para producción

---

## 💡 Mi Recomendación Final

**Usar OPCIÓN 1**: Hacer merge de `backend` → `limpia-logs`, luego `limpia-logs` → `backend`

**Razones:**
1. ✅ Mantiene `backend` como rama de producción (convención)
2. ✅ Trae todas las mejoras sin perder configuraciones de Render
3. ✅ Render sigue funcionando sin cambios
4. ✅ Código más limpio y mantenible
5. ✅ Todas las mejoras de seguridad y logs aplicadas

**Tiempo estimado:** 15-20 minutos (incluyendo resolución de conflictos menores)

---

## ⚠️ Importante

Después del merge, **verificar en Render**:
1. El build se completa correctamente
2. La aplicación inicia sin errores
3. Health check responde: `https://tu-app.onrender.com/actuator/health`
4. Los endpoints funcionan correctamente
5. CORS permite peticiones desde Vercel

