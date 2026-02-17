-- =========================================
-- DATOS INICIALES
-- =========================================
-- Nota: Las columnas cedula, periodo_academico y fecha_ceremonia se crean
-- automáticamente por Hibernate cuando ddl-auto=create, ya que están
-- definidas en las entidades JPA (UsuarioEntity y SolicitudEntity)

INSERT INTO Roles(idRol, nombre) VALUES (1,'Administrador');
INSERT INTO Roles(idRol, nombre) VALUES (2,'Estudiante');
INSERT INTO Roles(idRol, nombre) VALUES (3,'Coordinador');
INSERT INTO Roles(idRol, nombre) VALUES (4,'Secretario');
INSERT INTO Roles(idRol, nombre) VALUES (5,'Funcionario');
INSERT INTO Roles(idRol, nombre) VALUES (6,'Docente');
INSERT INTO Roles(idRol, nombre) VALUES (7,'Decano');
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

INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (1, 'EST001', 'Juan Pérez', 'juan.perez@unicauca.edu.co', 'password123', 1, 2, 1, '1234567890');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (2, 'EST002', 'María García', 'maria.garcia@unicauca.edu.co', 'password123', 1, 2, 2, '2345678901');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (100, 'EST003', 'Ana Sofía Rodríguez', 'ana.rodriguez@unicauca.edu.co', 'password123', 1, 2, 3, '3456789012');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (101, 'EST004', 'Carlos Eduardo Martínez', 'carlos.martinez@unicauca.edu.co', 'password123', 1, 2, 4, '4567890123');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (102, 'EST005', 'Laura Valentina López', 'laura.lopez@unicauca.edu.co', 'password123', 1, 2, 1, '5678901234');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (103, 'EST006', 'Diego Alejandro Herrera', 'diego.herrera@unicauca.edu.co', 'password123', 1, 2, 2, '6789012345');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (104, 'EST007', 'Valentina Morales', 'valentina.morales@unicauca.edu.co', 'password123', 1, 2, 3, '7890123456');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (3, 'DOC001', 'Carlos López', 'carlos.lopez@unicauca.edu.co', 'password123', 1, 6, 1, '8901234567');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (4, '1047', 'Carlos Alberto Ardila Albarracin', 'cardila@unicauca.edu.co', 'password123', 1, 6, 2, '9012345678');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (5, '1048', 'Carlos Alberto Cobos Lozada', 'ccobos@unicauca.edu.co', 'password123', 1, 6, 3, '0123456789');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (6, '1049', 'Carolina Gonzalez Serrano', 'cgonzals@unicauca.edu.co', 'password123', 1, 6, 4, '1111111111');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (7, '1050', 'Cesar Alberto Collazos Ordonez', 'ccollazo@unicauca.edu.co', 'password123', 1, 6, 1, '2222222222');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (8, '1051', 'Ember Ubeimar Martinez Flor', 'eumartinez@unicauca.edu.co', 'password123', 1, 6, 2, '3333333333');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (9, '1052', 'Erwin Meza Vega', 'emezav@unicauca.edu.co', 'password123', 1, 6, 3, '4444444444');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (10, '1053', 'Francisco Jose Pino Correa', 'fjpino@unicauca.edu.co', 'password123', 1, 6, 4, '5555555555');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (11, '1054', 'Jorge Jair Moreno Chaustre', 'jjmoreno@unicauca.edu.co', 'password123', 1, 6, 1, '6666666666');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (12, '1055', 'Julio Ariel Hurtado Alegria', 'ahurtado@unicauca.edu.co', 'password123', 1, 6, 2, '7777777777');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (13, '1056', 'Luz Marina Sierra Martinez', 'lsierra@unicauca.edu.co', 'password123', 1, 6, 3, '8888888888');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (14, '1057', 'Martha Eliana Mendoza Becerra', 'mmendoza@unicauca.edu.co', 'password123', 1, 6, 4, '9999999999');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (15, '1058', 'Miguel Angel Nino Zambrano', 'manzamb@unicauca.edu.co', 'password123', 1, 6, 1, '1010101010');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (16, '1059', 'Nestor Milciades Diaz Marino', 'nediaz@unicauca.edu.co', 'password123', 1, 6, 2, '1212121212');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (17, '1060', 'Pablo Augusto Mage Imbachi', 'pmage@unicauca.edu.co', 'password123', 1, 6, 3, '1313131313');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (18, '1061', 'Roberto Carlos Naranjo Cuervo', 'rnaranjo@unicauca.edu.co', 'password123', 1, 6, 4, '1414141414');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (19, '1062', 'Sandra Milena Roa Martinez', 'smroa@unicauca.edu.co', 'password123', 1, 6, 1, '1515151515');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (20, '1063', 'Siler Amador Donado', 'samador@unicauca.edu.co', 'password123', 1, 6, 2, '1616161616');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (21, '1064', 'Wilson Libardo Pantoja Yepez', 'wpantoja@unicauca.edu.co', 'password123', 1, 6, 3, '1717171717');

