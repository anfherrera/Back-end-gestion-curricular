-- Migración: Agregar campo periodo_academico a la tabla Solicitudes
-- Fecha: 2024
-- Descripción: Agrega el campo periodo_academico para permitir filtrar y consultar solicitudes por período académico

-- Verificar si la columna ya existe antes de agregarla
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'Solicitudes' 
               AND COLUMN_NAME = 'periodo_academico');

SET @sql := IF(@exist = 0, 
    'ALTER TABLE Solicitudes ADD COLUMN periodo_academico VARCHAR(10) NULL COMMENT ''Período académico de la solicitud (ej: 2024-2)''',
    'SELECT ''La columna periodo_academico ya existe en la tabla Solicitudes'' AS mensaje');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

