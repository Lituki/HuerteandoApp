CREATE TABLE usuario (
    id_usuario bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre varchar NOT NULL,
    apellidos varchar,
    nick varchar NOT NULL,
    email varchar,
    password_hash varchar NOT NULL,
    avatar_url text,
    rol varchar NOT NULL DEFAULT 'USER',
    activo boolean NOT NULL DEFAULT true,
    fecha_registro timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso timestamp,
    CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario),
    CONSTRAINT usuario_nick_key UNIQUE (nick),
    CONSTRAINT usuario_email_key UNIQUE (email)
);

CREATE TABLE tipo_observacion (
    id_tipo_observacion smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre varchar NOT NULL,
    CONSTRAINT tipo_observacion_pkey PRIMARY KEY (id_tipo_observacion),
    CONSTRAINT tipo_observacion_nombre_key UNIQUE (nombre)
);

CREATE TABLE catalogo_eei (
    id_catalogo_eei bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre_cientifico varchar NOT NULL,
    nombre_comun varchar,
    reino varchar,
    familia varchar,
    normativa_ref text,
    fecha_actualizacion timestamp,
    CONSTRAINT catalogo_eei_pkey PRIMARY KEY (id_catalogo_eei),
    CONSTRAINT catalogo_eei_nombre_cientifico_key UNIQUE (nombre_cientifico)
);

CREATE TABLE especie (
    id_especie bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre_cientifico varchar NOT NULL,
    nombre_comun varchar,
    familia varchar,
    descripcion text,
    fecha_creacion timestamp,
    id_catalogo_eei bigint,
    CONSTRAINT especie_pkey PRIMARY KEY (id_especie),
    CONSTRAINT especie_nombre_cientifico_key UNIQUE (nombre_cientifico),
    CONSTRAINT especie_id_catalogo_eei_fkey FOREIGN KEY (id_catalogo_eei) REFERENCES catalogo_eei (id_catalogo_eei)
);

CREATE TABLE observacion (
    id_observacion bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_usuario bigint NOT NULL,
    id_tipo_observacion smallint NOT NULL,
    id_especie bigint,
    titulo varchar,
    descripcion text,
    fecha_observacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado varchar NOT NULL DEFAULT 'ABIERTA',
    nombre_tradicional varchar,
    identificacion_propuesta varchar,
    latitud numeric NOT NULL,
    longitud numeric NOT NULL,
    direccion_txt text,
    nombre_zona varchar,
    estado_identificacion varchar,
    fuente_identificacion varchar,
    confianza_ia numeric,
    creado_en timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT observacion_pkey PRIMARY KEY (id_observacion),
    CONSTRAINT observacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT observacion_id_tipo_observacion_fkey FOREIGN KEY (id_tipo_observacion) REFERENCES tipo_observacion (id_tipo_observacion),
    CONSTRAINT observacion_id_especie_fkey FOREIGN KEY (id_especie) REFERENCES especie (id_especie)
);

CREATE TABLE imagen (
    id_imagen bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    url_archivo text NOT NULL,
    titulo varchar,
    creado_en timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT imagen_pkey PRIMARY KEY (id_imagen),
    CONSTRAINT imagen_id_observacion_fkey FOREIGN KEY (id_observacion) REFERENCES observacion (id_observacion)
);

CREATE TABLE comentario (
    id_comentario bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    contenido text NOT NULL,
    creado_en timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editado_en timestamp,
    CONSTRAINT comentario_pkey PRIMARY KEY (id_comentario),
    CONSTRAINT comentario_id_observacion_fkey FOREIGN KEY (id_observacion) REFERENCES observacion (id_observacion),
    CONSTRAINT comentario_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE observacion_like (
    id_like bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    creado_en timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT observacion_like_pkey PRIMARY KEY (id_like),
    CONSTRAINT observacion_like_id_observacion_fkey FOREIGN KEY (id_observacion) REFERENCES observacion (id_observacion),
    CONSTRAINT observacion_like_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT uq_observacion_usuario UNIQUE (id_observacion, id_usuario)
);
