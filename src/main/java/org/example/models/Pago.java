package org.example.models;

import java.time.LocalDateTime;

public class Pago {

    private Long pagoId;
    private Long compraId;
    private double monto;
    private String comprobante_url;
    private LocalDateTime fechaPago;
    private String estado;

    public Pago() {
        this.fechaPago = LocalDateTime.now();
        this.estado = "Pendiente";
    }

    public Pago(Long compraId, double monto, String comprobante_url) {
        this.compraId = compraId;
        this.monto = monto;
        this.comprobante_url = comprobante_url;
        this.fechaPago = LocalDateTime.now();
        this.estado = "Pendiente";
    }

    public Long getPagoId() { return pagoId; }
    public void setPagoId(Long pagoId) { this.pagoId = pagoId; }

    public Long getCompraId() { return compraId; }
    public void setCompraId(Long compraId) { this.compraId = compraId; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getComprobante_url() { return comprobante_url; }
    public void setComprobante_url(String comprobante_url) { this.comprobante_url = comprobante_url; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}