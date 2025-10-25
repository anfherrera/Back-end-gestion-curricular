@echo off
chcp 65001 > nul
color 0A
cls

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║        SISTEMA DE GESTIÓN CURRICULAR                          ║
echo ║        Ejecutar Todas las Pruebas con Limpieza               ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo  🧪 Este script ejecuta un ciclo completo:
echo     1. Limpia compilaciones anteriores
echo     2. Compila el proyecto
echo     3. Ejecuta todas las pruebas (143 tests)
echo.
echo  ⚠️  NOTA: Este proceso puede tomar 2-3 minutos
echo.
echo  💡 Para ejecutar pruebas específicas más rápido:
echo     • PRUEBAS_UNITARIAS.bat      (40 tests, ~5s)
echo     • PRUEBAS_INTEGRACION.bat    (38 tests, ~20s)
echo     • PRUEBAS_FUNCIONALES.bat    (33 tests, ~30s)
echo     • PRUEBAS_ACEPTACION.bat     (32 tests, ~30s)
echo     • PRUEBAS_TODAS.bat          (143 tests, sin limpieza)
echo.
echo ════════════════════════════════════════════════════════════════
echo.
pause

cd /d "%~dp0"

echo.
echo ⏳ [1/3] Limpiando compilaciones anteriores...
call mvnw.cmd clean

echo.
echo ⏳ [2/3] Compilando proyecto...
call mvnw.cmd compile

echo.
echo ⏳ [3/3] Ejecutando todas las pruebas...
echo.
call mvnw.cmd test

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo  ✅ PROCESO COMPLETADO
echo.
echo  📊 Ver resultados detallados en:
echo     target\surefire-reports\
echo.
echo  📝 Documentación de pruebas:
echo     RESUMEN_TESIS_PRUEBAS.md
echo     ESTRATEGIA_PRUEBAS_COMPLETA.md
echo.
pause

