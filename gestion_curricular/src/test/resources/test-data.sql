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

-- =========================================
-- DATOS PARA SOLICITUDES DE HOMOLOGACIÓN
-- =========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, idUsuario, idCurso) VALUES 
(1, 'Solicitud de Homologacion - Juan Pérez', '2025-01-15 09:00:00', '2025-1', 1, 1),
(2, 'Solicitud de Homologacion - Pedro Gómez', '2025-01-16 10:30:00', '2025-1', 5, 2);

-- Tabla específica de homologación
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES 
(1, '/docs/homologacion/juan_pm_fo4.pdf', '/docs/homologacion/juan_contenido.pdf'),
(2, '/docs/homologacion/pedro_pm_fo4.pdf', '/docs/homologacion/pedro_contenido.pdf');

-- Estados de solicitud para homologación
-- Nota: La tabla se llama EstadosSolicitudes y la FK es idfkSolicitud
INSERT INTO EstadosSolicitudes (idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES 
(10, 'Enviada', '2025-01-15 09:00:00', 1),
(11, 'APROBADA_FUNCIONARIO', '2025-01-16 11:00:00', 1),
(12, 'Enviada', '2025-01-16 10:30:00', 2);

-- =========================================
-- DATOS PARA SOLICITUDES DE REINGRESO
-- =========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, idUsuario, idCurso) VALUES 
(3, 'Solicitud de Reingreso - Juan Pérez', '2025-01-17 09:00:00', '2025-1', 1, 1),
(4, 'Solicitud de Reingreso - Pedro Gómez', '2025-01-18 10:30:00', '2025-1', 5, 2);

-- Tabla específica de reingreso
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (3), (4);

-- Estados de solicitud para reingreso
INSERT INTO EstadosSolicitudes (idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES 
(20, 'Enviada', '2025-01-17 09:00:00', 3),
(21, 'APROBADA_FUNCIONARIO', '2025-01-18 11:00:00', 3),
(22, 'Enviada', '2025-01-18 10:30:00', 4);

-- =========================================
-- DATOS PARA SOLICITUDES DE ECAES
-- =========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, idUsuario, idCurso) VALUES 
(5, 'Solicitud de ECAES - Juan Pérez', '2025-01-19 09:00:00', '2025-1', 1, 1),
(6, 'Solicitud de ECAES - Pedro Gómez', '2025-01-20 10:30:00', '2025-1', 5, 2);

-- Tabla específica de ECAES
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES 
(5, 'CC', '1020101234', '2020-01-15', '2000-05-20'),
(6, 'CC', '1020115678', '2020-02-10', '2001-03-15');

-- Estados de solicitud para ECAES
INSERT INTO EstadosSolicitudes (idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES 
(30, 'Enviada', '2025-01-19 09:00:00', 5),
(31, 'preRegistrado', '2025-01-20 11:00:00', 5),
(32, 'Enviada', '2025-01-20 10:30:00', 6);

-- =========================================
-- DATOS PARA FECHAS ECAES
-- =========================================
INSERT INTO FechaEcaes(idFechaEcaes, periodoAcademico, inscripcion_est_by_facultad, registro_recaudo_ordinario, registro_recaudo_extraordinario, citacion, aplicacion, resultados_individuales) VALUES 
(1, '2025-1', '2025-02-01', '2025-02-15', '2025-02-20', '2025-03-01', '2025-03-10', '2025-04-15'),
(2, '2025-2', '2025-07-01', '2025-07-15', '2025-07-20', '2025-08-01', '2025-08-10', '2025-09-15');

-- Commit para asegurar que los datos se persisten
COMMIT;

