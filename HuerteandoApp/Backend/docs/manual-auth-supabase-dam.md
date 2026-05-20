# Manual DAM simplificado: Supabase Auth + Spring Boot + Android

## 1. Objetivo (version sencilla y defendible)
Esta version esta pensada para implementar autenticacion de forma realista en DAM, sin complejidad innecesaria.

Meta principal:

1. Publico (sin login): puede ver contenido.
2. Registrado (con login): puede crear observaciones, comentar y dar likes.
3. Admin: se deja como mejora opcional para una segunda fase.

Idea clave:

1. Supabase autentica al usuario y devuelve un JWT.
2. Android guarda ese token y lo envia al backend.
3. Spring Boot valida el JWT y protege rutas privadas.

---

## 2. Estado actual del proyecto
Backend:

1. Ya existe login y registro basicos locales en `UsuarioController`.
2. Ya existe configuracion de Supabase Storage en `application.properties` (`supabase.url`, `supabase.key`, `supabase.bucket`).
3. Falta configurar Spring Security para validar JWT de Supabase en toda la API.

Android:

1. Existe cliente Retrofit (`ApiClient`) y contrato API (`ApiService`).
2. Falta enviar `Authorization: Bearer <token>` de forma automatica.

---

## 3. Flujo funcional minimo (recomendado)
1. El usuario abre la app y puede navegar contenido publico.
2. Si intenta comentar, dar like o crear observacion, se pide login.
3. Android hace login en Supabase.
4. Supabase devuelve `access_token`.
5. Android guarda el token.
6. Android llama a la API con:
   1. `Authorization: Bearer <access_token>`
7. Spring valida token:
   1. valido -> permite accion privada
   2. invalido o ausente -> `401 Unauthorized`

Nota importante:

1. La `anon key` puede estar en Android.
2. La `service_role key` nunca debe ir en Android.

---

## 4. Permisos simplificados (fase 1)
### 4.1 Matriz DAM
| Accion | Publico | Registrado | Admin |
|---|---|---|---|
| Ver observaciones, especies, tipos, comentarios | Si | Si | Si |
| Crear observacion | No | Si | Si |
| Dar/quitar like | No | Si | Si |
| Crear/borrar comentario | No | Si | Si |
| Subir imagen | No | Si | Si |
| Gestion total del sistema | No | No | Si (fase 2) |

### 4.2 Reglas practicas
1. Publico:
   1. Solo `GET` en recursos de consulta.
2. Registrado:
   1. Puede ejecutar `POST/PUT/DELETE` de interaccion normal.
3. Admin:
   1. Se deja como ampliacion posterior para no bloquear la primera entrega.

### 4.3 Endpoints minimos a proteger
Publicos:

1. `GET /api/observaciones`
2. `GET /api/observaciones/{id}`
3. `GET /api/tipos-observacion`
4. `GET /api/especies`

Privados (requieren login):

1. `POST /api/observaciones`
2. `POST /api/observaciones/{id}/comentarios`
3. `POST /api/observaciones/{id}/megustas`
4. `POST /api/observaciones/{id}/imagenes`

---

## 5. Configuracion en Supabase
## 5.1 Crear proyecto
1. Crea proyecto en Supabase.
2. Guarda:
   1. Project URL.
   2. Anon key.
   3. JWT issuer/JWKS (segun panel de API/Auth).

## 5.2 Activar Email/Password
1. Ve a Authentication.
2. Activa proveedor Email.
3. Configura confirmacion de correo segun estrategia del proyecto.

## 5.3 Rol de aplicacion en metadata
Supabase tiene rol tecnico (`authenticated`), pero para negocio necesitas un rol de app:

1. Guarda en `user_metadata` un campo `app_role`.
2. Valores permitidos:
   1. `REGISTERED`
   2. `ADMIN`
3. Si no existe `app_role`, tratar al usuario como `REGISTERED` por defecto (o denegar, segun politica).

Ejemplo de metadata al registrar:

