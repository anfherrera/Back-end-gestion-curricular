@echo off
REM ========================================
REM Script para ejecutar pruebas del sistema
REM ========================================

echo.
echo ========================================
echo    SISTEMA DE GESTION CURRICULAR
echo    Ejecutando Pruebas Automatizadas
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] Limpiando compilaciones anteriores...
call mvnw.cmd clean

echo.
echo [2/3] Compilando proyecto...
call mvnw.cmd compile

echo.
echo [3/3] Ejecutando pruebas...
echo.
call mvnw.cmd test

echo.
echo ========================================
echo    PRUEBAS COMPLETADAS
echo ========================================
echo.
echo Ver resultados detallados en:
echo   target\surefire-reports\
echo.

pause

