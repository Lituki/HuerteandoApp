package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/*
Tabla: usuario
Ficha base del usuario: datos, rol y estado de cuenta.
*/
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellidos")
    private String apellidos;

    @Column(name = "nick", nullable = false, unique = true)
    private String nick; // obligatorio y único

    @Column(name = "email", unique = true)
    private String email;

    @JsonIgnore // nunca se envía el hash por la API
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "avatar_url", columnDefinition = "text")
    private String avatarUrl;

    @Column(name = "rol", nullable = false)
    private String rol; // por defecto USER

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @PrePersist
    void alCrear() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
