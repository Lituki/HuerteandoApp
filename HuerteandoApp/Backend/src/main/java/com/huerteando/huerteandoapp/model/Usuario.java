package com.huerteando.huerteandoapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
Tabla: usuario
Ficha base del usuario: datos, rol y estado de cuenta.
*/
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id; // id interno

    @Column(name = "nombre", nullable = false)
    private String nombre; // dato obligatorio

    @Column(name = "apellidos")
    private String apellidos; // opcional

    @Column(name = "nick", nullable = false, unique = true)
    private String nick; // obligatorio y unico

    @Column(name = "email", unique = true)
    private String email; // opcional, pero no se puede repetir

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // solo hash, nunca password en claro

    @Column(name = "avatar_url")
    private String avatarUrl; // enlace del avatar

    @Column(name = "rol", nullable = false)
    private String rol; // por defecto suele ser USER

    @Column(name = "activo", nullable = false)
    private Boolean activo; // activo/inactivo

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro; // fecha de alta

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso; // ultimo login, si existe

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getNick() { return nick; }
    public void setNick(String nick) { this.nick = nick; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}
