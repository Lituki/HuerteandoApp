# Manual de conexión Android ↔ API REST
## Proyecto Huerteando

Este manual explica cómo conectar la app Android con la API desde cero.
Está escrito para el nivel de DAM, sin complicaciones innecesarias.

---

## 1. Qué es un endpoint y cómo funciona

Un endpoint es simplemente una URL a la que la app hace una petición y el servidor responde.

Hay 4 tipos de petición (los que usamos en este proyecto):

| Tipo   | Para qué sirve          | Ejemplo                          |
|--------|-------------------------|----------------------------------|
| GET    | Pedir datos             | Dame todas las observaciones     |
| POST   | Enviar datos nuevos     | Crear una observación nueva      |
| PUT    | Modificar algo          | Editar una observación           |
| DELETE | Borrar algo             | Eliminar una observación         |

---

## 2. Librería que vamos a usar: Retrofit

Retrofit es la librería más usada en Android para llamar a APIs REST.
Convierte las respuestas JSON del servidor en objetos Java automáticamente.

### Añadir al build.gradle (módulo app)

```gradle
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
}
```

También necesitas permiso de internet en el `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 3. Configuración base de Retrofit

Crea una clase `RetrofitClient.java` en tu proyecto Android.
Esta clase se encarga de crear la conexión con el servidor.

```java
public class RetrofitClient {

    // Cambia esta URL por la IP de tu servidor cuando lo despliegues
    // En desarrollo local con emulador Android usa 10.0.2.2 en vez de localhost
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
```

> **Ojo con la URL en el emulador:**
> - `localhost` o `127.0.0.1` en Android apunta al propio móvil, no a tu PC.
> - Para apuntar a tu PC desde el emulador usa `10.0.2.2`.
> - Si usas un móvil físico, usa la IP local de tu PC (ej: `192.168.1.X`).

---

## 4. Clases modelo en Android

Necesitas clases Java que representen los datos que llegan del servidor.
Tienen que tener los mismos nombres de campo que devuelve la API.

### Observacion.java (modelo Android)

```java
public class Observacion {
    private Long id;
    private String titulo;
    private String descripcion;
    private String estadoObservacion;
    private String nombreTradicional;
    private String identificacionPropuesta;
    private double latitud;
    private double longitud;
    private String direccionTxt;
    private String nombreZona;
    private String fechaObservacion;
    private String creadoEn;
    private TipoObservacion tipoObservacion;
    private Usuario usuario;
    private Especie especie; // puede ser null

    // Getters y setters de todos los campos
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getEstadoObservacion() { return estadoObservacion; }
    public TipoObservacion getTipoObservacion() { return tipoObservacion; }
    public Usuario getUsuario() { return usuario; }
    public Especie getEspecie() { return especie; }
    public String getNombreZona() { return nombreZona; }
    public String getFechaObservacion() { return fechaObservacion; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    // ... añade los que necesites
}
```

### TipoObservacion.java (modelo Android)

```java
public class TipoObservacion {
    private Integer id;
    private String nombre;
    private String descripcion;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
```

### Usuario.java (modelo Android)

```java
public class Usuario {
    private Long id;
    private String nick;
    private String nombre;
    private String apellidos;
    private String avatarUrl;

    public Long getId() { return id; }
    public String getNick() { return nick; }
    public String getNombre() { return nombre; }
    public String getAvatarUrl() { return avatarUrl; }
}
```

### Especie.java (modelo Android)

```java
public class Especie {
    private Long id;
    private String nombreCientifico;
    private String nombreComun;
    private String familia;

    public Long getId() { return id; }
    public String getNombreCientifico() { return nombreCientifico; }
    public String getNombreComun() { return nombreComun; }
}
```

---

## 5. Interfaz de la API

Crea una interfaz `ApiService.java` donde defines todos los endpoints que vas a usar.

```java
public interface ApiService {

    // ── TIPOS DE OBSERVACIÓN ──────────────────────────────────────────
    // GET /api/tipos-observacion
    @GET("api/tipos-observacion")
    Call<List<TipoObservacion>> getTipos();


