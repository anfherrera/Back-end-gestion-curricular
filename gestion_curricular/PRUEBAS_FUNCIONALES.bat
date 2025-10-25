@echo off
chcp 65001 > nul
color 0E
cls

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║        PRUEBAS FUNCIONALES - FLUJOS DE NEGOCIO                ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo  📋 Tests a ejecutar: 33 pruebas funcionales
echo.
echo  📁 Módulos:
echo     • Gestión de Paz y Salvo (10 tests)
echo     • Gestión de Cursos de Verano (10 tests)
echo     • Gestión de Estadísticas (13 tests)
echo.
echo  ℹ️  NOTA IMPORTANTE:
echo     Estas pruebas validan FLUJOS COMPLETOS de negocio.
echo     Algunas pueden fallar porque validan funcionalidades
echo     aún no implementadas completamente en el backend.
echo.
echo     ✅ Esto es ESPERADO y demuestra el valor de las pruebas
echo        como especificación ejecutable de requisitos.
echo.
echo ════════════════════════════════════════════════════════════════
echo.
pause

echo.
echo ⏳ Ejecutando pruebas funcionales...
echo.

call mvnw.cmd test -Dtest="*FuncionalTest"

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo  ✅ Ejecución completada
echo.
echo  💡 Interpretación de resultados:
echo     • Tests que PASAN: Funcionalidad implementada correctamente
echo     • Tests que FALLAN: Funcionalidad pendiente o incompleta
echo.
echo  📝 Ambos resultados son valiosos para el desarrollo del sistema
echo.
pause

