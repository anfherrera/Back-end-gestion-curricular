-- ==========================================
-- MIGRACIÓN: Agregar columna fecha_fin a EstadosCursos
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
               AND COLUMN_NAME = 'fecha_fin');
SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE EstadosCursos ADD COLUMN fecha_fin DATETIME NULL', 
    'SELECT "Columna fecha_fin ya existe, no es necesario agregarla" AS mensaje');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==========================================
-- NOTAS:
-- ==========================================
-- - El campo fecha_fin es opcional (nullable = true)
-- - Para cursos existentes, la fecha_fin será NULL y se calculará automáticamente como fecha_inicio + 6 semanas
-- - Para nuevos cursos, se guardará la fecha_fin proporcionada por la funcionaria
-- ==========================================