INSERT INTO Roles(idRol, nombre) VALUES (1,'Administrador');
INSERT INTO Roles(idRol, nombre) VALUES (2,'Estudiante');
INSERT INTO Roles(idRol, nombre) VALUES (3,'Coordinador');
INSERT INTO Roles(idRol, nombre) VALUES (4,'Secretario');
INSERT INTO Roles(idRol, nombre) VALUES (5,'Funcionario');
INSERT INTO Programas(idPrograma, codigo, nombre_programa) VALUES (1,'1046','Ingenieria de Sistemas');
INSERT INTO Programas(idPrograma, codigo, nombre_programa) VALUES (2,'1047','Ingenieria Electronica y Telecomunicaciones');
INSERT INTO Programas(idPrograma, codigo, nombre_programa) VALUES (3,'1048','Ingenieria Automatica Industrial');
INSERT INTO Programas(idPrograma, codigo, nombre_programa) VALUES (4,'1049','Tecnologia en Telematica');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (1, '1046','Pepe Perez');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (2, '1047','Carlos Alberto Ardila Albarracin');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (3, '1048','Carlos Alberto Cobos Lozada');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (4, '1049','Carolina Gonzalez Serrano');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (5, '1050','Cesar Alberto Collazos Ordonez');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (6, '1051','Ember Ubeimar Martinez Flor');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (7, '1052','Erwin Meza Vega');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (8, '1053','Francisco Jose Pino Correa');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (9, '1054','Jorge Jair Moreno Chaustre');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (10, '1055','Julio Ariel Hurtado Alegria');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (11, '1056','Luz Marina Sierra Martinez');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (12, '1057','Martha Eliana Mendoza Becerra');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (13, '1058','Miguel Angel Nino Zambrano');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (14, '1059','Nestor Milciades Diaz Marino');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (15, '1060','Pablo Augusto Mage Imbachi');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (16, '1061','Roberto Carlos Naranjo Cuervo');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (17, '1062','Sandra Milena Roa Martinez');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (18, '1063','Siler Amador Donado');
INSERT INTO Docentes(idDocente, codigo_docente, nombre_docente) VALUES (19, '1064','Wilson Libardo Pantoja Yepez');

-- Datos de prueba para cursos de verano
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (1, 'BD001', 'Bases de Datos', 3);

-- Agregar más solicitudes de cursos de verano para probar la actualización automática
-- NOTA: Estas líneas están comentadas porque usan columnas obsoletas (fecha_solicitud, estado, tipo_solicitud, etc.) que no existen en la entidad actual
-- INSERT INTO Solicitudes(idSolicitud, fecha_solicitud, estado, observaciones, idUsuario, idMateria, idPrograma, tipo_solicitud) VALUES (15, '2025-08-20', 'Aprobada', 'Nueva solicitud de curso de verano - Bases de Datos', 1, 1, 1, 'CURSO_VERANO');
-- INSERT INTO Solicitudes(idSolicitud, fecha_solicitud, estado, observaciones, idUsuario, idMateria, idPrograma, tipo_solicitud) VALUES (16, '2025-08-21', 'Enviada', 'Nueva solicitud de curso de verano - Calidad de Software', 2, 2, 1, 'CURSO_VERANO');
-- INSERT INTO Solicitudes(idSolicitud, fecha_solicitud, estado, observaciones, idUsuario, idMateria, idPrograma, tipo_solicitud) VALUES (17, '2025-08-22', 'En Proceso', 'Nueva solicitud de curso de verano - Metodología de la Investigación', 3, 3, 2, 'CURSO_VERANO');
-- INSERT INTO Solicitudes(idSolicitud, fecha_solicitud, estado, observaciones, idUsuario, idMateria, idPrograma, tipo_solicitud) VALUES (18, '2025-08-23', 'Aprobada', 'Nueva solicitud de curso de verano - Bases de Datos', 4, 1, 3, 'CURSO_VERANO');
-- INSERT INTO Solicitudes(idSolicitud, fecha_solicitud, estado, observaciones, idUsuario, idMateria, idPrograma, tipo_solicitud) VALUES (19, '2025-08-24', 'Rechazada', 'Nueva solicitud de curso de verano - Calidad de Software', 5, 2, 4, 'CURSO_VERANO');

