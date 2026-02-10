package com.inventario.models;

public class Model_Cassette {

    private String modelo;
    private String descripcion;

    public Model_Cassette(String modelo, String descripcion){
        this.modelo = modelo;
        this.descripcion = descripcion;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
