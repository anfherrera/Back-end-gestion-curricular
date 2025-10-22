# 📋 INSTRUCCIONES PARA EL FRONTEND - PESTAÑA DE PREDICCIONES

## 🎯 OBJETIVO

Crear una **nueva pestaña de "Predicciones y Recomendaciones"** en el Dashboard de Cursos de Verano que permita a coordinadores y funcionarios:

1. ✅ Ver **recomendaciones accionables** basadas en regresión lineal
2. ✅ Priorizar acciones por **nivel de urgencia** (CRÍTICA, ALTA, MEDIA, BAJA)
3. ✅ Acceder a **recursos necesarios cuantificados** (docentes, aulas, laboratorios)
4. ✅ Seguir **cronogramas específicos** de planificación
5. ✅ Recibir **alertas críticas** ante situaciones de alta demanda

---

## 📡 ENDPOINT A UTILIZAR

El mismo endpoint que ya se usa para el dashboard de Cursos de Verano:

```typescript
GET http://localhost:5000/api/estadisticas/cursos-verano
```

**Respuesta:**
```json
{
  "totalSolicitudes": 9,
  "materias": [...],
  "programas": [...],
  "predicciones": {  // ⭐ NUEVA SECCIÓN
    "recomendacionesFuturas": [ /* Recomendaciones accionables */ ],
    "alertasCriticas": [ /* Alertas urgentes */ ],
    "estadisticasRecomendaciones": {
      "totalRecomendaciones": 7,
      "prioridadAlta": 2,
      "prioridadMedia": 4,
      "prioridadBaja": 1,
      "alertasCriticas": 0
    }
  }
}
```

---

## 🏗️ ESTRUCTURA DEL COMPONENTE

### **Ubicación Sugerida:**

```
src/
  app/
    dashboard-cursos-verano/
      predicciones/
        predicciones.component.ts
        predicciones.component.html
        predicciones.component.scss
```

### **Ruta:**

```typescript
{
  path: 'cursos-verano/predicciones',
  component: PrediccionesComponent,
  canActivate: [AuthGuard]  // Solo coordinadores y funcionarios
}
```

---

## 🎨 DISEÑO DE LA INTERFAZ

### **1. Barra Superior de Resumen**

```
┌────────────────────────────────────────────────────────────────┐
│  📊 RESUMEN DE RECOMENDACIONES                                 │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │   🔴 2   │  │   🟡 4   │  │   🟢 1   │  │   ⚠️ 0   │       │
│  │  ALTAS   │  │  MEDIAS  │  │  BAJAS   │  │ CRÍTICAS │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└────────────────────────────────────────────────────────────────┘
```

**Código:**
```html
<div class="resumen-predicciones">
  <div class="card-resumen card-alta">
    <span class="numero">{{ estadisticas.prioridadAlta }}</span>
    <span class="etiqueta">ALTAS</span>
  </div>
  <div class="card-resumen card-media">
    <span class="numero">{{ estadisticas.prioridadMedia }}</span>
    <span class="etiqueta">MEDIAS</span>
  </div>
  <div class="card-resumen card-baja">
    <span class="numero">{{ estadisticas.prioridadBaja }}</span>
    <span class="etiqueta">BAJAS</span>
  </div>
  <div class="card-resumen card-critica">
    <span class="numero">{{ estadisticas.alertasCriticas }}</span>
    <span class="etiqueta">CRÍTICAS</span>
  </div>
</div>
```

---

### **2. Filtros y Ordenamiento**

```
┌────────────────────────────────────────────────────────────────┐
│  Filtrar por:                                                   │
│  [ Todas ▼ ]  [ Prioridad ▼ ]  [ Categoría ▼ ]                │
│  [ 🔍 Buscar... ]                            [ Exportar PDF ↓ ] │
└────────────────────────────────────────────────────────────────┘
```

---

### **3. Tarjetas de Recomendaciones**

#### **EJEMPLO 1: Recomendación de Materia (ALTA)**

