INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Administrador')
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Estudiante')
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Coordinador')
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Secretario')
INSERT INTO `roles`(`idRol`, `nombre`) VALUES (NULL,'Funcionario')
INSERT INTO `programas`(`idPrograma`, `codigo`, `nombre_programa`) VALUES (NULL,'1046','Ingenieria Sistemas')
INSERT INTO `programas`(`idPrograma`, `codigo`, `nombre_programa`) VALUES (NULL,'1047','Ingenieria Electronica')
INSERT INTO `programas`(`idPrograma`, `codigo`, `nombre_programa`) VALUES (NULL,'1048','Ingenieria de Telecomunicaciones')
INSERT INTO `docentes`(`idDocente`, `codigo_docente`, `nombre_docente`) VALUES (NULL, '1046','Pepe Perez')

-- Datos de prueba para cursos de verano
INSERT INTO `materias`(`idMateria`, `codigo_materia`, `nombre_materia`, `creditos`, `idfkPrograma`) VALUES (NULL, 'MAT001', 'Matemáticas Básicas', 3, 1)
INSERT INTO `materias`(`idMateria`, `codigo_materia`, `nombre_materia`, `creditos`, `idfkPrograma`) VALUES (NULL, 'PROG001', 'Programación I', 4, 1)
INSERT INTO `materias`(`idMateria`, `codigo_materia`, `nombre_materia`, `creditos`, `idfkPrograma`) VALUES (NULL, 'BD001', 'Bases de Datos', 3, 1)

INSERT INTO `usuarios`(`idUsuario`, `codigo_usuario`, `nombre_usuario`, `correo`, `contrasena`, `idfkRol`) VALUES (NULL, 'EST001', 'Juan Pérez', 'juan.perez@unicauca.edu.co', 'password123', 2)
INSERT INTO `usuarios`(`idUsuario`, `codigo_usuario`, `nombre_usuario`, `correo`, `contrasena`, `idfkRol`) VALUES (NULL, 'EST002', 'María García', 'maria.garcia@unicauca.edu.co', 'password123', 2)
INSERT INTO `usuarios`(`idUsuario`, `codigo_usuario`, `nombre_usuario`, `correo`, `contrasena`, `idfkRol`) VALUES (NULL, 'DOC001', 'Carlos López', 'carlos.lopez@unicauca.edu.co', 'password123', 1)

INSERT INTO `cursos_ofertados_verano`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 1, 1, 'A', 30, 'A-101')
INSERT INTO `cursos_ofertados_verano`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 2, 1, 'B', 25, 'A-102')
INSERT INTO `cursos_ofertados_verano`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 3, 1, 'C', 20, 'A-103')

INSERT INTO `estados_curso_ofertado`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Publicado', NOW(), 1)
INSERT INTO `estados_curso_ofertado`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Preinscripcion', NOW(), 2)
INSERT INTO `estados_curso_ofertado`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Inscripcion', NOW(), 3)

-- Cursos adicionales para pruebas del frontend
INSERT INTO `materias`(`idMateria`, `codigo_materia`, `nombre_materia`, `creditos`, `idfkPrograma`) VALUES (NULL, 'FIS401', 'Física Cuántica', 4, 1)
INSERT INTO `materias`(`idMateria`, `codigo_materia`, `nombre_materia`, `creditos`, `idfkPrograma`) VALUES (NULL, 'QUI301', 'Química Orgánica', 3, 1)
INSERT INTO `materias`(`idMateria`, `codigo_materia`, `nombre_materia`, `creditos`, `idfkPrograma`) VALUES (NULL, 'BIO201', 'Biología Molecular', 3, 1)

INSERT INTO `cursos_ofertados_verano`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 4, 1, 'A', 30, 'B-101')
INSERT INTO `cursos_ofertados_verano`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 5, 1, 'B', 25, 'B-102')
INSERT INTO `cursos_ofertados_verano`(`idCurso`, `idfkMateria`, `idfkDocente`, `grupo`, `cupo_estimado`, `salon`) VALUES (NULL, 6, 1, 'C', 20, 'B-103')

INSERT INTO `estados_curso_ofertado`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Abierto', NOW(), 4)
INSERT INTO `estados_curso_ofertado`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Cerrado', NOW(), 5)
INSERT INTO `estados_curso_ofertado`(`idEstado`, `estado_actual`, `fecha_registro_estado`, `idfkCurso`) VALUES (NULL, 'Preinscripcion', NOW(), 6)