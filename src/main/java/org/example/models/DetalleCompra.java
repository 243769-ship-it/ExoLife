package org.example.models;

public class DetalleCompra {

    private Long detalleId;
    private Long compraId;
    private Long itemId;
    private int cantidad;
    private double precioUnitario;

    public DetalleCompra() {
    }

    public DetalleCompra(Long compraId, Long itemId, int cantidad, double precioUnitario) {
        this.compraId = compraId;
        this.itemId = itemId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getDetalleId() { return detalleId; }
    public void setDetalleId(Long detalleId) { this.detalleId = detalleId; }

    public Long getCompraId() { return compraId; }
    public void setCompraId(Long compraId) { this.compraId = compraId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
}