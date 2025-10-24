-- =========================================
-- DATOS DE PRUEBA PARA TESTS AUTOMATIZADOS
-- =========================================

-- Programas académicos
INSERT INTO programa (id_programa, codigo_programa, nombre_programa) VALUES 
(1, 'IET', 'Ingeniería Electrónica y Telecomunicaciones'),
(2, 'ICOMP', 'Ingeniería de Computación'),
(3, 'ISIST', 'Ingeniería de Sistemas');

-- Roles
INSERT INTO rol (id_rol, nombre_rol) VALUES 
(1, 'ESTUDIANTE'),
(2, 'FUNCIONARIO'),
(3, 'COORDINADOR'),
(4, 'SECRETARIA'),
(5, 'DECANO');

-- Usuarios de prueba
INSERT INTO usuario (id_usuario, nombre_completo, correo, contrasena, codigo, rol_id_rol, programa_id_programa) VALUES 
-- Estudiante
(1, 'Juan Pérez Estudiante', 'estudiante.test@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', '102010', 1, 1),
-- Funcionario
(2, 'Maria González Funcionario', 'funcionario.test@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', 'F001', 2, NULL),
-- Coordinador
(3, 'Carlos Ramírez Coordinador', 'coordinador.test@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', 'C001', 3, 1),
-- Secretaria
(4, 'Ana López Secretaria', 'secretaria.test@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', 'S001', 4, NULL),
-- Estudiantes adicionales para pruebas de cursos de verano
(5, 'Pedro Gómez', 'pedro.gomez@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', '102011', 1, 1),
(6, 'Laura Martínez', 'laura.martinez@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', '102012', 1, 2);

-- Materias
INSERT INTO materia (id_materia, codigo_materia, nombre_materia, creditos, programa_id_programa) VALUES 
(1, 'MAT101', 'Cálculo Diferencial', 4, 1),
(2, 'FIS101', 'Física I', 4, 1),
(3, 'PROG101', 'Programación I', 3, 2),
(4, 'ALG101', 'Álgebra Lineal', 3, 1),
(5, 'BASE101', 'Bases de Datos', 3, 2);

-- Docentes
INSERT INTO docente (id_docente, nombre_docente, correo_docente) VALUES 
(1, 'Dr. Roberto García', 'roberto.garcia@unicauca.edu.co'),
(2, 'Dra. Sandra Muñoz', 'sandra.munoz@unicauca.edu.co'),
(3, 'Ing. Pablo Castro', 'pablo.castro@unicauca.edu.co');

-- Estados de solicitud
INSERT INTO estado_solicitud (id_estado_solicitud, nombre_estado) VALUES 
(1, 'EN_REVISION_FUNCIONARIO'),
(2, 'EN_REVISION_COORDINADOR'),
(3, 'EN_REVISION_SECRETARIA'),
(4, 'APROBADA'),
(5, 'RECHAZADA'),
(6, 'PENDIENTE');

-- Cursos ofertados para cursos de verano
INSERT INTO curso_ofertado_verano (id_curso_ofertado, nombre_curso, periodo_academico, cupo_maximo, cupo_minimo, materia_id_materia, docente_id_docente) VALUES 
(1, 'Cálculo Diferencial - Verano 2024', '2024-1', 30, 15, 1, 1),
(2, 'Física I - Verano 2024', '2024-1', 25, 12, 2, 2),
(3, 'Programación I - Verano 2024', '2024-1', 35, 15, 3, 3);

-- Estados de cursos ofertados
INSERT INTO estado_curso_ofertado (id_estado_curso, nombre_estado, curso_ofertado_id_curso_ofertado) VALUES 
(1, 'PREINSCRIPCION', 1),
(2, 'PREINSCRIPCION', 2),
(3, 'PREINSCRIPCION', 3);

-- Commit para asegurar que los datos se persisten
COMMIT;

