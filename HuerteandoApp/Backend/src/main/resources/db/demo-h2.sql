-- El schema lo crea el script de estructura (huerteando-DB-H2.sql).

DELETE FROM observacion_like;
DELETE FROM comentario;
DELETE FROM imagen;
DELETE FROM observacion;
DELETE FROM especie;
DELETE FROM catalogo_eei;
DELETE FROM tipo_observacion;
DELETE FROM usuario;

INSERT INTO tipo_observacion (nombre) VALUES
    ('Planta'),
    ('Rincon'),
    ('Incidencia');

INSERT INTO usuario (nombre, apellidos, nick, email, password_hash, rol)
VALUES
    ('Sergio', 'Caro', 'sergio', 'sergio@demo.local', '1234', 'USER'),
    ('Antonio', 'Perez', 'antonio', 'antonio@demo.local', '1234', 'USER'),
    ('Pepe', 'Martinez', 'pepe', 'pepe@demo.local', '1234', 'USER'),
    ('Pedro', 'Garcia', 'pedro', 'pedro@demo.local', '1234', 'USER'),
    ('Clara', 'Lopez', 'clara', 'clara@demo.local', '1234', 'USER'),
    ('Sofia', 'Ruiz', 'sofia', 'sofia@demo.local', '1234', 'USER'),
    ('Leonor', 'Sanchez', 'leonor', 'leonor@demo.local', '1234', 'USER'),
    ('Angustias', 'Navarro', 'angustias', 'angustias@demo.local', '1234', 'USER'),
    ('Admin', 'Demo', 'admin', 'admin@demo.local', 'admin', 'ADMIN');

INSERT INTO catalogo_eei (nombre_cientifico, nombre_comun, reino, familia, normativa_ref, fecha_actualizacion)
VALUES
    ('Arundo donax', 'Cana comun', 'Plantae', 'Poaceae', 'Real Decreto 630/2013', CURRENT_TIMESTAMP),
    ('Ailanthus altissima', 'Arbol del cielo', 'Plantae', 'Simaroubaceae', 'Real Decreto 630/2013', CURRENT_TIMESTAMP);

INSERT INTO especie (nombre_cientifico, nombre_comun, familia, descripcion, fecha_creacion, id_catalogo_eei)
VALUES
    (
        'Arundo donax',
        'Cana comun',
        'Poaceae',
        'Muy comun en acequias.',
        CURRENT_TIMESTAMP,
        (SELECT id_catalogo_eei FROM catalogo_eei WHERE nombre_cientifico = 'Arundo donax')
    );

INSERT INTO especie (nombre_cientifico, nombre_comun, familia, descripcion, fecha_creacion, id_catalogo_eei)
VALUES
    (
        'Ailanthus altissima',
        'Arbol del cielo',
        'Simaroubaceae',
        'Rebrota con fuerza.',
        CURRENT_TIMESTAMP,
        (SELECT id_catalogo_eei FROM catalogo_eei WHERE nombre_cientifico = 'Ailanthus altissima')
    );

INSERT INTO especie (nombre_cientifico, nombre_comun, familia, descripcion, fecha_creacion, id_catalogo_eei)
VALUES
    ('Nerium oleander', 'Adelfa', 'Apocynaceae', 'Muy tipica en ramblas.', CURRENT_TIMESTAMP, NULL);

INSERT INTO observacion (
    id_usuario,
    id_tipo_observacion,
    id_especie,
    titulo,
    descripcion,
    fecha_observacion,
    estado,
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
        (SELECT id_usuario FROM usuario WHERE nick = 'sergio'),
        (SELECT id_tipo_observacion FROM tipo_observacion WHERE nombre = 'Planta'),
        (SELECT id_especie FROM especie WHERE nombre_cientifico = 'Arundo donax'),
        'Cana junto a acequia',
        'Mata extensa en el borde del canal',
        DATEADD('DAY', -2, CURRENT_TIMESTAMP),
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
        DATEADD('DAY', -2, CURRENT_TIMESTAMP),
        DATEADD('DAY', -2, CURRENT_TIMESTAMP)
    ),
    (
        (SELECT id_usuario FROM usuario WHERE nick = 'clara'),
        (SELECT id_tipo_observacion FROM tipo_observacion WHERE nombre = 'Planta'),
        NULL,
        'Planta sin identificar',
        'Ejemplar joven, pendiente de confirmar especie',
        DATEADD('DAY', -1, CURRENT_TIMESTAMP),
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
        DATEADD('DAY', -1, CURRENT_TIMESTAMP),
        DATEADD('DAY', -1, CURRENT_TIMESTAMP)
    ),
    (
        (SELECT id_usuario FROM usuario WHERE nick = 'admin'),
        (SELECT id_tipo_observacion FROM tipo_observacion WHERE nombre = 'Incidencia'),
        NULL,
        'Vertido detectado',
        'Se observa residuo en margen de riego',
        DATEADD('HOUR', -3, CURRENT_TIMESTAMP),
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
        DATEADD('HOUR', -3, CURRENT_TIMESTAMP),
        DATEADD('HOUR', -3, CURRENT_TIMESTAMP)
    );

INSERT INTO imagen (id_observacion, url_archivo, titulo, creado_en)
VALUES
    (
        (SELECT id_observacion FROM observacion WHERE titulo = 'Cana junto a acequia'),
        'https://example.org/img/cana-acequia-1.jpg',
        'Detalle de tallos',
        DATEADD('DAY', -2, CURRENT_TIMESTAMP)
    ),
    (
        (SELECT id_observacion FROM observacion WHERE titulo = 'Planta sin identificar'),
        'https://example.org/img/planta-sin-id-1.jpg',
        'Vista general',
        DATEADD('DAY', -1, CURRENT_TIMESTAMP)
    );

INSERT INTO comentario (id_observacion, id_usuario, contenido, creado_en, editado_en)
VALUES
    (
        (SELECT id_observacion FROM observacion WHERE titulo = 'Cana junto a acequia'),
        (SELECT id_usuario FROM usuario WHERE nick = 'antonio'),
        'Coincido con la identificacion propuesta.',
        DATEADD('DAY', -1, CURRENT_TIMESTAMP),
        NULL
    ),
    (
        (SELECT id_observacion FROM observacion WHERE titulo = 'Planta sin identificar'),
        (SELECT id_usuario FROM usuario WHERE nick = 'pepe'),
        'Podria ser ailanto, revisar hoja compuesta.',
        DATEADD('HOUR', -12, CURRENT_TIMESTAMP),
        NULL
    );

INSERT INTO observacion_like (id_observacion, id_usuario, creado_en)
VALUES
    (
        (SELECT id_observacion FROM observacion WHERE titulo = 'Cana junto a acequia'),
        (SELECT id_usuario FROM usuario WHERE nick = 'clara'),
        DATEADD('HOUR', -20, CURRENT_TIMESTAMP)
    ),
    (
        (SELECT id_observacion FROM observacion WHERE titulo = 'Planta sin identificar'),
        (SELECT id_usuario FROM usuario WHERE nick = 'sergio'),
        DATEADD('HOUR', -10, CURRENT_TIMESTAMP)
    ),
    (
        (SELECT id_observacion FROM observacion WHERE titulo = 'Vertido detectado'),
        (SELECT id_usuario FROM usuario WHERE nick = 'admin'),
        DATEADD('HOUR', -2, CURRENT_TIMESTAMP)
    );
