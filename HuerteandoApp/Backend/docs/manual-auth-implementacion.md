# Manual de implementación de la Api: Supabase Auth

---

## Qué hace esta implementación

- Cualquiera puede ver observaciones, especies y comentarios sin login.
- Solo usuarios registrados pueden crear observaciones, comentar, dar like y subir imágenes.
- Spring Boot valida que el usuario esté autenticado mediante un token JWT de Supabase.
- Android hace login en Supabase, guarda el token y lo envía automáticamente en cada petición.

```
Android → login en Supabase → recibe token JWT
Android → llama a la API con: Authorization: Bearer TOKEN
Spring Boot → valida el token → permite o deniega
```

---

## Flujo recomendado de registro

Para evitar usuarios creados en Supabase pero rechazados despues por PostgreSQL,
Android debe comprobar primero si el nick y el email estan libres en el backend:

```
Android → GET /api/auth/disponibilidad?nick=...&email=...
Backend → responde si nick/email estan disponibles
Android → si estan libres, registra en Supabase
Android → si Supabase OK, crea perfil en /api/auth/register
```

Esto evita este caso problematico:

```
Supabase crea el usuario
Backend rechaza el perfil con 409 porque nick/email ya existian
```

Si Supabase devuelve que el email ya existe, Android no debe llamar al backend:
ese correo ya esta registrado en Supabase Auth.

---

## Lo que YA está hecho en el backend (no toques esto)

- `SecurityConfig.java` — define rutas públicas y privadas, valida JWT
- `application-postgres.properties` — tiene la URL del JWKS de Supabase y el algoritmo ES256
- `pom.xml` — tiene las dependencias de Spring Security y OAuth2 Resource Server
- `UsuarioController.java` — tiene todos los endpoints de auth
- `UsuarioServiceImpl.java` — tiene `buscarPorEmail()`
- `UsuarioRepository.java` — tiene `findByEmail()`

---

## PARTE 1: SUPABASE

### Paso 1.1 — Crear el proyecto en Supabase

1. Ve a https://supabase.com y crea una cuenta.
2. Crea un nuevo proyecto. Nombre: `huerteando`, región: Europa.
3. Espera a que arranque (~1 minuto).

### Paso 1.2 — Activar login con email

1. Panel de Supabase → **Authentication → Providers**.
2. Activa **Email**.
3. Desactiva "Confirm email" (más cómodo para desarrollo).

### Paso 1.3 — Copiar las claves

Ve a **Settings → API** y copia:

| Clave | Dónde va |
|-------|----------|
| Project URL | `application-postgres.properties` (jwk-set-uri) |
| anon public key | `supabase_strings.xml` en Android |

La **service_role key** nunca va en Android ni en el código fuente.

### Paso 1.4 — Actualizar la URL del JWKS en el backend

Abre `src/main/resources/application-postgres.properties` y sustituye la URL:

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://TU-PROYECTO.supabase.co/auth/v1/.well-known/jwks.json
spring.security.oauth2.resourceserver.jwt.jws-algorithms=ES256
```

La segunda línea es obligatoria: Supabase firma con ES256, no con RS256.
Sin ella Spring rechaza todos los tokens aunque sean válidos.

---

## PARTE 2: BACKEND — qué hace cada endpoint

El backend ya tiene todo implementado. Aquí se explica qué hace cada endpoint.

### POST /api/auth/register

Android llama a este endpoint después de registrar al usuario en Supabase.

- Recibe: `nick`, `nombre`, `apellidos`, `email`, `avatarUrl`, `password`
- La contraseña que llega siempre es `"SUPABASE_MANAGED"`. La real la gestiona Supabase.
- Devuelve: el objeto `Usuario` creado (201 Created).

**¿Por qué "SUPABASE_MANAGED"?**
La columna `password_hash` es `NOT NULL` en la base de datos. En vez de cambiar el esquema,
usamos este placeholder. El endpoint `/api/auth/login` bloquea el acceso si detecta este valor.

### POST /api/auth/login

Login clásico con nick y contraseña. Se mantiene por compatibilidad con cuentas antiguas.
Las cuentas creadas con Supabase no pueden entrar por aquí (devuelve 403).

### POST /api/auth/login-jwt

Android llama a este endpoint después de hacer login en Supabase.

- No recibe body. El token JWT viene en el header `Authorization: Bearer TOKEN`.
- Spring Security valida el token automáticamente antes de que llegue al método.
- El método extrae el email del token y busca el usuario en PostgreSQL.
- Devuelve: `{ id, nick, nombre, apellidos, email, fechaRegistro, rol, avatarUrl }`
- Si el usuario no existe en PostgreSQL: 404 (debe registrarse desde la app primero).

### GET /api/auth/disponibilidad

Android llama a este endpoint antes de registrar en Supabase.

Ejemplo:

```
GET /api/auth/disponibilidad?nick=sergio&email=sergio@test.com
```

Devuelve:

```json
{
  "nickDisponible": true,
  "emailDisponible": true,
  "disponible": true
}
```

Si `disponible` es `false`, Android debe mostrar un mensaje y no llamar a Supabase.

### GET /api/usuarios/{id}

Devuelve el perfil de un usuario por su id. Lo usa la pantalla de perfil de Android.

### POST /api/usuarios/{id}/avatar

Sube un avatar a Supabase Storage y guarda la URL pública en el usuario.

---

## PARTE 3: VERIFICAR EL BACKEND CON POSTMAN

### Paso 3.1 — Arrancar el backend y probar rutas públicas

```
GET http://localhost:8080/api/observaciones  →  200 OK (sin token)
POST http://localhost:8080/api/observaciones →  401 Unauthorized (sin token)
```

### Paso 3.2 — Obtener un token de prueba

```
POST https://TU-PROYECTO.supabase.co/auth/v1/token?grant_type=password
Headers:
  apikey: TU-ANON-KEY
  Content-Type: application/json
Body:
  { "email": "test@test.com", "password": "123456" }
```

Copia el `access_token` de la respuesta.

### Paso 3.3 — Probar el endpoint login-jwt

```
POST http://localhost:8080/api/auth/login-jwt
Headers:
  Authorization: Bearer TU-ACCESS-TOKEN
```

Debe devolver 200 con los datos del perfil. Si devuelve 404, el usuario no tiene
perfil en PostgreSQL — regístralo primero desde la app.

---
