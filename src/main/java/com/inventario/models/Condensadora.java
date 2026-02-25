package com.inventario.models;

import java.util.Date;

public class Condensadora {

    private String condensadora;
    private Integer numSecuencia;
    private String estado;
    private String marca;
    private String modelo;
    private Long numSerieCond;
    private String loc_condensadora;
    private String gas;
    private String fechaInstalacion;
    private String fechaBaja;
    private String fechaRevision;
    private String averia;
    private String observaciones;

    public Condensadora(String condensadora, Integer numSecuencia,String estado, String marca, String modelo, Long numSerieCond, String loc_condensadora, String gas, String fechaInstalacion,String fechaBaja, String fechaRevision, String averia, String observaciones) {
        this.condensadora = condensadora;
        this.numSecuencia = numSecuencia;
        this.estado = estado;
        this.marca = marca;
        this.modelo = modelo;
        this.numSerieCond = numSerieCond;
        this.loc_condensadora = loc_condensadora;
        this.gas = gas;
        this.fechaInstalacion = fechaInstalacion;
        this.fechaBaja = fechaBaja;
        this.fechaRevision = fechaRevision;
        this.averia = averia;
        this.observaciones = observaciones;
    }

    public String getCondensadora() {
        return condensadora;
    }

    public void setCondensadora(String condensadora) {
        this.condensadora = condensadora;
    }

    public Integer getNumSecuencia() {
        return numSecuencia;
    }

    public void setNumSecuencia(Integer numSecuencia) {
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

    public Long getNumSerieCond() {
        return numSerieCond;
    }

    public void setNumSerieCond(Long numSerieCond) {
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

    public String getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(String fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
}
