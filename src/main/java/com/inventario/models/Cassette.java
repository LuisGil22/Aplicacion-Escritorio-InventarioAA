package com.inventario.models;

import java.util.Objects;

public class Cassette {
    private String numCassette;
    private String numSecuencia;
    private String estado;
    private String planta;
    private String nombre;
    private Double potenciaCalor;
    private Double potenciaFrio;
    private String marcaModelo;
    private String numSerieCas;
    private String condensadora;
    private String localizacionCondensadora;
    private String gas;
    private String fechaInstalacion;
    private String fechaRevision;
    private String averia;
    private String foto;

    public Cassette(String numCassette, String numSecuencia, String estado, String planta, String nombre, Double potenciaCalor, Double potenciaFrio, String marcaModelo, String numSerieCas, String condensadora, String localizacionCondensadora, String gas, String fechaInstalacion, String fechaRevision, String averia, String foto) {
        this.numCassette = numCassette;
        this.numSecuencia = numSecuencia;
        this.estado = estado;
        this.planta = planta;
        this.nombre = nombre;
        this.potenciaCalor = potenciaCalor;
        this.potenciaFrio = potenciaFrio;
        this.marcaModelo = marcaModelo;
        this.numSerieCas = numSerieCas;
        this.condensadora = condensadora;
        this.localizacionCondensadora = localizacionCondensadora;
        this.gas = gas;
        this.fechaInstalacion = fechaInstalacion;
        this.fechaRevision= fechaRevision;
        this.averia = averia;
        this.foto = foto;
    }

    public String getNumCassette() {
        return numCassette;
    }

    public void setNumCassette(String numCassette) {
        this.numCassette = numCassette;
    }

    public String getNumSecuencia() {
        return numSecuencia;
    }

    public void setNumSecuencia(String numSecuencia) {
        this.numSecuencia = numSecuencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPotenciaCalor() {
        return potenciaCalor;
    }

    public void setPotenciaCalor(Double potenciaCalor) {
        this.potenciaCalor = potenciaCalor;
    }

    public Double getPotenciaFrio() {
        return potenciaFrio;
    }

    public void setPotenciaFrio(Double potenciaFrio) {
        this.potenciaFrio = potenciaFrio;
    }

    public String getMarcaModelo() {
        return marcaModelo;
    }

    public void setMarcaModelo(String marcaModelo) {
        this.marcaModelo = marcaModelo;
    }

    public String getNumSerieCas() {
        return numSerieCas;
    }

    public void setNumSerieCas(String numSerieCas) {
        this.numSerieCas = numSerieCas;
    }

    public String getCondensadora() {
        return condensadora;
    }

    public void setCondensadora(String condensadora) {
        this.condensadora = condensadora;
    }

    public String getLocalizacionCondensadora() {
        return localizacionCondensadora;
    }

    public void setLocalizacionCondensadora(String localizacionCondensadora) {
        this.localizacionCondensadora = localizacionCondensadora;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(String fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getAveria() {
        return averia;
    }

    public void setAveria(String averia) {
        this.averia = averia;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) return false;
        Cassette cassette = (Cassette) o;
        return numCassette.equals(cassette.numCassette) && numSecuencia.equals(cassette.numSecuencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numCassette, numSecuencia);
    }


}
