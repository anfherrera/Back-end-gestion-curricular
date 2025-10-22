# 🚀 GUÍA RÁPIDA: IMPLEMENTAR PREDICCIONES EN EL FRONTEND

## ⚡ INICIO RÁPIDO (5 MINUTOS)

### **1. Verificar que el Backend está funcionando**

```bash
# Abrir navegador o Postman
GET http://localhost:5000/api/estadisticas/cursos-verano
```

**Buscar en la respuesta:**
```json
{
  "predicciones": {
    "recomendacionesFuturas": [ ... ],  // ✅ Debe existir
    "alertasCriticas": [ ... ],         // ✅ Debe existir
    "estadisticasRecomendaciones": { ... } // ✅ Debe existir
  }
}
```

✅ Si ves estos campos → **Backend OK**  
❌ Si no los ves → Reiniciar backend

---

### **2. Crear el Componente**

```bash
ng generate component dashboard-cursos-verano/predicciones
```

---

### **3. Agregar Ruta**

**`app-routing.module.ts`:**
```typescript
{
  path: 'cursos-verano/predicciones',
  component: PrediccionesComponent
}
```

---

### **4. Agregar Enlace en el Menú**

**Donde esté el menú de Cursos de Verano:**
```html
<a routerLink="/cursos-verano/predicciones" class="nav-link">
  🔮 Predicciones
</a>
```

---

## 📋 CÓDIGO MÍNIMO FUNCIONAL

### **predicciones.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-predicciones',
  templateUrl: './predicciones.component.html',
  styleUrls: ['./predicciones.component.scss']
})
export class PrediccionesComponent implements OnInit {
  recomendaciones: any[] = [];
  alertas: any[] = [];
  stats: any = {};
  
  constructor(private estadisticasService: EstadisticasService) {}
  
  ngOnInit() {
    this.estadisticasService.obtenerEstadisticasCursosVerano().subscribe(
      data => {
        if (data.predicciones) {
          this.recomendaciones = data.predicciones.recomendacionesFuturas || [];
          this.alertas = data.predicciones.alertasCriticas || [];
          this.stats = data.predicciones.estadisticasRecomendaciones || {};
        }
      }
    );
  }
  
  getColor(prioridad: string): string {
    const colores = {
      'CRITICA': '#DC2626',
      'ALTA': '#EF4444',
      'MEDIA': '#F59E0B',
      'BAJA': '#10B981'
    };
    return colores[prioridad] || '#6B7280';
  }
}
```

---

### **predicciones.component.html**

```html
<div class="predicciones-container">
  
  <!-- Resumen -->
  <div class="resumen">
    <h2>📊 Resumen de Recomendaciones</h2>
    <div class="stats-grid">
      <div class="stat-card alta">
        <span class="numero">{{ stats.prioridadAlta || 0 }}</span>
        <span class="label">🔴 ALTAS</span>
      </div>
      <div class="stat-card media">
        <span class="numero">{{ stats.prioridadMedia || 0 }}</span>
        <span class="label">🟡 MEDIAS</span>
      </div>
      <div class="stat-card baja">
        <span class="numero">{{ stats.prioridadBaja || 0 }}</span>
        <span class="label">🟢 BAJAS</span>
      </div>
    </div>
  </div>
  
  <!-- Alertas Críticas -->
  <div class="alertas" *ngIf="alertas.length > 0">
    <div class="alerta-card" *ngFor="let alerta of alertas">
      <h3>⚠️ {{ alerta.titulo }}</h3>
      <p>{{ alerta.descripcion }}</p>
      <ul>
        <li *ngFor="let accion of alerta.acciones">{{ accion }}</li>
      </ul>
    </div>
  </div>
  
  <!-- Recomendaciones -->
  <div class="recomendaciones">
    <div class="rec-card" 
         *ngFor="let rec of recomendaciones"
         [style.border-left-color]="getColor(rec.prioridad)">
      
      <div class="header">
        <span class="prioridad" 
              [style.background-color]="getColor(rec.prioridad)">
          {{ rec.prioridad }}
        </span>
        <h3>{{ rec.titulo }}</h3>
      </div>
      
      <p class="descripcion">{{ rec.descripcion }}</p>
      
      <!-- Proyección -->
      <div class="proyeccion" *ngIf="rec.demandaActual">
        <strong>📈 Proyección:</strong>
        <p>{{ rec.demandaActual }} → {{ rec.demandaProyectada }} estudiantes 
           ({{ rec.porcentajeCrecimiento > 0 ? '+' : '' }}{{ rec.porcentajeCrecimiento }}%)</p>
      </div>
      
      <!-- Recursos -->
      <div class="recursos" *ngIf="rec.recursos">
        <strong>👥 Recursos:</strong>
        <ul>
          <li>{{ rec.recursos.docentes }} docente(s)</li>
          <li>{{ rec.recursos.aulas }} aula(s)</li>
          <li *ngIf="rec.recursos.laboratorios > 0">
            {{ rec.recursos.laboratorios }} laboratorio(s)
          </li>
        </ul>
      </div>
      
      <!-- Acciones -->
      <div class="acciones">
        <strong>✅ Acciones:</strong>
        <ul>
          <li *ngFor="let accion of rec.acciones">
            <input type="checkbox"> {{ accion }}
          </li>
        </ul>
      </div>
      
    </div>
  </div>
  
</div>
```

---

### **predicciones.component.scss**

```scss
.predicciones-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

