-- ==========================================
-- MIGRACIÓN: Agregar columna periodo_academico a EstadosCursos
-- ==========================================
-- ⚠️ IMPORTANTE: Este script SOLO es necesario para PRODUCCIÓN
-- 
-- 📌 ¿Cuándo ejecutarlo?
--   ✅ PRODUCCIÓN: Ejecutar este script ANTES de desplegar la nueva versión
--   ❌ DESARROLLO: NO ejecutar (Hibernate crea la columna automáticamente)
-- 
-- 📌 ¿Cómo ejecutarlo?
--   1. Conectarse a la base de datos de producción
--   2. Ejecutar este script UNA SOLA VEZ
--   3. Luego iniciar la aplicación
-- 
-- 📌 ¿Por qué es necesario?
--   En producción, Hibernate NO modifica la estructura de las tablas (ddl-auto=validate)
--   Por eso necesitas ejecutar este script manualmente para agregar la columna
-- ==========================================

-- Verificar si la columna existe antes de agregarla (idempotente)
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'EstadosCursos' 
               AND COLUMN_NAME = 'periodo_academico');
SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE EstadosCursos ADD COLUMN periodo_academico VARCHAR(10) NULL', 
    'SELECT "Columna periodo_academico ya existe, no es necesario agregarla" AS mensaje');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==========================================
-- NOTAS:
-- ==========================================
-- - El campo periodo_academico es opcional (nullable = true)
-- - Formato esperado: "YYYY-P" (ej: "2025-1", "2025-2")
-- - Para cursos existentes, el periodo_academico será NULL y se calculará automáticamente basándose en la fecha de inicio
-- - Para nuevos cursos, se guardará el periodo_academico proporcionado por la funcionaria
-- ==========================================
