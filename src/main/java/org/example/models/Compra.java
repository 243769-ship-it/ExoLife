package org.example.models;

import java.time.LocalDateTime;

public class Compra {
    private Long compraId;
    private Long usuarioId;
    private Long direccionId;
    private String estado;
    private Double total;
    private LocalDateTime fecha;
    private String guiaEnvioNumero;
    private LocalDateTime fechaEntregaConfirmada;
    private Long empaquetador_id;
    private String comprobanteUrl;
    private String comprobantePublicId;
    // NUEVO CAMPO
    private String evidenciaEntregaUrl;

    public Compra() {
    }

    // --- Getters y Setters ---

    public Long getCompraId() {
        return compraId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getDireccionId() {
        return direccionId;
    }

    public void setDireccionId(Long direccionId) {
        this.direccionId = direccionId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getGuiaEnvioNumero() {
        return guiaEnvioNumero;
    }

    public void setGuiaEnvioNumero(String guiaEnvioNumero) {
        this.guiaEnvioNumero = guiaEnvioNumero;
    }

    public LocalDateTime getFechaEntregaConfirmada() {
        return fechaEntregaConfirmada;
    }

    public void setFechaEntregaConfirmada(LocalDateTime fechaEntregaConfirmada) {
        this.fechaEntregaConfirmada = fechaEntregaConfirmada;
    }

    public Long getEmpaquetador_id() {
        return empaquetador_id;
    }

    public void setEmpaquetador_id(Long empaquetador_id) {
        this.empaquetador_id = empaquetador_id;
    }

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }

    public String getComprobantePublicId() {
        return comprobantePublicId;
    }

    public void setComprobantePublicId(String comprobantePublicId) {
        this.comprobantePublicId = comprobantePublicId;
    }

    public String getEvidenciaEntregaUrl() {
        return evidenciaEntregaUrl;
    }

    public void setEvidenciaEntregaUrl(String evidenciaEntregaUrl) {
        this.evidenciaEntregaUrl = evidenciaEntregaUrl;
    }
}