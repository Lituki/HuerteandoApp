# Catalogo de endpoints de Huerteando

Este documento resume los endpoints disenados en el backend y marca cuales se
estan usando ahora mismo desde la app Android.

Leyenda:

- **Usado ahora**: la app Android tiene una pantalla o flujo que llama a ese endpoint.
- **Disenado**: existe en backend/API, pero ahora mismo no es una parte principal del flujo o se usa como compatibilidad.
- **Publico**: se puede llamar sin login.
- **Privado**: requiere token JWT de Supabase, salvo que se indique lo contrario en la configuracion.

---

## Auth y usuarios

| Estado | Metodo | Endpoint | Acceso | Para que sirve | Uso actual |
|---|---|---|---|---|---|
| Usado ahora | GET | `/api/auth/disponibilidad?nick={nick}&email={email}` | Publico | Comprueba si nick/email estan libres antes de registrar en Supabase. | `RegistroActivity` |
| Usado ahora | POST | `/api/auth/register` | Publico | Crea el perfil local del usuario en la tabla `usuarios` despues de Supabase. | `RegistroActivity` |
| Usado ahora | POST | `/api/auth/login-jwt` | Privado | Valida el JWT de Supabase y devuelve el usuario local por email. | `LoginActivity` |
| Disenado | POST | `/api/auth/login` | Publico | Login clasico con nick/password del backend. Se mantiene por compatibilidad. | No se usa en el flujo nuevo con Supabase |
| Usado ahora | GET | `/api/usuarios/{id}` | Privado | Devuelve el perfil de un usuario por id. | `PerfilActivity` |
| Usado ahora | POST | `/api/usuarios/{id}/avatar` | Privado | Sube/cambia el avatar del usuario. | `PerfilActivity` |

Notas:

- El registro recomendado es: disponibilidad -> Supabase -> `/api/auth/register`.
- `/api/auth/login` no es el login principal actual. El login actual es Supabase + `/api/auth/login-jwt`.

---

## Tipos de observacion

| Estado | Metodo | Endpoint | Acceso | Para que sirve | Uso actual |
|---|---|---|---|---|---|
| Usado ahora | GET | `/api/tipos-observacion` | Publico | Lista los tipos disponibles: planta, rincon, incidencia, etc. | `ObservacionesActivity`, `CrearObservacionActivity` |
| Disenado | GET | `/api/tipos-observacion/{id}` | Publico | Consulta un tipo concreto por id. | Declarado en `ApiService`, pero sin uso claro en pantalla |

---

## Observaciones

| Estado | Metodo | Endpoint | Acceso | Para que sirve | Uso actual |
|---|---|---|---|---|---|
| Usado ahora | GET | `/api/observaciones` | Publico | Lista todas las observaciones. | `ObservacionesActivity` |
| Usado ahora | GET | `/api/observaciones?tipo={idTipo}` | Publico | Lista observaciones filtradas por tipo. | `ObservacionesActivity` |
| Usado ahora | GET | `/api/observaciones?usuario={idUsuario}` | Publico | Lista observaciones filtradas por usuario. | `ObservacionesActivity` |
| Usado ahora | GET | `/api/observaciones?estado_observacion={estado}` | Publico | Lista observaciones filtradas por estado. | `ObservacionesActivity` |
| Usado ahora | GET | `/api/observaciones/{id}` | Publico | Devuelve una observacion concreta. | `DetalleObservacionActivity`, `CrearObservacionActivity` al editar |
| Usado ahora | POST | `/api/observaciones` | Privado | Crea una observacion nueva. | `CrearObservacionActivity` |
| Usado ahora | PUT | `/api/observaciones/{id}` | Privado | Edita una observacion existente. | `CrearObservacionActivity` en modo editar |
| Usado ahora | DELETE | `/api/observaciones/{id}` | Privado | Elimina una observacion. | `DetalleObservacionActivity` |

Nota:

- El filtro por estado existe y funciona, pero se ha apuntado como mejora quitarlo de la interfaz principal porque visualmente carga demasiado la cabecera.

---

## Imagenes de observaciones

