package com.huerteando.huerteandoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configuración de seguridad de la API.
 *
 * Define qué rutas son públicas (sin token) y cuáles son privadas (requieren token JWT).
 * También desactiva CSRF (innecesario en APIs REST), elimina las sesiones (la API es
 * stateless) y delega la validación del JWT en Spring, que descarga las claves públicas
 * de Supabase automáticamente desde la URL configurada en application-postgres.properties.
 */

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
                    "/api/auth/register",
                    "/api/auth/disponibilidad"
                ).permitAll()

                // RUTAS PRIVADAS — cualquier otra petición requiere token válido
                .anyRequest().authenticated()
            )

            // Activar validación JWT con la configuración de application.properties
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
