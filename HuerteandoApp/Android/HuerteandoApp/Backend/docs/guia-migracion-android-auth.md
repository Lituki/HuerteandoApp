# Guía de migración Android: de Android-ini a Android-fin (Supabase Auth)

Esta guía explica paso a paso cómo transformar el proyecto Android-ini en Android-fin para integrar correctamente Supabase Auth y la gestión de sesión JWT, con ejemplos de código y explicaciones sencillas.

---

## 1. Crear la clase MyApp.java

Crea el archivo `MyApp.java` en `app/src/main/java/com/huerteando/app/`:

```java
package com.huerteando.app;

import android.app.Application;

public class MyApp extends Application {
    private static SessionManager session;

    @Override
    public void onCreate() {
        super.onCreate();
        session = new SessionManager(getApplicationContext());
    }

    public static SessionManager getSession() {
        return session;
    }
}
```

**Explicación:**
- Permite acceder a la sesión desde cualquier parte de la app sin pasar Context.

---

## 2. Modificar AndroidManifest.xml

Abre `app/src/main/AndroidManifest.xml` y:

- Añade el atributo `android:name` en `<application>`:

```xml
<application
    android:name=".MyApp"
    ... >
    ...
</application>
```

- Cambia el LAUNCHER de `LoginActivity` a `ObservacionesActivity`:

```xml
<activity android:name=".ui.ObservacionesActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<!-- Quita el intent-filter de LoginActivity si lo tenía -->
```

---

## 3. Reemplazar ApiClient.java

Sustituye el contenido de `ApiClient.java` por este (en `com.huerteando.app.api`):

