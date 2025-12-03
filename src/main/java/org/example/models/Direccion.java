package org.example.models;

public class Direccion {
    private Long direccion_ID;
    private Long usuario_id;
    private String calle;
    private String numero;
    private String ciudad;
    private String estado;
    private String cp;

    public Direccion() {}

    public Long getDireccion_ID() { return direccion_ID; }
    public Long getUsuario_id() { return usuario_id; }
    public String getCalle() { return calle; }
    public String getNumero() { return numero; }
    public String getCiudad() { return ciudad; }
    public String getEstado() { return estado; }
    public String getCp() { return cp; }

    public void setDireccion_ID(Long direccion_ID) { this.direccion_ID = direccion_ID; }
    public void setUsuario_id(Long usuario_id) { this.usuario_id = usuario_id; }
    public void setCalle(String calle) { this.calle = calle; }
    public void setNumero(String numero) { this.numero = numero; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setCp(String cp) { this.cp = cp; }
}