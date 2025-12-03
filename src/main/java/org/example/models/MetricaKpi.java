package org.example.models;

public class MetricaKpi {
    private long totalClientes;
    private long totalPedidosPendientes;
    private long totalProductos;
    private long totalAnimales;

    public MetricaKpi(long totalClientes, long totalPedidosPendientes, long totalProductos, long totalAnimales) {
        this.totalClientes = totalClientes;
        this.totalPedidosPendientes = totalPedidosPendientes;
        this.totalProductos = totalProductos;
        this.totalAnimales = totalAnimales;
    }

    public long getTotalClientes() { return totalClientes; }
    public long getTotalPedidosPendientes() { return totalPedidosPendientes; }
    public long getTotalProductos() { return totalProductos; }
    public long getTotalAnimales() { return totalAnimales; }
}