    // ── OBSERVACIONES ─────────────────────────────────────────────────
    // GET /api/observaciones  → todas
    @GET("api/observaciones")
    Call<List<Observacion>> getObservaciones();

    // GET /api/observaciones?tipo=1  → filtrar por tipo
    @GET("api/observaciones")
    Call<List<Observacion>> getObservacionesPorTipo(@Query("tipo") Long idTipo);

    // GET /api/observaciones?usuario=1  → observaciones de un usuario
    @GET("api/observaciones")
    Call<List<Observacion>> getObservacionesPorUsuario(@Query("usuario") Long idUsuario);

    // GET /api/observaciones?estado_observacion=ABIERTA  → filtrar por estado
    @GET("api/observaciones")
    Call<List<Observacion>> getObservacionesPorEstado(@Query("estado_observacion") String estadoObservacion);

    // Compatibilidad: también acepta "estado" en backend, pero usa estado_observacion en la app nueva.

    // GET /api/observaciones/{id}  → detalle de una observación
    @GET("api/observaciones/{id}")
    Call<Observacion> getObservacion(@Path("id") Long id);

    // POST /api/observaciones  → crear nueva
    @POST("api/observaciones")
    Call<Observacion> crearObservacion(@Body Observacion observacion);

    // PUT /api/observaciones/{id}  → editar
    @PUT("api/observaciones/{id}")
    Call<Observacion> editarObservacion(@Path("id") Long id, @Body Observacion observacion);

    // DELETE /api/observaciones/{id}  → borrar
    @DELETE("api/observaciones/{id}")
    Call<Void> borrarObservacion(@Path("id") Long id);


    // ── IMÁGENES ──────────────────────────────────────────────────────
    // GET /api/observaciones/{id}/imagenes
    @GET("api/observaciones/{id}/imagenes")
    Call<List<Imagen>> getImagenes(@Path("id") Long idObservacion);

    // POST /api/observaciones/{id}/imagenes
    @POST("api/observaciones/{id}/imagenes")
    Call<Imagen> subirImagen(@Path("id") Long idObservacion, @Body Imagen imagen);


    // ── COMENTARIOS ───────────────────────────────────────────────────
    // GET /api/observaciones/{id}/comentarios
    @GET("api/observaciones/{id}/comentarios")
    Call<List<Comentario>> getComentarios(@Path("id") Long idObservacion);

    // POST /api/observaciones/{id}/comentarios
    @POST("api/observaciones/{id}/comentarios")
    Call<Comentario> crearComentario(@Path("id") Long idObservacion, @Body Comentario comentario);


    // ── LIKES ─────────────────────────────────────────────────────────
    // GET /api/observaciones/{id}/likes/count
    @GET("api/observaciones/{id}/likes/count")
    Call<Map<String, Long>> getLikes(@Path("id") Long idObservacion);

    // GET /api/observaciones/{id}/likes/existe?idUsuario=1
    @GET("api/observaciones/{id}/likes/existe")
    Call<Map<String, Boolean>> existeLike(@Path("id") Long idObservacion, @Query("idUsuario") Long idUsuario);

    // POST /api/observaciones/{id}/likes?idUsuario=1
    @POST("api/observaciones/{id}/likes")
    Call<Void> darLike(@Path("id") Long idObservacion, @Query("idUsuario") Long idUsuario);

    // DELETE /api/observaciones/{id}/likes?idUsuario=1
    @DELETE("api/observaciones/{id}/likes")
    Call<Void> quitarLike(@Path("id") Long idObservacion, @Query("idUsuario") Long idUsuario);


    // ── USUARIOS ──────────────────────────────────────────────────────
    // POST /api/auth/register
    @POST("api/auth/register")
    Call<Usuario> register(@Body Usuario usuario);

    // POST /api/auth/login
    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> credenciales);

    // GET /api/usuarios/{id}
    @GET("api/usuarios/{id}")
    Call<Usuario> getPerfil(@Path("id") Long id);


    // ── ESPECIES ──────────────────────────────────────────────────────
    // GET /api/especies
    @GET("api/especies")
    Call<List<Especie>> getEspecies();