```
┌──────────────────────────────────────────────────────────────┐
│  🔴 PRIORIDAD ALTA                     [Expandir ▼] [✓ Hecho]│
│  📚 Ampliar oferta de Bases de Datos                          │
│  ──────────────────────────────────────────────────────────  │
│                                                                │
│  📈 PROYECCIÓN:                                               │
│  Demanda actual: 3 estudiantes                                │
│  Demanda proyectada: 15 estudiantes (+400%)                   │
│  Crecimiento: +12 estudiantes                                 │
│                                                                │
│  👥 RECURSOS NECESARIOS:                                      │
│  • 1 Docente(s) especializado(s)                              │
│  • 1 Aula(s) o laboratorio(s)                                 │
│  • 1 Grupo(s) de 20 estudiantes                               │
│                                                                │
│  📅 CRONOGRAMA:                                               │
│  ⏰ Inicio gestión: 2 meses antes del período                 │
│  📢 Publicación oferta: 6 semanas antes                       │
│  👨‍🏫 Contratación docentes: 4 semanas antes                   │
│  📝 Inicio inscripciones: 3 semanas antes                     │
│                                                                │
│  ✅ ACCIONES RECOMENDADAS:                                    │
│  ☐ Contratar 1 docente(s) especializado(s)                    │
│  ☐ Reservar 1 aula(s) o laboratorio(s)                        │
│  ☐ Publicar oferta académica con suficiente antelación       │
│                                                                │
│  💡 JUSTIFICACIÓN:                                            │
│  Predicción basada en regresión lineal con datos históricos.  │
│  Modelo: Regresión Lineal Simple.                             │
│  Crecimiento proyectado: +12 estudiantes.                     │
│                                                                │
│  💰 IMPACTO:                                                  │
│  • Estudiantes atendidos: 15                                  │
│  • Estudiantes beneficiados: 12                               │
│  • Cobertura objetivo: 100%                                   │
│  • Inversión estimada: $3,000,000                             │
└──────────────────────────────────────────────────────────────┘
```

**Código HTML:**
```html
<div class="recomendacion-card" 
     [ngClass]="'prioridad-' + rec.prioridad.toLowerCase()">
  
  <!-- Header -->
  <div class="card-header">
    <span class="prioridad-badge">
      {{ getIconoPrioridad(rec.prioridad) }} PRIORIDAD {{ rec.prioridad }}
    </span>
    <div class="acciones-header">
      <button (click)="expandir(rec.id)">Expandir ▼</button>
      <button (click)="marcarCompletada(rec.id)">✓ Hecho</button>
    </div>
  </div>
  
  <!-- Título -->
  <h3 class="titulo">{{ getIconoTipo(rec.tipo) }} {{ rec.titulo }}</h3>
  
  <!-- Proyección -->
  <div class="seccion proyeccion">
    <h4>📈 PROYECCIÓN:</h4>
    <ul>
      <li>Demanda actual: {{ rec.demandaActual }} estudiantes</li>
      <li>Demanda proyectada: {{ rec.demandaProyectada }} estudiantes 
          ({{ rec.porcentajeCrecimiento > 0 ? '+' : '' }}{{ rec.porcentajeCrecimiento }}%)</li>
      <li>Crecimiento: {{ rec.crecimiento > 0 ? '+' : '' }}{{ rec.crecimiento }} estudiantes</li>
    </ul>
  </div>
  
  <!-- Recursos -->
  <div class="seccion recursos" *ngIf="rec.recursos">
    <h4>👥 RECURSOS NECESARIOS:</h4>
    <ul>
      <li>• {{ rec.recursos.docentes }} Docente(s) especializado(s)</li>
      <li>• {{ rec.recursos.aulas }} Aula(s) o laboratorio(s)</li>
      <li>• {{ rec.recursos.gruposRecomendados }} Grupo(s) de {{ rec.recursos.capacidadPorGrupo }} estudiantes</li>
      <li *ngIf="rec.recursos.laboratorios > 0">• {{ rec.recursos.laboratorios }} Laboratorio(s) de cómputo</li>
    </ul>
  </div>
  
  <!-- Cronograma -->
  <div class="seccion cronograma" *ngIf="rec.cronograma && expandido">
    <h4>📅 CRONOGRAMA:</h4>
    <ul>
      <li>⏰ Inicio gestión: {{ rec.cronograma.inicioGestion }}</li>
      <li>📢 Publicación oferta: {{ rec.cronograma.publicacionOferta }}</li>
      <li>👨‍🏫 Contratación docentes: {{ rec.cronograma.contratacionDocentes }}</li>
      <li>📝 Inicio inscripciones: {{ rec.cronograma.inicioInscripciones }}</li>
    </ul>
  </div>
  
  <!-- Acciones -->
  <div class="seccion acciones">
    <h4>✅ ACCIONES RECOMENDADAS:</h4>
    <ul class="checklist">
      <li *ngFor="let accion of rec.acciones">
        <input type="checkbox" [id]="rec.id + '_' + accion">
        <label [for]="rec.id + '_' + accion">{{ accion }}</label>
      </li>
    </ul>
  </div>
  
  <!-- Justificación (Expandida) -->
  <div class="seccion justificacion" *ngIf="expandido">
    <h4>💡 JUSTIFICACIÓN:</h4>
    <p>{{ rec.justificacion }}</p>
  </div>
  
  <!-- Impacto (Expandida) -->
  <div class="seccion impacto" *ngIf="rec.impacto && expandido">
    <h4>💰 IMPACTO:</h4>
    <ul>
      <li>• Estudiantes atendidos: {{ rec.impacto.estudiantesAtendidos }}</li>
      <li>• Estudiantes beneficiados: {{ rec.impacto.estudiantesBeneficiados }}</li>
      <li>• Cobertura objetivo: {{ rec.impacto.tasaCoberturaObjetivo }}</li>
      <li>• Inversión estimada: {{ rec.impacto.inversionEstimada }}</li>
    </ul>
  </div>
  
</div>
```

