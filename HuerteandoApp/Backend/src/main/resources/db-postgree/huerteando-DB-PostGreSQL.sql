-- Script PostgreSQL (Supabase) alineado con las entidades JPA del proyecto.
-- Crea todo en el schema "huerteando" (coincide con currentSchema=huerteando).
-- Ojo: en Supabase/Postgres las tablas se referencian como "schema.tabla" (ej: huerteando.imagen), no "bd.schema.tabla".

CREATE SCHEMA IF NOT EXISTS huerteando;
SET search_path TO huerteando;

-- Limpieza (idempotente)
DROP TABLE IF EXISTS comentario CASCADE;
DROP TABLE IF EXISTS megusta CASCADE;
DROP TABLE IF EXISTS imagen CASCADE;
DROP TABLE IF EXISTS observacion CASCADE;
DROP TABLE IF EXISTS especie CASCADE;
DROP TABLE IF EXISTS tipo_observacion CASCADE;
DROP TABLE IF EXISTS catalogo_eei CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

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
    fecha_registro timestamp without time zone NOT NULL DEFAULT now(),
    ultimo_acceso timestamp without time zone,
    CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario),
    CONSTRAINT usuario_nick_key UNIQUE (nick),
    CONSTRAINT usuario_email_key UNIQUE (email)
);

CREATE TABLE tipo_observacion (
    id_tipo smallint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre varchar NOT NULL,
    descripcion text,
    CONSTRAINT tipo_observacion_pkey PRIMARY KEY (id_tipo),
    CONSTRAINT tipo_observacion_nombre_key UNIQUE (nombre)
);

CREATE TABLE catalogo_eei (
    id_eei bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre_cientifico varchar NOT NULL,
    nombre_comun varchar,
    reino varchar,
    familia varchar,
    normativa_ref text,
    fecha_actualizacion timestamp without time zone,
    CONSTRAINT catalogo_eei_pkey PRIMARY KEY (id_eei),
    CONSTRAINT catalogo_eei_nombre_cientifico_key UNIQUE (nombre_cientifico)
);

CREATE TABLE especie (
    id_especie bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    nombre_cientifico varchar NOT NULL,
    nombre_comun varchar,
    familia varchar,
    descripcion text,
    fecha_creacion timestamp without time zone,
    id_eei bigint,
    CONSTRAINT especie_pkey PRIMARY KEY (id_especie),
    CONSTRAINT especie_nombre_cientifico_key UNIQUE (nombre_cientifico),
    CONSTRAINT especie_id_eei_key UNIQUE (id_eei),
    CONSTRAINT especie_id_eei_fkey FOREIGN KEY (id_eei) REFERENCES catalogo_eei (id_eei)
);

CREATE TABLE observacion (
    id_observacion bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_usuario bigint NOT NULL,
    id_tipo smallint NOT NULL,
    id_especie bigint,
    titulo varchar,
    descripcion text,
    fecha_observacion timestamp without time zone NOT NULL DEFAULT now(),
    estado_observacion varchar NOT NULL DEFAULT 'ABIERTA',
    nombre_tradicional varchar,
    identificacion_propuesta varchar,
    latitud numeric NOT NULL,
    longitud numeric NOT NULL,
    direccion_txt text,
    nombre_zona varchar,
    estado_identificacion varchar,
    fuente_identificacion varchar,
    confianza_ia numeric,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    actualizado_en timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT observacion_pkey PRIMARY KEY (id_observacion),
    CONSTRAINT observacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT observacion_id_tipo_fkey FOREIGN KEY (id_tipo) REFERENCES tipo_observacion (id_tipo),
    CONSTRAINT observacion_id_especie_fkey FOREIGN KEY (id_especie) REFERENCES especie (id_especie)
);

CREATE TABLE imagen (
    id_imagen bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    url_archivo text NOT NULL,
    titulo varchar,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT imagen_pkey PRIMARY KEY (id_imagen),
    CONSTRAINT imagen_id_observacion_fkey FOREIGN KEY (id_observacion) REFERENCES observacion (id_observacion)
);

CREATE TABLE comentario (
    id_comentario bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    contenido text NOT NULL,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    editado_en timestamp without time zone,
    CONSTRAINT comentario_pkey PRIMARY KEY (id_comentario),
    CONSTRAINT comentario_id_observacion_fkey FOREIGN KEY (id_observacion) REFERENCES observacion (id_observacion),
    CONSTRAINT comentario_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE megusta (
    id_megusta bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_observacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    creado_en timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT megusta_pkey PRIMARY KEY (id_megusta),
    CONSTRAINT megusta_id_observacion_fkey FOREIGN KEY (id_observacion) REFERENCES observacion (id_observacion),
    CONSTRAINT megusta_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT uq_observacion_usuario UNIQUE (id_observacion, id_usuario)
);