-- Usuarios de prueba para roles de funcionarios
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (22, 'FUN001', 'Ana María Rodríguez', 'arodriguez@unicauca.edu.co', 'password123', 1, 5, 1, '1818181818');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (23, 'FUN002', 'Pedro Sánchez López', 'psanchez@unicauca.edu.co', 'password123', 1, 5, 2, '1919191919');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (24, 'COO001', 'María Elena Vargas', 'mvargas@unicauca.edu.co', 'password123', 1, 3, 3, '2020202020');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (25, 'SEC001', 'Carlos Eduardo Torres', 'ctorres@unicauca.edu.co', 'password123', 1, 3, 3, '2121212121');

-- Usuario Administrador por defecto (para desarrollo y pruebas)
-- Correo: admin@unicauca.edu.co
-- Password: password123 (hash BCrypt)
-- IMPORTANTE: Cambiar la contraseña en producción
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (26, 'ADMIN001', 'Administrador del Sistema', 'admin@unicauca.edu.co', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', 1, 1, 1, '0000000000');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (27, '1046190112', 'Daniel Cisneros', 'dcisneros@unicauca.edu.co', '$2a$12$kPCRQb320eqFwn08XBFDkuJYICOG7H5ou08fmHQ3.zaKZjAtGyI9G', 1, 2, 3, '100791411');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (28, '55555555', 'Juan Pablo', 'juanp@unicauca.edu.co', '$2a$12$hnaM9zNGpcYBN6FEWuECjO7Eh.rJ9dXPInXbt8sz9dedJp8zxniYi', 1, 3, 3, '55555555');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (29, '10293838', 'josefina rodriguez', 'josefina@unicauca.edu.co', '$2a$12$zHDPzonzaTYw/SDvqZaReehyTo9ri51vg5N1C9DaYHIdeZi9PVQUG', 1, 3, 3, '10293838');
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma, cedula) VALUES (30, '99009876', 'Maria flor', 'maria@unicauca.edu.co', '$2a$12$IterW17WKe75BkVsMSU9SuFb.DcAB6diCk8Ef8fxwzQ7nvdLJyBr.', 1, 4, 3, '0000000000');


-- Solo insertar cursos adicionales (el curso 1 ya existe)
-- Agregar periodo_academico a los cursos
-- Salones asignados del edificio FIET
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon, periodo_academico) VALUES (1, 1, 1, 'A', 30, '221', '2025-2');
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon, periodo_academico) VALUES (2, 2, 2, 'B', 25, '222', '2025-2');
INSERT INTO Cursos_ofertados(idCurso, idfkMateria, idfkDocente, grupo, cupo_estimado, salon, periodo_academico) VALUES (3, 3, 3, 'C', 20, '224', '2025-2');

-- Estados de cursos de verano (actualizados según el backend)
-- Estados válidos: Borrador, Abierto, Publicado, Preinscripcion, Inscripcion, Cerrado
-- NOTA: La columna fecha_fin se crea automáticamente en desarrollo por Hibernate (ddl-auto=create-drop)
-- En producción, ejecutar el script MIGRACION_FECHA_FIN.sql manualmente ANTES de iniciar la app