```json
{
  "data": {
    "app_role": "REGISTERED",
    "nick": "usuario1"
  }
}
```

---

## 6. Backend Spring Boot: implementacion paso a paso
## 6.1 Dependencias Maven
En `pom.xml`, agrega:

1. `spring-boot-starter-security`
2. `spring-boot-starter-oauth2-resource-server`
3. `spring-security-oauth2-jose`

Con esto Spring valida JWT sin parser manual fragil.

## 6.2 Propiedades de seguridad
En `application.properties` (o profile postgres), agrega bloque auth:

```properties
# Supabase Auth
supabase.auth.issuer=https://<tu-proyecto>.supabase.co/auth/v1
spring.security.oauth2.resourceserver.jwt.issuer-uri=${supabase.auth.issuer}
```

Opcional por JWKS explicito:

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://<tu-proyecto>.supabase.co/auth/v1/.well-known/jwks.json
```

Mantener separado de Storage para no mezclar responsabilidades.

## 6.3 Crear SecurityConfig
Crea una clase `SecurityConfig` que:

1. Desactive CSRF si API REST stateless.
2. Configure CORS.
3. Defina politica stateless.
4. Defina autorizacion por rutas y roles.
5. Active `oauth2ResourceServer().jwt()`.

Politica recomendada de rutas para tu requisito:

1. Publicas (sin login):
   1. `GET /api/observaciones/**`
   2. `GET /api/especies/**`
   3. `GET /api/tipos-observacion/**`
   4. `GET /api/observaciones/*/comentarios`
2. Registrado o Admin:
   1. `POST /api/observaciones/**`
   2. `PUT /api/observaciones/**`
   3. `DELETE /api/observaciones/**`
   4. `POST /api/observaciones/*/comentarios`
   5. `DELETE /api/observaciones/*/comentarios/**`
   6. `POST /api/observaciones/*/megustas`
   7. `DELETE /api/observaciones/*/megustas`
   8. `POST /api/observaciones/*/imagenes`
   9. `DELETE /api/observaciones/*/imagenes/**`
3. Solo Admin:
   1. Endpoints administrativos que decidas (por ejemplo borrado global de usuarios o catalogos sensibles).

## 6.4 Mapper de roles desde JWT
Necesitas convertir claim JWT a autoridades Spring.

1. Leer `user_metadata.app_role` o claim equivalente.
2. Mapear a:
   1. `ROLE_REGISTERED`
   2. `ROLE_ADMIN`
3. Si no hay rol:
   1. Default `ROLE_REGISTERED` para usuario autenticado, o
   2. Denegar segun politica estricta.

## 6.5 Endpoint de diagnostico
Agregar endpoint `GET /api/auth/me` para verificar:

1. `sub`
2. `email`
3. `app_role`
4. authorities resultantes

Sirve para depurar permisos desde Android rapidamente.

## 6.6 Integracion con controlador actual de usuarios
`UsuarioController` hoy tiene login local (`/api/auth/login`) y registro local (`/api/auth/register`).

Estrategia DAM recomendada en 2 fases:

1. Fase 1 (transicion):
   1. Mantener endpoints actuales.
   2. Introducir JWT para endpoints protegidos.
2. Fase 2 (objetivo final):
   1. Android usa Supabase Auth directamente.
   2. Backend elimina login local o lo deja solo para admin interno.

---

## 7. Android: implementacion paso a paso
## 7.1 Dependencias
Puedes mantener Retrofit y agregar cliente para Supabase Auth via Retrofit/OkHttp.

## 7.2 Crear servicio de auth Supabase separado
No mezclar con API de negocio.

1. `SupabaseAuthService`:
   1. signup
   2. login password
   3. refresh token
2. Base URL Supabase:
   1. `https://<tu-proyecto>.supabase.co/`

Headers requeridos en auth:

1. `apikey: <SUPABASE_ANON_KEY>`
2. `Content-Type: application/json`

## 7.3 Guardar tokens
Crear `SessionManager` en Android con:

1. accessToken
2. refreshToken
3. expiresAt
4. userId
5. appRole

## 7.4 Interceptor Authorization para API backend
En `ApiClient` de backend:

1. Leer `accessToken` de `SessionManager`.
2. Si existe, agregar header:
   1. `Authorization: Bearer <token>`

Esto aplica a todas las llamadas de `ApiService`.

## 7.5 Flujo de login
1. Pantalla login envia email/password a Supabase.
2. Guarda tokens al recibir respuesta.
3. Llama `GET /api/auth/me` en backend.
4. Si 200:
   1. entrar a Home
5. Si 401:
   1. limpiar sesion y mostrar error.

## 7.6 Control de UI por rol
1. Publico:
   1. ocultar botones de crear, comentar, like.
2. Registrado:
   1. mostrar acciones de interaccion.
3. Admin:
   1. mostrar menu admin.

Importante: ocultar en UI no sustituye seguridad backend.

---

## 8. Mapeo de permisos sobre tus endpoints actuales
Ajustado a `ApiService` actual:

1. Publico (permitAll):
   1. GET tipos
   2. GET observaciones y filtros
   3. GET observacion por id
   4. GET comentarios
   5. GET especies
   6. GET likes count/existe (opcional publico; recomendado publico para mostrar tarjeta)
2. Registrado/Admin:
   1. POST/PUT/DELETE observaciones
   2. POST/DELETE comentarios
   3. POST/DELETE megustas
   4. POST avatar
   5. POST/DELETE imagenes
3. Admin:
   1. Operaciones globales de moderacion que tu definas

---

## 9. Plan de migracion recomendado (sin romper app)
## 9.1 Sprint 1
1. SecurityConfig + JWT valido.
2. Endpoint `/api/auth/me`.
3. Android guarda token y lo envia en Authorization.
4. Dejar login local existente operativo temporalmente.

## 9.2 Sprint 2
1. En Android cambiar login principal a Supabase.
2. Marcar login local como legacy.
3. Aplicar reglas por rol en backend.

## 9.3 Sprint 3
1. Retirar login local del backend.
2. Endurecer permisos admin.
3. Añadir tests de autorizacion.

---

## 10. Casos de prueba obligatorios
## 10.1 Publico
1. GET observaciones -> 200.
2. POST observacion -> 401/403.
3. POST comentario -> 401/403.
4. POST like -> 401/403.

## 10.2 Registrado
1. POST observacion -> 201.
2. POST comentario -> 201.
3. POST like -> 201 o 409 si duplicado.
4. DELETE like -> 204.

## 10.3 Admin
1. Ejecuta operaciones de registrado -> OK.
2. Ejecuta endpoints admin -> OK.

## 10.4 Token
1. Token invalido -> 401.
2. Token expirado -> 401 y flujo refresh en Android.

---

## 11. Errores frecuentes y solucion
1. Siempre 401:
   1. Revisar header Authorization en interceptor.
2. Error de issuer:
   1. Revisar `issuer-uri` exactamente igual al de Supabase.
3. Rol no aplicado:
   1. Revisar claim real del JWT en `/api/auth/me`.
4. CORS en Android emulador:
   1. Ajustar CORS del backend para origenes necesarios.
5. Fuga de credenciales:
   1. Nunca exponer service role key fuera del backend.

---

## 12. Checklist final de entrega DAM
1. Supabase Auth funcionando (signup/login).
2. Backend valida JWT de Supabase.
3. Permisos por rol aplicados (Publico/Registrado/Admin).
4. Android envia Bearer token automatico.
5. UI se adapta por rol.
6. Casos de prueba documentados y ejecutados.
7. Login local legacy retirado o claramente marcado.

---

## 13. Nota de seguridad academica
Aunque sea proyecto DAM:

1. Evita guardar passwords locales en texto plano.
2. Evita logs con tokens completos.
3. Nunca publiques keys sensibles en repositorio.

Con esto tendras una base solida, evaluable y alineada con arquitectura moderna usando Supabase Auth + API protegida por JWT.
