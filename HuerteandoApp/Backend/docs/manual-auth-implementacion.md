# Guía de implementación: Supabase Auth + Spring Boot + Android
## Proyecto Huerteando — versión sencilla y defendible

---

## Qué vamos a conseguir

- Cualquiera puede ver observaciones, especies y comentarios sin login.
- Solo usuarios registrados pueden crear, comentar, dar like y subir imágenes.
- Spring Boot valida que el usuario esté autenticado mediante un token JWT.
- Android hace login en Supabase, guarda el token y lo envía automáticamente.

No hay roles en esta versión. Público vs registrado es suficiente para la entrega.

---

## Cómo funciona (idea general)

```
Android → login en Supabase → recibe token JWT
Android → llama a la API con: Authorization: Bearer TOKEN
Spring Boot → valida el token → permite o deniega
```

Si el token es válido: la acción se ejecuta.
Si no hay token o es inválido: Spring devuelve 401.

---

## PARTE 1: SUPABASE

### Paso 1.1 — Crear el proyecto

1. Ve a https://supabase.com y crea una cuenta si no tienes.
2. Crea un nuevo proyecto. Ponle nombre, elige región Europa.
3. Espera a que arranque (tarda un minuto).

### Paso 1.2 — Activar login con email y contraseña

1. En el panel de Supabase, ve a **Authentication → Providers**.
2. Activa **Email**.
3. Si no quieres que los usuarios tengan que confirmar el correo (más cómodo para desarrollo), desactiva "Confirm email".

### Paso 1.3 — Guardar las claves que necesitas

Ve a **Settings → API** y copia:

| Clave | Para qué sirve | Dónde va |
|-------|---------------|----------|
| Project URL | URL base de Supabase | Android y backend |
| anon public key | Clave pública para el cliente | Solo Android |
| JWT Secret | No lo necesitas (usamos JWKS) | — |

La **service_role key** nunca va en Android. Solo en backend si la necesitas para operaciones admin.

### Paso 1.4 — Obtener la URL del JWKS

La URL del JWKS es la que Spring Boot usará para validar tokens.
Tiene este formato fijo:

```
https://TU-PROYECTO.supabase.co/auth/v1/.well-known/jwks.json
```

Guárdala, la necesitarás en el backend.

---

## PARTE 2: BACKEND (Spring Boot)

El backend mantiene temporalmente el login local durante la migración, pero la
autenticación principal pasará a realizarse con Supabase Auth.

### Paso 2.1 — Añadir dependencias en pom.xml

Abre `pom.xml` y añade estas dos dependencias dentro de `<dependencies>`:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validación de tokens JWT (OAuth2 Resource Server) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Por qué estas dos: la primera activa Spring Security, la segunda le dice a Spring
cómo validar tokens JWT sin que tengas que escribir ese código tú.

### Paso 2.2 — Configurar la URL del JWKS

Abre `src/main/resources/application-postgres.properties` y añade al final:

```properties
# Supabase Auth — URL donde Spring descarga las claves públicas para validar JWT
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://TU-PROYECTO.supabase.co/auth/v1/.well-known/jwks.json
```

Sustituye `TU-PROYECTO` por el id real de tu proyecto Supabase.

Por qué jwk-set-uri y no issuer-uri: con Supabase, la opción issuer-uri a veces
falla porque Spring intenta autodescubrir la configuración y Supabase no siempre
la expone. jwk-set-uri es directa y fiable.

### Paso 2.3 — Crear SecurityConfig.java

Crea el fichero en:
`src/main/java/com/huerteando/huerteandoapp/config/SecurityConfig.java`

