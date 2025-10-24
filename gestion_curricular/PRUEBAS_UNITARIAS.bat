@echo off
REM ========================================
REM Ejecutar solo PRUEBAS UNITARIAS
REM ========================================

echo.
echo ========================================
echo    PRUEBAS UNITARIAS
echo    Paz y Salvo (12 tests)
echo ========================================
echo.

cd /d "%~dp0"

call mvnw.cmd test -Dtest=PazYSalvoUnidadTest

echo.
echo ========================================
echo    PRUEBAS UNITARIAS COMPLETADAS
echo ========================================
echo.

pause

