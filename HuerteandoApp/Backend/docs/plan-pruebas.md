# Plan de pruebas del proyecto Huerteando

Este documento recoge los tipos de pruebas recomendados para el proyecto. La idea
no es hacer una bateria enorme de tests, sino cubrir las partes importantes de
forma clara y defendible para un proyecto de DAM.

---

## 1. Pruebas de backend

### 1.1. Tests de repositorio

Sirven para comprobar que las consultas a base de datos funcionan correctamente.

Casos recomendados:

| Prueba | Que comprueba |
|---|---|
| Buscar usuario por email | Que `findByEmail` encuentra el usuario correcto |
| Comprobar nick existente | Que `existsByNick` detecta usuarios repetidos |
| Comprobar email existente | Que `existsByEmail` detecta emails repetidos |
| Contar likes | Que `countByObservacion_Id` devuelve el total correcto |
| Listar observaciones por tipo | Que el filtro por tipo devuelve solo las observaciones esperadas |
| Listar observaciones por usuario | Que el filtro por usuario funciona |
| Listar observaciones por estado | Que el filtro por estado funciona |

### 1.2. Tests de servicio

Son los mas importantes para este proyecto, porque prueban la logica de negocio
sin depender directamente de la interfaz.

Casos recomendados:

| Prueba | Resultado esperado |
|---|---|
| Crear usuario con nick/email libres | Usuario creado correctamente |
| Crear usuario con nick repetido | Se rechaza la creacion |
| Crear usuario con email repetido | Se rechaza la creacion |
| Comprobar disponibilidad de usuario libre | Devuelve disponible |
| Comprobar disponibilidad de usuario repetido | Devuelve no disponible |
| Crear observacion con datos validos | Observacion creada |
| Crear observacion sin usuario | No se crea |
| Crear observacion sin tipo | No se crea |
| Dar like por primera vez | Like creado |
| Dar like duplicado | No se duplica |
| Quitar like existente | Like eliminado |
| Crear comentario | Comentario creado |
| Eliminar comentario | Comentario eliminado |

### 1.3. Tests de controller

Prueban los endpoints HTTP del backend.

Casos recomendados:

| Endpoint | Prueba | Resultado esperado |
|---|---|---|
| `GET /api/observaciones` | Sin token | `200 OK` |
| `POST /api/observaciones` | Sin token | `401 Unauthorized` |
| `GET /api/auth/disponibilidad` | Nick/email libres | `200 OK` con `disponible: true` |
| `GET /api/auth/disponibilidad` | Nick/email repetidos | `200 OK` con `disponible: false` |
| `POST /api/auth/register` | Datos correctos | `201 Created` |
| `POST /api/auth/register` | Nick repetido | `409 Conflict` |
| `POST /api/auth/login-jwt` | Sin token | `401 Unauthorized` |
| `GET /api/especies` | Sin token | `200 OK` |
| `POST /api/especies` | Sin token | `401 Unauthorized` |

### 1.4. Tests de integracion

Estos tests prueban un flujo completo entre varias partes del backend.

Flujos recomendados:

1. Registro local de usuario:
   - comprobar disponibilidad;
   - crear usuario;
   - buscar usuario por email.

2. Observacion completa:
   - crear observacion;
   - consultar observacion;
   - editar observacion;
   - eliminar observacion.

3. Interaccion social:
   - crear comentario;
   - listar comentarios;
   - dar like;
   - contar likes;
   - quitar like.

---

## 2. Pruebas de Android

### 2.1. Tests unitarios sencillos

En Android conviene probar sobre todo clases que no dependen mucho de la pantalla.

Casos recomendados:

| Clase | Prueba |
|---|---|
| `SessionManager` | Guardar sesion |
| `SessionManager` | Borrar sesion |
| `SessionManager` | Comprobar si hay sesion |
| `RegistroRequest` | Crear objeto con nick, email y password |
| `Observacion` | Getters y setters principales |

### 2.2. Tests de API desde Android

Estos tests sirven para comprobar que `ApiService` tiene bien definidas las
llamadas al backend.

Casos recomendados:

