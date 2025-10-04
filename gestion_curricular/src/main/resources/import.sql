INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Administrador');
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Estudiante');
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Coordinador');
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Secretario');
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Funcionario');
INSERT INTO `programas`(`idPrograma`, `codigo`, `nombre_programa`) VALUES (NULL,'1046','Ingenieria Sistemas');
INSERT INTO `programas`(`idPrograma`, `codigo`, `nombre_programa`) VALUES (NULL,'1047','Ingenieria Electronica');
INSERT INTO `programas`(`idPrograma`, `codigo`, `nombre_programa`) VALUES (NULL,'1048','Ingenieria de Telecomunicaciones');
INSERT INTO `docentes`(`idDocente`, `codigo_docente`, `nombre_docente`) VALUES (NULL, '1046','Pepe Perez');

-- Datos de prueba para cursos de verano
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'MAT001', 'Matemáticas Básicas', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'PROG001', 'Programación I', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'BD001', 'Bases de Datos', 3);

-- Materias del programa de Ingeniería de Sistemas
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS803', 'Calidad de Software', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS801', 'Metodologia de la Investigacion', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS706', 'Teoria y Dinamica de Sistemas', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS703', 'Ingenieria de Software III', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS702', 'Sistemas Distribuidos', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS603', 'Sistemas Operativos', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS602', 'Ingenieria de Software II', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS601', 'Estructura de Lenguajes', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS504', 'Bases de Datos II', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS503', 'Ingenieria de Software I', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS502', 'Arquitectura Computacional', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS501', 'Teoria de la Computacion', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS402', 'Bases de Datos I', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS401', 'Estructura de Datos II', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS301', 'Estructuras de Datos I', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'SIS201', 'Programacion Orientada a Objetos', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, 'MAT131', 'Estadistica y Probabilidad', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '333-1', 'Inteligencia Artificial', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '328-1', 'Analisis Numerico', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '323-1', 'Vibraciones y Ondas', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '322-1', 'Ecuaciones Diferenciales Ordinarias', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '317-1', 'Electromagnetismo', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '316-1', 'Calculo III', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '312-1', 'Mecanica', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '311-1', 'Calculo II', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '307', 'Algebra Lineal', 4);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '21505', 'Lectura y Escritura', 2);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '11479', 'Introduccion a la Ingenieria de Sistemas', 1);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '11477', 'Introduccion a la Informatica', 3);
INSERT IGNORE INTO `materias`(`idMateria`, `codigo`, `nombre`, `creditos`) VALUES (NULL, '11465', 'Calculo I', 4);

INSERT INTO `usuarios`(`idUsuario`, `codigo_usuario`, `nombre_usuario`, `correo`, `contrasena`, `idfkRol`) VALUES (NULL, 'EST001', 'Juan Pérez', 'juan.perez@unicauca.edu.co', 'password123', 2);
INSERT INTO `usuarios`(`idUsuario`, `codigo_usuario`, `nombre_usuario`, `correo`, `contrasena`, `idfkRol`) VALUES (NULL, 'EST002', 'María García', 'maria.garcia@unicauca.edu.co', 'password123', 2);
INSERT INTO `usuarios`(`idUsuario`, `codigo_usuario`, `nombre_usuario`, `correo`, `contrasena`, `idfkRol`) VALUES (NULL, 'DOC001', 'Carlos López', 'carlos.lopez@unicauca.edu.co', 'password123', 1);

-- Solo insertar cursos adicionales (el curso 1 ya existe)
INSERT IGNORE INTO `cursos_ofertados`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 2, 1, 'B', 25, 'A-102');
INSERT IGNORE INTO `cursos_ofertados`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 3, 1, 'C', 20, 'A-103');

-- Solo insertar estados adicionales
INSERT IGNORE INTO `estadoscursos`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Publicado', NOW(), 1);
INSERT IGNORE INTO `estadoscursos`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Preinscripcion', NOW(), 2);
INSERT IGNORE INTO `estadoscursos`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Inscripcion', NOW(), 3);

-- Datos de prueba para inscripciones en la tabla existente
INSERT IGNORE INTO `solicitudes_cursos_verano_inscripcion`(`idSolicitud`, `nombre_solicitud`, `fecha_creacion`, `idfkUsuario`, `idfkCurso`, `idfkEstado`, `nombre_estudiante`, `codicion_solicitud`, `observacion`) VALUES (1, 'Inscripción - Matemáticas Básicas', '2024-01-15 10:30:00', 1, 1, 1, 'Ana González', 'Primera_Vez', 'Inscripción en curso de verano');
INSERT IGNORE INTO `solicitudes_cursos_verano_inscripcion`(`idSolicitud`, `nombre_solicitud`, `fecha_creacion`, `idfkUsuario`, `idfkCurso`, `idfkEstado`, `nombre_estudiante`, `codicion_solicitud`, `observacion`) VALUES (2, 'Inscripción - Programación I', '2024-01-16 14:20:00', 2, 2, 1, 'Carlos López', 'Primera_Vez', 'Inscripción en curso de verano');
INSERT IGNORE INTO `solicitudes_cursos_verano_inscripcion`(`idSolicitud`, `nombre_solicitud`, `fecha_creacion`, `idfkUsuario`, `idfkCurso`, `idfkEstado`, `nombre_estudiante`, `codicion_solicitud`, `observacion`) VALUES (3, 'Inscripción - Matemáticas Básicas', '2024-01-17 09:15:00', 3, 1, 2, 'María Rodríguez', 'Primera_Vez', 'Inscripción pendiente de pago');
INSERT IGNORE INTO `solicitudes_cursos_verano_inscripcion`(`idSolicitud`, `nombre_solicitud`, `fecha_creacion`, `idfkUsuario`, `idfkCurso`, `idfkEstado`, `nombre_estudiante`, `codicion_solicitud`, `observacion`) VALUES (4, 'Inscripción - Bases de Datos', '2024-01-18 16:45:00', 4, 3, 1, 'Pedro Martínez', 'Primera_Vez', 'Inscripción en curso de verano');