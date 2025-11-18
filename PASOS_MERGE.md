# 📝 Pasos para Actualizar Backend con Mejoras

## 🎯 Objetivo
Actualizar la rama `backend` (producción) con todas las mejoras de `limpia-logs`

---

## ✅ Paso 1: Traer Dockerfile a limpia-logs

```bash
# Estás en limpia-logs (verificar)
git checkout limpia-logs

# Traer Dockerfile de backend
git checkout backend -- Dockerfile

# Commit
git add Dockerfile
git commit -m "feat: agregar Dockerfile para despliegue en Render"
```

---

## ✅ Paso 2: Mergear backend → limpia-logs

```bash
# Asegurarte de estar en limpia-logs
git checkout limpia-logs

# Traer cambios de backend (configuraciones de Render)
git merge backend

# Si hay conflictos, resolverlos:
# - OpenAPIConfig.java: Mantener ambas configuraciones (dev y prod)
# - application-prod.properties: Mantener las mejoras de limpia-logs

# Commit del merge
git commit -m "merge: traer configuraciones de Render desde backend"
```

---

## ✅ Paso 3: Actualizar backend con todas las mejoras

```bash
# Cambiar a backend
git checkout backend

# Mergear todas las mejoras de limpia-logs
git merge limpia-logs

# Si hay conflictos menores, resolverlos

# Push a ambas ramas
git push origin limpia-logs
git push origin backend
```

---

## ✅ Paso 4: Verificar en Render

1. Render detectará automáticamente el push a `backend`
2. Iniciará un nuevo build
3. Verificar que el build sea exitoso
4. Verificar que la app inicie correctamente
5. Probar health check: `https://tu-app.onrender.com/actuator/health`

---

## ⚠️ Si hay conflictos

### Conflictos probables:

1. **OpenAPIConfig.java**:
   - Mantener ambas configuraciones de servidores (dev y prod)
   - Asegurar que la URL de producción sea correcta

2. **application-prod.properties**:
   - Mantener las mejoras de `limpia-logs` (health checks, etc.)
   - Verificar que las variables de entorno estén correctas

3. **SeguridadConfig.java**:
   - Mantener las mejoras de seguridad de `limpia-logs`

---

## 🔍 Verificación Post-Merge

```bash
# Verificar que backend tiene todos los cambios
git checkout backend
git log --oneline -5

# Verificar que Dockerfile existe
ls -la Dockerfile

# Verificar que no hay System.out.println
grep -r "System.out.println" gestion_curricular/src/main/java/ | wc -l
# Debe ser 0 (o solo en PasswordHashGenerator que es utilidad)
```

---

## 📋 Checklist Final

- [ ] Dockerfile agregado a limpia-logs
- [ ] Merge backend → limpia-logs completado
- [ ] Merge limpia-logs → backend completado
- [ ] Push realizado a ambas ramas
- [ ] Build en Render exitoso
- [ ] Health check funciona
- [ ] Variables de entorno verificadas en Render
- [ ] CORS configurado para Vercel

