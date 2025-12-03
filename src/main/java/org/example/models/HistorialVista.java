package org.example.models;

import java.time.LocalDateTime;

public class HistorialVista {

    private Integer historial_id;
    private Long usuario_id;
    private Integer producto_id;
    private Integer animal_id;
    private LocalDateTime fecha_vista;

    public HistorialVista() {}

    public HistorialVista(Integer producto_id, Integer animal_id) {
        this.producto_id = producto_id;
        this.animal_id = animal_id;
    }

    public HistorialVista(Long usuario_id, Integer producto_id) {
        this.usuario_id = usuario_id;
        this.producto_id = producto_id;
        this.animal_id = null;
    }

    public HistorialVista(Long usuario_id, Integer animal_id, boolean isAnimal) {
        this.usuario_id = usuario_id;
        this.animal_id = animal_id;
        this.producto_id = null;
    }

    public Integer getHistorial_id() {
        return historial_id;
    }
    public void setHistorial_id(Integer historial_id) {
        this.historial_id = historial_id;
    }

    public Long getUsuario_id() {
        return usuario_id;
    }
    public void setUsuario_id(Long usuario_id) {
        this.usuario_id = usuario_id;
    }

    public Integer getProducto_id() {
        return producto_id;
    }
    public void setProducto_id(Integer producto_id) {
        this.producto_id = producto_id;
    }

    public Integer getAnimal_id() {
        return animal_id;
    }
    public void setAnimal_id(Integer animal_id) {
        this.animal_id = animal_id;
    }

    public LocalDateTime getFecha_vista() {
        return fecha_vista;
    }
    public void setFecha_vista(LocalDateTime fecha_vista) {
        this.fecha_vista = fecha_vista;
    }
}