package com.inventario.models;

public class Marca {
    private String marca;
    private String descripcion;

    public Marca(String marca, String descripcion){
        this.marca = marca;
        this.descripcion = descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