| Estado | Metodo | Endpoint | Acceso | Para que sirve | Uso actual |
|---|---|---|---|---|---|
| Disenado | GET | `/api/observaciones/{idObservacion}/imagenes` | Publico | Lista las imagenes de una observacion. | No se usa directamente desde `ApiService`; las imagenes suelen venir dentro de la observacion |
| Usado ahora | POST | `/api/observaciones/{idObservacion}/imagenes` | Privado | Sube una imagen a una observacion. | `CrearObservacionActivity` |
| Usado ahora | DELETE | `/api/observaciones/{idObservacion}/imagenes/{idImagen}` | Privado | Elimina una imagen de una observacion. | `DetalleObservacionActivity` |

---

## Comentarios

| Estado | Metodo | Endpoint | Acceso | Para que sirve | Uso actual |
|---|---|---|---|---|---|
| Usado ahora | GET | `/api/observaciones/{idObservacion}/comentarios` | Publico | Lista los comentarios de una observacion. | `DetalleObservacionActivity` |
| Usado ahora | POST | `/api/observaciones/{idObservacion}/comentarios` | Privado | Crea un comentario en una observacion. | `DetalleObservacionActivity` |
| Usado ahora | DELETE | `/api/observaciones/{idObservacion}/comentarios/{idComentario}` | Privado | Elimina un comentario. | `DetalleObservacionActivity` |

---

## Me gustas

| Estado | Metodo | Endpoint | Acceso | Para que sirve | Uso actual |
|---|---|---|---|---|---|
| Usado ahora | GET | `/api/observaciones/{idObservacion}/megustas/count` | Publico | Devuelve el numero total de likes de una observacion. | `DetalleObservacionActivity` |
| Usado ahora | GET | `/api/observaciones/{idObservacion}/megustas/existe?idUsuario={idUsuario}` | Publico | Comprueba si un usuario ya dio like. | `DetalleObservacionActivity` |
| Usado ahora | POST | `/api/observaciones/{idObservacion}/megustas?idUsuario={idUsuario}` | Privado | Da like a una observacion. | `DetalleObservacionActivity` |
| Usado ahora | DELETE | `/api/observaciones/{idObservacion}/megustas?idUsuario={idUsuario}` | Privado | Quita el like de una observacion. | `DetalleObservacionActivity` |

Nota:

- En la lista principal las tarjetas muestran `numMeGustas`, pero ahora mismo ese valor puede llegar a `0` si el listado no lo rellena. Esta mejora esta documentada en `mejoras-pendientes-android.md`.

---

## Especies

| Estado | Metodo | Endpoint | Acceso | Para que sirve | Uso actual |
|---|---|---|---|---|---|
| Usado ahora | GET | `/api/especies` | Publico | Lista especies del catalogo. | `CrearObservacionActivity`, `EspeciesActivity` |
| Disenado | GET | `/api/especies/{id}` | Publico | Consulta una especie concreta. | Declarado en `ApiService`, pero sin uso claro en pantalla |
| Usado ahora | POST | `/api/especies` | Privado | Crea una especie nueva. | `EspeciesActivity` |
| Usado ahora | PUT | `/api/especies/{id}` | Privado | Edita una especie existente. | `EspeciesActivity` |
| Usado ahora | DELETE | `/api/especies/{id}` | Privado | Elimina una especie. | `EspeciesActivity` |

---

## Resumen rapido

Endpoints usados ahora por Android:

- Auth/usuarios: disponibilidad, registro, login JWT, perfil, avatar.
- Observaciones: listar, filtrar, ver detalle, crear, editar y eliminar.
- Imagenes: subir y eliminar.
- Comentarios: listar, crear y eliminar.
- Me gustas: contar, comprobar, dar y quitar.
- Especies: listar, crear, editar y eliminar.
- Tipos de observacion: listar.

Endpoints disenados pero poco usados o de compatibilidad:

- `POST /api/auth/login`: login local antiguo.
- `GET /api/tipos-observacion/{id}`: consulta individual de tipo.
- `GET /api/observaciones/{idObservacion}/imagenes`: listado directo de imagenes.
- `GET /api/especies/{id}`: consulta individual de especie.
