# 🧪 Guía Rápida de Pruebas

## Ejecución Rápida

### Ejecutar todas las pruebas
```bash
cd gestion_curricular
mvn clean test
```

### Ejecutar pruebas por módulo

```bash
# Paz y Salvo - Unitarias (12 tests)
mvn test -Dtest=PazYSalvoUnidadTest

# Estadísticas - Integración (15 tests)
mvn test -Dtest=EstadisticasIntegracionTest
```

## Resumen Rápido

| Módulo | Tests | Ubicación |
|--------|-------|-----------|
| **GEPA4 (Paz y Salvo)** | 12 | `src/test/java/.../pazysalvo/PazYSalvoUnidadTest.java` |
| **ME6 (Estadísticas)** | 15 | `src/test/java/.../estadisticas/EstadisticasIntegracionTest.java` |
| **TOTAL** | **27** | - |

## Resultado Esperado

```
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS ✅
```

## Ver Reporte Completo

📄 Consultar: 
- **RESUMEN_FINAL_PRUEBAS.md** - Resumen ejecutivo actualizado
- **REPORTE_PRUEBAS.md** - Reporte detallado original (referencia)

## Tecnologías

- ✅ JUnit 5
- ✅ Mockito (mocking)
- ✅ Spring Boot Test
- ✅ H2 Database (en memoria)
- ✅ MockMvc
- ✅ AssertJ

## Tipos de Pruebas

- ✅ **Unitarias:** 12 tests (PazYSalvoUnidadTest)
- ✅ **Integración:** 15 tests (EstadisticasIntegracionTest)

✅ **Todas las pruebas pasan exitosamente**