```java
package com.huerteando.app.api;

import com.huerteando.app.MyApp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request.Builder requestBuilder = chain.request().newBuilder();
                    String token = MyApp.getSession().getToken();
                    if (token != null) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(requestBuilder.build());
                })
                .addInterceptor(chain -> {
                    okhttp3.Response response = chain.proceed(chain.request());
                    if (response.code() == 401) {
                        MyApp.getSession().cerrarSesion();
                        retrofit = null;
                    }
                    return response;
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

**Explicación:**
- Añade el token JWT automáticamente a cada petición.
- Si el token caduca (401), cierra la sesión y fuerza login.

---

## 4. Actualizar SessionManager.java

Asegúrate de que tu `SessionManager.java` (en `com.huerteando.app`) tiene estos métodos:

```java
public class SessionManager {
    private final SharedPreferences prefs;
    private static final String TOKEN_KEY = "jwt_token";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE);
    }

    public void guardarToken(String token) {
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public boolean haySesion() {
        return getToken() != null;
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}
```

**Explicación:**
- Permite guardar, recuperar y borrar el token JWT de forma centralizada.

---

## 5. Actualizar LoginActivity.java

Reemplaza el flujo de login clásico por el nuevo flujo (Supabase → backend):

```java
// 1. Login en Supabase.
// En Android-fin se hace con OkHttp llamando directamente a supabase_url_login.
// Este bloque es pseudocodigo para entender el flujo:
// obtener JWT, guardarlo y llamar al backend.
client.auth.signInWithPassword(email, password, new Callback<AuthResponse>() {
    @Override
    public void onSuccess(AuthResponse response) {
        String jwt = response.getAccessToken();
        // Guardar el token ANTES de llamar al backend.
        // ApiClient lo leera y lo enviara como Authorization: Bearer TOKEN.
        // No escribas el JWT en Logcat: es una credencial privada.
        MyApp.getSession().guardarToken(jwt);
        // 2. Llama al backend para obtener el perfil
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.loginJwt().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Guarda datos de usuario si quieres
                    startActivity(new Intent(LoginActivity.this, ObservacionesActivity.class));
                } else {
                    Toast.makeText(context, "Error de perfil", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onError(Throwable error) {
        Toast.makeText(context, "Login fallido", Toast.LENGTH_SHORT).show();
    }
});
```

**Explicación:**
- Primero loguea en Supabase, luego llama al backend con el token recibido.
- En `Android-fin` no se usa la librería oficial de Supabase: se hace una petición HTTP directa con OkHttp a la URL guardada en `supabase_strings.xml`.
- El JWT no debe imprimirse en logs, porque permite autenticarse mientras sea válido.

---

## 6. Actualizar RegistroActivity.java

Haz el registro en este orden:

```text
1. Comprobar en backend si nick/email estan libres
2. Si estan libres, registrar en Supabase
3. Si Supabase responde OK, crear perfil local en backend
```

### 6.1. Corregir el campo de nick

En `Android-ini`, el primer campo del registro es `editNick` y se veia como
`Usuario (nick)`, asi que la idea original estaba bien. El problema aparecio
durante la integracion: se cambio `login_nick_hint` a `Email` para el login, y
como registro reutilizaba ese mismo string, la pantalla empezo a pedir dos
correos: uno en el campo de nick y otro en `Correo electronico`.

Debe quedar asi:

```xml
<!-- strings.xml -->
<string name="registro_nick_hint">Nombre de usuario</string>
```

```xml
<!-- activity_registro.xml -->
<com.google.android.material.textfield.TextInputLayout
    ...
    android:hint="@string/registro_nick_hint">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/editNick"
        ...
        android:inputType="text" />
</com.google.android.material.textfield.TextInputLayout>
```

El login puede seguir usando `Email`, pero el registro debe diferenciar:

```text
Nombre de usuario -> nick en tabla usuarios
Correo electronico -> email en Supabase y tabla usuarios
```

### 6.2. Añadir endpoint al ApiService

```java
@GET("api/auth/disponibilidad")
Call<Map<String, Boolean>> comprobarDisponibilidad(
        @Query("nick") String nick,
        @Query("email") String email
);
```

### 6.3. Comprobar disponibilidad antes de Supabase

Ejemplo resumido:

```java
ApiService api = ApiClient.getClient().create(ApiService.class);
api.comprobarDisponibilidad(nick, email).enqueue(new Callback<Map<String, Boolean>>() {
    @Override
    public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
        if (!response.isSuccessful() || response.body() == null) {
            mostrarError("No se pudo comprobar el usuario");
            return;
        }

        boolean nickDisponible = Boolean.TRUE.equals(response.body().get("nickDisponible"));
        boolean emailDisponible = Boolean.TRUE.equals(response.body().get("emailDisponible"));

        if (!nickDisponible) {
            mostrarError("El nombre de usuario ya existe");
            return;
        }

        if (!emailDisponible) {
            mostrarError("El correo ya existe en Huerteando");
            return;
        }

        // Si todo esta libre, aqui se llama a Supabase.
        registrarEnSupabase(nick, password, nombre, apellidos, email);
    }

    @Override
    public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
        mostrarError("No se ha podido conectar con el backend");
    }
});
```

**Explicación:**
- Supabase gestiona la contraseña real.
- El backend crea el perfil local del usuario.
- `"SUPABASE_MANAGED"` es un marcador para que `password_hash` no quede vacío.
- El orden real de `RegistroRequest` en `Android-fin` es:

```java
new RegistroRequest(nick, "SUPABASE_MANAGED", nombre, apellidos, email, avatarUrl)
```

---

## 7. Crear supabase_strings.xml


### Dónde y cómo crear el archivo

Crea el archivo llamado `supabase_strings.xml` en la ruta:
`app/src/main/res/values/supabase_strings.xml`

### Ejemplo completo de contenido

```xml
<resources>
    <!-- URL para registro de usuario en Supabase -->
    <string name="supabase_url_signup">https://TU-PROYECTO.supabase.co/auth/v1/signup</string>
    <!-- URL para login de usuario en Supabase -->
    <string name="supabase_url_login">https://TU-PROYECTO.supabase.co/auth/v1/token?grant_type=password</string>
    <!-- Clave pública (anon key) de Supabase, se obtiene en Settings > API > anon public key -->
    <string name="supabase_api_key">TU-ANON-KEY</string>
</resources>
```

### Explicación de cada string
- Cambia `TU-PROYECTO` por el identificador de tu proyecto en Supabase (ejemplo: `moqignnvjhcxnhtzcsgx`).
- Copia la anon key desde el panel de Supabase: **Settings → API → anon public key**.
- Estos valores se usan en las peticiones HTTP para registrar y loguear usuarios en Supabase desde la app Android.

---

## 8. Actualizar ObservacionesActivity.java

Antes de abrir la pantalla de crear observación (FAB), comprueba si hay sesión:

```java
fab.setOnClickListener(v -> {
    if (!MyApp.getSession().haySesion()) {
        startActivity(new Intent(this, LoginActivity.class));
    } else {
        startActivity(new Intent(this, CrearObservacionActivity.class));
    }
});
```

---


**Siguiendo estos pasos y ejemplos, se puede migrar de Android-ini a Android-fin de forma sencilla y segura.**

Si necesita ayuda con algún archivo concreto, que consulte esta guía o pida ejemplos adicionales.
