package org.example.models;

import java.math.BigDecimal;

public class Producto {


    private Integer producto_id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Integer categoria_producto_id;
    private Integer vistas_contador;
    private String imagen_url;

    public Producto() {}

    public Producto(Integer producto_id, String nombre, String descripcion, BigDecimal precio, Integer stock, Integer categoria_producto_id, Integer vistas_contador, String imagen_url) {
        this.producto_id = producto_id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria_producto_id = categoria_producto_id;
        this.vistas_contador = vistas_contador;
        this.imagen_url = imagen_url;
    }

    public Integer getProducto_id() {
        return producto_id;
    }

    public void setProducto_id(Integer producto_id) {
        this.producto_id = producto_id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getCategoria_producto_id() {
        return categoria_producto_id;
    }

    public void setCategoria_producto_id(Integer categoria_producto_id) {
        this.categoria_producto_id = categoria_producto_id;
    }

    public Integer getVistas_contador() {
        return vistas_contador;
    }

    public void setVistas_contador(Integer vistas_contador) {
        this.vistas_contador = vistas_contador;
    }

    public String getImagen_url() {
        return imagen_url;
    }

    public void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }
}