---

### **4. Alertas Críticas (Si existen)**

```
┌──────────────────────────────────────────────────────────────┐
│  ⚠️ ALERTA CRÍTICA - ACCIÓN INMEDIATA REQUERIDA              │
│  ──────────────────────────────────────────────────────────  │
│                                                                │
│  🔴 Crecimiento excepcional en Programación Avanzada          │
│                                                                │
│  ATENCIÓN: Esta materia presenta un crecimiento proyectado    │
│  del 150%, significativamente superior al promedio.           │
│                                                                │
│  🚨 ACCIONES URGENTES:                                        │
│  ☐ Convocar reunión urgente con coordinación académica        │
│  ☐ Evaluar contratar docentes adicionales urgentemente        │
│  ☐ Verificar espacios físicos alternativos                    │
│  ☐ Considerar modalidad virtual/híbrida                       │
│                                                                │
│  [ Marcar como atendida ]                   [ Ver detalles ]  │
└──────────────────────────────────────────────────────────────┘
```

**Estilo CSS:**
```scss
.alerta-critica {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  border: 3px solid #dc2626;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(220, 38, 38, 0.3);
  animation: pulse 2s infinite;
  
  h3 {
    color: #991b1b;
    font-weight: bold;
    font-size: 1.25rem;
    margin-bottom: 12px;
  }
  
  .descripcion {
    font-size: 1rem;
    color: #7f1d1d;
    margin-bottom: 16px;
  }
  
  .acciones-urgentes {
    background: white;
    border-radius: 8px;
    padding: 16px;
    
    li {
      color: #991b1b;
      font-weight: 600;
      padding: 8px 0;
      
      &:first-child {
        font-size: 1.1rem;
        text-transform: uppercase;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(220, 38, 38, 0.3);
  }
  50% {
    box-shadow: 0 12px 32px rgba(220, 38, 38, 0.5);
  }
}
```

---

## 📊 COLORES Y ESTILOS

### **Paleta de Prioridades:**

```scss
$prioridad-critica: #DC2626;  // Rojo intenso
$prioridad-alta: #EF4444;     // Rojo
$prioridad-media: #F59E0B;    // Amarillo/Naranja
$prioridad-baja: #10B981;     // Verde

.prioridad-critica {
  border-left: 6px solid $prioridad-critica;
  background: linear-gradient(to right, #fee2e2, white);
}

.prioridad-alta {
  border-left: 6px solid $prioridad-alta;
  background: linear-gradient(to right, #fef2f2, white);
}

.prioridad-media {
  border-left: 6px solid $prioridad-media;
  background: linear-gradient(to right, #fef3c7, white);
}

.prioridad-baja {
  border-left: 6px solid $prioridad-baja;
  background: linear-gradient(to right, #d1fae5, white);
}
```