// Resumen
.resumen {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.stat-card {
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  
  .numero {
    display: block;
    font-size: 2.5rem;
    font-weight: bold;
    margin-bottom: 8px;
  }
  
  .label {
    display: block;
    font-size: 0.9rem;
    opacity: 0.8;
  }
  
  &.alta {
    background: linear-gradient(135deg, #fee2e2, #fecaca);
    color: #991b1b;
  }
  
  &.media {
    background: linear-gradient(135deg, #fef3c7, #fde68a);
    color: #92400e;
  }
  
  &.baja {
    background: linear-gradient(135deg, #d1fae5, #a7f3d0);
    color: #065f46;
  }
}

// Alertas
.alertas {
  margin-bottom: 24px;
}

.alerta-card {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  border: 3px solid #dc2626;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  
  h3 {
    color: #991b1b;
    margin-bottom: 12px;
  }
  
  p {
    color: #7f1d1d;
    margin-bottom: 16px;
  }
  
  ul {
    list-style: none;
    padding: 0;
    
    li {
      padding: 8px 0;
      color: #991b1b;
      font-weight: 600;
      
      &:before {
        content: "🔴 ";
      }
    }
  }
}

// Recomendaciones
.recomendaciones {
  display: grid;
  gap: 20px;
}

.rec-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  border-left: 6px solid #6B7280;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  }
  
  .header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    
    .prioridad {
      padding: 4px 12px;
      border-radius: 20px;
      color: white;
      font-size: 0.75rem;
      font-weight: bold;
      text-transform: uppercase;
    }
    
    h3 {
      margin: 0;
      font-size: 1.25rem;
    }
  }
  
  .descripcion {
    color: #4B5563;
    margin-bottom: 16px;
    line-height: 1.6;
  }
  
  .proyeccion,
  .recursos,
  .acciones {
    margin-top: 16px;
    padding: 12px;
    background: #F9FAFB;
    border-radius: 8px;
    
    strong {
      display: block;
      margin-bottom: 8px;
      color: #1F2937;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        padding: 4px 0;
        color: #4B5563;
      }
    }
  }
  
  .acciones {
    ul {
      list-style: none;
      padding: 0;
      
      li {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 0;
        
        input[type="checkbox"] {
          width: 18px;
          height: 18px;
          cursor: pointer;
        }
      }
    }
  }
}
```

---

## 🎯 RESULTADO ESPERADO

Después de implementar esto, deberías ver:

```
┌────────────────────────────────────────────────────────┐
│  📊 Resumen de Recomendaciones                         │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │   🔴 2   │  │   🟡 4   │  │   🟢 1   │             │
│  │  ALTAS   │  │  MEDIAS  │  │  BAJAS   │             │
│  └──────────┘  └──────────┘  └──────────┘             │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│  🔴 ALTA        📚 Ampliar oferta de Bases de Datos    │
│                                                         │
│  La materia Bases de Datos presenta una tendencia      │
│  de crecimiento del 33%...                             │
│                                                         │
│  📈 Proyección:                                        │
│  3 → 4 estudiantes (+33%)                              │
│                                                         │
│  👥 Recursos:                                          │
│  • 1 docente(s)                                        │
│  • 1 aula(s)                                           │
│  • 1 laboratorio(s)                                    │
│                                                         │
│  ✅ Acciones:                                          │
│  ☐ Contratar 1 docente especializado                   │
│  ☐ Reservar 1 laboratorio de cómputo                   │
│  ☐ Publicar oferta con antelación                      │
└────────────────────────────────────────────────────────┘
```

---

## ✅ CHECKLIST DE VERIFICACIÓN

- [ ] Backend respondiendo en `/api/estadisticas/cursos-verano`
- [ ] Campo `predicciones` existe en la respuesta
- [ ] Componente `PrediccionesComponent` creado
- [ ] Ruta configurada
- [ ] Enlace en el menú agregado
- [ ] Recomendaciones mostrándose en pantalla
- [ ] Colores correctos por prioridad
- [ ] Responsive en mobile

---

## 🐛 TROUBLESHOOTING

### **Problema 1: No veo `predicciones` en la respuesta**
```bash
# Verificar que el backend esté actualizado
cd gestion_curricular
.\mvnw.cmd spring-boot:run
```

### **Problema 2: Error 404 en el endpoint**
```bash
# Verificar que el backend esté corriendo en puerto 5000
netstat -ano | findstr :5000
```

### **Problema 3: `recomendacionesFuturas` está vacío**
```bash
# Normal si no hay suficientes datos históricos
# Verificar logs del backend: buscar "[RECOMENDACIONES]"
```

---

## 📞 AYUDA

**Archivos de referencia completos:**
- `ESTRUCTURA_PREDICCIONES_FRONTEND.md` - Estructura JSON detallada
- `INSTRUCCIONES_FRONTEND_PREDICCIONES.md` - Código completo del componente
- `RESUMEN_PREDICCIONES_IMPLEMENTADAS.md` - Explicación de la funcionalidad

**Endpoint de prueba:**
```
GET http://localhost:5000/api/estadisticas/cursos-verano
```

**Buscar en logs del backend:**
```
[RECOMENDACIONES]
[ALERTAS]
[PREDICCIONES]
```

---

## 🚀 SIGUIENTE NIVEL (OPCIONAL)

Una vez funcione lo básico, puedes agregar:

1. **Filtros avanzados**
   - Por tipo (Materia, Programa, Temporal)
   - Por prioridad (Alta, Media, Baja)
   - Búsqueda por texto

2. **Exportación**
   - PDF con todas las recomendaciones
   - Excel para seguimiento

3. **Seguimiento**
   - Marcar recomendaciones como "completadas"
   - Guardar en localStorage o backend

4. **Gráficos**
   - Visualizar tendencias con Chart.js
   - Mostrar crecimiento proyectado

5. **Notificaciones**
   - Alertas push cuando hay CRÍTICAS
   - Email a coordinadores

---

**¡ÉXITO! 🎉**

Con esta guía rápida deberías tener la funcionalidad básica funcionando en **menos de 30 minutos**.

