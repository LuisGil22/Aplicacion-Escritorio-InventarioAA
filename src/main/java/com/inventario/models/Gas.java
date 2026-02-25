package com.inventario.models;

public class Gas {
    private String gas;
    private Double pcg_pca;
    private String fechaCaducidad;
    private String fechaRevision;
    private String observaciones;

    public Gas(String gas, Double pcg_pca, String fechaCaducidad, String fechaRevision, String observaciones) {
        this.gas = gas;
        this.pcg_pca = pcg_pca;
        this.fechaCaducidad = fechaCaducidad;
        this.fechaRevision = fechaRevision;
        this.observaciones = observaciones;
    }

    public String getGas(){
        return gas;
    }

    public void setGas(String gas){
        this.gas = gas;
    }

    public Double getPcg_pca() {
        return pcg_pca;
    }

    public void setPcg_pca(Double pcg_pca) {
        this.pcg_pca = pcg_pca;
    }

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(String fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
