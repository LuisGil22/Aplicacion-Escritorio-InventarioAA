package com.inventario.models;

import java.util.Date;

public class Condensadora {

    private String condensadoras;
    private int numSecuencia;
    private String estado;
    private String marca;
    private String modelo;
    private long numSerieCond;
    private String loc_condensadora;
    private String gas;
    private String fechaInstalacion;
    private String fechaRevision;
    private String averia;
    private String observaciones;

    public Condensadora(String condensadoras, int numSecuencia,String estado, String marca, String modelo, long numSerieCond, String loc_condensadora, String gas, String fechaInstalacion, String fechaRevision, String averia, String observaciones) {
        this.condensadoras = condensadoras;
        this.numSecuencia = numSecuencia;
        this.estado = estado;
        this.marca = marca;
        this.modelo = modelo;
        this.numSerieCond = numSerieCond;
        this.loc_condensadora = loc_condensadora;
        this.gas = gas;
        this.fechaInstalacion = fechaInstalacion;
        this.fechaRevision = fechaRevision;
        this.averia = averia;
        this.observaciones = observaciones;
    }

    public String getCondensadoras() {
        return condensadoras;
    }

    public void setCondensadoras(String condensadoras) {
        this.condensadoras = condensadoras;
    }

    public int getNumSecuencia() {
        return numSecuencia;
    }

    public void setNumSecuencia(int numSecuencia) {
        this.numSecuencia = numSecuencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getAveria() {
        return averia;
    }

    public void setAveria(String averia) {
        this.averia = averia;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(String fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getLoc_condensadora() {
        return loc_condensadora;
    }

    public void setLoc_condensadora(String loc_condensadora) {
        this.loc_condensadora = loc_condensadora;
    }

    public long getNumSerieCond() {
        return numSerieCond;
    }

    public void setNumSerieCond(long numSerieCond) {
        this.numSerieCond = numSerieCond;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }
}
