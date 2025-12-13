-- Script de migración para agregar campo fecha_ceremonia a la tabla Solicitudes
-- Fecha: 2025-01-27
-- Descripción: Agrega el campo fecha_ceremonia como campo opcional para filtrar por fechas de ceremonias de graduación

-- Verificar si la columna ya existe antes de agregarla
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'Solicitudes' 
               AND COLUMN_NAME = 'fecha_ceremonia');

SET @sql := IF(@exist = 0, 
    'ALTER TABLE Solicitudes ADD COLUMN fecha_ceremonia DATE NULL COMMENT ''Fecha de la ceremonia de graduación (para filtrar por ceremonias: marzo, junio, diciembre, etc.)''',
    'SELECT ''La columna fecha_ceremonia ya existe en la tabla Solicitudes'' AS mensaje');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear índice para mejorar rendimiento en búsquedas por fecha de ceremonia
SET @index_exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                     WHERE TABLE_SCHEMA = DATABASE() 
                     AND TABLE_NAME = 'Solicitudes' 
                     AND INDEX_NAME = 'idx_solicitudes_fecha_ceremonia');

SET @sql_index := IF(@index_exist = 0, 
    'CREATE INDEX idx_solicitudes_fecha_ceremonia ON Solicitudes(fecha_ceremonia)',
    'SELECT ''El índice idx_solicitudes_fecha_ceremonia ya existe'' AS mensaje');

PREPARE stmt_index FROM @sql_index;
EXECUTE stmt_index;
DEALLOCATE PREPARE stmt_index;

-- Nota: La columna es nullable para permitir que solicitudes existentes no tengan fecha de ceremonia
-- Se puede actualizar gradualmente según corresponda

