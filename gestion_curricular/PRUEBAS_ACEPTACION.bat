@echo off
chcp 65001 > nul
color 0B
cls

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║        PRUEBAS DE ACEPTACIÓN (BDD)                            ║
echo ║        Behavior Driven Development                            ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo  📋 Tests a ejecutar: 32 pruebas de aceptación
echo.
echo  📁 Módulos:
echo     • Paz y Salvo - Criterios de Aceptación (8 tests)
echo     • Cursos de Verano - Criterios de Aceptación (8 tests)
echo     • Estadísticas - Criterios de Aceptación (11 tests)
echo.
echo  🎯 Formato BDD:
echo     GIVEN (Dado que): Precondiciones
echo     WHEN (Cuando): Acción ejecutada
echo     THEN (Entonces): Resultado esperado
echo.
echo  ℹ️  NOTA IMPORTANTE:
echo     Estas pruebas documentan CRITERIOS DE ACEPTACIÓN del cliente.
echo     Algunas pueden fallar porque validan requisitos pendientes
echo     de implementación completa.
echo.
echo     ✅ Los fallos identifican áreas de mejora y requisitos
echo        pendientes, demostrando el valor del enfoque BDD.
echo.
echo ════════════════════════════════════════════════════════════════
echo.
pause

echo.
echo ⏳ Ejecutando pruebas de aceptación BDD...
echo.

call mvnw.cmd test -Dtest="*AceptacionTest"

echo.
echo ════════════════════════════════════════════════════════════════
echo.
echo  ✅ Ejecución completada
echo.
echo  💡 Interpretación de resultados:
echo     • Tests que PASAN: Criterio de aceptación cumplido
echo     • Tests que FALLAN: Criterio pendiente de implementar
echo.
echo  📝 Las pruebas BDD sirven como contrato entre negocio y desarrollo
echo.
pause

