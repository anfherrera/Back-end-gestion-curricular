-- Migration: Agregar campos de trabajo de grado a Solicitudes_PazYSalvo
-- Fecha: 2025-12-17

ALTER TABLE Solicitudes_PazYSalvo
ADD COLUMN titulo_trabajo_grado VARCHAR(500) NULL,
ADD COLUMN director_trabajo_grado VARCHAR(200) NULL;

