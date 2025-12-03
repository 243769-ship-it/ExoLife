package org.example.models;

import java.math.BigDecimal;
import java.math.RoundingMode; // Importación para RoundingMode
import java.util.HashMap;
import java.util.Map;

public class Animal {

    private Integer animal_id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String tamano;
    private Integer vistas_contador;
    private String imagen_url;

    private static final Map<String, BigDecimal> MODIFICADORES_PRECIO = new HashMap<>();
    static {
        MODIFICADORES_PRECIO.put("Pequeño", BigDecimal.valueOf(1.00));
        MODIFICADORES_PRECIO.put("Mediano", BigDecimal.valueOf(1.10));
        MODIFICADORES_PRECIO.put("Grande", BigDecimal.valueOf(1.25));
    }

    public Animal() {}

    public Animal(Integer animal_id, String nombre, String descripcion, BigDecimal precio, Integer stock, String tamano, Integer vistas_contador, String imagen_url) {
        this.animal_id = animal_id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.tamano = tamano;
        this.vistas_contador = vistas_contador;
        this.imagen_url = imagen_url;
    }

    public BigDecimal getPrecioPorTamano(String tamano) {
        BigDecimal multiplicador = MODIFICADORES_PRECIO.getOrDefault(tamano, BigDecimal.valueOf(1.00));
        return this.precio.multiply(multiplicador).setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, BigDecimal> getOpcionesDePrecio() {
        Map<String, BigDecimal> opciones = new HashMap<>();
        for (String key : MODIFICADORES_PRECIO.keySet()) {
            opciones.put(key, getPrecioPorTamano(key));
        }
        return opciones;
    }

    public Integer getAnimal_id() {
        return animal_id;
    }

    public void setAnimal_id(Integer animal_id) {
        this.animal_id = animal_id;
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

    public String getTamano() {
        return tamano;
    }

    public void setTamano(String tamano) {
        this.tamano = tamano;
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