package com.huerteando.huerteandoapp.api;

import com.huerteando.huerteandoapp.controller.ComentarioController;
import com.huerteando.huerteandoapp.controller.EspecieController;
import com.huerteando.huerteandoapp.controller.ImagenController;
import com.huerteando.huerteandoapp.controller.MeGustaController;
import com.huerteando.huerteandoapp.controller.ObservacionController;
import com.huerteando.huerteandoapp.controller.TipoObservacionController;
import com.huerteando.huerteandoapp.controller.UsuarioController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiBasicIntegrationTest {

    @Test
    void usuarioControllerExponeRutasBasicasDeAuthYPerfil() throws Exception {
        assertMethodMapping(UsuarioController.class, "register", PostMapping.class, "/api/auth/register");
        assertMethodMapping(UsuarioController.class, "login", PostMapping.class, "/api/auth/login");
        assertMethodMapping(UsuarioController.class, "perfil", GetMapping.class, "/api/usuarios/{id}");
    }

    @Test
    void observacionControllerExponeCRUDPrincipal() throws Exception {
        assertClassMapping(ObservacionController.class, "/api/observaciones");
        assertMethodHasAnyPath(ObservacionController.class, "listar", GetMapping.class, "", "/");
        assertMethodMapping(ObservacionController.class, "obtenerPorId", GetMapping.class, "/{id}");
        assertMethodHasAnnotation(ObservacionController.class, "crear", PostMapping.class);
        assertMethodMapping(ObservacionController.class, "actualizar", PutMapping.class, "/{id}");
        assertMethodMapping(ObservacionController.class, "eliminar", DeleteMapping.class, "/{id}");
    }

    @Test
    void controladoresAnidadosMantienenLaRutaEsperada() throws Exception {
        assertClassMapping(TipoObservacionController.class, "/api/tipos-observacion");
        assertClassMapping(ComentarioController.class, "/api/observaciones/{idObservacion}/comentarios");
        assertClassMapping(MeGustaController.class, "/api/observaciones/{idObservacion}/megustas");
        assertClassMapping(ImagenController.class, "/api/observaciones/{idObservacion}/imagenes");
        assertClassMapping(EspecieController.class, "/api/especies");
    }

    @Test
    void meGustaControllerDevuelveElContadorEsperadoEnElContrato() throws Exception {
        Method method = MeGustaController.class.getDeclaredMethod("contar", Long.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(new String[]{"/count"}, mapping.value());
    }

    private static void assertClassMapping(Class<?> controllerClass, String expectedPath) {
        RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
        assertNotNull(mapping, controllerClass.getSimpleName() + " debe tener @RequestMapping");
        assertTrue(mapping.value().length > 0, controllerClass.getSimpleName() + " debe declarar una ruta base");
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertMethodMapping(Class<?> controllerClass, String methodName,
                                            Class<?> annotationType, String expectedPath) throws Exception {
        Method method = controllerClass.getDeclaredMethod(methodName, methodParameterTypes(controllerClass, methodName));
        Object annotation = method.getAnnotation((Class) annotationType);

        assertNotNull(annotation, controllerClass.getSimpleName() + "." + methodName + " debe tener " + annotationType.getSimpleName());
        String[] values = annotationValues(annotation);
        assertTrue(values.length > 0, controllerClass.getSimpleName() + "." + methodName + " debe declarar ruta");
        assertEquals(expectedPath, values[0]);
    }

    private static void assertMethodHasAnyPath(Class<?> controllerClass, String methodName,
                                               Class<?> annotationType, String... expectedPaths) throws Exception {
        Method method = controllerClass.getDeclaredMethod(methodName, methodParameterTypes(controllerClass, methodName));
        Object annotation = method.getAnnotation((Class) annotationType);

        assertNotNull(annotation, controllerClass.getSimpleName() + "." + methodName + " debe tener " + annotationType.getSimpleName());
        String[] values = annotationValues(annotation);

        boolean matched = false;
        for (String expected : expectedPaths) {
            if (values.length > 0 && expected.equals(values[0])) {
                matched = true;
                break;
            }
        }

        assertTrue(matched, controllerClass.getSimpleName() + "." + methodName + " debe aceptar una de estas rutas: " + String.join(", ", expectedPaths));
    }

    private static void assertMethodHasAnnotation(Class<?> controllerClass, String methodName,
                                                  Class<?> annotationType) throws Exception {
        Method method = controllerClass.getDeclaredMethod(methodName, methodParameterTypes(controllerClass, methodName));
        Object annotation = method.getAnnotation((Class) annotationType);
        assertNotNull(annotation, controllerClass.getSimpleName() + "." + methodName + " debe tener " + annotationType.getSimpleName());
    }

    private static Class<?>[] methodParameterTypes(Class<?> controllerClass, String methodName) throws Exception {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method.getParameterTypes();
            }
        }
        throw new NoSuchMethodException(controllerClass.getName() + "." + methodName);
    }

    private static String[] annotationValues(Object annotation) {
        if (annotation instanceof GetMapping mapping) return mapping.value();
        if (annotation instanceof PostMapping mapping) return mapping.value();
        if (annotation instanceof PutMapping mapping) return mapping.value();
        if (annotation instanceof DeleteMapping mapping) return mapping.value();
        throw new IllegalArgumentException("Anotacion no soportada: " + annotation.getClass().getName());
    }
}
