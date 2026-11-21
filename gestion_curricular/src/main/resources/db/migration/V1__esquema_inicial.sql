-- Esquema MySQL derivado de las entidades JPA.

CREATE TABLE Cursos_ofertados (
    cupo_estimado integer not null,
    idCurso integer not null auto_increment,
    idfkDocente integer not null,
    idfkMateria integer not null,
    fecha_fin datetime(6),
    fecha_inicio datetime(6),
    periodo_academico varchar(10),
    salon varchar(100) not null,
    grupo enum ('A','B','C','D') not null,
    primary key (idCurso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cursosEstudiantes (
    idCurso integer not null,
    idUsuario integer not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Docentes (
    idDocente integer not null auto_increment,
    codigo_docente varchar(12) not null,
    nombre_docente varchar(100) not null,
    primary key (idDocente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Documentos (
    esValido TINYINT,
    idDocumento integer not null auto_increment,
    idSolicitud integer,
    idUsuarioSubio integer,
    fecha_documento datetime(6),
    ruta_documento varchar(500),
    comentario varchar(2000),
    nombre varchar(255),
    primary key (idDocumento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Estadisticas (
    idEstadistica integer not null auto_increment,
    total_aprobadas integer,
    total_rechazadas integer,
    total_solicitudes integer not null,
    nombre varchar(100) not null,
    nombres_procesos varbinary(255),
    nombres_programas varbinary(255),
    periodos_academico varbinary(255),
    primary key (idEstadistica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE EstadosCursos (
    idEstado integer not null auto_increment,
    idfkCurso integer,
    fecha_registro_estado datetime(6),
    estado_actual varchar(100) not null,
    primary key (idEstado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE EstadosSolicitudes (
    idEstado integer not null auto_increment,
    idfkSolicitud integer,
    fecha_registro_estado datetime(6) not null,
    estado_actual varchar(100) not null,
    comentario varchar(500),
    primary key (idEstado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE FechaEcaes (
    aplicacion date not null,
    citacion date not null,
    idFechaEcaes integer not null auto_increment,
    inscripcion_est_by_facultad date not null,
    registro_recaudo_extraordinario date not null,
    registro_recaudo_ordinario date not null,
    resultados_individuales date not null,
    periodoAcademico varchar(255) not null,
    primary key (idFechaEcaes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Materias (
    creditos integer not null,
    idMateria integer not null auto_increment,
    codigo varchar(12) not null,
    nombre varchar(100) not null,
    primary key (idMateria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Notificaciones (
    esUrgente TINYINT not null,
    idCurso integer,
    idNotificacion integer not null auto_increment,
    idSolicitud integer,
    idUsuario integer not null,
    leida TINYINT not null,
    fechaCreacion datetime(6) not null,
    tipoNotificacion varchar(50) not null,
    tipoSolicitud varchar(50) not null,
    accion varchar(100),
    titulo varchar(200) not null,
    urlAccion varchar(500),
    mensaje varchar(1000) not null,
    primary key (idNotificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Programas (
    idPrograma integer not null auto_increment,
    codigo varchar(12) not null,
    nombre_programa varchar(100) not null,
    primary key (idPrograma)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Roles (
    idRol integer not null auto_increment,
    nombre varchar(100) not null,
    primary key (idRol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Salones (
    activo bit,
    idSalon integer not null auto_increment,
    numero_salon varchar(10) not null,
    edificio varchar(100),
    primary key (idSalon)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Solicitudes (
    idCurso integer,
    idSolicitud integer not null auto_increment,
    idUsuario integer,
    fecha_ceremonia datetime(6),
    fecha_registro_solicitud datetime(6) not null,
    periodo_academico varchar(50) not null,
    nombre_solicitud varchar(100) not null,
    primary key (idSolicitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Solicitudes_Cursos_Verano_Inscripcion (
    idSolicitud integer not null,
    nombre_estudiante varchar(100) not null,
    observacion varchar(100) not null,
    codicion_solicitud enum ('Primera_Vez','Habilitación','Repeteción') not null,
    primary key (idSolicitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Solicitudes_Cursos_Verano_Preinscripcion (
    idSolicitud integer not null,
    nombre_estudiante varchar(100) not null,
    observacion varchar(100) not null,
    codicion_solicitud enum ('Primera_Vez','Habilitación','Repeteción') not null,
    primary key (idSolicitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Solicitudes_Ecaes (
    idSolicitud integer not null,
    fecha_expedicion datetime(6) not null,
    fecha_nacimiento datetime(6) not null,
    numero_documento varchar(100) not null,
    tipoDocumento enum ('CC','CE') not null,
    primary key (idSolicitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Solicitudes_Homogolacion (
    idSolicitud integer not null,
    programa_destino varchar(200),
    programa_origen varchar(200),
    ruta_PM_FO_4_FOR_27 varchar(255),
    ruta_contenido_programatico varchar(255),
    primary key (idSolicitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Solicitudes_PazYSalvo (
    idSolicitud integer not null,
    director_trabajo_grado varchar(200),
    titulo_trabajo_grado varchar(500),
    primary key (idSolicitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Solicitudes_Reingreso (
    idSolicitud integer not null,
    primary key (idSolicitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Usuarios (
    estado_usuario TINYINT not null,
    idUsuario integer not null auto_increment,
    idfkPrograma integer not null,
    idfkRol integer not null,
    codigo varchar(12) not null,
    cedula varchar(20) not null,
    correo varchar(100) not null,
    nombre_completo varchar(100) not null,
    password varchar(100) not null,
    primary key (idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

alter table Cursos_ofertados add constraint UK_3ochhytns8xfjaus54hapq3tx unique (idfkDocente);
alter table Cursos_ofertados add constraint UK_jtswq606tkpa13mdgv3qsxqyi unique (idfkMateria);
alter table Materias add constraint UK_ryv1uk5j3fnwvdx98xfcmn90f unique (codigo);
alter table Programas add constraint UK_fm68bbimscuc4v69r3hy1tsuy unique (codigo);
alter table Programas add constraint UK_aq38249mf9gumqegp9k5hivgp unique (nombre_programa);
alter table Salones add constraint UK_bhr9vt9sbcvbigbokcm7spms8 unique (numero_salon);
alter table Usuarios add constraint UK_8l6y1ak1f3dr0b1qsuas7vs6u unique (cedula);
alter table Usuarios add constraint UK_5fqsgapyqgm5bcl6kfr0ejovo unique (correo);
alter table Cursos_ofertados add constraint FKh43pt80flk4b3hav9pmywh2ex foreign key (idfkDocente) references Docentes (idDocente);
alter table Cursos_ofertados add constraint FKia9q9jeqptvsew2t6ge5004oc foreign key (idfkMateria) references Materias (idMateria);
alter table cursosEstudiantes add constraint FKg76vcto8fwpo5qknugcfhnnys foreign key (idUsuario) references Usuarios (idUsuario);
alter table cursosEstudiantes add constraint FKjxdw2j6jc3hs0etgy7fl73ucr foreign key (idCurso) references Cursos_ofertados (idCurso);
alter table Documentos add constraint FK8ldanyfwxo7ahkbq5oxg6htui foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table EstadosCursos add constraint FKp26kqe0a3pdujbeh06rbwpo2l foreign key (idfkCurso) references Cursos_ofertados (idCurso);
alter table EstadosSolicitudes add constraint FK5oftqd1ykno8havi6l8r5r7kh foreign key (idfkSolicitud) references Solicitudes (idSolicitud);
alter table Notificaciones add constraint FKmq3dtpysnw43fgxnsq1apx1b6 foreign key (idCurso) references Cursos_ofertados (idCurso);
alter table Notificaciones add constraint FKonvqb2l4o3qubcd1ar8odsnw foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table Notificaciones add constraint FK3hfvs87rxxq4lhakpr6f5vrsi foreign key (idUsuario) references Usuarios (idUsuario);
alter table Solicitudes add constraint FKcc7fpdev3390jkyqqfbw1jj31 foreign key (idCurso) references Cursos_ofertados (idCurso);
alter table Solicitudes add constraint FKo3x21kcxaqnec7hdrjee1cupi foreign key (idUsuario) references Usuarios (idUsuario);
alter table Solicitudes_Cursos_Verano_Inscripcion add constraint FKqmu82k4amat2ko1hk47m8p9os foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table Solicitudes_Cursos_Verano_Preinscripcion add constraint FK5d031482v79gy0aj8dm98aw9d foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table Solicitudes_Ecaes add constraint FKni7vu377n4v4u5a1fnavf79ih foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table Solicitudes_Homogolacion add constraint FK5h43vut5upiusudur3bqh6d99 foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table Solicitudes_PazYSalvo add constraint FK99c77k4bpu5tmria8k37us45y foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table Solicitudes_Reingreso add constraint FKqdjumpfn2wlw1jys3mujng9ro foreign key (idSolicitud) references Solicitudes (idSolicitud);
alter table Usuarios add constraint FK1p4fx6w3adyjhqp1ijof8wmwe foreign key (idfkPrograma) references Programas (idPrograma);
alter table Usuarios add constraint FKdj1ebfpx18wjb487ohi1csd7d foreign key (idfkRol) references Roles (idRol);
