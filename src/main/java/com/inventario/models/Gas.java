package com.inventario.models;

public class Gas {
    private String gas;
    private String descripcion;

    public Gas(String gas, String descripcion){
        this.gas = gas;
        this.descripcion = descripcion;
    }

    public String getGas(){
        return gas;
    }

    public void setGas(String gas){
        this.gas = gas;
    }

    public String getDescripcion(){
        return descripcion;
    }

    public void setDescripcion(String descripcion){
        this.descripcion = descripcion;
    }

}
