# 🧪 PRUEBAS AUTOMATIZADAS - GUÍA COMPLETA

## 📋 ÍNDICE RÁPIDO
1. [Cómo ejecutar las pruebas](#-cómo-ejecutar-las-pruebas)
2. [Tipos de pruebas implementadas](#-tipos-de-pruebas-implementadas)
3. [Interpretar resultados](#-cómo-interpretar-los-resultados)
4. [Resumen para trabajo de grado](#-resumen-para-trabajo-de-grado)

---

## 🚀 CÓMO EJECUTAR LAS PRUEBAS

### OPCIÓN 1: Doble Click (Más Fácil) 🖱️

**Para todas las pruebas (28 tests):**
- Doble click en: `EJECUTAR_PRUEBAS.bat`

**Para pruebas unitarias (12 tests):**
- Doble click en: `PRUEBAS_UNITARIAS.bat`

**Para pruebas de integración (15 tests):**
- Doble click en: `PRUEBAS_INTEGRACION.bat`

### OPCIÓN 2: Desde la Terminal 💻

```bash
# 1. Ir a la carpeta del proyecto
cd "D:\Cursos\Trabajo de grado\Back-end-gestion-curricular\gestion_curricular"

# 2. Ejecutar todas las pruebas (28 tests)
.\mvnw.cmd test

# 3. O ejecutar solo las que necesites:
.\mvnw.cmd test -Dtest=PazYSalvoUnidadTest              # Solo unitarias (12)
.\mvnw.cmd test -Dtest=EstadisticasIntegracionTest     # Solo integración (15)
```

### OPCIÓN 3: Desde tu IDE 🎨

**IntelliJ IDEA:**
1. Abre: `src/test/java/.../pazysalvo/PazYSalvoUnidadTest.java`
2. Click derecho → "Run 'PazYSalvoUnidadTest'"
3. Ver resultados en panel inferior

**Visual Studio Code:**
1. Panel "Testing" (ícono 🧪)
2. Click ▶️ en el test que quieras ejecutar

**Eclipse:**
1. Click derecho en archivo de test
2. "Run As" → "JUnit Test"

---

## 📊 TIPOS DE PRUEBAS IMPLEMENTADAS

### RESUMEN GENERAL

| Tipo | Cantidad | Archivo | Tiempo | ¿Qué Prueba? |
|------|----------|---------|--------|--------------|
| **🧩 Unitarias** | 12 | `PazYSalvoUnidadTest` | ~2s | Lógica de negocio aislada |
| **🔗 Integración** | 15 | `EstadisticasIntegracionTest` | ~16s | Controller + Service + BD |
| **✅ Contexto** | 1 | `GestionCurricularApplicationTests` | ~4s | Arranque Spring Boot |
| **📊 TOTAL** | **28** | - | ~35s | Sistema completo |

---

### 🧩 PRUEBAS UNITARIAS (12 tests)

**Archivo:** `src/test/java/.../pazysalvo/PazYSalvoUnidadTest.java`

**¿Qué son?**
Pruebas que validan **solo la lógica de negocio** del caso de uso de Paz y Salvo, **sin base de datos** ni dependencias externas. Usan Mockito para simular (mock) las dependencias.

**Tecnologías:**
- ✅ JUnit 5
- ✅ Mockito (mocking)
- ✅ AssertJ (assertions)

**Lista de tests:**
1. ✅ `testCrearSolicitudPazYSalvoExitoso` - Crear solicitud válida
2. ✅ `testCrearSolicitudAsociaDocumentosHuerfanos` - Asociar documentos automáticamente
3. ✅ `testCrearSolicitudSinUsuario` - Validar error si no hay usuario
4. ✅ `testListarTodasLasSolicitudes` - Listar todas las solicitudes
5. ✅ `testListarSolicitudesParaFuncionario` - Filtrar por rol funcionario
6. ✅ `testListarSolicitudesParaCoordinador` - Filtrar por rol coordinador
7. ✅ `testListarSolicitudesParaSecretaria` - Filtrar por rol secretaria
8. ✅ `testFiltrarSolicitudesPorRolEstudiante` - Filtrar por rol estudiante
9. ✅ `testFiltrarSolicitudesPorRolFuncionario` - Validar filtrado específico
10. ✅ `testSolicitudNulaNoDebeGuardarse` - Validar solicitud nula
11. ✅ `testUsuarioInexistenteNoCreaScolicitud` - Validar usuario inexistente
12. ✅ `testEstadoInicialSeCreaCorrectamente` - Crear estado inicial

**Características:**
- ⚡ **Rápidas** (~2 segundos)
- 🎯 **Aisladas** (sin dependencias externas)
- 🔄 **Repetibles** (siempre dan el mismo resultado)
- 🚫 **NO usan base de datos**

---

### 🔗 PRUEBAS DE INTEGRACIÓN (15 tests)

**Archivo:** `src/test/java/.../estadisticas/EstadisticasIntegracionTest.java`

**¿Qué son?**
Pruebas que validan el **sistema completo** (Controller + Service + Base de Datos). Simulan peticiones HTTP reales y verifican que todo funcione integrado.

**Tecnologías:**
- ✅ Spring Boot Test
- ✅ MockMvc (simulación HTTP)
- ✅ H2 Database (BD en memoria)
- ✅ JsonPath (validación JSON)

**Lista de tests:**

| # | Test | Endpoint | Qué Valida |
|---|------|----------|------------|
| 1 | `testObtenerEstadisticasGlobalesRetorna200` | `GET /api/estadisticas/globales` | Estadísticas generales |
| 2 | `testObtenerEstadisticasPorProcesoRetorna200` | `GET /api/estadisticas/proceso/{tipo}` | Filtro por proceso |
| 3 | `testObtenerEstadisticasPorEstadoRetorna200` | `GET /api/estadisticas/estado/{estado}` | Filtro por estado |
| 4 | `testObtenerEstadisticasPorProgramaRetorna200` | `GET /api/estadisticas/programa/{id}` | Filtro por programa |
| 5 | `testObtenerResumenCompletoRetorna200` | `GET /api/estadisticas/resumen` | Resumen completo |
| 6 | `testObtenerDashboardEjecutivoRetorna200` | `GET /api/estadisticas/dashboard` | Dashboard con KPIs |
| 7 | `testObtenerEstadisticasRendimientoRetorna200` | `GET /api/estadisticas/rendimiento` | Indicadores rendimiento |
| 8 | `testObtenerEstadisticasCursosVeranoRetorna200` | `GET /api/estadisticas/cursos-verano` | Análisis cursos verano |
| 9 | `testExportarEstadisticasPDFRetornaArchivo` | `GET /api/estadisticas/export/pdf` | **Exportación PDF** |
| 10 | `testExportarEstadisticasExcelRetornaArchivo` | `GET /api/estadisticas/export/excel` | **Exportación Excel** |
| 11 | `testObtenerEstadisticasFiltradasRetorna200` | `GET /api/estadisticas/filtradas` | Filtros dinámicos |
| 12 | `testObtenerTotalEstudiantesRetorna200` | `GET /api/estadisticas/estudiantes/total` | Total estudiantes |
| 13 | `testObtenerEstudiantesPorProgramaRetorna200` | `GET /api/estadisticas/estudiantes/programa` | Por programa |
| 14 | `testObtenerConfiguracionEstilos` | `GET /api/estadisticas/config/estilos` | Config dashboard |
| 15 | `testValidarEstructuraRespuestaEstadisticasGlobales` | - | Estructura JSON |

**Características:**
- 🐢 **Más lentas** (~16 segundos)
- 🔗 **Integradas** (todo el stack completo)
- 🗄️ **Usan base de datos** (H2 en memoria)
- 🌐 **Prueban endpoints REST** reales

**¿Por qué incluyen pruebas funcionales?**
Los tests 9 y 10 (exportación PDF/Excel) son **pruebas funcionales** porque validan funcionalidades end-to-end completas del usuario.

---

### ✅ TEST DE CONTEXTO (1 test)

**Archivo:** `GestionCurricularApplicationTests.java`

**¿Qué hace?**
Verifica que la aplicación Spring Boot **arranca correctamente** y todas las dependencias se cargan bien.

```java
@Test
void contextLoads() {
    // Si llega aquí, Spring Boot arrancó exitosamente ✅
}
```

---

## 📈 CÓMO INTERPRETAR LOS RESULTADOS

### ✅ TODO BIEN (Esto es lo que quieres ver)

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running co...pazysalvo.PazYSalvoUnidadTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running co...estadisticas.EstadisticasIntegracionTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS ✅
[INFO] Total time: 35.992 s
```

**Significa:**
- ✅ Las 28 pruebas pasaron exitosamente
- ✅ Sistema funciona correctamente
- ✅ Puedes entregar tu trabajo con confianza

### ❌ ALGO FALLÓ (Necesitas revisar)

```
[ERROR] Tests run: 28, Failures: 2, Errors: 1, Skipped: 0
[ERROR] 
[ERROR] Failures:
[ERROR]   PazYSalvoUnidadTest.testCrearSolicitud:45
[ERROR]     Expected: <APROBADA>
[ERROR]     but was: <PENDIENTE>
[INFO] 
[INFO] BUILD FAILURE ❌
```

**Significa:**
- ❌ 2 tests no pasaron la validación (Failures)
- ❌ 1 test tuvo un error/excepción (Errors)
- 🔍 Revisar línea 45 del test indicado

---

## 📁 VER REPORTES DETALLADOS

### Después de ejecutar las pruebas:

**Ubicación de reportes:**
```
gestion_curricular/target/surefire-reports/
```

**Ver en navegador:**
```bash
start target\surefire-reports\
```

**Contenido:**
- 📄 Archivos `.txt` con logs detallados
- 📄 Archivos `.xml` con resultados (para CI/CD)
- 📊 Resumen de cada test ejecutado

---

## 🎓 RESUMEN PARA TRABAJO DE GRADO

### Para tu documento escrito:

> Se diseñó e implementó una suite de pruebas automatizadas compuesta por **28 casos de prueba** que validan el correcto funcionamiento de los módulos principales del sistema (GEPA4 - Paz y Salvo, y ME6 - Estadísticas).
>
> Las pruebas incluyen:
> - **12 pruebas unitarias** que validan la lógica de negocio del módulo de Paz y Salvo utilizando Mockito para aislar dependencias.
> - **15 pruebas de integración** que validan el módulo de Estadísticas mediante peticiones HTTP simuladas con MockMvc, incluyendo pruebas funcionales de exportación a PDF y Excel.
> - **1 prueba de contexto** que verifica el arranque correcto de la aplicación Spring Boot.
>
> Se utilizó **JUnit 5** como framework de testing, **H2 Database** como base de datos en memoria para las pruebas de integración, y **Mockito** para mocking en pruebas unitarias. Las pruebas se ejecutan automáticamente con Maven y tienen una tasa de éxito del 100%.

### Para tu presentación:

```
📊 PRUEBAS AUTOMATIZADAS

✅ 28 Pruebas Implementadas
   • 12 Unitarias (Paz y Salvo)
   • 15 Integración (Estadísticas)
   • 1 Contexto (Spring Boot)

✅ Tasa de Éxito: 100%

🛠️ Tecnologías:
   • JUnit 5
   • Mockito
   • Spring Boot Test
   • H2 Database
   • MockMvc

⚡ Ejecución: ~35 segundos
```

### Tabla para diapositiva:

| Tipo | Tests | Módulo | Estado |
|------|-------|--------|--------|
| 🧩 Unitarias | 12 | Paz y Salvo | ✅ 100% |
| 🔗 Integración | 15 | Estadísticas | ✅ 100% |
| ⚙️ Funcionales | 2* | Exportación | ✅ 100% |
| **Total** | **28** | Sistema | ✅ **100%** |

*Incluidas en integración

---

## 💡 TIPS Y NOTAS IMPORTANTES

### ✅ NO necesitas:
- ❌ MySQL corriendo (se usa H2 en memoria)
- ❌ Internet después de la primera ejecución
- ❌ Configuración adicional

### ⚡ Primera vez vs. siguientes:
- **Primera ejecución:** 2-3 minutos (descarga dependencias)
- **Siguientes veces:** 30-40 segundos

### 🔄 Limpiar y recompilar:
```bash
.\mvnw.cmd clean test
```

### 📊 Ver solo resumen:
```bash
.\mvnw.cmd test | Select-String "Tests run|BUILD"
```

---

## ❓ PROBLEMAS COMUNES Y SOLUCIONES

### 1. "mvnw.cmd no se reconoce"
**Causa:** No estás en la carpeta correcta  
**Solución:**
```bash
cd "D:\Cursos\Trabajo de grado\Back-end-gestion-curricular\gestion_curricular"
```

### 2. "Java no encontrado"
**Causa:** Java 17 no está instalado o no está en PATH  
**Solución:** Verificar instalación:
```bash
java -version
```
Debe mostrar: `java version "17.x.x"`

### 3. Tests tardan mucho
**Causa:** Es normal, las pruebas de integración arrancan Spring Boot completo  
**Solución:** Esperar 30-40 segundos. Si tarda más de 2 minutos, revisar logs.

### 4. "Port 8080 already in use"
**Causa:** Otro proceso usa el puerto (raro en tests)  
**Solución:** Las pruebas usan puertos aleatorios, no debería pasar. Si ocurre, cerrar otras instancias de la aplicación.

---

## 🎯 CHECKLIST ANTES DE ENTREGAR

Verifica esto antes de presentar tu trabajo:

- [ ] ✅ Ejecutar: `.\mvnw.cmd test`
- [ ] ✅ Verificar: `BUILD SUCCESS`
- [ ] ✅ Verificar: `Tests run: 28, Failures: 0, Errors: 0`
- [ ] ✅ Tomar screenshot de resultados
- [ ] ✅ Incluir este documento en anexos
- [ ] ✅ Mencionar las 28 pruebas en el documento
- [ ] ✅ Preparar diapositiva sobre las pruebas

---

## 🎬 DEMOSTRACIÓN EN VIVO (Opcional)

Si quieres hacer una demo en tu sustentación:

1. Abre tu laptop/proyector
2. Ejecuta: `.\mvnw.cmd test`
3. Muestra la pantalla mientras se ejecutan
4. Señala: "Como pueden ver, las 28 pruebas se ejecutan automáticamente"
5. Al finalizar, muestra: **"BUILD SUCCESS ✅"**
6. Di: *"Todas las pruebas pasan exitosamente, validando el correcto funcionamiento del sistema"*

**Tiempo estimado:** 40-60 segundos

---

## 📚 ARCHIVOS DE REFERENCIA

| Archivo | Descripción |
|---------|-------------|
| `PRUEBAS_AUTOMATIZADAS.md` | Este documento (guía completa) |
| `EJECUTAR_PRUEBAS.bat` | Script para ejecutar todas las pruebas |
| `PRUEBAS_UNITARIAS.bat` | Script para ejecutar solo unitarias |
| `PRUEBAS_INTEGRACION.bat` | Script para ejecutar solo integración |
| `src/test/java/.../pazysalvo/PazYSalvoUnidadTest.java` | Código de pruebas unitarias |
| `src/test/java/.../estadisticas/EstadisticasIntegracionTest.java` | Código de pruebas integración |
| `src/test/resources/application-test.properties` | Configuración de tests |
| `src/test/resources/test-data.sql` | Datos de prueba |

---

## 🎉 CONCLUSIÓN

Has implementado con éxito un sistema de pruebas automatizadas **completo y profesional**:

✅ **28 pruebas** que validan el sistema  
✅ **100% de éxito** en todas las pruebas  
✅ **Tecnologías modernas** (JUnit 5, Mockito, Spring Boot Test)  
✅ **Documentación completa** para tu trabajo de grado  
✅ **Fácil de ejecutar** (doble click o un comando)  

**¡Excelente trabajo! 🎓🚀**

---

**Desarrollado por:** Andrés Felipe Herrera Artunduaga  
**Universidad del Cauca - FIET**  
**Fecha:** Octubre 2024  
**Proyecto:** Sistema de Gestión Curricular - Propuesta 2

