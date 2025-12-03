package org.example.models;

public class MetricaItemVisto {
    private String nombre;
    private int vistas;

    public MetricaItemVisto(String nombre, int vistas) {
        this.nombre = nombre;
        this.vistas = vistas;
    }

    public String getNombre() { return nombre; }
    public int getVistas() { return vistas; }
}