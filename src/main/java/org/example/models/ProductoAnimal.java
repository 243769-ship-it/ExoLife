package org.example.models;

public class ProductoAnimal {

    private Integer producto_animal_id;
    private Integer producto_id;
    private Integer animal_id;

    public ProductoAnimal() {}

    public ProductoAnimal(Integer producto_id, Integer animal_id) {
        this.producto_id = producto_id;
        this.animal_id = animal_id;
    }

    public ProductoAnimal(Integer producto_animal_id, Integer producto_id, Integer animal_id) {
        this.producto_animal_id = producto_animal_id;
        this.producto_id = producto_id;
        this.animal_id = animal_id;
    }

    public Integer getProducto_animal_id() {
        return producto_animal_id;
    }

    public void setProducto_animal_id(Integer producto_animal_id) {
        this.producto_animal_id = producto_animal_id;
    }

    public Integer getProducto_id() {
        return producto_id;
    }

    public void setProducto_id(Integer producto_id) {
        this.producto_id = producto_id;
    }

    public Integer getAnimal_id() {
        return animal_id;
    }

    public void setAnimal_id(Integer animal_id) {
        this.animal_id = animal_id;
    }
}