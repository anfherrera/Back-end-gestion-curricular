-- Datos compartidos por las pruebas que consultan solicitudes por ID.
INSERT INTO Roles(idRol, nombre) VALUES (1,'Administrador');
INSERT INTO Roles(idRol, nombre) VALUES (2,'Estudiante');
INSERT INTO Roles(idRol, nombre) VALUES (3,'Coordinador');
INSERT INTO Roles(idRol, nombre) VALUES (4,'Secretario');
INSERT INTO Roles(idRol, nombre) VALUES (5,'Funcionario');
INSERT INTO Roles(idRol, nombre) VALUES (6,'Docente');
INSERT INTO Roles(idRol, nombre) VALUES (7,'Decano');
INSERT INTO Programas(idPrograma, codigo, nombre_programa) VALUES (1, '1046', 'Ingenieria de Sistemas');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES
(1, 'FIXTURE01', 'Juan Pérez', 'juan.fixture@example.test', 'sin-acceso', 1, 2, 1, '900000001'),
(5, 'FIXTURE05', 'Pedro Gómez', 'pedro.fixture@example.test', 'sin-acceso', 1, 2, 1, '900000005');

-- Solicitudes de homologación
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, idUsuario, idCurso) VALUES
(1, 'Solicitud de Homologacion - Juan Pérez', '2025-01-15 09:00:00', '2025-1', 1, NULL),
(2, 'Solicitud de Homologacion - Pedro Gómez', '2025-01-16 10:30:00', '2025-1', 5, NULL);

-- Tabla específica de homologación
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES
(1, '/docs/homologacion/juan_pm_fo4.pdf', '/docs/homologacion/juan_contenido.pdf'),
(2, '/docs/homologacion/pedro_pm_fo4.pdf', '/docs/homologacion/pedro_contenido.pdf');

-- Estados de solicitud para homologación
INSERT INTO EstadosSolicitudes (idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES
(10, 'Enviada', '2025-01-15 09:00:00', 1),
(11, 'APROBADA_FUNCIONARIO', '2025-01-16 11:00:00', 1),
(12, 'Enviada', '2025-01-16 10:30:00', 2);

-- =========================================
-- DATOS PARA SOLICITUDES DE REINGRESO
-- =========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, idUsuario, idCurso) VALUES
(3, 'Solicitud de Reingreso - Juan Pérez', '2025-01-17 09:00:00', '2025-1', 1, NULL),
(4, 'Solicitud de Reingreso - Pedro Gómez', '2025-01-18 10:30:00', '2025-1', 5, NULL);

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
(5, 'Solicitud de ECAES - Juan Pérez', '2025-01-19 09:00:00', '2025-1', 1, NULL),
(6, 'Solicitud de ECAES - Pedro Gómez', '2025-01-20 10:30:00', '2025-1', 5, NULL);

-- Tabla específica de ECAES
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES
(5, 'CC', '1020101234', '2020-01-15', '2000-05-20'),
(6, 'CC', '1020115678', '2020-02-10', '2001-03-15');

-- Estados de solicitud para ECAES
INSERT INTO EstadosSolicitudes (idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES
(30, 'Enviada', '2025-01-19 09:00:00', 5),
(31, 'preRegistrado', '2025-01-20 11:00:00', 5),
(32, 'Enviada', '2025-01-20 10:30:00', 6);


-- Cursos consultados por ID en las pruebas de verano.
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (1, '1046','Docente de prueba 1');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (2, '1047','Docente de prueba 2');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (3, '1048','Docente de prueba 3');
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (1, 'BD001', 'Bases de Datos', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (2, 'SIS803', 'Calidad de Software', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (3, 'SIS801', 'Metodologia de la Investigacion', 3);
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon, periodo_academico) VALUES (1, 1, 1, 'A', 30, '221', '2025-2');
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon, periodo_academico) VALUES (2, 2, 2, 'B', 25, '222', '2025-2');
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon, periodo_academico) VALUES (3, 3, 3, 'C', 20, '224', '2025-2');
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (1, 'Borrador', '2025-06-25 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (2, 'Abierto', '2025-06-28 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (3, 'Publicado', '2025-07-01 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (4, 'Preinscripcion', '2025-07-02 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (5, 'Borrador', '2025-06-26 08:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (6, 'Abierto', '2025-06-29 08:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (7, 'Publicado', '2025-07-01 10:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (8, 'Preinscripcion', '2025-07-02 10:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (9, 'Borrador', '2025-06-27 08:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (10, 'Abierto', '2025-06-30 08:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (11, 'Publicado', '2025-07-01 12:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (12, 'Preinscripcion', '2025-07-02 12:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (13, 'Inscripcion', '2025-07-03 08:00:00', 3);

INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (26, 'ADMIN001', 'Administrador del Sistema', 'admin@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', 1, 1, 1, '000009000');

-- Solicitud aprobada usada para generar certificados de paz y salvo.
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, periodo_academico) VALUES (37, 'Solicitud de Paz y Salvo', '2025-08-26 09:20:00', 1, '2025-2');
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (37);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (37, 'APROBADA', '2025-09-06 09:20:00', 37);
