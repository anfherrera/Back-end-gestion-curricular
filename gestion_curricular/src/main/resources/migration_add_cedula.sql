-- Script de migración para agregar campo cédula a la tabla Usuarios
-- Fecha: 2025-01-27
-- Descripción: Agrega el campo cedula como campo opcional y único a la tabla Usuarios

-- Paso 1: Agregar columna cedula (nullable y unique)
ALTER TABLE Usuarios 
ADD COLUMN cedula VARCHAR(20) NULL UNIQUE;

-- Paso 2: Crear índice para mejorar rendimiento en búsquedas por cédula
CREATE INDEX idx_usuarios_cedula ON Usuarios(cedula);

-- Nota: La columna es nullable para permitir que usuarios existentes no tengan cédula inicialmente
-- Se puede actualizar gradualmente con un script de actualización de datos si es necesario

-- Ejemplo de actualización de datos (ejecutar solo si se necesita):
-- UPDATE Usuarios SET cedula = CONCAT('TEMP_', idUsuario) WHERE cedula IS NULL;
-- Luego actualizar con cédulas reales según corresponda

