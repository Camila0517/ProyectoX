package com.giru.backend;

public class UsuarioDTO {

    private Long id;
    private String username;
    private String email;
    private Rol rol;

    public UsuarioDTO(Long id, String username, String email, Rol rol) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.rol = rol;
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public Rol getRol() { return rol; }
}
