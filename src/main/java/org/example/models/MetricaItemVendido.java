package org.example.models;

public class MetricaItemVendido {
    private long id;
    private String nombre;
    private int totalVendido;
    private String tipo;

    public MetricaItemVendido(long id, String nombre, int totalVendido, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.totalVendido = totalVendido;
        this.tipo = tipo;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getTotalVendido() { return totalVendido; }
    public String getTipo() { return tipo; }
}