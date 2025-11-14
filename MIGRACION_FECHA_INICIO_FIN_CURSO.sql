-- ==========================================
-- MIGRACIÓN: Agregar columnas fecha_inicio, fecha_fin y periodo_academico a Cursos_ofertados
-- ==========================================
-- ⚠️ IMPORTANTE: Este script SOLO es necesario para PRODUCCIÓN
--
-- 📌 ¿Cuándo ejecutarlo?
--   ✅ PRODUCCIÓN: Ejecutar este script ANTES de desplegar la nueva versión
--   ❌ DESARROLLO: NO ejecutar (Hibernate crea las columnas automáticamente)
--
-- 📌 ¿Cómo ejecutarlo?
--   1. Conectarse a la base de datos de producción
--   2. Ejecutar este script UNA SOLA VEZ
--   3. Luego iniciar la aplicación
--
-- 📌 ¿Por qué es necesario?
--   En producción, Hibernate NO modifica la estructura de las tablas (ddl-auto=validate)
--   Por eso necesitas ejecutar este script manualmente para agregar las columnas
-- ==========================================

-- Verificar si la columna fecha_inicio existe antes de agregarla (idempotente)
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'Cursos_ofertados'
               AND COLUMN_NAME = 'fecha_inicio');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE Cursos_ofertados ADD COLUMN fecha_inicio DATETIME NULL',
    'SELECT "Columna fecha_inicio ya existe, no es necesario agregarla" AS mensaje');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar si la columna fecha_fin existe antes de agregarla (idempotente)
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'Cursos_ofertados'
               AND COLUMN_NAME = 'fecha_fin');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE Cursos_ofertados ADD COLUMN fecha_fin DATETIME NULL',
    'SELECT "Columna fecha_fin ya existe, no es necesario agregarla" AS mensaje');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar si la columna periodo_academico existe antes de agregarla (idempotente)
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'Cursos_ofertados'
               AND COLUMN_NAME = 'periodo_academico');
SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE Cursos_ofertados ADD COLUMN periodo_academico VARCHAR(10) NULL',
    'SELECT "Columna periodo_academico ya existe, no es necesario agregarla" AS mensaje');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==========================================
-- MIGRACIÓN DE DATOS: Copiar fechas desde EstadosCursos a Cursos_ofertados
-- ==========================================
-- Si hay cursos existentes, copiar la fecha_inicio desde el primer estado
-- NOTA: fecha_fin y periodo_academico ya NO están en EstadosCursos, solo en Cursos_ofertados

UPDATE Cursos_ofertados co
INNER JOIN (
    SELECT 
        ec.idfkCurso,
        MIN(ec.fecha_registro_estado) AS fecha_inicio
    FROM EstadosCursos ec
    WHERE ec.fecha_registro_estado IS NOT NULL
    GROUP BY ec.idfkCurso
) estados ON co.idCurso = estados.idfkCurso
SET 
    co.fecha_inicio = estados.fecha_inicio
WHERE co.fecha_inicio IS NULL; -- Solo actualizar si no tiene fecha_inicio (evitar sobrescribir)

-- NOTA: fecha_fin y periodo_academico se calcularán automáticamente si no están

-- ==========================================
-- NOTAS:
-- ==========================================
-- - Los campos fecha_inicio, fecha_fin y periodo_academico son opcionales (nullable = true)
-- - Estos campos están SOLO en Cursos_ofertados, NO en EstadosCursos
-- - EstadosCursos solo tiene fecha_registro_estado (fecha en que se registró ese estado)
-- - Para cursos existentes, se copiará fecha_inicio desde el primer estado
-- - Para nuevos cursos, se guardarán directamente en Cursos_ofertados
-- ==========================================

