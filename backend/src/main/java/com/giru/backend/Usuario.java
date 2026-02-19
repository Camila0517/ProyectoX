package com.giru.backend;

import jakarta.persistence.*;
import jakarta.persistence.Table;


@Entity
@Table(name = "usuarios")
public class Usuario {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private String username;
    @Column(unique = true)
    private String email;
    private String password;

    public Usuario() {}

    public Usuario(String username, String email, String password, Rol rol) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public Long getId() { return id; }

    public Rol getRol() { return rol; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public void setId(Long id) { this.id = id; }

    public void setRol(Rol rol) { this.rol = rol; }

    public void setUsername(String username) { this.username = username; }

    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }
}
