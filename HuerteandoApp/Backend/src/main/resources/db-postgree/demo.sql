-- Datos DEMO para PostgreSQL/Supabase.
-- Requiere que las tablas ya existan (ejecuta antes db/huerteando-DB-PostGreSQL.sql).
-- Ojo: en Postgres NO uses nombres tipo "huerteando.huerteando.imagen" (3 partes). Usa "huerteando.imagen".

-- Limpieza (idempotente)
-- Nota: usamos DELETE (más compatible). Si quieres reiniciar IDs, ejecuta también db/reset.sql y recrea el esquema.
DELETE FROM huerteando.megusta;
DELETE FROM huerteando.comentario;
DELETE FROM huerteando.imagen;
DELETE FROM huerteando.observacion;
DELETE FROM huerteando.especie;
DELETE FROM huerteando.catalogo_eei;
DELETE FROM huerteando.tipo_observacion;
DELETE FROM huerteando.usuario;

-- Tipos de observacion basicos
INSERT INTO huerteando.tipo_observacion (nombre, descripcion) VALUES
    ('Planta', 'Observacion de especie vegetal'),
    ('Rincon', 'Lugar o zona de interes en la huerta'),
    ('Incidencia', 'Problema ambiental o de mantenimiento');

-- Usuarios de prueba
INSERT INTO huerteando.usuario (nombre, apellidos, nick, email, password_hash, rol)
VALUES
    ('Sergio',    'Caro',     'sergio',    'sergio@demo.local',    '1234',  'USER'),
    ('Antonio',   'Pérez',    'antonio',   'antonio@demo.local',   '1234',  'USER'),
    ('Pepe',      'Martínez', 'pepe',      'pepe@demo.local',      '1234',  'USER'),
    ('Pedro',     'García',   'pedro',     'pedro@demo.local',     '1234',  'USER'),
    ('Clara',     'López',    'clara',     'clara@demo.local',     '1234',  'USER'),
    ('Sofía',     'Ruiz',     'sofia',     'sofia@demo.local',     '1234',  'USER'),
    ('Leonor',    'Sánchez',  'leonor',    'leonor@demo.local',    '1234',  'USER'),
    ('Angustias', 'Navarro',  'angustias', 'angustias@demo.local', '1234',  'USER'),
    ('Admin',     'Demo',     'admin',     'admin@demo.local',     'admin', 'ADMIN');

-- Catalogo EEI
INSERT INTO huerteando.catalogo_eei (nombre_cientifico, nombre_comun, reino, familia, normativa_ref, fecha_actualizacion)
VALUES
    ('Arundo donax',        'Caña común',      'Plantae', 'Poaceae',       'Real Decreto 630/2013', now()),
    ('Ailanthus altissima', 'Árbol del cielo', 'Plantae', 'Simaroubaceae', 'Real Decreto 630/2013', now());

-- Especies
-- Mezcla de invasoras y no invasoras
INSERT INTO huerteando.especie (nombre_cientifico, nombre_comun, familia, descripcion, fecha_creacion, id_eei)
VALUES
    (
        'Arundo donax',
        'Caña común',
        'Poaceae',
        'Muy comun en acequias.',
        CURRENT_TIMESTAMP,
        (SELECT id_eei FROM huerteando.catalogo_eei WHERE nombre_cientifico = 'Arundo donax')
    ),
    (
        'Ailanthus altissima',
        'Árbol del cielo',
        'Simaroubaceae',
        'Rebrota con fuerza.',
        CURRENT_TIMESTAMP,
        (SELECT id_eei FROM huerteando.catalogo_eei WHERE nombre_cientifico = 'Ailanthus altissima')
    ),
    (
        'Nerium oleander',
        'Adelfa',
        'Apocynaceae',
        'Muy tipica en ramblas.',
        CURRENT_TIMESTAMP,
        NULL
    );