```java
package com.huerteando.huerteandoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // La API es REST, no usa formularios ni sesiones → desactivamos CSRF
            .csrf(csrf -> csrf.disable())

            // Sin sesiones: cada petición se valida por su token, nada más
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CORS: permite peticiones desde Android y desde el futuro panel web
            .cors(Customizer.withDefaults())

            .authorizeHttpRequests(auth -> auth

                // RUTAS PÚBLICAS — cualquiera puede hacer GET de consulta
                .requestMatchers(HttpMethod.GET,
                    "/api/observaciones/**",
                    "/api/especies/**",
                    "/api/tipos-observacion/**"
                ).permitAll()

                // Login y registro local se mantienen públicos (fase de transición)
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register"
                ).permitAll()

                // RUTAS PRIVADAS — cualquier otra petición requiere token válido
                .anyRequest().authenticated()
            )

            // Activar validación JWT con la configuración de application.properties
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
```

### Paso 2.4 — Añadir endpoint de diagnóstico /api/auth/me

Este endpoint sirve para verificar desde Android que el token llega bien al backend.
Devuelve los datos que Spring extrajo del JWT.

Abre `UsuarioController.java` y añade este método:

```java
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Map;

// GET /api/auth/me
// Requiere token válido. Devuelve los datos del JWT para depuración.
@GetMapping("/api/auth/me")
public ResponseEntity<Map<String, Object>> me(JwtAuthenticationToken token) {
    return ResponseEntity.ok(Map.of(
        "sub",    token.getToken().getSubject(),
        "email",  token.getToken().getClaimAsString("email"),
        "claims", token.getToken().getClaims()
    ));
}
```

### Paso 2.5 — Verificar que el backend arranca

Arranca el backend y comprueba en Postman:

| Petición | Resultado esperado |
|----------|--------------------|
| GET /api/observaciones | 200 (sin token) |
| POST /api/observaciones | 401 (sin token) |
| GET /api/auth/me sin token | 401 |
| GET /api/auth/me con token válido de Supabase | 200 con datos del JWT |

Para obtener un token de prueba: regístra un usuario en Supabase desde el panel
(Authentication → Users → Add user) y haz login desde Postman directamente
contra Supabase:

```
POST https://TU-PROYECTO.supabase.co/auth/v1/token?grant_type=password
Headers:
  apikey: TU-ANON-KEY
  Content-Type: application/json
Body:
  { "email": "test@test.com", "password": "123456" }
```

Copia el `access_token` de la respuesta y úsalo en Postman como Bearer token.

---

## PARTE 3: ANDROID

El proyecto ya tiene `ApiClient`, `ApiService` y `SessionManager`.
Solo hay que añadir clases nuevas y modificar las existentes mínimamente.

### Paso 3.1 — Crear las clases de Supabase Auth

Crea el paquete `api/supabase/` dentro de `com.huerteando.app`.

#### SupabaseSession.java

Modelo que representa la respuesta de Supabase al hacer login.

```java
package com.huerteando.app.api.supabase;

import com.google.gson.annotations.SerializedName;

public class SupabaseSession {

    // Gson mapea "access_token" del JSON a este campo
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private int expiresIn;

    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public int    getExpiresIn()    { return expiresIn; }
}
```

#### SupabaseAuthService.java

Interfaz Retrofit para los endpoints de auth de Supabase.

```java
package com.huerteando.app.api.supabase;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SupabaseAuthService {

    // Login con email y contraseña
    @POST("auth/v1/token?grant_type=password")
    Call<SupabaseSession> login(
        @Header("apikey") String apikey,
        @Body Map<String, String> body
    );

    // Registro de usuario nuevo
    @POST("auth/v1/signup")
    Call<SupabaseSession> signup(
        @Header("apikey") String apikey,
        @Body Map<String, String> body
    );
}
```

#### SupabaseAuthClient.java

Cliente Retrofit separado para Supabase. No mezclar con el cliente de la API.

```java
package com.huerteando.app.api.supabase;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseAuthClient {

    // URL base de tu proyecto Supabase
    private static final String BASE_URL = "https://TU-PROYECTO.supabase.co/";

    // Para pruebas DAM puede ir aquí temporalmente,
    // pero lo recomendable es moverlo a local.properties / BuildConfig
    public static final String ANON_KEY = "TU-ANON-KEY";

    private static SupabaseAuthService service;

    public static SupabaseAuthService get() {
        if (service == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

            service = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SupabaseAuthService.class);
        }
        return service;
    }
}
```