    // GET /api/especies/{id}
    @GET("api/especies/{id}")
    Call<Especie> getEspecie(@Path("id") Long id);
}
```

---

## 6. Cómo hacer una llamada real

Las llamadas a la API **no se pueden hacer en el hilo principal** de Android
(el que maneja la pantalla). Si lo haces, la app se congela o peta.

Retrofit lo resuelve con callbacks: le dices "cuando termines, ejecuta esto".

### Ejemplo: cargar lista de observaciones

```java
// En tu Activity o Fragment:

ApiService api = RetrofitClient.getClient().create(ApiService.class);

api.getObservaciones().enqueue(new Callback<List<Observacion>>() {

    @Override
    public void onResponse(Call<List<Observacion>> call, Response<List<Observacion>> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<Observacion> lista = response.body();
            // Aquí ya tienes los datos, actualiza el RecyclerView
            adaptador.setLista(lista);
        } else {
            // El servidor respondió pero con error (ej: 404, 500)
            Toast.makeText(context, "Error del servidor", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call<List<Observacion>> call, Throwable t) {
        // No se pudo conectar (sin internet, servidor apagado...)
        Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show();
    }
});
```

### Ejemplo: crear una observación nueva

```java
// Construyes el objeto con los datos del formulario
Observacion nueva = new Observacion();
nueva.setTitulo(editTitulo.getText().toString());
nueva.setDescripcion(editDescripcion.getText().toString());
nueva.setLatitud(latitudActual);
nueva.setLongitud(longitudActual);

// El tipo y el usuario los tienes que setear como objetos con id
TipoObservacion tipo = new TipoObservacion();
tipo.setId(idTipoSeleccionado);
nueva.setTipoObservacion(tipo);

// Opcional: si no lo mandas, el backend suele poner ABIERTA por defecto.
nueva.setEstadoObservacion("ABIERTA");

Usuario usuario = new Usuario();
usuario.setId(idUsuarioLogueado);
nueva.setUsuario(usuario);

// Llamada a la API
api.crearObservacion(nueva).enqueue(new Callback<Observacion>() {

    @Override
    public void onResponse(Call<Observacion> call, Response<Observacion> response) {
        if (response.isSuccessful()) {
            Observacion creada = response.body();
            Long idNueva = creada.getId(); // guarda este id para subir imágenes después
            Toast.makeText(context, "Observación creada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error al crear", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call<Observacion> call, Throwable t) {
        Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show();
    }
});
```

### Ejemplo: login

```java
Map<String, String> credenciales = new HashMap<>();
credenciales.put("nick", editNick.getText().toString());
credenciales.put("password", editPassword.getText().toString());

api.login(credenciales).enqueue(new Callback<Map<String, Object>>() {

    @Override
    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
        if (response.isSuccessful() && response.body() != null) {
            Map<String, Object> datos = response.body();
            // Guardamos el id del usuario para usarlo en otras llamadas
            double idDouble = (double) datos.get("id"); // Gson devuelve números como Double
            Long idUsuario = (long) idDouble;
            String nick = (String) datos.get("nick");

            // Guarda el id en SharedPreferences para no perderlo al cambiar de pantalla
            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
            prefs.edit().putLong("idUsuario", idUsuario).apply();
            prefs.edit().putString("nick", nick).apply();

            // Ir a la pantalla principal
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else if (response.code() == 401) {
            Toast.makeText(context, "Nick o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
        Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show();
    }
});
```

### Ejemplo: dar like

```java
Long idObservacion = 1L;  // el id de la observación que estás viendo
Long idUsuario = ...; // el que tienes guardado en SharedPreferences

api.darLike(idObservacion, idUsuario).enqueue(new Callback<Void>() {

    @Override
    public void onResponse(Call<Void> call, Response<Void> response) {
        if (response.isSuccessful()) {
            // 201 Created → like dado, pinta el corazón relleno
            btnLike.setImageResource(R.drawable.ic_corazon_relleno);
        } else if (response.code() == 409) {
            // 409 Conflict → ya había dado like antes
        }
    }

    @Override
    public void onFailure(Call<Void> call, Throwable t) {
        Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show();
    }
});
```

---

## 7. Códigos de respuesta HTTP que devuelve la API

Es importante saber qué significa cada código para mostrar el mensaje correcto al usuario.

| Código | Significado                              | Cuándo ocurre en Huerteando               |
|--------|------------------------------------------|-------------------------------------------|
| 200    | OK, todo bien                            | GET con datos                             |
| 201    | Creado correctamente                     | POST exitoso                              |
| 204    | Borrado correctamente (sin contenido)    | DELETE exitoso                            |
| 400    | Petición incorrecta                      | Faltan campos obligatorios                |
| 401    | No autorizado                            | Login con contraseña incorrecta           |
| 403    | Prohibido                                | Cuenta desactivada                        |
| 404    | No encontrado                            | Id que no existe                          |
| 409    | Conflicto                                | Nick ya en uso, like ya dado              |
| 500    | Error interno del servidor               | Bug en el backend                         |

---

## 8. Guardar la sesión del usuario

Cuando el usuario hace login, necesitas guardar su id para usarlo en otras llamadas
(crear observación, dar like, comentar...).

Usa `SharedPreferences`, que es como un mini almacén de datos en Android:

```java
// Guardar al hacer login
SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
prefs.edit().putLong("idUsuario", idUsuario).apply();
prefs.edit().putString("nick", nick).apply();

// Leer en cualquier otra Activity
SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
Long idUsuario = prefs.getLong("idUsuario", -1L); // -1 si no hay sesión
String nick = prefs.getString("nick", null);

// Comprobar si hay sesión activa
boolean hayLogin = idUsuario != -1L;

// Borrar al cerrar sesión
prefs.edit().clear().apply();
```

---

## 9. Flujo completo de crear una observación con imagen

Este es el flujo más complejo de la app, porque primero hay que crear la observación
y luego subir la imagen con el id que devuelve el servidor.

```
1. Usuario rellena el formulario
2. POST /api/observaciones  → servidor devuelve la observación con su id
3. Con ese id: POST /api/observaciones/{id}/imagenes
4. Mostrar confirmación al usuario
```

```java
// Paso 1: crear la observación
api.crearObservacion(observacion).enqueue(new Callback<Observacion>() {
    @Override
    public void onResponse(Call<Observacion> call, Response<Observacion> response) {
        if (response.isSuccessful()) {
            Long idCreada = response.body().getId();

            // Paso 2: subir la imagen con el id que acabamos de recibir
            Imagen imagen = new Imagen();
            imagen.setUrlArchivo(urlDeLaFoto);
            imagen.setTitulo("Foto principal");

            api.subirImagen(idCreada, imagen).enqueue(new Callback<Imagen>() {
                @Override
                public void onResponse(Call<Imagen> call, Response<Imagen> response) {
                    Toast.makeText(context, "Observación e imagen guardadas", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<Imagen> call, Throwable t) { }
            });
        }
    }
    @Override
    public void onFailure(Call<Observacion> call, Throwable t) { }
});
```

---

## 10. Errores comunes y cómo solucionarlos

**La app no conecta con el servidor**
- Comprueba que el servidor está arrancado en IntelliJ
- En el emulador usa `10.0.2.2` en vez de `localhost`
- Comprueba que tienes `<uses-permission android:name="android.permission.INTERNET" />` en el manifest

**`onFailure` siempre se ejecuta**
- El servidor no está arrancado
- La URL base está mal
- No hay internet en el emulador (menú del emulador → Extended controls → Cellular)

**Los campos llegan como `null`**
- El nombre del campo en tu clase Java no coincide con el que devuelve la API
- Comprueba el JSON que devuelve el servidor abriendo la URL en el navegador: `http://10.0.2.2:8080/api/observaciones`

**`NetworkOnMainThreadException`**
- Estás haciendo la llamada sin `.enqueue()`, en el hilo principal
- Usa siempre `.enqueue(new Callback...)` como en los ejemplos

**El login devuelve el id como `Double` en vez de `Long`**
- Gson convierte todos los números a `Double` por defecto en un `Map<String, Object>`
- Conviértelo así: `Long id = ((Double) datos.get("id")).longValue();`
