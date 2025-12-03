package org.example.models;

public class DetalleCompraItem {
    private Long productoId;
    private Long animalId;
    private int cantidad;

    public DetalleCompraItem(Long productoId, Long animalId, int cantidad) {
        this.productoId = productoId;
        this.animalId = animalId;
        this.cantidad = cantidad;
    }

    public Long getProductoId() { return productoId; }
    public Long getAnimalId() { return animalId; }
    public int getCantidad() { return cantidad; }
}