Para moverla a local.properties (recomendado si el repo es público):
1. Añade en `local.properties`: `SUPABASE_ANON_KEY=eyJ...`
2. Añade en `build.gradle.kts` del módulo app:
   ```kotlin
   buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY")}\"")
   ```
3. Usa `BuildConfig.SUPABASE_ANON_KEY` en lugar de la constante.

### Paso 3.2 — Actualizar SessionManager.java

El SessionManager actual guarda datos del usuario (id, nick, rol...).
Solo hay que añadirle los métodos para guardar y leer el token JWT.

Añade estas constantes y métodos al SessionManager existente:

```java
// Añadir junto a las otras constantes KEY_*
private static final String KEY_ACCESS_TOKEN  = "access_token";
private static final String KEY_REFRESH_TOKEN = "refresh_token";

// Añadir junto a los otros métodos

/** Guarda los tokens recibidos de Supabase tras el login. */
public void guardarTokens(String accessToken, String refreshToken) {
    editor.putString(KEY_ACCESS_TOKEN, accessToken);
    editor.putString(KEY_REFRESH_TOKEN, refreshToken);
    editor.apply();
}

public String getAccessToken()  { return prefs.getString(KEY_ACCESS_TOKEN, null); }
public String getRefreshToken() { return prefs.getString(KEY_REFRESH_TOKEN, null); }
```

El método `cerrarSesion()` ya hace `editor.clear()`, así que borrará los tokens también.

### Paso 3.3 — Actualizar ApiClient.java

El ApiClient actual no envía ningún token. Hay que añadir un interceptor que
lea el token del SessionManager y lo añada automáticamente a cada petición.

El método `getClient()` necesita recibir un `Context` para poder acceder al
SessionManager. Reemplaza la clase completa:

```java
package com.huerteando.app.api;

import android.content.Context;
import com.huerteando.app.utils.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String BASE_URL = "https://huerteandoapp-1.onrender.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // getApplicationContext() evita memory leak si se pasa una Activity
            SessionManager session = new SessionManager(context.getApplicationContext());

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                // Interceptor de autenticación: añade el token si existe
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    String token = session.getAccessToken();
                    if (token != null) {
                        builder.addHeader("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();
        }
        return retrofit;
    }
}
```

Importante: ahora `getClient()` recibe `Context`. Tendrás que actualizar todas
las llamadas existentes de `ApiClient.getClient()` a `ApiClient.getClient(this)`
o `ApiClient.getClient(requireContext())` según estés en Activity o Fragment.

### Paso 3.4 — Añadir endpoint /api/auth/me en ApiService.java

Añade este método a la interfaz `ApiService`:

```java
// GET /api/auth/me — verifica que el token es válido y devuelve datos del usuario
@GET("api/auth/me")
Call<Map<String, Object>> getMe();
```

### Paso 3.5 — Actualizar LoginActivity.java

El login actual va contra el backend local. Hay que cambiarlo para que vaya
contra Supabase. El flujo nuevo es:

1. Android llama a Supabase con email y contraseña.
2. Supabase devuelve el token JWT.
3. Android guarda el token en SessionManager.
4. Android llama a /api/auth/me para verificar que el backend acepta el token.
5. Si todo va bien, ir a ObservacionesActivity.

Reemplaza el método `realizarLogin()` en `LoginActivity`:

```java
private void realizarLogin() {
    String email    = texto(editNick);     // el campo se llama editNick pero usamos email
    String password = texto(editPassword);

    if (email.isEmpty()) {
        mostrarError("Introduce tu email");
        return;
    }
    if (password.isEmpty()) {
        mostrarError("Introduce tu contraseña");
        return;
    }

    tvError.setVisibility(View.GONE);
    btnLogin.setEnabled(false);
    btnLogin.setText("Accediendo...");

    // Paso 1: login en Supabase
    Map<String, String> body = new HashMap<>();
    body.put("email", email);
    body.put("password", password);

    SupabaseAuthClient.get().login(SupabaseAuthClient.ANON_KEY, body)
        .enqueue(new Callback<SupabaseSession>() {

            @Override
            public void onResponse(Call<SupabaseSession> call, Response<SupabaseSession> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Paso 2: guardar el token
                    SupabaseSession sesion = response.body();
                    sessionManager.guardarTokens(
                        sesion.getAccessToken(),
                        sesion.getRefreshToken()
                    );

                    // Paso 3: verificar con el backend
                    ApiService api = ApiClient.getClient(LoginActivity.this).create(ApiService.class);
                    api.getMe().enqueue(new Callback<Map<String, Object>>() {

                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Entrar");

                            if (response.isSuccessful()) {
                                irAObservaciones();
                            } else {
                                sessionManager.cerrarSesion();
                                mostrarError("Error al verificar sesión");
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Entrar");
                            sessionManager.cerrarSesion();
                            mostrarError("No se pudo conectar con el servidor");
                        }
                    });

                } else {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                    mostrarError("Email o contraseña incorrectos");
                }
            }

            @Override
            public void onFailure(Call<SupabaseSession> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                mostrarError("No se pudo conectar con Supabase");
            }
        });
}
```

Añade los imports necesarios en LoginActivity:
```java
import com.huerteando.app.api.supabase.SupabaseAuthClient;
import com.huerteando.app.api.supabase.SupabaseSession;
```

### Paso 3.6 — Actualizar las Activities existentes

Todas las Activities que usan `ApiClient.getClient()` deben pasar el contexto.

Busca en el proyecto todas las ocurrencias de `ApiClient.getClient()` y
cámbialas por `ApiClient.getClient(this)` si estás en una Activity, o
`ApiClient.getClient(requireContext())` si estás en un Fragment.

---

## Orden de implementación recomendado

Sigue este orden para poder probar cada paso antes de continuar:

1. Supabase: crear proyecto y activar email auth.
2. Backend: añadir dependencias y compilar (sin SecurityConfig todavía).
3. Backend: añadir SecurityConfig y verificar que arranca.
4. Backend: probar con Postman que GET público funciona y POST devuelve 401.
5. Backend: añadir /api/auth/me y probar con token real de Supabase.
6. Android: crear clases SupabaseSession, SupabaseAuthService, SupabaseAuthClient.
7. Android: actualizar SessionManager con los métodos de token.
8. Android: actualizar ApiClient con el interceptor.
9. Android: actualizar LoginActivity.
10. Android: probar login completo end-to-end.

---

## Errores frecuentes

**Backend siempre devuelve 401 aunque el token sea válido**
- Revisa que la URL del jwk-set-uri es exactamente la de tu proyecto Supabase.
- Comprueba que el token no ha caducado (duran 1 hora por defecto).

**Android: onFailure siempre se ejecuta al llamar a Supabase**
- Comprueba que tienes INTERNET en el AndroidManifest.
- Verifica que la BASE_URL de SupabaseAuthClient termina en `/`.

**Los campos del JWT llegan como null en /api/auth/me**
- El email puede no estar en el nivel raíz del token. Usa `claims` para ver
  todos los campos disponibles y localizar dónde está cada dato.

**ApiClient.getClient() da error de compilación**
- Ahora requiere Context. Cambia todas las llamadas a `getClient(this)`.

---

## Qué NO hace esta versión (ampliaciones futuras)

- No hay roles (ADMIN se añadirá con el panel web).
- No hay refresh token automático: si caduca el token, el usuario vuelve a hacer login.
- El login local del backend (`/api/auth/login`) se mantiene por compatibilidad
  pero en la versión final se retirará.