-- Materias del programa de Ingeniería de Sistemas
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (2, 'SIS803', 'Calidad de Software', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (3, 'SIS801', 'Metodologia de la Investigacion', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (4, 'SIS706', 'Teoria y Dinamica de Sistemas', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (5, 'SIS703', 'Ingenieria de Software III', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (6, 'SIS702', 'Sistemas Distribuidos', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (7, 'SIS603', 'Sistemas Operativos', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (8, 'SIS602', 'Ingenieria de Software II', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (9, 'SIS601', 'Estructura de Lenguajes', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (10, 'SIS504', 'Bases de Datos II', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (11, 'SIS503', 'Ingenieria de Software I', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (12, 'SIS502', 'Arquitectura Computacional', 4);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (13, 'SIS501', 'Teoria de la Computacion', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (14, 'SIS402', 'Bases de Datos I', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (15, 'SIS401', 'Estructura de Datos II', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (16, 'SIS301', 'Estructuras de Datos I', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (17, 'SIS201', 'Programacion Orientada a Objetos', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (18, 'MAT131', 'Estadistica y Probabilidad', 4);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (19, '333-1', 'Inteligencia Artificial', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (20, '328-1', 'Analisis Numerico', 4);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (21, '323-1', 'Vibraciones y Ondas', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (22, '322-1', 'Ecuaciones Diferenciales Ordinarias', 4);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (23, '317-1', 'Electromagnetismo', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (24, '316-1', 'Calculo III', 4);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (25, '312-1', 'Mecanica', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (26, '311-1', 'Calculo II', 4);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (27, '307', 'Algebra Lineal', 4);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (28, '21505', 'Lectura y Escritura', 2);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (29, '11479', 'Introduccion a la Ingenieria de Sistemas', 1);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (30, '11477', 'Introduccion a la Informatica', 3);
INSERT INTO Materias(idMateria, codigo, nombre, creditos) VALUES (31, '11465', 'Calculo I', 4);

INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (1, 'EST001', 'Juan Pérez', 'juan.perez@unicauca.edu.co', 'password123', 1, 2, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (2, 'EST002', 'María García', 'maria.garcia@unicauca.edu.co', 'password123', 1, 2, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (100, 'EST003', 'Ana Sofía Rodríguez', 'ana.rodriguez@unicauca.edu.co', 'password123', 1, 2, 3);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (101, 'EST004', 'Carlos Eduardo Martínez', 'carlos.martinez@unicauca.edu.co', 'password123', 1, 2, 4);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (102, 'EST005', 'Laura Valentina López', 'laura.lopez@unicauca.edu.co', 'password123', 1, 2, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (103, 'EST006', 'Diego Alejandro Herrera', 'diego.herrera@unicauca.edu.co', 'password123', 1, 2, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (104, 'EST007', 'Valentina Morales', 'valentina.morales@unicauca.edu.co', 'password123', 1, 2, 3);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (3, 'DOC001', 'Carlos López', 'carlos.lopez@unicauca.edu.co', 'password123', 1, 1, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (4, '1047', 'Carlos Alberto Ardila Albarracin', 'cardila@unicauca.edu.co', 'password123', 1, 1, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (5, '1048', 'Carlos Alberto Cobos Lozada', 'ccobos@unicauca.edu.co', 'password123', 1, 1, 3);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (6, '1049', 'Carolina Gonzalez Serrano', 'cgonzals@unicauca.edu.co', 'password123', 1, 1, 4);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (7, '1050', 'Cesar Alberto Collazos Ordonez', 'ccollazo@unicauca.edu.co', 'password123', 1, 1, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (8, '1051', 'Ember Ubeimar Martinez Flor', 'eumartinez@unicauca.edu.co', 'password123', 1, 1, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (9, '1052', 'Erwin Meza Vega', 'emezav@unicauca.edu.co', 'password123', 1, 1, 3);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (10, '1053', 'Francisco Jose Pino Correa', 'fjpino@unicauca.edu.co', 'password123', 1, 1, 4);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (11, '1054', 'Jorge Jair Moreno Chaustre', 'jjmoreno@unicauca.edu.co', 'password123', 1, 1, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (12, '1055', 'Julio Ariel Hurtado Alegria', 'ahurtado@unicauca.edu.co', 'password123', 1, 1, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (13, '1056', 'Luz Marina Sierra Martinez', 'lsierra@unicauca.edu.co', 'password123', 1, 1, 3);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (14, '1057', 'Martha Eliana Mendoza Becerra', 'mmendoza@unicauca.edu.co', 'password123', 1, 1, 4);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (15, '1058', 'Miguel Angel Nino Zambrano', 'manzamb@unicauca.edu.co', 'password123', 1, 1, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (16, '1059', 'Nestor Milciades Diaz Marino', 'nediaz@unicauca.edu.co', 'password123', 1, 1, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (17, '1060', 'Pablo Augusto Mage Imbachi', 'pmage@unicauca.edu.co', 'password123', 1, 1, 3);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (18, '1061', 'Roberto Carlos Naranjo Cuervo', 'rnaranjo@unicauca.edu.co', 'password123', 1, 1, 4);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (19, '1062', 'Sandra Milena Roa Martinez', 'smroa@unicauca.edu.co', 'password123', 1, 1, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (20, '1063', 'Siler Amador Donado', 'samador@unicauca.edu.co', 'password123', 1, 1, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (21, '1064', 'Wilson Libardo Pantoja Yepez', 'wpantoja@unicauca.edu.co', 'password123', 1, 1, 3);

-- Usuarios de prueba para roles de funcionarios
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (22, 'FUN001', 'Ana María Rodríguez', 'arodriguez@unicauca.edu.co', 'password123', 1, 5, 1);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (23, 'FUN002', 'Pedro Sánchez López', 'psanchez@unicauca.edu.co', 'password123', 1, 5, 2);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (24, 'COO001', 'María Elena Vargas', 'mvargas@unicauca.edu.co', 'password123', 1, 3, 3);
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) VALUES (25, 'SEC001', 'Carlos Eduardo Torres', 'ctorres@unicauca.edu.co', 'password123', 1, 4, 4);

-- Solo insertar cursos adicionales (el curso 1 ya existe)
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon) VALUES (1, 1, 1, 'A', 30, 'A-101');
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon) VALUES (2, 2, 2, 'B', 25, 'A-102');
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon) VALUES (3, 3, 3, 'C', 20, 'A-103');

-- Solo insertar estados adicionales
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (1, 'Publicado', '2025-07-01 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (2, 'Preinscripcion', '2025-07-02 08:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (3, 'Inscripcion', '2025-07-03 08:00:00', 3);
-- Datos de prueba para inscripciones en cursos de verano
-- NOTA: Con herencia JOINED, primero se inserta en Solicitudes (tabla padre), luego en la tabla hija
-- Inscripciones de cursos de verano (extendiendo de Solicitudes)
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (100, 'Inscripcion - Matematicas Basicas - Ana', '2025-07-15 10:30:00', false, 1, 1);
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (100, 'Ana Gonzalez', 'Primera_Vez', 'Inscripcion en curso de verano');

INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (101, 'Inscripcion - Programacion I - Carlos', '2025-07-16 14:20:00', false, 2, 2);
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (101, 'Carlos Lopez', 'Primera_Vez', 'Inscripcion en curso de verano');

INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (102, 'Inscripcion - Matematicas Basicas - Maria', '2025-07-17 09:15:00', false, 3, 1);
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (102, 'Maria Rodriguez', 'Primera_Vez', 'Inscripcion validada');

INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (103, 'Inscripcion - Bases de Datos - Pedro', '2025-07-18 16:45:00', false, 4, 3);
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (103, 'Pedro Martinez', 'Primera_Vez', 'Inscripcion en curso de verano');

-- ==========================================
-- DATOS PARA SOLICITUDES DE REINGRESO (9 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (1, 'Solicitud de Reingreso - Juan Perez', '2025-07-20 09:00:00', false, 1, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (2, 'Solicitud de Reingreso - Maria Garcia', '2025-07-21 10:30:00', false, 2, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (3, 'Solicitud de Reingreso - Ana Lopez', '2025-07-22 14:15:00', false, 3, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (4, 'Solicitud de Reingreso - Carlos Ruiz', '2025-07-23 11:45:00', false, 4, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (5, 'Solicitud de Reingreso - Laura Torres', '2025-07-24 16:20:00', false, 5, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (6, 'Solicitud de Reingreso - Diego Moreno', '2025-07-25 08:30:00', false, 6, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (7, 'Solicitud de Reingreso - Sofia Jimenez', '2025-07-26 13:10:00', false, 7, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (8, 'Solicitud de Reingreso - Andres Vega', '2025-07-27 15:40:00', false, 8, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (9, 'Solicitud de Reingreso - Camila Herrera', '2025-07-28 12:00:00', false, 9, 3);

-- Tabla específica de reingreso
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (1);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (2);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (3);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (4);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (5);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (6);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (7);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (8);
INSERT INTO Solicitudes_Reingreso(idSolicitud) VALUES (9);

-- ==========================================
-- DATOS PARA SOLICITUDES DE HOMOLOGACIÓN (8 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (10, 'Solicitud de Homologacion - Roberto Silva', '2025-07-29 09:15:00', false, 10, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (11, 'Solicitud de Homologacion - Patricia Ramirez', '2025-07-30 10:45:00', false, 11, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (12, 'Solicitud de Homologacion - Miguel Castro', '2025-08-01 14:30:00', false, 12, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (13, 'Solicitud de Homologacion - Isabel Mendoza', '2025-08-02 11:20:00', false, 13, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (14, 'Solicitud de Homologacion - Fernando Diaz', '2025-08-03 16:10:00', false, 14, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (15, 'Solicitud de Homologacion - Valeria Rojas', '2025-08-04 08:50:00', false, 15, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (16, 'Solicitud de Homologacion - Gabriel Morales', '2025-08-05 13:25:00', false, 16, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (17, 'Solicitud de Homologacion - Adriana Vega', '2025-08-06 15:35:00', false, 17, 2);

-- Tabla específica de homologación
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (10, '/docs/homologacion/roberto_silva_pm_fo4.pdf', '/docs/homologacion/roberto_silva_contenido.pdf');
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (11, '/docs/homologacion/patricia_ramirez_pm_fo4.pdf', '/docs/homologacion/patricia_ramirez_contenido.pdf');
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (12, '/docs/homologacion/miguel_castro_pm_fo4.pdf', '/docs/homologacion/miguel_castro_contenido.pdf');
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (13, '/docs/homologacion/isabel_mendoza_pm_fo4.pdf', '/docs/homologacion/isabel_mendoza_contenido.pdf');
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (14, '/docs/homologacion/fernando_diaz_pm_fo4.pdf', '/docs/homologacion/fernando_diaz_contenido.pdf');
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (15, '/docs/homologacion/valeria_rojas_pm_fo4.pdf', '/docs/homologacion/valeria_rojas_contenido.pdf');
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (16, '/docs/homologacion/gabriel_morales_pm_fo4.pdf', '/docs/homologacion/gabriel_morales_contenido.pdf');
INSERT INTO Solicitudes_Homogolacion(idSolicitud, ruta_PM_FO_4_FOR_27, ruta_contenido_programatico) VALUES (17, '/docs/homologacion/adriana_vega_pm_fo4.pdf', '/docs/homologacion/adriana_vega_contenido.pdf');

-- ==========================================
-- DATOS PARA SOLICITUDES DE CURSOS DE VERANO (9 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (18, 'Solicitud Curso Verano - Nicolas Perez', '2025-08-07 09:30:00', false, 18, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (19, 'Solicitud Curso Verano - Daniela Sanchez', '2025-08-08 11:15:00', false, 19, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (20, 'Solicitud Curso Verano - Sebastian Lopez', '2025-08-09 14:45:00', false, 20, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (21, 'Solicitud Curso Verano - Andrea Ramirez', '2025-08-10 10:20:00', false, 21, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (22, 'Solicitud Curso Verano - Felipe Castro', '2025-08-11 15:30:00', false, 22, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (23, 'Solicitud Curso Verano - Natalia Herrera', '2025-08-12 08:45:00', false, 23, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (24, 'Solicitud Curso Verano - Alejandro Vargas', '2025-08-13 13:20:00', false, 24, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (25, 'Solicitud Curso Verano - Juliana Torres', '2025-08-14 16:10:00', false, 25, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (26, 'Solicitud Curso Verano - Mauricio Gomez', '2025-08-15 12:00:00', false, 1, 3);

-- Tabla específica de cursos de verano (usando la tabla existente)
-- NOTA: Estas líneas están comentadas porque faltan campos obligatorios (nombre_estudiante, etc.)
-- Los registros de las líneas 115-118 ya contienen datos completos de inscripciones
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (18);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (19);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (20);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (21);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (22);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (23);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (24);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (25);
-- INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud) VALUES (26);

-- ==========================================
-- DATOS PARA SOLICITUDES ECAES (10 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (27, 'Solicitud ECAES - Ricardo Morales', '2025-08-16 09:45:00', false, 2, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (28, 'Solicitud ECAES - Carmen Ruiz', '2025-08-17 11:30:00', false, 3, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (29, 'Solicitud ECAES - Oscar Jimenez', '2025-08-18 14:15:00', false, 4, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (30, 'Solicitud ECAES - Lucia Moreno', '2025-08-19 10:40:00', false, 5, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (31, 'Solicitud ECAES - Hector Diaz', '2025-08-20 15:25:00', false, 6, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (32, 'Solicitud ECAES - Rosa Vega', '2025-08-21 08:55:00', false, 7, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (33, 'Solicitud ECAES - Victor Sanchez', '2025-08-22 13:35:00', false, 8, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (34, 'Solicitud ECAES - Elena Castro', '2025-08-23 16:20:00', false, 9, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (35, 'Solicitud ECAES - Armando Torres', '2025-08-24 12:15:00', false, 10, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (36, 'Solicitud ECAES - Gloria Herrera', '2025-08-25 14:50:00', false, 11, 1);

-- Tabla específica de ECAES
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (27, 'CC', '12345678', '2010-05-15', '1995-03-20');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (28, 'CC', '87654321', '2011-08-22', '1996-07-10');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (29, 'CE', 'CE123456', '2012-12-10', '1997-01-15');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (30, 'CC', '11223344', '2013-04-18', '1998-09-05');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (31, 'CC', '55667788', '2014-11-30', '1995-12-12');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (32, 'CC', '99887766', '2015-06-25', '1996-04-28');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (33, 'CC', '44332211', '2016-09-14', '1997-08-03');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (34, 'CE', 'CE789012', '2017-02-08', '1998-11-18');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (35, 'CC', '77889900', '2018-07-12', '1995-06-25');
INSERT INTO Solicitudes_Ecaes(idSolicitud, tipoDocumento, numero_documento, fecha_expedicion, fecha_nacimiento) VALUES (36, 'CC', '00998877', '2019-10-05', '1996-02-14');

-- ==========================================
-- DATOS PARA SOLICITUDES DE PAZ Y SALVO (10 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (37, 'Solicitud Paz y Salvo - Roberto Silva', '2025-08-26 09:20:00', false, 12, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (38, 'Solicitud Paz y Salvo - Patricia Ramirez', '2025-08-27 11:10:00', false, 13, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (39, 'Solicitud Paz y Salvo - Miguel Castro', '2025-08-28 14:40:00', false, 14, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (40, 'Solicitud Paz y Salvo - Isabel Mendoza', '2025-08-29 10:25:00', false, 15, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (41, 'Solicitud Paz y Salvo - Fernando Diaz', '2025-08-30 15:15:00', false, 16, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (42, 'Solicitud Paz y Salvo - Valeria Rojas', '2025-09-01 08:35:00', false, 17, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (43, 'Solicitud Paz y Salvo - Gabriel Morales', '2025-09-02 13:45:00', false, 18, 1);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (44, 'Solicitud Paz y Salvo - Adriana Vega', '2025-09-03 16:30:00', false, 19, 2);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (45, 'Solicitud Paz y Salvo - Nicolas Perez', '2025-09-04 12:05:00', false, 20, 3);
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, idUsuario, idCurso) VALUES (46, 'Solicitud Paz y Salvo - Daniela Sanchez', '2025-09-05 14:20:00', false, 21, 1);

-- Tabla específica de paz y salvo
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (37);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (38);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (39);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (40);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (41);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (42);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (43);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (44);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (45);
INSERT INTO Solicitudes_PazYSalvo(idSolicitud) VALUES (46);

-- ==========================================
-- ESTADOS DE SOLICITUDES CON DISTRIBUCIÓN CORRECTA
-- Total: 46 solicitudes
-- 45% Aprobadas = 21 solicitudes
-- 25% En Proceso = 11 solicitudes  
-- 20% Enviadas = 9 solicitudes
-- 10% Rechazadas = 5 solicitudes
-- ==========================================

-- Estados APROBADAS (21 solicitudes - 45%)
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (1, 'Aprobado', '2025-07-25 10:00:00', 1);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (2, 'Aprobado', '2025-07-26 11:30:00', 2);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (3, 'Aprobado', '2025-07-27 14:15:00', 3);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (4, 'Aprobado', '2025-07-28 09:45:00', 4);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (5, 'Aprobado', '2025-07-29 16:20:00', 5);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (6, 'Aprobado', '2025-08-03 08:30:00', 10);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (7, 'Aprobado', '2025-08-04 10:45:00', 11);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (8, 'Aprobado', '2025-08-05 14:30:00', 12);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (9, 'Aprobado', '2025-08-06 11:20:00', 13);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (10, 'Aprobado', '2025-08-11 09:30:00', 18);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (11, 'Aprobado', '2025-08-12 11:15:00', 19);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (12, 'Aprobado', '2025-08-13 14:45:00', 20);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (13, 'Aprobado', '2025-08-14 10:20:00', 21);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (14, 'Aprobado', '2025-08-20 09:45:00', 27);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (15, 'Aprobado', '2025-08-21 11:30:00', 28);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (16, 'Aprobado', '2025-08-22 14:15:00', 29);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (17, 'Aprobado', '2025-08-23 10:40:00', 30);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (18, 'Aprobado', '2025-09-06 09:20:00', 37);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (19, 'Aprobado', '2025-09-07 11:10:00', 38);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (20, 'Aprobado', '2025-09-08 14:40:00', 39);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (21, 'Aprobado', '2025-09-09 10:25:00', 40);

-- Estados EN PROCESO (11 solicitudes - 25%)
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (22, 'En Proceso', '2025-07-30 08:30:00', 6);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (23, 'En Proceso', '2025-07-31 13:10:00', 7);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (24, 'En Proceso', '2025-08-07 15:40:00', 14);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (25, 'En Proceso', '2025-08-08 12:00:00', 15);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (26, 'En Proceso', '2025-08-15 15:30:00', 22);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (27, 'En Proceso', '2025-08-16 08:45:00', 23);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (28, 'En Proceso', '2025-08-24 15:25:00', 31);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (29, 'En Proceso', '2025-08-25 08:55:00', 32);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (30, 'En Proceso', '2025-09-10 15:15:00', 41);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (31, 'En Proceso', '2025-09-11 08:35:00', 42);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (32, 'En Proceso', '2025-09-12 13:45:00', 43);

-- Estados EnviadaS (9 solicitudes - 20%)
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (33, 'Enviada', '2025-08-01 12:00:00', 8);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (34, 'Enviada', '2025-08-02 12:00:00', 9);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (35, 'Enviada', '2025-08-09 16:10:00', 16);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (36, 'Enviada', '2025-08-10 12:00:00', 17);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (37, 'Enviada', '2025-08-17 13:20:00', 24);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (38, 'Enviada', '2025-08-18 16:10:00', 25);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (39, 'Enviada', '2025-08-26 12:15:00', 33);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (40, 'Enviada', '2025-08-27 14:50:00', 34);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (41, 'Enviada', '2025-09-13 16:30:00', 44);

-- Estados RechazadoS (5 solicitudes - 10%)
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (42, 'Rechazado', '2025-08-19 10:40:00', 26);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (43, 'Rechazado', '2025-08-28 15:25:00', 35);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (44, 'Rechazado', '2025-08-29 08:55:00', 36);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (45, 'Rechazado', '2025-09-14 12:05:00', 45);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (46, 'Rechazado', '2025-09-15 14:20:00', 46);

-- ==========================================
-- ESTADOS CORRECTOS PARA PAZ Y SALVO (siguiendo el flujo)
-- IDs 37-40: Estado 'Aprobado' (4 solicitudes aprobadas por coordinador para secretaria)
-- ==========================================
-- Cambiar los estados 'Aprobado' de paz y salvo a 'APROBADA_COORDINADOR' para que la secretaria las vea
UPDATE EstadosSolicitudes SET estado_actual = 'APROBADA_COORDINADOR' WHERE idEstado IN (18, 19, 20, 21);

-- ==========================================
-- VALIDACIÓN DE DATOS CARGADOS
-- ==========================================
-- SELECT COUNT(*) FROM Solicitudes;
-- SELECT COUNT(*) FROM EstadosSolicitudes;
-- SELECT COUNT(*) FROM Solicitudes_Reingreso;
-- SELECT COUNT(*) FROM Solicitudes_Homogolacion;
-- SELECT COUNT(*) FROM Solicitudes_CursoVeranoInscripcion;
-- SELECT COUNT(*) FROM Solicitudes_Ecaes;
-- SELECT COUNT(*) FROM Solicitudes_PazYSalvo;
