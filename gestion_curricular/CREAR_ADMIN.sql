-- Script para crear usuario Administrador directamente en la base de datos
-- Ejecutar este script si el usuario admin@unicauca.edu.co no existe o no puede hacer login

-- Hash BCrypt para "password123" (verificado)
-- Este hash se generó con BCryptPasswordEncoder y es válido para la contraseña "password123"

-- Opción 1: Insertar si no existe
INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, idfkRol, idfkPrograma) 
SELECT 26, 'ADMIN001', 'Administrador del Sistema', 'admin@unicauca.edu.co', 
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG', 
       1, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM Usuarios WHERE correo = 'admin@unicauca.edu.co'
);

-- Opción 2: Actualizar password si el usuario ya existe pero con password incorrecta
UPDATE Usuarios 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjFOjtXgjOU92LhL6YP1dPW1Z/uLlLG',
    idfkRol = 1,
    estado_usuario = 1
WHERE correo = 'admin@unicauca.edu.co';

-- Verificar que el usuario fue creado/actualizado correctamente
SELECT idUsuario, codigo, nombre_completo, correo, estado_usuario, 
       (SELECT nombre FROM Roles WHERE idRol = u.idfkRol) as rol
FROM Usuarios u
WHERE correo = 'admin@unicauca.edu.co';

