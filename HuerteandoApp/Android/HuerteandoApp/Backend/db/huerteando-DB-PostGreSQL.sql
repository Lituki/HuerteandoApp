


CREATE TABLE IF NOT EXISTS usuario (
    id_usuario bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre character varying NOT NULL,
    apellidos character varying,
    nick character varying NOT NULL,
    email character varying,
    password_hash character varying NOT NULL,
    avatar_url text,
    rol character varying NOT NULL DEFAULT 'USER',
    activo boolean NOT NULL DEFAULT true,
    fecha_registro timestamp without time zone NOT NULL DEFAULT now(),
    ultimo_acceso timestamp without time zone,
    CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario),
    CONSTRAINT usuario_nick_key UNIQUE (nick),
    CONSTRAINT usuario_email_key UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS tipo_observacion (
    id_tipo smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre character varying NOT NULL,
    descripcion text,
    CONSTRAINT tipo_observacion_pkey PRIMARY KEY (id_tipo),
    CONSTRAINT tipo_observacion_nombre_key UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS catalogo_eei (
    id_eei bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre_cientifico character varying NOT NULL,
    nombre_comun character varying,
    reino character varying,
    familia character varying,
    normativa_ref text,
    fecha_actualizacion timestamp without time zone,
    CONSTRAINT catalogo_eei_pkey PRIMARY KEY (id_eei),
    CONSTRAINT catalogo_eei_nombre_cientifico_key UNIQUE (nombre_cientifico)
);

CREATE TABLE IF NOT EXISTS especie (
    id_especie bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre_cientifico character varying NOT NULL,
    nombre_comun character varying,
    familia character varying,
    descripcion text,
    fecha_creacion timestamp without time zone,
    id_eei bigint,
    CONSTRAINT especie_pkey PRIMARY KEY (id_especie),
    CONSTRAINT especie_nombre_cientifico_key UNIQUE (nombre_cientifico),
    CONSTRAINT especie_id_eei_key UNIQUE (id_eei),
    CONSTRAINT especie_id_eei_fkey
        FOREIGN KEY (id_eei)
        REFERENCES catalogo_eei (id_eei)
);

CREATE TABLE IF NOT EXISTS observacion (
    id_observacion bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_usuario bigint NOT NULL,
    id_tipo smallint NOT NULL,
    id_especie bigint,
    titulo character varying,
    descripcion text,
    fecha_observacion timestamp without time zone NOT NULL DEFAULT now(),
    estado_observacion character varying NOT NULL DEFAULT 'ABIERTA',
    nombre_tradicional character varying,
    identificacion_propuesta character varying,
    latitud numeric NOT NULL,
    longitud numeric NOT NULL,
    direccion_txt text,
    nombre_zona character varying,
    estado_identificacion character varying,
    fuente_identificacion character varying,
    confianza_ia numeric,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    actualizado_en timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT observacion_pkey PRIMARY KEY (id_observacion),
    CONSTRAINT observacion_id_usuario_fkey
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario),
    CONSTRAINT observacion_id_tipo_fkey
        FOREIGN KEY (id_tipo)
        REFERENCES tipo_observacion (id_tipo),
    CONSTRAINT observacion_id_especie_fkey
        FOREIGN KEY (id_especie)
        REFERENCES especie (id_especie)
);

CREATE TABLE IF NOT EXISTS imagen (
    id_imagen bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    url_archivo text NOT NULL,
    titulo character varying,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT imagen_pkey PRIMARY KEY (id_imagen),
    CONSTRAINT imagen_id_observacion_fkey
        FOREIGN KEY (id_observacion)
        REFERENCES observacion (id_observacion)
);

CREATE TABLE IF NOT EXISTS comentario (
    id_comentario bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    contenido text NOT NULL,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    editado_en timestamp without time zone,
    CONSTRAINT comentario_pkey PRIMARY KEY (id_comentario),
    CONSTRAINT comentario_id_observacion_fkey
        FOREIGN KEY (id_observacion)
        REFERENCES observacion (id_observacion),
    CONSTRAINT comentario_id_usuario_fkey
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
);

CREATE TABLE IF NOT EXISTS "like" (
    id_like bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT like_pkey PRIMARY KEY (id_like),
    CONSTRAINT like_id_observacion_fkey
        FOREIGN KEY (id_observacion)
        REFERENCES observacion (id_observacion),
    CONSTRAINT like_id_usuario_fkey
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario),
    CONSTRAINT uq_observacion_usuario UNIQUE (id_observacion, id_usuario)
);

COMMIT;