package org.example.models;

public class MetricaStockBajo {
    private long id;
    private String nombre;
    private int stock;
    private String tipo;

    public MetricaStockBajo(long id, String nombre, int stock, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.tipo = tipo;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getStock() { return stock; }
    public String getTipo() { return tipo; }
}