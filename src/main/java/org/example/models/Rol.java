package org.example.models;

public class Rol {

    private int rol_id;

    private String nombre;

    public Rol() {}

    public Rol(int rol_id, String nombre) {
        this.rol_id = rol_id;
        this.nombre = nombre;
    }

    public int getRol_id() { return rol_id; }
    public void setRol_id(int rol_id) { this.rol_id = rol_id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}