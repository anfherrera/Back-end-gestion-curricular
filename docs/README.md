# 📚 Documentación - Sistema de Predicciones y Recomendaciones

## 🎯 Para el Equipo de Frontend

Este directorio contiene **toda la documentación** necesaria para implementar la **pestaña de Predicciones y Recomendaciones** en el dashboard de Cursos de Verano.

---

## 🚀 INICIO RÁPIDO (30 minutos)

### **👉 EMPEZAR AQUÍ:**

1. **Leer:** [`GUIA_RAPIDA_FRONTEND.md`](./GUIA_RAPIDA_FRONTEND.md) ⚡
2. **Copiar** el código del componente
3. **Crear** el componente en Angular
4. **Probar** el endpoint
5. **¡Listo!**

---

## 📋 DOCUMENTOS DISPONIBLES

| Archivo | Propósito | Tiempo |
|---------|-----------|--------|
| **[GUIA_RAPIDA_FRONTEND.md](./GUIA_RAPIDA_FRONTEND.md)** | Código mínimo funcional para empezar | **30 min** ⚡ |
| **[INSTRUCCIONES_FRONTEND_PREDICCIONES.md](./INSTRUCCIONES_FRONTEND_PREDICCIONES.md)** | Implementación completa con estilos profesionales | **1 hora** |
| **[ESTRUCTURA_PREDICCIONES_FRONTEND.md](./ESTRUCTURA_PREDICCIONES_FRONTEND.md)** | Estructura JSON detallada y TypeScript interfaces | Referencia |
| **[RESUMEN_FINAL_USUARIO.md](./RESUMEN_FINAL_USUARIO.md)** | Resumen ejecutivo de la funcionalidad | Lectura |
| **[RESUMEN_PREDICCIONES_IMPLEMENTADAS.md](./RESUMEN_PREDICCIONES_IMPLEMENTADAS.md)** | Explicación técnica completa | Lectura |

---

## 📡 ENDPOINT

```
GET http://localhost:5000/api/estadisticas/cursos-verano
```

**Estructura de la respuesta:**
```json
{
  "predicciones": {
    "recomendacionesFuturas": [ /* Recomendaciones accionables */ ],
    "alertasCriticas": [ /* Alertas urgentes */ ],
    "estadisticasRecomendaciones": {
      "totalRecomendaciones": 7,
      "prioridadAlta": 2,
      "prioridadMedia": 4,
      "prioridadBaja": 1
    }
  }
}
```

---

## 🎨 RESULTADO ESPERADO

### **Nueva pestaña:** "Predicciones y Recomendaciones"

```
┌────────────────────────────────────────────────┐
│  📊 RESUMEN DE RECOMENDACIONES                 │
│  🔴 2 ALTAS  🟡 4 MEDIAS  🟢 1 BAJA            │
└────────────────────────────────────────────────┘

┌────────────────────────────────────────────────┐
│  🔴 ALTA                                       │
│  📚 Ampliar oferta de Bases de Datos          │
│                                                │
│  📈 Demanda: 3 → 4 estudiantes (+33%)         │
│  👥 Recursos: 1 docente, 1 aula, 1 lab        │
│  💰 Inversión: $3,000,000                     │
│                                                │
│  ✅ ACCIONES:                                  │
│  ☐ Contratar 1 docente especializado          │
│  ☐ Reservar 1 laboratorio                     │
│  ☐ Publicar oferta con 6 semanas antes        │
└────────────────────────────────────────────────┘
```

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [ ] Leer `GUIA_RAPIDA_FRONTEND.md`
- [ ] Verificar que backend esté corriendo (`http://localhost:5000`)
- [ ] Probar endpoint en Postman o navegador
- [ ] Crear componente `PrediccionesComponent`
- [ ] Configurar ruta en Angular
- [ ] Agregar enlace en el menú
- [ ] Copiar código del componente
- [ ] Aplicar estilos
- [ ] Probar en navegador
- [ ] Verificar responsive en mobile

---

## 🐛 TROUBLESHOOTING

### **Problema: No veo el campo `predicciones` en la respuesta**
```bash
# Verificar que el backend esté actualizado
cd gestion_curricular
.\mvnw.cmd spring-boot:run
```

### **Problema: Error 404 en el endpoint**
```bash
# Verificar que el backend esté corriendo
# Debe estar en: http://localhost:5000
```

### **Problema: `recomendacionesFuturas` está vacío**
- Es normal si no hay suficientes datos históricos
- Verificar logs del backend: buscar `[RECOMENDACIONES]`

---

## 📞 SOPORTE

**Preguntas frecuentes:**
1. ¿Cómo funciona la predicción? → Ver `RESUMEN_PREDICCIONES_IMPLEMENTADAS.md`
2. ¿Qué datos necesito? → Ver `ESTRUCTURA_PREDICCIONES_FRONTEND.md`
3. ¿Código completo? → Ver `INSTRUCCIONES_FRONTEND_PREDICCIONES.md`

**Contacto:**
- Revisar issues en el repositorio
- Consultar logs del backend
- Preguntar al equipo de backend

---

## 🎯 VALOR DE LA FUNCIONALIDAD

### **Para coordinadores:**
- ✅ Planificación anticipada (2 meses antes)
- ✅ Recursos cuantificados (docentes, aulas, presupuesto)
- ✅ Cronogramas específicos
- ✅ Decisiones basadas en datos, no intuición

### **Para estudiantes:**
- ✅ Certeza de que habrá cursos
- ✅ Mejor oferta académica
- ✅ Más cupos disponibles

---

## 🚀 TECNOLOGÍAS USADAS

**Backend:**
- Java 17
- Spring Boot 3.5.6
- Apache Commons Math (Regresión Lineal)
- MySQL

**Frontend (sugerido):**
- Angular
- TypeScript
- Tailwind CSS / Material Design
- Chart.js (opcional para gráficos)

---

## 📊 METODOLOGÍA

**Regresión Lineal Simple:**
- Umbral de tendencia: 5% (estándar académico)
- Modelo: Apache Commons Math `SimpleRegression`
- Confiabilidad: MEDIA (basado en datos históricos)

**Tipos de recomendaciones:**
1. **OFERTA_MATERIA:** Recomendaciones por materia específica
2. **ENFOQUE_PROGRAMA:** Recomendaciones estratégicas por programa
3. **PLANIFICACION_TEMPORAL:** Recomendaciones de calendario
4. **ALERTA_CAPACIDAD:** Alertas críticas (crecimiento >50%)
5. **ALERTA_PROGRAMA:** Alertas de demanda concentrada

---

## ✅ ESTADO

- ✅ **Backend:** Completado y funcionando
- ✅ **Endpoint:** Disponible y respondiendo
- ✅ **Documentación:** Completa
- ✅ **Código de ejemplo:** Listo
- ⏳ **Frontend:** Pendiente de implementación

---

## 🎉 ¡ÉXITO!

Con esta documentación, el equipo de frontend tiene **TODO** lo necesario para implementar la funcionalidad en **30 minutos a 1 hora**.

**¡Manos a la obra!** 🚀

---

**Última actualización:** 21 de Octubre de 2025  
**Versión:** 1.0  
**Mantenedor:** Equipo Backend

