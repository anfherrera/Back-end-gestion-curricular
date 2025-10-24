@echo off
REM ========================================
REM Ejecutar solo PRUEBAS DE INTEGRACIÓN
REM ========================================

echo.
echo ========================================
echo    PRUEBAS DE INTEGRACION
echo    Estadisticas (15 tests)
echo ========================================
echo.

cd /d "%~dp0"

call mvnw.cmd test -Dtest=EstadisticasIntegracionTest

echo.
echo ========================================
echo    PRUEBAS DE INTEGRACION COMPLETADAS
echo ========================================
echo.

pause

