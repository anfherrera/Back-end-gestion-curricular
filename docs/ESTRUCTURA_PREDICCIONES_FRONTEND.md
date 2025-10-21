# 📊 ESTRUCTURA DE PREDICCIONES Y RECOMENDACIONES ACCIONABLES

## 🎯 Propósito
Este documento describe la estructura JSON de las **recomendaciones accionables** generadas por el backend para su visualización en el frontend en una **pestaña dedicada de Predicciones**.

---

## 📡 Endpoint

```
GET /api/estadisticas/cursos-verano
```

---

## 📋 Estructura JSON Completa

### **Objeto Principal: `predicciones`**

```json
{
  "predicciones": {
    "demandaEstimadaProximoPeriodo": 11,
    "confiabilidad": "MEDIA",
    "fechaPrediccion": "2025-10-21T16:00:00.000+00:00",
    "metodologia": "Regresión Lineal Simple aplicada a datos históricos con umbral de tendencia del 5%",
    
    "estadisticasRecomendaciones": {
      "totalRecomendaciones": 7,
      "prioridadAlta": 2,
      "prioridadMedia": 4,
      "prioridadBaja": 1,
      "alertasCriticas": 0
    },
    
    "recomendacionesFuturas": [ /* Array de recomendaciones */ ],
    "alertasCriticas": [ /* Array de alertas urgentes */ ],
    
    "materiasConTendenciaCreciente": [ /* Array de materias */ ],
    "programasConTendenciaCreciente": [ /* Array de programas */ ],
    "prediccionesTemporales": { /* Datos temporales */ }
  }
}
```

---

## 🎯 1. RECOMENDACIONES POR MATERIA

### **Tipo:** `OFERTA_MATERIA`

```json
{
  "id": "MAT_BASES_DE_DATOS",
  "tipo": "OFERTA_MATERIA",
  "categoria": "CURSOS_VERANO",
  "prioridad": "ALTA",  // ALTA | MEDIA | BAJA
  "titulo": "Ampliar oferta de Bases de Datos",
  "materia": "Bases de Datos",
  
  "demandaActual": 3,
  "demandaProyectada": 15,
  "crecimiento": 12,
  "porcentajeCrecimiento": 400,
  
  "recursos": {
    "docentes": 1,
    "aulas": 1,
    "laboratorios": 1,
    "gruposActuales": 1,
    "gruposRecomendados": 1,
    "capacidadPorGrupo": 20
  },
  
  "descripcion": "La materia Bases de Datos presenta una tendencia de crecimiento del 400%, pasando de 3 a 15 estudiantes proyectados. Se recomienda ampliar la oferta para garantizar cobertura completa.",
  
  "acciones": [
    "Contratar 1 docente(s) especializado(s)",
    "Reservar 1 aula(s) o laboratorio(s)",
    "Publicar oferta académica con suficiente antelación"
  ],
  
  "justificacion": "Predicción basada en regresión lineal con datos históricos. Modelo: Regresión Lineal Simple. Crecimiento proyectado: +12 estudiantes.",
  
  "cronograma": {
    "inicioGestion": "2 meses antes del período",
    "publicacionOferta": "6 semanas antes",
    "contratacionDocentes": "4 semanas antes",
    "inicioInscripciones": "3 semanas antes"
  },
  
  "impacto": {
    "estudiantesAtendidos": 15,
    "estudiantesBeneficiados": 12,
    "tasaCoberturaObjetivo": "100%",
    "inversionEstimada": "$3000000.00"
  }
}
```

---

## 🎓 2. RECOMENDACIONES POR PROGRAMA

### **Tipo:** `ENFOQUE_PROGRAMA`

```json
{
  "id": "PROG_INGENIERIA_DE_SISTEMAS",
  "tipo": "ENFOQUE_PROGRAMA",
  "categoria": "ESTRATEGIA_ACADEMICA",
  "prioridad": "ALTA",
  "titulo": "Priorizar oferta para Ingeniería de Sistemas",
  "programa": "Ingeniería de Sistemas",
  
  "solicitudesActuales": 3,
  "solicitudesProyectadas": 4,
  "incremento": 1,
  "porcentajeIncremento": 33,
  
  "descripcion": "Ingeniería de Sistemas muestra un crecimiento significativo del 33% en la demanda de cursos de verano. Se proyectan 4 solicitudes para el próximo período, con un incremento de 1 estudiantes. Es prioritario garantizar oferta académica suficiente para este programa.",
  
  "acciones": [
    "Ampliar oferta de cursos de verano en 1 cupo(s)",
    "Identificar materias críticas del programa con mayor demanda",
    "Coordinar con director(a) de programa para validar necesidades",
    "Evaluar disponibilidad de docentes del programa"
  ],
  
  "justificacion": "Análisis predictivo indica crecimiento sostenido. Modelo: Regresión Lineal Simple. Representa oportunidad para mejorar indicadores académicos del programa.",
  
  "beneficios": [
    "Reducción de deserción por pérdida de materias",
    "Mejora en tiempos de graduación",
    "Mayor satisfacción estudiantil",
    "Optimización de trayectorias académicas"
  ]
}
```