-- Curso 1: Bases de Datos - Estado: Preinscripcion (para que aparezca en preinscripciones)
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (1, 'Borrador', '2025-06-25 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (2, 'Abierto', '2025-06-28 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (3, 'Publicado', '2025-07-01 08:00:00', 1);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (4, 'Preinscripcion', '2025-07-02 08:00:00', 1);

-- Curso 2: Calidad de Software - Estado: Preinscripcion (para que aparezca en preinscripciones)
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (5, 'Borrador', '2025-06-26 08:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (6, 'Abierto', '2025-06-29 08:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (7, 'Publicado', '2025-07-01 10:00:00', 2);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (8, 'Preinscripcion', '2025-07-02 10:00:00', 2);

-- Curso 3: Metodología de la Investigación - Estado: Inscripcion (para que aparezca en inscripciones)
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (9, 'Borrador', '2025-06-27 08:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (10, 'Abierto', '2025-06-30 08:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (11, 'Publicado', '2025-07-01 12:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (12, 'Preinscripcion', '2025-07-02 12:00:00', 3);
INSERT INTO EstadosCursos(idEstado, estado_actual, fecha_registro_estado, idfkCurso) VALUES (13, 'Inscripcion', '2025-07-03 08:00:00', 3);
-- Datos de prueba para inscripciones en cursos de verano
-- NOTA: Con herencia JOINED, primero se inserta en Solicitudes (tabla padre), luego en la tabla hija
-- Inscripciones de cursos de verano (extendiendo de Solicitudes)
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (100, 'Curso de Verano - Inscripcion - Matematicas Basicas - Ana', '2025-07-15 10:30:00', 1, 1, '2025-2');
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (100, 'Ana Gonzalez', 'Primera_Vez', 'Inscripcion en curso de verano');

INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (101, 'Curso de Verano - Inscripcion - Programacion I - Carlos', '2025-07-16 14:20:00', 2, 2, '2025-2');
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (101, 'Carlos Lopez', 'Primera_Vez', 'Inscripcion en curso de verano');

INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (102, 'Curso de Verano - Inscripcion - Matematicas Basicas - Maria', '2025-07-17 09:15:00', 3, 1, '2025-2');
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (102, 'Maria Rodriguez', 'Primera_Vez', 'Inscripcion validada');

INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (103, 'Curso de Verano - Inscripcion - Bases de Datos - Pedro', '2025-07-18 16:45:00', 4, 3, '2025-2');
INSERT INTO Solicitudes_Cursos_Verano_Inscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (103, 'Pedro Martinez', 'Primera_Vez', 'Inscripcion en curso de verano');

-- ==========================================
-- DATOS PARA SOLICITUDES DE REINGRESO (9 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (1, 'Solicitud de Reingreso - Juan Perez', '2025-07-20 09:00:00', 1, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (2, 'Solicitud de Reingreso - Maria Garcia', '2025-07-21 10:30:00', 2, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (3, 'Solicitud de Reingreso - Ana Lopez', '2025-07-22 14:15:00', 3, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (4, 'Solicitud de Reingreso - Carlos Ruiz', '2025-07-23 11:45:00', 4, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (5, 'Solicitud de Reingreso - Laura Torres', '2025-07-24 16:20:00', 5, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (6, 'Solicitud de Reingreso - Diego Moreno', '2025-07-25 08:30:00', 6, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (7, 'Solicitud de Reingreso - Sofia Jimenez', '2025-07-26 13:10:00', 7, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (8, 'Solicitud de Reingreso - Andres Vega', '2025-07-27 15:40:00', 8, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (9, 'Solicitud de Reingreso - Camila Herrera', '2025-07-28 12:00:00', 9, 3, '2025-2');

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
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (10, 'Solicitud de Homologacion - Roberto Silva', '2025-07-29 09:15:00', 10, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (11, 'Solicitud de Homologacion - Patricia Ramirez', '2025-07-30 10:45:00', 11, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (12, 'Solicitud de Homologacion - Miguel Castro', '2025-08-01 14:30:00', 12, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (13, 'Solicitud de Homologacion - Isabel Mendoza', '2025-08-02 11:20:00', 13, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (14, 'Solicitud de Homologacion - Fernando Diaz', '2025-08-03 16:10:00', 14, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (15, 'Solicitud de Homologacion - Valeria Rojas', '2025-08-04 08:50:00', 15, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (16, 'Solicitud de Homologacion - Gabriel Morales', '2025-08-05 13:25:00', 16, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (17, 'Solicitud de Homologacion - Adriana Vega', '2025-08-06 15:35:00', 17, 2, '2025-2');

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
-- DATOS PARA SOLICITUDES DE CURSOS DE VERANO - PREINSCRIPCIONES (9 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (18, 'Solicitud Curso Verano - Nicolas Perez', '2025-08-07 09:30:00', 18, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (19, 'Solicitud Curso Verano - Daniela Sanchez', '2025-08-08 11:15:00', 19, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (20, 'Solicitud Curso Verano - Sebastian Lopez', '2025-08-09 14:45:00', 20, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (21, 'Solicitud Curso Verano - Andrea Ramirez', '2025-08-10 10:20:00', 21, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (22, 'Solicitud Curso Verano - Felipe Castro', '2025-08-11 15:30:00', 22, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (23, 'Solicitud Curso Verano - Natalia Herrera', '2025-08-12 08:45:00', 23, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (24, 'Solicitud Curso Verano - Alejandro Vargas', '2025-08-13 13:20:00', 24, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (25, 'Solicitud Curso Verano - Juliana Torres', '2025-08-14 16:10:00', 25, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (26, 'Solicitud Curso Verano - Mauricio Gomez', '2025-08-15 12:00:00', 1, 3, '2025-2');

-- Tabla específica de preinscripciones de cursos de verano
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (18, 'Nicolas Perez', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (19, 'Daniela Sanchez', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (20, 'Sebastian Lopez', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (21, 'Andrea Ramirez', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (22, 'Felipe Castro', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (23, 'Natalia Herrera', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (24, 'Alejandro Vargas', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (25, 'Juliana Torres', 'Primera_Vez', 'Preinscripcion en curso de verano');
INSERT INTO Solicitudes_Cursos_Verano_Preinscripcion(idSolicitud, nombre_estudiante, codicion_solicitud, observacion) VALUES (26, 'Mauricio Gomez', 'Primera_Vez', 'Preinscripcion en curso de verano');

-- ==========================================
-- DATOS PARA SOLICITUDES ECAES (10 solicitudes)
-- ==========================================
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (27, 'Solicitud ECAES - Ricardo Morales', '2025-08-16 09:45:00', 2, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (28, 'Solicitud ECAES - Carmen Ruiz', '2025-08-17 11:30:00', 3, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (29, 'Solicitud ECAES - Oscar Jimenez', '2025-08-18 14:15:00', 4, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (30, 'Solicitud ECAES - Lucia Moreno', '2025-08-19 10:40:00', 5, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (31, 'Solicitud ECAES - Hector Diaz', '2025-08-20 15:25:00', 6, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (32, 'Solicitud ECAES - Rosa Vega', '2025-08-21 08:55:00', 7, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (33, 'Solicitud ECAES - Victor Sanchez', '2025-08-22 13:35:00', 8, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (34, 'Solicitud ECAES - Elena Castro', '2025-08-23 16:20:00', 9, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (35, 'Solicitud ECAES - Armando Torres', '2025-08-24 12:15:00', 10, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (36, 'Solicitud ECAES - Gloria Herrera', '2025-08-25 14:50:00', 11, 1, '2025-2');

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
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (37, 'Solicitud Paz y Salvo - Roberto Silva', '2025-08-26 09:20:00', 12, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (38, 'Solicitud Paz y Salvo - Patricia Ramirez', '2025-08-27 11:10:00', 13, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (39, 'Solicitud Paz y Salvo - Miguel Castro', '2025-08-28 14:40:00', 14, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (40, 'Solicitud Paz y Salvo - Isabel Mendoza', '2025-08-29 10:25:00', 15, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (41, 'Solicitud Paz y Salvo - Fernando Diaz', '2025-08-30 15:15:00', 16, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (42, 'Solicitud Paz y Salvo - Valeria Rojas', '2025-09-01 08:35:00', 17, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (43, 'Solicitud Paz y Salvo - Gabriel Morales', '2025-09-02 13:45:00', 18, 1, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (44, 'Solicitud Paz y Salvo - Adriana Vega', '2025-09-03 16:30:00', 19, 2, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (45, 'Solicitud Paz y Salvo - Nicolas Perez', '2025-09-04 12:05:00', 20, 3, '2025-2');
INSERT INTO Solicitudes(idSolicitud, nombre_solicitud, fecha_registro_solicitud, idUsuario, idCurso, periodo_academico) VALUES (46, 'Solicitud Paz y Salvo - Daniela Sanchez', '2025-09-05 14:20:00', 21, 1, '2025-2');

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
-- ESTADOS DE SOLICITUDES CON DISTRIBUCIÓN EQUILIBRADA POR PROCESO
-- Total: 50 solicitudes
-- 45% Aprobadas = 21 solicitudes
-- 25% En Proceso (APROBADA_FUNCIONARIO + APROBADA_COORDINADOR) = 15 solicitudes  
-- 20% Enviadas = 9 solicitudes
-- 10% Rechazadas = 5 solicitudes
-- ==========================================

-- ========== REINGRESO (IDs 1-9) - 9 solicitudes ==========
-- 4 Aprobadas, 2 En Proceso, 2 Enviadas, 1 Rechazada
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (1, 'APROBADA', '2025-07-25 10:00:00', 1);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (2, 'APROBADA', '2025-07-26 11:30:00', 2);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (3, 'APROBADA', '2025-07-27 14:15:00', 3);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (4, 'APROBADA', '2025-07-28 09:45:00', 4);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (5, 'RECHAZADA', '2025-07-29 16:20:00', 5);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (6, 'APROBADA_FUNCIONARIO', '2025-07-30 08:30:00', 6);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (7, 'APROBADA_FUNCIONARIO', '2025-07-31 13:10:00', 7);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (8, 'ENVIADA', '2025-08-01 12:00:00', 8);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (9, 'ENVIADA', '2025-08-02 12:00:00', 9);

-- ========== HOMOLOGACIÓN (IDs 10-17) - 8 solicitudes ==========
-- 3 Aprobadas, 2 En Proceso, 2 Enviadas, 1 Rechazada
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (10, 'APROBADA', '2025-08-03 08:30:00', 10);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (11, 'APROBADA', '2025-08-04 10:45:00', 11);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (12, 'APROBADA', '2025-08-05 14:30:00', 12);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (13, 'RECHAZADA', '2025-08-06 11:20:00', 13);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (14, 'APROBADA_FUNCIONARIO', '2025-08-07 15:40:00', 14);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (15, 'APROBADA_FUNCIONARIO', '2025-08-08 12:00:00', 15);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (16, 'ENVIADA', '2025-08-09 16:10:00', 16);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (17, 'ENVIADA', '2025-08-10 12:00:00', 17);

-- ========== CURSOS DE VERANO - PREINSCRIPCIONES (IDs 18-26) - 9 solicitudes ==========
-- 4 Aprobadas, 2 En Proceso, 2 Enviadas, 1 Rechazada
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (18, 'APROBADA', '2025-08-11 09:30:00', 18);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (19, 'APROBADA', '2025-08-12 11:15:00', 19);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (20, 'APROBADA', '2025-08-13 14:45:00', 20);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (21, 'APROBADA', '2025-08-14 10:20:00', 21);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (22, 'APROBADA_FUNCIONARIO', '2025-08-15 15:30:00', 22);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (23, 'APROBADA_FUNCIONARIO', '2025-08-16 08:45:00', 23);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (24, 'ENVIADA', '2025-08-17 13:20:00', 24);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (25, 'ENVIADA', '2025-08-18 16:10:00', 25);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (26, 'RECHAZADA', '2025-08-19 10:40:00', 26);

-- ========== ECAES (IDs 27-36) - 10 solicitudes ==========
-- 3 Aprobadas, 4 En Proceso, 2 Enviadas, 1 Rechazada
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (27, 'APROBADA', '2025-08-20 09:45:00', 27);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (28, 'APROBADA', '2025-08-21 11:30:00', 28);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (29, 'APROBADA', '2025-08-22 14:15:00', 29);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (30, 'RECHAZADA', '2025-08-23 10:40:00', 30);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (31, 'APROBADA_COORDINADOR', '2025-08-24 15:25:00', 31);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (32, 'APROBADA_COORDINADOR', '2025-08-25 08:55:00', 32);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (33, 'APROBADA_COORDINADOR', '2025-08-26 12:15:00', 33);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (34, 'APROBADA_COORDINADOR', '2025-08-27 14:50:00', 34);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (35, 'ENVIADA', '2025-08-28 15:25:00', 35);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (36, 'ENVIADA', '2025-08-29 08:55:00', 36);

-- ========== PAZ Y SALVO (IDs 37-46) - 10 solicitudes ==========
-- 3 Aprobadas, 5 En Proceso, 1 Enviada, 1 Rechazada
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (37, 'APROBADA', '2025-09-06 09:20:00', 37);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (38, 'APROBADA', '2025-09-07 11:10:00', 38);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (39, 'APROBADA', '2025-09-08 14:40:00', 39);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (40, 'APROBADA_COORDINADOR', '2025-09-09 10:25:00', 40);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (41, 'APROBADA_FUNCIONARIO', '2025-09-10 15:15:00', 41);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (42, 'APROBADA_FUNCIONARIO', '2025-09-11 08:35:00', 42);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (43, 'APROBADA_FUNCIONARIO', '2025-09-12 13:45:00', 43);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (44, 'APROBADA_FUNCIONARIO', '2025-09-13 16:30:00', 44);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (45, 'ENVIADA', '2025-09-14 12:05:00', 45);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (46, 'RECHAZADA', '2025-09-15 14:20:00', 46);

-- ==========================================
-- ESTADOS PARA INSCRIPCIONES DE CURSOS DE VERANO (IDs 100-103)
-- Agregando los 4 estados faltantes para completar las 50 solicitudes
-- ==========================================
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (47, 'APROBADA', '2025-07-15 11:00:00', 100);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (48, 'APROBADA', '2025-07-16 15:00:00', 101);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (49, 'APROBADA', '2025-07-17 10:00:00', 102);
INSERT INTO EstadosSolicitudes(idEstado, estado_actual, fecha_registro_estado, idfkSolicitud) VALUES (50, 'APROBADA', '2025-07-18 17:00:00', 103);

-- ==========================================
-- TABLA DE NOTIFICACIONES
-- ==========================================
-- La tabla Notificaciones se crea automáticamente mediante JPA/Hibernate
-- Las notificaciones se generan automáticamente cuando:
-- - Se crea una nueva solicitud
-- - Se cambia el estado de una solicitud
-- No se requieren datos iniciales para esta tabla

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
-- SELECT COUNT(*) FROM Notificaciones;

-- =========================================
-- SALONES DEL EDIFICIO FIET
-- =========================================
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (1, '221', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (2, '222', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (3, '224', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (4, '225', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (5, '226', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (6, '227', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (7, '228', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (8, '229', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (9, '230', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (10, '231', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (11, '236', 'FIET', true);
INSERT INTO Salones(idSalon, numero_salon, edificio, activo) VALUES (12, '234', 'FIET', true);

-- =========================================
-- NOTA IMPORTANTE SOBRE MIGRACIONES
-- =========================================
-- Las columnas de las entidades JPA (incluyendo titulo_trabajo_grado y director_trabajo_grado
-- en SolicitudPazYSalvoEntity) se crean AUTOMÁTICAMENTE cuando:
-- - spring.jpa.hibernate.ddl-auto=create (desarrollo)
-- - spring.jpa.hibernate.ddl-auto=update (producción con datos existentes)
--
-- NO es necesario ejecutar scripts ALTER TABLE manualmente.
-- Hibernate crea todas las columnas basándose en las anotaciones @Column de las entidades.
--
-- Si necesitas agregar columnas a una base de datos existente en producción:
-- 1. Cambia temporalmente ddl-auto=update (NO create, para no perder datos)
-- 2. Reinicia la aplicación
-- 3. Hibernate agregará las columnas automáticamente
-- 4. Vuelve a cambiar a ddl-auto=validate para producción
-- =========================================