| Metodo de `ApiService` | Que comprobar |
|---|---|
| `getObservaciones()` | Recibe una lista |
| `getObservacion(id)` | Recibe una observacion concreta |
| `comprobarDisponibilidad(nick, email)` | Recibe el mapa de disponibilidad |
| `registrar(request)` | Envia bien el body de registro |
| `loginJwt()` | Usa el token guardado en el interceptor |
| `getComentarios(id)` | Recibe comentarios |
| `getMeGustasCount(id)` | Recibe contador de likes |
| `subirImagen(...)` | Envia multipart correctamente |

### 2.3. Pruebas manuales de interfaz

Para un proyecto DAM, estas pruebas son muy utiles porque demuestran que la app
funciona desde el punto de vista del usuario.

| Pantalla | Prueba | Resultado esperado |
|---|---|---|
| Inicio/lista | Entrar sin login | Se ven observaciones |
| Inicio/lista | Filtrar por tipo | Cambia la lista |
| Registro | Nick/email libres | Usuario registrado |
| Registro | Nick repetido | Muestra error y no registra en Supabase |
| Registro | Email repetido | Muestra error |
| Login | Credenciales correctas | Entra en la app |
| Login | Credenciales incorrectas | Muestra error |
| Detalle | Ver comentarios | Se cargan comentarios |
| Detalle | Crear comentario logueado | Comentario creado |
| Detalle | Dar like logueado | Sube contador |
| Detalle | Quitar like | Baja contador |
| Crear observacion | Datos validos | Observacion creada |
| Editar observacion | Cambiar descripcion | Observacion actualizada |
| Perfil | Ver datos | Muestra nick, email y avatar |
| Perfil | Cambiar avatar | Avatar actualizado |
| Sesion | Cerrar sesion | Vuelve a estado sin login |

---

## 3. Minimo recomendable para entregar

Si no da tiempo a probar todo, el minimo razonable seria:

1. Tests de servicio del backend:
   - usuario duplicado;
   - disponibilidad;
   - crear observacion;
   - dar like duplicado.

2. Tests de controller:
   - rutas publicas devuelven `200`;
   - rutas privadas sin token devuelven `401`;
   - registro devuelve `201` o `409`.

3. Tabla de pruebas manuales Android:
   - registro;
   - login;
   - crear observacion;
   - comentar;
   - dar/quitar like;
   - cerrar sesion.

Con esto el proyecto queda probado de forma suficiente sin meter una estructura
demasiado compleja.

---

## 4. Pruebas backend implementadas

Se han añadido pruebas automáticas para comprobar endpoints reales del backend
local en:

```text
src/test/java/com/huerteando/huerteandoapp/api/BackendHttpSmokeTest.java
```

Estas pruebas son de tipo **smoke test HTTP**: no arrancan otro backend, sino que
comprueban que el backend local ya levantado responde correctamente en:

```text
http://localhost:8080
```

### Tests incluidos

| Test | Endpoint probado | Resultado esperado |
|---|---|---|
| `listarObservacionesDevuelveOk` | `GET /api/observaciones/` | `200 OK` y devuelve una lista |
| `listarTiposObservacionDevuelveOk` | `GET /api/tipos-observacion` | `200 OK` y devuelve una lista |
| `listarEspeciesDevuelveOk` | `GET /api/especies` | `200 OK` y devuelve una lista |
| `disponibilidadDetectaUsuarioExistente` | `GET /api/auth/disponibilidad` | `200 OK` y devuelve los campos de disponibilidad |
| `crearObservacionSinTokenDevuelveUnauthorized` | `POST /api/observaciones` | `401 Unauthorized` |

Tambien se ha ajustado el test base:

```text
src/test/java/com/huerteando/huerteandoapp/HuerteandoAppApplicationTests.java
```

Ese test comprueba que el contexto de Spring arranca con el perfil `h2`. Se le
ha añadido una configuracion JWT de prueba para que Spring Security pueda crear
el `JwtDecoder` durante los tests.

### Resultado de la ejecucion

Comando usado:

```text
mvn test
```

Resultado:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Nota importante

Para que `BackendHttpSmokeTest` se ejecute de verdad, el backend debe estar
arrancado antes en:

```text
http://localhost:8080
```

Si no esta arrancado, esas pruebas se omiten usando `Assumptions`, para que no
rompan la compilacion por un problema de entorno.