---

## 💻 CÓDIGO COMPLETO DEL COMPONENTE

### **predicciones.component.ts**

```typescript
import { Component, OnInit } from '@angular/core';
import { EstadisticasService } from '../../services/estadisticas.service';

interface Recomendacion {
  id: string;
  tipo: string;
  categoria: string;
  prioridad: string;
  titulo: string;
  materia?: string;
  programa?: string;
  demandaActual?: number;
  demandaProyectada?: number;
  crecimiento?: number;
  porcentajeCrecimiento?: number;
  recursos?: any;
  descripcion: string;
  acciones: string[];
  justificacion: string;
  cronograma?: any;
  impacto?: any;
  beneficios?: string[];
}

@Component({
  selector: 'app-predicciones',
  templateUrl: './predicciones.component.html',
  styleUrls: ['./predicciones.component.scss']
})
export class PrediccionesComponent implements OnInit {
  recomendaciones: Recomendacion[] = [];
  alertasCriticas: any[] = [];
  estadisticas: any = {};
  cargando: boolean = true;
  error: string = '';
  
  filtroTipo: string = 'TODAS';
  filtroPrioridad: string = 'TODAS';
  busqueda: string = '';
  
  recomendacionesExpandidas: Set<string> = new Set();
  recomendacionesCompletadas: Set<string> = new Set();
  
  constructor(private estadisticasService: EstadisticasService) {}
  
  ngOnInit() {
    this.cargarPredicciones();
  }
  
  cargarPredicciones() {
    this.cargando = true;
    this.estadisticasService.obtenerEstadisticasCursosVerano().subscribe(
      (data: any) => {
        console.log('📊 Datos recibidos:', data);
        
        if (data && data.predicciones) {
          this.recomendaciones = data.predicciones.recomendacionesFuturas || [];
          this.alertasCriticas = data.predicciones.alertasCriticas || [];
          this.estadisticas = data.predicciones.estadisticasRecomendaciones || {
            totalRecomendaciones: 0,
            prioridadAlta: 0,
            prioridadMedia: 0,
            prioridadBaja: 0,
            alertasCriticas: 0
          };
          
          // Ordenar por prioridad
          this.ordenarPorPrioridad();
          
          console.log('✅ Recomendaciones cargadas:', this.recomendaciones.length);
          console.log('🚨 Alertas críticas:', this.alertasCriticas.length);
        } else {
          this.error = 'No se encontraron predicciones en la respuesta';
        }
        
        this.cargando = false;
      },
      (error: any) => {
        console.error('❌ Error cargando predicciones:', error);
        this.error = 'Error al cargar las predicciones. Por favor, intente nuevamente.';
        this.cargando = false;
      }
    );
  }
  
  ordenarPorPrioridad() {
    const prioridadOrden: {[key: string]: number} = { 
      'CRITICA': 0, 
      'ALTA': 1, 
      'MEDIA': 2, 
      'BAJA': 3 
    };
    
    this.recomendaciones.sort((a, b) => {
      return prioridadOrden[a.prioridad] - prioridadOrden[b.prioridad];
    });
  }
  
  get recomendacionesFiltradas(): Recomendacion[] {
    let filtradas = this.recomendaciones;
    
    // Filtro por tipo
    if (this.filtroTipo !== 'TODAS') {
      filtradas = filtradas.filter(r => r.tipo === this.filtroTipo);
    }
    
    // Filtro por prioridad
    if (this.filtroPrioridad !== 'TODAS') {
      filtradas = filtradas.filter(r => r.prioridad === this.filtroPrioridad);
    }
    
    // Búsqueda
    if (this.busqueda) {
      const termino = this.busqueda.toLowerCase();
      filtradas = filtradas.filter(r => 
        r.titulo.toLowerCase().includes(termino) ||
        (r.materia && r.materia.toLowerCase().includes(termino)) ||
        (r.programa && r.programa.toLowerCase().includes(termino)) ||
        r.descripcion.toLowerCase().includes(termino)
      );
    }
    
    return filtradas;
  }
  
  expandir(id: string) {
    if (this.recomendacionesExpandidas.has(id)) {
      this.recomendacionesExpandidas.delete(id);
    } else {
      this.recomendacionesExpandidas.add(id);
    }
  }
  
  estaExpandida(id: string): boolean {
    return this.recomendacionesExpandidas.has(id);
  }
  
  marcarCompletada(id: string) {
    if (this.recomendacionesCompletadas.has(id)) {
      this.recomendacionesCompletadas.delete(id);
    } else {
      this.recomendacionesCompletadas.add(id);
    }
  }
  
  estaCompletada(id: string): boolean {
    return this.recomendacionesCompletadas.has(id);
  }
  
  getColorPrioridad(prioridad: string): string {
    switch (prioridad) {
      case 'CRITICA': return '#DC2626';
      case 'ALTA': return '#EF4444';
      case 'MEDIA': return '#F59E0B';
      case 'BAJA': return '#10B981';
      default: return '#6B7280';
    }
  }
  
  getIconoPrioridad(prioridad: string): string {
    switch (prioridad) {
      case 'CRITICA': return '🔴';
      case 'ALTA': return '🔴';
      case 'MEDIA': return '🟡';
      case 'BAJA': return '🟢';
      default: return '⚪';
    }
  }
  
  getIconoTipo(tipo: string): string {
    switch (tipo) {
      case 'OFERTA_MATERIA': return '📚';
      case 'ENFOQUE_PROGRAMA': return '🎓';
      case 'PLANIFICACION_TEMPORAL': return '📅';
      case 'ALERTA_CAPACIDAD': return '⚠️';
      case 'ALERTA_PROGRAMA': return '📊';
      default: return '📄';
    }
  }
  
  exportarPDF() {
    // Implementar exportación a PDF
    console.log('📄 Exportando a PDF...');
  }
  
  exportarExcel() {
    // Implementar exportación a Excel
    console.log('📊 Exportando a Excel...');
  }
}
```

