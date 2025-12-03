package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private Long carritoId;
    private Long usuarioId;
    private List<CarritoItem> items = new ArrayList<>();

    public Long getCarritoId() { return carritoId; }
    public void setCarritoId(Long carritoId) { this.carritoId = carritoId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public List<CarritoItem> getItems() { return items; }
    public void setItems(List<CarritoItem> items) { this.items = items; }
}
