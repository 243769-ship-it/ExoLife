package org.example.models;


public class Usuario {

    private Long id;

    private String nombre;

    private String email;

    private String passwordHash;

    private Integer rol_id;

    public Usuario() {}

    public Usuario(Long id, String nombre, String email, String passwordHash, Integer rol_id) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol_id = rol_id;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() {
        return rol_id != null ? String.valueOf(rol_id) : null;}

    public void setRol(String rol) {
        if (rol != null && !rol.isEmpty()) {
            try {
                this.rol_id = Integer.parseInt(rol);
            } catch (NumberFormatException e) {
                this.rol_id = null;
            }
        }
    }

    public Integer getRol_id() { return rol_id; }
    public void setRol_id(Integer rol_id) { this.rol_id = rol_id; }
}