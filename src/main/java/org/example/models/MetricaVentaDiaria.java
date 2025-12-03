package org.example.models;

public class MetricaVentaDiaria {
    private String fecha;
    private double totalVendido;

    public  MetricaVentaDiaria(String fecha, double totalVendido) {
        this.fecha = fecha;
        this.totalVendido = totalVendido;
    }
    public String getFecha() { return fecha; }
    public double getTotalVendido() { return totalVendido; }
}