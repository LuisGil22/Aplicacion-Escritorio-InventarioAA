package com.inventario.models;

public class Model_Cassette {

    private String modeloCas;
    private String descripcion;

    public Model_Cassette(String modeloCas, String descripcion){
        this.modeloCas = modeloCas;
        this.descripcion = descripcion;
    }

    public String getModeloCas() {
        return modeloCas;
    }

    public void setModeloCas(String modeloCas) {
        this.modeloCas = modeloCas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