-- Observaciones
INSERT INTO huerteando.observacion (
    id_usuario,
    id_tipo,
    id_especie,
    titulo,
    descripcion,
    fecha_observacion,
    estado_observacion,
    nombre_tradicional,
    identificacion_propuesta,
    latitud,
    longitud,
    direccion_txt,
    nombre_zona,
    estado_identificacion,
    fuente_identificacion,
    confianza_ia,
    creado_en,
    actualizado_en
)
VALUES
    (
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'sergio'),
        (SELECT id_tipo FROM huerteando.tipo_observacion WHERE nombre = 'Planta'),
        (SELECT id_especie FROM huerteando.especie WHERE nombre_cientifico = 'Arundo donax'),
        'Cana junto a acequia',
        'Mata extensa en el borde del canal',
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        'ABIERTA',
        'Cana',
        'Arundo donax',
        37.987123,
        -1.112456,
        'Camino de la acequia mayor',
        'Huerta norte',
        'PROPUESTA',
        'IA',
        0.87,
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days'
    ),
    (
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'clara'),
        (SELECT id_tipo FROM huerteando.tipo_observacion WHERE nombre = 'Planta'),
        NULL,
        'Planta sin identificar',
        'Ejemplar joven, pendiente de confirmar especie',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        'ABIERTA',
        'Hierba alta',
        'Posible ailanto',
        37.992654,
        -1.118321,
        'Senda lateral del huerto',
        'Huerta sur',
        'PENDIENTE',
        'MANUAL',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'admin'),
        (SELECT id_tipo FROM huerteando.tipo_observacion WHERE nombre = 'Incidencia'),
        NULL,
        'Vertido detectado',
        'Se observa residuo en margen de riego',
        CURRENT_TIMESTAMP - INTERVAL '3 hours',
        'ABIERTA',
        NULL,
        NULL,
        37.990011,
        -1.120099,
        'Cruce de acequias',
        'Zona central',
        'NO_APLICA',
        'MANUAL',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '3 hours',
        CURRENT_TIMESTAMP - INTERVAL '3 hours'
    );

INSERT INTO huerteando.imagen (id_observacion, url_archivo, titulo, creado_en)
VALUES
    (
        (SELECT id_observacion FROM huerteando.observacion WHERE titulo = 'Cana junto a acequia'),
        'https://example.org/img/cana-acequia-1.jpg',
        'Detalle de tallos',
        CURRENT_TIMESTAMP - INTERVAL '2 days'
    ),
    (
        (SELECT id_observacion FROM huerteando.observacion WHERE titulo = 'Planta sin identificar'),
        'https://example.org/img/planta-sin-id-1.jpg',
        'Vista general',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    );

INSERT INTO huerteando.comentario (id_observacion, id_usuario, contenido, creado_en, editado_en)
VALUES
    (
        (SELECT id_observacion FROM huerteando.observacion WHERE titulo = 'Cana junto a acequia'),
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'antonio'),
        'Coincido con la identificacion propuesta.',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        NULL
    ),
    (
        (SELECT id_observacion FROM huerteando.observacion WHERE titulo = 'Planta sin identificar'),
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'pepe'),
        'Podria ser ailanto, revisar hoja compuesta.',
        CURRENT_TIMESTAMP - INTERVAL '12 hours',
        NULL
    );

INSERT INTO huerteando.megusta (id_observacion, id_usuario, creado_en)
VALUES
    (
        (SELECT id_observacion FROM huerteando.observacion WHERE titulo = 'Cana junto a acequia'),
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'clara'),
        CURRENT_TIMESTAMP - INTERVAL '20 hours'
    ),
    (
        (SELECT id_observacion FROM huerteando.observacion WHERE titulo = 'Planta sin identificar'),
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'sergio'),
        CURRENT_TIMESTAMP - INTERVAL '10 hours'
    ),
    (
        (SELECT id_observacion FROM huerteando.observacion WHERE titulo = 'Vertido detectado'),
        (SELECT id_usuario FROM huerteando.usuario WHERE nick = 'admin'),
        CURRENT_TIMESTAMP - INTERVAL '2 hours'
    );