---

## 📅 3. RECOMENDACIÓN TEMPORAL

### **Tipo:** `PLANIFICACION_TEMPORAL`

```json
{
  "id": "TEMPORAL_AGOSTO",
  "tipo": "PLANIFICACION_TEMPORAL",
  "categoria": "CALENDARIO_ACADEMICO",
  "prioridad": "ALTA",
  "titulo": "Preparar oferta anticipada para Agosto",
  
  "mesPico": "Agosto",
  "solicitudesActuales": 9,
  "solicitudesProyectadas": 11,
  "incrementoEsperado": 2,
  "porcentajeCrecimiento": 22,
  
  "descripcion": "Agosto es el mes pico de demanda para cursos de verano. Se proyecta un incremento del 22%, pasando de 9 a 11 solicitudes. Es fundamental anticipar la planificación académica y logística.",
  
  "acciones": [
    "Publicar calendario de cursos de verano 8 semanas antes de Agosto",
    "Preparar capacidad para 11 estudiantes",
    "Coordinar disponibilidad de docentes con 6 semanas de anticipación",
    "Reservar aulas y laboratorios con 4 semanas de anticipación",
    "URGENTE: Considerar contratación temporal adicional"
  ],
  
  "cronograma": {
    "planificacionInicial": "8 semanas antes",
    "publicacionOferta": "6 semanas antes",
    "aperturaInscripciones": "4 semanas antes",
    "cierreInscripciones": "1 semana antes",
    "inicioCursos": "Agosto"
  },
  
  "justificacion": "Análisis histórico indica que Agosto concentra el mayor volumen de solicitudes. Planificación anticipada garantiza mejor experiencia estudiantil."
}
```

---

## 🚨 4. ALERTAS CRÍTICAS

### **Tipo:** `ALERTA_CAPACIDAD` (Crecimiento >50%)

```json
{
  "id": "ALERTA_CRITICA_PROGRAMACION_AVANZADA",
  "tipo": "ALERTA_CAPACIDAD",
  "categoria": "URGENTE",
  "prioridad": "CRITICA",
  "titulo": "⚠️ ALERTA: Crecimiento excepcional en Programación Avanzada",
  "materia": "Programación Avanzada",
  "crecimientoProyectado": "150%",
  
  "descripcion": "ATENCIÓN: La materia Programación Avanzada presenta un crecimiento proyectado del 150%, significativamente superior al promedio. Se requiere acción inmediata para garantizar cobertura.",
  
  "acciones": [
    "🔴 ACCIÓN INMEDIATA REQUERIDA",
    "Convocar reunión urgente con coordinación académica",
    "Evaluar contratar docentes adicionales con carácter urgente",
    "Verificar disponibilidad de espacios físicos alternativos",
    "Considerar modalidad virtual/híbrida si no hay capacidad presencial"
  ]
}
```

### **Tipo:** `ALERTA_PROGRAMA` (Demanda >10 solicitudes)

```json
{
  "id": "ALERTA_PROGRAMA_INGENIERIA_DE_SISTEMAS",
  "tipo": "ALERTA_PROGRAMA",
  "categoria": "IMPORTANTE",
  "prioridad": "ALTA",
  "titulo": "📊 Demanda concentrada en Ingeniería de Sistemas",
  "programa": "Ingeniería de Sistemas",
  "demandaProyectada": 15,
  
  "descripcion": "Ingeniería de Sistemas concentra una demanda proyectada de 15 solicitudes, requiere estrategia específica.",
  
  "acciones": [
    "Coordinar con director(a) de programa",
    "Identificar materias críticas específicas del programa",
    "Evaluar docentes especializados del programa",
    "Planificar oferta diversificada para atender necesidades del programa"
  ]
}
```

---

## 🎨 VISUALIZACIÓN RECOMENDADA PARA EL FRONTEND

### **Pestaña: "Predicciones y Recomendaciones"**