---

## 🚀 PASOS DE IMPLEMENTACIÓN

### **1. Crear el componente** (5 min)
```bash
ng generate component dashboard-cursos-verano/predicciones
```

### **2. Agregar ruta** (2 min)
```typescript
// app-routing.module.ts
{
  path: 'cursos-verano/predicciones',
  component: PrediccionesComponent,
  canActivate: [AuthGuard]
}
```

### **3. Agregar enlace en el menú** (3 min)
```html
<a routerLink="/cursos-verano/predicciones">
  🔮 Predicciones y Recomendaciones
</a>
```

### **4. Copiar código del componente** (10 min)
- Copiar `predicciones.component.ts`
- Copiar `predicciones.component.html`
- Copiar `predicciones.component.scss`

### **5. Probar endpoint** (5 min)
```typescript
console.log('Test:', await fetch('http://localhost:5000/api/estadisticas/cursos-verano').then(r => r.json()));
```

### **6. Ajustar estilos** (15 min)
- Adaptar colores al tema del sistema
- Agregar responsive design
- Optimizar para mobile

**TIEMPO TOTAL ESTIMADO:** ~40 minutos

---

## ✅ CHECKLIST DE VERIFICACIÓN

- [ ] Componente creado y ruta configurada
- [ ] Endpoint `/api/estadisticas/cursos-verano` respondiendo correctamente
- [ ] Recomendaciones mostrándose con colores por prioridad
- [ ] Alertas críticas destacándose visualmente
- [ ] Filtros funcionando (tipo, prioridad, búsqueda)
- [ ] Expansión/colapso de tarjetas funcionando
- [ ] Checklist de acciones interactivo
- [ ] Responsive en mobile y tablet
- [ ] Exportación a PDF/Excel (opcional)
- [ ] Pruebas con coordinadores/funcionarios

---

## 📞 SOPORTE

Si tienes dudas sobre la estructura de los datos, consulta:
- `ESTRUCTURA_PREDICCIONES_FRONTEND.md` (archivo detallado)
- Endpoint de prueba: `GET /api/estadisticas/cursos-verano`
- Logs del backend: Buscar `[RECOMENDACIONES]` y `[ALERTAS]`

---

**¡ÉXITO! 🚀**

Con esta implementación, coordinadores y funcionarios tendrán una herramienta poderosa para la toma de decisiones basada en datos.

