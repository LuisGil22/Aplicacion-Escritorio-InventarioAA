package com.inventario.models;

public class Planta {

    private String planta;
    private String descripcion;

    public Planta(String planta, String descripcion){
        this.planta = planta;
        this.descripcion = descripcion;
    }

    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