#### **1. Panel de Resumen (Dashboard Superior)**
```typescript
{
  totalRecomendaciones: 7,
  prioridadAlta: 2,      // Mostrar en rojo
  prioridadMedia: 4,     // Mostrar en amarillo
  prioridadBaja: 1,      // Mostrar en verde
  alertasCriticas: 0     // Mostrar en rojo parpadeante si > 0
}
```

#### **2. Tarjetas de Alertas Críticas (Si existen)**
- **Color:** Rojo intenso (#DC2626)
- **Icono:** ⚠️
- **Posición:** Top de la página
- **Estilo:** Borde parpadeante o animado

#### **3. Tarjetas de Recomendaciones (Ordenadas por prioridad)**

**Prioridad ALTA:**
- Color: Rojo/Naranja (#EF4444)
- Borde grueso
- Icono: 🔴

**Prioridad MEDIA:**
- Color: Amarillo (#F59E0B)
- Borde medio
- Icono: 🟡

**Prioridad BAJA:**
- Color: Verde (#10B981)
- Borde delgado
- Icono: 🟢

#### **Estructura de Cada Tarjeta:**

```
┌──────────────────────────────────────────────────────────────┐
│  🔴 PRIORIDAD ALTA                                 [Ver más ▼]│
│  📚 Ampliar oferta de Bases de Datos                          │
│                                                                │
│  📈 Demanda: 3 → 15 estudiantes (+400%)                       │
│  👥 Grupos recomendados: 1                                    │
│  👨‍🏫 Docentes necesarios: 1                                    │
│  💰 Inversión estimada: $3,000,000                            │
│                                                                │
│  📝 ACCIONES RECOMENDADAS:                                    │
│  ☐ Contratar 1 docente especializado                          │
│  ☐ Reservar 1 laboratorio de cómputo                          │
│  ☐ Publicar oferta con 6 semanas de anticipación             │
│                                                                │
│  📅 Cronograma:                                               │
│  • Inicio gestión: 2 meses antes                              │
│  • Publicación: 6 semanas antes                               │
│  • Contratación: 4 semanas antes                              │
│                                                                │
│  💡 Justificación: Crecimiento proyectado de +12 estudiantes  │
│     basado en regresión lineal con datos históricos           │
└──────────────────────────────────────────────────────────────┘
```

#### **4. Filtros de Visualización**
- **Por tipo:** Materia | Programa | Temporal | Alertas
- **Por prioridad:** Alta | Media | Baja | Crítica
- **Por categoría:** Cursos Verano | Estrategia Académica | Calendario

#### **5. Exportación**
- 📄 PDF de recomendaciones
- 📊 Excel con plan de acción
- 📧 Correo a coordinadores

---

## 💻 CÓDIGO DE EJEMPLO PARA EL FRONTEND

### **TypeScript Interface**

```typescript
interface Recomendacion {
  id: string;
  tipo: 'OFERTA_MATERIA' | 'ENFOQUE_PROGRAMA' | 'PLANIFICACION_TEMPORAL';
  categoria: string;
  prioridad: 'ALTA' | 'MEDIA' | 'BAJA' | 'CRITICA';
  titulo: string;
  materia?: string;
  programa?: string;
  
  demandaActual?: number;
  demandaProyectada?: number;
  crecimiento?: number;
  porcentajeCrecimiento?: number;
  
  recursos?: {
    docentes: number;
    aulas: number;
    laboratorios: number;
    gruposActuales: number;
    gruposRecomendados: number;
    capacidadPorGrupo: number;
  };
  
  descripcion: string;
  acciones: string[];
  justificacion: string;
  
  cronograma?: {
    inicioGestion: string;
    publicacionOferta: string;
    contratacionDocentes: string;
    inicioInscripciones: string;
  };
  
  impacto?: {
    estudiantesAtendidos: number;
    estudiantesBeneficiados: number;
    tasaCoberturaObjetivo: string;
    inversionEstimada: string;
  };
  
  beneficios?: string[];
}

interface AlertaCritica {
  id: string;
  tipo: 'ALERTA_CAPACIDAD' | 'ALERTA_PROGRAMA';
  categoria: string;
  prioridad: 'CRITICA' | 'ALTA';
  titulo: string;
  materia?: string;
  programa?: string;
  descripcion: string;
  acciones: string[];
  crecimientoProyectado?: string;
  demandaProyectada?: number;
}
```

### **Servicio Angular**

```typescript
@Injectable({
  providedIn: 'root'
})
export class PrediccionesService {
  private apiUrl = 'http://localhost:5000/api/estadisticas';
  
  constructor(private http: HttpClient) {}
  
  obtenerPrediccionesCursosVerano(): Observable<any> {
    return this.http.get(`${this.apiUrl}/cursos-verano`);
  }
  
  obtenerRecomendacionesPorPrioridad(prioridad: string): Observable<Recomendacion[]> {
    return this.obtenerPrediccionesCursosVerano().pipe(
      map(data => {
        const recomendaciones = data.predicciones.recomendacionesFuturas || [];
        return recomendaciones.filter((r: Recomendacion) => r.prioridad === prioridad);
      })
    );
  }
  
  obtenerAlertasCriticas(): Observable<AlertaCritica[]> {
    return this.obtenerPrediccionesCursosVerano().pipe(
      map(data => data.predicciones.alertasCriticas || [])
    );
  }
}
```

### **Componente Angular**

```typescript
@Component({
  selector: 'app-predicciones-cursos-verano',
  templateUrl: './predicciones-cursos-verano.component.html'
})
export class PrediccionesCursosVeranoComponent implements OnInit {
  recomendaciones: Recomendacion[] = [];
  alertasCriticas: AlertaCritica[] = [];
  estadisticas: any;
  filtroActivo: string = 'TODAS';
  
  constructor(private prediccionesService: PrediccionesService) {}
  
  ngOnInit() {
    this.cargarPredicciones();
  }
  
  cargarPredicciones() {
    this.prediccionesService.obtenerPrediccionesCursosVerano().subscribe(
      data => {
        this.recomendaciones = data.predicciones.recomendacionesFuturas || [];
        this.alertasCriticas = data.predicciones.alertasCriticas || [];
        this.estadisticas = data.predicciones.estadisticasRecomendaciones || {};
        
        // Ordenar por prioridad
        this.ordenarPorPrioridad();
      },
      error => {
        console.error('Error cargando predicciones:', error);
      }
    );
  }
  
  ordenarPorPrioridad() {
    const prioridadOrden = { 'CRITICA': 0, 'ALTA': 1, 'MEDIA': 2, 'BAJA': 3 };
    this.recomendaciones.sort((a, b) => 
      prioridadOrden[a.prioridad] - prioridadOrden[b.prioridad]
    );
  }
  
  filtrarRecomendaciones(tipo: string) {
    this.filtroActivo = tipo;
    // Implementar lógica de filtro
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
}
```

---

## 📊 DATOS DE EJEMPLO REALES

Con los datos actuales del sistema (9 solicitudes de cursos de verano, 3 materias, 4 programas), el backend generará aproximadamente:

- **3 recomendaciones por materia** (Bases de Datos, Metodología, Calidad de Software)
- **4 recomendaciones por programa** (Ing. Sistemas, Telemática, Electrónica, Automática)
- **1 recomendación temporal** (Agosto como mes pico)
- **0-2 alertas críticas** (dependiendo del crecimiento proyectado)

**Total:** ~7-10 recomendaciones accionables

---

## ✅ BENEFICIOS PARA COORDINADORES Y FUNCIONARIOS

1. ✅ **Planificación Anticipada:** Cronogramas claros con fechas límite
2. ✅ **Recursos Cuantificados:** Número exacto de docentes, aulas, laboratorios
3. ✅ **Priorización:** Sistema de colores y prioridades para decisiones urgentes
4. ✅ **Justificación Técnica:** Basado en regresión lineal, no en intuición
5. ✅ **Impacto Medible:** Estudiantes beneficiados, cobertura, inversión
6. ✅ **Acciones Específicas:** Lista de tareas concretas, no generalidades
7. ✅ **Alertas Proactivas:** Identificación temprana de situaciones críticas

---

## 🚀 PRÓXIMOS PASOS PARA EL FRONTEND

1. Crear componente `PrediccionesCursosVeranoComponent`
2. Diseñar tarjetas responsivas con Tailwind CSS o Material Design
3. Implementar filtros por tipo, prioridad y categoría
4. Agregar funcionalidad de exportación a PDF/Excel
5. Incluir gráficos de tendencias (Chart.js o ApexCharts)
6. Agregar notificaciones push para alertas críticas
7. Sistema de "marcar como completada" para recomendaciones

---

## 📚 DOCUMENTACIÓN ADICIONAL

- **Regresión Lineal:** Umbral de tendencia del 5% (estándar académico)
- **Capacidad por grupo:** 20 estudiantes (configurable)
- **Inversión docente:** $3,000,000 COP por docente/curso
- **Anticipación mínima:** 2 meses para planificación completa

---

**Fecha de creación:** 21 de Octubre de 2025  
**Versión:** 1.0  
**Autor:** Backend - Sistema de Gestión Curricular  
**Contacto:** Coordinación Académica

